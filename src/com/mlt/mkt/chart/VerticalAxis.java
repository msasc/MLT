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

import java.awt.Color;
import java.awt.Font;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.mlt.desktop.control.Canvas;
import com.mlt.desktop.control.GridBagPane;
import com.mlt.desktop.graphic.Composition;
import com.mlt.desktop.graphic.Line;
import com.mlt.desktop.graphic.Rectangle;
import com.mlt.desktop.graphic.Render;
import com.mlt.desktop.graphic.Stroke;
import com.mlt.desktop.graphic.Text;
import com.mlt.desktop.layout.Anchor;
import com.mlt.desktop.layout.Constraints;
import com.mlt.desktop.layout.Dimension;
import com.mlt.desktop.layout.Fill;
import com.mlt.desktop.layout.Insets;
import com.mlt.mkt.data.PlotData;
import com.mlt.mkt.data.PlotScale;
import com.mlt.util.Colors;
import com.mlt.util.Formats;
import com.mlt.util.Numbers;

/**
 *
 * @author Miquel Sas
 */
class VerticalAxis extends GridBagPane {

	/**
	 * Axis canvas.
	 */
	class AxisCanvas extends Canvas {
		@Override
		protected void paintCanvas(Canvas.Context gc) {
			plot(gc);
		}
	}

	/** Cursor index. */
	private static final int CURSOR = 0;

	/**
	 * Returns a list of increases to apply.
	 *
	 * @param integerDigits The number of integer digits.
	 * @param decimalDigits The number of decimal digits.
	 * @param multipliers   The list of multipliers.
	 * @return The list of increases.
	 */
	public static List<BigDecimal> getIncreases(
		int integerDigits,
		int decimalDigits,
		int... multipliers) {
	
		List<BigDecimal> increaments = new ArrayList<>();
		int upperScale = decimalDigits;
		int lowerScale = (integerDigits - 1) * (-1);
		for (int scale = upperScale; scale >= lowerScale; scale--) {
			for (int multiplier : multipliers) {
				double number = Math.pow(10, -scale);
				if (Double.isFinite(number)) {
					BigDecimal value = Numbers.getBigDecimal(number, scale);
					BigDecimal multiplicand =
						new BigDecimal(multiplier).setScale(0, RoundingMode.HALF_UP);
					increaments.add(value.multiply(multiplicand));
				}
			}
		}
		return increaments;
	}

	/** The plotter pane (chart) that contains this info pane. */
	private PlotterPane chart;
	/** The line and text color. */
	private Color foregroundColor = Colors.DARKGRAY;
	/** The length of the small line before each value. */
	private double lineLength = 5;
	/** The line stroke. */
	private Stroke lineStroke = new Stroke(1.0);
	/** The surround cursor value fill color. */
	private Color surroundColor = Colors.ANTIQUEWHITE;
	/** The insets to surround the price with a rectangle. */
	private Insets surroundInsets = new Insets(4, 2, 4, 3);
	/** The text font. */
	private Font textFont = new Font(Font.DIALOG, Font.PLAIN, 12);
	/** The text color. */
	private Color textColor = Colors.DARKSLATEGRAY;
	/** Insets of the text. */
	private Insets textInsets = new Insets(5, 8, 5, 8);

	/** Axis canvas. */
	private AxisCanvas canvas;
	/** Last y coordinate to plot the cursor value. */
	private double lastY;

	/** The cursor. */
	private Render cursor = new Render(new Composition("CR"));
	/** Cursor operation. */
	private int cursorOperation = ChartContainer.CURSOR_PLOT;

	/** Axis width, set externally. */
	private double axisWidth = -1;

	/**
	 * Constructor.
	 *
	 * @param chart The plotter pane (chart) that contains this vertical axis.
	 */
	public VerticalAxis(PlotterPane chart) {
		this.chart = chart;

		/* Setup the canvas. */
		canvas = new AxisCanvas();
		add(canvas, new Constraints(Anchor.CENTER, Fill.BOTH, 0, 0, Insets.EMPTY));
	}

