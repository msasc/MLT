package app.mlt.plaf.statistics;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mlt.db.Field;
import com.mlt.db.FieldGroup;
import com.mlt.db.Record;
import com.mlt.db.Table;
import com.mlt.db.View;
import com.mlt.db.rdbms.DBPersistor;
import com.mlt.desktop.converters.NumberScaleConverter;
import com.mlt.mkt.data.Instrument;
import com.mlt.mkt.data.Period;
import com.mlt.util.Numbers;
import com.mlt.util.Properties;
import com.mlt.util.Strings;
import com.mlt.util.xml.parser.Parser;

import app.mlt.plaf.DB;
import app.mlt.plaf.MLT;

/**
 * Statistics built on a list of averages.
 *
 * @author Miquel Sas
 */
public class Statistics {
	
	/**
	 * Return statistics averages from a statistics record.
	 * 
	 * @param rc The statistics definition record.
	 * @return The statistics object.
	 */
	public static Statistics get(Record rc) throws Exception {

		/* Instrument and period. */
		Instrument instrument = DB.to_instrument(rc.getValue(DB.FIELD_INSTRUMENT_ID).getString());
		Period period = DB.to_period(rc.getValue(DB.FIELD_PERIOD_ID).getString());

		/* Statistics averages. */
		Statistics stats = new Statistics(instrument, period);
		stats.id = rc.getValue(DB.FIELD_STATISTICS_ID).getString();
		stats.setParameters(rc.getValue(DB.FIELD_STATISTICS_PARAMS).getString());

		return stats;

	}


	/** Identifier that indicates the class of statistics. */
	private String id;

	/** Instrument. */
	private Instrument instrument;
	/** Period. */
	private Period period;

	/** Parameters. */
	private Parameters parameters;

	/** Internal properties to cache data. */
	private Properties properties;

	/**
	 * @param instrument The instrument.
	 * @param period     The period.
	 */
	public Statistics(Instrument instrument, Period period) {
		super();
		this.instrument = instrument;
		this.period = period;
		this.parameters = new Parameters();
		this.properties = new Properties();
	}

	/**
	 * @param index Index of average.
	 * @return Number of candles.
	 */
	public int getCandleCount(int index) {
		List<Average> averages = getParameters().getAverages();
		int fast = (index == 0 ? 1 : averages.get(index - 1).getPeriod());
		int slow = averages.get(index).getPeriod();
		int mult = averages.size() - index;
		int count = (slow / fast) * mult;
		return count;

	}

	/**
	 * @param index Index of average.
	 * @return Size of candles or number of bars per candle.
	 */
	public int getCandleSize(int index) {
		List<Average> averages = getParameters().getAverages();
		int size = (index == 0 ? 1 : averages.get(index - 1).getPeriod());
		return size;
	}

	/**
	 * @param key The field group key.
	 * @return The field group with the given key.
	 */
	private FieldGroup getFieldGroup(String key) {
		FieldGroup fieldGroup = (FieldGroup) properties.getObject(key);
		return fieldGroup;
	}

	/**
	 * @return The list of average fields.
	 */
	public List<Field> getFieldListAvg() {
		List<Field> fields = new ArrayList<>();
		List<Average> averages = getParameters().getAverages();
		for (int i = 0; i < averages.size(); i++) {
			Average avg = averages.get(i);
			String name = getNameAvg(avg);
			String header = DB.header(name);
			Field field = DB.field_double(name, header);
			field.setStringConverter(new NumberScaleConverter(8));
			field.setFieldGroup(getFieldGroup(DB.GROUP_AVG));
			Calculator calculator = new Calculator.Avg(name, avg);
			field.getProperties().setObject("CALCULATOR", calculator);
			fields.add(field);
		}
		return fields;
	}

	/**
	 * @return The list of average delta fields.
	 */
	public List<Field> getFieldListAvgDeltas() {
		List<Field> fields = new ArrayList<>();
		List<Average> averages = getParameters().getAverages();
		for (int i = 0; i < averages.size(); i++) {
			Average avg = averages.get(i);
			String name = getNameAvgDelta(avg);
			String header = DB.header(name);
			Field field = DB.field_double(name, header);
			field.setStringConverter(new NumberScaleConverter(8));
			field.setFieldGroup(getFieldGroup(DB.GROUP_AVG_DELTAS));
			Calculator calculator = new Calculator.AvgDelta(name, avg);
			field.getProperties().setObject("CALCULATOR", calculator);
			fields.add(field);
		}
		return fields;
	}

