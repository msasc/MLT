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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import com.mlt.desktop.TaskFrame;
import com.mlt.desktop.action.ActionRun;
import com.mlt.desktop.control.Control;
import com.mlt.desktop.control.PopupMenu;
import com.mlt.desktop.control.PopupMenuProvider;
import com.mlt.desktop.control.TablePane;
import com.mlt.desktop.control.TableRecord;
import com.mlt.desktop.control.TableRecordModel;
import com.mlt.desktop.control.table.SelectionMode;
import com.mlt.desktop.converters.NumberScaleConverter;
import com.mlt.desktop.icon.IconGrid;
import com.mlt.mkt.data.DataConverter;
import com.mlt.mkt.data.DataRecordSet;
import com.mlt.mkt.data.Instrument;
import com.mlt.mkt.data.Period;
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
public class Statistics {

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

				View view = getView(normalized, true, true, true, true, true, true, true);
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
			frame.addTasks(new TaskAveragesRaw(Statistics.this));
			frame.addTasks(new TaskAveragesRanges(Statistics.this));
			frame.addTasks(new TaskAveragesNormalize(Statistics.this));
			frame.addTasks(new TaskAveragesPivots(Statistics.this));
			frame.addTasks(new TaskAveragesLabelsCalc(Statistics.this));
			frame.addTasks(new TaskAveragesPatterns(Statistics.this, true, 0.8));
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
			set("statistics/averages/average", "period", "integer");
			set("statistics/averages/average", "delta", "double");
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

					/* Period. */
					int period = getInteger(attributes, "period");
					if (period <= 0) {
						throw new Exception("Invalid period " + period);
					}

					/* Delta. */
					double delta = getDouble(attributes, "delta");
					if (delta <= 0) {
						throw new Exception("Invalid delta " + delta);
					}

					/* Smooths. */
					int[] smooths = getIntegerArray(attributes, "smooths");

					/* Add the average. */
					averages.add(new Average(period, delta, smooths));
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
	 * Return statistics averages from a statistics record.
	 * 
	 * @param rc The statistics definition record.
	 * @return The statistics object.
	 */
	public static Statistics getStatistics(Record rc) throws Exception {

		/* Instrument and period. */
		Instrument instrument = DB.to_instrument(rc.getValue(DB.FIELD_INSTRUMENT_ID).getString());
		Period period = DB.to_period(rc.getValue(DB.FIELD_PERIOD_ID).getString());

		/* Statistics averages. */
		Statistics stats = new Statistics(instrument, period);
		stats.setId(rc.getValue(DB.FIELD_STATISTICS_ID).getString());
		stats.setParameters(rc.getValue(DB.FIELD_STATISTICS_PARAMS).getString());

		return stats;

	}

	/** Identifier that indicates the class of statistics. */
	private String id;
	/** Instrument. */
	private Instrument instrument;
	/** Period. */
	private Period period;

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

	/** File path for pattern files. */
	private String filePath;

