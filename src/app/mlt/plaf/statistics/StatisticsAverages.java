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

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.mlt.db.Calculator;
import com.mlt.db.Field;
import com.mlt.db.FieldGroup;
import com.mlt.db.Index;
import com.mlt.db.ListPersistor;
import com.mlt.db.Order;
import com.mlt.db.Persistor;
import com.mlt.db.PersistorException;
import com.mlt.db.Record;
import com.mlt.db.RecordSet;
import com.mlt.db.Table;
import com.mlt.db.Value;
import com.mlt.db.View;
import com.mlt.db.rdbms.DBPersistor;
import com.mlt.desktop.Option;
import com.mlt.desktop.TaskFrame;
import com.mlt.desktop.action.ActionRun;
import com.mlt.desktop.control.Control;
import com.mlt.desktop.control.PopupMenu;
import com.mlt.desktop.control.PopupMenuProvider;
import com.mlt.desktop.control.TablePane;
import com.mlt.desktop.control.TableRecord;
import com.mlt.desktop.control.TableRecordModel;
import com.mlt.desktop.control.table.SelectionMode;
import com.mlt.desktop.icon.IconGrid;
import com.mlt.mkt.data.DataRecordSet;
import com.mlt.mkt.data.Instrument;
import com.mlt.mkt.data.Period;
import com.mlt.ml.function.Normalizer;
import com.mlt.util.HTML;
import com.mlt.util.Logs;
import com.mlt.util.Numbers;
import com.mlt.util.StringConverter;
import com.mlt.util.Strings;
import com.mlt.util.xml.Parser;
import com.mlt.util.xml.ParserHandler;
import com.mlt.util.xml.XMLAttribute;
import com.mlt.util.xml.XMLWriter;

