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

import java.awt.BorderLayout;

import com.mlt.desktop.layout.Anchor;
import com.mlt.desktop.layout.Constraints;
import com.mlt.desktop.layout.Fill;
import com.mlt.desktop.layout.Insets;
import com.mlt.desktop.layout.Position;

/**
 * A border pane with top, left, bottom, right and center controls.
 *
 * @author Miquel Sas
 */
public class BorderPane extends Pane {

	/** Top control. */
	private Control top;
	/** Left control. */
	private Control left;
	/** Bottom control. */
	private Control bottom;
	/** Right control. */
	private Control right;
	/** Center control. */
	private Control center;

	/**
	 * Constructor.
	 */
	public BorderPane() {
		super();
		setLayout(new BorderLayout());
	}

	/**
	 * Return the bottom control.
	 * 
	 * @return The control.
	 */
	public Control getBottom() {
		return getControl(Position.BOTTOM);
	}

	/**
	 * Return the center control.
	 * 
	 * @return The control.
	 */
	public Control getCenter() {
		return getControl(Position.CENTER);
	}

	/**
	 * Return the pane to hold the control with the insets.
	 * 
	 * @param control The control.
	 * @param insets  The insets.
	 * @return The parent pane.
	 */
	private GridBagPane getComponentPane(Control control, Insets insets) {
		/* Default insets. */
		if (insets == null) insets = new Insets(0, 0, 0, 0);
		/* Pane to setup the control with insets. */
		GridBagPane pane = new GridBagPane();
		pane.add(control, new Constraints(Anchor.CENTER, Fill.BOTH, 0, 0, insets));
		return pane;
	}

	/**
	 * Return the control in the position, that is in a pane.
	 * 
	 * @param position The position.
	 * @return The control.
	 */
	private Control getControl(Position position) {
		switch (position) {
		case TOP:
			return top;
		case LEFT:
			return left;
		case BOTTOM:
			return bottom;
		case RIGHT:
			return right;
		case CENTER:
			return center;
		}
		return null;
	}

	/**
	 * Return the left control.
	 * 
	 * @return The control.
	 */
	public Control getLeft() {
		return getControl(Position.LEFT);
	}

	/**
	 * Return the border layout position given a geometry position.
	 * 
	 * @return The border layout position.
	 */
	private String getPosition(Position position) {
		switch (position) {
		case TOP:
			return BorderLayout.NORTH;
		case LEFT:
			return BorderLayout.WEST;
		case BOTTOM:
			return BorderLayout.SOUTH;
		case RIGHT:
			return BorderLayout.EAST;
		case CENTER:
			return BorderLayout.CENTER;
		}
		return BorderLayout.CENTER;
	}

	/**
	 * Return the right control.
	 * 
	 * @return The control.
	 */
	public Control getRight() {
		return getControl(Position.RIGHT);
	}

	/**
	 * Return the top control.
	 * 
	 * @return The control.
	 */
	public Control getTop() {
		return getControl(Position.TOP);
	}

	/**
	 * Remove the bottom control.
	 */
	public void removeBottom() {
		removePosition(Position.BOTTOM);
	}

	/**
	 * Remove the center control.
	 */
	public void removeCenter() {
		removePosition(Position.CENTER);
	}

	/**
	 * Remove the left control.
	 */
	public void removeLeft() {
		removePosition(Position.LEFT);
	}

	/**
	 * Remove and clear the given position.
	 * 
	 * @param position The position.
	 */
	private void removePosition(Position position) {
		Control control = getControl(position);
		if (control == null) return;
		GridBagPane pane = (GridBagPane) control.getProperty("BORDER_PANE_CONTAINER");
		remove(pane);
		switch (position) {
		case TOP:
			top = null;
			break;
		case LEFT:
			left = null;
			break;
		case BOTTOM:
			bottom = null;
			break;
		case RIGHT:
			right = null;
			break;
		case CENTER:
			center = null;
			break;
		}
	}

	/**
	 * Remove the right control.
	 */
	public void removeRight() {
		removePosition(Position.RIGHT);
	}

	/**
	 * Remove the top control.
	 */
	public void removeTop() {
		removePosition(Position.TOP);
	}

	/**
	 * Set the bottom control.
	 * 
	 * @param control The control.
	 */
	public void setBottom(Control control) {
		setBottom(control, null);
	}

	/**
	 * Set the bottom control.
	 * 
	 * @param control The control.
	 * @param insets  Insets.
	 */
	public void setBottom(Control control, Insets insets) {
		setControl(control, insets, Position.BOTTOM);
	}

	/**
	 * Set the center control.
	 * 
	 * @param control The control.
	 */
	public void setCenter(Control control) {
		setCenter(control, null);
	}

	/**
	 * Set the center control.
	 * 
	 * @param control The control.
	 * @param insets  Insets.
	 */
	public void setCenter(Control control, Insets insets) {
		setControl(control, insets, Position.CENTER);
	}

	/**
	 * Set the control with insets at the position.
	 * 
	 * @param control  The control.
	 * @param insets   The insets.
	 * @param position The position.
	 */
	private void setControl(Control control, Insets insets, Position position) {
		if (control == null) {
			removePosition(position);
			return;
		}
		GridBagPane pane = getComponentPane(control, insets);
		control.setProperty("BORDER_PANE_CONTAINER", pane);
		getComponent().add(pane.getComponent(), getPosition(position));
		switch (position) {
		case TOP:
			top = control;
			break;
		case LEFT:
			left = control;
			break;
		case BOTTOM:
			bottom = control;
			break;
		case RIGHT:
			right = control;
			break;
		case CENTER:
			center = control;
			break;
		}
	}

	/**
	 * Set the left control.
	 * 
	 * @param control The control.
	 */
	public void setLeft(Control control) {
		setLeft(control, null);
	}

	/**
	 * Set the left control.
	 * 
	 * @param control The control.
	 * @param insets  Insets.
	 */
	public void setLeft(Control control, Insets insets) {
		setControl(control, insets, Position.LEFT);
	}

	/**
	 * Set the right control.
	 * 
	 * @param control The control.
	 */
	public void setRight(Control control) {
		setRight(control, null);
	}

	/**
	 * Set the right control.
	 * 
	 * @param control The control.
	 * @param insets  Insets.
	 */
	public void setRight(Control control, Insets insets) {
		setControl(control, insets, Position.RIGHT);
	}

	/**
	 * Set the top control.
	 * 
	 * @param control The control.
	 */
	public void setTop(Control control) {
		setTop(control, null);
	}

	/**
	 * Set the top control.
	 * 
	 * @param control The control.
	 * @param insets  Insets.
	 */
	public void setTop(Control control, Insets insets) {
		setControl(control, insets, Position.TOP);
	}
}
