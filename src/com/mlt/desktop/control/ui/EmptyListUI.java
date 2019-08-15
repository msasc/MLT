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
package com.mlt.desktop.control.ui;

import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JList;
import javax.swing.plaf.ListUI;

/**
 * Empty list UI aimed to avoid implementing abstract methods not used in certain implementations like
 * <em>RowHeaderUI</em>.
 *
 * @author Miquel Sas
 */
public class EmptyListUI extends ListUI {

	/**
	 * Constructor.
	 */
	public EmptyListUI() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public int locationToIndex(JList list, Point location) {
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Point indexToLocation(JList list, int index) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Rectangle getCellBounds(JList list, int index1, int index2) {
		return null;
	}
}
