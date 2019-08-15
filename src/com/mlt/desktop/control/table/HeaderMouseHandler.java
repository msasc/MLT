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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import com.mlt.desktop.event.MouseHandler;

/**
 * The mouse handler to handle mouse events on the header.
 */
public class HeaderMouseHandler extends MouseHandler {

	/**
	 * 
	 */
	private final TableCmp tableCmp;
	/** Margin to accept the point to be in the resize area of the column header. */
	private int resizeMargin = 4;

	/**
	 * Constructor.
	 * 
	 * @param tableCmp TODO
	 */
	public HeaderMouseHandler(TableCmp tableCmp) {
		super();
		this.tableCmp = tableCmp;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseClicked(MouseEvent e) {

		/* Button 1 actions. */
		if (e.getButton() == MouseEvent.BUTTON1) {

			/* When the point is in the resize area adjust the column width. */
			if (isResize(e.getPoint())) {
				if (e.getClickCount() == 2) {
					int column = getColumnToResize(e.getPoint());
					if (column >= 0) {
						this.tableCmp.adjustColumnWidth(column);
					}
				}
				return;
			}

			/* Not in the resize area, check sorting. */
			if (this.tableCmp.sortingEnabled) {
				e.consume();
				if (e.getClickCount() == 2) {
					return;
				}

				boolean controlMask = ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0);
				int column = this.tableCmp.getTableHeader().columnAtPoint(e.getPoint());
				this.tableCmp.sort(column, controlMask);
			}
		}
	}

	/**
	 * Returns the number of the column to resize given the point of the mouse, by checking if the point is in the limit
	 * of two subsequent columns.
	 * 
	 * @param p The mouse point.
	 * @return The column number to resize or -1 if none applicable.
	 */
	private int getColumnToResize(Point p) {
		int column = this.tableCmp.getTableHeader().columnAtPoint(p);
		if (column == -1) {
			return -1;
		} else if (column == 0) {
			if (isResize(p, column)) {
				return column;
			}
		} else {
			if (isResize(p, column - 1)) {
				return column - 1;
			} else if (isResize(p, column)) {
				return column;
			}
		}
		return -1;
	}

	/**
	 * Returns a boolean indicating if the point is in the resize area.
	 * 
	 * @param p The mouse event point.
	 * @return A boolean indicating if the point is in the resize area.
	 */
	private boolean isResize(Point p) {
		int column = this.tableCmp.getTableHeader().columnAtPoint(p);
		Rectangle r = this.tableCmp.getTableHeader().getHeaderRect(column);
		return p.x < r.x + resizeMargin || p.x > (r.x + r.width) - resizeMargin;
	}

	/**
	 * Returns a boolean indicating if the point is in the resize area in the right of the column.
	 * 
	 * @param p      The point.
	 * @param column The column number.
	 * @return A boolean.
	 */
	private boolean isResize(Point p, int column) {
		Rectangle r = this.tableCmp.getTableHeader().getHeaderRect(column);
		int xRight = r.x + r.width;
		int x = p.x;
		return x > xRight - resizeMargin && x < xRight + resizeMargin;
	}
}