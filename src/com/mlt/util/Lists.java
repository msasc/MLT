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
package com.mlt.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Array and list utility functions.
 *
 * @author Miquel Sas
 */
public class Lists {

	/**
	 * Add the element to the first position of a list.
	 *
	 * @param <T>  The type.
	 * @param e    The element to add.
	 * @param list The list.
	 */
	public static <T> void addFirst(T e, List<T> list) {
		list.add(0, e);
	}

	/**
	 * Add the element to the last position of a list.
	 *
	 * @param <T>  The type.
	 * @param e    The element to add.
	 * @param list The list.
	 */
	public static <T> void addLast(T e, List<T> list) {
		list.add(e);
	}

	/**
	 * Returns a list given the argument array.
	 *
	 * @param array The array.
	 * @return The list.
	 */
	public static List<Double> asList(double... array) {
		List<Double> list = new ArrayList<>();
		if (array != null) {
			for (double element : array) {
				list.add(element);
			}
		}
		return list;
	}

	/**
	 * Returns a list given the argument array.
	 *
	 * @param array The array.
	 * @return The list.
	 */
	public static List<Integer> asList(int... array) {
		List<Integer> list = new ArrayList<>();
		if (array != null) {
			for (int element : array) {
				list.add(element);
			}
		}
		return list;
	}

	/**
	 * Returns a list given the argument array.
	 *
	 * @param <T>   The type.
	 * @param array The array.
	 * @return The list.
	 */
	@SafeVarargs
	public static <T> List<T> asList(T... array) {
		if (array == null) {
			return new ArrayList<>();
		}
		List<T> list = new ArrayList<>(array.length);
		for (T e : array) {
			list.add(e);
		}
		return list;
	}

	/**
	 * Compare two byte arrays.
	 *
	 * @param a The first array.
	 * @param b The second array.
	 * @return -1, 0 or 1 if a is less, equal or greater than b.
	 */
	public static int compare(byte[] a, byte[] b) {
		int length = Math.max(a.length, b.length);
		for (int i = 0; i < length; i++) {
			int ia = (i < a.length ? a[i] : 0);
			int ib = (i < b.length ? b[i] : 0);
			if (ia < ib) {
				return -1;
			}
			if (ia > ib) {
				return 1;
			}
		}
		return 0;
	}

	/**
	 * Compares two lists of the same size.
	 *
	 * @param <T>        The type to compare.
	 * @param list1      First list.
	 * @param list2      Second list.
	 * @param comparator The comparator.
	 * @return The comparison integer.
	 */
	public static <T> int compare(List<T> list1, List<T> list2, Comparator<T> comparator) {
		if (list1 == null || list2 == null || comparator == null) {
			throw new NullPointerException();
		}
		for (int i = 0; i < list1.size(); i++) {
			int compare = comparator.compare(list1.get(i), list2.get(i));
			if (compare != 0) {
				return compare;
			}
		}
		return 0;
	}

