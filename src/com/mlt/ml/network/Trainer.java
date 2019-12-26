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

package com.mlt.ml.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TreeMap;

import com.mlt.desktop.Alert;
import com.mlt.desktop.FileChooser;
import com.mlt.desktop.Option;
import com.mlt.ml.data.Pattern;
import com.mlt.ml.data.PatternSource;
import com.mlt.ml.function.distance.DistanceEuclidean;
import com.mlt.ml.function.Distance;
import com.mlt.task.Task;
import com.mlt.util.Numbers;
import com.mlt.util.Strings;

/**
 * Trainer task for networks.
 * 
 * @author Miquel Sas
 */
public class Trainer extends Task {

	/**
	 * Subtract the values of vector b from vector a (must have the same length).
	 *
	 * @param x Vector x.
	 * @param y Vector y.
	 * @return The result of subtracting the values.
	 */
	private static double[] subtract(double[] x, double[] y) {
		double[] r = new double[x.length];
		for (int i = 0; i < x.length; i++) {
			r[i] = x[i] - y[i];
		}
		return r;
	}

	/**
	 * Return the trace data.
	 * 
	 * @param error            Iteration error.
	 * @param trainPerformance Train performance.
	 * @param testPerformance  Test performance.
	 * @return The trace.
	 */
	private static String trace(
		BigDecimal error,
		BigDecimal trainPerformance,
		BigDecimal testPerformance) {
		StringBuilder b = new StringBuilder();
		b.append("(");
		b.append(error);
		if (trainPerformance != null) {
			b.append(", ");
			b.append(trainPerformance);
		}
		if (testPerformance != null) {
			b.append(", ");
			b.append(testPerformance);
		}
		b.append(")");
		return b.toString();
	}

	/**
	 * Trace of error, train performance and test performance.
	 */
	static class Trace {
		BigDecimal error;
		int trainMatches;
		int trainSize;
		BigDecimal trainPerformance;
		int testMatches;
		int testSize;
		BigDecimal testPerformance;

		Trace(
			BigDecimal error,
			int trainMatches,
			int trainSize,
			BigDecimal trainPerformance,
			int testMatches,
			int testSize,
			BigDecimal testPerformance) {

			this.error = error;
			this.trainMatches = trainMatches;
			this.trainSize = trainSize;
			this.trainPerformance = trainPerformance;
			this.testMatches = testMatches;
			this.testSize = testSize;
			this.testPerformance = testPerformance;
		}

		public String toString() {
			StringBuilder b = new StringBuilder();
			b.append("(");
			b.append(error);
			if (trainPerformance != null) {
				b.append(", ");
				b.append(trainMatches);
				b.append("/");
				b.append(trainSize);
				b.append(", ");
				b.append(trainPerformance);
			}
			if (testPerformance != null) {
				b.append(", ");
				b.append(testMatches);
				b.append("/");
				b.append(testSize);
				b.append(", ");
				b.append(testPerformance);
			}
			b.append(")");
			return b.toString();
		}
	}

	/**
	 * File filter.
	 */
	class TrainerFileFilter extends javax.swing.filechooser.FileFilter
		implements java.io.FileFilter {

		@Override
		public boolean accept(File file) {
			if (file != null) {
				if (file.isDirectory()) {
					return true;
				}
				String name = file.getName();
				if (fileExtension != null) {
					if (!name.toLowerCase().endsWith("." + fileExtension.toLowerCase())) {
						return false;
					}
				}
				if (fileRoot != null) {
					if (!name.toLowerCase().startsWith(fileRoot.toLowerCase())) {
						return false;
					}
				}
				return true;
			}
			return false;
		}

		@Override
		public String getDescription() {
			if (fileRoot != null) {
				return fileRoot + " Network files";
			}
			return null;
		}
	}

