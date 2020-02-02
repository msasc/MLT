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

/**
 * Number utilities.
 *
 * @author Miquel Sas
 */
public class Numbers {

	/** Maximum positive double. */
	public static final double MAX_DOUBLE = Double.MAX_VALUE;
	/** Minimum negative double. */
	public static final double MIN_DOUBLE = -Double.MAX_VALUE;
	/** Maximum positive integer. */
	public static final int MAX_INTEGER = Integer.MAX_VALUE;
	/** Minimum negative integer. */
	public static final int MIN_INTEGER = -Integer.MAX_VALUE;

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
	 * Check in the list.
	 *
	 * @param value  The value to check.
	 * @param values The list of values.
	 * @return A boolean.
	 */
	public static boolean in(int value, int... values) {
		for (int v : values) {
			if (v == value) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check in the list.
	 *
	 * @param value  The value to check.
	 * @param values The list of values.
	 * @return A boolean.
	 */
	public static boolean in(double value, double... values) {
		for (double v : values) {
			if (v == value) {
				return true;
			}
		}
		return false;
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
	 * @param base    The base.
	 * @param compare The compare.
	 * @return The percentage of the base versus the compare.
	 */
	public static double percentDelta(double base, double compare) {
		return 100.0 * delta(base, compare);
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
	 * @param base    The base.
	 * @param compare The compare.
	 * @return The relative value of the base versus the compare.
	 */
	public static double factor(double base, double compare) {
		if (compare == 0) {
			return 0;
		}
		return (base / compare);
	}

	/**
	 * @param base    The base.
	 * @param compare The compare.
	 * @return The unitary delta (increase/decrease) value of the base versus the
	 *         compare.
	 */
	public static double delta(double base, double compare) {
		if (compare == 0) {
			return 0;
		}
		return ((base / compare) - 1);
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
