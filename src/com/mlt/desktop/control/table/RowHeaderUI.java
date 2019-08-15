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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.FocusEvent;

import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import com.mlt.desktop.control.ui.EmptyListUI;
import com.mlt.desktop.event.ListUIHandler;

/**
 * ListUI for the row header list. Removes all unnecessary functionality present in a BasicListUI.
 *
 * @author Miquel Sas
 */
public class RowHeaderUI extends EmptyListUI {

	/**
	 * Event handler.
	 */
	class EventHandler extends ListUIHandler {
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void focusGained(FocusEvent e) {
			tableCmp.requestFocus();
		}
	}

	/** Table component where the row header is installed. */
	private TableCmp tableCmp;
	/** Row header list. */
	private RowHeader rowHeader;
	/** The cell renderer pane, set when installing the UI. */
	private CellRendererPane rendererPane;

	/**
	 * Constructor.
	 * 
	 * @param tableCmp  The table component.
	 * @param rowHeader The row header.
	 */
	RowHeaderUI(TableCmp tableCmp, RowHeader rowHeader) {
		super();
		this.tableCmp = tableCmp;
		this.rowHeader = rowHeader;
	}

	/**
	 * Paint one List cell: compute the relevant state, get the "rubber stamp" cell renderer component, and then use the
	 * {@code CellRendererPane} to paint it. Subclasses may want to override this method rather than {@code paint()}.
	 *
	 * @param g            an instance of {@code Graphics}
	 * @param row          a row
	 * @param rowBounds    a bounding rectangle to render to
	 * @param cellRenderer a list of {@code ListCellRenderer}
	 * @param dataModel    a list model
	 * @param selModel     a selection model
	 * @param leadIndex    a lead index
	 * @see #paint
	 */
	protected void paintCell(Graphics g, int row, Rectangle rowBounds, ListCellRenderer<Object> cellRenderer,
	ListModel<Object> dataModel, ListSelectionModel selModel, int leadIndex) {

		Object value = dataModel.getElementAt(row);
		boolean cellHasFocus = rowHeader.hasFocus() && (row == leadIndex);
		boolean isSelected = selModel.isSelectedIndex(row);

		Component renderer = cellRenderer.getListCellRendererComponent(rowHeader, value, row, isSelected, cellHasFocus);

		int cx = rowBounds.x;
		int cy = rowBounds.y;
		int cw = rowBounds.width;
		int ch = rowBounds.height;

		rendererPane.paintComponent(g, renderer, rowHeader, cx, cy, cw, ch, true);
	}

	/**
	 * Paint the rows that intersect the Graphics objects clipRect. This method calls paintCell as necessary. Subclasses
	 * may want to override these methods.
	 *
	 * @see #paintCell
	 */
	@Override
	public void paint(Graphics g, JComponent c) {
		Shape clip = g.getClip();
		paintImpl(g, c);
		g.setClip(clip);

	}

