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
 * Insets.
 *
 * @author Miquel Sas
 */
public class Insets {

	/** Empty insets. */
	public static final Insets EMPTY = new Insets(0, 0, 0, 0);

	/** Top. */
	private double top;
	/** Left. */
	private double left;
	/** Bottom. */
	private double bottom;
	/** Right. */
	private double right;

	/**
	 * Constructor.
	 *
	 * @param top    Top.
	 * @param left   Left.
	 * @param bottom Bottom.
	 * @param right  Right.
	 */
	public Insets(double top, double left, double bottom, double right) {
		super();
		this.top = top;
		this.left = left;
		this.bottom = bottom;
		this.right = right;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof Insets) {
			Insets other = (Insets) obj;
			return top == other.top && right == other.right && bottom == other.bottom && left == other.left;
		}
		return false;
	}

	/**
	 * Return the top.
	 *
	 * @return Top.
	 */
	public double getTop() {
		return top;
	}

	/**
	 * Return the left.
	 *
	 * @return Left.
	 */
	public double getLeft() {
		return left;
	}

	/**
	 * Return the bottom.
	 *
	 * @return Bottom.
	 */
	public double getBottom() {
		return bottom;
	}

	/**
	 * Return the right.
	 *
	 * @return Right.
	 */
	public double getRight() {
		return right;
	}

	/**
	 * Invert this insets.
	 *
	 * @return The inverted insets.
	 */
	public Insets invert() {
		return new Insets(-top, -left, -bottom, -right);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		long bits = 17L;
		bits = 37L * bits + Double.doubleToLongBits(top);
		bits = 37L * bits + Double.doubleToLongBits(right);
		bits = 37L * bits + Double.doubleToLongBits(bottom);
		bits = 37L * bits + Double.doubleToLongBits(left);
		return (int) (bits ^ (bits >> 32));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Insets [top=" + top + ", left=" + left + ", bottom=" + bottom + ", right=" + right + "]";
	}
}
