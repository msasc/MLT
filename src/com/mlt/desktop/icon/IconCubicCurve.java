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

import java.awt.Graphics2D;

import com.mlt.desktop.graphic.CubicCurve;
import com.mlt.desktop.graphic.Line;

/**
 * Small cubic curve icon.
 * 
 * @author Miquel Sas
 */
public class IconCubicCurve extends AbstractIcon {

	/**
	 * Constructor.
	 */
	public IconCubicCurve() {
		super();
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

		double xCenter = x0 + (x1 - x0) / 2;
		double yCenter = y1 + (y2 - y1) / 2;
		Line hline = new Line(x0, yCenter, x1, yCenter);
		Line vline = new Line(xCenter, y0, xCenter, y2);
		CubicCurve cc = new CubicCurve(x3, y3, x0, y0, x2, y2, x1, y1);

		g2d.draw(hline.getShape());
		g2d.draw(vline.getShape());
		g2d.draw(cc.getShape());
	}
}
