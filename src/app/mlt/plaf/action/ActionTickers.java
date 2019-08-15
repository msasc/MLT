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
package app.mlt.plaf.action;

import java.awt.Color;
import java.awt.Font;
import java.sql.Date;
import java.time.LocalDate;

import javax.swing.Icon;

import com.mlt.db.Condition;
import com.mlt.db.Criteria;
import com.mlt.db.ListPersistor;
import com.mlt.db.Persistor;
import com.mlt.db.PersistorException;
import com.mlt.db.Record;
import com.mlt.db.RecordSet;
import com.mlt.db.Value;
import com.mlt.desktop.Option;
import com.mlt.desktop.Option.Group;
import com.mlt.desktop.action.ActionRun;
import com.mlt.desktop.control.GridBagPane;
import com.mlt.desktop.control.OptionPane;
import com.mlt.desktop.control.TablePane;
import com.mlt.desktop.control.TableRecord;
import com.mlt.desktop.control.table.SelectionMode;
import com.mlt.desktop.control.table.TableRecordModel;
import com.mlt.desktop.icon.IconChar;
import com.mlt.desktop.icon.IconGrid;
import com.mlt.desktop.icon.Icons;
import com.mlt.desktop.layout.Anchor;
import com.mlt.desktop.layout.Constraints;
import com.mlt.desktop.layout.Fill;
import com.mlt.desktop.layout.Insets;
import com.mlt.desktop.layout.Orientation;
import com.mlt.mkt.chart.ChartContainer;
import com.mlt.mkt.chart.plotter.HistogramPlotter;
import com.mlt.mkt.data.Data;
import com.mlt.mkt.data.DataListSource;
import com.mlt.mkt.data.DataRecordSet;
import com.mlt.mkt.data.IndicatorDataList;
import com.mlt.mkt.data.IndicatorUtils;
import com.mlt.mkt.data.Instrument;
import com.mlt.mkt.data.Period;
import com.mlt.mkt.data.PlotData;
import com.mlt.mkt.data.PlotType;
import com.mlt.mkt.data.info.DataInfo;
import com.mlt.mkt.data.info.PriceInfo;
import com.mlt.mkt.data.info.VolumeInfo;
import com.mlt.mkt.server.Server;
import com.mlt.util.Colors;
import com.mlt.util.Logs;

import app.mlt.plaf.MLT;
import app.mlt.plaf.db.Database;
import app.mlt.plaf.db.Fields;

/**
 * Packs the tickers actions, starting with the main tickers browse.
 *
 * @author Miquel Sas
 */
public class ActionTickers extends ActionRun {

	/**
	 * Browse the ticker.
	 */
	class ActionBrowse extends ActionRun {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			try {
				if (tableTickers.getSelectedRecord() == null) {
					return;
				}
				Record selected = tableTickers.getSelectedRecord();

				Server server = MLT.getServer();
				Database db = MLT.getDatabase();
				String instrumentId = selected.getValue(Fields.INSTRUMENT_ID).getString();
				Instrument instrument = db.fromRecordToInstrument(db.getRecord_Instrument(server, instrumentId));
				String periodId = selected.getValue(Fields.PERIOD_ID).getString();
				Period period = Period.parseId(periodId);

				String key = "BROWSE-" + instrumentId + "-" + periodId;
				String text = instrument.getDescription() + " " + period;
				MLT.getStatusBar().setProgressIndeterminate(key, "Setup " + text, true);

				ListPersistor persistor = 
					new ListPersistor(db.getPersistor_DataPrice(server, instrument, period));
				
				long timeLower = Date.valueOf(LocalDate.of(2018, 1, 1)).getTime();
				long timeUpper = Date.valueOf(LocalDate.of(2018, 12, 31)).getTime();
				Criteria criteria = new Criteria();
				criteria.add(Condition.fieldGE(persistor.getField(0), new Value(timeLower)));
				criteria.add(Condition.fieldLE(persistor.getField(0), new Value(timeUpper)));
//				persistor.setGlobalCriteria(criteria);

