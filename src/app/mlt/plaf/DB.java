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

import java.util.Currency;
import java.util.HashMap;

import com.mlt.db.Condition;
import com.mlt.db.Criteria;
import com.mlt.db.Field;
import com.mlt.db.Persistor;
import com.mlt.db.PersistorDDL;
import com.mlt.db.PersistorException;
import com.mlt.db.Record;
import com.mlt.db.RecordSet;
import com.mlt.db.Table;
import com.mlt.db.Value;
import com.mlt.db.rdbms.DBPersistorDDL;
import com.mlt.desktop.LookupRecords;
import com.mlt.mkt.data.Instrument;
import com.mlt.mkt.data.Period;

import app.mlt.plaf.db.Fields;
import app.mlt.plaf.db.tables.TableTicker;
import app.mlt.plaf.db.tables.TableInstruments;
import app.mlt.plaf.db.tables.TablePeriods;
import app.mlt.plaf.db.tables.TableServers;
import app.mlt.plaf.db.tables.TableStatistics;
import app.mlt.plaf.db.tables.TableTickers;

/**
 * Statically centralizes access to lookups, persistors, records, recordsets,
 * etc.
 *
 * @author Miquel Sas
 */
public class DB {

	public static final String INSTRUMENTS = "instruments";
	public static final String PERIODS = "periods";
	public static final String SERVERS = "servers";
	public static final String STATISTICS = "statistics";
	public static final String TICKERS = "tickers";

	/** Map of tables. */
	private static HashMap<String, Table> tables = new HashMap<>();
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
	 * Access the instruments table.
	 * 
	 * @return The table.
	 */
	public static TableInstruments table_instruments() {
		TableInstruments table = (TableInstruments) tables.get(INSTRUMENTS);
		if (table == null) {
			table = new TableInstruments();
			tables.put(INSTRUMENTS, table);
		}
		return table;
	}

	/**
	 * Access the periods table.
	 * 
	 * @return The table.
	 */
	public static TablePeriods table_periods() {
		TablePeriods table = (TablePeriods) tables.get(PERIODS);
		if (table == null) {
			table = new TablePeriods();
			tables.put(PERIODS, table);
		}
		return table;
	}

	/**
	 * Access the servers table.
	 * 
	 * @return The table.
	 */
	public static TableServers table_servers() {
		TableServers table = (TableServers) tables.get(SERVERS);
		if (table == null) {
			table = new TableServers();
			tables.put(SERVERS, table);
		}
		return table;
	}

	/**
	 * Access the statistics table.
	 * 
	 * @return The table.
	 */
	public static TableStatistics table_statistics() {
		TableStatistics table = (TableStatistics) tables.get(STATISTICS);
		if (table == null) {
			table = new TableStatistics();
			tables.put(STATISTICS, table);
		}
		return table;
	}

	/**
	 * Access to the price table.
	 * 
	 * @param instrument Instrument.
	 * @param period     Period.
	 * @return The table.
	 */
	public static TableTicker table_ticker(Instrument instrument, Period period) {
		String name = name_ticker(instrument, period);
		TableTicker table = (TableTicker) tables.get(name);
		if (table == null) {
			table = new TableTicker(instrument, period);
			tables.put(name, table);
		}
		return table;
	}

	/**
	 * Access the tickers table.
	 * 
	 * @return The table.
	 */
	public static TableTickers table_tickers() {
		TableTickers table = (TableTickers) tables.get(TICKERS);
		if (table == null) {
			table = new TableTickers();
			tables.put(TICKERS, table);
		}
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
