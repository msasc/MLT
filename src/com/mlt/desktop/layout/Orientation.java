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

import javax.swing.SwingConstants;

/**
 * Orientation.
 *
 * @author Miquel Sas
 */
public enum Orientation {
	/** The horizontal orientation */
	HORIZONTAL,
	/** The vertical orientation */
	VERTICAL;

	/**
	 * Check whether this orientation is horizontal.
	 *
	 * @return A boolean.
	 */
	public boolean isHorizontal() {
		return this == HORIZONTAL;
	}

	/**
	 * Check whether this orientation is vertical.
	 *
	 * @return A boolean.
	 */
	public boolean isVertical() {
		return this == Orientation.VERTICAL;
	}

	/**
	 * Convert.
	 *
	 * @return AWT orientation.
	 */
	public int toAWT() {
		switch (this) {
		case HORIZONTAL:
			return SwingConstants.HORIZONTAL;
		case VERTICAL:
			return SwingConstants.VERTICAL;
		}
		return SwingConstants.HORIZONTAL;
	}

}
