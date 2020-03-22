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

import java.awt.Font;
import java.awt.GridBagLayout;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mlt.desktop.AWT;
import com.mlt.desktop.layout.Alignment;
import com.mlt.desktop.layout.Anchor;
import com.mlt.desktop.layout.Constraints;
import com.mlt.desktop.layout.Dimension;
import com.mlt.desktop.layout.Fill;
import com.mlt.desktop.layout.Insets;

/**
 * Label control with margin.
 *
 * @author Miquel Sas
 */
public class Label extends Control {

	/**
	 * Returns the preferred size for a label.
	 *
	 * @param label The label.
	 * @return The preferred size.
	 */
	public static Dimension getPreferredSize(Label label) {
		return getPreferredSize(label, "Sample text");
	}

	/**
	 * Returns the preferred size for a label.
	 *
	 * @param label      The label.
	 * @param sampleText The sample text used to get the size.
	 * @return The preferred size.
	 */
	public static Dimension getPreferredSize(Label label, String sampleText) {
		String text = label.getText();
		label.setText(sampleText);
		Dimension size = label.getPreferredSize();
		label.setText(text);
		return size;
	}

	/**
	 * Sets the appropriate preferred and minimum sizes to the label.
	 *
	 * @param label The label.
	 */
	public static void setPreferredAndMinimumSize(Label label) {
		label.setPreferredSize(getPreferredSize(label));
		label.setMinimumSize(getPreferredSize(label));
	}

	/** The swing label. */
	private JLabel label = new JLabel();
	/** The margin. */
	private Insets margin = new Insets(0, 0, 0, 0);

	/**
	 * Constructor.
	 */
	public Label() {
		super();
		setComponent(new JPanel(new GridBagLayout()));
		setOpaque(false);
		doLayout();
	}

	/**
	 * Constructor.
	 *
	 * @param text The text.
	 */
	public Label(String text) {
		super();
		setComponent(new JPanel(new GridBagLayout()));
		setOpaque(false);
		setText(text);
		doLayout();
	}

	/**
	 * Constructor.
	 *
	 * @param image The icon image.
	 */
	public Label(Icon image) {
		super();
		setComponent(new JPanel(new GridBagLayout()));
		setOpaque(false);
		setIcon(image);
		doLayout();
	}

	/**
	 * Do the layout.
	 */
	private void doLayout() {
		getComponent().removeAll();
		Constraints constraints = new Constraints(Anchor.TOP, Fill.BOTH, 0, 0, margin);
		getComponent().add(label, AWT.toAWT(constraints));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JPanel getComponent() {
		return (JPanel) super.getComponent();
	}

	/*
	 * Specific label functionality.
	 */
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Font getFont() {
		return label.getFont();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFont(Font font) {
		label.setFont(font);
	}

	/**
	 * Return the icon.
	 *
	 * @return The icon.
	 * @see javax.swing.JLabel#getIcon()
	 */
	public Icon getIcon() {
		return label.getIcon();
	}

	/**
	 * Set the icon.
	 *
	 * @param icon The icon.
	 * @see javax.swing.JLabel#setIcon(javax.swing.Icon)
	 */
	public final void setIcon(Icon icon) {
		label.setIcon(icon);
	}

	/**
	 * Set the icon-text gap.
	 *
	 * @param iconTextGap The gap.
	 */
	public void setIconTextGap(int iconTextGap) {
		label.setIconTextGap(iconTextGap);
	}

	/**
	 * Return the current margin.
	 *
	 * @return The margin.
	 */
	public Insets getMargin() {
		return margin;
	}

	/**
	 * Set the margin.
	 *
	 * @param margin The margin.
	 */
	public void setMargin(Insets margin) {
		this.margin = margin;
		doLayout();
	}

	/**
	 * Return the text.
	 *
	 * @return The text.
	 * @see javax.swing.JLabel#getText()
	 */
	public String getText() {
		return label.getText();
	}

	/**
	 * Set the text.
	 *
	 * @param text The text.
	 * @see javax.swing.JLabel#setText(java.lang.String)
	 */
	public final void setText(String text) {
		label.setText(text);
	}

	/**
	 * Set the horizontal alignment.
	 *
	 * @param alignment The horizontal alignment.
	 */
	public void setHorizontalAlignment(Alignment alignment) {
		if (!alignment.isHorizontal()) {
			throw new IllegalArgumentException();
		}
		label.setHorizontalAlignment(AWT.toAWT(alignment));
	}

	/**
	 * Set the vertical alignment.
	 *
	 * @param alignment The vertical alignment.
	 */
	public void setVerticalAlignment(Alignment alignment) {
		if (!alignment.isVertical()) throw new IllegalArgumentException();
		label.setVerticalAlignment(AWT.toAWT(alignment));
	}
}
