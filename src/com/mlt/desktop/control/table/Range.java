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
package com.mlt.desktop.control.table;

/**
 * Cell range.
 * 
 * @author Miquel Sas
 */
public class Range {

	/** Top row. */
	int topRow;
	/** Left column. */
	int leftColumn;
	/** Bottom row. */
	int bottomRow;
	/** Right column. */
	int rightColumn;

	/** A boolean that indicates if the range is selected. */
	private boolean selected;

	/**
	 * Constructor.
	 * 
	 * @param topRow      Top row.
	 * @param leftColumn  Left column.
	 * @param bottomRow   Bottom row.
	 * @param rightColumn Right column.
	 * @param selected    A boolean that indicates whether the range is selected.
	 */
	public Range(int topRow, int leftColumn, int bottomRow, int rightColumn, boolean selected) {
		super();
		this.topRow = topRow;
		this.leftColumn = leftColumn;
		this.bottomRow = bottomRow;
		this.rightColumn = rightColumn;
		this.selected = selected;
	}

	/**
	 * Check if the cell is contained in the range.
	 * 
	 * @param row    The cell row.
	 * @param column The cell column.
	 * @return A boolean.
	 */
	public boolean containsCell(int row, int column) {
		if (column < leftColumn || column > rightColumn)
			return false;
		if (row < topRow || row > bottomRow)
			return false;
		return true;
	}

	/**
	 * Return the top row.
	 * 
	 * @return The top row.
	 */
	public int getTopRow() {
		return topRow;
	}

	/**
	 * Set the top row.
	 * 
	 * @param topRow The top row.
	 */
	public void setTopRow(int topRow) {
		this.topRow = topRow;
	}

	/**
	 * Return the left column.
	 * 
	 * @return The left column.
	 */
	public int getLeftColumn() {
		return leftColumn;
	}

	/**
	 * Set the left column.
	 * 
	 * @param leftColumn The left column.
	 */
	public void setLeftColumn(int leftColumn) {
		this.leftColumn = leftColumn;
	}

	/**
	 * Return the bottom row.
	 * 
	 * @return The bottom row.
	 */
	public int getBottomRow() {
		return bottomRow;
	}

	/**
	 * Set the bottom row.
	 * 
	 * @param bottomRow The bottom row.
	 */
	public void setBottomRow(int bottomRow) {
		this.bottomRow = bottomRow;
	}

	/**
	 * Return the right column.
	 * 
	 * @return The right column.
	 */
	public int getRightColumn() {
		return rightColumn;
	}

	/**
	 * Set the right column.
	 * 
	 * @param rightColumn The right column.
	 */
	public void setRightColumn(int rightColumn) {
		this.rightColumn = rightColumn;
	}

	/**
	 * Check if the range is selected.
	 * 
	 * @return A boolean.
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Set the range as selected.
	 * 
	 * @param selected A boolean.
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return topRow + leftColumn + bottomRow + rightColumn;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Range) {
			Range r = (Range) obj;
			return topRow == r.topRow && leftColumn == r.leftColumn && bottomRow == r.bottomRow
			&& rightColumn == r.rightColumn;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "[" + topRow + ", " + leftColumn + ", " + bottomRow + ", " + rightColumn + "]";
	}
}