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
package com.mlt.db;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Locale;

import com.mlt.util.Resources;

/**
 * Default field validator for type, maximum, minimum and possible values,
 * required and nullable.
 *
 * @author Miquel Sas
 */
public class DefaultFieldValidator extends Validator<Value> {

	/** The field. */
	private Field field;
	/** The locale for messages. */
	private Locale locale;

	/**
	 * Constructor.
	 *
	 * @param field The field to validate.
	 */
	public DefaultFieldValidator(Field field) {
		this(Locale.getDefault(), field);
	}

	/**
	 * Constructor.
	 *
	 * @param locale The working locale for string literals.
	 * @param field  The field to validate.
	 */
	public DefaultFieldValidator(Locale locale, Field field) {
		super();
		this.locale = locale;
		this.field = field;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean validate(Value value, Object operation) {

		// Strict type
		if (!value.getType().equals(field.getType())) {
			return false;
		}

		// Maximum value
		if (field.getMaximumValue() != null) {
			if (value.compareTo(field.getMaximumValue()) > 0) {
				return false;
			}
		}

		// Minimum value
		if (field.getMinimumValue() != null) {
			if (value.compareTo(field.getMinimumValue()) < 0) {
				return false;
			}
		}

		// Possible values
		if (!field.getPossibleValues().isEmpty()) {
			if (!value.in(new ArrayList<>(field.getPossibleValues()))) {
				return false;
			}
		}

		// Non empty required
		if (field.isRequired() && value.isEmpty()) {
			return false;
		}
		// Nullable
		return !(!field.isNullable() && value.isNull());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getMessage(Value value, Object operation) {

		// Strict type
		if (!value.getType().equals(field.getType())) {
			return MessageFormat.format(Resources.getText("fieldValidType", locale),
				value.getType(), field.getType());
		}

		// Maximum value
		if (field.getMaximumValue() != null) {
			if (value.compareTo(field.getMaximumValue()) > 0) {
				return MessageFormat.format(Resources.getText("fieldValidMax", locale), value,
					field.getMaximumValue());
			}
		}

		// Minimum value
		if (field.getMinimumValue() != null) {
			if (value.compareTo(field.getMinimumValue()) < 0) {
				return MessageFormat.format(Resources.getText("fieldValidMin", locale), value,
					field.getMinimumValue());
			}
		}

		// Possible values
		if (!field.getPossibleValues().isEmpty()) {
			if (!value.in(new ArrayList<>(field.getPossibleValues()))) {
				return MessageFormat.format(Resources.getText("fieldValidPossible", locale), value);
			}
		}

		// Non empty required
		if (field.isRequired() && value.isEmpty()) {
			return Resources.getText("fieldValidEmpy", locale);
		}

		// Nullable
		if (!field.isNullable() && value.isNull()) {
			return Resources.getText("fieldValidEmpy", locale);
		}

		return null;
	}

}
