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
package com.mlt.desktop.control;

import com.mlt.desktop.layout.Anchor;
import com.mlt.desktop.layout.Constraints;
import com.mlt.desktop.layout.Fill;
import com.mlt.desktop.layout.Insets;

/**
 * A table pane contains a table and a status bar. By default, the status bar shows in label "LB-TABLE" the selected row
 * and the total number of rows.
 *
 * @author Miquel Sas
 */
public class TablePane extends GridBagPane {

	/** The table label in the status bar. */
	public static final String TABLE_LABEL = "LB-TABLE";

	/**
	 * Selection listener.
	 */
	private static class Listener implements Table.Listener {

		/** Table pane. */
		private TablePane tablePane;

		/**
		 * Constructor.
		 * 
		 * @param tablePane The table pane.
		 */
		private Listener(TablePane tablePane) {
			super();
			this.tablePane = tablePane;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void focusCellChanged(Table table) {
			tablePane.getStatusBar().setLabel(TABLE_LABEL, table.getRowInfo());
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void selectionChanged(Table table) {
			tablePane.getStatusBar().setLabel(TABLE_LABEL, table.getRowInfo());
		}

	}

	/** The table. */
	private Table table;
	/** The status bar. */
	private StatusBar statusBar;

	/**
	 * Constructor.
	 * 
	 * @param table The main table.
	 */
	public TablePane(Table table) {
		super();

		/* Register table and status bar. */
		this.table = table;
		this.statusBar = new StatusBar();

		/* Add the listener to the table. */
		Listener listener = new Listener(this);
		this.table.addListener(listener);
		listener.selectionChanged(table);

		/* Add table and status bar. */
		Insets insets = new Insets(0, 0, 0, 0);
		add(this.table, new Constraints(Anchor.TOP, Fill.BOTH, 0, 0, insets));
		add(this.statusBar, new Constraints(Anchor.BOTTOM, Fill.HORIZONTAL, 0, 1, insets));

	}

	/**
	 * Return the table.
	 * 
	 * @return The table.
	 */
	public Table getTable() {
		return table;
	}

	/**
	 * Return the status bar.
	 * 
	 * @return The status bar.
	 */
	public StatusBar getStatusBar() {
		return statusBar;
	}

}
