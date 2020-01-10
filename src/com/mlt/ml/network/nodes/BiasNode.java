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
import java.util.Arrays;

import com.mlt.ml.network.Edge;
import com.mlt.ml.network.Node;

/**
 * A bias node without weights adjustments. The backward process does nothing.
 *
 * @author Miquel Sas
 */
public class BiasNode extends Node {

	/** Bias weights. */
	private double[] weights;

	/**
	 * Constructor used for restore.
	 */
	public BiasNode() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param prefix Node name prefix.
	 * @param size   The bias size.
	 */
	public BiasNode(String prefix, int size) {
		super();
		setName(getName(prefix));
		this.weights = new double[size];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addInputEdge(Edge edge) throws IllegalStateException {
		throw new IllegalStateException("No input edges allowed");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addOutputEdge(Edge edge) throws IllegalStateException {
		if (outputEdges.size() > 0) {
			throw new IllegalStateException("More than one output edge");
		}
		if (edge.getSize() != weights.length) {
			throw new IllegalStateException("Invalid output edge size");
		}
		edge.setInputNode(this);
		outputEdges.add(edge);
	}

	/**
	 * A bias node without weights adjustment does nothing in the backward process.
	 */
	@Override
	public void backward() {}

	/**
	 * A bias node just pushes the weights as output values.
	 */
	@Override
	public void forward() {
		pushForward(weights);
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
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getOutputSize() {
		return weights.length;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize() {
		/* Validate. */
		if (inputEdges.size() > 0) throw new IllegalStateException("No input edges allowed");
		if (outputEdges.size() == 0) throw new IllegalStateException("Output edges empty");
		if (outputEdges.size() > 1) throw new IllegalStateException("More than one output edge");
		if (outputEdges.get(0).getSize() != weights.length) throw new IllegalStateException(
			"Invalid output edge size");
		/* Initialize. */
		Arrays.fill(weights, 1.0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void restore(InputStream is) throws IOException {
		restoreProperties(is);
		weights = properties.getDouble1A("weights");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void save(OutputStream os) throws IOException {
		properties.setDouble1A("weights", weights);
		saveProperties(os);
	}
}
