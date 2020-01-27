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
public class Metrics implements Comparable<Metrics> {

	/**
	 * @param metrics The list of values.
	 * @param period  The period.
	 * @param type    Average type ("SMA"/"WMA).
	 * @return The list of the SMA averages.
	 */
	public static List<Metrics> averages(List<Metrics> metrics, int period, String type) {
		if (!Strings.in(type, "SMA", "WMA")) {
			throw new IllegalArgumentException("Invalid aveage type " + type);
		}
		List<Metrics> averages = new ArrayList<>();
		for (int i = 0; i < metrics.size(); i++) {
			int index = Math.max(0, i - period + 1);
			List<double[]> rawValues = new ArrayList<>();
			for (int j = index; j <= i; j++) {
				rawValues.add(metrics.get(j).valuesRaw());
			}
			double[] rawAvg = null;
			if (type.equals("SMA")) {
				rawAvg = Vector.averageSMA(rawValues);
			}
			if (type.equals("WMA")) {
				rawAvg = Vector.averageWMA(rawValues);
			}
			averages.add(new Metrics(rawAvg));
		}
		return averages;
	}

	/**
	 * @param decimals     Decimals for number precision.
	 * @param trainMetrics List of train metrics.
	 * @param testMetrics  List of test metrics.
	 * @return The summary report.
	 */
	public static String summary(
		int decimals,
		List<Metrics> trainMetrics,
		List<Metrics> testMetrics) {

		int padNumber = decimals + 3;
		int sepBlock = 2;
		int sepItem = 1;
		int size = trainMetrics.size();

		List<String> titles = titles();

		int padTitle = 0;
		for (String title : titles) {
			padTitle = Math.max(padTitle, title.length());
		}
		padTitle = Math.max(padTitle, padNumber);

		int padMetrics = titles.size() * padTitle + (titles.size() - 1) * sepItem;
		int padIndex = Numbers.getDigits(size);
		int sepIndex = 2;

		StringWriter s = new StringWriter();
		PrintWriter p = new PrintWriter(s);

		/* Headers. */

		p.print(Strings.repeat(" ", padIndex + sepIndex));
		p.print(Strings.centerPad("Training", padMetrics, " "));
		p.print(Strings.repeat(" ", sepBlock));
		p.print(Strings.centerPad("Test", padMetrics, " "));
		p.println();

		p.print(Strings.repeat(" ", padIndex + sepIndex));
		p.print(Strings.repeat("-", padMetrics));
		p.print(Strings.repeat(" ", sepBlock));
		p.print(Strings.repeat("-", padMetrics));
		p.println();

		p.print(Strings.repeat(" ", padIndex + sepIndex));
		for (int i = 0; i < titles.size(); i++) {
			String title = titles.get(i);
			if (i > 0) {
				p.print(Strings.repeat(" ", sepItem));
			}
			p.print(Strings.leftPad(title, padTitle, " "));
		}
		p.print(Strings.repeat(" ", sepBlock));
		for (int i = 0; i < titles.size(); i++) {
			String title = titles.get(i);
			if (i > 0) {
				p.print(Strings.repeat(" ", sepItem));
			}
			p.print(Strings.leftPad(title, padTitle, " "));
		}
		p.println();

		p.print(Strings.repeat(" ", padIndex + sepIndex));
		for (int i = 0; i < titles.size(); i++) {
			if (i > 0) {
				p.print(Strings.repeat(" ", sepItem));
			}
			p.print(Strings.repeat("-", padTitle));
		}
		p.print(Strings.repeat(" ", sepBlock));
		for (int i = 0; i < titles.size(); i++) {
			if (i > 0) {
				p.print(Strings.repeat(" ", sepItem));
			}
			p.print(Strings.repeat("-", padTitle));
		}
		p.println();

		/* Values. */

		for (int m = 0; m < size; m++) {
			Metrics vtr = trainMetrics.get(m);
			Metrics vts = testMetrics.get(m);
			if (m > 0) {
				vtr.variance(trainMetrics.get(m - 1));
				vts.variance(testMetrics.get(m - 1));
			}
			List<BigDecimal> vtrs = vtr.valuesFmt(decimals);
			List<BigDecimal> vtss = vts.valuesFmt(decimals);

			p.print(Strings.leftPad(m, padIndex));
			p.print(Strings.repeat(" ", sepIndex));
			for (int i = 0; i < vtrs.size(); i++) {
				Number v = vtrs.get(i);
				if (i > 0) {
					p.print(Strings.repeat(" ", sepItem));
				}
				p.print(Strings.leftPad(v, padTitle));
			}
			p.print(Strings.repeat(" ", sepBlock));
			for (int i = 0; i < vtss.size(); i++) {
				Number v = vtss.get(i);
				if (i > 0) {
					p.print(Strings.repeat(" ", sepItem));
				}
				p.print(Strings.leftPad(v, padTitle));
			}
			p.println();
		}

		p.close();
		return s.toString();
	}
	
