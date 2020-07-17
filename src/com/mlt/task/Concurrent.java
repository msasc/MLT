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

package com.mlt.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;

/**
 * Concurrent executor of a list of lists of tasks.
 *
 * @author Miquel Sas
 */
public class Concurrent {

	/** List of concurrent callables. */
	private List<Callable<Void>> concurrents;
	/** Pool size. */
	private int poolSize;
	/** Maximum concurrent tasks to start pooling. */
	private int maxConcurrent;
	/** Pool. */
	private ForkJoinPool pool;

	/**
	 * @param poolSize      Pool size.
	 * @param maxConcurrent Maximum concurrent tasks to start pooling.
	 */
	public Concurrent(int poolSize, int maxConcurrent) {
		super();
		this.poolSize = poolSize;
		this.maxConcurrent = maxConcurrent;
		this.concurrents = new ArrayList<>();
	}

	/**
	 * Add a callable task to the list of concurrent tasks and invoke the pool if
	 * the maximum concurrent size is reached.
	 * 
	 * @param concurrent The concurrent callable task.
	 */
	public void add(Callable<Void> concurrent) {
		concurrents.add(concurrent);
		if (concurrents.size() >= maxConcurrent) {
			getPool().invokeAll(concurrents);
			concurrents.clear();
		}
	}
	
	/**
	 * End by pooling not pooled tasks and shutting down the pool.
	 */
	public void end() {
		if (pool == null) {
			return;
		}
		if (!concurrents.isEmpty()) {
			getPool().invokeAll(concurrents);
			concurrents.clear();
		}
		getPool().shutdown();
		pool = null;
	}
	
	/**
	 * @return The pool initialized.
	 */
	private ForkJoinPool getPool() {
		if (pool == null) {
			pool = new ForkJoinPool(this.poolSize);
		}
		return pool;
	}
}
