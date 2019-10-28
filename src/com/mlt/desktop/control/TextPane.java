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
package com.mlt.desktop.control;

import javax.swing.JTextPane;

import com.mlt.db.Value;
import com.mlt.desktop.EditContext;
import com.mlt.desktop.EditField;

/**
 * Text pane extension.
 *
 * @author Miquel Sas
 */
public class TextPane extends Control implements EditField {

	/**
	 * Constructor.
	 */
	public TextPane() {
		super();
		setComponent(new JTextPane());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JTextPane getComponent() {
		return (JTextPane) super.getComponent();
	}

	/**
	 * Return the content type.
	 * 
	 * @return The content type.
	 * @see javax.swing.JEditorPane#getContentType()
	 */
	public String getContentType() {
		return getComponent().getContentType();
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
	 * Set the content type.
	 * 
	 * @param type The content type.
	 * @see javax.swing.JEditorPane#setContentType(java.lang.String)
	 */
	public void setContentType(String type) {
		getComponent().setContentType(type);
	}

	/**
	 * Check editable.
	 * 
	 * @return A boolean.
	 * @see javax.swing.text.JTextComponent#isEditable()
	 */
	public boolean isEditable() {
		return getComponent().isEditable();
	}

	/**
	 * Set editable.
	 * 
	 * @param b A boolean.
	 * @see javax.swing.text.JTextComponent#setEditable(boolean)
	 */
	public void setEditable(boolean b) {
		getComponent().setEditable(b);
	}

	/**
	 * Return the text.
	 * 
	 * @return The text.
	 * @see javax.swing.JEditorPane#getText()
	 */
	public String getText() {
		return getComponent().getText();
	}

	/**
	 * Set the text.
	 * 
	 * @param text The text.
	 * @see javax.swing.JEditorPane#setText(java.lang.String)
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
		getComponent().setText(value.toString());
		if (getEditContext() != null) {
			getEditContext().fireValueActions(this, previousValue, value);
		}
	}
}
