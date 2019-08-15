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
package com.mlt.mkt.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Indicator data source. IndicatorUtils can be calculated over a list of data lists, using a set of indexes for each
 * data list. The indicator source packs data list and the set of indexes to apply.
 *
 * @author Miquel Sas
 */
public class IndicatorSource {

	/**
	 * The data list.
	 */
	private DataList dataList;
	/**
	 * The list of indexes.
	 */
	private List<Integer> indexes;

	/**
	 * Constructor assigning fields.
	 *
	 * @param dataList The data list.
	 * @param indexes  The indexes.
	 */
	public IndicatorSource(DataList dataList, List<Integer> indexes) {
		super();
		this.dataList = dataList;
		this.indexes = indexes;
	}

	/**
	 * Constructor assigning fields.
	 *
	 * @param dataList The data list.
	 * @param indexes  The indexes.
	 */
	public IndicatorSource(DataList dataList, int... indexes) {
		super();
		this.dataList = dataList;
		setIndexes(indexes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IndicatorSource) {
			IndicatorSource is = (IndicatorSource) obj;
			if (!getDataList().equals(is.getDataList())) {
				return false;
			}
			if (!getIndexes().equals(is.getIndexes())) {
				return false;
			}
			return true;
		}
		return super.equals(obj);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 71 * hash + Objects.hashCode(this.dataList);
		return hash;
	}

	/**
	 * Returns the data list.
	 *
	 * @return the dataList
	 */
	public DataList getDataList() {
		return dataList;
	}

	/**
	 * Sets the data list.
	 *
	 * @param dataList The data list.
	 */
	public void setDataList(DataList dataList) {
		this.dataList = dataList;
	}

	/**
	 * Returns the list of indexes.
	 *
	 * @return The list of indexes.
	 */
	public List<Integer> getIndexes() {
		return indexes;
	}

	/**
	 * Sets the list of indexes.
	 *
	 * @param indexes The list of indexes.
	 */
	public void setIndexes(List<Integer> indexes) {
		this.indexes = indexes;
	}

	/**
	 * Sets the list of indexes.
	 *
	 * @param indexes The list of indexes.
	 */
	public void setIndexes(int... indexes) {
		this.indexes = new ArrayList<>();
		for (int index : indexes) {
			this.indexes.add(index);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(getDataList().toString());
		b.append("[");
		for (int i = 0; i < indexes.size(); i++) {
			if (i > 0) {
				b.append(", ");
			}
			b.append(indexes.get(i));
		}
		b.append("]");
		return b.toString();
	}

}
