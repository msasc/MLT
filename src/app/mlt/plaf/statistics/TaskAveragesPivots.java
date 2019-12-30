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
 * Calculate the zigzag pivots to later determine up-down-out zones.
 *
 * @author Miquel Sas
 */
public class TaskAveragesPivots extends TaskAverages {

	/** Origin persistor. */
	private Persistor persistor;
	/** List persistor on table states. */
	private ListPersistor listPersistor;
	/** Number of bars ahead, backward or forward. */
	private int barsAhead;
	/** Alias to eval zigzag. */
	private String alias = DB.FIELD_BAR_CLOSE;

	/**
	 * Constructor.
	 * 
	 * @param stats The statistics averages.
	 */
	public TaskAveragesPivots(StatisticsAverages stats) {
		super(stats);
		setId("averages-zigzag");
		setTitle(stats.getLabel() + " - Calculate zig-zag pivots");
		persistor = stats.getTableSources().getPersistor();
		listPersistor = new ListPersistor(persistor);
		barsAhead = stats.getBarsAhead();
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
		map.put(DB.FIELD_SOURCES_PIVOT_CALC, new Value(0));
		map.put(DB.FIELD_SOURCES_REFV_CALC, new Value(0.0));
		persistor.update(new Criteria(), map);
		
		/* Count. */
		calculateTotalWork();
		
		/* Concurrent pool. */
		Concurrent concurrent = new Concurrent(50, 500);

		/* Previous pivot tracked and its index. */
		int previousPivot = 0;
		int previousIndex = -1;
		
		/* Iterate states. */
		for (int i = 0; i < listPersistor.size(); i++) {

			/* Check cancel requested. */
			if (isCancelRequested()) {
				setCancelled();
				break;
			}

			/* Retrieve record. */
			Record rcSrc = listPersistor.getRecord(i);

			/* Notify work. */
			StringBuilder b = new StringBuilder();
			b.append(rcSrc.toString(DB.FIELD_BAR_TIME_FMT));
			b.append(", ");
			b.append(rcSrc.toString(DB.FIELD_BAR_OPEN));
			b.append(", ");
			b.append(rcSrc.toString(DB.FIELD_BAR_HIGH));
			b.append(", ");
			b.append(rcSrc.toString(DB.FIELD_BAR_LOW));
			b.append(", ");
			b.append(rcSrc.toString(DB.FIELD_BAR_CLOSE));
			update(b.toString(), (i + 1), listPersistor.size());
			
			/* Current value to compare with. */
			double value = rcSrc.getValue(alias).getDouble();
			rcSrc.setValue(DB.FIELD_SOURCES_REFV_CALC, value);

			/* Process only if bars ahead on both sides. */
			if (i < barsAhead || listPersistor.size() - 1 - i < barsAhead) {
				concurrent.add(new Record.Update(rcSrc, persistor));
				continue;
			}

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
				if (!topBackward && !bottomBackward) {
					break;
				}
				if (previousIndex >= 0 && j == previousIndex) {
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
				if (!topForward && !bottomForward) {
					break;
				}
			}

			/* Check state. */
			if (topBackward && topForward && bottomBackward && bottomForward) {
				throw new IllegalStateException("Illegal state: both top and bottom");
			}

			/* Set zigzag pivot. */
			int pivot = 0;
			if ((topForward || bottomForward) && (topBackward || bottomBackward)) {
				if (topBackward && topForward) {
					pivot = 1;
				}
				if (bottomBackward && bottomForward) {
					pivot = -1;
				}
				if (previousPivot != 0) {
					if (pivot == previousPivot) {
						pivot = 0;
					}
				}
			}
			
			/* Register last pivot. */
			if (pivot != 0) {
				previousPivot = pivot;
				previousIndex = i;
			}

			/* Update. */
			rcSrc.setValue(DB.FIELD_SOURCES_PIVOT_CALC, pivot);
			concurrent.add(new Record.Update(rcSrc, persistor));
		}
		concurrent.end();
	}

}