	/**
	 * Clear the cursor.
	 */
	public void clearCursor() {
		cursorOperation = ChartContainer.CURSOR_CLEAR;
		canvas.repaintImmediately();
	}

	/**
	 * Returns the value by which should be increased an initial value to plot the rounded values.
	 *
	 * @param value The starting value.
	 * @return The increase value.
	 */
	private BigDecimal getIncreaseValue(Canvas.Context gc, double value) {
		
		// Data context.
		DataContext dc = chart.getDataContext();

		// The increase that do not overlap texts.
		BigDecimal increase = null;

		// Calculate the minimum line height to not overlap text adding some padding.
		double minimumHeight = Text.getSize("1000", textFont).getHeight() * 2.0;

		// The maximum value and its y coordinate.
		int integerDigits = Numbers.getDigits(value);
		int decimalDigits = dc.getPlotData().getPipScale();
		double y = dc.getCoordinateY(value);

		// The list of increases.
		List<BigDecimal> increases = VerticalAxis.getIncreases(integerDigits, decimalDigits, 1, 2, 5, 10);

		// Take the first increase that do not overlaps the text.
		for (BigDecimal incr : increases) {
			double nextValue = value - incr.doubleValue();
			double nextY = dc.getCoordinateY(nextValue);
			if (nextY - y >= minimumHeight) {
				increase = incr;
				break;
			}
		}

		return increase;
	}

	/**
	 * Return the minimum width required by the axis.
	 * 
	 * @return The minimum width.
	 */
	public double getMinimumWidth() {
		Locale locale = Locale.getDefault();
		PlotData plotData = chart.getDataContext().getPlotData();
		double maxValue = plotData.getMaximumValue();
		double minValue = plotData.getMinimumValue();
		int tickScale = plotData.getTickScale();
		String maximum = Formats.fromDouble(maxValue, tickScale, locale);
		String minimum = Formats.fromDouble(minValue, tickScale, locale);
		double widthMax = Text.getSize(maximum, textFont).getWidth();
		double widthMin = Text.getSize(minimum, textFont).getWidth();
		double textWidth = Math.max(widthMax, widthMin);
		double width = lineLength + textInsets.getLeft() + textWidth + textInsets.getRight();
		return width;
	}

