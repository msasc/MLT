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

package com.mlt.ml.function;

/**
 * Checker for a macth between the pattern output and the network output.
 *
 * @author Miquel Sas
 */
public interface Matcher {
	/**
	 * Check whether the argument vectors can be considered to be the same.
	 * 
	 * @param patternOutput Pattern output
	 * @param networkOutput Network output.
	 * @return A boolean.
	 */
	boolean match(double[] patternOutput, double[] networkOutput);
}
