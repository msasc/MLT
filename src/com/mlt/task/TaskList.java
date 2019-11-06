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

	/** List of tasks. */
	private List<Task> tasks = new ArrayList<>();

	/**
	 * Constructor.
	 */
	public TaskList() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param locale The working locale for the task.
	 */
	public TaskList(Locale locale) {
		super(locale);
	}

	/**
	 * Add a task to the list of tasks to execute.
	 * 
	 * @param task The task.
	 */
	public void addTask(Task task) {
		
		/* Add the task. */
		tasks.add(task);
		
		/*
		 * Reset the list of status keys.
		 */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected long calculateTotalWork() throws Throwable {
		return tasks.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void compute() throws Throwable {
		/* Sequentially call each task. */
		for (Task task : tasks) {
			task.call();
		}
	}
}
