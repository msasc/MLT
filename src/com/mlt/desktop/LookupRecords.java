/*
 * Copyright (C) 2017 Miquel Sas
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.mlt.desktop;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import com.mlt.db.Record;
import com.mlt.db.RecordSet;
import com.mlt.desktop.control.Dialog;
import com.mlt.desktop.control.GridBagPane;
import com.mlt.desktop.control.Stage;
import com.mlt.desktop.control.TablePane;
import com.mlt.desktop.control.TableRecord;
import com.mlt.desktop.control.table.SelectionMode;
import com.mlt.desktop.control.table.TableRecordModel;
import com.mlt.desktop.event.MouseHandler;
import com.mlt.desktop.layout.Insets;
import com.mlt.util.Lists;

/**
 * Lookup records from a recordset or a list of records in a modal dialog.
 *
 * @author Miquel Sas
 */
public class LookupRecords {

	/**
	 * Action select.
	 */
	class ActionSelect extends AbstractAction {
		/** The table record. */
		private TableRecord table;
		/** The list of selected records filled by the action. */
		private List<Record> selectedRecords;

		/**
		 * Constructor.
		 * 
		 * @param table The table record.
		 */
		ActionSelect(TableRecord table, List<Record> selectedRecords) {
			super();
			this.table = table;
			this.selectedRecords = selectedRecords;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			selectedRecords.addAll(table.getSelectedRecords());
		}
	}
	
	/**
	 * Double click mouse listener.
	 */
	class MouseListener extends MouseHandler {
		
		Option option;

