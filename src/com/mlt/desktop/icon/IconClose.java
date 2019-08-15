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
 * Small close (X) icon.
 * 
 * @author Miquel Sas
 */
public class IconClose extends AbstractIcon {

	/**
	 * Constructor.
	 */
	public IconClose() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void paintIcon(Graphics2D g2d) {
		/* x0, y0, x1, y1 are the edges of the frame. */
		double x0 = getMarginLeft();
		double y0 = getMarginTop();
		double x1 = getWidth() - getMarginRight();
		double y1 = getHeight() - getMarginBottom();
		Stroke saveStroke = g2d.getStroke();
		g2d.setStroke(new BasicStroke(0.5f));
		Path path = new Path(Path.WIND_EVEN_ODD, 4);
		path.moveTo(x0, y0);
		path.lineTo(x1, y1);
		path.moveTo(x0, y1);
		path.lineTo(x1, y0);
		g2d.draw(path.getShape());
		g2d.setStroke(saveStroke);
	}

}
