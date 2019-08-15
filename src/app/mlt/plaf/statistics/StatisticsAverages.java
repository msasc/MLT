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
import com.mlt.db.Table;
import com.mlt.db.View;
import com.mlt.db.rdbms.DBEngine;
import com.mlt.db.rdbms.DBPersistor;
import com.mlt.desktop.Option;
import com.mlt.mkt.data.Instrument;
import com.mlt.mkt.data.Period;
import com.mlt.mkt.server.Server;
import com.mlt.util.Numbers;
import com.mlt.util.Strings;
import com.mlt.util.xml.Parser;
import com.mlt.util.xml.ParserHandler;
import com.mlt.util.xml.XMLAttribute;
import com.mlt.util.xml.XMLWriter;

import app.mlt.plaf.db.Database;
import app.mlt.plaf.db.Domains;
import app.mlt.plaf.db.Fields;
import app.mlt.plaf.db.fields.FieldDataInst;
import app.mlt.plaf.db.fields.FieldPeriod;
import app.mlt.plaf.db.fields.FieldTime;
import app.mlt.plaf.db.fields.FieldTimeFmt;

/**
 * Statistics built on a list of averages.
 *
 * @author Miquel Sas
 */
public class StatisticsAverages extends StatisticsTicker {

	/**
	 * Parameters handler.
	 */
	class ParametersHandler extends ParserHandler {
		/**
		 * Called to notify an element start.
		 */
		@Override
		public void elementStart(String namespace, String elementName, String path, Attributes attributes) throws SAXException {
			if (path.equals("averages/average")) {

				/* Type. */
				String attrType = attributes.getValue("type");
				Average.Type type = Average.Type.valueOf(attrType);

				/* Period. */
				String attrPeriod = attributes.getValue("period");
				int period = Integer.parseInt(attrPeriod);

				/* Smooths. */
				String attrSmooths = attributes.getValue("smooths");
				int[] smooths = null;
				if (!attrSmooths.isEmpty()) {
					String[] s_smooths = Strings.parse(attrSmooths, ",");
					smooths = new int[s_smooths.length];
					for (int i = 0; i < s_smooths.length; i++) {
						smooths[i] = Integer.parseInt(s_smooths[i]);
					}
				}

				/* Add the average. */
				averages.add(new Average(type, period, smooths));
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
	 * @param dbEngine   The database engine.
	 * @param server     Server.
	 * @param instrument Instrument.
	 * @param period     Period.
	 * @param idSuffix   Id suffix.
	 */
	public StatisticsAverages(
		DBEngine dbEngine,
		Server server,
		Instrument instrument,
		Period period,
		String idSuffix) {
		super(dbEngine, server, instrument, period);
		setId("st" + idSuffix);
	}

	/**
	 * Add an average to the list of averages. Averages must be added in inverse
	 * order, from greater to smallest.
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
		/* Period must be less than first. */
		if (avg.getPeriod() > averages.get(0).getPeriod()) {
			throw new IllegalArgumentException("Period must be less than first one.");
		}
		/* Period must be a divisor of last one. */
		if (averages.get(averages.size() - 1).getPeriod() % avg.getPeriod() != 0) {
			throw new IllegalArgumentException("Period must be a divisor of last one.");
		}
		/* Set as the first. */
		averages.add(0, avg);
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
			String name = Fields.averageName(average);
			String header = Fields.averageHeader(average);
			String label = Fields.averageLabel(average);
			Field field = Domains.getDouble(name, header, label);
			fields.add(field);
		}
		return fields;
	}