	/**
	 * @param instrument The instrument.
	 * @param period     The period.
	 */
	public Statistics(Instrument instrument, Period period) {
		super();

		this.instrument = instrument;
		this.period = period;

		properties = new Properties();
		averages = new ArrayList<>();
		barsAhead = 100;
		percentCalc = 10;
		percentEdit = 10;
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
	DataConverter getChartDataConverter() {
		DataConverter dataConverter = (DataConverter) properties.getObject("data_converter");
		if (dataConverter != null) {
			return dataConverter;
		}

		View view = getView(true, true, true, true, true, true, true, false);
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
		fields.addAll(getFieldListAverages());
		fields.addAll(getFieldListPatterns(false));
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
	ListPersistor getChartListPersistor() {
		ListPersistor persistor = (ListPersistor) properties.getObject("data_persistor");
		if (persistor == null) {
			View view = getView(true, true, true, true, true, true, true, false);
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
	 * @param key The field group key.
	 * @return The field group.
	 */
	FieldGroup getFieldGroup(String key) {
		FieldGroup fieldGroup = (FieldGroup) properties.getObject(key);
		return fieldGroup;
	}

	/**
	 * @return The list of average fields.
	 */
	List<Field> getFieldListAverages() {
		List<Field> fields = new ArrayList<>();
		for (int i = 0; i < averages.size(); i++) {
			Average avg = averages.get(i);
			String name = getNameAvg(avg);
			String header = getHeader(name);
			Field field = DB.field_double(name, header);
			field.setStringConverter(new NumberScaleConverter(8));
			field.setFieldGroup(getFieldGroup("group-avgs"));
			fields.add(field);
		}
		return fields;
	}

	/**
	 * @return The list of slope fields.
	 */
	List<Field> getFieldListAvgSlopes() {
		List<Field> fields = new ArrayList<>();
		for (int i = 0; i < averages.size(); i++) {
			Average avg = averages.get(i);
			String name = getNameAvgSlope(avg);
			String header = getHeader(name);
			Field field = DB.field_double(name, header);
			field.setStringConverter(new NumberScaleConverter(8));
			field.setFieldGroup(getFieldGroup("group-avg-slopes"));
			fields.add(field);
		}
		return fields;
	}

	/**
	 * @return The list of spread fields.
	 */
	List<Field> getFieldListAvgSpreads() {
		List<Field> fields = new ArrayList<>();
		String name, header;
		Field field;
		for (int i = 0; i < averages.size(); i++) {
			Average fast = averages.get(i);
			for (int j = i + 1; j < averages.size(); j++) {
				Average slow = averages.get(j);
				name = getNameAvgSpread(fast, slow);
				header = getHeader(name);
				field = DB.field_double(name, header);
				field.setStringConverter(new NumberScaleConverter(8));
				field.setFieldGroup(getFieldGroup("group-avg-spreads"));
				fields.add(field);
			}
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
				if (candleName.equals(DB.FIELD_CANDLE_REL_RANGE) && index == count - 1) {
					continue;
				}
				if (candleName.equals(DB.FIELD_CANDLE_REL_BODY) && index == count - 1) {
					continue;
				}
				String name = getNameCandle(size, index, candleName);
				String header = getHeader(name);
				Field field = DB.field_double(name, header);
				field.setStringConverter(new NumberScaleConverter(8));
				field.setFieldGroup(getFieldGroup("group-candles-" + size));
				fields.add(field);
			}
		}
		return fields;
	}

	/**
	 * @return The list of pattern fields (raw and normalized)
	 */
	List<Field> getFieldListPatterns(boolean candles) {
		List<Field> fields = new ArrayList<>();
		fields.addAll(getFieldListAvgSlopes());
		fields.addAll(getFieldListAvgSpreads());
		fields.addAll(getFieldListVariances());
		fields.addAll(getFieldListVarSlopes());
		fields.addAll(getFieldListVarSpreads());
		if (candles) fields.addAll(getFieldListCandles());
		return fields;
	}

	/**
	 * @return The list of variance fields.
	 */
	List<Field> getFieldListVariances() {
		List<Field> fields = new ArrayList<>();
		String name, header;
		Field field;
		for (int i = 0; i < averages.size() - 1; i++) {
			Average fast = averages.get(i);
			for (int j = i + 1; j < averages.size(); j++) {
				Average slow = averages.get(j);
				for (int k = j; k < averages.size(); k++) {
					int period = averages.get(k).getPeriod();
					name = getNameVar(fast, slow, period);
					header = getHeader(name);
					field = DB.field_double(name, header);
					field.setStringConverter(new NumberScaleConverter(8));
					field.setFieldGroup(getFieldGroup("group-vars"));
					fields.add(field);
				}
			}
		}
		return fields;
	}

	/**
	 * @return The list of variance slope fields.
	 */
	List<Field> getFieldListVarSlopes() {
		List<Field> fields = new ArrayList<>();
		String name, header;
		Field field;
		for (int i = 0; i < averages.size() - 1; i++) {
			Average fast = averages.get(i);
			for (int j = i + 1; j < averages.size(); j++) {
				Average slow = averages.get(j);
				for (int k = j; k < averages.size(); k++) {
					int period = averages.get(k).getPeriod();
					name = getNameVarSlope(fast, slow, period);
					header = getHeader(name);
					field = DB.field_double(name, header);
					field.setStringConverter(new NumberScaleConverter(8));
					field.setFieldGroup(getFieldGroup("group-var-slopes"));
					fields.add(field);
				}
			}
		}
		return fields;
	}

	/**
	 * @return The list of variance slope fields.
	 */
	List<Field> getFieldListVarSpreads() {
		List<Field> varFields = getFieldListVariances();
		List<Field> fields = new ArrayList<>();
		String name, header;
		Field field;
		for (int i = 0; i < varFields.size() - 1; i++) {
			String nameFast = varFields.get(i).getName();
			String nameSlow = varFields.get(i + 1).getName();
			name = getNameVarSpread(nameFast, nameSlow);
			header = getHeader(name);
			field = DB.field_double(name, header);
			field.setStringConverter(new NumberScaleConverter(8));
			field.setFieldGroup(getFieldGroup("group-var-spreads"));
			fields.add(field);
		}
		return fields;
	}

	/**
	 * Return the id.
	 * 
	 * @return The id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Return the instrument.
	 * 
	 * @return The instrument.
	 */
	public Instrument getInstrument() {
		return instrument;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLabel() {
		StringBuilder b = new StringBuilder();
		b.append(getInstrument().getId());
		b.append(" ");
		b.append(getPeriod().toString());
		b.append(" ");
		b.append(getId());
		return b.toString();
	}

	/**
	 * @param avg The average.
	 * @return The name.
	 */
	String getNameAvg(Average avg) {
		return "avg_" + getPadded(avg.getPeriod());
	}

	/**
	 * @param avg The average.
	 * @return The field name.
	 */
	String getNameAvgSlope(Average avg) {
		return "avg_slope_" + getPadded(avg.getPeriod());
	}

	/**
	 * @param fast Fast average.
	 * @param slow Slow average.
	 * @return the field name.
	 */
	String getNameAvgSpread(Average fast, Average slow) {
		StringBuilder b = new StringBuilder();
		b.append("avg_spread_");
		b.append(getPadded(fast.getPeriod()));
		b.append("_");
		b.append(getPadded(slow.getPeriod()));
		return b.toString();
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
	 * @param fast   Fast average.
	 * @param slow   Slow average.
	 * @param period Back period.
	 * @return the field name.
	 */
	String getNameVar(Average fast, Average slow, int period) {
		return getNameVar("var", fast, slow, period);
	}

	/**
	 * @param prefix Prefix.
	 * @param fast   Fast average.
	 * @param slow   Slow average.
	 * @param period Back period.
	 * @return the field name.
	 */
	private String getNameVar(String prefix, Average fast, Average slow, int period) {
		StringBuilder b = new StringBuilder();
		b.append(prefix);
		b.append("_");
		b.append(getPadded(fast.getPeriod()));
		b.append("_");
		b.append(getPadded(slow.getPeriod()));
		b.append("_");
		b.append(getPadded(period));
		return b.toString();
	}

	/**
	 * @param fast   Fast average.
	 * @param slow   Slow average.
	 * @param period Back period.
	 * @return the field name.
	 */
	String getNameVarSlope(Average fast, Average slow, int period) {
		return getNameVar("var_slope", fast, slow, period);
	}

	/**
	 * @param nameFast Fast variance name.
	 * @param nameSlow Slow variance name.
	 * @return Spread name.
	 */
	String getNameVarSpread(String nameFast, String nameSlow) {
		nameFast = Strings.remove(nameFast, "var_");
		nameSlow = Strings.remove(nameSlow, "var_");
		StringBuilder b = new StringBuilder();
		b.append("var_spread_");
		b.append(nameFast);
		b.append("_");
		b.append(nameSlow);
		return b.toString();
	}

	/**
	 * Return a network definition given the list of hidden layer sizes factors
	 * versus the previous layer size.
	 * 
	 * @param factors The list of hidden layer size factors.
	 * @return The network definition.
	 */
	Network getNetwork(int... sizes) {

		Network network = new Network();
		network.setName(getNetworkName(sizes));

		/* Layers. */
		int inputSize, outputSize;
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
//			activation = new ActivationSigmoid();
			outputSize = sizes[i];
			network.addBranch(Builder.branchPerceptron(inputSize, outputSize, activation));
		}
		inputSize = sizes[sizes.length - 1];
		outputSize = getPatternOutputSize();
		activation = new ActivationSoftMax();
		network.addBranch(Builder.branchPerceptron(inputSize, outputSize, activation));

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
		option.setAction(new ActionChart(this));
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
			Numbers.getDigits(averages.get(averages.size() - 1).getPeriod()),
			"0");
	}

	/**
	 * {@inheritDoc}
	 */
	public String getParametersDescription() {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < averages.size(); i++) {
			if (i > 0) {
				b.append(", ");
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
		DefaultPattern pattern = new DefaultPattern(inputValues, outputValues);
		return pattern;
	}

	/**
	 * @return The list of pattern fields.
	 */
	List<Field> getPatternFields() {
		@SuppressWarnings("unchecked")
		List<Field> fields = (List<Field>) properties.getObject("pattern_fields");
		if (fields == null) {
			fields = new ArrayList<>(getFieldListPatterns(true));
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
		root.append(
			DB.name_ticker(
				getInstrument(),
				getPeriod(),
				getTableNameSuffix(null)));
		root.append("-" + getPatternInputSize());
		return Strings.replace(root.toString(), "_", "-").toUpperCase();
	}

	/**
	 * @return The pattern input size.
	 */
	int getPatternInputSize() {
		return getPatternFields().size();
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
	 * @return The pattern output size.
	 */
	int getPatternOutputSize() {
		return 3;
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
				Pattern pattern = new DefaultPattern(inputValues, outputValues);
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
	 * Return the period.
	 * 
	 * @return The period.
	 */
	public Period getPeriod() {
		return period;
	}

	/**
	 * @param suffix Last suffix.
	 * @return The table name suffix.
	 */
	String getTableNameSuffix(String suffix) {
		StringBuilder b = new StringBuilder();
		b.append(getId());
		if (suffix != null) {
			b.append("_");
			b.append(suffix);
		}
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

		List<Field> fields = getFieldListPatterns(true);
		for (Field field : fields) {
			table.addField(field);
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

		List<Field> fields = getFieldListPatterns(true);
		for (Field field : fields) {
			table.addField(field);
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

		table.getField(DB.FIELD_BAR_TIME).setFieldGroup(getFieldGroup("group-data"));
		table.getField(DB.FIELD_BAR_OPEN).setFieldGroup(getFieldGroup("group-data"));
		table.getField(DB.FIELD_BAR_HIGH).setFieldGroup(getFieldGroup("group-data"));
		table.getField(DB.FIELD_BAR_LOW).setFieldGroup(getFieldGroup("group-data"));
		table.getField(DB.FIELD_BAR_CLOSE).setFieldGroup(getFieldGroup("group-data"));
		table.getField(DB.FIELD_BAR_TIME_FMT).setFieldGroup(getFieldGroup("group-data"));

		table.addField(DB.field_data(instrument, DB.FIELD_SOURCES_REFV_CALC, "V-Calc"));
		table.addField(DB.field_integer(DB.FIELD_SOURCES_PIVOT_CALC, "P-Calc"));
		table.addField(DB.field_string(DB.FIELD_SOURCES_LABEL_CALC, 2, "L-Calc"));
		table.addField(DB.field_string(DB.FIELD_SOURCES_LABEL_NETC, 2, "L-Net-C"));

		table.addField(DB.field_data(instrument, DB.FIELD_SOURCES_REFV_EDIT, "V-Edit"));
		table.addField(DB.field_integer(DB.FIELD_SOURCES_PIVOT_EDIT, "P-Edit"));
		table.addField(DB.field_string(DB.FIELD_SOURCES_LABEL_EDIT, 2, "L-Edit"));
		table.addField(DB.field_string(DB.FIELD_SOURCES_LABEL_NETE, 2, "L-Net-E"));

		table.getField(DB.FIELD_SOURCES_REFV_CALC).setFieldGroup(getFieldGroup("group-labels"));
		table.getField(DB.FIELD_SOURCES_PIVOT_CALC).setFieldGroup(getFieldGroup("group-labels"));
		table.getField(DB.FIELD_SOURCES_LABEL_CALC).setFieldGroup(getFieldGroup("group-labels"));
		table.getField(DB.FIELD_SOURCES_LABEL_NETC).setFieldGroup(getFieldGroup("group-labels"));

		table.getField(DB.FIELD_SOURCES_REFV_EDIT).setFieldGroup(getFieldGroup("group-labels"));
		table.getField(DB.FIELD_SOURCES_PIVOT_EDIT).setFieldGroup(getFieldGroup("group-labels"));
		table.getField(DB.FIELD_SOURCES_LABEL_EDIT).setFieldGroup(getFieldGroup("group-labels"));
		table.getField(DB.FIELD_SOURCES_LABEL_NETE).setFieldGroup(getFieldGroup("group-labels"));

		List<Field> fields = getFieldListAverages();
		for (Field field : fields) {
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
	String getTabPaneKey(String prefix) {
		StringBuilder b = new StringBuilder();
		b.append("-");
		b.append(getInstrument().getId());
		b.append("-");
		b.append(getPeriod().getId());
		b.append("-");
		b.append(getId());
		return b.toString();
	}

	/**
	 * @param suffix The tab suffix.
	 * @return A proper tab text.
	 */
	String getTabPaneText(String suffix) {
		StringBuilder b = new StringBuilder();
		b.append(getInstrument().getDescription());
		b.append(" ");
		b.append(getPeriod());
		b.append(" ");
		b.append(getId());
		b.append(" ");
		b.append(suffix);
		return b.toString();
	}

	/**
	 * @return The he trainer to train the network..
	 * @throws Exception
	 */
	private Trainer getTrainer() throws Exception {

		Network network = getNetwork(512);

		Trainer trainer = new Trainer();
		trainer.setProgressModulus(10);
		trainer.setNetwork(network);
		trainer.setPatternSourceTraining(getPatternSource(true, true));
		trainer.setPatternSourceTest(getPatternSource(true, false));

		trainer.setShuffle(false);
		trainer.setScore(false);
		trainer.setEpochs(20);
		trainer.setGenerateReport(true, network.getName());

		trainer.setFilePath("res/network/");
		trainer.setFileRoot(network.getName());

		trainer.setTitle(network.getName());

		return trainer;
	}

	/**
	 * @param normalized Normalized / raw values.
	 * @param averages   Show averages.
	 * @param avgSlopes  Show average slopes.
	 * @param avgSpreads Show average spreads.
	 * @param variances  Show variances.
	 * @param varSlopes  Show variance slopes.
	 * @param varSpreads Show variance spreads.
	 * @param candles    Show candles.
	 * @return The view.
	 */
	View getView(
		boolean normalized,
		boolean averages,
		boolean avgSlopes,
		boolean avgSpreads,
		boolean variances,
		boolean varSlopes,
		boolean varSpreads,
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
		if (avgSlopes) {
			b.append("-avg_slopes");
		}
		if (avgSpreads) {
			b.append("-avg_spreads");
		}
		if (variances) {
			b.append("-vars");
		}
		if (varSlopes) {
			b.append("-vars_slopes");
		}
		if (varSpreads) {
			b.append("-vars_spreads");
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

		List<Field> fields;
		if (averages) {
			fields = getFieldListAverages();
			fields.forEach(field -> view.addField(tableSrc.getField(field.getAlias())));
		}
		fields = new ArrayList<>();
		if (avgSlopes) fields.addAll(getFieldListAvgSlopes());
		if (avgSpreads) fields.addAll(getFieldListAvgSpreads());
		if (variances) fields.addAll(getFieldListVariances());
		if (varSlopes) fields.addAll(getFieldListVarSlopes());
		if (varSpreads) fields.addAll(getFieldListVarSpreads());
		if (candles) fields.addAll(getFieldListCandles());
		fields.forEach(field -> view.addField(tableRel.getField(field.getAlias())));

		view.setOrderBy(tableSrc.getPrimaryKey());
		view.setPersistor(new DBPersistor(MLT.getDBEngine(), view));
		return view;
	}

	private void setFieldGroups() {

		List<String> groupKeys = new ArrayList<>();
		Iterator<Object> keys = properties.keySet().iterator();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			if (key.startsWith("group-")) {
				groupKeys.add(key);
			}
		}
		groupKeys.forEach(key -> properties.remove(key));

		int ndx = 0;
		properties.setObject("group-data", new FieldGroup(ndx++, "data", "Data"));
		properties.setObject("group-labels", new FieldGroup(ndx++, "labels", "Labels"));
		properties.setObject("group-avgs", new FieldGroup(ndx++, "avgs", "Averages"));
		properties.setObject("group-avg-slopes", new FieldGroup(ndx++, "avg-slopes", "Avg-Slopes"));
		properties.setObject(
			"group-avg-spreads",
			new FieldGroup(ndx++, "avg-spreads", "Avg-Spreads"));
		properties.setObject("group-vars", new FieldGroup(ndx++, "vars", "Variances"));
		properties.setObject("group-var-slopes", new FieldGroup(ndx++, "var-slopes", "Var-Slopes"));
		properties.setObject(
			"group-var-spreads",
			new FieldGroup(ndx++, "var-spreads", "Var-Spreads"));
		for (int i = 0; i < averages.size(); i++) {
			int size = getCandleSize(i);
			String suffix = getPadded(size);
			properties.setObject(
				"group-candles-" + size,
				new FieldGroup(ndx++, "candles_" + suffix, "Candles " + suffix));
		}
	}

	/**
	 * Set the id.
	 * 
	 * @param id The statistics id.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setParameters(String parameters) throws Exception {
		ParametersHandler handler = new ParametersHandler();
		Parser parser = new Parser();
		parser.parse(new ByteArrayInputStream(parameters.getBytes()), handler);
		averages.clear();
		averages.addAll(handler.averages);
		barsAhead = handler.barsAhead;
		percentCalc = handler.percentCalc;
		percentEdit = handler.percentEdit;
		setFieldGroups();
	}

	/**
	 * {@inheritDoc}
	 */
	public void validate() throws Exception {
		// TODO Auto-generated method stub

	}
}
