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

import java.util.Locale;

import javax.swing.JScrollPane;
import javax.swing.table.TableColumnModel;

import com.mlt.desktop.control.table.Cell;
import com.mlt.desktop.control.table.SelectionMode;
import com.mlt.desktop.control.table.TableCmp;
import com.mlt.desktop.control.table.TableCmpListener;
import com.mlt.desktop.control.table.TableModel;
import com.mlt.util.Formats;
import com.mlt.util.Numbers;
import com.mlt.util.Resources;

/**
 * Table extension. Note that this table by-passes the sort mechanism.
 *
 * @author Miquel Sas
 */
public class Table extends Control {

	/**
	 * Listener to table events.
	 */
	public interface Listener {
		/**
		 * Fired when the cell focus changes.
		 * 
		 * @param table The table.
		 */
		void focusCellChanged(Table table);

		/**
		 * Fired when the rows or columns selected change.
		 * 
		 * @param table The table.
		 */
		void selectionChanged(Table table);
	}

	/**
	 * Table component listener forwarder.
	 */
	static class Forwarder implements TableCmpListener {
		/** The listener to whom forward. */
		private Table.Listener listener;

		/**
		 * Constructor.
		 * 
		 * @param listener The listener to whom forward.
		 */
		Forwarder(Listener listener) {
			super();
			this.listener = listener;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void focusCellChanged(TableCmp tableCmp) {
			listener.focusCellChanged(tableCmp.getControl());
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void selectionChanged(TableCmp tableCmp) {
			listener.selectionChanged(tableCmp.getControl());
		}

	}

	/**
	 * Default constructor, with single selection.
	 */
	public Table() {
		this(SelectionMode.SINGLE_ROW_SELECTION);
	}

	/**
	 * Constructor setting the selection mode.
	 * 
	 * @param selectionMode The selection mode.
	 */
	public Table(SelectionMode selectionMode) {
		super();

		setComponent(new JScrollPane(new TableCmp()));

		/* Assign this table to the component. */
		getTableComponent().setTableControl(this);

		/* Setup the table. */
		getTableComponent().setup();

		/* Default selection mode. */
		setSelectionMode(selectionMode);

	}

	/**
	 * Add a listener.
	 * 
	 * @param listener The listener.
	 */
	public void addListener(Table.Listener listener) {
		getTableComponent().addListener(new Forwarder(listener));
	}

	/**
	 * Adjust the column size. Based on the current header and row-column preferred width, scanning a maximum number of
	 * rows randomly including the first and last rows.
	 * 
	 * @param column The column number.
	 */
	public void adjustColumnWidth(int column) {
		getTableComponent().adjustColumnWidth(column);
	}

	/**
	 * Adjust the width of all columns.
	 */
	public void adjustColumnWidths() {
		getTableComponent().adjustColumnWidths();
	}

	/**
	 * Clear any selection.
	 */
	public void clearSelection() {
		getTableComponent().clearSelection();
	}

	protected int convertRowIndexToModel(int viewRow) {
		return getTableComponent().convertRowIndexToModel(viewRow);
	}

	protected int convertRowIndexToView(int viewRow) {
		return getTableComponent().convertRowIndexToView(viewRow);
	}

	/**
	 * Return the table column model.
	 * 
	 * @return The column model.
	 */
	protected TableColumnModel getColumnModel() {
		return getTableComponent().getColumnModel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JScrollPane getComponent() {
		return (JScrollPane) super.getComponent();
	}

	protected final TableCmp getTableComponent() {
		return (TableCmp) getComponent().getViewport().getView();
	}

	/**
	 * Return the model.
	 * 
	 * @return The model.
	 */
	public TableModel getModel() {
		if (getTableComponent().getModel() instanceof TableModel) {
			return (TableModel) getTableComponent().getModel();
		}
		return null;
	}

	/**
	 * Return the number of visible rows.
	 * 
	 * @return The number of visible rows.
	 */
	public int getRowCount() {
		return getTableComponent().getRowCount();
	}

	/**
	 * Return the row information.
	 * 
	 * @return The row information.
	 */
	public String getRowInfo() {
		StringBuilder b = new StringBuilder();
		Cell focusCell = getFocusCell();
		if (focusCell != null) {
			int row = focusCell.getRow() + 1;
			int rows = getRowCount();
			b.append(Resources.getText("tokenRow"));
			b.append(" ");
			b.append(Formats.fromInteger(row, Locale.getDefault()));
			b.append(" ");
			b.append(Resources.getText("tokenOf"));
			b.append(" ");
			b.append(Formats.fromInteger(rows, Locale.getDefault()));
			int linesPercentScale = Math.max(0, Numbers.getDigits(rows) - 2);
			double percent = 100d * Double.valueOf(row) / Double.valueOf(rows);
			b.append(" (");
			b.append(Formats.fromDouble(percent, linesPercentScale, Locale.getDefault()));
			b.append(" %)");
		}
		return b.toString();
	}

	/**
	 * Return the focused cell.
	 * 
	 * @return The last focused cell or null if none was focused.
	 */
	public Cell getFocusCell() {
		return getTableComponent().getFocusCell();
	}

	/**
	 * Return the focused column.
	 * 
	 * @return The focused column.
	 */
	public int getFocusColumn() {
		return getTableComponent().getFocusColumn();
	}

	/**
	 * Return the focused row.
	 * 
	 * @return The focused row.
	 */
	public int getFocusRow() {
		return getTableComponent().getFocusRow();
	}

	/**
	 * Return the first selected cell.
	 * 
	 * @return The first selected cell.
	 */
	public Cell getSelectedCell() {
		return getTableComponent().getSelectedCell();
	}

	/**
	 * Return the number of selected cells.
	 * 
	 * @return the number of selected cells.
	 */
	public int getSelectedCellCount() {
		return getTableComponent().getSelectedCellCount();
	}

	/**
	 * Return the list of selected cells.
	 * 
	 * @return The list of selected cells.
	 */
	public Cell[] getSelectedCells() {
		return getTableComponent().getSelectedCells();
	}

	/**
	 * Return the first selected column.
	 * 
	 * @return The first selected column.
	 */
	public int getSelectedColumn() {
		return getTableComponent().getSelectedColumn();
	}

	/**
	 * Return the number of selected columns.
	 * 
	 * @return the number of selected columns.
	 */
	public int getSelectedColumnCount() {
		return getTableComponent().getSelectedColumnCount();
	}

	/**
	 * Return the list of selected columns.
	 * 
	 * @return The list of selected columns.
	 */
	public int[] getSelectedColumns() {
		return getTableComponent().getSelectedColumns();
	}

	/**
	 * Return the selected row.
	 * 
	 * @return The selected row.
	 */
	public int getSelectedRow() {
		return getTableComponent().getSelectedRow();
	}

	/**
	 * Return the number of selected rows.
	 * 
	 * @return The number of selected rows.
	 */
	public int getSelectedRowCount() {
		return getTableComponent().getSelectedRowCount();
	}

	/**
	 * Return the selected rows.
	 * 
	 * @return The selected rows.
	 */
	public int[] getSelectedRows() {
		return getTableComponent().getSelectedRows();
	}

	/**
	 * Return the current selection mode.
	 * 
	 * @return The current selection mode.
	 */
	public SelectionMode getSelectionMode() {
		return getTableComponent().getSelectionMode();
	}

	/**
	 * Check if the cell is focused.
	 * 
	 * @param row    The row.
	 * @param column The column.
	 * @return A boolean.
	 */
	public boolean isCellFocused(int row, int column) {
		return getTableComponent().isCellFocused(row, column);
	}

	/**
	 * Check whether the cell is selected.
	 * 
	 * @param row    The row.
	 * @param column The column.
	 * @return A boolean.
	 */
	public boolean isCellSelected(int row, int column) {
		return getTableComponent().isCellSelected(row, column);
	}

	/**
	 * Check if the cell is totally visible.
	 * 
	 * @param row    The row.
	 * @param column The column.
	 * @return A boolean.
	 */
	public boolean isCellVisible(int row, int column) {
		return getTableComponent().isCellVisible(row, column);
	}

	/**
	 * Check if the column is focused.
	 * 
	 * @param column The column.
	 * @return A boolean.
	 */
	public boolean isColumnFocused(int column) {
		return getTableComponent().isColumnFocused(column);
	}

	/**
	 * Check whether the column is selected.
	 * 
	 * @param column The column.
	 * @return A boolean.
	 */
	public boolean isColumnSelected(int column) {
		return getTableComponent().isColumnSelected(column);
	}

	/**
	 * Check if a column totally is visible.
	 * 
	 * @param column The index position of the column.
	 * @return <code>true</code> if the column is visible.
	 */
	public boolean isColumnVisible(int column) {
		return getTableComponent().isColumnVisible(column);
	}

	/**
	 * Check if the row is focused.
	 * 
	 * @param row The row.
	 * @return A boolean.
	 */
	public boolean isRowFocused(int row) {
		return getTableComponent().isRowFocused(row);
	}

	/**
	 * Check whether the row is selected.
	 * 
	 * @param row The row.
	 * @return A boolean.
	 */
	public boolean isRowSelected(int row) {
		return getTableComponent().isRowSelected(row);
	}

	/**
	 * Check if a row totally is visible.
	 * 
	 * @param row The index position of the row.
	 * @return <code>true</code> if the row is visible.
	 */
	public boolean isRowVisible(int row) {
		return getTableComponent().isRowVisible(row);
	}

	/**
	 * Returns a boolean that indicates if sorting clicking the header is enabled.
	 * 
	 * @return A boolean that indicates if sorting clicking the header is enabled.
	 */
	public boolean isSortingEnabled() {
		return getTableComponent().isSortingEnabled();
	}
	
	/**
	 * Change the focus to the argument cell.
	 *
	 * @param rowIndex    The row index.
	 * @param columnIndex The column index.
	 */
	public void moveFocus(int rowIndex, int columnIndex) {
		getTableComponent().moveFocus(rowIndex, columnIndex);
	}


	/**
	 * Select all cells, row or columns.
	 */
	public void selectAll() {
		getTableComponent().selectAll();
	}

	/**
	 * Set the margin to adjust column widths.
	 * 
	 * @param marginToAdjustColumnWidth The margin to adjust column widths.
	 */
	public void setMarginToAdjustColumnWidth(int marginToAdjustColumnWidth) {
		getTableComponent().setMarginToAdjustColumnWidth(marginToAdjustColumnWidth);
	}

	/*
	 * Column width adjustment.
	 */

	/**
	 * Set the maximum number of rows used to adjust column width.
	 * 
	 * @param rowsToAdjustColumnWidth The number of rows.
	 */
	public void setMaximumRowsToAdjustColumnWidth(int rowsToAdjustColumnWidth) {
		getTableComponent().setRowsToAdjustColumnWidth(rowsToAdjustColumnWidth);
	}

	/**
	 * Set the table model.
	 * 
	 * @param model The table model.
	 */
	public void setModel(TableModel model) {
		getTableComponent().setModel(model);
	}

	/*
	 * Column width adjustment.
	 */

	/**
	 * Set (ensure) that a given row is visible.
	 * 
	 * @param row The index position of the row.
	 */
	public void setRowVisible(int row) {
		getTableComponent().setRowVisible(row);
	}

	/**
	 * Set the range as selected/unselected.
	 * 
	 * @param topRow      Top row.
	 * @param leftColumn  Left column.
	 * @param bottomRow   Bottom row.
	 * @param rightColumn Right column.
	 * @param selected    A boolean that indicates whether the range must be selected.
	 * @param clear       A boolean that indicated whether the list of previously selected ranges should be cleared.
	 */
	public void setSelectedRange(int topRow, int leftColumn, int bottomRow, int rightColumn, boolean selected,
	boolean clear) {
		getTableComponent().setSelectedRange(topRow, leftColumn, bottomRow, rightColumn, selected, clear);
	}

	/**
	 * Select the row, toggling any previous selection.
	 * 
	 * @param row The row index.
	 */
	public void setSelectedRow(int row) {
		getTableComponent().setSelectedRow(row);
	}

	/**
	 * Set the selected range of rows as selected.
	 * 
	 * @param topRow    Top row.
	 * @param bottomRow Bottom row.
	 */
	public void setSelectedRowRange(int topRow, int bottomRow) {
		getTableComponent().setSelectedRowRange(topRow, bottomRow);
	}

	/**
	 * Set the selected range of rows as selected indicating whether any previous selection should be cleared.
	 * 
	 * @param topRow    Top row.
	 * @param bottomRow Bottom row.
	 * @param clear     A boolean indicating whether any previous selection should be cleared.
	 */
	public void setSelectedRowRange(int topRow, int bottomRow, boolean clear) {
		getTableComponent().setSelectedRowRange(topRow, bottomRow, clear);
	}

	/**
	 * Set the rows a selected.
	 * 
	 * @param rows The rows to select.
	 */
	public void setSelectedRows(int... rows) {
		getTableComponent().setSelectedRows(rows);
	}

	/**
	 * Set the selection mode.
	 * 
	 * @param selectionMode The selection mode.
	 */
	public final void setSelectionMode(SelectionMode selectionMode) {
		getTableComponent().setSelectionMode(selectionMode);
	}

	/**
	 * Sets a boolean that indicates if sorting clicking the header is enabled.
	 * 
	 * @param sortingEnabled A boolean.
	 */
	public void setSortingEnabled(boolean sortingEnabled) {
		getTableComponent().setSortingEnabled(sortingEnabled);
	}
}
