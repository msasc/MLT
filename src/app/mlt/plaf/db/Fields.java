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

/**
 * Field names.
 *
 * @author Miquel Sas
 */
public class Fields {

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
}
