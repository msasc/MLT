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

package com.mlt.desktop;

import java.util.ArrayList;
import java.util.List;

import com.mlt.db.Field;
import com.mlt.db.FieldGroup;
import com.mlt.db.FieldProperties;
import com.mlt.db.Record;
import com.mlt.db.RecordList;
import com.mlt.db.RecordSet;
import com.mlt.desktop.control.Button;
import com.mlt.desktop.control.Dialog;
import com.mlt.desktop.control.GridBagPane;
import com.mlt.desktop.control.Stage;
import com.mlt.desktop.control.TableRecord;
import com.mlt.desktop.control.TableRecordModel;
import com.mlt.desktop.control.table.SelectionMode;
import com.mlt.desktop.icon.IconArrow;
import com.mlt.desktop.layout.Anchor;
import com.mlt.desktop.layout.Constraints;
import com.mlt.desktop.layout.Direction;
import com.mlt.desktop.layout.Fill;
import com.mlt.desktop.layout.Insets;
import com.mlt.util.Resources;

/**
 * Column chooser for a table record.
 *
 * @author Miquel Sas
 */
public class ColumnChooser {

	/** Owner stage. */
	private Stage owner;
	/** Table record to configure columns view. */
	private TableRecord table;
	/** Field properties maneger. */
	private FieldProperties properties;
	/** List of possible fields. */
	private List<Record> possibleFields;
	/**
	 * List of available field aliases. By default, all the fields of the master
	 * record can be selected.
	 */
	private List<String> availableFields = new ArrayList<>();
	/** List of not selected fields. */
	private List<String> notSelectedFields = new ArrayList<>();
	/** List of selected fields. */
	private List<String> selectedFields = new ArrayList<>();

	/** Left table. */
	private TableRecord leftTable;
	/** Right table. */
	private TableRecord rightTable;

	/**
	 * Constructor.
	 * 
	 * @param table The table to configure the columns.
	 */
	public ColumnChooser(TableRecord table) {
		this(null, table);
	}

	/**
	 * Constructor.
	 * 
	 * @param owner Window owner.
	 * @param table The table to configure the columns.
	 */
	public ColumnChooser(Stage owner, TableRecord table) {
		super();
		this.owner = owner;
		this.table = table;
		this.properties = new FieldProperties();

		/* Build the list of possible fields. */
		possibleFields = new ArrayList<>();
		for (int i = 0; i < getMasterRecord().size(); i++) {
			Field field = getMasterRecord().getField(i);
			possibleFields.add(properties.getProperties(field, i, true));
		}
		possibleFields.sort(new FieldProperties.Sorter());
	}

	/**
	 * Optionally define the available fields.
	 * 
	 * @param alias The field alias.
	 */
	public void addAvailableField(String alias) {
		if (!getMasterRecord().getFieldList().containsField(alias)) {
			throw new IllegalArgumentException("Invalid field alias: " + alias);
		}
		availableFields.add(alias);
	}

	/**
	 * Define initially selected fields.
	 * 
	 * @param alias The field alias.
	 */
	public void addSelectedField(String alias) {
		if (!getMasterRecord().getFieldList().containsField(alias)) {
			throw new IllegalArgumentException("Invalid field alias: " + alias);
		}
		selectedFields.add(alias);
	}

	/**
	 * Create the direction button.
	 * 
	 * @param direction The direction.
	 * @return The button.
	 */
	private Button createButton(Direction direction) {

		IconArrow arrow = new IconArrow();
		arrow.setDirection(direction);
		arrow.setSize(24, 24);
		arrow.setClosed(true);

		switch (direction) {
		case LEFT:
		case RIGHT:
			arrow.setMarginFactors(0.20, 0.25, 0.20, 0.25);
			break;
		case UP:
		case DOWN:
			arrow.setMarginFactors(0.25, 0.20, 0.25, 0.20);
			break;
		default:
			break;
		}

		Button button = new Button();
		button.setAction(null);
		button.setIconTextGap(0);
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setIcon(arrow);
		button.setText(null);
		switch (direction) {
		case LEFT:
			button.setName("LEFT");
			button.setToolTipText(Resources.getText("colPaneButtonLeftTooltip"));
			button.setAction(l -> {
				moveLeft();
			});
			break;
		case RIGHT:
			button.setName("RIGHT");
			button.setToolTipText(Resources.getText("colPaneButtonRightTooltip"));
			button.setAction(l -> {
				moveRight();
			});
			break;
		case UP:
			button.setName("UP");
			button.setToolTipText(Resources.getText("colPaneButtonUpTooltip"));
			button.setAction(l -> {
				moveUp();
			});
			break;
		case DOWN:
			button.setName("DOWN");
			button.setToolTipText(Resources.getText("colPaneButtonDownTooltip"));
			button.setAction(l -> {
				moveDown();
			});
			break;
		default:
			break;
		}

		return button;
	}

