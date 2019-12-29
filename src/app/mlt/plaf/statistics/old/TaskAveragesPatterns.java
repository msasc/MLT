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

package app.mlt.plaf.statistics.old;

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

import app.mlt.plaf.DB;
import app.mlt.plaf.statistics.Average;

/**
 * Generate patterns.
 *
 * @author Miquel Sas
 */
public class TaskAveragesPatterns extends TaskAverages {

	private Persistor persistorCandles;
	private Persistor persistorPatterns;
	private Persistor persistorStates;
	private List<Average> averages;
	private String[] candleNames =
		new String[] {
			DB.FIELD_CANDLE_RANGE,
			DB.FIELD_CANDLE_BODY_FACTOR,
			DB.FIELD_CANDLE_BODY_POS,
			DB.FIELD_CANDLE_REL_POS,
			DB.FIELD_CANDLE_SIGN
		};

	/**
	 * @param stats The statistics averages.
	 */
	public TaskAveragesPatterns(StatisticsAverages stats) {
		super(stats);
		setId("averages-patterns");
		setTitle(stats.getLabel() + " - Calculate patterns");

		this.persistorCandles = stats.getTableCandles().getPersistor();
		this.persistorPatterns = stats.getTablePatterns().getPersistor();
		this.persistorStates = stats.getTableStates().getPersistor();
		this.averages = stats.getAverages();
	}

	@Override
	protected long calculateTotalWork() throws Throwable {
		setTotalWork(persistorCandles.count(getCriteriaCandles()));
		return getTotalWork();
	}

	@Override
	protected void compute() throws Throwable {

		/* Query option. */
		Option option = queryOption();
		if (option.equals("CANCEL")) {
			throw new Exception("Calculation cancelled by user.");
		}
		if (option.equals("START")) {
			Table table = stats.getTablePatterns();
			if (DB.ddl().existsTable(table)) {
				DB.ddl().dropTable(table);
			}
			DB.ddl().buildTable(table);
		}

		/* Count. */
		calculateTotalWork();

		/* Work tracking. */
		long totalWork = getTotalWork();
		long workDone = 0;
		int poolSize = 100;
		int maxConcurrent = 5000;
		ForkJoinPool pool = new ForkJoinPool(poolSize);
		List<Callable<Void>> concurrents = new ArrayList<>();

		long timePrev = -1;
		List<Record> candles = new ArrayList<>();
		Order order = stats.getTableCandles().getPrimaryKey();
		RecordIterator iter = persistorCandles.iterator(getCriteriaCandles(), order);
		while (iter.hasNext()) {

			/* Check cancel requested. */
			if (isCancelRequested()) {
				setCancelled();
				break;
			}

			/* Retrieve candle record. */
			Record cn = iter.next();
			long time = cn.getValue(DB.FIELD_BAR_TIME).getLong();
			int size = cn.getValue(DB.FIELD_CANDLE_SIZE).getInteger();
			int norder = cn.getValue(DB.FIELD_CANDLE_NORDER).getInteger();

			/* Notify work. */
			workDone++;
			StringBuilder b = new StringBuilder();
			b.append(cn.toString(DB.FIELD_BAR_TIME_FMT));
			b.append(" ");
			b.append(stats.getPadded(size));
			b.append(" ");
			b.append(stats.getPadded(norder));
			update(b.toString(), workDone, totalWork);

			/* Check patterns of same time read. */
			if (timePrev == -1) {
				timePrev = time;
			}
			if (timePrev == time) {
				candles.add(cn);
			} else {
				Record st = persistorStates.getDefaultRecord();
				st.setValue(DB.FIELD_BAR_TIME, time);
				if (!persistorStates.refresh(st)) {
					throw new IllegalStateException("States time " + time + " not found");
				}
				Record pt = getRecordPattern(st, candles);
				concurrents.add(new Record.Insert(pt, persistorPatterns));
				candles.clear();
			}

			/* Next time. */
			timePrev = time;

			/* Update. */
			if (concurrents.size() >= maxConcurrent) {
				pool.invokeAll(concurrents);
				concurrents.clear();
			}
		}
		iter.close();
		if (!candles.isEmpty()) {
			Record st = persistorStates.getDefaultRecord();
			st.setValue(DB.FIELD_BAR_TIME, timePrev);
			if (!persistorStates.refresh(st)) {
				throw new IllegalStateException("States time " + timePrev + " not found");
			}
			Record pt = getRecordPattern(st, candles);
			concurrents.add(new Record.Insert(pt, persistorPatterns));
			candles.clear();
		}
		if (!concurrents.isEmpty()) {
			pool.invokeAll(concurrents);
			concurrents.clear();
		}

	}

	/**
	 * @return The criteria to process the candles table.
	 */
	private Criteria getCriteriaCandles() throws Throwable {
		Field fTIME = persistorCandles.getField(DB.FIELD_BAR_TIME);
		long time = getLastPatternsTime();
		Criteria criteria = new Criteria();
		criteria.add(Condition.fieldGT(fTIME, new Value(time)));
		return criteria;
	}

	/**
	 * @return The last time of the patterns table.
	 */
	private long getLastPatternsTime() throws Throwable {
		long time = 0;
		Field fTIME = persistorPatterns.getField(DB.FIELD_BAR_TIME);
		Order order = new Order();
		order.add(fTIME, false);
		RecordIterator iter = persistorPatterns.iterator(null, order);
		if (iter.hasNext()) {
			Record rc = iter.next();
			time = rc.getValue(DB.FIELD_BAR_TIME).getLong();
		}
		iter.close();
		return time;
	}

	/**
	 * @param st      The states record.
	 * @param candles The list of candles.
	 * @return The pattern record.
	 */
	private Record getRecordPattern(Record st, List<Record> candles) {

		/* Pattern record. */
		Record pt = persistorPatterns.getDefaultRecord();
		pt.setValue(DB.FIELD_BAR_TIME, st.getValue(DB.FIELD_BAR_TIME));

		/* Labels, calculated and edited. */
		pt.setValue(DB.FIELD_STATES_LABEL_CALC, st.getValue(DB.FIELD_STATES_LABEL_CALC));
		pt.setValue(DB.FIELD_STATES_LABEL_EDIT, st.getValue(DB.FIELD_STATES_LABEL_EDIT));

		/* Slopes of averages, normalized. */
		for (int i = 0; i < averages.size(); i++) {
			Average avg = averages.get(i);
			String name = stats.getNameSlope(avg);
			String name_nrm = stats.getNameSuffix(name, "nrm");
			pt.setValue(name, st.getValue(name_nrm));
		}

		/* Spreads within averages, normalized. */
		for (int i = 0; i < averages.size(); i++) {
			Average fast = averages.get(i);
			for (int j = i + 1; j < averages.size(); j++) {
				Average slow = averages.get(j);
				String name = stats.getNameSpread(fast, slow);
				String name_nrm = stats.getNameSuffix(name, "nrm");
				pt.setValue(name, st.getValue(name_nrm));
			}
		}

		/* Candles. */
		for (Record cn : candles) {
			int size = cn.getValue(DB.FIELD_CANDLE_SIZE).getInteger();
			int norder = cn.getValue(DB.FIELD_CANDLE_NORDER).getInteger();
			for (String name : candleNames) {
				String nameNrm = stats.getNameSuffix(name, "nrm");
				String nameCnd = stats.getNameCandle(size, norder, name);
				pt.setValue(nameCnd, cn.getValue(nameNrm));
			}
		}

		return pt;
	}
}
