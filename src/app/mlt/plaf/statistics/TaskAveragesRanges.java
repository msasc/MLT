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

import java.util.List;

import com.mlt.db.Field;
import com.mlt.db.PersistorException;
import com.mlt.db.Record;
import com.mlt.db.RecordSet;
import com.mlt.db.Table;
import com.mlt.db.View;
import com.mlt.db.rdbms.DBPersistor;

import app.mlt.plaf.DB;
import app.mlt.plaf.MLT;
import app.mlt.plaf.db.Domains;
import app.mlt.plaf.db.Fields;

/**
 * Calculate min-max values for all raw values that must be normalized.
 *
 * @author Miquel Sas
 */
public class TaskAveragesRanges extends TaskAverages {

	/**
	 * Constructor.
	 * 
	 * @param stats The statistics averages.
	 */
	public TaskAveragesRanges(StatisticsAverages stats) {
		super(stats);
		setId("averages-ranges");
		setTitle(stats.getLabel() + " - Calculate min-max raw values");
		setEstimateSpeed(false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected long calculateTotalWork() throws Throwable {
		long totalWork = stats.getFieldListToNormalize().size();
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

		/* Do iterate fields to normalize. */
		List<Field> fields = stats.getFieldListToNormalize();
		long totalWork = getTotalWork();
		long workDone = 0;
		for (int i = 0; i < fields.size(); i++) {

			/* Check cancel requested. */
			if (isCancelRequested()) {
				setCancelled();
				break;
			}
			
			/* Retrieve values. */
			String name = fields.get(i).getName();
			workDone = i + 1;
			update(name, workDone, totalWork);
			Record rcView = getData(name);
			double minimum = rcView.getValue(Fields.RANGE_MINIMUM).getDouble();
			double maximum = rcView.getValue(Fields.RANGE_MAXIMUM).getDouble();
			double average = rcView.getValue(Fields.RANGE_AVERAGE).getDouble();
			double std_dev = rcView.getValue(Fields.RANGE_STDDEV).getDouble();
			Record record = ranges.getDefaultRecord();
			record.setValue(Fields.RANGE_NAME, name);
			record.setValue(Fields.RANGE_MINIMUM, minimum);
			record.setValue(Fields.RANGE_MAXIMUM, maximum);
			record.setValue(Fields.RANGE_AVERAGE, average);
			record.setValue(Fields.RANGE_STDDEV, std_dev);
			ranges.getPersistor().insert(record);
		}
	}

	/**
	 * Return record the view to calculate the range for a field.
	 * 
	 * @param name The field name.
	 * @return The view.
	 * @throws PersistorException 
	 */
	private Record getData(String name) throws PersistorException {
		
		View view = new View();
		view.setMasterTable(stats.getTableStates());
		
		Field minimum = Domains.getDouble(Fields.RANGE_MINIMUM, "Minimum");
		minimum.setFunction("min(" + name + ")");
		view.addField(minimum);
		
		Field maximum = Domains.getDouble(Fields.RANGE_MAXIMUM, "Maximum");
		maximum.setFunction("max(" + name + ")");
		view.addField(maximum);

		Field average = Domains.getDouble(Fields.RANGE_AVERAGE, "Average");
		average.setFunction("avg(" + name + ")");
		view.addField(average);

		Field std_dev = Domains.getDouble(Fields.RANGE_STDDEV, "Std Dev");
		std_dev.setFunction("stddev(" + name + ")");
		view.addField(std_dev);

		view.setPersistor(new DBPersistor(MLT.getDBEngine(), view));
		
		RecordSet rs = view.getPersistor().select(null);
		Record rc = rs.get(0);
		return rc;
	}
}
