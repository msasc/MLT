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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;

/**
 * Helper calculator to execute a consumer functions in parallel.
 *
 * @author Miquel Sas
 */
public class IndexFunction {
	
	/** List of callables. */
	private List<IndexCall> functions;
	/** Size. */
	private int size;
	/** Function. */
	private Consumer<Integer> function;

	/**
	 * @param size     The size or number of functions.
	 * @param function The consumer function.
	 */
	public IndexFunction(int size, Consumer<Integer> function) {
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
			for (int index = 0; index < size; index++) {
				function.accept(index);
			}
		}
	}
	
	/**
	 * @param parallel A boolean.
	 */
	public void setParallel(boolean parallel) {
		functions = null;
		if (!parallel) {
			return;
		}
		functions = new ArrayList<>();
		for (int index = 0; index < size; index++) {
			functions.add(new IndexCall(index, function));
		}
	}
}
