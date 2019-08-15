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
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.border.LineBorder;

import com.mlt.desktop.Alert;
import com.mlt.desktop.border.LineBorderSides;
import com.mlt.desktop.graphic.Stroke;
import com.mlt.desktop.icon.IconArrow;
import com.mlt.desktop.icon.IconChar;
import com.mlt.desktop.icon.IconClose;
import com.mlt.desktop.icon.IconSquare;
import com.mlt.desktop.layout.Alignment;
import com.mlt.desktop.layout.Anchor;
import com.mlt.desktop.layout.Constraints;
import com.mlt.desktop.layout.Dimension;
import com.mlt.desktop.layout.Direction;
import com.mlt.desktop.layout.Fill;
import com.mlt.desktop.layout.Insets;
import com.mlt.task.State;
import com.mlt.task.Task;
import com.mlt.task.TaskAdapter;
import com.mlt.util.Resources;
import com.mlt.util.Strings;
import java.awt.EventQueue;

/**
 * Progress pane to monitor the progress of a single task.
 *
 * @author Miquel Sas
 */
public class ProgressPane extends GridBagPane {

	/**
	 * Listener to notify containers that it has been requested to remove the progress pane.
	 */
	public interface Listener {

		/**
		 * Request to remove the pane from the container.
		 *
		 * @param pane The progress pane.
		 */
		void remove(ProgressPane pane);
	}

	/**
	 * Action start a task.
	 */
	class ActionStart extends AbstractAction {

		@Override
		public void actionPerformed(ActionEvent e) {
			start();
		}
	}

	/**
	 * Action cancel a task.
	 */
	class ActionCancel extends AbstractAction {

		@Override
		public void actionPerformed(ActionEvent e) {
			cancel();
		}
	}

