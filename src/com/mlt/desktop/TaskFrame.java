/*
 * Copyright (C) 2017 Miquel Sas
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

package com.mlt.desktop;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.KeyStroke;

import com.mlt.desktop.control.Frame;
import com.mlt.desktop.control.GridBagPane;
import com.mlt.desktop.control.TaskPane;
import com.mlt.task.Task;
import com.mlt.util.Lists;
import com.mlt.util.Resources;

/**
 * A frame to monitor and manage a list of tasks.
 *
 * @author Miquel Sas
 */
public class TaskFrame {

	/** Option window. */
	private OptionWindow taskFrame;
	/** Task pane. */
	private TaskPane taskPane;

	/**
	 * Constructor.
	 */
	public TaskFrame() {
		super();
		taskFrame = new OptionWindow(new Frame(new GridBagPane()));
		taskPane = new TaskPane();
		taskFrame.setCenter(taskPane);
		taskFrame.setOptionsBottom();

		/* Options. */

		Option optionStart = new Option();
		optionStart.setKey("START");
		optionStart.setText(Resources.getText("buttonStart"));
		optionStart.setToolTip(Resources.getText("tooltipStartNotRunning"));
		optionStart.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.CTRL_DOWN_MASK));
		optionStart.setAction(listener -> {
			taskPane.start();
		});

		Option optionCancel = new Option();
		optionCancel.setKey("CANCEL");
		optionCancel.setText(Resources.getText("buttonCancel"));
		optionCancel.setToolTip(Resources.getText("tooltipCancelAllRunning"));
		optionCancel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, KeyEvent.CTRL_DOWN_MASK));
		optionCancel.setAction(listener -> {
			taskPane.cancel();
		});

		Option optionRemove = new Option();
		optionRemove.setKey("REMOVE");
		optionRemove.setText(Resources.getText("buttonRemove"));
		optionRemove.setToolTip(Resources.getText("tooltipRemoveInactive"));
		optionRemove.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, KeyEvent.CTRL_DOWN_MASK));
		optionRemove.setAction(listener -> {
			taskPane.remove();
		});

		Option optionClose = new Option();
		optionClose.setKey("CLOSE");
		optionClose.setText(Resources.getText("buttonClose"));
		optionClose.setToolTip(Resources.getText("tooltipCloseTaskFrame"));
		optionClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
		optionClose.setDefaultClose(true);
		optionClose.setAction(listener -> {
			if (taskPane.isAnyTaskRunning()) {
				String title = Resources.getText("taskCanCloseTitle");
				String message = Resources.getText("taskCanCloseMessage");
				Alert.warning(taskFrame.getWindow(), title, message);
				return;
			}
			taskFrame.close();
		});

		List<Option> options = new ArrayList<>();
		options.add(optionStart);
		options.add(optionCancel);
		options.add(optionRemove);
		options.add(optionClose);

		taskFrame.getOptionPane().add(options);
		taskFrame.getOptionPane().setMnemonics();
	}

	/**
	 * Add a list of tasks to be managed.
	 * 
	 * @param tasks The list of tasks.
	 */
	public void addTasks(Task... tasks) {
		addTasks(Lists.asList(tasks));
	}

	/**
	 * Add a list of tasks to be managed.
	 * 
	 * @param tasks The list of tasks.
	 */
	public void addTasks(List<Task> tasks) {
		taskPane.addTasks(tasks);
	}

	/**
	 * Set the title.
	 * 
	 * @param title The title.
	 */
	public void setTitle(String title) {
		taskFrame.setTitle(title);
	}

	/**
	 * 
	 * @see com.mlt.lib.desktop.OptionWindow#show()
	 */
	public void show() {
		taskFrame.setSize(0.5, 0.8);
		taskFrame.centerOnScreen();
		taskFrame.show();
	}
}
