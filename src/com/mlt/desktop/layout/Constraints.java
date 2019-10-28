/*
 * Copyright (C) 2017 Miquel Sas
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
 * Helper class to manage layout constraints.
 *
 * @author Miquel Sas
 */
public class Constraints {

	/** Anchor. */
	private Anchor anchor;
	/** Fill. */
	private Fill fill;
	/** Grid x. */
	private int x;
	/** Grid y. */
	private int y;
	/** Width or number of horizontal cells. */
	private int width;
	/** Height or number of vertical cells. */
	private int height;
	/** Weight x. */
	private double weightx;
	/** Weight y. */
	private double weighty;
	/** Insets. */
	private Insets insets;

	/**
	 * Constructor.
	 *
	 * @param anchor Anchor.
	 * @param fill   Fill.
	 * @param x      Grid x.
	 * @param y      Grid y.
	 * @param insets Insets.
	 */
	public Constraints(Anchor anchor, Fill fill, int x, int y, Insets insets) {
		super();
		this.anchor = anchor;
		this.fill = fill;
		this.x = x;
		this.y = y;
		this.width = 1;
		this.height = 1;
		this.weightx = (fill == Fill.HORIZONTAL || fill == Fill.BOTH ? 1 : 0);
		this.weighty = (fill == Fill.VERTICAL || fill == Fill.BOTH ? 1 : 0);
		this.insets = insets;
	}

	/**
	 * Constructor.
	 *
	 * @param anchor Anchor.
	 * @param fill   Fill.
	 * @param x      Grid x.
	 * @param y      Grid y.
	 * @param width  Width or number of horizontal cells.
	 * @param height Height or number of vertical cells.
	 * @param insets Insets.
	 */
	public Constraints(Anchor anchor, Fill fill, int x, int y, int width, int height, Insets insets) {
		super();
		this.anchor = anchor;
		this.fill = fill;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.weightx = (fill == Fill.HORIZONTAL || fill == Fill.BOTH ? 1 : 0);
		this.weighty = (fill == Fill.VERTICAL || fill == Fill.BOTH ? 1 : 0);
		this.insets = insets;
	}

	/**
	 * Constructor.
	 *
	 * @param anchor  Anchor.
	 * @param fill    Fill.
	 * @param x       Grid x.
	 * @param y       Grid y.
	 * @param width   Width or number of horizontal cells.
	 * @param height  Height or number of vertical cells.
	 * @param weightx Weight x.
	 * @param weighty Weight y.
	 * @param insets  Insets.
	 */
	public Constraints(
		Anchor anchor,
		Fill fill, 
		int x, 
		int y, 
		int width, 
		int height, 
		double weightx, 
		double weighty, 
		Insets insets) {

		super();

		this.anchor = anchor;
		this.fill = fill;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.weightx = weightx;
		this.weighty = weighty;
		this.insets = insets;
	}

	/**
	 * Return the anchor.
	 *
	 * @return The anchor.
	 */
	public Anchor getAnchor() {
		return anchor;
	}

	/**
	 * Return the fill.
	 *
	 * @return The fill.
	 */
	public Fill getFill() {
		return fill;
	}

	/**
	 * Return the x coordinate.
	 *
	 * @return The x coord.
	 */
	public int getX() {
		return x;
	}

	/**
	 * Return the y coord.
	 *
	 * @return The y coord.
	 */
	public int getY() {
		return y;
	}

	/**
	 * Return the height.
	 *
	 * @return The height.
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Return the width.
	 *
	 * @return The width.
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Return the x weight.
	 *
	 * @return The x weight.
	 */
	public double getWeightx() {
		return weightx;
	}

	/**
	 * Return the y weight.
	 *
	 * @return The y weight.
	 */
	public double getWeighty() {
		return weighty;
	}

	/**
	 * Return the insets.
	 *
	 * @return The insets.
	 */
	public Insets getInsets() {
		return insets;
	}

}
