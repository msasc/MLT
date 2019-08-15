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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.plaf.metal.MetalBorders;
import javax.swing.table.TableCellRenderer;

import com.mlt.desktop.AWT;
import com.mlt.desktop.layout.Alignment;
import com.mlt.desktop.layout.Anchor;
import com.mlt.desktop.layout.Constraints;
import com.mlt.desktop.layout.Fill;
import com.mlt.desktop.layout.Insets;

/**
 * Header cell renderer aimed to be used with a table component.
 * 
 * @author Miquel Sas
 */
public class HeaderCellRenderer extends JPanel implements TableCellRenderer, PropertyChangeListener {

	/** Table component. */
	private TableCmp tableCmp;
	/** Label text. */
	private JLabel labelText;
	/** Label Icon. */
	private JLabel labelIcon;
	/** Unselected background color. */
	private Color unselectedBackground;
	/** Selected background color. */
	private Color selectedBackground;

	/**
	 * Constructor.
	 * 
	 * @param tableCmp The table component.
	 */
	public HeaderCellRenderer(TableCmp tableCmp) {
		super();
		this.tableCmp = tableCmp;

		/* Set this renderer as a listener to table configuration change properties. */
		this.tableCmp.getCfg().addPropertyChangeListener(this);

		setOpaque(true);
		setLayout(new GridBagLayout());

		setTextHorizontalAlignment(Alignment.CENTER);

		setup();
	}

	/**
	 * Layout the components setting the margins.
	 */
	private void setup() {
		setFont(tableCmp.getCfg().getFont(TableCfg.COLUMN_HEADER_TEXT_FONT));
		unselectedBackground = tableCmp.getCfg().getColor(TableCfg.COLUMN_HEADER_UNSELECTED_COLOR);
		selectedBackground = tableCmp.getCfg().getColor(TableCfg.COLUMN_HEADER_SELECTED_COLOR);
		Insets textMargin = tableCmp.getCfg().getMargin(TableCfg.COLUMN_HEADER_TEXT_MARGIN);
		Insets iconMargin = tableCmp.getCfg().getMargin(TableCfg.COLUMN_HEADER_ICON_MARGIN);
		removeAll();
		add(getLabelText(), AWT.toAWT(new Constraints(Anchor.LEFT, Fill.BOTH, 0, 0, textMargin)));
		add(getLabelIcon(), AWT.toAWT(new Constraints(Anchor.RIGHT, Fill.NONE, 1, 0, iconMargin)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object object, boolean isSelected, boolean hasFocus,
	int row, int column) {

		setBorder(new MetalBorders.TableHeaderBorder());
		if (tableCmp.isColumnFocused(column)) {
			setBackground(selectedBackground);
		} else {
			setBackground(unselectedBackground);
		}
		setText(object.toString());
		return this;
	}

	/**
	 * Return the icon label.
	 * 
	 * @return The icon label.
	 */
	private JLabel getLabelIcon() {
		if (labelIcon == null) {
			labelIcon = new JLabel();
		}
		return labelIcon;
	}

	/**
	 * Return the text label.
	 * 
	 * @return The text label.
	 */
	private JLabel getLabelText() {
		if (labelText == null) {
			labelText = new JLabel();
		}
		return labelText;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFont(Font font) {
		getLabelText().setFont(font);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Font getFont() {
		return getLabelText().getFont();
	}

	/**
	 * Set the text horizontal alignment.
	 * 
	 * @param alignment The text horizontal alignment.
	 */
	public void setTextHorizontalAlignment(Alignment alignment) {
		if (!alignment.isHorizontal())
			throw new IllegalArgumentException();
		getLabelText().setHorizontalAlignment(AWT.toAWT(alignment));
	}

	/**
	 * Set the sort icon.
	 * 
	 * @param sortIcon The sort icon.
	 */
	public void setSortIcon(Icon sortIcon) {
		getLabelIcon().setIcon(sortIcon);
	}

	/**
	 * Set the text.
	 * 
	 * @param text The text.
	 */
	public void setText(String text) {
		getLabelText().setText(text);
	}

	/**
	 * Called when table component configuration parameters change.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		setup();
	}
}