	/** Label to show the current error. */
	private static final String LABEL_ERROR = "LABEL-ERROR";
	/** Label to show the history of flat errors and performances. */
	private static final String LABEL_FLAT = "LABEL-FLAT";
	/** Label to show the history of score errors and performances. */
	private static final String LABEL_SCORE = "LABEL-SCORE";
	/**
	 * Label to inform of an additional process like saving or calculating
	 * performance.
	 */
	private static final String LABEL_PROCESSING = "LABEL-PROCESSING";
	/**
	 * Label to inform of an additional process like saving or calculating
	 * performance.
	 */
	private static final String PROGRESS_PROCESSING = "PROGRESS-PROCESSING";

	/** Status to show the current error. */
	private static final String STATUS_ERROR = "STATUS-ERROR";
	/** Status to show the history of flat errors and performances. */
	private static final String STATUS_FLAT = "STATUS-FLAT";
	/** Status to show the history of score errors and performances. */
	private static final String STATUS_SCORE = "STATUS-SCORE";
	/**
	 * Status to inform of an additional process like saving or calculating
	 * performance.
	 */
	private static final String STATUS_PROCESSING = "STATUS-PROCESSING";

	/** The network. */
	private Network network;
	/** The training pattern source. */
	private PatternSource patternSourceTraining;
	/** The test pattern source. */
	private PatternSource patternSourceTest;
	/** The number of epochs or turns to the full list of patterns. */
	private int epochs = 500;
	/** The file to save the network. */
	private File file;
	/** A boolean to indicate whether to save/restore network data. */
	private boolean saveNetworkData = true;
	/**
	 * A boolean that indicates whether the train source should be shuffled before
	 * each iteration.
	 */
	private boolean shuffle = true;

	/** File path. */
	private String filePath;
	/** File root name. */
	private String fileRoot;
	/** File extension. */
	private String fileExtension;

	/** Test size. */
	private int testSize;
	/** Test matches. */
	private int testMatches;
	/** Test performance. */
	private BigDecimal testPerformance;
	/** Train size. */
	private int trainSize;
	/** Train matches. */
	private int trainMatches;
	/** Train performance. */
	private BigDecimal trainPerformance;

	/** Error decimals. */
	private int errorDecimals = 8;
	/** Performance decimals. */
	private int performanceDecimals = 2;
	/** Maximum trace length. */
	private int maxTrace = 4;
	/** Best trace. */
	private Trace bestTrace;

	/** Random to set random scored sizes. */
	private Random random = new Random();

	/**
	 * Default constructor.
	 */
	public Trainer() {
		this(Locale.getDefault());
	}

	/**
	 * Constructor assigning the locale.
	 * 
	 * @param locale The locale for messages.
	 */
	public Trainer(Locale locale) {
		super(locale);
		addStatus(STATUS_ERROR);
		addStatus(STATUS_FLAT);
		addStatus(STATUS_SCORE);
		addStatus(STATUS_PROCESSING);
	}

	/**
	 * Calculate the test performance.
	 */
	private void calculateTestPerformance() {
		removeStatusLabel(STATUS_PROCESSING, LABEL_PROCESSING);
		int matches = 0;
		int size = patternSourceTest.size();
		for (int i = 0; i < size; i++) {
			if (isCancelRequested()) {
				setCancelled();
				break;
			}
			updateStatusProgress(
				STATUS_PROCESSING,
				PROGRESS_PROCESSING,
				"Calculating test performance: " + (i + 1) + " of " + size,
				(i + 1), size);
//			updateStatusProgress(
//				STATUS_PROCESSING,
//				PROGRESS_PROCESSING,
//				(i + 1), size);
			Pattern pattern = patternSourceTest.get(i);
			double[] patternInput = pattern.getInputValues();
			double[] networkOutput = network.calculate(patternInput);
			if (pattern.matches(networkOutput)) {
				matches += 1;
			}
		}
		double performance = 100.0 * matches / size;
		testSize = patternSourceTest.size();
		testMatches = (int) matches;
		testPerformance = Numbers.getBigDecimal(performance, performanceDecimals);
		removeStatusProgress(STATUS_PROCESSING, PROGRESS_PROCESSING);
	}

