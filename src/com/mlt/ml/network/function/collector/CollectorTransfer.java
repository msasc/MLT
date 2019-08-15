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

package com.mlt.ml.network.function.collector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import com.mlt.ml.network.function.Collector;

/**
 *
 * @author Miquel Sas
 */
public class CollectorTransfer implements Collector {

	/**
	 * Constructor.
	 */
	public CollectorTransfer() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double[] collect(Collection<double[]> vectors) {
		if (vectors.isEmpty()) throw new IllegalStateException("No vectors");
		if (vectors.size() > 1) throw new IllegalStateException("Too many vectors");
		return vectors.iterator().next();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void restore(InputStream is) throws IOException {}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void save(OutputStream os) throws IOException {}
}
