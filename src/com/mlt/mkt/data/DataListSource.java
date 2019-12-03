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

import com.mlt.db.ListPersistor;
import com.mlt.db.PersistorException;
import com.mlt.db.Record;
import com.mlt.mkt.data.info.DataInfo;
import com.mlt.util.Logs;

/**
 * A data list that retrieves data from a data source backed by a persistor.
 *
 * @author Miquel Sas
 */
public class DataListSource extends DataList {

	/** Underlying list persistor. */
	private ListPersistor persistor;
	/** Data converter. */
	private DataConverter converter;

	/**
	 * Constructor.
	 * 
	 * @param dataInfo The data info.
	 * @param persistor The persistor.
	 */
	public DataListSource(DataInfo dataInfo, ListPersistor persistor) {
		super(dataInfo);
		this.persistor = persistor;
		this.converter = new DataConverter(persistor.getDefaultRecord());
	}

	/**
	 * Constructor.
	 * 
	 * @param dataInfo The data info.
	 * @param persistor The persistor.
	 * @param converter The data converter.
	 */
	public DataListSource(DataInfo dataInfo, ListPersistor persistor, DataConverter converter) {
		super(dataInfo);
		this.persistor = persistor;
		this.converter = converter;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void add(Data data) {
		Record record = converter.getRecord(data);
		try {
			persistor.insert(record);
		} catch (PersistorException exc) {
			Logs.catching(exc);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size() {
		return Long.valueOf(persistor.size()).intValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEmpty() {
		return persistor.size() == 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Data get(int index) {
		return converter.getData(persistor.getRecord(index));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Data remove(int index) {
		Data data = get(index);
		Record record = converter.getRecord(data);
		try {
			persistor.delete(record);
		} catch (PersistorException exc) {
			Logs.catching(exc);
		}
		return data;
	}
}
