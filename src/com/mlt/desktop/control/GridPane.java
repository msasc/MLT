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

import java.awt.GridLayout;
import java.util.List;

/**
 * A pane with grid layout.
 *
 * @author Miquel Sas
 */
public class GridPane extends Pane {

	/**
	 * Constructor.
	 * 
	 * @param rows    Number of rows.
	 * @param columns Number of columns
	 */
	public GridPane(int rows, int columns) {
		super();
		setLayout(new GridLayout(rows, columns));
	}

	/**
	 * Constructor.
	 * 
	 * @param rows    Number of rows.
	 * @param columns Number of columns
	 * @param hgap    Horizontal gap.
	 * @param vgap    Vertical gap.
	 */
	public GridPane(int rows, int columns, int hgap, int vgap) {
		super();
		setLayout(new GridLayout(rows, columns, hgap, vgap));
	}

	/**
	 * Override to return the proper layout.
	 */
	@Override
	public GridLayout getLayout() {
		return (GridLayout) super.getLayout();
	}

	/**
	 * Remove all components.
	 * 
	 * @see java.awt.Container#removeAll()
	 */
	public void removeAll() {
		getComponent().removeAll();
	}

	/**
	 * Return the number of columns.
	 * 
	 * @return The number of columns.
	 */
	public int getColumns() {
		return getLayout().getColumns();
	}

	/**
	 * Return the control at the given row and column.
	 * 
	 * @param row    The row.
	 * @param column The column.
	 * @return The control.
	 */
	public Control getControl(int row, int column) {
		if (row >= getRows()) throw new IllegalArgumentException("Invalid row: " + row);
		if (column >= getColumns()) throw new IllegalArgumentException("Invalid column: " + column);
		int index = row * getColumns() + column;
		return getControl(index);
	}

	/**
	 * Return the horizontal gap.
	 * 
	 * @return The horizontal gap.
	 */
	public int getHGap() {
		return getLayout().getHgap();
	}

	/**
	 * Return the number of rows.
	 * 
	 * @return The number of rows.
	 */
	public int getRows() {
		return getLayout().getRows();
	}

	/**
	 * Return the vertical gap.
	 * 
	 * @return The vertical gap.
	 */
	public int getVGap() {
		return getLayout().getVgap();
	}

	/**
	 * Set the number of columns.
	 * 
	 * @param columns The number of columns.
	 */
	public void setColumns(int columns) {
		getLayout().setColumns(columns);
	}

	/**
	 * Set the control at the given row and column.
	 * 
	 * @param row     The row.
	 * @param column  The column.
	 * @param control The control.
	 */
	public void setControl(int row, int column, Control control) {
		if (row > getRows()) {
			throw new IllegalArgumentException("Invalid row: " + row);
		}
		if (column > getColumns()) {
			throw new IllegalArgumentException("Invalid column: " + column);
		}

		/* Current controls. */
		List<Control> controls = getControls();

		/* Remove all. */
		removeAll();

		/* Set the argument control in the proper position. */
		int index = row * getColumns() + column;
		if (index < controls.size()) {
			controls.set(index, control);
		} else {
			for (int i = index - controls.size(); i > 0; i--) {
				controls.add(new Label());
			}
			controls.add(control);
		}

		/* Add controls to the pane. */
		controls.forEach(c -> add(c));
	}

	/**
	 * Set the horizontal gap.
	 * 
	 * @param hgap The horizontal gap.
	 */
	public void setHGap(int hgap) {
		getLayout().setHgap(hgap);
	}

	/**
	 * Set the number of rows.
	 * 
	 * @param rows The number of rows.
	 */
	public void setRows(int rows) {
		getLayout().setRows(rows);
	}

	/**
	 * Set the vertical gap.
	 * 
	 * @param vgap The vertical gap.
	 */
	public void setVGap(int vgap) {
		getLayout().setVgap(vgap);
	}
}
