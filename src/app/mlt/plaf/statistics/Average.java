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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mlt.util.FixedSizeList;

/**
 * Defines a smoothed average used as a movement descriptor.
 * 
 * @author Miquel Sas
 */
public class Average implements Comparable<Average> {

	private static double getAverage(Type type, int period, FixedSizeList<Double> buffer) {
		int startIndex = (buffer.size() < period ? 0 : buffer.size() - period);
		int endIndex = buffer.size() - 1;
		switch (type) {
		case SMA:
			return getAverageSMA(startIndex, endIndex, buffer);
		case WMA:
			return getAverageWMA(startIndex, endIndex, buffer);
		}
		throw new IllegalArgumentException();
	}

	private static double getAverageSMA(
		int startIndex,
		int endIndex,
		FixedSizeList<Double> buffer) {

		double average = 0;
		for (int index = startIndex; index <= endIndex; index++) {
			average += buffer.get(index);
		}
		average /= ((double) (endIndex - startIndex + 1));
		return average;
	}

	private static double getAverageWMA(
		int startIndex,
		int endIndex,
		FixedSizeList<Double> buffer) {

		double average = 0;
		double weight = 1;
		double totalWeight = 0;
		for (int index = startIndex; index <= endIndex; index++) {
			average += (buffer.get(index) * weight);
			totalWeight += weight;
			weight += 1;
		}
		average /= totalWeight;
		return average;
	}

	public static String getHeaderAverage(Average average) {
		return "Avg " + average.toString();
	}

	public static String getHeaderSlope(Average average, String suffix) {
		return "Avg-Slope " + average.getPeriod() + "_" + suffix;
	}

	public static String getHeaderSpread(Average fast, Average slow, String suffix) {
		return "Avg-Spread " + fast.getPeriod() + "/" + slow.getPeriod() + " " + suffix;
	}

	public static String getLabelAverage(Average average) {
		return "Average " + average.toString();
	}

	public static String getLabelSlope(Average average, String suffix) {
		return "Average slope " + average.getPeriod() + "_" + suffix + " value";
	}

	public static String getLabelSpread(Average fast, Average slow, String suffix) {
		return "Average Spread " + fast.getPeriod() + "/" + slow.getPeriod() + " " + suffix + " value";
	}

	public static String getNameAverage(Average average) {
		return "average_" + average.getPeriod();
	}

	public static String getNameSlope(Average average, String suffix) {
		return "average_slope_" + average.getPeriod() + "_" + suffix;
	}

	public static String getNameSpread(Average fast, Average slow, String suffix) {
		return "average_spread_" + fast.getPeriod() + "_" + slow.getPeriod() + "_" + suffix;
	}

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

	/** Smooth buffers. */
	private List<FixedSizeList<Double>> smoothBuffers;

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
	 * Return the average value.
	 * 
	 * @param buffer The fixed size list buffer of values to average.
	 * @return The average value, conveniently smoothed.
	 */
	public double getAverage(FixedSizeList<Double> buffer) {
		double average = getAverage(type, period, buffer);
		for (int i = 0; i < smooths.length; i++) {
			int smooth = smooths[i];
			FixedSizeList<Double> smoothBuffer = getSmoothBuffers().get(i);
			smoothBuffer.add(average);
			average = getAverage(type, smooth, smoothBuffer);
		}
		return average;
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
	 * @return The list of smooth buffers.
	 */
	private List<FixedSizeList<Double>> getSmoothBuffers() {
		if (smoothBuffers == null) {
			smoothBuffers = new ArrayList<>();
			for (int i = 0; i < smooths.length; i++) {
				smoothBuffers.add(new FixedSizeList<>(smooths[i]));
			}
		}
		return smoothBuffers;
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