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

package com.mlt.ml.data;

import com.mlt.util.Numbers;

/**
 * Default pattern.
 *
 * @author Miquel Sas
 */
public class DefaultPattern extends Pattern {
	
	/** Input values. */
	private double[] inputValues;
	/** Output values. */
	private double[] outputValues;
	/** Decimals to round results for matches. */
	private int decimals;

	/**
	 * Constructor.
	 * 
	 * @param inputValues Input values.
	 * @param outputValues Output values.
	 */
	public DefaultPattern(double[] inputValues, double[] outputValues) {
		this(inputValues, outputValues, 0);
	}

	/**
	 * Constructor.
	 * 
	 * @param inputValues Input values.
	 * @param outputValues Output values.
	 */
	public DefaultPattern(double[] inputValues, double[] outputValues, int decimals) {
		if (inputValues == null || outputValues == null) {
			throw new NullPointerException();
		}
		this.inputValues = inputValues;
		this.outputValues = outputValues;
		this.decimals = decimals;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean matches(double[] networkOutput) {
		if (outputValues.length != networkOutput.length) {
			return false;
		}
		for (int i = 0; i < outputValues.length; i++) {
			double out = Numbers.round(outputValues[i], decimals);
			double net = Numbers.round(networkOutput[i], decimals);
			if (out != net) {
				return false;
			}
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double[] getInputValues() {
		return inputValues;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double[] getOutputValues() {
		return outputValues;
	}

}
