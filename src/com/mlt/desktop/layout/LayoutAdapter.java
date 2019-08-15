/*
 * Copyright (C) 2017 Miquel Sas
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

package com.mlt.desktop.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.List;

/**
 * An adapter for the {@code java.awt.LayoutManager2} interface.
 *
 * @author Miquel Sas
 */
public class LayoutAdapter implements LayoutManager {

	/**
	 * Constructor.
	 */
	public LayoutAdapter() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addLayoutComponent(String name, Component comp) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeLayoutComponent(Component comp) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Dimension preferredLayoutSize(Container container) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Dimension minimumLayoutSize(Container container) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void layoutContainer(Container container) {
	}

	/**
	 * Returns the list of visible components of the container.
	 * 
	 * @param container The container container.
	 * @return The list of visible components.
	 */
	protected List<Component> getVisibleComponents(Container container) {
		List<Component> components = new ArrayList<>();
		for (int i = 0; i < container.getComponentCount(); i++) {
			Component component = container.getComponent(i);
			if (component.isVisible()) {
				components.add(component);
			}
		}
		return components;
	}
}
