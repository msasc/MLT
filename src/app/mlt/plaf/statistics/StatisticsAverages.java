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
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.mlt.db.Field;
import com.mlt.db.Index;
import com.mlt.db.Record;
import com.mlt.db.Table;
import com.mlt.db.View;
import com.mlt.db.rdbms.DBPersistor;
import com.mlt.desktop.Option;
import com.mlt.mkt.data.Instrument;
import com.mlt.mkt.data.Period;
import com.mlt.util.HTML;
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
public class StatisticsAverages extends StatisticsTicker {

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
	 * Return the list of averages given the parameters string.
	 * 
	 * @param parameters The parameters string.
	 * @return The list of averages.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static List<Average> getAverages(
		String parameters) throws ParserConfigurationException, SAXException, IOException {
		ParametersHandler handler = new ParametersHandler();
		Parser parser = new Parser();
		parser.parse(new ByteArrayInputStream(parameters.getBytes()), handler);
		return handler.getAverages();
	}

	/**
	 * Return the parameters description part of averages.
	 * 
	 * @param averages The list of averages.
	 * @return The description.
	 */
	public static String getParametersDescription(List<Average> averages) {
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
	 * Return statistics averages from a statistics record.
	 * 
	 * @param rc The statistics definition record.
	 * @return The statistics object.
	 */
	public static StatisticsAverages getStatistics(Record rc) {
		try {

			/* Instrument and period. */
			Instrument instrument = DB.to_instrument(rc.getValue(Fields.INSTRUMENT_ID).getString());
			Period period = DB.to_period(rc.getValue(Fields.PERIOD_ID).getString());

			/* Statistics averages. */
			StatisticsAverages stats = new StatisticsAverages(instrument, period);
			stats.setId(rc.getValue(Fields.STATISTICS_ID).getString());
			stats.setKey(rc.getValue(Fields.STATISTICS_KEY).getString());
			stats.setParameters(rc.getValue(Fields.STATISTICS_PARAMS).getString());

			return stats;

		} catch (Exception exc) {

		}
		return null;
	}

	/**
	 * Validates that the list of averages is correct for the purpose of the
	 * StatisticsAverages.
	 * 
	 * @param averages The list of averages.
	 * @throws IllegalArgumentException
	 */
	public static void validate(List<Average> averages) throws IllegalArgumentException {
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

	/** List of averages. */
	private List<Average> averages = new ArrayList<>();
	/** States table. */
	private Table tableStates;
	/** Ranges table. */
	private Table tableRanges;

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
	 * Return the list of average fields.
	 * 
	 * @return The list of average fields.
	 */
	public List<Field> getFieldListAverages() {
		List<Field> fields = new ArrayList<>();
		for (int i = 0; i < averages.size(); i++) {
			Average average = averages.get(i);
			String name = "average_" + average.getPeriod();
			String header = "Avg " + average.getPeriod();
			String label = "Average " + average.toString();
			fields.add(Domains.getDouble(name, header, label));
		}
		return fields;
	}

	/**
	 * Return the list of fields for slopes. For each slope, a raw and a normalized
	 * value.
	 * 
	 * @return The list of fields for slopes.
	 */
	public List<Field> getFieldListAverageSlopes() {
		List<Field> fields = new ArrayList<>();
		String name, header, label;
		for (int i = 0; i < averages.size(); i++) {
			int p = averages.get(i).getPeriod();
			/* Raw value. */
			name = "average_slope_" + p + "_raw";
			header = "Avg-Slope " + p + " raw";
			label = "Average slope " + p + " raw value";
			fields.add(Domains.getDouble(name, header, label));
			/* Normalized value. */
			name = "average_slope_" + p + "_nrm";
			header = "Avg-Slope " + p + " nrm";
			label = "Average slope " + p + " normalized value";
			fields.add(Domains.getDouble(name, header, label));
		}
		return fields;
	}

	/**
	 * Return the list of fields for spreads. For each spread, a raw and a normalize
	 * value.
	 * 
	 * @return The list of fields for slopes.
	 */
	public List<Field> getFieldListAverageSpreads() {
		List<Field> fields = new ArrayList<>();
		String name, header, label;
		for (int i = 0; i < averages.size(); i++) {
			int fp = averages.get(i).getPeriod();
			for (int j = i + 1; j < averages.size(); j++) {
				int sp = averages.get(j).getPeriod();
				/* Raw value. */
				name = "average_spread_" + fp + "_" + sp + "_raw";
				header = "Avg-Spread " + fp + "/" + sp + " raw";
				label = "Average spread between " + fp + " and " + sp + " raw value";
				fields.add(Domains.getDouble(name, header, label));
				/* Normalized value. */
				name = "average_spread_" + fp + "_" + sp + "_nrm";
				header = "Avg-Spread " + fp + "/" + sp + " nrm";
				label = "Average spread between " + fp + " and " + sp + " normalized value";
				fields.add(Domains.getDouble(name, header, label));
			}
		}
		return fields;
	}

	/**
	 * Returns the list of candle related fields.
	 * 
	 * @return The list of candle related fields.
	 */
	public List<Field> getFieldListCandles() {

		List<Field> fields = new ArrayList<>();
		String name, header, label;
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
				fields.add(Domains.getDouble(name, header, label));
				name = "high_" + id;
				header = "High " + id;
				label = "High " + id;
				fields.add(Domains.getDouble(name, header, label));
				name = "low_" + id;
				header = "Low " + id;
				label = "Low " + id;
				fields.add(Domains.getDouble(name, header, label));
				name = "close_" + id;
				header = "Close " + id;
				label = "Close " + id;
				fields.add(Domains.getDouble(name, header, label));

				/* Sign: 1, 0, -1 */
				name = "sign_" + id;
				header = "Sign " + id;
				label = "Sign " + id;
				fields.add(Domains.getDouble(name, header, label));

				/* Range, raw and normalized. */
				name = "range_" + id + "_raw";
				header = "Range " + id + " raw";
				label = "Range " + id + " raw value";
				fields.add(Domains.getDouble(name, header, label));
				name = "range_" + id + "_nrm";
				header = "Range " + id + " nrm";
				label = "Range " + id + " normalized value";
				fields.add(Domains.getDouble(name, header, label));

				/* Body size as a factor of the range, no need to normalize. */
				name = "body_size_" + id;
				header = "Body-size " + id;
				label = "Body size " + id;
				fields.add(Domains.getDouble(name, header, label));

				/* Body relative position within the range. */
				name = "body_pos_" + id;
				header = "Body-pos " + id;
				label = "Body position " + id;
				fields.add(Domains.getDouble(name, header, label));

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
					fields.add(Domains.getDouble(name, header, label));
					name = "high_" + id + "_factor_raw";
					header = "High " + id + " factor raw";
					label = "High " + id + " factor raw value";
					fields.add(Domains.getDouble(name, header, label));
					name = "low_" + id + "_factor_raw";
					header = "Low " + id + " factor raw";
					label = "Low " + id + " factor raw value";
					fields.add(Domains.getDouble(name, header, label));
					name = "close_" + id + "_factor_raw";
					header = "Close " + id + " factor raw";
					label = "Close " + id + " factor raw value";
					fields.add(Domains.getDouble(name, header, label));
					/* Normalized values. */
					name = "open_" + id + "_factor_nrm";
					header = "Open " + id + " factor nrm";
					label = "Open " + id + " factor normalized value";
					fields.add(Domains.getDouble(name, header, label));
					name = "high_" + id + "_factor_nrm";
					header = "High " + id + " factor nrm";
					label = "High " + id + " factor normalized value";
					fields.add(Domains.getDouble(name, header, label));
					name = "low_" + id + "_factor_nrm";
					header = "Low " + id + " factor nrm";
					label = "Low " + id + " factor normalized value";
					fields.add(Domains.getDouble(name, header, label));
					name = "close_" + id + "_factor_nrm";
					header = "Close " + id + " factor nrm";
					label = "Close " + id + " factor normalized value";
					fields.add(Domains.getDouble(name, header, label));
				}
			}
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
	 * {@inheritDoc}
	 */
	@Override
	public String getLegend() {
		HTML html = new HTML();
		return html.toString(true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Option> getOptions() {
		// TODO Auto-generated method stub
		return null;
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
		return getParametersDescription(averages);
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
	private Table getTableRanges() {
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
	private Table getTableStates() {
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
		tableStates.getField(Fields.BAR_TIME).setPrimaryKey(true);

		tableStates.addField(new FieldTimeFmt(Fields.BAR_TIME_FMT, period));
		tableStates.addField(new FieldDataInst(instrument, Fields.BAR_OPEN, "Open", "Open value"));
		tableStates.addField(new FieldDataInst(instrument, Fields.BAR_HIGH, "High", "High value"));
		tableStates.addField(new FieldDataInst(instrument, Fields.BAR_LOW, "Low", "Low value"));
		tableStates
			.addField(new FieldDataInst(instrument, Fields.BAR_CLOSE, "Close", "Close value"));

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

		View view = tableStates.getComplexView(tableStates.getPrimaryKey());
		tableStates.setPersistor(new DBPersistor(MLT.getDBEngine(), view));

		return tableStates;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setParameters(String parameters) {
		try {
			averages.clear();
			averages.addAll(getAverages(parameters));
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
	}
}
