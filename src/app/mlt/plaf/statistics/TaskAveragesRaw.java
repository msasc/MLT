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
import com.mlt.task.Concurrent;
import com.mlt.util.FixedSizeQueue;
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
	public TaskAveragesRaw(Statistics stats) {
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

		/* Concurrent pool. */
		Concurrent concurrent = new Concurrent(50, 500);

		/* Buffers. */
		List<Average> averages = stats.getAverages();
		int maxPeriod = averages.get(averages.size() - 1).getPeriod();
		FixedSizeQueue<Double> avgBuffer = new FixedSizeQueue<>(maxPeriod);
		FixedSizeQueue<Record> rcTickBuffer = new FixedSizeQueue<>(maxPeriod);
		FixedSizeQueue<Record> rcSrcBuffer = new FixedSizeQueue<>(maxPeriod);
		FixedSizeQueue<Record> rcRawBuffer = new FixedSizeQueue<>(maxPeriod);

		List<Field> varFields = stats.getFieldListVariances();

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
			rcTickBuffer.add(rcTick);

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
			rcSrcBuffer.add(rcSrc);

			/* Calculate averages. */
			for (int i = 0; i < averages.size(); i++) {
				Average avg = averages.get(i);
				double value = avg.getAverage(avgBuffer);
				String name = stats.getNameAvg(avg);
				rcSrc.setValue(name, value);
			}

			/* Raw values record. */
			Record rcRaw = persistorRaw.getDefaultRecord();
			rcRaw.setValue(DB.FIELD_BAR_TIME, rcTick.getValue(DB.FIELD_BAR_TIME));
			rcRawBuffer.add(rcRaw);

			/* Avg-Slopes. */
			for (int i = 0; i < averages.size(); i++) {
				Average avg = averages.get(i);
				String nameAvg = stats.getNameAvg(avg);
				double prev = rcSrcPrev.getValue(nameAvg).getDouble();
				double curr = rcSrc.getValue(nameAvg).getDouble();
				double slope = 0;
				if (prev != 0) {
					slope = (curr / prev) - 1;
				}
				String nameSlope = stats.getNameAvgSlope(avg);
				rcRaw.setValue(nameSlope, slope);
			}

			/* Avg-Spreads. */
			for (int i = 0; i < averages.size(); i++) {
				Average fast = averages.get(i);
				String nameFast = stats.getNameAvg(fast);
				double avgFast = rcSrc.getValue(nameFast).getDouble();
				for (int j = i + 1; j < averages.size(); j++) {
					Average slow = averages.get(j);
					String nameSlow = stats.getNameAvg(slow);
					double avgSlow = rcSrc.getValue(nameSlow).getDouble();
					double spread = (avgSlow == 0 ? 0 : (avgFast / avgSlow) - 1);
					String nameSpread = stats.getNameAvgSpread(fast, slow);
					rcRaw.setValue(nameSpread, spread);
				}
			}

			/* Vars. */
			for (int i = 0; i < averages.size() - 1; i++) {
				Average fast = averages.get(i);
				Average slow = averages.get(i + 1);
				String nameFast = stats.getNameAvg(fast);
				String nameSlow = stats.getNameAvg(slow);
				for (int j = i + 1; j < averages.size(); j++) {
					int period = averages.get(j).getPeriod();
					double var = getVariance(rcSrcBuffer, nameFast, nameSlow, period);
					String nameVar = stats.getNameVar(fast, slow, period);
					rcRaw.setValue(nameVar, var);
				}
			}

			/* Var slopes. */
			for (int i = 0; i < averages.size() - 1; i++) {
				Average fast = averages.get(i);
				Average slow = averages.get(i + 1);
				for (int j = i + 1; j < averages.size(); j++) {
					int period = averages.get(j).getPeriod();
					String nameVar = stats.getNameVar(fast, slow, period);
					String nameSlope = stats.getNameVarSlope(fast, slow, period);
					Record rcCurr = rcRawBuffer.getLast(0);
					Record rcPrev = null;
					if (rcRawBuffer.size() > 1) {
						rcPrev = rcRawBuffer.getLast(1);
					}
					double varCurr = rcCurr.getValue(nameVar).getDouble();
					double varPrev = 0;
					if (rcPrev != null) {
						varPrev = rcPrev.getValue(nameVar).getDouble();
					}
					double slope = varCurr - varPrev;
					rcRaw.setValue(nameSlope, slope);
				}
			}

			/* Var-Spreads. */
			for (int i = 0; i < varFields.size() - 1; i++) {
				String nameFast = varFields.get(i).getName();
				String nameSlow = varFields.get(i + 1).getName();
				String nameSpread = stats.getNameVarSpread(nameFast, nameSlow);
				double fast = rcRaw.getValue(nameFast).getDouble();
				double slow = rcRaw.getValue(nameSlow).getDouble();
				double spread = (slow == 0 ? 0 : (fast / slow) - 1);
				rcRaw.setValue(nameSpread, spread);
			}

			/* Candles. */
			for (int i = 0; i < averages.size(); i++) {
				int size = stats.getCandleSize(i);
				int count = stats.getCandleCount(i);
				List<Data> candles = getCandles(size, count, rcTickBuffer);
				for (int index = 0; index < candles.size(); index++) {
					Data candle = candles.get(index);
					
					String range = stats.getNameCandle(size, index, DB.FIELD_CANDLE_RANGE);
					rcRaw.setValue(range, new Value(OHLC.getRange(candle)));
					
					String body_factor =
						stats.getNameCandle(size, index, DB.FIELD_CANDLE_BODY_FACTOR);
					rcRaw.setValue(body_factor, new Value(OHLC.getBodyFactor(candle)));
					
					String body_pos = stats.getNameCandle(size, index, DB.FIELD_CANDLE_BODY_POS);
					rcRaw.setValue(body_pos, new Value(OHLC.getBodyPosition(candle)));
					
					String sign = stats.getNameCandle(size, index, DB.FIELD_CANDLE_SIGN);
					rcRaw.setValue(sign, new Value(OHLC.getSign(candle)));
					
					if (index < candles.size() - 1) {
						Data previous = candles.get(index + 1);
						
						String rel_pos = stats.getNameCandle(size, index, DB.FIELD_CANDLE_REL_POS);
						rcRaw.setValue(
							rel_pos,
							new Value(OHLC.getRelativePosition(candle, previous)));
						
						String rel_range = stats.getNameCandle(size, index, DB.FIELD_CANDLE_REL_RANGE);
						rcRaw.setValue(
							rel_range,
							new Value(OHLC.getRelativeRange(candle, previous)));
						
						String rel_body = stats.getNameCandle(size, index, DB.FIELD_CANDLE_REL_BODY);
						rcRaw.setValue(
							rel_body,
							new Value(OHLC.getRelativeBody(candle, previous)));
					}
				}
			}

			/* Register previous record and calculated (modulus). */
			rcSrcPrev = rcSrc;

			/* Queue insert. */
			concurrent.add(new Record.Insert(rcSrc, persistorSources));
			concurrent.add(new Record.Insert(rcRaw, persistorRaw));
		}
		iter.close();
		concurrent.end();
	}

	/**
	 * @param size   Candle size.
	 * @param count  Number of candles.
	 * @param buffer Buffer of records.
	 * @return The list of candles between periods.
	 */
	private List<Data> getCandles(int size, int count, FixedSizeQueue<Record> buffer) {
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
				Record rc = buffer.getFirst(j);
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

	/**
	 * @param rcRawBuffer Raw buffer.
	 * @param nameFast    Fast average name.
	 * @param nameSlow    Medium average name.
	 * @param period      Slow period.
	 * @return Variance.
	 */
	private double getVariance(
		FixedSizeQueue<Record> rcRawBuffer,
		String nameFast,
		String nameSlow,
		int period) {

		period = Math.min(rcRawBuffer.size(), period);
		double var = 0;
		for (int i = 0; i < period; i++) {
			Record rc = rcRawBuffer.getLast(i);
			double fast = rc.getValue(nameFast).getDouble();
			double medium = rc.getValue(nameSlow).getDouble();
			var += (fast - medium);
		}
		var /= ((double) period);
		return var;
	}

}
