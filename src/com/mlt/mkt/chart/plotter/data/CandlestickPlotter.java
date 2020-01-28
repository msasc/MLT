/*
 * Copyright (C) 2018 Miquel Sas
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.mlt.mkt.chart.plotter.data;

import com.mlt.desktop.control.Canvas;
import com.mlt.desktop.graphic.Path;
import com.mlt.desktop.graphic.Stroke;
import com.mlt.mkt.chart.DataContext;
import com.mlt.mkt.chart.plotter.DataPlotter;
import com.mlt.mkt.data.OHLC;
import com.mlt.mkt.data.Data;
import com.mlt.mkt.data.DataList;
import com.mlt.util.Colors;
import java.awt.Color;

/**
 * Plotter of candlesticks.
 *
 * @author Miquel Sas
 */
public class CandlestickPlotter extends DataPlotter {

	/** Border color. */
	private Color borderColor = Colors.BLACK;
	/** A boolean that indicates whether the border should be painted. */
	private boolean paintBorder = true;
	/** Stroke. */
	private Stroke stroke = new Stroke(1.0);

	/**
	 * Constructor.
	 */
	public CandlestickPlotter() {
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
	public CandlestickPlotter(int open, int high, int low, int close) {
		super();
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

		/* Data and context. */
		Data data = dataList.get(index);
		double open = data.getValue(OHLC.OPEN);
		double high = data.getValue(OHLC.HIGH);
		double low = data.getValue(OHLC.LOW);
		double close = data.getValue(OHLC.CLOSE);
		boolean bullish = OHLC.isBullish(data);
		DataContext dc = getContext();

		double periodX = dc.getCoordinateX(index);
		double candleX = periodX + getDataMargin(dc);
		double openY = dc.getCoordinateY(open);
		double highY = dc.getCoordinateY(high);
		double lowY = dc.getCoordinateY(low);
		double closeY = dc.getCoordinateY(close);
		double bodyHigh = (bullish ? closeY : openY);
		double bodyLow = (bullish ? openY : closeY);
		double candleWidth = getDataWidth(dc);
		double centerX = dc.getCenterCoordinateX(periodX);

		Color fillColor;
		if (dataList.isOdd(index)) {
			if (bullish) {
				fillColor = getColorBullishOdd();
			} else {
				fillColor = getColorBearishOdd();
			}
		} else {
			if (bullish) {
				fillColor = getColorBullishEven();
			} else {
				fillColor = getColorBearishEven();
			}
		}

		Path path = new Path();
		path.setStroke(stroke);
		if (candleWidth <= 1) {
			path.setDrawPaint(fillColor);
			path.moveTo(centerX, highY);
			path.lineTo(centerX, lowY);
			gc.draw(path);
		} else {
			path.setDrawPaint(borderColor);
			path.setFillPaint(fillColor);
			/* Upper shadow. */
			path.moveTo(centerX, highY);
			path.lineTo(centerX, bodyHigh);
			/* Body. */
			path.moveTo(candleX, bodyHigh);
			path.lineTo(candleX + candleWidth, bodyHigh);
			path.lineTo(candleX + candleWidth, bodyLow);
			path.lineTo(candleX, bodyLow);
			path.lineTo(candleX, bodyHigh);
			/* Lower shadow. */
			path.moveTo(centerX, bodyLow);
			path.lineTo(centerX, lowY);
			gc.fill(path);
			if (paintBorder) {
				gc.draw(path);
			}
		}

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