				TableRecordModel model = new TableRecordModel(persistor.getDefaultRecord());
				model.addColumn(Fields.TIME_FMT);
				model.addColumn(Fields.OPEN);
				model.addColumn(Fields.HIGH);
				model.addColumn(Fields.LOW);
				model.addColumn(Fields.CLOSE);
				model.addColumn(Fields.VOLUME);
				model.setRecordSet(new DataRecordSet(persistor));

				TableRecord table = new TableRecord();
				table.setSelectionMode(SelectionMode.SINGLE_ROW_SELECTION);
				table.setModel(model);
				table.setSelectedRow(0);

				TablePane tablePane = new TablePane(table);

				IconGrid iconGrid = new IconGrid();
				iconGrid.setSize(16, 16);
				iconGrid.setMarginFactors(0.12, 0.12, 0.12, 0.12);

				MLT.getTabbedPane().addTab(key, iconGrid, text, "Defined ", tablePane);

				MLT.getStatusBar().removeProgress(key);

			} catch (PersistorException exc) {
				Logs.catching(exc);
			}
		}
	}

	/**
	 * Chart the ticker.
	 */
	class ActionChart extends ActionRun {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			try {
				if (tableTickers.getSelectedRecord() == null) {
					return;
				}
				Record selected = tableTickers.getSelectedRecord();

				Server server = MLT.getServer();
				Database db = MLT.getDatabase();
				String instrumentId = selected.getValue(Fields.INSTRUMENT_ID).getString();
				Instrument instrument = db.fromRecordToInstrument(db.getRecord_Instrument(server, instrumentId));
				String periodId = selected.getValue(Fields.PERIOD_ID).getString();
				Period period = Period.parseId(periodId);

				String key = "CHART-" + instrumentId + "-" + periodId;
				String text = instrument.getDescription() + " " + period;
				MLT.getStatusBar().setProgressIndeterminate(key, "Setup " + text, true);

				ListPersistor persistor =
					new ListPersistor(db.getPersistor_DataPrice(server, instrument, period));
				persistor.setCacheSize(50000);

				/* Build the plot data. */
				DataInfo infoPrice = new PriceInfo(instrument, period);
				DataListSource price = new DataListSource(infoPrice, persistor);
				price.setPlotType(PlotType.CANDLESTICK);
				PlotData plotDataPrice = new PlotData();
				plotDataPrice.add(price);

				/* A smoothed WMA of 20 periods. */
				IndicatorDataList wma20 =
					IndicatorUtils.getSmoothedWeightedMovingAverage(price, Data.CLOSE, Colors.DARKRED, 200, 5, 3, 3);
//				plotDataPrice.add(wma20);

				/* A smoothed WMA of 100 periods. */
				IndicatorDataList wma100 =
					IndicatorUtils.getSmoothedWeightedMovingAverage(price, Data.CLOSE, Colors.DARKBLUE, 600, 5, 3, 3);
//				plotDataPrice.add(wma100);

//				IndicatorDataList sma100 = IndicatorUtils.getSimpleMovingAverage(price, Data.CLOSE, Colors.DARKBLUE, 100);
//				plotDataPrice.add(sma100);

				/* Volume plot data. */
				VolumeInfo infoVolume = new VolumeInfo(instrument, period);
				DataListSource volume = new DataListSource(infoVolume, persistor);
				HistogramPlotter volumePlotter = new HistogramPlotter(Data.VOLUME);
				volumePlotter.setColorBullishEven(Colors.DARKGRAY);
				volumePlotter.setColorBearishEven(Colors.DARKGRAY);
				volumePlotter.setColorBullishOdd(Colors.DARKGRAY);
				volumePlotter.setColorBearishOdd(Colors.DARKGRAY);
				volumePlotter.setDataWidthFactor(0.5);
				volumePlotter.setPaintBorder(false);
				volume.addPlotter(volumePlotter);
//				volume.setPlotType(PlotType.HISTOGRAM);
				PlotData plotDataVolume = new PlotData();
				plotDataVolume.setZeroAsMinimum(true);
				plotDataVolume.add(volume);

				ChartContainer chart = new ChartContainer();
				chart.addPlotData(plotDataPrice);
//				chart.addPlotData(plotDataVolume);

				Icon icon = Icons.getIcon(Icons.APP_16x16_CHART);
				MLT.getTabbedPane().addTab(key, icon, text, "Defined ", chart);

				MLT.getStatusBar().removeProgress(key);
			} catch (PersistorException exc) {
				Logs.catching(exc);
			}
		}
	}

	/**
	 * The table.
	 */
	private TableRecord tableTickers;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		try {
			if (MLT.getTabbedPane().getTabIndex("TICKERS") >= 0) {
				return;
			}
			MLT.getStatusBar().setLabel("TICKERS", "Setup tickers");

			Server server = MLT.getServer();
			Persistor persistor = MLT.getDatabase().getPersistor_Tickers();
			RecordSet recordSet = MLT.getDatabase().getRecordSet_Tickers(server);
			Record masterRecord = persistor.getDefaultRecord();

			TableRecordModel model = new TableRecordModel(masterRecord);
			model.addColumn(Fields.INSTRUMENT_ID);
			model.addColumn(Fields.PERIOD_NAME);
			model.addColumn(Fields.TABLE_NAME);
			model.setRecordSet(recordSet);

			tableTickers = new TableRecord();
			tableTickers.setSelectionMode(SelectionMode.SINGLE_ROW_SELECTION);
			tableTickers.setModel(model);
			tableTickers.setSelectedRow(0);

			TablePane tablePane = new TablePane(tableTickers);

			Option create = new Option();
			create.setText("Create");
			create.setToolTip("Create a new ticker");
			create.setOptionGroup(Group.EDIT);

			Option delete = new Option();
			delete.setText("Delete");
			delete.setToolTip("Delete the selected ticker");
			delete.setOptionGroup(Group.EDIT);

			Option browse = new Option();
			browse.setText("Browse");
			browse.setToolTip("Browse the selected ticker");
			browse.setAction(new ActionBrowse());
			browse.setOptionGroup(Group.EDIT);

			Option chart = new Option();
			chart.setText("Chart");
			chart.setToolTip("View a chart with the selected ticker");
			chart.setAction(new ActionChart());
			chart.setOptionGroup(Group.EDIT);

			Option purge = new Option();
			purge.setText("Purge");
			purge.setToolTip("Purge the selected ticker");
			purge.setOptionGroup(Group.OPERATION);

			Option download = new Option();
			download.setText("Download");
			download.setToolTip("Download the selected ticker");
			download.setOptionGroup(Group.OPERATION);

			OptionPane optionPane = new OptionPane(Orientation.HORIZONTAL);
			optionPane.add(create, delete, browse, chart, purge, download);
			tableTickers.setPopupMenuProvider(optionPane);

			GridBagPane pane = new GridBagPane();
			pane.add(tablePane, new Constraints(Anchor.TOP, Fill.BOTH, 0, 0, new Insets(0, 0, 0, 0)));
			pane.add(optionPane, new Constraints(Anchor.BOTTOM, Fill.HORIZONTAL, 0, 1, new Insets(0, 0, 0, 0)));

			IconChar iconChar = new IconChar();
			iconChar.setText("k");
			iconChar.setPaintForegroundEnabled(Color.RED);
			iconChar.setFont(new Font("Times New Roman Italic", Font.ITALIC, 12));
			iconChar.setSize(16, 16);
			iconChar.setMarginFactors(0.1, 0.1, 0.1, 0.1);
			iconChar.setFilled(false);
			iconChar.setOpaque(false);

			MLT.getTabbedPane().addTab("TICKERS", iconChar, "Tickers", "Defined tickers", pane);

			MLT.getStatusBar().removeLabel("TICKERS");
		} catch (PersistorException exc) {
			Logs.catching(exc);
		}
	}
}
