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

import com.mlt.db.Persistor;
import com.mlt.db.Record;
import com.mlt.db.RecordIterator;
import com.mlt.db.Table;
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

		/* States and ticker tables. */
		Table states = stats.getTableStates();
		Table ticker = DB.table_ticker(stats.getInstrument(), stats.getPeriod());

		/* Count and retrieve already calculated. */
		calculateTotalWork();
		long totalWork = getTotalWork();
		long calculated = states.getPersistor().count(null);

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
		RecordIterator iter = ticker.getPersistor().iterator(null, ticker.getPrimaryKey());
		Record rcPrev = null;
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
			Record rcStat = states.getDefaultRecord();
			rcStat.setValue(DB.FIELD_BAR_TIME, rcTick.getValue(DB.FIELD_BAR_TIME));
			rcStat.setValue(DB.FIELD_BAR_OPEN, rcTick.getValue(DB.FIELD_BAR_OPEN));
			rcStat.setValue(DB.FIELD_BAR_HIGH, rcTick.getValue(DB.FIELD_BAR_HIGH));
			rcStat.setValue(DB.FIELD_BAR_LOW, rcTick.getValue(DB.FIELD_BAR_LOW));
			rcStat.setValue(DB.FIELD_BAR_CLOSE, rcTick.getValue(DB.FIELD_BAR_CLOSE));

			/* Calculate averages. */
			for (int i = 0; i < averages.size(); i++) {
				Average average = averages.get(i);
				double value = average.getAverage(avgBuffer);
				String name = stats.getNameAverage(i);
				rcStat.setValue(name, value);
			}

			/* Calculate raw slopes. */
			if (rcPrev == null) {
				rcPrev = rcStat;
			}
			for (int i = 0; i < averages.size(); i++) {
				String nameAverage = stats.getNameAverage(i);
				double prev = rcPrev.getValue(nameAverage).getDouble();
				double curr = rcStat.getValue(nameAverage).getDouble();
				double slope = 0;
				if (prev != 0) {
					slope = (curr / prev) - 1;
				}
				String nameSlope = stats.getNameSlope(i, "raw");
				rcStat.setValue(nameSlope, slope);
			}

			/* Calculate raw spreads. */
			for (int i = 0; i < averages.size(); i++) {
				String nameFast = stats.getNameAverage(i);
				double avgFast = rcStat.getValue(nameFast).getDouble();
				for (int j = i + 1; j < averages.size(); j++) {
					String nameSlow = stats.getNameAverage(j);
					double avgSlow = rcStat.getValue(nameSlow).getDouble();
					double spread = (avgFast / avgSlow) - 1;
					String nameSpread = stats.getNameSpread(i, j, "raw");
					rcStat.setValue(nameSpread, spread);
				}
			}

			/* Calculate candles raw values. */
			for (int i = 0; i < averages.size(); i++) {
				int fast = (i == 0 ? 1 : averages.get(i - 1).getPeriod());
				int slow = averages.get(i).getPeriod();
				List<Data> candles = getCandles(fast, slow, rcBuffer);
				for (int j = 0; j < candles.size(); j++) {

					Data candle = candles.get(j);

					String time = stats.getNameCandle(DB.FIELD_BAR_TIME, fast, slow, j);
					rcStat.setValue(time, candle.getTime());

					String open = stats.getNameCandle(DB.FIELD_BAR_OPEN, fast, slow, j);
					rcStat.setValue(open, OHLC.getOpen(candle));

					String high = stats.getNameCandle(DB.FIELD_BAR_HIGH, fast, slow, j);
					rcStat.setValue(high, OHLC.getHigh(candle));

					String low = stats.getNameCandle(DB.FIELD_BAR_LOW, fast, slow, j);
					rcStat.setValue(low, OHLC.getLow(candle));

					String close = stats.getNameCandle(DB.FIELD_BAR_CLOSE, fast, slow, j);
					rcStat.setValue(close, OHLC.getClose(candle));

					String range = stats.getNameCandle(DB.FIELD_BAR_RANGE, fast, slow, j, "raw");
					rcStat.setValue(range, OHLC.getRange(candle));

					String bodyFactor =
						stats.getNameCandle(DB.FIELD_BAR_BODY_FACTOR, fast, slow, j, "raw");
					rcStat.setValue(bodyFactor, OHLC.getBodyFactor(candle));

					String bodyPos =
						stats.getNameCandle(DB.FIELD_BAR_BODY_POS, fast, slow, j, "raw");
					rcStat.setValue(bodyPos, OHLC.getBodyPosition(candle));

					String sign = stats.getNameCandle(DB.FIELD_BAR_SIGN, fast, slow, j, "raw");
					rcStat.setValue(sign, OHLC.getSign(candle));

					if (j < candles.size() - 1) {
						Data previous = candles.get(j + 1);
						String center =
							stats.getNameCandle(DB.FIELD_BAR_REL_POS, fast, slow, j, j + 1, "raw");
						rcStat.setValue(center, OHLC.getRelativePositions(candle, previous));
					}
				}
			}

			/* Register previous record and calculated (modulus). */
			rcPrev = rcStat;

			/* Insert if passed already calculated. */
			if (workDone > calculated) {
				states.getPersistor().insert(rcStat);
			}
		}
		iter.close();
	}

	/**
	 * Return the list of candles between periods.
	 * 
	 * @param fastPeriod Fast period.
	 * @param slowPeriod Slow period.
	 * @param buffer     Buffer of records.
	 * @return The list of candles.
	 */
	private List<Data> getCandles(int fastPeriod, int slowPeriod, FixedSizeList<Record> buffer) {
		List<Data> candles = new ArrayList<>();
		int countCandles = slowPeriod / fastPeriod;
		for (int i = 0; i < countCandles; i++) {
			int startIndex = fastPeriod * i;
			int endIndex = startIndex + fastPeriod - 1;
			if (startIndex >= buffer.size()) {
				startIndex = buffer.size() - 1;
			}
			if (endIndex >= buffer.size()) {
				endIndex = buffer.size() - 1;
			}
			long time = 0;
			double open = 0;
			double high = Numbers.MIN_DOUBLE;
			double low = Numbers.MAX_DOUBLE;
			double close = 0;
			for (int j = startIndex; j <= endIndex; j++) {
				Record rc = buffer.getTail(j);
				if (j == startIndex) {
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
				if (j == endIndex) {
					close = rc.getValue(DB.FIELD_BAR_CLOSE).getDouble();
				}
			}
			candles.add(new Data(time, open, high, low, close));
		}
		return candles;
	}

}
