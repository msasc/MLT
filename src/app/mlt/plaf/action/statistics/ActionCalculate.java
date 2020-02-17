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

package app.mlt.plaf.action.statistics;

import com.mlt.desktop.TaskFrame;
import com.mlt.util.Properties;

import app.mlt.plaf.action.ActionStatistics;
import app.mlt.plaf.statistics.Statistics;
import app.mlt.plaf.statistics.TaskLabels;
import app.mlt.plaf.statistics.TaskNormalize;
import app.mlt.plaf.statistics.TaskPivots;
import app.mlt.plaf.statistics.TaskRanges;
import app.mlt.plaf.statistics.TaskRaw;

/**
 * Calculate raw, ranges, normalized, pivots, labels and patterns.
 *
 * @author Miquel Sas
 */
public class ActionCalculate extends ActionStatistics {

	/**
	 * @param rootProperties
	 */
	public ActionCalculate(Properties rootProperties) {
		super(rootProperties);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		Statistics stats = getStatistics();
		TaskFrame frame = new TaskFrame();
		frame.setTitle(stats.getLabel() + " - Calculate raw/normalized/pivots/labels");
		frame.addTasks(new TaskRaw(stats));
		frame.addTasks(new TaskRanges(stats));
		frame.addTasks(new TaskNormalize(stats));
		frame.addTasks(new TaskPivots(stats));
		frame.addTasks(new TaskLabels(stats, true));
//		frame.addTasks(new TaskPatterns(Statistics.this, true, 0.8));
		frame.show();
	}

}
