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

import java.util.Currency;
import java.util.HashMap;

import com.mlt.db.Condition;
import com.mlt.db.Criteria;
import com.mlt.db.Field;
import com.mlt.db.Persistor;
import com.mlt.db.PersistorException;
import com.mlt.db.Record;
import com.mlt.db.RecordSet;
import com.mlt.db.Table;
import com.mlt.db.Value;
import com.mlt.desktop.LookupRecords;
import com.mlt.mkt.data.Instrument;
import com.mlt.mkt.data.Period;

import app.mlt.plaf.MLT;
import app.mlt.plaf.db.tables.TableDataPrice;
import app.mlt.plaf.db.tables.TableInstruments;
import app.mlt.plaf.db.tables.TablePeriods;
import app.mlt.plaf.db.tables.TableServers;
import app.mlt.plaf.db.tables.TableStatistics;
import app.mlt.plaf.db.tables.TableTickers;

/**
 * Provide access to the database objects.
 *
 * @author Miquel Sas
 */
public class Database {
	/**
	 * Lookup provider.
	 */
	public class LookupProvider {
		/**
		 * Lookup an instrument.
		 * 
		 * @return The selected instrument record.
		 * @throws PersistorException
		 */
		public Record instrument() throws PersistorException {
			LookupRecords lookup = new LookupRecords(persistor().instrument().getDefaultRecord());
			lookup.addColumn(Fields.INSTRUMENT_ID);
			lookup.addColumn(Fields.INSTRUMENT_DESC);
			lookup.addColumn(Fields.INSTRUMENT_PIP_VALUE);
			lookup.addColumn(Fields.INSTRUMENT_PIP_SCALE);
			lookup.addColumn(Fields.INSTRUMENT_TICK_VALUE);
			lookup.addColumn(Fields.INSTRUMENT_TICK_SCALE);
			lookup.addColumn(Fields.INSTRUMENT_VOLUME_SCALE);
			lookup.addColumn(Fields.INSTRUMENT_PRIMARY_CURRENCY);
			lookup.addColumn(Fields.INSTRUMENT_SECONDARY_CURRENCY);
			lookup.setRecordSet(recordSet().instrument());
			Record record = lookup.lookupRecord();
			return record;
		}

		/**
		 * Lookup a period.
		 * 
		 * @return The selected period.
		 * @throws PersistorException
		 */
		public Record period() throws PersistorException {
			LookupRecords lookup = new LookupRecords(persistor().period().getDefaultRecord());
			lookup.addColumn(Fields.PERIOD_ID);
			lookup.addColumn(Fields.PERIOD_NAME);
			lookup.addColumn(Fields.PERIOD_SIZE);
			lookup.setRecordSet(recordSet().period());
			Record record = lookup.lookupRecord();
			return record;
		}
	}
	
	/**
	 * Persistor provider.
	 */
	public class PersistorProvider {
		/**
		 * Returns the data price persistor.
		 * 
		 * @param instrument The instrument.
		 * @param period     The period.
		 * @return The persistor.
		 */
		public Persistor price(Instrument instrument, Period period) {
			return table().price(instrument, period).getPersistor();
		}

		/**
		 * Returns the instruments persistor.
		 * 
		 * @return The persistor.
		 */
		public Persistor instrument() {
			return table().instrument().getPersistor();
		}

		/**
		 * Returns the periods persistor.
		 * 
		 * @return The persistor.
		 */
		public Persistor period() {
			return table().period().getPersistor();
		}

		/**
		 * Returns the servers persistor.
		 * 
		 * @return The persistor.
		 */
		public Persistor server() {
			return table().server().getPersistor();
		}

		/**
		 * Returns the statistics persistor.
		 * 
		 * @return The persistor.
		 */
		public Persistor statistic() {
			return table().statistic().getPersistor();
		}

		/**
		 * Returns the tickers persistor.
		 * 
		 * @return The persistor.
		 */
		public Persistor ticker() {
			return table().ticker().getPersistor();
		}
	}

	/**
	 * Record provider.
	 */
	public class RecordProvider {
		/**
		 * Return the ticker record.
		 * 
		 * @param instrument Instrument.
		 * @param period     Period.
		 * @return The record.
		 */
		public Record ticker(Instrument instrument, Period period) {
			Record record = persistor().ticker().getDefaultRecord();
			record.setValue(Fields.SERVER_ID, new Value(MLT.getServer().getId()));
			record.setValue(Fields.INSTRUMENT_ID, new Value(instrument.getId()));
			record.setValue(Fields.PERIOD_ID, new Value(period.getId()));
			record.setValue(Fields.TABLE_NAME, new Value(getName_Ticker(instrument, period)));
			return record;
		}

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

