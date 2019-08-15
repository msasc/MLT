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
package com.mlt.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A task that executes other tasks in a concurrent pool. If one of the tasks of the pool is indeterminate, then all are
 * considered indeterminate.
 *
 * @author Miquel Sas
 */
public class TaskPool extends Task {

	/**
	 * Counter task.
	 */
	class Counter extends Task {

		Task task;

		Counter(Task task) {
			super(task.getLocale());
			this.task = task;
		}

		@Override
		protected void compute() throws Throwable {
			if (TaskPool.this.isCancelRequested()) {
				task.cancel();
				return;
			}
			task.setTotalWork(task.calculateTotalWork());
			if (TaskPool.this.isCancelRequested()) {
				task.cancel();
				return;
			}
			TaskPool.this.getLock().lock();
			try {
				TaskPool.this.countedTasks++;
				TaskPool.this.updateProgress(TaskPool.this.countedTasks, tasks.size());
				TaskPool.this.updateProgressMessage();
			} finally {
				TaskPool.this.getLock().unlock();
			}
		}

		@Override
		public boolean isCancelSupported() {
			return true;
		}

		@Override
		public boolean isIndeterminate() {
			return false;
		}

		@Override
		protected long calculateTotalWork() {
			return 0;
		}
	}

	/**
	 * Tasks listener.
	 */
	class TaskListener extends TaskAdapter {

		@Override
		public void onState(State state) {
			if (state.equals(State.FAILED)) {
				/* If a task fails, cancel execution. */
				TaskPool.this.cancel();
			}

			if (state.equals(State.SUCCEEDED)) {
				if (TaskPool.this.isIndeterminate()) {
					TaskPool.this.getLock().lock();
					try {
						TaskPool.this.successfulTasks++;
						TaskPool.this.updateProgress(TaskPool.this.successfulTasks, tasks.size());
						TaskPool.this.updateProgressMessage();
					} finally {
						TaskPool.this.getLock().unlock();
					}
				}
			}
		}
	}

	/**
	 * List of tasks to run concurrently.
	 */
	private List<Task> tasks = new ArrayList<>();
	/**
	 * Indeterminate flag.
	 */
	private boolean indeterminateSet = false;
	/**
	 * Indeterminate flag.
	 */
	private boolean cancelSupportedSet = false;
	/**
	 * Counted tasks when counting.
	 */
	private int countedTasks;
	/**
	 * Counter of successful tasks.
	 */
	private int successfulTasks;
	/**
	 * Execution pool.
	 */
	private ThreadPoolExecutor pool;

	/**
	 * Constructor.
	 */
	public TaskPool() {
		this(Locale.getDefault());
	}

	/**
	 * Constructor.
	 *
	 * @param locale Working locale.
	 */
	public TaskPool(Locale locale) {
		super(locale);
		pool = new ThreadPoolExecutor(0, 500, 2, TimeUnit.SECONDS, new SynchronousQueue<>());
	}