	/**
	 * Create the table for fields.
	 * 
	 * @param recordSet The fields recordset.
	 * @return The table configurated.
	 */
	private TableRecord createTable(RecordSet recordSet) {
		TableRecordModel model = new TableRecordModel(properties.getProperties());
		/* Check field groups other than default. */
		boolean group = false;
		for (int i = 0; i < getMasterRecord().size(); i++) {
			if (!getMasterRecord().getField(i).getFieldGroup().equals(FieldGroup.EMPTY_FIELD_GROUP)) {
				group = true;
				break;
			}
		}
		if (group) {
			model.addColumn(FieldProperties.GROUP);
		}
		model.addColumn(FieldProperties.INDEX);
		model.addColumn(FieldProperties.ALIAS);
		model.addColumn(FieldProperties.HEADER);
		model.addColumn(FieldProperties.TITLE);
		model.addColumn(FieldProperties.TYPE);
		model.addColumn(FieldProperties.LENGTH);
		model.addColumn(FieldProperties.DECIMALS);
		model.setRecordSet(recordSet);
		TableRecord table = new TableRecord();
		table.setSelectionMode(SelectionMode.MULTIPLE_ROW_INTERVAL);
		table.setModel(model);
		table.setSelectedRow(0);
		return table;
	}

	/**
	 * @return The master record.
	 */
	private Record getMasterRecord() {
		return table.getModel().getMasterRecord();
	}

	/**
	 * @param alias Field alias.
	 * @return Field properties.
	 */
	private Record getProperties(String alias) {
		for (Record properties : possibleFields) {
			if (properties.getValue(FieldProperties.ALIAS).equals(alias)) {
				return properties;
			}
		}
		return null;
	}

	/**
	 * @return The recordset of not selected fields.
	 */
	private RecordSet getRecordSetNotSelected() {
		RecordList rs = new RecordList(properties.getFieldList());
		for (String alias : notSelectedFields) {
			rs.add(getProperties(alias));
		}
		return rs;
	}

	/**
	 * @return The recordset of selected fields.
	 */
	private RecordSet getRecordSetSelected() {
		RecordList rs = new RecordList(properties.getFieldList());
		for (String alias : selectedFields) {
			rs.add(getProperties(alias));
		}
		return rs;
	}

	/**
	 * Move select right fields down.
	 */
	private void moveDown() {
		int[] rows = rightTable.getSelectedRows();
		if (rows == null || rows.length == 0) {
			return;
		}
		for (int i = rows.length - 1; i >= 0; i--) {
			int row = rows[i];
			if (row < selectedFields.size() - 1) {
				String src = selectedFields.get(row );
				String dst = selectedFields.get(row + 1);
				selectedFields.set(row + 1, src);
				selectedFields.set(row,  dst);
				rows[i]++;
			}
		}
		rightTable.getModel().setRecordSet(getRecordSetSelected());
		rightTable.adjustColumnWidths();
		rightTable.clearSelection();
		rightTable.setSelectedRows(rows);
	}

	/**
	 * Move selected fields left.
	 */
	private void moveLeft() {
		int row = rightTable.getSelectedRow();
		if (row < 0) {
			return;
		}
		List<Record> selected = rightTable.getSelectedRecords();
		for (Record rc : selected) {
			String alias = rc.getValue(FieldProperties.ALIAS).getString();
			selectedFields.remove(alias);
			notSelectedFields.add(alias);
		}
		sortFieldAliases();
		leftTable.getModel().setRecordSet(getRecordSetNotSelected());
		leftTable.adjustColumnWidths();
		rightTable.getModel().setRecordSet(getRecordSetSelected());
		rightTable.adjustColumnWidths();
		if (row >= rightTable.getRowCount()) {
			row = rightTable.getRowCount() - 1;
		}
		rightTable.setSelectedRow(row);
		leftTable.clearSelection();
	}

	/**
	 * Move selected fields right.
	 */
	private void moveRight() {
		int row = leftTable.getSelectedRow();
		if (row < 0) {
			return;
		}
		List<Record> selected = leftTable.getSelectedRecords();
		for (Record rc : selected) {
			String alias = rc.getValue(FieldProperties.ALIAS).getString();
			notSelectedFields.remove(alias);
			selectedFields.add(alias);
		}
		sortFieldAliases();
		leftTable.getModel().setRecordSet(getRecordSetNotSelected());
		leftTable.adjustColumnWidths();
		rightTable.getModel().setRecordSet(getRecordSetSelected());
		rightTable.adjustColumnWidths();
		if (row >= leftTable.getRowCount()) {
			row = leftTable.getRowCount() - 1;
		}
		leftTable.setSelectedRow(row);
		rightTable.clearSelection();
	}
	
