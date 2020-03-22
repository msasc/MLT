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
package com.mlt.desktop.control.table;

import java.util.HashMap;

import javax.swing.table.AbstractTableModel;

/**
 * Table model, extension of <em>AbstractTableModel</em> with an utility to set
 * the rows, columns or cells that are editable.
 *
 * @author Miquel Sas
 */
public abstract class TableModel extends AbstractTableModel {

	/**
	 * Small structure to set which columns, rows or single cells are editable.
	 */
	static class EditCell {
		/** The row, -1 indicates that all rows are editable. */
		private int row = -1;
		/** The column, -1 indicates that all columns are editable. */
		private int column = -1;

		/**
		 * Constructor assigning row and column.
		 * 
		 * @param row    The row.
		 * @param column The column.
		 */
		EditCell(int row, int column) {
			super();
			this.row = row;
			this.column = column;
		}

		/**
		 * Returns an integer indicating the hash code.
		 * 
		 * @return The hash code.
		 */
		@Override
		public int hashCode() {
			return row + column;
		}

		/**
		 * Returns a boolean indicating if the argument object is equal to this object.
		 * 
		 * @return A boolean indicating if the argument object is equal to this object.
		 */
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof EditCell)) {
				return false;
			}
			EditCell ec = (EditCell) obj;
			return (ec.row == row && ec.column == column);
		}

		/**
		 * Returns the row.
		 * 
		 * @return The row.
		 */
		public int getRow() {
			return row;
		}

		/**
		 * Returns the column.
		 * 
		 * @return The column.
		 */
		public int getColumn() {
			return column;
		}
	}

	/** A map to manage which cells are editable. */
	private HashMap<EditCell, Boolean> editMap = new HashMap<>();

	/**
	 * Default constructor.
	 */
	public TableModel() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		Boolean editable = editMap.get(new EditCell(rowIndex, columnIndex));
		/* If not specified the cell, check for the column for all rows. */
		if (editable == null) {
			editable = editMap.get(new EditCell(-1, columnIndex));
		}
		/* Check for all columns for the row. */
		if (editable == null) {
			editable = editMap.get(new EditCell(rowIndex, -1));
		}
		/* Check for all cells (all rows and all columns). */
		if (editable == null) {
			editable = editMap.get(new EditCell(-1, -1));
		}
		/* If nothing set, then it's not editable. */
		if (editable == null) {
			return false;
		}
		return editable.booleanValue();
	}

	/**
	 * Set the cell editable.
	 * 
	 * @param rowIndex    The row, if -1 all rows are editable.
	 * @param columnIndex The column index, -1 all columns are editable.
	 * @param editable    A boolean indicating if the cell is editable.
	 */
	public void setCellEditable(int rowIndex, int columnIndex, boolean editable) {
		editMap.put(new EditCell(rowIndex, columnIndex), editable);
	}

	/**
	 * Remove the cell editable configuration.
	 * 
	 * @param rowIndex    The row index.
	 * @param columnIndex The column index.
	 */
	public void removeCellEditable(int rowIndex, int columnIndex) {
		editMap.remove(new EditCell(rowIndex, columnIndex));
	}

	/**
	 * Effectively sort the model.
	 * 
	 * @param tableSorter The table sort definition.
	 */
	protected abstract void sortModel(TableSorter tableSorter);

	/**
	 * Sort the model and fire that data has changed.
	 * 
	 * @param tableSorter The table sort definition.
	 */
	public void sort(TableSorter tableSorter) {
		sortModel(tableSorter);
		fireTableDataChanged();
	}
}
