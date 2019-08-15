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
package com.mlt.desktop.control.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.plaf.metal.MetalBorders;

import com.mlt.desktop.AWT;
import com.mlt.desktop.layout.Anchor;
import com.mlt.desktop.layout.Constraints;
import com.mlt.desktop.layout.Dimension;
import com.mlt.desktop.layout.Fill;
import com.mlt.desktop.layout.Insets;

/**
 * Row header. Shows row numbers and helps select rows and view the focused row.
 * 
 * @author Miquel Sas
 */
public class RowHeader extends JList<Object> implements PropertyChangeListener, TableCmpListener, TableModelListener {

	/**
	 * Model.
	 */
	class Model extends AbstractListModel<Object> {
		@Override
		public int getSize() {
			return tableCmp.getRowCount();
		}

		@Override
		public Object getElementAt(int index) {
			return index;
		}

	}

	/**
	 * Cell renderer.
	 */
	class Renderer extends JPanel implements ListCellRenderer<Object> {

		/** Label. */
		private JLabel label;
		/** Color unselected. */
		private Color unselectedBackground;
		/** Color selected. */
		private Color selectedBackground;

		/**
		 * Constructor.
		 */
		private Renderer() {
			super();

			setOpaque(true);
			setLayout(new GridBagLayout());
			setBorder(new MetalBorders.TableHeaderBorder());

			label = new JLabel();
			label.setOpaque(false);
			label.setHorizontalAlignment(JLabel.CENTER);

			setFont(new Font(Font.DIALOG, Font.BOLD, 12));
			setDoubleBuffered(true);

			setup();
		}

		/**
		 * Do the layout with the given margin.
		 * 
		 * @param margin The margin.
		 */
		private void setup() {
			unselectedBackground = tableCmp.getCfg().getColor(TableCfg.ROW_HEADER_UNSELECTED_COLOR);
			selectedBackground = tableCmp.getCfg().getColor(TableCfg.ROW_HEADER_SELECTED_COLOR);
			Insets margin = tableCmp.getCfg().getMargin(TableCfg.ROW_HEADER_MARGIN);
			removeAll();
			add(label, AWT.toAWT(new Constraints(Anchor.TOP, Fill.BOTH, 0, 0, margin)));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected,
		boolean focus) {

			if (tableCmp.isRowFocused(index)) {
				setBackground(selectedBackground);
			} else {
				setBackground(unselectedBackground);
			}

			label.setText((value == null) ? "" : Integer.toString(index + 1));
			setPreferredSize(null);
			Dimension size = new Dimension(getPreferredSize().getWidth(), tableCmp.getRowHeight(index));
			setPreferredSize(AWT.toAWT(size));

			return this;
		}
	}

	/** Table component. */
	private TableCmp tableCmp;
	/** Renderer. */
	private Renderer renderer;

	/**
	 * Constructor.
	 * 
	 * @param tableCmp The related table component that installs this row header.
	 */
	public RowHeader(TableCmp tableCmp) {
		super();
		this.tableCmp = tableCmp;

		/* Set this renderer as a listener to table configuration change properties. */
		this.tableCmp.getCfg().addPropertyChangeListener(this);

		setModel(new Model());
		setCellRenderer(renderer = new Renderer());
		setDragEnabled(false);

		setUI(new RowHeaderUI(tableCmp, this));
	}

	/**
	 * Called when table component configuration parameters change.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		renderer.setup();
		repaint();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void focusCellChanged(TableCmp tableCmp) {
		revalidate();
		repaint();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void selectionChanged(TableCmp tableCmp) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void tableChanged(TableModelEvent e) {
		RowHeaderUI ui = (RowHeaderUI) getUI();
		ui.calculateCellWidth();
	}
}