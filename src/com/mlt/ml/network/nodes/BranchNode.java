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
import java.util.ArrayList;
import java.util.List;

import com.mlt.ml.function.Collector;
import com.mlt.ml.network.Edge;
import com.mlt.ml.network.Node;

/**
 * A branch node has one or more input edges and one or more output edges. It
 * also has a forward aggregation function
 * that processes inputs to produce an output that is pushed to all output
 * edges, and a backward aggregation function
 * that processes deltas and produces a delta that is pushed to all input edges.
 * The size is the same for all input and
 * output edges.
 *
 * @author Miquel Sas
 */
public class BranchNode extends Node {

	/** Input-output size. */
	private int size;
	/** Forward function. */
	private Collector<double[]> forwardFunction;
	/** Backward function. */
	private Collector<double[]> backwardFunction;

	/**
	 * Constructor used for restore.
	 */
	public BranchNode() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param size             The input-output size.
	 * @param forwardFunction  Forward collector.
	 * @param backwardFunction Backward collector.
	 */
	public BranchNode(
		int size,
		Collector<double[]> forwardFunction,
		Collector<double[]> backwardFunction) {
		super();
		this.size = size;
		this.forwardFunction = forwardFunction;
		this.backwardFunction = backwardFunction;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addInputEdge(Edge edge) throws IllegalStateException {
		if (edge.getSize() != size) {
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
		if (edge.getSize() != size) {
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
		List<double[]> outputDeltas = new ArrayList<>();
		for (int i = 0; i < outputEdges.size(); i++) {
			Edge edge = outputEdges.get(i);
			if (edge.isRecurrent()) continue;
			outputDeltas.add(edge.getBackwardData());
		}
		if (outputDeltas.isEmpty()) {
			outputDeltas.add(new double[size]);
		}
		double[] inputDeltas = backwardFunction.collect(outputDeltas);
		pushBackward(inputDeltas);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void forward() {
		List<double[]> inputValues = new ArrayList<>();
		for (int i = 0; i < inputEdges.size(); i++) {
			inputValues.add(inputEdges.get(i).getForwardData());
		}
		double[] outputValues = forwardFunction.collect(inputValues);
		pushForward(outputValues);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getId() {
		return "BR";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getInputSize() {
		return size;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getOutputSize() {
		return size;
	}

	/**
	 * Return the size.
	 * 
	 * @return The size.
	 */
	public int getSize() {
		return size;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize() {
		if (inputEdges.size() == 0) {
			throw new IllegalStateException("No input edges");
		}
		for (int i = 0; i < inputEdges.size(); i++) {
			Edge edge = inputEdges.get(i);
			if (edge.getSize() != size) {
				throw new IllegalStateException("Invalid input edge size");
			}
		}
		if (outputEdges.size() == 0) {
			throw new IllegalStateException("No output edges");
		}
		for (int i = 0; i < outputEdges.size(); i++) {
			Edge edge = outputEdges.get(i);
			if (edge.getSize() != size) {
				throw new IllegalStateException("Invalid output edge size");
			}
		}
		if (backwardFunction == null) {
			throw new IllegalStateException("Backward function is null");
		}
		if (forwardFunction == null) {
			throw new IllegalStateException("Forward function is null");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void restore(InputStream is) throws IOException {
		restoreProperties(is);
		size = properties.getInteger("size");
		forwardFunction = (Collector<double[]>) properties.getObject("forward-function");
		backwardFunction = (Collector<double[]>) properties.getObject("backward-function");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void save(OutputStream os) throws IOException {
		properties.setInteger("size", size);
		properties.setObject("forward-function", forwardFunction);
		properties.setObject("backward-function", backwardFunction);
		saveProperties(os);
	}
}
