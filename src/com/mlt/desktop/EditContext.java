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

package com.mlt.desktop;

import java.awt.Font;
import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.List;

import com.mlt.db.Field;
import com.mlt.db.Value;
import com.mlt.desktop.action.Action;
import com.mlt.desktop.control.BooleanEditor;
import com.mlt.desktop.control.ComboBox;
import com.mlt.desktop.control.Control;
import com.mlt.desktop.control.DateField;
import com.mlt.desktop.control.Label;
import com.mlt.desktop.control.NumberField;
import com.mlt.desktop.control.StringField;
import com.mlt.desktop.formatter.NumberFilter;
import com.mlt.desktop.formatter.StringFilter;
import com.mlt.desktop.layout.Dimension;
import com.mlt.desktop.layout.Fill;
import com.mlt.util.Numbers;

/**
 * A context that contains the necessary items to edit a field.
 *
 * @author Miquel Sas
 */
public class EditContext {

	/** Key to get/set the current value. */
	public static final String CURRENT_VALUE = "CURRENT_VALUE";
	/** Key to get/set the edit context. */
	public static final String EDIT_CONTEXT = "EDIT_CONTEXT";
	/** Key to get/set the previous value. */
	public static final String PREVIOUS_VALUE = "PREVIOUS_VALUE";

	/**
	 * Helper to get the current value from an action performing the action value
	 * functionality.
	 * 
	 * @param action The action.
	 * @return The current value.
	 */
	public static Value getCurrentValue(Action action) {
		return (Value) action.getProperty(CURRENT_VALUE);
	}

	/**
	 * Helper to get the edit context from an action performing the action value
	 * functionality.
	 * 
	 * @param action The action.
	 * @return The edit context.
	 */
	public static EditContext getEditMode(Action action) {
		return (EditContext) action.getProperty(EDIT_CONTEXT);
	}

	/**
	 * Helper to get the previous value from an action performing the action value
	 * functionality.
	 * 
	 * @param action The action.
	 * @return The previous value.
	 */
	public static Value getPreviousValue(Action action) {
		return (Value) action.getProperty(PREVIOUS_VALUE);
	}

	/** Edited field. */
	private Field field;
	/** Value actions. */
	private List<Action> valueActions = new ArrayList<>();
	/** Edit field. */
	private EditField editField;
	/** Label. */
	private Label label;

	/**
	 * Constructor.
	 * 
	 * @param field The edited field.
	 */
	public EditContext(Field field) {
		super();
		this.field = field;
	}

	/**
	 * Add an action to be performed when a value changes.
	 * 
	 * @param action The action.
	 */
	public void addValueAction(Action action) {
		action.setProperty(EDIT_CONTEXT, this);
		valueActions.add(action);
	}

	/**
	 * Fire value actions.
	 * 
	 * @param control       The control that triggers this action.
	 * @param previousValue The previous value.
	 * @param currentValue  The current value.
	 */
	public void fireValueActions(Control control, Value previousValue, Value currentValue) {
		for (Action action : valueActions) {
			action.setProperty(PREVIOUS_VALUE, previousValue);
			action.setProperty(CURRENT_VALUE, currentValue);
			action.actionPerformed(Action.event(control));
		}
	}

	/**
	 * Return the proper edit field control.
	 * 
	 * @return The edit field control.
	 */
	public EditField getEditField() {
		if (editField == null) {
			if (field.isBoolean()) {
				editField = getEditorBoolean();
			} else if (field.isDate()) {
				editField = getEditorDate();
			} else if (field.isPossibleValues()) {
				editField = getEditorPossibleValues();
			} else if (field.isNumber()) {
				editField = getEditorNumber();
			} else if (field.isString()) {
				editField = getEditorString();
			}
		}
		
		if (editField == null) {
			throw new NullPointerException();
		}

		return editField;
	}

	/**
	 * Shortcut to access the editor control.
	 * 
	 * @return The control.
	 */
	public Control getEditor() {
		Control editor = getEditField().getControl();
		editor.setName(getNameEditor());
		return editor;
	}

	/**
	 * Return a suitable editor for a boolean field.
	 * 
	 * @return The editor.
	 */
	private BooleanEditor getEditorBoolean() {
		BooleanEditor editor = new BooleanEditor();
		editor.setProperty(EDIT_CONTEXT, this);
		if (field.isEditBooleanInCheckBox()) {
			editor.setCheckBox();
		} else {
			editor.setComboBox();
		}
		setFont(editor);
		return editor;
	}

	/**
	 * Return a suitable editor for a date field.
	 * 
	 * @return The editor.
	 */
	private DateField getEditorDate() {
		DateField editor = new DateField();
		editor.setProperty(EDIT_CONTEXT, this);
		setFont(editor);
		setPreferredSize(editor);
		return editor;
	}

