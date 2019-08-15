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

package app.mlt.ml.mnist.viewer;

import java.awt.Color;

import com.mlt.desktop.control.Canvas;
import com.mlt.desktop.graphic.Line;
import com.mlt.desktop.graphic.Rectangle;
import com.mlt.desktop.graphic.Stroke;
import com.mlt.util.Numbers;

/**
 * A canvas to paint an image of rows/columns, from a double[] normalized input.
 *
 * @author Miquel Sas
 */
public class ImageCanvas extends Canvas {

	/** Rows. */
	private int rows;
	/** Columns. */
	private int columns;
	/** Data vector. */
	private double[] image;
	/** A boolean that indicates whether to draw the grid. */
	private boolean drawGrid = true;
	/** Line color. */
	private Color lineColor = new Color(160, 160, 160);
	/** Line width. */
	private float lineWidth = 1.0f; // Always 1.0
	/** Draw factor. */
	private double drawFactor = 0.95;

	/**
	 * Constructor.
	 * 
	 * @param rows    Rows.
	 * @param columns Columns.
	 */
	public ImageCanvas(int rows, int columns) {
		super();
		this.rows = rows;
		this.columns = columns;
	}

	/**
	 * paint the image.
	 * 
	 * @param image The image, as a vector.
	 */
	public void paint(double[] image) {
		if (image.length != rows * columns) {
			throw new IllegalArgumentException("Invalid image size");
		}
		this.image = image;
		repaint();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void paintCanvas(Context gc) {

		/* Clear background. */
		gc.clear();

		/* Images still not loaded. */
		if (image == null) {
			return;
		}

		/* Canvas dimension. */
		double canvasWidth = getSize().getWidth();
		double canvasHeight = getSize().getHeight();

		/* Draw are dimension, apply draw factor. */
		double drawWidth = canvasWidth * drawFactor;
		double drawHeight = canvasHeight * drawFactor;

		/* Moving edges. */
		double x1, x2, y1, y2;

		/* Draw with grid lines. */
		if (drawGrid) {

			/* Cell dimension (square). */
			double cellWidth = (drawWidth - ((columns + 1) * lineWidth)) / columns;
			double cellHeight = (drawHeight - ((rows + 1) * lineWidth)) / rows;
			double cellSize = Numbers.round(Math.min(cellWidth, cellHeight), 0);

			/* Recalculate draw dimension using the square cell size. */
			drawWidth = (cellSize * columns) + ((columns + 1) * lineWidth);
			drawHeight = (cellSize * rows) + ((rows + 1) * lineWidth);

			/* Origin (x=left, y=top) of the frame to draw. */
			double x = Numbers.round((canvasWidth - drawWidth) / 2, 0);
			double y = Numbers.round((canvasHeight - drawHeight) / 2, 0);

			/* Stroke. */
			Stroke stroke = new Stroke(lineWidth, Stroke.CAP_SQUARE, Stroke.JOIN_MITER);

			/* Draw the grid horizontal lines. */
			x1 = x + Math.floor(lineWidth / 2);
			y1 = y + Math.floor(lineWidth / 2);
			x2 = x1 + drawWidth - lineWidth;
			for (int r = 0; r <= rows; r++) {
				Line line = new Line(x1, y1, x2, y1);
				line.setStroke(stroke);
				line.setDrawPaint(lineColor);
				gc.draw(line);
				y1 += (cellSize + lineWidth);
			}

			/* Draw the grid vertical lines. */
			x1 = x + Math.floor(lineWidth / 2);
			y1 = y + Math.floor(lineWidth / 2);
			y2 = y1 + drawHeight - lineWidth;
			for (int c = 0; c <= columns; c++) {
				Line line = new Line(x1, y1, x1, y2);
				line.setStroke(stroke);
				line.setDrawPaint(lineColor);
				gc.draw(line);
				x1 += (cellSize + lineWidth);
			}

			/* Draw image pixels. */
			y1 = y + lineWidth - (Numbers.isOdd(lineWidth) ? 1 : 0);
			for (int r = 0; r < rows; r++) {
				x1 = x + lineWidth - (Numbers.isOdd(lineWidth) ? 1 : 0);
				for (int c = 0; c < columns; c++) {
					int i = r * columns + c;
					float f = Double.valueOf(image[i]).floatValue();
					Color color = new Color(f, f, f);
					Rectangle rect = new Rectangle(x1, y1, cellSize, cellSize);
					rect.setFillPaint(color);
					gc.fill(rect);
					x1 += cellSize + lineWidth;
				}
				y1 += cellSize + lineWidth;
			}

		} else {

			/* Cell dimension (square). */
			double cellWidth = Numbers.round(drawWidth / columns, 0);
			double cellHeight = Numbers.round(drawHeight / rows, 0);
			double cellSize = Math.min(cellWidth, cellHeight);

			/* Recalculate draw dimension using the square cell size. */
			drawWidth = (cellSize * columns);
			drawHeight = (cellSize * rows);

			/* Origin (x=left, y=top) of the frame to draw. */
			double x = Numbers.round((canvasWidth - drawWidth) / 2, 0);
			double y = Numbers.round((canvasHeight - drawHeight) / 2, 0);

			/* Draw image pixels. */
			y1 = y - 1;
			for (int r = 0; r < rows; r++) {
				x1 = x - 1;
				for (int c = 0; c < columns; c++) {
					int i = r * columns + c;
					float f = Double.valueOf(image[i]).floatValue();
					Color color = new Color(f, f, f);
					Rectangle rect = new Rectangle(x1, y1, cellSize, cellSize);
					rect.setFillPaint(color);
					gc.fill(rect);
					x1 += cellSize;
				}
				y1 += cellSize;
			}

		}
	}

	/**
	 * Set the draw factor to dimension the draw area in the available size.
	 * 
	 * @param drawFactor The draw factor.
	 */
	public void setDrawFactor(double drawFactor) {
		this.drawFactor = drawFactor;
	}

	/**
	 * Set whether the grid should be drawn.
	 * 
	 * @param drawGrid A boolean.
	 */
	public void setDrawGrid(boolean drawGrid) {
		this.drawGrid = drawGrid;
	}

	/**
	 * Set the line color.
	 * 
	 * @param lineColor The line color.
	 */
	public void setLineColor(Color lineColor) {
		this.lineColor = lineColor;
	}

	/**
	 * Set the line width.
	 * 
	 * @param lineWidth The line width.
	 */
	public void setLineWidth(float lineWidth) {
		this.lineWidth = (float) Numbers.round(lineWidth, 0);
	}

}
