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

import java.awt.BasicStroke;
import java.awt.Color;

import com.mlt.desktop.graphic.Line;
import com.mlt.desktop.graphic.Rectangle;
import com.mlt.util.Numbers;
import java.awt.Graphics2D;
import java.awt.Stroke;

/**
 * Grid icon.
 * 
 * @author Miquel Sas
 */
public class IconGrid extends AbstractIcon {

	/**
	 * Constructor.
	 */
	public IconGrid() {
		super();
	}

	/**
	 * Return the horizontal cell factor.
	 * 
	 * @return The horizontal cell factor.
	 */
	public double getHorizontalCellFactor() {
		return getProperties().getDouble("HORIZONTAL_FACTOR", 0.2);
	}

	/**
	 * Return the vertical cell factor.
	 * 
	 * @return The vertical cell factor.
	 */
	public double getVerticalCellFactor() {
		return getProperties().getDouble("VERTICAL_FACTOR", 0.2);
	}

	/**
	 * Set the margin factors.
	 * 
	 * @param horizontal Horizontal margin factor.
	 * @param vertical   Vertical margin factor.
	 */
	public void setCellFactors(double horizontal, double vertical) {
		getProperties().setDouble("HORIZONTAL_FACTOR", horizontal);
		getProperties().setDouble("VERTICAL_FACTOR", vertical);
	}

	/**
	 * Returns the number of cells.
	 * 
	 * @param cellSize  Cell size.
	 * @param maxSize   Maximum size (width or height)
	 * @param lineWidth Line width.
	 * @return The number of cells that fit in the maximum size.
	 */
	private int getCells(double cellSize, double maxSize, double lineWidth) {
		int cells = 0;
		while (true) {
			double cellCount = cells;
			double size = cellSize * cellCount + (cellCount + 1) * lineWidth;
			if (size > maxSize) {
				break;
			}
			cells++;
		}
		return cells;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void paintIcon(Graphics2D g2d) {
		Stroke saveStroke = g2d.getStroke();
		g2d.setStroke(new BasicStroke(0.5f));

		double x0 = getMarginLeft();
		double y0 = getMarginTop();
		double x1 = getWidth() - getMarginRight();
		double y1 = getHeight() - getMarginBottom();

		double width = x1 - x0 + 1;
		double height = y1 - y0 + 1;

		double lineWidth = getStrokeEnabled().getLineWidth();
		double cellHeight = Numbers.round(height * getVerticalCellFactor(), 0);
		int rows = getCells(cellHeight, height, lineWidth);
		double cellWidth = Numbers.round(width * getHorizontalCellFactor(), 0);
		int cols = getCells(cellWidth, width, lineWidth);
		height = cellHeight * rows + (rows + 1) * lineWidth;
		y1 = y0 + height - 1;
		width = cellWidth * cols + (cols + 1) * lineWidth;
		x1 = x0 + width - 1;

		g2d.setPaint(Color.WHITE);
		g2d.fill(new Rectangle(x0, y0, width - 1, height - 1).getShape());
		g2d.setPaint(Color.LIGHT_GRAY);
		g2d.fill(new Rectangle(x0, y0, width - 1, cellHeight).getShape());

		g2d.setPaint(Color.GRAY);
		double yx = y0;
		for (int i = 0; i < rows; i++) {
			g2d.draw(new Line(x0, yx, x1, yx).getShape());
			yx += cellHeight + 1;
		}
		g2d.draw(new Line(x0, yx, x1, yx).getShape());

		double xy = x0;
		for (int i = 0; i < cols; i++) {
			g2d.draw(new Line(xy, y0, xy, y1).getShape());
			xy += cellWidth + 1;
		}
		g2d.draw(new Line(xy, y0, xy, y1).getShape());

		g2d.setStroke(saveStroke);
	}
}
