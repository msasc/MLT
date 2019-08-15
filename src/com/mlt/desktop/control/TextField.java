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

import javax.swing.JTextField;

import com.mlt.desktop.AWT;
import com.mlt.desktop.formatter.TextFieldFilter;
import com.mlt.desktop.layout.Alignment;
import com.mlt.desktop.layout.Insets;

/**
 * A text field with an optional filter.
 *
 * @author Miquel Sas
 */
public class TextField extends Control {

	/** Optional filter. */
	private TextFieldFilter filter;

	/**
	 * 
	 */
	public TextField() {
		super();
		setComponent(new JTextField());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JTextField getComponent() {
		return (JTextField) super.getComponent();
	}

	/**
	 * Return the current filter or null if none has been assigned.
	 * 
	 * @return The filter.
	 */
	public TextFieldFilter getFilter() {
		return filter;
	}

	/**
	 * Return the current margin.
	 *
	 * @return The margin.
	 */
	public Insets getMargin() {
		return AWT.fromAWT(getComponent().getMargin());
	}

	/**
	 * Return the text.
	 * 
	 * @return The text.
	 */
	public String getText() {
		return getComponent().getText();
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
	 * Assign a filter.
	 * 
	 * @param filter The filter to apply.
	 */
	public void setFilter(TextFieldFilter filter) {
		this.filter = filter;
		this.filter.setTextField(getComponent());
	}

	/**
	 * Set the horizontal alignment.
	 * 
	 * @param alignment The alignment.
	 */
	public void setHorizontalAlignment(Alignment alignment) {
		if (!alignment.isHorizontal()) {
			throw new IllegalArgumentException("Invalid horizontal alignment");
		}
		getComponent().setHorizontalAlignment(AWT.toAWT(alignment));
	}

	/**
	 * Set the margin.
	 *
	 * @param margin The margin.
	 */
	public void setMargin(Insets margin) {
		getComponent().setMargin(AWT.toAWT(margin));
	}

	/**
	 * Set the text.
	 * 
	 * @param text The text.
	 */
	public void setText(String text) {
		getComponent().setText(text);
	}
}
