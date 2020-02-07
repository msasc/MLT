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
import com.mlt.task.Concurrent;
import com.mlt.util.FixedSizeQueue;

import app.mlt.plaf.DB;

/**
 * Gearate deltas on raw values.
 *
 * @author Miquel Sas
 */
public class TaskRawDeltas extends TaskAverages {

	private Persistor persistorRaw;
	private Persistor persistorDeltas;

	/**
	 * @param stats The statistics averages.
	 */
	public TaskRawDeltas(Statistics stats) {
		super(stats);
		setId("averages-raw-deltas");
		setTitle(stats.getLabel() + " - Calculate raw deltas");

		persistorRaw = stats.getTableRaw().getPersistor();
		persistorDeltas = stats.getTableRawDeltas().getPersistor();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected long calculateTotalWork() throws Throwable {
		setTotalWork(persistorRaw.count(getCriteria()));
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
			tables.add(stats.getTableRawDeltas());
			tables.add(stats.getTableRanges());
			tables.add(stats.getTableNormalized());
			tables.add(stats.getTableNormalizedDeltas());
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

		/* Buffers. */
		int deltasHistory = stats.getDeltasHistory();
		FixedSizeQueue<Record> rawBuffer = new FixedSizeQueue<>(deltasHistory);

		List<Field> fields = stats.getFieldListPatterns(true);

		/* Iterate raw values. */
		Order order = persistorRaw.getView().getMasterTable().getPrimaryKey();
		RecordIterator iter = persistorRaw.iterator(getCriteria(), order);
		while (iter.hasNext()) {

			/* Check cancel requested. */
			if (isCancelRequested()) {
				setCancelled();
				break;
			}

			/* Retrieve record. */
			Record rcRaw = iter.next();
			rawBuffer.add(rcRaw);

			/* Notify work done. */
			workDone++;
			StringBuilder b = new StringBuilder();
			b.append(rcRaw.toString(DB.FIELD_BAR_TIME_FMT));
			update(b.toString(), workDone, totalWork);

			/* Generate deltas history. */
			for (int i = 1; i <= deltasHistory; i++) {
				int index = Math.min(i, rawBuffer.size()) - 1;
				Record rcRawPrev = null;
				if (index > 0) {
					rcRawPrev = rawBuffer.getLast(index);
				} else {
					rcRawPrev = persistorRaw.getDefaultRecord();
				}
				Record rcDeltas = persistorDeltas.getDefaultRecord();
				rcDeltas.setValue(DB.FIELD_BAR_TIME, rcRaw.getValue(DB.FIELD_BAR_TIME));
				rcDeltas.setValue(DB.FIELD_DELTA, i);
				for (Field field : fields) {
					String alias = field.getAlias();
					double curr = rcRaw.getValue(alias).getDouble();
					double prev = rcRawPrev.getValue(alias).getDouble();
					double delta = curr - prev;
					rcDeltas.setValue(alias, delta);
				}
				concurrent.add(new Record.Insert(rcDeltas, persistorDeltas));
			}

		}
		iter.close();
		concurrent.end();
	}

	/**
	 * @return The criteria to select records of the raw table.
	 */
	private Criteria getCriteria() throws Throwable {
		Field ftime = persistorRaw.getField(DB.FIELD_BAR_TIME);
		Value vtime = new Value(getLastDeltasTime());
		Criteria criteria = new Criteria();
		criteria.add(Condition.fieldGT(ftime, vtime));
		return criteria;
	}

	/**
	 * @return The last time of the deltas table.
	 */
	private long getLastDeltasTime() throws Throwable {
		long time = 0;
		Field ftime = persistorDeltas.getField(DB.FIELD_BAR_TIME);
		Order order = new Order();
		order.add(ftime, false);
		RecordIterator iter = persistorDeltas.iterator(null, order);
		if (iter.hasNext()) {
			Record rc = iter.next();
			time = rc.getValue(DB.FIELD_BAR_TIME).getLong();
		}
		iter.close();
		return time;
	}
}
