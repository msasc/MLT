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

import com.mlt.ml.data.mnist.MNIST;
import com.mlt.ml.data.mnist.NumberImage;
import com.mlt.ml.data.mnist.NumberImagePattern;
import com.mlt.util.HTML;

import app.mlt.ml.mnist.viewer.ImageSource;

/**
 * An image source for a MNIST pattern.
 *
 * @author Miquel Sas
 */
public class SourceMNIST extends ImageSource {

	/** MNIST pattern. */
	private NumberImagePattern pattern;

	/**
	 * Constructor.
	 */
	public SourceMNIST() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getRows() {
		return MNIST.IMAGE_ROWS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getColumns() {
		return MNIST.IMAGE_COLUMNS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double[] getImage() {
		if (pattern == null) {
			double[] image = new double[getRows() * getColumns()];
			return image;
		}
		return pattern.getInputValues();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getInformation() {
		if (pattern == null) {
			return "No image";
		}
		NumberImage image = pattern.getImage();
		HTML info = new HTML();
		info.print("MNIST image");
		info.print(", ");
		info.print("rows: ");
		info.print(getRows());
		info.print(", ");
		info.print("columns: ");
		info.print(getColumns());
		info.print(", ");
		info.print("image number: ");
		info.print(image.getNumber());
		int networkOutput = pattern.getProperties().getInteger("NET-OUT", -1);
		if (networkOutput >= 0) {
			info.print(", ");
			info.print("network number: ");
			info.print(networkOutput);
		}
		return info.toString();
	}

	/**
	 * Set the pattern.
	 * 
	 * @param pattern The number image pattern.
	 */
	public void setPattern(NumberImagePattern pattern) {
		this.pattern = pattern;
	}
}