import app.mlt.plaf.DB;
import app.mlt.plaf.MLT;
import app.mlt.plaf.db.Domains;
import app.mlt.plaf.db.Fields;
import app.mlt.plaf.db.converters.NumberScaleConverter;
import app.mlt.plaf.db.fields.FieldDataInst;
import app.mlt.plaf.db.fields.FieldTime;
import app.mlt.plaf.db.fields.FieldTimeFmt;

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

	/**
	 * Browse the ranges.
	 */
	class ActionBrowseRanges extends ActionRun {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			String key = null;
			try {
				Instrument instrument = getInstrument();
				Period period = getPeriod();

				StringBuilder keyBuilder = new StringBuilder();
				keyBuilder.append("BROWSE-RANGES-");
				keyBuilder.append(instrument.getId());
				keyBuilder.append("-");
				keyBuilder.append(period.getId());
				keyBuilder.append("-");
				keyBuilder.append(getId());
				keyBuilder.append("-");
				keyBuilder.append(getKey());
				key = keyBuilder.toString();

				StringBuilder textBuilder = new StringBuilder();
				textBuilder.append(instrument.getDescription());
				textBuilder.append(" ");
				textBuilder.append(period);
				textBuilder.append(" ");
				textBuilder.append(getId());
				textBuilder.append(" ");
				textBuilder.append(getKey());
				textBuilder.append(" Ranges");
				String text = textBuilder.toString();

				Persistor persistor = getViewRanges().getPersistor();
				MLT.getStatusBar().setProgressIndeterminate(key, "Setup " + text, true);

				TableRecordModel model = new TableRecordModel(persistor.getDefaultRecord());
				model.addColumn(Fields.RANGE_NAME);
				model.addColumn(Fields.RANGE_MIN_MAX);
				model.addColumn(Fields.RANGE_MINIMUM);
				model.addColumn(Fields.RANGE_MAXIMUM);
				model.addColumn(Fields.RANGE_AVERAGE);
				model.addColumn(Fields.RANGE_STDDEV);
				model.addColumn(Fields.RANGE_RANGE);

				model.setRecordSet(getViewRanges().getPersistor().select(null));

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

			} catch (Exception exc) {
				Logs.catching(exc);
				MLT.getStatusBar().removeProgress(key);
			}
		}
	}

	/**
	 * Browse the ranges.
	 */
	class ActionBrowseRangesRaw extends ActionRun {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			String key = null;
			try {
				Instrument instrument = getInstrument();
				Period period = getPeriod();

				StringBuilder keyBuilder = new StringBuilder();
				keyBuilder.append("BROWSE-RANGES-RAW");
				keyBuilder.append(instrument.getId());
				keyBuilder.append("-");
				keyBuilder.append(period.getId());
				keyBuilder.append("-");
				keyBuilder.append(getId());
				keyBuilder.append("-");
				keyBuilder.append(getKey());
				key = keyBuilder.toString();

				StringBuilder textBuilder = new StringBuilder();
				textBuilder.append(instrument.getDescription());
				textBuilder.append(" ");
				textBuilder.append(period);
				textBuilder.append(" ");
				textBuilder.append(getId());
				textBuilder.append(" ");
				textBuilder.append(getKey());
				textBuilder.append(" Ranges raw");
				String text = textBuilder.toString();

				Persistor persistor = getTableRanges().getPersistor();
				MLT.getStatusBar().setProgressIndeterminate(key, "Setup " + text, true);

				TableRecordModel model = new TableRecordModel(persistor.getDefaultRecord());
				for (int i = 0; i < getTableRanges().getFieldCount(); i++) {
					String alias = getTableRanges().getField(i).getAlias();
					if (alias.equals(Fields.BAR_TIME)) {
						continue;
					}
					model.addColumn(alias);
				}

				Order order = new Order();
				order.add(getTableRanges().getField(Fields.RANGE_NAME));
				order.add(getTableRanges().getField(Fields.RANGE_MIN_MAX));
				order.add(getTableRanges().getField(Fields.RANGE_VALUE));

				model.setRecordSet(getTableRanges().getPersistor().select(null, order));

				TableRecord table = new TableRecord(true);
				table.setSelectionMode(SelectionMode.SINGLE_ROW_SELECTION);
				table.setModel(model);
				table.setSelectedRow(0);

				TablePane tablePane = new TablePane(table);

				IconGrid iconGrid = new IconGrid();
				iconGrid.setSize(16, 16);
				iconGrid.setMarginFactors(0.12, 0.12, 0.12, 0.12);

				MLT.getTabbedPane().addTab(key, iconGrid, text, "Defined ", tablePane);
				MLT.getStatusBar().removeProgress(key);

			} catch (Exception exc) {
				Logs.catching(exc);
				MLT.getStatusBar().removeProgress(key);
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
				Instrument instrument = getInstrument();
				Period period = getPeriod();

				StringBuilder keyBuilder = new StringBuilder();
				keyBuilder.append("BROWSE-STATS-");
				keyBuilder.append(instrument.getId());
				keyBuilder.append("-");
				keyBuilder.append(period.getId());
				keyBuilder.append("-");
				keyBuilder.append(getId());
				keyBuilder.append("-");
				keyBuilder.append(getKey());
				String key = keyBuilder.toString();

				StringBuilder textBuilder = new StringBuilder();
				textBuilder.append(instrument.getDescription());
				textBuilder.append(" ");
				textBuilder.append(period);
				textBuilder.append(" ");
				textBuilder.append(getId());
				textBuilder.append(" ");
				textBuilder.append(getKey());
				textBuilder.append(" States");
				String text = textBuilder.toString();

				MLT.getStatusBar().setProgressIndeterminate(key, "Setup " + text, true);

				ListPersistor persistor = new ListPersistor(getTableStates().getPersistor());
				persistor.setCacheSize(40000);
				persistor.setPageSize(100);

				TableRecordModel model = new TableRecordModel(persistor.getDefaultRecord());
				model.addColumn(Fields.BAR_TIME_FMT);
				model.addColumn(Fields.BAR_OPEN);
				model.addColumn(Fields.BAR_HIGH);
				model.addColumn(Fields.BAR_LOW);
				model.addColumn(Fields.BAR_CLOSE);

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
			frame.show();
		}

	}

	/**
	 * Range calculator (average + 2 * std_dev)
	 */
	class CalculatorRange implements Calculator {
		@Override
		public Value getValue(Record record) {
			String min_max = record.getValue(Fields.RANGE_MIN_MAX).getString();
			double average = record.getValue(Fields.RANGE_AVERAGE).getDouble();
			double std_dev = record.getValue(Fields.RANGE_STDDEV).getDouble();
			double mult = (min_max.equals("min") ? -1 : 1);
			double value = average + (2 * mult * std_dev);
			return new Value(value);
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
		private List<Average> averages = new ArrayList<>();

		/**
		 * Constructor.
		 */
		public ParametersHandler() {
			super();
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
				if (!path.equals("averages") && !path.equals("averages/average")) {
					throw new Exception("Invalid path: " + path);
				}

				/* Validate that the average path has no attributes. */
				if (path.equals("averages")) {
					if (attributes.getLength() > 0) {
						throw new Exception("Path \"" + averages + "\" can not have attributes");
					}
				}

				/* Validate and retrieve attributes of averages/average path. */
				if (path.equals("averages/average")) {

					/*
					 * Attributes must be type, period, optionally smooths. Any attribute that is
					 * not one of these is not valid. At least type and period must be set.
					 */
					boolean attrTypeSet = false;
					boolean attrPeriodSet = false;
					for (int i = 0; i < attributes.getLength(); i++) {
						String name = attributes.getQName(i);
						if (!Strings.in(name, "type", "period", "smooths")) {
							throw new Exception("Invalid attribute " + name + " in path " + path);
						}
						if (name.equals("type")) attrTypeSet = true;
						if (name.equals("period")) attrPeriodSet = true;
					}
					if (!attrTypeSet) {
						throw new Exception("The attribute \"type\" must be set.");
					}
					if (!attrPeriodSet) {
						throw new Exception("The attribute \"period\" must be set.");
					}

					/* Type. */
					String attrType = attributes.getValue("type");
					Average.Type type = Average.Type.valueOf(attrType);

					/* Period. */
					String attrPeriod = attributes.getValue("period");
					int period = 0;
					try {
						period = Integer.parseInt(attrPeriod);
					} catch (NumberFormatException exc) {
						throw new Exception("Invalid period " + attrPeriod, exc);
					}

					/* Smooths. */
					String attrSmooths = attributes.getValue("smooths");
					int[] smooths = null;
					if (attrSmooths != null && !attrSmooths.isEmpty()) {
						String[] s_smooths = Strings.parse(attrSmooths, ",");
						smooths = new int[s_smooths.length];
						for (int i = 0; i < s_smooths.length; i++) {
							smooths[i] = Integer.parseInt(s_smooths[i]);
						}
					}

					/* Add the average. */
					averages.add(new Average(type, period, smooths));
				}
			} catch (Exception exc) {
				throw new SAXException(exc.getMessage(), exc);
			}
		}

		/**
		 * Return the list of averages.
		 * 
		 * @return The list of averages.
		 */
		public List<Average> getAverages() {
			return averages;
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
		Instrument instrument = DB.to_instrument(rc.getValue(Fields.INSTRUMENT_ID).getString());
		Period period = DB.to_period(rc.getValue(Fields.PERIOD_ID).getString());

		/* Statistics averages. */
		StatisticsAverages stats = new StatisticsAverages(instrument, period);
		stats.setId(rc.getValue(Fields.STATISTICS_ID).getString());
		stats.setKey(rc.getValue(Fields.STATISTICS_KEY).getString());
		stats.setParameters(rc.getValue(Fields.STATISTICS_PARAMS).getString());

		return stats;

	}

	/** List of averages. */
	private List<Average> averages = new ArrayList<>();
	/** States table. */
	private Table tableStates;
	/** Ranges table. */
	private Table tableRanges;

	/** Map of field lists. */
	private HashMap<String, List<Field>> mapLists = new HashMap<>();

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
	 * @param label  Field label.
	 * @param fast   Fast period.
	 * @param slow   Slow period.
	 * @param index  Field index.
	 * @param scale  Format scale.
	 * @return The field.
	 */
	private Field getCandleField(
		String name,
		String header,
		String label,
		int fast,
		int slow,
		int index,
		int scale) {
		return getCandleField(name, header, label, fast, slow, index, scale, null);
	}

	/**
	 * Return the candle field.
	 * 
	 * @param name   Field name.
	 * @param header Field header.
	 * @param label  Field label.
	 * @param fast   Fast period.
	 * @param slow   Slow period.
	 * @param index  Field index.
	 * @param scale  Format scale.
	 * @param suffix Optional suffix.
	 * @return The field.
	 */
	private Field getCandleField(
		String name,
		String header,
		String label,
		int fast,
		int slow,
		int index,
		int scale,
		String suffix) {
		return getCandleField(name, header, label, fast, slow, index, -1, scale, suffix);
	}

	/**
	 * Return the candle field.
	 * 
	 * @param name   Field name.
	 * @param header Field header.
	 * @param label  Field label.
	 * @param fast   Fast period.
	 * @param slow   Slow period.
	 * @param index0 Field first index.
	 * @param index1 Field second index.
	 * @param scale  Format scale.
	 * @param suffix Optional suffix.
	 * @return The field.
	 */
	private Field getCandleField(
		String name,
		String header,
		String label,
		int fast,
		int slow,
		int index0,
		int index1,
		int scale,
		String suffix) {
		name = getNameCandle(name, fast, slow, index0, index1, suffix);
		header = getHeaderCandle(header, fast, slow, index0, index1, suffix);
		label = getLabelCandle(label, fast, slow, index0, index1, suffix);
		Field field = Domains.getDouble(name, header, label);
		field.setStringConverter(getNumberConverter(scale));
		return field;
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
				Field field = Domains.getDouble(name, header, label);
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
			int count = slow / fast;
			for (int j = 0; j < count; j++) {
				// @formatter:off

					/* Open, high, low, close. */
					fields.add(getCandleField("open", "Open", "Open", fast, slow, j, 4));
					fields.add(getCandleField("high", "High", "High", fast, slow, j, 4));
					fields.add(getCandleField("low", "Low", "Low", fast, slow, j, 4));
					fields.add(getCandleField("close", "Close", "Close", fast, slow, j, 4));

					/* Raw values. */
					fields.add(getCandleField("range", "Range", "Range", fast, slow, j, 4, "raw"));
					fields.add(getCandleField("body_factor", "Body factor", "factor", fast, slow, j, 8, "raw"));
					fields.add(getCandleField("body_pos", "Body pos", "Body position", fast, slow, j, 8, "raw"));
					if (j < count - 1) {
						fields.add(getCandleField("center_factor", "Center factor", "Sign", fast, slow, j, j + 1, 8, "raw"));
					}
					fields.add(getCandleField("sign", "Sign", "Sign", fast, slow, j, 8, "raw"));
					
					/* Normalized values. */
					fields.add(getCandleField("range", "Range", "Range", fast, slow, j, 8, "nrm"));
					fields.add(getCandleField("body_factor", "Body factor", "factor", fast, slow, j, 8, "nrm"));
					fields.add(getCandleField("body_pos", "Body pos", "Body position", fast, slow, j, 8, "nrm"));
					if (j < count - 1) {
						fields.add(getCandleField("center_factor", "Center factor", "Sign", fast, slow, j, j + 1, 8, "nrm"));
					}
					
					/* Sign, continuous from -1 to 1. */
					fields.add(getCandleField("sign", "Sign", "Sign", fast, slow, j, 8, "nrm"));
					
					// @formatter:on
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
				field = Domains.getDouble(name, header, label);
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
					field = Domains.getDouble(name, header, label);
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
	 * @param index  The index.
	 * @return The name of the candle.
	 */
	public String getHeaderCandle(String header, int fast, int slow, int index) {
		return getHeaderCandle(header, fast, slow, index, null);
	}

	/**
	 * Return the header of an Open/High/Low/Close... candle that relates the fast
	 * and slow periods.
	 * 
	 * @param header The name (open/high/low/close)
	 * @param fast   The fast period.
	 * @param slow   The slow period.
	 * @param index  The index.
	 * @param suffix The suffix.
	 * @return The name of the candle.
	 */
	public String getHeaderCandle(String header, int fast, int slow, int index, String suffix) {
		return getLabelCandle(header, fast, slow, index, suffix);
	}

	/**
	 * Return the label of an Open/High/Low/Close... candle that relates the fast
	 * and slow periods.
	 * 
	 * @param label  The name (open/high/low/close)
	 * @param fast   The fast period.
	 * @param slow   The slow period.
	 * @param index0 The first index.
	 * @param index1 The second index.
	 * @param suffix The suffix.
	 * @return The name of the candle.
	 */
	public String getHeaderCandle(
		String label,
		int fast,
		int slow,
		int index0,
		int index1,
		String suffix) {
		return getLabelCandle(label, fast, slow, index0, index1, suffix);
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
	 * Return the label of an Open/High/Low/Close... candle that relates the fast
	 * and slow periods.
	 * 
	 * @param label The name (open/high/low/close)
	 * @param fast  The fast period.
	 * @param slow  The slow period.
	 * @param index The index.
	 * @return The name of the candle.
	 */
	public String getLabelCandle(String label, int fast, int slow, int index) {
		return getLabelCandle(label, fast, slow, index, null);
	}

	/**
	 * Return the label of an Open/High/Low/Close... candle that relates the fast
	 * and slow periods.
	 * 
	 * @param label  The name (open/high/low/close)
	 * @param fast   The fast period.
	 * @param slow   The slow period.
	 * @param index  The index.
	 * @param suffix The suffix.
	 * @return The name of the candle.
	 */
	public String getLabelCandle(String label, int fast, int slow, int index, String suffix) {
		return getLabelCandle(label, fast, slow, index, -1, suffix);
	}

	/**
	 * Return the label of an Open/High/Low/Close... candle that relates the fast
	 * and slow periods.
	 * 
	 * @param label  The name (open/high/low/close)
	 * @param fast   The fast period.
	 * @param slow   The slow period.
	 * @param index0 The first index.
	 * @param index1 The second index.
	 * @param suffix The suffix.
	 * @return The name of the candle.
	 */
	public String getLabelCandle(
		String label,
		int fast,
		int slow,
		int index0,
		int index1,
		String suffix) {
		int count = slow / fast;
		int pad = Numbers.getDigits(count);
		StringBuilder b = new StringBuilder();
		b.append(label);
		b.append(" ");
		b.append(fast);
		b.append("/");
		b.append(slow);
		b.append(" - ");
		b.append(Strings.leftPad(Integer.toString(index0), pad));
		if (index1 >= 0) {
			b.append(" vs ");
			b.append(Strings.leftPad(Integer.toString(index1), pad));
		}
		if (suffix != null) {
			b.append(" - ");
			b.append(suffix);
		}
		return b.toString();
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
	 * @return The map with the normalizers of the range fields.
	 */
	public HashMap<String, Normalizer> getMapNormalizers() throws PersistorException {
		HashMap<String, Normalizer> map = new HashMap<>();
		RecordSet recordSet = getViewRanges().getPersistor().select(null);
		for (Record record : recordSet) {
			String name = record.getValue(Fields.RANGE_NAME).getString();
			String min_max = record.getValue(Fields.RANGE_MIN_MAX).getString();
			double minimum = record.getValue(Fields.RANGE_MINIMUM).getDouble();
			double maximum = record.getValue(Fields.RANGE_MAXIMUM).getDouble();
			double range = record.getValue(Fields.RANGE_RANGE).getDouble();
			if (min_max.equals("max")) {
				if (range > maximum) {
					range = maximum;
				}
			} else {
				if (range < minimum) {
					range = minimum;
				}
			}
			Normalizer normalizer = map.get(name);
			if (normalizer == null) {
				normalizer = new Normalizer();
				normalizer.setNormalizedLow(-1);
				normalizer.setNormalizedHigh(1);
				map.put(name, normalizer);
			}
			if (min_max.equals("max")) {
				normalizer.setDataHigh(range);
			} else {
				normalizer.setDataLow(range);
			}
		}
		return map;
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
	 * @param index The index.
	 * @return The name of the candle.
	 */
	public String getNameCandle(String name, int fast, int slow, int index) {
		return getNameCandle(name, fast, slow, index, null);
	}

	/**
	 * Return the name of an open/high/low/close... candle that relates the fast and
	 * slow periods.
	 * 
	 * @param name   The name (open/high/low/close)
	 * @param fast   The fast period.
	 * @param slow   The slow period.
	 * @param index  The index.
	 * @param suffix The suffix.
	 * @return The name of the candle.
	 */
	public String getNameCandle(String name, int fast, int slow, int index, String suffix) {
		return getNameCandle(name, fast, slow, index, -1, suffix);
	}

	/**
	 * Return the name of an open/high/low/close... candle that relates the fast and
	 * slow periods.
	 * 
	 * @param name   The name (open/high/low/close)
	 * @param fast   The fast period.
	 * @param slow   The slow period.
	 * @param index  The index.
	 * @param suffix The suffix.
	 * @return The name of the candle.
	 */
	public String getNameCandle(
		String name,
		int fast,
		int slow,
		int index0,
		int index1,
		String suffix) {
		int count = slow / fast;
		int pad = Numbers.getDigits(count);
		StringBuilder b = new StringBuilder();
		b.append(name);
		b.append("_");
		b.append(fast);
		b.append("_");
		b.append(slow);
		b.append("_");
		b.append(Strings.leftPad(Integer.toString(index0), pad));
		if (index1 >= 0) {
			b.append("vs");
			b.append(Strings.leftPad(Integer.toString(index1), pad));
		}
		if (suffix != null) {
			b.append("_");
			b.append(suffix);
		}
		return b.toString();
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
		options.add(optionCalculate);

		/* Browse statictics. */
		Option optionBrowseStats = new Option();
		optionBrowseStats.setKey("BROWSE-STATS");
		optionBrowseStats.setText("Browse states");
		optionBrowseStats.setToolTip("Browse state values");
		optionBrowseStats.setAction(new ActionBrowseStats());
		optionBrowseStats.setOptionGroup(new Option.Group("BROWSE", 2));
		optionBrowseStats.setSortIndex(2);
		options.add(optionBrowseStats);

		/* Browse ranges. */
		Option optionBrowseRanges = new Option();
		optionBrowseRanges.setKey("BROWSE-RANGES");
		optionBrowseRanges.setText("Browse ranges statistic values");
		optionBrowseRanges
			.setToolTip("Browse ranges minimum, maximum, average and standard deviation values");
		optionBrowseRanges.setAction(new ActionBrowseRanges());
		optionBrowseRanges.setOptionGroup(new Option.Group("BROWSE", 2));
		optionBrowseRanges.setSortIndex(2);
		options.add(optionBrowseRanges);

		/* Browse ranges raw values. */
		Option optionBrowseRangesRaw = new Option();
		optionBrowseRangesRaw.setKey("BROWSE-RANGES-RAW");
		optionBrowseRangesRaw.setText("Browse ranges raw values");
		optionBrowseRangesRaw.setToolTip("Browse ranges raw values");
		optionBrowseRangesRaw.setAction(new ActionBrowseRangesRaw());
		optionBrowseRangesRaw.setOptionGroup(new Option.Group("BROWSE", 2));
		optionBrowseRangesRaw.setSortIndex(3);
		options.add(optionBrowseRangesRaw);

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

		/* Name of the field. */
		Field fieldName = Domains.getString(Fields.RANGE_NAME, 60, "Name", "Field name");
		tableRanges.addField(fieldName);

		/* Min-max indicator. */
		Field fieldMinMax = Domains.getString(Fields.RANGE_MIN_MAX, 3, "Min-Max", "Min-Max");
		tableRanges.addField(fieldMinMax);

		/* Value. */
		Field fieldValue = Domains.getDouble(Fields.RANGE_VALUE, "Value", "Value");
		fieldValue.setStringConverter(getNumberConverter(8));
		tableRanges.addField(fieldValue);

		/* Reference of the time of the registered values. */
		Field fieldTime = new FieldTime(Fields.BAR_TIME);
		tableRanges.addField(fieldTime);
		tableRanges.addField(new FieldTimeFmt(Fields.BAR_TIME_FMT, period));

		/* Non unique index on name, min-max and period. */
		Index index = new Index();
		index.add(tableRanges.getField(Fields.RANGE_NAME));
		index.add(tableRanges.getField(Fields.RANGE_MIN_MAX));
		index.add(tableRanges.getField(Fields.BAR_TIME));
		index.setUnique(false);
		tableRanges.addIndex(index);

		View view = tableRanges.getComplexView(index);
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
		tableStates.addField(new FieldTime(Fields.BAR_TIME));

		tableStates.addField(new FieldDataInst(instrument, Fields.BAR_OPEN, "Open", "Open value"));
		tableStates.addField(new FieldDataInst(instrument, Fields.BAR_HIGH, "High", "High value"));
		tableStates.addField(new FieldDataInst(instrument, Fields.BAR_LOW, "Low", "Low value"));
		tableStates
			.addField(new FieldDataInst(instrument, Fields.BAR_CLOSE, "Close", "Close value"));

		tableStates.addField(new FieldTimeFmt(Fields.BAR_TIME_FMT, period));

		int ndx = 0;
		FieldGroup grpData = new FieldGroup(ndx++, "data", "Data");
		tableStates.getField(Fields.BAR_TIME).setFieldGroup(grpData);
		tableStates.getField(Fields.BAR_OPEN).setFieldGroup(grpData);
		tableStates.getField(Fields.BAR_HIGH).setFieldGroup(grpData);
		tableStates.getField(Fields.BAR_LOW).setFieldGroup(grpData);
		tableStates.getField(Fields.BAR_CLOSE).setFieldGroup(grpData);
		tableStates.getField(Fields.BAR_TIME_FMT).setFieldGroup(grpData);

		/*
		 * Calculated pivot (High=1, None=0, Low=-1) and Label (Long=1, Out=0, Short=-1)
		 */
		FieldGroup grpLabels = new FieldGroup(ndx++, "labels", "Labels");
		tableStates.addField(Domains.getInteger("pivot", "Pivot", "Pivot"));
		tableStates.addField(Domains.getInteger("label", "Label", "Label"));
		tableStates.getField("pivot").setFieldGroup(grpLabels);
		tableStates.getField("label").setFieldGroup(grpLabels);

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

		/* Normailzed flag. */
		FieldGroup grpControls = new FieldGroup(ndx++, "controls", "Controls");
		Field normalized = Domains.getString(Fields.STATES_NORMALIZED, 1, "Nrm", "Normalized");
		normalized.setFieldGroup(grpControls);
		tableStates.addField(normalized);

		tableStates.getField(Fields.BAR_TIME).setPrimaryKey(true);
		
		Index index = new Index();
		index.add(tableStates.getField(Fields.STATES_NORMALIZED));
		tableStates.addIndex(index);
		
		View view = tableStates.getComplexView(tableStates.getPrimaryKey());
		tableStates.setPersistor(new DBPersistor(MLT.getDBEngine(), view));

		return tableStates;
	}

	/**
	 * Return the ranges view.
	 * 
	 * @return
	 */
	public View getViewRanges() {

		View view = new View();
		view.setMasterTable(getTableRanges());

		Field name = getTableRanges().getField(Fields.RANGE_NAME);
		Field min_max = getTableRanges().getField(Fields.RANGE_MIN_MAX);
		Field minimum = Domains.getDouble(Fields.RANGE_MINIMUM, "Minimum", "Minimum");
		minimum.setStringConverter(getNumberConverter(8));
		minimum.setFunction("min(" + Fields.RANGE_VALUE + ")");
		Field maximum = Domains.getDouble(Fields.RANGE_MAXIMUM, "Maximum", "Maximum");
		maximum.setStringConverter(getNumberConverter(8));
		maximum.setFunction("max(" + Fields.RANGE_VALUE + ")");
		Field average = Domains.getDouble(Fields.RANGE_AVERAGE, "Average", "Average");
		average.setStringConverter(getNumberConverter(8));
		average.setFunction("avg(" + Fields.RANGE_VALUE + ")");
		Field std_dev = Domains.getDouble(Fields.RANGE_STDDEV, "Std Dev", "Std Dev");
		std_dev.setStringConverter(getNumberConverter(8));
		std_dev.setFunction("stddev(" + Fields.RANGE_VALUE + ")");
		Field range =
			Domains.getDouble(Fields.RANGE_RANGE, "Range (avg+-2*stddev)", "Range (avg+-2*stddev)");
		range.setPersistent(false);
		range.setStringConverter(getNumberConverter(8));
		range.setCalculator(new CalculatorRange());

		view.addField(name);
		view.addField(min_max);
		view.addField(minimum);
		view.addField(maximum);
		view.addField(average);
		view.addField(std_dev);
		view.addField(range);

		view.addGroupBy(name);
		view.addGroupBy(min_max);

		view.addOrderBy(name);
		view.addOrderBy(min_max);

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
		averages.addAll(handler.getAverages());
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
	}
}
