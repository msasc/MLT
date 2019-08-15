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

import com.mlt.desktop.layout.Direction;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;

/**
 * An arrow icon, with or without leg or shadow.
 *
 * @author Miquel Sas
 */
public class IconArrow extends AbstractIcon {

	/**
	 * Constructor.
	 */
	public IconArrow() {
		super();
	}

	/**
	 * Return the direction.
	 * 
	 * @return The direction.
	 */
	public Direction getDirection() {
		return (Direction) getProperties().getObject("DIRECTION", Direction.NONE);
	}

	/**
	 * Set the direction.
	 * 
	 * @param direction The direction.
	 */
	public void setDirection(Direction direction) {
		getProperties().setObject("DIRECTION", direction);
	}

	/**
	 * Check whether the arrow is closed.
	 * 
	 * @return A boolean.
	 */
	public boolean isClosed() {
		return getProperties().getBoolean("CLOSED", false);
	}

	/**
	 * Set whether the arrow is closed.
	 * 
	 * @param closed A boolean.
	 */
	public void setClosed(boolean closed) {
		getProperties().setBoolean("CLOSED", closed);
	}

	/**
	 * Check whether the arrow is opened..
	 * 
	 * @return A boolean.
	 */
	public boolean isOpened() {
		return !isClosed();
	}

	/**
	 * Set whether the arrow is opened.
	 * 
	 * @param opened A boolean.
	 */
	public void setOpened(boolean opened) {
		setClosed(!opened);
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
		double x0 = 0, y0 = 0, x1 = 0, y1 = 0, x2 = 0, y2 = 0;
		switch (getDirection()) {
		case LEFT:
			x0 = getMarginLeft();
			y0 = getMarginTop() + ((getHeight() - getMarginTop() - getMarginBottom()) / 2);
			x1 = getWidth() - getMarginRight();
			y1 = getMarginTop();
			x2 = x1;
			y2 = getHeight() - getMarginBottom();
			break;
		case RIGHT:
			x0 = getWidth() - getMarginRight();
			y0 = getMarginTop() + ((getHeight() - getMarginTop() - getMarginBottom()) / 2);
			x1 = getMarginLeft();
			y1 = getMarginTop();
			x2 = x1;
			y2 = getHeight() - getMarginBottom();
			break;
		default:
		}
		paintArrow(g2d, x0, y0, x1, y1, x2, y2);
	}

	/**
	 * Paint an arrow with 3 points.
	 * 
	 * @param g2d Graphics2D.
	 * @param x0  Point 0 x.
	 * @param y0  Point 0 y.
	 * @param x1  Point 1 x.
	 * @param y1  Point 1 y.
	 * @param x2  Point 2 x.
	 * @param y2  Point 2 y.
	 */
	private void paintArrow(Graphics2D g2d, double x0, double y0, double x1, double y1, double x2, double y2) {
		Stroke saveStroke = g2d.getStroke();
		g2d.setStroke(new BasicStroke(0.5f));
		Path path = new Path(Path.WIND_EVEN_ODD, 3);
		path.moveTo(x1, y1);
		path.lineTo(x0, y0);
		path.lineTo(x2, y2);
		if (isClosed()) {
			path.lineTo(x1, y1);
		}
		if (isEmpty()) {
			g2d.draw(path.getShape());
		} else {
			g2d.fill(path.getShape());
		}
		g2d.setStroke(saveStroke);
	}
}
