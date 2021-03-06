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

package app.mlt.plaf_old.action;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.KeyStroke;

import com.mlt.db.FieldGroup;
import com.mlt.db.Order;
import com.mlt.db.Persistor;
import com.mlt.db.PersistorException;
import com.mlt.db.Record;
import com.mlt.db.RecordSet;
import com.mlt.db.Table;
import com.mlt.db.Value;
import com.mlt.desktop.Alert;
import com.mlt.desktop.Option;
import com.mlt.desktop.Option.Group;
import com.mlt.desktop.OptionWindow;
import com.mlt.desktop.action.Action;
import com.mlt.desktop.action.ActionRun;
import com.mlt.desktop.control.Control;
import com.mlt.desktop.control.Dialog;
import com.mlt.desktop.control.FormRecordPane;
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
import com.mlt.mkt.data.Instrument;
import com.mlt.mkt.data.Period;
import com.mlt.util.Logs;

import app.mlt.plaf_old.DB;
import app.mlt.plaf_old.MLT;
import app.mlt.plaf_old.statistics.Average;
import app.mlt.plaf_old.statistics.Statistics;

/**
 * Define and manage statistics on tickers.
 *
 * @author Miquel Sas
 */
public class ActionStatistics extends ActionRun {

	/**
	 * @param statsPrev Previous statistics.
	 * @param statsNext Next statistics.
	 * @return A boolean indicating whether the averages has been modified between
	 *         two parameters.
	 */
	static final boolean checkAveragesModified(
		Statistics statsPrev,
		Statistics statsNext) {
		List<Average> avgsPrev = statsPrev.getAverages();
		List<Average> avgsNext = statsNext.getAverages();
		if (avgsPrev.size() != avgsNext.size()) {
			return true;
		}
		for (int i = 0; i < avgsPrev.size(); i++) {
			Average avgPrev = avgsPrev.get(i);
			Average avgNext = avgsNext.get(i);
			if (!avgPrev.equals(avgNext)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Create a new statistics.
	 */
	class ActionCreate extends ActionRun {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			try {

				/* Ticker. */
				Record rcTicker = DB.lookup_ticker();
				if (rcTicker == null) {
					return;
				}
				String instrumentId = rcTicker.getValue(DB.FIELD_INSTRUMENT_ID).getString();
				String periodId = rcTicker.getValue(DB.FIELD_PERIOD_ID).getString();
				Instrument instrument = DB.to_instrument(instrumentId);
				Period period = DB.to_period(periodId);

				/* Statistics persistor. */
				Persistor persistor = DB.persistor_statistics();

				/* Statistics record to edit. */
				Record rc = persistor.getDefaultRecord();
				rc.setValue(DB.FIELD_SERVER_ID, new Value(MLT.getServer().getId()));
				rc.setValue(DB.FIELD_INSTRUMENT_ID, new Value(instrument.getId()));
				rc.setValue(DB.FIELD_PERIOD_ID, new Value(period.getId()));
				rc.setValue(DB.FIELD_PERIOD_NAME, new Value(period.toString()));
				rc.setValue(DB.FIELD_PERIOD_UNIT_INDEX, new Value(period.getUnit().ordinal()));
				rc.setValue(DB.FIELD_PERIOD_SIZE, new Value(period.getSize()));

				/* Form. */
				FormRecordPane form = new FormRecordPane(rc);
				form.setLayoutByRows(FieldGroup.EMPTY_FIELD_GROUP);

				form.addField(DB.FIELD_SERVER_ID);
				form.addField(DB.FIELD_INSTRUMENT_ID);
				form.addField(DB.FIELD_PERIOD_NAME);
				form.addField(DB.FIELD_STATISTICS_ID);
				form.addField(DB.FIELD_STATISTICS_PARAMS);

				form.getEditContext(DB.FIELD_SERVER_ID).getEditField().setEnabled(false);
				form.getEditContext(DB.FIELD_INSTRUMENT_ID).getEditField().setEnabled(false);
				form.getEditContext(DB.FIELD_PERIOD_NAME).getEditField().setEnabled(false);

				form.layout();
				form.updateEditors();

				OptionWindow wnd = new OptionWindow(new Dialog(null, new GridBagPane()));
				wnd.setTitle("New statistics");
				wnd.setOptionsBottom();

				wnd.setCenter(form.getPane());

				ValidatorStats validator = new ValidatorStats();
				validator.form = form;

				Option accept =
					Option.option_ACCEPT(
						KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK));
				accept.setCloseWindow(true);
				accept.setAction(validator);
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

				/* Everyting ok, setup the statistics. */
				rc = form.getRecord();
				Statistics stats = Statistics.getStatistics(rc);

				/* Do create the tables again. */
				List<Table> tables = stats.getTables();
				for (Table table : tables) {
					if (DB.ddl().existsTable(table)) {
						DB.ddl().dropTable(table);
					}
					DB.ddl().buildTable(table);
				}

				/* Save the record. */
				persistor.save(rc);

				/* Add to model. */
				RecordSet recordSet = tableStats.getModel().getRecordSet();
				Order order = persistor.getView().getOrderBy();
				int index = recordSet.getInsertIndex(rc, order);
				recordSet.add(index, rc);

			} catch (Exception exc) {
				Logs.catching(exc);
			}
		}
	}

	/**
	 * Delete the selected statistics.
	 */
	class ActionDelete extends ActionRun {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			try {
				/* Selected record. */
				Record rc = tableStats.getSelectedRecord();
				if (rc == null) {
					return;
				}

				/* Ask. */
				Option option = Alert.confirm("Delete current statistics");
				if (Option.isCancel(option)) {
					return;
				}

				/* Statistics averages. */
				Statistics stats = Statistics.getStatistics(rc);

				/* Statistics persistor. */
				Persistor persistor = DB.persistor_statistics();

				/* Drop the tables. */
				List<Table> tables = stats.getTables();
				for (Table table : tables) {
					if (DB.ddl().existsTable(table)) {
						DB.ddl().dropTable(table);
					}
				}

				/* Delete the record. */
				persistor.delete(rc);

				/* Remove from table. */
				int row = tableStats.getSelectedRow();
				tableStats.getModel().getRecordSet().remove(row);

			} catch (Exception exc) {
				Logs.catching(exc);
			}
		}
	}

