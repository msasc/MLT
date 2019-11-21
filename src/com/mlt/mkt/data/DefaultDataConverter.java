/*
 * Copyright (C) 2018 Miquel Sas
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.mlt.mkt.data;

import java.util.HashMap;

import com.mlt.db.Field;
import com.mlt.db.Persistor;
import com.mlt.db.Record;
import com.mlt.db.Value;

/**
 * Default data converter.
 * <p>
 * The general contract for a timed data backed by a persistor is:
 * <p>
 * The first field is a long, the time of the timed data and subsequent
 * <b>persistent</b> fields of type double and are
 * conform a list of double values to convert, along with the time, into a data
 * element.
 *
 * @author Miquel Sas
 */
public class DefaultDataConverter implements DataConverter {

	/** Default record. */
	private Record defaultRecord;
	/** The indexes of the value fields. */
	private int[] indexes;
	/** Indexes map. */
	private HashMap<String, Integer> mapIndexes;

	/**
	 * Constructor.
	 * 
	 * @param persistor The back end data persistor.
	 */
	public DefaultDataConverter(Persistor persistor) {
		super();
		defaultRecord = persistor.getDefaultRecord();
		int length = 0;
		for (int i = 1; i < persistor.getFieldCount(); i++) {
			Field field = persistor.getField(i);
			if (field.isDouble() && field.isPersistent()) {
				length++;
			}
		}
		indexes = new int[length];
		mapIndexes = new HashMap<>();
		int index = 0;
		for (int i = 1; i < persistor.getFieldCount(); i++) {
			Field field = persistor.getField(i);
			if (field.isDouble() && field.isPersistent()) {
				indexes[index] = i;
				mapIndexes.put(field.getAlias(), index);
				index++;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Data getData(Record record) {
		long time = record.getValue(0).getLong();
		double[] values = new double[indexes.length];
		for (int i = 0; i < indexes.length; i++) {
			values[i] = record.getValue(indexes[i]).getDouble();
		}
		return new Data(time, values);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Record getRecord(Data data) {
		Record record = Record.copy(defaultRecord);
		record.setValue(0, new Value(data.getTime()));
		for (int i = 0; i < indexes.length; i++) {
			double value = data.getValue(i);
			int index = indexes[i];
			record.setValue(index, new Value(value));
		}
		return record;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getIndex(String alias) {
		Integer index = mapIndexes.get(alias);
		return (index == null ? -1 : index);
	}

}