	/**
	 * Action info.
	 */
	class ActionInfo extends AbstractAction {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (task.getState().equals(State.FAILED)) {
				Throwable exc = task.getException();
				String trace = Strings.getStackTrace(exc);
				Stage owner = getStage();
				String title = Resources.getText("taskException");
				Alert.error(owner, title, trace);
			}
		}
	}

	/**
	 * Action remove the task.
	 */
	class ActionRemove extends AbstractAction {

		@Override
		public void actionPerformed(ActionEvent e) {
			remove();
		}
	}

	/**
	 * Task listener.
	 */
	class TaskListener extends TaskAdapter {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onState(State state) {
			labelState.setText(state.toString());
			if (state.equals(State.RUNNING)) {
				buttonStart.setEnabled(false);
				buttonCancel.setEnabled(true);
				buttonRemove.setEnabled(false);
			} else {
				buttonStart.setEnabled(true);
				buttonCancel.setEnabled(false);
				buttonRemove.setEnabled(true);
				if (progressBar.isIndeterminate()) {
					progressBar.setIndeterminate(false);
					progressBar.setTotalWork(0);
					progressBar.setWorkDone(0);
				}
			}
			buttonInfo.setEnabled(state.equals(State.FAILED));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onTitle(String title) {
			labelTitle.setText(title);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onMessage(String message) {
			EventQueue.invokeLater(() -> {
				labelMessage.setText(message);
			});
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onProgress(long workDone, long totalWork) {
			if (workDone < 0) {
				if (!progressBar.isIndeterminate()) {
					progressBar.setIndeterminate(true);
				}
				return;
			}
			if (progressBar.isIndeterminate()) {
				progressBar.setIndeterminate(false);
			}
			/* Convert workDone and totalWork to base 1000 */
			if (totalWork <= 0) {
				return;
			}
			progressBar.setWorkDone((int) (10000 * workDone / totalWork));
			progressBar.setTotalWork(10000);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onProgressMessage(String progressMessage) {
			labelProgress.setText(progressMessage);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onStatusLabel(String statusKey, String labelKey, String text) {
			for (StatusBar statusBar : statusBars) {
				if (statusBar.getName().equals("KEY-" + statusKey)) {
					statusBar.setLabel(labelKey, text);
					break;
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onStatusProgress(String statusKey, String progressKey, int workDone, int totalWork) {
			for (StatusBar statusBar : statusBars) {
				if (statusBar.getName().equals("KEY-" + statusKey)) {
					statusBar.setProgress(progressKey, workDone, totalWork);
					break;
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onStatusProgress(String statusKey, String progressKey, String text, int workDone, int totalWork) {
			for (StatusBar statusBar : statusBars) {
				if (statusBar.getName().equals("KEY-" + statusKey)) {
					statusBar.setProgress(progressKey, text, workDone, totalWork);
					break;
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onStatusRemoveLabel(String statusKey, String labelKey) {
			for (StatusBar statusBar : statusBars) {
				if (statusBar.getName().equals("KEY-" + statusKey)) {
					statusBar.removeLabel(labelKey);
					break;
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onStatusRemoveProgress(String statusKey, String progressKey) {
			for (StatusBar statusBar : statusBars) {
				if (statusBar.getName().equals("KEY-" + statusKey)) {
					statusBar.removeProgress(progressKey);
					break;
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onTimeMessage(String timeMessage) {
			labelTime.setText(timeMessage);
		}

	}

	/*
	 * Pool and task.
	 */
	/** The pool to submit the task. */
	private ThreadPoolExecutor pool;
	/** The task to be monitored and managed. */
	private Task task;

	/*
	 * Layout components.
	 */
	/** Button start. */
	private Button buttonStart;
	/** Button cancel. */
	private Button buttonCancel;
	/** Button info. */
	private Button buttonInfo;
	/** Button remove. */
	private Button buttonRemove;

	/** Title. */
	private Label labelTitle;
	/** User message. */
	private Label labelMessage;
	/** Pre-defined progress message. */
	private Label labelProgress;
	/** Pre-defined time message. */
	private Label labelTime;
	/** State. */
	private Label labelState;
	/** List of additional status bars by key. */
	private List<StatusBar> statusBars;
	/** Error message. */
	private Label labelError;

	/** Progress bar. */
	private ProgressBar progressBar;

	/** Progress pane listeners. */
	private List<Listener> listeners = new ArrayList<>();

	/**
	 * Constructor.
	 *
	 * @param pool The join pool.
	 * @param task The task.
	 */
	public ProgressPane(ThreadPoolExecutor pool, Task task) {
		super();
		this.pool = pool;
		this.task = task;

		/*
		 * Create components.
		 */
		IconArrow iconStart = new IconArrow();
		iconStart.setDirection(Direction.RIGHT);
		iconStart.setSize(24, 24);
		iconStart.setClosed(true);
		iconStart.setMarginFactors(0.20, 0.30, 0.20, 0.30);
		buttonStart = createButton("START", "tooltipStartTask", new ActionStart(), iconStart);

		IconSquare iconCancel = new IconSquare();
		iconCancel.setSize(24, 24);
		iconCancel.setMarginFactors(0.3, 0.3, 0.3, 0.3);
		buttonCancel = createButton("CANCEL", "tooltipCancelTask", new ActionCancel(), iconCancel);

		IconChar iconChar = new IconChar();
		iconChar.setText("i");
		iconChar.setPaintForegroundEnabled(Color.RED);
		iconChar.setFont(new Font("Times New Roman Italic", Font.ITALIC, 12));
		iconChar.setSize(24, 24);
		iconChar.setMarginFactors(0.17, 0.33, 0.17, 0.33);
		iconChar.setFilled(false);
		buttonInfo = createButton("INFO", "tooltipErrorInfo", new ActionInfo(), iconChar);

		IconClose iconClose = new IconClose();
		iconClose.setSize(24, 24);
		iconClose.setMarginFactors(0.25, 0.25, 0.25, 0.25);
		buttonRemove = createButton("REMOVE", "tooltipRemoveTask", new ActionRemove(), iconClose);

		Font fontTitle = new Font("Dialog", Font.BOLD, 16);
		Font fontPlain = new Font("Dialog", Font.PLAIN, 14);

		labelTitle = createLabel("TITLE", fontTitle);
		labelMessage = createLabel("MESSAGE", fontPlain);
		labelProgress = createLabel("PROGRESS", fontPlain);
		labelTime = createLabel("TIME", fontPlain);
		labelState = createLabel("STATE", fontPlain);

		statusBars = new ArrayList<>();
		for (String key : task.getStatusKeys()) {
			StatusBar statusBar = new StatusBar(new Insets(0, 0, 0, 0));
			statusBar.setName("KEY-" + key);
			statusBar.setFont(fontPlain);
			statusBar.setHorizontalAlignment(Alignment.LEFT);
			statusBar.setOpaque(false);
			statusBar.setProgressStringPainted(false);
			statusBar.setProgressBorder(new LineBorder(Color.LIGHT_GRAY));
			statusBars.add(statusBar);
		}

		labelError = createLabel("ERROR", fontPlain);

		progressBar = new ProgressBar();
		progressBar.setOpaque(false);
		progressBar.setBorder(new LineBorder(Color.LIGHT_GRAY));
		progressBar.setIndeterminate(false);
		progressBar.setPreferredSize(progressBar.getPreferredSize());
		progressBar.setMinimumSize(progressBar.getPreferredSize());

		/*
		 * Setup and layout.
		 */
		buttonStart.setEnabled(true);
		buttonCancel.setEnabled(false);
		buttonCancel.setVisible(task.isCancelSupported());
		buttonInfo.setEnabled(false);
		buttonRemove.setEnabled(true);

		labelTitle.setText(task.getTitle());
		Label.setPreferredAndMinimumSize(labelTitle);
		Label.setPreferredAndMinimumSize(labelMessage);
		Label.setPreferredAndMinimumSize(labelProgress);
		Label.setPreferredAndMinimumSize(labelTime);
		Label.setPreferredAndMinimumSize(labelState);
		Label.setPreferredAndMinimumSize(labelError);

		setBorder(new LineBorderSides(Color.LIGHT_GRAY, new Stroke(), false, false, true, false));
		setBackground(Color.WHITE);

		GridBagPane paneTitleAndButtons = new GridBagPane();
		paneTitleAndButtons.setOpaque(false);
		paneTitleAndButtons.add(labelTitle,
			new Constraints(Anchor.LEFT, Fill.HORIZONTAL, 0, 0, new Insets(0, 0, 0, 0)));
		paneTitleAndButtons.add(buttonStart, new Constraints(Anchor.RIGHT, Fill.NONE, 1, 0, new Insets(0, 1, 0, 0)));
		paneTitleAndButtons.add(buttonCancel, new Constraints(Anchor.RIGHT, Fill.NONE, 2, 0, new Insets(0, 1, 0, 0)));
		paneTitleAndButtons.add(buttonInfo, new Constraints(Anchor.RIGHT, Fill.NONE, 3, 0, new Insets(0, 1, 0, 0)));
		paneTitleAndButtons.add(buttonRemove, new Constraints(Anchor.RIGHT, Fill.NONE, 4, 0, new Insets(0, 1, 0, 0)));

		Anchor anchor = Anchor.TOP;
		Fill fill = Fill.HORIZONTAL;
		int y = 0;
		
		Insets insetsTitle = new Insets(5, 5, 0, 5);
		Insets insetsLine = new Insets(2, 5, 0, 5);
		Insets insetsError = new Insets(2, 5, 5, 5);
		
		
		add(paneTitleAndButtons, new Constraints(anchor, fill, 0, y++, 1, 1, 1, 0, insetsTitle));
		add(labelMessage, new Constraints(anchor, fill, 0, y++, 1, 1, 1, 0, insetsLine));
		add(labelProgress, new Constraints(anchor, fill, 0, y++, 1, 1, 1, 0, insetsLine));
		add(labelTime, new Constraints(anchor, fill, 0, y++, 1, 1, 1, 0, insetsLine));
		add(labelState, new Constraints(anchor, fill, 0, y++, 1, 1, 1, 0, insetsLine));
		add(progressBar, new Constraints(anchor, fill, 0, y++, 1, 1, 1, 0, insetsTitle));
		for (StatusBar statusBar : statusBars) {
			add(statusBar, new Constraints(anchor, fill, 0, y++, 1, 1, 1, 0, insetsLine));
		}
		add(labelError, new Constraints(anchor, fill, 0, y++, 1, 1, 1, 1, insetsError));

		/*
		 * Install the task listener.
		 */
		task.addListener(new TaskListener());
	}

	/**
	 * Add a progress pane listeners.
	 *
	 * @param listener The listener.
	 */
	public void addProgressPaneListener(Listener listener) {
		listeners.add(listener);
	}

	/**
	 * Return the task.
	 *
	 * @return The task.
	 */
	public Task getTask() {
		return task;
	}

	/**
	 * Calculate the preferred height.
	 *
	 * @return The preferred height.
	 */
	public int calculatePreferredHeight() {
		GridBagLayout layout = (GridBagLayout) getLayout();
		int height = 0;
		for (int i = 0; i < getControlCount(); i++) {
			Control cmp = getControl(i);
			int top = layout.getConstraints(cmp.getComponent()).insets.top;
			int bottom = layout.getConstraints(cmp.getComponent()).insets.bottom;
			int cmpHeight = Double.valueOf(cmp.getPreferredSize().getHeight()).intValue();
			height += top + cmpHeight + bottom;
		}
		return height;
	}

	/**
	 * Create the button.
	 *
	 * @param name   Name.
	 * @param action Action.
	 * @param image  Image.
	 * @return The button.
	 */
	private Button createButton(String name, String toolTipKey, Action action, Icon icon) {
		Button button = new Button();
		button.setName(name);
		button.setAction(action);
		button.setText(null);
		button.setToolTipText(Resources.getText(toolTipKey));
		button.setBackground(Color.WHITE);
		button.setIconTextGap(0);
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setIcon(icon);
		Dimension size = new Dimension(icon.getIconWidth(), icon.getIconHeight());
		button.setMinimumSize(size);
		button.setMaximumSize(size);
		button.setPreferredSize(size);
		return button;
	}

	/**
	 * Create the label.
	 *
	 * @param name Name.
	 * @param font Font.
	 * @return The label.
	 */
	private Label createLabel(String name, Font font) {
		Label label = new Label();
		label.setName(name);
		label.setFont(font);
		return label;
	}

	/*
	 * Actions.
	 */
	/**
	 * Start the task.
	 */
	public void start() {
		if (!task.getState().equals(State.RUNNING)) {
			task.reinitialize();
			pool.execute(task);
		}
	}

	/**
	 * Cancel the task if running.
	 */
	public void cancel() {
		if (task.getState().equals(State.RUNNING)) {
			task.cancel();
		}
	}

	/**
	 * Remove the task if not running.
	 */
	public void remove() {
		if (task.getState().equals(State.RUNNING)) {
			return;
		}
		listeners.forEach(listener -> listener.remove(ProgressPane.this));
	}
}
