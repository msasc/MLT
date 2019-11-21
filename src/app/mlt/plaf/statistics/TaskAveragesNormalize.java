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

import java.util.HashMap;
import java.util.List;

import com.mlt.db.Condition;
import com.mlt.db.Criteria;
import com.mlt.db.Field;
import com.mlt.db.Persistor;
import com.mlt.db.Record;
import com.mlt.db.RecordIterator;
import com.mlt.db.Table;
import com.mlt.db.Value;
import com.mlt.ml.function.Normalizer;

import app.mlt.plaf.db.Fields;

/**
 * Normalize raw values.
 *
 * @author Miquel Sas
 */
public class TaskAveragesNormalize extends TaskAverages {

	/**
	 * Constructor.
	 * 
	 * @param stats The statistics averages.
	 */
	public TaskAveragesNormalize(StatisticsAverages stats) {
		super(stats);
		setId("averages-nrm");
		setTitle(stats.getLabel() + " - Normalize values");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected long calculateTotalWork() throws Throwable {
		setTotalWork(stats.getTableStates().getPersistor().count(getSelectCriteria()));
		return getTotalWork();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void compute() throws Throwable {

		/* Count. */
		calculateTotalWork();

		/* List of fields to normalize (raw values) and normalizers. */
		List<Field> fields = stats.getFieldListToNormalize();
		HashMap<String, Normalizer> normalizers = stats.getMapNormalizers();
		if (normalizers.size() != fields.size()) {
			throw new IllegalStateException("Bad normalizers");
		}

		/* Iterate states. */
		long totalWork = getTotalWork();
		long workDone = 0;
		Table states = stats.getTableStates();
		RecordIterator iter =
			states.getPersistor().iterator(getSelectCriteria(), states.getPrimaryKey());
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

			/* Fields to normalize. */
			for (Field field : fields) {
				String name_raw = field.getName();
				String name_nrm = name_raw.substring(0, name_raw.length() - 3) + "nrm";
				Normalizer normalizer = normalizers.get(name_raw);
				double value_raw = rcStates.getValue(name_raw).getDouble();
				double value_nrm = normalizer.normalize(value_raw);
				rcStates.setValue(name_nrm, value_nrm);
			}
			rcStates.setValue(Fields.STATES_NORMALIZED, "Y");

			/* Update. */
			states.getPersistor().update(rcStates);
		}
		iter.close();
	}

	private Criteria getSelectCriteria() {
		Persistor persistor = stats.getTableStates().getPersistor();
		Field field = persistor.getField(Fields.STATES_NORMALIZED);
		Criteria criteria = new Criteria();
		criteria.add(Condition.fieldEQ(field, new Value("")));
		return criteria;
	}
}
