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

import java.awt.EventQueue;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.border.EmptyBorder;

import com.mlt.desktop.action.ActionRun;
import com.mlt.desktop.control.BorderPane;
import com.mlt.desktop.control.Button;
import com.mlt.desktop.control.Control;
import com.mlt.desktop.control.SplitPane;
import com.mlt.desktop.control.ToolBar;
import com.mlt.desktop.event.ComponentHandler;
import com.mlt.desktop.event.MouseHandler;
import com.mlt.desktop.icon.Icons;
import com.mlt.desktop.layout.Insets;
import com.mlt.desktop.layout.Orientation;
import com.mlt.mkt.data.PlotData;
import com.mlt.util.Numbers;
import com.mlt.util.Resources;

/**
 * Top chart panel.
 *
 * @author Miquel Sas
 */
public class ChartContainer extends BorderPane {

	/** Cursor operation plot. */
	public static final int CURSOR_PLOT = 0;
	/** Cursor operation clear. */
	public static final int CURSOR_CLEAR = 1;

	/**
	 * Configure a tool bar button.
	 *
	 * @param icon       The icon string.
	 * @param tooltipKey The tool tip key.
	 * @param action     The action.
	 * @return The button.
	 */
	private static Button button(String icon, String tooltipKey, ActionListener action) {
		Button button = new Button();
		button.setIcon(Icons.getIcon(icon));
		button.setToolTipText(Resources.getText(tooltipKey));
		button.setIconTextGap(0);
		button.setMargin(Insets.EMPTY);
		button.setAction(action);
		return button;
	}

	/**
	 * Enum toolbar actions.
	 */
	enum ToolBarId {
		SCROLL_BACK, SCROLL_FRONT, SCROLL_START, SCROLL_END, ZOOM_IN, ZOOM_OUT;
	}

	/**
	 * Listener to fire the layout when the component is shown.
	 */
	class ComponentListener extends ComponentHandler {
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void componentShown(ComponentEvent e) {
			layoutCharts();
		}
	}

	/**
	 * Toolbar action.
	 */
	class ToolBarAction extends ActionRun {
		ToolBarId id;

		ToolBarAction(ToolBarId id) {
			this.id = id;
		}

		@Override
		public void run() {
			if (charts.isEmpty()) {
				return;
			}
			PlotData plotData = charts.get(0).getDataContext().getPlotData();
			switch (id) {
			case SCROLL_BACK:
				EventQueue.invokeLater(() -> {
					int scroll = plotData.getBarsToScrollOrZoom();
					if (plotData.scroll(scroll)) refreshAll();
				});
				break;
			case SCROLL_FRONT:
				EventQueue.invokeLater(() -> {
					int scroll = plotData.getBarsToScrollOrZoom() * (-1);
					if (plotData.scroll(scroll)) refreshAll();
				});
				break;
			case SCROLL_START:
				EventQueue.invokeLater(() -> {
					if (plotData.scrollStart()) refreshAll();
				});
				break;
			case SCROLL_END:
				EventQueue.invokeLater(() -> {
					if (plotData.scrollEnd()) refreshAll();
				});
				break;
			case ZOOM_IN:
				EventQueue.invokeLater(() -> {
					int zoom = plotData.getBarsToScrollOrZoom();
					if (plotData.zoom(zoom)) refreshAll();
				});
				break;
			case ZOOM_OUT:
				EventQueue.invokeLater(() -> {
					int zoom = plotData.getBarsToScrollOrZoom() * (-1);
					if (plotData.zoom(zoom)) refreshAll();
				});
				break;
			}
		}
	}

	/**
	 * Mouse listener.
	 */
	class MouseListener extends MouseHandler {
		PlotterPane chart;

		MouseListener(PlotterPane chart) {
			this.chart = chart;
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			if (!chart.getDataContext().isInitialized()) {
				return;
			}
			double x = e.getX();
			double y = e.getY();

			/* Current chart cursor display. */
			chart.setCursor(x, y);
			horizontalAxis.setCursor(x);
			chart.setInfo();

			/* Propagate current x position to the rest of charts. */
			for (PlotterPane c : charts) {
				if (c != chart) {
					c.setCursor(x, -1);
					c.setInfo();
				}
			}
		}

		@Override
		public void mouseExited(MouseEvent e) {
			chart.clearCursor();
			horizontalAxis.clearCursor();
			for (PlotterPane c : charts) {
				if (c != chart) {
					c.clearCursor();
				}
			}
		}
	}

	/** The toolbar. */
	private ToolBar toolBar;
	/** List of plotter panes. */
	private List<PlotterPane> charts = new ArrayList<>();
	/** Horizontal axis. */
	private HorizontalAxis horizontalAxis;
	/** Zoom pane. */
	private ZoomPane zoomPane;

