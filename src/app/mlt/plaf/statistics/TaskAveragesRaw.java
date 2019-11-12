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

import com.mlt.db.Criteria;
import com.mlt.db.Persistor;
import com.mlt.db.Record;
import com.mlt.db.RecordIterator;
import com.mlt.db.Table;
import com.mlt.db.Value;
import com.mlt.mkt.data.Data;
import com.mlt.mkt.data.Instrument;
import com.mlt.mkt.data.Period;
import com.mlt.task.Task;
import com.mlt.util.FixedSizeList;
import com.mlt.util.Numbers;

import app.mlt.plaf.DB;
import app.mlt.plaf.db.Fields;

/**
 * Calculate all the raw values for the states table of the statistics averages.
 *
 * @author Miquel Sas
 */
public class TaskAveragesRaw extends Task {

	/** Underlying statistics averages. */
	private StatisticsAverages stats;

	/**
	 * Constructor.
	 * 
	 * @param stats The statistics averages.
	 */
	public TaskAveragesRaw(StatisticsAverages stats) {
		super();
		this.stats = stats;
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

		/* Count. */
		calculateTotalWork();

		/* States and ticker tables. */
		Table states = stats.getTableStates();
		Table ticker = DB.table_ticker(stats.getInstrument(), stats.getPeriod());

		/* Delete data (if process from scratch) */
		states.getPersistor().delete((Criteria) null);

		/* Averages and maximum period. */
		List<Average> averages = stats.getAverages();
		int maxPeriod = averages.get(averages.size() - 1).getPeriod();

		/* Iterate ticker. */
		long totalWork = getTotalWork();
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
			avgBuffer.add(rcTick.getValue(Fields.BAR_CLOSE).getDouble());

			/* Notify work. */
			workDone++;
			if (workDone % 100 == 0 || workDone == totalWork) {
				StringBuilder b = new StringBuilder();
				b.append(rcTick.toString(Fields.BAR_TIME_FMT));
				b.append(", ");
				b.append(rcTick.toString(Fields.BAR_OPEN));
				b.append(", ");
				b.append(rcTick.toString(Fields.BAR_HIGH));
				b.append(", ");
				b.append(rcTick.toString(Fields.BAR_LOW));
				b.append(", ");
				b.append(rcTick.toString(Fields.BAR_CLOSE));
				update(b.toString(), workDone, totalWork);
			}

			/* Statistics record. */
			Record rcStat = states.getDefaultRecord();
			rcStat.setValue(Fields.BAR_TIME, rcTick.getValue(Fields.BAR_TIME));
			rcStat.setValue(Fields.BAR_OPEN, rcTick.getValue(Fields.BAR_OPEN));
			rcStat.setValue(Fields.BAR_HIGH, rcTick.getValue(Fields.BAR_HIGH));
			rcStat.setValue(Fields.BAR_LOW, rcTick.getValue(Fields.BAR_LOW));
			rcStat.setValue(Fields.BAR_CLOSE, rcTick.getValue(Fields.BAR_CLOSE));

			/* Calculate averages. */
			for (int i = 0; i < averages.size(); i++) {
				Average average = averages.get(i);
				double value = average.getAverage(avgBuffer);
				String name = stats.getNameAverage(i);
				rcStat.setValue(name, new Value(value));
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
				rcStat.setValue(nameSlope, new Value(slope));
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
					rcStat.setValue(nameSpread, new Value(spread));
				}
			}

			/* Calculate candles raw values. */
			rcBuffer.add(rcStat);
			for (int i = 0; i < averages.size() - 1; i++) {
				int fast = averages.get(i).getPeriod();
				int slow = averages.get(i + 1).getPeriod();
				List<Data> candles = getCandles(fast, slow, rcBuffer);
				for (int j = 0; j < candles.size(); j++) {
					
					Data candle = candles.get(j);
					
					/* Open, high, low, close. */
					double open = candle.getValue(Data.OPEN);
					double high = candle.getValue(Data.HIGH);
					double low = candle.getValue(Data.LOW);
					double close = candle.getValue(Data.CLOSE);
					String nameOpen = stats.getNameCandle("open", fast, slow, j);
					rcStat.setValue(nameOpen, new Value(open));
					String nameHigh = stats.getNameCandle("high", fast, slow, j);
					rcStat.setValue(nameHigh, new Value(high));
					String nameLow = stats.getNameCandle("low", fast, slow, j);
					rcStat.setValue(nameLow, new Value(low));
					String nameClose = stats.getNameCandle("close", fast, slow, j);
					rcStat.setValue(nameClose, new Value(close));
					
					/* Range, raw value. */
					double range = high - low;
					String nameRange = stats.getNameCandle("range", fast, slow, j, "raw");
					rcStat.setValue(nameRange, new Value(range));
				}
			}

			/* Insert the record. */
			states.getPersistor().insert(rcStat);

			/* Register previous record. */
			rcPrev = rcStat;
		}
		iter.close();
	}

	/**
	 * Return the list of candles between periods.
	 * 
	 * @param fastPeriod Fast period.
	 * @param slowPeriod Slow period.
	 * @param rcBuffer   Buffer of records.
	 * @return The list of candles.
	 */
	private List<Data> getCandles(int fastPeriod, int slowPeriod, FixedSizeList<Record> rcBuffer) {
		List<Data> candles = new ArrayList<>();
		int countCandles = slowPeriod / fastPeriod;
		for (int i = 0; i < countCandles; i++) {
			int startIndex = fastPeriod * i;
			int endIndex = startIndex + fastPeriod - 1;
			if (startIndex >= rcBuffer.size()) {
				startIndex = rcBuffer.size() - 1;
			}
			if (endIndex >= rcBuffer.size()) {
				endIndex = rcBuffer.size() - 1;
			}
			double open = 0;
			double high = Numbers.MIN_DOUBLE;
			double low = Numbers.MAX_DOUBLE;
			double close = 0;
			for (int j = startIndex; j <= endIndex; j++) {
				Record rc = rcBuffer.get(j);
				if (j == startIndex) {
					open = rc.getValue(Fields.BAR_OPEN).getDouble();
				}
				double high_rc = rc.getValue(Fields.BAR_HIGH).getDouble();
				if (high_rc > high) {
					high = high_rc;
				}
				double low_rc = rc.getValue(Fields.BAR_LOW).getDouble();
				if (low_rc < low) {
					low = low_rc;
				}
				if (j == endIndex) {
					close = rc.getValue(Fields.BAR_CLOSE).getDouble();
				}
			}
			candles.add(new Data(0, open, high, low, close));
		}
		return candles;
	}
}
