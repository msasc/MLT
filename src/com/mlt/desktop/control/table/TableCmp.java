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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.HierarchyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.mlt.desktop.control.Table;
import com.mlt.desktop.event.HierarchyBoundsHandler;
import com.mlt.desktop.layout.Direction;
import com.mlt.util.Lists;
import com.mlt.util.Numbers;

/**
 * Table component.
 */
public class TableCmp extends JTable {

	/**
	 * Bounds listener.
	 */
	private class BoundsListener extends HierarchyBoundsHandler {

		@Override
		public void ancestorResized(HierarchyEvent e) {
			pageSize = -1;
		}
	}

	/** Parent table control. */
	private Table tableControl;
	/** List of table listeners. */
	private List<TableCmpListener> listeners = new ArrayList<>();

	/*
	 * Column width and row height adjustment.
	 */
	/** A standard margin (inset) to adjust column width. */
	private int marginToAdjustColumnWidth = 15;
	/** The number of rows to adjust column width. */
	private int rowsToAdjustColumnWidth = 100;
	/** Transient row height recalculated every time that columns are resized. */
	private transient int rowHeight = -1;

	/**
	 * Fixed row height. By default fixed row height is set to true and a suitable
	 * row height is calculated every time
	 * the width of the columns is recalculated, scanning 'rowsToAdjustColumnWidth'
	 * rows.
	 * <p>
	 * If fixed row height is set to false, a complete scan of all rows is required
	 * to set the height of each row. This
	 * is done again when adjusting columns widths, and the
	 * 'rowsToAdjustColumnWidth' is replaced by the total number of
	 * rows.
	 */
	private boolean fixedRowHeight = true;
	/** Page size, re-calculated at every resize event. */
	private int pageSize = -1;

	/*
	 * Sorting.
	 */
	/** The table sort definition. */
	private TableSorter tableSort;
	/** A boolean that indicates if sorting clicking the header is enabled. */
	boolean sortingEnabled = true;

	/*
	 * Cell selection mode helper members.
	 */
	/** Selection mode. */
	private SelectionMode selectionMode;
	/** List of selected/unselected ranges. */
	private List<Range> selectedRanges = new ArrayList<>();
	/** Previous focus cell. */
	private Cell previousFocusCell;
	/** Current focused cell. */
	private Cell focusCell = null;
	/** Horizontal movement of current cell. */
	private Direction horizontalDirection = Direction.NONE;
	/** Vertical movement of current cell. */
	private Direction verticalDirection = Direction.NONE;

	/** Table configuration. */
	private TableCfg cfg = new TableCfg();

	/**
	 * Private constructor.
	 */
	public TableCmp() {
		super();
	}

	/**
	 * Return the configuration parameters.
	 *
	 * @return
	 */
	public TableCfg getCfg() {
		return cfg;
	}

	/**
	 * Return the parent table control.
	 *
	 * @return The parent table control.
	 */
	public Table getControl() {
		return tableControl;
	}

	/**
	 * Add a table listener.
	 *
	 * @param listener The listener.
	 */
	public void addListener(TableCmpListener listener) {
		listeners.add(listener);
	}

	/**
	 * Adjust the column size. Based on the current header and row-column preferred
	 * width, scanning a maximum number of
	 * rows randomly including the first and last rows.
	 *
	 * @param column The column number.
	 */
	public void adjustColumnWidth(int column) {
		int[] rows = getRowsToAdjustColumnWidth();
		adjustColumnWidth(column, rows);
	}

	/**
	 * Adjust the column width using information from the list of rows.
	 *
	 * @param viewColumn The view column.
	 * @param viewRows   The view rows to check.
	 */
	private void adjustColumnWidth(int viewColumn, int[] viewRows) {

		TableColumn tableColumn = getColumnModel().getColumn(viewColumn);

		/*
		 * First get the preferred width of the header by getting its value and its
		 * renderer.
		 */
		Object headerValue = tableColumn.getHeaderValue();
		TableCellRenderer headerRenderer = tableColumn.getHeaderRenderer();
		if (headerRenderer == null) {
			headerRenderer = getTableHeader().getDefaultRenderer();
		}
		Dimension headerSize = getPreferredSize(headerRenderer, headerValue, 0, viewColumn);
		int headerWidth = headerSize.width + marginToAdjustColumnWidth;

		/*
		 * Then scan the rows and the width of the column. Keep the maximum between the
		 * row and the header.
		 */
		int modelColumn = convertColumnIndexToModel(viewColumn);
		int dataWidth = 0;
		for (int i = 0; i < viewRows.length; i++) {
			int row = viewRows[i];
			int modelRow = convertRowIndexToModel(row);
			Object value = getModel().getValueAt(modelRow, modelColumn);
			TableCellRenderer renderer = tableColumn.getCellRenderer();
			Dimension size = getPreferredSize(renderer, value, row, viewColumn);
			int width = size.width + marginToAdjustColumnWidth;
			dataWidth = Math.max(dataWidth, width);

			/* Adjust row height. */
			rowHeight = Math.max(rowHeight, size.height);
		}

		/*
		 * Set the preferred width as the maximum of the header and the data widths.
		 */
		tableColumn.setPreferredWidth(Math.max(dataWidth, headerWidth));
	}

