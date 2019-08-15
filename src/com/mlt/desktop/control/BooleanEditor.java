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

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.SwingConstants;

import com.mlt.db.Value;
import com.mlt.desktop.AWT;
import com.mlt.desktop.EditContext;
import com.mlt.desktop.EditField;
import com.mlt.desktop.layout.Insets;
import com.mlt.util.Resources;

/**
 * A boolean editor.
 *
 * @author Miquel Sas
 */
public class BooleanEditor extends Control implements EditField {

	/** Yes. */
	private String yes;
	/** No. */
	private String no;

	/**
	 * Constructor. Default boolean editor is a check box.
	 */
	public BooleanEditor() {
		super();
		setComboBox();
	}

	/**
	 * Return the boolean value.
	 *
	 * @return The boolean value.
	 */
	public boolean getBoolean() {
		if (isCheckBox()) return getCheckBox().isSelected();
		if (isComboBox()) return getComboBox().getSelectedItem().equals(yes);
		throw new IllegalStateException();
	}

	/**
	 * Return the editor as a check box.
	 *
	 * @return The check box.
	 */
	private JCheckBox getCheckBox() {
		return (JCheckBox) getComponent();
	}

	/**
	 * Return the combo box.
	 *
	 * @return The combo box.
	 */
	@SuppressWarnings("unchecked")
	private JComboBox<String> getComboBox() {
		return (JComboBox<String>) getComponent();
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
	 * Return the value.
	 */
	@Override
	public Value getValue() {
		return new Value(getBoolean());
	}

	/**
	 * Check whether this boolean editor is a check box.
	 *
	 * @return A boolean.
	 */
	public boolean isCheckBox() {
		return (getComponent() instanceof JCheckBox);
	}

	/**
	 * Check whether this boolean editor is a combo box.
	 *
	 * @return A boolean.
	 */
	public boolean isComboBox() {
		return (getComponent() instanceof JComboBox);
	}

	/**
	 * Set the boolean value.
	 *
	 * @param b The value.
	 */
	public void setBoolean(boolean b) {
		if (isCheckBox()) getCheckBox().setSelected(b);
		if (isComboBox()) getComboBox().setSelectedItem(b ? yes : no);
	}

	/**
	 * Set this boolean editor a check box.
	 */
	public final void setCheckBox() {
		boolean b = (getComponent() != null ? getBoolean() : false);
		setComponent(new JCheckBox());
		getCheckBox().setIconTextGap(0);
		getCheckBox().setMargin(AWT.toAWT(new Insets(0, -2, 0, 0)));
		getCheckBox().setHorizontalTextPosition(SwingConstants.RIGHT);
		setBoolean(b);
	}

	/**
	 * Set this boolean editor a combo box.
	 */
	public final void setComboBox() {
		boolean b = (getComponent() != null ? getBoolean() : false);
		setComponent(new JComboBox<String>());
		yes = Resources.getText("tokenYes");
		no = Resources.getText("tokenNo");
		getComboBox().addItem(no);
		getComboBox().addItem(yes);
		setBoolean(b);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setValue(Value value) {
		if (value == null) {
			throw new NullPointerException("Value can not be null");
		}
		if (!value.isBoolean()) {
			throw new IllegalArgumentException("Value is not a boolean");
		}
		Value previousValue = getValue();
		setBoolean(value.getBoolean());
		if (getEditContext() != null) {
			getEditContext().fireValueActions(this, previousValue, value);
		}
	}
}