	/**
	 * Calculate the train performance.
	 * 
	 * @param matches Matches.
	 * @param size    Size.
	 */
	private void calculateTrainPerformance(int matches, int size) {
		double performance = 100.0 * matches / size;
		trainSize = size;
		trainMatches = matches;
		trainPerformance = Numbers.getBigDecimal(performance, performanceDecimals);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected long calculateTotalWork() throws Throwable {
		return patternSourceTraining.size() * epochs;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void compute() throws Throwable {
		validate();

		/* Clear messages. */
		clearMessage();
		clearStatusLabel(STATUS_ERROR, LABEL_ERROR);
		clearStatusLabel(STATUS_FLAT, LABEL_FLAT);
		clearStatusLabel(STATUS_SCORE, LABEL_SCORE);
		clearStatusLabel(STATUS_PROCESSING, LABEL_PROCESSING);

		boolean restored = false;
		if (saveNetworkData) {

			/* Get possible files. */
			List<File> files = getFiles();
			if (files.isEmpty()) {
				file = getNewFile(files);
			} else {

				/* Check whether to create a new file or use an existing file. */
				Option newFile = new Option();
				newFile.setKey("NEW-FILE");
				newFile.setText("Create a new file");
				newFile.setCloseWindow(true);
				Option useExisting = new Option();
				useExisting.setKey("USE-EXISTING");
				useExisting.setText("Use an existing file");
				useExisting.setCloseWindow(true);
				Option cancel = new Option();
				cancel.setKey("CANCEL");
				cancel.setText("Cancel");
				cancel.setDefaultClose(true);
				cancel.setCloseWindow(true);
				Alert alert = new Alert();
				alert.setTitle("Network file");
				alert.setText("Please select the network file");
				alert.setOptions(newFile, useExisting, cancel);
				Option result = alert.show();
				if (result.equals(cancel)) {
					setCancelled();
					return;
				}
				if (result.equals(newFile)) {
					file = getNewFile(files);
				} else {
					FileChooser chooser = new FileChooser(new File(filePath));
					chooser.setDialogTitle("Please, select a network file");
					chooser.setDialogType(FileChooser.OPEN_DIALOG);
					chooser.setFileSelectionMode(FileChooser.FILES_ONLY);
					chooser.setAcceptAllFileFilterUsed(true);
					chooser.setFileFilter(new TrainerFileFilter());
					file = null;
					if (chooser.showDialog(null) == FileChooser.APPROVE_OPTION) {
						file = chooser.getSelectedFile();
					} else {
						setCancelled();
						return;
					}
				}
			}
			setTitle(file.getName());
			/* Restore the network. */
			updateStatusLabel(STATUS_PROCESSING, LABEL_PROCESSING, "Restoring the network data...");
			restored = restore();
			clearStatusLabel(STATUS_PROCESSING, LABEL_PROCESSING);
		}

		/* Calculate and register total work. */
		long totalWork = calculateTotalWork();
		long workDone = 0;
		setTotalWork(totalWork);

		/* Register minimum error and maximum performances. */
		BigDecimal maxTestPerformance = null;
		BigDecimal maxTrainPerformance = null;

		/* Lists to trace errors and performances. */
		List<Trace> traceFlat = new ArrayList<>();
		List<Trace> traceScore = new ArrayList<>();

		if (restored) {
			/* Calculate test performance. */
			if (patternSourceTest != null) {
				calculateTestPerformance();
				if (isCancelled()) {
					return;
				}
				if (maxTestPerformance == null
					|| testPerformance.compareTo(maxTestPerformance) > 0) {
					maxTestPerformance = testPerformance;
				}
				calculateTrainPerformance(testMatches, testSize);
				bestTrace =
					new Trace(Numbers.getBigDecimal(0,
						errorDecimals), testMatches, testSize, testPerformance, testMatches,
						testSize,
						testPerformance);
			}
		}

		/* Iterate epochs. */
		int patternsSize = patternSourceTraining.size();
		boolean scanFlat = true;
		TreeMap<Double, Integer> scoreMap = new TreeMap<>((a, b) -> (Double.compare(a, b) * -1));
		double iterationError = 0;

		testSize = 0;
		testMatches = 0;
		testPerformance = null;
		trainSize = 0;
		trainMatches = 0;
		trainPerformance = null;
		bestTrace = null;

		for (int epoch = 1; epoch <= epochs; epoch++) {

			/* List of pattern indexes. */
			int[] patternIndexes = new int[patternSourceTraining.size()];

			if (scanFlat) {
				if (shuffle) patternSourceTraining.shuffle();
				for (int i = 0; i < patternIndexes.length; i++) {
					patternIndexes[i] = i;
				}
			} else {
				int scoreSize = random.nextInt(patternsSize / 2) + 1;
				int[] scoreIndexes = new int[scoreSize];
				Iterator<Integer> iter = scoreMap.values().iterator();
				int index = 0;
				while (iter.hasNext()) {
					scoreIndexes[index++] = iter.next();
					if (index >= scoreSize) break;
				}
				index = 0;
				for (int i = 0; i < patternIndexes.length; i++) {
					patternIndexes[i] = scoreIndexes[index++];
					if (index >= scoreSize) index = 0;
				}
			}

			/*
			 * Reset the iteration error and the number of matches to calculate the
			 * iteration performance.
			 */
			iterationError = 0;
			Distance distance = new DistanceEuclidean();
			int matches = 0;
			scoreMap.clear();
			for (int i = 0; i < patternIndexes.length; i++) {

				/* Check cancelled. */
				if (isCancelRequested()) {
					setCancelled();
					break;
				}

				/* Work done and notify work. */
				workDone += 1;
				StringBuilder work = new StringBuilder();
				work.append("Epoch " + epoch);
				work.append(" Pattern " + (i + 1));
				update(work.toString(), workDone, totalWork);

				/* Process the pattern. */
				int index = patternIndexes[i];
				Pattern pattern = patternSourceTraining.get(index);
				double[] patternOutput = pattern.getOutputValues();
				double[] networkOutput = network.forward(pattern.getInputValues());
				if (pattern.matches(networkOutput)) {
					matches += 1;
				}

				/* Total error. */
				double patternError = distance.distance(patternOutput, networkOutput);
				iterationError += patternError;
				if ((i + 1) % getProgressModulus() == 0 || (i + 1) >= patternIndexes.length) {
					BigDecimal error =
						Numbers.getBigDecimal(iterationError / (i + 1), errorDecimals);
					updateStatusLabel(STATUS_ERROR, LABEL_ERROR,
						trace(error, maxTrainPerformance, maxTestPerformance));
				}

				/* Add the error and pattern index to the score map. */
				scoreMap.put(patternError, index);

				/* Errors or deltas and backward. Discard the return vector. */
				double[] networkErrors = subtract(patternOutput, networkOutput);
				network.backward(networkErrors);
			}

			/* Last network unfold if history size is not a multiple of the source size. */
			network.unfold();

			/* Check cancelled. */
			if (isCancelled()) break;

			/* Save data. */
			boolean save = false;

			/* Calculate iteration (training) performance. */
			calculateTrainPerformance(matches, patternSourceTraining.size());
			if (maxTrainPerformance == null
				|| trainPerformance.compareTo(maxTrainPerformance) > 0) {
				maxTrainPerformance = trainPerformance;
				save = (maxTestPerformance == null);
			}

			/* Calculate test performance. */
			if (patternSourceTest != null) {
				calculateTestPerformance();
				/* Check cancelled. */
				if (isCancelled()) {
					break;
				}
				if (maxTestPerformance == null
					|| testPerformance.compareTo(maxTestPerformance) > 0) {
					maxTestPerformance = testPerformance;
					save = true;
				}
			}

			if (saveNetworkData && save) {
				updateStatusLabel(STATUS_PROCESSING, LABEL_PROCESSING,
					"Saving the network data...");
				save();
				clearStatusLabel(STATUS_PROCESSING, LABEL_PROCESSING);
			}

			/* History of epoch errors. */
			BigDecimal error = Numbers.getBigDecimal(iterationError / patternsSize, errorDecimals);
			pushTrace(scanFlat ? traceFlat : traceScore, error);
			updateStatusLabel(STATUS_FLAT, LABEL_FLAT, getTraceMessage(traceFlat));
			updateStatusLabel(STATUS_SCORE, LABEL_SCORE, getTraceMessage(traceScore));

			/* Performance message when score factor is 0.0. */
			StringBuilder msg = new StringBuilder();
			msg.append("Best performance -> train (");
			msg.append(bestTrace.trainMatches);
			msg.append("/");
			msg.append(bestTrace.trainSize);
			msg.append(", ");
			msg.append(bestTrace.trainPerformance);
			msg.append(")");
			if (testPerformance != null) {
				msg.append(" test (");
				msg.append(bestTrace.testMatches);
				msg.append("/");
				msg.append(bestTrace.testSize);
				msg.append(", ");
				msg.append(bestTrace.testPerformance);
				msg.append(")");
			}
			updateStatusLabel(STATUS_PROCESSING, LABEL_PROCESSING, msg.toString());

			/* Commute scan flat. */
			scanFlat = !scanFlat;
		}
	}

	/**
	 * Return the list of files with the path, root and extension.
	 * 
	 * @return The list of accepted files.
	 */
	private List<File> getFiles() {
		List<File> files = new ArrayList<>();
		File path = new File(filePath);
		File[] filesInPath = path.listFiles(new TrainerFileFilter());
		for (File file : filesInPath) {
			files.add(file);
		}
		return files;
	}

	/**
	 * Return a new valid file name.
	 * 
	 * @param files The list of current files for the network.
	 * @return A new valid file name.
	 */
	private File getNewFile(List<File> files) {
		int index = 0;
		for (File file : files) {
			String name = file.getName();
			int dot = name.lastIndexOf('.');
			name = name.substring(0, dot);
			int hyphen = name.lastIndexOf('-');
			String indexSuffix = name.substring(hyphen + 1);
			int fileIndex = Integer.valueOf(indexSuffix);
			if (fileIndex > index) {
				index = fileIndex;
			}
		}
		index++;
		String fileName =
			fileRoot + "-" + Strings.leftPad(Integer.toString(index), 2, "0") + "." + fileExtension;
		File file = new File(filePath, fileName);
		return file;
	}

	/**
	 * Return the trace message.
	 * 
	 * @param trace The trace list.
	 * @return The message.
	 */
	private String getTraceMessage(List<Trace> trace) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < trace.size(); i++) {
			if (i > 0) b.append(", ");
			b.append(getTraceMessageSimple(trace.get(i)));
		}
		return b.toString();
	}

