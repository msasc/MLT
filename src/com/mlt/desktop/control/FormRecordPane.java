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

package com.mlt.desktop.control;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mlt.db.Field;
import com.mlt.db.FieldGroup;
import com.mlt.db.FieldList;
import com.mlt.db.Record;
import com.mlt.db.Value;
import com.mlt.desktop.EditContext;
import com.mlt.desktop.EditField;
import com.mlt.desktop.EditMode;
import com.mlt.desktop.border.LineBorderSides;
import com.mlt.desktop.graphic.Stroke;
import com.mlt.desktop.graphic.Text;
import com.mlt.desktop.layout.Anchor;
import com.mlt.desktop.layout.Constraints;
import com.mlt.desktop.layout.Dimension;
import com.mlt.desktop.layout.Fill;
import com.mlt.desktop.layout.Insets;
import com.mlt.util.Numbers;

/**
 * A pane that hold a form to layout fields of a record. Fields are distributed
 * in tabs by field group, if there are diferrent field groups, or in a single
 * form if there is only one field group.
 *
 * @author Miquel Sas
 */
public class FormRecordPane {

	/**
	 * Grid of fields. It is a box that contains a list of fields laid out
	 * vertically. Each row contains a label and an input control.
	 */
	private static class Grid {
		int row;
		int column;
		List<EditContext> contexts = new ArrayList<>();

		Grid(int row, int column) {
			this.row = row;
			this.column = column;
		}
	}

	/**
	 * Group of grids of fields.
	 */
	private static class Group implements Comparable<Group> {
		FieldGroup fieldGroup;
		List<Grid> grids = new ArrayList<>();

		Group(FieldGroup fieldGroup) {
			this.fieldGroup = fieldGroup;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int compareTo(Group group) {
			return fieldGroup.compareTo(group.fieldGroup);
		}
	}

	/**
	 * Layouts.
	 */
	private static enum Layout {
		ROWS, COLUMNS
	}

	/**
	 * Row of a grid.
	 */
	private static class Row {
		EditContext context;
		Label label;
		Control editor;
	}

	/** Edit mode. */
	private EditMode editMode = EditMode.NO_RESTRICTION;
	/** List of groups of grids. */
	private List<Group> groups = new ArrayList<>();
	/** Main pane. */
	private GridBagPane pane = new GridBagPane();
	/** The record. */
	private Record record;
	/** Map of group layouts. */
	private Map<Group, Layout> layouts = new HashMap<>();

	/** Border color. */
	private Color borderColor = Color.LIGHT_GRAY;
	/** Border stroke. */
	private Stroke borderStroke = new Stroke();
	/** Grid insets surrounding labels and fields. */
	private Insets gridInsets = new Insets(10, 10, 10, 10);
	/** Group insets surrounding the group`. */
	private Insets groupInsets = new Insets(10, 10, 10, 10);
	/** Label to editor gap. */
	private int labelGap = 5;
	/** Field to field gap. */
	private int fieldGap = 2;
	/** Preferred row height. */
	private double preferredRowHeight = -1;

	/**
	 * Constructor.
	 * 
	 * @param fieldList The list of fields.
	 */
	public FormRecordPane(FieldList fieldList) {
		super();
		/* The field list must be set. */
		if (fieldList == null) throw new NullPointerException();
		/* Make a full copy of the source record to be able to discard the edition. */
		this.record = Record.copyDataAndFields(new Record(fieldList));
	}

	/**
	 * Constructor.
	 * 
	 * @param record The record to edit.
	 */
	public FormRecordPane(Record record) {
		super();
		/* The record must be set. */
		if (record == null) throw new NullPointerException();
		/* Make a full copy of the source record to be able to discard the edition. */
		this.record = Record.copyDataAndFields(record);
	}

	/**
	 * Add a field to the default (0, 0) sub-panel.
	 * 
	 * @param alias The field alias.
	 */
	public void addField(String alias) {
		addField(alias, 0, 0);
	}

