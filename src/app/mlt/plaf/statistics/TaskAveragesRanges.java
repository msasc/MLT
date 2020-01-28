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

import com.mlt.db.Field;
import com.mlt.db.Persistor;
import com.mlt.db.PersistorException;
import com.mlt.db.Record;
import com.mlt.db.RecordSet;
import com.mlt.db.Table;
import com.mlt.db.View;
import com.mlt.db.rdbms.DBPersistor;

import app.mlt.plaf.DB;
import app.mlt.plaf.MLT;

/**
 * Calculate min-max-avg-stddev values for all fields that must be normalized.
 *
 * @author Miquel Sas
 */
public class TaskAveragesRanges extends TaskAverages {
	
	private Persistor persistorRanges;
	private List<Field> fields;

	/**
	 * @param stats
	 */
	public TaskAveragesRanges(Statistics stats) {
		super(stats);
		setId("averages-ranges");
		setTitle(stats.getLabel() + " - Calculate min-max raw values");
		
		persistorRanges = stats.getTableRanges().getPersistor();
		fields = new ArrayList<>(stats.getFieldListPatterns(true));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected long calculateTotalWork() throws Throwable {
		setTotalWork(fields.size());
		return getTotalWork();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void compute() throws Throwable {
		
		/* Always calculate from scratch drop/create the table. */
		Table tableRanges = stats.getTableRanges();
		if (DB.ddl().existsTable(tableRanges)) {
			DB.ddl().dropTable(tableRanges);
		}
		DB.ddl().buildTable(tableRanges);
		
		/* Count. */
		calculateTotalWork();

		/* Iterate fields. */
		long totalWork = getTotalWork();
		long workDone = 0;
		for (Field field : fields) {

			/* Check cancel requested. */
			if (isCancelRequested()) {
				setCancelled();
				break;
			}
			
			/* Retrieve values. */
			String name = field.getAlias();
			workDone += 1;
			update(name, workDone, totalWork);
			Record rcView = getDataRaw(name);
			double minimum = rcView.getValue(DB.FIELD_RANGE_MINIMUM).getDouble();
			double maximum = rcView.getValue(DB.FIELD_RANGE_MAXIMUM).getDouble();
			double average = rcView.getValue(DB.FIELD_RANGE_AVERAGE).getDouble();
			double std_dev = rcView.getValue(DB.FIELD_RANGE_STDDEV).getDouble();
			
			Record rcRanges = persistorRanges.getDefaultRecord();
			rcRanges.setValue(DB.FIELD_RANGE_NAME, name);
			rcRanges.setValue(DB.FIELD_RANGE_MINIMUM, minimum);
			rcRanges.setValue(DB.FIELD_RANGE_MAXIMUM, maximum);
			rcRanges.setValue(DB.FIELD_RANGE_AVERAGE, average);
			rcRanges.setValue(DB.FIELD_RANGE_STDDEV, std_dev);
			persistorRanges.insert(rcRanges);
		}
	}

	/**
	 * @param name The field name.
	 * @return The record of the view to calculate the range for a field.
	 */
	private Record getDataRaw(String name) throws PersistorException {

		View view = new View();
		view.setMasterTable(stats.getTableRaw());

		Field minimum = DB.field_double(DB.FIELD_RANGE_MINIMUM, "Minimum");
		minimum.setFunction("min(" + name + ")");
		view.addField(minimum);

		Field maximum = DB.field_double(DB.FIELD_RANGE_MAXIMUM, "Maximum");
		maximum.setFunction("max(" + name + ")");
		view.addField(maximum);

		Field average = DB.field_double(DB.FIELD_RANGE_AVERAGE, "Average");
		average.setFunction("avg(" + name + ")");
		view.addField(average);

		Field std_dev = DB.field_double(DB.FIELD_RANGE_STDDEV, "Std Dev");
		std_dev.setFunction("stddev(" + name + ")");
		view.addField(std_dev);

		view.setPersistor(new DBPersistor(MLT.getDBEngine(), view));

		RecordSet rs = view.getPersistor().select(null);
		Record rc = rs.get(0);
		return rc;
	}
}