	/**
	 * Return a simple message for the trace.
	 * 
	 * @param trace The trace.
	 * @return The message.
	 */
	private String getTraceMessageSimple(Trace trace) {
		StringBuilder b = new StringBuilder();
		b.append("(");
		b.append(trace.error);
		if (trace.trainPerformance != null) {
			b.append(", ");
			b.append(trace.trainPerformance);
		}
		if (trace.testPerformance != null) {
			b.append(", ");
			b.append(trace.testPerformance);
		}
		b.append(")");
		return b.toString();
	}

	/**
	 * Push the trace to the trace list and set the best trace.
	 * 
	 * @param traces The trace.
	 * @param error  The error.
	 */
	private void pushTrace(List<Trace> traces, BigDecimal error) {
		Trace trace =
			new Trace(error, trainMatches, trainSize, trainPerformance, testMatches,
				testSize, testPerformance);
		if (bestTrace == null) {
			bestTrace = trace;
		} else {
			if (trace.testPerformance != null) {
				if (trace.testPerformance.compareTo(bestTrace.testPerformance) >= 0) {
					bestTrace = trace;
				}
			} else {
				if (trace.trainPerformance != null) {
					if (trace.trainPerformance.compareTo(bestTrace.trainPerformance) >= 0) {
						bestTrace = trace;
					}
				}
			}
		}
		traces.add(trace);
		if (traces.size() > maxTrace) traces.remove(0);
	}

