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
package com.mlt.mkt.chart;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.mlt.desktop.control.BorderPane;
import com.mlt.desktop.control.Canvas;
import com.mlt.desktop.control.GridBagPane;
import com.mlt.desktop.event.MouseHandler;
import com.mlt.desktop.graphic.Composition;
import com.mlt.desktop.graphic.Line;
import com.mlt.desktop.graphic.Render;
import com.mlt.desktop.graphic.Stroke;
import com.mlt.desktop.layout.Anchor;
import com.mlt.desktop.layout.Constraints;
import com.mlt.desktop.layout.Dimension;
import com.mlt.desktop.layout.Fill;
import com.mlt.desktop.layout.Insets;
import com.mlt.mkt.chart.plotter.DataPlotter;
import com.mlt.mkt.data.Data;
import com.mlt.mkt.data.DataList;
import com.mlt.mkt.data.IndicatorDataList;
import com.mlt.mkt.data.PlotData;
import com.mlt.mkt.data.info.DataInfo;
import com.mlt.util.Colors;
import com.mlt.util.Formats;

/**
 * The pane that effectively plots charts. Has an info pane, a vertical axis and
 * the center chart plotter.
 *
 * @author Miquel Sas
 */
public class PlotterPane extends BorderPane {

	/** Cursor horizontal line. */
	private static final int CURSOR_HLINE = 0;
	/** Cursor vertical line. */
	private static final int CURSOR_VLINE = 1;

	/**
	 * Plot canvas.
	 */
	class PlotCanvas extends Canvas {
		@Override
		protected void paintCanvas(Canvas.Context gc) {
			plot(gc);
		}
	}

	/** Parent chart container. */
	private ChartContainer container;
	/** Data context. */
	private DataContext dc;
	/** Info pane. */
	private InfoPane infoPane;
	/** Info pane background selected. */
	private Color infoPaneBackgoundSelected = new Color(232, 232, 232);
	/** Info pane background unselected. */
	private Color infoPaneBackgoundUnselected = new Color(244, 244, 244);
	/** Vertical axis. */
	private VerticalAxis verticalAxis;
	/** Inset factors to calculate the effective plot area. */
	private Insets insetFactors = new Insets(0.05, 0.02, 0.05, 0.02);
	/** Plot canvas. */
	private PlotCanvas plotCanvas;

	/** Last mouse x. */
	private double lastX;
	/** Last mouse y. */
	private double lastY;

	/** Cursor. */
	private Render cursor;
	/** Cursor stroke. */
	private Stroke cursorStroke =
		new Stroke(1.0, Stroke.CAP_BUTT, Stroke.JOIN_MITER, 1, new double[] { 2 }, 0);
	/** Cursor line color. */
	private Color cursorColor = Colors.DARKGRAY;

	/** Cursor operation. */
	private int cursorOperation = ChartContainer.CURSOR_PLOT;

	/**
	 * Constructor.
	 *
	 * @param container Parent chart container.
	 * @param plotData  The data to plot in this plotter pane.
	 */
	public PlotterPane(ChartContainer container, PlotData plotData) {
		super();
		this.container = container;
		this.dc = new DataContext(this, plotData);

		/* Plot canvas on the center. */
		plotCanvas = new PlotCanvas();
		GridBagPane pane = new GridBagPane();
		pane.add(plotCanvas, new Constraints(Anchor.CENTER, Fill.BOTH, 0, 0, Insets.EMPTY));
		setCenter(pane);

		/* Info pane on top. */
		infoPane = new InfoPane(this);
		infoPane.startInfo();
		infoPane.addInfo(plotData.getInfoInstrument(), Font.BOLD, Colors.DARKSLATEGRAY);
		infoPane.addInfo(plotData.getInfoPeriod(), Font.BOLD, Colors.BLUE);
		infoPane.endInfo();
		setTop(infoPane);

		/* Vertical axis on the right. */
		verticalAxis = new VerticalAxis(this);
		setRight(verticalAxis);

		/*
		 * Cursor, vertical and horizontal lines throw a rendering object to save the
		 * underlying canvas.
		 */
		cursor = new Render();
		cursor.add(new Composition("HLINE"));
		cursor.add(new Composition("VLINE"));
	}