	/**
	 * Adjust the width of all columns.
	 */
	public void adjustColumnWidths() {

		/* Reset the row height. */
		rowHeight = -1;

		/* Process columns. */
		int columns = getModel().getColumnCount();
		int[] rows = getRowsToAdjustColumnWidth();
		for (int column = 0; column < columns; column++) {
			adjustColumnWidth(column, rows);
		}

		/* Set the recalculated row height. */
		if (rowHeight > 0 && fixedRowHeight) {
			setRowHeight(rowHeight);
		} else if (!fixedRowHeight) {
			setRowHeights();
		}
	}

	/**
	 * Set the model and notify that data has changed..
	 *
	 * @param The data model.
	 */
	public void setModel(TableModel dataModel) {
		/* If the row header is present, add it as model listener. */
		if (tableControl.getComponent().getRowHeader() != null) {
			JViewport viewport = (JViewport) tableControl.getComponent().getRowHeader();
			Component rowHeaderView = viewport.getView();
			if (rowHeaderView instanceof RowHeader) {
				RowHeader rowHeader = (RowHeader) rowHeaderView;
				dataModel.addTableModelListener(rowHeader);
			}
		}
		/* Do set the model. */
		super.setModel(dataModel);
		dataModel.fireTableDataChanged();
	}

	/**
	 * Calculate and set each row height.
	 */
	private void setRowHeights() {
		int rows = getRowCount();
		for (int row = 0; row < rows; row++) {
			setRowHeight(row, calculateRowHeight(row));
		}
	}

	/**
	 * Calculate the required height for a given row.
	 *
	 * @param row The row index.
	 * @return The height.
	 */
	private int calculateRowHeight(int row) {
		int rowHeight = 0;
		int columns = getColumnCount();
		for (int column = 0; column < columns; column++) {
			TableColumn tableColumn = getColumnModel().getColumn(column);
			int modelRow = convertRowIndexToModel(row);
			int modelColumn = convertColumnIndexToModel(column);
			Object value = getModel().getValueAt(modelRow, modelColumn);
			TableCellRenderer renderer = tableColumn.getCellRenderer();
			if (renderer == null) continue;
			Dimension size = getPreferredSize(renderer, value, row, column);
			rowHeight = Math.max(rowHeight, size.height);
		}
		return rowHeight;
	}

	/**
	 * Clear any selection.
	 */
	@Override
	public void clearSelection() {
		if (selectedRanges == null) return;
		selectedRanges.clear();
		revalidate();
		repaint();
	}

	/**
	 * Check whether the range contains the column.
	 *
	 * @param range  The range.
	 * @param column The column.
	 * @return A boolean.
	 */
	private boolean containsColumn(Range range, int column) {
		if (getRangeHeight(range) < getRowCount()) return false;
		return range.getLeftColumn() >= column && range.getRightColumn() <= column;
	}

	/**
	 * Check if the range contains the row.
	 *
	 * @param range The range.
	 * @param row   The row.
	 * @return A boolean.
	 */
	private boolean containsRow(Range range, int row) {
		if (getRangeWidth(range) < getColumnCount()) return false;
		return range.getTopRow() >= row && range.getBottomRow() <= row;
	}

	/**
	 * Convert a cell to a range..
	 *
	 * @param cell     The cell.
	 * @param selected A boolean indicating whether the range is selected.
	 * @return The range.
	 */
	private Range convertCelltoRange(Cell cell, boolean selected) {
		int topRow, leftColumn, bottomRow, rightColumn;
		if (selectionMode.isCell()) {
			topRow = cell.getRow();
			leftColumn = cell.getColumn();
			bottomRow = cell.getRow();
			rightColumn = cell.getColumn();
		} else if (selectionMode.isColumn()) {
			topRow = 0;
			leftColumn = cell.getColumn();
			bottomRow = Numbers.MAX_INTEGER;
			rightColumn = cell.getColumn();
		} else {
			topRow = cell.getRow();
			leftColumn = 0;
			bottomRow = cell.getRow();
			rightColumn = Numbers.MAX_INTEGER;
		}
		return new Range(topRow, leftColumn, bottomRow, rightColumn, selected);
	}

