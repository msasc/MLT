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
package com.mlt.desktop.layout;

/**
 * Dimension.
 *
 * @author Miquel Sas
 */
public class Dimension {
	/** Width. */
	private double width;
	/** Height. */
	private double height;

	/**
	 * Constructor.
	 * 
	 * @param width  The width.
	 * @param height The height.
	 */
	public Dimension(double width, double height) {
		super();
		this.width = width;
		this.height = height;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof Dimension) {
			Dimension d = (Dimension) obj;
			return getWidth() == d.getWidth() && getHeight() == d.getHeight();
		}
		return false;
	}

	/**
	 * Return the width.
	 * 
	 * @return The width.
	 */
	public double getWidth() {
		return width;
	}

	/**
	 * Return the height.
	 * 
	 * @return The height.
	 */
	public double getHeight() {
		return height;
	}

	/**
	 * Returns a hash code value for the Dimension2D object.
	 * 
	 * @return a hash code value for the Dimension2D object.
	 */
	@Override
	public int hashCode() {
		long bits = 7L;
		bits = 31L * bits + Double.doubleToLongBits(getWidth());
		bits = 31L * bits + Double.doubleToLongBits(getHeight());
		return (int) (bits ^ (bits >> 32));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Dimension [width = " + getWidth() + ", height = " + getHeight() + "]";
	}

}
