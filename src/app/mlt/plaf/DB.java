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

import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.Currency;

import com.mlt.db.Calculator;
import com.mlt.db.Condition;
import com.mlt.db.Criteria;
import com.mlt.db.Field;
import com.mlt.db.ForeignKey;
import com.mlt.db.Order;
import com.mlt.db.Persistor;
import com.mlt.db.PersistorDDL;
import com.mlt.db.PersistorException;
import com.mlt.db.Record;
import com.mlt.db.RecordSet;
import com.mlt.db.Table;
import com.mlt.db.Types;
import com.mlt.db.Value;
import com.mlt.db.rdbms.DBPersistor;
import com.mlt.db.rdbms.DBPersistorDDL;
import com.mlt.desktop.EditContext;
import com.mlt.desktop.LookupRecords;
import com.mlt.desktop.control.TextArea;
import com.mlt.desktop.converters.NumberScaleConverter;
import com.mlt.desktop.converters.TimeFmtConverter;
import com.mlt.desktop.layout.Dimension;
import com.mlt.desktop.layout.Fill;
import com.mlt.mkt.data.Instrument;
import com.mlt.mkt.data.Period;

import app.mlt.plaf.statistics.StatisticsAverages;

/**
 * Statically centralizes access to lookups, persistors, records, recordsets,
 * etc.
 *
 * @author Miquel Sas
 */
public class DB {

	/**
	 * Calculator to return the value of the named field.
	 */
	static class FieldValue implements Calculator {

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

