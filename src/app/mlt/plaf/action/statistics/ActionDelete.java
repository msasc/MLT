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

import java.util.List;

import com.mlt.db.Persistor;
import com.mlt.db.Table;
import com.mlt.desktop.Alert;
import com.mlt.desktop.Option;
import com.mlt.util.Logs;
import com.mlt.util.Properties;

import app.mlt.plaf.DB;
import app.mlt.plaf.action.ActionStatistics;
import app.mlt.plaf.statistics.Statistics;

/**
 * Delete the seleted statistics.
 *
 * @author Miquel Sas
 */
public class ActionDelete extends ActionStatistics {

	/**
	 * Constructor.
	 * 
	 * @param rootProperties Properties of the root action.
	 */
	public ActionDelete(Properties rootProperties) {
		super();
		getProperties().putAll(rootProperties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		try {
			/* Statistics averages. */
			Statistics stats = getStatistics();
			if (stats == null) {
				return;
			}

			/* Ask. */
			Option option = Alert.confirm("Delete current statistics");
			if (Option.isCancel(option)) {
				return;
			}

			/* Statistics persistor. */
			Persistor persistor = DB.persistor_statistics();

			/* Drop the tables. */
			List<Table> tables = stats.getAllTables();
			for (Table table : tables) {
				if (DB.ddl().existsTable(table)) {
					DB.ddl().dropTable(table);
				}
			}

			/* Delete the record. */
			persistor.delete(getRootRecord());

			/* Remove from table. */
			int row = getRootTable().getSelectedRow();
			getRootTable().getModel().getRecordSet().remove(row);

		} catch (Exception exc) {
			Logs.catching(exc);
		}
	}

}
