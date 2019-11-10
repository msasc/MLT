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
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.mlt.db.Field;
import com.mlt.db.Index;
import com.mlt.db.ListPersistor;
import com.mlt.db.Record;
import com.mlt.db.Table;
import com.mlt.db.View;
import com.mlt.db.rdbms.DBPersistor;
import com.mlt.desktop.Option;
import com.mlt.desktop.TaskFrame;
import com.mlt.desktop.action.ActionRun;
import com.mlt.desktop.control.TablePane;
import com.mlt.desktop.control.TableRecord;
import com.mlt.desktop.control.TableRecordModel;
import com.mlt.desktop.control.table.SelectionMode;
import com.mlt.desktop.icon.IconGrid;
import com.mlt.mkt.data.DataRecordSet;
import com.mlt.mkt.data.Instrument;
import com.mlt.mkt.data.Period;
import com.mlt.util.HTML;
import com.mlt.util.Logs;
import com.mlt.util.Numbers;
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
import app.mlt.plaf.db.fields.FieldPeriod;
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
				for (int i = 0; i < getFieldListAverages().size(); i++) {
					model.addColumn(getFieldListAverages().get(i).getAlias());
				}
				for (int i = 0; i < getFieldListAverageSlopes().size(); i++) {
					model.addColumn(getFieldListAverageSlopes().get(i).getAlias());
				}
				for (int i = 0; i < getFieldListAverageSpreads().size(); i++) {
					model.addColumn(getFieldListAverageSpreads().get(i).getAlias());
				}
				for (int i = 0; i < getFieldListCandles().size(); i++) {
					model.addColumn(getFieldListCandles().get(i).getAlias());
				}

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

			/* Calculate raw values. */
			TaskAveragesRaw taskRaw = new TaskAveragesRaw(StatisticsAverages.this);

			/* Task frame. */
			TaskFrame frame = new TaskFrame();
			frame.setTitle(getLabel());
			frame.addTasks(taskRaw);
			frame.show();
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
	
	/** Averages field list. */
	private List<Field> fieldListAverages;
	/** Average slopes field list. */
	private List<Field> fieldListAverageSlopes;
	/** Average spreads field list. */
	private List<Field> fieldListAverageSpreads;
	/** Candles field list. */
	private List<Field> fieldListCandles;

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
	 * Return the list of average fields.
	 * 
	 * @return The list of average fields.
	 */
	public List<Field> getFieldListAverages() {
		if (fieldListAverages == null) {
			fieldListAverages = new ArrayList<>();
			for (int i = 0; i < averages.size(); i++) {
				Average average = averages.get(i);
				String name = Average.getNameAverage(average);
				String header = Average.getHeaderAverage(average);
				String label = Average.getLabelAverage(average);
				Field field = Domains.getDouble(name, header, label);
				field.setStringConverter(new NumberScaleConverter(getInstrument().getPipScale()));
				fieldListAverages.add(field);
			}
		}
		return fieldListAverages;
	}

	/**
	 * Return the list of fields for slopes. For each slope, a raw and a normalized
	 * value.
	 * 
	 * @return The list of fields for slopes.
	 */
	public List<Field> getFieldListAverageSlopes() {
		if (fieldListAverageSlopes == null) {
			fieldListAverageSlopes = new ArrayList<>();
			String name, header, label;
			Field field;
			for (int i = 0; i < averages.size(); i++) {
				Average average = averages.get(i);
				/* Raw value. */
				name = Average.getNameSlope(average, "raw");
				header = Average.getHeaderSlope(average, "raw");
				label = Average.getLabelSlope(average, "raw");
				field = Domains.getDouble(name, header, label);
				field.setStringConverter(new NumberScaleConverter(getInstrument().getPipScale()));
				fieldListAverageSlopes.add(field);
				/* Normalized value. */
				name = Average.getNameSlope(average, "nrm");
				header = Average.getHeaderSlope(average, "nrm");
				label = Average.getLabelSlope(average, "nrm");
				field = Domains.getDouble(name, header, label);
				field.setStringConverter(new NumberScaleConverter(getInstrument().getPipScale()));
				fieldListAverageSlopes.add(field);
			}
		}
		return fieldListAverageSlopes;
	}

	/**
	 * Return the list of fields for spreads. For each spread, a raw and a normalize
	 * value.
	 * 
	 * @return The list of fields for slopes.
	 */
	public List<Field> getFieldListAverageSpreads() {
		if (fieldListAverageSpreads == null) {
			fieldListAverageSpreads = new ArrayList<>();
			String name, header, label;
			Field field;
			for (int i = 0; i < averages.size(); i++) {
				Average fast = averages.get(i);
				for (int j = i + 1; j < averages.size(); j++) {
					Average slow = averages.get(j);
					/* Raw value. */
					name = Average.getNameSpread(fast, slow, "raw");
					header = Average.getHeaderSpread(fast, slow, "raw");
					label = Average.getLabelSpread(fast, slow, "raw");
					field = Domains.getDouble(name, header, label);
					field.setStringConverter(new NumberScaleConverter(getInstrument().getPipScale()));
					fieldListAverageSpreads.add(field);
					/* Normalized value. */
					name = Average.getNameSpread(fast, slow, "nrm");
					header = Average.getHeaderSpread(fast, slow, "nrm");
					label = Average.getLabelSpread(fast, slow, "nrm");
					field = Domains.getDouble(name, header, label);
					field.setStringConverter(new NumberScaleConverter(getInstrument().getPipScale()));
					fieldListAverageSpreads.add(field);
				}
			}
		}
		return fieldListAverageSpreads;
	}

	/**
	 * Returns the list of candle related fields.
	 * 
	 * @return The list of candle related fields.
	 */
	public List<Field> getFieldListCandles() {
		if (fieldListCandles == null) {

			fieldListCandles = new ArrayList<>();
			String name, header, label;
			Field field;
			for (int i = 0; i < averages.size(); i++) {

				/* Number of candles and pad for index. */
				int count, period;
				if (i == 0) {
					count = averages.get(i).getPeriod();
					period = 0;
				} else {
					count = averages.get(i).getPeriod() / averages.get(i - 1).getPeriod();
					period = averages.get(i - 1).getPeriod();
				}
				int pad = Numbers.getDigits(count);

				/* Create the candles fields for level candles. */
				for (int j = 0; j < count; j++) {
					String curr = Strings.leftPad(Integer.toString(j), pad);
					String id = period + "_" + curr;

					/* Open, high, low, close. */
					name = "open_" + id;
					header = "Open " + id;
					label = "Open " + id;
					field = Domains.getDouble(name, header, label);
					field.setStringConverter(new NumberScaleConverter(getInstrument().getPipScale()));
					fieldListCandles.add(field);
					name = "high_" + id;
					header = "High " + id;
					label = "High " + id;
					field = Domains.getDouble(name, header, label);
					field.setStringConverter(new NumberScaleConverter(getInstrument().getPipScale()));
					fieldListCandles.add(field);
					name = "low_" + id;
					header = "Low " + id;
					label = "Low " + id;
					field = Domains.getDouble(name, header, label);
					field.setStringConverter(new NumberScaleConverter(getInstrument().getPipScale()));
					fieldListCandles.add(field);
					name = "close_" + id;
					header = "Close " + id;
					label = "Close " + id;
					field = Domains.getDouble(name, header, label);
					field.setStringConverter(new NumberScaleConverter(getInstrument().getPipScale()));
					fieldListCandles.add(field);

					/* Sign: 1, 0, -1 */
					name = "sign_" + id;
					header = "Sign " + id;
					label = "Sign " + id;
					fieldListCandles.add(Domains.getDouble(name, header, label));

					/* Range, raw and normalized. */
					name = "range_" + id + "_raw";
					header = "Range " + id + " raw";
					label = "Range " + id + " raw value";
					fieldListCandles.add(Domains.getDouble(name, header, label));
					name = "range_" + id + "_nrm";
					header = "Range " + id + " nrm";
					label = "Range " + id + " normalized value";
					fieldListCandles.add(Domains.getDouble(name, header, label));

					/* Body size as a factor of the range, no need to normalize. */
					name = "body_size_" + id;
					header = "Body-size " + id;
					label = "Body size " + id;
					fieldListCandles.add(Domains.getDouble(name, header, label));

					/* Body relative position within the range. */
					name = "body_pos_" + id;
					header = "Body-pos " + id;
					label = "Body position " + id;
					fieldListCandles.add(Domains.getDouble(name, header, label));

					/*
					 * Factor of change of open, high, low and close, of this candle versus the next
					 * candle. Raw and normalized values.
					 */
					if (j < count - 1) {
						String next = Strings.leftPad(Integer.toString(j + 1), pad);
						id = period + "_" + curr + "_" + next;
						/* Raw values. */
						name = "open_" + id + "_factor_raw";
						header = "Open " + id + " factor raw";
						label = "Open " + id + " factor raw value";
						fieldListCandles.add(Domains.getDouble(name, header, label));
						name = "high_" + id + "_factor_raw";
						header = "High " + id + " factor raw";
						label = "High " + id + " factor raw value";
						fieldListCandles.add(Domains.getDouble(name, header, label));
						name = "low_" + id + "_factor_raw";
						header = "Low " + id + " factor raw";
						label = "Low " + id + " factor raw value";
						fieldListCandles.add(Domains.getDouble(name, header, label));
						name = "close_" + id + "_factor_raw";
						header = "Close " + id + " factor raw";
						label = "Close " + id + " factor raw value";
						fieldListCandles.add(Domains.getDouble(name, header, label));
						/* Normalized values. */
						name = "open_" + id + "_factor_nrm";
						header = "Open " + id + " factor nrm";
						label = "Open " + id + " factor normalized value";
						fieldListCandles.add(Domains.getDouble(name, header, label));
						name = "high_" + id + "_factor_nrm";
						header = "High " + id + " factor nrm";
						label = "High " + id + " factor normalized value";
						fieldListCandles.add(Domains.getDouble(name, header, label));
						name = "low_" + id + "_factor_nrm";
						header = "Low " + id + " factor nrm";
						label = "Low " + id + " factor normalized value";
						fieldListCandles.add(Domains.getDouble(name, header, label));
						name = "close_" + id + "_factor_nrm";
						header = "Close " + id + " factor nrm";
						label = "Close " + id + " factor normalized value";
						fieldListCandles.add(Domains.getDouble(name, header, label));
					}
				}
			}
		}
		return fieldListCandles;
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
	 * {@inheritDoc}
	 */
	@Override
	public String getLegend() {
		HTML html = new HTML();
		return html.toString();
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
		options.add(optionCalculate);

		/* Browse statictics. */
		Option optionBrowseStats = new Option();
		optionBrowseStats.setKey("BROWSE-STATS");
		optionBrowseStats.setText("Browse statistics");
		optionBrowseStats.setToolTip("Browse statistics values");
		optionBrowseStats.setAction(new ActionBrowseStats());
		options.add(optionBrowseStats);

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

		/* Period. */
		Field fieldPeriod = new FieldPeriod(Fields.RANGE_PERIOD);
		tableRanges.addField(fieldPeriod);

		/* Value. */
		Field fieldValue = Domains.getDouble(Fields.RANGE_VALUE, "Value", "Value");
		tableRanges.addField(fieldValue);

		/* Reference of the time of the registered values. */
		Field fieldTime = new FieldTime(Fields.BAR_TIME);
		tableRanges.addField(fieldTime);

		/* Non unique index on name, min-max and period. */
		Index index = new Index();
		index.add(tableRanges.getField(Fields.RANGE_NAME));
		index.add(tableRanges.getField(Fields.RANGE_MIN_MAX));
		index.add(tableRanges.getField(Fields.RANGE_PERIOD));
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

		/* Calculated pivot (High=1, None=0, Low=-1) */
		tableStates.addField(Domains.getInteger("pivot", "Pivot", "Pivot"));

		/* Label (Long=1, Out=0, Short=-1) */
		tableStates.addField(Domains.getInteger("label", "Label", "Label"));

		/* Average fields. */
		List<Field> averageFields = getFieldListAverages();
		for (Field field : averageFields) {
			tableStates.addField(field);
		}

		/* Slopes of averages: raw and normalized. */
		List<Field> slopeFields = getFieldListAverageSlopes();
		for (Field field : slopeFields) {
			tableStates.addField(field);
		}

		/* Spreads within averages: raw and normalized. */
		List<Field> spreadFields = getFieldListAverageSpreads();
		for (Field field : spreadFields) {
			tableStates.addField(field);
		}

		/* Candle fields. */
		List<Field> candleFields = getFieldListCandles();
		for (Field field : candleFields) {
			tableStates.addField(field);
		}

		tableStates.getField(Fields.BAR_TIME).setPrimaryKey(true);
		View view = tableStates.getComplexView(tableStates.getPrimaryKey());
		tableStates.setPersistor(new DBPersistor(MLT.getDBEngine(), view));

		return tableStates;
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
