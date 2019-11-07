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

import java.util.List;

import com.mlt.db.Table;
import com.mlt.desktop.Option;
import com.mlt.mkt.data.Instrument;
import com.mlt.mkt.data.Period;

/**
 * Statistics on tickers.
 *
 * @author Miquel Sas
 */
public abstract class Statistics {

	/**
	 * Identifier that indicates the class of statistics. For instance, staticstics
	 * over averages could have a "AVG" identifier.
	 */
	private String id;
	/**
	 * A key to classify statistics within the same identifier. A kind of
	 * subclasses.
	 */
	private String key;
	/**
	 * Instrument.
	 */
	private Instrument instrument;
	/**
	 * Period.
	 */
	private Period period;

	/**
	 * Constructor.
	 * 
	 * @param instrument Instrument.
	 * @param period     Period.
	 */
	public Statistics(Instrument instrument, Period period) {
		super();
		this.instrument = instrument;
		this.period = period;
	}

	/**
	 * Return the id.
	 * 
	 * @return The id.
	 */
	public String getId() {
		return id;
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
	 * Return the key or subclass.
	 * 
	 * @return The key.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Returns a legend description of the statistics, with description of
	 * paramemters, used tables, menu options offered, and any internal details of
	 * interest.
	 * <p>
	 * Recomended to use HTML format.
	 * 
	 * @return The statistics legend or summary.
	 */
	public abstract String getLegend();

	/**
	 * Return the list of options associated with the statistics. These options are
	 * expected to be suitably configured to be selected from a popup menu.
	 * 
	 * @return The list of options associated with the statistics.
	 */
	public abstract List<Option> getOptions();

	/**
	 * Return a string that stores internal parameter values to be restored later,
	 * generally in an XML format.
	 * 
	 * @return The internal parameters as a string.
	 */
	public abstract String getParameters();

	/**
	 * Return the parameters description in a more readable form.
	 * 
	 * @return The parameters description.
	 */
	public abstract String getParametersDescription();

	/**
	 * Return the period.
	 * 
	 * @return The period.
	 */
	public Period getPeriod() {
		return period;
	}

	/**
	 * Set the id.
	 * 
	 * @param id The statistics id.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Set the key or subclass.
	 * 
	 * @param key The key.
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * Returns the list of tables where statistic results are stored.
	 * 
	 * @return The list of result tables.
	 */
	public abstract List<Table> getTables();

	/**
	 * Set the string with internal parameters stored.
	 * 
	 * @param parameters The parameters (XML).
	 */
	public abstract void setParameters(String parameters) throws Exception;

	/**
	 * Validate the statistics internal parameters.
	 * 
	 * @throws Exception
	 */
	public abstract void validate() throws Exception;
}
