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
import com.mlt.util.BlockQueue;
import com.mlt.util.Numbers;
import com.mlt.util.Strings;

import app.mlt.plaf.DB;

/**
 * Generate source averages and the rest of raw values.
 *
 * @author Miquel Sas
 */
public class TaskRaw extends TaskStatistics {

	private Persistor persistorTicker;
	private Persistor persistorSrc;
	private Persistor persistorRaw;

	/**
	 * @param stats The statistics.
	 */
	public TaskRaw(Statistics stats) {
		super(stats);
		setId("averages-raw");
		setTitle(stats.getLabel() + " - Calculate raw values");

		Instrument instrument = stats.getInstrument();
		Period period = stats.getPeriod();
		persistorTicker = DB.persistor_ticker(instrument, period);
		persistorSrc = stats.getTableSrc().getPersistor();
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
			List<Table> tables = new ArrayList<>();
			tables.add(stats.getTableSrc());
			tables.add(stats.getTableRaw());
			tables.add(stats.getTableRng());
			tables.add(stats.getTableNrm());
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
		Concurrent concurrent = new Concurrent(10, 50);

		/* List with all fields to process. */
		List<Field> varFields = stats.getFieldListVar();
		List<Field> patternFields = stats.getFieldListPatterns(true);

		/* Parameters and buffers. */
		List<Integer> deltas = stats.getParameters().getDeltas();
		List<Average> avgs = stats.getParameters().getAverages();
		int maxPeriod = avgs.get(avgs.size() - 1).getPeriod();
		BlockQueue<Record> src = new BlockQueue<>(maxPeriod);
		List<BlockQueue<Record>> raws = new ArrayList<>();
		for (int i = 0; i <= deltas.size(); i++) {
			BlockQueue<Record> raw = new BlockQueue<>(maxPeriod);
			raws.add(raw);
		}

		/* Iterate ticker. */
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

			/* Source buffer. */
			Record rcSrc = persistorSrc.getDefaultRecord();
			rcSrc.setValue(DB.FIELD_BAR_TIME, rcTick.getValue(DB.FIELD_BAR_TIME));
			rcSrc.setValue(DB.FIELD_BAR_OPEN, rcTick.getValue(DB.FIELD_BAR_OPEN));
			rcSrc.setValue(DB.FIELD_BAR_HIGH, rcTick.getValue(DB.FIELD_BAR_HIGH));
			rcSrc.setValue(DB.FIELD_BAR_LOW, rcTick.getValue(DB.FIELD_BAR_LOW));
			rcSrc.setValue(DB.FIELD_BAR_CLOSE, rcTick.getValue(DB.FIELD_BAR_CLOSE));
			src.add(rcSrc);

			/* Raw buffers. */
			for (int i = 0; i <= deltas.size(); i++) {
				Record rcRaw = persistorRaw.getDefaultRecord();
				rcRaw.setValue(DB.FIELD_PATTERN_DELTA, i);
				rcRaw.setValue(DB.FIELD_BAR_TIME, rcTick.getValue(DB.FIELD_BAR_TIME));
				raws.get(i).add(rcRaw);
			}

			/* Averages. */
			for (int i = 0; i < avgs.size(); i++) {
				Average avg = avgs.get(i);
				String nameAvg = getName("avg", pad(avg));
				double value = avg.getAverage(src);
				src.getLast().setValue(nameAvg, value);
			}

			/* Average deltas. */
			for (int i = 0; i < avgs.size(); i++) {
				Average avg = avgs.get(i);
				String nameDelta = getName("avg", "delta", pad(avg));
				String nameAvg = getName("avg", pad(avg));
				double value = getVariance(src, nameAvg, avg.getPeriod());
				raws.get(0).getLast().setValue(nameDelta, value);
			}

			/* Average slopes. */
			for (int i = 0; i < avgs.size(); i++) {
				Average avg = avgs.get(i);
				String nameAvg = getName("avg", pad(avg));
				String nameSlope = getName("avg", "slope", pad(avg));
				Record rcCurr = src.getLast(0);
				Record rcPrev = rcCurr;
				if (src.size() > 1) {
					rcPrev = src.getLast(1);
				}
				double prev = rcPrev.getValue(nameAvg).getDouble();
				double curr = rcCurr.getValue(nameAvg).getDouble();
				double value = Numbers.delta(curr, prev);
				raws.get(0).getLast().setValue(nameSlope, value);
			}

			/* Average spreads. */
			for (int i = 0; i < avgs.size(); i++) {
				Average avgFast = avgs.get(i);
				String nameFast = getName("avg", pad(avgFast));
				for (int j = i + 1; j < avgs.size(); j++) {
					Average avgSlow = avgs.get(j);
					String nameSlow = getName("avg", pad(avgSlow));
					String nameSpread = getName("avg", "spread", pad(avgFast), pad(avgSlow));
					Record rcAvg = src.getLast();
					double fast = rcAvg.getValue(nameFast).getDouble();
					double slow = rcAvg.getValue(nameSlow).getDouble();
					double value = Numbers.delta(fast, slow);
					raws.get(0).getLast().setValue(nameSpread, value);
				}
			}

			/* Variances. */
			for (int i = 0; i < avgs.size() - 1; i++) {
				Average fast = avgs.get(i);
				String nameFast = getName("avg", pad(fast));
				for (int j = i + 1; j < avgs.size(); j++) {
					Average slow = avgs.get(j);
					String nameSlow = getName("avg", pad(slow));
					for (int k = j; k < avgs.size(); k++) {
						int period = avgs.get(k).getPeriod();
						String nameVar = getName("var", pad(fast), pad(slow), pad(period));
						double value = getVariance(src, nameFast, nameSlow, period);
						raws.get(0).getLast().setValue(nameVar, value);
					}
				}
			}

			/* Variance slopes. */
			for (int i = 0; i < avgs.size() - 1; i++) {
				Average fast = avgs.get(i);
				for (int j = i + 1; j < avgs.size(); j++) {
					Average slow = avgs.get(j);
					for (int k = j; k < avgs.size(); k++) {
						int period = avgs.get(k).getPeriod();
						String nameSlope =
							getName("var", "slope", pad(fast), pad(slow), pad(period));
						String nameVar = getName("var", pad(fast), pad(slow), pad(period));
						Record rcCurr = raws.get(0).getLast(0);
						Record rcPrev = rcCurr;
						if (src.size() > 1) {
							rcPrev = raws.get(0).getLast(1);
						}
						double prev = rcPrev.getValue(nameVar).getDouble();
						double curr = rcCurr.getValue(nameVar).getDouble();
						double slope = Numbers.delta(curr, prev);
						raws.get(0).getLast().setValue(nameSlope, slope);
					}
				}
			}

			/* Variance spreads. */
			for (int i = 0; i < varFields.size() - 1; i++) {
				String nameFast = varFields.get(i).getName();
				String nameSlow = varFields.get(i + 1).getName();
				String nameFastSuffix = Strings.remove(nameFast, "var_");
				String nameSlowSuffix = Strings.remove(nameSlow, "var_");
				String nameSpread = getName("var", "spread", nameFastSuffix, nameSlowSuffix);
				Record rcRaw = raws.get(0).getLast();
				double fast = rcRaw.getValue(nameFast).getDouble();
				double slow = rcRaw.getValue(nameSlow).getDouble();
				double value = Numbers.delta(fast, slow);
				raws.get(0).getLast().setValue(nameSpread, value);
			}

			/* Candles. */
			for (int i = 0; i < avgs.size(); i++) {
				int size = stats.getCandleSize(i);
				int count = stats.getCandleCount(i);
				List<Data> candles = getCandles(size, count, src);
				Record rcRaw = raws.get(0).getLast();
				for (int index = 0; index < candles.size(); index++) {
					Data candle = candles.get(index);

					String range = getNameCandle(size, index, DB.FIELD_CANDLE_RANGE);
					rcRaw.setValue(range, new Value(OHLC.getRange(candle)));

					String body_factor = getNameCandle(size, index, DB.FIELD_CANDLE_BODY_FACTOR);
					rcRaw.setValue(body_factor, new Value(OHLC.getBodyFactor(candle)));

					String body_pos = getNameCandle(size, index, DB.FIELD_CANDLE_BODY_POS);
					rcRaw.setValue(body_pos, new Value(OHLC.getBodyPosition(candle)));

					String sign = getNameCandle(size, index, DB.FIELD_CANDLE_SIGN);
					rcRaw.setValue(sign, new Value(OHLC.getSign(candle)));

					if (index < candles.size() - 1) {
						Data previous = candles.get(index + 1);

						String rel_pos = getNameCandle(size, index, DB.FIELD_CANDLE_REL_POS);
						rcRaw.setValue(rel_pos, OHLC.getRelativePosition(candle, previous));

						String rel_range = getNameCandle(size, index, DB.FIELD_CANDLE_REL_RANGE);
						rcRaw.setValue(rel_range, OHLC.getRelativeRange(candle, previous));

						String rel_body = getNameCandle(size, index, DB.FIELD_CANDLE_REL_BODY);
						rcRaw.setValue(rel_body, OHLC.getRelativeBody(candle, previous));
					}
				}
			}

			/* Deltas on all pattern fields. */
			int size = raws.get(0).size();
			Record rcCurr = raws.get(0).getLast();
			for (int i = 0; i < deltas.size(); i++) {
				int delta = Math.min(size - 1, deltas.get(i));
				Record rcPrev = raws.get(0).getLast(delta);
				for (Field field : patternFields) {
					String alias = field.getAlias();
					double curr = rcCurr.getValue(alias).getDouble();
					double prev = rcPrev.getValue(alias).getDouble();
					double value = Numbers.delta(curr, prev);
					raws.get(i + 1).getLast().setValue(alias, value);
				}
			}

			/* Update. */
			concurrent.add(new Record.Insert(rcSrc, persistorSrc));
			for (int i = 0; i <= deltas.size(); i++) {
				Record rcRaw = raws.get(i).getLast();
				concurrent.add(new Record.Insert(rcRaw, persistorRaw));
			}
		}
		iter.close();
	}

	/**
	 * @param size   Candle size.
	 * @param count  Number of candles.
	 * @param buffer Buffer of records.
	 * @return The list of candles between periods.
	 */
	private List<Data> getCandles(int size, int count, BlockQueue<Record> buffer) {
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
		Field ftime = persistorSrc.getField(DB.FIELD_BAR_TIME);
		Order order = new Order();
		order.add(ftime, false);
		RecordIterator iter = persistorSrc.iterator(null, order);
		if (iter.hasNext()) {
			Record rc = iter.next();
			time = rc.getValue(DB.FIELD_BAR_TIME).getLong();
		}
		iter.close();
		return time;
	}

	/**
	 * @param buffer  Raw buffer.
	 * @param nameAvg Average name.
	 * @param period  Slow period.
	 * @return Variance between the close price and the average.
	 */
	private double getVariance(BlockQueue<Record> buffer, String nameAvg, int period) {
		return getVariance(buffer, DB.FIELD_BAR_CLOSE, nameAvg, period);
	}

	/**
	 * @param buffer   Raw buffer.
	 * @param nameFast Fast average name.
	 * @param nameSlow Medium average name.
	 * @param period   Slow period.
	 * @return Variance between the fast and slow averages.
	 */
	private double getVariance(
		BlockQueue<Record> buffer,
		String nameFast,
		String nameSlow,
		int period) {

		period = Math.min(buffer.size(), period);
		double variance = 0;
		for (int i = 0; i < period; i++) {
			Record rc = buffer.getLast(i);
			double fast = rc.getValue(nameFast).getDouble();
			double slow = rc.getValue(nameSlow).getDouble();
			double var = Numbers.delta(fast - slow, slow);
			variance += var;
		}
		variance /= ((double) period);
		return variance;
	}
}
