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

import com.mlt.ml.data.Pattern;
import com.mlt.ml.function.Normalizer;

/**
 * A MNIST number image pattern.
 *
 * @author Miquel Sas
 */
public class NumberImagePattern extends Pattern {

	/**
	 * Return the number given the network output.
	 * 
	 * @param out The network output.
	 * @return The image number.
	 */
	public static int getNumber(double[] out) {
		int number = -1;
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < out.length; i++) {
			if (number < 0 || out[i] > max) {
				number = i;
				max = out[i];
			}
		}
		return number;
	}

	/** Underlying number image. */
	private NumberImage image;
	/** Input values. */
	private double[] inputValues;
	/** Output values. */
	private double[] outputValues;
	/** A boolean that indicates if input is bipolar. */
	private boolean bipolarInput = false;
	/** A boolean that indicates if output is bipolar. */
	private boolean bipolarOutput = false;

	/**
	 * Constructor.
	 * 
	 * @param image   The number image.
	 * @param bipolarInput A boolean that indicates if input must be bipolar.
	 * @param bipolarOutput A boolean that indicates if output must be bipolar.
	 */
	public NumberImagePattern(NumberImage image, boolean bipolarInput, boolean bipolarOutput) {
		super();
		this.image = image;
		this.bipolarInput = bipolarInput;
		this.bipolarOutput = bipolarOutput;
		setLabel(Integer.toString(image.getNumber()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double[] getInputValues() {
		if (inputValues == null) {
			Normalizer normalizer = new Normalizer(image.getMaximum(), image.getMinimum(), 1, (bipolarInput ? -1 : 0));
			inputValues = new double[image.getRows() * image.getColumns()];
			int index = 0;
			for (int row = 0; row < image.getRows(); row++) {
				for (int column = 0; column < image.getColumns(); column++) {
					double imageByte = image.getImage()[row][column];
					inputValues[index++] = normalizer.normalize(imageByte);
				}
			}
		}
		return inputValues;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double[] getOutputValues() {
		if (outputValues == null) {
			Normalizer normalizer = new Normalizer(1, 0, 1, (bipolarOutput ? -1 : 0));
			int number = image.getNumber();
			outputValues = new double[10];
			int index = 0;
			for (int i = 0; i < number; i++) {
				outputValues[index++] = normalizer.normalize(0.0);
			}
			outputValues[index++] = normalizer.normalize(1.0);
			for (int i = number + 1; i < 10; i++) {
				outputValues[index++] = normalizer.normalize(0.0);
			}
		}
		return outputValues;
	}

	/**
	 * Returns the number image.
	 * 
	 * @return The number image.
	 */
	public NumberImage getImage() {
		return image;
	}
}