	/**
	 * Add a field indicating the coordinates of the where it should lay. In the
	 * corresponding grid, fields are added sequentially.
	 * 
	 * @param alias  The field alias.
	 * @param row    The row index.
	 * @param column The column index.
	 */
	public void addField(String alias, int row, int column) {

		/* The field must exist. */
		Field field = record.getField(alias);
		if (field == null) {
			throw new IllegalArgumentException("Invalid field alias.");
		}

		/* Row and column can not be negative. */
		if (row < 0) {
			throw new IllegalArgumentException("Invalid row index.");
		}
		if (column < 0) {
			throw new IllegalArgumentException("Invalid column index.");
		}

		/* Get create the group. */
		Group group = getGroup(field.getFieldGroup());

		/* If the group is empty the grid must be (0,0) */
		if (group.grids.isEmpty()) {
			if (row != 0 || column != 0) {
				throw new IllegalArgumentException("First grid in an empty group must be (0,0)");
			}
			/* Ensure that the top-left grid exists. */
			getGrid(group, 0, 0);
		}

		/*
		 * At least a grid on the top or on the left must exist if row and column are
		 * not (0,0).
		 */
		if (row != 0 || column != 0) {
			if (!isGrid(group, row - 1, column) && !isGrid(group, row, column - 1)) {
				throw new IllegalArgumentException(
					"At least a grid on the top or on the left must exist.");
			}
		}

		/*
		 * If the grid not exists and it is either (0,1) or (1,0), and the group layout
		 * has not been set, then it is the time to determine and set the group layout.
		 * If the second grid added is (0,1), then the layout will be by rows, while if
		 * the second grid added is (1,0) then the layout will be by columns.
		 */
		if (!isGrid(group, row, column) && !layouts.containsKey(group)) {
			if (row == 0 && column == 1) {
				layouts.put(group, Layout.ROWS);
			}
			if (row == 1 && column == 0) {
				layouts.put(group, Layout.COLUMNS);
			}
		}

		/*
		 * Now it is ensured that the grid (0,0) exists, if grids (0,1) and (1,0) both
		 * not exist, then then the current required grid must be one of them, and the
		 * group layout is already set. We can now ensure that the required grid, if not
		 * exists, meets the layout requirements.
		 */
		if (!isGrid(group, row, column)) {
			if (!layouts.containsKey(group)) {
				throw new IllegalArgumentException("The group has not a layout already set.");
			}
			if (layouts.get(group) == Layout.COLUMNS) {
				if (row == 0 && !isGrid(group, row, column - 1)) {
					throw new IllegalArgumentException("A grid on the left must exists");
				}
				if (row != 0 && !isGrid(group, row - 1, column)) {
					throw new IllegalArgumentException("A grid on the top must exists");
				}
			}
			if (layouts.get(group) == Layout.ROWS) {
				if (column == 0 && !isGrid(group, row - 1, column)) {
					throw new IllegalArgumentException("A grid on the top must exists");
				}
				if (column != 0 && !isGrid(group, row, column - 1)) {
					throw new IllegalArgumentException("A grid on the left must exists");
				}
			}
		}

		/* Everything ok to get/create the grid and add the field. */
		Grid grid = getGrid(group, row, column);
		grid.contexts.add(new EditContext(field));

		/* Sort the groups to always ensure the proper order. */
		groups.sort(null);
	}

	/**
	 * Return a list with all edit contexts.
	 * 
	 * @return The list of edit contexts.
	 */
	private List<EditContext> getAllEditContexts() {
		List<EditContext> contexts = new ArrayList<>();
		for (Group group : groups) {
			for (Grid grid : group.grids) {
				for (EditContext context : grid.contexts) {
					if (!contexts.contains(context)) {
						contexts.add(context);
					}
				}
			}
		}
		return contexts;
	}

