/*
 * Copyright (C) 2018 Miquel Sas
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package app.mlt.plaf.statistics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;

import com.mlt.db.Condition;
import com.mlt.db.Criteria;
import com.mlt.db.Field;
import com.mlt.db.Order;
import com.mlt.db.Persistor;
import com.mlt.db.Record;
import com.mlt.db.RecordIterator;
import com.mlt.db.Table;
import com.mlt.db.Value;
import com.mlt.desktop.Option;
import com.mlt.mkt.data.Data;
import com.mlt.mkt.data.Instrument;
import com.mlt.mkt.data.OHLC;
import com.mlt.mkt.data.Period;
import com.mlt.util.FixedSizeList;
import com.mlt.util.Numbers;

import app.mlt.plaf.DB;

/**
 * Gearate source and raw values.
 *
 * @author Miquel Sas
 */
public class TaskAveragesRaw extends TaskAverages {

	private Persistor persistorTicker;
	private Persistor persistorSources;
	private Persistor persistorRaw;

	/**
	 * @param stats The statistics averages.
	 */
	public TaskAveragesRaw(StatisticsAverages stats) {
		super(stats);
		setId("averages-raw");
		setTitle(stats.getLabel() + " - Calculate raw values");

		Instrument instrument = stats.getInstrument();
		Period period = stats.getPeriod();
		persistorTicker = DB.persistor_ticker(instrument, period);
		persistorSources = stats.getTableSources().getPersistor();
		persistorRaw = stats.getTableRaw().getPersistor();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected long calculateTotalWork() throws Throwable {
		setTotalWork(persistorTicker.count(getCriteriaTicker()));
		return getTotalWork();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void compute() throws Throwable {

		/* Query option. */
		Option option = queryOption();
		if (option.equals("CANCEL")) {
			throw new Exception("Calculation cancelled by user.");
		}

		/* If start from begining, rebuild all tables. */
		if (option.equals("START")) {
			List<Table> tables = stats.getTables();
			for (Table table : tables) {
				if (DB.ddl().existsTable(table)) {
					DB.ddl().dropTable(table);
				}
				DB.ddl().buildTable(table);
			}
		}

		/* Count and retrieve pending total work. */
		calculateTotalWork();
		long totalWork = getTotalWork();
		long workDone = 0;

		/* Nothing pending to calculate. */
		if (totalWork <= 0) {
			return;
		}

		/* Pool. */
		List<Callable<Void>> concurrents = new ArrayList<>();
		int poolSize = 50;
		int maxConcurrent = 1000;
		ForkJoinPool pool = new ForkJoinPool(poolSize);
		List<Average> averages = stats.getParameters().averages;
		int maxPeriod = averages.get(averages.size() - 1).getPeriod();
		FixedSizeList<Double> avgBuffer = new FixedSizeList<>(maxPeriod);
		FixedSizeList<Record> rcBuffer = new FixedSizeList<>(maxPeriod);

		/* Iterate ticker. */
		Record rcSrcPrev = null;
		Order order = persistorTicker.getView().getMasterTable().getPrimaryKey();
		RecordIterator iter = persistorTicker.iterator(getCriteriaTicker(), order);
		while (iter.hasNext()) {

			/* Check cancel requested. */
			if (isCancelRequested()) {
				setCancelled();
				break;
			}

			/* Retrieve record. */
			Record rcTick = iter.next();
			avgBuffer.add(rcTick.getValue(DB.FIELD_BAR_CLOSE).getDouble());
			rcBuffer.add(rcTick);

			/* Notify work done. */
			workDone++;
			StringBuilder b = new StringBuilder();
			b.append(rcTick.toString(DB.FIELD_BAR_TIME_FMT));
			b.append(", ");
			b.append(rcTick.toString(DB.FIELD_BAR_OPEN));
			b.append(", ");
			b.append(rcTick.toString(DB.FIELD_BAR_HIGH));
			b.append(", ");
			b.append(rcTick.toString(DB.FIELD_BAR_LOW));
			b.append(", ");
			b.append(rcTick.toString(DB.FIELD_BAR_CLOSE));
			update(b.toString(), workDone, totalWork);

			/* Sources record. */
			Record rcSrc = persistorSources.getDefaultRecord();
			rcSrc.setValue(DB.FIELD_BAR_TIME, rcTick.getValue(DB.FIELD_BAR_TIME));
			rcSrc.setValue(DB.FIELD_BAR_OPEN, rcTick.getValue(DB.FIELD_BAR_OPEN));
			rcSrc.setValue(DB.FIELD_BAR_HIGH, rcTick.getValue(DB.FIELD_BAR_HIGH));
			rcSrc.setValue(DB.FIELD_BAR_LOW, rcTick.getValue(DB.FIELD_BAR_LOW));
			rcSrc.setValue(DB.FIELD_BAR_CLOSE, rcTick.getValue(DB.FIELD_BAR_CLOSE));
			if (rcSrcPrev == null) {
				rcSrcPrev = rcSrc;
			}

			/* Calculate averages. */
			for (int i = 0; i < averages.size(); i++) {
				Average avg = averages.get(i);
				double value = avg.getAverage(avgBuffer);
				String name = stats.getNameAverage(avg);
				rcSrc.setValue(name, value);
			}

			/* Raw values record. */
			Record rcRaw = persistorRaw.getDefaultRecord();
			rcRaw.setValue(DB.FIELD_BAR_TIME, rcTick.getValue(DB.FIELD_BAR_TIME));

			/* Slopes. */
			for (int i = 0; i < averages.size(); i++) {
				Average avg = averages.get(i);
				String nameAvg = stats.getNameAverage(avg);
				double prev = rcSrcPrev.getValue(nameAvg).getDouble();
				double curr = rcSrc.getValue(nameAvg).getDouble();
				double slope = 0;
				if (prev != 0) {
					slope = (curr / prev) - 1;
				}
				String nameSlope = stats.getNameSlope(avg);
				rcRaw.setValue(nameSlope, slope);
			}

			/* Spreads. */
			for (int i = 0; i < averages.size(); i++) {
				Average fast = averages.get(i);
				String nameFast = stats.getNameAverage(fast);
				double avgFast = rcSrc.getValue(nameFast).getDouble();
				for (int j = i + 1; j < averages.size(); j++) {
					Average slow = averages.get(j);
					String nameSlow = stats.getNameAverage(slow);
					double avgSlow = rcSrc.getValue(nameSlow).getDouble();
					double spread = (avgFast / avgSlow) - 1;
					String nameSpread = stats.getNameSpread(fast, slow);
					rcRaw.setValue(nameSpread, spread);
				}
			}

			/* Candles. */
			for (int i = 0; i < averages.size(); i++) {
				int size = stats.getCandleSize(i);
				int count = stats.getCandleCount(i);
				List<Data> candles = getCandles(size, count, rcBuffer);
				for (int index = 0; index < candles.size(); index++) {
					Data candle = candles.get(index);
					String range = stats.getNameCandle(size, index, DB.FIELD_CANDLE_RANGE);
					rcRaw.setValue(range, new Value(OHLC.getRange(candle)));
					String body_factor = stats.getNameCandle(size, index, DB.FIELD_CANDLE_BODY_FACTOR);
					rcRaw.setValue(body_factor, new Value(OHLC.getBodyFactor(candle)));
					String body_pos = stats.getNameCandle(size, index, DB.FIELD_CANDLE_BODY_POS);
					rcRaw.setValue(body_pos, new Value(OHLC.getBodyPosition(candle)));
					String sign = stats.getNameCandle(size, index, DB.FIELD_CANDLE_SIGN);
					rcRaw.setValue(sign, new Value(OHLC.getSign(candle)));
					if (index < candles.size() - 1) {
						Data previous = candles.get(index + 1);
						String rel_pos = stats.getNameCandle(size, index, DB.FIELD_CANDLE_REL_POS);
						rcRaw.setValue(rel_pos, new Value(OHLC.getRelativePosition(candle, previous)));
					}
				}
			}

			/* Register previous record and calculated (modulus). */
			rcSrcPrev = rcSrc;

			/* Queue insert. */
			concurrents.add(new Record.Insert(rcSrc, persistorSources));
			concurrents.add(new Record.Insert(rcRaw, persistorRaw));

			/* Passed already calculated, do insert. */
			if (concurrents.size() >= maxConcurrent) {
				pool.invokeAll(concurrents);
				concurrents.clear();
			}
		}
		iter.close();
		if (!concurrents.isEmpty()) {
			pool.invokeAll(concurrents);
			concurrents.clear();
		}
		pool.shutdown();

	}

	/**
	 * @param size   Candle size.
	 * @param count  Number of candles.
	 * @param buffer Buffer of records.
	 * @return The list of candles between periods.
	 */
	private List<Data> getCandles(int size, int count, FixedSizeList<Record> buffer) {
		List<Data> candles = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			int start = buffer.size() - (size * (i + 1));
			if (start < 0) {
				start = 0;
			}
			int end = start + size - 1;
			if (end >= buffer.size()) {
				end = buffer.size() - 1;
			}
			long time = 0;
			double open = 0;
			double high = Numbers.MIN_DOUBLE;
			double low = Numbers.MAX_DOUBLE;
			double close = 0;
			for (int j = start; j <= end; j++) {
				Record rc = buffer.getHead(j);
				if (j == start) {
					time = rc.getValue(DB.FIELD_BAR_TIME).getLong();
					open = rc.getValue(DB.FIELD_BAR_OPEN).getDouble();
				}
				double high_rc = rc.getValue(DB.FIELD_BAR_HIGH).getDouble();
				if (high_rc > high) {
					high = high_rc;
				}
				double low_rc = rc.getValue(DB.FIELD_BAR_LOW).getDouble();
				if (low_rc < low) {
					low = low_rc;
				}
				if (j == end) {
					close = rc.getValue(DB.FIELD_BAR_CLOSE).getDouble();
				}
			}
			Data data = new Data(time, open, high, low, close);
			candles.add(data);
		}
		return candles;
	}

	/**
	 * @return The criteria to select record of the ticker after the last sources
	 *         time.
	 */
	private Criteria getCriteriaTicker() throws Throwable {
		Field ftime = persistorTicker.getField(DB.FIELD_BAR_TIME);
		Value vtime = new Value(getLastSourcesTime());
		Criteria criteria = new Criteria();
		criteria.add(Condition.fieldGT(ftime, vtime));
		return criteria;
	}
	
	/**
	 * @return The last time of the sources (and raw) table.
	 */
	private long getLastSourcesTime() throws Throwable {
		long time = 0;
		Field ftime = persistorSources.getField(DB.FIELD_BAR_TIME);
		Order order = new Order();
		order.add(ftime, false);
		RecordIterator iter = persistorSources.iterator(null, order);
		if (iter.hasNext()) {
			Record rc = iter.next();
			time = rc.getValue(DB.FIELD_BAR_TIME).getLong();
		}
		iter.close();
		return time;
	}

}
