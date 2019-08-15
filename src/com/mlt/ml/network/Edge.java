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

package com.mlt.ml.network;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * An edge of a computational graph.
 * <p>
 * Data (double[]), normally called values, is forward pushed to the edge by an
 * external means if the edge is an input edge, or by the input node if it is a
 * transfer edge. The data is then made available to the output node, allowing
 * for further processing, normally output calculations.
 * <p>
 * Data (double[]), normally called deltas, is backward pushed to the edge by an
 * external means if the edge is an output edge, or by the output node if it is
 * a transfer edge. The data is then made available to the input node, allowing
 * for further processing, normally internal parameters adjusting.
 * <p>
 * An edge without an input node is an input edge. An edge without an output
 * node is an output edge. An edge with both an input and an output node is a
 * transfer edge.
 * <p>
 * Note that recurrent edges do not transfer data (deltas) backward. Although
 * not necessary, it could have sense in a one pulse network graph, that is, a
 * network with a history size of one, it looses any sense in a batch process, a
 * network with a history size greater than one.
 * <p>
 * The data transferred through an edge is a <em>double[]</em>. Other
 * implementations, like ML.NET or DL4J, enhance using multiple dimension
 * arrays. We use a vector for simplicity, because 2D or 3D arrays can be simply
 * mapped to a 1D array.
 *
 * @author Miquel Sas
 */
public class Edge {

	/** Input node. */
	private Node inputNode;
	/** Output node. */
	private Node outputNode;

	/** Size of the input and output vectors. */
	private int size;

	/** Deque to maintain the backward queue (deltas). */
	private Deque<double[]> backwardQueue = new ArrayDeque<>();
	/** Deque to maintain the forward queue (values). */
	private Deque<double[]> forwardQueue = new ArrayDeque<>();

	/**
	 * A boolean that indicates whether the edge starts a recurrent path.
	 */
	private boolean recurrent = false;

	/**
	 * Constructor.
	 */
	public Edge() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param size The size of the input and output vectors.
	 */
	public Edge(int size) {
		this();
		this.size = size;
	}

	/**
	 * Constructor.
	 * 
	 * @param size      The size of the input and output vectors.
	 * @param recurrent A boolean.
	 */
	public Edge(int size, boolean recurrent) {
		this();
		this.size = size;
		this.recurrent = recurrent;
	}

	/**
	 * Return the backward data.
	 * 
	 * @return The backward data, normally called deltas.
	 */
	public double[] getBackwardData() {
		if (backwardQueue.isEmpty()) {
			return new double[size];
		}
		return backwardQueue.getFirst();
	}

	/**
	 * Return the current size of the backward queue.
	 * 
	 * @return The current size of the backward queue.
	 */
	public int getBackwardQueueSize() {
		return backwardQueue.size();
	}

	/**
	 * Return the forward data.
	 * 
	 * @return The forward data, normally called values.
	 */
	public double[] getForwardData() {
		if (forwardQueue.isEmpty()) {
			return new double[size];
		}
		return forwardQueue.getFirst();
	}

	/**
	 * Return the current size of the forward queue.
	 * 
	 * @return The current size of the forward queue.
	 */
	public int getForwardQueueSize() {
		return forwardQueue.size();
	}

	/**
	 * Return the input node.
	 * 
	 * @return The input node.
	 */
	public Node getInputNode() {
		return inputNode;
	}

	/**
	 * Return the output node.
	 * 
	 * @return The output node.
	 */
	public Node getOutputNode() {
		return outputNode;
	}

	/**
	 * Return the size of the input and output vectors.
	 * 
	 * @return The size.
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Return an id that uniquely identifies this edge.
	 * 
	 * @return The UUID.
	 */
	public String getUUID() {
		StringBuilder b = new StringBuilder();
		b.append("[");
		if (inputNode != null) {
			b.append(inputNode.getUUID());
		}
		b.append("][");
		if (outputNode != null) {
			b.append(outputNode.getUUID());
		}
		b.append("]");
		return b.toString();
	}

	/**
	 * Check whether the forward and backward queues are empty.
	 * 
	 * @return A boolean.
	 */
	public boolean isEmpty() {
		return forwardQueue.isEmpty() && backwardQueue.isEmpty();
	}

	/**
	 * Check whether the edge is recurrent.
	 * 
	 * @return A boolean.
	 */
	public boolean isRecurrent() {
		return recurrent;
	}

	/**
	 * Check whether this is an input edge, the input node is null.
	 * 
	 * @return A boolean.
	 */
	public boolean isInput() {
		return (inputNode == null);
	}

	/**
	 * Check whether this is an output edge, the output node is null.
	 * 
	 * @return A boolean.
	 */
	public boolean isOutput() {
		return (outputNode == null);
	}

	/**
	 * Push backward values (deltas), adding them at the begining of the backward
	 * queue.
	 * 
	 * @param outputDeltas The vector of output deltas.
	 */
	public void pushBackward(double[] outputDeltas) {
		if (outputDeltas.length != size) {
			throw new IllegalArgumentException("Invalid output deltas size");
		}
		double[] deltas = new double[size];
		System.arraycopy(outputDeltas, 0, deltas, 0, size);
		backwardQueue.addFirst(deltas);
	}

	/**
	 * Push forward values, adding them at the begining of the forward queue.
	 * 
	 * @param inputValues The vector of input values.
	 */
	public void pushForward(double[] inputValues) {
		if (inputValues.length != size) {
			throw new IllegalArgumentException("Invalid input values size");
		}
		double[] values = new double[size];
		System.arraycopy(inputValues, 0, values, 0, size);
		forwardQueue.addFirst(values);
	}

	/**
	 * Set the input node.
	 * 
	 * @param inputNode The input node.
	 */
	public void setInputNode(Node inputNode) {
		this.inputNode = inputNode;
	}

	/**
	 * Set the output node.
	 * 
	 * @param outputNode the output node.
	 */
	public void setOutputNode(Node outputNode) {
		this.outputNode = outputNode;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("[");
		if (inputNode == null) {
			b.append("--");
		} else {
			b.append(inputNode.toString());
		}
		b.append(" -> ");
		if (outputNode == null) {
			b.append("--");
		} else {
			b.append(outputNode.toString());
		}
		if (!forwardQueue.isEmpty() && forwardQueue.size() > 1) {
			b.append(", ");
			b.append(forwardQueue.size());
			b.append(", ");
			b.append(backwardQueue.size());
		}
		b.append("]");
		return b.toString();
	}

	/**
	 * Unfold the edge by removing the current first deltas and values.
	 */
	void unfold() {
		if (!backwardQueue.isEmpty()) {
			backwardQueue.removeFirst();
		}
		if (!forwardQueue.isEmpty()) {
			forwardQueue.removeFirst();
		}
	}
}