	/**
	 * Return the list of fields of candles per average.
	 * 
	 * @param suffix The suffix.
	 * @return The list of fields.
	 */
	public List<Field> getFieldListCandles(String suffix) {
		List<Field> fields = new ArrayList<>();
		int maxPeriod = averages.get(averages.size() - 1).getPeriod();

		/* Calculate maximum integer digits to pad the number. */
		int pad = -1;
		for (int i = 0; i < averages.size() - 1; i++) {
			Average average = averages.get(i);
			int period = average.getPeriod();
			int candles = maxPeriod / period;
			pad = Math.max(pad, Numbers.getDigits(candles));
		}

		/* Build fields. */
		for (int i = 0; i < averages.size() - 1; i++) {
			Average average = averages.get(i);
			int period = average.getPeriod();
			int candles = maxPeriod / period;
			for (int j = 0; j < candles; j++) {

				/* Range. */
				String rangeName = Fields.fieldName("range", period, j, pad, suffix);
				String rangeHeader = Fields.fieldHeader("Range", period, j, pad, suffix);
				String rangeLabel = Fields.fieldLabel("Range", period, j, pad, suffix);
				Field rangeField = Domains.getDouble(rangeName, rangeHeader, rangeLabel);
				fields.add(rangeField);

				/* Body size. */
				String bodySizeName = Fields.fieldName("body_size", period, j, pad, suffix);
				String bodySizeHeader = Fields.fieldHeader("Body-Size", period, j, pad, suffix);
				String bodySizeLabel = Fields.fieldLabel("Body-Size", period, j, pad, suffix);
				Field bodySizeField =
					Domains.getDouble(bodySizeName, bodySizeHeader, bodySizeLabel);
				fields.add(bodySizeField);

				/* Body position (center of body relative to range). */
				String bodyPosName = Fields.fieldName("body_pos", period, j, pad, suffix);
				String bodyPosHeader = Fields.fieldHeader("body_pos", period, j, pad, suffix);
				String bodyPosLabel = Fields.fieldLabel("body_pos", period, j, pad, suffix);
				Field bodyPosField = Domains.getDouble(bodyPosName, bodyPosHeader, bodyPosLabel);
				fields.add(bodyPosField);

				/* Relative position between this and next candle. */
				if (j < candles - 1) {
					String relPosName = Fields.fieldNameRel("rel_pos", period, j, pad, suffix);
					String relPosHeader = Fields.fieldHeaderRel("rel_pos", period, j, pad, suffix);
					String relPosLabel = Fields.fieldLabelRel("rel_pos", period, j, pad, suffix);
					Field relPosField = Domains.getDouble(relPosName, relPosHeader, relPosLabel);
					fields.add(relPosField);
				}
			}
		}
		return fields;
	}
	
	/**
	 * Return the field list with increases in percentage of the last N close values
	 * @param suffix
	 * @return
	 */
	public List<Field> getFieldListIncreases(String suffix) {
		List<Field> fields = new ArrayList<>();
		return fields;
	}

	/**
	 * Return the list of slope fields.
	 * 
	 * @return The list of slope fields.
	 */
	public List<Field> getFieldListSlopes(String suffix) {
		List<Field> fields = new ArrayList<>();
		for (int i = 0; i < averages.size(); i++) {
			Average average = averages.get(i);
			String name = Fields.slopeName(average, suffix);
			String header = Fields.slopeHeader(average, suffix);
			String label = Fields.slopeLabel(average, suffix);
			Field field = Domains.getDouble(name, header, label);
			fields.add(field);
		}
		return fields;
	}

