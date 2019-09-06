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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.mlt.desktop.control.Canvas;
import com.mlt.desktop.control.GridBagPane;
import com.mlt.desktop.graphic.Composition;
import com.mlt.desktop.graphic.Drawing;
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
import com.mlt.mkt.data.DataList;
import com.mlt.mkt.data.PlotData;
import com.mlt.mkt.data.Unit;
import com.mlt.util.Colors;

/**
 * An horizontal axis in a chart view. The horizontal axis contains one or two lines and shows time information
 * depending on the period shown.
 *
 * @author Miquel Sas
 */
class HorizontalAxis extends GridBagPane {

	/** Cursor index. */
	private static final int CURSOR = 0;

	/**
	 * Axis canvas.
	 */
	class AxisCanvas extends Canvas {
		@Override
		protected void paintCanvas(Canvas.Context gc) {
			plot(gc);
		}
	}

	/** The text color. */
	private Color textColor = Colors.DARKSLATEGRAY;
	/** The text font. */
	private Font textFont = new Font(Font.DIALOG, Font.PLAIN, 12);
	/** The line color. */
	private Color foregroundColor = Colors.DARKGRAY;
	/** Insets of the text. */
	private Insets textInsets = new Insets(8, 2, 2, 2);
	/** The length of the small line before each value. */
	private double lineLength = 5;
	/** The line stroke. */
	private Stroke lineStroke = new Stroke(1.0);
	/** Axis height. */
	private double axisHeight = -1;
	/** List of plotter panes (charts). */
	private List<PlotterPane> charts;
	/** Axis canvas. */
	private AxisCanvas canvas;
	/** The surround cursor value fill color. */
	private Color surroundColor = Colors.ANTIQUEWHITE;
	/** The insets to surround the price with a rectangle. */
	private Insets surroundInsets = new Insets(2, 2, 2, 2);

	/** Last x coordinate to plot the cursor value. */
	private double lastX;
	/** The cursor. */
	private Render cursor = new Render(new Composition("CR"));
	/** Cursor operation. */
	private int cursorOperation = ChartContainer.CURSOR_PLOT;

	/**
	 * Constructor.
	 *
	 * @param dc Plot data context.
	 */
	public HorizontalAxis(List<PlotterPane> charts) {
		this.charts = charts;

		/* Setup the canvas. */
		canvas = new AxisCanvas();
		add(canvas, new Constraints(Anchor.CENTER, Fill.BOTH, 0, 0, Insets.EMPTY));
		
		DataContext dc = getDC();
		dc.ensureContext();
		setSizes();
	}

	/**
	 * Returns the time period to plot that fits in the available width.
	 *
	 * @param gc             The graphics context.
	 * @param timeElapsed    The total time elapsed.
	 * @param availableWidth The available width.
	 * @return The time period that fits.
	 */
	private TimePeriod getTimePeriodThatFits(Canvas.Context gc, long timeElapsed, double availableWidth) {

		TimePeriod[] timePeriods = TimePeriod.values();
		for (TimePeriod timePeriod : timePeriods) {
			long millis = timePeriod.getMillis();
			double periods = timeElapsed / millis;
			double widthPerPeriod = (availableWidth / periods);
			String sample = timePeriod.getSample();
			double width = Text.getSize(sample, gc.getFont()).getWidth();
			double necessaryWidthPerPeriod = textInsets.getLeft() + width + textInsets.getRight();
			if (widthPerPeriod > necessaryWidthPerPeriod) {
				return timePeriod;
			}
		}

		return TimePeriod.DECADE;
	}

	/**
	 * Return the data context.
	 *
	 * @return The context.
	 */
	private DataContext getDC() {
		return charts.get(0).getDataContext();
	}

	/**
	 * {@inheritDoc}
	 */
	private void plot(Canvas.Context gc) {
		getDC().ensureContext();
		setSizes();

		if (gc.isImmediateRepaint()) {
			if (cursorOperation == ChartContainer.CURSOR_PLOT) {
				plotCursorValue(gc, lastX);
			}
			if (cursorOperation == ChartContainer.CURSOR_CLEAR) {
				cursor.restore(gc);
			}
		} else {
			gc.clear(getBackground());
			plotAxis(gc);
			cursor.clearSave();
		}
	}

