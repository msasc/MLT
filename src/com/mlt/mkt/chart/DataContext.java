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
package com.mlt.mkt.chart;

import com.mlt.desktop.graphic.Rectangle;
import com.mlt.desktop.layout.Dimension;
import com.mlt.desktop.layout.Insets;
import com.mlt.mkt.data.PlotData;
import com.mlt.mkt.data.PlotScale;
import com.mlt.util.Numbers;

/**
 * Plot data context information. Plot data is displayed through a chart plotter that has an information bar, a vertical
 * axis, and a center canvas where prices, volumes, indicators and other drawings are displayed. Plot data must be
 * contextualized to be able to map coordinates to data.
 *
 * @author Miquel Sas
 */
public class DataContext {

	/** Parent plotter pane. */
	private PlotterPane chart;
	/** The plot data. */
	private PlotData plotData;
	/** Effective chart plotter rectangle. */
	private Rectangle bounds;

	/**
	 * The width of a period. This is the visible width per period. Bars, candles or histogram bars are centered on it.
	 */
	private double periodWidth;
	/** Minimum period width to set the data width to 1. */
	private double minimumPeriodWidth = 4;
	/** A boolean that indicates if the context has been initialized. */
	private boolean initialized = false;
	/**
	 * Constructor.
	 *
	 * @param chart    The parent plotter pane.
	 * @param plotData Plot data.
	 */
	public DataContext(PlotterPane chart, PlotData plotData) {
		super();
		this.chart = chart;
		this.plotData = plotData;
	}

	/**
	 * Return the plot data.
	 *
	 * @return The plot data.
	 */
	public PlotData getPlotData() {
		return plotData;
	}

	/**
	 * Return the height of the chart canvas.
	 *
	 * @return The height.
	 */
	public double getPlotHeight() {
		return chart.getPlotSize().getHeight();
	}

	/**
	 * Return the width of the chart canvas.
	 *
	 * @return The width.
	 */
	public double getPlotWidth() {
		return chart.getPlotSize().getWidth();
	}

	/**
	 * Check whether the context has been initialized.
	 *
	 * @return A boolean.
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * Ensure the context.
	 */
	public void ensureContext() {

		Dimension size = chart.getPlotSize();
		Insets insets = chart.getPlotInsets();

		/* Effective plot bounds. */
		double x = insets.getLeft();
		double y = insets.getTop();
		double width = size.getWidth() - insets.getLeft() - insets.getRight();
		double height = size.getHeight() - insets.getTop() - insets.getBottom();
		bounds = new Rectangle(x, y, width, height);

		/*
		 * Available width per data item. As a general rule, it can be 75% of the available width per bar, as an odd
		 * number, and if the result is less than 2, plot just a vertical line of 1 pixel width.
		 */
		int startIndex = plotData.getStartIndex();
		int endIndex = plotData.getEndIndex();
		double periods = endIndex - startIndex + 1;
		periodWidth = bounds.getWidth() / periods;
		initialized = true;
	}

	/**
	 * Returns the coordinate of the drawing center for a bar, candle, line or histogram, given the starting X
	 * coordinate.
	 *
	 * @param x The starting x coordinate.
	 * @return The vertical line X coordinate.
	 */
	public double getCenterCoordinateX(double x) {
		double center = x;
		if (periodWidth >= minimumPeriodWidth) {
			center += (periodWidth / 2);
		}
		return center;
	}

	/**
	 * Returns the X coordinate where starts the area to plot a given data index.
	 *
	 * @param index The data index.
	 * @return The X coordinate.
	 */
	public double getCoordinateX(double index) {
		double startIndex = plotData.getStartIndex();
		double endIndex = plotData.getEndIndex();
		double indexFactor = (index - startIndex) / (endIndex - startIndex);
		double relativeX = indexFactor * bounds.getWidth();
		double coordinateX = bounds.getX() + relativeX;
		return coordinateX;
	}

	/**
	 * Returns the Y coordinate, starting at the top of the paint area, given the value.
	 *
	 * @param value The value to retrieve its Y coordinate.
	 * @return The Y coordinate for the argument value.
	 */
	public double getCoordinateY(double value) {
		double maximumValue = plotData.getMaximumValue();
		double minimumValue = plotData.getMinimumValue();
		if (plotData.getPlotScale().equals(PlotScale.LOGARITHMIC)) {
			maximumValue = Math.log1p(maximumValue);
			minimumValue = Math.log1p(minimumValue);
			value = Math.log1p(value);
		}
		double valueFactor = (value - minimumValue) / (maximumValue - minimumValue);
		double relativeY = 0;
		if (Double.isFinite(value) && Double.isFinite(valueFactor)) {
			relativeY = valueFactor * bounds.getHeight();
		}
		double coordinateY = bounds.getY() + bounds.getHeight() - relativeY;
		return coordinateY;
	}

	/**
	 * Returns the index on the data given the x coordinate in the plot area.
	 *
	 * @param x The x coordinate in the plot area.
	 * @return The index on the data.
	 */
	public int getDataIndex(double x) {

		if (bounds == null) {
			ensureContext();
		}

		if (x < bounds.getX()) {
			return plotData.getStartIndex();
		}
		if (x > bounds.getX() + bounds.getWidth()) {
			return plotData.getEndIndex();
		}

		double xRelative = x - bounds.getX();
		int startIndex = plotData.getStartIndex();
		int endIndex = plotData.getEndIndex();
		double xFactor = xRelative / bounds.getWidth();
		if (!Double.isFinite(xFactor)) {
			return 0;
		}
		double indexes = endIndex - startIndex + 1;
		double numPeriods = indexes * xFactor;
		int periods = Double.valueOf(Numbers.round(numPeriods, 0)).intValue();
		int index = startIndex + periods - 1;
		return index;
	}

	/**
	 * Returns the data value given the y coordinate in the plot area.
	 *
	 * @param y The y coordinate in the plot area.
	 * @return The data value.
	 */
	public double getDataValue(double y) {
		double yRelative = y - bounds.getY();
		double minimumValue = plotData.getMinimumValue();
		double maximumValue = plotData.getMaximumValue();
		if (plotData.getPlotScale().equals(PlotScale.LOGARITHMIC)) {
			maximumValue = Math.log(maximumValue);
			minimumValue = Math.log(minimumValue);
		}
		double height = bounds.getHeight();
		double factor = (height - yRelative) / height;
		double value = minimumValue + ((maximumValue - minimumValue) * factor);
		if (plotData.getPlotScale().equals(PlotScale.LOGARITHMIC)) {
			value = Math.pow(Math.E, value);
		}
		if (Double.isInfinite(value) || Double.isNaN(value)) {
			return value;
		}
		int tickScale = plotData.getTickScale();
		return Numbers.round(value, tickScale);
	}

	/**
	 * Return the width of a period on the chart.
	 * 
	 * @return The width of a period.
	 */
	public double getPeriodWidth() {
		return periodWidth;
	}
}