	/**
	 * Constructor.
	 */
	public ChartContainer() {
		super();

		/* Listener to fire the layout of charts when it is shown. */
		addComponentListener(new ComponentListener());

		/* The toolbar on the top. */
		toolBar = new ToolBar();
		toolBar.addButton(button(Icons.FLAT_24x24_SCROLL_FRONT, "tooltipScrollFront", new ToolBarAction(ToolBarId.SCROLL_FRONT)));
		toolBar.addButton(button(Icons.FLAT_24x24_SCROLL_BACK, "tooltipScrollBack", new ToolBarAction(ToolBarId.SCROLL_BACK)));
		toolBar.addSeparator();
		toolBar.addButton(button(Icons.FLAT_24x24_SCROLL_START, "tooltipScrollStart", new ToolBarAction(ToolBarId.SCROLL_START)));
		toolBar.addButton(button(Icons.FLAT_24x24_SCROLL_END, "tooltipScrollEnd", new ToolBarAction(ToolBarId.SCROLL_END)));
		toolBar.addSeparator();
		toolBar.addButton(button(Icons.FLAT_24x24_ZOOM_IN, "tooltipZoomIn", new ToolBarAction(ToolBarId.ZOOM_IN)));
		toolBar.addButton(button(Icons.FLAT_24x24_ZOOM_OUT, "tooltipZoomOut", new ToolBarAction(ToolBarId.ZOOM_OUT)));
		toolBar.addSeparator();
		zoomPane = new ZoomPane(charts);
		toolBar.addPane(zoomPane);
		setTop(toolBar);

	}

	/**
	 * Add a chart to the list of charts.
	 *
	 * @param plotData The plot data.
	 */
	public void addPlotData(PlotData plotData) {
		addPlotData(plotData, false);
	}

	/**
	 * Add a chart to the list of charts indicating whether to force a layout.
	 *
	 * @param plotData The plot data.
	 * @param layout A boolean.
	 */
	public void addPlotData(PlotData plotData, boolean layout) {
		PlotterPane chart = new PlotterPane(this, plotData);
		chart.addMouseHandler(new MouseListener(chart));
		charts.add(chart);
		if (layout) {
			layoutCharts();
		}
	}

	/**
	 * Close the given chart.
	 * 
	 * @param chart The chart to close.
	 */
	public void close(PlotterPane chart) {
		charts.remove(chart);
		layoutCharts();
	}

	/**
	 * Return the list with all current split panes.
	 * 
	 * @return The list of split panes.
	 */
	private List<SplitPane> getAllSplitPanes() {
		List<Control> controls = Control.getAllChildControls(getCenter());
		List<SplitPane> splitPanes = new ArrayList<>();
		for (Control control : controls) {
			if (control instanceof SplitPane) {
				splitPanes.add((SplitPane) control);
			}
		}
		return splitPanes;
	}

	/**
	 * Create and return a default split pane.
	 * 
	 * @return The split pane.
	 */
	private SplitPane getSplitPane() {
		SplitPane sp = new SplitPane(Orientation.VERTICAL);
		sp.setBorder(new EmptyBorder(0, 0, 0, 0));
		sp.setDividerSize(6);
		sp.setResizeWeight(0.5);
		sp.setContinuousLayout(true);
		return sp;
	}

	/**
	 * Layout the list of charts.
	 */
	private void layoutCharts() {

		/* Clear. */
		if (getCenter() != null) {
			setCenter(null);
		}

		/* Nothing to layout. */
		if (charts.isEmpty()) {
			revalidate();
			repaint();
			return;
		}

		/* The horizontal axis is common to any included chart. */
		if (horizontalAxis == null) {
			horizontalAxis = new HorizontalAxis(charts);
			setBottom(horizontalAxis);
		}

		/*
		 * Add panels in the inverse order of the list, leaving the last one that will be the top control of the last
		 * split pane when the scan process will finish. If there is only one chart, no split pane will be added.
		 */
		double toolBarHeight = toolBar.getPreferredSize().getHeight();
		double horizontalAxisHeight = horizontalAxis.getAxisHeight();
		double height = getSize().getHeight() - toolBarHeight - horizontalAxisHeight;
		double panelHeight = height * 0.15;
		for (int i = charts.size() - 1; i > 0; i--) {

			/* Create the split panel. */
			SplitPane splitPane = getSplitPane();
			height -= panelHeight;
			int location = (int) (height);
			splitPane.setDividerLocation(location);

			/* Set the bottom the chart pointed by index. */
			splitPane.setBottomControl(charts.get(i));

			/*
			 * If there are no split panels added, then simply add this one, otherwise this one will be the top
			 * component of the last one added.
			 */
			if (getAllSplitPanes().isEmpty()) {
				setCenter(splitPane);
			} else {
				getAllSplitPanes().get(getAllSplitPanes().size() - 1).setTopControl(splitPane);
			}
		}

		if (charts.size() == 1) {
			setCenter(charts.get(0));
		} else {
			getAllSplitPanes().get(getAllSplitPanes().size() - 1).setTopControl(charts.get(0));
		}

		refreshAll();
	}

	/**
	 * Refresh all chart components.
	 */
	private void refreshAll() {

		/* Ensure contexts and set indexes. */
		charts.get(0).getDataContext().ensureContext();
		PlotData plotData = charts.get(0).getDataContext().getPlotData();
		plotData.calculateFrame();
		for (int i = 1; i < charts.size(); i++) {
			charts.get(i).getDataContext().ensureContext();
			charts.get(i).getDataContext().getPlotData().setStartAndEndIndexes(plotData);
			charts.get(i).getDataContext().getPlotData().calculateFrame();
		}

		/* Calculate the width of vertical axis. */
		double verticalAxisWidth = Numbers.MIN_DOUBLE;
		for (int i = 0; i < charts.size(); i++) {
			PlotterPane chart = charts.get(i);
			double minWidth = chart.getVerticalAxis().getMinimumWidth();
			verticalAxisWidth = Math.max(verticalAxisWidth, minWidth);
		}
		for (int i = 0; i < charts.size(); i++) {
			PlotterPane chart = charts.get(i);
			chart.getVerticalAxis().setAxisWidth(verticalAxisWidth);
		}
		revalidate();
		repaint();
	}
}