		MouseListener(Option option) {
			super();
			this.option = option;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				option.doClick();
			}
		}
		
	}

	/** Window owner. */
	private Stage owner;
	/** Table record model. */
	private TableRecordModel model;
	/** Window title. */
	private String title;
	/** List of selected rows. */
	private List<Integer> selectedRows = new ArrayList<>();

	/** Height factor. */
	private double heightFactor = 0.8;
	/** Width factor. */
	private double widthFactor = 0.8;
	/** Pack indicator. */
	private boolean pack = false;

	/**
	 * Constructor.
	 * 
	 * @param masterRecord The master record.
	 */
	public LookupRecords(Record masterRecord) {
		this(null, masterRecord);
	}

	/**
	 * Constructor.
	 * 
	 * @param owner        The owner.
	 * @param masterRecord The master record.
	 */
	public LookupRecords(Stage owner, Record masterRecord) {
		super();
		this.owner = owner;
		this.model = new TableRecordModel(masterRecord);
	}

	/**
	 * Add a column.
	 * 
	 * @param index The field index.
	 * @see com.mlt.lib.desktop.control.table.TableRecordModel#addColumn(int)
	 */
	public void addColumn(int index) {
		model.addColumn(index);
	}

	/**
	 * Add a column.
	 * 
	 * @param alias The field alias.
	 * @see com.mlt.lib.desktop.control.table.TableRecordModel#addColumn(java.lang.String)
	 */
	public void addColumn(String alias) {
		model.addColumn(alias);
	}

	/**
	 * Returns a list of row ranges from the list of selected rows.
	 * 
	 * @return The list of row ranges.
	 */
	private List<int[]> getSelectedRowRanges() {
		List<int[]> ranges = new ArrayList<>();
		int[] rows = Lists.toIntegerArray(selectedRows);
		Arrays.sort(rows);
		for (int i = 0; i < rows.length; i++) {
			if (i > 0) {
				if (rows[i] == rows[i - 1] + 1) {
					int[] range = Lists.getLast(ranges);
					range[1] = rows[i];
					continue;
				}
			}
			int[] range = new int[2];
			range[0] = rows[i];
			range[1] = rows[i];
			ranges.add(range);
		}
		return ranges;
	}

	/**
	 * Lookup for a single record.
	 * 
	 * @return The selected record or null;
	 */
	public Record lookupRecord() {
		List<Record> records = lookupRecords(SelectionMode.SINGLE_ROW_SELECTION);
		if (!records.isEmpty()) return records.get(0);
		return null;
	}

	/**
	 * Lookup records and return the list of selected records.
	 * 
	 * @param multipleRowInterval A boolean indicating whether multiple row interval
	 *                            selection should apply.
	 * @return The list of selected records.
	 */
	public List<Record> lookupRecords() {
		return lookupRecords(true);
	}

	/**
	 * Lookup records and return the list of selected records.
	 * 
	 * @param multipleRowInterval A boolean indicating whether multiple row interval
	 *                            selection should apply.
	 * @return The list of selected records.
	 */
	public List<Record> lookupRecords(boolean multipleRowInterval) {
		return lookupRecords(
			multipleRowInterval ? SelectionMode.MULTIPLE_ROW_INTERVAL
				: SelectionMode.SINGLE_ROW_INTERVAL);
	}

	/**
	 * Lookup records and return the list of selected records.
	 * 
	 * @param selectionMode The selection mode.
	 * @return The list of selected records.
	 */
	private List<Record> lookupRecords(SelectionMode selectionMode) {
		/* Initialize the root pane. */
		OptionWindow wnd = new OptionWindow(new Dialog(owner, new GridBagPane()));
		wnd.setTitle(title);

		/* The result list of selected records. */
		List<Record> selectedRecords = new ArrayList<>();

		/* Setup the table. */
		TableRecord table = new TableRecord();
		table.setSelectionMode(selectionMode);
		table.setModel(model);

		/* Setup the options. */
		Option select = Option.option_SELECT();
		ActionSelect actionSelect = new ActionSelect(table, selectedRecords);
		select.setAction(actionSelect);
		select.setCloseWindow(true);
		select.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
		table.addMouseListener(new MouseListener(select));
		Option cancel = Option.option_CANCEL();
		List<Option> options = new ArrayList<>();
		options.add(select);
		options.add(cancel);

		/* Setup the window, add the options. */
		wnd.setCenter(new TablePane(table), new Insets(5, 5, 5, 5));
		wnd.setOptionsBottom();
		wnd.getOptionPane().add(options);
		wnd.getOptionPane().setMnemonics();

		/* Initially selected rows. */
		if (selectedRows.isEmpty()) {
			selectedRows.add(0);
		}
		List<int[]> ranges = getSelectedRowRanges();
		for (int i = 0; i < ranges.size(); i++) {
			int topRow = ranges.get(i)[0];
			int bottomRow = ranges.get(i)[1];
			table.setSelectedRowRange(topRow, bottomRow, false);
		}

		/* Show the window. */
		if (pack) {
			wnd.pack();
		} else {
			wnd.setSize(widthFactor, heightFactor);
		}
		wnd.centerOnScreen();
		wnd.show();

		return selectedRecords;
	}

	/**
	 * Set the pack indicator.
	 * 
	 * @param pack A boolean.
	 */
	public void setPack(boolean pack) {
		this.pack = pack;
	}

	/**
	 * Set the recordset to select records.
	 * 
	 * @param recordSet The recordset.
	 */
	public void setRecordSet(RecordSet recordSet) {
		model.setRecordSet(recordSet);
	}

	/**
	 * Set the list of selected rows.
	 * 
	 * @param row The row to select.
	 */
	public void setSelectedRow(int row) {
		setSelectedRows(row);
	}

	/**
	 * Set the list of selected rows.
	 * 
	 * @param rows The list of rows.
	 */
	public void setSelectedRows(int... rows) {
		if (rows != null) {
			selectedRows.clear();
			for (int row : rows) {
				selectedRows.add(row);
			}
		}
	}

	/**
	 * Set the size factors.
	 * 
	 * @param widthFactor  Width factor.
	 * @param heightFactor Height factor.
	 */
	public void setSize(double widthFactor, double heightFactor) {
		this.widthFactor = widthFactor;
		this.heightFactor = heightFactor;
	}

	/**
	 * Set the title.
	 * 
	 * @param title The title.
	 */
	public void setTitle(String title) {
		this.title = title;
	}
}
