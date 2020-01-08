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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import com.mlt.ml.network.Edge;
import com.mlt.ml.network.Node;
import com.mlt.util.IndexFunction;
import com.mlt.util.Numbers;

/**
 * A 2D filter node.
 *
 * @author Miquel Sas
 */
public class Filter2DNode extends Node {

	/**
	 * Print a shape on the filter.
	 * 
	 * @param filter The filter.
	 * @param shapes The list of shapes to to print on it.
	 */
	public static void print(double[][] filter, Shape... shapes) {

		int rows = filter.length;
		int columns = filter[0].length;

		BufferedImage img = new BufferedImage(columns, rows, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = img.createGraphics();
		g2d.setPaint(Color.BLACK);
		g2d.fill(new Rectangle(0, 0, columns, columns));

		g2d.setPaint(Color.WHITE);
		for (Shape shape : shapes) {
			g2d.draw(shape);
		}

		for (int row = 0; row < rows; row++) {
			for (int column = 0; column < columns; column++) {
				Color c = new Color(img.getRGB(column, row));
				double b = Math.max(Math.max(c.getRed(), c.getGreen()), c.getBlue());
				filter[row][column] = b / 255.0;
			}
		}
	}

	/**
	 * Print an arc with 1.0 values on the filter.
	 * 
	 * @param filter The filter, may not be squared.
	 * @param x      The X coordinate of the upper-left corner of the arc's framing
	 *               rectangle.
	 * @param y      The Y coordinate of the upper-left corner of the arc's framing
	 *               rectangle.
	 * @param width  The overall width of the full ellipse of which this arc is a
	 *               partial section.
	 * @param height The overall height of the full ellipse of which this arc is a
	 *               partial section.
	 * @param start  The starting angle of the arc in degrees.
	 * @param extent The angular extent of the arc in degrees.
	 */
	public static void printArc(
		double[][] filter,
		double x,
		double y,
		double width,
		double height,
		double start,
		double extent) {

		int rows = filter.length;
		int columns = filter[0].length;

		if (x < 0 || x > columns) throw new IllegalArgumentException("Invalid left corner");
		if (y < 0 || y > rows) throw new IllegalArgumentException("Invalid upper corner");
		if (x + width - 1 > columns) throw new IllegalArgumentException("Invalid width");
		if (y + height - 1 > rows) throw new IllegalArgumentException("Invalid height");

		Arc2D.Double arc = new Arc2D.Double(x, y, width, height, start, extent, Arc2D.OPEN);
		print(filter, arc);
	}

	/**
	 * Print the number of points randomly selecting rows and columns.
	 * 
	 * @param filter The filter.
	 * @param size   The number of prints.
	 */
	public static void printRandom(double[][] filter, int size) {
		int rows = filter.length;
		int columns = filter[0].length;
		Random random = new Random();
		for (int i = 0; i < size; i++) {
			int row = random.nextInt(rows);
			int column = random.nextInt(columns);
			filter[row][column] = 1.0;
		}
	}

	/**
	 * Return the vector of filter values.
	 * 
	 * @param filter The 2D filter.
	 * @return The 1D filter.
	 */
	private static double[] toVector(double[][] filter) {
		int rows = filter.length;
		int columns = filter[0].length;
		double[] filterValues = new double[rows * columns];
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < columns; col++) {
				int index = row * columns + col;
				filterValues[index] = filter[row][col];
			}
		}
		return filterValues;
	}

	/** Filter columns. */
	private int filterColumns;
	/** Filter rows. */
	private int filterRows;
	/** Filter values. */
	private double[] filterValues;
	/** Forward function. */
	private IndexFunction forwardFunction;
	/** Input columns. */
	private int inputColumns;
	/** Input rows. */
	private int inputRows;
	/** Input values cached for parallel process. */
	private double[] inputValues;
	/** Output columns. */
	private int outputColumns;
	/** Output rows. */
	private int outputRows;
	/** Output values cached to avoid GC stress. */
	private double[] outputValues;
	/** Padding. */
	private boolean padding;
	/** Pad columns. */
	private int padColumns;
	/** Pad rows. */
	private int padRows;
	/** Pad value. */
	private double padValue;

	/**
	 * Default constructor to restore.
	 */
	public Filter2DNode() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param name          Node name.
	 * @param inputRows     Input rows.
	 * @param inputColumns  Input columns.
	 * @param filterRows    Filter rows.
	 * @param filterColumns Filter columns.
	 * @param filter        Filter (2D).
	 * @param padding       Padding flag.
	 * @param padValue      Pad value.
	 */
	public Filter2DNode(String name, int inputRows, int inputColumns, double[][] filter,
		boolean padding,
		double padValue) {
		super();
		setName(name);

		/* Rows and columns. */
		this.filterRows = filter.length;
		this.filterColumns = filter[0].length;
		if (!Numbers
			.isOdd(filterRows)) throw new IllegalArgumentException("Filter rows must be odd");
		if (!Numbers
			.isOdd(filterColumns)) throw new IllegalArgumentException("Filter columns must be odd");
		if (filterRows > inputRows) throw new IllegalArgumentException(
			"Filter rows greater than input rows");
		if (filterRows > inputRows) throw new IllegalArgumentException(
			"Filter rows greater than input rows");
		if (filterColumns > inputColumns) throw new IllegalArgumentException(
			"Filter columns greater than input columns");

		/* Transform to 1D filter. */
		this.filterValues = Filter2DNode.toVector(filter);

		/* Register the rest of values. */
		this.inputRows = inputRows;
		this.inputColumns = inputColumns;
		this.outputRows = (padding ? inputRows : inputRows - filterRows + 1);
		this.outputColumns = (padding ? inputColumns : inputColumns - filterColumns + 1);
		this.padding = padding;
		this.padValue = padValue;
		this.padRows = (padding ? filterRows / 2 : 0);
		this.padColumns = (padding ? filterColumns / 2 : 0);

		this.outputValues = new double[outputRows * outputColumns];
		/* Initialize the function. */
		forwardFunction = new IndexFunction(outputRows * outputColumns, (index) -> forward(index));
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
		if (edge.getSize() != (inputRows * inputColumns)) {
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
		if (edge.getSize() != (outputRows * outputColumns)) {
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
	 * @param index Index in the result vector.
	 */
	private void forward(int index) {
		int outputRow = index / outputColumns;
		int outputColumn = index % outputColumns;
		double outputValue = 0;
		for (int filterRow = 0; filterRow < filterRows; filterRow++) {
			for (int filterColumn = 0; filterColumn < filterColumns; filterColumn++) {
				int inputRow = outputRow + filterRow - padRows;
				int inputColumn = outputColumn + filterColumn - padColumns;
				double inputValue = 0;
				if (inputRow < 0 || inputColumn < 0 || inputRow >= inputRows
					|| inputColumn >= inputColumns) {
					inputValue = padValue;
				} else {
					int inputIndex = inputRow * inputColumns + inputColumn;
					inputValue = inputValues[inputIndex];
				}
				int filterIndex = filterRow * filterColumns + filterColumn;
				double filterValue = filterValues[filterIndex];
				outputValue += (inputValue * filterValue);
			}
		}
		int outputIndex = outputRow * outputColumns + outputColumn;
		outputValues[outputIndex] = outputValue;
	}

	/**
	 * Return the filter columns.
	 * 
	 * @return The filter columns.
	 */
	public int getFilterColumns() {
		return filterColumns;
	}

	/**
	 * Return the filter rows.
	 * 
	 * @return The filter rows.
	 */
	public int getFilterRows() {
		return filterRows;
	}

	/**
	 * Return the filter values.
	 * 
	 * @return The filter values.
	 */
	public double[] getFilterValues() {
		return filterValues;
	}

	/**
	 * {@inheritDocs}
	 */
	@Override
	public int getInputSize() {
		return inputRows * inputColumns;
	}

	/**
	 * Return the output columns.
	 * 
	 * @return The output columns.
	 */
	public int getOutputColumns() {
		return outputColumns;
	}

	/**
	 * Return the output rows.
	 * 
	 * @return The output rows.
	 */
	public int getOutputRows() {
		return outputRows;
	}

	/**
	 * {@inheritDocs}
	 */
	@Override
	public int getOutputSize() {
		return outputRows * outputColumns;
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
		inputRows = properties.getInteger("input-rows");
		inputColumns = properties.getInteger("input-columns");
		filterRows = properties.getInteger("filter-rows");
		filterColumns = properties.getInteger("filter-columns");
		outputRows = properties.getInteger("output-rows");
		outputColumns = properties.getInteger("output-columns");
		filterValues = properties.getDouble1A("filter-values");
		padding = properties.getBoolean("padding");
		padValue = properties.getDouble("pad-value");

		padRows = (!padding ? 0 : filterRows / 2);
		padColumns = (!padding ? 0 : filterColumns / 2);

		outputValues = new double[outputRows * outputColumns];
		/* Initialize the function. */
		forwardFunction = new IndexFunction(outputRows * outputColumns, (index) -> forward(index));
	}

	/**
	 * {@inheritDocs}
	 */
	@Override
	public void save(OutputStream os) throws IOException {
		properties.setInteger("input-rows", inputRows);
		properties.setInteger("input-columns", inputColumns);
		properties.setInteger("filter-rows", filterRows);
		properties.setInteger("filter-columns", filterColumns);
		properties.setInteger("output-rows", outputRows);
		properties.setInteger("output-columns", outputColumns);
		properties.setDouble1A("filter-values", filterValues);
		properties.setBoolean("padding", padding);
		properties.setDouble("pad-value", padValue);
		saveProperties(os);
	}

}
