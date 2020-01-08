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
		while (count % module != 0) {
			module--;
		}
		int indexes = count / module;
		int start = 0;
		while (true) {
			int end = start + indexes - 1;
			if (end >= count) {
				end = count - 1;
			}
			Range range = new Range();
			range.start = start;
			range.end = end;
			ranges.add(range);
			if (end == count - 1) {
				break;
			}
			start = end + 1;
		}
		return ranges;
	}

	/** First or start index. */
	public int start;
	/** Last or end index. */
	public int end;
}
