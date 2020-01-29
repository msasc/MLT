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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.Icon;

import com.mlt.db.Field;
import com.mlt.db.FieldGroup;
import com.mlt.db.FieldList;
import com.mlt.db.ListPersistor;
import com.mlt.db.Record;
import com.mlt.db.Types;
import com.mlt.db.Value;
import com.mlt.db.View;
import com.mlt.desktop.Option;
import com.mlt.desktop.Option.Group;
import com.mlt.desktop.OptionWindow;
import com.mlt.desktop.action.ActionRun;
import com.mlt.desktop.control.Canvas.Context;
import com.mlt.desktop.control.Control;
import com.mlt.desktop.control.Dialog;
import com.mlt.desktop.control.FormRecordPane;
import com.mlt.desktop.control.GridBagPane;
import com.mlt.desktop.control.PopupMenu;
import com.mlt.desktop.graphic.Path;
import com.mlt.desktop.graphic.Stroke;
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
import com.mlt.mkt.data.OHLC;
import com.mlt.mkt.data.PlotData;
import com.mlt.mkt.data.info.DataInfo;
import com.mlt.util.Formats;
import com.mlt.util.Lists;
import com.mlt.util.Logs;
import com.mlt.util.Numbers;

import app.mlt.plaf.DB;
import app.mlt.plaf.MLT;

/**
 * Charts on statistics.
 *
 * @author Miquel Sas
 */
public class ActionChart extends ActionRun {

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

			this.indexOpen = getDataConverter().getIndex(DB.FIELD_BAR_OPEN);
			this.indexHigh = getDataConverter().getIndex(DB.FIELD_BAR_HIGH);
			this.indexLow = getDataConverter().getIndex(DB.FIELD_BAR_LOW);
			this.indexClose = getDataConverter().getIndex(DB.FIELD_BAR_CLOSE);
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
			super();

			this.size = size;
			this.count = count;

