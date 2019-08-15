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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiConsumer;

/**
 * Helper calculator to execute a range functions in parallel.
 *
 * @author Miquel Sas
 */
public class RangeFunction {
	
	/**
	 * Build a list of range functions sized for the total indexes to process according to the available procssors.
	 * 
	 * @param indexCount The number of indexes to process.
	 * @param function   The bi-consumer function.
	 * @return The list of range functions.
	 */
	public static List<Range> list(int indexCount, BiConsumer<Integer, Integer> function) {
		int mod = Runtime.getRuntime().availableProcessors();
		while (indexCount % mod != 0) {
			mod--;
		}
		int rangeIndexes = indexCount / mod;
		List<Range> functions = new ArrayList<>();
		int start = 0;
		while (true) {
			int end = start + rangeIndexes - 1;
			if (end >= indexCount) end = indexCount - 1;
			functions.add(new Range(start, end, function));
			if (end == indexCount - 1) break;
			start = end + 1;
		}
		return functions;
	}
	
	/**
	 * A callable function to be executed for a range of indexes, start to end. Many calculations on matrices and vectors
	 * can be safely executed in parallel for a certain range of indexes.
	 */
	public static class Range implements Callable<Void> {

		/** Start index. */
		private int start;
		/** End index. */
		private int end;
		/** Bi-consumer function. */
		private BiConsumer<Integer, Integer> function;

		/**
		 * Constructor.
		 * 
		 * @param start    Start index.
		 * @param end      End index.
		 * @param function Bi-consumer function.
		 */
		public Range(int start, int end, BiConsumer<Integer, Integer> function) {
			this.start = start;
			this.end = end;
			this.function = function;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Void call() throws Exception {
			function.accept(start, end);
			return null;
		}
	}

	/** List of range functions. */
	private List<Range> functions;
	/** Size. */
	private int size;
	/** Function. */
	private BiConsumer<Integer, Integer> function;
	

	/**
	 * Constructor.
	 * 
	 * @param size Execution size, number of indexes.
	 * @param function The bi-consumer function.
	 */
	public RangeFunction(int size, BiConsumer<Integer, Integer> function) {
		super();
		this.size = size;
		this.function = function;
		setParallel(true);
	}

	/**
	 * Do process.
	 */
	public void process() {	
		if (functions != null) {
			ForkJoinPool.commonPool().invokeAll(functions);
		} else {
			function.accept(0, size - 1);
		}
	}
	
	/**
	 * Enable/disable parallel processing.
	 * 
	 * @param parallel A boolean.
	 */
	public void setParallel(boolean parallel) {
		functions = null;
		if (!parallel) return;
		functions = RangeFunction.list(size, function);
	}
}
