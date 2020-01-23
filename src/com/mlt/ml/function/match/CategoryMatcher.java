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

package com.mlt.ml.function.match;

import com.mlt.ml.function.Matcher;

/**
 * Eval the match when the correct output is a category, normally one element is
 * 1 and the rest 0. The index of the maximum of the network patytern has to be
 * the same than the index of the maximum of the pattern output.
 *
 * @author Miquel Sas
 */
public class CategoryMatcher implements Matcher {

	/**
	 * Constructor.
	 */
	public CategoryMatcher() {}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean match(double[] patternOutput, double[] networkOutput) {
		if (patternOutput.length != networkOutput.length) {
			throw new IllegalArgumentException();
		}
		int indexPattern = -1;
		int indexNetwork = -1;
		double maxPattern = Double.NEGATIVE_INFINITY;
		double maxNetwork = Double.NEGATIVE_INFINITY;
		int length = patternOutput.length;
		for (int i = 0; i < length; i++) {
			if (networkOutput[i] > maxNetwork) {
				maxNetwork = networkOutput[i];
				indexNetwork = i;
			}
			if (patternOutput[i] > maxPattern) {
				maxPattern = patternOutput[i];
				indexPattern = i;
			}
		}
		return indexPattern == indexNetwork;
	}

}
