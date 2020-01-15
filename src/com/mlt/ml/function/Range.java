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

package com.mlt.ml.function;

import java.util.ArrayList;
import java.util.List;

/**
 * A small structure to handle a range of indexes.
 *
 * @author Miquel Sas
 */
public class Range {

	/**
	 * @param count  The number of indexes.
	 * @param module The module to fraction count (available processors)
	 * @return The list of ranges.
	 */
	public static List<Range> getRanges(int count, int module) {
		List<Range> ranges = new ArrayList<>();
		int indexes = (count > module ? count / module : 1);
		int start = 0;
		while (true) {
			int end = start + indexes - 1;
			if (end >= count) {
				end = count - 1;
			}
			ranges.add(new Range(start, end));
			if (end == count - 1) {
				break;
			}
			start = end + 1;
		}
		return ranges;
	}

	/** First or start index. */
	private int start;
	/** Last or end index. */
	private int end;

	/**
	 * @param start The start index.
	 * @param end   The end index.
	 */
	public Range(int start, int end) {
		super();
		this.start = start;
		this.end = end;
	}

	/**
	 * @return The start index.
	 */
	public int getStart() {
		return start;
	}

	/**
	 * @return The end index.
	 */
	public int getEnd() {
		return end;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "(" + start + ", " + end + ")";
	}
}
