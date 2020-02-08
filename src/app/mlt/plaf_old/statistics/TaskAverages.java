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

package app.mlt.plaf_old.statistics;

import com.mlt.desktop.Alert;
import com.mlt.desktop.Option;
import com.mlt.task.Task;
import com.mlt.util.HTML;

/**
 * Root of tasks based on averages statistics.
 *
 * @author Miquel Sas
 */
public abstract class TaskAverages extends Task {

	/** Underlying statistics averages. */
	protected Statistics stats;

	/**
	 * Constructor.
	 * 
	 * @param stats The statistics averages.
	 */
	public TaskAverages(Statistics stats) {
		super();
		this.stats = stats;
	}

	/**
	 * Ask the calculation option.
	 * 
	 * @return The calculation option.
	 */
	protected Option queryOption() {

		HTML text = new HTML();
		text.startTag("h2");
		text.print("Start from the begining or continue from last record calculated?");
		text.endTag("h2");

		Option optionContinue = new Option();
		optionContinue.setKey("CONITNUE");
		optionContinue.setText("Continue from last calculated record");
		optionContinue.setToolTip("Continue calculations from last calculated record");
		optionContinue.setCloseWindow(true);

		Option optionStart = new Option();
		optionStart.setKey("START");
		optionStart.setText("Start from begining");
		optionStart.setToolTip("Start calculations from the begining");
		optionStart.setCloseWindow(true);

		Option optionCancel = new Option();
		optionCancel.setKey("CANCEL");
		optionCancel.setText("Cancel calculations");
		optionCancel.setToolTip("Cancel calculations");
		optionCancel.setCloseWindow(true);

		Alert alert = new Alert();
		alert.setType(Alert.Type.CONFIRMATION);
		alert.setTitle("Calculation option");
		alert.setText(text);
		alert.setOptions(optionContinue, optionStart, optionCancel);

		return alert.show();
	}
}
