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
import com.mlt.db.PersistorException;
import com.mlt.db.Record;
import com.mlt.db.RecordSet;
import com.mlt.db.Table;
import com.mlt.db.Value;
import com.mlt.db.View;
import com.mlt.db.rdbms.DBPersistor;
import com.mlt.util.Strings;

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
		long totalWork = stats.getFieldNamesToNormalizeStates().size();
		List<Average> avgs = stats.getAverages();
		int candlesNormalize = stats.getFieldNamesToNormalizeCandles().size();
		totalWork += avgs.size() * candlesNormalize;
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

		/* List of names to normalize. */
		List<String> names;

		/* Normalize states. */
		names = stats.getFieldNamesToNormalizeStates();
		long totalWork = getTotalWork();
		long workDone = 0;
		for (int i = 0; i < names.size(); i++) {

			/* Check cancel requested. */
			if (isCancelRequested()) {
				setCancelled();
				break;
			}

			/* Retrieve values. */
			String name = names.get(i);
			String name_raw = DB.name_suffix(name, "raw");
			workDone += 1;
			update(name_raw, workDone, totalWork);
			Record rcView = getDataStates(name_raw);
			double minimum = rcView.getValue(DB.FIELD_RANGE_MINIMUM).getDouble();
			double maximum = rcView.getValue(DB.FIELD_RANGE_MAXIMUM).getDouble();
			double average = rcView.getValue(DB.FIELD_RANGE_AVERAGE).getDouble();
			double std_dev = rcView.getValue(DB.FIELD_RANGE_STDDEV).getDouble();

			Record record = ranges.getDefaultRecord();
			record.setValue(DB.FIELD_RANGE_NAME, name);
			record.setValue(DB.FIELD_RANGE_MINIMUM, minimum);
			record.setValue(DB.FIELD_RANGE_MAXIMUM, maximum);
			record.setValue(DB.FIELD_RANGE_AVERAGE, average);
			record.setValue(DB.FIELD_RANGE_STDDEV, std_dev);

			ranges.getPersistor().insert(record);
		}
		if (isCancelled()) {
			return;
		}

		/* Normalize candles. */
		names = stats.getFieldNamesToNormalizeCandles();
		int pad = stats.getCandlePad();
		List<Average> avgs = stats.getAverages();
		for (int i = 0; i < avgs.size(); i++) {

			int size = stats.getCandleSize(i);
			for (int j = 0; j < names.size(); j++) {

				/* Check cancel requested. */
				if (isCancelRequested()) {
					setCancelled();
					break;
				}

				/* Retrieve values. */
				String name = names.get(j);
				String name_raw = DB.name_suffix(name, "raw");
				String name_pad = name + "_" + Strings.leftPad(Integer.toString(size), pad, "0");
				workDone += 1;
				update(name_raw, workDone, totalWork);

				Record rcView = getDataCandles(size, name_raw);
				double minimum = rcView.getValue(DB.FIELD_RANGE_MINIMUM).getDouble();
				double maximum = rcView.getValue(DB.FIELD_RANGE_MAXIMUM).getDouble();
				double average = rcView.getValue(DB.FIELD_RANGE_AVERAGE).getDouble();
				double std_dev = rcView.getValue(DB.FIELD_RANGE_STDDEV).getDouble();

				Record record = ranges.getDefaultRecord();
				record.setValue(DB.FIELD_RANGE_NAME, name_pad);
				record.setValue(DB.FIELD_RANGE_MINIMUM, minimum);
				record.setValue(DB.FIELD_RANGE_MAXIMUM, maximum);
				record.setValue(DB.FIELD_RANGE_AVERAGE, average);
				record.setValue(DB.FIELD_RANGE_STDDEV, std_dev);

				ranges.getPersistor().insert(record);
			}

			if (isCancelled()) {
				break;
			}
		}
	}

	/**
	 * @param size The size of the candles.
	 * @param name The field name.
	 * @return The record of the view to calculate the range for a field..
	 */
	private Record getDataCandles(int size, String name) throws PersistorException {

		View view = new View();
		view.setMasterTable(stats.getTableCandles());

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

		Field fSIZE = stats.getTableCandles().getField(DB.FIELD_CANDLE_SIZE);
		Criteria criteria = new Criteria();
		criteria.add(Condition.fieldEQ(fSIZE, new Value(size)));

		RecordSet rs = view.getPersistor().select(criteria);
		Record rc = rs.get(0);
		return rc;

	}

	/**
	 * @param name The field name.
	 * @return The record of the view to calculate the range for a field..
	 */
	private Record getDataStates(String name) throws PersistorException {

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
