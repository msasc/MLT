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

package com.mlt.ml.network.nodes.optimizers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;

import com.mlt.ml.function.Range;
import com.mlt.ml.network.nodes.WeightsOptimizer;
import com.mlt.util.FixedSizeList;
import com.mlt.util.Matrix;

/**
 * Adaptative optimizer.
 *
 * @author Miquel Sas
 */
public class AdaOptimizer extends WeightsOptimizer {

	/**
	 * Gradients manager.
	 */
	private class Gradients {

		/**
		 * Callable to calculate the gradients concurrently.
		 */
		private class Calculator implements Callable<Void> {
			private Range range;

			private Calculator(Range range) {
				this.range = range;
			}

			@Override
			public Void call() throws Exception {
				gradients(range.getStart(), range.getEnd());
				return null;
			}
		}

		/**
		 * Callable function to collect the gradients from the raw queue.
		 */
		private class Collector implements Callable<Void> {
			private Range range;

			private Collector(Range range) {
				this.range = range;
			}

			@Override
			public Void call() throws Exception {
				switch (type) {
				case ADD:
					gradientsADD(range.getStart(), range.getEnd());
					break;
				case SMA:
					gradientsSMA(range.getStart(), range.getEnd());
					break;
				case WMA:
					gradientsWMA(range.getStart(), range.getEnd());
					break;
				default:
					break;
				}
				return null;
			}
		}

		/** Queue of input gradients. */
		private FixedSizeList<double[][]> inputQueue;
		/** Size of the input queue. */
		private int inputQueueSize = 5;
		/** Queue of output gradients. */
		private FixedSizeList<double[][]> outputQueue;
		/** Size of the output queue. */
		private int outputQueueSize = 5;
		/** Queue of gradients deltas. */
		private FixedSizeList<double[][]> deltasQueue;
		/** Size of the deltas queue. */
		private int deltasQueueSize = 5;
		
		/** Collector type. */
		private CollectorType type = CollectorType.WMA;
		/** List of collectors to process concurrently. */
		private List<Collector> collectors;
		/** List of functions to calculate gradients. */
		private List<Calculator> calculators;

		/**
		 * Constructor.
		 */
		private Gradients() {
			inputQueue = new FixedSizeList<>(inputQueueSize);
			outputQueue = new FixedSizeList<>(outputQueueSize);
			deltasQueue = new FixedSizeList<>(deltasQueueSize);
		}

		/**
		 * @param start Start input index.
		 * @param end   End input index.
		 */
		private void gradients(int start, int end) {
			int outputSize = getNode().getOutputSize();
			double[] inputValues = getNode().getInputValues();
			double[] outputDeltas = getNode().getOutputDeltas();
			for (int in = start; in <= end; in++) {
				for (int out = 0; out < outputSize; out++) {
					matrix[in][out] = inputValues[in] * outputDeltas[out];
				}
			}
		}

		/**
		 * Consurrently (by input index) calculates the addition of the raw gradients
		 * queue.
		 * 
		 * @param start Start input index.
		 * @param end   End input index.
		 */
		private void gradientsADD(int start, int end) {
			if (inputQueue.isEmpty()) {
				return;
			}
			int outputSize = getNode().getOutputSize();
			Iterator<double[][]> iter = inputQueue.iterator();
			while (iter.hasNext()) {
				double[][] gradients = iter.next();
				for (int in = start; in <= end; in++) {
					for (int out = 0; out < outputSize; out++) {
						matrix[in][out] += gradients[in][out];
					}
				}
			}
		}

		/**
		 * Consurrently (by input index) calculates the SMA of the raw gradients queue.
		 * 
		 * @param start Start input index.
		 * @param end   End input index.
		 */
		private void gradientsSMA(int start, int end) {
			if (inputQueue.isEmpty()) {
				return;
			}
			int outputSize = getNode().getOutputSize();
			Iterator<double[][]> iter = inputQueue.iterator();
			while (iter.hasNext()) {
				double[][] gradients = iter.next();
				for (int in = start; in <= end; in++) {
					for (int out = 0; out < outputSize; out++) {
						matrix[in][out] += gradients[in][out];
					}
				}
			}
			double size = ((double) inputQueue.size());
			for (int in = start; in <= end; in++) {
				for (int out = 0; out < outputSize; out++) {
					matrix[in][out] /= size;
				}
			}
		}

		/**
		 * Consurrently (by input index) calculates the WMA of the raw gradients queue.
		 * 
		 * @param start Start input index.
		 * @param end   End input index.
		 */
		private void gradientsWMA(int start, int end) {
			if (inputQueue.isEmpty()) {
				return;
			}
			int outputSize = getNode().getOutputSize();
			Iterator<double[][]> iter = inputQueue.iterator();
			double weight = 1;
			double total = 0;
			while (iter.hasNext()) {
				double[][] gradients = iter.next();
				for (int in = start; in <= end; in++) {
					for (int out = 0; out < outputSize; out++) {
						matrix[in][out] += (gradients[in][out] * weight);
					}
				}
				total += weight;
				weight += 1;
			}
			for (int in = start; in <= end; in++) {
				for (int out = 0; out < outputSize; out++) {
					matrix[in][out] /= total;
				}
			}
		}
		
