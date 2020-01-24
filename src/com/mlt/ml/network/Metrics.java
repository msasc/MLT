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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.mlt.ml.function.Matcher;
import com.mlt.ml.function.match.CategoryMatcher;
import com.mlt.util.Numbers;
import com.mlt.util.Strings;
import com.mlt.util.Vector;

/**
 * Metrics used to eval the performance of a network configuration.
 *
 * @author Miquel Sas
 */
public class Metrics {

	/**
	 * @param trainMetrics List of train metrics.
	 * @param testMetrics  List of test metrics.
	 * @return A full report.
	 */
	public static String getReport(List<Metrics> trainMetrics, List<Metrics> testMetrics) {

		/* Validate. */
		if (trainMetrics.isEmpty() || testMetrics.isEmpty()) {
			return "";
		}
		if (trainMetrics.size() != testMetrics.size()) {
			return "";
		}

		Metrics master = trainMetrics.get(0);
		int length = master.length;
		int padNumber = master.decimals + 3;
		int padVector = (length * padNumber) + ((length - 1) * 2);
		int size = trainMetrics.size();
		List<String> titles = new ArrayList<>();
		titles.add("Mean Absolute Error");
		titles.add("Mean Square Error");
		titles.add("Root Mean Square Error");
		titles.add("R-Square");
		int padTitle = 0;
		for (String title : titles) {
			padTitle = Math.max(padTitle, title.length());
		}
		padTitle += 2;
		int sep = 4;

		StringWriter s = new StringWriter();
		PrintWriter p = new PrintWriter(s);

		p.print(Strings.repeat(" ", padTitle));
		p.print(Strings.centerPad("Training", padVector, " "));
		p.print(Strings.repeat(" ", sep));
		p.print(Strings.centerPad("Test", padVector, " "));
		p.println();

		p.print(Strings.repeat(" ", padTitle));
		p.print(Strings.repeat("-", padVector));
		p.print(Strings.repeat(" ", sep));
		p.print(Strings.repeat("-", padVector));
		p.println();

		for (int i = 0; i < titles.size(); i++) {
			String title = titles.get(i);
			for (int j = 0; j < size; j++) {
				if (j == 0) {
					p.print(Strings.rightPad(title, padTitle));
				} else {
					p.print(Strings.repeat(" ", padTitle));
				}
				Metrics mtrain = trainMetrics.get(j); 
				Metrics mtest = testMetrics.get(j); 
				BigDecimal[] train = null;
				BigDecimal[] test = null;
				switch (i) {
				case 0:
					train = mtrain.getMeanAbsoluteErrorVector();
					test = mtest.getMeanAbsoluteErrorVector();
					break;
				case 1:
					train = mtrain.getMeanSquaredErrorVector();
					test = mtest.getMeanSquaredErrorVector();
					break;
				case 2:
					train = mtrain.getRootMeanSquaredErrorVector();
					test = mtest.getRootMeanSquaredErrorVector();
					break;
				case 3:
					train = mtrain.getRSquaredVector();
					test = mtest.getRSquaredVector();
					break;
				}
				p.println();
			}
		}

		p.close();
		return s.toString();
	}

	/** Label. */
	private String label;
	/** Decimals for presentation. */
	private int decimals = 4;

	/** Mean absolute error. */
	private double[] meanAbsoluteError;
	/** Mean squared error. */
	private double[] meanSquaredError;
	/** Root mean squared error. */
	private double[] rootMeanSquaredError;
	/** Coeficient of determination (R-Square). */
	private double[] rSquared;
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
				rSquared[i] = 1 - ssres[i] / sstot[i];
			}
			performance = ((double) matches) / ((double) size);
		}
	}

	/**
	 * @param value The value to format.
	 * @return The formatted value with decimals.
	 */
	private BigDecimal getFormatted(double value) {
		if (pass != 2) {
			throw new IllegalStateException();
		}
		return Numbers.getBigDecimal(value, decimals);
	}

	/**
	 * @param values The values to format.
	 * @return The formatted values with decimals.
	 */
	private BigDecimal[] getFormatted(double[] values) {
		if (pass != 2) {
			throw new IllegalStateException();
		}
		BigDecimal[] fmt = new BigDecimal[length];
		for (int i = 0; i < length; i++) {
			fmt[i] = getFormatted(values[i]);
		}
		return fmt;
	}

	/**
	 * @return The label.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return The mean absolute error.
	 */
	public BigDecimal getMeanAbsoluteError() {
		return getFormatted(Vector.mean(meanAbsoluteError));
	}

	/**
	 * @return The mean absolute error.
	 */
	public BigDecimal[] getMeanAbsoluteErrorVector() {
		return getFormatted(meanAbsoluteError);
	}

	/**
	 * @return The mean square error.
	 */
	public BigDecimal getMeanSquaredError() {
		return getFormatted(Vector.mean(meanSquaredError));
	}

	/**
	 * @return The mean square error.
	 */
	public BigDecimal[] getMeanSquaredErrorVector() {
		return getFormatted(meanSquaredError);
	}

	/**
	 * @return The overall performance.
	 */
	public BigDecimal getPerformance() {
		return getFormatted(performance);
	}

	/**
	 * @return The root mean square error.
	 */
	public BigDecimal getRootMeanSquaredError() {
		return getFormatted(Vector.mean(rootMeanSquaredError));
	}

	/**
	 * @return The root mean square error.
	 */
	public BigDecimal[] getRootMeanSquaredErrorVector() {
		return getFormatted(rootMeanSquaredError);
	}

	/**
	 * @return The coeficient of determination.
	 */
	public BigDecimal getRSquared() {
		return getFormatted(Vector.mean(rSquared));
	}

	/**
	 * @return The coeficient of determination.
	 */
	public BigDecimal[] getRSquaredVector() {
		return getFormatted(rSquared);
	}

	/**
	 * Reset.
	 */
	public void reset() {
		matches = 0;
		meanAbsoluteError = new double[length];
		meanSquaredError = new double[length];
		rootMeanSquaredError = new double[length];
		rSquared = new double[length];
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
	public void setLabel(String label) {
		this.label = label;
	}
}
