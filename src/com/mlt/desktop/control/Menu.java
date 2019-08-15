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

import javax.swing.JMenu;

/**
 * A menu control of a menu bar.
 *
 * @author Miquel Sas
 */
public class Menu extends MenuItem {

	/**
	 * Constructor.
	 */
	public Menu() {
		super(JMenu.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JMenu getComponent() {
		return (JMenu) super.getComponent();
	}

	/**
	 * Add a menu item.
	 * 
	 * @param menuItem The menu item.
	 */
	public void add(MenuItem menuItem) {
		getComponent().add(menuItem.getComponent());
	}
}