	/**
	 * Add the mouse handler to the canvas.
	 *
	 * @param handler The mouse handler.
	 */
	public void addMouseHandler(MouseHandler handler) {
		plotCanvas.addMouseListener(handler);
		plotCanvas.addMouseMotionListener(handler);
		plotCanvas.addMouseWheelListener(handler);
	}

	/**
	 * Clear the cursor.
	 */
	public void clearCursor() {
		cursorOperation = ChartContainer.CURSOR_CLEAR;
		verticalAxis.clearCursor();
		plotCanvas.repaintImmediately();
	}

	/**
	 * Return the parent chart container.
	 * 
	 * @return The container.
	 */
	public ChartContainer getContainer() {
		return container;
	}

	/**
	 * Return the data context.
	 *
	 * @return The data context.
	 */
	public DataContext getDataContext() {
		return dc;
	}

	/**
	 * Returns the information of the data values.
	 *
	 * @param y The y coordinate.
	 * @return The information string.
	 */
	private String getInfoValue(double value) {
		StringBuilder b = new StringBuilder();
		// Scale to apply to value.
		int tickScale = dc.getPlotData().getTickScale();
		b.append("P: ");
		b.append(Formats.fromDouble(value, tickScale, Locale.getDefault()));
		return b.toString();
	}

	/**
	 * Returns the plot insets calculated with the plot factors.
	 *
	 * @return The plot insets.
	 */
	public Insets getPlotInsets() {
		double areaWidth = plotCanvas.getSize().getWidth();
		double areaHeight = plotCanvas.getSize().getHeight();
		double insetTop = areaHeight * insetFactors.getTop();
		double insetLeft = areaWidth * insetFactors.getLeft();
		double insetBottom = areaHeight * insetFactors.getBottom();
		double insetRight = areaWidth * insetFactors.getRight();
		return new Insets(insetTop, insetLeft, insetBottom, insetRight);
	}

	/**
	 * Returns the canvas plot size.
	 *
	 * @return The size.
	 */
	public Dimension getPlotSize() {
		return plotCanvas.getSize();
	}

	/**
	 * Return the vertical axis.
	 * 
	 * @return The vertical axis.
	 */
	public VerticalAxis getVerticalAxis() {
		return verticalAxis;
	}

	/**
	 * Do the plot.
	 *
	 * @param gc The graphics context.
	 */
	private void plot(Canvas.Context gc) {
		dc.ensureContext();
		PlotData plotData = dc.getPlotData();

		if (!gc.isImmediateRepaint()) {
			List<DataList> dataLists = plotData.getDataLists();
			dataLists.forEach(dataList -> dataList.setContext(dc));

			/* Separate non indicator lists. */
			List<DataList> indicators = new ArrayList<>();
			List<DataList> notIndicators = new ArrayList<>();
			for (DataList dataList : dataLists) {
				if (dataList instanceof IndicatorDataList) {
					if (dataList.isPlot()) {
						indicators.add(dataList);
					}
				} else {
					if (dataList.isPlot()) {
						notIndicators.add(dataList);
					}
				}
			}

			gc.clear(Colors.WHITESMOKE);
			int startIndex = plotData.getStartIndex();
			int endIndex = plotData.getEndIndex();

			/* Plot not indicators. */
			for (DataList dataList : notIndicators) {
				for (DataPlotter plotter : dataList.getDataPlotters()) {
					plotter.plot(gc, dataList, startIndex, endIndex);
				}
			}

			/* Plot indicators. */
			for (DataList dataList : indicators) {
				for (DataPlotter plotter : dataList.getDataPlotters()) {
					plotter.plot(gc, dataList, startIndex, endIndex);
				}
			}

			cursor.clearSave();
			setInfo();
		}

		if (gc.isImmediateRepaint()) {
			if (cursorOperation == ChartContainer.CURSOR_PLOT) {
				plotCursorValue(gc);
				cursor.render(gc);
			}
			if (cursorOperation == ChartContainer.CURSOR_CLEAR) {
				cursor.restore(gc);
			}
		}

	}

