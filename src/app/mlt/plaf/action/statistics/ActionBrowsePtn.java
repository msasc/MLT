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

import java.util.ArrayList;
import java.util.List;

import com.mlt.db.Persistor;
import com.mlt.db.Record;
import com.mlt.desktop.Option;
import com.mlt.desktop.Option.Group;
import com.mlt.desktop.action.ActionRun;
import com.mlt.desktop.control.GridBagPane;
import com.mlt.desktop.control.OptionPane;
import com.mlt.desktop.control.TablePane;
import com.mlt.desktop.control.TableRecord;
import com.mlt.desktop.control.TableRecordModel;
import com.mlt.desktop.control.table.SelectionMode;
import com.mlt.desktop.icon.IconGrid;
import com.mlt.desktop.layout.Anchor;
import com.mlt.desktop.layout.Constraints;
import com.mlt.desktop.layout.Fill;
import com.mlt.desktop.layout.Insets;
import com.mlt.desktop.layout.Orientation;
import com.mlt.task.Concurrent;
import com.mlt.util.Logs;
import com.mlt.util.Properties;

import app.mlt.plaf.DB;
import app.mlt.plaf.MLT;
import app.mlt.plaf.action.ActionStatistics;
import app.mlt.plaf.statistics.Statistics;

/**
 * Browse and activate/deactivate patterns.
 *
 * @author Miquel Sas
 */
public class ActionBrowsePtn extends ActionStatistics {

	class ChangeActivation extends ActionRun {
		boolean activate;

		ChangeActivation(Properties properties, boolean activate) {
			this.activate = activate;
			getProperties().putAll(properties);
		}

		@Override
		public void run() {
			try {
				TableRecord table = (TableRecord) getProperties().getObject("TABLE-PATTERNS");
				List<Record> records = table.getSelectedRecords();
				Persistor persistor = getStatistics().getTablePtn().getPersistor();
				Concurrent concurrent = new Concurrent(10, 50);
				for (Record record : records) {
					record.setValue(DB.FIELD_PATTERN_ACTIVE, activate);
//					persistor.update(record);
					concurrent.add(new Record.Update(record, persistor));
				}
				concurrent.end();
				table.getModel().refresh();
			} catch (Exception exc) {
				Logs.catching(exc);
			}
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param rootProperties Properties of the root action.
	 */
	public ActionBrowsePtn(Properties rootProperties) {
		super(rootProperties);
	}

	private List<Option> getOptionsEdit() {

		List<Option> options = new ArrayList<>();

		Option option;

		option = new Option();
		option.setText("Activate");
		option.setToolTip("Activate selected patterns");
		option.setOptionGroup(Group.EDIT);
		option.setAction(new ChangeActivation(getProperties(), true));
		options.add(option);

		option = new Option();
		option.setText("Deactivate");
		option.setToolTip("Deactivate selected patterns");
		option.setOptionGroup(Group.EDIT);
		option.setAction(new ChangeActivation(getProperties(), false));
		options.add(option);

		return options;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		try {
			Statistics stats = getStatistics();
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
			getProperties().setObject("TABLE-PATTERNS", table);

			TablePane tablePane = new TablePane(table);

			OptionPane optionPane = new OptionPane(Orientation.HORIZONTAL);
			optionPane.add(getOptionsEdit());

			GridBagPane pane = new GridBagPane();
			pane.add(
				tablePane,
				new Constraints(Anchor.TOP, Fill.BOTH, 0, 0, new Insets(0, 0, 0, 0)));
			pane.add(
				optionPane,
				new Constraints(Anchor.BOTTOM, Fill.HORIZONTAL, 0, 1, new Insets(0, 0, 0, 0)));

			IconGrid iconGrid = new IconGrid();
			iconGrid.setSize(16, 16);
			iconGrid.setMarginFactors(0.12, 0.12, 0.12, 0.12);

			MLT.getTabbedPane().addTab(key, iconGrid, text, "Defined ", pane);

		} catch (Exception exc) {
			Logs.catching(exc);
		}
	}
}
