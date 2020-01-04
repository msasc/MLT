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

package com.mlt.ml.network.nodes;

/**
 * Weights optimizer.
 *
 * @author Miquel Sas
 */
public abstract class WeightsOptimizer {

	/** Input size. */
	protected int inputSize;
	/** Output size. */
	protected int outputSize;

	/** Matrix of weights (in-out). */
	protected double[][] weights;
	/** Cached input deltas, to be accessed concurrently. */
	protected double[] inputDeltas;
	/** Cached input values, to be accessed concurrently. */
	protected double[] inputValues;
	/** Cached output deltas, to be accessed concurrently. */
	protected double[] outputDeltas;
	/** Cached output values, to be accessed concurrently. */
	protected double[] outputValues;

	/**
	 * Constructor.
	 */
	public WeightsOptimizer() {
		super();
	}

	/**
	 * Set internal protected parameters to calculate the step.
	 * 
	 * @param inputSize    Input size.
	 * @param outputSize   Output size.
	 * @param weights      Weights.
	 * @param inputDeltas  Input deltas.
	 * @param inputValues  Input values.
	 * @param outputDeltas Output deltas.
	 * @param outputValues Output values.
	 */
	public void set(
		int inputSize,
		int outputSize,
		double[][] weights,
		double[] inputDeltas,
		double[] inputValues,
		double[] outputDeltas,
		double[] outputValues) {
		
		this.inputSize = inputSize;
		this.outputSize = outputSize;
		this.weights = weights;
		this.inputDeltas = inputDeltas;
		this.inputValues = inputValues;
		this.outputDeltas = outputDeltas;
		this.outputValues = outputValues;
	}

	/**
	 * Backward process from input indexes start to end.
	 * 
	 * @param start Input start index.
	 * @param end   Input end index.
	 */
	public abstract void backward(int start, int end);
}