	/**
	 * Return a suitable editor for a number field.
	 * 
	 * @return The editor.
	 */
	private NumberField getEditorNumber() {
		NumberFilter.Type type = NumberFilter.Type.LOCALE;
		int length = -1;
		int decimals = -1;
		if (field.isDecimal()) {
			length = field.getLength() - field.getDecimals();
			decimals = field.getDecimals();
		}
		if (field.isInteger() || field.isLong()) {
			length = field.getLength();
			decimals = 0;
		}
		NumberField editor = new NumberField();
		editor.setProperty(EDIT_CONTEXT, this);
		setFont(editor);
		editor.setFilter(type, length, decimals);
		setPreferredSize(editor);
		return editor;
	}

	/**
	 * Return a suitable editor for a possible values field.
	 * 
	 * @return The editor.
	 */
	private ComboBox getEditorPossibleValues() {
		ComboBox editor = new ComboBox();
		editor.setProperty(EDIT_CONTEXT, this);
		setFont(editor);
		field.getPossibleValues().forEach(value -> editor.addValue(value));
		return editor;
	}

	/**
	 * Return a basic string editor.
	 * 
	 * @return The editor.
	 */
	private StringField getEditorString() {
		StringField editor = new StringField();
		editor.setProperty(EDIT_CONTEXT, this);
		setFont(editor);

		StringFilter filter = null;
		if (field.isLowercase()) {
			filter = new StringFilter(StringFilter.LOWERCASE, field.getLength());
		} else if (field.isUppercase()) {
			filter = new StringFilter(StringFilter.UPPERCASE, field.getLength());
		} else {
			filter = new StringFilter(field.getLength());
		}
		editor.setFilter(filter);
		setPreferredSize(editor);
		return editor;
	}

	/**
	 * Return the edited field.
	 * 
	 * @return The field.
	 */
	public Field getField() {
		return field;
	}

	/**
	 * Return the appropriate fill to apply in a layout.
	 * 
	 * @return The fill.
	 */
	public Fill getFill() {
		if (field.isBoolean()) {
			return Fill.NONE;
		} else if (field.isPossibleValues()) {
			return Fill.NONE;
		} else if (field.isNumber()) {
			return Fill.NONE;
		} else if (field.isFixedWidth()) {
			return Fill.NONE;
		}
		return Fill.HORIZONTAL;
	}

	/**
	 * Return the label.
	 * 
	 * @return The label.
	 */
	public Label getLabel() {
		if (label == null) {
			label = new Label(field.getDisplayLabel());
			label.setName(getNameLabel());
		}
		return label;
	}

	/**
	 * Return the name of the editor.
	 * 
	 * @return The name.
	 */
	public String getNameEditor() {
		return "editor-" + field.getAlias();
	}

	/**
	 * Return the name of the label.
	 * 
	 * @return The name.
	 */
	public String getNameLabel() {
		return "label-" + field.getAlias();
	}

	/**
	 * Shortcut to access the value.
	 * 
	 * @return The control value.
	 */
	public Value getValue() {
		return getEditField().getValue();
	}

	/**
	 * Adjust the control font based on the field.
	 * 
	 * @param editor The editor control.
	 */
	private void setFont(Control editor) {

		/* All editors. */
		Font font = editor.getFont();
		String name = font.getName();
		int style = font.getStyle();
		int size = font.getSize();
		if (field.getFontName() != null) {
			name = field.getFontName();
		}
		if (field.getFontStyle() >= 0) {
			style = field.getFontStyle();
		}
		if (field.getFontSize() > 0) {
			size = field.getFontSize();
		}
		editor.setFont(new Font(name, style, size));
	}

	/**
	 * Set the preferred size to the control. The font, if any diferent than
	 * default, must have been set.
	 * 
	 * @param editor The editor control.
	 */
	private void setPreferredSize(Control editor) {

		/* The string to calculate an average character width. */
		String str = null;

		/* Date-time fields. */
		if (field.isDate()) {
			str = "9999-99-99";
		}
		if (field.isTime()) {
			str = "99:99:99";
		}
		if (field.isDateTime()) {
			str = "9999-99-99 99:99:99";
		}

		/* Numeric fields. */
		if (field.isNumber()) {
			str = "0123456789.,-";
		}

		/* String fields not possible values. */
		if (field.isString() && !field.isPossibleValues()) {
			if (field.isUppercase()) {
				str = "ABCDEFGHIJKLMNOPQRSTUVWXYZ_0123456789";
			} else {
				str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_0123456789";
			}
		}

		/* Skip other field types. */
		if (str == null) {
			return;
		}

		FontMetrics metrics = editor.getFontMetrics();
		double strWidth = metrics.stringWidth(str);
		double avgWidth = strWidth / str.length();
		double factor = 1.5;
		double width = Numbers.round(avgWidth * (field.getDisplayLength() + 1) * factor, 0);
		Dimension size = editor.getPreferredSize();
		if (field.isFixedWidth()) {
			editor.setPreferredSize(new Dimension(width, size.getHeight()));
			editor.setMinimumSize(new Dimension(width, size.getHeight()));
//			editor.setMaximumSize(new Dimension(width, size.getHeight()));
		} else {
			editor.setMaximumSize(new Dimension(width, size.getHeight()));
		}
	}

	/**
	 * Short cut to set the value.
	 * 
	 * @param value The value.
	 */
	public void setValue(Value value) {
		getEditField().setValue(value);
	}
}
