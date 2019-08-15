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
package com.mlt.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Number utilities.
 *
 * @author Miquel Sas
 */
public class Numbers {

	/**
	 * Maximum positive double.
	 */
	public static final double MAX_DOUBLE = Double.MAX_VALUE;
	/**
	 * Minimum negative double.
	 */
	public static final double MIN_DOUBLE = -Double.MAX_VALUE;
	/**
	 * Maximum positive integer.
	 */
	public static final int MAX_INTEGER = Integer.MAX_VALUE;
	/**
	 * Minimum negative integer.
	 */
	public static final int MIN_INTEGER = -Integer.MAX_VALUE;
	/**
	 * Too small of a number. Useful to prevent numbers to become too small or
	 * negative infinite.
	 */
	public static final double TOO_SMALL = -1.0E50;
	/**
	 * Too big of a number. Useful to prevent numbers to become too big or infinite.
	 */
	public static final double TOO_BIG = 1.0E50;

	/**
	 * Bound the number so that it does not become too big or too small.
	 * 
	 * @param d The number to check.
	 * @return The new number. Only changed if it was too big or too small.
	 */
	public static double bound(final double d) {
		if (d < TOO_SMALL || d == Double.NEGATIVE_INFINITY) {
			return TOO_SMALL;
		} else if (d > TOO_BIG || d == Double.POSITIVE_INFINITY) {
			return TOO_BIG;
		} else {
			return d;
		}
	}

	/**
	 * Returns the floor number to the given decimal places. The decimal places can
	 * be negative.
	 *
	 * @param number   The source number.
	 * @param decimals The number of decimal places.
	 * @return The floor.
	 */
	public static double floor(double number, int decimals) {
		double pow = number * Math.pow(10, decimals);
		double floor = Math.floor(pow);
		double value = floor / Math.pow(10, decimals);
		return value;
	}

	/**
	 * Returns the big decimal for the value and scale.
	 *
	 * @param value    The value.
	 * @param decimals The number of decimal places.
	 * @return The big decimal.
	 */
	public static BigDecimal getBigDecimal(double value, int decimals) {
		return new BigDecimal(value).setScale(decimals, RoundingMode.HALF_UP);
	}

	/**
	 * Returns the number of integer digits of a number.
	 *
	 * @param number The number to check.
	 * @return The number of integer digits.
	 */
	public static int getDigits(double number) {
		String str = new BigDecimal(number).toPlainString();
		int index = str.indexOf('.');
		if (index <= 0) {
			return str.length();
		}
		return index;
	}

	/**
	 * Returns a list of increases to apply.
	 *
	 * @param integerDigits The number of integer digits.
	 * @param decimalDigits The number of decimal digits.
	 * @param multipliers   The list of multipliers.
	 * @return The list of increases.
	 */
	public static List<BigDecimal> getIncreases(int integerDigits, int decimalDigits, int... multipliers) {

		List<BigDecimal> increaments = new ArrayList<>();
		int upperScale = decimalDigits;
		int lowerScale = (integerDigits - 1) * (-1);
		for (int scale = upperScale; scale >= lowerScale; scale--) {
			for (int multiplier : multipliers) {
				double number = Math.pow(10, -scale);
				if (Double.isFinite(number)) {
					BigDecimal value = Numbers.getBigDecimal(number, scale);
					BigDecimal multiplicand = new BigDecimal(multiplier).setScale(0, RoundingMode.HALF_UP);
					increaments.add(value.multiply(multiplicand));
				}
			}
		}
		return increaments;
	}

	/**
	 * Check if the number is even.
	 *
	 * @param l The number.
	 * @return A boolean.
	 */
	public static boolean isEven(long l) {
		return (l % 2 == 0);
	}

	/**
	 * Check if the number is odd.
	 *
	 * @param l The number.
	 * @return A boolean.
	 */
	public static boolean isOdd(long l) {
		return !isEven(l);
	}

	/**
	 * Check if the number is even.
	 *
	 * @param d The number.
	 * @return A boolean.
	 */
	public static boolean isEven(double d) {
		return (d % 2 == 0);
	}

	/**
	 * Check if the number is odd.
	 *
	 * @param d The number.
	 * @return A boolean.
	 */
	public static boolean isOdd(double d) {
		return !isEven(d);
	}

	/**
	 * Return the maximum.
	 *
	 * @param nums List of numbers.
	 * @return The maximum.
	 */
	public static int max(int... nums) {
		int max = MIN_INTEGER;
		for (int num : nums) {
			if (num > max) {
				max = num;
			}
		}
		return max;
	}

	/**
	 * Return the maximum.
	 *
	 * @param nums List of numbers.
	 * @return The maximum.
	 */
	public static double max(double... nums) {
		double max = MIN_DOUBLE;
		for (double num : nums) {
			if (num > max) {
				max = num;
			}
		}
		return max;
	}

	/**
	 * Returns the remainder of the division of two integers.
	 *
	 * @param numerator   The numerator.
	 * @param denominator The denominator.
	 * @return The remainder.
	 */
	public static int remainder(int numerator, int denominator) {
		return numerator % denominator;
	}

	/**
	 * Round a number (in mode that most of us were taught in grade school).
	 *
	 * @param value    The value to round.
	 * @param decimals The number of decimal places.
	 * @return The rounded value.
	 */
	public static double round(double value, int decimals) {
		return new BigDecimal(value).setScale(decimals, RoundingMode.HALF_UP).doubleValue();
	}

}
