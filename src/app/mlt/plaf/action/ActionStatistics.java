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

package app.mlt.plaf.action;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import com.mlt.db.Persistor;
import com.mlt.db.PersistorException;
import com.mlt.db.Record;
import com.mlt.db.RecordSet;
import com.mlt.desktop.Option;
import com.mlt.desktop.Option.Group;
import com.mlt.desktop.action.ActionRun;
import com.mlt.desktop.control.Control;
import com.mlt.desktop.control.GridBagPane;
import com.mlt.desktop.control.OptionPane;
import com.mlt.desktop.control.PopupMenu;
import com.mlt.desktop.control.PopupMenuProvider;
import com.mlt.desktop.control.TablePane;
import com.mlt.desktop.control.TableRecord;
import com.mlt.desktop.control.TableRecordModel;
import com.mlt.desktop.control.table.SelectionMode;
import com.mlt.desktop.icon.IconChar;
import com.mlt.desktop.layout.Anchor;
import com.mlt.desktop.layout.Constraints;
import com.mlt.desktop.layout.Fill;
import com.mlt.desktop.layout.Insets;
import com.mlt.desktop.layout.Orientation;
import com.mlt.util.Logs;
import com.mlt.util.Properties;

import app.mlt.plaf.DB;
import app.mlt.plaf.MLT;
import app.mlt.plaf.action.statistics.ActionBrowsePatterns;
import app.mlt.plaf.action.statistics.ActionCalculate;
import app.mlt.plaf.action.statistics.ActionCreate;
import app.mlt.plaf.action.statistics.ActionDelete;
import app.mlt.plaf.statistics.Statistics;

/**
 * Root of statistics actions.
 *
 * @author Miquel Sas
 */
public class ActionStatistics extends ActionRun {

	/**
	 * Popup menu provider.
	 */
	class MenuProvider implements PopupMenuProvider {
		@Override
		public PopupMenu getPopupMenu(Control control) {

			/* Option pane options. */
			OptionPane optionPane = (OptionPane) getProperties().getObject("OPTION-PANE");
			PopupMenu popup = optionPane.getPopupMenu(control);

			/* Options from selected statistics. */
			List<Option> options = getOptionsStats();
			Option.sort(options);
			popup.addSeparator();
			for (int i = 0; i < options.size(); i++) {
				popup.add(options.get(i).getMenuItem());
				if (i < options.size() - 1) {
					Option.Group gprev = options.get(i).getOptionGroup();
					Option.Group gnext = options.get(i + 1).getOptionGroup();
					if (!gprev.equals(gnext)) {
						popup.addSeparator();
					}
				}
			}

			return popup;
		}

	}

	/**
	 * Constructor.
	 */
	public ActionStatistics() {
		super();
	}

	/**
	 * Constructor for child actions.
	 * 
	 * @param rootProperties The root action properties.
	 */
	protected ActionStatistics(Properties rootProperties) {
		super();
		getProperties().putAll(rootProperties);
	}

	private List<Option> getOptionsEdit() {

		List<Option> options = new ArrayList<>();

		Option option;

		option = new Option();
		option.setText("Create");
		option.setToolTip("Create a new statistics");
		option.setOptionGroup(Group.EDIT);
		option.setAction(new ActionCreate(getProperties()));
		options.add(option);

		option = new Option();
		option.setText("Delete");
		option.setToolTip("Delete the selected statistics");
		option.setOptionGroup(Group.EDIT);
		option.setAction(new ActionDelete(getProperties()));
		options.add(option);

		return options;
	}

