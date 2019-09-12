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
package app.mlt.plaf.db;

import com.mlt.util.Strings;

import app.mlt.plaf.statistics.Average;

/**
 * Field names.
 *
 * @author Miquel Sas
 */
public class Fields {

	/*
	 * Data filters.
	 */

	public static final String DATA_FILTER = "data_filter";

	/*
	 * Instrument descriptors.
	 */

	public static final String INSTRUMENT_ID = "instr_id";
	public static final String INSTRUMENT_DESC = "instr_desc";
	public static final String INSTRUMENT_PIP_VALUE = "instr_pipv";
	public static final String INSTRUMENT_PIP_SCALE = "instr_pips";
	public static final String INSTRUMENT_PRIMARY_CURRENCY = "instr_currp";
	public static final String INSTRUMENT_SECONDARY_CURRENCY = "instr_currs";
	public static final String INSTRUMENT_TICK_VALUE = "instr_tickv";
	public static final String INSTRUMENT_TICK_SCALE = "instr_ticks";
	public static final String INSTRUMENT_VOLUME_SCALE = "instr_vols";

	/*
	 * Offer sides.
	 */

	public static final String OFFER_SIDE = "offer_side";

	/*
	 * Period descriptors.
	 */

	public static final String PERIOD = "period";
	public static final String PERIOD_ID = "period_id";
	public static final String PERIOD_NAME = "period_name";
	public static final String PERIOD_SIZE = "period_size";
	public static final String PERIOD_UNIT_INDEX = "period_unit_index";

	/*
	 * Server descriptors.
	 */

	public static final String SERVER_ID = "server_id";
	public static final String SERVER_NAME = "server_name";
	public static final String SERVER_TITLE = "server_title";

	/*
	 * Bar data, time, open, high, low, close, volume.
	 */

	public static final String TIME = "time";
	public static final String TIME_FMT = "time_fmt";
	public static final String OPEN = "open";
	public static final String HIGH = "high";
	public static final String LOW = "low";
	public static final String CLOSE = "close";
	public static final String VOLUME = "volume";

	/*
	 * Ticker table name.
	 */

	public static final String TABLE_NAME = "table_name";
	
	/*
	 * Range (statistics) fields.
	 */
	
	public static final String RANGE_NAME = "name";
	public static final String RANGE_MIN_MAX = "min_max";
	public static final String RANGE_PERIOD = "period";
	public static final String RANGE_VALUE = "value";

	/*
	 * Statistics
	 */

	public static final String STATISTICS_ID = "stats_id";
	public static final String STATISTICS_KEY = "stats_key";
	public static final String STATISTICS_PARAMS = "stats_params";
	
	/*
	 * Averages type, period and smooths.
	 */
	
	public static final String AVERAGE_TYPE = "avg_type";
	public static final String AVERAGE_PERIOD = "avg_period";
	public static final String AVERAGE_SMOOTHS = "avg_smooths";

	/**
	 * Returns the field header for an average.
	 * 
	 * @param average The average.
	 * @return The header.
	 */
	public static String averageHeader(Average average) {
		return "Avg-" + average.getPeriod();
	}

	/**
	 * Returns the field label for an average.
	 * 
	 * @param average The average.
	 * @return The label.
	 */
	public static String averageLabel(Average average) {
		StringBuilder b = new StringBuilder();
		b.append("Average (");
		b.append(average.getPeriod());
		for (int smooth : average.getSmooths()) {
			b.append(", ");
			b.append(smooth);
		}
		b.append(")");
		return b.toString();
	}

	/**
	 * Returns the field name for an average.
	 * 
	 * @param average The average.
	 * @return The name.
	 */
	public static String averageName(Average average) {
		return "average_" + average.getPeriod();
	}

	/**
	 * Return the complex field header.
	 * 
	 * @param prefix Prefix.
	 * @param period Period.
	 * @param index  Index.
	 * @param pad    Padding.
	 * @param suffix Suffix.
	 * @return The field name.
	 */
	public static String fieldHeader(String prefix, int period, int index, int pad, String suffix) {
		StringBuilder b = new StringBuilder();
		b.append(prefix);
		b.append("-");
		b.append(period);
		b.append("-");
		b.append(Strings.leftPad(Integer.toString(index), pad));
		b.append("-");
		b.append(suffix);
		return b.toString();
	}

	/**
	 * Return the complex field header (consecutive indexes).
	 * 
	 * @param prefix Prefix.
	 * @param period Period.
	 * @param index  Index.
	 * @param pad    Padding.
	 * @param suffix Suffix.
	 * @return The field name.
	 */
	public static String fieldHeaderRel(String prefix, int period, int index, int pad, String suffix) {
		StringBuilder b = new StringBuilder();
		b.append(prefix);
		b.append("-");
		b.append(period);
		b.append("-");
		b.append(Strings.leftPad(Integer.toString(index), pad));
		b.append("-");
		b.append(Strings.leftPad(Integer.toString(index + 1), pad));
		b.append("-");
		b.append(suffix);
		return b.toString();
	}