	/**
	 * Check whether the destination list contains any of the elements of the source
	 * list.
	 * 
	 * @param <T> The type.
	 * @param dst The destination list
	 * @param src The source list.
	 * @return A boolean.
	 */
	public static <T> boolean containsAny(List<T> dst, List<T> src) {
		for (T e : src) {
			if (dst.contains(e)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return a copy of the source array.
	 *
	 * @param a The source array.
	 * @return The copy.
	 */
	public static byte[] copy(byte[] a) {
		byte[] b = new byte[a.length];
		System.arraycopy(a, 0, b, 0, a.length);
		return b;
	}

	/**
	 * Count the number of incidences in the list.
	 *
	 * @param <T>  The type.
	 * @param e    The element to count.
	 * @param list The list.
	 * @return The number of incidences.
	 */
	public static <T> int count(T e, List<T> list) {
		int count = 0;
		for (T t : list) {
			if (t.equals(e)) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Return an empty list ready to add elements.
	 *
	 * @param <T> The type to create the empty list.
	 * @return An empty list.
	 */
	public static <T> List<T> emptyList() {
		return new ArrayList<>();
	}

	/**
	 * Check if both arrays are equal.
	 *
	 * @param a The first array.
	 * @param b The second array.
	 * @return
	 */
	public static boolean equals(byte[] a, byte[] b) {
		if (a == b) {
			return true;
		}
		if (a == null || b == null) {
			return false;
		}
		int length = a.length;
		if (b.length != length) {
			return false;
		}
		for (int i = 0; i < length; i++) {
			if (a[i] != b[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Check whether two lists are equal.
	 *
	 * @param l1 List 1.
	 * @param l2 List 2.
	 * @return A boolean.
	 */
	public static boolean equals(List<?> l1, List<?> l2) {
		if (l1.size() != l2.size()) {
			return false;
		}
		for (int i = 0; i < l1.size(); i++) {
			if (l1.get(i) == null && l2.get(i) != null) {
				return false;
			}
			if (l1.get(i) != null && l2.get(i) == null) {
				return false;
			}
			if (l1.get(i) == null && l2.get(i) == null) {
				continue;
			}
			if (!l1.get(i).equals(l2.get(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Return the first element of a collection.
	 * 
	 * @param <T>        The type of the collection elements.
	 * @param collection The collection.
	 * @return The first element.
	 */
	public static <T> T getFirst(Collection<T> collection) {
		Iterator<T> i = collection.iterator();
		if (i.hasNext()) {
			return i.next();
		}
		return null;
	}

	/**
	 * Returns the first element of a list.
	 *
	 * @param <T>  The type.
	 * @param list The list.
	 * @return The first element.
	 */
	public static <T> T getFirst(List<T> list) {
		if (list == null || list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

	/**
	 * Returns the last element of a collection.
	 *
	 * @param <T>        The type.
	 * @param collection The collection.
	 * @return The last element.
	 */
	public static <T> T getLast(Collection<T> collection) {
		if (collection == null || collection.isEmpty()) {
			return null;
		}
		T last = null;
		Iterator<T> i = collection.iterator();
		while (i.hasNext()) {
			last = i.next();
		}
		return last;
	}

	/**
	 * Returns the last element of a list.
	 *
	 * @param <T>  The type.
	 * @param list The list.
	 * @return The last element.
	 */
	public static <T> T getLast(List<T> list) {
		if (list == null || list.isEmpty()) {
			return null;
		}
		return list.get(list.size() - 1);
	}

	/**
	 * Check in the list.
	 *
	 * @param i        The check integer.
	 * @param integers The list of integers.
	 * @return A boolean.
	 */
	public static boolean in(int i, int... integers) {
		for (int c : integers) {
			if (c == i) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check in the list.
	 *
	 * @param <T>    The type to check.
	 * @param value  The value to check.
	 * @param values The list of values.
	 * @return A boolean.
	 */
	public static <T> boolean in(T value, List<T> values) {
		return values.stream().anyMatch((v) -> (v.equals(value)));
	}

	/**
	 * Check in the list.
	 *
	 * @param <T>    The type to check in.
	 * @param value  The value to check.
	 * @param values The list of values.
	 * @return A boolean.
	 */
	@SafeVarargs
	public static <T> boolean in(T value, T... values) {
		for (T v : values) {
			if (v.equals(value)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the index of this value in a list of values, or -1 if this value is
	 * not in the list.
	 *
	 * @param <T>    The type to check.
	 * @param value  The value to check.
	 * @param values The list of values.
	 * @return The index of this value in the list or -1.
	 */
	public static <T> int indexOf(T value, List<T> values) {
		return values.indexOf(value);
	}

	/**
	 * Returns the index of this value in a list of values, or -1 if this value is
	 * not in the list.
	 *
	 * @param <T>    The type to check.
	 * @param value  The value to check.
	 * @param values The list of values.
	 * @return The index of this value in the list or -1.
	 */
	@SafeVarargs
	public static <T> int indexOf(T value, T... values) {
		for (int index = 0; index < values.length; index++) {
			if (values[index].equals(value)) {
				return index;
			}
		}
		return -1;
	}

	/**
	 * Randomly get an element of the list.
	 *
	 * @param <T>  The type.
	 * @param list The list.
	 * @return The selected element.
	 */
	public static <T> T randomGet(List<T> list) {
		return list.get(ThreadLocalRandom.current().nextInt(list.size()));
	}

	/**
	 * Remove the first element in the list.
	 *
	 * @param <T>  The type.
	 * @param list The list.
	 * @return The removed element.
	 */
	public static <T> T removeFirst(List<T> list) {
		return list.remove(0);
	}

	/**
	 * Remove the last element in the list.
	 *
	 * @param <T>  The type.
	 * @param list The list.
	 * @return The removed element.
	 */
	public static <T> T removeLast(List<T> list) {
		return list.remove(list.size() - 1);
	}

	/**
	 * Reverse the list.
	 * 
	 * @param <T>  The type.
	 * @param list The list to reverse.
	 */
	public static <T> void reverse(List<T> list) {
		int size = list.size();
		int head;
		int tail;
		for (int i = 0; i < size; i++) {
			head = i;
			tail = size - head - 1;
			T e = list.get(head);
			list.set(head, list.get(tail));
			list.set(tail, e);
		}
	}

	/**
	 * Shuffle the list.
	 *
	 * @param <T>  The type of the list to shuffle.
	 * @param list The list to shuffle.
	 */
	public static <T> void shuffle(List<T> list) {
		shuffle(list, list.size() / 2);
	}

	/**
	 * Shuffle the list.
	 *
	 * @param <T>   The type of the list to shuffle.
	 * @param list  The list to shuffle.
	 * @param flips The number of flips to perform.
	 */
	public static <T> void shuffle(List<T> list, int flips) {
		for (int i = 0; i < flips; i++) {
			int indexFrom = ThreadLocalRandom.current().nextInt(list.size());
			T valueFrom = list.get(indexFrom);
			int indexTo = ThreadLocalRandom.current().nextInt(list.size());
			T valueTo = list.get(indexTo);
			list.set(indexTo, valueFrom);
			list.set(indexFrom, valueTo);
		}
	}

	/**
	 * Returns the array of double values given the list.
	 *
	 * @param list The list of doubles.
	 * @return The array.
	 */
	public static double[] toDoubleArray(List<Double> list) {
		double[] values = new double[list.size()];
		for (int i = 0; i < list.size(); i++) {
			values[i] = list.get(i);
		}
		return values;
	}

	/**
	 * Returns the array of int values given the list.
	 *
	 * @param list The list of doubles.
	 * @return The array.
	 */
	public static int[] toIntegerArray(List<Integer> list) {
		int[] values = new int[list.size()];
		for (int i = 0; i < list.size(); i++) {
			values[i] = list.get(i);
		}
		return values;
	}

	/**
	 * Returns a list accessible by index from a collection.
	 * 
	 * @param <T>        The type of data.
	 * @param collection The collection.
	 * @return The list.
	 */
	public static <T> List<T> toList(Collection<T> collection) {
		List<T> list = new ArrayList<>();
		for (T e : collection) {
			list.add(e);
		}
		return list;
	}

	/**
	 * Returns a list accessible by index from a collection.
	 * 
	 * @param <T>        The type of data.
	 * @param collection The collection.
	 * @return The list.
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> toList(T... collection) {
		List<T> list = new ArrayList<>();
		for (T e : collection) {
			list.add(e);
		}
		return list;
	}
}