	/**
	 * Extend the range with a cell that is moving in the current directions.
	 *
	 * @param range The range
	 * @param cell  The cell.
	 */
	private void extendRange(Range range, Cell cell) {

		int rowIndex = cell.getRow();
		int columnIndex = cell.getColumn();

		int topRow = Math.min(range.getTopRow(), rowIndex);
		int leftColumn = Math.min(range.getLeftColumn(), columnIndex);
		int bottomRow = Math.max(range.getBottomRow(), rowIndex);
		int rightColumn = Math.max(range.getRightColumn(), columnIndex);

		if (selectionMode.isCell() || selectionMode.isRow()) {
			if (rowIndex >= range.getTopRow() && rowIndex <= range.getBottomRow()) {
				if (verticalDirection.isUp()) bottomRow = rowIndex;
				if (verticalDirection.isDown()) topRow = rowIndex;
			}
		}
		if (selectionMode.isCell() || selectionMode.isColumn()) {
			if (columnIndex >= range.getLeftColumn() && columnIndex <= range.getRightColumn()) {
				if (horizontalDirection.isRight()) leftColumn = columnIndex;
				if (horizontalDirection.isLeft()) rightColumn = columnIndex;
			}
		}

		range.setTopRow(topRow);
		range.setLeftColumn(leftColumn);
		range.setBottomRow(bottomRow);
		range.setRightColumn(rightColumn);
	}

