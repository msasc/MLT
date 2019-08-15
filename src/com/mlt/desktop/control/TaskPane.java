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
package com.mlt.desktop.control;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.mlt.desktop.layout.Anchor;
import com.mlt.desktop.layout.Constraints;
import com.mlt.desktop.layout.Dimension;
import com.mlt.desktop.layout.Fill;
import com.mlt.desktop.layout.Insets;
import com.mlt.task.State;
import com.mlt.task.Task;
import com.mlt.util.Lists;

/**
 * A panel aimed to monitor the progress of a set of tasks.
 *
 * @author Miquel Sas
 */
public class TaskPane extends ScrollPane {

	/**
	 * Listener to remove progress panes when required by the pane.
	 */
	class ProgressPaneListener implements ProgressPane.Listener {
		@Override
		public void remove(ProgressPane pane) {
			progressPanes.remove(pane);
			layoutProgressPanes();
		}
	}

	/** Listener of progress panes. */
	private ProgressPaneListener progressPaneListener;
	/** List of progress panes. */
	private List<ProgressPane> progressPanes = new ArrayList<>();

	/** Execution pool. */
	private ThreadPoolExecutor pool;

	/**
	 * Constructor assigning the parallelism.
	 */
	public TaskPane() {
		super();
		pool = new ThreadPoolExecutor(0, 500, 2, TimeUnit.SECONDS, new SynchronousQueue<>());
		progressPaneListener = new ProgressPaneListener();

		GridBagPane pane = new GridBagPane();
		pane.setBackground(Color.WHITE);
		setView(pane);
	}

	/*
	 * Add tasks.
	 */

	/**
	 * Add a task to be managed.
	 * 
	 * @param task The task.
	 */
	public void addTask(Task task) {
		ProgressPane pane = new ProgressPane(pool, task);
		pane.addProgressPaneListener(progressPaneListener);
		progressPanes.add(pane);
		layoutProgressPanes();
	}

	/**
	 * Add the list of tasks.
	 * 
	 * @param tasks The list of tasks.
	 */
	public void addTasks(Task... tasks) {
		addTasks(Lists.asList(tasks));
	}

	/**
	 * Add the list of tasks.
	 * 
	 * @param tasks The list of tasks.
	 */
	public void addTasks(List<Task> tasks) {
		for (Task task : tasks) {
			ProgressPane pane = new ProgressPane(pool, task);
			pane.addProgressPaneListener(progressPaneListener);
			progressPanes.add(pane);
		}
		layoutProgressPanes();
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

	/*
	 * Scroll pane and layout.
	 */

	/**
	 * Layout the progress panes.
	 */
	private void layoutProgressPanes() {

		/* The pane that contains the progress panes. */
		GridBagPane pane = (GridBagPane) getView();
		pane.removeAll();

		/* Desired width for progress panes. */
		double width = pane.getPreferredSize().getWidth() - getVerticalScrollBar().getPreferredSize().getWidth() - 2;

		/* Do layout. */
		for (int i = 0; i < progressPanes.size(); i++) {

			ProgressPane progressPane = progressPanes.get(i);
			int height = progressPane.calculatePreferredHeight();
			progressPane.setPreferredSize(new Dimension(width, height));
			Insets insets = new Insets(5, 5, (i < progressPanes.size() - 1 ? 0 : 5), 5);
			double weighty = (i < progressPanes.size() - 1 ? 0 : 1);

			pane.add(progressPane, new Constraints(Anchor.TOP, Fill.HORIZONTAL, 0, i, 1, 1, 1, weighty, insets));
		}

		if (!progressPanes.isEmpty()) {
			revalidate();
		}
		repaint();
	}

	/*
	 * Helpers for containers of the task pane.
	 */

	/**
	 * Check if any task is running.
	 * 
	 * @return A boolean.
	 */
	public boolean isAnyTaskRunning() {
		for (int i = progressPanes.size() - 1; i >= 0; i--) {
			if (progressPanes.get(i).getTask().getState().equals(State.RUNNING)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Start all not running tasks.
	 */
	public void start() {
		for (int i = progressPanes.size() - 1; i >= 0; i--) {
			progressPanes.get(i).start();
		}
	}

	/**
	 * Cancel all running tasks.
	 */
	public void cancel() {
		for (int i = progressPanes.size() - 1; i >= 0; i--) {
			progressPanes.get(i).cancel();
		}
	}

	/**
	 * Remove all not running tasks.
	 */
	public void remove() {
		for (int i = progressPanes.size() - 1; i >= 0; i--) {
			progressPanes.get(i).remove();
		}
	}
}