	private void paintImpl(Graphics g, JComponent c) {

		ListCellRenderer<Object> renderer = rowHeader.getCellRenderer();
		ListModel<Object> dataModel = rowHeader.getModel();
		ListSelectionModel selModel = rowHeader.getSelectionModel();
		int size;

		if ((renderer == null) || (size = dataModel.getSize()) == 0) {
			return;
		}

		// Determine how many columns we need to paint
		Rectangle paintBounds = g.getClipBounds();

		int maxY = paintBounds.y + paintBounds.height;
		int leadIndex = rowHeader.getLeadSelectionIndex();

		int rowIncrement = 1;

		// And then how many rows in this column
		int row = convertLocationToRow(paintBounds.y);
		int rowCount = rowHeader.getModel().getSize();
		int index = row;
		Rectangle rowBounds = getCellBounds(rowHeader, index, index);

		if (rowBounds == null) {
			// Not valid, bail!
			return;
		}
		while (row < rowCount && rowBounds.y < maxY && index < size) {
			rowBounds.height = tableCmp.getRowHeight(row);
			g.setClip(rowBounds.x, rowBounds.y, rowBounds.width, rowBounds.height);
			g.clipRect(paintBounds.x, paintBounds.y, paintBounds.width, paintBounds.height);
			paintCell(g, index, rowBounds, renderer, dataModel, selModel, leadIndex);
			rowBounds.y += rowBounds.height;
			index += rowIncrement;
			row++;
		}
		// Empty out the renderer pane, allowing renderers to be gc'ed.
		rendererPane.removeAll();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Dimension getPreferredSize(JComponent c) {

		int lastRow = rowHeader.getModel().getSize() - 1;
		if (lastRow < 0) {
			return new Dimension(0, 0);
		}

		int width = rowHeader.getFixedCellWidth();
		int height = 0;

		Rectangle bounds = getCellBounds(rowHeader, lastRow);

		if (bounds != null) {
			height = bounds.y + bounds.height;
		} else {
			height = 0;
		}
		return new Dimension(width, height);
	}

	/**
	 * Initializes <code>this.list</code> by calling <code>installDefaults()</code>, <code>installListeners()</code>,
	 * and <code>installKeyboardActions()</code> in order.
	 *
	 * @see #installDefaults
	 * @see #installListeners
	 * @see #installKeyboardActions
	 */
	@Override
	public void installUI(JComponent c) {
		rendererPane = new CellRendererPane();
		rowHeader.add(rendererPane);

		/* Install listeners. */
		EventHandler handler = new EventHandler();
		rowHeader.addFocusListener(handler);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Rectangle getCellBounds(JList list, int index1, int index2) {

		int minIndex = Math.min(index1, index2);
		int maxIndex = Math.max(index1, index2);

		Rectangle minBounds = getCellBounds(list, minIndex);

		if (minIndex == maxIndex) {
			return minBounds;
		}
		Rectangle maxBounds = getCellBounds(list, maxIndex);

		if (maxBounds != null) {
			if (minBounds.x != maxBounds.x) {
				// Different columns
				minBounds.y = 0;
				minBounds.height = rowHeader.getHeight();
			}
			minBounds.add(maxBounds);
		}
		return minBounds;
	}

	/**
	 * Gets the bounds of the specified model index, returning the resulting bounds, or null if <code>index</code> is
	 * not valid.
	 */
	private Rectangle getCellBounds(JList<?> list, int row) {

		int x = 0;
		int y = 0;
		int width = rowHeader.getFixedCellWidth();
		int height = (tableCmp.isFixedRowHeight() ? tableCmp.getRowHeight() : tableCmp.getRowHeight(row));
		if (tableCmp.isFixedRowHeight()) {
			y += (tableCmp.getRowHeight() * row);
		} else {
			for (int i = 0; i < row; i++) {
				y += tableCmp.getRowHeight(i);
			}
		}
		return new Rectangle(x, y, width, height);
	}

	private int convertLocationToRow(int y) {
		int size = rowHeader.getModel().getSize();

		if (size <= 0) {
			return -1;
		}

		if (tableCmp.isFixedRowHeight()) {
			int height = tableCmp.getRowHeight();
			int row = (height == 0) ? 0 : (y / height);
			return (row < 0 ? 0 : (row >= size ? size - 1 : row));
		} else {
			int yscan = 0;
			int row = 0;
			if (y < yscan) {
				return 0;
			}
			int i;
			for (i = 0; i < size; i++) {
				int height = tableCmp.getRowHeight(i);
				if ((y >= yscan) && (y < yscan + height)) {
					return row;
				}
				yscan += height;
				row += 1;
			}
			return i - 1;
		}

	}

	/**
	 * Calculate the fixed cell width.
	 */
	void calculateCellWidth() {
		int lastRow = tableCmp.getRowCount() - 1;
		Object value = lastRow + 1;
		ListCellRenderer<Object> renderer = rowHeader.getCellRenderer();
		Component c = renderer.getListCellRendererComponent(rowHeader, value, lastRow, false, false);
		Dimension cellSize = c.getPreferredSize();
		rowHeader.setFixedCellWidth(cellSize.width);
	}
}
