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

import java.util.List;

import javax.swing.border.EtchedBorder;

import com.mlt.desktop.control.Canvas;
import com.mlt.desktop.control.GridBagPane;
import com.mlt.desktop.graphic.Rectangle;
import com.mlt.desktop.layout.Anchor;
import com.mlt.desktop.layout.Constraints;
import com.mlt.desktop.layout.Dimension;
import com.mlt.desktop.layout.Fill;
import com.mlt.desktop.layout.Insets;
import com.mlt.mkt.data.PlotData;
import com.mlt.util.Colors;

/**
 * A zoom vision of the data being displayed on the chart.
 * 
 * @author Miquel Sas
 */
public class ZoomPane extends GridBagPane {

	/**
	 * Zoom canvas.
	 */
	class ZoomCanvas extends Canvas {

		@Override
		protected void paintCanvas(Canvas.Context gc) {
			plot(gc);
		}
	}

	/** Zoom canvas. */
	private ZoomCanvas canvas;
	/** Charts to retrieve plot data. */
	private List<PlotterPane> charts;

	/**
	 * 
	 */
	public ZoomPane(List<PlotterPane> charts) {
		super();
		this.charts = charts;

		canvas = new ZoomCanvas();
		add(canvas, new Constraints(Anchor.CENTER, Fill.BOTH, 0, 0, Insets.EMPTY));

		setPreferredSize(new Dimension(150, 16));
		setMaximumSize(new Dimension(150, 16));
		setBorder(new EtchedBorder());
	}

	/**
	 * Do the plot.
	 *
	 * @param gc The graphics context.
	 */
	private void plot(Canvas.Context gc) {
		if (charts.isEmpty()) {
			return;
		}
		gc.clear();

		PlotData pd = charts.get(0).getDataContext().getPlotData();
		double minIndex = pd.getMinimumIndex();
		double maxIndex = pd.getMaximumIndex();
		double dataSize = pd.getDataSize();

		double zoomFactor = (maxIndex - minIndex + 1) / dataSize;
		double zoomPos = ((maxIndex + minIndex) / 2) / dataSize;

		double vmarg = 2;
		double hmarg = 4;

		double gcWidth = gc.getWidth();
		double gcHeight = gc.getHeight();
		double dataWidth = (gcWidth - 2 * hmarg) * zoomFactor;
		double dataPos = (gcWidth - 2 * hmarg) * zoomPos;

		double x = hmarg + dataPos - dataWidth / 2;
		double y = vmarg;
		double width = dataWidth;
		double height = gcHeight - (2 * vmarg);
		Rectangle rc = new Rectangle(x, y, width, height);
		rc.setDrawPaint(Colors.DARKGRAY);
		rc.setFillPaint(Colors.DARKGRAY);

		gc.fill(rc);
		gc.draw(rc);
	}

}
