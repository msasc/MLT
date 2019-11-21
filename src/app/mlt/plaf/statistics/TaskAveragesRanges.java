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

import app.mlt.plaf.DB;
import app.mlt.plaf.db.Fields;

/**
 * Calculate min-max values for all raw values that must be normalized.
 *
 * @author Miquel Sas
 */
public class TaskAveragesRanges extends TaskAverages {

	/**
	 * Track buffer.
	 */
	static class Buffer {
		List<Track> buffer;
		int maxSize;

		Buffer(int maxSize) {
			this.maxSize = maxSize;
			this.buffer = new ArrayList<>(maxSize * 4);
		}

		void add(long time, double value, boolean min) {
			int index = 0;
			for (int i = 0; i < buffer.size(); i++) {
				if (min ? value >= buffer.get(i).value : value <= buffer.get(i).value) {
					break;
				}
				index++;
			}
			buffer.add(index, new Track(time, value));
			if (buffer.size() > maxSize) {
				buffer.remove(0);
			}
		}

		void addMax(long time, double value) {
			add(time, value, false);
		}

		void addMin(long time, double value) {
			add(time, value, true);
		}

	}

	/**
	 * Min-max value and time structure.
	 */
	static class Track {

		long time;
		double value;

		Track(long time, double value) {
			this.time = time;
			this.value = value;
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param stats The statistics averages.
	 */
	public TaskAveragesRanges(StatisticsAverages stats) {
		super(stats);
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

		/* Alwways calculate from scratch drop/create the table. */
		Table ranges = stats.getTableRanges();
		if (DB.ddl().existsTable(ranges)) {
			DB.ddl().dropTable(ranges);
		}
		DB.ddl().buildTable(ranges);

		/* Count. */
		calculateTotalWork();

		/* List of fields to normalize (raw values). Set their buffers. */
		List<Field> fields = stats.getFieldListToNormalize();
		int maxBufferSize = 50;
		for (Field field : fields) {
			field.getProperties().setObject("buffer-max", new Buffer(maxBufferSize));
			field.getProperties().setObject("buffer-min", new Buffer(maxBufferSize));
		}

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
			if (workDone % 10 == 0 || workDone == totalWork) {
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

			/* Iterate fields and buil the list of maximums and minimums per field. */
			for (Field field : fields) {

				Buffer bufferMax = (Buffer) field.getProperties().getObject("buffer-max");
				Buffer bufferMin = (Buffer) field.getProperties().getObject("buffer-min");

				String alias = field.getAlias();
				long time = rcStates.getValue(Fields.BAR_TIME).getLong();
				double value = rcStates.getValue(alias).getDouble();

				bufferMax.addMax(time, value);
				bufferMin.addMin(time, value);
			}
		}
		iter.close();

		/* Save. First calculate new total work. */
		totalWork = 0;
		for (Field field : fields) {
			Buffer bufferMax = (Buffer) field.getProperties().getObject("buffer-max");
			Buffer bufferMin = (Buffer) field.getProperties().getObject("buffer-min");
			totalWork += bufferMax.buffer.size();
			totalWork += bufferMin.buffer.size();
		}
		workDone = 0;
		update("", workDone, totalWork);
		for (Field field : fields) {
			String name = field.getName();
			Buffer bufferMax = (Buffer) field.getProperties().getObject("buffer-max");
			Buffer bufferMin = (Buffer) field.getProperties().getObject("buffer-min");
			for (int i = 0; i < bufferMax.buffer.size(); i++) {
				workDone++;
				update("Save " + name + " - max - " + (i + 1), workDone, totalWork);
				Track track = bufferMax.buffer.get(i);
				Record rc = ranges.getDefaultRecord();
				rc.setValue(Fields.RANGE_NAME, name);
				rc.setValue(Fields.RANGE_MIN_MAX, "max");
				rc.setValue(Fields.RANGE_VALUE, track.value);
				rc.setValue(Fields.BAR_TIME, track.time);
				ranges.getPersistor().insert(rc);
			}
			for (int i = 0; i < bufferMin.buffer.size(); i++) {
				workDone++;
				update("Save " + name + " - min - " + (i + 1), workDone, totalWork);
				Track track = bufferMin.buffer.get(i);
				Record rc = ranges.getDefaultRecord();
				rc.setValue(Fields.RANGE_NAME, name);
				rc.setValue(Fields.RANGE_MIN_MAX, "min");
				rc.setValue(Fields.RANGE_VALUE, track.value);
				rc.setValue(Fields.BAR_TIME, track.time);
				ranges.getPersistor().insert(rc);
			}
		}
	}

}
