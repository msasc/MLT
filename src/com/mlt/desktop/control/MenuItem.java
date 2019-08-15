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

import com.mlt.util.Logs;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 * Menu item control.
 *
 * @author Miquel Sas
 */
public class MenuItem extends Control {

	/**
	 * Constructor.
	 */
	public MenuItem() {
		this(JMenuItem.class);
	}

	/**
	 * Constructor used to assign the proper component class (JManu or JMenuItem)
	 * 
	 * @param componentClass The component class.
	 */
	protected MenuItem(Class<? extends JMenuItem> componentClass) {
		// @off
		try {
			setComponent(componentClass.getDeclaredConstructor().newInstance());
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
		| NoSuchMethodException | SecurityException e) {
			Logs.catching(e);
		}
		// @on
	}

	/*
	 * Specific menu item functionality.
	 */

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JMenuItem getComponent() {
		return (JMenuItem) super.getComponent();
	}

	/**
	 * Set the accelerator keystroke.
	 * 
	 * @param keyStroke The accelerator keystroke.
	 * @see javax.swing.JMenuItem#setAccelerator(javax.swing.KeyStroke)
	 */
	public void setAccelerator(KeyStroke keyStroke) {
		getComponent().setAccelerator(keyStroke);
	}

	/**
	 * Set the action.
	 * 
	 * @param actionListener The action.
	 * @see javax.swing.AbstractButton#setAction(javax.swing.Action)
	 */
	public void setAction(ActionListener actionListener) {
		getComponent().addActionListener(actionListener);
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
	 * Set the icon.
	 * 
	 * @param icon The icon.
	 * @see javax.swing.AbstractButton#setIcon(Icon)
	 */
	public void setIcon(Icon icon) {
		getComponent().setIcon(icon);
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

	/**
	 * Do click the button.
	 * 
	 * @see javax.swing.AbstractButton#doClick()
	 */
	public void doClick() {
		getComponent().doClick();
	}
}
