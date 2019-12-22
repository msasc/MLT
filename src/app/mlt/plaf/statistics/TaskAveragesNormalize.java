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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;

import com.mlt.db.Condition;
import com.mlt.db.Criteria;
import com.mlt.db.Field;
import com.mlt.db.Order;
import com.mlt.db.Persistor;
import com.mlt.db.Record;
import com.mlt.db.RecordIterator;
import com.mlt.db.Value;
import com.mlt.db.ValueMap;
import com.mlt.desktop.Option;
import com.mlt.desktop.converters.TimeFmtConverter;
import com.mlt.ml.function.Normalizer;
import com.mlt.util.StringConverter;
import com.mlt.util.Strings;

import app.mlt.plaf.DB;

/**
 * Normalize raw values.
 *
 * @author Miquel Sas
 */
public class TaskAveragesNormalize extends TaskAverages {

	/** First time not normalized. */
	private long firstTimeNotNormalized = -1;

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

		Persistor persistor;

		persistor = stats.getTableStates().getPersistor();
		Field fieldTime = persistor.getField(DB.FIELD_BAR_TIME);
		Field fieldNrm = persistor.getField(DB.FIELD_STATES_NORMALIZED);
		Criteria criteria = new Criteria();
		criteria.add(Condition.fieldNE(fieldNrm, new Value("Y")));
		Order order = new Order();
		order.add(fieldTime, true);
		firstTimeNotNormalized = 0;
		RecordIterator iter = persistor.iterator(null, order);
		if (iter.hasNext()) {
			Record rc = iter.next();
			firstTimeNotNormalized = rc.getValue(DB.FIELD_BAR_TIME).getLong();
		}
		iter.close();

		long totalWork = 0;
		persistor = stats.getTableStates().getPersistor();
		totalWork += persistor.count(getSelectCriteria(persistor));
		persistor = stats.getTableCandles().getPersistor();
		totalWork += persistor.count(getSelectCriteria(persistor));

		setTotalWork(totalWork);
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
		if (option.equals("START")) {
			Persistor persistor = stats.getTableStates().getPersistor();
			ValueMap map = new ValueMap();
			map.put(DB.FIELD_STATES_NORMALIZED, new Value(""));
			Field field = persistor.getField(DB.FIELD_STATES_NORMALIZED);
			Criteria criteria = new Criteria();
			criteria.add(Condition.fieldEQ(field, new Value("Y")));
			persistor.update(criteria, map);
		}

		/* Count. */
		calculateTotalWork();
		
		/* Normalizers. */
		HashMap<String, Normalizer> normalizers = stats.getNormalizers();
		
		/* Names, persistors, order, itertor and concurrent updates. */
		List<String> names;
		Persistor persistor;
		Order order;
		RecordIterator iter;
		List<Callable<Void>> concurrents = new ArrayList<>();
		
		/* Work tracking. */
		long totalWork = getTotalWork();
		long workDone = 0;
		int poolSize = 200;
		int maxConcurrent = (1 + stats.getCandleCount()) * 400;
		ForkJoinPool pool = new ForkJoinPool(poolSize);

		/* Normalize states. */
		names = stats.getFieldNamesToNormalizeStates();
		persistor = stats.getTableStates().getPersistor();
		order = stats.getTableStates().getPrimaryKey();
		iter = persistor.iterator(getSelectCriteria(persistor), order);
		while (iter.hasNext()) {

			/* Check cancel requested. */
			if (isCancelRequested()) {
				setCancelled();
				break;
			}

			/* Retrieve record. */
			Record record = iter.next();

			/* Notify work. */
			workDone++;
			if (workDone % 10 == 0 || workDone == totalWork) {
				StringBuilder b = new StringBuilder();
				b.append(record.toString(DB.FIELD_BAR_TIME_FMT));
				b.append(", ");
				b.append(record.toString(DB.FIELD_BAR_OPEN));
				b.append(", ");
				b.append(record.toString(DB.FIELD_BAR_HIGH));
				b.append(", ");
				b.append(record.toString(DB.FIELD_BAR_LOW));
				b.append(", ");
				b.append(record.toString(DB.FIELD_BAR_CLOSE));
				update(b.toString(), workDone, totalWork);
			}

			/* Fields to normalize. */
			for (int i = 0; i < names.size(); i++) {
				String name = names.get(i);
				String name_raw = DB.name_suffix(name, "raw");
				String name_nrm = DB.name_suffix(name, "nrm");
				Normalizer normalizer = normalizers.get(name);
				double value_raw = record.getValue(name_raw).getDouble();
				double value_nrm = normalizer.normalize(value_raw);
				record.setValue(name_nrm, value_nrm);
			}
			record.setValue(DB.FIELD_STATES_NORMALIZED, "Y");
			concurrents.add(new Record.Update(record, persistor));

			/* Update. */
			if (concurrents.size() >= maxConcurrent) {
				pool.invokeAll(concurrents);
				concurrents.clear();
			}
		}
		iter.close();
		if (!concurrents.isEmpty()) {
			pool.invokeAll(concurrents);
			concurrents.clear();
		}
		
		/* Normalize candles. */
		String pattern = stats.getPeriod().getTimeFmtPattern();
		StringConverter converter = new TimeFmtConverter(new SimpleDateFormat(pattern));
		int pad = stats.getCandlePad();
		names = stats.getFieldNamesToNormalizeCandles();
		persistor = stats.getTableCandles().getPersistor();
		order = stats.getTableCandles().getPrimaryKey();
		iter = persistor.iterator(getSelectCriteria(persistor), order);
		while (iter.hasNext()) {

			/* Check cancel requested. */
			if (isCancelRequested()) {
				setCancelled();
				break;
			}

			/* Retrieve record. */
			Record record = iter.next();
			long time = record.getValue(DB.FIELD_BAR_TIME).getLong();
			String timeFmt = converter.valueToString(time);
			int size = record.getValue(DB.FIELD_CANDLE_SIZE).getInteger();
			String sizePad = Strings.leftPad(Integer.toString(size), pad, "0");

			/* Notify work. */
			workDone++;
			if (workDone % 10 == 0 || workDone == totalWork) {
				StringBuilder b = new StringBuilder();
				b.append(timeFmt);
				b.append(" - ");
				b.append(sizePad);
				update(b.toString(), workDone, totalWork);
			}

			/* Fields to normalize. */
			for (int i = 0; i < names.size(); i++) {
				String name = names.get(i);
				String name_raw = DB.name_suffix(name, "raw");
				String name_nrm = DB.name_suffix(name, "nrm");
				String name_get = name + "_" + sizePad;
				Normalizer normalizer = normalizers.get(name_get);
				double value_raw = record.getValue(name_raw).getDouble();
				double value_nrm = normalizer.normalize(value_raw);
				record.setValue(name_nrm, value_nrm);
			}
			concurrents.add(new Record.Update(record, persistor));

			/* Update. */
			if (concurrents.size() >= maxConcurrent) {
				pool.invokeAll(concurrents);
				concurrents.clear();
			}
		}
		iter.close();
		if (!concurrents.isEmpty()) {
			pool.invokeAll(concurrents);
			concurrents.clear();
		}
		pool.shutdown();
	}

	private Criteria getSelectCriteria(Persistor persistor) {
		Field field = persistor.getField(DB.FIELD_BAR_TIME);
		Criteria criteria = new Criteria();
		criteria.add(Condition.fieldGE(field, new Value(firstTimeNotNormalized)));
		return criteria;
	}
}
