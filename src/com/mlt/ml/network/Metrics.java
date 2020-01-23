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

package com.mlt.ml.network;

import com.mlt.ml.function.Matcher;
import com.mlt.ml.function.match.CategoryMatcher;
import com.mlt.util.Vector;

/**
 * Metrics used to eval the performance of a network configuration.
 *
 * @author Miquel Sas
 */
public class Metrics {
	
	/** Label. */
	private String label;

	/** Mean absolute error. */
	private double[] meanAbsoluteError;
	/** Mean squared error. */
	private double[] meanSquaredError;
	/** Root mean squared error. */
	private double[] rootMeanSquaredError;
	/** Coeficient of determination (R-Square). */
	private double[] rSquare;
	/** Overall performance. */
	private double performance;

	/** Match function, default is match category. */
	private Matcher matcher = new CategoryMatcher();

	/** Vectors length. */
	private int length;
	/** Size or number of patterns. */
	private int size;
	/** Number of matches. */
	private int matches;

	/** Mean correct data, necessary to calculate the r2. */
	private double[] mdata;
	/** Total square. */
	private double[] sstot;
	/** Total residual. */
	private double[] ssres;
	/** Calls to compute. */
	private int calls;
	/** Pass, 0 compute(a), 1 compute (a, b), 2 no more data. */
	private int pass;

	/**
	 * Constructor.
	 * 
	 * @param length Length of the data, correct and predict, vectors.
	 * @param size   Size of the pattern source.
	 */
	public Metrics(int length, int size) {
		this.length = length;
		this.size = size;
		reset();
	}

	/**
	 * Compute a pattern output to calculate the mean of data.
	 * 
	 * @param patternOutput The pattern output.
	 */
	public void compute(double[] patternOutput) {
		if (patternOutput.length != length) {
			throw new IllegalArgumentException();
		}
		if (pass != 0) {
			throw new IllegalStateException();
		}
		for (int i = 0; i < length; i++) {
			mdata[i] += patternOutput[i];
		}
		calls++;
		if (calls == size) {
			calls = 0;
			pass = 1;
			for (int i = 0; i < length; i++) {
				mdata[i] /= ((double) size);
			}
		}
	}

	/**
	 * Comput a pattern and a network output to calculate the rest of values.
	 * 
	 * @param patternOutput The pattern output.
	 * @param networkOutput The network output.
	 */
	public void compute(double[] patternOutput, double[] networkOutput) {
		if (patternOutput.length != length || networkOutput.length != length) {
			throw new IllegalArgumentException();
		}
		if (pass != 1) {
			throw new IllegalStateException();
		}
		for (int i = 0; i < length; i++) {
			double err = patternOutput[i] - networkOutput[i];
			meanAbsoluteError[i] += Math.abs(err);
			meanSquaredError[i] += Math.pow(err, 2);
			sstot[i] += Math.pow(patternOutput[i] - mdata[i], 2);
			ssres[i] += Math.pow(patternOutput[i] - networkOutput[i], 2);
		}
		if (matcher.match(patternOutput, networkOutput)) {
			matches++;
		}
		calls++;
		if (calls == size) {
			calls = 0;
			pass = 2;
			for (int i = 0; i < length; i++) {
				meanAbsoluteError[i] /= ((double) size);
				meanSquaredError[i] /= ((double) size);
				rootMeanSquaredError[i] = Math.sqrt(meanSquaredError[i]);
				rSquare[i] = 1 - ssres[i] / sstot[i];
			}
			performance = ((double) matches) / ((double) size);
		}
	}

	/**
	 * @return The label.
	 */
	protected String getLabel() {
		return label;
	}

	/**
	 * @return The mean absolute error.
	 */
	public double getMeanAbsoluteError() {
		if (pass != 2) {
			throw new IllegalStateException();
		}
		return Vector.mean(meanAbsoluteError);
	}

	/**
	 * @return The mean square error.
	 */
	public double getMeanSquaredError() {
		if (pass != 2) {
			throw new IllegalStateException();
		}
		return Vector.mean(meanSquaredError);
	}

	/**
	 * @return The overall performance.
	 */
	public double getPerformance() {
		if (pass != 2) {
			throw new IllegalStateException();
		}
		return performance;
	}

	/**
	 * @return The root mean square error.
	 */
	public double getRootMeanSquaredError() {
		if (pass != 2) {
			throw new IllegalStateException();
		}
		return Vector.mean(rootMeanSquaredError);
	}

	/**
	 * @return The coeficient of determination.
	 */
	public double getRSquare() {
		if (pass != 2) {
			throw new IllegalStateException();
		}
		return Vector.mean(rSquare);
	}

	/**
	 * Reset.
	 */
	public void reset() {
		matches = 0;
		meanAbsoluteError = new double[length];
		meanSquaredError = new double[length];
		rootMeanSquaredError = new double[length];
		rSquare = new double[length];
		performance = 0;

		mdata = new double[length];
		sstot = new double[length];
		ssres = new double[length];
		calls = 0;
		pass = 0;
	}

	/**
	 * @param label The label.
	 */
	protected void setLabel(String label) {
		this.label = label;
	}
}