	/**
	 * Restore the network data.
	 * 
	 * @return A boolean indicating that the network was restored.
	 * @throws IOException
	 */
	private boolean restore() throws IOException {
		if (file != null && file.exists() && file.length() != 0) {
			FileInputStream fi = new FileInputStream(file);
			BufferedInputStream bi = new BufferedInputStream(fi);
			network.restore(bi);
			bi.close();
			fi.close();
			return true;
		}
		return false;
	}

	/**
	 * Save the network data.
	 * 
	 * @throws IOException
	 */
	private void save() throws IOException {
		if (file != null) {
			String name = file.getName() + ".save";
			File fileSave = new File(file.getParentFile(), name);
			FileOutputStream fo = null;
			BufferedOutputStream bo = null;
			try {
				fo = new FileOutputStream(fileSave);
				bo = new BufferedOutputStream(fo);
				network.save(bo);
				bo.close();
				fo.close();
				if (file.exists()) {
					file.delete();
				}
				fileSave.renameTo(file);
			} finally {
				bo.close();
				fo.close();
				if (fileSave.exists()) {
					fileSave.delete();
				}
			}
		}
	}

	/**
	 * Set the number of epochs to process.
	 * 
	 * @param epochs The number of epochs to process.
	 */
	public void setEpochs(int epochs) {
		this.epochs = epochs;
	}