	/**
	 * Calculator to display the parameters description..
	 */
	static class ParamsDesc implements Calculator {
		@Override
		public Value getValue(Record record) {
			String id = record.getValue(FIELD_STATISTICS_ID).toString();
			String params = record.getValue(FIELD_STATISTICS_PARAMS).toString();
			if (id.equals("AVG") && !params.isEmpty()) {
				try {
					StatisticsAverages stats = StatisticsAverages.getStatistics(record);
					return new Value(stats.getParametersDescription());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return null;
		}
	}

	public static final String FIELD_BAR_TIME = "time";
	public static final String FIELD_BAR_TIME_FMT = "time_fmt";
	public static final String FIELD_BAR_OPEN = "open";
	public static final String FIELD_BAR_HIGH = "high";
	public static final String FIELD_BAR_LOW = "low";
	public static final String FIELD_BAR_CLOSE = "close";
	public static final String FIELD_BAR_VOLUME = "volume";

	public static final String FIELD_CANDLE_RANGE = "range";
	public static final String FIELD_CANDLE_BODY_FACTOR = "fbody";
	public static final String FIELD_CANDLE_BODY_POS = "pbody";
	public static final String FIELD_CANDLE_REL_POS = "frelpos";
	public static final String FIELD_CANDLE_SIGN = "sign";
	public static final String[] CANDLE_NAMES = new String[] {
		FIELD_CANDLE_RANGE,
		FIELD_CANDLE_BODY_FACTOR,
		FIELD_CANDLE_BODY_POS,
		FIELD_CANDLE_REL_POS,
		FIELD_CANDLE_SIGN
	};

	public static final String FIELD_INSTRUMENT_ID = "instr_id";
	public static final String FIELD_INSTRUMENT_DESC = "instr_desc";
	public static final String FIELD_INSTRUMENT_PIP_VALUE = "instr_pipv";
	public static final String FIELD_INSTRUMENT_PIP_SCALE = "instr_pips";
	public static final String FIELD_INSTRUMENT_PRIMARY_CURRENCY = "instr_currp";
	public static final String FIELD_INSTRUMENT_SECONDARY_CURRENCY = "instr_currs";
	public static final String FIELD_INSTRUMENT_TICK_VALUE = "instr_tickv";
	public static final String FIELD_INSTRUMENT_TICK_SCALE = "instr_ticks";
	public static final String FIELD_INSTRUMENT_VOLUME_SCALE = "instr_vols";

	public static final String FIELD_PERIOD_ID = "period_id";
	public static final String FIELD_PERIOD_NAME = "period_name";
	public static final String FIELD_PERIOD_SIZE = "period_size";
	public static final String FIELD_PERIOD_UNIT_INDEX = "period_unit_index";

	public static final String FIELD_RANGE_NAME = "name";
	public static final String FIELD_RANGE_MINIMUM = "minimum";
	public static final String FIELD_RANGE_MAXIMUM = "maximum";
	public static final String FIELD_RANGE_AVERAGE = "average";
	public static final String FIELD_RANGE_STDDEV = "std_dev";

	public static final String FIELD_SERVER_ID = "server_id";
	public static final String FIELD_SERVER_NAME = "server_name";
	public static final String FIELD_SERVER_TITLE = "server_title";

	public static final String FIELD_SOURCES_LABEL_CALC = "label_calc";
	public static final String FIELD_SOURCES_LABEL_NETC = "label_net_calc";
	public static final String FIELD_SOURCES_PIVOT_CALC = "pivot_calc";
	public static final String FIELD_SOURCES_REFV_CALC = "refv_calc";

	public static final String FIELD_SOURCES_LABEL_EDIT = "label_edit";
	public static final String FIELD_SOURCES_LABEL_NETE = "label_net_edit";
	public static final String FIELD_SOURCES_PIVOT_EDIT = "pivot_edit";
	public static final String FIELD_SOURCES_REFV_EDIT = "refv_edit";
	
	public static final String FIELD_STATISTICS_ID = "stats_id";
	public static final String FIELD_STATISTICS_KEY = "stats_key";
	public static final String FIELD_STATISTICS_PARAMS = "stats_params";
	public static final String FIELD_STATISTICS_PARAMS_DESC = "stats_params_desc";

	public static final String FIELD_TABLE_NAME = "table_name";

	public static final String TABLE_INSTRUMENTS = "instruments";
	public static final String TABLE_PERIODS = "periods";
	public static final String TABLE_SERVERS = "servers";
	public static final String TABLE_STATISTICS = "statistics";
	public static final String TABLE_TICKERS = "tickers";

	/** DDL. */
	private static PersistorDDL ddl;

	/**
	 * @return The persistor DDL.
	 */
	public static PersistorDDL ddl() {
		if (ddl == null) {
			ddl = new DBPersistorDDL(MLT.getDBEngine());
		}
		return ddl;
	}

	/**
	 * 
	 * @param name   Field name.
	 * @param header The field header.
	 * @return The field definition for a double value.
	 */
	public static Field field_boolean(String name, String header) {
		return field_boolean(name, header, header);
	}

	/**
	 * @param name   Field name.
	 * @param header The field header.
	 * @param label  The field label.
	 * @return The field definition for a double value.
	 */
	public static Field field_boolean(String name, String header, String label) {
		Field field = new Field();
		field.setName(name);
		field.setAlias(name);
		field.setType(Types.BOOLEAN);
		field.setHeader(header);
		field.setLabel(label);
		field.setTitle(label);
		return field;
	}

	/**
	 * @param instrument The instrument.
	 * @param name       The field name.
	 * @param header     The header.
	 * @return The field to show data (open, high, low, close) for an instrument.
	 */
	public static Field field_data(Instrument instrument, String name, String header) {
		return field_data(instrument, name, header, header);
	}

	/**
	 * @param instrument The instrument.
	 * @param name       The field name.
	 * @param header     The header.
	 * @param label      The label.
	 * @return The field to show data (open, high, low, close) for an instrument..
	 */
	public static Field field_data(
		Instrument instrument,
		String name,
		String header,
		String label) {
		Field field = field_double(name, header, label);
		field.setDisplayDecimals(instrument.getPipScale());
		field.setStringConverter(new NumberScaleConverter(instrument.getPipScale()));
		return field;
	}

	/**
	 * 
	 * @param name   Field name.
	 * @param header The field header.
	 * @return The field definition for a double value.
	 */
	public static Field field_double(String name, String header) {
		return field_double(name, header, header);
	}

	/**
	 * @param name   Field name.
	 * @param header The field header.
	 * @param label  The field label.
	 * @return The field definition for a double value.
	 */
	public static Field field_double(String name, String header, String label) {
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
	 * @param name   Field name.
	 * @param header The field header.
	 * @return The field definition for an integer value.
	 */
	public static Field field_integer(String name, String header) {
		return field_integer(name, header, header);
	}

	/**
	 * @param name   Field name.
	 * @param header The field header.
	 * @param label  The field label.
	 * @return The field definition for an integer value.
	 */
	public static Field field_integer(String name, String header, String label) {
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
	 * @param name   Field name.
	 * @param header The field header.
	 * @return The field definition for an long value.
	 */
	public static Field field_long(String name, String header) {
		return field_long(name, header, header);
	}

	/**
	 * @param name   Field name.
	 * @param header The field header.
	 * @param label  The label.
	 * @return The field definition for an long value.
	 */
	public static Field field_long(String name, String header, String label) {
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
	 * @param name   Field name.
	 * @param length Field length.
	 * @param header The field header.
	 * @return The field definition for a string value.
	 */
	public static Field field_string(String name, int length, String header) {
		return field_string(name, length, header, header);
	}

	/**
	 * @param name   Field name.
	 * @param length Field length.
	 * @param header The field header.
	 * @param label  The field label.
	 * @return The field definition for a string value.
	 */
	public static Field field_string(String name, int length, String header, String label) {
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
	 * @param period      The period.
	 * @param nameTime    The time name.
	 * @param header      The header.
	 * @return The field field definition for the formatted time.
	 */
	public static Field field_timeFmt(
		Period period,
		String nameTime,
		String header) {
		String nameTimeFmt = nameTime + "_fmt";
		Field field = field_long(nameTimeFmt, header);
		field.setPersistent(false);
		field.setCalculator(new FieldValue(nameTime));
		String pattern = period.getTimeFmtPattern();
		field.setStringConverter(new TimeFmtConverter(new SimpleDateFormat(pattern)));
		return field;
	}

	/**
	 * @param period      The period.
	 * @param nameTime    The time name.
	 * @param nameTimeFmt The formatted time name.
	 * @param header      The header.
	 * @return The field field definition for the formatted time.
	 */
	public static Field field_timeFmt(
		Period period,
		String nameTime,
		String nameTimeFmt,
		String header) {
		Field field = field_long(nameTimeFmt, header);
		field.setPersistent(false);
		field.setCalculator(new FieldValue(nameTime));
		String pattern = period.getTimeFmtPattern();
		field.setStringConverter(new TimeFmtConverter(new SimpleDateFormat(pattern)));
		return field;
	}

	/**
	 * Lookup an instrument.
	 * 
	 * @return The selected instrument record.
	 * @throws PersistorException
	 */
	public static Record lookup_instrument() throws PersistorException {
		LookupRecords lookup = new LookupRecords(persistor_instruments().getDefaultRecord());
		lookup.addColumn(FIELD_INSTRUMENT_ID);
		lookup.addColumn(FIELD_INSTRUMENT_DESC);
		lookup.addColumn(FIELD_INSTRUMENT_PIP_VALUE);
		lookup.addColumn(FIELD_INSTRUMENT_PIP_SCALE);
		lookup.addColumn(FIELD_INSTRUMENT_TICK_VALUE);
		lookup.addColumn(FIELD_INSTRUMENT_TICK_SCALE);
		lookup.addColumn(FIELD_INSTRUMENT_VOLUME_SCALE);
		lookup.addColumn(FIELD_INSTRUMENT_PRIMARY_CURRENCY);
		lookup.addColumn(FIELD_INSTRUMENT_SECONDARY_CURRENCY);
		lookup.setRecordSet(recordset_instruments());
		lookup.setSize(0.5, 0.8);
		lookup.setTitle("Select the instrument");
		Record record = lookup.lookupRecord();
		return record;
	}

	/**
	 * Lookup a period.
	 * 
	 * @return The selected period.
	 * @throws PersistorException
	 */
	public static Record lookup_period() throws PersistorException {
		LookupRecords lookup = new LookupRecords(persistor_periods().getDefaultRecord());
		lookup.addColumn(FIELD_PERIOD_ID);
		lookup.addColumn(FIELD_PERIOD_NAME);
		lookup.addColumn(FIELD_PERIOD_SIZE);
		lookup.setRecordSet(recordset_periods());
		lookup.setSize(0.3, 0.4);
		lookup.setTitle("Select the instrument");
		Record record = lookup.lookupRecord();
		return record;
	}

	/**
	 * Lookup a ticker.
	 * 
	 * @return The selected ticker record.
	 * @throws PersistorException
	 */
	public static Record lookup_ticker() throws PersistorException {
		LookupRecords lookup = new LookupRecords(persistor_tickers().getDefaultRecord());
		lookup.addColumn(FIELD_INSTRUMENT_ID);
		lookup.addColumn(FIELD_PERIOD_ID);
		lookup.addColumn(FIELD_PERIOD_NAME);
		lookup.addColumn(FIELD_PERIOD_SIZE);
		lookup.setRecordSet(recordset_tickers());
		lookup.setSize(0.3, 0.4);
		lookup.setTitle("Select the ticker");
		Record record = lookup.lookupRecord();
		return record;
	}

	/**
	 * Return the generic ticker name.
	 * 
	 * @param instrument The instrument.
	 * @param period     The period.
	 * @param suffix     Optional suffix.
	 * @return The 'ticker' table name.
	 */
	public static String name_ticker(Instrument instrument, Period period, String suffix) {
		StringBuilder b = new StringBuilder();
		b.append(instrument.getId().toLowerCase());
		b.append("_");
		b.append(period.getId().toLowerCase());
		if (suffix != null) {
			b.append("_");
			b.append(suffix.toLowerCase());
		}
		return b.toString();
	}

	/**
	 * Return the generic ticker name.
	 * 
	 * @param instrument The instrument.
	 * @param period     The period.
	 * @return The 'ticker' table name.
	 */
	public static String name_ticker(Instrument instrument, Period period) {
		return name_ticker(instrument, period, null);
	}

	/**
	 * Returns the instruments persistor.
	 * 
	 * @return The persistor.
	 */
	public static Persistor persistor_instruments() {
		return table_instruments().getPersistor();
	}

	/**
	 * Returns the periods persistor.
	 * 
	 * @return The persistor.
	 */
	public static Persistor persistor_periods() {
		return table_periods().getPersistor();
	}

	/**
	 * Returns the servers persistor.
	 * 
	 * @return The persistor.
	 */
	public static Persistor persistor_servers() {
		return table_servers().getPersistor();
	}

	/**
	 * Returns the statistics persistor.
	 * 
	 * @return The persistor.
	 */
	public static Persistor persistor_statistics() {
		return table_statistics().getPersistor();
	}

	/**
	 * Returns the data price persistor.
	 * 
	 * @param instrument The instrument.
	 * @param period     The period.
	 * @return The persistor.
	 */
	public static Persistor persistor_ticker(Instrument instrument, Period period) {
		return table_ticker(instrument, period).getPersistor();
	}

	/**
	 * Returns the tickers persistor.
	 * 
	 * @return The persistor.
	 */
	public static Persistor persistor_tickers() {
		return table_tickers().getPersistor();
	}

	/**
	 * Returns the filled record for the instrument.
	 * 
	 * @param instrument The instrument.
	 * @return The record.
	 */
	public static Record record_instrument(Instrument instrument) {

		Value vSERVER_ID = new Value(MLT.getServer().getId());
		Value vINSTRUMENT_ID = new Value(instrument.getId());
		Value vINSTRUMENT_DESC = new Value(instrument.getDescription());
		Value vINSTRUMENT_PIP_VALUE = new Value(instrument.getPipValue());
		Value vINSTRUMENT_PIP_SCALE = new Value(instrument.getPipScale());
		Value vINSTRUMENT_TICK_VALUE = new Value(instrument.getTickValue());
		Value vINSTRUMENT_TICK_SCALE = new Value(instrument.getTickScale());
		Value vINSTRUMENT_VOLUME_SCALE = new Value(instrument.getVolumeScale());
		Value vINSTRUMENT_PRIMARY_CURRENCY =
			new Value(instrument.getPrimaryCurrency().toString());
		Value vINSTRUMENT_SECONDARY_CURRENCY =
			new Value(instrument.getSecondaryCurrency().toString());

		Record record = persistor_instruments().getDefaultRecord();
		record.setValue(FIELD_SERVER_ID, vSERVER_ID);
		record.setValue(FIELD_INSTRUMENT_ID, vINSTRUMENT_ID);
		record.setValue(FIELD_INSTRUMENT_DESC, vINSTRUMENT_DESC);
		record.setValue(FIELD_INSTRUMENT_PIP_VALUE, vINSTRUMENT_PIP_VALUE);
		record.setValue(FIELD_INSTRUMENT_PIP_SCALE, vINSTRUMENT_PIP_SCALE);
		record.setValue(FIELD_INSTRUMENT_TICK_VALUE, vINSTRUMENT_TICK_VALUE);
		record.setValue(FIELD_INSTRUMENT_TICK_SCALE, vINSTRUMENT_TICK_SCALE);
		record.setValue(FIELD_INSTRUMENT_VOLUME_SCALE, vINSTRUMENT_VOLUME_SCALE);
		record.setValue(FIELD_INSTRUMENT_PRIMARY_CURRENCY, vINSTRUMENT_PRIMARY_CURRENCY);
		record.setValue(FIELD_INSTRUMENT_SECONDARY_CURRENCY, vINSTRUMENT_SECONDARY_CURRENCY);
		return record;
	}

	/**
	 * Returns the filled record for the instrument.
	 * 
	 * @param id The instrument id.
	 * @return The record.
	 */
	public static Record record_instrument(String id) throws PersistorException {
		Persistor persistor = persistor_instruments();
		Record record = persistor.getDefaultRecord();
		record.setValue(FIELD_SERVER_ID, new Value(MLT.getServer().getId()));
		record.setValue(FIELD_INSTRUMENT_ID, new Value(id));
		persistor.refresh(record);
		return record;
	}

	/**
	 * Returns the filled record for the period.
	 * 
	 * @param id The period id.
	 * @return The record.
	 */
	public static Record record_period(String id) throws PersistorException {
		Persistor persistor = persistor_periods();
		Record record = persistor.getDefaultRecord();
		record.setValue(FIELD_PERIOD_ID, new Value(id));
		persistor.refresh(record);
		return record;
	}

	/**
	 * Return the ticker record.
	 * 
	 * @param instrument Instrument.
	 * @param period     Period.
	 * @return The record.
	 */
	public static Record record_ticker(Instrument instrument, Period period) {
		Record record = persistor_tickers().getDefaultRecord();
		record.setValue(FIELD_SERVER_ID, new Value(MLT.getServer().getId()));
		record.setValue(FIELD_INSTRUMENT_ID, new Value(instrument.getId()));
		record.setValue(FIELD_PERIOD_ID, new Value(period.getId()));
		record.setValue(FIELD_TABLE_NAME, new Value(name_ticker(instrument, period)));
		return record;
	}

	/**
	 * Returns a record set with the available instruments for the argument server.
	 * 
	 * @return The record set.
	 * @throws PersistorException If any persistence error occurs.
	 */
	public static RecordSet recordset_instruments() throws PersistorException {

		Persistor persistor = persistor_instruments();
		Criteria criteria = new Criteria();
		Field field = persistor.getField(FIELD_SERVER_ID);
		Value value = new Value(MLT.getServer().getId());
		criteria.add(Condition.fieldEQ(field, value));
		RecordSet recordSet = persistor.select(criteria);

		/* Track max pip and tick scale to set their values decimals. */
		int maxPipScale = 0;
		int maxTickScale = 0;
		for (int i = 0; i < recordSet.size(); i++) {
			Record record = recordSet.get(i);
			int pipScale = record.getValue(FIELD_INSTRUMENT_PIP_SCALE).getInteger();
			int tickScale = record.getValue(FIELD_INSTRUMENT_TICK_SCALE).getInteger();
			maxPipScale = Math.max(maxPipScale, pipScale);
			maxTickScale = Math.max(maxTickScale, tickScale);
		}
		recordSet.getField(FIELD_INSTRUMENT_PIP_VALUE).setDisplayDecimals(maxPipScale);
		recordSet.getField(FIELD_INSTRUMENT_TICK_VALUE).setDisplayDecimals(maxTickScale);
		return recordSet;
	}

	/**
	 * Returns a record set with the standard periods.
	 * 
	 * @return The record set.
	 * @throws PersistorException If any persistence error occurs.
	 */
	public static RecordSet recordset_periods() throws PersistorException {
		Persistor persistor = persistor_periods();
		RecordSet recordSet = persistor.select((Criteria) null);
		return recordSet;
	}

	/**
	 * Returns a record set with the defined statistics for the argument server.
	 * 
	 * @return The record set.
	 * @throws PersistorException If any persistence error occurs.
	 */
	public static RecordSet recordset_statistics() throws PersistorException {
		Persistor persistor = persistor_statistics();
		Field field = persistor.getField(FIELD_SERVER_ID);
		Value value = new Value(MLT.getServer().getId());
		Criteria criteria = new Criteria();
		criteria.add(Condition.fieldEQ(field, value));
		RecordSet recordSet = persistor.select(criteria);
		return recordSet;
	}

	/**
	 * Returns a record set with the defined tickers for the argument server.
	 * 
	 * @return The record set.
	 * @throws PersistorException If any persistence error occurs.
	 */
	public static RecordSet recordset_tickers() throws PersistorException {
		Persistor persistor = persistor_tickers();
		Field field = persistor.getField(FIELD_SERVER_ID);
		Value value = new Value(MLT.getServer().getId());
		Criteria criteria = new Criteria();
		criteria.add(Condition.fieldEQ(field, value));
		RecordSet recordSet = persistor.select(criteria);
		return recordSet;
	}

	public static String schema_server() {
		return "qtfx" + "_" + MLT.getServer().getId();
	}

	public static String schema_system() {
		return "qtfx";
	}

	/**
	 * @return The instruments table.
	 */
	public static Table table_instruments() {

		Table table = new Table();
		table.setName(TABLE_INSTRUMENTS);
		table.setSchema(schema_system());

		table.addField(field_string(FIELD_SERVER_ID, 20, "Server id"));
		table.addField(field_string(FIELD_INSTRUMENT_ID, 20, "Instrument"));
		table.addField(field_string(FIELD_INSTRUMENT_DESC, 120, "Instrument description"));
		table.addField(field_double(FIELD_INSTRUMENT_PIP_VALUE, "Pip value"));
		table.addField(field_integer(FIELD_INSTRUMENT_PIP_SCALE, "Pip scale"));
		table.addField(field_double(FIELD_INSTRUMENT_TICK_VALUE, "Tick value"));
		table.addField(field_integer(FIELD_INSTRUMENT_TICK_SCALE, "Tick scale"));
		table.addField(field_integer(FIELD_INSTRUMENT_VOLUME_SCALE, "Volume scale"));
		table.addField(field_string(FIELD_INSTRUMENT_PRIMARY_CURRENCY, 6, "P-Currency"));
		table.addField(field_string(FIELD_INSTRUMENT_SECONDARY_CURRENCY, 6, "S-Currency"));

		table.getField(FIELD_SERVER_ID).setPrimaryKey(true);
		table.getField(FIELD_INSTRUMENT_ID).setPrimaryKey(true);

		table.setPersistor(
			new DBPersistor(MLT.getDBEngine(), table.getComplexView(table.getPrimaryKey())));

		return table;
	}

	/**
	 * @return The periods table.
	 */
	public static Table table_periods() {
		Table table = new Table();
		table.setName(TABLE_PERIODS);
		table.setSchema(schema_system());

		table.addField(field_string(FIELD_PERIOD_ID, 5, "Period id"));
		table.addField(field_string(FIELD_PERIOD_NAME, 15, "Period name"));
		table.addField(field_integer(FIELD_PERIOD_UNIT_INDEX, "Period unit index"));
		table.addField(field_integer(FIELD_PERIOD_SIZE, "Period size"));

		table.getField(FIELD_PERIOD_ID).setPrimaryKey(true);

		Order order = new Order();
		order.add(table.getField(FIELD_PERIOD_UNIT_INDEX));
		order.add(table.getField(FIELD_PERIOD_SIZE));

		table.setPersistor(new DBPersistor(MLT.getDBEngine(), table.getComplexView(order)));

		return table;
	}

	/**
	 * @return The servers table.
	 */
	public static Table table_servers() {
		Table table = new Table();
		table.setName(TABLE_SERVERS);
		table.setSchema(schema_system());

		table.addField(field_string(FIELD_SERVER_ID, 20, "Server id"));
		table.addField(field_string(FIELD_SERVER_NAME, 60, "Server name"));
		table.addField(field_string(FIELD_SERVER_TITLE, 120, "Server title"));

		table.getField(FIELD_SERVER_ID).setPrimaryKey(true);

		table.setPersistor(
			new DBPersistor(MLT.getDBEngine(), table.getComplexView(table.getPrimaryKey())));

		return table;
	}

	/**
	 * @return The statistics table.
	 */
	public static Table table_statistics() {
		Table table = new Table();
		table.setName(TABLE_STATISTICS);
		table.setSchema(schema_system());

		table.addField(field_string(FIELD_SERVER_ID, 20, "Server id"));
		table.addField(field_string(FIELD_INSTRUMENT_ID, 20, "Instrument"));
		table.addField(field_string(FIELD_PERIOD_ID, 5, "Period id"));

		table.addField(field_string(FIELD_STATISTICS_ID, 5, "Id", "Statistics id"));
		table.getField(FIELD_STATISTICS_ID).addPossibleValue("AVG", "Averages");

		table.addField(field_string(FIELD_STATISTICS_KEY, 2, "Key", "Statistics key"));
		table.getField(FIELD_STATISTICS_KEY).setUppercase(true);

		Field params =
			field_string(
				FIELD_STATISTICS_PARAMS,
				Types.FIXED_LENGTH * 10,
				"Statistics params");
		TextArea textArea = new TextArea();
		textArea.setPreferredSize(new Dimension(600, 300));
		textArea.setFont(new Font("Courier", Font.PLAIN, 14));
		params.getProperties().setObject(EditContext.EDIT_FIELD, textArea);
		params.getProperties().setObject(EditContext.FILL, Fill.BOTH);
		table.addField(params);

		Field paramsDesc =
			field_string(
				FIELD_STATISTICS_PARAMS_DESC,
				1024,
				"Parameters description");
		paramsDesc.setPersistent(false);
		paramsDesc.setCalculator(new ParamsDesc());
		table.addField(paramsDesc);

		table.getField(FIELD_SERVER_ID).setPrimaryKey(true);
		table.getField(FIELD_INSTRUMENT_ID).setPrimaryKey(true);
		table.getField(FIELD_PERIOD_ID).setPrimaryKey(true);
		table.getField(FIELD_STATISTICS_ID).setPrimaryKey(true);
		table.getField(FIELD_STATISTICS_KEY).setPrimaryKey(true);

		Table tablePeriods = table_periods();
		ForeignKey fkPeriods = new ForeignKey(false);
		fkPeriods.setLocalTable(table);
		fkPeriods.setForeignTable(tablePeriods);
		fkPeriods.add(table.getField(FIELD_PERIOD_ID), tablePeriods.getField(FIELD_PERIOD_ID));
		table.addForeignKey(fkPeriods);

		Order order = new Order();
		order.add(table.getField(FIELD_SERVER_ID));
		order.add(table.getField(FIELD_INSTRUMENT_ID));
		order.add(tablePeriods.getField(FIELD_PERIOD_UNIT_INDEX));
		order.add(tablePeriods.getField(FIELD_PERIOD_SIZE));
		order.add(table.getField(FIELD_STATISTICS_ID));
		order.add(table.getField(FIELD_STATISTICS_KEY));

		table.setPersistor(
			new DBPersistor(MLT.getDBEngine(), table.getComplexView(order)));

		return table;
	}

	/**
	 * @param instrument Instrument.
	 * @param period     Period.
	 * @return The ticker table.
	 */
	public static Table table_ticker(Instrument instrument, Period period) {
		Table table = new Table();
		table.setName(name_ticker(instrument, period));
		table.setSchema(schema_server());

		table.addField(field_long(FIELD_BAR_TIME, "Time"));
		table.addField(field_data(instrument, FIELD_BAR_OPEN, "Open"));
		table.addField(field_data(instrument, FIELD_BAR_HIGH, "High"));
		table.addField(field_data(instrument, FIELD_BAR_LOW, "Low"));
		table.addField(field_data(instrument, FIELD_BAR_CLOSE, "Close"));
		table.addField(field_double(FIELD_BAR_VOLUME, "Volume"));
		table.getField(FIELD_BAR_VOLUME).setDecimals(instrument.getVolumeScale());
		table.addField(field_timeFmt(period, FIELD_BAR_TIME, FIELD_BAR_TIME_FMT, "Time fmt"));

		table.getField(FIELD_BAR_TIME).setPrimaryKey(true);
		table.setPersistor(
			new DBPersistor(MLT.getDBEngine(), table.getComplexView(table.getPrimaryKey())));

		return table;
	}

	/**
	 * @return The tickers table.
	 */
	public static Table table_tickers() {
		Table table = new Table();
		table.setName(TABLE_TICKERS);
		table.setSchema(schema_system());

		table.addField(field_string(FIELD_SERVER_ID, 20, "Server id"));
		table.addField(field_string(FIELD_INSTRUMENT_ID, 20, "Instrument"));
		table.addField(field_string(FIELD_PERIOD_ID, 5, "Period id"));
		table.addField(field_string(FIELD_TABLE_NAME, 30, "Table name"));

		table.getField(FIELD_SERVER_ID).setPrimaryKey(true);
		table.getField(FIELD_INSTRUMENT_ID).setPrimaryKey(true);
		table.getField(FIELD_PERIOD_ID).setPrimaryKey(true);

		Table tablePeriods = table_periods();
		ForeignKey fkPeriods = new ForeignKey(false);
		fkPeriods.setLocalTable(table);
		fkPeriods.setForeignTable(tablePeriods);
		fkPeriods.add(table.getField(FIELD_PERIOD_ID), tablePeriods.getField(FIELD_PERIOD_ID));
		table.addForeignKey(fkPeriods);

		Order order = new Order();
		order.add(table.getField(FIELD_SERVER_ID));
		order.add(table.getField(FIELD_INSTRUMENT_ID));
		order.add(tablePeriods.getField(FIELD_PERIOD_UNIT_INDEX));
		order.add(tablePeriods.getField(FIELD_PERIOD_SIZE));

		table.setPersistor(new DBPersistor(MLT.getDBEngine(), table.getComplexView(order)));
		return table;
	}

	/**
	 * Access to the instrument.
	 * 
	 * @param record The record.
	 * @return The instrument.
	 */
	public static Instrument to_instrument(Record record) {
		Instrument instrument = new Instrument();
		instrument.setId(record.getValue(FIELD_INSTRUMENT_ID).getString());
		instrument.setDescription(record.getValue(FIELD_INSTRUMENT_DESC).getString());
		instrument.setPipValue(record.getValue(FIELD_INSTRUMENT_PIP_VALUE).getDouble());
		instrument.setPipScale(record.getValue(FIELD_INSTRUMENT_PIP_SCALE).getInteger());
		instrument.setTickValue(record.getValue(FIELD_INSTRUMENT_TICK_VALUE).getDouble());
		instrument.setTickScale(record.getValue(FIELD_INSTRUMENT_TICK_SCALE).getInteger());
		instrument.setVolumeScale(record.getValue(FIELD_INSTRUMENT_VOLUME_SCALE).getInteger());
		String primaryCurrency = record.getValue(FIELD_INSTRUMENT_PRIMARY_CURRENCY).getString();
		instrument.setPrimaryCurrency(Currency.getInstance(primaryCurrency));
		String secondaryCurrency = record.getValue(FIELD_INSTRUMENT_SECONDARY_CURRENCY).getString();
		instrument.setSecondaryCurrency(Currency.getInstance(secondaryCurrency));
		return instrument;
	}

	/**
	 * Access to the instrument.
	 * 
	 * @param id Instrument id.
	 * @return The instrument.
	 * @throws PersistorException
	 */
	public static Instrument to_instrument(String id) throws PersistorException {
		return to_instrument(record_instrument(id));
	}

	/**
	 * Access to the period.
	 * 
	 * @param record The period record.
	 * @return The period.
	 */
	public static Period to_period(Record record) {
		String id = record.getValue(FIELD_PERIOD_ID).getString();
		return Period.parseId(id);
	}

	/**
	 * Access to the period.
	 * 
	 * @param id The period id.
	 * @return The period.
	 * @throws PersistorException
	 */
	public static Period to_period(String id) throws PersistorException {
		return to_period(record_period(id));
	}
}
