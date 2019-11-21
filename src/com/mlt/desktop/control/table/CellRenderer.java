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

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

import com.mlt.desktop.AWT;
import com.mlt.desktop.border.LineBorderSides;
import com.mlt.desktop.layout.Anchor;
import com.mlt.desktop.layout.Constraints;
import com.mlt.desktop.layout.Fill;
import com.mlt.desktop.layout.Insets;
import com.mlt.util.Numbers;

/**
 * Cell renderer to manage range borders and intersection colors, intended to be
 * used with a TableComponent. Note that
 * cell/column/row selection/focus is managed throw the renderer and thought the
 * isSelectd and hasFocus parameters are
 * not used.
 * 
 * @author Miquel Sas
 */
public class CellRenderer extends JPanel implements TableCellRenderer, PropertyChangeListener {

	/** Table component. */
	private TableCmp tableCmp;
	/** Label. */
	private JLabel label;

	/** Unselected foreground color. */
	private Color unselectedForeground;
	/** Unselected background color. */
	private Color unselectedBackground;
	/** Selected foreground color. */
	private Color selectedForeground;
	/** Selected background color. */
	private Color selectedBackground;

	/** Selected border. */
	private LineBorderSides selectedBorder;
	/** Unselected border. */
	private Border unselectedBorder;
	/** Focused border. */
	private LineBorderSides focusedBorder;
	/** Focused background color. */
	private Color focusedBackground;

	/**
	 * Constructor.
	 * 
	 * @param tableCmp The table component.
	 */
	public CellRenderer(TableCmp tableCmp) {
		super();
		this.tableCmp = tableCmp;
		setOpaque(true);
		setLayout(new GridBagLayout());
		tableCmp.getCfg().addPropertyChangeListener(this);
		label = new JLabel();
		label.setOpaque(false);
		setup();
	}

	/**
	 * Do the layout with the given margin.
	 */
	private void setup() {
		TableCfg cfg = tableCmp.getCfg();
		unselectedForeground = cfg.getColor(TableCfg.CELL_UNSELECTED_FOREGROUND);
		unselectedBackground = cfg.getColor(TableCfg.CELL_UNSELECTED_BACKGROUND);
		selectedForeground = cfg.getColor(TableCfg.CELL_SELECTED_FOREGROUND);
		selectedBackground = cfg.getColor(TableCfg.CELL_SELECTED_BACKGROUND);
		selectedBorder = (LineBorderSides) cfg.getBorder(TableCfg.CELL_SELECTED_BORDER);
		unselectedBorder = cfg.getBorder(TableCfg.CELL_UNSELECTED_BORDER);
		focusedBorder = (LineBorderSides) cfg.getBorder(TableCfg.CELL_FOCUSED_BORDER);
		focusedBackground = cfg.getColor(TableCfg.CELL_FOCUSED_BACKGROUND);
		Insets margin = cfg.getMargin(TableCfg.CELL_MARGIN);
		removeAll();
		add(label, AWT.toAWT(new Constraints(Anchor.TOP, Fill.BOTH, 0, 0, margin)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component getTableCellRendererComponent(
		JTable jtable,
		Object value,
		boolean isSelected,
		boolean hasFocus,
		int row,
		int column) {
		return getRendererComponent(value, row, column);
	}

	protected JLabel getLabel() {
		return label;
	}

	/**
	 * Do render.
	 * 
	 * @param value  The value.
	 * @param row    The row.
	 * @param column The column.
	 * @return The render component.
	 */
	private Component getRendererComponent(Object value, int row, int column) {

		/* Set font and text. */
		getLabel().setFont(tableCmp.getFont());
		getLabel().setText((value == null) ? "" : value.toString());

		/* Default border and colors. */
		Border border = unselectedBorder;
		Color foreground = unselectedForeground;
		Color background = unselectedBackground;

		/* If painting for print we are done. */
		if (isPaintingForPrint()) {
			setBorder(border);
			setForeground(foreground);
			setBackground(background);
			return this;
		}

		/*
		 * Border sides.
		 */
		boolean top = false;
		boolean left = false;
		boolean bottom = false;
		boolean right = false;

		/* Cell, column and row focus flags. */
		boolean cellFocused = tableCmp.isCellFocused(row, column);

		/*
		 * Determine if the the cell is contained in any range and, if so, if it is
		 * selected.
		 */
		boolean contained = false;
		boolean selected = false;
		List<Range> selectedRanges = tableCmp.getSelectedRanges();
		for (int i = 0; i < selectedRanges.size(); i++) {
			Range range = selectedRanges.get(i);
			if (range.containsCell(row, column)) {
				if (!top) top = (range.getTopRow() == row);
				if (!left) left = range.getLeftColumn() == column;
				if (!bottom) bottom =
					(range.getBottomRow() == row || range.getBottomRow() == Numbers.MAX_INTEGER);
				if (!right) right =
					(range.getRightColumn() == column
						|| range.getRightColumn() == Numbers.MAX_INTEGER);
				contained = true;
				selected = range.isSelected();
			}
		}

		/* If contained, set appropriate border and colors. */
		if (contained) {
			if (selected) {
				foreground = selectedForeground;
				background = selectedBackground;
			}
			border = selectedBorder.setSides(top, left, bottom, right);
		}

		/*
		 * If cell, column or row are focused, then this state prevails over selection.
		 * Get first the focus state and if
		 * no focus, then get the selection state and do as appropriate.
		 */

		if (cellFocused) {
			border = focusedBorder.setSides(true, true, true, true);
			background = focusedBackground;
		}

		/* Set border and colors. */
		setBorder(border);
		setForeground(foreground);
		setBackground(background);

		return this;
	}

	/**
	 * Called when table component configuration parameters change.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		setup();
	}
}