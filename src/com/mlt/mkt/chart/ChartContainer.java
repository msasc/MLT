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

import java.awt.EventQueue;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.mlt.desktop.control.BorderPane;
import com.mlt.desktop.control.Button;
import com.mlt.desktop.control.Control;
import com.mlt.desktop.control.SplitPane;
import com.mlt.desktop.control.ToolBar;
import com.mlt.desktop.event.ComponentHandler;
import com.mlt.desktop.event.Mask;
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
	 * Mouse listener.
	 */
	class MouseListener extends MouseHandler {
		PlotterPane chart;

		MouseListener(PlotterPane chart) {
			this.chart = chart;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3) {
				triggerPopupMenu(e);
			}
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

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			int sign = (e.getWheelRotation() >= 0 ? 1 : -1);

			/* If the control key is down, zoom. */
			if (Mask.check(e, Mask.CTRL)) {
				if (sign >= 0) {
					zoomIn(0.01);
				} else {
					zoomOut(0.01);
				}
				return;
			}

			/* If control key is not down, scroll. */
			if (Mask.check(e, 0)) {
				if (sign >= 0) {
					scrollBack(0.01);
				} else {
					scrollFront(0.01);
				}
				return;
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
		toolBar.addButton(button(
			Icons.FLAT_24x24_SCROLL_FRONT, "tooltipScrollFront", e -> scrollFront(0.05)));
		toolBar.addButton(button(
			Icons.FLAT_24x24_SCROLL_BACK, "tooltipScrollBack", e -> scrollBack(0.05)));
		toolBar.addSeparator();
		toolBar.addButton(button(
			Icons.FLAT_24x24_SCROLL_START, "tooltipScrollStart", e -> scrollStart()));
		toolBar.addButton(button(
			Icons.FLAT_24x24_SCROLL_END, "tooltipScrollEnd", e -> scrollEnd()));
		toolBar.addSeparator();
		toolBar.addButton(button(
			Icons.FLAT_24x24_ZOOM_IN, "tooltipZoomIn", e -> zoomIn(0.05)));
		toolBar.addButton(button(
			Icons.FLAT_24x24_ZOOM_OUT, "tooltipZoomOut", e -> zoomOut(0.05)));
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
	 * @param layout   A boolean.
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
	 * Check whether a plot data with the argument id is contained.
	 * 
	 * @param id The search id.
	 * @return A boolean.
	 */
	public boolean containsPlotData(String id) {
		for (PlotterPane chart : charts) {
			if (chart.getDataContext().getPlotData().getId().equals(id)) {
				return true;
			}
		}
		return false;
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
	 * Return the plot data with the given id or null if not exists.
	 * 
	 * @param id The plot data id.
	 * @return The plot data or null.
	 */
	public PlotData getPlotData(String id) {
		List<PlotData> plotDatas = getPlotDatas();
		for (PlotData plotData : plotDatas) {
			if (plotData.getId().equals(id)) {
				return plotData;
			}
		}
		return null;
	}

	/**
	 * Return the list of all plot datas.
	 * 
	 * @return The list ofplot datas.
	 */
	public List<PlotData> getPlotDatas() {
		List<PlotData> plotDatas = new ArrayList<>();
		for (PlotterPane chart : charts) {
			plotDatas.add(chart.getDataContext().getPlotData());
		}
		return plotDatas;
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
		 * Add panels in the inverse order of the list, leaving the last one that will
		 * be the top control of the last
		 * split pane when the scan process will finish. If there is only one chart, no
		 * split pane will be added.
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
			 * If there are no split panels added, then simply add this one, otherwise this
			 * one will be the top
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
	public void refreshAll() {

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

	/**
	 * Scroll back by one scroll unit.
	 */
	private void scrollBack(double factor) {
		if (charts.isEmpty()) return;
		PlotData plotData = charts.get(0).getDataContext().getPlotData();
		EventQueue.invokeLater(() -> {
			int scroll = plotData.getBarsToScrollOrZoom(factor);
			if (plotData.scroll(scroll)) refreshAll();
		});
	}

	/**
	 * Scroll front by one scroll unit.
	 */
	private void scrollFront(double factor) {
		if (charts.isEmpty()) return;
		PlotData plotData = charts.get(0).getDataContext().getPlotData();
		EventQueue.invokeLater(() -> {
			int scroll = plotData.getBarsToScrollOrZoom(factor) * (-1);
			if (plotData.scroll(scroll)) refreshAll();
		});
	}

	/**
	 * Scroll to start.
	 */
	private void scrollStart() {
		if (charts.isEmpty()) return;
		PlotData plotData = charts.get(0).getDataContext().getPlotData();
		EventQueue.invokeLater(() -> {
			if (plotData.scrollStart()) refreshAll();
		});
	}

	/**
	 * Scroll to end.
	 */
	private void scrollEnd() {
		if (charts.isEmpty()) return;
		PlotData plotData = charts.get(0).getDataContext().getPlotData();
		EventQueue.invokeLater(() -> {
			if (plotData.scrollEnd()) refreshAll();
		});
	}

	/**
	 * Zoom in.
	 */
	private void zoomIn(double factor) {
		if (charts.isEmpty()) return;
		PlotData plotData = charts.get(0).getDataContext().getPlotData();
		EventQueue.invokeLater(() -> {
			int zoom = plotData.getBarsToScrollOrZoom(factor);
			if (plotData.zoom(zoom)) refreshAll();
		});
	}

	/**
	 * Zoom out.
	 */
	private void zoomOut(double factor) {
		if (charts.isEmpty()) {
			return;
		}
		PlotData plotData = charts.get(0).getDataContext().getPlotData();
		EventQueue.invokeLater(() -> {
			int zoom = plotData.getBarsToScrollOrZoom(factor) * (-1);
			if (plotData.zoom(zoom)) refreshAll();
		});
	}
}
