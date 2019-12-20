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
import java.util.concurrent.locks.ReentrantLock;

/**
 * Concurrent executor of a list of lists of tasks.
 *
 * @author Miquel Sas
 */
public class Concurrent implements Runnable {

	/** List of lists of tasks pending to execute. */
	private List<List<Callable<Void>>> lists;
	/** Current list pending to be filled. */
	private List<Callable<Void>> list;
	/** Maxmimum size of a single list. */
	private int maximumListSize = 100;
	/** Maximum number of lists. */
	private int maximumLists = 2;
	/** Fork join pool. */
	private ForkJoinPool pool;

	/**
	 * A flag that indicates that the main process has terminated, telling this
	 * thread to execute all pending tasks and then terminate.
	 */
	private boolean end;
	/**
	 * A flag that indicates that the thread should terminate without executing any
	 * pending task.
	 */
	private boolean terminate;

	/** Lock. */
	private ReentrantLock lock = new ReentrantLock();

	/**
	 * Default constructor.
	 */
	public Concurrent() {
		super();
		pool = new ForkJoinPool(10);
		lists = new ArrayList<>();
		end = false;
		terminate = false;
	}

	/**
	 * Indicate that the thread should execute all pending tasks and then terminate.
	 */
	public void end() {
		try {
			lock.lock();
			end = true;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Push a callable task to be concurrently executed.
	 * 
	 * @param callable The callable task.
	 */
	public void push(Callable<Void> callable) {
		try {
			lock.lock();
			/* Ensure list. */
			if (list == null) {
				list = new ArrayList<>();
			}
			/* Add to current list. */
			list.add(callable);
			/* Check maximum list size. */
			if (list.size() >= maximumListSize) {
				lists.add(list);
				list = new ArrayList<>();
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		while (true) {
			try {
				lock.lock();

				/* If should terminate, do it. */
				if (terminate) {
					/* Helping GC. */
					lists.clear();
					lists = null;
					list.clear();
					list = null;
					break;
				}

				/* If the lists are too big... */
				if (lists.size() >= maximumLists) {
					while (!lists.isEmpty()) {
						pool.invokeAll(lists.remove(0));
					}
					continue;
				}
				
				/* Normally execute the first list. */
				if (!end || !lists.isEmpty()) {
					pool.invokeAll(lists.remove(0));
					continue;
				}
				
				/* Normal end. */
				if (end && lists.isEmpty()) {
					break;
				}
				
			} finally {
				lock.unlock();
			}
		}
	}

	/**
	 * Indicate that the thread sould terminate without executing pending tasks.
	 */
	public void terminate() {
		try {
			lock.lock();
			terminate = true;
		} finally {
			lock.unlock();
		}
	}
}
