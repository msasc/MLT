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
package app.mlt.plaf;

import java.text.SimpleDateFormat;

import com.mlt.db.Calculator;
import com.mlt.db.Field;
import com.mlt.db.Record;
import com.mlt.db.Types;
import com.mlt.db.Value;
import com.mlt.desktop.converters.NumberScaleConverter;
import com.mlt.desktop.converters.TimeFmtConverter;
import com.mlt.mkt.data.Instrument;
import com.mlt.mkt.data.Period;

/**
 * Field names.
 *
 * @author Miquel Sas
 */
public class Fields {

	/**
	 * Calculator to return the value of the named field.
	 */
	public static class FieldValue implements Calculator {
	
		private String alias;
	
		/**
		 * Consturctor.
		 * 
		 * @param alias Field alias.
		 */
		public FieldValue(String alias) {
			super();
			this.alias = alias;
		}
	
		@Override
		public Value getValue(Record record) {
			return record.getValue(alias);
		}
	
	}

	/* Averages type, period and smooths. */
	public static final String AVERAGE_TYPE = "avg_type";
	public static final String AVERAGE_PERIOD = "avg_period";
	public static final String AVERAGE_SMOOTHS = "avg_smooths";

	/* Bar data, time, open, high, low, close, volume. */
	public static final String BAR_TIME = "time";
	public static final String BAR_TIME_FMT = "time_fmt";
	public static final String BAR_OPEN = "open";
	public static final String BAR_HIGH = "high";
	public static final String BAR_LOW = "low";
	public static final String BAR_CLOSE = "close";
	public static final String BAR_VOLUME = "volume";

	/* Data filters. */
	public static final String DATA_FILTER = "data_filter";

	/* Instrument descriptors. */
	public static final String INSTRUMENT_ID = "instr_id";
	public static final String INSTRUMENT_DESC = "instr_desc";
	public static final String INSTRUMENT_PIP_VALUE = "instr_pipv";
	public static final String INSTRUMENT_PIP_SCALE = "instr_pips";
	public static final String INSTRUMENT_PRIMARY_CURRENCY = "instr_currp";
	public static final String INSTRUMENT_SECONDARY_CURRENCY = "instr_currs";
	public static final String INSTRUMENT_TICK_VALUE = "instr_tickv";
	public static final String INSTRUMENT_TICK_SCALE = "instr_ticks";
	public static final String INSTRUMENT_VOLUME_SCALE = "instr_vols";

	/* Offer sides. */
	public static final String OFFER_SIDE = "offer_side";

	/* Period descriptors. */
	public static final String PERIOD = "period";
	public static final String PERIOD_ID = "period_id";
	public static final String PERIOD_NAME = "period_name";
	public static final String PERIOD_SIZE = "period_size";
	public static final String PERIOD_UNIT_INDEX = "period_unit_index";
	
	/* Statistics process calculated track. */
	public static final String PROCESS_ID = "process";
	public static final String PROCESS_TRACK = "calculated";
	
	/* Range (statistics) fields. */
	public static final String RANGE_NAME = "name";
	public static final String RANGE_PERIOD = "period";
	public static final String RANGE_MINIMUM = "minimum";
	public static final String RANGE_MAXIMUM = "maximum";
	public static final String RANGE_AVERAGE = "average";
	public static final String RANGE_STDDEV = "std_dev";
	public static final String RANGE_AVG_STD_10 = "avg_std_10";
	public static final String RANGE_AVG_STD_20 = "avg_std_20";

	/* Server descriptors. */
	public static final String SERVER_ID = "server_id";
	public static final String SERVER_NAME = "server_name";
	public static final String SERVER_TITLE = "server_title";
	
	/* Normalized flag in the states table. */
	public static final String STATES_NORMALIZED = "normalized";

	/* Statistics. */
	public static final String STATISTICS_ID = "stats_id";
	public static final String STATISTICS_KEY = "stats_key";
	public static final String STATISTICS_PARAMS = "stats_params";
	public static final String STATISTICS_PARAMS_DESC = "stats_params_desc";

	/* Ticker table name. */
	public static final String TABLE_NAME = "table_name";