	/**
	 * Effectively plot the axis.
	 * 
	 * @param gc The graphics context.
	 */
	private void plotAxis(Canvas.Context gc) {
		DataContext dc = getDC();

		/* Draw a line on the top. */
		Line hline = new Line(0, 1, dc.getPlotWidth(), 0);
		hline.setStroke(lineStroke);
		hline.setDrawPaint(foregroundColor);
		gc.draw(hline);

		/* Ensure context. */
		if (charts.isEmpty()) {
			return;
		}

		/* Calculate available width and time elapsed. */
		PlotData plotData = dc.getPlotData();
		DataList dataList = plotData.get(0);
		int startIndex = plotData.getStartIndex();
		if (startIndex < 0) {
			startIndex = 0;
		}
		int endIndex = plotData.getEndIndex();
		if (endIndex >= dataList.size()) {
			endIndex = dataList.size() - 1;
		}
		if (startIndex >= endIndex) {
			return;
		}
		double startX = dc.getCoordinateX(startIndex);
		double endX = dc.getCoordinateX(endIndex);
		double availableWidth = endX - startX + 1;
		long startTime = dataList.get(startIndex).getTime();
		long endTime = dataList.get(endIndex).getTime();
		long timeElapsed = endTime - startTime;

		/* Calculate necessary width per time period. */
		TimePeriod timePeriod = getTimePeriodThatFits(gc, timeElapsed, availableWidth);
		Dimension size = Text.getSize(timePeriod.getSample(), textFont);
		double necessaryWidth = textInsets.getLeft() + size.getWidth() + textInsets.getRight() + 1;

		/* Iterate from start index to end index painting when required. */
		double lastX = 0;
		for (int index = startIndex + 1; index <= endIndex; index++) {

			/* Current and previous times. */
			long timeCurrent = dataList.get(index).getTime();
			long timePrevious = dataList.get(index - 1).getTime();

			/* If start of period, not do nothing. */
			boolean startPeriod = timePeriod.isStartTimePeriod(timeCurrent, timePrevious);
			if (!startPeriod) {
				continue;
			}

			/* Get the string and plot it. */
			String stringToPlot = timePeriod.getStringToPlot(timeCurrent);
			double x = dc.getCenterCoordinateX(dc.getCoordinateX(index));
			double xText = x - size.getWidth() / 2;

			/* Check overlap. */
			if (xText - lastX < necessaryWidth) {
				continue;
			}
			lastX = xText;

			/* Draw the vertical line. */
			double lineX = x;
			Line vline = new Line(lineX, 0, lineX, lineLength);
			vline.setStroke(lineStroke);
			vline.setDrawPaint(foregroundColor);
			gc.draw(vline);

			/* Draw the string. */
			double y = lineLength + textInsets.getTop() + size.getHeight() / 2.0;
			Text text = new Text(stringToPlot, textFont, xText, y);
			text.setFillPaint(textColor);
			gc.fill(text);
		}
	}

	/**
	 * Draw the cursor value with the small line, surrounded by a rectangle.
	 *
	 * @param y The y coordinate.
	 */
	public void plotCursorValue(Canvas.Context gc, double x) {
		DataContext dc = getDC();
		if (Double.isInfinite(x) || Double.isNaN(x)) {
			return;
		}

		PlotData pd = dc.getPlotData();
		int startIndex = pd.getStartIndex();
		int endIndex = pd.getEndIndex();
		Unit unit = getDC().getPlotData().getPeriod().getUnit();
		int index = dc.getDataIndex(x);
		if (index < startIndex || index > endIndex) {
			return;
		}
		LocalDateTime time = null;
		if (!pd.isOutOfRange(index)) {
			time = pd.getTimeFromIndex(index);
		} else {
			time = pd.getTimeFromIndexOutOfRange(index);
		}

		cursor.clear(CURSOR);
		Line vline = new Line(x, 0, x, lineLength);
		vline.setStroke(lineStroke);
		vline.setDrawPaint(foregroundColor);
		cursor.addDraw(CURSOR, vline);

		/*
		 * A two line text if the unit is time. The foirst line is the date, and the second line is the time.
		 */
		if (unit.isTime()) {

			DateTimeFormatter fmtDate = DateTimeFormatter.ofPattern(unit.getDatePattern());
			String strDate = time.format(fmtDate);
			Dimension szDate = Text.getSize(strDate, textFont);
			double yDate = lineLength + textInsets.getTop() + surroundInsets.getTop() + szDate.getHeight();
			double xDate = x - szDate.getWidth() / 2;
			Text textDate = new Text(strDate, textFont, xDate, yDate);
			textDate.setFillPaint(textColor);

			DateTimeFormatter fmtTime = DateTimeFormatter.ofPattern(unit.getTimePattern());
			String strTime = time.format(fmtTime);
			Dimension szTime = Text.getSize(strTime, textFont);
			double yTime = yDate + textInsets.getBottom() + szTime.getHeight();
			double xTime = x - szTime.getWidth() / 2;
			Text textTime = new Text(strTime, textFont, xTime, yTime);
			textTime.setFillPaint(textColor);

			Rectangle surround = Drawing.getBounds(textDate, textTime).extend(surroundInsets);
			surround.setStroke(lineStroke);
			surround.setDrawPaint(foregroundColor);
			surround.setFillPaint(surroundColor);

			cursor.addPaint(CURSOR, surround);
			cursor.addFill(CURSOR, textDate);
			cursor.addFill(CURSOR, textTime);
		}

		cursor.render(gc);
	}

	/**
	 * Set the cursor value.
	 *
	 * @param x x coordinate.
	 */
	public void setCursor(double x) {
		cursorOperation = ChartContainer.CURSOR_PLOT;
		if (x < 0) {
			return;
		}
		lastX = x;
		canvas.repaintImmediately();
	}

	/**
	 * Clear the cursor.
	 */
	public void clearCursor() {
		cursorOperation = ChartContainer.CURSOR_CLEAR;
		canvas.repaintImmediately();
	}

	/**
	 * Sets this axis sizes.
	 */
	private void setSizes() {
		if (charts.isEmpty()) {
			return;
		}
		double width = getDC().getPlotWidth();
		Dimension size = new Dimension(width, getAxisHeight());
		setPreferredSize(size);
		setSize(size);
	}

	/**
	 * Return the required axis height to show the axis and the curso info.
	 *
	 * @return The axis height.
	 */
	public double getAxisHeight() {
		if (axisHeight < 0) {
			Unit unit = getDC().getPlotData().getPeriod().getUnit();
			double textHeight = Text.getSize("0000", textFont).getHeight();
			axisHeight = lineLength;
			axisHeight += textInsets.getTop();
			axisHeight += surroundInsets.getTop();
			axisHeight += textHeight;
			axisHeight += textInsets.getBottom();
			if (unit.isTime()) {
				axisHeight += textHeight;
				axisHeight += textInsets.getBottom();
			}
			axisHeight += surroundInsets.getBottom();
		}
		return axisHeight;
	}
}
