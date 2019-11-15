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

package com.mlt.util;

import java.util.ArrayList;
import java.util.List;

/**
 * A fixed size list, backed by an array list. Elements are efficiently added
 * until the list reaches the maximum size, and when a new element is added, the
 * first element is lost.
 *
 * @author Miquel Sas
 */
public class FixedSizeList<E> {

	/** Internal list. */
	private List<E> list = new ArrayList<>();
	/** Maximum size. */
	private int maximumSize = -1;
	/** First index. */
	private int firstIndex = -1;
	/** Last index. */
	private int lastIndex = -1;
	/** Size factor. */
	private int sizeFactor;

	/**
	 * Constructor.
	 * 
	 * @param maximumSize Maximum size.
	 */
	public FixedSizeList(int maximumSize) {
		this(maximumSize, 4);
	}

	/**
	 * Constructor.
	 * 
	 * @param maximumSize Maximum size.
	 * @param sizeFactor  Size factor, GE than 2.
	 */
	public FixedSizeList(int maximumSize, int sizeFactor) {
		super();
		if (maximumSize <= 0) {
			throw new IllegalArgumentException("Invalid maximum size");
		}
		if (sizeFactor < 2) {
			throw new IllegalArgumentException("Invalid size factor");
		}
		this.maximumSize = maximumSize;
		this.sizeFactor = sizeFactor;
	}

	/**
	 * Add a new element to the queue.
	 * 
	 * @param e The element to add.
	 */
	public void add(E e) {

		/* List is empty, initialize. */
		if (list.isEmpty()) {
			list.add(e);
			firstIndex = 0;
			lastIndex = 0;
			return;
		}

		/* Size less than maximum size. */
		if (size() < maximumSize) {
			list.add(e);
			firstIndex = 0;
			lastIndex++;
			return;
		}

		/* Size equals maximum size. */
		if (size() != maximumSize) {
			throw new IllegalStateException();
		}
		list.set(firstIndex, null);
		firstIndex++;
		lastIndex++;
		if (list.size() > lastIndex) {
			list.set(lastIndex, e);
		} else {
			list.add(e);
		}

		/*
		 * List size equals maximumSize * sizeFactor and lastIndex equals list.size()-1
		 */
		if (list.size() == maximumSize * sizeFactor && lastIndex == list.size() - 1) {
			for (int index = firstIndex; index <= lastIndex; index++) {
				list.set(index - firstIndex, list.get(index));
			}
			firstIndex = 0;
			lastIndex = maximumSize - 1;
			/* Clear rest to let GC do its work. */
			for (int index = lastIndex + 1; index < list.size(); index++) {
				list.set(index, null);
			}
		}

	}

	/**
	 * Clear the list.
	 */
	public void clear() {
		list.clear();
		firstIndex = -1;
		lastIndex = -1;
	}

	/**
	 * Return the element at the given index starting with index 0 at the head or
	 * origin of the queue.
	 * 
	 * @param index The index.
	 * @return The element.
	 */
	public E getHead(int index) {
		if (index < 0 || index >= size()) {
			throw new ArrayIndexOutOfBoundsException();
		}
		return list.get(firstIndex + index);
	}

	/**
	 * Return the element at the given index starting with index 0 at the tail or
	 * end of the queue.
	 * 
	 * @param index The index.
	 * @return The element.
	 */
	public E getTail(int index) {
		if (index < 0 || index >= size()) {
			throw new ArrayIndexOutOfBoundsException();
		}
		return list.get(lastIndex - index);
	}

	/**
	 * Return the maximum size.
	 * 
	 * @return The maximum size.
	 */
	public int getMaximumSize() {
		return maximumSize;
	}

	/**
	 * Return the working size of the queue.
	 * 
	 * @return The size.
	 */
	public int size() {
		if (list.isEmpty()) {
			return 0;
		}
		return lastIndex - firstIndex + 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		if (!list.isEmpty()) {
			b.append("[");
			for (int i = 0; i < size(); i++) {
				if (i > 0) {
					b.append(", ");
				}
				b.append(getHead(i));
			}
			b.append("]");
		}
		return b.toString();
	}
}