			this.indexOpen = getDataConverter().getIndex(DB.FIELD_BAR_OPEN);
			this.indexHigh = getDataConverter().getIndex(DB.FIELD_BAR_HIGH);
			this.indexLow = getDataConverter().getIndex(DB.FIELD_BAR_LOW);
			this.indexClose = getDataConverter().getIndex(DB.FIELD_BAR_CLOSE);

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
			super();
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
					path.addHint(
						RenderingHints.KEY_ANTIALIASING,
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
						path.setStroke(
							new Stroke(1.0, Stroke.CAP_BUTT, Stroke.JOIN_MITER, 1, new double[] {
								2 }, 0));
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
			super();
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
		if (pivots.isEmpty()) {
			return pivots;
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

	/** Plot data list. */
	private List<PlotData> plotDataList;
	/** Data converter. */
	private DataConverter dataConverter;
	/** List persistor. */
	private ListPersistor persistor;
	/** Plot all candles flag. */
	private boolean plotAllCandles = true;
	/** Statistics. */
	private Statistics stats;

	/**
	 * Constructor.
	 */
	public ActionChart(Statistics stats) {
		this.stats = stats;
	}

	private void configurePlotters(ChartContainer chart, PlotData plotData) {

		FieldList fields = new FieldList();
		List<DataPlotter> plotters = plotData.getPlotters();
		for (DataPlotter plotter : plotters) {
			Field field = new Field();
			field.setType(Types.BOOLEAN);
			field.setEditBooleanInCheckBox(true);
			field.setName(plotter.getId());
			field.setHeader(plotter.getDescription());
			field.setLabel(plotter.getDescription());
			field.setTitle(plotter.getDescription());
			field.getProperties().setObject("PLOTTER", plotter);
			fields.addField(field);
		}

		Record rc = new Record(fields);
		for (int i = 0; i < rc.size(); i++) {
			DataPlotter plotter = (DataPlotter) rc.getField(i).getProperties().getObject("PLOTTER");
			rc.setValue(i, plotter.isPlot());
		}

		FormRecordPane form = new FormRecordPane(rc);
		form.setLayoutByRows(FieldGroup.EMPTY_FIELD_GROUP);
		for (Field field : fields) {
			String alias = field.getAlias();
			form.addField(alias);
		}

		form.layout();
		form.updateEditors();

		OptionWindow wnd = new OptionWindow(new Dialog(null, new GridBagPane()));
		wnd.setTitle("Configure plotters");
		wnd.setOptionsBottom();
		wnd.setCenter(form.getPane());
		
		Option select = new Option();
		select.setKey("select");
		select.setText("Select all");
		select.setAction(e -> {
			for (Field field : fields) {
				String alias = field.getAlias();
				form.getEditContext(alias).setValue(new Value(true));
			}
		});
		wnd.getOptionPane().add(select);

		Option clear = new Option();
		clear.setKey("clear");
		clear.setText("Clear all");
		clear.setAction(e -> {
			for (Field field : fields) {
				String alias = field.getAlias();
				form.getEditContext(alias).setValue(new Value(false));
			}
		});
		wnd.getOptionPane().add(clear);

		Option accept = new Option();
		accept.setKey("accept");
		accept.setText("Accept");
		accept.setCloseWindow(true);
		wnd.getOptionPane().add(accept);

		Option cancel = new Option();
		cancel.setKey("cancel");
		cancel.setText("Cancel");
		cancel.setCloseWindow(true);
		wnd.getOptionPane().add(cancel);

		wnd.getOptionPane().setMnemonics();

		wnd.pack();
		wnd.centerOnScreen();
		wnd.show();

		Option option = wnd.getOptionExecuted();
		if (option.equals(cancel)) {
			return;
		}

		form.updateRecord();
		rc = form.getRecord();
		for (int i = 0; i < rc.size(); i++) {
			Field field = rc.getField(i);
			Value value = rc.getValue(i);
			for (DataPlotter plotter : plotters) {
				if (plotter.getId().equals(field.getAlias())) {
					plotter.setPlot(value.getBoolean());
					break;
				}
			}
		}

		chart.refreshAll();
	}

	/**
	 * @return The chart converter to data structure.
	 */
	private DataConverter getDataConverter() {
		if (dataConverter != null) {
			return dataConverter;
		}

		View view = stats.getView(true, true, true, true, true, true, true, false);
		List<Integer> list = new ArrayList<>();

		/* Open, high, low, close. */
		list.add(view.getFieldIndex(DB.FIELD_BAR_OPEN));
		list.add(view.getFieldIndex(DB.FIELD_BAR_HIGH));
		list.add(view.getFieldIndex(DB.FIELD_BAR_LOW));
		list.add(view.getFieldIndex(DB.FIELD_BAR_CLOSE));

		/* Pivots, reference values and labels. */
		list.add(view.getFieldIndex(DB.FIELD_SOURCES_PIVOT_CALC));
		list.add(view.getFieldIndex(DB.FIELD_SOURCES_REFV_CALC));

		/* Rest of fields. */
		List<Field> fields = new ArrayList<>();
		fields.addAll(stats.getFieldListAverages());
		fields.addAll(stats.getFieldListPatterns(false));
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

		return dataConverter;
	}

	/**
	 * @return The chart list persistor.
	 */
	private ListPersistor getListPersistor() {
		if (persistor == null) {
			View view = stats.getView(true, true, true, true, true, true, true, false);
			persistor = new ListPersistor(view.getPersistor());
		}
		return persistor;
	}

	/**
	 * @return A list with all plot datas.
	 */
	private List<PlotData> getPlotDataList() {
		if (plotDataList != null) {
			return plotDataList;
		}

		plotDataList = new ArrayList<>();

		/* Prices and averages. */
		{
			DataInfo info = new DataInfo();
			info.setInstrument(stats.getInstrument());
			info.setName("prices-averages");
			info.setDescription("States prices and averages");
			info.setPeriod(stats.getPeriod());
			DataListSource dataList =
				new DataListSource(info, getListPersistor(), getDataConverter());

			info.addOutput("Open", "O", OHLC.OPEN, "Open data value");
			info.addOutput("High", "H", OHLC.HIGH, "High data value");
			info.addOutput("Low", "L", OHLC.LOW, "Low data value");
			info.addOutput("Close", "C", OHLC.CLOSE, "Close data value");
			CandlestickPlotter plotterPrices = new CandlestickPlotter();
			plotterPrices.setId("Prices");
			plotterPrices.setDescription("Prices (candlestick)");
			dataList.addPlotter(plotterPrices);

			List<Average> averages = stats.getAverages();
			for (int i = 0; i < averages.size(); i++) {
				String name = averages.get(i).toString();
				Field field = stats.getFieldListAverages().get(i);
				String label = field.getLabel();
				int index = getDataConverter().getIndex(field.getAlias());
				info.addOutput(name, name, index, label);
				LinePlotter linePlotter = new LinePlotter(index);
				linePlotter.setId(field.getAlias());
				linePlotter.setDescription(field.getLabel());
				dataList.addPlotter(linePlotter);
			}

			PlotterPivots plotterPivots = new PlotterPivots();
			plotterPivots.setId("Pivots");
			plotterPivots.setDescription("Pivots on prices");
			plotterPivots.indexPivot = getDataConverter().getIndex(DB.FIELD_SOURCES_PIVOT_CALC);
			plotterPivots.indexData = getDataConverter().getIndex(DB.FIELD_SOURCES_REFV_CALC);
			plotterPivots.setIndex(getDataConverter().getIndex(DB.FIELD_BAR_CLOSE));
			dataList.addPlotter(plotterPivots);

			PlotData plotData = new PlotData("key-prices-and-averages");
			plotData.setDescription("Prices and averages");
			plotData.getProperties().setString("GROUP", "prices");
			plotData.add(dataList);
			plotDataList.add(plotData);
		}

		/* Averages. */
		{
			PlotData plotData =
				getPlotDataFieldList(
					"key-avgs",
					"Averages",
					stats.getFieldListAverages());
			plotData.getProperties().setString("GROUP", "avgs");
			plotDataList.add(plotData);
		}

		/* Slopes on averages. */
		{
			PlotData plotData =
				getPlotDataFieldList(
					"key-avg-slopes",
					"Average slopes",
					stats.getFieldListAvgSlopes());
			plotData.getProperties().setString("GROUP", "avgs");
			plotDataList.add(plotData);
		}

		/* Spreads on averages. */
		{
			PlotData plotData =
				getPlotDataFieldList(
					"key-avg-spreads",
					"Average spreads",
					stats.getFieldListAvgSpreads());
			plotData.getProperties().setString("GROUP", "avgs");
			plotDataList.add(plotData);
		}

		/* Variances of averages. */
		{
			PlotData plotData =
				getPlotDataFieldList(
					"key-vars",
					"Average variances",
					stats.getFieldListVariances());
			plotData.getProperties().setString("GROUP", "vars");
			plotDataList.add(plotData);
		}

		/* Slopes on variances. */
		{
			PlotData plotData =
				getPlotDataFieldList(
					"key-var-slopes",
					"Variance slopes",
					stats.getFieldListVarSlopes());
			plotData.getProperties().setString("GROUP", "vars");
			plotDataList.add(plotData);
		}

		/* Spreads on variances. */
		{
			PlotData plotData =
				getPlotDataFieldList(
					"key-var-spreads",
					"Variance spreads",
					stats.getFieldListVarSpreads());
			plotData.getProperties().setString("GROUP", "vars");
			plotDataList.add(plotData);
		}

		/* Labels and pivots. */
		{
			DataInfo info = new DataInfo();
			info.setInstrument(stats.getInstrument());
			info.setName("key-labels");
			info.setDescription("Calculated labels and pivots");
			info.setPeriod(stats.getPeriod());
			DataListSource dataList =
				new DataListSource(info, getListPersistor(), getDataConverter());

			PlotterLabels plotter = new PlotterLabels();
			plotter.setId("labels-pivots");
			plotter.setDescription("Labels and pivots");
			plotter.indexPivot = getDataConverter().getIndex(DB.FIELD_SOURCES_PIVOT_CALC);
			plotter.indexData = getDataConverter().getIndex(DB.FIELD_SOURCES_REFV_CALC);
			plotter.aliasLabel = DB.FIELD_SOURCES_LABEL_CALC;
			plotter.setIndex(getDataConverter().getIndex(DB.FIELD_BAR_CLOSE));
			dataList.addPlotter(plotter);

			PlotData plotData = new PlotData("key-labels");
			plotData.setDescription("Calculated labels and pivots");
			plotData.getProperties().setString("GROUP", "labels");
			plotData.add(dataList);

			plotDataList.add(plotData);
		}

		/* Candles. */
		{
			List<Average> averages = stats.getAverages();
			for (int i = 1; i < averages.size(); i++) {
				int size = stats.getCandleSize(i);
				int count = stats.getCandleCount(i);

				String name = "key-candles-" + size;

				DataInfo info = new DataInfo(new CandlesFormatter(size, count));
				info.setInstrument(stats.getInstrument());
				info.setName(name);
				info.setDescription("Candles size " + size);
				info.setPeriod(stats.getPeriod());
				DataListSource dataList =
					new DataListSource(info, stats.getChartListPersistor(), stats.getChartDataConverter());

				PlotterCandles plotter = new PlotterCandles(size, count);
				plotter.setId(name);
				plotter.setDescription("Candles of size " + size);
				dataList.addPlotter(plotter);

				PlotData plotData = new PlotData(name);
				plotData.setDescription("Candles size " + size);
				plotData.getProperties().setString("GROUP", "candles");
				plotData.add(dataList);

				plotDataList.add(plotData);
			}
		}

		return plotDataList;
	}

	/**
	 * @param key         Plot data key.
	 * @param description Description.
	 * @param fields      List of fields (plotter line)
	 * @return The plot data.
	 */
	private PlotData getPlotDataFieldList(String key, String description, List<Field> fields) {

		DataInfo info = new DataInfo();
		info.setInstrument(stats.getInstrument());
		info.setName(key);
		info.setDescription(description);
		info.setPeriod(stats.getPeriod());
		DataListSource dataList =
			new DataListSource(info, getListPersistor(), getDataConverter());

		for (Field field : fields) {
			String name = field.getHeader();
			String label = field.getLabel();
			int index = getDataConverter().getIndex(field.getAlias());
			info.addOutput(name, name, index, label);
			LinePlotter plotter = new LinePlotter(index);
			plotter.setId(field.getAlias());
			plotter.setDescription(field.getLabel());
			plotter.setColor(Color.BLACK);
			dataList.addPlotter(plotter);
		}

		PlotData plotData = new PlotData(key);
		plotData.setDescription(description);
		plotData.add(dataList);

		return plotData;

	}

	/**
	 * @param control The control that triggers the popup menu.
	 * @return The popup menu.
	 */
	private PopupMenu getPopupMenu(Control control) {
		if (!(control instanceof ChartContainer)) {
			return null;
		}
		List<PlotData> plotDataList = getPlotDataList();
		PopupMenu popup = new PopupMenu();
		ChartContainer chart = (ChartContainer) control;
		String group = null;
		for (PlotData plotData : plotDataList) {

			/* Plot data not contained, add option to select it. */
			if (!chart.containsPlotData(plotData.getId())) {
				Option option = new Option();
				option.setKey(plotData.getId());
				option.setText(plotData.getDescription());
				option.setToolTip(plotData.getDescription());
				option.setOptionGroup(Group.CONFIGURE);
				option.setDefaultClose(false);
				option.setCloseWindow(false);
				option.setAction(l -> chart.addPlotData(plotData, true));

				String plotDataGroup = plotData.getProperties().getString("GROUP");
				if (group == null) {
					group = plotDataGroup;
				}
				if (!group.equals(plotDataGroup)) {
					popup.addSeparator();
				}
				popup.add(option.getMenuItem());
				group = plotDataGroup;
			}

			/* Skip candles. */
			if (plotData.getId().startsWith("key-candles")) {
				continue;
			}

			/*
			 * The plot data if contained. Give the oportunity to activate/deactivate
			 * plotters, except for the candles plotter.
			 */
			if (chart.containsPlotData(plotData.getId())) {
				Option option = new Option();
				option.setKey(plotData.getId());
				StringBuilder text = new StringBuilder();
				text.append("Configure plotters of ");
				text.append(plotData.getDescription().toLowerCase());
				option.setText(text.toString());
				option.setToolTip(plotData.getDescription());
				option.setOptionGroup(Group.CONFIGURE);
				option.setDefaultClose(false);
				option.setCloseWindow(false);
				option.setAction(l -> configurePlotters(chart, plotData));

				String plotDataGroup = plotData.getProperties().getString("GROUP");
				if (group == null) {
					group = plotDataGroup;
				}
				if (!group.equals(plotDataGroup)) {
					popup.addSeparator();
				}
				popup.add(option.getMenuItem());
				group = plotDataGroup;
			}
		}

		List<Average> averages = stats.getAverages();
		for (int i = 1; i < averages.size(); i++) {
			int size = stats.getCandleSize(i);
			if (chart.containsPlotData("key-candles-" + size)) {
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
					chart.refreshAll();
				});
				popup.add(option.getMenuItem());
			}
		}
		return popup;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		try {

			String key = stats.getTabPaneKey("STATS-CHART");
			String text = stats.getTabPaneText("Chart");
			MLT.getStatusBar().setProgressIndeterminate(key, "Setup " + text, true);

			ChartContainer chart = new ChartContainer();
			chart.setPopupMenuProvider(control -> getPopupMenu(control));
			chart.addPlotData(getPlotDataList().get(0));

			Icon icon = Icons.getIcon(Icons.APP_16x16_CHART);
			MLT.getTabbedPane().addTab(key, icon, text, text, chart);

			MLT.getStatusBar().removeProgress(key);

		} catch (Exception exc) {
			Logs.catching(exc);
		}
	}

}
