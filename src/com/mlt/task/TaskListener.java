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

/**
 * Interface that should implement listeners interested in receiving task notifications.
 *
 * @author Miquel Sas
 */
public interface TaskListener {

	/**
	 * Called when the task changes the state.
	 *
	 * @param state The new state.
	 */
	void onState(State state);

	/**
	 * Called when the title changes.
	 *
	 * @param title The title.
	 */
	void onTitle(String title);

	/**
	 * Called to update the main message.
	 *
	 * @param message The message.
	 */
	void onMessage(String message);

	/**
	 * Called to update the progress of the task, generally through a progress bar.
	 *
	 * @param workDone  The work done.
	 * @param totalWork The total work.
	 */
	void onProgress(long workDone, long totalWork);

	/**
	 * Called to update the progress message.
	 *
	 * @param progressMessage The progress message.
	 */
	void onProgressMessage(String progressMessage);

	/**
	 * Called to set a status text.
	 * 
	 * @param statusKey The status key.
	 * @param labelKey  The label key in the status bar.
	 * @param text      The text to show.
	 */
	void onStatusLabel(String statusKey, String labelKey, String text);

	/**
	 * Called to set a status progress.
	 * 
	 * @param statusKey   The status key.
	 * @param progressKey The label key in the status bar.
	 * @param workDone    Work done.
	 * @param totalWork   Total work.
	 */
	void onStatusProgress(String statusKey, String progressKey, int workDone, int totalWork);

	/**
	 * Called to remove a status label.
	 * 
	 * @param statusKey The status key.
	 * @param labelKey  The label key.
	 */
	void onStatusRemoveLabel(String statusKey, String labelKey);

	/**
	 * Called to remove a status progress.
	 * 
	 * @param statusKey The status key.
	 * @param progressKey  The progress key.
	 */
	void onStatusRemoveProgress(String statusKey, String progressKey);

	/**
	 * Called to set a status progress.
	 * 
	 * @param statusKey   The status key.
	 * @param progressKey The label key in the status bar.
	 * @param text        Progress text.
	 * @param workDone    Work done.
	 * @param totalWork   Total work.
	 */
	void onStatusProgress(String statusKey, String progressKey, String text, int workDone, int totalWork);

	/**
	 * Called to update the time message.
	 *
	 * @param timeMessage The time message.
	 */
	void onTimeMessage(String timeMessage);
}
