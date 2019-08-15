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
 * Anchors of constraints.
 *
 * @author Miquel Sas
 */
public enum Anchor {
	/** Top. */
	TOP,
	/** Top-Left. */
	TOP_LEFT,
	/** Top-Right. */
	TOP_RIGHT,
	/** Left. */
	LEFT,
	/** Bottom. */
	BOTTOM,
	/** Bottom-Left. */
	BOTTOM_LEFT,
	/** Bottom-Right. */
	BOTTOM_RIGHT,
	/** Right. */
	RIGHT,
	/** Center. */
	CENTER;

	/**
	 * Check whether this position is top.
	 * 
	 * @return A boolean.
	 */
	public boolean isTop() {
		return this == TOP;
	}

	/**
	 * Check whether this position is left.
	 * 
	 * @return A boolean.
	 */
	public boolean isLeft() {
		return this == LEFT;
	}

	/**
	 * Check whether this position is bottom.
	 * 
	 * @return A boolean.
	 */
	public boolean isBottom() {
		return this == BOTTOM;
	}

	/**
	 * Check whether this position is right.
	 * 
	 * @return A boolean.
	 */
	public boolean isRight() {
		return this == RIGHT;
	}

	/**
	 * Check whether this position is center.
	 * 
	 * @return A boolean.
	 */
	public boolean isCenter() {
		return this == CENTER;
	}

}
