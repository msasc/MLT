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

package com.mlt.ml.network.function.activation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.mlt.ml.network.function.Activation;
import com.mlt.util.IO;

/**
 * ReLU activation.
 *
 * @author Miquel Sas
 */
public class ActivationReLU implements Activation {

	/** Threshold. */
	private double threshold = 0;

	/**
	 * Constructor.
	 */
	public ActivationReLU() {
		super();
	}

	/**
	 * Constructor setting the threshold.
	 * 
	 * @param threshold The minimum value or threshold.
	 */
	public ActivationReLU(double threshold) {
		super();
		this.threshold = threshold;
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
	public void activations(double[] triggers, double[] outputs) {
		int length = triggers.length;
		for (int i = 0; i < length; i++) {
			outputs[i] = (triggers[i] > threshold ? triggers[i] : threshold);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void derivatives(double[] outputs, double[] derivatives) {
		int length = outputs.length;
		for (int i = 0; i < length; i++) {
			derivatives[i] = (outputs[i] <= threshold ? 0.0 : 1.0);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void restore(InputStream is) throws IOException {
		threshold = IO.readDouble(is);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void save(OutputStream os) throws IOException {
		IO.writeDouble(os, threshold);
	}
}
