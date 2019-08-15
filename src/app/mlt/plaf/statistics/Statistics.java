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
import com.mlt.db.Types;
import com.mlt.desktop.Option;

/**
 * Statistics descriptor.
 *
 * @author Miquel Sas
 */
public abstract class Statistics {

	/**
	 * Output descriptor.
	 */
	public static class Output {
		/** Id. */
		private String id;
		/** Description. */
		private String description;
		/** Type. */
		private Types type;

		/**
		 * Constructor.
		 * 
		 * @param id          Id.
		 * @param description Description.
		 * @param type        Data type.
		 */
		public Output(String id, String description, Types type) {
			super();
			this.id = id;
			this.description = description;
			this.type = type;
		}

		/**
		 * Return the output id.
		 * 
		 * @return The output id.
		 */
		public String getName() {
			return id;
		}

		/**
		 * Return the description.
		 * 
		 * @return The description.
		 */
		public String getDescription() {
			return description;
		}

		/**
		 * Return the data type.
		 * 
		 * @return The data type.
		 */
		public Types getType() {
			return type;
		}

	}

	/** Identifier. */
	private String id;
	/** Description. */
	private String description;

	/**
	 * Constructor.
	 */
	public Statistics() {
		super();
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
	 * Set the id.
	 * 
	 * @param id The statistics id.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Return the description.
	 * 
	 * @return The description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set the description.
	 * 
	 * @param description The description.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Return the list of options associated with the statistics. These options are
	 * expected to be suitably configured to be selected from a popup menu.
	 * 
	 * @return The list of options associated with the statistics.
	 */
	public abstract List<Option> getOptions();

	/**
	 * Return a string that stores internal parameter values to be restored later.
	 * 
	 * @return The internal parameters as a string.
	 */
	public abstract String getParameters();
	
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
	public abstract void setParameters(String parameters);
}
