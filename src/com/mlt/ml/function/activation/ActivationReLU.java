/*
 * Copyright (C) 2018 Miquel Sas
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.mlt.ml.function.activation;

import com.mlt.ml.function.Activation;

/**
 * ReLU activation.
 *
 * @author Miquel Sas
 */
public class ActivationReLU implements Activation {

	/** Leaky alpha. */
	private double alpha = 0.1;

	/**
	 * Constructor.
	 */
	public ActivationReLU() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getId() {
		return "RL";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double[] activations(double[] triggers) {
		double[] outputs = new double[triggers.length];
		for (int i = 0; i < triggers.length; i++) {
			double trigger = triggers[i];
			double output = (trigger <= 0 ? alpha * trigger : trigger);
			outputs[i] = output;
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
			derivatives[i] = (alpha == 0.0 ? 0.0 : 1.0);
		}
		return derivatives;
	}
}
