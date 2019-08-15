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

import java.nio.channels.IllegalSelectorException;

import javax.swing.JComponent;
import javax.swing.JSplitPane;

import com.mlt.desktop.layout.Orientation;

/**
 * A split pane.
 *
 * @author Miquel Sas
 */
public class SplitPane extends Control {

	/**
	 * Constructor.
	 */
	public SplitPane() {
		this(Orientation.VERTICAL);
	}

	/**
	 * Constructor.
	 *
	 * @param orientation Split orientation.
	 */
	public SplitPane(Orientation orientation) {
		super();
		if (orientation.isVertical()) {
			setComponent(new JSplitPane(JSplitPane.VERTICAL_SPLIT));
		} else {
			setComponent(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT));
		}
	}

	/**
	 * Return the bottom control or null if none set.
	 * 
	 * @return The bottom control.
	 */
	public Control getBottomControl() {
		if (getSplitOrientation() != Orientation.VERTICAL) throw new IllegalSelectorException();
		JComponent component = (JComponent) getComponent().getBottomComponent();
		return Control.getControl(component);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JSplitPane getComponent() {
		return (JSplitPane) super.getComponent();
	}

	/**
	 * Return the left control or null if none set.
	 * 
	 * @return The left control.
	 */
	public Control getLeftControl() {
		if (getSplitOrientation() != Orientation.HORIZONTAL) throw new IllegalSelectorException();
		JComponent component = (JComponent) getComponent().getLeftComponent();
		return Control.getControl(component);
	}

	/**
	 * Return the right control or null if none set.
	 * 
	 * @return The right control.
	 */
	public Control getRightControl() {
		if (getSplitOrientation() != Orientation.HORIZONTAL) throw new IllegalSelectorException();
		JComponent component = (JComponent) getComponent().getRightComponent();
		return Control.getControl(component);
	}

	/**
	 * Return the split orientation.
	 * 
	 * @return The split orientation.
	 */
	public Orientation getSplitOrientation() {
		if (getComponent().getOrientation() == JSplitPane.VERTICAL_SPLIT) return Orientation.VERTICAL;
		return Orientation.HORIZONTAL;
	}

	/**
	 * Return the top control or null if none set.
	 * 
	 * @return The top control.
	 */
	public Control getTopControl() {
		if (getSplitOrientation() != Orientation.VERTICAL) throw new IllegalSelectorException();
		JComponent component = (JComponent) getComponent().getTopComponent();
		return Control.getControl(component);
	}
	
	/**
	 * Reset to preferred sizes.
	 */
	public void resetToPreferredSizes() {
		getComponent().resetToPreferredSizes();
	}

	/**
	 * Set the bottom control.
	 * 
	 * @param control The control.
	 */
	public void setBottomControl(Control control) {
		if (getSplitOrientation() != Orientation.VERTICAL) throw new IllegalSelectorException();
		getComponent().setBottomComponent(control.getComponent());
	}

	/**
	 * Set the continuous layout.
	 * 
	 * @param b A boolean.
	 */
	public void setContinuousLayout(boolean b) {
		getComponent().setContinuousLayout(b);
	}

	/**
	 * Set the absolute divider location.
	 * 
	 * @param location The absolute location.
	 */
	public void setDividerLocation(int location) {
		getComponent().setDividerLocation(location);
	}

	/**
	 * Set the divider size.
	 * 
	 * @param size
	 */
	public void setDividerSize(int size) {
		getComponent().setDividerSize(size);
	}

	/**
	 * Set the left control.
	 * 
	 * @param control The control.
	 */
	public void setLeftControl(Control control) {
		if (getSplitOrientation() != Orientation.HORIZONTAL) throw new IllegalSelectorException();
		getComponent().setLeftComponent(control.getComponent());
	}

	/**
	 * Set the resize weight.
	 * 
	 * @param weight The resize weight.
	 */
	public void setResizeWeight(double weight) {
		getComponent().setResizeWeight(weight);
	}

	/**
	 * Set the right control.
	 * 
	 * @param control The control.
	 */
	public void setRightControl(Control control) {
		if (getSplitOrientation() != Orientation.HORIZONTAL) throw new IllegalSelectorException();
		getComponent().setRightComponent(control.getComponent());
	}

	/**
	 * Set the top control.
	 * 
	 * @param control The control.
	 */
	public void setTopControl(Control control) {
		if (getSplitOrientation() != Orientation.VERTICAL) throw new IllegalSelectorException();
		getComponent().setTopComponent(control.getComponent());
	}

}
