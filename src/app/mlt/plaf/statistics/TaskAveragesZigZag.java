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
 * Calculate the starting zigzag pivots to later determine up-down zones.
 *
 * @author Miquel Sas
 */
public class TaskAveragesZigZag extends TaskAverages {

	/** Origin persistor. */
	private Persistor persistor;
	/** List persistor on table states. */
	private ListPersistor listPersistor;
	/** Number of bars ahead, backward or forward. */
	private int barsAhead = 10;
	/** Alias to eval zigzag. */
	private String alias = DB.FIELD_BAR_CLOSE;

	/**
	 * Constructor.
	 * 
	 * @param stats The statistics averages.
	 */
	public TaskAveragesZigZag(StatisticsAverages stats) {
		super(stats);
		setId("averages-zigzag");
		setTitle(stats.getLabel() + " - Calculate zig-zag");
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

		/* Iterate states. */
		for (int i = 0; i < listPersistor.size(); i++) {

			/* Check cancel requested. */
			if (isCancelRequested()) {
				setCancelled();
				break;
			}

			/* Retrieve record. */
			Record rcStates = listPersistor.getRecord(i);

			/* Notify work. */
			StringBuilder b = new StringBuilder();
			b.append(rcStates.toString(DB.FIELD_BAR_TIME_FMT));
			b.append(", ");
			b.append(rcStates.toString(DB.FIELD_BAR_OPEN));
			b.append(", ");
			b.append(rcStates.toString(DB.FIELD_BAR_HIGH));
			b.append(", ");
			b.append(rcStates.toString(DB.FIELD_BAR_LOW));
			b.append(", ");
			b.append(rcStates.toString(DB.FIELD_BAR_CLOSE));
			update(b.toString(), (i + 1), listPersistor.size());

			/* Current value to caompare with. */
			double value = rcStates.getValue(alias).getDouble();
			rcStates.setValue(DB.FIELD_STATES_REFV, value);

			/* Move backward. */
			boolean topBackward = true;
			boolean bottomBackward = true;
			int backwardIndex = Math.max(0, i - barsAhead);
			for (int j = i - 1; j >= backwardIndex; j--) {
				double check = listPersistor.getRecord(j).getValue(alias).getDouble();
				if (check >= value) {
					topBackward = false;
				}
				if (check <= value) {
					bottomBackward = false;
				}
			}

			/* Move forward. */
			boolean topForward = true;
			boolean bottomForward = true;
			int forwardIndex = Math.min(listPersistor.size() - 1, i + barsAhead);
			for (int j = i + 1; j <= forwardIndex; j++) {
				double check = listPersistor.getRecord(j).getValue(alias).getDouble();
				if (check >= value) {
					topForward = false;
				}
				if (check <= value) {
					bottomForward = false;
				}
			}

			/* Set zigzag pivot. */
			rcStates.setValue(DB.FIELD_STATES_PIVOT, 0);
			if (i >= barsAhead && listPersistor.size() - i > barsAhead) {
				if (topBackward && topForward) {
					rcStates.setValue(DB.FIELD_STATES_PIVOT, 1);
				} else if (bottomBackward && bottomForward) {
					rcStates.setValue(DB.FIELD_STATES_PIVOT, -1);
				} else {
					rcStates.setValue(DB.FIELD_STATES_PIVOT, 0);
				}
			}

			/* Update. */
			persistor.update(rcStates);
		}
	}

}
