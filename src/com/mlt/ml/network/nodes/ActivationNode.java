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

import com.mlt.ml.function.Activation;
import com.mlt.ml.network.Edge;
import com.mlt.ml.network.Node;

/**
 * An activation node, that can have only one input edge and one output edge.
 *
 * @author Miquel Sas
 */
public class ActivationNode extends Node {

	/** Size of values. */
	private int size;
	/** Activation function. */
	private Activation activation;
	/** Flat spot to avoid near zero derivatives. */
	private static double flatSpot = 0.00;

	/**
	 * Constructor used for restore.
	 */
	public ActivationNode() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param size       The size of values that flow throw the node.
	 * @param activation The activation function.
	 */
	public ActivationNode(int size, Activation activation) {
		super();
		this.size = size;
		this.activation = activation;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addInputEdge(Edge edge) throws IllegalStateException {
		if (inputEdges.size() > 0) {
			throw new IllegalStateException("More than one input edge");
		}
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
		if (outputEdges.size() > 0) {
			throw new IllegalStateException("More than one output edge");
		}
		if (edge.getSize() != size) {
			throw new IllegalStateException("Invalid output edge size");
		}
		edge.setInputNode(this);
		outputEdges.add(edge);
	}

	/**
	 * Apply the derivatives to the input deltas and push them.
	 */
	@Override
	public void backward() {
		double[] deltas = outputEdges.get(0).getBackwardData();
		double[] values = outputEdges.get(0).getForwardData();
		double[] derivatives = activation.derivatives(values);
		/*
		 * Apply derivatives to deltas including a flat spot to avoid near zero
		 * derivatives.
		 */
		for (int i = 0; i < size; i++) {
			deltas[i] = deltas[i] * (derivatives[i] + flatSpot);
		}
		pushBackward(deltas);
	}

	/**
	 * Apply the activation function and push the output values.
	 */
	@Override
	public void forward() {
		Edge inputEdge = inputEdges.get(0);
		double[] triggerValues = inputEdge.getForwardData();
		double[] outputValues = activation.activations(triggerValues);
		pushForward(outputValues);
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
	 * {@inheritDoc}
	 */
	@Override
	public void initialize() {
		/* Validate. */
		if (inputEdges.size() == 0) throw new IllegalStateException("Input edges empty");
		if (inputEdges.size() > 1) throw new IllegalStateException("More than one input edge");
		if (inputEdges.get(0)
			.getSize() != size) throw new IllegalStateException("Invalid input edge size");
		if (outputEdges.size() == 0) throw new IllegalStateException("Output edges empty");
		if (outputEdges.size() > 1) throw new IllegalStateException("More than one output edge");
		if (outputEdges.get(0)
			.getSize() != size) throw new IllegalStateException("Invalid output edge size");
		if (activation == null) throw new IllegalStateException("Activation is null");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void restore(InputStream is) throws IOException {
		restoreProperties(is);
		size = properties.getInteger("size");
		activation = (Activation) properties.getObject("activation");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void save(OutputStream os) throws IOException {
		properties.setInteger("size", size);
		properties.setObject("activation", activation);
		saveProperties(os);
	}
}