	private List<Option> getOptionsStats() {

		List<Option> options = new ArrayList<>();

		Option option;
		Option.Group groupCalculate = new Option.Group("CALCULATE", 1);
		Option.Group groupBrowse = new Option.Group("BROWSE", 2);
		Option.Group groupChart = new Option.Group("CHART", 3);

		TableRecord tableStats = getRootTable();
		Record rc = tableStats.getSelectedRecord();
		if (rc == null) {
			return options;
		}

		option = new Option();
		option.setKey("CALCULATE");
		option.setText("Calculate");
		option.setToolTip("Calculate statistic values");
		option.setAction(new ActionCalculate(getProperties()));
		option.setOptionGroup(groupCalculate);
		option.setSortIndex(1);
		options.add(option);

		option = new Option();
		option.setKey("BROWSE-PTN");
		option.setText("Browse and activate patterns");
		option.setToolTip("Browse and activate patterns");
		option.setAction(new ActionBrowsePatterns(getProperties()));
		option.setOptionGroup(groupBrowse);
		option.setSortIndex(1);
		options.add(option);

		return options;
	}
	
	protected OptionPane getRootOptionPane() {
		return (OptionPane) getProperties().getObject("OPTION-PANE");
	}

	protected Record getRootRecord() {
		TableRecord tableStats = getRootTable();
		Record rc = tableStats.getSelectedRecord();
		return rc;
	}

	protected TableRecord getRootTable() {
		return (TableRecord) getProperties().getObject("TABLE-STATS");
	}
	
	protected Statistics getStatistics() {
		Record rc = getRootRecord();
		if (rc == null) {
			return null;
		}
		Statistics stats = null;
		try {
			stats = Statistics.get(rc);
		} catch (Exception exc) {
			Logs.catching(exc);
		}
		return stats;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		try {
	
			if (MLT.getTabbedPane().getTabIndex("STATS") >= 0) {
				return;
			}
			MLT.getStatusBar().setLabel("STATS", "Setup statistics");
	
			Persistor persistor = DB.persistor_statistics();
			RecordSet recordSet = DB.recordset_statistics();
			Record masterRecord = persistor.getDefaultRecord();
	
			TableRecordModel model = new TableRecordModel(masterRecord);
			model.addColumn(DB.FIELD_INSTRUMENT_ID);
			model.addColumn(DB.FIELD_PERIOD_NAME);
			model.addColumn(DB.FIELD_STATISTICS_ID);
			model.addColumn(DB.FIELD_STATISTICS_PARAMS_DESC);
			model.setRecordSet(recordSet);
	
			TableRecord tableStats = new TableRecord();
			tableStats.setSelectionMode(SelectionMode.SINGLE_ROW_SELECTION);
			tableStats.setModel(model);
			tableStats.setSelectedRow(0);
			getProperties().setObject("TABLE-STATS", tableStats);
	
			TablePane tablePane = new TablePane(tableStats);
	
			OptionPane optionPane = new OptionPane(Orientation.HORIZONTAL);
			optionPane.add(getOptionsEdit());
			getProperties().setObject("OPTION-PANE", optionPane);
	
			tableStats.setPopupMenuProvider(new MenuProvider());
	
			GridBagPane pane = new GridBagPane();
			pane.add(
				tablePane,
				new Constraints(Anchor.TOP, Fill.BOTH, 0, 0, new Insets(0, 0, 0, 0)));
			pane.add(
				optionPane,
				new Constraints(Anchor.BOTTOM, Fill.HORIZONTAL, 0, 1, new Insets(0, 0, 0, 0)));
	
			IconChar iconChar = new IconChar();
			iconChar.setText("s");
			iconChar.setPaintForegroundEnabled(Color.RED);
			iconChar.setFont(new Font("Times New Roman Italic", Font.ITALIC, 12));
			iconChar.setSize(16, 16);
			iconChar.setMarginFactors(0.1, 0.1, 0.1, 0.1);
			iconChar.setFilled(false);
			iconChar.setOpaque(false);
	
			MLT.getTabbedPane().addTab("STATS", iconChar, "Statistics", "Defined statistics", pane);
			MLT.getStatusBar().removeLabel("STATS");
	
		} catch (PersistorException exc) {
			Logs.catching(exc);
		}
	}
}
