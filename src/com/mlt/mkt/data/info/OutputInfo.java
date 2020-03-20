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
package com.mlt.mkt.data.info;

import java.util.Objects;

import com.mlt.mkt.chart.plotter.DataPlotter;

/**
 * Information about data output values.
 *
 * @author Miquel Sas
 */
public class OutputInfo {

	/**
	 * The output name, for instance <b>Close</b> for the close value of an
	 * <b>Data</b> instance.
	 */
	private String name;
	/**
	 * A short name to build a short information string, like for instance <b>C</b>
	 * for the close value of an
	 * <b>Data</b> instance.
	 */
	private String shortName;
	/**
	 * An optional description.
	 */
	private String description;
	/**
	 * The index of this output in the data object.
	 */
	private int index;
	/**
	 * Associated data plotter if any.
	 */
	private DataPlotter plotter;

	/**
	 * Default constructor.
	 */
	public OutputInfo() {
		super();
	}

	/**
	 * Constructor assigning the name, short name and description..
	 *
	 * @param name        The name.
	 * @param shortName   The short name.
	 * @param index       The data index.
	 * @param description The description.
	 */
	public OutputInfo(String name, String shortName, int index, String description) {
		super();
		this.name = name;
		this.shortName = shortName;
		this.index = index;
		this.description = description;
	}

	/**
	 * Constructor assigning the name, short name and description..
	 *
	 * @param name        The name.
	 * @param shortName   The short name.
	 * @param index       The data index.
	 * @param description The description.
	 * @param plotter     The data plotter.
	 */
	public OutputInfo(
		String name,
		String shortName,
		int index,
		String description,
		DataPlotter plotter) {
		super();
		this.name = name;
		this.shortName = shortName;
		this.index = index;
		this.description = description;
		this.plotter = plotter;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof OutputInfo) {
			OutputInfo out = (OutputInfo) obj;
			if (!getName().equals(out.getName())) {
				return false;
			}
			return getIndex() == out.getIndex();
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
	 * @return The data index.
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @return The name.
	 */
	public String getName() {
		if (name == null && shortName != null) {
			return shortName;
		}
		return name;
	}

	/**
	 * @return The plotter if any.
	 */
	public DataPlotter getPlotter() {
		return plotter;
	}

	/**
	 * @return The short name.
	 */
	public String getShortName() {
		if (shortName == null && name != null) {
			return name;
		}
		return shortName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int hash = 3;
		hash = 53 * hash + Objects.hashCode(this.name);
		return hash;
	}

	/**
	 * @param description The description.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param index The data index.
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * @param name The name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param plotter The data plotter if any.
	 */
	public void setPlotter(DataPlotter plotter) {
		this.plotter = plotter;
	}

	/**
	 * @param shortName The short name.
	 */
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	/**
	 * @return A string representation.
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("[");
		b.append(getName());
		b.append(", ");
		b.append(getIndex());
		b.append("]");
		return b.toString();
	}

}