	/**
	 * @return The list of slope fields.
	 */
	public List<Field> getFieldListAvgSlopes() {
		List<Field> fields = new ArrayList<>();
		List<Average> averages = getParameters().getAverages();
		for (int i = 0; i < averages.size(); i++) {
			Average avg = averages.get(i);
			String name = getNameAvgSlope(avg);
			String header = DB.header(name);
			Field field = DB.field_double(name, header);
			field.setStringConverter(new NumberScaleConverter(8));
			field.setFieldGroup(getFieldGroup(DB.GROUP_AVG_SLOPES));
			String nameAvg = getNameAvg(avg);
			Calculator calculator = new Calculator.AvgSlope(nameAvg, name);
			field.getProperties().setObject("CALCULATOR", calculator);
			fields.add(field);
		}
		return fields;
	}

	/**
	 * @return The list of spread fields.
	 */
	public List<Field> getFieldListAvgSpreads() {
		List<Field> fields = new ArrayList<>();
		List<Average> averages = getParameters().getAverages();
		String name, header;
		Field field;
		for (int i = 0; i < averages.size(); i++) {
			Average fast = averages.get(i);
			for (int j = i + 1; j < averages.size(); j++) {
				Average slow = averages.get(j);
				name = getNameAvgSpread(fast, slow);
				header = DB.header(name);
				field = DB.field_double(name, header);
				field.setStringConverter(new NumberScaleConverter(8));
				field.setFieldGroup(getFieldGroup(DB.GROUP_AVG_SPREADS));
				fields.add(field);
			}
		}
		return fields;
	}

