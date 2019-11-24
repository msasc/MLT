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
	 * Calculator to display the parameters description..
	 */
	static class ParamsDesc implements Calculator {
		@Override
		public Value getValue(Record record) {
			String id = record.getValue(Fields.STATISTICS_ID).toString();
			String params = record.getValue(Fields.STATISTICS_PARAMS).toString();
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

	public static final String INSTRUMENTS = "instruments";
	public static final String PERIODS = "periods";
	public static final String SERVERS = "servers";
	public static final String STATISTICS = "statistics";
	public static final String TICKERS = "tickers";

	/** DDL. */
	private static PersistorDDL ddl;

	/**
	 * Return the proper persistor DDL.
	 * 
	 * @return The persistor DDL.
	 */
	public static PersistorDDL ddl() {
		if (ddl == null) {
			ddl = new DBPersistorDDL(MLT.getDBEngine());
		}
		return ddl;
	}

	/**
	 * Lookup an instrument.
	 * 
	 * @return The selected instrument record.
	 * @throws PersistorException
	 */
	public static Record lookup_instrument() throws PersistorException {
		LookupRecords lookup = new LookupRecords(persistor_instruments().getDefaultRecord());
		lookup.addColumn(Fields.INSTRUMENT_ID);
		lookup.addColumn(Fields.INSTRUMENT_DESC);
		lookup.addColumn(Fields.INSTRUMENT_PIP_VALUE);
		lookup.addColumn(Fields.INSTRUMENT_PIP_SCALE);
		lookup.addColumn(Fields.INSTRUMENT_TICK_VALUE);
		lookup.addColumn(Fields.INSTRUMENT_TICK_SCALE);
		lookup.addColumn(Fields.INSTRUMENT_VOLUME_SCALE);
		lookup.addColumn(Fields.INSTRUMENT_PRIMARY_CURRENCY);
		lookup.addColumn(Fields.INSTRUMENT_SECONDARY_CURRENCY);
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
		lookup.addColumn(Fields.PERIOD_ID);
		lookup.addColumn(Fields.PERIOD_NAME);
		lookup.addColumn(Fields.PERIOD_SIZE);
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
		lookup.addColumn(Fields.INSTRUMENT_ID);
		lookup.addColumn(Fields.PERIOD_ID);
		lookup.addColumn(Fields.PERIOD_NAME);
		lookup.addColumn(Fields.PERIOD_SIZE);
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
		record.setValue(Fields.SERVER_ID, vSERVER_ID);
		record.setValue(Fields.INSTRUMENT_ID, vINSTRUMENT_ID);
		record.setValue(Fields.INSTRUMENT_DESC, vINSTRUMENT_DESC);
		record.setValue(Fields.INSTRUMENT_PIP_VALUE, vINSTRUMENT_PIP_VALUE);
		record.setValue(Fields.INSTRUMENT_PIP_SCALE, vINSTRUMENT_PIP_SCALE);
		record.setValue(Fields.INSTRUMENT_TICK_VALUE, vINSTRUMENT_TICK_VALUE);
		record.setValue(Fields.INSTRUMENT_TICK_SCALE, vINSTRUMENT_TICK_SCALE);
		record.setValue(Fields.INSTRUMENT_VOLUME_SCALE, vINSTRUMENT_VOLUME_SCALE);
		record.setValue(Fields.INSTRUMENT_PRIMARY_CURRENCY, vINSTRUMENT_PRIMARY_CURRENCY);
		record.setValue(Fields.INSTRUMENT_SECONDARY_CURRENCY, vINSTRUMENT_SECONDARY_CURRENCY);
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
		record.setValue(Fields.SERVER_ID, new Value(MLT.getServer().getId()));
		record.setValue(Fields.INSTRUMENT_ID, new Value(id));
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
		record.setValue(Fields.PERIOD_ID, new Value(id));
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
		record.setValue(Fields.SERVER_ID, new Value(MLT.getServer().getId()));
		record.setValue(Fields.INSTRUMENT_ID, new Value(instrument.getId()));
		record.setValue(Fields.PERIOD_ID, new Value(period.getId()));
		record.setValue(Fields.TABLE_NAME, new Value(name_ticker(instrument, period)));
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
		Field field = persistor.getField(Fields.SERVER_ID);
		Value value = new Value(MLT.getServer().getId());
		criteria.add(Condition.fieldEQ(field, value));
		RecordSet recordSet = persistor.select(criteria);

		/* Track max pip and tick scale to set their values decimals. */
		int maxPipScale = 0;
		int maxTickScale = 0;
		for (int i = 0; i < recordSet.size(); i++) {
			Record record = recordSet.get(i);
			int pipScale = record.getValue(Fields.INSTRUMENT_PIP_SCALE).getInteger();
			int tickScale = record.getValue(Fields.INSTRUMENT_TICK_SCALE).getInteger();
			maxPipScale = Math.max(maxPipScale, pipScale);
			maxTickScale = Math.max(maxTickScale, tickScale);
		}
		recordSet.getField(Fields.INSTRUMENT_PIP_VALUE).setDisplayDecimals(maxPipScale);
		recordSet.getField(Fields.INSTRUMENT_TICK_VALUE).setDisplayDecimals(maxTickScale);
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
		Field field = persistor.getField(Fields.SERVER_ID);
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
		Field field = persistor.getField(Fields.SERVER_ID);
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
		table.setName(DB.INSTRUMENTS);
		table.setSchema(DB.schema_system());

		table.addField(Fields.getString(Fields.SERVER_ID, 20, "Server id"));
		table.addField(Fields.getString(Fields.INSTRUMENT_ID, 20, "Instrument"));
		table.addField(Fields.getString(Fields.INSTRUMENT_DESC, 120, "Instrument description"));
		table.addField(Fields.getDouble(Fields.INSTRUMENT_PIP_VALUE, "Pip value"));
		table.addField(Fields.getInteger(Fields.INSTRUMENT_PIP_SCALE, "Pip scale"));
		table.addField(Fields.getDouble(
			Fields.INSTRUMENT_TICK_VALUE, "Tick value", "Instrument tick value"));
		table.addField(Fields.getInteger(
			Fields.INSTRUMENT_TICK_SCALE, "Tick scale", "Instrument tick scale"));
		table.addField(Fields.getInteger(
			Fields.INSTRUMENT_VOLUME_SCALE, "Volume scale", "Instrument volume scale"));
		table.addField(Fields.getString(
			Fields.INSTRUMENT_PRIMARY_CURRENCY, 6, "P-Currency", "Primary currency"));
		table.addField(Fields.getString(
			Fields.INSTRUMENT_SECONDARY_CURRENCY, 6, "S-Currency", "Secondary currency"));

		table.getField(Fields.SERVER_ID).setPrimaryKey(true);
		table.getField(Fields.INSTRUMENT_ID).setPrimaryKey(true);

		table.setPersistor(
			new DBPersistor(MLT.getDBEngine(), table.getComplexView(table.getPrimaryKey())));

		return table;
	}

	/**
	 * @return The periods table.
	 */
	public static Table table_periods() {
		Table table = new Table();
		table.setName(DB.PERIODS);
		table.setSchema(DB.schema_system());

		table.addField(Fields.getString(Fields.PERIOD_ID, 5, "Period id"));
		table.addField(Fields.getString(Fields.PERIOD_NAME, 15, "Period name"));
		table.addField(Fields.getInteger(Fields.PERIOD_UNIT_INDEX, "Period unit index"));
		table.addField(Fields.getInteger(Fields.PERIOD_SIZE, "Period size"));

		table.getField(Fields.PERIOD_ID).setPrimaryKey(true);

		Order order = new Order();
		order.add(table.getField(Fields.PERIOD_UNIT_INDEX));
		order.add(table.getField(Fields.PERIOD_SIZE));

		table.setPersistor(new DBPersistor(MLT.getDBEngine(), table.getComplexView(order)));

		return table;
	}

	/**
	 * @return The servers table.
	 */
	public static Table table_servers() {
		Table table = new Table();
		table.setName(DB.SERVERS);
		table.setSchema(DB.schema_system());

		table.addField(Fields.getString(Fields.SERVER_ID, 20, "Server id"));
		table.addField(Fields.getString(Fields.SERVER_NAME, 60, "Server name"));
		table.addField(Fields.getString(Fields.SERVER_TITLE, 120, "Server title"));

		table.getField(Fields.SERVER_ID).setPrimaryKey(true);

		table.setPersistor(
			new DBPersistor(MLT.getDBEngine(), table.getComplexView(table.getPrimaryKey())));

		return table;
	}

	/**
	 * @return The statistics table.
	 */
	public static Table table_statistics() {
		Table table = new Table();
		table.setName(DB.STATISTICS);
		table.setSchema(DB.schema_system());

		table.addField(Fields.getString(Fields.SERVER_ID, 20, "Server id"));
		table.addField(Fields.getString(Fields.INSTRUMENT_ID, 20, "Instrument"));
		table.addField(Fields.getString(Fields.PERIOD_ID, 5, "Period id"));

		table.addField(Fields.getString(Fields.STATISTICS_ID, 5, "Id", "Statistics id"));
		table.getField(Fields.STATISTICS_ID).addPossibleValue("AVG", "Averages");

		table.addField(Fields.getString(Fields.STATISTICS_KEY, 2, "Key", "Statistics key"));

		Field params =
			Fields.getString(
				Fields.STATISTICS_PARAMS,
				Types.FIXED_LENGTH * 10,
				"Statistics params");
		TextArea textArea = new TextArea();
		textArea.setPreferredSize(new Dimension(600, 300));
		textArea.setFont(new Font("Courier", Font.PLAIN, 14));
		params.getProperties().setObject(EditContext.EDIT_FIELD, textArea);
		params.getProperties().setObject(EditContext.FILL, Fill.BOTH);
		table.addField(params);

		Field paramsDesc =
			Fields.getString(
				Fields.STATISTICS_PARAMS_DESC,
				1024,
				"Parameters description");
		paramsDesc.setPersistent(false);
		paramsDesc.setCalculator(new ParamsDesc());
		table.addField(paramsDesc);

		table.getField(Fields.SERVER_ID).setPrimaryKey(true);
		table.getField(Fields.INSTRUMENT_ID).setPrimaryKey(true);
		table.getField(Fields.PERIOD_ID).setPrimaryKey(true);
		table.getField(Fields.STATISTICS_ID).setPrimaryKey(true);
		table.getField(Fields.STATISTICS_KEY).setPrimaryKey(true);

		Table tablePeriods = DB.table_periods();
		ForeignKey fkPeriods = new ForeignKey(false);
		fkPeriods.setLocalTable(table);
		fkPeriods.setForeignTable(tablePeriods);
		fkPeriods.add(table.getField(Fields.PERIOD_ID),
			tablePeriods.getField(Fields.PERIOD_ID));
		table.addForeignKey(fkPeriods);

		table.setPersistor(
			new DBPersistor(MLT.getDBEngine(), table.getComplexView(table.getPrimaryKey())));

		return table;
	}

	/**
	 * @param instrument Instrument.
	 * @param period     Period.
	 * @return The ticker table.
	 */
	public static Table table_ticker(Instrument instrument, Period period) {
		Table table = new Table();
		table.setName(DB.name_ticker(instrument, period));
		table.setSchema(DB.schema_server());

		table.addField(Fields.getLong(Fields.BAR_TIME, "Time"));
		table.addField(Fields.getData(instrument, Fields.BAR_OPEN, "Open"));
		table.addField(Fields.getData(instrument, Fields.BAR_HIGH, "High"));
		table.addField(Fields.getData(instrument, Fields.BAR_LOW, "Low"));
		table.addField(Fields.getData(instrument, Fields.BAR_CLOSE, "Close"));
		table.addField(Fields.getDouble(Fields.BAR_VOLUME, "Volume"));
		table.getField(Fields.BAR_VOLUME).setDecimals(instrument.getVolumeScale());
		table.addField(
			Fields.getTimeFmt(period, Fields.BAR_TIME, Fields.BAR_TIME_FMT, "Time fmt"));

		table.getField(Fields.BAR_TIME).setPrimaryKey(true);
		table.setPersistor(
			new DBPersistor(MLT.getDBEngine(), table.getComplexView(table.getPrimaryKey())));

		return table;
	}

	/**
	 * @return The tickers table.
	 */
	public static Table table_tickers() {
		Table table = new Table();
		table.setName(DB.TICKERS);
		table.setSchema(DB.schema_system());

		table.addField(Fields.getString(Fields.SERVER_ID, 20, "Server id"));
		table.addField(Fields.getString(Fields.INSTRUMENT_ID, 20, "Instrument"));
		table.addField(Fields.getString(Fields.PERIOD_ID, 5, "Period id"));
		table.addField(Fields.getString(Fields.TABLE_NAME, 30, "Table name"));

		table.getField(Fields.SERVER_ID).setPrimaryKey(true);
		table.getField(Fields.INSTRUMENT_ID).setPrimaryKey(true);
		table.getField(Fields.PERIOD_ID).setPrimaryKey(true);

		Table tablePeriods = DB.table_periods();
		ForeignKey fkPeriods = new ForeignKey(false);
		fkPeriods.setLocalTable(table);
		fkPeriods.setForeignTable(tablePeriods);
		fkPeriods.add(
			table.getField(Fields.PERIOD_ID),
			tablePeriods.getField(Fields.PERIOD_ID));
		table.addForeignKey(fkPeriods);

		Order order = new Order();
		order.add(table.getField(Fields.SERVER_ID));
		order.add(table.getField(Fields.INSTRUMENT_ID));
		order.add(tablePeriods.getField(Fields.PERIOD_UNIT_INDEX));
		order.add(tablePeriods.getField(Fields.PERIOD_SIZE));

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
		instrument.setId(record.getValue(Fields.INSTRUMENT_ID).getString());
		instrument.setDescription(record.getValue(Fields.INSTRUMENT_DESC).getString());
		instrument.setPipValue(record.getValue(Fields.INSTRUMENT_PIP_VALUE).getDouble());
		instrument.setPipScale(record.getValue(Fields.INSTRUMENT_PIP_SCALE).getInteger());
		instrument.setTickValue(record.getValue(Fields.INSTRUMENT_TICK_VALUE).getDouble());
		instrument.setTickScale(record.getValue(Fields.INSTRUMENT_TICK_SCALE).getInteger());
		instrument.setVolumeScale(record.getValue(Fields.INSTRUMENT_VOLUME_SCALE).getInteger());
		String primaryCurrency = record.getValue(Fields.INSTRUMENT_PRIMARY_CURRENCY).getString();
		instrument.setPrimaryCurrency(Currency.getInstance(primaryCurrency));
		String secondaryCurrency =
			record.getValue(Fields.INSTRUMENT_SECONDARY_CURRENCY).getString();
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
		String id = record.getValue(Fields.PERIOD_ID).getString();
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
