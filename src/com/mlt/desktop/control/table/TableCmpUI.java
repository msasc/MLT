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
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.CellEditor;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.basic.BasicTableUI;

import com.mlt.desktop.event.Mask;
import com.mlt.desktop.event.MouseHandler;

/**
 * Table UI. Plugs its own key and mouse listeners.
 * 
 * @author Miquel Sas
 */
class TableCmpUI extends BasicTableUI {

	/**
	 * Key handler.
	 */
	private class KeyListener extends KeyHandler {

		@Override
		public void keyPressed(KeyEvent e) {

			/* Table component. */
			TableCmp tableCmp = (TableCmp) table;

			/* Check key code is managed. */
			int keyCode = e.getKeyCode();

			/* Row and column helper values. */
			int lastRow = tableCmp.getFocusCell().getRow();
			int lastColumn = tableCmp.getFocusCell().getColumn();
			int maxRow = tableCmp.getRowCount() - 1;
			int maxColumn = tableCmp.getColumnCount() - 1;

			/*
			 * Ctrl, toggle and extend (shift) key. The toggle (ctrl) key is used for ctrl-page-up/down and
			 * ctrl-home/end, thus it can not be used as toggle.
			 */
			boolean ctrl = Mask.check(e, Mask.CTRL);
			boolean toggle = false;
			boolean extend = Mask.check(e, Mask.SHIFT);

			/* Calculate the next row and column depending on the key pressed. */
			int row = 0;
			int column = 0;
			if (keyCode == KeyEvent.VK_UP) {
				row = Math.max(lastRow - 1, 0);
				column = lastColumn;
			} else if (keyCode == KeyEvent.VK_DOWN) {
				row = Math.min(lastRow + 1, maxRow);
				column = lastColumn;
			} else if (keyCode == KeyEvent.VK_LEFT) {
				row = lastRow;
				column = Math.max(lastColumn - 1, 0);
			} else if (keyCode == KeyEvent.VK_RIGHT) {
				row = lastRow;
				column = Math.min(lastColumn + 1, maxColumn);
			} else if (keyCode == KeyEvent.VK_PAGE_UP) {
				row = (ctrl ? 0 : Math.max(lastRow - tableCmp.getPageSize(), 0));
				column = lastColumn;
			} else if (keyCode == KeyEvent.VK_PAGE_DOWN) {
				row = (ctrl ? maxRow : Math.min(lastRow + tableCmp.getPageSize(), maxRow));
				column = lastColumn;
			} else if (keyCode == KeyEvent.VK_HOME) {
				row = (ctrl ? 0 : lastRow);
				column = 0;
			} else if (e.getKeyCode() == KeyEvent.VK_END) {
				row = (ctrl ? maxRow : lastRow);
				column = maxColumn;
			} else {
				return;
			}

			tableCmp.moveSelection(row, column, toggle, extend);
			e.consume();
		}
	}

	/**
	 * Mouse handler.
	 */
	private class MouseListener extends MouseHandler implements MouseInputListener {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void mouseDragged(MouseEvent e) {

			/* Table component. */
			TableCmp tableCmp = (TableCmp) table;

			/* Check ignore the event. */
			if (!tableCmp.isEnabled() || e.isConsumed())
				return;

			/* Get the row and column at the mouse point. */
			Point p = e.getPoint();
			int row = tableCmp.rowAtPoint(p);
			int column = tableCmp.columnAtPoint(p);

			/* The auto scroller can generate drag events outside the table's range. */
			if ((column == -1) || (row == -1)) {
				return;
			}

			/*
			 * For a dragged mouse, use the control or shift mask as extend. Toggle is always false and extend always
			 * true.
			 */
			tableCmp.moveSelection(row, column, false, true);
			e.consume();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void mousePressed(MouseEvent e) {

			/* Table component. */
			TableCmp tableCmp = (TableCmp) table;

			/* Check ignore the event. */
			if (!tableCmp.isEnabled() || e.isConsumed())
				return;

			/* If editing and can't stop editing, re-composite focus. */
			if (tableCmp.isEditing() && !tableCmp.getCellEditor().stopCellEditing()) {
				Component editor = tableCmp.getEditorComponent();
				if (editor != null && !editor.hasFocus() && editor.isFocusable()) {
					editor.requestFocus();
				}
				return;
			}

			/* Mouse point, row and column. */
			Point p = e.getPoint();
			int row = tableCmp.rowAtPoint(p);
			int column = tableCmp.columnAtPoint(p);

			/* Adjust focus. */
			if ((!tableCmp.hasFocus()) && (tableCmp.isRequestFocusEnabled())) {
				tableCmp.requestFocus();
			}
			CellEditor editor = tableCmp.getCellEditor();
			if (editor == null || editor.shouldSelectCell(e)) {
				/*
				 * For mouse pressed, only take into account toggle, extends should always be false.
				 */
				if (Mask.check(e, Mask.BUTTON1)) {
					boolean toggle = Mask.check(e, Mask.CTRL);
					boolean extend = (toggle ? false : Mask.check(e, Mask.SHIFT));
					tableCmp.moveSelection(row, column, toggle, extend);
				}
			}

			e.consume();
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param tableComponent TODO
	 */
	TableCmpUI() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected KeyListener createKeyListener() {
		return new KeyListener();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected MouseInputListener createMouseInputListener() {
		return new MouseListener();
	}

}