	/**
	 * Return the list of spread fields.
	 * 
	 * @return The list of spread fields.
	 */
	public List<Field> getFieldListSpreads(String suffix) {
		List<Field> fields = new ArrayList<>();
		for (int i = 0; i < averages.size() - 1; i++) {
			Average fast = averages.get(i);
			for (int j = i + 1; j < averages.size(); j++) {
				Average slow = averages.get(j);
				String name = Fields.spreadName(fast, slow, suffix);
				String header = Fields.spreadHeader(fast, slow, suffix);
				String label = Fields.spreadLabel(fast, slow, suffix);
				Field field = Domains.getDouble(name, header, label);
				fields.add(field);
			}
		}
		return fields;
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
	public List<Table> getTables() {
		List<Table> tables = new ArrayList<>();
		tables.add(getTableRanges());
		tables.add(getTableStates());
		return tables;
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

		Server server = getServer();
		Instrument instrument = getInstrument();
		Period period = getPeriod();
		String name = Database.getName_Ticker(instrument, period, "_" + getId() + "_rng");

		tableRanges.setSchema(Database.getSchema(server));
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
		Field fieldTime = new FieldTime(Fields.TIME);
		tableRanges.addField(fieldTime);

		/* Non unique index on name, min-max and period. */
		Index index = new Index();
		index.add(tableRanges.getField(Fields.RANGE_NAME));
		index.add(tableRanges.getField(Fields.RANGE_MIN_MAX));
		index.add(tableRanges.getField(Fields.RANGE_PERIOD));
		index.setUnique(false);
		tableRanges.addIndex(index);

		DBEngine dbEngine = getDBEngine();
		View view = tableRanges.getComplexView(index);
		tableRanges.setPersistor(new DBPersistor(dbEngine, view));

		return tableRanges;
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

		Server server = getServer();
		Instrument instrument = getInstrument();
		Period period = getPeriod();
		String name = Database.getName_Ticker(instrument, period, "_" + getId() + "_src");

		tableStates.setSchema(Database.getSchema(server));
		tableStates.setName(name);

		/* Time, open, high, low, close. */
		tableStates.addField(new FieldTime(Fields.TIME));
		tableStates.getField(Fields.TIME).setPrimaryKey(true);

		tableStates.addField(new FieldTimeFmt(Fields.TIME_FMT, period));
		tableStates.addField(new FieldDataInst(instrument, Fields.OPEN, "Open", "Open value"));
		tableStates.addField(new FieldDataInst(instrument, Fields.HIGH, "High", "High value"));
		tableStates.addField(new FieldDataInst(instrument, Fields.LOW, "Low", "Low value"));
		tableStates.addField(new FieldDataInst(instrument, Fields.CLOSE, "Close", "Close value"));

		/* Calculated pivot (High=1, None=0, Low=-1) */
		tableStates.addField(Domains.getInteger("pivot", "Pivot", "Pivot"));

		/* Label (Long=1, Out=0, Short=-1) */
		tableStates.addField(Domains.getInteger("label", "Label", "Label"));

		/* Average fields. */
		List<Field> averageFields = getFieldListAverages();
		for (Field field : averageFields) {
			tableStates.addField(field);
		}

		/* Slopes of averages: raw. */
		List<Field> slopeFieldsRaw = getFieldListSlopes("raw");
		for (Field field : slopeFieldsRaw) {
			tableStates.addField(field);
		}

		/* Spread fields: raw. */
		List<Field> spreadFieldsRaw = getFieldListSpreads("raw");
		for (Field field : spreadFieldsRaw) {
			tableStates.addField(field);
		}

		/* Candle fields: raw. */
		List<Field> candleFieldsRaw = getFieldListCandles("raw");
		for (Field field : candleFieldsRaw) {
			tableStates.addField(field);
		}

		/* Slopes of averages: normalized. */
		List<Field> slopeFieldsNrm = getFieldListSlopes("nrm");
		for (Field field : slopeFieldsNrm) {
			tableStates.addField(field);
		}

		/* Spread fields: normalized. */
		List<Field> spreadFieldsNrm = getFieldListSpreads("nrm");
		for (Field field : spreadFieldsNrm) {
			tableStates.addField(field);
		}

		/* Candle fields: normalized. */
		List<Field> candleFieldsNrm = getFieldListCandles("nrm");
		for (Field field : candleFieldsNrm) {
			tableStates.addField(field);
		}

		DBEngine dbEngine = getDBEngine();
		View view = tableStates.getComplexView(tableStates.getPrimaryKey());
		tableStates.setPersistor(new DBPersistor(dbEngine, view));

		return tableStates;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setParameters(String parameters) {
		ParametersHandler handler = new ParametersHandler();
		Parser parser = new Parser();
		averages.clear();
		try {
			parser.parse(new ByteArrayInputStream(parameters.getBytes()), handler);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
	}
}
