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
import java.util.Locale;

/**
 * A task that sequentially executes a list of tasks.
 *
 * @author Miquel Sas
 */
public class TaskList extends Task {

	/** Key for the title. */
	private static final String LABEL_TITLE = "LABEL-TITLE";
	/** Key for the message. */
	private static final String LABEL_MESSAGE = "LABEL-MESSAGE";
	/** Key for the progress label. */
	private static final String LABEL_PROGRESS = "LABEL-PROGRESS";
	/** Key for the time. */
	private static final String LABEL_TIME = "LABEL-TIME";
	/** Key for the state. */
	private static final String LABEL_STATE = "LABEL-STATE";
	/** Key for the progress bar. */
	private static final String PROGRESS_BAR = "PROGRESS-BAR";

	/**
	 * Task adapter to display sub-tasks performance.
	 */
	class TaskHandler extends TaskAdapter {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onState(State state) {
			notifyStatusLabel(LABEL_STATE, LABEL_STATE, state.name());
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onTitle(String title) {
			notifyStatusLabel(LABEL_TITLE, LABEL_TITLE, title);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onMessage(String message) {
			notifyStatusLabel(LABEL_MESSAGE, LABEL_MESSAGE, message);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onProgress(long workDone, long totalWork) {
			notifyStatusProgress(PROGRESS_BAR, PROGRESS_BAR, (int) workDone, (int) totalWork);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onProgressMessage(String progressMessage) {
			notifyStatusLabel(LABEL_PROGRESS, LABEL_PROGRESS, progressMessage);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onTimeMessage(String timeMessage) {
			notifyStatusLabel(LABEL_TIME, LABEL_TIME, timeMessage);
		}
	}

	/** List of tasks. */
	private List<Task> tasks = new ArrayList<>();
	/** Task handler. */
	private TaskHandler handler;
	/** Current executing task. */
	private Task executingTask;

	/**
	 * Constructor.
	 */
	public TaskList() {
		this(Locale.getDefault());
	}

	/**
	 * Constructor.
	 * 
	 * @param locale The working locale for the task.
	 */
	public TaskList(Locale locale) {
		super(locale);
		this.handler = new TaskHandler();
		addStatus(LABEL_TITLE);
		addStatus(LABEL_MESSAGE);
		addStatus(LABEL_PROGRESS);
		addStatus(LABEL_TIME);
		addStatus(LABEL_STATE);
		addStatus(PROGRESS_BAR);
	}

	/**
	 * Add a task to the list of tasks to execute.
	 * 
	 * @param task The task.
	 */
	public void addTask(Task task) {
		task.addListener(handler);
		tasks.add(task);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected long calculateTotalWork() throws Throwable {
		return tasks.size();
	}

	/**
	 * Override to forward the cancel request to the executing task.
	 */
	@Override
	public void cancel() {
		super.cancel();
		if (executingTask != null) {
			executingTask.cancel();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void compute() throws Throwable {
		update("", 0, tasks.size());
		executingTask = null;
		for (int i = 0; i < tasks.size(); i++) {

			/* Check whether cancel is requested to exit. */
			if (isCancelRequested()) {
				setCancelled();
				break;
			}

			/* Task to execute. */
			executingTask = tasks.get(i);
			notifyStatusLabel(LABEL_TITLE, LABEL_TITLE, executingTask.getTitle());

			/* Update this task execution information. */
			String title = executingTask.getTitle();
			if (title == null) {
				title = "SUB-TASK " + (i + 1);
			}
			update(title, i + 1, tasks.size());

			/* Do call the task. */
			executingTask.call();

			/* Check any execution exception. */
			Throwable exception = executingTask.getException();
			if (exception != null) {
				throw exception;
			}
		}
	}
}
