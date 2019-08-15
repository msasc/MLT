/*
 * Copyright (C) 2018 Miquel Sas
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.mlt.desktop.control;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.Locale;

import com.mlt.db.Field;
import com.mlt.db.Value;
import com.mlt.desktop.EditContext;
import com.mlt.desktop.EditField;
import com.mlt.desktop.formatter.NumberFilter;
import com.mlt.desktop.formatter.NumberFilter.Type;
import com.mlt.desktop.formatter.TextFieldFilter;
import com.mlt.desktop.layout.Alignment;
import com.mlt.util.Formats;

/**
 * Number text field that implements the field control interface.
 *
 * @author Miquel Sas
 */
public class NumberField extends TextField implements EditField {

	/**
	 * Constructor.
	 */
	public NumberField() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Control getControl() {
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EditContext getEditContext() {
		return (EditContext) getProperty(EditContext.EDIT_CONTEXT);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NumberFilter getFilter() {
		return (NumberFilter) super.getFilter();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Value getValue() {
		if (getFilter().getType() == NumberFilter.Type.PLAIN) {
			return parsePlain();
		}
		if (getFilter().getType() == NumberFilter.Type.LOCALE) {
			return parseLocale();
		}
		return null;
	}

	/**
	 * Return the value parsing a localized text number.
	 * 
	 * @return The value.
	 */
	private Value parseLocale() {
		String text = getComponent().getText();
		Field field = getEditContext().getField();
		Locale locale = Locale.getDefault();
		try {
			if (field.isDecimal()) {
				int decimals = field.getDecimals();
				BigDecimal number =
					Formats.toBigDecimal(text, locale).setScale(decimals, RoundingMode.HALF_UP);
				return new Value(number);
			}
			if (field.isDouble()) {
				Double number = Formats.toDouble(text, locale);
				return new Value(number);
			}
			if (field.isInteger()) {
				Integer number = Formats.toInteger(text, locale);
				return new Value(number);
			}
			if (field.isLong()) {
				Long number = Formats.toLong(text, locale);
				return new Value(number);
			}
		} catch (ParseException exc) {
			exc.printStackTrace();
		}
		throw new IllegalStateException();
	}

	/**
	 * Return the value parsing a plain text number.
	 * 
	 * @return The value.
	 */
	private Value parsePlain() {
		String text = getComponent().getText();
		Field field = getEditContext().getField();
		if (field.isDecimal()) {
			int decimals = field.getDecimals();
			BigDecimal number = new BigDecimal(text).setScale(decimals, RoundingMode.HALF_UP);
			return new Value(number);
		}
		if (field.isDouble()) {
			Double number = new Double(text);
			return new Value(number);
		}
		if (field.isInteger()) {
			Integer number = new Integer(text);
			return new Value(number);
		}
		if (field.isLong()) {
			Long number = new Long(text);
			return new Value(number);
		}
		throw new IllegalStateException();
	}

	/**
	 * Set the type of number.
	 * 
	 * @param type          The type.
	 * @param integerDigits The number of integer digits.
	 * @param decimalDigits The number of decimal digits.
	 */
	public void setFilter(NumberFilter.Type type, int integerDigits, int decimalDigits) {
		setHorizontalAlignment(Alignment.RIGHT);
		NumberFilter filter = new NumberFilter(type, integerDigits, decimalDigits);
		setFilter(filter);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFilter(TextFieldFilter filter) {
		if (!(filter instanceof NumberFilter)) {
			throw new IllegalArgumentException("Invalid filter type");
		}
		super.setFilter(filter);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setValue(Value value) {
		if (value == null) {
			throw new NullPointerException("Value can not be null");
		}
		if (!value.isNumber()) {
			throw new IllegalArgumentException("Value is not a number");
		}
		Value previousValue = getValue();
		String text = null;
		if (getFilter().getType() == Type.LOCALE) {
			text = Value.fromValue(value, Locale.getDefault());
		} else {
			text = value.toString();
		}
		if (text == null) {
			text = "0";
		}
		setText(text);
		if (getEditContext() != null) {
			getEditContext().fireValueActions(this, previousValue, value);
		}
	}
}
