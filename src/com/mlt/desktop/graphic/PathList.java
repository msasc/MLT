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

import java.util.ArrayList;
import java.util.List;

import com.mlt.desktop.control.Canvas;

/**
 * A list of paths aimed to be build concurrently and then applied to the
 * graphics context.
 *
 * @author Miquel Sas
 */
public class PathList {

	/** List of path items. */
	private List<PathItem> items = new ArrayList<>();

	/**
	 * Constructor.
	 */
	public PathList() {
		super();
	}

	/**
	 * Add a path.
	 * 
	 * @param path The path.
	 * @param fill A boolean indicating whether the path sould be filled.
	 * @param draw A boolean indicating whether the path border sould be drawn.
	 */
	public void add(Path path, boolean fill, boolean draw) {
		items.add(new PathItem(path, fill, draw));
	}

	/**
	 * Add a path item.
	 * 
	 * @param pathItem The path item.
	 */
	public void add(PathItem pathItem) {
		items.add(pathItem);
	}

	/**
	 * Plot the list in the graphics context.
	 * 
	 * @param gc The canvas graphics context.
	 */
	public void plot(Canvas.Context gc) {
		for (int i = 0; i < items.size(); i++) {
			items.get(i).plot(gc);
		}
	}
}
