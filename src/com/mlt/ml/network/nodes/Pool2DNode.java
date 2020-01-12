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

import com.mlt.ml.function.IndexFunction;
import com.mlt.ml.network.Edge;
import com.mlt.ml.network.Node;

/**
 * A 2D pool node.
 *
 * @author Miquel Sas
 */
public class Pool2DNode extends Node {

	/** Input rows. */
	private int inputRows;
	/** Input columns. */
	private int inputColumns;
	/** Pool rows. */
	private int poolRows;
	/** Pool columns. */
	private int poolColumns;
	/** Output rows. */
	private int outputRows;
	/** Output columns. */
	private int outputColumns;

	/** Input values cached for parallel process. */
	private double[] inputValues;
	/** Output values cached to avoid GC stress. */
	private double[] outputValues;

	/** Forward function. */
	private IndexFunction forwardFunction;

	/**
	 * Default constructor to restore.
	 */
	public Pool2DNode() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param inputRows    Input rows.
	 * @param inputColumns Input columns.
	 * @param poolRows     Pool rows.
	 * @param poolColumns  Pool columns.
	 */
	public Pool2DNode(
		int inputRows,
		int inputColumns,
		int poolRows,
		int poolColumns) {
		super();

		/* Pool rows must be a multiple of input rows. */
		if (inputRows % poolRows != 0) {
			throw new IllegalArgumentException("Invalid pool rows");
		}
		/* Pool columns must be a multiple of input columns. */
		if (inputColumns % poolColumns != 0) {
			throw new IllegalArgumentException("Invalid pool columns");
		}

		this.inputRows = inputRows;
		this.inputColumns = inputColumns;
		this.poolRows = poolRows;
		this.poolColumns = poolColumns;
		this.outputRows = inputRows / poolRows;
		this.outputColumns = inputColumns / poolColumns;

		this.outputValues = new double[outputRows * outputColumns];
		initializeFunction();
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
		if (edge.getSize() != (inputRows * inputColumns)) {
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
		if (edge.getSize() != (outputRows * outputColumns)) {
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
	 * @param index Index in the result vector.
	 */
	private void forward(int index) {
		int outputRow = index / outputColumns;
		int outputColumn = index % outputColumns;
		double max = Double.NEGATIVE_INFINITY;
		for (int poolRow = 0; poolRow < poolRows; poolRow++) {
			for (int poolColumn = 0; poolColumn < poolColumns; poolColumn++) {
				int inputRow = outputRow * poolRows + poolRow;
				int inputColumn = outputColumn * poolColumns + poolColumn;
				int inputIndex = inputRow * inputColumns + inputColumn;
				double inputValue = inputValues[inputIndex];
				if (inputValue > max) {
					max = inputValue;
				}
			}
		}
		int outputIndex = outputRow * outputColumns + outputColumn;
		outputValues[outputIndex] = max;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getInputSize() {
		return inputRows * inputColumns;
	}

	/**
	 * Return the number of output columns.
	 * 
	 * @return The number of output columns.
	 */
	public int getOutputColumns() {
		return outputColumns;
	}

	/**
	 * Return the number of output rows.
	 * 
	 * @return The number of output rows.
	 */
	public int getOutputRows() {
		return outputRows;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getOutputSize() {
		return outputRows * outputColumns;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize() {}

	/**
	 * Initialize the forward parallel function.
	 */
	private void initializeFunction() {
		forwardFunction = new IndexFunction(outputRows * outputColumns, (i) -> forward(i));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void restore(InputStream is) throws IOException {
		restoreProperties(is);
		inputRows = properties.getInteger("input-rows");
		inputColumns = properties.getInteger("input-columns");
		poolRows = properties.getInteger("pool-rows");
		poolColumns = properties.getInteger("pool-columns");
		outputRows = properties.getInteger("output-rows");
		outputColumns = properties.getInteger("output-columns");

		outputValues = new double[outputRows * outputColumns];
		initializeFunction();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void save(OutputStream os) throws IOException {
		properties.setInteger("input-rows", inputRows);
		properties.setInteger("input-columns", inputColumns);
		properties.setInteger("pool-rows", poolRows);
		properties.setInteger("pool-columns", poolColumns);
		properties.setInteger("output-rows", outputRows);
		properties.setInteger("output-columns", outputColumns);
		saveProperties(os);
	}
}