	/**
	 * Move select right fields up.
	 */
	private void moveUp() {
		int[] rows = rightTable.getSelectedRows();
		if (rows == null || rows.length == 0) {
			return;
		}
		for (int i = 0; i < rows.length; i++) {
			int row = rows[i];
			if (row > 0) {
				String src = selectedFields.get(row );
				String dst = selectedFields.get(row - 1);
				selectedFields.set(row - 1, src);
				selectedFields.set(row,  dst);
				rows[i]--;
			}
		}
		rightTable.getModel().setRecordSet(getRecordSetSelected());
		rightTable.adjustColumnWidths();
		rightTable.clearSelection();
		rightTable.setSelectedRows(rows);
	}

	/**
	 * Show the column chooser and choose/select columns or cancel it.
	 */
	public void show() {
		
		/* Table model. */
		TableRecordModel model = table.getModel();

		/* If no available fields where defined, add all. */
		if (availableFields.isEmpty()) {
			for (int i = 0; i < getMasterRecord().size(); i++) {
				availableFields.add(getMasterRecord().getField(i).getAlias());
			}
		}
		
		/* Selected fields from currently visible. */
		List<String> modelFields = new ArrayList<>();
		for (int i = 0; i < model.getColumnCount(); i++) {
			int index = model.getFieldIndex(i);
			String alias = getMasterRecord().getField(index).getAlias();
			modelFields.add(alias);
		}
		selectedFields.clear();
		selectedFields.addAll(modelFields);
		for (int modelIndex = 0; modelIndex < model.getColumnCount(); modelIndex++) {
			int viewIndex = table.convertColumnIndexToView(modelIndex);
			selectedFields.set(viewIndex, modelFields.get(modelIndex));
		}

		/* At least, the selected fields must be available. */
		for (String alias : selectedFields) {
			if (!availableFields.contains(alias)) {
				availableFields.add(alias);
			}
		}

		/* The list of not selected fields. */
		for (String alias : availableFields) {
			if (!selectedFields.contains(alias)) {
				notSelectedFields.add(alias);
			}
		}

		/* Sort the lists of field aliases. */
		sortFieldAliases();

		/* Setup the pane with tables of fields and transfer buttons. */
		GridBagPane paneFields = new GridBagPane();

		/* Left table. */
		leftTable = createTable(getRecordSetNotSelected());
		paneFields.add(leftTable, new Constraints(
			Anchor.TOP_LEFT, Fill.BOTH, 0, 0, new Insets(0, 0, 0, 0)));

		/* Center button pane. */
		GridBagPane buttons = new GridBagPane();
		buttons.add(createButton(Direction.LEFT), new Constraints(
			Anchor.CENTER, Fill.NONE, 0, 0, new Insets(0, 10, 5, 10)));
		buttons.add(createButton(Direction.RIGHT), new Constraints(
			Anchor.CENTER, Fill.NONE, 0, 1, new Insets(0, 10, 5, 10)));
		buttons.add(createButton(Direction.UP), new Constraints(
			Anchor.CENTER, Fill.NONE, 0, 2, new Insets(0, 10, 5, 10)));
		buttons.add(createButton(Direction.DOWN), new Constraints(
			Anchor.CENTER, Fill.NONE, 0, 3, new Insets(0, 10, 0, 10)));
		paneFields.add(buttons, new Constraints(
			Anchor.CENTER, Fill.VERTICAL, 1, 0, new Insets(0, 0, 0, 0)));

		/* Right table. */
		rightTable = createTable(getRecordSetSelected());
		paneFields.add(rightTable, new Constraints(
			Anchor.TOP_RIGHT, Fill.BOTH, 2, 0, new Insets(0, 0, 0, 0)));

		/* Create and show the window. */
		OptionWindow wnd = new OptionWindow(new Dialog(owner, new GridBagPane()));
		wnd.setTitle(Resources.getText("colPaneTitleSelection"));
		wnd.setOptionsBottom();
		wnd.setCenter(paneFields);
		
		Option accept = Option.option_ACCEPT();
		accept.setCloseWindow(true);
		Option cancel = Option.option_CANCEL();
		cancel.setCloseWindow(true);
		
		wnd.getOptionPane().add(accept, cancel);
		wnd.setSize(0.8, 0.8);
		wnd.centerOnScreen();
		wnd.show();
		
		Option option = wnd.getOptionExecuted();
		if (option.equals(Option.KEY_CANCEL)) {
			return;
		}

		model.removeAllColumns();
		for (String alias : selectedFields) {
			model.addColumn(alias);
		}
		table.setModel(model);
	};

	/**
	 * Sort the lists of fields.
	 */
	private void sortFieldAliases() {
		sortFieldAliases(availableFields);
		sortFieldAliases(notSelectedFields);
	}

	/**
	 * Sort the lists of fields.
	 * 
	 * @param aliases The list of field aliases to sort.
	 */
	private void sortFieldAliases(List<String> aliases) {
		List<String> sorted = new ArrayList<>();
		for (Record rc : possibleFields) {
			String alias = rc.getValue(FieldProperties.ALIAS).getString();
			if (aliases.contains(alias)) {
				sorted.add(alias);
			}
		}
		aliases.clear();
		aliases.addAll(sorted);
	}
}
