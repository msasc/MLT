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

package app.mlt.plaf_old.statistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mlt.util.BlockQueue;
import com.mlt.util.Numbers;

/**
 * Defines a smoothed average used as a movement descriptor.
 * 
 * @author Miquel Sas
 */
public class Average implements Comparable<Average> {

	/**
	 * @param period Average period.
	 * @param delta  Weight delta (increase).
	 * @param buffer Buffer of double values.
	 */
	private static double getAverage(int period, double delta, BlockQueue<Double> buffer) {
		int startIndex = (buffer.size() < period ? 0 : buffer.size() - period);
		int endIndex = buffer.size() - 1;
		double average = 0;
		double weight = 1;
		double totalWeight = 0;
		for (int index = startIndex; index <= endIndex; index++) {
			average += (buffer.getFirst(index) * weight);
			totalWeight += weight;
			weight += delta;
		}
		average /= totalWeight;
		return average;
	}

	/** Average period. */
	private int period;
	/** Average delta (WMA weight delta). */
	private double delta = 1.0;
	/** Smoothing periods. */
	private int[] smooths;

	/** Smooth buffers. */
	private List<BlockQueue<Double>> smoothBuffers;

	/**
	 * Constructor.
	 * 
	 * @param period  Average period.
	 * @param delta   WMA weight delta.
	 * @param smooths Smoothing periods.
	 */
	public Average(int period, double delta, int... smooths) {
		super();
		if (period <= 0) {
			throw new IllegalArgumentException("Period must be greater than zero");
		}
		if (delta <= 0) {
			throw new IllegalArgumentException("Delta must be greater than zero");
		}
		this.period = period;
		this.delta = delta;
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
				if (getDelta() == a.getDelta()) {
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
	public double getAverage(BlockQueue<Double> buffer) {
		double average = getAverage(period, delta, buffer);
		for (int i = 0; i < smooths.length; i++) {
			int smooth = smooths[i];
			BlockQueue<Double> smoothBuffer = getSmoothBuffers().get(i);
			smoothBuffer.add(average);
			average = getAverage(smooth, 1.0, smoothBuffer);
		}
		return average;
	}

	/**
	 * Returns the WMA weight delta.
	 * 
	 * @return The WMA weight delta.
	 */
	public int getPeriod() {
		return period;
	}

	/**
	 * @return The SMA period.
	 */
	public double getDelta() {
		return delta;
	}

	/**
	 * @return The list of smooth buffers.
	 */
	private List<BlockQueue<Double>> getSmoothBuffers() {
		if (smoothBuffers == null) {
			smoothBuffers = new ArrayList<>();
			for (int i = 0; i < smooths.length; i++) {
				smoothBuffers.add(new BlockQueue<>(smooths[i]));
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
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("[");
		b.append(period);
		b.append("; ");
		b.append(Numbers.getBigDecimal(delta, 2));
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
		b.append("]");
		return b.toString();
	}
}