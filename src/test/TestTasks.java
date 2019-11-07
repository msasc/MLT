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

package test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.mlt.desktop.TaskFrame;
import com.mlt.task.Task;
import com.mlt.task.TaskList;
import com.mlt.task.sample.SampleTask;
import com.mlt.util.Numbers;
import com.mlt.util.Resources;
import com.mlt.util.Strings;

/**
 * Test an individual sample task, and a task list made of sample tasks.
 *
 * @author Miquel Sas
 */
public class TestTasks {
	
	static {
		Resources.addBaseTextResource("res/strings/StringsLibrary.xml");
		Locale.setDefault(Locale.US);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		List<Task> tasks = new ArrayList<>();
		tasks.add(getTask("0000", 10000, 5));
		tasks.add(getTaskList("LIST", 50, 400, 1));


		TaskFrame frame = new TaskFrame();
		frame.setTitle("Task tester");
		frame.addTasks(tasks);
		frame.show();
	}

	private static SampleTask getTask(String id, long iterations, long sleep) {
		SampleTask task = new SampleTask();
		task.setTitle("Task " + id);
		task.setIterations(iterations);
		task.setSleep(sleep);
		task.setCancelSupported(true);
		task.setCountSeconds(0);
		return task;
	}
	
	private static Task getTaskList(String idList, int size, long iterations, long sleep) {
		TaskList taskList = new TaskList();
		taskList.setTitle("Task " + idList);
		int digits = Numbers.getDigits(size);
		for (int i = 0; i < size; i++) {
			String id = Strings.leftPad(Integer.toString(i), digits, "0");
			SampleTask task = getTask(id, iterations, sleep);
//			if ( i == 5) {
//				task.setThrowAfterIterations(100);
//			}
			taskList.addTask(task);
		}
		return taskList;
	}
}
