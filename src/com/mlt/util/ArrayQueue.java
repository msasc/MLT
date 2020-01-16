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

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * A queue that efficiently grows at the head and at the tail.
 *
 * @author Miquel Sas
 */
public class ArrayQueue<E> extends AbstractList<E> implements Queue<E> {

	/**
	 * Internal ascending iterator.
	 */
	private class AscendingIterator implements Iterator<E> {

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
			ArrayQueue.this.remove(index);
			index--;
			nextCalled = false;
		}

	}

	/**
	 * Internal descending iterator.
	 */
	private class DescendingIterator implements Iterator<E> {

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
			E element = getLast(index);
			nextCalled = true;
			return element;
		}

		@Override
		public void remove() {
			if (!nextCalled) {
				throw new IllegalStateException("Next has not been called");
			}
			ArrayQueue.this.remove(index);
			index--;
			nextCalled = false;
		}

	}

	/** Internal data. */
	private Object[] data;
	/** First index. */
	private int firstIndex = -1;
	/** Last index. */
	private int lastIndex = -1;
	/** Growing numerator. */
	private int growingNumerator;
	/** Growing denominator. */
	private int growingDenominator;

	/**
	 * Constructor.
	 */
	public ArrayQueue() {
		this(16, 3, 2);
	}

	/**
	 * Constructor.
	 * 
	 * @param initialCapacity Initial capacity.
	 */
	public ArrayQueue(int initialCapacity) {
		this(initialCapacity, 3, 2);
	}

	/**
	 * Constructor.
	 * 
	 * @param initialCapacity    Initial capacity.
	 * @param growingNumerator   Growing numerator.
	 * @param growingDenominator Growing denominator.
	 */
	public ArrayQueue(int initialCapacity, int growingNumerator, int growingDenominator) {
		super();
		if (initialCapacity < 5) {
			throw new IllegalArgumentException("Invalid initial capacity");
		}
		if (growingNumerator <= growingDenominator) {
			throw new IllegalArgumentException("Invalid growing numerator and denominator");
		}
		this.growingNumerator = growingNumerator;
		this.growingDenominator = growingDenominator;

		/* Initialize the data with initial capacity. */
		data = new Object[initialCapacity];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean add(E e) {
		addLast(e);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addFirst(E e) {

		/* No elements added. Set the first element at the center. */
		if (firstIndex == -1 && lastIndex == -1) {
			firstIndex = data.length / 2;
			lastIndex = firstIndex;
			data[firstIndex] = e;
			return;
		}

		/* If the first index is zero, center the data. */
		if (firstIndex == 0) {
			centerData();
		}

		/* Add the element at the head. */
		data[--firstIndex] = e;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addLast(E e) {

		/* No elements added. Set the first element at the center. */
		if (firstIndex == -1 && lastIndex == -1) {
			firstIndex = data.length / 2;
			lastIndex = firstIndex;
			data[firstIndex] = e;
			return;
		}

		/* If the last index is the last index of the list, center the data. */
		if (lastIndex == data.length - 1) {
			centerData();
		}

		/* Add the element at the tail. */
		data[++lastIndex] = e;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<E> ascendingIterator() {
		return new AscendingIterator();
	}

	/**
	 * Center current data within the internal list.
	 */
	private void centerData() {
		/* Check grow required. */
		if (size() >= data.length - 1) {
			int length = data.length * growingNumerator / growingDenominator;
			data = Arrays.copyOf(data, length);
		}
		/* Do center. */
		Object[] tmp = new Object[size()];
		for (int i = 0; i < tmp.length; i++) {
			tmp[i] = data[firstIndex + i];
		}
		for (int i = 0; i < data.length; i++) {
			data[i] = null;
		}
		firstIndex = (data.length - tmp.length) / 2;
		lastIndex = firstIndex + tmp.length - 1;
		for (int i = 0; i < tmp.length; i++) {
			data[firstIndex + i] = tmp[i];
		}
	}

	/**
	 * Clear the list.
	 */
	@Override
	public void clear() {
		for (int i = 0; i < data.length; i++) {
			data[i] = null;
		}
		firstIndex = -1;
		lastIndex = -1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<E> descendingIterator() {
		return new DescendingIterator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E get(int index) {
		return getFirst(index);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E getFirst() {
		return getFirst(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public E getFirst(int index) {
		if (index < 0 || index >= size()) {
			throw new ArrayIndexOutOfBoundsException();
		}
		return (E) data[firstIndex + index];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E getLast() {
		return getLast(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public E getLast(int index) {
		if (index < 0 || index >= size()) {
			throw new ArrayIndexOutOfBoundsException();
		}
		return (E) data[lastIndex - index];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<E> iterator() {
		return new AscendingIterator();
	}

	/**
	 * Remove and return the element at the given index starting with index 0 at the
	 * head or origin of the queue.
	 * 
	 * @param index The index.
	 * @return The element.
	 */
	@SuppressWarnings("unchecked")
	public E remove(int index) {
		if (index < 0 || index >= size()) {
			throw new ArrayIndexOutOfBoundsException();
		}
		E element = (E) data[firstIndex + index];
		for (int i = firstIndex + index + 1; i <= lastIndex; i++) {
			data[i - 1] = data[i];
		}
		data[lastIndex] = null;
		lastIndex--;
		return element;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size() {
		if (firstIndex == -1 && lastIndex == -1) {
			return 0;
		}
		return lastIndex - firstIndex + 1;
	}
}
