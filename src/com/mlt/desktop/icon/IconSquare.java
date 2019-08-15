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
package com.mlt.desktop.icon;

import com.mlt.desktop.graphic.Path;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;

/**
 * Small square icon.
 * 
 * @author Miquel Sas
 */
public class IconSquare extends AbstractIcon {

	/**
	 * Constructor.
	 */
	public IconSquare() {
		super();
	}

	/**
	 * Check whether the arrow is empty (not filled).
	 * 
	 * @return A boolean.
	 */
	public boolean isEmpty() {
		return getProperties().getBoolean("EMPTY", true);
	}

	/**
	 * Set whether the arrow is empty (not filled).
	 * 
	 * @param empty A boolean.
	 */
	public void setEmpty(boolean empty) {
		getProperties().setBoolean("EMPTY", empty);
	}

	/**
	 * Check whether the arrow is filled.
	 * 
	 * @return A boolean.
	 */
	public boolean isFilled() {
		return !isEmpty();
	}

	/**
	 * Set whether the arrow is filled.
	 * 
	 * @param filled A boolean.
	 */
	public void setFilled(boolean filled) {
		setEmpty(!filled);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void paintIcon(Graphics2D g2d) {
		double x0 = getMarginLeft();
		double y0 = getMarginTop();
		double x1 = getWidth() - getMarginRight();
		double y1 = getMarginTop();
		double x2 = getWidth() - getMarginRight();
		double y2 = getHeight() - getMarginBottom();
		double x3 = getMarginLeft();
		double y3 = getHeight() - getMarginBottom();
		Stroke saveStroke = g2d.getStroke();
		g2d.setStroke(new BasicStroke(0.5f));
		Path path = new Path(Path.WIND_EVEN_ODD, 4);
		path.moveTo(x0, y0);
		path.lineTo(x1, y1);
		path.lineTo(x2, y2);
		path.lineTo(x3, y3);
		path.lineTo(x0, y0);
		if (isEmpty()) {
			g2d.draw(path.getShape());
		} else {
			g2d.fill(path.getShape());
		}
		g2d.setStroke(saveStroke);
	}
}
