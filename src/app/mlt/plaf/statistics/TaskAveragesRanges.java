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
import com.mlt.db.Field;
import com.mlt.db.Record;
import com.mlt.db.RecordIterator;
import com.mlt.db.Table;
import com.mlt.db.Value;
import com.mlt.task.Task;
import com.mlt.util.FixedSizeList;

import app.mlt.plaf.DB;
import app.mlt.plaf.db.Fields;

/**
 * Calculate min-max values for all raw values that must be normalized, for the
 * 2 slowest periods.
 *
 * @author Miquel Sas
 */
public class TaskAveragesRanges extends Task {

	/** Underlying statistics averages. */
	private StatisticsAverages stats;

	/**
	 * Constructor.
	 * 
	 * @param stats The statistics averages.
	 */
	public TaskAveragesRanges(StatisticsAverages stats) {
		super();
		this.stats = stats;
		setId("averages-ranges");
		setTitle(stats.getLabel() + " - Calculate min-max raw values");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected long calculateTotalWork() throws Throwable {
		Table states = stats.getTableStates();
		long totalWork = states.getPersistor().count((Criteria) null);
		setTotalWork(totalWork);
		return getTotalWork();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void compute() throws Throwable {

		/* To calculate from scratch drop/create the table. */
		Table ranges = stats.getTableRanges();
		if (DB.ddl().existsTable(ranges)) {
			DB.ddl().dropTable(ranges);
		}
		DB.ddl().buildTable(ranges);

		/* Count. */
		calculateTotalWork();

		/* List of states record buffers. */
		List<FixedSizeList<Record>> buffers = createBuffers();

		/* List of fields to normalize (raw values) */
		List<Field> fields = stats.getFieldListToNormalize();

		/* Iterate states. */
		long totalWork = getTotalWork();
		long workDone = 0;
		Table states = stats.getTableStates();
		RecordIterator iter = states.getPersistor().iterator(null, states.getPrimaryKey());
		while (iter.hasNext()) {

			/* Check cancel requested. */
			if (isCancelRequested()) {
				setCancelled();
				break;
			}

			/* Retrieve record. */
			Record rcStates = iter.next();

			/* Notify work. */
			workDone++;
			if (workDone % 100 == 0 || workDone == totalWork) {
				StringBuilder b = new StringBuilder();
				b.append(rcStates.toString(Fields.BAR_TIME_FMT));
				b.append(", ");
				b.append(rcStates.toString(Fields.BAR_OPEN));
				b.append(", ");
				b.append(rcStates.toString(Fields.BAR_HIGH));
				b.append(", ");
				b.append(rcStates.toString(Fields.BAR_LOW));
				b.append(", ");
				b.append(rcStates.toString(Fields.BAR_CLOSE));
				update(b.toString(), workDone, totalWork);
			}
			
			/* Push the record in the buffers. */
			for (FixedSizeList<Record> buffer : buffers) {
				buffer.add(rcStates);
			}

			/* Check trigger calculations. */
			for (FixedSizeList<Record> buffer : buffers) {
				if (buffer.size() == buffer.getMaximumSize()) {
					int period = buffer.getMaximumSize();
					for (Field field : fields) {
						double maxValue = Double.MIN_VALUE;
						long maxTime = -1;
						double minValue = Double.MAX_VALUE;
						long minTime = -1;
						String name = field.getAlias();
						for (int i = 0; i < buffer.size(); i++) {
							Record rc = buffer.getHead(i);
							long time = rc.getValue(Fields.BAR_TIME).getLong();
							double value = rc.getValue(name).getDouble();
							if (value > maxValue) {
								maxValue = value;
								maxTime = time;
							}
							if (value < minValue) {
								minValue = value;
								minTime = time;
							}
						}
						if (maxTime >= 0) {
							Record rcMax = ranges.getDefaultRecord();
							rcMax.setValue(Fields.RANGE_NAME, new Value(name));
							rcMax.setValue(Fields.RANGE_MIN_MAX, new Value("max"));
							rcMax.setValue(Fields.RANGE_PERIOD, new Value(period));
							rcMax.setValue(Fields.RANGE_VALUE, new Value(maxValue));
							rcMax.setValue(Fields.BAR_TIME, new Value(maxTime));
							ranges.getPersistor().insert(rcMax);
						}
						if (minTime >= 0) {
							Record rcMin = ranges.getDefaultRecord();
							rcMin.setValue(Fields.RANGE_NAME, new Value(name));
							rcMin.setValue(Fields.RANGE_MIN_MAX, new Value("min"));
							rcMin.setValue(Fields.RANGE_PERIOD, new Value(period));
							rcMin.setValue(Fields.RANGE_VALUE, new Value(minValue));
							rcMin.setValue(Fields.BAR_TIME, new Value(minTime));
							ranges.getPersistor().insert(rcMin);
						}
					}
					buffer.clear();
				}
			}
		}
		iter.close();
	}

	/**
	 * Create the list of buffers. The buffer maximum size is the average period and
	 * triggers the scan to calculate ranges.
	 * 
	 * @return The list of states record buffers.
	 */
	private List<FixedSizeList<Record>> createBuffers() {
		List<Average> avgs = stats.getAverages();
		List<FixedSizeList<Record>> buffers = new ArrayList<>();
		if (avgs.size() > 1) {
			buffers.add(new FixedSizeList<>(avgs.get(avgs.size() - 2).getPeriod()));
		}
		buffers.add(new FixedSizeList<>(avgs.get(avgs.size() - 1).getPeriod()));
		return buffers;
	}
}