	/**
	 * Plot the cursor vertical and horizontal lines.
	 *
	 * @param gc The graphics context.
	 */
	private void plotCursorValue(Canvas.Context gc) {
		/* Horizontal line. */
		if (Double.isInfinite(lastY) || Double.isNaN(lastY)) {
			return;
		}
		if (lastY >= 0) {
			Line hline = new Line(0, lastY, gc.getWidth(), lastY);
			hline.setStroke(cursorStroke);
			hline.setDrawPaint(cursorColor);
			cursor.setEnabled(CURSOR_HLINE, true);
			cursor.clear(CURSOR_HLINE);
			cursor.addDraw(CURSOR_HLINE, hline);
		} else {
			cursor.setEnabled(CURSOR_HLINE, false);
		}
		/* Vertical line. */
		if (Double.isInfinite(lastX) || Double.isNaN(lastX)) {
			return;
		}
		if (lastX >= 0) {
			Line vline = new Line(lastX, 0, lastX, gc.getHeight());
			vline.setStroke(cursorStroke);
			vline.setDrawPaint(cursorColor);
			cursor.setEnabled(CURSOR_VLINE, true);
			cursor.clear(CURSOR_VLINE);
			cursor.addDraw(CURSOR_VLINE, vline);
		} else {
			cursor.setEnabled(CURSOR_VLINE, true);
		}
	}

	/**
	 * Set the cursor value, repainting the horizontal and vertical cursor lines,
	 * and painting the cursor value on the
	 * vertical axis.
	 *
	 * @param x The mouse x.
	 * @param y The mouse y.
	 */
	public void setCursor(double x, double y) {
		lastX = x;
		lastY = y;
		cursorOperation = ChartContainer.CURSOR_PLOT;
		if (y >= 0) {
			verticalAxis.setCursor(lastY);
		} else {
			verticalAxis.clearCursor();
		}
		plotCanvas.repaintImmediately();
	}

	/**
	 * Set the chart info.
	 */
	public void setInfo() {

		PlotData pd = dc.getPlotData();
		int index = dc.getDataIndex(lastX);

		/* Instrument and period. */
		infoPane.startInfo();

		infoPane.addInfo(pd.getInfoInstrument(), Font.BOLD, Colors.BLACK);
		infoPane.addInfo(pd.getInfoPeriod(), Font.BOLD, Colors.BLUE);

		/* Iterate data lists. */
		if (!pd.isOutOfRange(index)) {
			infoPane.addInfo(pd.getInfoTimeFromIndex(index), Font.PLAIN, Colors.BLACK);
			boolean black = false;
			for (int i = 0; i < pd.size(); i++) {
				Color color = (black ? Colors.BLACK : Colors.BLUE);
				black = !black;
				DataInfo info = pd.getDataInfo(i);
				String text = "";
				if (index >= 0 && index < pd.get(i).size()) {
					Data data = pd.get(i).get(index);
					if (data.isValid()) {
						text = info.getInfoData(data, false);
					}
				}
				if (!text.isEmpty()) {
					infoPane.addInfo(text, Font.PLAIN, color);
				}
			}
		} else {
			infoPane.addInfo(pd.getInfoTimeFromIndexOutOfRange(index), Font.PLAIN, Colors.BLACK);
		}

		/* The cursor value. */
		if (lastY >= 0) {
			infoPane.addInfo(getInfoValue(dc.getDataValue(lastY)), Font.PLAIN, Colors.RED);
		}

		/* Number of visible bars. */
		int minIndex = pd.getMinimumIndex();
		int maxIndex = pd.getMaximumIndex();
		int numBars = maxIndex - minIndex + 1;
		int totalBars = pd.getDataSize();
		infoPane.addInfo(" Bars " + numBars + " / " + totalBars, Font.PLAIN, Colors.BLUE);

		/* Number of visible periods. */
		int startIndex = pd.getStartIndex();
		int endIndex = pd.getEndIndex();
		int periods = endIndex - startIndex + 1;
		infoPane.addInfo(" Periods " + periods, Font.PLAIN | Font.ITALIC, Colors.BLACK);

		if (lastY >= 0) {
			infoPane.setBackground(infoPaneBackgoundSelected);
		} else {
			infoPane.setBackground(infoPaneBackgoundUnselected);
		}

		infoPane.endInfo();
	}
}
