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

package app.mlt.plaf.statistics.action;

import com.mlt.db.Persistor;
import com.mlt.desktop.action.ActionRun;
import com.mlt.desktop.control.TablePane;
import com.mlt.desktop.control.TableRecord;
import com.mlt.desktop.control.TableRecordModel;
import com.mlt.desktop.control.table.SelectionMode;
import com.mlt.desktop.icon.IconGrid;
import com.mlt.util.Logs;

import app.mlt.plaf.statistics.Statistics;
import app.mlt.plaf_old.MLT;
import app.mlt.plaf.DB;

/**
 * Browse and activate pattern fields.
 *
 * @author Miquel Sas
 */
public class ActionBrowsePatterns extends ActionRun {
	
	private Statistics stats;

	public ActionBrowsePatterns(Statistics stats) {
		super();
		this.stats = stats;
	}

	@Override
	public void run() {
		try {
			String key = stats.getTabPaneKey("PATTERNS");
			String text = stats.getTabPaneText("Patterns");
			
			Persistor persistor = stats.getTablePtn().getPersistor();
			
			TableRecordModel model = new TableRecordModel(persistor.getDefaultRecord());
			model.addColumn(DB.FIELD_PATTERN_DELTA);
			model.addColumn(DB.FIELD_PATTERN_NAME);
			model.addColumn(DB.FIELD_PATTERN_ACTIVE);

			model.setRecordSet(persistor.select(null));
			
			TableRecord table = new TableRecord(false);
			table.setSelectionMode(SelectionMode.MULTIPLE_ROW_INTERVAL);
			table.setModel(model);
			table.setSelectedRow(0);

			TablePane tablePane = new TablePane(table);

			IconGrid iconGrid = new IconGrid();
			iconGrid.setSize(16, 16);
			iconGrid.setMarginFactors(0.12, 0.12, 0.12, 0.12);

			MLT.getTabbedPane().addTab(key, iconGrid, text, "Defined ", tablePane);

		} catch (Exception exc) {
			Logs.catching(exc);
		}
	}
}
