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
 * Calculate all the raw values for the states table of the statistics averages.
 *
 * @author Miquel Sas
 */
public class TaskAveragesRaw extends TaskAverages {
	
	/**
	 * Constructor.
	 * 
	 * @param stats The statistics averages.
	 */
	public TaskAveragesRaw(StatisticsAverages stats) {
		super(stats);
		setId("averages-raw");
		setTitle(stats.getLabel() + " - Calculate raw values");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected long calculateTotalWork() throws Throwable {
		Instrument instrument = stats.getInstrument();
		Period period = stats.getPeriod();
		Persistor persistor = DB.persistor_ticker(instrument, period);
		setTotalWork(persistor.count(null));
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
		if (option.equals("START")) {
			List<Table> tables = stats.getTables();
			for (Table table : tables) {
				if (DB.ddl().existsTable(table)) {
					DB.ddl().dropTable(table);
				}
				DB.ddl().buildTable(table);
			}
		}

		/* Delete candles after the last states, if any. */
		deleteCandles();

		/* States and ticker tables. */
		Persistor statesPersistor = stats.getTableStates().getPersistor();
		Table tickerTable = DB.table_ticker(stats.getInstrument(), stats.getPeriod());
		Persistor tickerPersistor = tickerTable.getPersistor();
		Persistor candlesPersistor = stats.getTableCandles().getPersistor();

		/* Count and retrieve already calculated. */
		calculateTotalWork();
		long totalWork = getTotalWork();
		long calculated = statesPersistor.count(null);

		/* If already all calculated, do nothing. */
		if (calculated == totalWork) {
			return;
		}

		/* Averages and maximum period. */
		List<Average> averages = stats.getAverages();
		int maxPeriod = averages.get(averages.size() - 1).getPeriod();

		/* Iterate ticker. */
		int modulus = 10;
		long workDone = 0;
		FixedSizeList<Double> avgBuffer = new FixedSizeList<>(maxPeriod);
		FixedSizeList<Record> rcBuffer = new FixedSizeList<>(maxPeriod);
		RecordIterator iter = tickerPersistor.iterator(null, tickerTable.getPrimaryKey());
		Record rcPrev = null;
		List<Callable<Void>> insertor = new ArrayList<>();
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

			/* Notify work. */
			workDone++;
			if (workDone % modulus == 0 || workDone == totalWork) {
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
			}

			/* If workDone < calculated - (maxPeriod * 2), do nothig. */
			if (workDone < calculated - (maxPeriod * 2)) {
				continue;
			}

			/* Statistics record. */
			Record rcStat = statesPersistor.getDefaultRecord();
			rcStat.setValue(DB.FIELD_BAR_TIME, rcTick.getValue(DB.FIELD_BAR_TIME));
			rcStat.setValue(DB.FIELD_BAR_OPEN, rcTick.getValue(DB.FIELD_BAR_OPEN));
			rcStat.setValue(DB.FIELD_BAR_HIGH, rcTick.getValue(DB.FIELD_BAR_HIGH));
			rcStat.setValue(DB.FIELD_BAR_LOW, rcTick.getValue(DB.FIELD_BAR_LOW));
			rcStat.setValue(DB.FIELD_BAR_CLOSE, rcTick.getValue(DB.FIELD_BAR_CLOSE));

			/* Calculate averages. */
			for (int i = 0; i < averages.size(); i++) {
				Average avg = averages.get(i);
				double value = avg.getAverage(avgBuffer);
				String name = Average.getNameAverage(avg);
				rcStat.setValue(name, value);
			}

			/* Calculate raw slopes. */
			if (rcPrev == null) {
				rcPrev = rcStat;
			}
			for (int i = 0; i < averages.size(); i++) {
				Average avg = averages.get(i);
				String nameAverage = Average.getNameAverage(avg);
				double prev = rcPrev.getValue(nameAverage).getDouble();
				double curr = rcStat.getValue(nameAverage).getDouble();
				double slope = 0;
				if (prev != 0) {
					slope = (curr / prev) - 1;
				}
				String nameSlope = Average.getNameSlope(avg, "raw");
				rcStat.setValue(nameSlope, slope);
			}

			/* Calculate raw spreads. */
			for (int i = 0; i < averages.size(); i++) {
				Average fast = averages.get(i);
				String nameFast = Average.getNameAverage(fast);
				double avgFast = rcStat.getValue(nameFast).getDouble();
				for (int j = i + 1; j < averages.size(); j++) {
					Average slow = averages.get(j);
					String nameSlow = Average.getNameAverage(slow);
					double avgSlow = rcStat.getValue(nameSlow).getDouble();
					double spread = (avgFast / avgSlow) - 1;
					String nameSpread = Average.getNameSpread(fast, slow, "raw");
					rcStat.setValue(nameSpread, spread);
				}
			}

			/* If not achieved work done, just register previous record and continue. */
			if (workDone <= calculated) {
				rcPrev = rcStat;
				continue;
			}
			
			/* Insert. */
			insertor.add(new Record.Insert(rcStat, statesPersistor));

			/* Calculate candles raw values. */
			for (int i = 0; i < averages.size(); i++) {
				int size = stats.getCandleSize(i);
				int count = stats.getCandleCount(i);
				List<Data> candles = getCandles(size, count, rcBuffer);
				for (int j = 0; j < candles.size(); j++) {

					Data candle = candles.get(j);
					Record rc = candlesPersistor.getDefaultRecord();
					rc.setValue(DB.FIELD_BAR_TIME, rcTick.getValue(DB.FIELD_BAR_TIME));
					rc.setValue(DB.FIELD_CANDLE_SIZE, new Value(size));
					rc.setValue(DB.FIELD_CANDLE_NORDER, new Value(j));
					rc.setValue(DB.FIELD_CANDLE_TIME, new Value(candle.getTime()));
					rc.setValue(DB.FIELD_CANDLE_OPEN, new Value(OHLC.getOpen(candle)));
					rc.setValue(DB.FIELD_CANDLE_HIGH, new Value(OHLC.getHigh(candle)));
					rc.setValue(DB.FIELD_CANDLE_LOW, new Value(OHLC.getLow(candle)));
					rc.setValue(DB.FIELD_CANDLE_CLOSE, new Value(OHLC.getClose(candle)));

					String range = DB.FIELD_CANDLE_RANGE + "_raw";
					rc.setValue(range, new Value(OHLC.getRange(candle)));
					String bodyFactor = DB.FIELD_CANDLE_BODY_FACTOR + "_raw";
					rc.setValue(bodyFactor, new Value(OHLC.getBodyFactor(candle)));
					String bodyPos = DB.FIELD_CANDLE_BODY_POS + "_raw";
					rc.setValue(bodyPos, new Value(OHLC.getBodyPosition(candle)));
					String sign = DB.FIELD_CANDLE_SIGN + "_raw";
					rc.setValue(sign, new Value(OHLC.getSign(candle)));
					if (j < candles.size() - 1) {
						Data previous = candles.get(j + 1);
						String center = DB.FIELD_CANDLE_REL_POS + "_raw";
						rc.setValue(center, new Value(OHLC.getRelativePositions(candle, previous)));
					}
					insertor.add(new Record.Insert(rc, candlesPersistor));
				}
			}

			/* Register previous record and calculated (modulus). */
			rcPrev = rcStat;

			/* Passed already calculated, do insert. */
			if (insertor.size() >= 100) {
				ForkJoinPool.commonPool().invokeAll(insertor);
				insertor.clear();
			}
		}
		iter.close();
		if (!insertor.isEmpty()) {
			ForkJoinPool.commonPool().invokeAll(insertor);
			insertor.clear();
		}
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
	 * Delete eventual candles after the last states time.
	 */
	private void deleteCandles() throws Throwable {
		long time = getLastStatesTime();
		Persistor persistor = stats.getTableCandles().getPersistor();
		Field fTIME = persistor.getField(DB.FIELD_BAR_TIME);
		Criteria criteria = new Criteria();
		criteria.add(Condition.fieldGT(fTIME, new Value(time)));
		persistor.delete(criteria);
	}

	/**
	 * @return The last time of the states table.
	 * @throws Throwable
	 */
	private long getLastStatesTime() throws Throwable {
		long time = 0;
		Persistor persistor = stats.getTableStates().getPersistor();
		Field fTIME = persistor.getField(DB.FIELD_BAR_TIME);
		Order order = new Order();
		order.add(fTIME, false);
		RecordIterator iter = persistor.iterator(null, order);
		if (iter.hasNext()) {
			Record rc = iter.next();
			time = rc.getValue(DB.FIELD_BAR_TIME).getLong();
		}
		iter.close();
		return time;
	}
}
