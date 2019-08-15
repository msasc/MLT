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

import java.util.Locale;

import javax.swing.JComboBox;

import com.mlt.db.Value;
import com.mlt.desktop.EditContext;
import com.mlt.desktop.EditField;

/**
 * A combo box control that implements the field control interface.
 *
 * @author Miquel Sas
 */
public class ComboBox extends Control implements EditField {

	/**
	 * Items of the combo box.
	 */
	class Item extends Value {

		/**
		 * Constructor.
		 * 
		 * @param v The value.
		 */
		Item(Value v) {
			super(v);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			if (getLabel() != null) {
				return getLabel();
			}
			return Value.fromValue(this, Locale.getDefault());
		}
	}

	/**
	 * Constructor.
	 */
	public ComboBox() {
		super();
		setComponent(new JComboBox<Item>());
	}

	/**
	 * Add a value to the list of values.
	 * 
	 * @param value The value to add.
	 */
	public void addValue(Value value) {
		getComponent().addItem(new Item(value));
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public JComboBox<Item> getComponent() {
		return (JComboBox<Item>) super.getComponent();
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
	public Control getControl() {
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Value getValue() {
		Item item = (Item) getComponent().getSelectedItem();
		Value value;
		if (item == null) {
			value = getEditContext().getField().getDefaultValue();
		} else {
			value = new Value(item);
		}
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setValue(Value value) {
		if (value == null) {
			throw new NullPointerException("Value can not be null");
		}
		Value previousValue = getValue();
		boolean selectedIndexSet = false;
		for (int i = 0; i < getComponent().getItemCount(); i++) {
			if (getComponent().getItemAt(i).equals(value)) {
				getComponent().setSelectedIndex(i);
				selectedIndexSet = true;
				break;
			}
		}
		if (getComponent().getItemCount() > 0 && !selectedIndexSet) {
			getComponent().setSelectedIndex(0);
		}
		if (getEditContext() != null) {
			getEditContext().fireValueActions(this, previousValue, value);
		}
	}

}
