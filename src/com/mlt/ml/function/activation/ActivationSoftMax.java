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
import com.mlt.util.Numbers;

/**
 * Soft-max activation.
 *
 * @author Miquel Sas
 */
public class ActivationSoftMax implements Activation {

	/**
	 * Constructor.
	 */
	public ActivationSoftMax() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getId() {
		return "SM";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double[] activations(double[] triggers) {
		double[] outputs = new double[triggers.length];
		double div = 0;
		for (int i = 0; i < triggers.length; i++) {
			double p = Numbers.bound(Math.exp(triggers[i]));
			outputs[i] = p;
			div += p;
		}
		if (div != 0) {
			for (int i = 0; i < triggers.length; i++) {
				outputs[i] /= div;
			}
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
			derivatives[i] = 1.0;
		}
		return derivatives;
	}

}
