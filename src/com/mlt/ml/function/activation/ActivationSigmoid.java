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

package com.mlt.ml.function.activation;

import com.mlt.ml.function.Activation;

/**
 * Sigmoid activation.
 *
 * @author Miquel Sas
 */
public class ActivationSigmoid implements Activation {

	/**
	 * Constructor.
	 */
	public ActivationSigmoid() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getId() {
		return "SG";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double[] activations(double[] triggers) {
		double[] outputs = new double[triggers.length];
		for (int i = 0; i < triggers.length; i++) {
			outputs[i] = 1 / (1 + Math.exp(-triggers[i]));
		}
		return outputs;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double[] derivatives(double[] outputs) {
		double[] derivatives = new double[outputs.length];
		for (int i = 0; i < outputs.length; i++) {
			derivatives[i] = outputs[i] * (1 - outputs[i]);
		}
		return derivatives;
	}
}