	/**
	 * Set the file path.
	 * 
	 * @param filePath The parent file path.
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * Set the file root name.
	 * 
	 * @param fileRoot The file root name.
	 */
	public void setFileRoot(String fileRoot) {
		this.fileRoot = fileRoot;
	}

	/**
	 * Set the file extension.
	 * 
	 * @param fileExtension The file extension.
	 */
	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}

	/**
	 * Set the shuffle flag.
	 * 
	 * @param shuffle A boolean.
	 */
	public void setShuffle(boolean shuffle) {
		this.shuffle = shuffle;
	}

	/**
	 * Set the network.
	 * 
	 * @param network The network.
	 */
	public void setNetwork(Network network) {
		this.network = network;
	}

	/**
	 * Set the test pattern source.
	 * 
	 * @param patternSource The test pattern source.
	 */
	public void setPatternSourceTest(PatternSource patternSource) {
		this.patternSourceTest = patternSource;
	}

	/**
	 * Set the training pattern source.
	 * 
	 * @param patternSource The training pattern source.
	 */
	public void setPatternSourceTraining(PatternSource patternSource) {
		this.patternSourceTraining = patternSource;
	}

	/**
	 * Set whether network data should be saved/restored.
	 * 
	 * @param saveNetworkData A boolean.
	 */
	public void setSaveNetworkData(boolean saveNetworkData) {
		this.saveNetworkData = saveNetworkData;
	}

	/**
	 * Validate the task after setting the network, the files and the sources.
	 */
	private void validate() {
		if (network == null) {
			throw new IllegalStateException("The network must be set");
		}
		if (patternSourceTraining == null) {
			throw new IllegalStateException("The training pattern source must be set");
		}
		if (saveNetworkData) {
			if (filePath == null) {
				throw new IllegalStateException("The file path is required to save the network data");
			}
			File path = new File(filePath);
			if (!path.exists()) {
				throw new IllegalStateException("The file path does not exist");
			}
			if (!path.isDirectory()) {
				throw new IllegalStateException("The file path must be a directory");
			}
			if (fileRoot == null) {
				throw new IllegalStateException(
					"The file root is required to save the network data");
			}
			if (fileExtension == null) {
				fileExtension = "dat";
			}
		}
		
		/* Check shuffle supported. */
		if (shuffle) {
			try {
				patternSourceTraining.shuffle();
			} catch (Exception exc) {
				shuffle = false;
			}
		}
	}

}
