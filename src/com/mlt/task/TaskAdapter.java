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
 * Task listener adapter.
 *
 * @author Miquel Sas
 */
public class TaskAdapter implements TaskListener {

	/**
	 * Constructor.
	 */
	public TaskAdapter() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onMessage(String message) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onProgress(long workDone, long totalWork) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onProgressMessage(String progressMessage) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onState(State state) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStatusLabel(String statusKey, String labelKey, String text) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStatusProgress(String statusKey, String progressKey, int workDone, int totalWork) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStatusProgress(String statusKey, String progressKey, String text, int workDone, int totalWork) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStatusRemoveLabel(String statusKey, String labelKey) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStatusRemoveProgress(String statusKey, String progressKey) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onTimeMessage(String timeMessage) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onTitle(String title) {
	}
}
