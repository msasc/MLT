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
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import javax.swing.Icon;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.mlt.db.Field;
import com.mlt.db.FieldGroup;
import com.mlt.db.ListPersistor;
import com.mlt.db.Persistor;
import com.mlt.db.Record;
import com.mlt.db.Relation;
import com.mlt.db.Table;
import com.mlt.db.View;
import com.mlt.db.rdbms.DBPersistor;
import com.mlt.desktop.Alert;
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
import com.mlt.ml.data.DefaultPattern;
import com.mlt.ml.data.ListPatternSource;
import com.mlt.ml.data.Pattern;
import com.mlt.ml.data.PatternSource;
import com.mlt.ml.function.Activation;
import com.mlt.ml.function.activation.ActivationSigmoid;
import com.mlt.ml.function.activation.ActivationSoftMax;
import com.mlt.ml.network.Builder;
import com.mlt.ml.network.Network;
import com.mlt.ml.network.Trainer;
import com.mlt.util.Formats;
import com.mlt.util.IO;
import com.mlt.util.Lists;
import com.mlt.util.Logs;
import com.mlt.util.Numbers;
import com.mlt.util.Properties;
import com.mlt.util.Strings;
import com.mlt.util.xml.parser.Parser;
import com.mlt.util.xml.parser.ParserHandler;

import app.mlt.plaf.DB;
import app.mlt.plaf.MLT;

/**
 * Statistics built on a list of averages.
 * 
 * @author Miquel Sas
 */
public class StatisticsAverages extends Statistics {

	private static final Function<Integer, String> KEY_CANDLES = size -> "candles-" + size;
	private static final String KEY_LABELS = "labels";
	private static final String KEY_PRICES_AND_AVERAGES = "prices_and_averages";
	private static final String KEY_SLOPES = "slopes";
	private static final String KEY_SPREADS = "spreads";
	private static final String KEY_PIVOTS = "pivots";

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
				String key = getTabPaneKey("STATS-RANGES");
				String text = getTabPaneText("Ranges");

				Persistor persistor = getTableRanges().getPersistor();

				TableRecordModel model = new TableRecordModel(persistor.getDefaultRecord());
				model.addColumn(DB.FIELD_RANGE_NAME);
				model.addColumn(DB.FIELD_RANGE_MINIMUM);
				model.addColumn(DB.FIELD_RANGE_MAXIMUM);
				model.addColumn(DB.FIELD_RANGE_AVERAGE);
				model.addColumn(DB.FIELD_RANGE_STDDEV);

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
	 * Browse raw or normalized values.
	 */
	class ActionBrowseView extends ActionRun {

		boolean normalized;

		private ActionBrowseView(boolean normalized) {
			this.normalized = normalized;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			try {
				String key = getTabPaneKey("STATS-BROWSE-" + (normalized ? "NRM" : "RAW"));
				String text = getTabPaneText(normalized ? "Normalized values" : "Raw values");

				MLT.getStatusBar().setProgressIndeterminate(key, "Setup " + text, true);

				View view = getView(normalized, true, true, true, true);
				ListPersistor persistor = new ListPersistor(view.getPersistor(), view.getOrderBy());
				persistor.setCacheSize(10000);
				persistor.setPageSize(100);

				TableRecordModel model = new TableRecordModel(persistor.getDefaultRecord());
				int index = view.getFieldIndex(DB.FIELD_BAR_TIME_FMT);
				for (int i = index; i < view.getFieldCount(); i++) {
					model.addColumn(view.getField(i).getAlias());
				}

				model.setRecordSet(new DataRecordSet(persistor));

				TableRecord table = new TableRecord(true);
				table.setSelectionMode(SelectionMode.SINGLE_ROW_SELECTION);
				table.setModel(model);
				table.setSelectedRow(0);
				table.setPopupMenuProvider(new MenuTable(table));

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
	 * Action to calculate the statistics by a list of tasks.
	 */
	class ActionCalculate extends ActionRun {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			TaskFrame frame = new TaskFrame();
			frame.setTitle(getLabel() + " - Calculate raw/normalized/pivots/labels");
			frame.addTasks(new TaskAveragesRaw(StatisticsAverages.this));
			frame.addTasks(new TaskAveragesRanges(StatisticsAverages.this));
			frame.addTasks(new TaskAveragesNormalize(StatisticsAverages.this));
			frame.addTasks(new TaskAveragesPivots(StatisticsAverages.this));
			frame.addTasks(new TaskAveragesLabelsCalc(StatisticsAverages.this));
			frame.addTasks(new TaskAveragesPatterns(StatisticsAverages.this, true, 0.8));
			frame.show();
		}

	}

	/**
	 * Action to train the statistics by a list of tasks.
	 */
	class ActionTrain extends ActionRun {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			try {
				TaskFrame frame = new TaskFrame();
				frame.setTitle(getLabel() + " - Training tasks");
				frame.addTasks(getTrainer());
				frame.show();
			} catch (Exception exc) {
				Logs.catching(exc);
			}
		}

	}

	/**
	 * Chart the ticker.
	 */
	class ActionChartSources extends ActionRun {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			try {

				String key = getTabPaneKey("STATS-CHART");
				String text = getTabPaneText("Chart");
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

