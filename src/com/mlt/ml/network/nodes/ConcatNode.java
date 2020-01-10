/*
 * Copyright (C) 2018 Miquel Sas
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General PublicLicense as published by the Free Software
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

import com.mlt.ml.network.Edge;
import com.mlt.ml.network.Node;

/**
 * A concatenation node receives several input edges (nodes) and produces a
 * single output that concatenates all inputs.
 *
 * @author Miquel Sas
 */
public class ConcatNode extends Node {

	/** Cached output values to avoid stressing GC. */
	private double[] outputValues;

	/**
	 * Constructor for restore.
	 */
	public ConcatNode() {
		super();
	}

	/**
	 * Constructor for restore.
	 * 
	 * @param prefix Node name prefix.
	 */
	public ConcatNode(String prefix) {
		super();
		setName(getName(prefix));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addInputEdge(Edge edge) throws IllegalStateException {
		/* Add input edges first. */
		if (outputEdges.size() > 0) {
			throw new IllegalStateException("Must add input edges before than output edges.");
		}
		edge.setOutputNode(this);
		inputEdges.add(edge);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addOutputEdge(Edge edge) throws IllegalStateException {
		/* Only one output edge. */
		if (outputEdges.size() > 0) {
			throw new IllegalStateException("More than one output edge");
		}
		/* Must match the size of input edges. */
		if (edge.getSize() != getOutputSize()) {
			throw new IllegalStateException("Invalid output edge size");
		}
		edge.setInputNode(this);
		outputEdges.add(edge);
		outputValues = new double[edge.getSize()];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void backward() {
		double[] outputDeltas = outputEdges.get(0).getBackwardData();
		int index = 0;
		for (int i = 0; i < inputEdges.size(); i++) {
			Edge inputEdge = inputEdges.get(i);
			double[] inputDeltas = new double[inputEdge.getSize()];
			System.arraycopy(outputDeltas, index, inputDeltas, 0, inputDeltas.length);
			index += inputDeltas.length;
			if (!inputEdge.isRecurrent()) {
				inputEdge.pushBackward(inputDeltas);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void forward() {
		int index = 0;
		for (int i = 0; i < inputEdges.size(); i++) {
			Edge inputEdge = inputEdges.get(i);
			double[] inputValues = inputEdge.getForwardData();
			System.arraycopy(inputValues, 0, outputValues, index, inputValues.length);
			index += inputValues.length;
		}
		pushForward(outputValues);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getExtendedDescription() {
		return "";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getInputSize() {
		return -1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getOutputSize() {
		int size = 0;
		for (int i = 0; i < inputEdges.size(); i++) {
			Edge edge = inputEdges.get(i);
			size += edge.getSize();
		}
		return size;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize() {
		/* Validate. */
		if (inputEdges.size() == 0) throw new IllegalStateException("Input edges empty");
		if (outputEdges.size() == 0) throw new IllegalStateException("Output edges empty");
		if (outputEdges.size() > 1) throw new IllegalStateException("More than one output edge");
		int inputSize = 0;
		for (int i = 0; i < inputEdges.size(); i++) {
			Edge edge = inputEdges.get(i);
			inputSize += edge.getSize();
		}
		if (inputSize != outputEdges.get(0).getSize()) {
			throw new IllegalStateException("Total input size and output size do not match.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void restore(InputStream is) throws IOException {
		restoreProperties(is);
		int outputSize = properties.getInteger("output-size");
		outputValues = new double[outputSize];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void save(OutputStream os) throws IOException {
		properties.setInteger("output-size", outputValues.length);
		saveProperties(os);
	}
}
