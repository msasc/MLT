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
package com.mlt.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * An order definition.
 *
 * @author Miquel Sas
 */
public class Order {

	/**
	 * An order segment is a small structure to pack order segment information.
	 */
	public static class Segment implements Comparable<Object> {

		/**
		 * The field.
		 */
		private Field field;
		/**
		 * The ascending flag.
		 */
		private boolean asc = true;

		/**
		 * Default constructor.
		 */
		public Segment() {
			super();
		}

		/**
		 * Copy.
		 *
		 * @param segment The segment.
		 */
		public Segment(Segment segment) {
			this.field = segment.field;
			this.asc = segment.asc;
		}

		/**
		 * Constructor assigning field and asc.
		 *
		 * @param field The field
		 * @param asc   The ascending flag
		 */
		public Segment(Field field, boolean asc) {
			super();
			if (field == null) {
				throw new NullPointerException();
			}
			this.field = field;
			this.asc = asc;
		}

		/**
		 * Get the field.
		 *
		 * @return The field.
		 */
		public Field getField() {
			return field;
		}

		/**
		 * Check the ascending flag.
		 *
		 * @return A <code>boolean</code>
		 */
		public boolean isAsc() {
			return asc;
		}

		/**
		 * Set the ascending flag.
		 *
		 * @param asc The ascending flag.
		 */
		public void setAsc(boolean asc) {
			this.asc = asc;
		}

		/**
		 * Set the field.
		 *
		 * @param field The field.
		 */
		public void setField(Field field) {
			if (field == null) {
				throw new NullPointerException();
			}
			this.field = field;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			int hash = 0;
			hash ^= field.hashCode();
			hash ^= Boolean.valueOf(asc).hashCode();
			return hash;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final Segment other = (Segment) obj;
			if (!Objects.equals(this.field, other.field)) {
				return false;
			}
			return this.asc == other.asc;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int compareTo(Object o) {
			if (o == null) {
				throw new NullPointerException();
			}
			if (!(o instanceof Segment)) {
				throw new UnsupportedOperationException("Not comparable type: " + o.getClass().getName());
			}
			Segment orderSegment = (Segment) o;
			int compare = field.compareTo(orderSegment.field);
			if (compare != 0) {
				return compare * (asc ? 1 : -1);
			}
			return 0;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			StringBuilder b = new StringBuilder(128);
			if (field != null) {
				b.append(field.toString());
			} else {
				b.append("null");
			}
			b.append(" - ");
			if (asc) {
				b.append("ASC");
			} else {
				b.append("DESC");
			}
			return b.toString();
		}

	}

	/** List of segments. */
	private List<Segment> segments = new ArrayList<>();

	/**
	 * Default constructor.
	 */
	public Order() {
		super();
	}

	/**
	 * Copy.
	 *
	 * @param order Source order.
	 */
	public Order(Order order) {
		super();
		for (Segment segment : order.segments) {
			segments.add(new Segment(segment));
		}
	}

	/**
	 * Add an ascending segment with the given field.
	 *
	 * @param field The field.
	 */
	public void add(Field field) {
		segments.add(new Segment(field, true));
	}

	/**
	 * Add a segment defined by the field and the ascending flag.
	 *
	 * @param field The field
	 * @param asc   The ascending flag
	 */
	public void add(Field field, boolean asc) {
		segments.add(new Segment(field, asc));
	}

	/**
	 * Returns a boolean indicating whether this order contains the argument field.
	 *
	 * @param field The field.
	 * @return A boolean.
	 */
	public boolean contains(Field field) {
		for (Segment segment : segments) {
			if (segment.getField().equals(field)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the segment containing the argument field or null.
	 *
	 * @param field The field.
	 * @return The segment containing the argument field or null.
	 */
	public Segment get(Field field) {
		for (Segment segment : segments) {
			if (segment.getField().equals(field)) {
				return segment;
			}
		}
		return null;
	}

	/**
	 * Returns the segment at the given index.
	 *
	 * @param index The segment index.
	 * @return The segment.
	 */
	public Segment get(int index) {
		return segments.get(index);
	}

	/**
	 * Returns the field at the given index.
	 *
	 * @param index The index of the field.
	 * @return The field.
	 */
	public Field getField(int index) {
		return get(index).getField();
	}

	/**
	 * Invert the ascending flag for each segment.
	 */
	public void invertAsc() {
		for (int i = 0; i < segments.size(); i++) {
			segments.get(i).setAsc(!segments.get(i).isAsc());
		}
	}

	/**
	 * Check if the segment is ascending.
	 *
	 * @param index The index of the segment.
	 * @return A boolean.
	 */
	public boolean isAsc(int index) {
		return get(index).isAsc();
	}

	/**
	 * Sets the segment containing the field, if any, ascending or descending.
	 *
	 * @param field The field.
	 * @param asc   Ascending/descending flag.
	 */
	public void set(Field field, boolean asc) {
		Segment segment = get(field);
		if (segment != null) {
			segment.setAsc(asc);
		}
	}
	
	/**
	 * Set all the order as ascending.
	 */
	public void setAscending() {
		for (int i = 0; i < segments.size(); i++) {
			segments.get(i).setAsc(true);
		}		
	}

	/**
	 * Set all the order as descending.
	 */
	public void setDescending() {
		for (int i = 0; i < segments.size(); i++) {
			segments.get(i).setAsc(false);
		}		
	}

	/**
	 * Return the size or number of segments.
	 *
	 * @return The number of segments.
	 */
	public int size() {
		return segments.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder(256);
		for (int i = 0; i < segments.size(); i++) {
			b.append(segments.get(i).toString());
			if (i < segments.size() - 1) {
				b.append("; ");
			}
		}
		return b.toString();
	}
}
