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

package com.mlt.ml.function;

import com.mlt.ml.function.activation.ActivationBipolarSigmoid;
import com.mlt.ml.function.activation.ActivationReLU;
import com.mlt.ml.function.activation.ActivationSigmoid;
import com.mlt.ml.function.activation.ActivationSoftMax;
import com.mlt.ml.function.activation.ActivationTANH;

/**
 * Activation function.
 *
 * @author Miquel Sas
 */
public interface Activation {
	
	/** BipolarSigmoid. */
	public static final String BIPOLAR_SIGMOID = "BipolarSigmoid";
	/** ReLU. */
	public static final String RE_LU = "ReLU";
	/** Sigmoid. */
	public static final String SIGMOID = "Sigmoid";
	/** Softmax. */
	public static final String SOFT_MAX = "SoftMax";
	/** TANH. */
	public static final String TANH = "TANH";
	
	/**
	 * @param name The activation name.
	 * @return The activation given the name.
	 */
	public static Activation get(String name) {
		if (name.equals(BIPOLAR_SIGMOID)) {
			return new ActivationBipolarSigmoid();
		}
		if (name.equals(RE_LU)) {
			return new ActivationReLU();
		}
		if (name.equals(SIGMOID)) {
			return new ActivationSigmoid();
		}
		if (name.equals(SOFT_MAX)) {
			return new ActivationSoftMax();
		}
		if (name.equals(TANH)) {
			return new ActivationTANH();
		}
		throw new IllegalArgumentException("Invalid ativation name: " + name);
	}

	/**
	 * Return an identification id of this activation function.
	 * 
	 * @return The id.
	 */
	String getId();

	/**
	 * Calculates the output values of the function given the trigger values.
	 * 
	 * @param triggers The trigger (weighted sum plus bias) values.
	 * @return The activation outputs .
	 */
	double[] activations(double[] triggers);

	/**
	 * Calculates the first derivatives of the function, given the outputs.
	 * 
	 * @param outputs     The outputs obtained applying the triggers to
	 *                    <i>activations</i>.
	 * @return The derivatives.
	 */
	double[] derivatives(double[] outputs);
}
