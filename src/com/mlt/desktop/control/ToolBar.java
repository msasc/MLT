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

import com.mlt.desktop.AWT;
import com.mlt.desktop.layout.Dimension;
import com.mlt.desktop.layout.Orientation;
import javax.swing.JToolBar;

/**
 * A tool bar control.
 *
 * @author Miquel Sas
 */
public class ToolBar extends Control {

	/**
	 * Constructor with horizontal orientation.
	 */
	public ToolBar() {
		this(Orientation.HORIZONTAL);
	}

	/**
	 * Constructor.
	 *
	 * @param orientation The orientation.
	 */
	public ToolBar(Orientation orientation) {
		super();
		if (orientation.isHorizontal()) {
			setComponent(new JToolBar(JToolBar.HORIZONTAL));
		} else {
			setComponent(new JToolBar(JToolBar.VERTICAL));
		}
		getComponent().setFloatable(false);
	}

	/**
	 * Add a button to the tool bar.
	 *
	 * @param button The button.
	 */
	public void addButton(Button button) {
		getComponent().add(button.getComponent());
	}

	/**
	 * Add a pane to the tool bar.
	 *
	 * @param pane The pane.
	 */
	public void addPane(Pane pane) {
		getComponent().add(pane.getComponent());
	}

	/**
	 * Add a separator.
	 */
	public void addSeparator() {
		getComponent().addSeparator();
	}

	/**
	 * Add a separator of the given size.
	 *
	 * @param size The size of the separator.
	 */
	public void addSeparator(Dimension size) {
		getComponent().addSeparator(AWT.toAWT(size));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JToolBar getComponent() {
		return (JToolBar) super.getComponent();
	}
}
