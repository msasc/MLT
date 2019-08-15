/*
 * Copyright (C) 2018 Miquel Sas
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.mlt.desktop.control;

import javax.swing.JSlider;
import javax.swing.event.ChangeListener;

import com.mlt.desktop.AWT;
import com.mlt.desktop.layout.Orientation;

/**
 * A slider control.
 *
 * @author Miquel Sas
 */
public class Slider extends Control {

	/**
	 * Constructor.
	 */
	public Slider() {
		super();
		setComponent(new JSlider());
	}

	/**
	 * Constructor.
	 * 
	 * @param orientation The orientation.
	 */
	public Slider(Orientation orientation) {
		super();
		setComponent(new JSlider(AWT.toAWT(orientation)));
	}

	/**
	 * Add a change listener.
	 * 
	 * @param listener The listener.
	 */
	public void addChangeListener(ChangeListener listener) {
		getComponent().addChangeListener(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JSlider getComponent() {
		return (JSlider) super.getComponent();
	}

	/**
	 * Return the orientation.
	 * 
	 * @return The orientation.
	 */
	public Orientation getOrientation() {
		return AWT.fromAWT(getComponent().getOrientation());
	}

	/**
	 * Return the current value.
	 * 
	 * @return
	 */
	public int getValue() {
		return getComponent().getValue();
	}

	/**
	 * Invert behavior.
	 * 
	 * @param inverted A boolean.
	 */
	public void setInverted(boolean inverted) {
		getComponent().setInverted(inverted);
	}

	/**
	 * Set the maximum value.
	 * 
	 * @param maximum The maximum value.
	 */
	public void setMaximum(int maximum) {
		getComponent().setMaximum(maximum);
	}

	/**
	 * Set the minimum value.
	 * 
	 * @param minimum The minimum value.
	 */
	public void setMinimum(int minimum) {
		getComponent().setMinimum(minimum);
	}

	/**
	 * Set the orientation.
	 * 
	 * @param orientation The orientation.
	 */
	public void setOrientation(Orientation orientation) {
		getComponent().setOrientation(AWT.toAWT(orientation));
	}

	/**
	 * Set the value.
	 * 
	 * @param value The value.
	 */
	public void setValue(int value) {
		getComponent().setValue(value);
	}
}
