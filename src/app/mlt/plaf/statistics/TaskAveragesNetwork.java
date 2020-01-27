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

package app.mlt.plaf.statistics;

import com.mlt.db.Criteria;
import com.mlt.db.Persistor;
import com.mlt.db.ValueMap;

/**
 * Apply the network to set network labels to visually show them in the graph.
 *
 * @author Miquel Sas
 */
public class TaskAveragesNetwork extends TaskAverages {
	
	private Persistor persistorSrc;
	private Persistor persistorView;

	/**
	 * @param stats The statistics averages.
	 */
	public TaskAveragesNetwork(StatisticsAverages stats) {
		super(stats);
		setId("averages-net");
		setTitle(stats.getLabel() + " - Apply network labels");
		
		this.persistorSrc = stats.getTableSources().getPersistor();
		this.persistorView = stats.getView(true, false, true, true, true, true, true, true).getPersistor();;
	}

	@Override
	protected long calculateTotalWork() throws Throwable {
		setTotalWork(persistorView.count(null));
		return getTotalWork();
	}

	@Override
	protected void compute() throws Throwable {
		
		/* Reset values. */
		ValueMap map = new ValueMap();
//		map.put(DB.FIELD_SOURCES_LABEL_CALC_NET, new Value(0));
		persistorSrc.update(new Criteria(), map);

	}

}
