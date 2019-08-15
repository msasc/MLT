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

import java.awt.GridBagLayout;

import com.mlt.desktop.AWT;
import com.mlt.desktop.layout.Constraints;

/**
 * A pane with grid bag layout.
 *
 * @author Miquel Sas
 */
public class GridBagPane extends Pane {

	/**
	 * Constructor.
	 */
	public GridBagPane() {
		super();
		setLayout(new GridBagLayout());
	}

	/**
	 * Add the control.
	 * 
	 * @param control     The control to add.
	 * @param constraints The constraints.
	 */
	public final void add(Control control, Constraints constraints) {
		getComponent().add(control.getComponent(), AWT.toAWT(constraints));
	}

	/**
	 * Remove all components.
	 * 
	 * @see java.awt.Container#removeAll()
	 */
	public void removeAll() {
		getComponent().removeAll();
	}

}
