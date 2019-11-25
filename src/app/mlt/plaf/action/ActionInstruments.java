/*
 * Copyright (C) 2018 Miquel Sas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package app.mlt.plaf.action;

import java.awt.Color;
import java.awt.Font;

import com.mlt.db.Persistor;
import com.mlt.db.PersistorException;
import com.mlt.db.Record;
import com.mlt.db.RecordSet;
import com.mlt.desktop.action.ActionRun;
import com.mlt.desktop.control.TablePane;
import com.mlt.desktop.control.TableRecord;
import com.mlt.desktop.control.TableRecordModel;
import com.mlt.desktop.control.table.SelectionMode;
import com.mlt.desktop.icon.IconChar;
import com.mlt.util.Logs;

import app.mlt.plaf.DB;
import app.mlt.plaf.MLT;

/**
 * Packs the instruments available and synchronize actions.
 *
 * @author Miquel Sas
 */
public class ActionInstruments {

	/**
	 * Browse available instruments.
	 */
	public static class Available extends ActionRun {

		/**
		 * Constructor.
		 */
		public Available() {
			super();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			try {
				if (MLT.getTabbedPane().getTabIndex("INST-AV") >= 0) {
					return;
				}
				MLT.getStatusBar().setLabel("INST-AV", "Setup available instruments");

				Persistor persistor = DB.persistor_instruments();
				RecordSet recordSet = DB.recordset_instruments();
				Record masterRecord = persistor.getDefaultRecord();

				TableRecordModel model = new TableRecordModel(masterRecord);
				model.addColumn(DB.FIELD_INSTRUMENT_ID);
				model.addColumn(DB.FIELD_INSTRUMENT_DESC);
				model.addColumn(DB.FIELD_INSTRUMENT_PIP_VALUE);
				model.addColumn(DB.FIELD_INSTRUMENT_PIP_SCALE);
				model.addColumn(DB.FIELD_INSTRUMENT_TICK_VALUE);
				model.addColumn(DB.FIELD_INSTRUMENT_TICK_SCALE);
				model.addColumn(DB.FIELD_INSTRUMENT_VOLUME_SCALE);
				model.addColumn(DB.FIELD_INSTRUMENT_PRIMARY_CURRENCY);
				model.addColumn(DB.FIELD_INSTRUMENT_SECONDARY_CURRENCY);
				model.setRecordSet(recordSet);

				TableRecord table = new TableRecord(true);
				table.setSelectionMode(SelectionMode.SINGLE_ROW_SELECTION);
				table.setModel(model);
				table.setSelectedRow(0);

				IconChar iconChar = new IconChar();
				iconChar.setText("i");
				iconChar.setPaintForegroundEnabled(Color.RED);
				iconChar.setFont(new Font("Times New Roman Italic", Font.ITALIC, 12));
				iconChar.setSize(16, 16);
				iconChar.setMarginFactors(0.1, 0.25, 0.1, 0.25);
				iconChar.setFilled(false);
				iconChar.setOpaque(false);

				MLT.getTabbedPane().addTab("INST-AV", iconChar, "Instruments", "Available instruments",
				new TablePane(table));

				MLT.getStatusBar().removeLabel("INST-AV");
			} catch (PersistorException exc) {
				Logs.catching(exc);
			}
		}
	}

}
