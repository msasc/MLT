/*
 * Copyright (C) 2018 Miquel Sas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.mlt.mkt.data;

import java.util.Comparator;
import java.util.Iterator;

import com.mlt.db.Criteria;
import com.mlt.db.ListPersistor;
import com.mlt.db.Order;
import com.mlt.db.Persistor;
import com.mlt.db.Record;
import com.mlt.db.RecordSet;

/**
 * A recordset of time series data.
 * 
 * @author Miquel Sas
 */
public class DataRecordSet extends RecordSet {

	/** The list persistor. */
	private ListPersistor persistor;

	/**
	 * Constructor.
	 * 
	 * @param persistor The list persistor.
	 */
	public DataRecordSet(ListPersistor persistor) {
		super(persistor.getView().getFieldList());
		this.persistor = persistor;
	}

	/**
	 * Constructor.
	 * 
	 * @param persistor The persistor.
	 */
	public DataRecordSet(Persistor persistor) {
		super(persistor.getView().getFieldList());
		this.persistor = new ListPersistor(persistor);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void add(int index, Record record) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void add(Record record) {
	}

	/**
	 * Not supported.
	 */
	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get a record given its index in the record list.
	 *
	 * @return The Record.
	 * @param index The index in the record list.
	 */
	@Override
	public Record get(int index) {
		return persistor.getRecord(index);
	}

	/**
	 * Not supported.
	 */
	@Override
	public RecordSet getCopy() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported.
	 */
	@Override
	public int getInsertIndex(Record record, Order order) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported.
	 */
	@Override
	public RecordSet getRecordSet(Criteria criteria) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * Check if the recordset can be sorted. Default is true, overwrite this method if the recorset implementation can
	 * not be sorted.
	 * 
	 * @return A boolean.
	 */
	@Override
	public boolean isSortable() {
		return false;
	}

	/**
	 * Not supported.
	 */
	@Override
	public Iterator<Record> iterator() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported.
	 */
	@Override
	public Record remove(int index) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported.
	 */
	@Override
	public void set(int index, Record record) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns this record set size.
	 *
	 * @return The size.
	 */
	@Override
	public int size() {
		return persistor.size();
	}

	/**
	 * Not supported.
	 */
	@Override
	public void sort() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported.
	 */
	@Override
	public void sort(Comparator<Record> comparator) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported.
	 */
	@Override
	public void sort(Order order) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Record[] toArray() {
		int size = size();
		Record[] records = new Record[size];
		for (int i = 0; i < size; i++) {
			records[i] = get(i);
		}
		return records;
	}
}
