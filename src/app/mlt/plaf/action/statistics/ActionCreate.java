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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.KeyStroke;

import com.mlt.db.Field;
import com.mlt.db.FieldGroup;
import com.mlt.db.Order;
import com.mlt.db.Persistor;
import com.mlt.db.Record;
import com.mlt.db.RecordSet;
import com.mlt.db.Table;
import com.mlt.db.Value;
import com.mlt.desktop.Option;
import com.mlt.desktop.OptionWindow;
import com.mlt.desktop.action.Action;
import com.mlt.desktop.control.Dialog;
import com.mlt.desktop.control.FormRecordPane;
import com.mlt.desktop.control.GridBagPane;
import com.mlt.desktop.control.TableRecord;
import com.mlt.mkt.data.Instrument;
import com.mlt.mkt.data.Period;
import com.mlt.task.Concurrent;
import com.mlt.util.Logs;
import com.mlt.util.Properties;

import app.mlt.plaf.DB;
import app.mlt.plaf.MLT;
import app.mlt.plaf.action.ActionStatistics;
import app.mlt.plaf.statistics.Statistics;

/**
 * Create new statistics.
 *
 * @author Miquel Sas
 */
public class ActionCreate extends ActionStatistics {
	
	/**
	 * Form statistics validator.
	 */
	class Validator extends Action {
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
				/* Validate retrieving parameters. */
				@SuppressWarnings("unused")
				Statistics stats = Statistics.get(rc);
				/* Everything ok, apply controls to record. */
				form.updateRecord();
			} catch (Exception exc) {
				getProperties().setBoolean(CAN_CONTINUE, false);
			}
		}

	}

	/**
	 * Constructor.
	 * 
	 * @param rootProperties Properties of the root action.
	 */
	public ActionCreate(Properties rootProperties) {
		super();
		getProperties().putAll(rootProperties);
	}

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

			Validator validator = new Validator();
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
			Statistics stats = Statistics.get(rc);

			/* Do create the tables again. */
			List<Table> tables = stats.getAllTables();
			for (Table table : tables) {
				if (DB.ddl().existsTable(table)) {
					DB.ddl().dropTable(table);
				}
				DB.ddl().buildTable(table);
			}

			/* Create list of patterns fields. */
			createPatternFields(stats);

			/* Save the record. */
			persistor.save(rc);

			/* Add to model. */
			TableRecord tableStats = getRootTable();
			RecordSet recordSet = tableStats.getModel().getRecordSet();
			Order order = persistor.getView().getOrderBy();
			int index = recordSet.getInsertIndex(rc, order);
			recordSet.add(index, rc);

		} catch (Exception exc) {
			Logs.catching(exc);
		}
	}

	private void createPatternFields(Statistics stats) throws Exception {
		int deltas = stats.getParameters().getDeltas().size();
		Persistor persistor = stats.getTablePtn().getPersistor();
		Concurrent concurrent = new Concurrent(10, 50);
		List<Field> fields = stats.getFieldListPatterns(true);
		for (int i = 0; i <= deltas; i++) {
			for (Field field : fields) {
				Record rc = persistor.getDefaultRecord();
				rc.setValue(DB.FIELD_PATTERN_DELTA, i);
				rc.setValue(DB.FIELD_PATTERN_NAME, field.getName());
				rc.setValue(DB.FIELD_PATTERN_ACTIVE, false);
				concurrent.add(new Record.Insert(rc, persistor));
			}
		}
		concurrent.end();
	}
}
