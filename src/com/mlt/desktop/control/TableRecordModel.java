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
package com.mlt.desktop.control;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.mlt.db.Field;
import com.mlt.db.Order;
import com.mlt.db.Record;
import com.mlt.db.RecordList;
import com.mlt.db.RecordSet;
import com.mlt.db.RecordSetListener;
import com.mlt.db.Types;
import com.mlt.db.Value;
import com.mlt.desktop.control.table.TableModel;
import com.mlt.desktop.control.table.TableSorter;
import com.mlt.util.Logs;

/**
 * A table model aimed to work with sets of records.
 *
 * @author Miquel Sas
 */
public class TableRecordModel extends TableModel {

	/**
	 * The listener to recordset changes.
	 */
	class Listener implements RecordSetListener {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void added(Record record) {
			int index = recordSet.size() - 1;
			added(index, record);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void added(int index, Record record) {
			fireTableRowsInserted(index, index);
			index = recordSet.indexOf(record);
			fireUpdateTables(index);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void removed(int index, Record record) {
			fireTableRowsDeleted(index, index);
			if (recordSet.size() <= index) {
				index = recordSet.size() - 1;
			}
			fireUpdateTables(index);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void set(int index, Record record) {
			fireTableRowsUpdated(index, index);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void sorted() {
			fireTableDataChanged();
		}

	}

	/** The master record used to configure the model. */
	private Record masterRecord;
	/** The list of field indexes of the record to show as columns. */
	private List<Integer> fieldIndexes = new ArrayList<>();
	/** The <code>RecordSet</code>. */
	private RecordSet recordSet;
	/** The recordset listener. */
	private RecordSetListener listener;

	/** List of table records that display this model. */
	private List<TableRecord> tables = new ArrayList<>();

	/**
	 * Constructor.
	 * 
	 * @param masterRecord The master record.
	 */
	public TableRecordModel(Record masterRecord) {
		super();
		this.masterRecord = masterRecord;
		this.listener = new Listener();
		this.recordSet = new RecordList();
		this.recordSet.setFieldList(masterRecord.getFieldList());
		this.recordSet.addListener(listener);
	}

	/**
	 * Add a column indicating the field index.
	 * 
	 * @param index The field index in the master record.
	 */
	public void addColumn(int index) {
		if (index < 0 || index >= masterRecord.size()) {
			throw new IllegalArgumentException("Invalid field index");
		}
		fieldIndexes.add(index);
		fireTableStructureChanged();
	}

	/**
	 * Add a column indicating the field alias.
	 * 
	 * @param alias The field alias.
	 */
	public void addColumn(String alias) {
		int index = masterRecord.getFieldList().getFieldIndex(alias);
		addColumn(index);
	}

	/**
	 * Add the table to the list of tables that display this model.
	 * 
	 * @param table The table record.
	 */
	void addTable(TableRecord table) {
		if (!tables.contains(table)) {
			tables.add(table);
		}
	}
	
	private void fireUpdateTables(int row) {
		for (TableRecord table : tables) {
			table.adjustColumnWidths();
			if (row >= 0) {
				table.setSelectedRow(row);
			}
			table.repaint();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getColumnCount() {
		return fieldIndexes.size();
	}

	/**
	 * Returns the field index given the column index.
	 * 
	 * @param columnIndex The column index.
	 * @return The field index.
	 */
	public int getFieldIndex(int columnIndex) {
		if (columnIndex < 0 || columnIndex >= fieldIndexes.size()) {
			throw new IllegalArgumentException("Invalid column index " + columnIndex);
		}
		return fieldIndexes.get(columnIndex);
	}

	/**
	 * Returns the master record of this model.
	 * 
	 * @return The master record.
	 */
	public Record getMasterRecord() {
		return masterRecord;
	}

	/**
	 * Return the recordset.
	 * 
	 * @return The recordset.
	 */
	public RecordSet getRecordSet() {
		return recordSet;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getRowCount() {
		return recordSet.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {

		/* Validate row and column. */
		if (rowIndex < 0 || rowIndex >= recordSet.size()) {
			throw new IllegalArgumentException("Invalid row index " + rowIndex);
		}
		if (columnIndex < 0 || columnIndex >= fieldIndexes.size()) {
			throw new IllegalArgumentException("Invalid column index " + columnIndex);
		}

		/* Return the value. */
		return recordSet.get(rowIndex).getValue(getFieldIndex(columnIndex));
	}

	/**
	 * Remove all columns.
	 */
	public void removeAllColumns() {
		fieldIndexes.clear();
		removeCellEditable(-1, -1);
		fireTableStructureChanged();
	}

	/**
	 * Remove a given fieldIndex.
	 * 
	 * @param column The column index.
	 */
	public void removeColumn(int column) {
		if (column < 0 || column >= fieldIndexes.size()) {
			throw new IllegalArgumentException("Invalid fieldIndex index " + column);
		}
		fieldIndexes.remove(column);
		removeCellEditable(-1, column);
		fireTableStructureChanged();
	}

	/**
	 * Remove the table from the list of tables that display this model.
	 * 
	 * @param table The tale record to remove.
	 */
	void removeTable(TableRecord table) {
		tables.remove(table);
	}

	/**
	 * Set the record set without any persistence execution involved.
	 * 
	 * @param recordSet The record set.
	 */
	public void setRecordSet(RecordSet recordSet) {
		this.recordSet = recordSet;
		this.recordSet.addListener(listener);
		fireTableRowsInserted(0, getRowCount());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (aValue instanceof String) {
			String strFmt = (String) aValue;
			Record record = recordSet.get(rowIndex);
			int fieldIndex = getFieldIndex(columnIndex);
			Field field = record.getField(fieldIndex);
			Types type = field.getType();
			int scale = field.getDecimals();
			Value value = null;
			try {
				value = Value.toValue(type, scale, strFmt, Locale.getDefault());
				record.setValue(fieldIndex, value);
			} catch (ParseException exc) {
				Logs.catching(exc);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void sortModel(TableSorter tableSorter) {
		/* Create an order. */
		Order order = new Order();
		for (TableSorter.Key key : tableSorter.getKeys()) {
			int fieldIndex = getFieldIndex(key.getColumn());
			Field field = masterRecord.getField(fieldIndex);
			boolean ascending = key.isAscending();
			order.add(field, ascending);
		}
		recordSet.sort(order);
	}

}
