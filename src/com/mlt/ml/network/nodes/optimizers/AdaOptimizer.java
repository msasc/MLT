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

import java.util.Iterator;

import com.mlt.ml.network.nodes.WeightsOptimizer;
import com.mlt.util.FixedSizeList;
import com.mlt.util.IndexFunction;
import com.mlt.util.Matrix;

/**
 * Adaptative optimizer.
 *
 * @author Miquel Sas
 */
public class AdaOptimizer extends WeightsOptimizer {

	/** Learning rates. */
	private double[][] learningRates;
	/** Momentums. */
	private double[][] momentums;
	/** Last weight deltas to apply to momentums. */
	private double[][] weightDeltas;

	private FixedSizeList<double[][]> gradientsQueue;
	private double[][] gradients;
	private IndexFunction gradientsAdd;

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

			/* Accumulate input delta using the current weight. */
			inputDelta += (weights[in][out] * outputDeltas[out]);
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
	 * @return The gradients calculated with input values and output deltas.
	 */
	private double[][] gradients() {
		int inputSize = getNode().getInputSize();
		int outputSize = getNode().getOutputSize();
		double[] inputValues = getNode().getInputValues();
		double[] outputDeltas = getNode().getOutputDeltas();
		double[][] gradients = new double[inputSize][outputSize];
		for (int in = 0; in < inputSize; in++) {
			for (int out = 0; out < outputSize; out++) {
				gradients[in][out] = inputValues[in] * outputDeltas[out];
			}
		}
		return gradients;
	}
	
	/**
	 * @param in The input index of the gradients to add.
	 */
	private void gradientsAdd(int in) {
		int outputSize = getNode().getOutputSize();
		Iterator<double[][]> iter = gradientsQueue.iterator();
		while (iter.hasNext()) {
			double[][] matrix = iter.next();
			for (int out = 0; out < outputSize; out++) {
				gradients[in][out] += matrix[in][out];
			}
		}
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
			Matrix.fill(momentums, 0.2);

			/* Last weight deltas. */
			weightDeltas = new double[inputSize][outputSize];
			Matrix.fill(weightDeltas, 0.0);

			/* Gradients queue. */
			gradientsQueue = new FixedSizeList<>(5);

			/* Gradients. */
			gradients = new double[inputSize][outputSize];

			/* List of tasks to add gradients. */
			gradientsAdd = new IndexFunction(inputSize, (in) -> gradientsAdd(in));
		}

		/* Push and calculate gradients. */
		gradientsQueue.add(gradients());
		Matrix.fill(gradients, 0.0);
		gradientsAdd.process();;
	}

}