	/**
	 * Add a task to the list of tasks to execute.
	 *
	 * @param task The task.
	 */
	public void addTask(Task task) {
		task.setTaskPool(this);
		tasks.add(task);
		task.addListener(new TaskListener());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void cancel() {
		super.cancel();
		for (Task task : tasks) {
			task.cancel();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isIndeterminate() {
		if (!indeterminateSet) {
			/*
			 * If any task is indeterminate, then consider the pool indeterminate.
			 */
			setIndeterminate(false);
			for (Task task : tasks) {
				if (task.isIndeterminate()) {
					setIndeterminate(true);
					break;
				}
			}
			indeterminateSet = true;
		}
		return super.isIndeterminate();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCancelSupported() {
		if (!cancelSupportedSet) {
			/*
			 * If any task does no support cancel, then the pool should not support it.
			 */
			setCancelSupported(true);
			for (Task task : tasks) {
				if (!task.isCancelSupported()) {
					setCancelSupported(false);
					break;
				}
			}
			cancelSupportedSet = true;
		}
		return super.isCancelSupported();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void reinitialize() {
		countedTasks = 0;
		cancelSupportedSet = false;
		indeterminateSet = false;
		successfulTasks = 0;
		tasks.forEach(t -> t.reinitialize());
		super.reinitialize();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void compute() throws Throwable {

		if (isIndeterminate()) {
			/* If indeterminate, tag all to act as indeterminate. */
			tasks.forEach(task -> task.setIndeterminate(true));
		} else {
			/* Count. */
			calculateTotalWork();
		}

		/* Cancelled during count. */
		boolean processTasks = true;
		if (isCancelSupported() && isCancelRequested()) {
			processTasks = false;
		}

		/* Do process tasks. */
		if (processTasks) {
			if (isIndeterminate()) {
				startElapsedTimeTask();
			}
			updateMessage(getText("taskParallel"));
			pool.invokeAll(tasks);
			if (isIndeterminate()) {
				stopElapsedTimeTask();
			}
		}

		/* Set final state based on children state. */
		boolean succeeded = true;
		for (Task task : tasks) {
			if (!task.getState().equals(State.SUCCEEDED)) {
				succeeded = false;
				break;
			}
		}
		if (succeeded) {
			setState(State.SUCCEEDED);
			return;
		}
		boolean cancelled = true;
		for (Task task : tasks) {
			if (task.getState().equals(State.READY)) {
				continue;
			}
			if (task.getState().equals(State.SUCCEEDED)) {
				continue;
			}
			if (!task.getState().equals(State.CANCELLED)) {
				cancelled = false;
				break;
			}
		}
		if (cancelled) {
			setState(State.CANCELLED);
			return;
		}
		boolean failed = false;
		for (Task task : tasks) {
			if (task.getState().equals(State.FAILED)) {
				setException(task.getException());
				failed = true;
				break;
			}
		}
		if (failed) {
			setState(State.FAILED);
			return;
		}
		throw new IllegalStateException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected long calculateTotalWork() throws Throwable {
		setWorkDone(0);
		setTotalWork(0);
		List<Counter> counters = new ArrayList<>();
		tasks.forEach(task -> counters.add(new Counter(task)));
		updateCountingStart();
		pool.invokeAll(counters);
		updateCountingEnd();
		long totalWork = 0;
		for (Task task : tasks) {
			totalWork += task.getTotalWork();
		}
		setTotalWork(totalWork);
		return getTotalWork();
	}

	/**
	 * Update start counting.
	 */
	@Override
	protected void updateCountingStart() {
		if (isPooled()) {
			return;
		}
		setCounting(true);
		updateProgress(0, tasks.size());
		updateMessage(getText("tokenCounting") + "...");
	}

	/**
	 * Return the progress message.
	 *
	 * @return The progress message.
	 */
	@Override
	protected String getMessageProgress() {
		StringBuilder b = new StringBuilder();
		if (!getState().equals(State.READY)) {
			b.append(getMessageProgressDeterminate());
		}
		return b.toString();
	}

	/*
	 * Configure pool.
	 */
	/**
	 * Set the core pool size.
	 *
	 * @param corePoolSize The core pool size.
	 */
	public void setCorePoolSize(int corePoolSize) {
		pool.setCorePoolSize(corePoolSize);
	}

	/**
	 * Set the maximum pool size.
	 *
	 * @param maximumPoolSize The maximum pool size.
	 */
	public void setMaximumPoolSize(int maximumPoolSize) {
		pool.setMaximumPoolSize(maximumPoolSize);
	}

	/**
	 * Set the keep alive time in seconds.
	 *
	 * @param seconds The keep alive time in seconds.
	 */
	public void setKeepAliveSeconds(int seconds) {
		pool.setKeepAliveTime(seconds, TimeUnit.SECONDS);
	}
}
