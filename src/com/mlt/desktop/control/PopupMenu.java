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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.MenuKeyListener;

/**
 * Popup menu extension.
 *
 * @author Miquel Sas
 */
public class PopupMenu extends Control {

	/**
	 * Constructor.
	 */
	public PopupMenu() {
		super();
		setComponent(new JPopupMenu());
	}

	/**
	 * Constructor.
	 * 
	 * @param label The label or text.
	 */
	public PopupMenu(String label) {
		super();
		setComponent(new JPopupMenu(label));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JPopupMenu getComponent() {
		return (JPopupMenu) super.getComponent();
	}

	/**
	 * Add a menu item.
	 * 
	 * @param menuItem
	 */
	public void add(MenuItem menuItem) {
		getComponent().add(menuItem.getComponent());
	}

	/**
	 * Add a menu key listener.
	 * 
	 * @param l The listener.
	 * @see javax.swing.JPopupMenu#addMenuKeyListener(MenuKeyListener)
	 */
	public void addMenuKeyListener(MenuKeyListener l) {
		getComponent().addMenuKeyListener(l);
	}

	/**
	 * Add a separator.
	 * 
	 * @see javax.swing.JPopupMenu#addSeparator()
	 */
	public void addSeparator() {
		getComponent().addSeparator();
	}

	/**
	 * Returns the list with all menu items in this popup menu.
	 * 
	 * @return The list of menu items.
	 */
	public List<MenuItem> getMenuItems() {
		List<MenuItem> menuItems = new ArrayList<>();
		for (int i = 0; i < getComponent().getComponentCount(); i++) {
			JMenuItem item = (JMenuItem) getComponent().getComponent(i);
			MenuItem menuItem = (MenuItem) Control.getControl(item);
			menuItems.add(menuItem);
		}
		return menuItems;
	}
}
