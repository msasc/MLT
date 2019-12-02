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
import com.mlt.desktop.graphic.Line;
import com.mlt.desktop.graphic.Path;
import com.mlt.desktop.graphic.Stroke;
import com.mlt.mkt.chart.DataContext;
import com.mlt.mkt.chart.plotter.DataPlotter;
import com.mlt.mkt.data.OHLC;
import com.mlt.mkt.data.DataList;
import com.mlt.util.Colors;

/**
 * Plotter of histogram bars.
 *
 * @author Miquel Sas
 */
public class HistogramPlotter extends DataPlotter {

	/** Border color. */
	private Color borderColor = Colors.DARKSLATEGRAY;
	/** A boolean that indicates whether the border should be painted. */
	private boolean paintBorder = true;
	/** Stroke. */
	private Stroke stroke = new Stroke(1.0);

	/**
	 * Constructor.
	 */
	public HistogramPlotter() {
		super();
		setIndex(OHLC.VOLUME);
	}

	/**
	 * Constructor.
	 * 
	 * @param index The index to plot.
	 */
	public HistogramPlotter(int index) {
		super();
		setIndex(index);
	}

	/**
	 * Plot the index.
	 *
	 * @param gc       The canvas context.
	 * @param dataList Data list.
	 * @param index    Index.
	 * @param yZero    The y coordinate of the zero value.
	 */
	private void plot(Canvas.Context gc, DataList dataList, int index, double yZero) {

		DataContext dc = getContext();

		double value = dataList.get(index).getValue(getIndex());
		double previousValue = (index == 0 ? value : dataList.get(index - 1).getValue(getIndex()));
		boolean bullish = (value > previousValue);

		double y = dc.getCoordinateY(value);
		double periodX = dc.getCoordinateX(index);
		double barWidth = getDataWidth(dc);
		double barX = periodX + getDataMargin(dc);
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
		if (barWidth <= 1) {
			path.setDrawPaint(fillColor);
			path.moveTo(centerX, yZero);
			path.lineTo(centerX, y);
			gc.draw(path);
		} else {
			path.setDrawPaint(borderColor);
			path.setFillPaint(fillColor);
			path.moveTo(barX, yZero);
			path.lineTo(barX + barWidth, yZero);
			path.lineTo(barX + barWidth, y);
			path.lineTo(barX, y);
			path.lineTo(barX, yZero);
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

		/* Zero y coord. */
		double startX = getContext().getCoordinateX(startIndex - 1);
		double endX = getContext().getCoordinateX(endIndex + 1);
		double yZero = getContext().getCoordinateY(0);

		/* Plot each index. */
		for (int index = startIndex; index <= endIndex; index++) {
			if (index >= 0 && index < dataList.size()) {
				plot(gc, dataList, index, yZero);
			}
		}
		
		/* Zero line. */
		Line line = new Line(startX, yZero, endX, yZero);
		line.setDrawPaint(borderColor);
		line.setStroke(stroke);
		gc.draw(line);
	}

	/**
	 * Set whether this histogram plotter should paint the border.
	 * 
	 * @param paintBorder A boolean.
	 */
	public void setPaintBorder(boolean paintBorder) {
		this.paintBorder = paintBorder;
	}

}
