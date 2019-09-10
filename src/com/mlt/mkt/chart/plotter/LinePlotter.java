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
package com.mlt.mkt.chart.plotter;

import java.awt.Color;
import java.awt.Paint;
import java.awt.RenderingHints;

import com.mlt.desktop.control.Canvas;
import com.mlt.desktop.graphic.Path;
import com.mlt.desktop.graphic.Stroke;
import com.mlt.mkt.chart.DataContext;
import com.mlt.mkt.data.Data;
import com.mlt.mkt.data.DataList;
import com.mlt.util.Colors;
import com.mlt.util.Numbers;

/**
 * A line plotter.
 *
 * @author Miquel Sas
 */
public class LinePlotter extends DataPlotter {

	/** The line stroke. */
	private Stroke stroke = new Stroke(0.5);
	/** The line color. */
	private Paint linePaint = Colors.BLACK;

	/**
	 * Constructor with the zero index.
	 */
	public LinePlotter() {
		super();
		setIndex(0);
	}

	/**
	 * Constructor.
	 * 
	 * @param index The data index.
	 */
	public LinePlotter(int index) {
		super();
		setIndex(index);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void plot(Canvas.Context gc, DataList dataList, int startIndex, int endIndex) {

		DataContext dc = getContext();
		Path path = new Path();
		path.setStroke(stroke);
		path.setDrawPaint(linePaint);
		path.addHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		double lastX = Numbers.MIN_DOUBLE;
		double lastY = Numbers.MIN_DOUBLE;
		Color lastColor = null;
		for (int index = startIndex; index <= endIndex; index++) {
			if (index >= 0 && index < dataList.size()) {
				Data data = dataList.get(index);
				double value = data.getValue(getIndex());
				double x = dc.getCenterCoordinateX(dc.getCoordinateX(index));
				double y = dc.getCoordinateY(value);

				// First.
				if (lastX == Numbers.MIN_DOUBLE && lastY == Numbers.MIN_DOUBLE) {
					path.moveTo(x, y);
				} else {
					boolean bullish = (y > lastY);
					Color color = null;
					if (dataList.isOdd(index)) {
						if (bullish) {
							color = getColorBullishOdd();
						} else {
							color = getColorBearishOdd();
						}
					} else {
						if (bullish) {
							color = getColorBullishEven();
						} else {
							color = getColorBearishEven();
						}
					}
					if (lastColor == null) {
						lastColor = color;
					}
					if (color.equals(lastColor)) {
						path.lineTo(x, y);
					} else {
						path.setDrawPaint(lastColor);
						gc.draw(path);

						path.reset();
						path.setDrawPaint(color);
						path.moveTo(lastX, lastY);
						path.lineTo(x, y);
					}
					lastColor = color;
				}
				lastX = x;
				lastY = y;
			}
		}
		path.setDrawPaint(lastColor);
		gc.draw(path);
	}

}
