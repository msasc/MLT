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

import java.awt.BasicStroke;
import java.awt.Shape;

/**
 * A basic stroke that uses double values.
 *
 * @author Miquel Sas
 */
public class Stroke implements java.awt.Stroke {

	/** Shortcut to BasicStroke.CAP_BUTT. */
	public static final int CAP_BUTT = BasicStroke.CAP_BUTT;
	/** Shortcut to BasicStroke.CAP_ROUND. */
	public static final int CAP_ROUND = BasicStroke.CAP_ROUND;
	/** Shortcut to BasicStroke.CAP_SQUARE. */
	public static final int CAP_SQUARE = BasicStroke.CAP_SQUARE;
	/** Shortcut to BasicStroke.JOIN_BEVEL. */
	public static final int JOIN_BEVEL = BasicStroke.JOIN_BEVEL;
	/** Shortcut to BasicStroke.JOIN_MITER. */
	public static final int JOIN_MITER = BasicStroke.JOIN_MITER;
	/** Shortcut to BasicStroke.JOIN_ROUND. */
	public static final int JOIN_ROUND = BasicStroke.JOIN_ROUND;

	/** Internal basic stroke. */
	private BasicStroke stroke;

	/**
	 * Constructor.
	 */
	public Stroke() {
		this(1);
	}

	/**
	 * Constructor.
	 *
	 * @param lineWidth Line width.
	 */
	public Stroke(double lineWidth) {
		this(lineWidth, CAP_SQUARE, JOIN_MITER);
	}

	/**
	 * Constructor.
	 *
	 * @param lineWidth Line width.
	 * @param cap       Cap.
	 * @param join      Join.
	 */
	public Stroke(double lineWidth, int cap, int join) {
		this(lineWidth, cap, join, 10, null, 0);
	}

	/**
	 * Constructor.
	 *
	 * @param lineWidth Line width.
	 * @param cap       Cap.
	 * @param join      Join.
	 * @param dash      Dash array.
	 */
	public Stroke(double lineWidth, int cap, int join, double[] dash) {
		this(lineWidth, cap, join, 10, dash, 0);
	}

	/**
	 * Constructor.
	 *
	 * @param lineWidth  Line width.
	 * @param cap        Cap.
	 * @param join       Join.
	 * @param miterLimit Miter limit.
	 * @param dash       Dash array.
	 * @param dashPhase  Dash phase.
	 */
	public Stroke(double lineWidth, int cap, int join, double miterLimit, double[] dash, double dashPhase) {

		float fwidth = Double.valueOf(lineWidth).floatValue();
		float fmiterLimit = Double.valueOf(miterLimit).floatValue();
		float[] fdash = null;
		if (dash != null) {
			fdash = new float[dash.length];
			for (int i = 0; i < dash.length; i++) {
				fdash[i] = Double.valueOf(dash[i]).floatValue();
			}
		}
		float fdashPhase = Double.valueOf(dashPhase).floatValue();
		stroke = new BasicStroke(fwidth, cap, join, fmiterLimit, fdash, fdashPhase);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Shape createStrokedShape(Shape p) {
		return stroke.createStrokedShape(p);
	}

	public float getLineWidth() {
		return stroke.getLineWidth();
	}

	public int getEndCap() {
		return stroke.getEndCap();
	}

	public int getLineJoin() {
		return stroke.getLineJoin();
	}

	public float getMiterLimit() {
		return stroke.getMiterLimit();
	}

	public float[] getDashArray() {
		return stroke.getDashArray();
	}

	public float getDashPhase() {
		return stroke.getDashPhase();
	}

}
