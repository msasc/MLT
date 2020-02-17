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
public class TaskNormalize extends TaskStatistics {

	private Persistor persistorRaw;
	private Persistor persistorNrm;

	/**
	 * @param stats
	 */
	public TaskNormalize(Statistics stats) {
		super(stats);
		setId("averages-nrm");
		setTitle(stats.getLabel() + " - Normalize raw values");
		persistorRaw = stats.getTableRaw().getPersistor();
		persistorNrm = stats.getTableNrm().getPersistor();
	}

	@Override
	protected long calculateTotalWork() throws Throwable {
		setTotalWork(persistorRaw.count(getCriteriaRaw()));
		return getTotalWork();
	}

	@Override
	protected void compute() throws Throwable {

		/* Query option. */
		Option option = queryOption();
		if (option.equals("CANCEL")) {
			throw new Exception("Calculation cancelled by user.");
		}

		/* If start from begining, rebuild the normalized table. */
		if (option.equals("START")) {
			Table table = stats.getTableNrm();
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

		/* List of fields to normalize. */
		List<Field> fields = stats.getFieldListPatterns(true);

		/* List of normalizers per delta level. */
		List<HashMap<String, Normalizer>> normalizers = new ArrayList<>();
		int size = stats.getParameters().getDeltas().size();
		for (int i = 0; i <= size; i++) {
			normalizers.add(getNormalizers(i));
		}
		
		/* Concurrent pool. */
		Concurrent concurrent = new Concurrent(10, 50);
		
		/* Iterate raw values. */
		Order order = persistorRaw.getView().getMasterTable().getPrimaryKey();
		RecordIterator iter = persistorRaw.iterator(getCriteriaRaw(), order);
		while (iter.hasNext()) {

			/* Check cancel requested. */
			if (isCancelRequested()) {
				setCancelled();
				break;
			}
			
			/* Raw record. */
			Record rcRaw = iter.next();
			long time = rcRaw.getValue(DB.FIELD_BAR_TIME).getLong();
			int delta = rcRaw.getValue(DB.FIELD_PATTERN_DELTA).getInteger();

			/* Notify work done. */
			workDone++;
			update(rcRaw.toString(DB.FIELD_BAR_TIME_FMT), workDone, totalWork);
			
			/* Normalized record. */
			Record rcNrm = persistorNrm.getDefaultRecord();
			rcNrm.setValue(DB.FIELD_BAR_TIME, time);
			rcNrm.setValue(DB.FIELD_PATTERN_DELTA, delta);
			HashMap<String, Normalizer> map = normalizers.get(delta);
			for (Field field : fields) {
				String alias = field.getAlias();
				Normalizer normalizer = map.get(alias);
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

		Field fDELTA = persistorNrm.getField(DB.FIELD_PATTERN_DELTA);
		Field fTIME = persistorNrm.getField(DB.FIELD_BAR_TIME);

		Criteria criteria = new Criteria();
		criteria.add(Condition.fieldEQ(fDELTA, new Value(0)));

		Order order = new Order();
		order.add(fTIME, false);

		RecordIterator iter = persistorNrm.iterator(criteria, order);
		if (iter.hasNext()) {
			Record rc = iter.next();
			time = rc.getValue(DB.FIELD_BAR_TIME).getLong();
		}
		iter.close();

		return time;
	}

	/**
	 * @param delta The delta.
	 * @return The normalizers for the delta.
	 * @throws PersistorException
	 */
	private HashMap<String, Normalizer> getNormalizers(int delta) throws PersistorException {

		Field fDELTA = stats.getTableRng().getField(DB.FIELD_PATTERN_DELTA);
		Field fNAME = stats.getTableRng().getField(DB.FIELD_PATTERN_NAME);

		Criteria criteria = new Criteria();
		criteria.add(Condition.fieldEQ(fDELTA, new Value(delta)));

		Order order = new Order();
		order.add(fNAME);

		RecordSet rs = stats.getTableRng().getPersistor().select(criteria, order);
		HashMap<String, Normalizer> map = new HashMap<>();
		for (Record rc : rs) {
			String name = rc.getValue(DB.FIELD_PATTERN_NAME).getString();
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
