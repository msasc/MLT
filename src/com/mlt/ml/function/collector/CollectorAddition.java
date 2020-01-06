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

package com.mlt.ml.function.collector;

import java.util.Collection;

import com.mlt.ml.function.Collector;
import com.mlt.util.Vector;

/**
 * Addition collector function.
 *
 * @author Miquel Sas
 */
public class CollectorAddition implements Collector {

	/**
	 * Constructor.
	 */
	public CollectorAddition() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double[] collect(Collection<double[]> vectors) {
		double[] result = Vector.add(vectors);
		if (result == null) {
			throw new IllegalArgumentException("Empty vectors.");
		}
		return result;
	}

}