	/**
	 * Return the edit with the field with the given alias or null if none exists.
	 * 
	 * @param alias The alias.
	 * @return The edit context with the field or null.
	 */
	public EditContext getEditContext(String alias) {
		for (Group group : groups) {
			for (Grid grid : group.grids) {
				for (EditContext context : grid.contexts) {
					if (context.getField().getAlias().equals(alias)) {
						return context;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns the existing grid of fields in the group or creates a new one.
	 * 
	 * @param group  The group of grids.
	 * @param row    Row index.
	 * @param column Column index..
	 * @return The grid of fields.
	 */
	private Grid getGrid(Group group, int row, int column) {
		for (Grid grid : group.grids) {
			if (grid.row == row && grid.column == column) {
				return grid;
			}
		}
		Grid grid = new Grid(row, column);
		group.grids.add(grid);
		return grid;
	}

	/**
	 * Return the pane of a grid of fields, with the fields laid out and sized.
	 * 
	 * @param grid The grid of fields.
	 * @return The layout pane.
	 */
	private GridBagPane getGridPane(Grid grid) {

		/* Build the list of rows. */
		List<Row> rows = new ArrayList<>();
		for (int i = 0; i < grid.contexts.size(); i++) {
			EditContext context = grid.contexts.get(i);
			Label label = context.getLabel();
			Control editor = context.getEditor();
			Row row = new Row();
			row.context = context;
			row.label = label;
			row.editor = editor;
			rows.add(row);
		}

		/* The prefrerred row height. */
		double rowHeight = getPreferredRowHeight();

		/* Add rows to the pane with insets to get the same height for all rows. */
		GridBagPane pane = new GridBagPane();
		for (int i = 0; i < rows.size(); i++) {
			EditContext context = rows.get(i).context;
			Label label = rows.get(i).label;
			Control editor = rows.get(i).editor;

			/* Insets and constraints for the label. */
			double labelHeight = label.getPreferredSize().getHeight();
			double labelTop = Numbers.round((rowHeight - labelHeight) / 2, 0);
			double labelLeft = 0;
			double labelBottom = 0;
			double labelRight = labelGap;
			if (i > 0) {
				labelTop += fieldGap;
			}
			Insets labelInsets = new Insets(labelTop, labelLeft, labelBottom, labelRight);
			Constraints labelConstraints = new Constraints(
				Anchor.TOP_LEFT,
				Fill.NONE,
				0, i, 1, 1, 0, 0, labelInsets);

			/* Insets and constraints for the editor. */
			double weightx = 1;
			Fill fill = context.getFill();
			double weighty = (fill == Fill.VERTICAL || fill == Fill.BOTH ? 1 : 0);
			double editorHeight = editor.getPreferredSize().getHeight();
			double editorTop = 0;
			if (editorHeight < rowHeight) {
				editorTop = Numbers.round((rowHeight - editorHeight) / 2, 0);
			}
			double editorLeft = 0;
			double editorBottom = 0;
			double editorRight = 0;
			if (i > 0) {
				editorTop += fieldGap;
			}
			Insets editorInsets = new Insets(editorTop, editorLeft, editorBottom, editorRight);
			Constraints editorConstraints = new Constraints(
				Anchor.TOP_LEFT, fill, 1, i, 1, 1, weightx, weighty, editorInsets);

			/* Do add the row. */
			pane.add(label, labelConstraints);
			pane.add(editor, editorConstraints);
		}

		return pane;
	}

	/**
	 * Returns the group of the argument field group or creates a new one if not
	 * exists.
	 * 
	 * @param fieldGroup The field group.
	 * @return The group or null.
	 */
	private Group getGroup(FieldGroup fieldGroup) {
		for (Group group : groups) {
			if (group.fieldGroup.equals(fieldGroup)) {
				return group;
			}
		}
		Group group = new Group(fieldGroup);
		groups.add(group);
		return group;
	}

	/**
	 * Layout and return the pane for the group.
	 * 
	 * @param group The source group.
	 * @return The pane.
	 */
	private GridBagPane getGroupPane(Group group) {
		GridBagPane groupPane = new GridBagPane();
		Constraints constraints =
			new Constraints(Anchor.TOP_LEFT, Fill.BOTH, 0, 0, groupInsets);
		if (layouts.get(group) == Layout.COLUMNS) {
			groupPane.add(getGroupPaneByColumns(group), constraints);
		} else {
			groupPane.add(getGroupPaneByRows(group), constraints);
		}
		return groupPane;
	}

	/**
	 * Layout and an return the pane for group with a layout by columns.
	 * 
	 * @param group The group.
	 * @return The pane.
	 */
	private GridBagPane getGroupPaneByColumns(Group group) {

		GridBagPane groupPane = new GridBagPane();

		/* Count columns in row 0. */
		int columns = 0;
		for (Grid grid : group.grids) {
			if (grid.row == 0) {
				columns++;
			}
		}

		/* Iterate each column. */
		for (int column = 0; column < columns; column++) {

			/* Buil the list of grids of the column. */
			List<Grid> gridRows = new ArrayList<>();
			for (Grid grid : group.grids) {
				if (grid.column == column) {
					gridRows.add(grid);
				}
			}
			gridRows.sort((g1, g2) -> Integer.compare(g1.row, g2.row));

			/* The column pane. */
			GridBagPane columnPane = new GridBagPane();
			Fill fillColumn = Fill.VERTICAL;

			/* Add the rows. */
			int rows = gridRows.size();
			for (int row = 0; row < rows; row++) {

				Grid grid = gridRows.get(row);
				Fill fillGrid = Fill.NONE;
				for (EditContext context : grid.contexts) {
					fillGrid = Fill.merge(fillGrid, context.getFill());
				}

				Constraints constraintsGrid = new Constraints(
					Anchor.TOP_LEFT,
					fillGrid,
					0, row, 1, 1, 1, 1,
					gridInsets);

				Constraints constraintsLayout = new Constraints(
					Anchor.TOP_LEFT,
					row == rows - 1 ? Fill.BOTH : Fill.HORIZONTAL,
					column, row, 1, 1,
					1,
					row == rows - 1 ? 1 : 0, Insets.EMPTY);

				GridBagPane gridPane = getGridPane(grid);
				GridBagPane layoutPane = new GridBagPane();
				layoutPane.setBorder(new LineBorderSides(
					borderColor,
					borderStroke,
					true,
					true,
					(row == rows - 1 ? true : false),
					column == columns - 1 ? true : false));

				layoutPane.add(gridPane, constraintsGrid);
				columnPane.add(layoutPane, constraintsLayout);
				fillColumn = Fill.merge(fillColumn, fillGrid);
			}

			/* Add the column pane. */
			Insets insetsColumn = new Insets(0, 0, 0, 0);
			Constraints constraints =
				new Constraints(Anchor.TOP_LEFT, fillColumn, column, 0, insetsColumn);
			groupPane.add(columnPane, constraints);
		}

		return groupPane;
	}

	/**
	 * Layout and an return the pane for group with a layout by rows.
	 * 
	 * @param group The group.
	 * @return The pane.
	 */
	private GridBagPane getGroupPaneByRows(Group group) {

		GridBagPane groupPane = new GridBagPane();

		/* Count rows in column 0. */
		int rows = 0;
		for (Grid grid : group.grids) {
			if (grid.column == 0) {
				rows++;
			}
		}

		/* Iterate each row. */
		for (int row = 0; row < rows; row++) {

			/* Buil the list of grids of the row. */
			List<Grid> gridColumns = new ArrayList<>();
			for (Grid grid : group.grids) {
				if (grid.row == row) {
					gridColumns.add(grid);
				}
			}
			gridColumns.sort((g1, g2) -> Integer.compare(g1.column, g2.column));

			/* The row pane. */
			GridBagPane rowPane = new GridBagPane();
			Fill fillRow = Fill.HORIZONTAL;

			/* Add the columns, anchor left, and fill none (later review). */
			int columns = gridColumns.size();
			for (int column = 0; column < columns; column++) {

				/* Grid to layout. */
				Grid grid = gridColumns.get(column);
				Fill fillGrid = Fill.NONE;
				for (EditContext context : grid.contexts) {
					fillGrid = Fill.merge(fillGrid, context.getFill());
				}

				/* Get the grid pane. */
				GridBagPane gridPane = getGridPane(grid);
				Constraints constraintsGrid = null;
				{
					double weighty = (fillGrid == Fill.VERTICAL || fillGrid == Fill.BOTH ? 1 : 0);
					constraintsGrid = new Constraints(
						Anchor.TOP_LEFT,
						fillGrid,
						column, 0, 1, 1, 1, weighty, gridInsets);
				}

				/*
				 * Put the grid pane in a new pane to set the proper border depending on the row
				 * and column, and apply global grid insets.
				 */
				GridBagPane layoutPane = new GridBagPane();
				layoutPane.setBorder(new LineBorderSides(
					borderColor,
					borderStroke,
					true,
					true,
					(row == rows - 1 ? true : false),
					column == columns - 1 ? true : false));
				Constraints constraintsLayout = null;
				{
					double weightx = (column == columns - 1 ? 1 : 0);
					double weighty = (fillGrid == Fill.VERTICAL || fillGrid == Fill.BOTH ? 1 : 0);
					constraintsLayout = new Constraints(
						Anchor.TOP_LEFT,
						Fill.BOTH,
						column, row, 1, 1,
						weightx,
						weighty, Insets.EMPTY);
				}

				layoutPane.add(gridPane, constraintsGrid);
				rowPane.add(layoutPane, constraintsLayout);
				fillRow = Fill.merge(fillRow, fillGrid);
			}

			/* Add the row pane. */
			Insets insets = new Insets(0, 0, 0, 0);
			Constraints constraints = new Constraints(Anchor.LEFT, fillRow, 0, row, insets);
			groupPane.add(rowPane, constraints);
		}

		return groupPane;
	}

	/**
	 * Return the font within labels and controls with the maximum height.
	 * 
	 * @return The font within labels and controls with the maximum height.
	 */
	private Font getMaxFont() {
		StringBuilder b = new StringBuilder();
		for (char c = 32; c < 256; c++) {
			b.append(c);
		}
		String str = b.toString();
		Font font = null;
		Dimension size = null;
		for (Group group : groups) {
			for (Grid grid : group.grids) {
				for (EditContext context : grid.contexts) {
					if (font == null) {
						font = context.getLabel().getFont();
						size = Text.getSize(str, font);
					}
					Font labelFont = context.getLabel().getFont();
					Dimension labelSize = Text.getSize(str, labelFont);
					if (labelSize.getHeight() > size.getHeight()) {
						font = labelFont;
						size = labelSize;
					}
					Font editorFont = context.getEditor().getFont();
					Dimension editorSize = Text.getSize(str, editorFont);
					if (editorSize.getHeight() > size.getHeight()) {
						font = editorFont;
						size = editorSize;
					}
				}
			}
		}
		return font;
	}

	/**
	 * Return the preferred row height using a combo box and the greatest font,
	 * because a combo box is a litter higher that the usual text field.
	 * 
	 * @return The preferred row height.
	 */
	private double getPreferredRowHeight() {
		if (preferredRowHeight <= 0) {
			ComboBox editor = new ComboBox();
			editor.setFont(getMaxFont());
			editor.addValue(new Value("Test Value"));
			preferredRowHeight = editor.getPreferredSize().getHeight();
		}

		return preferredRowHeight;
	}

	/**
	 * Return the main pane.
	 * 
	 * @return The pane.
	 */
	public GridBagPane getPane() {
		return pane;
	}

	/**
	 * Return the edited record.
	 * 
	 * @return The record.
	 */
	public Record getRecord() {
		return record;
	}

	/**
	 * Check whether the group has a grid with the argument row and column.
	 * 
	 * @param group  The group.
	 * @param row    The row.
	 * @param column The column.
	 * @return A boolean.
	 */
	private boolean isGrid(Group group, int row, int column) {
		for (Grid grid : group.grids) {
			if (grid.row == row && grid.column == column) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Auto-layout fields, attending at the group and grid definition.
	 * <p>
	 * If we have only one Group (FieldGroup), we will add a single grid pane with
	 * rows. If we have more than one group, a tab pane is used to layout each group
	 * in a different tab.
	 * <p>
	 * A Group is a GridBagPane with a Grid's that are layout either by rows or
	 * columns, depending on how grids are added.
	 * 
	 * A Grid is another GridBagPane with a row for each field, a column for the
	 * labels, a column for the field edit control.
	 */
	public void layout() {

		/* No groups. */
		if (groups.isEmpty()) {
			throw new IllegalStateException("Pane is empty");
		}

		/* Clear the current pane. */
		pane.removeAll();

		/* Only one field group. */
		if (groups.size() == 1) {
			Insets insets = new Insets(0, 0, 0, 0);
			Constraints constraints = new Constraints(Anchor.TOP_LEFT, Fill.BOTH, 0, 0, insets);
			GridBagPane groupPane = getGroupPane(groups.get(0));
			pane.add(groupPane, constraints);
			setEditMode();
			return;
		}

		/* More than one field group. */
		TabPane tabPane = new TabPane();
		for (Group group : groups) {
			String id = group.fieldGroup.getName();
			String title = group.fieldGroup.getDisplayTitle();
			String tooltip = group.fieldGroup.getDisplayDescription();
			GridBagPane groupPane = getGroupPane(group);
			tabPane.addTab(id, title, tooltip, groupPane);
		}
		tabPane.setSelectedIndex(0);
		Insets insets = new Insets(0, 0, 0, 0);
		Constraints constraints = new Constraints(Anchor.TOP, Fill.BOTH, 0, 0, insets);
		pane.add(tabPane, constraints);

		setEditMode();

	}

	/**
	 * Apply the current edit mode.
	 */
	private void setEditMode() {
		List<EditContext> contexts = getAllEditContexts();
		for (EditContext context : contexts) {
			if (editMode == EditMode.DELETE || editMode == EditMode.READ_ONLY) {
				context.getEditField().setEnabled(false);
				continue;
			}
			if (editMode == EditMode.UPDATE && context.getField().isPrimaryKey()) {
				context.getEditField().setEnabled(false);
				continue;
			}
		}
		List<Control> controls = Control.getAllChildControls(pane);
		for (Control control : controls) {
			if (control instanceof EditField && control.isEnabled()) {
				control.setFocusable(true);
				continue;
			}
			if (control instanceof Button && control.isEnabled()) {
				control.setFocusable(true);
				continue;
			}
			control.setFocusable(false);
		}
	}

	/**
	 * Set the edit mode.
	 * 
	 * @param editMode The edit mode.
	 */
	public void setEditMode(EditMode editMode) {
		this.editMode = editMode;
		setEditMode();
	}

	/**
	 * Set the layout of the field group.
	 * 
	 * @param fieldGroup The field group.
	 * @param layout     The layout to set.
	 */
	private void setLayout(FieldGroup fieldGroup, Layout layout) {
		/*
		 * The corresponding group must not exist, to avoid changing the layout when it
		 * already has been set, possibly with grids added.
		 */
		for (Group group : groups) {
			if (group.fieldGroup.equals(fieldGroup)) {
				throw new IllegalStateException("The group must not exist");
			}
		}

		/* Create the group and set the layout. */
		Group group = new Group(fieldGroup);
		groups.add(group);
		layouts.put(group, layout);
	}

	/**
	 * Set the layout of the field group by columns.
	 * 
	 * @param fieldGroup The field group.
	 */
	public void setLayoutByColumns(FieldGroup fieldGroup) {
		setLayout(fieldGroup, Layout.COLUMNS);
	}

	/**
	 * Set the layout of the field group by rows.
	 * 
	 * @param fieldGroup The field group.
	 */
	public void setLayoutByRows(FieldGroup fieldGroup) {
		setLayout(fieldGroup, Layout.ROWS);
	}

	/**
	 * Update editor controls with record values.
	 */
	public void updateEditors() {
		for (Group group : groups) {
			for (Grid grid : group.grids) {
				for (EditContext context : grid.contexts) {
					String alias = context.getField().getAlias();
					Value value = record.getValue(alias);
					context.setValue(value);
				}
			}
		}
	}

	/**
	 * Update the record from data in editors.
	 */
	public void updateRecord() {
		for (Group group : groups) {
			for (Grid grid : group.grids) {
				for (EditContext context : grid.contexts) {
					String alias = context.getField().getAlias();
					Value value = context.getValue();
					record.setValue(alias, value);
				}
			}
		}
	}
}
