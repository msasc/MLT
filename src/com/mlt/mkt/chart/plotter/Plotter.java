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
package com.mlt.mkt.chart.plotter;

import com.mlt.mkt.chart.DataContext;
import com.mlt.util.Properties;

/**
 * Base abstract class of all plotter subclasses. Note that the frame for the
 * plot data must have been calculated by a
 * call to <i>PlotData.calculateFrame</i> prior to any plot operation, except
 * for those repaints in a small clip bounds
 * that do not modify the frame maximum and minimum.
 * <p>
 * This base plotter primarily offers the context that has methods to calculate
 * coordinates from values and values from
 * coordinates.
 * <p>
 * Also offers a set of static plot utilities.
 * 
 * @author Miquel Sas
 */
public class Plotter {

	/** Identifier. */
	private String id;
	/** Description. */
	private String description;
	/** The plotter context. */
	private DataContext context;
	/** User properties. */
	private Properties properties = new Properties();

	/**
	 * Constructor.
	 */
	public Plotter() {
		super();
		this.id = getClass().getSimpleName();
		this.description = getClass().getSimpleName();
	}

	/**
	 * Equals if it is exactly the same class and has the same id.
	 */
	@Override
	public boolean equals(Object obj) {
		if (getClass().getName().equals(obj.getClass().getName())) {
			Plotter plotter = (Plotter) obj;
			if (getId().equals(plotter.getId())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return The description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return The string identifier.
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return The plotter context.
	 */
	public DataContext getContext() {
		return context;
	}

	/**
	 * @param context The plotter context.
	 */
	public void setContext(DataContext context) {
		this.context = context;
	}

	/**
	 * @return The user properties.
	 */
	public Properties getProperties() {
		return properties;
	}

	/**
	 * @param description The description.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param id The string identifier.
	 */
	public void setId(String id) {
		this.id = id;
	}
}
