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
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.mlt.db.Field;
import com.mlt.db.FieldGroup;
import com.mlt.db.ListPersistor;
import com.mlt.db.Order;
import com.mlt.db.Persistor;
import com.mlt.db.Record;
import com.mlt.db.RecordIterator;
import com.mlt.db.Relation;
import com.mlt.db.Table;
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
import com.mlt.desktop.converters.NumberScaleConverter;
import com.mlt.desktop.icon.IconGrid;
import com.mlt.mkt.data.DataRecordSet;
import com.mlt.mkt.data.Instrument;
import com.mlt.mkt.data.Period;
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

	/**
	 * Browse raw values.
	 */
	class ActionBrowseRaw extends ActionRun {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			try {
				String key = getTabPaneKey("STATS-BROWSE");
				String text = getTabPaneText("States");

				MLT.getStatusBar().setProgressIndeterminate(key, "Setup " + text, true);

				View view = getViewRaw();
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
			frame.setTitle(getLabel());
			frame.addTasks(new TaskAveragesRaw(StatisticsAverages.this));
			frame.show();
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
	 * Parameters.
	 */
	public static class Parameters {
		/** List of averages. */
		public List<Average> averages;
		/** Bars ahead to calculate pivots. */
		public int barsAhead;
		/** Percentage to set calculated labels. */
		public double percentCalc;
		/** Percentage to set edited labels. */
		public double percentEdit;
	}

	/**
	 * Parameters handler.
	 */
	private static class ParametersHandler extends ParserHandler {

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
	 * @param statsPrev Previous statistics.
	 * @param statsNext Next statistics.
	 * @return A boolean indicating whether the averages has been modified between
	 *         two parameters.
	 */
	public static final boolean checkAveragesModified(
		StatisticsAverages statsPrev,
		StatisticsAverages statsNext) {
		List<Average> avgsPrev = statsPrev.getParameters().averages;
		List<Average> avgsNext = statsNext.getParameters().averages;
		if (avgsPrev.size() != avgsNext.size()) {
			return true;
		}
		for (int i = 0; i < avgsPrev.size(); i++) {
			Average avgPrev = avgsPrev.get(i);
			Average avgNext = avgsNext.get(i);
			if (!avgPrev.equals(avgNext)) {
				return true;
			}
		}
		return false;
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

	/**
	 * @param instrument The instrument.
	 * @param period     The period.
	 */
	public StatisticsAverages(Instrument instrument, Period period) {
		super(instrument, period);
		properties = new Properties();
		Parameters parameters = new Parameters();
		parameters.averages = new ArrayList<>();
		parameters.barsAhead = 100;
		parameters.percentCalc = 10;
		parameters.percentEdit = 10;
		properties.setObject("parameters", parameters);
	}

	/**
	 * Add an average to the list of averages in a period ascending order.
	 * 
	 * @param avg The average to add.
	 */
	public void addAverage(Average avg) {
		List<Average> averages = getParameters().averages;
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
		getParameters().averages.clear();
		getParameters().averages = null;
		properties.clear();
		properties = null;
		super.finalize();
	}

	/**
	 * @param index Index of average.
	 * @return Number of candles.
	 */
	public int getCandleCount(int index) {
		List<Average> averages = getParameters().averages;
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
	public int getCandleSize(int index) {
		List<Average> averages = getParameters().averages;
		int size = (index == 0 ? 1 : averages.get(index - 1).getPeriod());
		return size;
	}

	/**
	 * @return The list of average fields.
	 */
	public List<Field> getFieldListAverages() {
		List<Average> averages = getParameters().averages;
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
	 * @param i Averages index.
	 * @return The list of candle fields.
	 */
	public List<Field> getFieldListCandles(int i) {
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
	public List<Field> getFieldListSlopes() {
		List<Average> averages = getParameters().averages;
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
	public List<Field> getFieldListSpreads() {
		List<Average> averages = getParameters().averages;
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
	 * @return The last time of the states table.
	 */
	long getLastSourcesTime() throws Throwable {
		long time = 0;
		Persistor persistor = getTableSources().getPersistor();
		Field ftime = persistor.getField(DB.FIELD_BAR_TIME);
		Order order = new Order();
		order.add(ftime, false);
		RecordIterator iter = persistor.iterator(null, order);
		if (iter.hasNext()) {
			Record rc = iter.next();
			time = rc.getValue(DB.FIELD_BAR_TIME).getLong();
		}
		iter.close();
		return time;
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

		/* Browse source and raw values. */
		Option optionBrowseStats = new Option();
		optionBrowseStats.setKey("BROWSE-RAW");
		optionBrowseStats.setText("Browse raw values");
		optionBrowseStats.setToolTip("Browse source and raw values");
		optionBrowseStats.setAction(new ActionBrowseRaw());
		optionBrowseStats.setOptionGroup(new Option.Group("BROWSE", 2));
		optionBrowseStats.setSortIndex(1);
		options.add(optionBrowseStats);

		return options;
	}

	/**
	 * @param number The number, size or period.
	 * @return The padded string.
	 */
	private String getPadded(int number) {
		List<Average> averages = getParameters().averages;
		return Strings.leftPad(
			Integer.toString(number),
			Numbers.getDigits(averages.get(averages.size() - 1).getPeriod()), "0");
	}

	/**
	 * @return The parameters.
	 */
	public Parameters getParameters() {
		return (Parameters) properties.getObject("parameters");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getParametersDescription() {
		List<Average> averages = getParameters().averages;
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
	 * @param suffix Last suffix.
	 * @return The table name suffix.
	 */
	private String getTableNameSuffix(String suffix) {
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
	public Table getTableNormalized() {

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

		List<Average> averages = getParameters().averages;
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
	public Table getTableRanges() {

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
	public Table getTableRaw() {

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

		List<Average> averages = getParameters().averages;
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
	public Table getTableSources() {

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

		table.addField(DB.field_data(instrument, DB.FIELD_STATES_REFV_CALC, "V-Calc"));
		table.addField(DB.field_integer(DB.FIELD_STATES_PIVOT_CALC, "P-Calc"));
		table.addField(DB.field_integer(DB.FIELD_STATES_LABEL_CALC, "L-Calc"));
		table.addField(DB.field_integer(DB.FIELD_STATES_LABEL_CALC_SET, "LC-Set"));
		table.addField(DB.field_data(instrument, DB.FIELD_STATES_REFV_EDIT, "V-Edit"));
		table.addField(DB.field_integer(DB.FIELD_STATES_PIVOT_EDIT, "P-Edit"));
		table.addField(DB.field_integer(DB.FIELD_STATES_LABEL_EDIT, "L-Edit"));
		table.addField(DB.field_integer(DB.FIELD_STATES_LABEL_EDIT_SET, "LE-Set"));

		FieldGroup grpLabels = new FieldGroup(ndx++, "labels", "Labels");
		table.getField(DB.FIELD_STATES_REFV_CALC).setFieldGroup(grpLabels);
		table.getField(DB.FIELD_STATES_PIVOT_CALC).setFieldGroup(grpLabels);
		table.getField(DB.FIELD_STATES_LABEL_CALC).setFieldGroup(grpLabels);
		table.getField(DB.FIELD_STATES_LABEL_CALC_SET).setFieldGroup(grpLabels);
		table.getField(DB.FIELD_STATES_REFV_EDIT).setFieldGroup(grpLabels);
		table.getField(DB.FIELD_STATES_PIVOT_EDIT).setFieldGroup(grpLabels);
		table.getField(DB.FIELD_STATES_LABEL_EDIT).setFieldGroup(grpLabels);
		table.getField(DB.FIELD_STATES_LABEL_EDIT_SET).setFieldGroup(grpLabels);

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
	 * @return The view to borwse raw values.
	 */
	public View getViewRaw() {
		View view = new View();
		view.setName("view-" + getLabel());

		Table tableSrc = getTableSources();
		Table tableRaw = getTableRaw();

		view.setMasterTable(tableSrc);

		Relation relRaw = new Relation();
		relRaw.setLocalTable(tableSrc);
		relRaw.setForeignTable(tableRaw);
		relRaw.add(tableSrc.getField(DB.FIELD_BAR_TIME), tableRaw.getField(DB.FIELD_BAR_TIME));
		view.addRelation(relRaw);

		view.addField(tableSrc.getField(DB.FIELD_BAR_TIME));
		view.addField(tableSrc.getField(DB.FIELD_BAR_TIME_FMT));
		view.addField(tableSrc.getField(DB.FIELD_BAR_OPEN));
		view.addField(tableSrc.getField(DB.FIELD_BAR_HIGH));
		view.addField(tableSrc.getField(DB.FIELD_BAR_LOW));
		view.addField(tableSrc.getField(DB.FIELD_BAR_CLOSE));

		List<Field> fields;

		fields = getFieldListAverages();
		for (Field field : fields) {
			view.addField(tableSrc.getField(field.getAlias()));
		}

		fields = getFieldListSlopes();
		for (Field field : fields) {
			view.addField(tableRaw.getField(field.getAlias()));
		}

		fields = getFieldListSpreads();
		for (Field field : fields) {
			view.addField(tableRaw.getField(field.getAlias()));
		}

		List<Average> averages = getParameters().averages;
		for (int i = 0; i < averages.size(); i++) {
			fields = getFieldListCandles(i);
			for (Field field : fields) {
				view.addField(tableRaw.getField(field.getAlias()));
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
		getParameters().averages.clear();
		getParameters().averages.addAll(handler.averages);
		getParameters().barsAhead = handler.barsAhead;
		getParameters().percentCalc = handler.percentCalc;
		getParameters().percentEdit = handler.percentEdit;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validate() throws Exception {
		// TODO Auto-generated method stub

	}
}
