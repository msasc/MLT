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
package com.mlt.desktop.control;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableColumn;

import com.mlt.db.Field;
import com.mlt.db.Record;
import com.mlt.desktop.control.table.SelectionMode;
import com.mlt.desktop.control.table.TableModel;
import com.mlt.desktop.control.table.TableRecordCellRenderer;
import com.mlt.desktop.control.table.TableRecordModel;

/**
 *
 * @author Miquel Sas
 */
public class TableRecord extends Table {

	/**
	 * Default constructor.
	 */
	public TableRecord() {
		super();
	}

	/**
	 * Constructor assigning the selection mode.
	 * 
	 * @param selectionMode The selection mode.
	 */
	public TableRecord(SelectionMode selectionMode) {
		super(selectionMode);
	}

	/*
	 * Access the selected rows as records.
	 */

	/**
	 * Return the selected record or null.
	 * 
	 * @return The selected record.
	 */
	public Record getSelectedRecord() {
		int viewRow = getSelectedRow();
		if (viewRow >= 0) {
			int modelRow = convertRowIndexToModel(viewRow);
			return getModel().getRecordSet().get(modelRow);
		}
		return null;
	}

	/**
	 * Return the list of selected records.
	 * 
	 * @return The list of selected records.
	 */
	public List<Record> getSelectedRecords() {
		int[] viewRows = getSelectedRows();
		List<Record> records = new ArrayList<>();
		for (int viewRow : viewRows) {
			int modelRow = convertRowIndexToModel(viewRow);
			records.add(getModel().getRecordSet().get(modelRow));
		}
		return records;
	}

	/*
	 * Table record model management.
	 */

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TableRecordModel getModel() {
		return (TableRecordModel) super.getModel();
	}

	/**
	 * Set the table model. If the model set is not a <code>TableRecordModel</code>, it throws a
	 * <code>ClassCastException</code>.
	 * 
	 * @param tableModel The table model.
	 */
	@Override
	public void setModel(TableModel tableModel) {

		/*
		 * Check that the table model is an instance of TableRecordModel.
		 */

		if (!(tableModel instanceof TableRecordModel)) {
			throw new ClassCastException("The table model must be an instance of TableRecordModel");
		}
		super.setModel(tableModel);

		/*
		 * Configure renderers and editors using the TableRecordModel.
		 */

		TableRecordModel model = (TableRecordModel) tableModel;
		Record masterRecord = model.getMasterRecord();
		for (int column = 0; column < model.getColumnCount(); column++) {

			/* Link the model with the view. */
			TableColumn tableColumn = getColumnModel().getColumn(column);
			tableColumn.setModelIndex(column);

			/* Get the field. */
			int index = model.getFieldIndex(column);
			Field field = masterRecord.getField(index);

			/* Set the header. */
			tableColumn.setHeaderValue(field.getDisplayHeader());

			/* Set the cell renderer. */
			TableRecordCellRenderer tableRecordCellRenderer = new TableRecordCellRenderer(getTableComponent(), field);
			tableColumn.setCellRenderer(tableRecordCellRenderer);
		}

		/* Adjust the column sizes to correctly display all data. */
		adjustColumnWidths();

		/* By default select the first row. */
		if (model.getRowCount() > 0) {
//			get
		}
	}
}
