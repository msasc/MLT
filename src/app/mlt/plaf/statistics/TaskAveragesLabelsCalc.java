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

import com.mlt.db.ListPersistor;
import com.mlt.db.Persistor;
import com.mlt.db.Record;

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

		/* Count. */
		calculateTotalWork();

		/* Indexes of previous, current and next pivots, and its values. */
		int indexPrev = -1;
		int pivotPrev = 0;
		int indexCurr = -1;
		int pivotCurr = 0;
		int indexNext = -1;
		int pivotNext = 0;

		/* Iterate states. */
		for (int i = 0; i < listPersistor.size(); i++) {

			/* Check cancel requested. */
			if (isCancelRequested()) {
				setCancelled();
				break;
			}

			/* Retrieve record. */
			Record rcCurr = listPersistor.getRecord(i);

			/* Notify work. */
			StringBuilder b = new StringBuilder();
			b.append(rcCurr.toString(DB.FIELD_BAR_TIME_FMT));
			b.append(", ");
			b.append(rcCurr.toString(DB.FIELD_BAR_OPEN));
			b.append(", ");
			b.append(rcCurr.toString(DB.FIELD_BAR_HIGH));
			b.append(", ");
			b.append(rcCurr.toString(DB.FIELD_BAR_LOW));
			b.append(", ");
			b.append(rcCurr.toString(DB.FIELD_BAR_CLOSE));
			update(b.toString(), (i + 1), listPersistor.size());

			/*
			 * Must have 3 pivots to determine the minimum amount to apply to both sides of
			 * the central pivot.
			 */
			int pivot = rcCurr.getValue(DB.FIELD_STATES_PIVOT_CALC).getInteger();
			if (pivot != 0) {
				if (indexPrev == -1) {
					indexPrev = i;
					pivotPrev = pivot;
				} else if (indexCurr == -1) {
					indexCurr = i;
					pivotCurr = pivot;
				} else if (indexNext == -1) {
					indexNext = i;
					pivotNext = pivot;
				}
			}

			/* Check all three set. */
			if (indexPrev == -1 || indexCurr == -1 || indexNext == -1) {
				continue;
			}

			/* Process pivots. */
			Record rcPrev = listPersistor.getRecord(indexPrev);
			Record rcNext = listPersistor.getRecord(indexNext);
			double refPrev = rcPrev.getValue(DB.FIELD_STATES_REFV_CALC).getInteger();
			double refCurr = rcCurr.getValue(DB.FIELD_STATES_REFV_CALC).getInteger();
			double refNext = rcNext.getValue(DB.FIELD_STATES_REFV_CALC).getInteger();
			
			/* Reset. */
			indexPrev = -1;
			pivotPrev = 0;
			indexCurr = -1;
			pivotCurr = 0;
			indexNext = -1;
			pivotNext = 0;
		}
	}

}