	/**
	 * Returns the field to show data (open, high, low, close) for an instrument.
	 * 
	 * @param instrument The instrument.
	 * @param name       The field name.
	 * @param header     The header.
	 * @return The field.
	 */
	public static Field getData(Instrument instrument, String name, String header) {
		return getData(instrument, name, header, header);
	}

	/**
	 * Returns the field to show data (open, high, low, close) for an instrument.
	 * 
	 * @param instrument The instrument.
	 * @param name       The field name.
	 * @param header     The header.
	 * @param label      The label.
	 * @return The field.
	 */
	public static Field getData(
		Instrument instrument,
		String name,
		String header,
		String label) {
		Field field = Fields.getDouble(name, header, label);
		field.setDisplayDecimals(instrument.getPipScale());
		field.setStringConverter(new NumberScaleConverter(instrument.getPipScale()));
		return field;
	}

	/**
	 * Returns field definition for a double value.
	 * 
	 * @param name   Field name.
	 * @param header The field header.
	 * @return The field definition.
	 */
	public static Field getDouble(String name, String header) {
		return getDouble(name, header, header);
	}

	/**
	 * Returns field definition for a double value.
	 * 
	 * @param name   Field name.
	 * @param header The field header.
	 * @param label  The field label.
	 * @return The field definition.
	 */
	public static Field getDouble(String name, String header, String label) {
		Field field = new Field();
		field.setName(name);
		field.setAlias(name);
		field.setType(Types.DOUBLE);
		field.setHeader(header);
		field.setLabel(label);
		field.setTitle(label);
		return field;
	}

	/**
	 * Returns field definition for a string value.
	 * 
	 * @param name   Field name.
	 * @param length Field length.
	 * @param header The field header.
	 * @return The field definition.
	 */
	public static Field getString(String name, int length, String header) {
		return getString(name, length, header, header);
	}

	/**
	 * Returns field definition for a string value.
	 * 
	 * @param name   Field name.
	 * @param length Field length.
	 * @param header The field header.
	 * @param label  The field label.
	 * @return The field definition.
	 */
	public static Field getString(String name, int length, String header, String label) {
		Field field = new Field();
		field.setName(name);
		field.setAlias(name);
		field.setType(Types.STRING);
		field.setLength(length);
		field.setHeader(header);
		field.setLabel(label);
		field.setTitle(label);
		return field;
	}

	/**
	 * Returns field definition for an integer value.
	 * 
	 * @param name   Field name.
	 * @param header The field header.
	 * @return The field definition.
	 */
	public static Field getInteger(String name, String header) {
		return getInteger(name, header, header);
	}

	/**
	 * Returns field definition for an integer value.
	 * 
	 * @param name   Field name.
	 * @param header The field header.
	 * @param label  The field label.
	 * @return The field definition.
	 */
	public static Field getInteger(String name, String header, String label) {
		Field field = new Field();
		field.setName(name);
		field.setAlias(name);
		field.setType(Types.INTEGER);
		field.setHeader(header);
		field.setLabel(label);
		field.setTitle(label);
		return field;
	}

	/**
	 * Returns field definition for an long value.
	 * 
	 * @param name   Field name.
	 * @param header The field header.
	 * @return The field definition.
	 */
	public static Field getLong(String name, String header) {
		return getLong(name, header, header);
	}

	/**
	 * Returns field definition for an long value.
	 * 
	 * @param name   Field name.
	 * @param header The field header.
	 * @param label  The label.
	 * @return The field definition.
	 */
	public static Field getLong(String name, String header, String label) {
		Field field = new Field();
		field.setName(name);
		field.setAlias(name);
		field.setType(Types.LONG);
		field.setHeader(header);
		field.setLabel(label);
		field.setTitle(label);
		return field;
	}

	/**
	 * Return the field definition for the formatted time.
	 * 
	 * @param period      The period.
	 * @param nameTime    The time name.
	 * @param nameTimeFmt The formatted time name.
	 * @param header      The header.
	 * @return The field field definition.
	 */
	public static Field getTimeFmt(
		Period period,
		String nameTime,
		String nameTimeFmt,
		String header) {
		Field field = getLong(nameTimeFmt, header);
		field.setPersistent(false);
		field.setCalculator(new FieldValue(nameTime));
		String pattern = period.getTimeFmtPattern();
		field.setStringConverter(new TimeFmtConverter(new SimpleDateFormat(pattern)));
		return field;
	}
}
