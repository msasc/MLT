/*
 * Copyright (C) 2018 Miquel Sas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package app.mlt.ml.mnist.viewer.source;

import com.mlt.ml.function.Normalizer;
import com.mlt.ml.network.Edge;
import com.mlt.ml.network.Node;
import com.mlt.ml.network.nodes.Pool2DNode;
import com.mlt.util.HTML;
import com.mlt.util.Numbers;

import app.mlt.ml.mnist.viewer.ImageSource;

/**
 * An image source for the output of a transfer node. Represents the output in a gray scale.
 *
 * @author Miquel Sas
 */
public class SourceTransferNode extends ImageSource {

	/** Transfer node. */
	private Node node;
	/** Output edge. */
	private Edge outputEdge;
	/** Rows. */
	private int rows;
	/** Columns. */
	private int columns;

	/**
	 * Constructor.
	 * 
	 * @param node The function node.
	 */
	public SourceTransferNode(Node node, boolean flat) {
		super();
		if (!node.isTransfer()) {
			throw new IllegalArgumentException("Node must be a transfer node");
		}
		this.node = node;
		this.outputEdge = node.getOutputEdges().get(0);

		/*
		 * If the node is a function node and the function is a 2D filter, check the function, else set row and columns
		 * to the next square root.
		 */
		if (flat) {
			rows = 1;
			columns = outputEdge.getSize();
		} else {
			double sqrt = Math.sqrt(outputEdge.getSize());
			if (sqrt == Numbers.round(sqrt, 0)) {
				rows = (int) sqrt;
			} else {
				rows = ((int) sqrt) + 1;
			}
			columns = rows;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getRows() {
		return rows;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getColumns() {
		return columns;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double[] getImage() {
		if (node instanceof Pool2DNode) {
			System.out.println();
		}
		double[] output = outputEdge.getForwardData();
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < output.length; i++) {
			if (output[i] < min) min = output[i];
			if (output[i] > max) max = output[i];
		}
		Normalizer n = new Normalizer(max, min, 1.0, 0.0);
		double[] image = new double[rows * columns];
		for (int i = 0; i < image.length; i++) {
			if (i < output.length) {
				image[i] = n.normalize(output[i]);
			} else {
				image[i] = 1.0;
			}
		}
		return image;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getInformation() {
		HTML info = new HTML();
		info.print(node.getName());
		info.print(", cells: ");
		info.print(outputEdge.getSize());
		info.print(", ");
		info.print("rows: ");
		info.print(rows);
		info.print(", ");
		info.print("columns: ");
		info.print(columns);
		return info.toString();
	}

}
