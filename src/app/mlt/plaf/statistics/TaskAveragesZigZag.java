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
import com.mlt.desktop.Option;

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
	private int barsAhead = 100;
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
		
		/* Query option. */
		Option option = queryOption();
		if (option.equals("CANCEL")) {
			throw new Exception("Calculation cancelled by user.");
		}
		if (option.equals("START")) {
			ValueMap map = new ValueMap();
			map.put(DB.FIELD_STATES_PIVOT_CALC, new Value(0));
			map.put(DB.FIELD_STATES_REFV, new Value(0.0));
			persistor.update(new Criteria(), map);
		}
		
		/* Count. */
		calculateTotalWork();

		/* Last pivot tracked and its index. */
		int lastPivot = 0;
		int lastIndex = -1;
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

			/* Current value to compare with. */
			double value = rcStates.getValue(alias).getDouble();
			rcStates.setValue(DB.FIELD_STATES_REFV, value);

			/* Move backward up to bars ahead or last pivot index. */
			boolean topBackward = true;
			boolean bottomBackward = true;
			int backwardIndex = Math.max(0, i - barsAhead);
			for (int j = i - 1; j >= backwardIndex; j--) {
				double check = listPersistor.getRecord(j).getValue(alias).getDouble();
				if (check >= value) {
					topBackward = false;
				}
				if (check < value) {
					bottomBackward = false;
				}
				if (lastIndex >= 0 && j == lastIndex) {
					break;
				}
			}

			/* Move forward up to bars ahead. */
			boolean topForward = true;
			boolean bottomForward = true;
			int forwardIndex = Math.min(listPersistor.size() - 1, i + barsAhead);
			for (int j = i + 1; j <= forwardIndex; j++) {
				double check = listPersistor.getRecord(j).getValue(alias).getDouble();
				if (check >= value) {
					topForward = false;
				}
				if (check < value) {
					bottomForward = false;
				}
			}

			/* Check state. */
			if (topBackward && topForward && bottomBackward && bottomForward) {
				throw new IllegalStateException("Illegal state: both top and bottom");
			}

			/* Set zigzag pivot. */
			int pivot = 0;
			/* Track pivot only if at least there are bars ahead on both sides. */
			if (i >= barsAhead && listPersistor.size() - i > barsAhead) {
				if (topBackward && topForward) {
					pivot = 1;
				}
				if (bottomBackward && bottomForward) {
					pivot = -1;
				}
			}
			
			/* Check that pivot, if set, is the inverse of last one. */
			if (pivot != 0 && lastPivot != 0) {
				if (pivot == lastPivot) {
//					throw new IllegalStateException("Illegal state: pivot repeated");
					pivot = 0;
				}
			}

			/* Update. */
			rcStates.setValue(DB.FIELD_STATES_PIVOT_CALC, pivot);
			persistor.update(rcStates);
			
			/* Register last pivot. */
			if (pivot != 0) {
				lastPivot = pivot;
				lastIndex = i;
			}
		}
	}

}
