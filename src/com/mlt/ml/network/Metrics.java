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
import java.util.function.Function;

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
	 * Manager of the teai and test history of metrics.
	 */
	public static class Manager {
		/** Raw history of train metrics. */
		private List<Metrics> trainHistoryRaw = new ArrayList<>();
		/** Raw history of test metrics. */
		private List<Metrics> testHistoryRaw = new ArrayList<>();
		/** Average history of train metrics. */
		private List<Metrics> trainHistoryAvg = new ArrayList<>();
		/** Average history of test metrics. */
		private List<Metrics> testHistoryAvg = new ArrayList<>();
		/** Decimals to report values. */
		private int decimals = 6;
		/** Default average type. */
		private String averageType = "SMA";
		/** Funcion to compute the average period, ther argument is the history size. */
		private Function<Integer, Integer> averagePeriod = (size) -> Math.max(5, size / 4);

		/**
		 * Default constructor.
		 */
		public Manager() {
			super();
		}

		/**
		 * @param decimals    Decimals to report values.
		 * @param averageType Average type.
		 */
		public Manager(int decimals, String averageType) {
			super();
			if (!Strings.in(averageType, "SMA", "WMA")) {
				throw new IllegalArgumentException();
			}
			this.decimals = decimals;
			this.averageType = averageType;
		}

		/**
		 * Add train and test metrics to the history.
		 * 
		 * @param trainMetrics Train metrics.
		 * @param testMetrics  Test metrics.
		 */
		public void add(Metrics trainMetrics, Metrics testMetrics) {
			trainHistoryRaw.add(trainMetrics);
			testHistoryRaw.add(testMetrics);
		}

		/**
		 * Calculate metrics averages.
		 */
		public void calculateAverages() {

			int size = trainHistoryRaw.size();
			int period = averagePeriod.apply(size);

			trainHistoryAvg.clear();
			testHistoryAvg.clear();

			for (int i = 0; i < size; i++) {
				int index = Math.max(0, i - period + 1);

				List<double[]> rawValuesTrain = new ArrayList<>();
				for (int j = index; j <= i; j++) {
					rawValuesTrain.add(trainHistoryRaw.get(j).valuesRaw());
				}
				List<double[]> rawValuesTest = new ArrayList<>();
				for (int j = index; j <= i; j++) {
					rawValuesTest.add(testHistoryRaw.get(j).valuesRaw());
				}

				double[] rawAvgTrain = null;
				double[] rawAvgTest = null;
				if (averageType.equals("SMA")) {
					rawAvgTrain = Vector.averageSMA(rawValuesTrain);
					rawAvgTest = Vector.averageSMA(rawValuesTest);
				}
				if (averageType.equals("WMA")) {
					rawAvgTrain = Vector.averageWMA(rawValuesTrain);
					rawAvgTest = Vector.averageWMA(rawValuesTest);
				}

				Metrics avgTrain = new Metrics();
				avgTrain.label = averageType;
				avgTrain.period = period;
				avgTrain.errAvg = rawAvgTrain[0];
				avgTrain.errVar = rawAvgTrain[1];
				avgTrain.errStd = rawAvgTrain[2];
				avgTrain.perf = rawAvgTrain[3];
				avgTrain.perfVar = rawAvgTrain[4];
				trainHistoryAvg.add(avgTrain);

				Metrics avgTest = new Metrics();
				avgTest.label = averageType;
				avgTest.period = period;
				avgTest.errAvg = rawAvgTest[0];
				avgTest.errVar = rawAvgTest[1];
				avgTest.errStd = rawAvgTest[2];
				avgTest.perf = rawAvgTest[3];
				avgTest.perfVar = rawAvgTest[4];
				testHistoryAvg.add(avgTest);
			}
		}

		/**
		 * Clear the history.
		 */
		public void clear() {
			trainHistoryRaw.clear();
			testHistoryRaw.clear();
		}

		/**
		 * @return The average test history, for any analytical purposes.
		 */
		public List<Metrics> getTestHistoryAvg() {
			return testHistoryAvg;
		}

		/**
		 * @return The raw test history, for any analytical purposes.
		 */
		public List<Metrics> getTestHistoryRaw() {
			return testHistoryRaw;
		}

		/**
		 * @return The average train history, for any analytical purposes.
		 */
		public List<Metrics> getTrainHistoryAvg() {
			return trainHistoryAvg;
		}

		/**
		 * @return The raw train history, for any analytical purposes.
		 */
		public List<Metrics> getTrainHistoryRaw() {
			return trainHistoryRaw;
		}

		public String summary() {

			calculateAverages();

			StringWriter s = new StringWriter();
			PrintWriter p = new PrintWriter(s);

			List<String> titles = new ArrayList<>();
			titles.add("Err raw");
			titles.add("Var raw");
			titles.add("Err avg");
			titles.add("Var avg");
			titles.add("Prf raw");
			titles.add("Var raw");
			titles.add("Prf avg");
			titles.add("Var avg");

			int columns = titles.size();
			int size = trainHistoryRaw.size();
			int padNumber = decimals + 3;
			int padColumn = 0;
			for (String title : titles) {
				padColumn = Math.max(padColumn, Math.max(padNumber, title.length()));
			}

			int padIndex = Numbers.getDigits(size);
			int padLabel = 0;
			for (Metrics metrics : trainHistoryRaw) {
				padLabel = Math.max(padLabel, Strings.length(metrics.label));
			}
			padLabel++;
			int sepPrefix = 2;
			int sepColumn = 1;
			int sepBlock = 2;
			int padMetrics = padColumn * columns + sepColumn * (columns - 1);

			/* Headers. */

			p.print(Strings.repeat(" ", padIndex + padLabel + sepPrefix));
			p.print(Strings.centerPad("Training", padMetrics, " "));
			p.print(Strings.repeat(" ", sepBlock));
			p.print(Strings.centerPad("Test", padMetrics, " "));
			p.println();

			p.print(Strings.repeat(" ", padIndex + padLabel + sepPrefix));
			p.print(Strings.repeat("-", padMetrics));
			p.print(Strings.repeat(" ", sepBlock));
			p.print(Strings.repeat("-", padMetrics));
			p.println();

			p.print(Strings.repeat(" ", padIndex + padLabel + sepPrefix));
			for (int i = 0; i < titles.size(); i++) {
				String title = titles.get(i);
				if (i > 0) {
					p.print(Strings.repeat(" ", sepColumn));
				}
				p.print(Strings.leftPad(title, padColumn, " "));
			}
			p.print(Strings.repeat(" ", sepBlock));
			for (int i = 0; i < titles.size(); i++) {
				String title = titles.get(i);
				if (i > 0) {
					p.print(Strings.repeat(" ", sepColumn));
				}
				p.print(Strings.leftPad(title, padColumn, " "));
			}
			p.println();
			
			p.print(Strings.repeat(" ", padIndex + padLabel + sepPrefix));
			for (int i = 0; i < titles.size(); i++) {
				if (i > 0) {
					p.print(Strings.repeat(" ", sepColumn));
				}
				p.print(Strings.repeat("-", padColumn));
			}
			p.print(Strings.repeat(" ", sepBlock));
			for (int i = 0; i < titles.size(); i++) {
				if (i > 0) {
					p.print(Strings.repeat(" ", sepColumn));
				}
				p.print(Strings.repeat("-", padColumn));
			}
			p.println();
			
			/* Values. */

			for (int m = 0; m < size; m++) {
				
				Metrics trainRaw = trainHistoryRaw.get(m);
				Metrics testRaw = testHistoryRaw.get(m);
				Metrics trainAvg = trainHistoryAvg.get(m);
				Metrics testAvg = testHistoryAvg.get(m);
				
				if (m > 0) {
					trainRaw.variance(trainHistoryRaw.get(m - 1));
					testRaw.variance(testHistoryRaw.get(m - 1));
					trainAvg.variance(trainHistoryAvg.get(m - 1));
					testAvg.variance(testHistoryAvg.get(m - 1));
				}
				
				List<BigDecimal> valuesTrain = new ArrayList<>();
				valuesTrain.add(Numbers.getBigDecimal(trainRaw.errAvg, decimals));
				valuesTrain.add(Numbers.getBigDecimal(trainRaw.errVar, decimals));
				valuesTrain.add(Numbers.getBigDecimal(trainAvg.errAvg, decimals));
				valuesTrain.add(Numbers.getBigDecimal(trainAvg.errVar, decimals));
				valuesTrain.add(Numbers.getBigDecimal(trainRaw.perf, decimals));
				valuesTrain.add(Numbers.getBigDecimal(trainRaw.perfVar, decimals));
				valuesTrain.add(Numbers.getBigDecimal(trainAvg.perf, decimals));
				valuesTrain.add(Numbers.getBigDecimal(trainAvg.perfVar, decimals));
				
				List<BigDecimal> valuesTest = new ArrayList<>();
				valuesTest.add(Numbers.getBigDecimal(testRaw.errAvg, decimals));
				valuesTest.add(Numbers.getBigDecimal(testRaw.errVar, decimals));
				valuesTest.add(Numbers.getBigDecimal(testAvg.errAvg, decimals));
				valuesTest.add(Numbers.getBigDecimal(testAvg.errVar, decimals));
				valuesTest.add(Numbers.getBigDecimal(testRaw.perf, decimals));
				valuesTest.add(Numbers.getBigDecimal(testRaw.perfVar, decimals));
				valuesTest.add(Numbers.getBigDecimal(testAvg.perf, decimals));
				valuesTest.add(Numbers.getBigDecimal(testAvg.perfVar, decimals));
				
				p.print(Strings.leftPad(m, padIndex));
				p.print(Strings.leftPad(trainRaw.label, padLabel));
				p.print(Strings.repeat(" ", sepPrefix));
				for (int i = 0; i < valuesTrain.size(); i++) {
					Number v = valuesTrain.get(i);
					if (i > 0) {
						p.print(Strings.repeat(" ", sepColumn));
					}
					p.print(Strings.leftPad(v, padColumn));
				}
				p.print(Strings.repeat(" ", sepBlock));
				for (int i = 0; i < valuesTest.size(); i++) {
					Number v = valuesTest.get(i);
					if (i > 0) {
						p.print(Strings.repeat(" ", sepColumn));
					}
					p.print(Strings.leftPad(v, padColumn));
				}
				p.println();
			}

			p.close();
			return s.toString();
		}
	}

	/** Label. */
	private String label;
	/** Average period. */
	private int period;

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
	 * Constructor for averages.
	 */
	private Metrics() {}

	/**
	 * @param Training label.
	 * @param length   Length of the data, correct and predict, vectors.
	 * @param size     Size of the pattern source.
	 */
	public Metrics(String label, int length, int size) {
		this.label = label;
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
	 * @return The label.
	 */
	public String getLabel() {
		return label;
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
	 * @return The period for averages.
	 */
	public int getPeriod() {
		return period;
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
	 * @param label The label.
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return The array of raw values.
	 */
	private double[] valuesRaw() {
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
