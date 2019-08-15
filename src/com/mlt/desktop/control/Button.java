/*
 * Copyright (C) 2018 Miquel Sas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.mlt.desktop.control;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import com.mlt.desktop.AWT;
import com.mlt.desktop.layout.Insets;

/**
 * Button.
 * 
 * @author Miquel Sas
 */
public class Button extends Control {

	/**
	 * Constructor.
	 */
	public Button() {
		super();
		setComponent(new JButton());
	}

	/**
	 * Do click the button.
	 * 
	 * @see javax.swing.AbstractButton#doClick()
	 */
	public void doClick() {
		getComponent().doClick();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JButton getComponent() {
		return (JButton) super.getComponent();
	}

	/**
	 * Return the icon.
	 * 
	 * @return The icon.
	 * @see javax.swing.AbstractButton#getIcon()
	 */
	public Icon getIcon() {
		return getComponent().getIcon();
	}

	/**
	 * Set the action.
	 * 
	 * @param actionListener The action.
	 * @see javax.swing.AbstractButton#setAction(javax.swing.Action)
	 */
	public void setAction(ActionListener actionListener) {
		getComponent().removeActionListener(actionListener);
		getComponent().addActionListener(actionListener);
	}

	/**
	 * Set the icon.
	 * 
	 * @param icon The icon.
	 * @see javax.swing.AbstractButton#setIcon(Icon)
	 */
	public void setIcon(Icon icon) {
		getComponent().setIcon(icon);
	}

	/**
	 * Return the icon-text gap.
	 * 
	 * @return The icon-text gap.
	 * @see javax.swing.AbstractButton#getIconTextGap()
	 */
	public int getIconTextGap() {
		return getComponent().getIconTextGap();
	}

	/**
	 * Set the icon-text gap.
	 * 
	 * @param gap The gap.
	 * @see javax.swing.AbstractButton#setIconTextGap(int)
	 */
	public void setIconTextGap(int gap) {
		getComponent().setIconTextGap(gap);
	}

	/**
	 * Return the margins.
	 * 
	 * @return The margins.
	 * @see javax.swing.AbstractButton#getMargin()
	 */
	public Insets getMargin() {
		return AWT.fromAWT(getComponent().getMargin());
	}

	/**
	 * Set the margins.
	 * 
	 * @param insets The margins.
	 * @see javax.swing.AbstractButton#setMargin(Insets)
	 */
	public void setMargin(Insets insets) {
		getComponent().setMargin(AWT.toAWT(insets));
	}

	/**
	 * Set the mnemonic.
	 * 
	 * @param mnemonic The mnemonic.
	 * @see javax.swing.AbstractButton#setMnemonic(int)
	 */
	public void setMnemonic(int mnemonic) {
		getComponent().setMnemonic(mnemonic);
	}

	/**
	 * Return the text.
	 * 
	 * @return The text.
	 * @see javax.swing.AbstractButton#getText()
	 */
	public String getText() {
		return getComponent().getText();
	}

	/**
	 * Set the text.
	 * 
	 * @param text The text.
	 * @see javax.swing.AbstractButton#setText(String)
	 */
	public void setText(String text) {
		getComponent().setText(text);
	}
}
