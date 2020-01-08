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

import com.mlt.ml.network.Edge;
import com.mlt.ml.network.Node;
import com.mlt.ml.network.nodes.optimizers.AdaOptimizer;
import com.mlt.util.IndexFunction;

/**
 * A matrix of weights node.
 * <p>
 * Accessors for input and anput size, weights, input and output values and
 * deltas, are provided to be used by the weights optimizer.
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
	/** Input size. */
	private int inputSize;
	/** Output size. */
	private int outputSize;

	/** Weights optimizer. */
	private WeightsOptimizer optimizer;

	/** Backward function. */
	private IndexFunction backwardFunction;
	/** Forward function. */
	private IndexFunction forwardFunction;

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

		optimizer = new AdaOptimizer();
		optimizer.setNode(this);
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

		optimizer.initializeBackward();
		backwardFunction.process();
		optimizer.finalizeBackward();

		pushBackward(inputDeltas);
	}

	/**
	 * Backward process from input indexes start to end.
	 * 
	 * @param in Input index.
	 */
	private void backward(int in) {
		optimizer.backward(in);
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
	 * @param out Output index.
	 */
	private void forward(int out) {
		double signal = 0;
		for (int in = 0; in < inputSize; in++) {
			double input = inputValues[in];
			double weight = weights[in][out];
			signal += (input * weight);
		}
		outputValues[out] = signal;
	}

	/**
	 * @return The input deltas.
	 */
	public double[] getInputDeltas() {
		return inputDeltas;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getInputSize() {
		return inputSize;
	}

	/**
	 * @return The input values
	 */
	public double[] getInputValues() {
		return inputValues;
	}

	/**
	 * @return The output deltas.
	 */
	public double[] getOutputDeltas() {
		return outputDeltas;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getOutputSize() {
		return outputSize;
	}

	/**
	 * @return The output values
	 */
	public double[] getOutputValues() {
		return outputValues;
	}

	/**
	 * @return The weights.
	 */
	public double[][] getWeights() {
		return weights;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize() {
		/* Validate. */
		if (inputEdges.size() == 0) throw new IllegalStateException("Input edges empty");
		if (inputEdges.size() > 1) throw new IllegalStateException("More than one input edge");
		if (inputEdges.get(0)
			.getSize() != inputSize) throw new IllegalStateException("Invalid input edge size");
		if (outputEdges.size() == 0) throw new IllegalStateException("Output edges empty");
		if (outputEdges.size() > 1) throw new IllegalStateException("More than one output edge");
		if (outputEdges.get(0)
			.getSize() != outputSize) throw new IllegalStateException("Invalid output edge size");
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
		backwardFunction = new IndexFunction(inputSize, (in) -> backward(in));
		forwardFunction = new IndexFunction(outputSize, (out) -> forward(out));
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
		optimizer = (WeightsOptimizer) properties.getObject("optimizer");
		optimizer.setNode(this);
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
		properties.setObject("optimizer", optimizer);
		saveProperties(os);
	}
}