		/**
		 * Initialize and add a deltas matrix.
		 */
		private void processDeltas() {

			/* Input and output sizes. */
			int inputSize = getNode().getInputSize();
			int outputSize = getNode().getOutputSize();
			
			/* Add a clean deltas matrix. */
			deltasQueue.add(new double[inputSize][outputSize]);
		}

		/**
		 * Calculate the gradiens and let the result in the gradients member.
		 */
		private void processGradients() {

			/* Input and output sizes. */
			int inputSize = getNode().getInputSize();
			int outputSize = getNode().getOutputSize();

			/* Initialize the list of calculators if required. */
			if (calculators == null) {
				calculators = new ArrayList<>();
				int module = Runtime.getRuntime().availableProcessors();
				List<Range> ranges = Range.getRanges(inputSize, module);
				for (Range range : ranges) {
					calculators.add(new Calculator(range));
				}
			}

			/* Initialize the result gradients. */
			matrix = new double[inputSize][outputSize];

			/* Process functions. */
			ForkJoinPool.commonPool().invokeAll(calculators);

			/* Add input gradients. */
			inputQueue.add(matrix);
		}

		/**
		 * Process the queue of raw gradients and store the result in the gradients
		 * member.
		 */
		private void processInputQueue() {

			/* Type == NONE, just retrieve the last one. */
			if (type == CollectorType.NONE) {
				outputQueue.add(inputQueue.getLast());
				return;
			}

			/* Input and output sizes. */
			int inputSize = getNode().getInputSize();
			int outputSize = getNode().getOutputSize();

			/* Initialize the list of collectors if required. */
			if (collectors == null) {
				collectors = new ArrayList<>();
				int module = Runtime.getRuntime().availableProcessors();
				List<Range> ranges = Range.getRanges(inputSize, module);
				for (Range range : ranges) {
					collectors.add(new Collector(range));
				}
			}

			/* Initialize the result gradients. */
			matrix = new double[inputSize][outputSize];

			/* Process functions. */
			ForkJoinPool.commonPool().invokeAll(collectors);

			/* Add gradients to the output queue. */
			outputQueue.add(matrix);
		}
	}

	/**
	 * Types of operations on gradients queue.
	 */
	public static enum CollectorType {
		ADD, SMA, WMA, NONE
	}

	/** Learning rates. */
	private double[][] learningRates;
	/** Momentums. */
	private double[][] momentums;
	/** Gradients manager (worker). */
	private Gradients gm;

	/**
	 * Transient matrix use in concurrent calculations. The matrix is first
	 * initialized, and concurrent processes acces only a range of indexes.
	 */
	private double[][] matrix;

	/**
	 * Constructor.
	 */
	public AdaOptimizer() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void backward(int start, int end) {

		/* Necessary data from the weights node. */
		int outputSize = getNode().getOutputSize();
		double[] inputDeltas = getNode().getInputDeltas();
		double[] outputDeltas = getNode().getOutputDeltas();
		double[][] weights = getNode().getWeights();
		double[][] gradients = gm.outputQueue.getLast();
		double[][] weightDeltas = gm.deltasQueue.getLast();

		/* Iterate input indexes from start to end. */
		for (int in = start; in <= end; in++) {

			/* Input value and initialize the input delta. */
			double inputDelta = 0;

			/* Iterate all output indexes for each input index. */
			for (int out = 0; out < outputSize; out++) {

				double outputDelta = outputDeltas[out];
				double weight = weights[in][out];
				inputDelta += (weight * outputDelta);

				/* Output delta and gradient (input value * output delta). */
				double gradient = gradients[in][out];

				/* Current learning rate and momentum and last weight delta. */
				double learningRate = learningRates[in][out];
				double momentum = momentums[in][out];
				double lastDelta = weightDeltas[in][out];

				/* Calculate the weight delta. */
				double prevDelta = momentum * lastDelta;
				double nextDelta = (1 - momentum) * (learningRate * gradient);
				double weightDelta = prevDelta + nextDelta;

				/* Apply weight delta to weight. */
				weights[in][out] += weightDelta;

				/* Save weight delta and gradient for next iteration. */
				weightDeltas[in][out] = weightDelta;
			}

			/* Save input delta for intermediate layers. */
			inputDeltas[in] = inputDelta;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void finalizeBackward() {}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription() {
		StringWriter s = new StringWriter();
		PrintWriter p = new PrintWriter(s);
		p.print("GQ: ");
		p.print(gm.type);
		if (gm.type != CollectorType.NONE) {
			p.print(" RS: " + gm.inputQueueSize);
		}
		p.close();
		return s.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initializeBackward() {

		/* Check internal data initialized. */
		if (learningRates == null) {

			/* Input and output sizes. */
			int inputSize = getNode().getInputSize();
			int outputSize = getNode().getOutputSize();

			/* Learning rates. */
			learningRates = new double[inputSize][outputSize];
			Matrix.fill(learningRates, 0.01);

			/* Momentums. */
			momentums = new double[inputSize][outputSize];
			Matrix.fill(momentums, 0.0);

			gm = new Gradients();
		}

		/* Calculate gradients and add them to the input queue. */
		gm.processGradients();
		/* Process the input queue and add the result to the output queue. */
		gm.processInputQueue();
		/* Initialize and add a deltas matrix. */
		gm.processDeltas();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initializeOptimizer() {
		learningRates = null;
		momentums = null;
		gm = null;
	}
}
