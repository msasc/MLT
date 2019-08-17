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

import com.mlt.db.rdbms.DBEngine;
import com.mlt.mkt.data.Instrument;
import com.mlt.mkt.data.Period;
import com.mlt.mkt.server.Server;

import app.mlt.plaf.MLT;

/**
 * Statistics on tickers.
 *
 * @author Miquel Sas
 */
public abstract class StatisticsTicker extends Statistics {

	/** Instrument. */
	private Instrument instrument;
	/** Period. */
	private Period period;

	/**
	 * Constructor.
	 * 
	 * @param instrument Instrument.
	 * @param period     Period.
	 */
	public StatisticsTicker(Instrument instrument, Period period) {
		super();
		this.instrument = instrument;
		this.period = period;
	}

	/**
	 * Sortcut to access the database engine.
	 * 
	 * @return The database engine.
	 */
	public DBEngine getDBEngine() {
		return MLT.getDatabase().getDBEngine();
	}

	/**
	 * Return the instrument.
	 * 
	 * @return The instrument.
	 */
	public Instrument getInstrument() {
		return instrument;
	}

	/**
	 * Return the period.
	 * 
	 * @return The period.
	 */
	public Period getPeriod() {
		return period;
	}

	/**
	 * Sortcut to access the server.
	 * 
	 * @return The server.
	 */
	public Server getServer() {
		return MLT.getServer();
	}

}
