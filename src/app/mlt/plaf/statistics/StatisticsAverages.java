/*
 * Copyright (C) 2018 Miquel Sas
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package app.mlt.plaf.statistics;

import java.awt.Color;
import java.awt.RenderingHints;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.swing.Icon;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.mlt.db.Field;
import com.mlt.db.FieldGroup;
import com.mlt.db.ListPersistor;
import com.mlt.db.Persistor;
import com.mlt.db.PersistorException;
import com.mlt.db.Record;
import com.mlt.db.RecordSet;
import com.mlt.db.Table;
import com.mlt.db.View;
import com.mlt.db.rdbms.DBPersistor;
import com.mlt.desktop.Option;
import com.mlt.desktop.Option.Group;
import com.mlt.desktop.TaskFrame;
import com.mlt.desktop.action.ActionRun;
import com.mlt.desktop.control.Canvas.Context;
import com.mlt.desktop.control.Control;
import com.mlt.desktop.control.PopupMenu;
import com.mlt.desktop.control.PopupMenuProvider;
import com.mlt.desktop.control.TablePane;
import com.mlt.desktop.control.TableRecord;
import com.mlt.desktop.control.TableRecordModel;
import com.mlt.desktop.control.table.SelectionMode;
import com.mlt.desktop.converters.NumberScaleConverter;
import com.mlt.desktop.graphic.Path;
import com.mlt.desktop.graphic.Stroke;
import com.mlt.desktop.icon.IconGrid;
import com.mlt.desktop.icon.Icons;
import com.mlt.mkt.chart.ChartContainer;
import com.mlt.mkt.chart.DataContext;
import com.mlt.mkt.chart.plotter.DataPlotter;
import com.mlt.mkt.chart.plotter.data.CandlestickPlotter;
import com.mlt.mkt.chart.plotter.data.LinePlotter;
import com.mlt.mkt.data.Data;
import com.mlt.mkt.data.DataConverter;
import com.mlt.mkt.data.DataList;
import com.mlt.mkt.data.DataListSource;
import com.mlt.mkt.data.DataRecordSet;
import com.mlt.mkt.data.Instrument;
import com.mlt.mkt.data.OHLC;
import com.mlt.mkt.data.Period;
import com.mlt.mkt.data.PlotData;
import com.mlt.mkt.data.info.DataInfo;
import com.mlt.ml.function.Normalizer;
import com.mlt.util.Formats;
import com.mlt.util.HTML;
import com.mlt.util.Lists;
import com.mlt.util.Logs;
import com.mlt.util.Numbers;
import com.mlt.util.StringConverter;
import com.mlt.util.Strings;
import com.mlt.util.xml.parser.Parser;
import com.mlt.util.xml.parser.ParserHandler;
import com.mlt.util.xml.writer.XMLAttribute;
import com.mlt.util.xml.writer.XMLWriter;

import app.mlt.plaf.DB;
import app.mlt.plaf.MLT;

/**
 * Statistics built on a list of averages. Average statistics is the main source
 * of data to train the networks to trade a financial product. Two main table
 * are created:
 * <ul>
 * <li>
 * <b>States</b> contains all the defined values to train the corresponding
 * networks, either with supervised data or by reinforcement learning,
 * normallized when necessary and described below.
 * </li>
 * </ul>
 *
 * @author Miquel Sas
 */
public class StatisticsAverages extends Statistics {

	private static final String KEY_PRICES_AND_AVERAGES = "prices_and_averages";
	private static final String KEY_SLOPES = "slopes";
	private static final String KEY_SPREADS = "spreads";
	private static final String KEY_ZIGZAG = "zig-zag";

	private static final String KEY_CANDLES(int size) {
		return "candles-" + size;
	}

	/**
	 * Chart the ticker.
	 */
	class ActionChartStates extends ActionRun {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			try {

				String key = getTabKey("STATS-CHART");
				String text = getTabText("Chart");
				MLT.getStatusBar().setProgressIndeterminate(key, "Setup " + text, true);

				ChartContainer chart = new ChartContainer();
				chart.setPopupMenuProvider(new MenuChart());
				chart.addPlotData(getPlotDataPriceAndAverages());
				chart.addPlotData(getPlotDataSlopes());
				chart.addPlotData(getPlotDataSpreads());

				Icon icon = Icons.getIcon(Icons.APP_16x16_CHART);
				MLT.getTabbedPane().addTab(key, icon, text, text, chart);

