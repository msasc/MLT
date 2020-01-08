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

import java.util.Collection;
import java.util.Iterator;

/**
 * Operations on matrices.
 *
 * @author Miquel Sas
 */
public class Matrix {

	/**
	 * Accumulates a collection of matrices of the same dimensions.
	 * 
	 * @param matrices The collection of matrices.
	 * @return The accumulated matrix.
	 */
	public static double[][] add(Collection<double[][]> matrices) {
		double[][] result = null;
		int rows = 0;
		int cols = 0;
		Iterator<double[][]> iter = matrices.iterator();
		while (iter.hasNext()) {
			double[][] matrix = iter.next();
			/* Initialize if required. */
			if (result == null) {
				rows = rows(matrix);
				cols = cols(matrix);
				result = new double[rows][cols];
			}
			/* Validate subsequent matrices dimensions. */
			if (rows(matrix) != rows || cols(matrix) != cols) {
				throw new IllegalArgumentException("Not all matrices have the same dimensions.");
			}
			/* Do accumulate. */
			for (int row = 0; row < rows; row++) {
				for (int col = 0; col < cols; col++) {
					result[row][col] += matrix[row][col];
				}
			}
		}
		return result;
	}

	/**
	 * Returns the number of columns of a matrix.
	 *
	 * @param matrix The argument matrix.
	 * @return The number of columns.
	 */
	public static int cols(double[][] matrix) {
		if (rows(matrix) != 0) {
			return matrix[0].length;
		}
		return 0;
	}

	/**
	 * Return a copy of the source matrix.
	 *
	 * @param src The source matrix.
	 * @return The copy.
	 */
	public static double[][] copy(double[][] src) {
		int rows = rows(src);
		int cols = cols(src);
		double[][] dst = new double[rows][cols];
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				dst[row][col] = src[row][col];
			}
		}
		return dst;
	}

	/**
	 * Cumulate the source array into the destination. Both must have the same
	 * dimensions.
	 *
	 * @param src The source.
	 * @param dst The destination.
	 */
	public static void cumulate(double[][] src, double[][] dst) {
		int rows = rows(src);
		int cols = cols(src);
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				dst[row][col] += src[row][col];
			}
		}
	}

	/**
	 * Calculate the Hadamard product of matrices a and b.
	 *
	 * @param a Matrix a.
	 * @param b Matrix b.
	 * @return The Hadamard product.
	 */
	public static double[][] hadamard(double[][] a, double[][] b) {
		int rows = rows(a);
		int cols = cols(a);
		double[][] h = new double[rows][cols];
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				h[r][c] = a[r][c] * b[r][c];
			}
		}
		return h;
	}

	/**
	 * Returns the number of rows of a matrix.
	 *
	 * @param matrix The argument matrix.
	 * @return The number of rows.
	 */
	public static int rows(double[][] matrix) {
		return matrix.length;
	}

	/**
	 * Set the matrix with a scalar value.
	 *
	 * @param matrix The matrix to initialize.
	 * @param value  The value to assign.
	 */
	public static void fill(double[][] matrix, double value) {
		int rows = rows(matrix);
		int cols = cols(matrix);
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				matrix[row][col] = value;
			}
		}
	}

}
