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

import com.mlt.db.Condition;
import com.mlt.db.Criteria;
import com.mlt.db.ListPersistor;
import com.mlt.db.Value;
import com.mlt.db.View;
import com.mlt.desktop.Option;
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
import com.mlt.mkt.data.DataRecordSet;
import com.mlt.util.Logs;
import com.mlt.util.Properties;

import app.mlt.plaf.DB;
import app.mlt.plaf.MLT;
import app.mlt.plaf.action.ActionStatistics;
import app.mlt.plaf.statistics.Statistics;

/**
 * Browse raw values.
 *
 * @author Miquel Sas
 */
public class ActionBrowseRaw extends ActionStatistics {

	private int delta;

	/**
	 * @param rootProperties
	 * @param delta
	 */
	public ActionBrowseRaw(Properties rootProperties, int delta) {
		super(rootProperties);
		this.delta = delta;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		try {
			Statistics stats = getStatistics();
			String key = stats.getTabPaneKey("RAW-" + delta);
			String text = stats.getTabPaneText("Raw delta " + delta);

			View view = stats.getTableRaw().getPersistor().getView();
			ListPersistor persistor = new ListPersistor(view.getPersistor(), view.getOrderBy());
			persistor.setCacheSize(10000);
			persistor.setPageSize(100);
			Criteria criteria = new Criteria();
			criteria.add(
				Condition.fieldEQ(view.getField(DB.FIELD_PATTERN_DELTA), new Value(delta)));
			persistor.setGlobalCriteria(criteria);

			TableRecordModel model = new TableRecordModel(persistor.getDefaultRecord());
			model.addColumn(DB.FIELD_PATTERN_DELTA);
			int index = view.getFieldIndex(DB.FIELD_BAR_TIME_FMT);
			for (int i = index; i < view.getFieldCount(); i++) {
				model.addColumn(view.getField(i).getAlias());
			}

			model.setRecordSet(new DataRecordSet(persistor));

			TableRecord table = new TableRecord(false);
			table.setSelectionMode(SelectionMode.MULTIPLE_ROW_INTERVAL);
			table.setModel(model);
			table.setSelectedRow(0);
			getProperties().setObject("TABLE-RAW-" + delta, table);

			TablePane tablePane = new TablePane(table);

			OptionPane optionPane = new OptionPane(Orientation.HORIZONTAL);
			optionPane.add(Option.option_COLUMNS(table));
			table.setPopupMenuProvider(optionPane);

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
