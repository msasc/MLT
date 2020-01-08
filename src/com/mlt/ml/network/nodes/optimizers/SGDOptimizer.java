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
 * Basic stochastic gradient descent optimizer.
 *
 * @author Miquel Sas
 */
public class SGDOptimizer extends WeightsOptimizer {

	/** Learning rate. */
	private double learningRate = 0.01;
	
	/**
	 * Constructor.
	 */
	public SGDOptimizer() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void backward(int start, int end) {
		
		double[] inputDeltas = getNode().getInputDeltas();
		double[] inputValues = getNode().getInputValues();
		double[] outputDeltas = getNode().getOutputDeltas();
		double[][] weights = getNode().getWeights();
		int outputSize = getNode().getOutputSize();
		
		for (int in = start; in <= end; in++) {
			double inputValue = inputValues[in];
			double inputDelta = 0;
			for (int out = 0; out < outputSize; out++) {
				double weight = weights[in][out];
				double outputDelta = outputDeltas[out];
				inputDelta += (weight * outputDelta);
				/* Weight delta. */
				double weightDelta = learningRate * outputDelta * inputValue;
				weights[in][out] += weightDelta;
			}
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
	public void initializeBackward() {}
}
