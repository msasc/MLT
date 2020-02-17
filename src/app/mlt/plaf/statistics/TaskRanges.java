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
import com.mlt.db.Order;
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
 * Calculate min-max-avg-stddev values for all fields that must be normalized.
 * 
 * @author Miquel Sas
 */
public class TaskRanges extends TaskStatistics {
	
	private Persistor persistorRng;
	private List<Field> fields;
	private View view;

	/**
	 * @param stats The statistics.
	 */
	public TaskRanges(Statistics stats) {
		super(stats);
		setId("averages-ranges");
		setTitle(stats.getLabel() + " - Calculate min-max raw values");
		
		persistorRng = stats.getTableRng().getPersistor();
		fields = stats.getFieldListPatterns(true);
	}

	@Override
	protected long calculateTotalWork() throws Throwable {
		int size = stats.getParameters().getDeltas().size() + 1;
		setTotalWork(fields.size() * size);
		return getTotalWork();
	}

	@Override
	protected void compute() throws Throwable {
		
		/* Always calculate from scratch drop/create the table. */
		Table tableRng = stats.getTableRng();
		if (DB.ddl().existsTable(tableRng)) {
			DB.ddl().dropTable(tableRng);
		}
		DB.ddl().buildTable(tableRng);
		
		/* Count. */
		calculateTotalWork();

		/* Iterate deltas and fields on deltas. */
		List<Integer> deltas = stats.getParameters().getDeltas();
		long totalWork = getTotalWork();
		long workDone = 0;
		
		for (int delta = 0; delta <= deltas.size(); delta++) {
			
			/* Check cancel requested. */
			if (isCancelRequested()) {
				setCancelled();
				break;
			}
			
			for (Field field : fields) {
				String name = field.getAlias();
				workDone += 1;
				update(name, workDone, totalWork);
				Record rcView = getDataRaw(name, delta);
				double minimum = rcView.getValue(DB.FIELD_RANGE_MINIMUM).getDouble();
				double maximum = rcView.getValue(DB.FIELD_RANGE_MAXIMUM).getDouble();
				double average = rcView.getValue(DB.FIELD_RANGE_AVERAGE).getDouble();
				double std_dev = rcView.getValue(DB.FIELD_RANGE_STDDEV).getDouble();

				Record rcRng = persistorRng.getDefaultRecord();
				rcRng.setValue(DB.FIELD_PATTERN_DELTA, delta);
				rcRng.setValue(DB.FIELD_PATTERN_NAME, name);
				rcRng.setValue(DB.FIELD_RANGE_MINIMUM, minimum);
				rcRng.setValue(DB.FIELD_RANGE_MAXIMUM, maximum);
				rcRng.setValue(DB.FIELD_RANGE_AVERAGE, average);
				rcRng.setValue(DB.FIELD_RANGE_STDDEV, std_dev);
				persistorRng.insert(rcRng);
			}
		}
	}

	/**
	 * @param name The field name.
	 * @return The record of the view to calculate the range for a field.
	 */
	private Record getDataRaw(String name, int deltaIndex) throws PersistorException {
		View view = getView(name);
		
		Field fDELTA = view.getField(DB.FIELD_PATTERN_DELTA);
		
		Criteria criteria = new Criteria();
		criteria.add(Condition.fieldEQ(fDELTA, new Value(deltaIndex)));
		
		Order order = new Order();
		order.add(fDELTA);
		
		RecordSet rs = view.getPersistor().select(criteria, order);
		Record rc = rs.get(0);
		
		return rc;
	}
	
	/**
	 * @param name The field name.
	 * @return The view.
	 */
	private View getView(String name) {
		if (view == null) {

			view = new View();
			Table master = stats.getTableRaw();
			view.setMasterTable(master);

			Field delta = master.getField(DB.FIELD_PATTERN_DELTA);
			view.addField(delta);
			view.addGroupBy(delta);

			Field minimum = DB.field_double(DB.FIELD_RANGE_MINIMUM, "Minimum");
			view.addField(minimum);

			Field maximum = DB.field_double(DB.FIELD_RANGE_MAXIMUM, "Maximum");
			view.addField(maximum);

			Field average = DB.field_double(DB.FIELD_RANGE_AVERAGE, "Average");
			view.addField(average);

			Field std_dev = DB.field_double(DB.FIELD_RANGE_STDDEV, "Std Dev");
			view.addField(std_dev);

			Persistor persistor = new DBPersistor(MLT.getDBEngine(), view);
			view.setPersistor(persistor);
		}
		
		view.getField(DB.FIELD_RANGE_MINIMUM).setFunction("min(" + name + ")");
		view.getField(DB.FIELD_RANGE_MAXIMUM).setFunction("max(" + name + ")");
		view.getField(DB.FIELD_RANGE_AVERAGE).setFunction("avg(" + name + ")");
		view.getField(DB.FIELD_RANGE_STDDEV).setFunction("stddev(" + name + ")");
		
		return view;
	}
}
