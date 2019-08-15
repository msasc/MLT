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

import com.mlt.ml.function.RangeFunction;
import com.mlt.ml.network.Edge;
import com.mlt.ml.network.Node;
import com.mlt.util.Numbers;

/**
 * A 1D filter node.
 *
 * @author Miquel Sas
 */
public class Filter1DNode extends Node {

	/** Filter size. */
	private int filterSize;
	/** Filter values. */
	private double[] filterValues;
	/** Padding. */
	private boolean padding;
	/** Pad value. */
	private double padValue;
	/** Pad size. */
	private int padSize;

	/** Input size. */
	private int inputSize;
	/** Input values cached for parallel process. */
	private double[] inputValues;
	/** Output size. */
	private int outputSize;
	/** Output values cached to avoid GC stress. */
	private double[] outputValues;

	/** Forward function for parallel processing. */
	private RangeFunction forwardFunction;

	/**
	 * Constructor for restore.
	 */
	public Filter1DNode() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param name         Node name.
	 * @param inputSize    Input size, necessary to validate the filter and
	 *                     calculate the output size.
	 * @param filterValues The filter.
	 * @param padding      A boolean indicating whether padding should be applied.
	 * @param padValue     Pad value.
	 */
	public Filter1DNode(String name, int inputSize, double[] filterValues, boolean padding, double padValue) {
		super();
		if (filterValues == null) throw new NullPointerException();
		if (filterValues.length > inputSize) throw new IllegalArgumentException("Filter too big");
		if (!Numbers.isOdd(filterValues.length)) throw new IllegalArgumentException("Filter length must be odd");

		setName(name);
		this.filterSize = filterValues.length;
		this.filterValues = filterValues;
		this.padding = padding;
		this.padValue = padValue;
		this.padSize = (padding ? filterSize / 2 : 0);

		this.inputSize = inputSize;
		this.inputValues = null; /* Assigned when forwarding. */

		this.outputSize = (padding ? inputSize : inputSize - filterSize + 1);
		this.outputValues = new double[outputSize];

		/* Initialize the function. */
		forwardFunction = new RangeFunction(outputSize, (s, e) -> forward(s, e));
	}

	/**
	 * {@inheritDocs}
	 */
	@Override
	public void addInputEdge(Edge edge) throws IllegalStateException {
		if (edge.isRecurrent()) {
			throw new IllegalStateException("Filter nodes do not admit recurrent input edges");
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
	 * {@inheritDocs}
	 */
	@Override
	public void addOutputEdge(Edge edge) throws IllegalStateException {
		if (edge.isRecurrent()) {
			throw new IllegalStateException("Filter nodes do not admit recurrent output edges");
		}
		if (outputEdges.size() > 0) {
			throw new IllegalStateException("More than one output edge");
		}
		if (edge.getSize() != (outputValues.length)) {
			throw new IllegalStateException("Invalid output edge size");
		}
		edge.setInputNode(this);
		outputEdges.add(edge);
	}

	/**
	 * {@inheritDocs}
	 */
	@Override
	public void backward() {}

	/**
	 * {@inheritDocs}
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
			double outputValue = 0;
			for (int filterIndex = 0; filterIndex < filterSize; filterIndex++) {
				int inputIndex = outputIndex + filterIndex - padSize;
				double inputValue = 0;
				if (inputIndex < 0 || inputIndex >= inputSize) {
					inputValue = padValue;
				} else {
					inputValue = inputValues[inputIndex];
				}
				double filterValue = filterValues[filterIndex];
				outputValue += (inputValue * filterValue);
			}
			outputValues[outputIndex] = outputValue;
		}
	}

	/**
	 * {@inheritDocs}
	 */
	@Override
	public int getInputSize() {
		return inputSize;
	}

	/**
	 * {@inheritDocs}
	 */
	@Override
	public int getOutputSize() {
		return outputSize;
	}

	/**
	 * {@inheritDocs}
	 */
	@Override
	public void initialize() {}

	/**
	 * {@inheritDocs}
	 */
	@Override
	public void restore(InputStream is) throws IOException {
		restoreProperties(is);
		filterSize = properties.getInteger("filter-size");
		filterValues = properties.getDouble1A("filter-values");
		padding = properties.getBoolean("padding");
		padValue = properties.getDouble("pad-value");
		padSize = properties.getInteger("pas-size");
		inputSize = properties.getInteger("input-size");
		outputSize = properties.getInteger("output-size");

		outputValues = new double[outputSize];

		/* Initialize the function. */
		forwardFunction = new RangeFunction(outputSize, (s, e) -> forward(s, e));
	}

	/**
	 * {@inheritDocs}
	 */
	@Override
	public void save(OutputStream os) throws IOException {
		properties.setInteger("filter-size", filterSize);
		properties.setDouble1A("filter-values", filterValues);
		properties.setBoolean("padding", padding);
		properties.setDouble("pad-value", padValue);
		properties.setInteger("pad-size", padSize);
		properties.setInteger("input-size", inputSize);
		properties.setInteger("output-size", outputSize);
		saveProperties(os);
	}

}
