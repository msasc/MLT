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
 * Directions.
 *
 * @author Miquel Sas
 */
public enum Direction {
	/** None. */
	NONE,
	/** Up. */
	UP,
	/** Down. */
	DOWN,
	/** Left. */
	LEFT,
	/** Right. */
	RIGHT;

	/**
	 * Check whether this direction is no direction.
	 *
	 * @return A boolean.
	 */
	public boolean isNone() {
		return this == NONE;
	}

	/**
	 * Check whether this direction is up.
	 *
	 * @return A boolean.
	 */
	public boolean isUp() {
		return this == UP;
	}

	/**
	 * Check whether this direction is down.
	 *
	 * @return A boolean.
	 */
	public boolean isDown() {
		return this == DOWN;
	}

	/**
	 * Check whether this direction is left.
	 *
	 * @return A boolean.
	 */
	public boolean isLeft() {
		return this == LEFT;
	}

	/**
	 * Check whether this direction is right.
	 *
	 * @return A boolean.
	 */
	public boolean isRight() {
		return this == RIGHT;
	}

	/**
	 * Check whether this direction is horizontal.
	 *
	 * @return A boolean.
	 */
	public boolean isHorizontal() {
		return (this == LEFT || this == RIGHT);
	}

	/**
	 * Check whether this direction is vertical.
	 *
	 * @return A boolean.
	 */
	public boolean isVertical() {
		return (this == LEFT || this == RIGHT);
	}
}
