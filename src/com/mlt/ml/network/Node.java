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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.mlt.ml.function.Activation;
import com.mlt.ml.function.Collector;
import com.mlt.ml.network.nodes.WeightsOptimizer;
import com.mlt.util.IO;
import com.mlt.util.Properties;
import com.mlt.util.Strings;

/**
 * A node of a computational graph.
 * <p>
 * In the forward pass, a node reads data from the input edges, processes it,
 * and pushes the resulting data to the
 * output edges.
 * <p>
 * In the backward pass, a node may read data, normally deltas to adjust
 * parameters, from the output edges, do as
 * internally expected, and optionally push backward data to the input edges.
 *
 * @author Miquel Sas
 */
public abstract class Node {

	/** List of input edges. */
	protected List<Edge> inputEdges = new ArrayList<>();
	/** List of output edges. */
	protected List<Edge> outputEdges = new ArrayList<>();
	/** List of additional properties. */
	protected Properties properties = new Properties();

	/**
	 * Constructor.
	 */
	public Node() {
		super();
		/** Unique node identifier. */
		properties.setString("uuid", UUID.randomUUID().toString());
	}

	/**
	 * Add an input edge, validating when necessary.
	 * 
	 * @param edge The input edge.
	 * @throws IllegalStateException
	 */
	public abstract void addInputEdge(Edge edge);

	/**
	 * Add an output edge, validating when necessary, perhaps throwing an
	 * <em>IllegalStateException</em> or an
	 * <em>IllegalArgumentException</em>.
	 * 
	 * @param edge The output edge.
	 * @throws IllegalStateException
	 */
	public abstract void addOutputEdge(Edge edge);