	/**
	 * Fire cell focus changed.
	 */
	protected void fireFocusCellChanged() {
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).focusCellChanged(this);
		}
	}

	/**
	 * Fire selection changed.
	 */
	protected void fireSelectionChanged() {
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).selectionChanged(this);
		}
	}

	/**
	 * Return the focused cell.
	 *
	 * @return The last focused cell or null if none was focused.
	 */
	public Cell getFocusCell() {
		return focusCell;
	}

	/**
	 * Return the focused column.
	 *
	 * @return The focused column.
	 */
	public int getFocusColumn() {
		return (focusCell == null ? -1 : focusCell.getColumn());
	}

	/**
	 * Return the focused row.
	 *
	 * @return The focused row.
	 */
	public int getFocusRow() {
		return (focusCell == null ? -1 : focusCell.getRow());
	}

	/**
	 * Return the page size, used for PG_DOWN and PG_UP keys.
	 *
	 * @return The page size.
	 */
	int getPageSize() {
		if (pageSize == -1 || !fixedRowHeight) {
			int firstRow = 0;
			if (!isRowVisible(firstRow)) {
				while (!isRowVisible(firstRow) && firstRow < getRowCount() - 1) {
					firstRow++;
				}
			}
			if (!isRowVisible(firstRow)) return 0;
			int lastRow = firstRow;
			while (isRowVisible(lastRow + 1) && lastRow < getRowCount() - 1) {
				lastRow++;
			}
			pageSize = lastRow - firstRow;
		}
		return pageSize;
	}

	/**
	 * Return the previous focus cell, set when the focus cell changes.
	 *
	 * @return The previous focus cell.
	 */
	public Cell getPreviousFocusCell() {
		return previousFocusCell;
	}

	/**
	 * Return the preferred size (not selected and not focused).
	 *
	 * @param renderer The table cell renderer.
	 * @param value    The value.
	 * @param row      The row.
	 * @param column   The column.
	 * @return The preferred size.
	 */
	private Dimension getPreferredSize(
		TableCellRenderer renderer,
		Object value,
		int row,
		int column) {
		return renderer.getTableCellRendererComponent(this, value, false, false, row, column)
			.getPreferredSize();
	}

	/**
	 * Return the list of cells in the range.
	 *
	 * @param range The range.
	 * @return The list of cells.
	 */
	private List<Cell> getRangeCells(Range range) {
		int topRow = range.getTopRow();
		int leftColumn = range.getLeftColumn();
		int bottomRow = Math.min(range.getBottomRow(), getRowCount() - 1);
		int rightColumn = Math.min(range.getRightColumn(), getColumnCount() - 1);
		List<Cell> cells = new ArrayList<>();
		for (int row = topRow; row <= bottomRow; row++) {
			for (int column = leftColumn; row <= rightColumn; row++) {
				cells.add(new Cell(row, column));
			}
		}
		return cells;
	}

	/**
	 * Return the total number of cell adding all the ranges in the list.
	 *
	 * @return The total number of cells.
	 */
	int getRangeCellsCount() {
		int count = 0;
		for (int i = 0; i < selectedRanges.size(); i++) {
			count += getRangeCellsCount(selectedRanges.get(i));
		}
		return count;
	}

	/**
	 * Return the number of cells in the range.
	 *
	 * @param range The range to check.
	 * @return The number of cell.
	 */
	private int getRangeCellsCount(Range range) {
		return getRangeHeight(range) * getRangeWidth(range);
	}

	/**
	 * Return the list with all range columns.
	 *
	 * @param range The range.
	 * @return The list of columns.
	 */
	private List<Integer> getRangeColumns(Range range) {
		List<Integer> columns = new ArrayList<>();
		if (getRangeHeight(range) < getRowCount()) return columns;
		for (int column = range.getLeftColumn(); column <= range.getRightColumn(); column++) {
			columns.add(column);
		}
		return columns;
	}

	/**
	 * Return the total number of columns adding all ranges in the list.
	 *
	 * @return The total number of rows.
	 */
	int getRangeColumnsCount() {
		int count = 0;
		for (int i = 0; i < selectedRanges.size(); i++) {
			count += getRangeColumnsCount(selectedRanges.get(i));
		}
		return count;
	}

	/**
	 * Returns the number of rows in a columns cell range.
	 *
	 * @param range The cell range.
	 * @return The number of columns.
	 */
	private int getRangeColumnsCount(Range range) {
		if (getRangeHeight(range) < getRowCount()) return 0;
		return (range.getRightColumn() - range.getLeftColumn() + 1);
	}

	/**
	 * Return the range height.
	 *
	 * @param range The range.
	 * @return The range height.
	 */
	private int getRangeHeight(Range range) {
		int topRow = range.getTopRow();
		int bottomRow = Math.min(range.getBottomRow(), getRowCount() - 1);
		return bottomRow - topRow + 1;
	}

	/**
	 * Return the list with all range rows.
	 *
	 * @param range The range.
	 * @return The list of rows.
	 */
	private List<Integer> getRangeRows(Range range) {
		List<Integer> rows = new ArrayList<>();
		if (getRangeWidth(range) < getColumnCount()) return rows;
		for (int row = range.getTopRow(); row <= range.getBottomRow(); row++) {
			rows.add(row);
		}
		return rows;
	}

	/**
	 * Return the total number of cell adding all ranges in the list.
	 *
	 * @return The total number of rows.
	 */
	int getRangeRowsCount() {
		int count = 0;
		for (int i = 0; i < selectedRanges.size(); i++) {
			Range range = selectedRanges.get(i);
			count += (getRangeWidth(range) < getColumnCount() ? 0 : getRangeHeight(range));
		}
		return count;
	}

	/**
	 * Return the range width.
	 *
	 * @param range The range.
	 * @return The range width.
	 */
	private int getRangeWidth(Range range) {
		int leftColumn = range.getLeftColumn();
		int rightColumn = Math.min(range.getRightColumn(), getColumnCount() - 1);
		return rightColumn - leftColumn + 1;
	}

	/**
	 * Return the rows to adjust column width.
	 *
	 * @return The list of rows.
	 */
	private int[] getRowsToAdjustColumnWidth() {
		int[] rows = null;
		int count = 0;
		if (getRowCount() <= rowsToAdjustColumnWidth) {
			count = getRowCount();
			rows = new int[count];
			for (int row = 0; row < count; row++) {
				rows[row] = row;
			}
		} else {
			count = rowsToAdjustColumnWidth;
			int rowCount = getRowCount();
			int page = (int) Numbers.round(count * 0.50, 0);
			rows = new int[count];
			int index = 0;
			/* First page. */
			for (int i = 0; i < page; i++) {
				rows[index] = i;
				index++;
			}
			/* Last page. */
			for (int i = rowCount - page; i < rowCount; i++) {
				rows[index] = i;
				index++;
			}
		}
		return rows;
	}

	/**
	 * Return the first selected cell or null.
	 *
	 * @return The first selected cell or null.
	 */
	public Cell getSelectedCell() {
		if (!selectionMode.isCell()) {
			throw new IllegalStateException();
		}
		if (getSelectedCellCount() == 0) {
			return null;
		}
		return getSelectedCells()[0];
	}

	/**
	 * Return the number of selected cells when the selection mode is cell.
	 *
	 * @return The number of selected cells.
	 */
	public int getSelectedCellCount() {
		if (!selectionMode.isCell()) {
			throw new IllegalStateException();
		}
		return getSelectedCellsList().size();
	}

	/**
	 * Returns an array with all selected cells.
	 *
	 * @return The selected cells.
	 */
	public Cell[] getSelectedCells() {
		if (!selectionMode.isCell()) {
			throw new IllegalStateException();
		}
		List<Cell> selectedCellsList = getSelectedCellsList();
		Cell[] selectedCells = new Cell[selectedCellsList.size()];
		for (int i = 0; i < selectedCellsList.size(); i++) {
			selectedCells[i] = selectedCellsList.get(i);
		}
		return selectedCells;
	}

	/**
	 * Return the list with all selected cells.
	 *
	 * @return The list with all selected cells.
	 */
	public List<Cell> getSelectedCellsList() {
		List<Cell> cells = new ArrayList<>();
		for (int i = 0; i < selectedRanges.size(); i++) {
			Range range = selectedRanges.get(i);
			List<Cell> rangeCells = getRangeCells(range);
			if (range.isSelected()) {
				cells.addAll(rangeCells);
			} else {
				cells.removeAll(rangeCells);
			}
		}
		return cells;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getSelectedColumn() {
		if (!selectionMode.isColumn()) {
			throw new IllegalStateException();
		}
		if (getSelectedColumnCount() == 0) {
			return -1;
		}
		return getSelectedColumns()[0];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getSelectedColumnCount() {
		return getSelectedColumnsList().size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int[] getSelectedColumns() {
		if (!selectionMode.isColumn()) {
			throw new IllegalStateException();
		}
		if (!selectionMode.isColumn()) {
			return new int[0];
		}
		List<Integer> selectedColumnsList = getSelectedColumnsList();
		int[] selectedColumns = new int[selectedColumnsList.size()];
		for (int i = 0; i < selectedColumnsList.size(); i++) {
			selectedColumns[i] = selectedColumnsList.get(i);
		}
		return selectedColumns;
	}

	/**
	 * Return the list with all selected columns.
	 *
	 * @return The list with all selected columns.
	 */
	public List<Integer> getSelectedColumnsList() {
		List<Integer> columns = new ArrayList<>();
		for (int i = 0; i < selectedRanges.size(); i++) {
			Range range = selectedRanges.get(i);
			List<Integer> rangeColumns = getRangeColumns(range);
			if (range.isSelected()) {
				columns.addAll(rangeColumns);
			} else {
				columns.removeAll(rangeColumns);
			}
		}
		return columns;
	}

	/**
	 * @return the selectedRanges
	 */
	List<Range> getSelectedRanges() {
		return selectedRanges;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getSelectedRow() {
		if (getSelectedRowCount() == 0) {
			return -1;
		}
		return getSelectedRows()[0];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getSelectedRowCount() {
		if (!selectionMode.isRow()) {
			return 0;
		}
		return getSelectedRowsList().size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int[] getSelectedRows() {
		if (!selectionMode.isRow()) {
			return new int[0];
		}
		List<Integer> selectedRowsList = getSelectedRowsList();
		int[] selectedRows = new int[selectedRowsList.size()];
		for (int i = 0; i < selectedRowsList.size(); i++) {
			selectedRows[i] = selectedRowsList.get(i);
		}
		return selectedRows;
	}

	/**
	 * Return the list with all selected rows.
	 *
	 * @return The list with all selected rows.
	 */
	public List<Integer> getSelectedRowsList() {
		List<Integer> rows = new ArrayList<>();
		for (int i = 0; i < selectedRanges.size(); i++) {
			Range range = selectedRanges.get(i);
			List<Integer> rangeRows = getRangeRows(range);
			if (range.isSelected()) {
				rows.addAll(rangeRows);
			} else {
				rows.removeAll(rangeRows);
			}
		}
		return rows;
	}

	/**
	 * Return the current selection mode.
	 *
	 * @return The current selection mode.
	 */
	public SelectionMode getSelectionMode() {
		return selectionMode;
	}

	/**
	 * Check if the cell is focused.
	 *
	 * @param row    The row.
	 * @param column The column.
	 * @return A boolean.
	 */
	public boolean isCellFocused(int row, int column) {
		if (focusCell != null) {
			return (focusCell.getRow() == row && focusCell.getColumn() == column);
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Override to support Table.SelectionMode
	 */
	@Override
	public boolean isCellSelected(int row, int column) {
		boolean selected = false;
		for (int i = 0; i < selectedRanges.size(); i++) {
			Range range = selectedRanges.get(i);
			if (range.containsCell(row, column)) {
				selected = range.isSelected();
			}
		}
		return selected;
	}

	/**
	 * Check if a cell is totally visible.
	 *
	 * @param row    The row index.
	 * @param column The column index.
	 * @return A boolean.
	 */
	public boolean isCellVisible(int row, int column) {
		Container parent = getParent();
		if (!(parent instanceof JViewport)) {
			return true;
		}
		JViewport viewport = (JViewport) parent;
		Rectangle rect = getCellRect(row, column, true);
		Point pt = viewport.getViewPosition();
		rect.setLocation(rect.x - pt.x, rect.y - pt.y);
		rect.x = 0;
		rect.width = 1;
		return new Rectangle(viewport.getExtentSize()).contains(rect);
	}

	/**
	 * Check if the column is focused.
	 *
	 * @param column The column.
	 * @return A boolean.
	 */
	public boolean isColumnFocused(int column) {
		if (focusCell != null) {
			return (focusCell.getColumn() == column);
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Override to support Table.SelectionMode
	 */
	@Override
	public boolean isColumnSelected(int column) {
		boolean selected = false;
		for (int i = 0; i < selectedRanges.size(); i++) {
			Range range = selectedRanges.get(i);
			if (containsColumn(range, column)) {
				selected = range.isSelected();
			}
		}
		return selected;
	}

	/**
	 * Check if a column is totally visible.
	 *
	 * @param column The index position of the column.
	 * @return <code>true</code> if the column is visible.
	 */
	public boolean isColumnVisible(int column) {
		return isCellVisible(0, column);
	}

	/**
	 * Check if the table is fixed row height.
	 *
	 * @return A boolean.
	 */
	public boolean isFixedRowHeight() {
		return fixedRowHeight;
	}

	/**
	 * Check if the row is focused.
	 *
	 * @param row The row.
	 * @return A boolean.
	 */
	public boolean isRowFocused(int row) {
		if (focusCell != null) {
			return (focusCell.getRow() == row);
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Override to support Table.SelectionMode
	 */
	@Override
	public boolean isRowSelected(int row) {
		boolean selected = false;
		for (int i = 0; i < selectedRanges.size(); i++) {
			Range range = selectedRanges.get(i);
			if (containsRow(range, row)) {
				selected = range.isSelected();
			}
		}
		return selected;
	}

	/**
	 * Check if a row is totally visible.
	 *
	 * @param row The index position of the row.
	 * @return <code>true</code> if the row is visible.
	 */
	public boolean isRowVisible(int row) {
		return isCellVisible(row, 0);
	}

	public boolean isSortingEnabled() {
		return sortingEnabled;
	}

	/**
	 * Change the focus to the argument cell.
	 *
	 * @param rowIndex    The row index.
	 * @param columnIndex The column index.
	 */
	public void moveFocus(int rowIndex, int columnIndex) {
		moveSelection(rowIndex, columnIndex, null, null);
	}

	/**
	 * Move the selection or the focus. If toggle and extend are null, only the
	 * focus is changed.
	 *
	 * @param rowIndex    Row index.
	 * @param columnIndex Column index.
	 * @param toggle      A boolean indicating if toggle (ctrl key) is pressed. The
	 *                    selection should be toggled.
	 * @param extend      A boolean indicating whether to extend (shift key) the
	 *                    selection.
	 */
	void moveSelection(int rowIndex, int columnIndex, Boolean toggle, Boolean extend) {

		/* Previous and current change cells. */
		Cell cell = new Cell(rowIndex, columnIndex);
		Cell previousFocusCell = focusCell;
		if (focusCell == null) {
			focusCell = cell;
		}

		/*
		 * Check horizontal and vertical direction of next cell.
		 */
		if (focusCell.getColumn() == cell.getColumn()) {
			horizontalDirection = Direction.NONE;
		} else if (focusCell.getColumn() < cell.getColumn()) {
			horizontalDirection = Direction.RIGHT;
		} else {
			horizontalDirection = Direction.LEFT;
		}
		if (focusCell.getRow() == cell.getRow()) {
			verticalDirection = Direction.NONE;
		} else if (focusCell.getRow() < cell.getRow()) {
			verticalDirection = Direction.DOWN;
		} else {
			verticalDirection = Direction.UP;
		}

		/*
		 * Check whether this is a selection change request or an only focus change
		 * request. When both toggle and extend
		 * are null, only the focus is changed.
		 */
		boolean selectionChange = (toggle != null && extend != null);
		if (selectionChange) {

			if (selectionMode.isSingleSelection()) {
				toggle = false;
				extend = false;
			} else if (selectionMode.isSingleInterval()) {
				toggle = false;
			}

			/* Apply. */
			if (selectionMode != SelectionMode.NONE) {

				if (!extend) {
					boolean selected = true;
					boolean clear = true;
					if (toggle) {
						if (selectionMode
							.isCell()) selected = !isCellSelected(rowIndex, columnIndex);
						if (selectionMode.isColumn()) selected = !isColumnSelected(columnIndex);
						if (selectionMode.isRow()) selected = !isRowSelected(rowIndex);
						clear = false;
					}
					setSelectedRange(convertCelltoRange(cell, selected), clear);
				} else {
					Range range = Lists.getLast(selectedRanges);
					extendRange(range, cell);
					setSelectedRange(range, false);
				}

			}
		}

		/* Register last changed cell. */
		focusCell = cell;

		/* Fire focus and selection changes. */
		if (!cell.equals(previousFocusCell)) {
			this.previousFocusCell = previousFocusCell;
			/* If the focus column has changed, repaint the header. */
			updateHeaderFocus();
			/* Fire focus change. */
			fireFocusCellChanged();
			/* Fire selection change if applicable. */
			if (selectionChange) fireSelectionChanged();
		}

		/* Required repaint. */
		repaint();

		/* Scroll after repainting. */
		Rectangle cellRect = getCellRect(rowIndex, columnIndex, true);
		if (cellRect != null) {
			scrollRectToVisible(cellRect);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
		Object value = getValueAt(row, column);
		return renderer.getTableCellRendererComponent(this, value, false, false, row, column);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void selectAll() {
		int topRow = 0;
		int leftColumn = 0;
		int bottomRow = getRowCount() - 1;
		int rightColumn = getColumnCount() - 1;
		setSelectedRange(topRow, leftColumn, bottomRow, rightColumn, true, true);
	}

	/**
	 * Set the additional margin to cell size to adjust column width.
	 *
	 * @param marginToAdjustColumnWidth The additional margin.
	 */
	public void setMarginToAdjustColumnWidth(int marginToAdjustColumnWidth) {
		this.marginToAdjustColumnWidth = marginToAdjustColumnWidth;
	}

	/**
	 * Set the number of random rows to check to adjust column width.
	 *
	 * @param rowsToAdjustColumnWidth The number of rows.
	 */
	public void setRowsToAdjustColumnWidth(int rowsToAdjustColumnWidth) {
		if (!Numbers.isEven(rowsToAdjustColumnWidth)) {
			throw new IllegalArgumentException("Rows to adjust column withs must be even.");
		}
		this.rowsToAdjustColumnWidth = rowsToAdjustColumnWidth;
	}

	/**
	 * Set (ensure) that a given row is visible.
	 *
	 * @param row The index position of the row.
	 */
	public void setRowVisible(int row) {
		if (isRowVisible(row)) {
			return;
		}
		Rectangle rect = getCellRect(row, 0, true);
		scrollRectToVisible(rect);
	}

	/**
	 * Set the range as selected/unselected.
	 *
	 * @param topRow      Top row.
	 * @param leftColumn  Left column.
	 * @param bottomRow   Bottom row.
	 * @param rightColumn Right column.
	 * @param selected    A boolean that indicates whether the range must be
	 *                    selected.
	 * @param clear       A boolean that indicated whether the list of previously
	 *                    selected ranges should be cleared.
	 */
	public void setSelectedRange(
		int topRow,
		int leftColumn,
		int bottomRow,
		int rightColumn,
		boolean selected,
		boolean clear) {

		if (topRow < 0) return;
		if (leftColumn < 0) return;
		if (bottomRow < 0) return;
		if (bottomRow >= getRowCount() && bottomRow != Numbers.MAX_INTEGER) return;
		if (rightColumn < 0) return;
		if (rightColumn >= getColumnCount() && rightColumn != Numbers.MAX_INTEGER) return;

		EventQueue.invokeLater(() -> {
			setSelectedRange(new Range(topRow, leftColumn, bottomRow, rightColumn, selected),
				clear);
			if (leftColumn == 0 && rightColumn == Numbers.MAX_INTEGER) {
				if (topRow == bottomRow) {
					moveFocus(topRow, leftColumn);
				} else {
					moveFocus(bottomRow, leftColumn);
				}
			} else {
				moveFocus(bottomRow, rightColumn);
			}
			updateHeaderFocus();
		});
	}

	/**
	 * Set/add the argument range to the list of selection ranges.
	 *
	 * @param range The range.
	 * @param clear A boolean indicating whether the list of ranges should be
	 *              cleared.
	 */
	private void setSelectedRange(Range range, boolean clear) {
		if (clear) {
			selectedRanges.clear();
		}
		selectedRanges.remove(range);
		selectedRanges.add(range);
	}

	/**
	 * Select the row, toggling any previous selection.
	 *
	 * @param row The row index.
	 */
	public void setSelectedRow(int row) {
		setSelectedRow(row, true);
	}

	/**
	 * Select the row indicating whether any previous selection should be cleared.
	 *
	 * @param row   The row index.
	 * @param clear A boolean indicating whether any previous selection should be
	 *              cleared.
	 */
	public void setSelectedRow(int row, boolean clear) {
		setSelectedRange(row, 0, row, Numbers.MAX_INTEGER, true, clear);
	}

	/**
	 * Set the selected range of rows as selected.
	 *
	 * @param topRow    Top row.
	 * @param bottomRow Bottom row.
	 */
	public void setSelectedRowRange(int topRow, int bottomRow) {
		setSelectedRowRange(topRow, bottomRow, true);
	}

	/**
	 * Set the selected range of rows as selected indicating whether any previous
	 * selection should be cleared.
	 *
	 * @param topRow    Top row.
	 * @param bottomRow Bottom row.
	 * @param clear     A boolean indicating whether any previous selection should
	 *                  be cleared.
	 */
	public void setSelectedRowRange(int topRow, int bottomRow, boolean clear) {
		setSelectedRange(topRow, 0, bottomRow, Numbers.MAX_INTEGER, true, clear);
	}

	/**
	 * Set the rows a selected.
	 *
	 * @param rows The rows to select.
	 */
	public void setSelectedRows(int... rows) {
		for (int row : rows) {
			setSelectedRange(row, 0, row, Numbers.MAX_INTEGER, true, false);
		}
	}

	/**
	 * Set the selection mode.
	 *
	 * @param selectionMode The selection mode.
	 */
	public void setSelectionMode(SelectionMode selectionMode) {
		if (selectionMode == null) throw new NullPointerException();
		this.selectionMode = selectionMode;
	}

	public void setSortingEnabled(boolean sortingEnabled) {
		this.sortingEnabled = sortingEnabled;
	}

	/**
	 * Set the parent table control.
	 *
	 * @param tableCtrl The table control.
	 */
	public void setTableControl(Table tableCtrl) {
		this.tableControl = tableCtrl;
	}

	/**
	 * Setup this table.
	 * 
	 * @param displayRowHeader A boolean that indicates whether to display a row
	 *                         header with row numbers.
	 */
	public void setup(boolean displayRowHeader) {

		/* Auto-resize mode. */
		setAutoResizeMode(TableCmp.AUTO_RESIZE_OFF);

		/* Add the bounds listener. */
		addHierarchyBoundsListener(new BoundsListener());

		/* Set the UI. */
		setUI(new TableCmpUI());

		/* Set header renderer and mouse handler. */
		getTableHeader().setDefaultRenderer(new HeaderCellRenderer(this));
		getTableHeader().addMouseListener(new HeaderMouseHandler(this));

		/* Set the row header. */
		if (displayRowHeader) {
			RowHeader rowHeader = new RowHeader(this);
			addListener(rowHeader);
			tableControl.getComponent().setRowHeaderView(rowHeader);
		}
	}

	/**
	 * Setup header after a sort.
	 */
	private void setupHeaders() {
		if (tableSort == null) {
			return;
		}
		Icon iconAscending = UIManager.getIcon("Table.ascendingSortIcon");
		Icon iconDescending = UIManager.getIcon("Table.descendingSortIcon");
		getTableHeader().setDefaultRenderer(null);
		for (int viewColumn = 0; viewColumn < getColumnCount(); viewColumn++) {
			TableColumn tableColumn = getColumnModel().getColumn(viewColumn);
			if (tableColumn.getHeaderRenderer() == null) {
				tableColumn.setHeaderRenderer(new HeaderCellRenderer(this));
			}
			HeaderCellRenderer renderer = (HeaderCellRenderer) tableColumn.getHeaderRenderer();
			int modelColumn = convertColumnIndexToModel(viewColumn);
			if (tableSort.contains(modelColumn)) {
				if (tableSort.getKey(modelColumn).isAscending()) {
					renderer.setSortIcon(iconAscending);
				} else {
					renderer.setSortIcon(iconDescending);
				}
			} else {
				renderer.setSortIcon(null);
			}
		}

		/* Adjust column widths and layout. */
		adjustColumnWidths();
	}

	/**
	 * Do sort by clicking the column header.
	 *
	 * @param column      The column.
	 * @param controlMask The control mask.
	 */
	void sort(int column, boolean controlMask) {

		/* Clear selection and focused cell. */
		focusCell = null;
		selectedRanges.clear();

		/*
		 * Convert to model column, because its the model who will sort the data.
		 */
		int modelColumn = convertColumnIndexToModel(column);

		/*
		 * If the model is not an instance of TableModel, this sort can not be executed.
		 */
		if (!(getModel() instanceof TableModel)) {
			throw new IllegalStateException();
		}
		TableModel model = (TableModel) getModel();

		/* Current table sort definition. */
		if (tableSort == null) {
			tableSort = new TableSorter();
		}

		/* List of keys. */
		List<TableSorter.Key> keys = tableSort.getKeys();

		/*
		 * Not control mask. If there is only one sort key and it matches the column,
		 * switch ascending/descending. If it
		 * does not matches the column or there is more than one sort key, then reset
		 * the sort keys to the column
		 * ascending.
		 */
		if (!controlMask) {
			if (keys.size() == 1 && keys.get(0).getColumn() == modelColumn) {
				/* Only one sort key and it matches the column. */
				keys.get(0).toggleOrder();
			} else {
				/* Reset the sort keys. */
				keys.clear();
				keys.add(new TableSorter.Key(modelColumn, TableSorter.Order.ASCENDING));
			}
		}

		/*
		 * Control mask. Add the column to the order or switch ascending/descending.
		 */
		if (controlMask) {
			if (tableSort.contains(modelColumn)) {
				/* The sort definition contains the column, toggle order. */
				tableSort.getKey(modelColumn).toggleOrder();
			} else {
				/* The column is not contained, add it ascending. */
				keys.add(new TableSorter.Key(modelColumn, TableSorter.Order.ASCENDING));
			}
		}

		/* Sort the model. */
		model.sort(tableSort);

		/* Setup headers and adjust column widths. */
		setupHeaders();

		/* Fire focus and selection changes. */
		fireFocusCellChanged();
		fireSelectionChanged();
	}

	/**
	 * Update header focus.
	 */
	private void updateHeaderFocus() {
		if (previousFocusCell == null || focusCell == null
			|| focusCell.getColumn() != previousFocusCell.getColumn()) {
			getTableHeader().resizeAndRepaint();
		}
	}
}