				MLT.getStatusBar().removeProgress(key);

			} catch (Exception exc) {
				Logs.catching(exc);
			}
		}
	}

	/**
	 * Browse the statistics.
	 */
	class ActionBrowseStats extends ActionRun {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			try {
				String key = getTabKey("STATS-BROWSE");
				String text = getTabText("States");

				MLT.getStatusBar().setProgressIndeterminate(key, "Setup " + text, true);

				ListPersistor persistor = new ListPersistor(getTableStates().getPersistor());
				persistor.setCacheSize(40000);
				persistor.setPageSize(100);

				TableRecordModel model = new TableRecordModel(persistor.getDefaultRecord());
				model.addColumn(DB.FIELD_BAR_TIME_FMT);
				model.addColumn(DB.FIELD_BAR_OPEN);
				model.addColumn(DB.FIELD_BAR_HIGH);
				model.addColumn(DB.FIELD_BAR_LOW);
				model.addColumn(DB.FIELD_BAR_CLOSE);

				List<Field> fields = null;

				fields = getFieldListAverages();
				for (int i = 0; i < fields.size(); i++) {
					model.addColumn(fields.get(i).getAlias());
				}

				fields = getFieldListSlopes("raw");
				for (int i = 0; i < fields.size(); i++) {
					model.addColumn(fields.get(i).getAlias());
				}

				fields = getFieldListSpreads("raw");
				for (int i = 0; i < fields.size(); i++) {
					model.addColumn(fields.get(i).getAlias());
				}

				fields = getFieldListSlopes("nrm");
				for (int i = 0; i < fields.size(); i++) {
					model.addColumn(fields.get(i).getAlias());
				}

				fields = getFieldListSpreads("nrm");
				for (int i = 0; i < fields.size(); i++) {
					model.addColumn(fields.get(i).getAlias());
				}

				for (int i = 0; i < averages.size(); i++) {
					fields = getFieldListCandles(i);
					for (int j = 0; j < fields.size(); j++) {
						model.addColumn(fields.get(j).getAlias());
					}
				}

				model.setRecordSet(new DataRecordSet(persistor));

				TableRecord table = new TableRecord(true);
				table.setSelectionMode(SelectionMode.SINGLE_ROW_SELECTION);
				table.setModel(model);
				table.setSelectedRow(0);
				table.setPopupMenuProvider(new MenuStates(table));

				TablePane tablePane = new TablePane(table);

				IconGrid iconGrid = new IconGrid();
				iconGrid.setSize(16, 16);
				iconGrid.setMarginFactors(0.12, 0.12, 0.12, 0.12);

				MLT.getTabbedPane().addTab(key, iconGrid, text, "Defined ", tablePane);
				MLT.getStatusBar().removeProgress(key);

			} catch (Exception exc) {
				Logs.catching(exc);
			}
		}
	}

	/**
	 * Browse the ranges table.
	 */
	class ActionBrowseRanges extends ActionRun {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			try {
				String key = getTabKey("STATS-RANGES");
				String text = getTabText("Ranges");

				Persistor persistor = getTableRanges().getPersistor();

				TableRecordModel model = new TableRecordModel(persistor.getDefaultRecord());
				model.addColumn(DB.FIELD_RANGE_NAME);
				model.addColumn(DB.FIELD_RANGE_MINIMUM);
				model.addColumn(DB.FIELD_RANGE_MAXIMUM);
				model.addColumn(DB.FIELD_RANGE_AVERAGE);
				model.addColumn(DB.FIELD_RANGE_STDDEV);
				model.addColumn(DB.FIELD_RANGE_AVG_STD_10);
				model.addColumn(DB.FIELD_RANGE_AVG_STD_20);

				model.setRecordSet(persistor.select(null));

				TableRecord table = new TableRecord(true);
				table.setSelectionMode(SelectionMode.SINGLE_ROW_SELECTION);
				table.setModel(model);
				table.setSelectedRow(0);

				TablePane tablePane = new TablePane(table);

				IconGrid iconGrid = new IconGrid();
				iconGrid.setSize(16, 16);
				iconGrid.setMarginFactors(0.12, 0.12, 0.12, 0.12);

				MLT.getTabbedPane().addTab(key, iconGrid, text, "Defined ", tablePane);

			} catch (Exception exc) {
				Logs.catching(exc);
			}
		}
	}

	/**
	 * Action to calculate the statistics by a list of tasks.
	 */
	class ActionCalculate extends ActionRun {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			TaskFrame frame = new TaskFrame();
			frame.setTitle(getLabel());

//			TaskList task = new TaskList();
//			task.setId("tasks");
//			task.setTitle("Calculate statistics");
//			task.addTask(new TaskAveragesRaw(StatisticsAverages.this));
//			task.addTask(new TaskAveragesRanges(StatisticsAverages.this));
//			frame.addTasks(task);

			frame.addTasks(new TaskAveragesRaw(StatisticsAverages.this));
			frame.addTasks(new TaskAveragesRanges(StatisticsAverages.this));
			frame.addTasks(new TaskAveragesNormalize(StatisticsAverages.this));
			frame.addTasks(new TaskAveragesZigZag(StatisticsAverages.this));
			frame.show();
		}

	}

	/**
	 * Track data and its index.
	 */
	class Candle {
		int index;
		Data data;

		Candle(int index, Data data) {
			this.index = index;
			this.data = data;
		}
	}

	/**
	 * Data information formatter for the candles plotter.
	 */
	class CandlesFormatter implements DataInfo.Formatter {

		int size;
		int count;

		int indexOpen;
		int indexHigh;
		int indexLow;
		int indexClose;

		CandlesFormatter(int size, int count) {
			this.size = size;
			this.count = count;

			this.indexOpen = getStatesDataConverter().getIndex(DB.FIELD_BAR_OPEN);
			this.indexHigh = getStatesDataConverter().getIndex(DB.FIELD_BAR_HIGH);
			this.indexLow = getStatesDataConverter().getIndex(DB.FIELD_BAR_LOW);
			this.indexClose = getStatesDataConverter().getIndex(DB.FIELD_BAR_CLOSE);
		}

		@Override
		public String getInfoData(DataInfo info, PlotData plotData, int listIndex, int dataIndex) {

			StringBuilder b = new StringBuilder();

			DataList dataList = plotData.get(listIndex);
			int startIndex = plotData.getStartIndex();
			int endIndex = plotData.getEndIndex();

			List<Candle> candles = new ArrayList<>();
			int countPlotted = 0;
			for (int index = endIndex; index >= 0; index--) {

				/* Plotted all required. */
				if (!plotAllCandles && countPlotted >= count) {
					break;
				}

				/* Add the candle. */
				if (index >= 0 && index < dataList.size()) {
					candles.add(new Candle(index, dataList.get(index)));
				}

				/* Process candles if required. */
				boolean indexIncluded = false;
				if (candles.size() == size || index == 0) {
					int start = candles.get(candles.size() - 1).index;
					int end = candles.get(0).index;
					if (start <= dataIndex && end >= dataIndex) {
						int tickScale = info.getTickScale();
						Locale locale = Locale.getDefault();
						indexIncluded = true;
						double open = candles.get(candles.size() - 1).data.getValue(indexOpen);
						double high = Numbers.MIN_DOUBLE;
						double low = Numbers.MAX_DOUBLE;
						double close = candles.get(0).data.getValue(indexClose);
						for (int i = candles.size() - 1; i >= 0; i--) {
							Candle candle = candles.get(i);
							if (candle.data.getValue(indexHigh) > high) {
								high = candle.data.getValue(indexHigh);
							}
							if (candle.data.getValue(indexLow) < low) {
								low = candle.data.getValue(indexLow);
							}
						}
						b.append("O: ");
						b.append(Formats.fromDouble(open, tickScale, locale));
						b.append(", H: ");
						b.append(Formats.fromDouble(high, tickScale, locale));
						b.append(", L: ");
						b.append(Formats.fromDouble(low, tickScale, locale));
						b.append(", C: ");
						b.append(Formats.fromDouble(close, tickScale, locale));
					}
					countPlotted++;
					candles.clear();

					/* If the index was in the candle, done. */
					if (indexIncluded) {
						break;
					}

					/* Exit if plotted the first candle. */
					if (start <= startIndex) {
						break;
					}
				}

			}

			return b.toString();
		}
	}

	/**
	 * Popup menu for the chart.
	 */
	class MenuChart implements PopupMenuProvider {

		@Override
		public PopupMenu getPopupMenu(Control control) {
			if (control instanceof ChartContainer) {
				PopupMenu popup = new PopupMenu();
				ChartContainer container = (ChartContainer) control;
				if (!container.containsPlotData(KEY_PRICES_AND_AVERAGES)) {
					Option option = new Option();
					option.setKey(KEY_PRICES_AND_AVERAGES);
					option.setText("Prices and averages");
					option.setToolTip("Prices and averages");
					option.setOptionGroup(Group.CONFIGURE);
					option.setDefaultClose(false);
					option.setCloseWindow(false);
					option.setAction(
						l -> container.addPlotData(getPlotDataPriceAndAverages(), true));
					popup.add(option.getMenuItem());
				}
				if (!container.containsPlotData(KEY_SLOPES)) {
					Option option = new Option();
					option.setKey(KEY_SLOPES);
					option.setText("Slopes of averages");
					option.setToolTip("Slopes of averages");
					option.setOptionGroup(Group.CONFIGURE);
					option.setDefaultClose(false);
					option.setCloseWindow(false);
					option.setAction(l -> container.addPlotData(getPlotDataSlopes(), true));
					popup.add(option.getMenuItem());
				}
				if (!container.containsPlotData(KEY_SPREADS)) {
					Option option = new Option();
					option.setKey(KEY_SPREADS);
					option.setText("Spreads between averages");
					option.setToolTip("Spreads between averages");
					option.setOptionGroup(Group.CONFIGURE);
					option.setDefaultClose(false);
					option.setCloseWindow(false);
					option.setAction(l -> container.addPlotData(getPlotDataSpreads(), true));
					popup.add(option.getMenuItem());
				}
				if (container.containsPlotData(KEY_PRICES_AND_AVERAGES)) {
					PlotData plotData = container.getPlotData(KEY_PRICES_AND_AVERAGES);
					PlotterZigZag zz = new PlotterZigZag();
					if (plotData.get(0).isPlotter(zz)) {
						Option option = new Option();
						option.setKey(KEY_ZIGZAG);
						option.setText("Remove zig-zag plotter");
						option.setToolTip("Remove zig-zag plotter");
						option.setOptionGroup(Group.CONFIGURE);
						option.setDefaultClose(false);
						option.setCloseWindow(false);
						option.setAction(l -> {
							plotData.get(0).removePlotter(zz);
							container.refreshAll();
						});
						popup.add(option.getMenuItem());
					} else {
						Option option = new Option();
						option.setKey(KEY_ZIGZAG);
						option.setText("Add zig-zag plotter");
						option.setToolTip("Add zig-zag plotter");
						option.setOptionGroup(Group.CONFIGURE);
						option.setDefaultClose(false);
						option.setCloseWindow(false);
						option.setAction(l -> {
							plotData.get(0).addPlotter(zz);
							container.refreshAll();
						});
						popup.add(option.getMenuItem());
					}
				}
				for (int i = 0; i < averages.size() - 1; i++) {
					int periodFast = averages.get(i).getPeriod();
					int periodSlow = averages.get(i + 1).getPeriod();
					int size = periodFast;
					int mult = averages.size() - i - 1;
					int count = (periodSlow / periodFast) * mult;
					if (!container.containsPlotData(KEY_CANDLES(size))) {
						Option option = new Option();
						option.setKey(KEY_CANDLES(size));
						option.setText("Add candles pane of size " + size);
						option.setToolTip("Add candles pane of size " + size);
						option.setOptionGroup(Group.CONFIGURE);
						option.setDefaultClose(false);
						option.setCloseWindow(false);
						PlotData plotData = getPlotDataCandles(size, count);
						option.setAction(l -> container.addPlotData(plotData, true));
						popup.add(option.getMenuItem());
					}
				}
				for (int i = 0; i < averages.size() - 1; i++) {
					int size = averages.get(i).getPeriod();
					;
					if (container.containsPlotData(KEY_CANDLES(size))) {
						Option option = new Option();
						option.setKey("toggle-plot-all-candles");
						if (plotAllCandles) {
							option.setText("Plot only level candles (size)");
							option.setToolTip("Plot only the candles of the level (size)");
						} else {
							option.setText("Plot all possible candles");
							option.setToolTip("Plot all possible candles");
						}
						option.setOptionGroup(Group.CONFIGURE);
						option.setDefaultClose(false);
						option.setCloseWindow(false);
						option.setAction(l -> {
							plotAllCandles = !plotAllCandles;
							container.refreshAll();
						});
						popup.add(option.getMenuItem());
					}
				}
				return popup;
			}
			return null;
		}
	}

	/**
	 * Popup menu for browse states.
	 */
	class MenuStates implements PopupMenuProvider {
		TableRecord table;

		MenuStates(TableRecord table) {
			this.table = table;
		}

		@Override
		public PopupMenu getPopupMenu(Control control) {
			PopupMenu popup = new PopupMenu();
			popup.add(Option.option_COLUMNS(table).getMenuItem());
			return popup;
		}

	}

	/**
	 * Parameters handler.
	 */
	public static class ParametersHandler extends ParserHandler {

		/** List of averages. */
		List<Average> averages = new ArrayList<>();
		/** Bars ahead. */
		int barsAhead;
		/** Percentage for calculated labels. */
		double percentCalc;
		/** Percentage for edited labels. */
		double percentEdit;

		/**
		 * Constructor.
		 */
		public ParametersHandler() {
			super();

			/* Setup valid paths. */
			set("statistics");
			set("statistics/averages");
			set("statistics/averages/average", "type", "string");
			set("statistics/averages/average", "period", "integer");
			set("statistics/averages/average", "smooths", "integer-array", false);
			set("statistics/zig-zag", "bars-ahead", "integer");
			set("statistics/label-calc", "percent", "double");
			set("statistics/label-edit", "percent", "double");
		}

		/**
		 * Called to notify an element start.
		 */
		@Override
		public void elementStart(
			String namespace,
			String elementName,
			String path,
			Attributes attributes) throws SAXException {

			try {
				/* Validate the path. */
				validate(path, attributes);

				/* Validate and retrieve attributes of averages/average path. */
				if (path.equals("statistics/averages/average")) {

					/* Type. */
					String attrType = getString(attributes, "type");
					Average.Type type = Average.Type.valueOf(attrType);

					/* Period. */
					int period = getInteger(attributes, "period");
					if (period <= 0) {
						throw new Exception("Invalid period " + period);
					}

					/* Smooths. */
					int[] smooths = getIntegerArray(attributes, "smooths");

					/* Add the average. */
					averages.add(new Average(type, period, smooths));
				}

				/* Validate and retrieve bars ahead parameter. */
				if (path.equals("statistics/zig-zag")) {
					barsAhead = getInteger(attributes, "bars-ahead");
					if (barsAhead <= 0) {
						throw new Exception("Invalid bars-ahead " + barsAhead);
					}
				}
				/* Validate and retrieve percent calc. */
				if (path.equals("statistics/label-calc")) {
					percentCalc = getDouble(attributes, "percent");
					if (percentCalc <= 0 || percentCalc >= 50) {
						throw new Exception("Invalid percentage for label calc " + percentCalc);
					}
				}
				/* Validate and retrieve percent edit. */
				if (path.equals("statistics/label-edit")) {
					percentEdit = getDouble(attributes, "percent");
					if (percentEdit <= 0 || percentEdit >= 50) {
						throw new Exception("Invalid percentage for label calc " + percentCalc);
					}
				}

			} catch (Exception exc) {
				throw new SAXException(exc.getMessage(), exc);
			}
		}
	}

	/**
	 * Candle plotter for the states data list. This plotter will plot the first
	 * candle of the iter-averages candles, that is, for instance, 5 periods candle,
	 * 20 periods candle, etc.
	 */
	class PlotterCandles extends DataPlotter {

		int size;
		int count;

		int indexOpen;
		int indexHigh;
		int indexLow;
		int indexClose;

		PlotterCandles(int size, int count) {
			super(KEY_CANDLES(size));

			this.size = size;
			this.count = count;

			this.indexOpen = getStatesDataConverter().getIndex(DB.FIELD_BAR_OPEN);
			this.indexHigh = getStatesDataConverter().getIndex(DB.FIELD_BAR_HIGH);
			this.indexLow = getStatesDataConverter().getIndex(DB.FIELD_BAR_LOW);
			this.indexClose = getStatesDataConverter().getIndex(DB.FIELD_BAR_CLOSE);

			setIndexes(indexOpen, indexHigh, indexLow, indexClose);
		}

		@Override
		public void plot(Context ctx, DataList dataList, int startIndex, int endIndex) {

			/*
			 * Plot candles of size periods starting at endIndex and moving backward.
			 */
			List<Candle> candles = new ArrayList<>();
			int countPlotted = 0;
			for (int index = endIndex; index >= 0; index--) {

				/* Plotted all required. */
				if (!plotAllCandles && countPlotted >= count) {
					break;
				}

				/* Add the candle. */
				if (index >= 0 && index < dataList.size()) {
					candles.add(new Candle(index, dataList.get(index)));
				}

				/* Process candles if required. */
				if (candles.size() == size || index == 0) {
					int start = candles.get(candles.size() - 1).index;
					int end = candles.get(0).index;
					double open = candles.get(candles.size() - 1).data.getValue(indexOpen);
					double high = Numbers.MIN_DOUBLE;
					double low = Numbers.MAX_DOUBLE;
					double close = candles.get(0).data.getValue(indexClose);
					for (int i = candles.size() - 1; i >= 0; i--) {
						Candle candle = candles.get(i);
						if (candle.data.getValue(indexHigh) > high) {
							high = candle.data.getValue(indexHigh);
						}
						if (candle.data.getValue(indexLow) < low) {
							low = candle.data.getValue(indexLow);
						}
					}
					plot(ctx, start, end, open, high, low, close);
					countPlotted++;
					candles.clear();

					/* Exit if plotted the first candle. */
					if (start <= startIndex) {
						break;
					}
				}

			}

		}

		private void plot(
			Context ctx,
			int startIndex,
			int endIndex,
			double open,
			double high,
			double low,
			double close) {

			boolean bullish = (close >= open);
			DataContext dc = getContext();

			double openY = dc.getCoordinateY(open);
			double highY = dc.getCoordinateY(high);
			double lowY = dc.getCoordinateY(low);
			double closeY = dc.getCoordinateY(close);
			double bodyHigh = (bullish ? closeY : openY);
			double bodyLow = (bullish ? openY : closeY);

			double periodXStart = dc.getCoordinateX(startIndex);
			double centerXStart = dc.getCenterCoordinateX(periodXStart);
			double periodXEnd = dc.getCoordinateX(endIndex);
			double centerXEnd = dc.getCenterCoordinateX(periodXEnd);
			double margin = getDataMargin(dc);
			double candleWidth = centerXEnd - centerXStart + (2 * margin);
			double candleCenter = (centerXStart + centerXEnd) / 2;
			double candleStart = candleCenter - (candleWidth / 2);

			Color fillColor;
			if (bullish) {
				fillColor = getColorBullishEven();
			} else {
				fillColor = getColorBearishEven();
			}

			Path path = new Path();
			path.setStroke(new Stroke(1.0));
			if (candleWidth <= 1) {
				path.setDrawPaint(fillColor);
				path.moveTo(centerXStart, highY);
				path.lineTo(centerXStart, lowY);
				ctx.draw(path);
			} else {
				path.setDrawPaint(Color.BLACK);
				path.setFillPaint(fillColor);
				/* Upper shadow. */
				path.moveTo(candleCenter, highY);
				path.lineTo(candleCenter, bodyHigh);
				/* Body. */
				path.moveTo(candleStart, bodyHigh);
				path.lineTo(candleStart + candleWidth, bodyHigh);
				path.lineTo(candleStart + candleWidth, bodyLow);
				path.lineTo(candleStart, bodyLow);
				path.lineTo(candleStart, bodyHigh);
				/* Lower shadow. */
				path.moveTo(candleCenter, bodyLow);
				path.lineTo(candleCenter, lowY);
				ctx.fill(path);
				ctx.draw(path);
			}
		}
	}

	/**
	 * Zig-zag data plotter for the states data list.
	 */
	class PlotterZigZag extends DataPlotter {

		class Pivot {
			double pivot;
			double value;
			int index;

			Pivot(double pivot, double value, int index) {
				this.pivot = pivot;
				this.value = value;
				this.index = index;
			}

		}

		int indexPivot;
		int indexData;

		PlotterZigZag() {
			super(KEY_ZIGZAG);

			indexPivot = getStatesDataConverter().getIndex(DB.FIELD_STATES_PIVOT_CALC);
			indexData = getStatesDataConverter().getIndex(DB.FIELD_STATES_REFV_CALC);
			setIndex(getStatesDataConverter().getIndex(DB.FIELD_BAR_CLOSE));
		}

		@Override
		public void plot(Context ctx, DataList dataList, int startIndex, int endIndex) {

			/*
			 * Build the first list of pivots visible.
			 */
			List<Pivot> pivots = new ArrayList<>();
			for (int index = startIndex; index <= endIndex; index++) {
				if (index < 0 || index > dataList.size()) {
					continue;
				}
				Data data = dataList.get(index);
				double pivot = data.getValue(indexPivot);
				if (pivot != 0) {
					double value = data.getValue(indexData);
					pivots.add(new Pivot(pivot, value, index));
				}
			}
			/*
			 * Scan from first pivot backward and analyze if there is any other pivot before
			 * to insert it as the first element of the list. I there is none, then scan up
			 * to the begining tracking minimum and maximum values and indexes and create
			 * the appropriate initial pivot.
			 */
			boolean anyPivotBefore = false;
			int firstIndex = (!pivots.isEmpty() ? pivots.get(0).index : startIndex);
			for (int index = firstIndex - 1; index >= 0; index--) {
				Data data = dataList.get(index);
				double pivot = data.getValue(indexPivot);
				if (pivot != 0) {
					double value = data.getValue(indexData);
					pivots.add(0, new Pivot(pivot, value, index));
					anyPivotBefore = true;
					break;
				}
			}
			if (!anyPivotBefore) {
				double minValue = Numbers.MAX_DOUBLE;
				double maxValue = Numbers.MIN_DOUBLE;
				int minIndex = -1;
				int maxIndex = -1;
				for (int index = firstIndex - 1; index >= 0; index--) {
					Data data = dataList.get(index);
					double value = data.getValue(indexData);
					if (value < minValue) {
						minValue = value;
						minIndex = index;
					}
					if (value > maxValue) {
						maxValue = value;
						maxIndex = index;
					}
				}
				if (pivots.get(0).pivot == 1) {
					pivots.add(0, new Pivot(-1, minValue, minIndex));
				} else {
					pivots.add(0, new Pivot(1, maxValue, maxIndex));
				}
			}
			/*
			 * Scan from last pivot forward and analyze if there is any other pivot after to
			 * add it as the last element of the list. I there is none, then scan up to the
			 * end tracking minimum and maximum values and indexes and create the
			 * appropriate final pivot.
			 */
			boolean anyPivotAfter = false;
			int lastIndex = (!pivots.isEmpty() ? pivots.get(pivots.size() - 1).index : endIndex);
			for (int index = lastIndex + 1; index < dataList.size(); index++) {
				Data data = dataList.get(index);
				double pivot = data.getValue(indexPivot);
				if (pivot != 0) {
					double value = data.getValue(indexData);
					pivots.add(new Pivot(pivot, value, index));
					anyPivotAfter = true;
					break;
				}
			}
			if (!anyPivotAfter) {
				double minValue = Numbers.MAX_DOUBLE;
				double maxValue = Numbers.MIN_DOUBLE;
				int minIndex = -1;
				int maxIndex = -1;
				for (int index = lastIndex + 1; index < dataList.size(); index++) {
					Data data = dataList.get(index);
					double value = data.getValue(indexData);
					if (value < minValue) {
						minValue = value;
						minIndex = index;
					}
					if (value > maxValue) {
						maxValue = value;
						maxIndex = index;
					}
				}
				if (pivots.get(pivots.size() - 1).pivot == 1) {
					pivots.add(new Pivot(-1, minValue, minIndex));
				} else {
					pivots.add(new Pivot(1, maxValue, maxIndex));
				}
			}

			/*
			 * Do plot.
			 */
			DataContext dc = getContext();
			Path path = new Path();
			path.setStroke(new Stroke(2.0));
			path.addHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			path.setDrawPaint(Color.BLACK);
			double x, y;
			for (int i = 0; i < pivots.size(); i++) {
				Pivot pivot = pivots.get(i);
				x = dc.getCenterCoordinateX(dc.getCoordinateX(pivot.index));
				y = dc.getCoordinateY(pivot.value);
				if (i == 0) {
					path.moveTo(x, y);
				} else {
					path.lineTo(x, y);
				}
			}
			ctx.draw(path);
		}
	}

	/**
	 * Return statistics averages from a statistics record.
	 * 
	 * @param rc The statistics definition record.
	 * @return The statistics object.
	 */
	public static StatisticsAverages getStatistics(Record rc) throws Exception {

		/* Instrument and period. */
		Instrument instrument = DB.to_instrument(rc.getValue(DB.FIELD_INSTRUMENT_ID).getString());
		Period period = DB.to_period(rc.getValue(DB.FIELD_PERIOD_ID).getString());

		/* Statistics averages. */
		StatisticsAverages stats = new StatisticsAverages(instrument, period);
		stats.setId(rc.getValue(DB.FIELD_STATISTICS_ID).getString());
		stats.setKey(rc.getValue(DB.FIELD_STATISTICS_KEY).getString());
		stats.setParameters(rc.getValue(DB.FIELD_STATISTICS_PARAMS).getString());

		return stats;

	}

	/** List of averages. */
	private List<Average> averages = new ArrayList<>();
	/** Bars ahead to calculate zig-zag operator. */
	private int barsAhead = 100;
	/** Percentage to exit calculated pivots. */
	private double percentCalc = 10;
	/** Percentage to exit edited pivots. */
	private double percentEdit = 10;

	/** States table. */
	private Table tableStates;
	/** Ranges table. */
	private Table tableRanges;

	/** Map of field lists. */
	private HashMap<String, List<Field>> mapLists = new HashMap<>();

	/** States data converter. */
	private DataConverter statesDataConverter;
	/** States list persistor. */
	private ListPersistor statesListPersistor;

	/** Plot all candles flag. */
	private boolean plotAllCandles = true;

	/**
	 * Constructor.
	 * 
	 * @param instrument Instrument.
	 * @param period     Period.
	 */
	public StatisticsAverages(Instrument instrument, Period period) {
		super(instrument, period);
	}

	/**
	 * Add an average to the list of averages in a period ascending order.
	 * 
	 * @param avg The average to add.
	 */
	public void addAverage(Average avg) {
		/* First one, just add. */
		if (averages.isEmpty()) {
			averages.add(avg);
			return;
		}
		/* Not exists. */
		if (averages.contains(avg)) {
			throw new IllegalArgumentException("Average exists.");
		}
		/* No same period. */
		for (Average a : averages) {
			if (a.getPeriod() == avg.getPeriod()) {
				throw new IllegalArgumentException("Repeated verage period.");
			}
		}
		/* Period must be greater than previous. */
		if (avg.getPeriod() <= averages.get(averages.size() - 1).getPeriod()) {
			throw new IllegalArgumentException("Period must be greater than the previous one.");
		}
		/* Do add. */
		averages.add(avg);
	}

	/**
	 * Return an unmodifiable copy of the list of averages.
	 * 
	 * @return The list of averages.
	 */
	public List<Average> getAverages() {
		return Collections.unmodifiableList(averages);
	}

	/**
	 * Return the candle field.
	 * 
	 * @param name   Field name.
	 * @param header Field header.
	 * @param fast   Fast period.
	 * @param slow   Slow period.
	 * @param mult   Count multiplier.
	 * @param index  Field index.
	 * @param scale  Format scale.
	 * @return The field.
	 */
	private Field getCandleField(
		String name,
		String header,
		int fast,
		int slow,
		int mult,
		int index,
		int scale) {
		return getCandleField(name, header, fast, slow, mult, index, scale, null);
	}

	/**
	 * @return The number of bars ahead for zig-zag calculation.
	 */
	public int getBarsAhead() {
		return barsAhead;
	}

	/**
	 * Return the candle field.
	 * 
	 * @param name   Field name.
	 * @param header Field header.
	 * @param fast   Fast period.
	 * @param slow   Slow period.
	 * @param mult   Count multiplier.
	 * @param index0 Field first index.
	 * @param index1 Field second index.
	 * @param scale  Format scale.
	 * @param suffix Optional suffix.
	 * @return The field.
	 */
	private Field getCandleField(
		String name,
		String header,
		int fast,
		int slow,
		int mult,
		int index0,
		int index1,
		int scale,
		String suffix) {
		name = getNameCandle(name, fast, slow, mult, index0, index1, suffix);
		header = getHeaderCandle(header, fast, slow, mult, index0, index1, suffix);
		Field field = DB.field_double(name, header);
		field.setStringConverter(getNumberConverter(scale));
		return field;
	}

	/**
	 * Return the candle field.
	 * 
	 * @param name   Field name.
	 * @param header Field header.
	 * @param fast   Fast period.
	 * @param slow   Slow period.
	 * @param mult   Count multiplier.
	 * @param index  Field index.
	 * @param scale  Format scale.
	 * @param suffix Optional suffix.
	 * @return The field.
	 */
	private Field getCandleField(
		String name,
		String header,
		int fast,
		int slow,
		int mult,
		int index,
		int scale,
		String suffix) {
		return getCandleField(name, header, fast, slow, mult, index, -1, scale, suffix);
	}

	/**
	 * Return the name of the field group for candles of the average.
	 * 
	 * @param avg Average index.
	 * @return The name of the field group for candles.
	 */
	public String getCandleGroupName(int avg) {
		int fast = (avg == 0 ? 1 : averages.get(avg - 1).getPeriod());
		int slow = averages.get(avg).getPeriod();
		StringBuilder b = new StringBuilder();
		b.append("candles_");
		b.append(fast);
		b.append("_");
		b.append(slow);
		return b.toString();
	}

	/**
	 * Return the title of the field group for candles of the average.
	 * 
	 * @param avg Average index.
	 * @return The title of the field group for candles.
	 */
	public String getCandleGroupTitle(int avg) {
		int fast = (avg == 0 ? 1 : averages.get(avg - 1).getPeriod());
		int slow = averages.get(avg).getPeriod();
		StringBuilder b = new StringBuilder();
		b.append("Candles ");
		b.append(fast);
		b.append("/");
		b.append(slow);
		return b.toString();
	}

	/**
	 * Return the list of average fields.
	 * 
	 * @return The list of average fields.
	 */
	public List<Field> getFieldListAverages() {
		List<Field> fields = mapLists.get("averages");
		if (fields == null) {
			fields = new ArrayList<>();
			for (int i = 0; i < averages.size(); i++) {
				String name = getNameAverage(i);
				String header = getHeaderAverage(i);
				String label = getLabelAverage(i);
				Field field = DB.field_double(name, header, label);
				field.setStringConverter(getNumberConverter(8));
				fields.add(field);
			}
			mapLists.put("averages", fields);
		}
		return fields;
	}

	/**
	 * Returns the list of candle related fields of an average.
	 * 
	 * @param i The average index.
	 * @return The list of candle related fields.
	 */
	public List<Field> getFieldListCandles(int i) {
		String key = getCandleGroupName(i);
		List<Field> fields = mapLists.get(key);
		if (fields == null) {
			fields = new ArrayList<>();
			int fast = (i == 0 ? 1 : averages.get(i - 1).getPeriod());
			int slow = averages.get(i).getPeriod();
			int mult = averages.size() - i;
			int count = (slow / fast) * mult;
			String name, header;
			for (int j = 0; j < count; j++) {

				/* Time start, raw and fmt. */
				name = getNameCandle(DB.FIELD_BAR_TIME, fast, slow, mult, j);
				header = getHeaderCandle("Time", fast, slow, mult, j);
				fields.add(DB.field_long(name, header));
				fields.add(DB.field_timeFmt(getPeriod(), name, name + "_fmt", header + " fmt"));

				/* Open, high, low, close. */
				fields.add(getCandleField(DB.FIELD_BAR_OPEN, "Open", fast, slow, mult, j, 4));
				fields.add(getCandleField(DB.FIELD_BAR_HIGH, "High", fast, slow, mult, j, 4));
				fields.add(getCandleField(DB.FIELD_BAR_LOW, "Low", fast, slow, mult, j, 4));
				fields.add(
					getCandleField(DB.FIELD_BAR_CLOSE, "Close", fast, slow, mult, j, 4));

				/* Raw values. */
				fields.add(getCandleField(
					DB.FIELD_BAR_RANGE, "Range", fast, slow, mult, j, 4, "raw"));
				fields.add(getCandleField(
					DB.FIELD_BAR_BODY_FACTOR, "Body factor", fast, slow, mult, j, 8, "raw"));
				fields.add(getCandleField(
					DB.FIELD_BAR_BODY_POS, "Body pos", fast, slow, mult, j, 8, "raw"));
				if (j < count - 1) {
					fields.add(getCandleField(
						DB.FIELD_BAR_REL_POS, "Rel pos", fast, slow, mult, j, j + 1, 8, "raw"));
				}
				fields
					.add(getCandleField(DB.FIELD_BAR_SIGN, "Sign", fast, slow, mult, j, 8, "raw"));

				/* Normalized values. */
				fields.add(getCandleField(
					DB.FIELD_BAR_RANGE, "Range", fast, slow, mult, j, 8, "nrm"));
				fields.add(getCandleField(
					DB.FIELD_BAR_BODY_FACTOR, "Body factor", fast, slow, mult, j, 8, "nrm"));
				fields.add(getCandleField(
					DB.FIELD_BAR_BODY_POS, "Body pos", fast, slow, mult, j, 8, "nrm"));
				if (j < count - 1) {
					fields.add(getCandleField(
						DB.FIELD_BAR_REL_POS, "Relative pos", fast, slow, mult, j, j + 1, 8,
						"nrm"));
				}

				/* Sign, continuous from -1 to 1. */
				fields.add(getCandleField(
					DB.FIELD_BAR_SIGN, "Sign", fast, slow, mult, j, 8, "nrm"));

			}
			mapLists.put(key, fields);
		}
		return fields;
	}

	/**
	 * Return the list of fields for slopes. For each slope, a raw and a normalized
	 * value.
	 * 
	 * @param suffix The suffix.
	 * @return The list of fields for slopes.
	 */
	public List<Field> getFieldListSlopes(String suffix) {
		List<Field> fields = mapLists.get("slopes-" + suffix);
		if (fields == null) {
			fields = new ArrayList<>();
			String name, header, label;
			Field field;
			for (int i = 0; i < averages.size(); i++) {
				name = getNameSlope(i, suffix);
				header = getHeaderSlope(i, suffix);
				label = getLabelSlope(i, suffix);
				field = DB.field_double(name, header, label);
				field.setStringConverter(getNumberConverter(8));
				fields.add(field);
			}
			mapLists.put("slopes-" + suffix, fields);
		}
		return fields;
	}

	/**
	 * Return the list of fields for spreads. For each spread, a raw and a normalize
	 * value.
	 * 
	 * @param suffix The suffix.
	 * @return The list of fields for slopes.
	 */
	public List<Field> getFieldListSpreads(String suffix) {
		List<Field> fields = mapLists.get("spreads-" + suffix);
		if (fields == null) {
			fields = new ArrayList<>();
			String name, header, label;
			Field field;
			for (int i = 0; i < averages.size(); i++) {
				for (int j = i + 1; j < averages.size(); j++) {
					name = getNameSpread(i, j, suffix);
					header = getHeaderSpread(i, j, suffix);
					label = getLabelSpread(i, j, suffix);
					field = DB.field_double(name, header, label);
					field.setStringConverter(getNumberConverter(8));
					fields.add(field);
				}
			}
			mapLists.put("spreads-" + suffix, fields);
		}
		return fields;
	}

	/**
	 * Return the list of fields to normalize.
	 * 
	 * @return The list of fields to normalize.
	 */
	public List<Field> getFieldListToNormalize() {
		List<Field> fields = new ArrayList<>();
		for (int i = 0; i < getTableStates().getFieldCount(); i++) {
			Field field = getTableStates().getField(i);
			if (field.getName().endsWith("_raw")) {
				fields.add(field);
			}
		}
		return fields;
	}

	/**
	 * Get the average header.
	 * 
	 * @param index The average index.
	 * @return The header.
	 */
	public String getHeaderAverage(int index) {
		Average avg = averages.get(index);
		return "Avg " + avg.toString();
	}

	/**
	 * Return the header of an Open/High/Low/Close... candle that relates the fast
	 * and slow periods.
	 * 
	 * @param header The name (open/high/low/close)
	 * @param fast   The fast period.
	 * @param slow   The slow period.
	 * @param mult   Count multiplier.
	 * @param index  The index.
	 * @return The name of the candle.
	 */
	public String getHeaderCandle(String header, int fast, int slow, int mult, int index) {
		return getHeaderCandle(header, fast, slow, mult, index, null);
	}

	/**
	 * Return the label of an Open/High/Low/Close... candle that relates the fast
	 * and slow periods.
	 * 
	 * @param label  The name (open/high/low/close)
	 * @param fast   The fast period.
	 * @param slow   The slow period.
	 * @param mult   Count multiplier.
	 * @param index0 The first index.
	 * @param index1 The second index.
	 * @param suffix The suffix.
	 * @return The name of the candle.
	 */
	public String getHeaderCandle(
		String label,
		int fast,
		int slow,
		int mult,
		int index0,
		int index1,
		String suffix) {
		int count = (slow / fast) * mult;
		int pad = Numbers.getDigits(count);
		StringBuilder b = new StringBuilder();
		b.append(label);
		b.append(" ");
		b.append(fast);
		b.append("/");
		b.append(slow);
		b.append(" - ");
		b.append(Strings.leftPad(Integer.toString(index0), pad, "0"));
		if (index1 >= 0) {
			b.append(" vs ");
			b.append(Strings.leftPad(Integer.toString(index1), pad, "0"));
		}
		if (suffix != null) {
			b.append(" - ");
			b.append(suffix);
		}
		return b.toString();
	}

	/**
	 * Return the header of an Open/High/Low/Close... candle that relates the fast
	 * and slow periods.
	 * 
	 * @param header The name (open/high/low/close)
	 * @param fast   The fast period.
	 * @param slow   The slow period.
	 * @param mult   Count multiplier.
	 * @param index  The index.
	 * @param suffix The suffix.
	 * @return The name of the candle.
	 */
	public String getHeaderCandle(
		String header,
		int fast,
		int slow,
		int mult,
		int index,
		String suffix) {
		return getHeaderCandle(header, fast, slow, mult, index, -1, suffix);
	}

	/**
	 * Get the slope header.
	 * 
	 * @param index  The average index.
	 * @param suffix The suffix.
	 * @return The slope header.
	 */
	public String getHeaderSlope(int index, String suffix) {
		Average avg = averages.get(index);
		return "Slope " + avg.getPeriod() + "_" + suffix;
	}

	/**
	 * Get the spread header.
	 * 
	 * @param fastIndex The fast average index.
	 * @param slowIndex The slow average index.
	 * @param suffix    The suffix.
	 * @return The spread header.
	 */
	public String getHeaderSpread(int fastIndex, int slowIndex, String suffix) {
		Average fast = averages.get(fastIndex);
		Average slow = averages.get(slowIndex);
		return "Spread " + fast.getPeriod() + "/" + slow.getPeriod() + " " + suffix;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getLabel() {
		StringBuilder b = new StringBuilder();
		b.append(getInstrument().getId());
		b.append(" ");
		b.append(getPeriod().toString());
		b.append(" ");
		b.append(getId());
		b.append(" ");
		b.append(getKey());
		return b.toString();
	}

	/**
	 * Return the average field label.
	 * 
	 * @param index Index of the average.
	 * @return The label.
	 */
	public String getLabelAverage(int index) {
		Average avg = averages.get(index);
		return "Average " + avg.toString();
	}

	/**
	 * Return the average slope field label.
	 * 
	 * @param index  The average index.
	 * @param suffix The suffix.
	 * @return The slope field label.
	 */
	public String getLabelSlope(int index, String suffix) {
		Average avg = averages.get(index);
		return "Slope " + avg.getPeriod() + "_" + suffix + " value";
	}

	/**
	 * Return the average spread field label.
	 * 
	 * @param indexFast The index of the fast average.
	 * @param indexSlow The index of the slow average.
	 * @param suffix    The suffix.
	 * @return The average spread field label.
	 */
	public String getLabelSpread(int indexFast, int indexSlow, String suffix) {
		Average fast = averages.get(indexFast);
		Average slow = averages.get(indexSlow);
		return "Spread " + fast.getPeriod() + "/" + slow.getPeriod() + " " + suffix + " value";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getLegend() {
		HTML html = new HTML();
		return html.toString();
	}

	/**
	 * Return the name of the average.
	 * 
	 * @param index The average index.
	 * @return The name.
	 */
	public String getNameAverage(int index) {
		Average avg = averages.get(index);
		return "average_" + avg.getPeriod();
	}

	/**
	 * Return the name of an open/high/low/close... candle that relates the fast and
	 * slow periods.
	 * 
	 * @param name  The name (open/high/low/close)
	 * @param fast  The fast period.
	 * @param slow  The slow period.
	 * @param mult  Count multiplier.
	 * @param index The index.
	 * @return The name of the candle.
	 */
	public String getNameCandle(String name, int fast, int slow, int mult, int index) {
		return getNameCandle(name, fast, slow, mult, index, null);
	}

	/**
	 * Return the name of an open/high/low/close... candle that relates the fast and
	 * slow periods.
	 * 
	 * @param name   The name (open/high/low/close)
	 * @param fast   The fast period.
	 * @param slow   The slow period.
	 * @param mult   Count multiplier.
	 * @param index  The index.
	 * @param suffix The suffix.
	 * @return The name of the candle.
	 */
	public String getNameCandle(
		String name,
		int fast,
		int slow,
		int mult,
		int index0,
		int index1,
		String suffix) {
		int count = (slow / fast) * mult;
		int pad = Numbers.getDigits(count);
		StringBuilder b = new StringBuilder();
		b.append(name);
		b.append("_");
		b.append(fast);
		b.append("_");
		b.append(slow);
		b.append("_");
		b.append(Strings.leftPad(Integer.toString(index0), pad, "0"));
		if (index1 >= 0) {
			b.append("vs");
			b.append(Strings.leftPad(Integer.toString(index1), pad, "0"));
		}
		if (suffix != null) {
			b.append("_");
			b.append(suffix);
		}
		return b.toString();
	}

	/**
	 * Return the name of an open/high/low/close... candle that relates the fast and
	 * slow periods.
	 * 
	 * @param name   The name (open/high/low/close)
	 * @param fast   The fast period.
	 * @param slow   The slow period.
	 * @param mult   Count multiplier.
	 * @param index  The index.
	 * @param suffix The suffix.
	 * @return The name of the candle.
	 */
	public String getNameCandle(
		String name,
		int fast,
		int slow,
		int mult,
		int index,
		String suffix) {
		return getNameCandle(name, fast, slow, mult, index, -1, suffix);
	}

	/**
	 * Return the name of the slope.
	 * 
	 * @param index  The average index.
	 * @param suffix The suffix.
	 * @return The name.
	 */
	public String getNameSlope(int index, String suffix) {
		Average avg = averages.get(index);
		return "slope_" + avg.getPeriod() + "_" + suffix;
	}

	/**
	 * Return the name of the spread field.
	 * 
	 * @param indexFast Index of the fas average.
	 * @param indexSlow Index of the slow average.
	 * @param suffix    Suffix (raw-nrm)
	 * @return The name of the spread field.
	 */
	public String getNameSpread(int indexFast, int indexSlow, String suffix) {
		Average fast = averages.get(indexFast);
		Average slow = averages.get(indexSlow);
		return "spread_" + fast.getPeriod() + "_" + slow.getPeriod() + "_" + suffix;
	}

	/**
	 * @return The map of normalizers.
	 */
	public HashMap<String, Normalizer> getNormalizers() throws PersistorException {
		RecordSet rs = getTableRanges().getPersistor().select(null);
		HashMap<String, Normalizer> map = new HashMap<>();
		for (Record rc : rs) {
			String name = rc.getValue(DB.FIELD_RANGE_NAME).getString();
			double average = rc.getValue(DB.FIELD_RANGE_AVERAGE).getDouble();
			double std_dev = rc.getValue(DB.FIELD_RANGE_STDDEV).getDouble();
			double dataHigh = average + (2 * std_dev);
			double dataLow = average - (2 * std_dev);
			Normalizer normalizer = new Normalizer(dataHigh, dataLow, 1, -1);
			map.put(name, normalizer);
		}
		return map;
	}

	/**
	 * Return the number string converter.
	 * 
	 * @param scale The scale
	 * @return The converter.
	 */
	private StringConverter getNumberConverter(int scale) {
		return new NumberScaleConverter(scale);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Option> getOptions() {
		List<Option> options = new ArrayList<>();

		/* Calculate the statistics. */
		Option optionCalculate = new Option();
		optionCalculate.setKey("CALCULATE");
		optionCalculate.setText("Calculate");
		optionCalculate.setToolTip("Calculate all statistics values");
		optionCalculate.setAction(new ActionCalculate());
		optionCalculate.setOptionGroup(new Option.Group("CALCULATE", 1));
		optionCalculate.setSortIndex(1);
		options.add(optionCalculate);

		/* Browse statictics. */
		Option optionBrowseStats = new Option();
		optionBrowseStats.setKey("BROWSE-STATS");
		optionBrowseStats.setText("Browse states");
		optionBrowseStats.setToolTip("Browse state values");
		optionBrowseStats.setAction(new ActionBrowseStats());
		optionBrowseStats.setOptionGroup(new Option.Group("BROWSE", 2));
		optionBrowseStats.setSortIndex(1);
		options.add(optionBrowseStats);

		/* Browse ranges. */
		Option optionBrowseRanges = new Option();
		optionBrowseRanges.setKey("BROWSE-RANGES");
		optionBrowseRanges.setText("Browse ranges");
		optionBrowseRanges.setToolTip("Browse range values");
		optionBrowseRanges.setAction(new ActionBrowseRanges());
		optionBrowseRanges.setOptionGroup(new Option.Group("BROWSE", 2));
		optionBrowseRanges.setSortIndex(2);
		options.add(optionBrowseRanges);

		/* Chart states. */
		Option optionCharStates = new Option();
		optionCharStates.setKey("CHART-STATES");
		optionCharStates.setText("Chart states");
		optionCharStates.setToolTip("Chart on states data");
		optionCharStates.setAction(new ActionChartStates());
		optionCharStates.setOptionGroup(new Option.Group("CHART", 3));
		optionCharStates.setSortIndex(1);
		options.add(optionCharStates);

		return options;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getParameters() {
		StringWriter sw = new StringWriter();
		XMLWriter xw = new XMLWriter(sw);

		/* Header. */
		xw.printHeader();

		/* Start averages. */
		xw.printTagStart("averages");
		xw.increaseTabLevel();

		for (Average average : averages) {
			XMLAttribute type = new XMLAttribute("type", average.getType().toString());
			XMLAttribute period = new XMLAttribute("period", average.getPeriod());
			int[] smoothArray = average.getSmooths();
			StringBuilder b = new StringBuilder();
			if (smoothArray != null) {
				for (int i = 0; i < smoothArray.length; i++) {
					if (i > 0) {
						b.append(", ");
					}
					b.append(smoothArray[i]);
				}
			}
			XMLAttribute smooths = new XMLAttribute("smooths", b.toString());
			xw.printTag("average", type, period, smooths);
		}

		/* End averages. */
		xw.decreaseTabLevel();
		xw.printTagEnd();

		xw.close();
		return sw.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getParametersDescription() {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < averages.size(); i++) {
			if (i > 0) {
				b.append("; ");
			}
			b.append(averages.get(i).toString());
		}
		return b.toString();
	}

	/**
	 * @return The percentage for calculated labels.
	 */
	public double getPercentCalc() {
		return percentCalc;
	}

	/**
	 * @return The percentage for edited labels.
	 */
	public double getPercentEdit() {
		return percentEdit;
	}

	/**
	 * @param size  The size of the candles.
	 * @param count The number of candles.
	 * @return The plot data for candles of the given size.
	 */
	private PlotData getPlotDataCandles(int size, int count) {

		/* Data info and data list. */
		DataInfo info = new DataInfo(new CandlesFormatter(size, count));
		info.setInstrument(getInstrument());
		info.setName(KEY_CANDLES(size));
		info.setDescription("Candles size " + size);
		info.setPeriod(getPeriod());
		DataListSource dataList =
			new DataListSource(info, getStatesListPersistor(), getStatesDataConverter());

		dataList.addPlotter(new PlotterCandles(size, count));

		PlotData plotData = new PlotData(KEY_CANDLES(size));
		plotData.add(dataList);

		return plotData;
	}

	/**
	 * @return The plot data for states prices and averages.
	 */
	private PlotData getPlotDataPriceAndAverages() {

		/* Data info and data list. */
		DataInfo info = new DataInfo();
		info.setInstrument(getInstrument());
		info.setName("prices-averages");
		info.setDescription("States prices and averages");
		info.setPeriod(getPeriod());
		DataListSource dataList =
			new DataListSource(info, getStatesListPersistor(), getStatesDataConverter());

		/* Prices and plotter. */
		info.addOutput("Open", "O", OHLC.OPEN, "Open data value");
		info.addOutput("High", "H", OHLC.HIGH, "High data value");
		info.addOutput("Low", "L", OHLC.LOW, "Low data value");
		info.addOutput("Close", "C", OHLC.CLOSE, "Close data value");
		dataList.addPlotter(new CandlestickPlotter());

		/* Averages. */
		for (int i = 0; i < averages.size(); i++) {
			String name = averages.get(i).toString();
			Field field = getFieldListAverages().get(i);
			String label = field.getLabel();
			int index = getStatesDataConverter().getIndex(field.getAlias());
			info.addOutput(name, name, index, label);
			dataList.addPlotter(new LinePlotter(index));
		}

		/* Pivot. */
		dataList.addPlotter(new PlotterZigZag());

		PlotData plotData = new PlotData(KEY_PRICES_AND_AVERAGES);
		plotData.add(dataList);

		return plotData;
	}

	/**
	 * @return The plot data for states slopes.
	 */
	private PlotData getPlotDataSlopes() {

		DataInfo info = new DataInfo();
		info.setInstrument(getInstrument());
		info.setName(KEY_SLOPES);
		info.setDescription("Averages slopes");
		info.setPeriod(getPeriod());
		DataListSource dataList =
			new DataListSource(info, getStatesListPersistor(), getStatesDataConverter());

		for (Field field : getFieldListSlopes("nrm")) {
			String name = field.getHeader();
			String label = field.getLabel();
			int index = getStatesDataConverter().getIndex(field.getAlias());
			info.addOutput(name, name, index, label);
			LinePlotter plotter = new LinePlotter(index);
			plotter.setColor(Color.BLACK);
			dataList.addPlotter(plotter);
		}

		PlotData plotData = new PlotData(KEY_SLOPES);
		plotData.add(dataList);

		return plotData;
	}

	/**
	 * @return The plot data for states spreads.
	 */
	private PlotData getPlotDataSpreads() {

		DataInfo info = new DataInfo();
		info.setInstrument(getInstrument());
		info.setName(KEY_SPREADS);
		info.setDescription("Averages spreads");
		info.setPeriod(getPeriod());
		DataListSource dataList =
			new DataListSource(info, getStatesListPersistor(), getStatesDataConverter());

		for (Field field : getFieldListSpreads("nrm")) {
			String name = field.getHeader();
			String label = field.getLabel();
			int index = getStatesDataConverter().getIndex(field.getAlias());
			info.addOutput(name, name, index, label);
			LinePlotter plotter = new LinePlotter(index);
			plotter.setColor(Color.BLACK);
			dataList.addPlotter(plotter);
		}

		PlotData plotData = new PlotData(KEY_SPREADS);
		plotData.add(dataList);

		return plotData;
	}

	/**
	 * @return The states converter to data structure.
	 */
	private DataConverter getStatesDataConverter() {
		if (statesDataConverter == null) {

			Table table = getTableStates();
			List<Integer> list = new ArrayList<>();

			/* Open, high, low, close. */
			list.add(table.getFieldIndex(DB.FIELD_BAR_OPEN));
			list.add(table.getFieldIndex(DB.FIELD_BAR_HIGH));
			list.add(table.getFieldIndex(DB.FIELD_BAR_LOW));
			list.add(table.getFieldIndex(DB.FIELD_BAR_CLOSE));

			List<Field> fields;

			/* Pivot and reference value. */
			list.add(table.getFieldIndex(DB.FIELD_STATES_PIVOT_CALC));
			list.add(table.getFieldIndex(DB.FIELD_STATES_REFV_CALC));

			/* Averages. */
			fields = getFieldListAverages();
			for (Field field : fields) {
				int index = table.getFieldIndex(field);
				list.add(index);
			}

			/* Slopes normalized. */
			fields = getFieldListSlopes("nrm");
			for (Field field : fields) {
				int index = table.getFieldIndex(field);
				list.add(index);
			}

			/* Spreads normalized. */
			fields = getFieldListSpreads("nrm");
			for (Field field : fields) {
				int index = table.getFieldIndex(field);
				list.add(index);
			}

			int[] indexes = Lists.toIntegerArray(list);
			Record masterRecord = getTableStates().getDefaultRecord();
			statesDataConverter = new DataConverter(masterRecord, indexes);

			/*
			 * Time of each first average (fast/slow) candle, except one period candle, to
			 * be stored as data properties.
			 */
//			for (int i = 0; i < averages.size() - 1; i++) {
//				int fast = averages.get(i).getPeriod();
//				int slow = averages.get(i + 1).getPeriod();
//				String name = getNameCandle(DB.FIELD_BAR_TIME, fast, slow, 0);
//				statesDataConverter.addProperty(name);
//			}
		}
		return statesDataConverter;
	}

	/**
	 * @return The states list persistor.
	 */
	private ListPersistor getStatesListPersistor() {
		if (statesListPersistor == null) {
			statesListPersistor = new ListPersistor(getTableStates().getPersistor());
		}
		return statesListPersistor;
	}

	/**
	 * @param prefix The tab prefix.
	 * @return A proper tab key.
	 */
	private String getTabKey(String prefix) {
		StringBuilder b = new StringBuilder();
		b.append("-");
		b.append(getInstrument().getId());
		b.append("-");
		b.append(getPeriod().getId());
		b.append("-");
		b.append(getId());
		b.append("-");
		b.append(getKey());
		return b.toString();
	}

	/**
	 * Return the table name suffix.
	 * 
	 * @return The table name suffix.
	 */
	private String getTableNameSuffix() {
		StringBuilder suffix = new StringBuilder();
		suffix.append("_");
		suffix.append(getId());
		suffix.append("_");
		suffix.append(getKey());
		return suffix.toString().toLowerCase();
	}

	/**
	 * Return the ranges table to calculate value means and standard deviations.
	 * 
	 * @return The ranges table.
	 */
	public Table getTableRanges() {
		if (tableRanges != null) {
			return tableRanges;
		}

		tableRanges = new Table();

		Instrument instrument = getInstrument();
		Period period = getPeriod();
		String name = DB.name_ticker(instrument, period, getTableNameSuffix() + "_rng");

		tableRanges.setSchema(DB.schema_server());
		tableRanges.setName(name);

		tableRanges.addField(DB.field_string(DB.FIELD_RANGE_NAME, 60, "Name"));
		tableRanges.addField(DB.field_double(DB.FIELD_RANGE_MINIMUM, "Minimum"));
		tableRanges.addField(DB.field_double(DB.FIELD_RANGE_MAXIMUM, "Maximum"));
		tableRanges.addField(DB.field_double(DB.FIELD_RANGE_AVERAGE, "Average"));
		tableRanges.addField(DB.field_double(DB.FIELD_RANGE_STDDEV, "Std Dev"));
		tableRanges.addField(DB.field_double(DB.FIELD_RANGE_AVG_STD_10, "Avg Std 1.0"));
		tableRanges.addField(DB.field_double(DB.FIELD_RANGE_AVG_STD_20, "Avg Std 2.0"));

		tableRanges.getField(DB.FIELD_RANGE_MINIMUM).setDisplayDecimals(8);
		tableRanges.getField(DB.FIELD_RANGE_MAXIMUM).setDisplayDecimals(8);
		tableRanges.getField(DB.FIELD_RANGE_AVERAGE).setDisplayDecimals(8);
		tableRanges.getField(DB.FIELD_RANGE_STDDEV).setDisplayDecimals(8);
		tableRanges.getField(DB.FIELD_RANGE_AVG_STD_10).setDisplayDecimals(2);
		tableRanges.getField(DB.FIELD_RANGE_AVG_STD_20).setDisplayDecimals(2);

		/* Primary key. */
		tableRanges.getField(DB.FIELD_RANGE_NAME).setPrimaryKey(true);

		View view = tableRanges.getComplexView(tableRanges.getPrimaryKey());
		tableRanges.setPersistor(new DBPersistor(MLT.getDBEngine(), view));
		return tableRanges;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Table> getTables() {
		List<Table> tables = new ArrayList<>();
		tables.add(getTableRanges());
		tables.add(getTableStates());
		return tables;
	}

	/**
	 * Return the states (all parameters) table.
	 * 
	 * @return The table.
	 */
	public Table getTableStates() {
		if (tableStates != null) {
			return tableStates;
		}

		tableStates = new Table();

		Instrument instrument = getInstrument();
		Period period = getPeriod();
		String name = DB.name_ticker(instrument, period, getTableNameSuffix() + "_src");

		tableStates.setSchema(DB.schema_server());
		tableStates.setName(name);

		/* Time, open, high, low, close. */
		tableStates.addField(DB.field_long(DB.FIELD_BAR_TIME, "Time"));

		tableStates.addField(DB.field_data(instrument, DB.FIELD_BAR_OPEN, "Open"));
		tableStates.addField(DB.field_data(instrument, DB.FIELD_BAR_HIGH, "High"));
		tableStates.addField(DB.field_data(instrument, DB.FIELD_BAR_LOW, "Low"));
		tableStates.addField(DB.field_data(instrument, DB.FIELD_BAR_CLOSE, "Close"));

		tableStates.addField(
			DB.field_timeFmt(period, DB.FIELD_BAR_TIME, DB.FIELD_BAR_TIME_FMT, "Time fmt"));

		int ndx = 0;
		FieldGroup grpData = new FieldGroup(ndx++, "data", "Data");
		tableStates.getField(DB.FIELD_BAR_TIME).setFieldGroup(grpData);
		tableStates.getField(DB.FIELD_BAR_OPEN).setFieldGroup(grpData);
		tableStates.getField(DB.FIELD_BAR_HIGH).setFieldGroup(grpData);
		tableStates.getField(DB.FIELD_BAR_LOW).setFieldGroup(grpData);
		tableStates.getField(DB.FIELD_BAR_CLOSE).setFieldGroup(grpData);
		tableStates.getField(DB.FIELD_BAR_TIME_FMT).setFieldGroup(grpData);

		/*
		 * Calculated pivot (High=1, None=0, Low=-1) and Label (Long=1, Out=0, Short=-1)
		 */
		FieldGroup grpLabels = new FieldGroup(ndx++, "labels", "Labels");
		tableStates.addField(
			DB.field_data(instrument, DB.FIELD_STATES_REFV_CALC, "Ref-val calc", "Ref value calc"));
		tableStates.addField(
			DB.field_data(instrument, DB.FIELD_STATES_REFV_EDIT, "Ref-val edit", "Ref value edit"));
		tableStates.addField(
			DB.field_integer(DB.FIELD_STATES_PIVOT_CALC, "Pivot calc", "Pivot calculated"));
		tableStates.addField(
			DB.field_integer(DB.FIELD_STATES_PIVOT_EDIT, "Pivot edit", "Pivot edited"));
		tableStates.addField(
			DB.field_integer(DB.FIELD_STATES_LABEL_CALC, "Label calc", "Label calculated pivot"));
		tableStates.addField(
			DB.field_integer(DB.FIELD_STATES_LABEL_EDIT, "Label edit", "Label edited pivot"));

		tableStates.getField(DB.FIELD_STATES_REFV_CALC).setFieldGroup(grpLabels);
		tableStates.getField(DB.FIELD_STATES_REFV_EDIT).setFieldGroup(grpLabels);
		tableStates.getField(DB.FIELD_STATES_PIVOT_CALC).setFieldGroup(grpLabels);
		tableStates.getField(DB.FIELD_STATES_PIVOT_EDIT).setFieldGroup(grpLabels);
		tableStates.getField(DB.FIELD_STATES_LABEL_CALC).setFieldGroup(grpLabels);
		tableStates.getField(DB.FIELD_STATES_LABEL_EDIT).setFieldGroup(grpLabels);

		/* Lists of fields. */
		List<Field> fields;

		/* Average fields. */
		FieldGroup grpAverages = new FieldGroup(ndx++, "avgs", "Averages");
		fields = getFieldListAverages();
		for (Field field : fields) {
			field.setFieldGroup(grpAverages);
			tableStates.addField(field);
		}

		/* Slopes of averages: raw. */
		FieldGroup grpSlopesRaw = new FieldGroup(ndx++, "slopes_raw", "Slopes raw");
		fields = getFieldListSlopes("raw");
		for (Field field : fields) {
			field.setFieldGroup(grpSlopesRaw);
			tableStates.addField(field);
		}

		/* Spreads within averages: raw. */
		FieldGroup grpSpreadsRaw = new FieldGroup(ndx++, "spreads_raw", "Spreads raw");
		fields = getFieldListSpreads("raw");
		for (Field field : fields) {
			field.setFieldGroup(grpSpreadsRaw);
			tableStates.addField(field);
		}

		/* Slopes of averages: normalized. */
		FieldGroup grpSlopesNrm = new FieldGroup(ndx++, "slopes_nrm", "Slopes nrm");
		fields = getFieldListSlopes("nrm");
		for (Field field : fields) {
			field.setFieldGroup(grpSlopesNrm);
			tableStates.addField(field);
		}

		/* Spreads within averages: normalized. */
		FieldGroup grpSpreadsNrm = new FieldGroup(ndx++, "spreads_nrm", "Spreads nrm");
		fields = getFieldListSpreads("nrm");
		for (Field field : fields) {
			field.setFieldGroup(grpSpreadsNrm);
			tableStates.addField(field);
		}

		/* Candle fields. */
		for (int i = 0; i < averages.size(); i++) {
			String grpName = getCandleGroupName(i);
			String grpTitle = getCandleGroupTitle(i);
			FieldGroup group = new FieldGroup(ndx++, grpName, grpTitle);
			fields = getFieldListCandles(i);
			for (Field field : fields) {
				field.setFieldGroup(group);
				tableStates.addField(field);
			}
		}

		/* Control flags. */
		FieldGroup grpControls = new FieldGroup(ndx++, "controls", "Controls");
		Field normalized = DB.field_string(DB.FIELD_STATES_NORMALIZED, 1, "Nrm", "Normalized");
		normalized.setFieldGroup(grpControls);
		tableStates.addField(normalized);
		Field pivotScanned =
			DB.field_string(DB.FIELD_STATES_PIVOT_SCANNED, 1, "Scn", "Pivot scanned");
		pivotScanned.setFieldGroup(grpControls);
		tableStates.addField(pivotScanned);

		tableStates.getField(DB.FIELD_BAR_TIME).setPrimaryKey(true);

		View view = tableStates.getComplexView(tableStates.getPrimaryKey());
		tableStates.setPersistor(new DBPersistor(MLT.getDBEngine(), view));
		
		return tableStates;
	}

	/**
	 * @param suffix The tab suffix.
	 * @return A proper tab text.
	 */
	private String getTabText(String suffix) {
		StringBuilder b = new StringBuilder();
		b.append(getInstrument().getDescription());
		b.append(" ");
		b.append(getPeriod());
		b.append(" ");
		b.append(getId());
		b.append(" ");
		b.append(getKey());
		b.append(" ");
		b.append(suffix);
		return b.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setParameters(String parameters) throws Exception {
		ParametersHandler handler = new ParametersHandler();
		Parser parser = new Parser();
		parser.parse(new ByteArrayInputStream(parameters.getBytes()), handler);
		averages.clear();
		averages.addAll(handler.averages);
		barsAhead = handler.barsAhead;
		percentCalc = handler.percentCalc;
		percentEdit = handler.percentEdit;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validate() throws Exception {
		for (int i = 0; i < averages.size(); i++) {
			Average avgCurr = averages.get(i);
			/* Must have smooths. */
			if (avgCurr.getSmooths().length == 0) {
				throw new IllegalArgumentException("Average " + avgCurr + " must have smooths");
			}
			/* Period greater than previous, and previous must be multiple. */
			if (i > 0) {
				Average avgPrev = averages.get(i - 1);
				if (avgPrev.getPeriod() >= avgCurr.getPeriod()) {
					throw new IllegalArgumentException(
						"Average period of " + avgCurr + " must greater than period of " + avgPrev);
				}
				if (Numbers.remainder(avgCurr.getPeriod(), avgPrev.getPeriod()) != 0) {
					throw new IllegalArgumentException(
						"Average " + avgCurr + " must be a multiple of " + avgPrev);
				}
			}
		}
		/* Must have positive bars ahead. */
		if (barsAhead <= 0) {
			throw new IllegalArgumentException("Invalid bars ahead for zig-zag " + barsAhead);
		}
	}
}