	/**
	 * Return the complex field label (consecutive indexes).
	 * 
	 * @param prefix Prefix.
	 * @param period Period.
	 * @param index  Index.
	 * @param pad    Padding.
	 * @param suffix Suffix.
	 * @return The field name.
	 */
	public static String fieldLabel(String prefix, int period, int index, int pad, String suffix) {
		return fieldHeader(prefix, period, index, pad, suffix);
	}

	/**
	 * Return the complex field label.
	 * 
	 * @param prefix Prefix.
	 * @param period Period.
	 * @param index  Index.
	 * @param pad    Padding.
	 * @param suffix Suffix.
	 * @return The field name.
	 */
	public static String fieldLabelRel(String prefix, int period, int index, int pad, String suffix) {
		return fieldHeaderRel(prefix, period, index, pad, suffix);
	}

	/**
	 * Return the complex field name.
	 * 
	 * @param prefix Prefix.
	 * @param period Period.
	 * @param index  Index.
	 * @param pad    Padding.
	 * @param suffix Suffix.
	 * @return The field name.
	 */
	public static String fieldName(String prefix, int period, int index, int pad, String suffix) {
		StringBuilder b = new StringBuilder();
		b.append(prefix);
		b.append("_");
		b.append(period);
		b.append("_");
		b.append(Strings.leftPad(Integer.toString(index), pad));
		b.append("_");
		b.append(suffix);
		return b.toString();
	}

	/**
	 * Return the complex field name (consecutive indexes).
	 * 
	 * @param prefix Prefix.
	 * @param period Period.
	 * @param index  Index.
	 * @param pad    Padding.
	 * @param suffix Suffix.
	 * @return The field name.
	 */
	public static String fieldNameRel(String prefix, int period, int index, int pad, String suffix) {
		StringBuilder b = new StringBuilder();
		b.append(prefix);
		b.append("_");
		b.append(period);
		b.append("_");
		b.append(Strings.leftPad(Integer.toString(index), pad));
		b.append("_");
		b.append(Strings.leftPad(Integer.toString(index + 1), pad));
		b.append("_");
		b.append(suffix);
		return b.toString();
	}

	/**
	 * Returns the field header for a slope.
	 * 
	 * @param average The average.
	 * @param suffix  The suffix.
	 * @return The field header.
	 */
	public static String slopeHeader(Average average, String suffix) {
		StringBuilder b = new StringBuilder();
		b.append("Slope");
		b.append("-");
		b.append(average.getPeriod());
		b.append("-");
		b.append(suffix);
		return b.toString();
	}

	/**
	 * Returns the field label for a slope.
	 * 
	 * @param average The average.
	 * @param suffix  The suffix.
	 * @return The field label.
	 */
	public static String slopeLabel(Average average, String suffix) {
		return slopeHeader(average, suffix);
	}

	/**
	 * Returns the field name for a slope.
	 * 
	 * @param average The average.
	 * @param suffix  The suffix.
	 * @return The field name.
	 */
	public static String slopeName(Average average, String suffix) {
		StringBuilder b = new StringBuilder();
		b.append("slope");
		b.append("_");
		b.append(average.getPeriod());
		b.append("_");
		b.append(suffix);
		return b.toString();
	}

	/**
	 * Returns the field header for a spread of averages.
	 * 
	 * @param fast   The fast average.
	 * @param slow   The slow average.
	 * @param suffix The suffix.
	 * @return The field header.
	 */
	public static String spreadHeader(Average fast, Average slow, String suffix) {
		StringBuilder b = new StringBuilder();
		b.append("Spread-");
		b.append("-");
		b.append(fast.getPeriod());
		b.append("-");
		b.append(slow.getPeriod());
		b.append("-");
		b.append(suffix);
		return b.toString();
	}

	/**
	 * Returns the field label for a spread of averages.
	 * 
	 * @param fast   The fast average.
	 * @param slow   The slow average.
	 * @param suffix The suffix.
	 * @return The field label.
	 */
	public static String spreadLabel(Average fast, Average slow, String suffix) {
		return spreadHeader(fast, slow, suffix);
	}

	/**
	 * Returns the field name for a spread of averages.
	 * 
	 * @param fast   The fast average.
	 * @param slow   The slow average.
	 * @param suffix The suffix.
	 * @return The field name.
	 */
	public static String spreadName(Average fast, Average slow, String suffix) {
		StringBuilder b = new StringBuilder();
		b.append("spread");
		b.append("_");
		b.append(fast.getPeriod());
		b.append("_");
		b.append(slow.getPeriod());
		b.append("_");
		b.append(suffix);
		return b.toString();
	}
}
