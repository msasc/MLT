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
import java.util.ArrayList;
import java.util.List;

import com.mlt.desktop.control.Button;
import com.mlt.desktop.control.Canvas;
import com.mlt.desktop.control.GridBagPane;
import com.mlt.desktop.graphic.Line;
import com.mlt.desktop.graphic.Stroke;
import com.mlt.desktop.graphic.Text;
import com.mlt.desktop.icon.IconClose;
import com.mlt.desktop.layout.Anchor;
import com.mlt.desktop.layout.Constraints;
import com.mlt.desktop.layout.Dimension;
import com.mlt.desktop.layout.Fill;
import com.mlt.desktop.layout.Insets;
import com.mlt.util.Colors;

/**
 * A panel located at the top of the container, aimed to contain an info panel and necessary controls like the close
 * button.
 *
 * @author Miquel Sas
 */
public class InfoPane extends GridBagPane {
	
	/**
	 * Info canvas.
	 */
	class InfoCanvas extends Canvas {
		@Override
		protected void paintCanvas(Canvas.Context gc) {
			plot(gc);
		}
	}

	/**
	 * Info segment.
	 */
	class Segment {
		String text;
		int style;
		Color color;

		Segment(String text, int style, Color color) {
			this.text = text;
			this.style = style;
			this.color = color;
		}
	}

	/** The plotter pane (chart) that contains this info pane. */
	private PlotterPane chart;
	/** Font, the same family and size for all segments. */
	private Font font = new Font(Font.DIALOG, Font.PLAIN, 12);
	/** Text insets. */
	private Insets insets = new Insets(4, 4, 4, 4);
	/** List of segments. */
	private List<Segment> segments = new ArrayList<>();
	/** Info canvas. */
	private InfoCanvas canvas = new InfoCanvas();
	/** The line stroke. */
	private Stroke lineStroke = new Stroke(1.0);
	/** The line color. */
	private Color lineColor = Colors.DARKGRAY;

	/**
	 * Constructor.
	 *
	 * @param chart The chart (plotter pane) that contains this info pane.
	 */
	public InfoPane(PlotterPane chart) {
		super();
		this.chart = chart;

		setSizes(chart.getContainer().getSize().getWidth());
		
		add(canvas, new Constraints(Anchor.CENTER, Fill.BOTH, 0, 0, Insets.EMPTY));

		Button close = new Button();
		close = new Button();
		close.setName("CLOSE");
		close.setText(null);
		close.setToolTipText("Close the current chart");
		close.setIconTextGap(0);
		close.setMargin(new Insets(0, 0, 0, 0));
		IconClose icon = new IconClose();
		icon.setSize(16, 16);
		close.setIcon(icon);
		Dimension size = new Dimension(icon.getIconWidth(), icon.getIconHeight());
		close.setMinimumSize(size);
		close.setMaximumSize(size);
		close.setPreferredSize(size);
		close.setAction(l -> {
			chart.getContainer().close(chart);
		});
		
		add(close, new Constraints(Anchor.CENTER, Fill.NONE, 1, 0, Insets.EMPTY));

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
	 * Plot the info.
	 *
	 * @param gc The graphics context.
	 */
	private void plot(Canvas.Context gc) {
		
		setSizes(chart.getContainer().getSize().getWidth());
		
		gc.clear(getBackground());

		Line hline = new Line(0, gc.getHeight(), gc.getWidth(), gc.getHeight());
		hline.setDrawPaint(lineColor);
		hline.setStroke(lineStroke);
		gc.draw(hline);

		double height = new Text("SAMPLE", font, 0, 0).getShapeBounds().getHeight();
		double y = insets.getTop() + height;
		double x = 0;

		String name = font.getName();
		int size = font.getSize();
		for (Segment seg : segments) {
			Font font = new Font(name, seg.style, size);
			x += insets.getLeft();
			String sample = getSample(seg.text);
			double width = new Text(sample, font, 0, 0).getShapeBounds().getWidth();
			Text text = new Text(seg.text, font, x, y);
			text.setFillPaint(seg.color);
			gc.fill(text);
			x += Math.ceil(width) + insets.getRight();
		}
	}

	/**
	 * Start info process.
	 */
	public void startInfo() {
		segments.clear();
	}

	/**
	 * Add an info element.
	 *
	 * @param text  The text.
	 * @param style The font style.
	 * @param color The color.
	 */
	public void addInfo(String text, int style, Color color) {
		if (!segments.isEmpty()) {
			segments.add(new Segment(" - ", Font.PLAIN, lineColor));
		}
		segments.add(new Segment(text, style, color));
	}

	/**
	 * End info process and show.
	 */
	public void endInfo() {
		canvas.repaint();
	}

	/**
	 * Sets this axis sizes.
	 * 
	 * @param width The plot width.
	 */
	private void setSizes(double width) {
		double height = new Text("SAMPLE", font, 0, 0).getShapeBounds().getHeight();
		height += insets.getTop() + insets.getBottom();
		Dimension size = new Dimension(width, height);
		setPreferredSize(size);
		setSize(size);
	}
}
