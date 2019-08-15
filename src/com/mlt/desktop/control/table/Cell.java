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
 * Table cell.
 *
 * @author Miquel Sas
 */
public class Cell {

	/** The row. */
	int row;
	/** The column. */
	int column;

	/**
	 * Constructor.
	 *
	 * @param row    Cell row.
	 * @param column Cell column.
	 */
	public Cell(int row, int column) {
		super();
		this.row = row;
		this.column = column;
	}

	/**
	 * Return the row.
	 *
	 * @return The row index.
	 */
	public int getRow() {
		return row;
	}

	/**
	 * Set the row.
	 *
	 * @param row The row.
	 */
	public void setRow(int row) {
		this.row = row;
	}

	/**
	 * Return the column.
	 *
	 * @return The column index.
	 */
	public int getColumn() {
		return column;
	}

	/**
	 * Set the column.
	 *
	 * @param column The column.
	 */
	public void setColumn(int column) {
		this.column = column;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return row + column;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Cell) {
			Cell ec = (Cell) obj;
			return (ec.row == row && ec.column == column);
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(row);
		b.append(", ");
		b.append(column);
		return b.toString();
	}
}
