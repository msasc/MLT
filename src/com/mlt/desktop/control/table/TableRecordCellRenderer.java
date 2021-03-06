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

import java.awt.Component;
import java.awt.Font;
import java.text.ParseException;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import com.mlt.db.Field;
import com.mlt.db.Value;
import com.mlt.desktop.AWT;
import com.mlt.desktop.icon.Icons;
import com.mlt.desktop.layout.Alignment;
import com.mlt.util.Formats;
import com.mlt.util.Resources;

/**
 * Table cell renderer for a table record.
 *
 * @author Miquel Sas
 */
public class TableRecordCellRenderer extends CellRenderer {

	/** Icon checked for boolean values. */
	private ImageIcon iconChecked;
	/** Icon unchecked for boolean values. */
	private ImageIcon iconUnchecked;
	/** The underlying field. */
	private Field field;

	/**
	 * Constructor.
	 *
	 * @param tableCmp The table component.
	 * @param field    The field.
	 */
	public TableRecordCellRenderer(TableCmp tableCmp, Field field) {
		super(tableCmp);
		this.field = field;
		Alignment alignment = Alignment.valueOf(field.getHorizontalAlignment());
		getLabel().setHorizontalAlignment(AWT.toAWT(alignment));
		this.iconChecked = Icons.getIcon(Icons.APP_16x16_CHECKED);
		this.iconUnchecked = Icons.getIcon(Icons.APP_16x16_UNCHECKED);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component getTableCellRendererComponent(
		JTable table,
		Object object,
		boolean isSelected,
		boolean hasFocus,
		int row,
		int column) {
		super.getTableCellRendererComponent(table, object, isSelected, hasFocus, row, column);

		/* Set the font if required. */
		if (field.getFontName() != null) {
			String name = field.getFontName();
			int style = field.getFontStyle();
			int size = field.getFontSize();
			getLabel().setFont(new Font(name, style, size));
		}

		/* Configure this table cell renderer (label) and return it. */
		Value value = (Value) object;
		if (value == null || value.isNull()) {
			getLabel().setText("");
			getLabel().setIcon(null);
			return this;
		}

		/* Password fields, fixed length of 10 asterisks. */
		if (field.isPassword()) {
			getLabel().setText("**********");
			return this;
		}

		/* Possible values field. */
		if (field.isPossibleValues()) {
			getLabel().setText(field.getPossibleValueLabel(value));
			return this;
		}

		/* Optional string converter. */
		if (field.getStringConverter() != null) {
			try {
				getLabel().setText(field.getStringConverter().valueToString(value));
			} catch (ParseException ignore) {}
			return this;
		}
		
		/* Icon field. */
		if (field.isIcon()) {
			getLabel().setText("");
			getLabel().setIcon(value.getIcon());
			getLabel().setHorizontalAlignment(SwingConstants.CENTER);
			getLabel().setVerticalAlignment(SwingConstants.CENTER);
			return this;
		}

		/* Boolean field. */
		if (field.isBoolean()) {
			if (field.isEditBooleanInCheckBox()) {
				getLabel().setText("");
				if (value.getBoolean()) {
					if (field.getIconChecked() == null) {
						getLabel().setIcon(iconChecked);
					} else {
						getLabel().setIcon(field.getIconChecked());
					}
				} else {
					if (field.getIconUnchecked() == null) {
						getLabel().setIcon(iconUnchecked);
					} else {
						getLabel().setIcon(field.getIconUnchecked());
					}
				}
				getLabel().setHorizontalAlignment(SwingConstants.CENTER);
				getLabel().setVerticalAlignment(SwingConstants.CENTER);
			} else {
				if (value.getBoolean()) {
					getLabel().setText(Resources.getText("tokenYes"));
				} else {
					getLabel().setText(null);
				}
			}
			return this;
		}

		/* Number with display decimals set. */
		if (field.isNumber()) {
			int decimals = field.getDisplayDecimals();
			if (decimals >= 0) {
				double d = value.getNumber().doubleValue();
				String text = Formats.fromDouble(d, decimals, Locale.getDefault());
				getLabel().setText(text);
				return this;
			}
		}

		/* Number, date-time or string */
		if (field.isNumber() ||
			field.isDate() ||
			field.isTime() ||
			field.isDateTime() ||
			field.isString()) {
			getLabel().setText(Value.fromValue(value, Locale.getDefault()));
			return this;
		}

		/* Rest of types is pending to develop. */
		return this;
	}
}