	/**
	 * Request deltas, apply any parameter update, and push deltas to input edges.
	 */
	public abstract void backward();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Node) {
			Node node = (Node) obj;
			return getUUID().equals(node.getUUID());
		}
		return false;
	}

	/**
	 * Request values from input edges, apply node calculations and push values to
	 * output edges.
	 */
	public abstract void forward();

	/**
	 * @return The branch index of the branch, the order in which the branch was added.
	 */
	public int getBranch() {
		return properties.getInteger("branch-index", -1);
	}

	/**
	 * @return The order order of the node within its branch.
	 */
	public int getOrder() {
		return properties.getInteger("branch-order", -1);
	}

	/**
	 * Return a description made with the information available at this abstract
	 * node level, and additionally the extended description of the node if any.
	 * 
	 * @return The node description.
	 */
	public String getDescription() {
		StringWriter s = new StringWriter();
		PrintWriter p = new PrintWriter(s);
		p.print(getBranch());
		p.print(".");
		p.print(getOrder());
		p.print(" ");
		p.print(getName());

		StringBuilder io = new StringBuilder();
		if (!inputEdges.isEmpty()) {
			io.append(" (I: ");
			for (int i = 0; i < inputEdges.size(); i++) {
				if (i > 0) io.append(", ");
				io.append(inputEdges.get(i).getSize());
			}
		}
		if (!outputEdges.isEmpty()) {
			if (io.length() == 0) {
				io.append(" (O: ");
			} else {
				io.append(", O: ");
			}
			for (int i = 0; i < outputEdges.size(); i++) {
				if (i > 0) io.append(", ");
				io.append(outputEdges.get(i).getSize());
			}
		}
		if (io.length() > 0) {
			io.append(")");
		}
		p.print(io.toString());

		p.close();
		return s.toString();
	}

	/**
	 * Return an unmodifiable list of input edges.
	 * 
	 * @return The input edges.
	 */
	public List<Edge> getInputEdges() {
		return Collections.unmodifiableList(inputEdges);
	}

	/**
	 * Return the input size even if the node has not been connected.
	 * 
	 * @return The input size.
	 */
	public abstract int getInputSize();

	/**
	 * Return the node name.
	 * 
	 * @return The name.
	 */
	public String getName() {
		return Strings.replace(getClass().getSimpleName(), "Node", "");
	}

	/**
	 * Return an unmodifiable list of output edges.
	 * 
	 * @return The output edges.
	 */
	public List<Edge> getOutputEdges() {
		return Collections.unmodifiableList(outputEdges);
	}

	/**
	 * Return the output size even if the node has not been connected.
	 * 
	 * @return The output size.
	 */
	public abstract int getOutputSize();

	/**
	 * Return the list of all siblings of this node, including it.
	 * 
	 * @return The list of siblings of the node including it.
	 */
	public List<Node> getSiblings() {
		List<Node> siblings = new ArrayList<>();
		siblings.add(this);

		List<Node> outputNodes = new ArrayList<>();
		for (Edge edge : outputEdges) {
			Node outputNode = edge.getOutputNode();
			if (outputNode != null && !outputNodes.contains(outputNode)) {
				outputNodes.add(outputNode);
			}
		}
		for (Node outputNode : outputNodes) {
			for (Edge edge : outputNode.inputEdges) {
				Node sibling = edge.getInputNode();
				if (sibling != null && !siblings.contains(sibling)) {
					siblings.add(sibling);
				}
			}
		}
		return siblings;
	}

	/**
	 * Return the uniform unique identifier.
	 * 
	 * @return The identifier.
	 */
	public final String getUUID() {
		return properties.getString("uuid");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return getUUID().hashCode();
	}

	/**
	 * Validate and eventually initialize the node internal data.
	 */
	public abstract void initialize();

	/**
	 * Check whether this node is the input node, that is, one of its input edges is
	 * the input edge of the network.
	 * 
	 * @return A boolean.
	 */
	public boolean isInput() {
		for (int i = 0; i < inputEdges.size(); i++) {
			if (inputEdges.get(i).isInput()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if this node is a leaf node, that is, it has no input edges.
	 * 
	 * @return A boolean.
	 */
	public boolean isLeaf() {
		return inputEdges.isEmpty();
	}

	/**
	 * Check whether this node is the output node, that is, one of its output edges
	 * is the output edge of the network.
	 * 
	 * @return A boolean.
	 */
	public boolean isOutput() {
		for (int i = 0; i < outputEdges.size(); i++) {
			if (outputEdges.get(i).isOutput()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check whether this node is strictly recurrent, that is, it has only one input
	 * edge and it is recurrent.
	 * 
	 * @return A boolean.
	 */
	public boolean isRecurrent() {
		return (inputEdges.size() == 1 && inputEdges.get(0).isRecurrent());
	}

	/**
	 * Check whether this node is a transfer node, it has only one input edge and
	 * one output edge.
	 * 
	 * @return A boolean.
	 */
	public boolean isTransfer() {
		return (inputEdges.size() == 1 && outputEdges.size() == 1);
	}

	/**
	 * Helper to push backward the input deltas as output deltas of input edges.
	 * 
	 * @param inputDeltas The input deltas.
	 */
	protected void pushBackward(double[] inputDeltas) {
		for (int i = 0; i < inputEdges.size(); i++) {
			inputEdges.get(i).pushBackward(inputDeltas);
		}
	}

	/**
	 * Helper to push forward the output values as input values of output edges.
	 * 
	 * @param outputValues The output values.
	 */
	protected void pushForward(double[] outputValues) {
		for (int i = 0; i < outputEdges.size(); i++) {
			outputEdges.get(i).pushForward(outputValues);
		}
	}

	/**
	 * Restore from an input stream.
	 * 
	 * @param is The input stream.
	 * @throws IOException
	 */
	public abstract void restore(InputStream is) throws IOException;

	/**
	 * Restore the properties from the input stream.
	 * 
	 * @param is The input stream.
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	protected void restoreProperties(InputStream is) throws IOException {
		/* Clear properties. */
		properties.clear();

		/* Number of values to restore. */
		int size = IO.readInt(is);

		/* Restore key by key. */
		for (int i = 0; i < size; i++) {

			/* Key and type. */
			String key = IO.readString(is);
			String type = IO.readString(is);

			/* Boolean. */
			if (type.equals("Boolean")) {
				properties.setBoolean(key, IO.readBoolean(is));
				continue;
			}
			/* Double. */
			if (type.equals("Double")) {
				properties.setDouble(key, IO.readDouble(is));
				continue;
			}
			/* Double1A. */
			if (type.equals("Double1A")) {
				properties.setDouble1A(key, IO.readDouble1A(is));
				continue;
			}
			/* Double2A. */
			if (type.equals("Double2A")) {
				properties.setDouble2A(key, IO.readDouble2A(is));
				continue;
			}
			/* Integer. */
			if (type.equals("Integer")) {
				properties.setInteger(key, IO.readInt(is));
				continue;
			}
			/* Long. */
			if (type.equals("Long")) {
				properties.setLong(key, IO.readLong(is));
				continue;
			}
			/* String. */
			if (type.equals("String")) {
				properties.setString(key, IO.readString(is));
				continue;
			}
			/* Activation. */
			if (type.equals("Activation")) {
				String className = IO.readString(is);
				Activation activation = null;
				try {
					activation = (Activation) Class.forName(className).newInstance();
				} catch (Exception exc) {
					throw new IOException(exc);
				}
				properties.setObject(key, activation);
				continue;
			}
			/* Collector. */
			if (type.equals("Collector")) {
				String className = IO.readString(is);
				Collector<double[]> collector = null;
				try {
					collector = (Collector<double[]>) Class.forName(className).newInstance();
				} catch (Exception exc) {
					throw new IOException(exc);
				}
				properties.setObject(key, collector);
				continue;
			}
			/* Node. */
			if (type.equals("Node")) {
				String className = IO.readString(is);
				Node node = null;
				try {
					node = (Node) Class.forName(className).newInstance();
				} catch (Exception exc) {
					throw new IOException(exc);
				}
				node.restoreProperties(is);
				properties.setObject(key, node);
				continue;
			}
			/* Weights optimizer. */
			if (type.equals("WeightsOptimizer")) {
				String className = IO.readString(is);
				WeightsOptimizer optimizer = null;
				try {
					optimizer = (WeightsOptimizer) Class.forName(className).newInstance();
				} catch (Exception exc) {
					throw new IOException(exc);
				}
				properties.setObject(key, optimizer);
				continue;
			}
			throw new IllegalStateException("Invalid type: " + type);
		}
	}

	/**
	 * Save to an output stream.
	 * 
	 * @param os The output stream.
	 * @throws IOException
	 */
	public abstract void save(OutputStream os) throws IOException;

	/**
	 * Save a properties container to an output stream using the <em>IO</em>
	 * utility.
	 * 
	 * @param os
	 * @throws IOException
	 */
	protected void saveProperties(OutputStream os) throws IOException {

		/* List of keys. */
		Set<Object> keySet = properties.keySet();

		/* Number of keys. */
		IO.writeInt(os, keySet.size());

		/* Iterate keys. */
		Iterator<Object> keys = keySet.iterator();
		while (keys.hasNext()) {

			/* Key and value. */
			Object okey = keys.next();
			if (!(okey instanceof String)) {
				throw new IllegalStateException("Property key must be of type string");
			}
			String key = (String) okey;
			Object value = properties.getObject(key);
			if (value == null) {
				throw new NullPointerException();
			}

			/* Boolean. */
			if (value instanceof Boolean) {
				IO.writeString(os, key);
				IO.writeString(os, "Boolean");
				IO.writeBoolean(os, (Boolean) value);
				continue;
			}
			/* Double. */
			if (value instanceof Double) {
				IO.writeString(os, key);
				IO.writeString(os, "Double");
				IO.writeDouble(os, (Double) value);
				continue;
			}
			/* Double1A. */
			if (value instanceof double[]) {
				IO.writeString(os, key);
				IO.writeString(os, "Double1A");
				IO.writeDouble1A(os, (double[]) value);
				continue;
			}
			/* Double2A. */
			if (value instanceof double[][]) {
				IO.writeString(os, key);
				IO.writeString(os, "Double2A");
				IO.writeDouble2A(os, (double[][]) value);
				continue;
			}
			/* Integer. */
			if (value instanceof Integer) {
				IO.writeString(os, key);
				IO.writeString(os, "Integer");
				IO.writeInt(os, (Integer) value);
				continue;
			}
			/* Long. */
			if (value instanceof Long) {
				IO.writeString(os, key);
				IO.writeString(os, "Long");
				IO.writeLong(os, (Long) value);
				continue;
			}
			/* String. */
			if (value instanceof String) {
				IO.writeString(os, key);
				IO.writeString(os, "String");
				IO.writeString(os, (String) value);
				continue;
			}
			/* Activation. */
			if (value instanceof Activation) {
				IO.writeString(os, key);
				IO.writeString(os, "Activation");
				IO.writeString(os, value.getClass().getName());
				continue;
			}
			/* Collector. */
			if (value instanceof Collector) {
				IO.writeString(os, key);
				IO.writeString(os, "Collector");
				IO.writeString(os, value.getClass().getName());
				continue;
			}
			/* Node. */
			if (value instanceof Node) {
				Node node = (Node) value;
				IO.writeString(os, key);
				IO.writeString(os, "Node");
				IO.writeString(os, node.getClass().getName());
				node.saveProperties(os);
				continue;
			}
			/* Weights optimizer. */
			if (value instanceof WeightsOptimizer) {
				WeightsOptimizer optimizer = (WeightsOptimizer) value;
				IO.writeString(os, key);
				IO.writeString(os, "WeightsOptimizer");
				IO.writeString(os, optimizer.getClass().getName());
				continue;
			}
			throw new IllegalStateException("Invalid value type: " + value.getClass().getName());
		}
	}

	/**
	 * @param branchIndex The branch index.
	 */
	public void setBranch(int branchIndex) {
		properties.setInteger("branch-index", branchIndex);
	}

	/**
	 * @param branchOrder The order of the node within the branch definition.
	 */
	public void setOrder(int branchOrder) {
		properties.setInteger("branch-order", branchOrder);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		String id = getName();
		return (!id.isEmpty() ? id : super.toString());
	}
}
