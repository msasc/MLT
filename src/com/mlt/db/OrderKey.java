/*
 * Copyright (C) 2018 Miquel Sas
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
package com.mlt.db;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.mlt.util.Lists;

/**
 * An order key.
 *
 * @author Miquel Sas
 */
public class OrderKey implements Comparable<Object> {

	/**
	 * Singleton segment comparator.
	 */
	public static final SegmentComparator SEGMENT_COMPARATOR = new SegmentComparator();

	/**
	 * Segment comparator.
	 */
	public static class SegmentComparator implements Comparator<Segment> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int compare(Segment s1, Segment s2) {
			int compare = s1.value.compareTo(s2.value);
			if (compare != 0) {
				return compare * (s1.asc ? 1 : -1);
			}
			return 0;
		}
	}

	/**
	 * An order key segment is a small structure to pack segment (value,asc/desc)
	 * information.
	 */
	public static class Segment implements Comparable<Object> {

		/**
		 * The ascending flag.
		 */
		private boolean asc = true;
		/**
		 * The value.
		 */
		private Value value = null;

		/**
		 * Constructor assigning value and ascending flag.
		 *
		 * @param value The field value.
		 * @param asc   The ascending flag.
		 */
		public Segment(Value value, boolean asc) {
			super();
			if (value == null) {
				throw new NullPointerException();
			}
			this.value = value;
			this.asc = asc;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int compareTo(Object o) {
			if (o == null) {
				throw new NullPointerException();
			}
			if (o instanceof Segment) {
				Segment segment = (Segment) o;
				return SEGMENT_COMPARATOR.compare(this, segment);
			}
			throw new IllegalArgumentException("Not comparable type: " + o.getClass().getName());
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object o) {
			return compareTo(o) == 0;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			int hash = 0;
			hash ^= value.hashCode();
			hash ^= Boolean.valueOf(asc).hashCode();
			return hash;
		}

		/**
		 * Get the value.
		 *
		 * @return The value.
		 */
		public Value getValue() {
			return value;
		}

		/**
		 * Check the ascending flag.
		 *
		 * @return A boolean
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
		 * Set the value.
		 *
		 * @param value The value.
		 */
		public void setValue(Value value) {
			if (value == null) {
				throw new NullPointerException();
			}
			this.value = value;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			StringBuilder b = new StringBuilder(128);
			if (value != null) {
				b.append(value.toString());
			} else {
				b.append("null");
			}
			b.append(", ");
			b.append(isAsc() ? "ASC" : "DESC");
			return b.toString();
		}

	}

	/**
	 * List of segments.
	 */
	private List<Segment> segments = new ArrayList<>();

	/**
	 * Default constructor.
	 */
	public OrderKey() {
		super();
	}

	/**
	 * Constructor assigning a list of values in ascending order.
	 *
	 * @param values The list of values.
	 */
	public OrderKey(Value... values) {
		this(Lists.asList(values));
	}

	/**
	 * Constructor assigning a list of values in ascending order.
	 *
	 * @param values The list of values.
	 */
	public OrderKey(List<Value> values) {
		super();
		for (Value value : values) {
			add(value, true);
		}
	}

	/**
	 * Add a value to the key.
	 *
	 * @param value The value.
	 */
	public void add(Value value) {
		add(value, true);
	}

	/**
	 * Add a value segment to the segment list.
	 *
	 * @param value The value of the segment.
	 * @param asc   The ascending/descending flag
	 */
	public final void add(Value value, boolean asc) {
		segments.add(new Segment(value, asc));
	}

	/**
	 * Return the segment at the given index.
	 *
	 * @param index The index.
	 * @return The segment.
	 */
	public Segment get(int index) {
		return segments.get(index);
	}

	/**
	 * Return the value at the position.
	 * 
	 * @param index The index position.
	 * @return The value.
	 */
	public Value getValue(int index) {
		return get(index).getValue();
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
	public int compareTo(Object o) {
		if (o instanceof OrderKey) {
			OrderKey orderKey = (OrderKey) o;
			return Lists.compare(segments, orderKey.segments, SEGMENT_COMPARATOR);
		}
		throw new IllegalArgumentException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int hash = 1;
		for (Segment segment : segments) {
			hash |= segment.hashCode();
		}
		return hash;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof OrderKey) {
			OrderKey orderKey = (OrderKey) obj;
			return Lists.equals(segments, orderKey.segments);
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder(256);
		for (int i = 0; i < size(); i++) {
			b.append(get(i).toString());
			if (i < size() - 1) {
				b.append("; ");
			}
		}
		return b.toString();
	}
}
