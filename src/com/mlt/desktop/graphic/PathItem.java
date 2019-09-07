/*
 * Copyright (C) 2018 Miquel Sas
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.mlt.desktop.graphic;

import com.mlt.desktop.control.Canvas;

/**
 * A path item to be used in a path list.
 *
 * @author Miquel Sas
 */
public class PathItem {
	/** Path. */
	private Path path;
	/** A boolean that indicates whether the path sould be filled. */
	private boolean fill;
	/** A boolean that indicates whether the path sould be drawn. */
	private boolean draw;

	/**
	 * Constructor.
	 * 
	 * @param path The path.
	 * @param fill A boolean that indicates whether the path sould be filled.
	 * @param draw A boolean that indicates whether the path sould be drawn.
	 */
	public PathItem(Path path, boolean fill, boolean draw) {
		super();
		this.path = path;
		this.fill = fill;
		this.draw = draw;
	}

	/**
	 * Plot the path item.
	 * 
	 * @param gc The graphics context.
	 */
	public void plot(Canvas.Context gc) {
		if (fill) {
			gc.fill(path);
		}
		if (draw) {
			gc.draw(path);
		}
	}

}
