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
import java.awt.geom.CubicCurve2D;

/**
 * A cubic curve.
 *
 * @author Miquel Sas
 */
public class CubicCurve extends Drawing {

	/** Internal AWT cubic curve. */
	private CubicCurve2D shape;

	/**
	 * Constructor.
	 *
	 * @param x1     The X coordinate for the start point of the resulting CubicCurve.
	 * @param y1     The Y coordinate for the start point of the resulting CubicCurve.
	 * @param ctrlx1 The X coordinate for the first control point of the resulting CubicCurve.
	 * @param ctrly1 The Y coordinate for the first control point of the resulting CubicCurve.
	 * @param ctrlx2 The X coordinate for the second control point of the resulting CubicCurve.
	 * @param ctrly2 The Y coordinate for the second control point of the resulting CubicCurve.
	 * @param x2     The X coordinate for the end point of the resulting CubicCurve.
	 * @param y2     The Y coordinate for the end point of the resulting CubicCurve
	 */
	public CubicCurve(double x1, double y1, double ctrlx1, double ctrly1, double ctrlx2, double ctrly2, double x2,
	double y2) {
		super();
		shape = new CubicCurve2D.Double(x1, y1, ctrlx1, ctrly1, ctrlx2, ctrly2, x2, y2);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Shape getShape() {
		return shape;
	}

}
