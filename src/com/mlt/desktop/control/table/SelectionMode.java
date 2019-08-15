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
 * Enumerate selection modes.
 * 
 * @author Miquel Sas
 */
public enum SelectionMode {

	/*
	 * No selection.
	 */

	NONE,

	/*
	 * Cell selection modes.
	 */

	/** Single cell selection. */
	SINGLE_CELL_SELECTION,
	/** Single cell interval. */
	SINGLE_CELL_INTERVAL,
	/** Multiple intervals of cell. */
	MULTIPLE_CELL_INTERVAL,

	/*
	 * Row selection modes.
	 */

	/** Single row selection. */
	SINGLE_ROW_SELECTION,
	/** Single row interval. */
	SINGLE_ROW_INTERVAL,
	/** Multiple intervals of rows. */
	MULTIPLE_ROW_INTERVAL,

	/*
	 * Column selection modes.
	 */

	/** Select only one column. */
	SINGLE_COLUMN_SELECTION,
	/** Select an interval of columns. */
	SINGLE_COLUMN_INTERVAL,
	/** Select multiple intervals of columns. */
	MULTIPLE_COLUMN_INTERVAL;

	/**
	 * Check whether this selection mode is none.
	 * 
	 * @return A boolean.
	 */
	public boolean isNone() {
		return this == NONE;
	}

	/**
	 * Check whether this selection mode is cell mode.
	 * 
	 * @return A boolean.
	 */
	public boolean isCell() {
		switch (this) {
		case SINGLE_CELL_SELECTION:
		case SINGLE_CELL_INTERVAL:
		case MULTIPLE_CELL_INTERVAL:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Check whether this selection mode is column mode.
	 * 
	 * @return A boolean.
	 */
	public boolean isColumn() {
		switch (this) {
		case SINGLE_COLUMN_SELECTION:
		case SINGLE_COLUMN_INTERVAL:
		case MULTIPLE_COLUMN_INTERVAL:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Check whether this selection mode is row mode.
	 * 
	 * @return A boolean.
	 */
	public boolean isRow() {
		switch (this) {
		case SINGLE_ROW_SELECTION:
		case SINGLE_ROW_INTERVAL:
		case MULTIPLE_ROW_INTERVAL:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Check whether this mode is single selection.
	 * 
	 * @return A boolean
	 */
	public boolean isSingleSelection() {
		switch (this) {
		case SINGLE_CELL_SELECTION:
		case SINGLE_COLUMN_SELECTION:
		case SINGLE_ROW_SELECTION:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Check whether this mode is single interval.
	 * 
	 * @return A boolean
	 */
	public boolean isSingleInterval() {
		switch (this) {
		case SINGLE_CELL_INTERVAL:
		case SINGLE_COLUMN_INTERVAL:
		case SINGLE_ROW_INTERVAL:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Check whether this mode is multiple interval.
	 * 
	 * @return A boolean
	 */
	public boolean isMultipleInterval() {
		switch (this) {
		case MULTIPLE_CELL_INTERVAL:
		case MULTIPLE_COLUMN_INTERVAL:
		case MULTIPLE_ROW_INTERVAL:
			return true;
		default:
			return false;
		}
	}
}