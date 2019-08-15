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

import java.util.ArrayList;
import java.util.List;

/**
 * Table sort definition.
 * 
 * @author Miquel Sas
 */
public class TableSorter {

	/**
	 * Key.
	 */
	public static class Key {
		/** Column index. */
		private int column;
		/** Sort order. */
		private TableSorter.Order order;

		/**
		 * Constructor.
		 * 
		 * @param column The column index.
		 * @param order  The sort order.
		 */
		public Key(int column, TableSorter.Order order) {
			super();
			if (order == null) {
				throw new NullPointerException();
			}
			this.column = column;
			this.order = order;
		}

		/**
		 * Return the column index.
		 * 
		 * @return The column index.
		 */
		public int getColumn() {
			return column;
		}

		/**
		 * Return the sort order.
		 * 
		 * @return The sort order.
		 */
		public TableSorter.Order getOrder() {
			return order;
		}

		/**
		 * Toggle the current order.
		 */
		public void toggleOrder() {
			order = (order.equals(Order.ASCENDING) ? Order.DESCENDING : Order.ASCENDING);
		}

		/**
		 * Check if the order is ascending.
		 * 
		 * @return A boolean.
		 */
		public boolean isAscending() {
			return order.equals(Order.ASCENDING);
		}

		/**
		 * Check if the order is descending.
		 * 
		 * @return A boolean.
		 */
		public boolean isDescending() {
			return order.equals(Order.DESCENDING);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			return "Column " + column + ", " + order;
		}
	}

	/**
	 * Order.
	 */
	public static enum Order {
		/** Ascending sort order. */
		ASCENDING,
		/** Descending sort order. */
		DESCENDING;
	}

	/** List of keys. */
	private List<TableSorter.Key> keys = new ArrayList<>();

	/**
	 * Constructor.
	 */
	public TableSorter() {
		super();
	}

	/**
	 * Give access to the list of keys.
	 * 
	 * @return The list of keys.
	 */
	public List<TableSorter.Key> getKeys() {
		return keys;
	}

	/**
	 * Check if the column is contained in the list of keys.
	 * 
	 * @param column The column to check.
	 * @return A boolean.
	 */
	public boolean contains(int column) {
		for (int i = 0; i < keys.size(); i++) {
			if (keys.get(i).column == column) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return the key of the column or null.
	 * 
	 * @param column The column.
	 * @return The key or null.
	 */
	public TableSorter.Key getKey(int column) {
		for (int i = 0; i < keys.size(); i++) {
			if (keys.get(i).column == column) {
				return keys.get(i);
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < keys.size(); i++) {
			if (i > 0) {
				b.append(", ");
			}
			b.append("[" + keys.get(i).toString() + "]");
		}
		return b.toString();
	}
}