	/**
	 * Build a sample with zeros as digits from the value string, to calculate the width of the text.
	 *
	 * @param str The value string.
	 * @return The sample string.
	 */
	private String getSample(String str) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (Character.isDigit(c)) {
				b.append('0');
			} else {
				b.append(c);
			}
		}
		return b.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	private void plot(Canvas.Context gc) {
		chart.getDataContext().ensureContext();

		if (gc.isImmediateRepaint()) {
			if (cursorOperation == ChartContainer.CURSOR_PLOT) {
				plotCursorValue(gc, lastY);
			}
			if (cursorOperation == ChartContainer.CURSOR_CLEAR) {
				cursor.restore(gc);
			}
		} else {
			gc.clear(getBackground());
			plotScale(gc);
			cursor.clearSave();
		}
	}

	/**
	 * Draw the cursor value with the small line, surrounded by a rectangle.
	 *
	 * @param y The y coordinate.
	 */
	public void plotCursorValue(Canvas.Context gc, double y) {
		if (Double.isInfinite(y) || Double.isNaN(y)) {
			return;
		}
		double value = chart.getDataContext().getDataValue(y);
		if (Double.isInfinite(value) || Double.isNaN(value)) {
			return;
		}
		int scale = chart.getDataContext().getPlotData().getTickScale();

		cursor.clear(CURSOR);
		Line hline = new Line(0, y, lineLength, y);
		hline.setStroke(lineStroke);
		hline.setDrawPaint(foregroundColor);
		cursor.addDraw(CURSOR, hline);

		String str = Formats.fromDouble(value, scale, Locale.getDefault());
		Dimension size = Text.getSize(getSample(str), textFont);
		double width = size.getWidth();
		double height = size.getHeight();

		double xStr = lineLength + textInsets.getLeft();
		double yStr = y + height / 2.0;

		double top = surroundInsets.getTop();
		double left = surroundInsets.getLeft();
		double bottom = surroundInsets.getBottom();
		double right = surroundInsets.getRight();

		double xRect = xStr - left;
		double yRect = yStr - height - top;
		double widthRect = left + width + right;
		double heightRect = top + height + bottom;
		Rectangle rect = new Rectangle(xRect, yRect, widthRect, heightRect);
		rect.setStroke(lineStroke);
		rect.setDrawPaint(foregroundColor);
		rect.setFillPaint(surroundColor);
		cursor.addPaint(CURSOR, rect);

		Text text = new Text(str, textFont, xStr, yStr);
		Color textColor = this.textColor;
		if (surroundColor != null) {
			if (Colors.brightness(surroundColor) < 0.5) {
				textColor = Color.WHITE;
			}
		}
		text.setFillPaint(textColor);
		cursor.addFill(CURSOR, text);
		cursor.render(gc);
	}

	/**
	 * Plot the vertical axis scale of numbers.
	 */
	private void plotScale(Canvas.Context gc) {

		/* A vertical line on the left. */
		Line vline = new Line(1, 0, 1, gc.getHeight());
		vline.setStroke(lineStroke);
		vline.setDrawPaint(foregroundColor);
		gc.draw(vline);

		/* Plot data. */
		PlotData plotData = chart.getDataContext().getPlotData();

		/* Retrieve the increase to apply and the decimal places to floor. */
		BigDecimal increase = getIncreaseValue(gc, plotData.getMaximumValue());
		if (increase == null) {
			return;
		}
		int floorScale = increase.scale();
		int pipScale = plotData.getPipScale();

		/*
		 * Iterate starting at the floor of the maximum value until the minimum value would be passed.
		 */
		double maximumValue = new BigDecimal(plotData.getMaximumValue()).setScale(pipScale
			- 1, RoundingMode.HALF_EVEN).doubleValue();
		double minimumValue = plotData.getMinimumValue();
		double plotValue = Numbers.floor(maximumValue, floorScale);

		PlotScale plotScale = plotData.getPlotScale();
		while (plotValue > minimumValue) {
			double y = chart.getDataContext().getCoordinateY(plotValue);
			plotValue(gc, y, plotValue, pipScale);
			if (plotScale.equals(PlotScale.LOGARITHMIC)) {
				increase = getIncreaseValue(gc, plotValue);
			}
			if (increase == null) {
				break;
			}
			plotValue -= increase.doubleValue();
		}
	}

	/**
	 * Draw a vertical axis value.
	 *
	 * @param gc    Graphics context.
	 * @param y     The y coordinate.
	 * @param value The value to plot.
	 * @param scale The scale.
	 */
	private void plotValue(Canvas.Context gc, double y, double value, int scale) {

		/* Draw the small line. */
		Line hline = new Line(0, y, lineLength, y);
		hline.setStroke(lineStroke);
		hline.setDrawPaint(foregroundColor);
		gc.draw(hline);

		/* The string to draw and its coordinates. */
		String strValue = Formats.fromDouble(value, scale, Locale.getDefault());
		Dimension size = Text.getSize(strValue, textFont);
		double xStr = lineLength + textInsets.getLeft();
		double yStr = y + size.getHeight() / 2.0;

		Text text = new Text(strValue, textFont, xStr, yStr);
		text.setFillPaint(textColor);
		gc.fill(text);
	}

	/**
	 * Set the axis width.
	 * 
	 * @param axisWidth The width.
	 */
	public void setAxisWidth(double axisWidth) {
		this.axisWidth = axisWidth;
		setSizes();
	}

	/**
	 * Set the cursor value.
	 *
	 * @param y y coordinate.
	 */
	public void setCursor(double y) {
		lastY = y;
		cursorOperation = ChartContainer.CURSOR_PLOT;
		canvas.repaintImmediately();
	}

	/**
	 * Sets this axis sizes.
	 */
	private void setSizes() {
		if (axisWidth < 0) {
			axisWidth = getMinimumWidth();
		}
		Dimension size = new Dimension(axisWidth, chart.getDataContext().getPlotHeight());
		setPreferredSize(size);
		setSize(size);
	}
}
