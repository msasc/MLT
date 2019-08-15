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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import com.mlt.ml.function.RangeFunction;
import com.mlt.ml.network.Edge;
import com.mlt.ml.network.Node;

/**
 * A matrix of weights node.
 *
 * @author Miquel Sas
 */
public class WeightsNode extends Node {

	/** Matrix of weights (in-out). */
	private double[][] weights;

	/** Cached input deltas, to be accessed concurrently. */
	private double[] inputDeltas;
	/** Cached input values, to be accessed concurrently. */
	private double[] inputValues;
	/** Cached output deltas, to be accessed concurrently. */
	private double[] outputDeltas;
	/** Cached output values, to be accessed concurrently. */
	private double[] outputValues;

	/** Backward function. */
	private RangeFunction backwardFunction;
	/** Forward function. */
	private RangeFunction forwardFunction;

	/** Input size. */
	private int inputSize;
	/** Output size. */
	private int outputSize;

	/**
	 * Constructor used to restore.
	 */
	public WeightsNode() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param name       Node name.
	 * @param inputSize  Input size.
	 * @param outputSize Output size.
	 */
	public WeightsNode(String name, int inputSize, int outputSize) {
		super();
		setName(name);
		this.inputSize = inputSize;
		this.outputSize = outputSize;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addInputEdge(Edge edge) throws IllegalStateException {
		if (inputEdges.size() > 0) {
			throw new IllegalStateException("More than one input edge");
		}
		if (edge.getSize() != inputSize) {
			throw new IllegalStateException("Invalid input edge size");
		}
		edge.setOutputNode(this);
		inputEdges.add(edge);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addOutputEdge(Edge edge) throws IllegalStateException {
		if (outputEdges.size() > 0) {
			throw new IllegalStateException("More than one output edge");
		}
		if (edge.getSize() != outputSize) {
			throw new IllegalStateException("Invalid output edge size");
		}
		edge.setInputNode(this);
		outputEdges.add(edge);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void backward() {
		if (inputEdges.get(0).isRecurrent()) {
			return;
		}
		inputValues = inputEdges.get(0).getForwardData();
		outputDeltas = outputEdges.get(0).getBackwardData();
		backwardFunction.process();
		pushBackward(inputDeltas);
	}

	/**
	 * Backward process from input indexes start to end.
	 * 
	 * @param start Input start index.
	 * @param end   Input end index.
	 */
	private void backward(int start, int end) {
		for (int in = start; in <= end; in++) {
			double input = inputValues[in];
			double inputDelta = 0;
			for (int out = 0; out < outputSize; out++) {
				double weight = weights[in][out];
				double outputDelta = outputDeltas[out];
				inputDelta += (weight * outputDelta);
				/* Weight delta with a fixed eta or learning rate. */
				double weightDelta = 0.01 * outputDelta * input;
				weights[in][out] += weightDelta;
			}
			inputDeltas[in] = inputDelta;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void forward() {
		inputValues = inputEdges.get(0).getForwardData();
		forwardFunction.process();
		pushForward(outputValues);
	}

	/**
	 * Forward process from output indexes start to end.
	 * 
	 * @param start Output start index.
	 * @param end   Output end index.
	 */
	private void forward(int start, int end) {
		for (int out = start; out <= end; out++) {
			double signal = 0;
			for (int in = 0; in < inputSize; in++) {
				double input = inputValues[in];
				double weight = weights[in][out];
				signal += (input * weight);
			}
			outputValues[out] = signal;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getInputSize() {
		return inputSize;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getOutputSize() {
		return outputSize;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize() {
		/* Validate. */
		if (inputEdges.size() == 0) throw new IllegalStateException("Input edges empty");
		if (inputEdges.size() > 1) throw new IllegalStateException("More than one input edge");
		if (inputEdges.get(0).getSize() != inputSize) throw new IllegalStateException("Invalid input edge size");
		if (outputEdges.size() == 0) throw new IllegalStateException("Output edges empty");
		if (outputEdges.size() > 1) throw new IllegalStateException("More than one output edge");
		if (outputEdges.get(0).getSize() != outputSize) throw new IllegalStateException("Invalid output edge size");
		/* Initialize. */
		weights = new double[inputSize][outputSize];
		initializeVectorsAndFunctions();
		Random random = new Random();
		for (int in = 0; in < inputSize; in++) {
			for (int out = 0; out < outputSize; out++) {
				weights[in][out] = random.nextGaussian();
			}
		}
	}

	/**
	 * Initialize vectors and functions.
	 */
	private void initializeVectorsAndFunctions() {
		inputDeltas = new double[inputSize];
		inputValues = new double[inputSize];
		outputValues = new double[outputSize];
		backwardFunction = new RangeFunction(inputSize, (s, e) -> backward(s, e));
		forwardFunction = new RangeFunction(outputSize, (s, e) -> forward(s, e));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void restore(InputStream is) throws IOException {
		restoreProperties(is);
		inputSize = properties.getInteger("input-size");
		outputSize = properties.getInteger("output-size");
		weights = properties.getDouble2A("weights");
		initializeVectorsAndFunctions();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void save(OutputStream os) throws IOException {
		properties.setInteger("input-size", inputSize);
		properties.setInteger("output-size", outputSize);
		properties.setDouble2A("weights", weights);
		saveProperties(os);
	}
}
