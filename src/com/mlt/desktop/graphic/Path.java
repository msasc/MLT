/*
 * Copyright (C) 2018 Miquel Sas
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.mlt.desktop.graphic;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;

/**
 * A general path.
 *
 * @author Miquel Sas
 */
public class Path extends Drawing {

	/** Winding rule non zero. */
	public static final int WIND_NON_ZERO = Path2D.WIND_NON_ZERO;
	/** Winding rule even odd. */
	public static final int WIND_EVEN_ODD = Path2D.WIND_EVEN_ODD;

	/** Internal general path. */
	private GeneralPath path;

	/**
	 * Default constructor, with winding rule non zero and initial capacity of 20.
	 */
	public Path() {
		super();
		this.path = new GeneralPath(WIND_NON_ZERO, 20);
	}

	/**
	 * Constructor with winding rule and initial capacity.
	 *
	 * @param rule     Winding rule.
	 * @param capacity Initial capacity.
	 */
	public Path(int rule, int capacity) {
		super();
		this.path = new GeneralPath(rule, capacity);
	}

	/**
	 * Appends the geometry of the specified drawing object to the path, possibly
	 * connecting the new geometry to the
	 * existing path segments with a line segment.
	 *
	 * @param drawing The drawing.
	 * @param connect A boolean.
	 */
	public void append(Drawing drawing, boolean connect) {
		path.append(drawing.getShape(), connect);
	}

	/**
	 * Resets the path to empty.
	 */
	public void clear() {
		path.reset();
	}

	/**
	 * Closes the current subpath by drawing a straight line back to the coordinates
	 * of the last moveTo.
	 */
	public void closePath() {
		path.closePath();
	}

	/**
	 * Adds a curved segment, defined by three new points, to the path by drawing a
	 * Bézier curve that intersects both
	 * the current coordinates and the specified coordinates (x3,y3), using the
	 * specified points (x1,y1) and (x2,y2) as
	 * Bézier control points.
	 *
	 * @param x1 The X coordinate of the first Bézier control point.
	 * @param y1 The Y coordinate of the first Bézier control point.
	 * @param x2 The X coordinate of the second Bézier control point.
	 * @param y2 The Y coordinate of the second Bézier control point.
	 * @param x3 The X coordinate of the third Bézier control point.
	 * @param y3 The Y coordinate of the third Bézier control point.
	 */
	public void curveTo(double x1, double y1, double x2, double y2, double x3, double y3) {
		path.curveTo(x1, y1, x2, y2, x3, y3);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Shape getShape() {
		return path;
	}

	/**
	 * Check whether this path is empty.
	 * 
	 * @return A boolean.
	 */
	public boolean isEmpty() {
		return path.getPathIterator(null).isDone();
	}

	/**
	 * Adds a point to the path by drawing a straight line.
	 *
	 * @param x x coord.
	 * @param y y coord.
	 */
	public void lineTo(double x, double y) {
		path.lineTo(x, y);
	}

	/**
	 * Adds a point to the path by drawing a straight line.
	 *
	 * @param p The point.
	 */
	public void lineTo(Point p) {
		path.lineTo(p.getX(), p.getY());
	}

	/**
	 * Adds a point to the path by moving to the specified coordinates.
	 *
	 * @param x x coord.
	 * @param y y coord.
	 */
	public void moveTo(double x, double y) {
		path.moveTo(x, y);
	}

	/**
	 * Adds a point to the path by moving to the specified coordinates.
	 *
	 * @param p The point.
	 */
	public void moveTo(Point p) {
		path.moveTo(p.getX(), p.getY());
	}

	/**
	 * Adds a curved segment, defined by two new points, to the path by drawing a
	 * Quadratic curve that intersects both
	 * the current coordinates and the specified coordinates (x2,y2), using the
	 * specified point (x1,y1) as a quadratic
	 * parametric control point.
	 *
	 * @param x1 x coord of the quadratic control point.
	 * @param y1 y coord of the quadratic control point.
	 * @param x2 x coord of the end point.
	 * @param y2 y coord of the end point.
	 */
	public void quadTo(double x1, double y1, double x2, double y2) {
		path.quadTo(x1, y1, x2, y2);
	}
}
