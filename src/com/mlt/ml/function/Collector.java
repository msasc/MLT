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

package com.mlt.ml.function;

import java.util.Collection;

/**
 * A function that is applied to a collection of vectors to produce a result vector.
 *
 * @author Miquel Sas
 */
public interface Collector {
	/**
	 * Apply the function to a collection of vectors and produce a result vector.
	 * 
	 * @param vectors The collection of vectors.
	 * @return The result vector.
	 */
	double[] collect(Collection<double[]> vectors);

}
