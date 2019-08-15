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

import java.awt.Color;

import com.mlt.desktop.graphic.Line;
import com.mlt.desktop.graphic.Stroke;
import com.mlt.util.Numbers;
import java.awt.Graphics2D;

/**
 * Small dock icon.
 *
 * @author Miquel Sas
 */
public class IconDock extends AbstractIcon {

	/**
	 * Constructor.
	 */
	public IconDock() {
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

		/* Margin factor. */
		double m = 0.4;
		/* l and t are the left and top margins. */
		double l = (x1 - x0) * m;
		double t = (y1 - y0) * m;
		/* w and h are the width and height of the docking frames. */
		double w = (x1 - x0) * (1 - m);
		double h = (y1 - y0) * (1 - m);
		/*
		 * hw is the header line width, 0.1 of the height, bw is the border line width, 0.05 of width/height.
		 */
		double hw = Math.max(Numbers.round(getHeight() * 0.1, 0), 2);
		double bw = Math.max(Numbers.round(getWidth() * 0.02, 0), 1);

		/* Same color for all strokes. */
		g2d.setPaint(Color.GRAY);

		/* Top docking frame. */
		g2d.setStroke(getStroke(hw));
		g2d.draw(new Line(x0 + l, y0, x0 + l + w, y0).getShape());
		g2d.setStroke(getStroke(bw));
		g2d.draw(new Line(x0 + l + w, y0, x0 + l + w, y0 + h).getShape());
		g2d.draw(new Line(x0 + l + w, y0 + h, x0 + w, y0 + h).getShape());
		g2d.draw(new Line(x0 + l, y0 + t, x0 + l, y0).getShape());

		/* Bottom docking frame. */
		g2d.setStroke(getStroke(hw));
		g2d.draw(new Line(x0, y0 + t, x0 + w, y0 + t).getShape());
		g2d.setStroke(getStroke(bw));
		g2d.draw(new Line(x0 + w, y0 + t, x0 + w, y0 + t + h).getShape());
		g2d.draw(new Line(x0 + w, y0 + t + h, x0, y0 + t + h).getShape());
		g2d.draw(new Line(x0, y0 + t + h, x0, y0 + t).getShape());
	}

	/**
	 * Return the stroke.
	 *
	 * @param w The line width.
	 * @return The stroke.
	 */
	private Stroke getStroke(double w) {
		return new Stroke(w, Stroke.CAP_BUTT, Stroke.JOIN_MITER);
	}
}
