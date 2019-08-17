/*
 * Copyright (C) 2018 Miquel Sas
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package app.mlt.plaf.db;

import java.util.Currency;
import java.util.HashMap;

import app.mlt.plaf.MLT;
import app.mlt.plaf.db.tables.TableDataPrice;
import app.mlt.plaf.db.tables.TableInstruments;
import app.mlt.plaf.db.tables.TablePeriods;
import app.mlt.plaf.db.tables.TableServers;
import app.mlt.plaf.db.tables.TableStatistics;
import app.mlt.plaf.db.tables.TableTickers;
import com.mlt.db.Condition;
import com.mlt.db.Criteria;
import com.mlt.db.Persistor;
import com.mlt.db.PersistorDDL;
import com.mlt.db.PersistorException;
import com.mlt.db.Record;
import com.mlt.db.RecordSet;
import com.mlt.db.Table;
import com.mlt.db.Value;
import com.mlt.db.rdbms.DBEngine;
import com.mlt.db.rdbms.DBPersistorDDL;
import com.mlt.desktop.LookupRecords;
import com.mlt.mkt.data.Instrument;
import com.mlt.mkt.data.Period;
import com.mlt.mkt.server.Server;

/**
 * Provide access to the database objects.
 *
 * @author Miquel Sas
 */
public class Database {

	/**
	 * Persistor provider.
	 */
	public class Persistors {

	}

	/**
	 * Record provider.
	 */
	public class Records {
		/**
		 * Returns the filled record for the instrument.
		 * 
		 * @param instrument The instrument.
		 * @return The record.
		 */
		public Record instrument(Instrument instrument) {

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

			Record record = getPersistor_Instruments().getDefaultRecord();
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
		 * @param instrument The instrument.
		 * @return The record.
		 */
		public Record instrument(String id) throws PersistorException {
			Persistor persistor = getPersistor_Instruments();
			Record record = getPersistor_Instruments().getDefaultRecord();
			record.setValue(Fields.SERVER_ID, new Value(MLT.getServer().getId()));
			record.setValue(Fields.INSTRUMENT_ID, new Value(id));
			persistor.refresh(record);
			return record;
		}

	}

	/**
	 * Recordsets provider.
	 */
	public class RecordSets {

	}

	/**
	 * Tables provider.
	 */
	public class Tables {
		/**
		 * Access to the price table.
		 * 
		 * @param server     Server.
		 * @param instrument Instrument.
		 * @param period     Period.
		 * @return The table.
		 */
		public TableDataPrice prices(Instrument instrument, Period period) {
			String name = getName_Ticker(instrument, period);
			TableDataPrice table = (TableDataPrice) mapTables.get(name);
			if (table == null) {
				table = new TableDataPrice(instrument, period);
				mapTables.put(name, table);
			}
			return table;
		}

		/**
		 * Access the instruments table.
		 * 
		 * @return The table.
		 */
		public TableInstruments instruments() {
			TableInstruments table = (TableInstruments) mapTables.get(INSTRUMENTS);
			if (table == null) {
				table = new TableInstruments(dbEngine);
				mapTables.put(INSTRUMENTS, table);
			}
			return table;
		}

		/**
		 * Access the periods table.
		 * 
		 * @return The table.
		 */
		public TablePeriods periods() {
			TablePeriods table = (TablePeriods) mapTables.get(PERIODS);
			if (table == null) {
				table = new TablePeriods(dbEngine);
				mapTables.put(PERIODS, table);
			}
			return table;
		}

		/**
		 * Access the servers table.
		 * 
		 * @return The table.
		 */
		public TableServers servers() {
			TableServers table = (TableServers) mapTables.get(SERVERS);
			if (table == null) {
				table = new TableServers(dbEngine);
				mapTables.put(SERVERS, table);
			}
			return table;
		}

		/**
		 * Access the statistics table.
		 * 
		 * @return The table.
		 */
		public TableStatistics statistics() {
			TableStatistics table = (TableStatistics) mapTables.get(STATISTICS);
			if (table == null) {
				table = new TableStatistics(dbEngine);
				mapTables.put(STATISTICS, table);
			}
			return table;
		}

		/**
		 * Access the tickers table.
		 * 
		 * @return The table.
		 */
		public TableTickers tickers() {
			TableTickers table = (TableTickers) mapTables.get(TICKERS);
			if (table == null) {
				table = new TableTickers(dbEngine);
				mapTables.put(TICKERS, table);
			}
			return table;
		}
	}

