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

import com.mlt.db.Criteria;
import com.mlt.db.ListPersistor;
import com.mlt.db.Persistor;
import com.mlt.db.Record;
import com.mlt.db.Value;
import com.mlt.db.ValueMap;
import com.mlt.task.Concurrent;

import app.mlt.plaf.DB;

/**
 * Setup labels on calculated/edited pivots.
 *
 * @author Miquel Sas
 */
public class TaskLabels extends TaskStatistics {

	/** Origin persistor. */
	private Persistor persistor;
	/** List persistor on table sources. */
	private ListPersistor listPersistor;
	/** Calculated/edited flag. */
	private boolean calculated;

	/**
	 * @param stats
	 */
	public TaskLabels(Statistics stats, boolean calculated) {
		super(stats);
		this.calculated = calculated;
		
		setId("averages-label-" + (calculated ? "calc" : "edit"));
		StringBuilder title = new StringBuilder();
		title.append(stats.getLabel());
		title.append(" - Labels on ");
		title.append(calculated ? "calculated" : "edited");
		title.append(" pivots");
		setTitle(title.toString());
		persistor = stats.getTableSrc().getPersistor();
		listPersistor = new ListPersistor(persistor);
	}

	@Override
	protected long calculateTotalWork() throws Throwable {
		long totalWork = listPersistor.size();
		setTotalWork(totalWork);
		return getTotalWork();
	}

	@Override
	protected void compute() throws Throwable {

		/* Reset values. */
		ValueMap map = new ValueMap();
		map.put(DB.FIELD_SOURCES_LABEL_CALC, new Value(""));
		persistor.update(new Criteria(), map);
		
		/* Count. */
		calculateTotalWork();

		/* Concurrent manager. */
		Concurrent concurrent = new Concurrent(10, 50);

		/* Iterate states. */
		double percent;
		String aliasPivot;
		String aliasValue;
		String aliasLabel;
		if (calculated) {
			percent = stats.getParameters().getPercentCalc();
			aliasPivot = DB.FIELD_SOURCES_PIVOT_CALC;
			aliasValue = DB.FIELD_SOURCES_REFV_CALC;
			aliasLabel = DB.FIELD_SOURCES_LABEL_CALC;
		} else {
			percent = stats.getParameters().getPercentCalc();
			aliasPivot = DB.FIELD_SOURCES_PIVOT_EDIT;
			aliasValue = DB.FIELD_SOURCES_REFV_EDIT;
			aliasLabel = DB.FIELD_SOURCES_LABEL_EDIT;
		}
		
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
			int pivot = rc.getValue(aliasPivot).getInteger();
			if (pivot == 0) {
				continue;
			}
			int index = i;
			double value = rc.getValue(aliasValue).getDouble();
			rc.setValue(aliasLabel, new Value("0"));
			concurrent.add(new Record.Update(rc, persistor));

			/* Analyze previous. */
			int indexPrev = -1;
			for (int j = i - 1; j >= 0; j--) {
				Record rcTmp = listPersistor.getRecord(j);
				int pivotTmp = rcTmp.getValue(aliasPivot).getInteger();
				if (pivotTmp != 0) {
					indexPrev = j;
					break;
				}
			}
			if (indexPrev == -1) {
				indexPrev = 0;
			}
			Record rcPrev = listPersistor.getRecord(indexPrev);
			double valuePrev = rcPrev.getValue(aliasValue).getDouble();
			double evalPrev = Math.abs(value - valuePrev) * percent / 100;
			boolean labelPrevSet = false;
			for (int j = indexPrev; j < index; j++) {
				Record rcTmp = listPersistor.getRecord(j);
				if (!labelPrevSet) {
					double valueTmp = rcTmp.getValue(aliasValue).getDouble();
					if (Math.abs(value - valueTmp) <= evalPrev) {
						rcTmp.setValue(aliasLabel, "0");
						concurrent.add(new Record.Update(rcTmp, persistor));
						labelPrevSet = true;
					}
				} else {
					rcTmp.setValue(aliasLabel, "0");
					concurrent.add(new Record.Update(rcTmp, persistor));
				}
			}
			if (!rcPrev.getValue(aliasLabel).getString().isEmpty()) {
				String labelPrev = (pivot == 1 ? "1" : "-1");
				for (int j = indexPrev; j < index; j++) {
					Record rcTmp = listPersistor.getRecord(j);
					if (rcTmp.getValue(aliasLabel).getString().isEmpty()) {
						rcTmp.setValue(aliasLabel, labelPrev);
						concurrent.add(new Record.Update(rcTmp, persistor));
					}
				}
			}

			/* Analyze next. */
			int indexNext = -1;
			for (int j = i + 1; j < listPersistor.size(); j++) {
				Record rcTmp = listPersistor.getRecord(j);
				int pivotTmp = rcTmp.getValue(aliasPivot).getInteger();
				if (pivotTmp != 0) {
					indexNext = j;
					break;
				}
			}
			if (indexNext == -1) {
				indexNext = listPersistor.size() - 1;
			}
			Record rcNext = listPersistor.getRecord(indexNext);
			double valueNext = rcNext.getValue(aliasValue).getDouble();
			double evalNext = Math.abs(value - valueNext) * percent / 100;
			boolean labelNextSet = false;
			for (int j = indexPrev; j > index; j--) {
				Record rcTmp = listPersistor.getRecord(j);
				if (!labelNextSet) {
					double valueTmp = rcTmp.getValue(aliasValue).getDouble();
					if (Math.abs(value - valueTmp) <= evalNext) {
						rcTmp.setValue(aliasLabel, "0");
						concurrent.add(new Record.Update(rcTmp, persistor));
						labelNextSet = true;
					}
				} else {
					rcTmp.setValue(aliasLabel, "0");
					concurrent.add(new Record.Update(rcTmp, persistor));
				}
			}
		}
		concurrent.end();
	}

}
