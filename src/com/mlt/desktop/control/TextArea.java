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

import java.awt.event.KeyEvent;

import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import com.mlt.db.Value;
import com.mlt.desktop.EditContext;
import com.mlt.desktop.EditField;
import com.mlt.desktop.layout.Dimension;

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
		setComponent(configure(new JTextArea()));
	}

	/**
	 * Constructor.
	 * 
	 * @param text The text.
	 */
	public TextArea(String text) {
		super();
		setComponent(configure(new JTextArea(text)));
	}

	/**
	 * Constructor.
	 * 
	 * @param rows    Number of rows.
	 * @param columns Number of columns.
	 */
	public TextArea(int rows, int columns) {
		super();
		setComponent(configure(new JTextArea(rows, columns)));
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
		setComponent(configure(new JTextArea(text, rows, columns)));
	}

	private JTextArea configure(JTextArea textArea) {
		textArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "");
		textArea.getInputMap().put(
			KeyStroke.getKeyStroke(
				KeyEvent.VK_ENTER,
				KeyEvent.ALT_DOWN_MASK),
			"insert-break");
		return textArea;
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
