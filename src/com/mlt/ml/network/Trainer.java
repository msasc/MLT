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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

import com.mlt.desktop.Alert;
import com.mlt.desktop.FileChooser;
import com.mlt.desktop.Option;
import com.mlt.ml.data.Pattern;
import com.mlt.ml.data.PatternSource;
import com.mlt.ml.function.Distance;
import com.mlt.ml.function.Matcher;
import com.mlt.ml.function.distance.DistanceEuclidean;
import com.mlt.task.Task;
import com.mlt.util.Lists;
import com.mlt.util.Numbers;
import com.mlt.util.Strings;
import com.mlt.util.Vector;

/**
 * Trainer of neural networks.
 *
 * @author Miquel Sas
 */
public class Trainer extends Task {

	/**
	 * File filter.
	 */
	private class TrainerFileFilter
		extends javax.swing.filechooser.FileFilter
		implements java.io.FileFilter {

		@Override
		public boolean accept(File file) {
			if (file != null) {
				if (file.isDirectory()) {
					return true;
				}
				String name = file.getName();
				if (!name.toLowerCase().endsWith(".dat")) {
					return false;
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

	/** Label key to show the current error. */
	private static final String LABEL_ERROR = "LABEL-ERROR";
	/** Label key of an additional processes. */
	private static final String LABEL_PROCESSING = "LABEL-PROCESSING";

	/** Option new file. */
	private static final String OPTION_NEW_FILE = "NEW-FILE";
	/** Option use existing. */
	private static final String OPTION_USE_EXISTING = "USE-EXISTING";
	/** Option cancel. */
	private static final String OPTION_CANCEL = "CANCEL";
	/** Progress key of additional processes. */
	private static final String PROGRESS_PROCESSING = "PROGRESS-PROCESSING";

	/** Status key to show the current error. */
	private static final String STATUS_ERROR = "STATUS-ERROR";
	/** Status key of additional processes. */
	private static final String STATUS_PROCESSING = "STATUS-PROCESSING";

	/** The network. */
	private Network network;
	/** The training pattern source. */
	private PatternSource sourceTrain;
	/** The test pattern source. */
	private PatternSource sourceTest;
	/** The number of epochs or turns to the full list of patterns. */
	private int epochs = 500;
	/** The file to save the network. */
	private File file;
	/** Shuffle flag indicator. */
	private boolean shuffle = true;
	/** Score flag indicator. */
	private boolean score = true;
	/** Error decimals. */
	private int errorDecimals = 8;
	/** Percentages decimals. */
	private int percentageDecimals = 2;

	/** File path. */
	private String filePath;
	/** File root name. */
	private String fileRoot;
	/** Distance function. */
	private Distance distance = new DistanceEuclidean();
	/** History of train metrics. */
	private List<Metrics> trainMetricsHistory = new ArrayList<>();
	/** History of test metrics. */
	private List<Metrics> testMetricsHistory = new ArrayList<>();

	/**
	 * A boolean that indicates if network data should be saved.When the trainer is
	 * is used to performance report along a fixed number of epochs, there is no
	 * interest in saving the network data.
	 */
	private boolean saveNetworkData = true;
	/**
	 * A boolean that indicates that the training process will be used to generate a
	 * performance report.
	 */
	private boolean generateReport = false;
	/** The name of the report file. */
	private String reportFile;

	/**
	 * Constructor.
	 */
	public Trainer() {
		this(Locale.getDefault());
	}

	/**
	 * Constructor.
	 * 
	 * @param locale Locale.
	 */
	public Trainer(Locale locale) {
		super(locale);
		addStatus(STATUS_ERROR);
		addStatus(STATUS_PROCESSING);
		setConsoleRequired(true);
	}

	/**
	 * Calculate the metrics.
	 * 
	 * @param train Train/test indicator.
	 * @return The metrics.
	 */
	private Metrics calculateMetrics(boolean train) {

		removeStatusLabel(STATUS_PROCESSING, LABEL_PROCESSING);
		removeStatusProgress(STATUS_PROCESSING, PROGRESS_PROCESSING);

		PatternSource source = train ? sourceTrain : sourceTest;
		int length = network.getOutputSize();
		int size = source.size();
		Metrics metrics = new Metrics(length, size);
		metrics.setLabel(train ? "Train" : "Test");

		/* A first iteration to calculate the mean of data. */
		for (int i = 0; i < size; i++) {

			if (isCancelRequested()) {
				setCancelled();
				return metrics;
			}

			int index = i + 1;
			double percent = (double) (index * 100) / (double) size;
			StringBuilder msg = new StringBuilder();
			msg.append("Calculating ");
			msg.append(train ? "train" : "test");
			msg.append(" data mean: ");
			msg.append(index);
			msg.append(" of ");
			msg.append(size);
			msg.append(" (");
			msg.append(Numbers.getBigDecimal(percent, percentageDecimals));
			msg.append("%)");
			updateStatusProgress(STATUS_PROCESSING, PROGRESS_PROCESSING, msg, index, size);

			Pattern pattern = source.get(i);
			double[] patternOutput = pattern.getOutputValues();
			metrics.compute(patternOutput);
		}

		/* A second iteration to calculate the rest of metrics. */
		for (int i = 0; i < size; i++) {

			if (isCancelRequested()) {
				setCancelled();
				return metrics;
			}

			int index = i + 1;
			double percent = (double) (index * 100) / (double) size;
			StringBuilder msg = new StringBuilder();
			msg.append("Calculating ");
			msg.append(train ? "train" : "test");
			msg.append(" rest of metrics: ");
			msg.append(index);
			msg.append(" of ");
			msg.append(size);
			msg.append(" (");
			msg.append(Numbers.getBigDecimal(percent, percentageDecimals));
			msg.append("%)");
			updateStatusProgress(STATUS_PROCESSING, PROGRESS_PROCESSING, msg, index, size);

			Pattern pattern = source.get(i);
			double[] patternInput = pattern.getInputValues();
			double[] patternOutput = pattern.getOutputValues();
			double[] networkOutput = network.calculate(patternInput);
			metrics.compute(patternOutput, networkOutput);
		}

		removeStatusProgress(STATUS_PROCESSING, PROGRESS_PROCESSING);
		return metrics;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected long calculateTotalWork() throws Throwable {
		setTotalWork(sourceTrain.size() * epochs);
		return getTotalWork();
	}

	/**
	 * Check and optionally restore a network file.
	 */
	private boolean checkRestore() throws Exception {
		List<File> files = getFiles();
		if (files.isEmpty()) {
			file = getNewFile(files);
		} else {
			Option option = getFileOption();
			if (option.equals(OPTION_CANCEL)) {
				setCancelled();
				return false;
			}
			if (option.equals(OPTION_NEW_FILE)) {
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
					return false;
				}
			}
		}
		setTitle(file.getName());
		updateStatusLabel(STATUS_PROCESSING, LABEL_PROCESSING, "Restoring the network data...");
		if (file != null && file.exists() && file.length() != 0) {
			FileInputStream fi = new FileInputStream(file);
			BufferedInputStream bi = new BufferedInputStream(fi);
			network.restore(bi);
			bi.close();
			fi.close();
		}
		clearStatusLabel(STATUS_PROCESSING, LABEL_PROCESSING);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void compute() throws Throwable {

		/* Validate. */
		validate();

		/* Initialize the network. */
		network.initialize();

		/* Clear messages. */
		consoleClear();
		clearMessage();
		clearStatusLabel(STATUS_ERROR, LABEL_ERROR);
		clearStatusLabel(STATUS_PROCESSING, LABEL_PROCESSING);
		updateProgress(0, 0);

		/* Check restore or create a new file. */
		if (saveNetworkData) {
			if (!checkRestore()) {
				return;
			}
		}

		/* Clear history. */
		trainMetricsHistory.clear();
		testMetricsHistory.clear();

		/* Calculate metrics. */
		Metrics trainMetrics = calculateMetrics(true);
		Metrics testMetrics = calculateMetrics(false);
		trainMetricsHistory.add(trainMetrics);
		testMetricsHistory.add(testMetrics);
		printMetrics();

		/* Iterate epochs. Start with a flat scan. */
		calculateTotalWork();
		long totalWork = getTotalWork();
		long workDone = 0;
		boolean scanFlat = true;
		Matcher match = sourceTrain.getMatch();
		TreeMap<Double, Integer> scoreMap = new TreeMap<>((a, b) -> (Double.compare(a, b) * -1));
		for (int epoch = 1; epoch <= epochs; epoch++) {

			/* Check cancelled. */
			if (isCancelRequested()) {
				setCancelled();
				break;
			}

			/*
			 * List of pattern indexes. To build the list of indexes, first decide whether
			 * to scan flat the training source or to scan a fraction of the the worst
			 * scored patterns.
			 */
			int[] indexes = null;
			if (scanFlat) {
				if (shuffle) {
					sourceTrain.shuffle();
				}
				indexes = getIndexesFlat(sourceTrain.size());
			} else {
				indexes = getIndexesScore(sourceTrain.size(), scoreMap);
			}

			int size = indexes.length;
			double epochError = 0;
			double matches = 0;
			for (int i = 0; i < size; i++) {

				/* Check cancelled. */
				if (isCancelRequested()) {
					setCancelled();
					break;
				}

				/* Work done and notify work. */
				workDone += 1;
				update(getMessage(epoch, i + 1, size), workDone, totalWork);

				/* Process the pattern. */
				int index = indexes[i];
				Pattern pattern = sourceTrain.get(index);
				double[] patternOutput = pattern.getOutputValues();
				double[] networkOutput = network.forward(pattern.getInputValues());
				if (match.match(patternOutput, networkOutput)) {
					matches += 1;
				}
				epochError += distance.distance(patternOutput, networkOutput);
				BigDecimal error = Numbers.getBigDecimal(epochError / (i + 1), errorDecimals);
				BigDecimal performance = Numbers.getBigDecimal(matches / (i + 1), 4);
				StringBuilder msg = new StringBuilder();
				msg.append("Epoch error ");
				msg.append(scanFlat ? "flat: " : "score: ");
				msg.append(error);
				msg.append(", performance: ");
				msg.append(performance);
				updateStatusLabel(STATUS_ERROR, LABEL_ERROR, msg);

				/* Add the error and pattern index to the score map. */
				scoreMap.put(error.doubleValue(), index);

				/* Errors or deltas and backward. Discard the return vector. */
				double[] networkDeltas = Vector.subtract(patternOutput, networkOutput);
				network.backward(networkDeltas);

				/* Adjust internal per step. */
				network.adjustStep();
			}

			/* Last network unfold if history size is not a multiple of the source size. */
			network.unfold();

			/* Adjust internals per iteration or batch. */
			network.adjustBatch();

			/* Check cancelled. */
			if (isCancelled()) {
				break;
			}

			/*
			 * Save data if both performances and errors are better. Save only if the scan
			 * has been a flat scan.
			 */

			/* Change scan flag. */
			scanFlat = score ? !scanFlat : true;
		}

		/* Compute has finished, generate a report if required. */
		if (!isCancelled() && generateReport) {
//			if (reportFile == null) {
//				reportFile = "report";
//			}
//			File file = new File(filePath, reportFile + ".txt");
//			FileWriter fw = new FileWriter(file, true);
//			fw.append(getReport());
//			fw.close();
		}
	}

	/**
	 * @return The file option.
	 */
	private Option getFileOption() {

		Option newFile = new Option();
		newFile.setKey(OPTION_NEW_FILE);
		newFile.setText("Create a new file");
		newFile.setCloseWindow(true);

		Option useExisting = new Option();
		useExisting.setKey(OPTION_USE_EXISTING);
		useExisting.setText("Use an existing file");
		useExisting.setCloseWindow(true);

		Option cancel = new Option();
		cancel.setKey(OPTION_CANCEL);
		cancel.setText("Cancel");
		cancel.setDefaultClose(true);
		cancel.setCloseWindow(true);

		Alert alert = new Alert();
		alert.setTitle("Network file");
		alert.setText("Please select the network file");
		alert.setOptions(newFile, useExisting, cancel);

		Option result = alert.show();
		return result;
	}

	/**
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
	 * @param size The size or number of indexes.
	 * @return The list of flat indexes.
	 */
	private int[] getIndexesFlat(int size) {
		int[] indexes = new int[size];
		for (int i = 0; i < size; i++) {
			indexes[i] = i;
		}
		return indexes;
	}

	/**
	 * @param size     The size or number of indexes.
	 * @param scoreMap The score map.
	 * @return The list of score indexes.
	 */
	private int[] getIndexesScore(int size, TreeMap<Double, Integer> scoreMap) {
		int scoreSize = ThreadLocalRandom.current().nextInt(size / 2) + 1;
		int[] scoreIndexes = new int[scoreSize];
		Iterator<Integer> iter = scoreMap.values().iterator();
		int index = 0;
		while (iter.hasNext()) {
			scoreIndexes[index++] = iter.next();
			if (index >= scoreSize) {
				break;
			}
		}
		List<Integer> indexes = new ArrayList<>();
		while (indexes.size() < size) {
			int i = ThreadLocalRandom.current().nextInt(scoreSize);
			indexes.add(scoreIndexes[i]);
		}
		return Lists.toIntegerArray(indexes);
	}

	/**
	 * @param epoch   The epoch.
	 * @param pattern The pattern index.
	 * @param size    The size or number of patterns.
	 * @return The process message.
	 */
	private String getMessage(int epoch, int pattern, int size) {
		double factor = (double) (pattern) / (double) size;
		BigDecimal percent = new BigDecimal(100.0 * factor).setScale(2, RoundingMode.HALF_UP);
		StringBuilder msg = new StringBuilder();
		msg.append("Epoch " + epoch + " of " + epochs);
		msg.append(" Pattern " + pattern);
		msg.append(" of ");
		msg.append(size);
		msg.append(" (" + percent + "%)");
		return msg.toString();
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
			fileRoot + "-" + Strings.leftPad(Integer.toString(index), 2, "0") + ".dat";
		File file = new File(filePath, fileName);
		return file;
	}

	/**
	 * @return The current performance report.
	 */
//	private String getReport() {
//
//		int size = trainPerformances.size();
//		int padError = errorDecimals + 4;
//		int padPerformance = performanceDecimals + 5;
//		int padIndex = Numbers.getDigits(size) + 2;
//
//		StringWriter s = new StringWriter();
//		PrintWriter p = new PrintWriter(s);
//		p.println(network.getDescription());
//
//		for (int i = 0; i < size; i++) {
//			int index = i + 1;
//			Performance train = trainPerformances.get(i);
//			Performance test = testPerformances.get(i);
//			p.print(Strings.rightPad(index, padIndex));
//			p.print(Strings.leftPad(train.error, padError));
//			p.print(Strings.leftPad(train.performance, padPerformance));
//			p.print(Strings.leftPad(test.error, padError));
//			p.print(Strings.leftPad(test.performance, padPerformance));
//			p.println();
//		}
//		p.println();
//		p.close();
//		return s.toString();
//	}

	private void printMetrics() {
		consoleClear();
		consolePrint(Metrics.getReport(trainMetricsHistory, testMetricsHistory));
	}

	/**
	 * Save the network data.
	 * 
	 * @throws IOException
	 */
	private void saveNetwork() throws IOException {
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
	 * @param distance The distance function.
	 */
	public void setDistance(Distance distance) {
		if (distance == null) throw new NullPointerException();
		this.distance = distance;
	}

	/**
	 * @param epochs The number of epochs to process.
	 */
	public void setEpochs(int epochs) {
		this.epochs = epochs;
	}

	/**
	 * @param errorDecimals The error decimals.
	 */
	public void setErrorDecimals(int errorDecimals) {
		this.errorDecimals = errorDecimals;
	}

	/**
	 * @param filePath The parent file path.
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * @param fileRoot The file root name.
	 */
	public void setFileRoot(String fileRoot) {
		this.fileRoot = fileRoot;
	}

	/**
	 * @param generateReport A boolean.
	 * @param reportFile     The name of the report file.
	 */
	public void setGenerateReport(boolean generateReport, String reportFile) {
		this.generateReport = generateReport;
		this.reportFile = reportFile;
		this.saveNetworkData = !generateReport;
	}

	/**
	 * @param network The network.
	 */
	public void setNetwork(Network network) {
		this.network = network;
	}

	/**
	 * @param patternSource The test pattern source.
	 */
	public void setPatternSourceTest(PatternSource patternSource) {
		this.sourceTest = patternSource;
	}

	/**
	 * @param patternSource The training pattern source.
	 */
	public void setPatternSourceTraining(PatternSource patternSource) {
		this.sourceTrain = patternSource;
	}

	/**
	 * @param percentageDecimals The percentage decimals.
	 */
	public void setPercentageDecimals(int percentageDecimals) {
		this.percentageDecimals = percentageDecimals;
	}

	/**
	 * @param score A boolean to train scored data.
	 */
	public void setScore(boolean score) {
		this.score = score;
	}

	/**
	 * @param shuffle A boolean indicating whether to shuffle sources.
	 */
	public void setShuffle(boolean shuffle) {
		this.shuffle = shuffle;
	}

	/**
	 * Validate the task after setting the network, the files and the sources.
	 */
	private void validate() {
		if (network == null) {
			throw new IllegalStateException("The network must be set");
		}
		if (sourceTrain == null) {
			throw new IllegalStateException("The training pattern source must be set");
		}
		if (sourceTest == null) {
			sourceTest = sourceTrain;
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
				throw new IllegalStateException("The file root is required to save the network data");
			}
		}

		/* Check shuffle supported. */
		if (shuffle) {
			try {
				sourceTrain.shuffle();
			} catch (Exception exc) {
				shuffle = false;
			}
		}
	}
}
