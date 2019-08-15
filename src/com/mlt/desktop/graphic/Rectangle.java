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
import java.awt.geom.Rectangle2D;

import com.mlt.desktop.layout.Dimension;
import com.mlt.desktop.layout.Insets;

/**
 * A rectangle.
 *
 * @author Miquel Sas
 */
public class Rectangle extends Drawing {

	/** Empty rectangle. */
	public static final Rectangle EMPTY = new Rectangle(0, 0, 0, 0);

	/** x coordinate. */
	private double x;
	/** y coordinate. */
	private double y;
	/** Width. */
	private double width;
	/** Height. */
	private double height;

	/**
	 * Constructor.
	 *
	 * @param x      x coord.
	 * @param y      y coord.
	 * @param width  Width.
	 * @param height Height.
	 */
	public Rectangle(double x, double y, double width, double height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	/**
	 * Return a rectangle that is the extension of this rectangle applying the insets. If the insets are positive, the
	 * result rectangle will be greater.
	 *
	 * @param insets The insets.
	 * @return The extended rectangle.
	 */
	public Rectangle extend(Insets insets) {
		double x = getX() - insets.getLeft();
		double y = getY() - insets.getTop();
		double width = insets.getLeft() + getWidth() + insets.getRight();
		double height = insets.getTop() + getHeight() + insets.getBottom();
		return new Rectangle(x, y, width, height);
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
	 * {@inheritDoc}
	 */
	@Override
	public Shape getShape() {
		Rectangle2D rect = new Rectangle2D.Double(x, y, width, height);
		return rect;
	}

	/**
	 * Return this rectangle size.
	 *
	 * @return The size.
	 */
	public Dimension getSize() {
		return new Dimension(getWidth(), getHeight());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return getClass().getName()
			+ "[x=" + x +
			",y=" + y +
			",w=" + width +
			",h=" + height + "]";
	}
}
