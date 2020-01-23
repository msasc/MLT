/*
 * Copyright (C) 2015 Miquel Sas
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.mlt.ml.data;

import com.mlt.util.Properties;

/**
 * A pattern, with inputs and optional expected label and outputs.
 *
 * @author Miquel Sas
 */
public abstract class Pattern {

	/** Optional label. */
	private String label;
	/** Optional properties of any kind. */
	private Properties properties;

	/**
	 * Return the pattern input values.
	 * 
	 * @return The pattern input values.
	 */
	public abstract double[] getInputValues();

	/**
	 * Return the optional pattern output values.
	 * 
	 * @return The pattern output values.
	 */
	public abstract double[] getOutputValues();

	/**
	 * Return the optional label.
	 * 
	 * @return The label.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Return the optional properties. If this method is not called, the properties bag is not created.
	 * 
	 * @return The properties.
	 */
	public Properties getProperties() {
		if (properties == null) properties = new Properties();
		return properties;
	}

	/**
	 * Set the label.
	 * 
	 * @param label The label.
	 */
	public void setLabel(String label) {
		this.label = label;
	}
}
