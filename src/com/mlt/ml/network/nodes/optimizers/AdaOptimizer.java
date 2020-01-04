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

import com.mlt.ml.network.nodes.WeightsOptimizer;

/**
 * Adaptative optimizer, with an learning rate and a momentum for each weight,
 * and a queue of output deltas to optionally calculate moving averages and
 * other functions on those output deltas.
 *
 * @author Miquel Sas
 */
public class AdaOptimizer extends WeightsOptimizer {

	/** Learning rates. */
	private double[][] learningRates;
	/** Momentums. */
	private double[][] momentums;
	/** Last weight deltas. */
	private double[][] weightDeltas;

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
		for (int in = start; in <= end; in++) {
			double input = inputValues[in];
			double inputDelta = 0;
			for (int out = 0; out < outputSize; out++) {
				double weight = weights[in][out];
				double outputDelta = outputDeltas[out];
				inputDelta += (weight * outputDelta);
				/* Weight delta. */
				double learningRate = learningRates[in][out];
				double momentum = momentums[in][out];
				double lastDelta = weightDeltas[in][out];
				double prevDelta = momentum * lastDelta;
				double nextDelta = (1 - momentum) * (learningRate * outputDelta * input);
				double weightDelta = prevDelta + nextDelta;
				weights[in][out] += weightDelta;
				weightDeltas[in][out] = weightDelta;
			}
			inputDeltas[in] = inputDelta;
		}
	}

	/**
	 * Check if should initialize internal arrays.
	 */
	private void checkInitialize() {
		if (learningRates != null) {
			return;
		}
		learningRates = new double[inputSize][outputSize];
		momentums = new double[inputSize][outputSize];
		weightDeltas = new double[inputSize][outputSize];
		for (int in = 0; in < inputSize; in++) {
			for (int out = 0; out < outputSize; out++) {
				learningRates[in][out] = 0.01;
				momentums[in][out] = 0.5;
				weightDeltas[in][out] = 0.0;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void set(
		int inputSize,
		int outputSize,
		double[][] weights,
		double[] inputDeltas,
		double[] inputValues,
		double[] outputDeltas,
		double[] outputValues) {
		
		super.set(
			inputSize,
			outputSize,
			weights,
			inputDeltas,
			inputValues,
			outputDeltas,
			outputValues);
		
		/* Check initialize. */
		checkInitialize();
		
		/* Push output deltas. */
	}

}