			this.indexOpen = getChartDataConverter().getIndex(DB.FIELD_BAR_OPEN);
			this.indexHigh = getChartDataConverter().getIndex(DB.FIELD_BAR_HIGH);
			this.indexLow = getChartDataConverter().getIndex(DB.FIELD_BAR_LOW);
			this.indexClose = getChartDataConverter().getIndex(DB.FIELD_BAR_CLOSE);
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
				if (!container.containsPlotData(KEY_LABELS)) {
					Option option = new Option();
					option.setKey(KEY_LABELS);
					option.setText("Calculated labels and pivots");
					option.setToolTip("Calculated labels and pivots");
					option.setOptionGroup(Group.CONFIGURE);
					option.setDefaultClose(false);
					option.setCloseWindow(false);
					option.setAction(l -> container.addPlotData(getPlotDataLabels(), true));
					popup.add(option.getMenuItem());
				}
				if (container.containsPlotData(KEY_PRICES_AND_AVERAGES)) {
					PlotData plotData = container.getPlotData(KEY_PRICES_AND_AVERAGES);
					PlotterPivots zz = new PlotterPivots();
					if (plotData.get(0).isPlotter(zz)) {
						Option option = new Option();
						option.setKey(KEY_PIVOTS);
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
						option.setKey(KEY_PIVOTS);
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
				for (int i = 1; i < averages.size(); i++) {
					int size = getCandleSize(i);
					int count = getCandleCount(i);
					if (!container.containsPlotData(KEY_CANDLES.apply(size))) {
						Option option = new Option();
						option.setKey(KEY_CANDLES.apply(size));
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
				for (int i = 1; i < averages.size(); i++) {
					int size = getCandleSize(i);
					if (container.containsPlotData(KEY_CANDLES.apply(size))) {
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
	 * Popup menu for browse table.
	 */
	class MenuTable implements PopupMenuProvider {
		TableRecord table;

		MenuTable(TableRecord table) {
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
	static class ParametersHandler extends ParserHandler {

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
			set("statistics/averages/average", "type", "string", "WMA, SMA");
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
				Alert.error(exc.getMessage());
				throw new SAXException(exc.getMessage(), exc);
			}
		}
	}

	/**
	 * Pivot structure.
	 */
	static class Pivot {

		double pivot;
		double value;
		int index;

		Pivot(double pivot, double value, int index) {
			this.pivot = pivot;
			this.value = value;
			this.index = index;
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
			super(KEY_CANDLES.apply(size));

			this.size = size;
			this.count = count;

			this.indexOpen = getChartDataConverter().getIndex(DB.FIELD_BAR_OPEN);
			this.indexHigh = getChartDataConverter().getIndex(DB.FIELD_BAR_HIGH);
			this.indexLow = getChartDataConverter().getIndex(DB.FIELD_BAR_LOW);
			this.indexClose = getChartDataConverter().getIndex(DB.FIELD_BAR_CLOSE);

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
	 * Label and pivot plotter.
	 */
	class PlotterLabels extends DataPlotter {

		int indexPivot;
		int indexData;
		String aliasLabel;

		Color colorNotSet = Color.BLACK;
		Color colorLong = new Color(32, 160, 32);
		Color colorShort = new Color(160, 32, 32);
		Color colorOut = Color.LIGHT_GRAY;

		public PlotterLabels() {
			super(KEY_LABELS);
		}

		@Override
		public void plot(Context ctx, DataList dataList, int startIndex, int endIndex) {

			DataContext dc = getContext();

			/* Draw paths. */
			List<Path> paths = new ArrayList<>();
			double x, y;
			Path path = null;
			for (int index = startIndex; index <= endIndex; index++) {

				if (index < 0 || index >= dataList.size()) {
					continue;
				}

				Data data = dataList.get(index);
				double value = data.getValue(indexData);
				String label = data.getProperties().getString(aliasLabel);
				boolean labelSet = !label.isEmpty();
				x = dc.getCenterCoordinateX(dc.getCoordinateX(index));
				y = dc.getCoordinateY(value);

				if (index > 0 && index > startIndex) {
					Data prev = dataList.get(index - 1);
					String previousLabel = prev.getProperties().getString(aliasLabel);
					boolean previousSet = !previousLabel.isEmpty();
					if (path != null && (labelSet != previousSet || label != previousLabel)) {
						path.lineTo(x, y);
						paths.add(path);
						path = null;
					}
				}

				if (path == null) {
					path = new Path();
					path.setStroke(new Stroke(2.0));
					path.addHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
					if (labelSet) {
						if (label.equals("1")) {
							path.setDrawPaint(colorLong);
						} else if (label.equals("-1")) {
							path.setDrawPaint(colorShort);
						} else {
							path.setDrawPaint(colorOut);
						}
					} else {
						path.setStroke(new Stroke(
							1.0,
							Stroke.CAP_BUTT,
							Stroke.JOIN_MITER, 1,
							new double[] { 2 }, 0));
						path.setDrawPaint(colorNotSet);
					}
					path.moveTo(x, y);
				} else {
					path.lineTo(x, y);
				}
			}
			if (path != null) {
				paths.add(path);
			}

			/* Draw. */
			paths.forEach(p -> ctx.draw(p));

			/* Draw pivots. */
			List<Pivot> pivots = getPivots(dataList, startIndex, endIndex, indexPivot, indexData);

			path = new Path();
			path.setStroke(new Stroke(2.0));
			path.addHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			path.setDrawPaint(Color.BLACK);
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
	 * Zig-zag data plotter for the states data list.
	 */
	class PlotterPivots extends DataPlotter {

		int indexPivot;
		int indexData;

		PlotterPivots() {
			super(KEY_PIVOTS);
		}

		@Override
		public void plot(Context ctx, DataList dataList, int startIndex, int endIndex) {

			List<Pivot> pivots = getPivots(dataList, startIndex, endIndex, indexPivot, indexData);

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
	 * @param name The field name with underline separators.
	 * @return A suitable header.
	 */
	private static String getHeader(String name) {
		String[] tokens = Strings.parse(name, "_");
		tokens[0] = Strings.capitalize(tokens[0]);
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < tokens.length; i++) {
			if (i > 0) {
				b.append(" ");
			}
			b.append(tokens[i]);
		}
		return b.toString();
	}

	/**
	 * @param dataList   The data list.
	 * @param startIndex Start index.
	 * @param endIndex   End index.
	 * @param indexPivot Pivot index.
	 * @param indexData  Data index.
	 * @return The list of pivots.
	 */
	private static List<Pivot> getPivots(
		DataList dataList,
		int startIndex,
		int endIndex,
		int indexPivot,
		int indexData) {

		/* Build the first list of visible pivots. */
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
		/* Check any pivot before. */
		int firstIndex = (!pivots.isEmpty() ? pivots.get(0).index : startIndex);
		for (int index = firstIndex - 1; index >= 0; index--) {
			Data data = dataList.get(index);
			double pivot = data.getValue(indexPivot);
			if (pivot != 0) {
				double value = data.getValue(indexData);
				pivots.add(0, new Pivot(pivot, value, index));
				break;
			}
		}
		/* Check any pivot after. */
		int lastIndex = (!pivots.isEmpty() ? pivots.get(pivots.size() - 1).index : endIndex);
		for (int index = lastIndex + 1; index < dataList.size(); index++) {
			Data data = dataList.get(index);
			double pivot = data.getValue(indexPivot);
			if (pivot != 0) {
				double value = data.getValue(indexData);
				pivots.add(new Pivot(pivot, value, index));
				break;
			}
		}

		return pivots;
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

	/** Properties. */
	private Properties properties;
	/** List of averages. */
	private List<Average> averages;
	/** Bars ahead to calculate pivots. */
	private int barsAhead;
	/** Percentage to set calculated labels. */
	private double percentCalc;
	/** Percentage to set edited labels. */
	private double percentEdit;
	/** Plot all candles flag. */
	private boolean plotAllCandles = true;

	/** File path for pattern files. */
	private String filePath;

	/**
	 * @param instrument The instrument.
	 * @param period     The period.
	 */
	public StatisticsAverages(Instrument instrument, Period period) {
		super(instrument, period);
		properties = new Properties();
		averages = new ArrayList<>();
		barsAhead = 100;
		percentCalc = 10;
		percentEdit = 10;
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
				throw new IllegalArgumentException("Repeated average period.");
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
	 * {@inheritDoc}
	 */
	@Override
	protected void finalize() throws Throwable {
		averages.clear();
		averages = null;
		properties.clear();
		properties = null;
		super.finalize();
	}

	/**
	 * @return The list of averages.
	 */
	public List<Average> getAverages() {
		return averages;
	}

	/**
	 * @return Tars ahead to calculate pivots.
	 */
	int getBarsAhead() {
		return barsAhead;
	}

	/**
	 * @param index Index of average.
	 * @return Number of candles.
	 */
	int getCandleCount(int index) {
		int fast = (index == 0 ? 1 : averages.get(index - 1).getPeriod());
		int slow = averages.get(index).getPeriod();
		int mult = averages.size() - index;
		int count = (slow / fast) * mult;
		return count;

	}

	/**
	 * @param index Index of average.
	 * @return Size of candles.
	 */
	int getCandleSize(int index) {
		int size = (index == 0 ? 1 : averages.get(index - 1).getPeriod());
		return size;
	}

	/**
	 * @return The chart converter to data structure.
	 */
	private DataConverter getChartDataConverter() {
		DataConverter dataConverter = (DataConverter) properties.getObject("data_converter");
		if (dataConverter != null) {
			return dataConverter;
		}

		View view = getView(true, true, true, true, false);
		List<Integer> list = new ArrayList<>();

		/* Open, high, low, close. */
		list.add(view.getFieldIndex(DB.FIELD_BAR_OPEN));
		list.add(view.getFieldIndex(DB.FIELD_BAR_HIGH));
		list.add(view.getFieldIndex(DB.FIELD_BAR_LOW));
		list.add(view.getFieldIndex(DB.FIELD_BAR_CLOSE));

		List<Field> fields;

		/* Pivots, reference values and labels. */
		list.add(view.getFieldIndex(DB.FIELD_SOURCES_PIVOT_CALC));
		list.add(view.getFieldIndex(DB.FIELD_SOURCES_REFV_CALC));

		/* Averages. */
		fields = getFieldListAverages();
		for (Field field : fields) {
			int index = view.getFieldIndex(field.getAlias());
			list.add(index);
		}

		/* Slopes normalized. */
		fields = getFieldListSlopes();
		for (Field field : fields) {
			int index = view.getFieldIndex(field.getAlias());
			list.add(index);
		}

		/* Spreads normalized. */
		fields = getFieldListSpreads();
		for (Field field : fields) {
			int index = view.getFieldIndex(field.getAlias());
			list.add(index);
		}

		int indexTime = view.getFieldIndex(DB.FIELD_BAR_TIME);
		int[] indexes = Lists.toIntegerArray(list);
		Record masterRecord = view.getDefaultRecord();
		dataConverter = new DataConverter(masterRecord, indexTime, indexes);
		dataConverter.addProperty(DB.FIELD_SOURCES_LABEL_CALC);
		dataConverter.addProperty(DB.FIELD_SOURCES_LABEL_NETC);

		properties.setObject("data_converter", dataConverter);
		return dataConverter;
	}

	/**
	 * @return The chart list persistor.
	 */
	private ListPersistor getChartListPersistor() {
		ListPersistor persistor = (ListPersistor) properties.getObject("data_persistor");
		if (persistor == null) {
			View view = getView(true, true, true, true, false);
			persistor = new ListPersistor(view.getPersistor());
			properties.setObject("data_persistor", persistor);
		}
		return persistor;
	}

//	private DataConverter getDataConverter(
//		String key,
//		FieldList fieldList,
//		boolean prices,
//		boolean pivotsAndLabels,
//		boolean averages,
//		boolean slopes,
//		boolean spreads) {
//		
//		return null;
//	}

	/**
	 * @return The list of average fields.
	 */
	List<Field> getFieldListAverages() {
		List<Field> fields = new ArrayList<>();
		for (int i = 0; i < averages.size(); i++) {
			Average avg = averages.get(i);
			String name = getNameAverage(avg);
			String header = getHeader(name);
			Field field = DB.field_double(name, header);
			field.setStringConverter(new NumberScaleConverter(8));
			fields.add(field);
		}
		return fields;
	}

	/**
	 * @return The list with all candle fields.
	 */
	List<Field> getFieldListCandles() {
		List<Field> fields = new ArrayList<>();
		for (int i = 0; i < averages.size(); i++) {
			fields.addAll(getFieldListCandles(i));
		}
		return fields;
	}

	/**
	 * @param i Averages index.
	 * @return The list of candle fields.
	 */
	List<Field> getFieldListCandles(int i) {
		List<Field> fields = new ArrayList<>();
		int size = getCandleSize(i);
		int count = getCandleCount(i);
		for (int index = 0; index < count; index++) {
			for (String candleName : DB.CANDLE_NAMES) {
				if (candleName.equals(DB.FIELD_CANDLE_REL_POS) && index == count - 1) {
					continue;
				}
				String name = getNameCandle(size, index, candleName);
				String header = getHeader(name);
				Field field = DB.field_double(name, header);
				field.setStringConverter(new NumberScaleConverter(8));
				fields.add(field);
			}
		}
		return fields;
	}

	/**
	 * @return The list of slope fields.
	 */
	List<Field> getFieldListSlopes() {
		List<Field> fields = new ArrayList<>();
		for (int i = 0; i < averages.size(); i++) {
			Average avg = averages.get(i);
			String name = getNameSlope(avg);
			String header = getHeader(name);
			Field field = DB.field_double(name, header);
			field.setStringConverter(new NumberScaleConverter(8));
			fields.add(field);
		}
		return fields;
	}

	/**
	 * @return The list of spread fields.
	 */
	List<Field> getFieldListSpreads() {
		List<Field> fields = new ArrayList<>();
		String name, header;
		Field field;
		for (int i = 0; i < averages.size(); i++) {
			Average fast = averages.get(i);
			for (int j = i + 1; j < averages.size(); j++) {
				Average slow = averages.get(j);
				name = getNameSpread(fast, slow);
				header = getHeader(name);
				field = DB.field_double(name, header);
				field.setStringConverter(new NumberScaleConverter(8));
				fields.add(field);
			}
		}
		return fields;
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
	 * @param avg The average.
	 * @return The name.
	 */
	String getNameAverage(Average avg) {
		return "average_" + getPadded(avg.getPeriod());
	}

	/**
	 * @param size  The size
	 * @param index The index.
	 * @param name  The name of the field (range, body_factor...)
	 * @return The field name.
	 */
	String getNameCandle(int size, int index, String name) {
		StringBuilder b = new StringBuilder();
		b.append("candle_");
		b.append(getPadded(size));
		b.append("_");
		b.append(getPadded(index));
		b.append("_");
		b.append(name);
		return b.toString();
	}

	/**
	 * @param avg The average.
	 * @return The field name.
	 */
	String getNameSlope(Average avg) {
		return "slope_" + getPadded(avg.getPeriod());
	}

	/**
	 * @param fast Fast average.
	 * @param slow Slow average.
	 * @return the field name.
	 */
	String getNameSpread(Average fast, Average slow) {
		StringBuilder b = new StringBuilder();
		b.append("spread_");
		b.append(getPadded(fast.getPeriod()));
		b.append("_");
		b.append(getPadded(slow.getPeriod()));
		return b.toString();
	}

	/**
	 * Return a network definition given the list of hidden layer sizes factors
	 * versus the previous layer size.
	 * 
	 * @param factors The list of hidden layer size factors.
	 * @return The network definition.
	 */
	Network getNetwork(double... factors) {

		Network network = new Network();
		int[] sizes = getNeworkSizes(factors);
		network.setName(getNetworkName(sizes));

		/* Layers. */
		int inputSize, outputSize, index;
		Activation activation;
		for (int i = 0; i < sizes.length; i++) {
			inputSize = -1;
			if (i == 0) {
				inputSize = getPatternInputSize();
			} else {
				inputSize = sizes[i - 1];
			}
			if (i == sizes.length - 1) {
				activation = new ActivationSoftMax();
			} else {
				activation = new ActivationSigmoid();
			}
			outputSize = sizes[i];
			index = i;
			network.addBranch(
				Builder.branchPerceptron("L" + index, inputSize, outputSize, activation));
		}
		inputSize = sizes[sizes.length - 1];
		outputSize = getPatternOutputSize();
		activation = new ActivationSoftMax();
		index = sizes.length;
		network.addBranch(Builder.branchPerceptron("L" + index, inputSize, outputSize, activation));

		return network;
	}

	/**
	 * @param sizes Hidden layers sizes.
	 * @return The name.
	 */
	String getNetworkName(int[] sizes) {
		StringBuilder name = new StringBuilder();
		name.append(DB.name_ticker(getInstrument(), getPeriod(), getTableNameSuffix("net")));
		name.append("-" + getPatternInputSize());
		for (int i = 0; i < sizes.length; i++) {
			name.append("-" + sizes[i]);
		}
		name.append("-" + getPatternOutputSize());
		return Strings.replace(name.toString(), "_", "-").toUpperCase();
	}

	/**
	 * @param factors Hidden layers factors.
	 * @return The sizes.
	 */
	int[] getNeworkSizes(double... factors) {
		int[] sizes = new int[factors.length];
		for (int i = 0; i < factors.length; i++) {
			double prevSize;
			if (i == 0) {
				prevSize = getPatternInputSize();
			} else {
				prevSize = sizes[i - 1];
			}
			sizes[i] = (int) (factors[i] * prevSize);
		}
		return sizes;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Option> getOptions() {
		List<Option> options = new ArrayList<>();
		Option option;
		Option.Group groupCalculate = new Option.Group("CALCULATE", 1);
		Option.Group groupBrowse = new Option.Group("BROWSE", 2);
		Option.Group groupChart = new Option.Group("CHART", 3);

		/* Calculate statistics. */
		option = new Option();
		option.setKey("CALCULATE");
		option.setText("Calculate");
		option.setToolTip("Calculate all statistics values");
		option.setAction(new ActionCalculate());
		option.setOptionGroup(groupCalculate);
		option.setSortIndex(1);
		options.add(option);

		/* Train statistics. */
		option = new Option();
		option.setKey("TRAIN");
		option.setText("Train");
		option.setToolTip("Train statistics");
		option.setAction(new ActionTrain());
		option.setOptionGroup(groupCalculate);
		option.setSortIndex(1);
		options.add(option);

		/* Browse source and raw values. */
		option = new Option();
		option.setKey("BROWSE-RAW");
		option.setText("Browse raw values");
		option.setToolTip("Browse source and raw values");
		option.setAction(new ActionBrowseView(false));
		option.setOptionGroup(groupBrowse);
		option.setSortIndex(1);
		options.add(option);

		/* Browse ranges. */
		option = new Option();
		option.setKey("BROWSE-RANGES");
		option.setText("Browse ranges");
		option.setToolTip("Browse ranges minimum, maximum, average and standard deviation");
		option.setAction(new ActionBrowseRanges());
		option.setOptionGroup(groupBrowse);
		option.setSortIndex(2);
		options.add(option);

		/* Browse source and normalized values. */
		option = new Option();
		option.setKey("BROWSE-NRM");
		option.setText("Browse normalized values");
		option.setToolTip("Browse source and normalized values");
		option.setAction(new ActionBrowseView(true));
		option.setOptionGroup(groupBrowse);
		option.setSortIndex(3);
		options.add(option);

		/* Chart states. */
		option = new Option();
		option.setKey("CHART-SOURCES");
		option.setText("Chart sources");
		option.setToolTip("Chart on sources data");
		option.setAction(new ActionChartSources());
		option.setOptionGroup(groupChart);
		option.setSortIndex(1);
		options.add(option);

		return options;
	}

	/**
	 * @param number The number, size or period.
	 * @return The padded string.
	 */
	String getPadded(int number) {
		return Strings.leftPad(
			Integer.toString(number),
			Numbers.getDigits(averages.get(averages.size() - 1).getPeriod()), "0");
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
	 * @param rc         The record with source and normalized values.
	 * @param calculated A boolean indicating if the label is calculated or edited.
	 * @return The training pattern.
	 */
	Pattern getPattern(Record rc, boolean calculated) {
		double[] inputValues = getPatternInputValues(rc);
		double[] outputValues = getPatternOutputValues(rc, calculated);
		DefaultPattern pattern = new DefaultPattern(inputValues, outputValues, 0);
		return pattern;
	}

	/**
	 * @return The list of pattern fields.
	 */
	List<Field> getPatternFields() {
		@SuppressWarnings("unchecked")
		List<Field> fields = (List<Field>) properties.getObject("pattern_fields");
		if (fields == null) {
			fields = new ArrayList<>();
			fields.addAll(getFieldListSlopes());
			fields.addAll(getFieldListSpreads());
			fields.addAll(getFieldListCandles());
			properties.setObject("pattern_fields", fields);
		}
		return fields;
	}

	/**
	 * @param calculated The calculated flag.
	 * @param train      Thain flag (null for all)
	 * @return The pattern file name.
	 */
	String getPatternFileName(boolean calculated, Boolean train) {
		StringBuilder name = new StringBuilder();
		name.append(getPatternFileRoot());
		if (calculated) {
			name.append("-CALC");
		} else {
			name.append("-EDIT");
		}
		if (train != null) {
			if (train) {
				name.append("-TRAIN");
			} else {
				name.append("-TEST");
			}
		}
		name.append(".patterns");
		return name.toString();
	}

	/**
	 * @return The path for pattern files.
	 */
	String getPatternFilePath() {
		if (filePath == null) {
			filePath = "res/network/";
		}
		return filePath;
	}

	/**
	 * @return The root name for pattern files.
	 */
	String getPatternFileRoot() {
		StringBuilder root = new StringBuilder();
		root.append(DB.name_ticker(
			getInstrument(),
			getPeriod(),
			getTableNameSuffix("net")));
		root.append("-" + getPatternInputSize());
		return Strings.replace(root.toString(), "_", "-").toUpperCase();
	}

	/**
	 * @param rc The record with source and normalized values.
	 * @return The input values.
	 */
	double[] getPatternInputValues(Record rc) {
		List<Field> fields = getPatternFields();
		double[] inputValues = new double[fields.size()];
		for (int i = 0; i < fields.size(); i++) {
			inputValues[i] = rc.getValue(fields.get(i).getAlias()).getDouble();
		}
		return inputValues;
	}

	/**
	 * @return The pattern input size.
	 */
	int getPatternInputSize() {
		return getPatternFields().size();
	}

	/**
	 * @param rc         The record with source and normalized values.
	 * @param calculated A boolean indicating if the label is calculated or edited.
	 * @return The output values.
	 */
	double[] getPatternOutputValues(Record rc, boolean calculated) {
		double[] outputValues = new double[3];
		String label;
		if (calculated) {
			label = rc.getValue(DB.FIELD_SOURCES_LABEL_CALC).getString();
		} else {
			label = rc.getValue(DB.FIELD_SOURCES_LABEL_EDIT).getString();
		}
		outputValues[0] = (label.equals("-1") ? 1 : 0);
		outputValues[1] = (label.equals("0") ? 1 : 0);
		outputValues[2] = (label.equals("1") ? 1 : 0);
		return outputValues;
	}

	/**
	 * @return The pattern output size.
	 */
	int getPatternOutputSize() {
		return 3;
	}

	/**
	 * @param calculated Calculated flag.
	 * @param train      Train flag (null for all)
	 * @return The pattern source.
	 */
	PatternSource getPatternSource(boolean calculated, Boolean train) throws Exception {
		List<Pattern> patterns = new ArrayList<>();
		File file = new File(getPatternFilePath(), getPatternFileName(calculated, train));
		BufferedInputStream bi = null;
		try {
			bi = new BufferedInputStream(new FileInputStream(file));
			int inputSize = IO.readInt(bi);
			if (inputSize != getPatternInputSize()) {
				throw new IllegalStateException("Invalid input size: " + inputSize);
			}
			int outputSize = IO.readInt(bi);
			if (outputSize != getPatternOutputSize()) {
				throw new IllegalStateException("Invalid output size: " + outputSize);
			}
			int count = IO.readInt(bi);
			for (int i = 0; i < count; i++) {
				double[] inputValues = IO.readDouble1A(bi);
				double[] outputValues = IO.readDouble1A(bi);
				Pattern pattern = new DefaultPattern(inputValues, outputValues, 0);
				patterns.add(pattern);
			}
		} catch (Exception exc) {
			throw exc;
		} finally {
			if (bi != null) {
				bi.close();
			}
		}
		return new ListPatternSource(patterns);
	}

	/**
	 * @param size  The size of the candles.
	 * @param count The number of candles.
	 * @return The plot data for candles of the given size.
	 */
	private PlotData getPlotDataCandles(int size, int count) {

		String name = KEY_CANDLES.apply(size);

		DataInfo info = new DataInfo(new CandlesFormatter(size, count));
		info.setInstrument(getInstrument());
		info.setName(name);
		info.setDescription("Candles size " + size);
		info.setPeriod(getPeriod());
		DataListSource dataList =
			new DataListSource(info, getChartListPersistor(), getChartDataConverter());

		dataList.addPlotter(new PlotterCandles(size, count));

		PlotData plotData = new PlotData(name);
		plotData.add(dataList);

		return plotData;
	}

	/**
	 * @return The plot data for labels.
	 */
	private PlotData getPlotDataLabels() {

		DataInfo info = new DataInfo();
		info.setInstrument(getInstrument());
		info.setName(KEY_LABELS);
		info.setDescription("Calculated labels and pivots");
		info.setPeriod(getPeriod());
		DataListSource dataList =
			new DataListSource(info, getChartListPersistor(), getChartDataConverter());

		PlotterLabels plotter = new PlotterLabels();
		plotter.indexPivot = getChartDataConverter().getIndex(DB.FIELD_SOURCES_PIVOT_CALC);
		plotter.indexData = getChartDataConverter().getIndex(DB.FIELD_SOURCES_REFV_CALC);
		plotter.aliasLabel = DB.FIELD_SOURCES_LABEL_CALC;
		plotter.setIndex(getChartDataConverter().getIndex(DB.FIELD_BAR_CLOSE));
		dataList.addPlotter(plotter);

		PlotData plotData = new PlotData(KEY_LABELS);
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
			new DataListSource(info, getChartListPersistor(), getChartDataConverter());

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
			int index = getChartDataConverter().getIndex(field.getAlias());
			info.addOutput(name, name, index, label);
			dataList.addPlotter(new LinePlotter(index));
		}

		/* Pivot. */
		PlotterPivots plotter = new PlotterPivots();
		plotter.indexPivot = getChartDataConverter().getIndex(DB.FIELD_SOURCES_PIVOT_CALC);
		plotter.indexData = getChartDataConverter().getIndex(DB.FIELD_SOURCES_REFV_CALC);
		plotter.setIndex(getChartDataConverter().getIndex(DB.FIELD_BAR_CLOSE));
		dataList.addPlotter(plotter);

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
			new DataListSource(info, getChartListPersistor(), getChartDataConverter());

		List<Field> fields = getFieldListSlopes();
		for (Field field : fields) {
			String name = field.getHeader();
			String label = field.getLabel();
			int index = getChartDataConverter().getIndex(field.getAlias());
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
			new DataListSource(info, getChartListPersistor(), getChartDataConverter());

		List<Field> fields = getFieldListSpreads();
		for (Field field : fields) {
			String name = field.getHeader();
			String label = field.getLabel();
			int index = getChartDataConverter().getIndex(field.getAlias());
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
	 * @return The percentage to set calculated labels.
	 */
	double getPercentCalc() {
		return percentCalc;
	}

	/**
	 * @return The percentage to set edited labels.
	 */
	double getPercentEdit() {
		return percentEdit;
	}

	/**
	 * @param suffix Last suffix.
	 * @return The table name suffix.
	 */
	String getTableNameSuffix(String suffix) {
		StringBuilder b = new StringBuilder();
		b.append(getId());
		b.append("_");
		b.append(getKey());
		b.append("_");
		b.append(suffix);
		return b.toString().toLowerCase();
	}

	/**
	 * @return The table with normalized values.
	 */
	Table getTableNormalized() {

		Table table = (Table) properties.getObject("table_nrm");
		if (table != null) {
			return table;
		}

		table = new Table();

		Instrument instrument = getInstrument();
		Period period = getPeriod();
		String name = DB.name_ticker(instrument, period, getTableNameSuffix("nrm"));

		table.setSchema(DB.schema_server());
		table.setName(name);

		table.addField(DB.field_long(DB.FIELD_BAR_TIME, "Time"));
		table.addField(DB.field_timeFmt(period, DB.FIELD_BAR_TIME, "Time fmt"));

		FieldGroup group;
		List<Field> fields;
		int ndx = 0;

		group = new FieldGroup(ndx++, "slopes", "Slopes");
		fields = getFieldListSlopes();
		for (Field field : fields) {
			field.setFieldGroup(group);
			table.addField(field);
		}

		group = new FieldGroup(ndx++, "spreads", "Spreads");
		fields = getFieldListSpreads();
		for (Field field : fields) {
			field.setFieldGroup(group);
			table.addField(field);
		}

		for (int i = 0; i < averages.size(); i++) {
			String suffix = getPadded(getCandleSize(i));
			group = new FieldGroup(ndx++, "candles_" + suffix, "Spreads " + suffix);
			fields = getFieldListCandles(i);
			for (Field field : fields) {
				field.setFieldGroup(group);
				table.addField(field);
			}
		}

		table.getField(DB.FIELD_BAR_TIME).setPrimaryKey(true);
		View view = table.getComplexView(table.getPrimaryKey());
		table.setPersistor(new DBPersistor(MLT.getDBEngine(), view));
		properties.setObject("table_nrm", table);
		return table;
	}

	/**
	 * @return The ranges table to calculate value means and standard deviations.
	 */
	Table getTableRanges() {

		Table table = (Table) properties.getObject("table_ranges");
		if (table != null) {
			return table;
		}

		table = new Table();

		Instrument instrument = getInstrument();
		Period period = getPeriod();
		String name = DB.name_ticker(instrument, period, getTableNameSuffix("rng"));

		table.setSchema(DB.schema_server());
		table.setName(name);

		table.addField(DB.field_string(DB.FIELD_RANGE_NAME, 60, "Name"));
		table.addField(DB.field_double(DB.FIELD_RANGE_MINIMUM, "Minimum"));
		table.addField(DB.field_double(DB.FIELD_RANGE_MAXIMUM, "Maximum"));
		table.addField(DB.field_double(DB.FIELD_RANGE_AVERAGE, "Average"));
		table.addField(DB.field_double(DB.FIELD_RANGE_STDDEV, "Std Dev"));

		table.getField(DB.FIELD_RANGE_MINIMUM).setDisplayDecimals(8);
		table.getField(DB.FIELD_RANGE_MAXIMUM).setDisplayDecimals(8);
		table.getField(DB.FIELD_RANGE_AVERAGE).setDisplayDecimals(8);
		table.getField(DB.FIELD_RANGE_STDDEV).setDisplayDecimals(8);

		/* Primary key. */
		table.getField(DB.FIELD_RANGE_NAME).setPrimaryKey(true);

		View view = table.getComplexView(table.getPrimaryKey());
		table.setPersistor(new DBPersistor(MLT.getDBEngine(), view));

		properties.setObject("table_ranges", table);
		return table;
	}

	/**
	 * @return The table with raw values.
	 */
	Table getTableRaw() {

		Table table = (Table) properties.getObject("table_raw");
		if (table != null) {
			return table;
		}

		table = new Table();

		Instrument instrument = getInstrument();
		Period period = getPeriod();
		String name = DB.name_ticker(instrument, period, getTableNameSuffix("raw"));

		table.setSchema(DB.schema_server());
		table.setName(name);

		table.addField(DB.field_long(DB.FIELD_BAR_TIME, "Time"));
		table.addField(DB.field_timeFmt(period, DB.FIELD_BAR_TIME, "Time fmt"));

		FieldGroup group;
		List<Field> fields;
		int ndx = 0;

		group = new FieldGroup(ndx++, "slopes", "Slopes");
		fields = getFieldListSlopes();
		for (Field field : fields) {
			field.setFieldGroup(group);
			table.addField(field);
		}

		group = new FieldGroup(ndx++, "spreads", "Spreads");
		fields = getFieldListSpreads();
		for (Field field : fields) {
			field.setFieldGroup(group);
			table.addField(field);
		}

		for (int i = 0; i < averages.size(); i++) {
			String suffix = getPadded(getCandleSize(i));
			group = new FieldGroup(ndx++, "candles_" + suffix, "Candles " + suffix);
			fields = getFieldListCandles(i);
			for (Field field : fields) {
				field.setFieldGroup(group);
				table.addField(field);
			}
		}

		table.getField(DB.FIELD_BAR_TIME).setPrimaryKey(true);
		View view = table.getComplexView(table.getPrimaryKey());
		table.setPersistor(new DBPersistor(MLT.getDBEngine(), view));
		properties.setObject("table_raw", table);
		return table;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Table> getTables() {
		List<Table> tables = new ArrayList<>();
		tables.add(getTableSources());
		tables.add(getTableRaw());
		tables.add(getTableRanges());
		tables.add(getTableNormalized());
		return tables;
	}

	/**
	 * @return The sources table.
	 */
	Table getTableSources() {

		Table table = (Table) properties.getObject("table_sources");
		if (table != null) {
			return table;
		}

		table = new Table();

		Instrument instrument = getInstrument();
		Period period = getPeriod();
		String name = DB.name_ticker(instrument, period, getTableNameSuffix("src"));

		table.setSchema(DB.schema_server());
		table.setName(name);

		table.addField(DB.field_long(DB.FIELD_BAR_TIME, "Time"));
		table.addField(DB.field_timeFmt(period, DB.FIELD_BAR_TIME, "Time fmt"));
		table.addField(DB.field_data(instrument, DB.FIELD_BAR_OPEN, "Open"));
		table.addField(DB.field_data(instrument, DB.FIELD_BAR_HIGH, "High"));
		table.addField(DB.field_data(instrument, DB.FIELD_BAR_LOW, "Low"));
		table.addField(DB.field_data(instrument, DB.FIELD_BAR_CLOSE, "Close"));

		int ndx = 0;
		FieldGroup grpData = new FieldGroup(ndx++, "data", "Data");
		table.getField(DB.FIELD_BAR_TIME).setFieldGroup(grpData);
		table.getField(DB.FIELD_BAR_OPEN).setFieldGroup(grpData);
		table.getField(DB.FIELD_BAR_HIGH).setFieldGroup(grpData);
		table.getField(DB.FIELD_BAR_LOW).setFieldGroup(grpData);
		table.getField(DB.FIELD_BAR_CLOSE).setFieldGroup(grpData);
		table.getField(DB.FIELD_BAR_TIME_FMT).setFieldGroup(grpData);

		table.addField(DB.field_data(instrument, DB.FIELD_SOURCES_REFV_CALC, "V-Calc"));
		table.addField(DB.field_integer(DB.FIELD_SOURCES_PIVOT_CALC, "P-Calc"));
		table.addField(DB.field_string(DB.FIELD_SOURCES_LABEL_CALC, 2, "L-Calc"));
		table.addField(DB.field_string(DB.FIELD_SOURCES_LABEL_NETC, 2, "L-Net-C"));

		table.addField(DB.field_data(instrument, DB.FIELD_SOURCES_REFV_EDIT, "V-Edit"));
		table.addField(DB.field_integer(DB.FIELD_SOURCES_PIVOT_EDIT, "P-Edit"));
		table.addField(DB.field_string(DB.FIELD_SOURCES_LABEL_EDIT, 2, "L-Edit"));
		table.addField(DB.field_string(DB.FIELD_SOURCES_LABEL_NETE, 2, "L-Net-E"));

		FieldGroup grpLabels = new FieldGroup(ndx++, "labels", "Labels");
		table.getField(DB.FIELD_SOURCES_REFV_CALC).setFieldGroup(grpLabels);
		table.getField(DB.FIELD_SOURCES_PIVOT_CALC).setFieldGroup(grpLabels);
		table.getField(DB.FIELD_SOURCES_LABEL_CALC).setFieldGroup(grpLabels);

		table.getField(DB.FIELD_SOURCES_REFV_EDIT).setFieldGroup(grpLabels);
		table.getField(DB.FIELD_SOURCES_PIVOT_EDIT).setFieldGroup(grpLabels);
		table.getField(DB.FIELD_SOURCES_LABEL_EDIT).setFieldGroup(grpLabels);

		FieldGroup grpAverages = new FieldGroup(ndx++, "avgs", "Averages");
		List<Field> fields = getFieldListAverages();
		for (Field field : fields) {
			field.setFieldGroup(grpAverages);
			table.addField(field);
		}

		table.getField(DB.FIELD_BAR_TIME).setPrimaryKey(true);
		View view = table.getComplexView(table.getPrimaryKey());
		table.setPersistor(new DBPersistor(MLT.getDBEngine(), view));
		properties.setObject("table_sources", table);
		return table;
	}

	/**
	 * @param prefix The tab prefix.
	 * @return A proper tab key.
	 */
	private String getTabPaneKey(String prefix) {
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
	 * @param suffix The tab suffix.
	 * @return A proper tab text.
	 */
	private String getTabPaneText(String suffix) {
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
	 * @return The he trainer to train the network..
	 * @throws Exception 
	 */
	Trainer getTrainer() throws Exception {

		Trainer trainer = new Trainer();
		trainer.setEpochs(500);
		trainer.setSaveNetworkData(true);

		Network network = getNetwork(0.6, 0.2);

		trainer.setNetwork(network);
		trainer.setPatternSourceTraining(getPatternSource(true, null));
//		trainer.setPatternSourceTest(getPatternSource(true, false));
		trainer.setShuffle(false);

		trainer.setFilePath("res/network/");
		trainer.setFileRoot(network.getName());
		trainer.setFileExtension("dat");

		trainer.setTitle(network.getName());
		trainer.setPerformanceDecimals(4);

		return trainer;
	}

	/**
	 * @param normalized Normalized / raw values.
	 * @param averages   Show averages.
	 * @param slopes     Show slopes.
	 * @param spreads    Show spreads.
	 * @param candles    Show candles.
	 * @return The view.
	 */
	View getView(
		boolean normalized,
		boolean averages,
		boolean slopes,
		boolean spreads,
		boolean candles) {
		View view = new View();
		StringBuilder b = new StringBuilder();
		b.append(getLabel());
		b.append("-view");
		if (averages) {
			b.append("-avgs");
		}
		if (normalized) {
			b.append("-nrm");
		} else {
			b.append("-raw");
		}
		if (slopes) {
			b.append("-slopes");
		}
		if (spreads) {
			b.append("-spreads");
		}
		if (candles) {
			b.append("-candles");
		}
		view.setName(b.toString());

		Table tableSrc = getTableSources();
		Table tableRel = (normalized ? getTableNormalized() : getTableRaw());

		view.setMasterTable(tableSrc);

		Relation relation = new Relation();
		relation.setLocalTable(tableSrc);
		relation.setForeignTable(tableRel);
		relation.add(tableSrc.getField(DB.FIELD_BAR_TIME), tableRel.getField(DB.FIELD_BAR_TIME));
		view.addRelation(relation);

		view.addField(tableSrc.getField(DB.FIELD_BAR_TIME));
		view.addField(tableSrc.getField(DB.FIELD_BAR_TIME_FMT));
		view.addField(tableSrc.getField(DB.FIELD_BAR_OPEN));
		view.addField(tableSrc.getField(DB.FIELD_BAR_HIGH));
		view.addField(tableSrc.getField(DB.FIELD_BAR_LOW));
		view.addField(tableSrc.getField(DB.FIELD_BAR_CLOSE));

		view.addField(tableSrc.getField(DB.FIELD_SOURCES_PIVOT_CALC));
		view.addField(tableSrc.getField(DB.FIELD_SOURCES_REFV_CALC));
		view.addField(tableSrc.getField(DB.FIELD_SOURCES_LABEL_CALC));
		view.addField(tableSrc.getField(DB.FIELD_SOURCES_LABEL_NETC));

		view.addField(tableSrc.getField(DB.FIELD_SOURCES_PIVOT_EDIT));
		view.addField(tableSrc.getField(DB.FIELD_SOURCES_REFV_EDIT));
		view.addField(tableSrc.getField(DB.FIELD_SOURCES_LABEL_EDIT));
		view.addField(tableSrc.getField(DB.FIELD_SOURCES_LABEL_NETE));

		List<Field> fields = null;

		if (averages) {
			fields = getFieldListAverages();
			for (Field field : fields) {
				view.addField(tableSrc.getField(field.getAlias()));
			}
		}

		if (slopes) {
			fields = getFieldListSlopes();
			for (Field field : fields) {
				view.addField(tableRel.getField(field.getAlias()));
			}
		}

		if (spreads) {
			fields = getFieldListSpreads();
			for (Field field : fields) {
				view.addField(tableRel.getField(field.getAlias()));
			}
		}

		if (candles) {
			fields = getFieldListCandles();
			for (Field field : fields) {
				view.addField(tableRel.getField(field.getAlias()));
			}
		}

		view.setOrderBy(tableSrc.getPrimaryKey());
		view.setPersistor(new DBPersistor(MLT.getDBEngine(), view));
		return view;
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
		// TODO Auto-generated method stub

	}
}
