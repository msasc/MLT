/*
 * Copyright (C) 2018 Miquel Sas
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
package com.mlt.mkt.data;

import com.mlt.util.Formats;

/**
 * Base class of timed data. An arbitrary number of double values with the starting time. It can be a data (open, high,
 * low, close, volume) pack or the list of values of an indicator or any list of values, each one with its meaning
 * detailed by an <em>OutputInfo</em> of the associated <em>DataInfo</em>.
 * 
 * @author Miquel Sas
 */
public class Data {

	/**
	 * Check if the data should accepted applyuuing the filter.
	 * 
	 * @param data   The data.
	 * @param filter The filter.
	 * @return A boolean.
	 */
	public static boolean accept(Data data, Filter filter) {
		switch (filter) {
		case NO_FILTER:
			return true;
		case ALL_FLATS:
			return !OHLC.isFlat(data);
		case WEEKENDS:
			// TODO implement weekend filter.
			return !OHLC.isFlat(data);
		default:
			return true;
		}
	}

	/** The data. */
	private double[] data;
	/** The start time in milliseconds. */
	private long time;
	/**
	 * A boolean that indicates if the data is valid and, for instance, should be plotted. Recall that some indicators
	 * can need a look backward or necessary number of bars to be calculated, and sometimes also a look forward, while
	 * the list of data has the same number of elements that the origin instrument list.
	 */
	private boolean valid = true;

	/**
	 * Default constructor.
	 */
	public Data() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param time The time.
	 * @param data The list of values.
	 */
	public Data(long time, double... data) {
		super();
		setTime(time);
		setData(data);
	}

	/**
	 * Constructs a data of the given size.
	 * 
	 * @param size The size or number of values.
	 */
	public Data(int size) {
		super();
		data = new double[size];
	}

	/**
	 * Returns the start time in millis.
	 * 
	 * @return The start time in millis.
	 */
	public long getTime() {
		return time;
	}

	/**
	 * Sets the start time in millis.
	 * 
	 * @param time The start time in millis.
	 */
	public void setTime(long time) {
		this.time = time;
	}

	/**
	 * Returns the data structure.
	 * 
	 * @return The data structure.
	 */
	public double[] getData() {
		return data;
	}

	/**
	 * Returns the value at the given index.
	 * 
	 * @param index The index.
	 * @return The value.
	 */
	public double getValue(int index) {
		return data[index];
	}

	/**
	 * Sets the value at the given index.
	 * 
	 * @param index The index.
	 * @param value The value.
	 */
	public void setValue(int index, double value) {
		data[index] = value;
	}

	/**
	 * Returns the internal data length.
	 * 
	 * @return The internal data length.
	 */
	public int size() {
		return data.length;
	}

	/**
	 * Sets the data structure.
	 * 
	 * @param data The data structure.
	 */
	public void setData(double[] data) {
		this.data = data;
	}

	/**
	 * Sets the data structure.
	 * 
	 * @param values The data structure.
	 */
	public void setValues(double[] values) {
		this.data = values;
	}

	/**
	 * Check if this data is valid.
	 * 
	 * @return A boolean that indicates if the data is valid and should be plotted or considered.
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * Set if this data is valid and should be plotted or considered.
	 * 
	 * @param valid A boolean that indicates if the data is valid and should be plotted or considered.
	 */
	public void setValid(boolean valid) {
		this.valid = valid;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(Formats.fromDateTime(time));
		if (data != null) {
			for (double d : data) {
				b.append(", ");
				b.append(d);
			}
		}
		return b.toString();
	}
}
