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
import java.util.HashMap;
import java.util.List;

import com.mlt.db.Condition;
import com.mlt.db.Criteria;
import com.mlt.db.Field;
import com.mlt.db.Order;
import com.mlt.db.Persistor;
import com.mlt.db.PersistorException;
import com.mlt.db.Record;
import com.mlt.db.RecordIterator;
import com.mlt.db.RecordSet;
import com.mlt.db.Table;
import com.mlt.db.Value;
import com.mlt.desktop.Option;
import com.mlt.ml.function.Normalizer;
import com.mlt.task.Concurrent;

import app.mlt.plaf.DB;

/**
 * Normalize values.
 *
 * @author Miquel Sas
 */
public class TaskAveragesNormalize extends TaskAverages {

	private Persistor persistorRaw;
	private Persistor persistorNrm;

	/**
	 * @param stats The statistics averages.
	 */
	public TaskAveragesNormalize(StatisticsAverages stats) {
		super(stats);
		setId("averages-nrm");
		setTitle(stats.getLabel() + " - Normalize raw values");
		persistorRaw = stats.getTableRaw().getPersistor();
		persistorNrm = stats.getTableNormalized().getPersistor();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected long calculateTotalWork() throws Throwable {
		setTotalWork(persistorRaw.count(getCriteriaRaw()));
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

		/* If start from begining, rebuild the normalized table. */
		if (option.equals("START")) {
			Table table = stats.getTableNormalized();
			if (DB.ddl().existsTable(table)) {
				DB.ddl().dropTable(table);
			}
			DB.ddl().buildTable(table);
		}

		/* Count and retrieve pending total work. */
		calculateTotalWork();
		long totalWork = getTotalWork();
		long workDone = 0;

		/* Nothing pending to calculate. */
		if (totalWork <= 0) {
			return;
		}

		/* List of aliases and normalizers. */
		List<Field> fields = new ArrayList<>(stats.getFieldListPatterns(true));
		HashMap<String, Normalizer> map = getNormalizers();
		List<String> aliases = new ArrayList<>();
		List<Normalizer> normalizers = new ArrayList<>();
		for (Field field : fields) {
			aliases.add(field.getAlias());
			normalizers.add(map.get(field.getAlias()));
		}

		/* Concurrent pool. */
		Concurrent concurrent = new Concurrent(50, 500);

		/* Iterate raw values. */
		Order order = persistorRaw.getView().getMasterTable().getPrimaryKey();
		RecordIterator iter = persistorRaw.iterator(getCriteriaRaw(), order);
		while (iter.hasNext()) {

			/* Check cancel requested. */
			if (isCancelRequested()) {
				setCancelled();
				break;
			}

			/* Retrieve record. */
			Record rcRaw = iter.next();

			/* Notify work done. */
			workDone++;
			update(rcRaw.toString(DB.FIELD_BAR_TIME_FMT), workDone, totalWork);

			/* Normalized record. */
			Record rcNrm = persistorNrm.getDefaultRecord();
			rcNrm.setValue(DB.FIELD_BAR_TIME, rcRaw.getValue(DB.FIELD_BAR_TIME));
			for (int i = 0; i < aliases.size(); i++) {
				String alias = aliases.get(i);
				Normalizer normalizer = normalizers.get(i);
				double valueRaw = rcRaw.getValue(alias).getDouble();
				double valueNrm = normalizer.normalize(valueRaw);
				rcNrm.setValue(alias, valueNrm);
			}

			/* Queue insert. */
			concurrent.add(new Record.Insert(rcNrm, persistorNrm));
		}
		iter.close();
		concurrent.end();
	}

	/**
	 * @return The criteria to select records of the raw table after the last
	 *         normalized time.
	 */
	private Criteria getCriteriaRaw() throws Throwable {
		Field ftime = persistorRaw.getField(DB.FIELD_BAR_TIME);
		Value vtime = new Value(getLastNormalizedTime());
		Criteria criteria = new Criteria();
		criteria.add(Condition.fieldGT(ftime, vtime));
		return criteria;
	}

	/**
	 * @return The last time of the states table.
	 */
	private long getLastNormalizedTime() throws Throwable {
		long time = 0;
		Field ftime = persistorNrm.getField(DB.FIELD_BAR_TIME);
		Order order = new Order();
		order.add(ftime, false);
		RecordIterator iter = persistorNrm.iterator(null, order);
		if (iter.hasNext()) {
			Record rc = iter.next();
			time = rc.getValue(DB.FIELD_BAR_TIME).getLong();
		}
		iter.close();
		return time;
	}

	/**
	 * @return The map of normalizers.
	 */
	private HashMap<String, Normalizer> getNormalizers() throws PersistorException {
		Field fname = stats.getTableRanges().getField(DB.FIELD_RANGE_NAME);
		Order order = new Order();
		order.add(fname);
		RecordSet rs = stats.getTableRanges().getPersistor().select(null, order);
		HashMap<String, Normalizer> map = new HashMap<>();
		for (Record rc : rs) {
			String name = rc.getValue(DB.FIELD_RANGE_NAME).getString();
			double average = rc.getValue(DB.FIELD_RANGE_AVERAGE).getDouble();
			double std_dev = rc.getValue(DB.FIELD_RANGE_STDDEV).getDouble();
			double dataHigh = average + (2 * std_dev);
			double dataLow = average - (2 * std_dev);
			Normalizer normalizer = new Normalizer(dataHigh, dataLow, 1, -1);
			map.put(name, normalizer);
		}
		return map;
	}
}
