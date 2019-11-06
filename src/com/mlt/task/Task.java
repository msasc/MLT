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

import com.mlt.util.Formats;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantLock;

import com.mlt.util.Numbers;
import com.mlt.util.Resources;

import java.util.Locale;

/**
 * A <em>Runnable</em> and <em>Callable&#60;Void&#62;</em> task that publishes states and messages.
 * <p>
 * Four string messages are published:
 * <ul>
 * <li><em>title</em>, a user task title.</li>
 * <li><em>message</em>, a user message.</li>
 * <li><em>progressMessage</em>, an internally and standard updated progress message.</li>
 * <li><em>timeMessage</em>, an internally and standard updated time message.</li>
 * </ul>
 * A list of additional messages tagged by key can also be defined.
 *
 * @author Miquel Sas
 */
public abstract class Task implements Runnable, Callable<Void> {

	/**
	 * Timer task to update time.
	 */
	class ElapsedTimeTask extends TimerTask {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			updateTimeMessage();
		}
	}

	/** Task title. */
	private String title;
	/** State. */
	private State state = State.READY;

	/** List of additional status keys. */
	private List<String> statusKeys = new ArrayList<>();

	/** List of task listeners. */
	private List<TaskListener> listeners = new ArrayList<>();
	/** Time start, set when the task changes to the RUNNING state. */
	private double timeStart = -1;
	/** Time elapsed, set on the call to <code>updateMessageTime()</code>. */
	private double timeElapsed = -1;
	/** Time estimated, set on the call to <code>updateMessageTime()</code>. */
	private double timeEstimated = -1;
	/** Time remaining, set on the call to <code>updateMessageTime()</code>. */
	private double timeRemaining = -1;
	/** Work done. */
	private long workDone = 0;
	/** Total work. */
	private long totalWork = 0;

	/** Progress decimals for the progress message. */
	private int progressDecimals = 1;
	/** Progress modulus. */
	private int progressModulus = -1;

	/** A boolean that indicates if the task is counting steps. */
	private boolean counting = false;
	/**
	 * Timer to update the time elapsed when it is not possible to predict the total time or when there is no step
	 * notification.
	 */
	private Timer elapsedTimer;
	/** A boolean that indicates if cancel is supported. */
	private boolean cancelSupported = true;
	/** A boolean that indicates if a cancel has been requested. */
	private boolean cancelRequested = false;
	/** A boolean that indicates if the task is indeterminate. */
	private boolean indeterminate = false;

	/** Lock. */
	private ReentrantLock lock = new ReentrantLock();
	/** Exception if the task fails. */
	private Throwable exception;
	/** Parent task pool when this task is run concurrently in a task pool. */
	private TaskPool taskPool;

	/** The working locale. */
	private Locale locale;

	/**
	 * Constructor.
	 */
	public Task() {
		this(Locale.getDefault());
	}

	/**
	 * Constructor.
	 *
	 * @param locale The working locale.
	 */
	public Task(Locale locale) {
		super();
		this.locale = locale;
	}

	/**
	 * Add a listener.
	 *
	 * @param listener The task listener.
	 */
	public void addListener(TaskListener listener) {
		listeners.add(listener);
	}

	/**
	 * Add an additional status accessed by string key.
	 *
	 * @param key The status key.
	 */
	protected void addStatus(String key) {
		statusKeys.add(key);
	}

	/**
	 * Request the total work.Called by the parent pool.
	 *
	 * @return The total work.
	 * @throws java.lang.Throwable
	 */
	protected abstract long calculateTotalWork() throws Throwable;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Void call() throws Exception {
		execute();
		return null;
	}

	/**
	 * Request to cancel the task.
	 */
	public void cancel() {
		try {
			lock.lock();
			cancelRequested = true;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Clear the main message.
	 */
	protected void clearMessage() {
		updateMessage("");
	}
	
	/**
	 * Clear all status keys.
	 */
	protected void clearStatusKeys() {
		statusKeys.clear();
	}

	/**
	 * Clear the status label.
	 * 
	 * @param statusKey Status key.
	 * @param labelKey  Label key.
	 */
	protected void clearStatusLabel(String statusKey, String labelKey) {
		updateStatusLabel(statusKey, labelKey, "");
	}

	/**
	 * Compute the task. Extenders should implement this method to effectively perform the task.
	 *
	 * @throws java.lang.Throwable
	 */
	protected abstract void compute() throws Throwable;

	/**
	 * Execute the task setting the proper states and registering the exception if any.
	 */
	private void execute() {

		/* Set state to RUNNING. */
		setState(State.RUNNING);

		/* Perform computation and register any exception. */
		try {
			compute();
		} catch (Throwable exc) {
			exception = exc;
		}

		/** Set the state. */
		if (isCancelled()) {
			setState(State.CANCELLED);
			return;
		}
		if (exception != null) {
			setState(State.FAILED);
			return;
		}
		setState(State.SUCCEEDED);
	}

	/**
	 * Return the execution exception if any.
	 *
	 * @return The exception.
	 */
	public Throwable getException() {
		return exception;
	}

	/**
	 * Return the working locale.
	 *
	 * @return The locale.
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * Share the lock.
	 *
	 * @return The lock.
	 */
	protected ReentrantLock getLock() {
		return lock;
	}

	/*
	 * Internal building of messages.
	 */
	/**
	 * Return the progress message.
	 *
	 * @return The progress message.
	 */
	protected String getMessageProgress() {
		StringBuilder b = new StringBuilder();
		if (isIndeterminate()) {
			b.append(getMessageProgressIndeterminate());
		} else if (isCounting()) {} else if (getState().equals(State.READY)) {} else {
			b.append(getMessageProgressDeterminate());
		}
		return b.toString();
	}

	/**
	 * Return a standard progress message for a determinate task.
	 *
	 * @return The message.
	 */
	protected String getMessageProgressDeterminate() {
		StringBuilder b = new StringBuilder();
		b.append(getText("tokenProcessed"));
		b.append(" ");
		double workDone = getWorkDone();
		double totalWork = getTotalWork();
		double percentage = (workDone <= 0 ? 0.0 : 100.0 * workDone / totalWork);
		b.append(Numbers.getBigDecimal(percentage, progressDecimals));
		b.append("% (");
		b.append(Formats.fromLong(Double.valueOf(workDone).longValue(), locale));
		b.append(" ");
		b.append(getText("tokenOf"));
		b.append(" ");
		b.append(Formats.fromLong(Double.valueOf(totalWork).longValue(), locale));
		b.append(")");
		return b.toString();
	}

	/**
	 * Return a standard progress message for an indeterminate task.
	 *
	 * @return The message.
	 */
	protected String getMessageProgressIndeterminate() {
		return getText("tokenProcessing") + "...";
	}

	/**
	 * Return the time message.
	 *
	 * @param indeterminate A boolean to to force indeterminate message style.
	 * @return The time message.
	 */
	protected String getMessageTime(boolean indeterminate) {
		if (timeStart < 0) {
			return "";
		}
		double currenTime = System.currentTimeMillis();
		timeElapsed = currenTime - timeStart;
		StringBuilder b = new StringBuilder();
		b.append(getText("tokenTime"));
		b.append(" ");
		b.append(getText("tokenElapsed").toLowerCase());
		b.append(" ");
		b.append(getTimeString(timeElapsed));
		if (!counting && !indeterminate && totalWork > 0) {
			double progress = (double) workDone / totalWork;
			timeEstimated = timeElapsed / progress;
			timeRemaining = timeEstimated - timeElapsed;
			b.append(", ");
			b.append(getText("tokenEstimated").toLowerCase());
			b.append(" ");
			b.append(getTimeString(timeEstimated));
			b.append(", ");
			b.append(getText("tokenRemaining").toLowerCase());
			b.append(" ");
			b.append(getTimeString(timeRemaining));
			b.append(", ");
			b.append(getText("tokenTime").toLowerCase());
			b.append(" ");
			b.append(getText("tokenStarted").toLowerCase());
			b.append(" ");
			b.append(getTimestampString(timeStart));
			b.append(", ");
			b.append(getText("tokenEnd").toLowerCase());
			b.append(" ");
			b.append(getText("tokenTime").toLowerCase());
			b.append(" ");
			b.append(getTimestampString(timeStart + timeEstimated));
		}
		return b.toString();
	}

	/**
	 * Return the progress modulus.
	 * 
	 * @return The progress modulus.
	 */
	protected int getProgressModulus() {
		return progressModulus;
	}

	/**
	 * Return the state.
	 *
	 * @return The state.
	 */
	public State getState() {
		return state;
	}

	/**
	 * Returns the list of keys for additional status.
	 *
	 * @return The list of keys.
	 */
	public List<String> getStatusKeys() {
		return statusKeys;
	}

	/**
	 * Short cut to retrieve a text.
	 *
	 * @param key The key.
	 * @return The text.
	 */
	protected String getText(String key) {
		return Resources.getText(key, locale);
	}

	/**
	 * Returns the formatted time-stamp.
	 *
	 * @param time The time in milliseconds.
	 * @return The formatted time-stamp.
	 */
	protected String getTimestampString(double time) {
		return Formats.fromDateTime(Double.valueOf(time).longValue());
	}

	/**
	 * Returns the time information string (seconds,minutes or hours).
	 *
	 * @param time The time in milliseconds.
	 * @return The time info.
	 */
	protected String getTimeString(double time) {
		int decimals = 1;
		double seconds = (time / 1000.0);
		if (seconds < 60) {
			StringBuilder b = new StringBuilder();
			b.append(Formats.fromDouble(seconds, decimals, locale));
			b.append(" ");
			b.append(getText("tokenSeconds").toLowerCase());
			return b.toString();
		}
		double minutes = (time / (1000.0 * 60.0));
		if (minutes < 60) {
			StringBuilder b = new StringBuilder();
			b.append(Formats.fromDouble(minutes, decimals, locale));
			b.append(" ");
			b.append(getText("tokenMinutes").toLowerCase());
			return b.toString();
		}
		double hours = (time / (1000.0 * 60.0 * 60.0));
		StringBuilder b = new StringBuilder();
		b.append(Formats.fromDouble(hours, decimals, locale));
		b.append(" ");
		b.append(getText("tokenHours").toLowerCase());
		return b.toString();
	}

	/**
	 * Return the title.
	 *
	 * @return The title.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Return the internal total work value.
	 *
	 * @return The internal total work value.
	 */
	protected long getTotalWork() {
		return totalWork;
	}

	/*
	 * Protected access to work done and total.
	 */
	/**
	 * Return the internal work done value.
	 *
	 * @return The internal work done value.
	 */
	protected long getWorkDone() {
		return workDone;
	}

	/**
	 * Check if the task has already been cancelled.
	 *
	 * @return A boolean.
	 */
	public boolean isCancelled() {
		return getState().equals(State.CANCELLED);
	}

	/**
	 * Check if a cancel has been requested.
	 *
	 * @return A boolean.
	 */
	public boolean isCancelRequested() {
		try {
			lock.lock();
			return cancelRequested;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Check if this tasks supports cancel.
	 *
	 * @return A boolean.
	 */
	public boolean isCancelSupported() {
		return cancelSupported;
	}

	/**
	 * Check if the task is counting.
	 *
	 * @return A boolean.
	 */
	protected boolean isCounting() {
		return counting;
	}

	/**
	 * Check if this task is indeterminate.
	 *
	 * @return A boolean.
	 */
	public boolean isIndeterminate() {
		return indeterminate;
	}

	/**
	 * Check if the task is being pooled.
	 *
	 * @return A boolean.
	 */
	public boolean isPooled() {
		return taskPool != null;
	}

	/**
	 * Notify the main message.
	 *
	 * @param message The message string.
	 */
	protected void notifyOnMessage(String message) {
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).onMessage(message);
		}
	}

	/**
	 * Notify the <em>workDone</em>, <em>totalWork</em> and <em>progress</em> properties.
	 *
	 * @param workDone  Work done.
	 * @param totalWork Total work.
	 */
	protected void notifyProgress(long workDone, long totalWork) {
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).onProgress(this.workDone, this.totalWork);
		}
	}

	/**
	 * Notify the progress message.
	 *
	 * @param message The message.
	 */
	protected void notifyProgressMessage(String message) {
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).onProgressMessage(message);
		}
	}

	/**
	 * Notify to remove a status label.
	 * 
	 * @param statusKey Status key.
	 * @param labelKey  Label key.
	 */
	protected void notifyRemoveStatusLabel(String statusKey, String labelKey) {
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).onStatusRemoveLabel(statusKey, labelKey);
		}
	}

	/**
	 * Notify to remove a status progress.
	 * 
	 * @param statusKey   Status key.
	 * @param progressKey Progress key.
	 */
	protected void notifyRemoveStatusProgress(String statusKey, String progressKey) {
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).onStatusRemoveProgress(statusKey, progressKey);
		}
	}

	/**
	 * Notify the state.
	 *
	 * @param state The state.
	 */
	protected void notifyState(State state) {
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).onState(state);
		}
	}

	/**
	 * Notify a status label.
	 * 
	 * @param statusKey The status key.
	 * @param labelKey  The label key.
	 * @param text      The text.
	 */
	protected void notifyStatusLabel(String statusKey, String labelKey, String text) {
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).onStatusLabel(statusKey, labelKey, text);
		}
	}

	/**
	 * Notify a status progress.
	 * 
	 * @param statusKey   The status key.
	 * @param progressKey The progress key.
	 * @param workDone    Work done.
	 * @param totalWork   Total work.
	 */
	protected void notifyStatusProgress(String statusKey, String progressKey, int workDone, int totalWork) {
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).onStatusProgress(statusKey, progressKey, workDone, totalWork);
		}
	}

	/**
	 * Notify a status progress.
	 * 
	 * @param statusKey   The status key.
	 * @param progressKey The progress key.
	 * @param text        Text.
	 * @param workDone    Work done.
	 * @param totalWork   Total work.
	 */
	protected void notifyStatusProgress(String statusKey, String progressKey, String text, int workDone,
		int totalWork) {
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).onStatusProgress(statusKey, progressKey, text, workDone, totalWork);
		}
	}

	/**
	 * Notify the time message.
	 *
	 * @param message The message.
	 */
	protected void notifyTimeMessage(String message) {
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).onTimeMessage(message);
		}
	}

	/*
	 * Reinitialize the task.
	 */
	/**
	 * {@inheritDoc}
	 */
	public void reinitialize() {

		// Reset state variables.
		timeElapsed = -1;
		timeEstimated = -1;
		timeRemaining = -1;
		timeStart = -1;
		workDone = -1;
		totalWork = -1;
		cancelRequested = false;
		counting = false;
		exception = null;

		setState(State.READY);

		// Update to blank.
		update("", 0, 0);
	}

	/**
	 * Remove a status label.
	 * 
	 * @param statusKey The status key.
	 * @param labelKey  The label key.
	 */
	protected void removeStatusLabel(String statusKey, String labelKey) {
		if (isPooled()) {
			return;
		}
		notifyRemoveStatusLabel(statusKey, labelKey);
	}

	/**
	 * Remove a status progress.
	 * 
	 * @param statusKey   The status key.
	 * @param progressKey The progress key.
	 */
	protected void removeStatusProgress(String statusKey, String progressKey) {
		if (isPooled()) {
			return;
		}
		notifyRemoveStatusProgress(statusKey, progressKey);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		execute();
	}

	/**
	 * Indicate that the task has been already cancelled. Extender should call this method when acquainted of a request
	 * of cancel, and immediately exit the main loop.
	 */
	protected void setCancelled() {
		setState(State.CANCELLED);
	}

	/**
	 * Set that the task supports cancel.
	 *
	 * @param cancelSupported A boolean.
	 */
	public void setCancelSupported(boolean cancelSupported) {
		this.cancelSupported = cancelSupported;
	}

	/**
	 * Set that this task is counting.
	 *
	 * @param counting
	 */
	protected void setCounting(boolean counting) {
		this.counting = counting;
		if (counting) {
			startElapsedTimeTask();
		} else {
			stopElapsedTimeTask();
		}
	}

	/**
	 * Set the exception.
	 *
	 * @param exc The exception.
	 */
	protected void setException(Throwable exc) {
		this.exception = exc;
	}

	/**
	 * Set that the task is indeterminate.
	 *
	 * @param indeterminate A boolean.
	 */
	public void setIndeterminate(boolean indeterminate) {
		this.indeterminate = indeterminate;
	}

	/*
	 * Parameters.
	 */
	/**
	 * Set the progress decimals.
	 *
	 * @param progressDecimals The progress decimals.
	 */
	public void setProgressDecimals(int progressDecimals) {
		this.progressDecimals = progressDecimals;
	}

	/**
	 * Set the progress modulus.
	 *
	 * @param progressModulus The progress modulus.
	 */
	public void setProgressModulus(int progressModulus) {
		this.progressModulus = progressModulus;
	}

	/**
	 * Set the state.
	 *
	 * @param state The state.
	 */
	protected void setState(State state) {
		this.state = state;
		if (state.equals(State.RUNNING)) {
			timeStart = System.currentTimeMillis();
		}
		notifyState(state);
	}

	/**
	 * Set the parent task pool.
	 *
	 * @param taskPool The parent task pool.
	 */
	public void setTaskPool(TaskPool taskPool) {
		this.taskPool = taskPool;
	}

	/**
	 * Set the title.
	 *
	 * @param title The title.
	 */
	public void setTitle(String title) {
		this.title = title;
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).onTitle(title);
		}
	}

	/**
	 * Set the internal total work.
	 *
	 * @param totalWork The total work.
	 */
	protected void setTotalWork(long totalWork) {
		this.totalWork = totalWork;
	}

	/**
	 * Set the work done internal value.
	 *
	 * @param workDone The work done.
	 */
	protected void setWorkDone(long workDone) {
		this.workDone = workDone;
	}

	/**
	 * Start the elapsed time task.
	 */
	protected void startElapsedTimeTask() {
		elapsedTimer = new Timer("counting-timer", true);
		elapsedTimer.schedule(new ElapsedTimeTask(), 0, 10);
	}

	/**
	 * Stop the elapsed time task.
	 */
	protected void stopElapsedTimeTask() {
		elapsedTimer.cancel();
		elapsedTimer = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return getState() + " " + getTitle();
	}

	/**
	 * Updates the user message, the time and the progress messages for an indeterminate task.
	 *
	 * @param message The user message.
	 */
	protected void update(String message) {
		updateProgress(-1, -1);
		updateMessage(message);
		updateProgressMessage();
		updateTimeMessage();
	}

	/**
	 * Updates the user message, the work done and total, the time and the progress messages.
	 *
	 * @param message   The user message.
	 * @param workDone  The work done.
	 * @param totalWork The total work.
	 */
	protected void update(String message, long workDone, long totalWork) {
		if (isPooled()) {
			return;
		}
		boolean update = true;
		if (progressModulus > 0 && workDone < totalWork) {
			if (workDone % progressModulus != 0) {
				update = false;
			}
		}
		if (update) {
			updateProgress(workDone, totalWork);
			updateMessage(message);
			updateProgressMessage();
			updateTimeMessage();
		}
	}

	/**
	 * Update end counting.
	 */
	protected void updateCountingEnd() {
		setCounting(false);
	}

	/**
	 * Update start counting.
	 */
	protected void updateCountingStart() {
		setCounting(true);
		if (isPooled()) {
			return;
		}
		updateProgress(-1, -1);
		updateMessage(getText("tokenCounting") + "...");
		updateProgressMessage();
	}

	/**
	 * Update the main message.
	 *
	 * @param message The message string.
	 */
	protected void updateMessage(String message) {
		if (isPooled()) {
			return;
		}
		notifyOnMessage(message);
	}

	/**
	 * Updates the <em>workDone</em>, <em>totalWork</em> and <em>progress</em> properties.
	 *
	 * @param workDone  Work done.
	 * @param totalWork Total work.
	 */
	protected void updateProgress(long workDone, long totalWork) {

		if (isPooled()) {

			/*
			 * When pooled update the progress by accumulating the work done to the parent task pool.
			 */
			if (indeterminate || workDone < 0 || totalWork < 0) {
				return;
			}
			taskPool.getLock().lock();
			try {
				// Current task increases.
				long workDoneDelta = (getWorkDone() < 0 ? workDone : workDone - getWorkDone());
				setWorkDone(workDone);
				long totalWorkDelta = (getTotalWork() < 0 ? totalWork : totalWork - getTotalWork());
				setTotalWork(totalWork);
				// Parent workDone and total work to submit.
				long parentWorkDone = (taskPool.getWorkDone() < 0 ? workDoneDelta
					: taskPool.getWorkDone() + workDoneDelta);
				long parentTotalWork = (taskPool.getTotalWork() < 0 ? totalWorkDelta
					: taskPool.getTotalWork() + totalWorkDelta);
				// Do submit.
				taskPool.updateProgress(parentWorkDone, parentTotalWork);
				taskPool.updateProgressMessage();
				taskPool.updateTimeMessage();
			} finally {
				taskPool.getLock().unlock();
			}
		} else {

			/*
			 * Do the normal progress update.
			 */
			// Adjustments.
			if (Double.isInfinite(workDone) || Double.isNaN(workDone)) {
				workDone = 0;
			}
			if (Double.isInfinite(totalWork) || Double.isNaN(totalWork)) {
				totalWork = 0;
			}
			if (workDone < 0) {
				workDone = 0;
			}
			if (totalWork < 0) {
				totalWork = 0;
			}
			if (workDone > totalWork) {
				workDone = totalWork;
			}

			// Register.
			this.workDone = (workDone > totalWork ? totalWork : workDone);
			this.totalWork = totalWork;

			// Notify.
			notifyProgress(workDone, totalWork);
		}
	}

	/**
	 * Update the progress message.
	 */
	protected void updateProgressMessage() {
		if (isPooled()) {
			return;
		}
		notifyProgressMessage(getMessageProgress());
	}

	/**
	 * Update a status label.
	 * 
	 * @param statusKey The status key.
	 * @param labelKey  The label key.
	 * @param text      The text.
	 */
	protected void updateStatusLabel(String statusKey, String labelKey, String text) {
		if (isPooled()) {
			return;
		}
		notifyStatusLabel(statusKey, labelKey, text);
	}

	/**
	 * Update a status label.
	 * 
	 * @param statusKey   The status key.
	 * @param progressKey The progress key.
	 * @param workDone    Work done.
	 * @param totalWork   Total work.
	 */
	protected void updateStatusProgress(String statusKey, String progressKey, int workDone, int totalWork) {
		if (isPooled()) {
			return;
		}
		notifyStatusProgress(statusKey, progressKey, workDone, totalWork);
	}

	/**
	 * Update a status label.
	 * 
	 * @param statusKey   The status key.
	 * @param progressKey The progress key.
	 * @param text        The text.
	 * @param workDone    Work done.
	 * @param totalWork   Total work.
	 */
	protected void updateStatusProgress(
		String statusKey,
		String progressKey,
		String text,
		int workDone,
		int totalWork) {
		if (isPooled()) {
			return;
		}
		notifyStatusProgress(statusKey, progressKey, text, workDone, totalWork);
	}

	/**
	 * Update the time message.
	 */
	protected void updateTimeMessage() {
		if (isPooled()) {
			return;
		}
		updateTimeMessage(isIndeterminate());
	}

	/**
	 * Update the time message.
	 *
	 * @param indeterminate A boolean to to force indeterminate message style.
	 */
	protected void updateTimeMessage(boolean indeterminate) {
		if (isPooled()) {
			return;
		}
		notifyTimeMessage(getMessageTime(indeterminate));
	}

	/**
	 * Update the title property.
	 *
	 * @param title The title string.
	 */
	protected void updateTitle(String title) {
		if (isPooled()) {
			return;
		}
		setTitle(title);
	}
}
