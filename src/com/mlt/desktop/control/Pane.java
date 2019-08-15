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

import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Pane extension. The default layout is grid bag layout.
 *
 * @author Miquel Sas
 */
public abstract class Pane extends Control {

	/**
	 * Constructor.
	 */
	public Pane() {
		super();
		setComponent(new JPanel(new GridBagLayout()));
		setFocusable(false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final JPanel getComponent() {
		return (JPanel) super.getComponent();
	}

	/**
	 * Add a control.
	 * 
	 * @param control The component.
	 * @return The added component.
	 */
	protected Component add(Control control) {
		return getComponent().add(control.getComponent());
	}

	/**
	 * Check whether this pane is empty, has no controls.
	 * 
	 * @return A boolean.
	 */
	public boolean isEmpty() {
		return getControlCount() == 0;
	}

	/**
	 * Remove the given control.
	 * 
	 * @param control
	 */
	protected void remove(Control control) {
		getComponent().remove(control.getComponent());
	}

	/**
	 * Remove all components.
	 * 
	 * @see java.awt.Container#removeAll()
	 */
	protected void removeAll() {
		getComponent().removeAll();
	}

	/**
	 * Return the control at the given position.
	 * 
	 * @param n The component index.
	 * @return The component.
	 */
	public Control getControl(int n) {
		JComponent component = (JComponent) getComponent().getComponent(n);
		return Control.getControl(component);
	}

	/**
	 * Return the number of components.
	 * 
	 * @return The number of components.
	 * @see java.awt.Container#getComponentCount()
	 */
	public int getControlCount() {
		return getComponent().getComponentCount();
	}

	/**
	 * Return a list with the firs level controls.
	 * 
	 * @return The list of controls.
	 */
	public List<Control> getControls() {
		List<Control> controls = new ArrayList<>();
		for (int i = 0; i < getControlCount(); i++) {
			controls.add(getControl(i));
		}
		return controls;
	}

	/**
	 * Return the layout manager.
	 * 
	 * @return The layout manager.
	 * @see java.awt.Container#getLayout()
	 */
	public LayoutManager getLayout() {
		return getComponent().getLayout();
	}

	/**
	 * Set the layout manager.
	 * 
	 * @param manager The layout manager.
	 * @see java.awt.Container#setLayout(java.awt.LayoutManager)
	 */
	protected final void setLayout(LayoutManager manager) {
		getComponent().setLayout(manager);
	}
}