	/**
	 * @return The list of titles.
	 */
	public static List<String> titles() {
		List<String> titles = new ArrayList<>();
		titles.add("Error");
		titles.add("Err Var");
		titles.add("Err Std");
		titles.add("Perform");
		titles.add("Perf Var");
		return titles;
	}

	/** Average absolute error. */
	private double errAvg;
	/** Average absolute error variation vs previous. */
	private double errVar;
	/** Average absolute error standard deviation. */
	private double errStd;

	/** Performance. */
	private double perf;
	/** Performance variation vs previous. */
	private double perfVar;

	/** M-Squared vector. */
	private double[] error;

	/** Match function, default is match category. */
	private Matcher matcher = new CategoryMatcher();

	/** Vectors length. */
	private int length;
	/** Size or number of patterns. */
	private int size;
	/** Number of matches. */
	private int matches;

	/** Calls to compute. */
	private int calls;

	/**
	 * @param length Length of the data, correct and predict, vectors.
	 * @param size   Size of the pattern source.
	 */
	public Metrics(int length, int size) {
		this.length = length;
		this.size = size;
		
		matches = 0;
		error = new double[length];
		calls = 0;

		errAvg = 0;
		errVar = 0;
		errStd = 0;

		perf = 0;
		perfVar = 0;
	}

	/**
	 * @param values Result values.
	 */
	private Metrics(double[] values) {
		errAvg = values[0];
		errVar = values[1];
		errStd = values[2];
		perf = values[3];
		perfVar = values[4];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(Metrics m) {
		return Double.compare(perf, m.perf);
	}

	/**
	 * Compute a pattern and a network output to calculate the rest of values.
	 * 
	 * @param patternOutput The pattern output.
	 * @param networkOutput The network output.
	 */
	public void compute(double[] patternOutput, double[] networkOutput) {
		if (patternOutput.length != length || networkOutput.length != length) {
			throw new IllegalArgumentException();
		}
		for (int i = 0; i < length; i++) {
			error[i] += Math.abs(patternOutput[i] - networkOutput[i]);
		}
		if (matcher.match(patternOutput, networkOutput)) {
			matches++;
		}
		calls++;
		if (calls < size) {
			double[] errorTmp = Vector.copy(error);
			for (int i = 0; i < length; i++) {
				errorTmp[i] /= ((double) calls);
			}
			errAvg = Vector.mean(errorTmp);
			errStd = Vector.stddev(errorTmp, errAvg);
			perf = ((double) matches) / ((double) calls);
		}
		if (calls == size) {
			for (int i = 0; i < length; i++) {
				error[i] /= ((double) size);
			}
			errAvg = Vector.mean(error);
			errStd = Vector.stddev(error, errAvg);
			perf = ((double) matches) / ((double) size);
		}
	}

	/**
	 * @return The errAvg.
	 */
	public double getErrAvg() {
		return errAvg;
	}

	/**
	 * @return The errVar.
	 */
	public double getErrVar() {
		return errVar;
	}

	/**
	 * @return The errStd.
	 */
	public double getErrStd() {
		return errStd;
	}

	/**
	 * @return The perf.
	 */
	public double getPerf() {
		return perf;
	}

	/**
	 * @return The perfVar.
	 */
	public double getPerfVar() {
		return perfVar;
	}

	/**
	 * Reset.
	 */
	public void reset() {
		matches = 0;
		error = new double[length];
		calls = 0;

		errAvg = 0;
		errVar = 0;
		errStd = 0;

		perf = 0;
		perfVar = 0;
	}

	/**
	 * @param decimals Decimals to format values.
	 * @return The list of values.
	 */
	public List<BigDecimal> valuesFmt(int decimals) {
		List<BigDecimal> values = new ArrayList<>();

		values.add(Numbers.getBigDecimal(errAvg, decimals));
		values.add(Numbers.getBigDecimal(errVar, decimals));
		values.add(Numbers.getBigDecimal(errStd, decimals));

		values.add(Numbers.getBigDecimal(perf, decimals));
		values.add(Numbers.getBigDecimal(perfVar, decimals));

		return values;
	}

	/**
	 * @return The array of raw values.
	 */
	public double[] valuesRaw() {
		return new double[] {
			errAvg, errVar, errStd, perf, perfVar
		};
	}

	/**
	 * Calculate variations vs the previous metrics.
	 * 
	 * @param m The previous metrics.
	 */
	public void variance(Metrics m) {
		if (m == null) {
			return;
		}
		if (m.errAvg != 0) {
			errVar = (errAvg / m.errAvg) - 1;
		}
		if (m.perf != 0) {
			perfVar = (perf / m.perf) - 1;
		}
	}
}
