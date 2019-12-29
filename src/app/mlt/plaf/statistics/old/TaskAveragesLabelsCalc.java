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

import com.mlt.db.Criteria;
import com.mlt.db.ListPersistor;
import com.mlt.db.Persistor;
import com.mlt.db.Record;
import com.mlt.db.Value;
import com.mlt.db.ValueMap;

import app.mlt.plaf.DB;

/**
 * Setup labels on calculated pivots.
 *
 * @author Miquel Sas
 */
public class TaskAveragesLabelsCalc extends TaskAverages {

	/** Origin persistor. */
	private Persistor persistor;
	/** List persistor on table states. */
	private ListPersistor listPersistor;

	/**
	 * Constructor.
	 * 
	 * @param stats The statistics averages.
	 */
	public TaskAveragesLabelsCalc(StatisticsAverages stats) {
		super(stats);
		setId("averages-label-calc");
		setTitle(stats.getLabel() + " - Labels on calculated pivots");
		persistor = stats.getTableStates().getPersistor();
		listPersistor = new ListPersistor(persistor);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected long calculateTotalWork() throws Throwable {
		long totalWork = listPersistor.size();
		setTotalWork(totalWork);
		return getTotalWork();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void compute() throws Throwable {

		/* Reset values. */
		ValueMap map = new ValueMap();
		map.put(DB.FIELD_STATES_LABEL_CALC, new Value(0));
		map.put(DB.FIELD_STATES_LABEL_CALC_SET, new Value(0));
		persistor.update(new Criteria(), map);
		
		/* Count. */
		calculateTotalWork();

		/* Pool. */
		int poolSize = 50;
		int maxConcurrent = 500;
		ForkJoinPool pool = new ForkJoinPool(poolSize);
		List<Callable<Void>> concurrents = new ArrayList<>();

		/* Iterate states. */
		double percentCalc = stats.getPercentCalc();
		for (int i = 0; i < listPersistor.size(); i++) {

			/* Check cancel requested. */
			if (isCancelRequested()) {
				setCancelled();
				break;
			}

			/* Retrieve record. */
			Record rc = listPersistor.getRecord(i);

			/* Notify work. */
			StringBuilder b = new StringBuilder();
			b.append(rc.toString(DB.FIELD_BAR_TIME_FMT));
			b.append(", ");
			b.append(rc.toString(DB.FIELD_BAR_OPEN));
			b.append(", ");
			b.append(rc.toString(DB.FIELD_BAR_HIGH));
			b.append(", ");
			b.append(rc.toString(DB.FIELD_BAR_LOW));
			b.append(", ");
			b.append(rc.toString(DB.FIELD_BAR_CLOSE));
			update(b.toString(), (i + 1), listPersistor.size());

			/* If not a pivot, continue. */
			int pivot = rc.getValue(DB.FIELD_STATES_PIVOT_CALC).getInteger();
			if (pivot == 0) {
				continue;
			}
			int index = i;
			double value = rc.getValue(DB.FIELD_STATES_REFV_CALC).getDouble();
			rc.setValue(DB.FIELD_STATES_LABEL_CALC, new Value(0));
			rc.setValue(DB.FIELD_STATES_LABEL_CALC_SET, new Value(1));
			concurrents.add(new Record.Update(rc, persistor));

			/* Analyze previous. */
			int indexPrev = -1;
			for (int j = i - 1; j >= 0; j--) {
				Record rcTmp = listPersistor.getRecord(j);
				int pivotTmp = rcTmp.getValue(DB.FIELD_STATES_PIVOT_CALC).getInteger();
				if (pivotTmp != 0) {
					indexPrev = j;
					break;
				}
			}
			if (indexPrev == -1) {
				indexPrev = 0;
			}
			Record rcPrev = listPersistor.getRecord(indexPrev);
			double valuePrev = rcPrev.getValue(DB.FIELD_STATES_REFV_CALC).getDouble();
			double evalPrev = Math.abs(value - valuePrev) * percentCalc / 100;
			boolean labelPrevSet = false;
			for (int j = indexPrev; j < index; j++) {
				Record rcTmp = listPersistor.getRecord(j);
				if (!labelPrevSet) {
					double valueTmp = rcTmp.getValue(DB.FIELD_STATES_REFV_CALC).getDouble();
					if (Math.abs(value - valueTmp) <= evalPrev) {
						rcTmp.setValue(DB.FIELD_STATES_LABEL_CALC, new Value(0));
						rcTmp.setValue(DB.FIELD_STATES_LABEL_CALC_SET, new Value(1));
						concurrents.add(new Record.Update(rcTmp, persistor));
						labelPrevSet = true;
					}
				} else {
					rcTmp.setValue(DB.FIELD_STATES_LABEL_CALC, new Value(0));
					rcTmp.setValue(DB.FIELD_STATES_LABEL_CALC_SET, new Value(1));
					concurrents.add(new Record.Update(rcTmp, persistor));
				}
			}
			if (rcPrev.getValue(DB.FIELD_STATES_LABEL_CALC_SET).getInteger() != 0) {
				int labelPrev = (pivot == 1 ? 1 : -1);
				for (int j = indexPrev; j < index; j++) {
					Record rcTmp = listPersistor.getRecord(j);
					if (rcTmp.getValue(DB.FIELD_STATES_LABEL_CALC_SET).getInteger() == 0) {
						rcTmp.setValue(DB.FIELD_STATES_LABEL_CALC, new Value(labelPrev));
						rcTmp.setValue(DB.FIELD_STATES_LABEL_CALC_SET, new Value(1));
						concurrents.add(new Record.Update(rcTmp, persistor));
					}
				}
			}

			/* Analyze next. */
			int indexNext = -1;
			for (int j = i + 1; j < listPersistor.size(); j++) {
				Record rcTmp = listPersistor.getRecord(j);
				int pivotTmp = rcTmp.getValue(DB.FIELD_STATES_PIVOT_CALC).getInteger();
				if (pivotTmp != 0) {
					indexNext = j;
					break;
				}
			}
			if (indexNext == -1) {
				indexNext = listPersistor.size() - 1;
			}
			Record rcNext = listPersistor.getRecord(indexNext);
			double valueNext = rcNext.getValue(DB.FIELD_STATES_REFV_CALC).getDouble();
			double evalNext = Math.abs(value - valueNext) * percentCalc / 100;
			boolean labelNextSet = false;
			for (int j = indexPrev; j > index; j--) {
				Record rcTmp = listPersistor.getRecord(j);
				if (!labelNextSet) {
					double valueTmp = rcTmp.getValue(DB.FIELD_STATES_REFV_CALC).getDouble();
					if (Math.abs(value - valueTmp) <= evalNext) {
						rcTmp.setValue(DB.FIELD_STATES_LABEL_CALC, new Value(0));
						rcTmp.setValue(DB.FIELD_STATES_LABEL_CALC_SET, new Value(1));
						concurrents.add(new Record.Update(rcTmp, persistor));
						labelNextSet = true;
					}
				} else {
					rcTmp.setValue(DB.FIELD_STATES_LABEL_CALC, new Value(0));
					rcTmp.setValue(DB.FIELD_STATES_LABEL_CALC_SET, new Value(1));
					concurrents.add(new Record.Update(rcTmp, persistor));
				}
			}
			if (concurrents.size() >= maxConcurrent) {
				pool.invokeAll(concurrents);
				concurrents.clear();
			}
			
			if (concurrents.size() >= maxConcurrent) {
				pool.invokeAll(concurrents);
				concurrents.clear();
			}

		}
		if (!concurrents.isEmpty()) {
			pool.invokeAll(concurrents);
			concurrents.clear();
		}
		pool.shutdown();
	}

}
