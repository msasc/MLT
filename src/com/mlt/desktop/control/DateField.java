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

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.mlt.db.Value;
import com.mlt.desktop.EditContext;
import com.mlt.desktop.EditField;
import com.mlt.desktop.formatter.DateFilter;
import com.mlt.desktop.formatter.TextFieldFilter;

/**
 * A date edit field.
 *
 * @author Miquel Sas
 */
public class DateField extends TextField implements EditField {

	/**
	 * Constructor.
	 */
	public DateField() {
		super();
		DateFilter filter = new DateFilter();
		filter.setDate();
		setFilter(filter);
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
	public DateFilter getFilter() {
		return (DateFilter) super.getFilter();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Value getValue() {
		DateFilter filter = (DateFilter) getFilter();
		LocalDate date = filter.getLocalDateTime().toLocalDate();
		return new Value(date);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFilter(TextFieldFilter filter) {
		if (!(filter instanceof DateFilter)) {
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
			throw new NullPointerException();
		}
		if (!value.isDate()) {
			throw new IllegalArgumentException("Not a date");
		}
		
		Value previousValue = getValue();
		if (value.isNull()) {
			getComponent().setText("");
			if (getEditContext() != null) {
				getEditContext().fireValueActions(this, previousValue, value);
			}
			return;
		}
		
		DateFilter filter = (DateFilter) getFilter();
		LocalDate date = value.getDate();
		
		int year = date.getYear();
		int month = date.getMonthValue();
		int day = date.getDayOfMonth();
		
		LocalDateTime time = LocalDateTime.of(year, month, day, 0, 0);
		filter.setLocalDateTime(time);
		if (getEditContext() != null) {
			getEditContext().fireValueActions(this, previousValue, value);
		}
	}
}
