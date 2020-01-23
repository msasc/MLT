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

	/**
	 * Constructor.
	 * 
	 * @param inputValues Input values.
	 * @param outputValues Output values.
	 */
	public DefaultPattern(double[] inputValues, double[] outputValues) {
		if (inputValues == null || outputValues == null) {
			throw new NullPointerException();
		}
		this.inputValues = inputValues;
		this.outputValues = outputValues;
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
