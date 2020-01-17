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

import com.mlt.ml.function.RangeFunction;
import com.mlt.ml.network.Edge;
import com.mlt.ml.network.Node;

/**
 * A pool 1D node.
 *
 * @author Miquel Sas
 */
public class Pool1DNode extends Node {

	/** Input size. */
	private int inputSize;
	/** Pool size. */
	private int poolSize;
	/** Output size. */
	private int outputSize;

	/** Input values cached for parallel process. */
	private double[] inputValues;
	/** Output values cached to avoid GC stress. */
	private double[] outputValues;

	/** Forward function. */
	private RangeFunction forwardFunction;

	/**
	 * Constructor.
	 * 
	 * @param inputSize Input size.
	 * @param poolSize  Pool size.
	 */
	public Pool1DNode(int inputSize, int poolSize) {
		super();

		/* Pool size must be a multiple of input size. */
		if (inputSize % poolSize != 0) {
			throw new IllegalArgumentException("Invalid pool size");
		}

		this.inputSize = inputSize;
		this.poolSize = poolSize;
		this.outputSize = inputSize / poolSize;
		this.outputValues = new double[outputSize];

		/* Initialize the function. */
		forwardFunction = new RangeFunction(outputSize, (s, e) -> forward(s, e));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addInputEdge(Edge edge) throws IllegalStateException {
		if (edge.isRecurrent()) {
			throw new IllegalStateException("Pool nodes do not admit recurrent input edges");
		}
		if (inputEdges.size() > 0) {
			throw new IllegalStateException("More than one input edge");
		}
		if (edge.getSize() != (inputSize)) {
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
		if (edge.isRecurrent()) {
			throw new IllegalStateException("Pool nodes do not admit recurrent output edges");
		}
		if (outputEdges.size() > 0) {
			throw new IllegalStateException("More than one output edge");
		}
		if (edge.getSize() != (outputSize)) {
			throw new IllegalStateException("Invalid output edge size");
		}
		edge.setInputNode(this);
		outputEdges.add(edge);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void backward() {}

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
	 * Forward to process in parallel.
	 * 
	 * @param startIndex Start index in the result vector.
	 * @param endIndex   End index in the result vector.
	 */
	private void forward(int startIndex, int endIndex) {
		for (int outputIndex = startIndex; outputIndex <= endIndex; outputIndex++) {
			double max = Double.NEGATIVE_INFINITY;
			for (int poolIndex = 0; poolIndex < poolSize; poolIndex++) {
				int inputIndex = outputIndex * poolSize + poolIndex;
				double inputValue = inputValues[inputIndex];
				if (inputValue > max) {
					max = inputValue;
				}
			}
			outputValues[outputIndex] = max;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getId() {
		return "P1";
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
	public void initialize() {}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void restore(InputStream is) throws IOException {
		restoreProperties(is);
		inputSize = properties.getInteger("input-size");
		poolSize = properties.getInteger("pool-size");
		outputSize = properties.getInteger("output-size");

		this.outputValues = new double[outputSize];
		forwardFunction = new RangeFunction(outputSize, (s, e) -> forward(s, e));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void save(OutputStream os) throws IOException {
		properties.setInteger("input-size", inputSize);
		properties.setInteger("pool-size", poolSize);
		properties.setInteger("output-size", outputSize);
		saveProperties(os);
	}

}
