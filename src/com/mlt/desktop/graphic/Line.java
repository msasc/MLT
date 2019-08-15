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

import java.awt.Shape;
import java.awt.geom.Line2D;

/**
 * A line.
 *
 * @author Miquel Sas
 */
public class Line extends Drawing {

	/** Start x. */
	private double startX;
	/** Start y. */
	private double startY;
	/** End x. */
	private double endX;
	/** End y. */
	private double endY;

	/**
	 * Constructor.
	 *
	 * @param startX Start x.
	 * @param startY Start y.
	 * @param endX   End x.
	 * @param endY   End y.
	 */
	public Line(double startX, double startY, double endX, double endY) {
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;
	}

	/**
	 * Return the start x.
	 *
	 * @return The start x.
	 */
	public double getStartX() {
		return startX;
	}

	/**
	 * Return that start y.
	 *
	 * @return The start y.
	 */
	public double getStartY() {
		return startY;
	}

	/**
	 * Return the end x.
	 *
	 * @return The end x.
	 */
	public double getEndX() {
		return endX;
	}

	/**
	 * Return the end y.
	 *
	 * @return The end y.
	 */
	public double getEndY() {
		return endY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Shape getShape() {
		Line2D line = new Line2D.Double(startX, startY, endX, endY);
		return line;
	}
}
