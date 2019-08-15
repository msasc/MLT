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

import com.mlt.db.Value;
import com.mlt.desktop.EditContext;
import com.mlt.desktop.EditField;
import com.mlt.desktop.formatter.StringFilter;
import com.mlt.desktop.formatter.TextFieldFilter;
import com.mlt.util.Strings;

/**
 * A string edit field.
 *
 * @author Miquel Sas
 */
public class StringField extends TextField implements EditField {

	/**
	 * Constructor.
	 */
	public StringField() {
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
	public Value getValue() {
		return new Value(getComponent().getText());
	}

	/**
	 * Set the filter.
	 * 
	 * @param maximumLength The maximum length.
	 */
	public void setFilter(int maximumLength) {
		setFilter(Strings.Modifier.NONE, maximumLength);
	}

	/**
	 * Set the filter.
	 * 
	 * @param modifier      The string case modifier.
	 * @param maximumLength The maximum length.
	 */
	public void setFilter(Strings.Modifier modifier, int maximumLength) {
		setFilter(new StringFilter(new StringFilter.MaskerCase(modifier), maximumLength));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFilter(TextFieldFilter filter) {
		if (!(filter instanceof StringFilter)) {
			throw new IllegalArgumentException("Invalid filter type");
		}
		super.setFilter(filter);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setValue(Value value) {
		Value previousValue = getValue();
		getComponent().setText(value.toString());
		if (getEditContext() != null) {
			getEditContext().fireValueActions(this, previousValue, value);
		}
	}

}
