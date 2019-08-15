/*
 * Copyright (C) 2018 Miquel Sas
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General PublicLicense as published by the Free Software
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

package com.mlt.ml.network.function;

import com.mlt.ml.network.Persistent;

/**
 * Activation function.
 *
 * @author Miquel Sas
 */
public interface Activation extends Persistent {

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
	 * @param outputs  The outputs to set.
	 */
	void activations(double[] triggers, double[] outputs);

	/**
	 * Calculates the first derivatives of the function, given the outputs.
	 * 
	 * @param outputs     The outputs obtained applying the triggers to
	 *                    <i>activations</i>.
	 * @param derivatives The derivatives to set.
	 */
	void derivatives(double[] outputs, double[] derivatives);
}
