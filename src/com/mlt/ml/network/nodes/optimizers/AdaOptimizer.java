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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;

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
	 * Structure with gradients data.
	 */
	class Gradients {

		/**
		 * Callable to calculate the gradients concurrently.
		 */
		class Calculator implements Callable<Void> {
			int in;

			Calculator(int in) {
				this.in = in;
			}

			@Override
			public Void call() throws Exception {
				gradients(in);
				return null;
			}
		}

		/**
		 * Callable function to collect the gradients from the raw queue.
		 */
		class Collector implements Callable<Void> {
			int in;

			Collector(int in) {
				this.in = in;
			}

			@Override
			public Void call() throws Exception {
				switch (type) {
				case ADD:
					gradientsADD(in);
					break;
				case SMA:
					gradientsSMA(in);
					break;
				case WMA:
					gradientsWMA(in);
					break;
				default:
					break;
				}
				return null;
			}
		}

		/** Queue of raw gradients. */
		FixedSizeList<double[][]> queueRaw;
		/** Collector type. */
		CollectorType type = CollectorType.NONE;
		/** List of collectors to process concurrently. */
		List<Collector> collectors;
		/** List of functions to calculate gradients. */
		List<Calculator> calculators;
		/** Gradients concurrently calculated/collected. */
		double[][] gradients;

		/**
		 * Constructor.
		 */
		Gradients() {
			queueRaw = new FixedSizeList<>(historySize);
		}

		/**
		 * Add gradients to the raw queue.
		 * 
		 * @param gradients The gradients.
		 */
		void addRaw(double[][] gradients) {
			queueRaw.add(gradients);
		}

		/**
		 * Calculate the gradiens and let the result in the gradients member.
		 */
		void processGradients() {

			/* Input and output sizes. */
			int inputSize = getNode().getInputSize();
			int outputSize = getNode().getOutputSize();

			/* Initialize the list of calculators if required. */
			if (calculators == null) {
				calculators = new ArrayList<>();
				for (int in = 0; in < inputSize; in++) {
					calculators.add(new Calculator(in));
				}
			}

			/* Initialize the result gradients. */
			gradients = new double[inputSize][outputSize];

			/* Process functions. */
			ForkJoinPool.commonPool().invokeAll(calculators);
		}

		/**
		 * Process the queue of raw gradients and store the result in the gradients
		 * member.
		 */
		void processQueueRaw() {

			/* Type == NONE, just retrieve the last one. */
			if (type == CollectorType.NONE) {
				gradients = queueRaw.getLast();
				return;
			}

			/* Input and output sizes. */
			int inputSize = getNode().getInputSize();
			int outputSize = getNode().getOutputSize();

			/* Initialize the list of collectors if required. */
			if (collectors == null) {
				collectors = new ArrayList<>();
				for (int in = 0; in < inputSize; in++) {
					collectors.add(new Collector(in));
				}
			}

			/* Initialize the result gradients. */
			gradients = new double[inputSize][outputSize];

			/* Process functions. */
			ForkJoinPool.commonPool().invokeAll(collectors);
		}

		/**
		 * @param in The input index of the gradients to process.
		 */
		void gradients(int in) {
			int outputSize = getNode().getOutputSize();
			double[] inputValues = getNode().getInputValues();
			double[] outputDeltas = getNode().getOutputDeltas();
			for (int out = 0; out < outputSize; out++) {
				gradients[in][out] = inputValues[in] * outputDeltas[out];
			}
		}

		/**
		 * Consurrently (by input index) calculates the addition of the raw gradients
		 * queue.
		 * 
		 * @param in The input index of the gradients to process.
		 */
		void gradientsADD(int in) {
			if (queueRaw.isEmpty()) {
				return;
			}
			int outputSize = getNode().getOutputSize();
			Iterator<double[][]> iter = queueRaw.iterator();
			while (iter.hasNext()) {
				double[][] matrix = iter.next();
				for (int out = 0; out < outputSize; out++) {
					gradients[in][out] += matrix[in][out];
				}
			}
		}

		/**
		 * Consurrently (by input index) calculates the EMA of the raw gradients queue.
		 * 
		 * @param in The input index of the gradients to process.
		 */
		void gradientsEMA(int in) {
			if (queueRaw.isEmpty()) {
				return;
			}
			int outputSize = getNode().getOutputSize();
			Iterator<double[][]> iter = queueRaw.iterator();
			while (iter.hasNext()) {
				double[][] matrix = iter.next();
				for (int out = 0; out < outputSize; out++) {
					gradients[in][out] += matrix[in][out];
				}
			}
			for (int out = 0; out < outputSize; out++) {
				gradients[in][out] /= ((double) queueRaw.size());
			}
		}

		/**
		 * Consurrently (by input index) calculates the SMA of the raw gradients queue.
		 * 
		 * @param in The input index of the gradients to process.
		 */
		void gradientsSMA(int in) {
			if (queueRaw.isEmpty()) {
				return;
			}
			int outputSize = getNode().getOutputSize();
			Iterator<double[][]> iter = queueRaw.iterator();
			while (iter.hasNext()) {
				double[][] matrix = iter.next();
				for (int out = 0; out < outputSize; out++) {
					gradients[in][out] += matrix[in][out];
				}
			}
			for (int out = 0; out < outputSize; out++) {
				gradients[in][out] /= ((double) queueRaw.size());
			}
		}

		/**
		 * Consurrently (by input index) calculates the WMA of the raw gradients queue.
		 * 
		 * @param in The input index of the gradients to process.
		 */
		void gradientsWMA(int in) {
			if (queueRaw.isEmpty()) {
				return;
			}
			int outputSize = getNode().getOutputSize();
			Iterator<double[][]> iter = queueRaw.iterator();
			double weight = 1;
			double total = 0;
			while (iter.hasNext()) {
				double[][] matrix = iter.next();
				for (int out = 0; out < outputSize; out++) {
					gradients[in][out] += (matrix[in][out] * weight);
				}
				total += weight;
				weight += 1;
			}
			for (int out = 0; out < outputSize; out++) {
				gradients[in][out] /= total;
			}
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
	/** Last weight deltas to apply to momentums. */
	private double[][] weightDeltas;

	/** Gradients worker. */
	private Gradients gradients;

	/** Size of queues. */
	private int historySize = 5;

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
	public void backward(int in) {

		/* Necessary data from the weights node. */
		int outputSize = getNode().getOutputSize();
		double[] inputDeltas = getNode().getInputDeltas();
		double[] outputDeltas = getNode().getOutputDeltas();
		double[][] weights = getNode().getWeights();

		/* Input value and initialize the input delta. */
		double inputDelta = 0;

		/* Iterate all output indexes for each input index. */
		for (int out = 0; out < outputSize; out++) {

			double outputDelta = outputDeltas[out];
			double weight = weights[in][out];
			inputDelta += (weight * outputDelta);

			/* Output delta and gradient (input value * output delta). */
			double gradient = gradients.gradients[in][out];

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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void finalizeBackward() {}

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

			/* Last weight deltas. */
			weightDeltas = new double[inputSize][outputSize];
			Matrix.fill(weightDeltas, 0.0);

			gradients = new Gradients();
		}

		/* Push and calculate gradients. */
		gradients.processGradients();
		gradients.addRaw(gradients.gradients);
		gradients.processQueueRaw();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initializeOptimizer() {
		learningRates = null;
		momentums = null;
		weightDeltas = null;
		gradients = null;
	}
}