	/**
	 * Modify the selected statistics.
	 */
	class ActionModify extends ActionRun {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			try {
				/* Selected record. */
				Record rc = tableStats.getSelectedRecord();
				if (rc == null) {
					return;
				}
				/* Current statistics. */
				Statistics statsPrev = Statistics.getStatistics(rc);
				
				/* Form. */
				FormRecordPane form = new FormRecordPane(rc);
				form.setLayoutByRows(FieldGroup.EMPTY_FIELD_GROUP);

				form.addField(DB.FIELD_SERVER_ID);
				form.addField(DB.FIELD_INSTRUMENT_ID);
				form.addField(DB.FIELD_PERIOD_NAME);
				form.addField(DB.FIELD_STATISTICS_ID);
				form.addField(DB.FIELD_STATISTICS_PARAMS);

				form.getEditContext(DB.FIELD_SERVER_ID).getEditField().setEnabled(false);
				form.getEditContext(DB.FIELD_INSTRUMENT_ID).getEditField().setEnabled(false);
				form.getEditContext(DB.FIELD_PERIOD_NAME).getEditField().setEnabled(false);
				form.getEditContext(DB.FIELD_STATISTICS_ID).getEditField().setEnabled(false);

				form.layout();
				form.updateEditors();
				
				OptionWindow wnd = new OptionWindow(new Dialog(null, new GridBagPane()));
				wnd.setTitle("Modify statistics parameters");
				wnd.setOptionsBottom();

				wnd.setCenter(form.getPane());

				ValidatorStats validator = new ValidatorStats();
				validator.form = form;

				Option accept =
					Option.option_ACCEPT(
						KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK));
				accept.setCloseWindow(true);
				accept.setAction(validator);
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
				
				/* Modified statistics. */
				Statistics statsNext = Statistics.getStatistics(form.getRecord());
				
				/* Add to model. */
				RecordSet recordSet = tableStats.getModel().getRecordSet();
				int index = tableStats.getSelectedRow();
				recordSet.set(index, form.getRecord());
				
				/* Persistor. */
				Persistor persistor = DB.persistor_statistics();
				persistor.update(form.getRecord());
				
				if (ActionStatistics.checkAveragesModified(statsPrev, statsNext)) {
					List<Table> tables;
					
					tables = statsPrev.getTables();
					for (Table table : tables) {
						if (DB.ddl().existsTable(table)) {
							DB.ddl().dropTable(table);
						}
					}
					
					tables = statsNext.getTables();
					for (Table table : tables) {
						if (DB.ddl().existsTable(table)) {
							DB.ddl().dropTable(table);
						}
						DB.ddl().buildTable(table);
					}
				}
				

			} catch (Exception exc) {
				Logs.catching(exc);
			}
		}
	}

	/**
	 * Popup menu provider.
	 */
	class MenuProvider implements PopupMenuProvider {
		@Override
		public PopupMenu getPopupMenu(Control control) {

			/* Option pane options. */
			PopupMenu popup = optionPane.getPopupMenu(control);

			/* Options from selected statistics. */
			Record rc = tableStats.getSelectedRecord();
			if (rc != null) {
				try {
					Statistics stats = Statistics.getStatistics(rc);
					List<Option> options = stats.getOptions();
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
				} catch (Exception exc) {
					Logs.catching(exc);
				}
			}

			return popup;
		}

	}

	/**
	 * Form statistics validator.
	 */
	class ValidatorStats extends Action {
		FormRecordPane form;

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				/* Edited record. */
				Record rc = form.getRecordEdited();
				/* Statistics id. */
				Value vStatId = rc.getValue(DB.FIELD_STATISTICS_ID);
				if (vStatId.isEmpty()) {
					throw new Exception("Statistics id can not be empty");
				}
				/* Validate and retrieve averages parameters. */
				Statistics stats = Statistics.getStatistics(rc);
				stats.validate();
				/* Everything ok, apply controls to record. */
				form.updateRecord();
			} catch (Exception exc) {
				getProperties().setBoolean(CAN_CONTINUE, false);
			}
		}

	}

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
			create.setAction(new ActionCreate());

			Option modify = new Option();
			modify.setText("Modify");
			modify.setToolTip("Modify the selected statistics parameters");
			modify.setOptionGroup(Group.EDIT);
			modify.setAction(new ActionModify());

			Option delete = new Option();
			delete.setText("Delete");
			delete.setToolTip("Delete the selected statistics");
			delete.setOptionGroup(Group.EDIT);
			delete.setAction(new ActionDelete());

			optionPane = new OptionPane(Orientation.HORIZONTAL);
			optionPane.add(create, modify, delete);

			tableStats.setPopupMenuProvider(new MenuProvider());

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
