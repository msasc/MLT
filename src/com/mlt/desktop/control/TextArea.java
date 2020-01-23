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

import javax.swing.JTextArea;

import com.mlt.db.Value;
import com.mlt.desktop.AWT;
import com.mlt.desktop.EditContext;
import com.mlt.desktop.EditField;
import com.mlt.desktop.layout.Dimension;
import com.mlt.desktop.layout.Insets;

/**
 * Text area hosted in a scroll pane.
 *
 * @author Miquel Sas
 */
public class TextArea extends Control implements EditField {

	/** Scrollpane for the edit field. */
	private ScrollPane scrollPane;

	/**
	 * Constructor.
	 */
	public TextArea() {
		super();
		setComponent(new JTextArea());
	}

	/**
	 * Constructor.
	 * 
	 * @param text The text.
	 */
	public TextArea(String text) {
		super();
		setComponent(new JTextArea(text));
	}

	/**
	 * Constructor.
	 * 
	 * @param rows    Number of rows.
	 * @param columns Number of columns.
	 */
	public TextArea(int rows, int columns) {
		super();
		setComponent(new JTextArea(rows, columns));
	}

	/**
	 * Constructor.
	 * 
	 * @param text    The text.
	 * @param rows    Number of rows.
	 * @param columns Number of columns.
	 */
	public TextArea(String text, int rows, int columns) {
		super();
		setComponent(new JTextArea(text, rows, columns));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JTextArea getComponent() {
		return (JTextArea) super.getComponent();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Control getControl() {
		if (scrollPane == null) {
			scrollPane = new ScrollPane(this);
		}
		return scrollPane;
	}

	/**
	 * @return The caret position.
	 */
	public int getCaretPosition() {
		return getComponent().getCaretPosition();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EditContext getEditContext() {
		return (EditContext) getProperty(EditContext.EDIT_CONTEXT);
	}

	/**
	 * Return the line wrap policy.
	 * 
	 * @return A boolean.
	 */
	public boolean getLineWrap() {
		return getComponent().getLineWrap();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Dimension getMaximumSize() {
		return getControl().getMaximumSize();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Dimension getMinimumSize() {
		return getControl().getMinimumSize();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Dimension getPreferredSize() {
		return getControl().getPreferredSize();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Value getValue() {
		return new Value(getComponent().getText());
	}

	/**
	 * Check editable.
	 * 
	 * @return A boolean.
	 */
	public boolean isEditable() {
		return getComponent().isEditable();
	}

	/**
	 * Replace.
	 * 
	 * @param str   Replace string.
	 * @param start Start position.
	 * @param end   End position.
	 */
	public void replaceRange(String str, int start, int end) {
		getComponent().replaceRange(str, start, end);
	}

	/**
	 * Set the caret position.
	 * 
	 * @param position The caret position.
	 */
	public void setCaretPosition(int position) {
		getComponent().setCaretPosition(position);
	}

	/**
	 * Set editable.
	 * 
	 * @param b A boolean.
	 */
	public void setEditable(boolean b) {
		getComponent().setEditable(b);
	}

	/**
	 * Set the line wrap policy.
	 * 
	 * @param wrap A boolean.
	 */
	public void setLineWrap(boolean wrap) {
		getComponent().setLineWrap(wrap);
	}
	
	public void setMargin(int top, int left, int bottom, int right) {
		getComponent().setMargin(AWT.toAWT(new Insets(top, left, bottom, right)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setMaximumSize(Dimension size) {
		getControl().setMaximumSize(size);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setMinimumSize(Dimension size) {
		getControl().setMinimumSize(size);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPreferredSize(Dimension size) {
		getControl().setPreferredSize(size);
	}

	/**
	 * @param text The text.
	 */
	public void setText(String text) {
		getComponent().setText(text);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setValue(Value value) {
		Value previousValue = getValue();
		setText(value.toString());
		if (getEditContext() != null) {
			getEditContext().fireValueActions(this, previousValue, value);
		}
	}
}