	/**
	 * @return The list with all candle fields.
	 */
	public List<Field> getFieldListCandles() {
		List<Field> fields = new ArrayList<>();
		List<Average> averages = getParameters().getAverages();
		for (int i = 0; i < averages.size(); i++) {
			fields.addAll(getFieldListCandles(i));
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
				if (candleName.equals(DB.FIELD_CANDLE_REL_RANGE) && index == count - 1) {
					continue;
				}
				if (candleName.equals(DB.FIELD_CANDLE_REL_BODY) && index == count - 1) {
					continue;
				}
				String name = getNameCandle(size, index, candleName);
				String header = DB.header(name);
				Field field = DB.field_double(name, header);
				field.setStringConverter(new NumberScaleConverter(8));
				field.setFieldGroup(getFieldGroup(DB.GROUP_CANDLES.apply(size)));
				fields.add(field);
			}
		}
		return fields;
	}

	/**
	 * @return The list of pattern fields (raw and normalized)
	 */
	public List<Field> getFieldListPatterns(boolean candles) {
		List<Field> fields = new ArrayList<>();
		fields.addAll(getFieldListAvgDeltas());
		fields.addAll(getFieldListAvgSlopes());
		fields.addAll(getFieldListAvgSpreads());
		fields.addAll(getFieldListVar());
		fields.addAll(getFieldListVarSlopes());
		fields.addAll(getFieldListVarSpreads());
		if (candles) fields.addAll(getFieldListCandles());
		return fields;
	}

	/**
	 * @return The list of variance fields.
	 */
	public List<Field> getFieldListVar() {
		List<Field> fields = new ArrayList<>();
		List<Average> averages = getParameters().getAverages();
		String name, header;
		Field field;
		for (int i = 0; i < averages.size() - 1; i++) {
			Average fast = averages.get(i);
			for (int j = i + 1; j < averages.size(); j++) {
				Average slow = averages.get(j);
				for (int k = j; k < averages.size(); k++) {
					int period = averages.get(k).getPeriod();
					name = getNameVar(fast, slow, period);
					header = DB.header(name);
					field = DB.field_double(name, header);
					field.setStringConverter(new NumberScaleConverter(8));
					field.setFieldGroup(getFieldGroup(DB.GROUP_VAR));
					fields.add(field);
				}
			}
		}
		return fields;
	}

	/**
	 * @return The list of variance slope fields.
	 */
	public List<Field> getFieldListVarSlopes() {
		List<Field> fields = new ArrayList<>();
		List<Average> averages = getParameters().getAverages();
		String name, header;
		Field field;
		for (int i = 0; i < averages.size() - 1; i++) {
			Average fast = averages.get(i);
			for (int j = i + 1; j < averages.size(); j++) {
				Average slow = averages.get(j);
				for (int k = j; k < averages.size(); k++) {
					int period = averages.get(k).getPeriod();
					name = getNameVarSlope(fast, slow, period);
					header = DB.header(name);
					field = DB.field_double(name, header);
					field.setStringConverter(new NumberScaleConverter(8));
					field.setFieldGroup(getFieldGroup(DB.GROUP_VAR_SLOPES));
					fields.add(field);
				}
			}
		}
		return fields;
	}

	/**
	 * @return The list of variance slope fields.
	 */
	public List<Field> getFieldListVarSpreads() {
		List<Field> varFields = getFieldListVar();
		List<Field> fields = new ArrayList<>();
		String name, header;
		Field field;
		for (int i = 0; i < varFields.size() - 1; i++) {
			String nameFast = varFields.get(i).getName();
			String nameSlow = varFields.get(i + 1).getName();
			name = getNameVarSpread(nameFast, nameSlow);
			header = DB.header(name);
			field = DB.field_double(name, header);
			field.setStringConverter(new NumberScaleConverter(8));
			field.setFieldGroup(getFieldGroup(DB.GROUP_VAR_SPREADS));
			fields.add(field);
		}
		return fields;
	}

	/**
	 * @return The id.
	 */
	public String getId() {
		return id;
	}

	/**
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
	private String getNameAvg(Average avg) {
		return "avg_" + getPadded(avg.getPeriod());
	}

	/**
	 * @param avg The average.
	 * @return The field name.
	 */
	private String getNameAvgDelta(Average avg) {
		return "avg_delta_" + getPadded(avg.getPeriod());
	}

	/**
	 * @param avg The average.
	 * @return The field name.
	 */
	private String getNameAvgSlope(Average avg) {
		return "avg_slope_" + getPadded(avg.getPeriod());
	}

	/**
	 * @param fast Fast average.
	 * @param slow Slow average.
	 * @return the field name.
	 */
	private String getNameAvgSpread(Average fast, Average slow) {
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
	private String getNameCandle(int size, int index, String name) {
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
	 * @param prefix The prefix.
	 * @param suffix Last suffix.
	 * @return The table name suffix.
	 */
	private String getNameSuffix(String prefix, String suffix) {
		StringBuilder b = new StringBuilder();
		b.append(prefix);
		if (suffix != null) {
			b.append("_");
			b.append(suffix);
		}
		return b.toString().toLowerCase();
	}

	/**
	 * @param fast   Fast average.
	 * @param slow   Slow average.
	 * @param period Back period.
	 * @return the field name.
	 */
	private String getNameVar(Average fast, Average slow, int period) {
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
	private String getNameVarSlope(Average fast, Average slow, int period) {
		return getNameVar("var_slope", fast, slow, period);
	}

	/**
	 * @param nameFast Fast variance name.
	 * @param nameSlow Slow variance name.
	 * @return Spread name.
	 */
	private String getNameVarSpread(String nameFast, String nameSlow) {
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
	 * @param number  The number, size or period.
	 * @param padding Padding size.
	 * @return The padded string.
	 */
	public String getPadded(int number) {
		return Strings.leftPad(Integer.toString(number), getPadding(), "0");
	}

	/**
	 * @return The padding size.
	 */
	public int getPadding() {
		List<Average> averages = getParameters().getAverages();
		return Numbers.getDigits(averages.get(averages.size() - 1).getPeriod());
	}

	/**
	 * @return The parameters.
	 */
	public Parameters getParameters() {
		return parameters;
	}

	/**
	 * @return The period.
	 */
	public Period getPeriod() {
		return period;
	}

	/**
	 * @return The table with the pattern fields and its activation..
	 */
	public Table getTablePatterns() {

		Table table = (Table) properties.getObject("table_patterns");
		if (table != null) {
			return table;
		}

		table = new Table();

		Instrument instrument = getInstrument();
		Period period = getPeriod();
		String name = DB.name_ticker(instrument, period, getNameSuffix(getId(), "ptn"));

		table.setSchema(DB.schema_server());
		table.setName(name);
		
		table.addField(DB.field_integer(DB.FIELD_DELTA, "Delta"));
		table.addField(DB.field_string(DB.FIELD_PATTERN_NAME, 60, "Name"));
		table.addField(DB.field_boolean(DB.FIELD_PATTERN_ACTIVE, "Active"));
		
		/* Primary key. */
		table.getField(DB.FIELD_DELTA).setPrimaryKey(true);
		table.getField(DB.FIELD_PATTERN_NAME).setPrimaryKey(true);

		View view = table.getComplexView(table.getPrimaryKey());
		table.setPersistor(new DBPersistor(MLT.getDBEngine(), view));

		properties.setObject("table_patterns", table);
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
		String name = DB.name_ticker(instrument, period, getNameSuffix(getId(), "rng"));

		table.setSchema(DB.schema_server());
		table.setName(name);

		table.addField(DB.field_string(DB.FIELD_RANGE_NAME, 60, "Name"));
		table.addField(DB.field_integer(DB.FIELD_DELTA, "Delta"));
		
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
		table.getField(DB.FIELD_DELTA).setPrimaryKey(true);

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
		String name = DB.name_ticker(instrument, period, getNameSuffix(getId(), "raw"));

		table.setSchema(DB.schema_server());
		table.setName(name);

		table.addField(DB.field_long(DB.FIELD_BAR_TIME, "Time"));
		table.addField(DB.field_integer(DB.FIELD_DELTA, "Delta"));
		
		table.addField(DB.field_timeFmt(period, DB.FIELD_BAR_TIME, "Time fmt"));

		List<Field> fields = new ArrayList<>();
		fields.addAll(getFieldListAvgDeltas());
		fields.addAll(getFieldListAvgSlopes());
		fields.addAll(getFieldListAvgSpreads());
		fields.addAll(getFieldListVar());
		fields.addAll(getFieldListVarSlopes());
		fields.addAll(getFieldListVarSpreads());
		fields.addAll(getFieldListCandles());
		for (Field field : fields) {
			table.addField(field);
		}

		table.getField(DB.FIELD_BAR_TIME).setPrimaryKey(true);
		table.getField(DB.FIELD_DELTA).setPrimaryKey(true);
		
		View view = table.getComplexView(table.getPrimaryKey());
		table.setPersistor(new DBPersistor(MLT.getDBEngine(), view));
		properties.setObject("table_raw", table);
		return table;
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
		String name = DB.name_ticker(instrument, period, getNameSuffix(getId(), "src"));

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

		List<Field> fields = getFieldListAvg();
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
	 * {@inheritDoc}
	 */
	public void setParameters(String strParams) throws Exception {
		
		/* Parse parameters. */
		Parser parser = new Parser();
		parser.parse(new ByteArrayInputStream(strParams.getBytes()), parameters);

		/* Clear field groups. */
		List<String> groupKeys = new ArrayList<>();
		Iterator<Object> keys = properties.keySet().iterator();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			if (key.startsWith("group-")) {
				groupKeys.add(key);
			}
		}
		groupKeys.forEach(key -> properties.remove(key));

		/* Set field groups. */
		int ndx = 0;
		properties.setObject(
			DB.GROUP_DATA,
			new FieldGroup(ndx++, "data", "Data"));
		properties.setObject(
			DB.GROUP_LABELS,
			new FieldGroup(ndx++, "labels", "Labels"));
		properties.setObject(
			DB.GROUP_AVG,
			new FieldGroup(ndx++, "avgW", "Averages"));
		properties.setObject(
			DB.GROUP_AVG_DELTAS,
			new FieldGroup(ndx++, "avg-deltas", "Avg-Deltas"));
		properties.setObject(
			DB.GROUP_AVG_SLOPES,
			new FieldGroup(ndx++, "avg-slopes", "Avg-Slopes"));
		properties.setObject(
			DB.GROUP_AVG_SPREADS,
			new FieldGroup(ndx++, "avg-spreads", "Avg-Spreads"));
		properties.setObject(
			DB.GROUP_VAR,
			new FieldGroup(ndx++, "vars", "Variances"));
		properties.setObject(
			DB.GROUP_VAR_SLOPES,
			new FieldGroup(ndx++, "var-slopes", "Var-Slopes"));
		properties.setObject(
			DB.GROUP_VAR_SPREADS,
			new FieldGroup(ndx++, "var-spreads", "Var-Spreads"));

		List<Average> averages = getParameters().getAverages();
		for (int i = 0; i < averages.size(); i++) {
			int size = getCandleSize(i);
			String suffix = getPadded(size);
			properties.setObject(
				DB.GROUP_CANDLES.apply(i),
				new FieldGroup(ndx++, "candles_" + suffix, "Candles " + suffix));
		}
	}

}
