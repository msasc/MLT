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
import java.awt.event.ActionEvent;

import com.mlt.db.FieldGroup;
import com.mlt.db.Persistor;
import com.mlt.db.PersistorException;
import com.mlt.db.Record;
import com.mlt.db.RecordSet;
import com.mlt.db.Value;
import com.mlt.desktop.Alert;
import com.mlt.desktop.Option;
import com.mlt.desktop.Option.Group;
import com.mlt.desktop.OptionWindow;
import com.mlt.desktop.action.Action;
import com.mlt.desktop.action.ActionRun;
import com.mlt.desktop.control.Dialog;
import com.mlt.desktop.control.FormRecordPane;
import com.mlt.desktop.control.GridBagPane;
import com.mlt.desktop.control.OptionPane;
import com.mlt.desktop.control.TablePane;
import com.mlt.desktop.control.TableRecord;
import com.mlt.desktop.control.table.SelectionMode;
import com.mlt.desktop.control.table.TableRecordModel;
import com.mlt.desktop.icon.IconChar;
import com.mlt.desktop.layout.Anchor;
import com.mlt.desktop.layout.Constraints;
import com.mlt.desktop.layout.Fill;
import com.mlt.desktop.layout.Insets;
import com.mlt.desktop.layout.Orientation;
import com.mlt.mkt.data.Instrument;
import com.mlt.mkt.data.Period;
import com.mlt.util.Logs;

import app.mlt.plaf.DB;
import app.mlt.plaf.MLT;
import app.mlt.plaf.db.Fields;

/**
 * Define and manage statistics on tickers.
 *
 * @author Miquel Sas
 */
public class ActionStatistics extends ActionRun {

	/**
	 * Create a new ticker.
	 */
	class ActionCreate extends ActionRun {
		
		/**
		 * Form statistics validator.
		 */
		class ValidatorStats extends Action {

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
			}
			
		}
		
		/**
		 * Return the record of statistics, without parameters
		 * @param instrument
		 * @param period
		 * @return
		 */
		Record getRecordStats(Instrument instrument, Period period) {
			
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			try {
				
				/* Instrument. */
				Record rcInstrument = DB.lookup_instrument();
				if (rcInstrument == null) {
					return;
				}
				Instrument instrument = DB.to_instrument(rcInstrument);

				/* Period. */
				Record rcPeriod = DB.lookup_period();
				if (rcPeriod == null) {
					return;
				}
				Period period = DB.to_period(rcPeriod);
				
				/* Statistics persistor. */
				Persistor pStats = DB.persistor_statistics();

				/* Statistics record to edit. */
				Record rcStats = pStats.getDefaultRecord();
				rcStats.setValue(Fields.SERVER_ID, new Value(MLT.getServer().getId()));
				rcStats.setValue(Fields.INSTRUMENT_ID, new Value(instrument.getId()));
				rcStats.setValue(Fields.PERIOD_ID, new Value(period.getId()));
				rcStats.setValue(Fields.PERIOD_NAME, new Value(period.toString()));

				/* Form. */
				FormRecordPane form = new FormRecordPane(rcStats);
				form.setLayoutByRows(FieldGroup.EMPTY_FIELD_GROUP);

				form.addField(Fields.SERVER_ID);
				form.addField(Fields.INSTRUMENT_ID);
				form.addField(Fields.PERIOD_NAME);
				form.addField(Fields.STATISTICS_ID);
				form.addField(Fields.STATISTICS_KEY);
				form.addField(Fields.STATISTICS_PARAMS);

				form.getEditContext(Fields.SERVER_ID).getEditField().setEnabled(false);
				form.getEditContext(Fields.INSTRUMENT_ID).getEditField().setEnabled(false);
				form.getEditContext(Fields.PERIOD_NAME).getEditField().setEnabled(false);

				form.layout();
				form.updateEditors();

				OptionWindow wnd = new OptionWindow(new Dialog(null, new GridBagPane()));
				wnd.setTitle("New statistics");
				wnd.setOptionsBottom();

				wnd.setCenter(form.getPane());
				
				Option accept = Option.option_ACCEPT();
				accept.setCloseWindow(true);
				wnd.getOptionPane().add(accept);
				
				Option cancel = Option.option_CANCEL();
				wnd.getOptionPane().add(cancel);
				wnd.getOptionPane().setMnemonics();

				wnd.pack();
				wnd.centerOnScreen();
				wnd.show();
				
				Option option = wnd.getOptionExecuted();
				if (Option.isCancel(option)) {
					return;
				}
				
				/* Statistics id. */
				Value vStatId = rcStats.getValue(Fields.STATISTICS_ID);
				if (vStatId.isEmpty()) {
					Alert.error("Statistics id can not be empty");
					return;
				}
				
				/* Validate that the statistics does not exist. */
				if (pStats.exists(rcStats)) {
					Alert.error("Statistics " + vStatId + " already exists!");
					return;
				}
				

			} catch (PersistorException exc) {
				Logs.catching(exc);
			}
		}
	}

	/**
	 * Constructor.
	 */
	public ActionStatistics() {
		super();
	}

	/**
	 * The table.
	 */
	private TableRecord tableStats;

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
			model.addColumn(Fields.INSTRUMENT_ID);
			model.addColumn(Fields.PERIOD_NAME);
			model.addColumn(Fields.STATISTICS_ID);
			model.addColumn(Fields.STATISTICS_PARAMS);
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
			create.setAction(new ActionCreate());

			Option delete = new Option();
			delete.setText("Delete");
			delete.setToolTip("Delete the selected statistics");
			delete.setOptionGroup(Group.EDIT);

			OptionPane optionPane = new OptionPane(Orientation.HORIZONTAL);
			optionPane.add(create, delete);
			tableStats.setPopupMenuProvider(optionPane);

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
