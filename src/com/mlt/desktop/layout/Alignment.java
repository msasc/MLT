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
 * Alignments.
 *
 * @author Miquel Sas
 */
public enum Alignment {
	/** Top. */
	TOP,
	/** Left. */
	LEFT,
	/** Center. */
	CENTER,
	/** Bottom. */
	BOTTOM,
	/** Right. */
	RIGHT;

	/**
	 * Check whether this horizontal alignment is left alignment.
	 *
	 * @return A boolean.
	 */
	public boolean isLeft() {
		return this == LEFT;
	}

	/**
	 * Check whether this alignment is center alignment.
	 *
	 * @return A boolean.
	 */
	public boolean isCenter() {
		return this == CENTER;
	}

	/**
	 * Check whether this horizontal alignment is right alignment.
	 *
	 * @return A boolean.
	 */
	public boolean isRight() {
		return this == RIGHT;
	}

	/**
	 * Check whether this vertical alignment is top.
	 *
	 * @return A boolean.
	 */
	public boolean isTop() {
		return this == TOP;
	}

	/**
	 * Check whether this vertical alignment is bottom.
	 *
	 * @return A boolean.
	 */
	public boolean isBottom() {
		return this == BOTTOM;
	}

	/**
	 * Check whether this alignment is horizontal.
	 *
	 * @return A boolean.
	 */
	public boolean isHorizontal() {
		return (this == LEFT || this == CENTER || this == RIGHT);
	}

	/**
	 * Check whether this alignment is vertical.
	 *
	 * @return A boolean.
	 */
	public boolean isVertical() {
		return (this == TOP || this == CENTER || this == BOTTOM);
	}
}
