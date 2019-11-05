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
import com.mlt.ml.network.Node;
import com.mlt.ml.network.nodes.Filter2DNode;
import com.mlt.util.HTML;

import app.mlt.ml.mnist.viewer.ImageSource;

/**
 * An image source to view the filter 2D node.
 *
 * @author Miquel Sas
 */
public class SourceFilter2D extends ImageSource {
	
	/** Transfer node. */
	private Node node;
	/** Filter rows. */
	private int rows;
	/** Filter columns. */
	private int columns;
	/** Filter values. */
	private double[] filterValues;

	/**
	 * Constructor.
	 * 
	 * @param node The function node.
	 */
	public SourceFilter2D(Node node) {
		super();
		
		/* Validate the node. */
		if (!(node instanceof Filter2DNode)) {
			throw new IllegalArgumentException("Node must be a Filter2DNode");
		}
		this.node = node;
		
		Filter2DNode filter = (Filter2DNode) node;
		this.rows = filter.getFilterRows();
		this.columns = filter.getFilterColumns();
		this.filterValues = filter.getFilterValues();
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
		double[] output = filterValues;
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < output.length; i++) {
			if (output[i] < min) min = output[i];
			if (output[i] > max) max = output[i];
		}
		Normalizer n = new Normalizer(max, min, 0.0, 1.0);
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
		info.print(filterValues.length);
		info.print(", ");
		info.print("rows: ");
		info.print(rows);
		info.print(", ");
		info.print("columns: ");
		info.print(columns);
		return info.toString();
	}

}
