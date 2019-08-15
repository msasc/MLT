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
import java.awt.Font;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import com.mlt.desktop.border.LineBorderSides;
import com.mlt.desktop.graphic.Stroke;
import com.mlt.desktop.layout.Insets;
import com.mlt.util.Properties;

/**
 * Centralizes all table configuration parameters and fires property changes.
 *
 * @author Miquel Sas
 */
public class TableCfg {

	/** Cell unselected foreground color. */
	public static final String CELL_UNSELECTED_FOREGROUND = "cell.unselected.foreground";
	/** Cell unselected background color. */
	public static final String CELL_UNSELECTED_BACKGROUND = "cell.unselected.background";
	/** Cell selected foreground color. */
	public static final String CELL_SELECTED_FOREGROUND = "cell.selected.foreground";
	/** Cell selected background color. */
	public static final String CELL_SELECTED_BACKGROUND = "cell.selected.background";
	/** Cell selected border. */
	public static final String CELL_SELECTED_BORDER = "cell.selected.border";
	/** Cell focused background. */
	public static final String CELL_FOCUSED_BACKGROUND = "cell.focused.background";
	/** Cell focused border. */
	public static final String CELL_FOCUSED_BORDER = "cell.focused.border";
	/** Cell unselected border. */
	public static final String CELL_UNSELECTED_BORDER = "cell.unselected.border";
	/** Cell margin. */
	public static final String CELL_MARGIN = "cell.margin";

	/** Column header text font. */
	public static final String COLUMN_HEADER_TEXT_FONT = "column.header.text.font";
	/** Column header text margin. */
	public static final String COLUMN_HEADER_TEXT_MARGIN = "column.header.text.margin";
	/** Column header icon margin. */
	public static final String COLUMN_HEADER_ICON_MARGIN = "column.header.icon.margin";
	/** Column header unselected color. */
	public static final String COLUMN_HEADER_UNSELECTED_COLOR = "column.header.unselected.color";
	/** Column header selected color. */
	public static final String COLUMN_HEADER_SELECTED_COLOR = "column.header.selected.color";

	/** Row header margin. */
	public static final String ROW_HEADER_MARGIN = "row.header.margin";
	/** Row header unselected color. */
	public static final String ROW_HEADER_UNSELECTED_COLOR = "row.header.unselected.color";
	/** Row header selected color. */
	public static final String ROW_HEADER_SELECTED_COLOR = "row.header.selected.color";

	/** Property change support. */
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	/** Properties. */
	private Properties properties = new Properties();

	/**
	 * Constructor.
	 */
	public TableCfg() {
		super();

		setColor(CELL_UNSELECTED_FOREGROUND, new Color(51, 51, 51));
		setColor(CELL_UNSELECTED_BACKGROUND, new Color(255, 255, 255));
		setColor(CELL_SELECTED_FOREGROUND, new Color(51, 51, 51));
		setColor(CELL_SELECTED_BACKGROUND, new Color(184, 207, 229));
		setBorder(CELL_SELECTED_BORDER, new LineBorderSides(new Color(64, 64, 64), new Stroke(1.0)));
//		float[] dash = new float[] { 5.0f };
//		int cap = BasicStroke.CAP_SQUARE;
//		int join = BasicStroke.JOIN_MITER;
//		BasicStroke stroke = new BasicStroke(2.0f, cap, join, 1.0f, dash, 0.0f);
		setColor(CELL_FOCUSED_BACKGROUND, new Color(220, 220, 220));
		setBorder(CELL_FOCUSED_BORDER, new LineBorderSides(new Color(128, 128, 128), new Stroke(1.0)));
		setBorder(CELL_UNSELECTED_BORDER, new EmptyBorder(1, 1, 1, 1));
		setMargin(CELL_MARGIN, new Insets(2, 5, 2, 5));

		setFont(COLUMN_HEADER_TEXT_FONT, new Font(Font.DIALOG, Font.BOLD, 12));
		setMargin(COLUMN_HEADER_TEXT_MARGIN, new Insets(5, 5, 5, 5));
		setMargin(COLUMN_HEADER_ICON_MARGIN, new Insets(5, 5, 5, 5));
		setColor(COLUMN_HEADER_UNSELECTED_COLOR, new Color(240, 240, 240));
		setColor(COLUMN_HEADER_SELECTED_COLOR, new Color(220, 220, 220));

		setColor(ROW_HEADER_UNSELECTED_COLOR, new Color(240, 240, 240));
		setColor(ROW_HEADER_SELECTED_COLOR, new Color(220, 220, 220));
		setMargin(ROW_HEADER_MARGIN, new Insets(5, 2, 2, 5));

	}

	/**
	 * Add a property change listener.
	 * 
	 * @param listener The listener.
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	/**
	 * Return the border.
	 * 
	 * @param key The key.
	 * @return The border.
	 */
	public Border getBorder(String key) {
		return (Border) properties.getObject(key);
	}

	/**
	 * Return the color.
	 * 
	 * @param key The key.
	 * @return
	 */
	public Color getColor(String key) {
		return (Color) properties.getObject(key);
	}

	/**
	 * Return the font.
	 * 
	 * @param key The key.
	 * @return The font.
	 */
	public Font getFont(String key) {
		return (Font) properties.getObject(key);
	}

	/**
	 * Return the margin (insets).
	 * 
	 * @param key The key.
	 * @return The insets margin.
	 */
	public Insets getMargin(String key) {
		return (Insets) properties.getObject(key);
	}

	/**
	 * Set the border.
	 * 
	 * @param key    The key.
	 * @param border The border.
	 */
	public final void setBorder(String key, Border border) {
		Object oldValue = getColor(key);
		properties.setObject(key, border);
		pcs.firePropertyChange(key, oldValue, border);
	}

	/**
	 * Set the color.
	 * 
	 * @param key   The key.
	 * @param color The color.
	 */
	public final void setColor(String key, Color color) {
		Object oldValue = getColor(key);
		properties.setObject(key, color);
		pcs.firePropertyChange(key, oldValue, color);
	}

	/**
	 * Set the font.
	 * 
	 * @param key  The key.
	 * @param font The font.
	 */
	public final void setFont(String key, Font font) {
		Object oldValue = getColor(key);
		properties.setObject(key, font);
		pcs.firePropertyChange(key, oldValue, font);
	}

	/**
	 * Set the margin.
	 * 
	 * @param key    The key.
	 * @param margin The margin.
	 */
	public final void setMargin(String key, Insets margin) {
		Object oldValue = getMargin(key);
		properties.setObject(key, margin);
		pcs.firePropertyChange(key, oldValue, margin);
	}

	/**
	 * Remove a property change listener.
	 * 
	 * @param listener The listener.
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}
}
