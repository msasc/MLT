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

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A fixed size list, backed by an array list. Elements are efficiently added
 * until the list reaches the maximum size, and when a new element is added, the
 * first element is lost.
 *
 * @author Miquel Sas
 */
public class FixedSizeList<E> extends AbstractCollection<E> {

	/**
	 * Internal iterator.
	 */
	private class InternalIterator implements Iterator<E> {

		/** Index of the current element. */
		private int index = -1;
		/** Call to next control flag, to apply remove properly. */
		private boolean nextCalled = false;

		@Override
		public boolean hasNext() {
			return !isEmpty() && index < size() - 1;
		}

		@Override
		public E next() {
			if (!hasNext()) {
				throw new IllegalStateException("Empty list or end of list reached.");
			}
			index++;
			E element = getFirst(index);
			nextCalled = true;
			return element;
		}

		@Override
		public void remove() {
			if (!nextCalled) {
				throw new IllegalStateException("Next has not been called");
			}
			FixedSizeList.this.remove(index);
			index--;
			nextCalled = false;
		}

	}

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
	@Override
	public boolean add(E e) {

		/* List is empty, initialize. */
		if (list.isEmpty()) {
			list.add(e);
			firstIndex = 0;
			lastIndex = 0;
			return true;
		}

		/* Size less than maximum size. */
		if (size() < maximumSize) {
			list.add(e);
			firstIndex = 0;
			lastIndex++;
			return true;
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

		return true;
	}

	/**
	 * Clear the list.
	 */
	@Override
	public void clear() {
		super.clear();
		firstIndex = -1;
		lastIndex = -1;
	}

	/**
	 * Return the first element of the queue.
	 * 
	 * @return The element.
	 */
	public E getFirst() {
		return getFirst(0);
	}

	/**
	 * Return the element at the given index starting with index 0 at the head or
	 * origin of the queue.
	 * 
	 * @param index The index.
	 * @return The element.
	 */
	public E getFirst(int index) {
		if (index < 0 || index >= size()) {
			throw new ArrayIndexOutOfBoundsException();
		}
		return list.get(firstIndex + index);
	}

	/**
	 * Return the last element of the queue.
	 * 
	 * @return The element.
	 */
	public E getLast() {
		return getLast(0);
	}

	/**
	 * Return the element at the given index starting with index 0 at the tail or
	 * end of the queue.
	 * 
	 * @param index The index.
	 * @return The element.
	 */
	public E getLast(int index) {
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
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<E> iterator() {
		return new InternalIterator();
	}

	/**
	 * Remove and return the element at the given index starting with index 0 at the
	 * head or origin of the queue.
	 * 
	 * @param index The index.
	 * @return The element.
	 */
	public E remove(int index) {
		if (index < 0 || index >= size()) {
			throw new ArrayIndexOutOfBoundsException();
		}
		E element = list.remove(firstIndex + index);
		lastIndex--;
		return element;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size() {
		if (list.isEmpty()) {
			return 0;
		}
		return lastIndex - firstIndex + 1;
	}
}
