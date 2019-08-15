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
package com.mlt.ml.data.mnist;

/**
 * MNIST sample image data: a 28*28 byte image matrix and its number (0 to 9)
 *
 * @author Miquel Sas
 */
public class NumberImage {

	/** The byte array of 28*28 = 784 elements. */
	private int[][] image;
	/** The number. */
	private int number;
	/** Maximum byte value. */
	private int maximum = Integer.MIN_VALUE;
	/** Minimum byte value. */
	private int minimum = Integer.MAX_VALUE;

	/**
	 * Constructor assigning the number and the bytes.
	 *
	 * @param number The represented number
	 * @param bytes  The raw bytes list
	 */
	public NumberImage(int number, int[][] bytes) {
		super();
		if (number < 0 || number > 9) {
			throw new IllegalArgumentException("Invalid number " + number);
		}
		this.number = number;
		image = bytes;
	}

	/**
	 * Return the number of columns.
	 * 
	 * @return The number of columns.
	 */
	public int getColumns() {
		return image[0].length;
	}

	/**
	 * Returns the image as a two dimension byte array.
	 *
	 * @return The image
	 */
	public int[][] getImage() {
		return image;
	}

	/**
	 * Return the maximum pixel value.
	 * 
	 * @return The maximum pixel value.
	 */
	public int getMaximum() {
		calculateMinMax();
		return maximum;
	}

	/**
	 * Return the minimum pixel value.
	 * 
	 * @return The minimum pixel value.
	 */
	public int getMinimum() {
		calculateMinMax();
		return minimum;
	}

	/**
	 * Calculate the minimum and maximum pixel values.
	 */
	private void calculateMinMax() {
		if (minimum == Integer.MAX_VALUE && maximum == Integer.MIN_VALUE) {
			int rows = getRows();
			int cols = getColumns();
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					if (image[r][c] > maximum) {
						maximum = image[r][c];
					}
					if (image[r][c] < minimum) {
						minimum = image[r][c];
					}
				}
			}
		}
	}

	/**
	 * Returns the number.
	 *
	 * @return The number
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * Return the number of rows.
	 * 
	 * @return The number of rows.
	 */
	public int getRows() {
		return image.length;
	}

	/**
	 * Returns the string representation.
	 */
	@Override
	public String toString() {
		return Integer.toString(getNumber());
	}
}
