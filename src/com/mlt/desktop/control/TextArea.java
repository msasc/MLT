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

/**
 * Text area extension.
 *
 * @author Miquel Sas
 */
public class TextArea extends Control {

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

	/*
	 * Specific text ares functionality.
	 */

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

}