	public static final String INSTRUMENTS = "instruments";
	public static final String PERIODS = "periods";
	public static final String SERVERS = "servers";
	public static final String STATISTICS = "statistics";
	public static final String SYSTEM_SCHEMA = "qtfx";
	public static final String TICKERS = "tickers";

	/**
	 * Return the generic ticker name.
	 * 
	 * @param instrument The instrument.
	 * @param period     The period.
	 * @param suffix     Optional suffix.
	 * @return The 'ticker' table name.
	 */
	public static String getName_Ticker(Instrument instrument, Period period, String suffix) {
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
	public static String getName_Ticker(Instrument instrument, Period period) {
		return getName_Ticker(instrument, period, null);
	}

	/**
	 * Return the schema name.
	 * 
	 * @param server The server.
	 * @return The name.
	 */
	public static String getSchema(Server server) {
		return SYSTEM_SCHEMA + "_" + server.getId();
	}

	/** DB engine. */
	private DBEngine dbEngine;
	/** Persistors provider. */
	private Persistors persistors;
	/** Records provider. */
	private Records records;
	/** Recordsets provider. */
	private RecordSets recordSets;
	/** Tables provider. */
	private Tables tables;
	/** Map of tables. */
	private HashMap<String, Table> mapTables = new HashMap<>();

	/**
	 * Constructor.
	 */
	public Database(DBEngine dbEngine) {
		super();
		this.dbEngine = dbEngine;
		this.persistors = new Persistors();
		this.records = new Records();
		this.recordSets = new RecordSets();
		this.tables = new Tables();
	}

	/**
	 * Return the conected database engine.
	 * 
	 * @return The database engine.
	 */
	public DBEngine getDBEngine() {
		return dbEngine;
	}

	/**
	 * Returns a suitable DDL.
	 * 
	 * @return The DDL.
	 */
	public PersistorDDL getDDL() {
		return new DBPersistorDDL(dbEngine);
	}

	/**
	 * Returns the data price persistor.
	 * 
	 * @return The persistor.
	 */
	public Persistor getPersistor_DataPrice(Server server, Instrument instrument, Period period) {
		return tables().prices(instrument, period).getPersistor();
	}

	/**
	 * Returns the instruments persistor.
	 * 
	 * @return The persistor.
	 */
	public Persistor getPersistor_Instruments() {
		return tables().instruments().getPersistor();
	}

	/**
	 * Returns the periods persistor.
	 * 
	 * @return The persistor.
	 */
	public Persistor getPersistor_Periods() {
		return tables().periods().getPersistor();
	}

	/**
	 * Returns the servers persistor.
	 * 
	 * @return The persistor.
	 */
	public Persistor getPersistor_Servers() {
		return tables().servers().getPersistor();
	}

	/**
	 * Returns the statistics persistor.
	 * 
	 * @return The persistor.
	 */
	public Persistor getPersistor_Statistics() {
		return tables().statistics().getPersistor();
	}

	/**
	 * Returns the tickers persistor.
	 * 
	 * @return The persistor.
	 */
	public Persistor getPersistor_Tickers() {
		return tables().tickers().getPersistor();
	}

	/**
	 * Return the ticker record.
	 * 
	 * @param server     Server.
	 * @param instrument Instrument.
	 * @param period     Period.
	 * @return The record.
	 */
	public Record getRecord_Ticker(Server server, Instrument instrument, Period period) {
		Record record = getPersistor_Tickers().getDefaultRecord();
		record.setValue(Fields.SERVER_ID, new Value(server.getId()));
		record.setValue(Fields.INSTRUMENT_ID, new Value(instrument.getId()));
		record.setValue(Fields.PERIOD_ID, new Value(period.getId()));
		record.setValue(Fields.TABLE_NAME, new Value(getName_Ticker(instrument, period)));
		return record;
	}

	/**
	 * Returns a record set with the available instruments for the argument server.
	 * 
	 * @param server The server.
	 * @return The record set.
	 * @throws PersistorException If any persistence error occurs.
	 */
	public RecordSet getRecordSet_AvailableInstruments(Server server) throws PersistorException {

		Persistor persistor = getPersistor_Instruments();
		Criteria criteria = new Criteria();
		criteria.add(
			Condition.fieldEQ(persistor.getField(Fields.SERVER_ID), new Value(server.getId())));
		RecordSet recordSet = persistor.select(criteria);

		// Track max pip and tick scale to set their values decimals.
		int maxPipScale = 0;
		int maxTickScale = 0;
		for (int i = 0; i < recordSet.size(); i++) {
			Record record = recordSet.get(i);
			maxPipScale =
				Math.max(maxPipScale, record.getValue(Fields.INSTRUMENT_PIP_SCALE).getInteger());
			maxTickScale =
				Math.max(maxTickScale, record.getValue(Fields.INSTRUMENT_TICK_SCALE).getInteger());
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
	public RecordSet getRecordSet_Periods() throws PersistorException {
		Persistor persistor = getPersistor_Periods();
		RecordSet recordSet = persistor.select((Criteria) null);
		return recordSet;
	}

	/**
	 * Returns a record set with the defined statistics for the argument server.
	 * 
	 * @param server The server.
	 * @return The record set.
	 * @throws PersistorException If any persistence error occurs.
	 */
	public RecordSet getRecordSet_Statistics(Server server) throws PersistorException {
		Persistor persistor = getPersistor_Statistics();
		Criteria criteria = new Criteria();
		criteria.add(
			Condition.fieldEQ(persistor.getField(Fields.SERVER_ID), new Value(server.getId())));
		RecordSet recordSet = persistor.select(criteria);
		return recordSet;
	}

	/**
	 * Returns a record set with the defined tickers for the argument server.
	 * 
	 * @param server The server.
	 * @return The record set.
	 * @throws PersistorException If any persistence error occurs.
	 */
	public RecordSet getRecordSet_Tickers(Server server) throws PersistorException {
		Persistor persistor = getPersistor_Tickers();
		Criteria criteria = new Criteria();
		criteria.add(
			Condition.fieldEQ(persistor.getField(Fields.SERVER_ID), new Value(server.getId())));
		RecordSet recordSet = persistor.select(criteria);
		return recordSet;
	}

	/**
	 * Lookup an instrument.
	 * 
	 * @return The selected instrument record.
	 * @throws PersistorException
	 */
	public Record lookupInstrument() throws PersistorException {
		LookupRecords lookup = new LookupRecords(getPersistor_Instruments().getDefaultRecord());
		lookup.addColumn(Fields.INSTRUMENT_ID);
		lookup.addColumn(Fields.INSTRUMENT_DESC);
		lookup.addColumn(Fields.INSTRUMENT_PIP_VALUE);
		lookup.addColumn(Fields.INSTRUMENT_PIP_SCALE);
		lookup.addColumn(Fields.INSTRUMENT_TICK_VALUE);
		lookup.addColumn(Fields.INSTRUMENT_TICK_SCALE);
		lookup.addColumn(Fields.INSTRUMENT_VOLUME_SCALE);
		lookup.addColumn(Fields.INSTRUMENT_PRIMARY_CURRENCY);
		lookup.addColumn(Fields.INSTRUMENT_SECONDARY_CURRENCY);
		lookup.setRecordSet(getRecordSet_AvailableInstruments(MLT.getServer()));
		Record record = lookup.lookupRecord();
		return record;
	}

	/**
	 * Lookup a period.
	 * 
	 * @return The selected period.
	 * @throws PersistorException
	 */
	public Record lookupPeriod() throws PersistorException {
		LookupRecords lookup = new LookupRecords(getPersistor_Periods().getDefaultRecord());
		lookup.addColumn(Fields.PERIOD_ID);
		lookup.addColumn(Fields.PERIOD_NAME);
		lookup.addColumn(Fields.PERIOD_SIZE);
		lookup.setRecordSet(getRecordSet_Periods());
		Record record = lookup.lookupRecord();
		return record;
	}

	public Instrument fromRecordToInstrument(Record record) {
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

	public Period fromRecordToPeriod(Record record) {
		String id = record.getValue(Fields.PERIOD_ID).getString();
		return Period.parseId(id);
	}

	public Persistors persistors() {
		return persistors;
	}

	public Records records() {
		return records;
	}

	public RecordSets recordSets() {
		return recordSets;
	}

	public Tables tables() {
		return tables;
	}
}
