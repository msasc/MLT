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
package com.mlt.desktop.graphic;

/**
 * A 2D point.
 *
 * @author Miquel Sas
 */
public class Point {

	/** x coord. */
	private double x;
	/** y coord. */
	private double y;

	/**
	 * Constructor.
	 *
	 * @param x x coord.
	 * @param y y coord.
	 */
	public Point(double x, double y) {
		super();
		this.x = x;
		this.y = y;
	}

	/**
	 * Return the x coord.
	 *
	 * @return The x coord.
	 */
	public double getX() {
		return x;
	}

	/**
	 * Return the y coord.
	 *
	 * @return The y coord.
	 */
	public double getY() {
		return y;
	}

}
