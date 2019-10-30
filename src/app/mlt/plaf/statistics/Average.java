/*
 * Copyright (C) 2015 Miquel Sas
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

import java.util.Arrays;

/**
 * Defines a smoothed average used as a movement descriptor.
 * 
 * @author Miquel Sas
 */
public class Average implements Comparable<Average> {
	
	/**
	 * Types of averages used.
	 */
	public enum Type {
		SMA,
		WMA
	}

	/** Average period. */
	private int period;
	/** Smoothing periods. */
	private int[] smooths;
	/** Average type. */
	private Type type = Type.SMA;

	/**
	 * Constructor.
	 * 
	 * @param type    The average type.
	 * @param period  Average period.
	 * @param smooths Smoothing periods.
	 */
	public Average(Type type, int period, int... smooths) {
		super();
		if (type == null) {
			throw new NullPointerException("Type can not be null");
		}
		if (period <= 0) {
			throw new IllegalArgumentException("Period must be greater than zero");
		}
		this.type = type;
		this.period = period;
		this.smooths = (smooths == null ? new int[0] : smooths);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(Average o) {
		if (o instanceof Average) {
			Average a = (Average) o;
			return Integer.compare(getPeriod(), a.getPeriod());
		}
		throw new IllegalArgumentException("Not comparable types");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof Average) {
			Average a = (Average) o;
			if (getPeriod() == a.getPeriod()) {
				if (getType().equals(a.getType())) {
					if (Arrays.equals(getSmooths(), a.getSmooths())) {
						return true;
					}
				}
			}
		}
		return false;
	}
	/**
	 * Returns the SMA period.
	 * 
	 * @return The SMA period.
	 */
	public int getPeriod() {
		return period;
	}

	/**
	 * Returns the smoothing periods.
	 * 
	 * @return The smoothing periods.
	 */
	public int[] getSmooths() {
		return smooths;
	}

	/**
	 * Returns the type.
	 * 
	 * @return The type.
	 */
	public Type getType() {
		return type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(type.toString());
		b.append("(");
		b.append(period);
		b.append(")");
		if (smooths.length > 0) {
			b.append(" (");
			for (int i = 0; i < smooths.length; i++) {
				if (i > 0) {
					b.append(", ");
				}
				b.append(smooths[i]);
			}
			b.append(")");
		}
		return b.toString();
	}
}