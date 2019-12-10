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
package com.mlt.mkt.chart.plotter.data;

import java.awt.Color;

import com.mlt.desktop.control.Canvas;
import com.mlt.desktop.graphic.Path;
import com.mlt.desktop.graphic.Stroke;
import com.mlt.mkt.chart.DataContext;
import com.mlt.mkt.chart.plotter.DataPlotter;
import com.mlt.mkt.data.OHLC;
import com.mlt.mkt.data.Data;
import com.mlt.mkt.data.DataList;

/**
 * Plotter of bars.
 *
 * @author Miquel Sas
 */
public class BarPlotter extends DataPlotter {

	/** Stroke . */
	private Stroke stroke = new Stroke(1.0);

	/**
	 * Constructor.
	 */
	public BarPlotter() {
		this(OHLC.OPEN, OHLC.HIGH, OHLC.LOW, OHLC.CLOSE);
	}

	/**
	 * Constructor indicating the indexes of the open, high, low and close values.
	 * 
	 * @param open  Open index.
	 * @param high  High index.
	 * @param low   Low index.
	 * @param close Close index.
	 */
	public BarPlotter(int open, int high, int low, int close) {
		super("bar");
		setIndexes(new int[] { open, high, low, close });
	}

	/**
	 * Plot the index.
	 *
	 * @param gc       The canvas context.
	 * @param dataList Data list.
	 * @param index    Index.
	 */
	private void plot(Canvas.Context gc, DataList dataList, int index) {

		/* Data. */
		Data data = dataList.get(index);
		double open = data.getValue(OHLC.OPEN);
		double high = data.getValue(OHLC.HIGH);
		double low = data.getValue(OHLC.LOW);
		double close = data.getValue(OHLC.CLOSE);
		boolean bullish = OHLC.isBullish(data);

		/* Context. */
		DataContext dc = getContext();

		/* The X coordinate to start painting. */
		double x = dc.getCoordinateX(index);

		/* And the Y coordinate for each value. */
		double openY = dc.getCoordinateY(open);
		double highY = dc.getCoordinateY(high);
		double lowY = dc.getCoordinateY(low);
		double closeY = dc.getCoordinateY(close);

		// The X coordinate of the vertical line, either the candle.
		double barWidth = getDataWidth(dc);
		double verticalLineX = dc.getCenterCoordinateX(x);

		/* The color to draw. */
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

		Path path = new Path();
		path.setStroke(stroke);
		path.setDrawPaint(color);

		/* The vertical bar line. */
		path.moveTo(verticalLineX, highY);
		path.lineTo(verticalLineX, lowY);
		/* Open and close horizontal lines if the bar width is greater than 1. */
		if (barWidth > 1) {
			double lineWidth = stroke.getLineWidth();
			// Open horizontal line.
			path.moveTo(x, openY);
			path.lineTo(verticalLineX - lineWidth, openY);
			// Close horizontal line
			path.moveTo(verticalLineX + lineWidth, closeY);
			path.lineTo(x + barWidth - lineWidth, closeY);
		}
		gc.draw(path);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void plot(Canvas.Context gc, DataList dataList, int startIndex, int endIndex) {
		for (int index = startIndex; index <= endIndex; index++) {
			if (index >= 0 && index < dataList.size()) {
				plot(gc, dataList, index);
			}
		}
	}
}
