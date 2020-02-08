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

import com.mlt.db.Persistor;
import com.mlt.db.PersistorException;
import com.mlt.db.Record;
import com.mlt.db.RecordSet;
import com.mlt.desktop.Option;
import com.mlt.desktop.Option.Group;
import com.mlt.desktop.action.ActionRun;
import com.mlt.desktop.control.GridBagPane;
import com.mlt.desktop.control.OptionPane;
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

import app.mlt.plaf.DB;
import app.mlt.plaf.MLT;

/**
 * Define and manage statistics on tickers.
 *
 * @author Miquel Sas
 */
public class ActionStatistics extends ActionRun {

	/** The table. */
	private TableRecord tableStats;
	/** Option pane. */
	private OptionPane optionPane;
	
	/**
	 * Constructor.
	 */
	public ActionStatistics() {
		super();
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

			tableStats = new TableRecord();
			tableStats.setSelectionMode(SelectionMode.SINGLE_ROW_SELECTION);
			tableStats.setModel(model);
			tableStats.setSelectedRow(0);

			TablePane tablePane = new TablePane(tableStats);

			Option create = new Option();
			create.setText("Create");
			create.setToolTip("Create a new statistics");
			create.setOptionGroup(Group.EDIT);
//			create.setAction(new ActionCreate());

			Option modify = new Option();
			modify.setText("Modify");
			modify.setToolTip("Modify the selected statistics parameters");
			modify.setOptionGroup(Group.EDIT);
//			modify.setAction(new ActionModify());

			Option delete = new Option();
			delete.setText("Delete");
			delete.setToolTip("Delete the selected statistics");
			delete.setOptionGroup(Group.EDIT);
//			delete.setAction(new ActionDelete());

			optionPane = new OptionPane(Orientation.HORIZONTAL);
			optionPane.add(create, modify, delete);

//			tableStats.setPopupMenuProvider(new MenuProvider());

			GridBagPane pane = new GridBagPane();
			pane.add(tablePane,
				new Constraints(Anchor.TOP, Fill.BOTH, 0, 0, new Insets(0, 0, 0, 0)));
			pane.add(optionPane,
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
