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

import com.mlt.db.Condition;
import com.mlt.db.Criteria;
import com.mlt.db.Field;
import com.mlt.db.Persistor;
import com.mlt.db.PersistorException;
import com.mlt.db.Record;
import com.mlt.db.RecordSet;
import com.mlt.db.Table;
import com.mlt.db.Value;
import com.mlt.db.View;
import com.mlt.db.rdbms.DBPersistor;

import app.mlt.plaf.DB;
import app.mlt.plaf.MLT;

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
	 * @return The total number of states records to consider.
	 */
	private long countStates() throws Throwable {
		return stats.getTableStates().getPersistor().count(null);
	}

	/**
	 * @param name    The field name.
	 * @param minimum Minimum value.
	 * @param maximum Maximum value.
	 * @return The number of records with the field GE minimum and LE maximum.
	 */
	private long countStates(String name, double minimum, double maximum) throws Throwable {
		Persistor persistor = stats.getTableStates().getPersistor();
		Field field = persistor.getField(name);
		Criteria criteria = new Criteria();
		criteria.add(Condition.fieldGE(field, new Value(minimum)));
		criteria.add(Condition.fieldLE(field, new Value(maximum)));
		long count = persistor.count(criteria);
		return count;
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
		double countStates = countStates();

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
			double minimum = rcView.getValue(DB.FIELD_RANGE_MINIMUM).getDouble();
			double maximum = rcView.getValue(DB.FIELD_RANGE_MAXIMUM).getDouble();
			double average = rcView.getValue(DB.FIELD_RANGE_AVERAGE).getDouble();
			double std_dev = rcView.getValue(DB.FIELD_RANGE_STDDEV).getDouble();
			
			double minimum_10 = average - (1 * std_dev);
			double maximum_10 = average + (1 * std_dev);
			double count_10 = countStates(name, minimum_10, maximum_10);
			double avg_std_10 = 100 * count_10 / countStates;
			
			double minimum_20 = average - (2 * std_dev);
			double maximum_20 = average + (2 * std_dev);
			double count_20 = countStates(name, minimum_20, maximum_20);
			double avg_std_20 = 100 * count_20 / countStates;
			
			Record record = ranges.getDefaultRecord();
			record.setValue(DB.FIELD_RANGE_NAME, name);
			record.setValue(DB.FIELD_RANGE_MINIMUM, minimum);
			record.setValue(DB.FIELD_RANGE_MAXIMUM, maximum);
			record.setValue(DB.FIELD_RANGE_AVERAGE, average);
			record.setValue(DB.FIELD_RANGE_STDDEV, std_dev);
			record.setValue(DB.FIELD_RANGE_AVG_STD_10, avg_std_10);
			record.setValue(DB.FIELD_RANGE_AVG_STD_20, avg_std_20);
			
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