			Record record = persistor().instrument().getDefaultRecord();
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
			Persistor persistor = persistor().instrument();
			Record record = persistor.getDefaultRecord();
			record.setValue(Fields.SERVER_ID, new Value(MLT.getServer().getId()));
			record.setValue(Fields.INSTRUMENT_ID, new Value(id));
			persistor.refresh(record);
			return record;
		}
	}

	/**
	 * Recordsets provider.
	 */
	public class RecordSetProvider {
		/**
		 * Returns the data price persistor.
		 * 
		 * @return The persistor.
		 */
		public Persistor price(Instrument instrument, Period period) {
			return table().price(instrument, period).getPersistor();
		}

		/**
		 * Returns a record set with the available instruments for the argument server.
		 * 
		 * @return The record set.
		 * @throws PersistorException If any persistence error occurs.
		 */
		public RecordSet instrument() throws PersistorException {

			Persistor persistor = persistor().instrument();
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
		public RecordSet period() throws PersistorException {
			Persistor persistor = persistor().period();
			RecordSet recordSet = persistor.select((Criteria) null);
			return recordSet;
		}

		/**
		 * Returns a record set with the defined statistics for the argument server.
		 * 
		 * @return The record set.
		 * @throws PersistorException If any persistence error occurs.
		 */
		public RecordSet statistic() throws PersistorException {
			Persistor persistor = persistor().statistic();
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
		public RecordSet ticker() throws PersistorException {
			Persistor persistor = persistor().ticker();
			Field field = persistor.getField(Fields.SERVER_ID);
			Value value = new Value(MLT.getServer().getId());
			Criteria criteria = new Criteria();
			criteria.add(Condition.fieldEQ(field, value));
			RecordSet recordSet = persistor.select(criteria);
			return recordSet;
		}
	}

	/**
	 * Tables provider.
	 */
	public class TableProvider {
		/**
		 * Access to the price table.
		 * 
		 * @param server     Server.
		 * @param instrument Instrument.
		 * @param period     Period.
		 * @return The table.
		 */
		public TableDataPrice price(Instrument instrument, Period period) {
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
		public TableInstruments instrument() {
			TableInstruments table = (TableInstruments) mapTables.get(INSTRUMENTS);
			if (table == null) {
				table = new TableInstruments();
				mapTables.put(INSTRUMENTS, table);
			}
			return table;
		}

		/**
		 * Access the periods table.
		 * 
		 * @return The table.
		 */
		public TablePeriods period() {
			TablePeriods table = (TablePeriods) mapTables.get(PERIODS);
			if (table == null) {
				table = new TablePeriods();
				mapTables.put(PERIODS, table);
			}
			return table;
		}

		/**
		 * Access the servers table.
		 * 
		 * @return The table.
		 */
		public TableServers server() {
			TableServers table = (TableServers) mapTables.get(SERVERS);
			if (table == null) {
				table = new TableServers();
				mapTables.put(SERVERS, table);
			}
			return table;
		}

		/**
		 * Access the statistics table.
		 * 
		 * @return The table.
		 */
		public TableStatistics statistic() {
			TableStatistics table = (TableStatistics) mapTables.get(STATISTICS);
			if (table == null) {
				table = new TableStatistics();
				mapTables.put(STATISTICS, table);
			}
			return table;
		}

		/**
		 * Access the tickers table.
		 * 
		 * @return The table.
		 */
		public TableTickers ticker() {
			TableTickers table = (TableTickers) mapTables.get(TICKERS);
			if (table == null) {
				table = new TableTickers();
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

	/** Lookup provider. */
	private LookupProvider lookup;
	/** Persistors provider. */
	private PersistorProvider persistor;
	/** Records provider. */
	private RecordProvider record;
	/** Recordsets provider. */
	private RecordSetProvider recordSet;
	/** Tables provider. */
	private TableProvider table;
	
	/** Map of tables. */
	private HashMap<String, Table> mapTables = new HashMap<>();

	/**
	 * Constructor.
	 */
	public Database() {
		super();
		this.lookup = new LookupProvider();
		this.persistor = new PersistorProvider();
		this.record = new RecordProvider();
		this.recordSet = new RecordSetProvider();
		this.table = new TableProvider();
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

	public LookupProvider lookup() {
		return lookup;
	}
	
	public PersistorProvider persistor() {
		return persistor;
	}

	public RecordProvider record() {
		return record;
	}

	public RecordSetProvider recordSet() {
		return recordSet;
	}

	public TableProvider table() {
		return table;
	}
}
