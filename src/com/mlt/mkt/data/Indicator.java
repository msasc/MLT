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

import java.util.List;
import java.util.Objects;

import com.mlt.mkt.data.info.IndicatorInfo;

/**
 * Indicator base class.
 * <p>
 * Implementations must define the <b><i>IndicatorInfo</i></b> that, additionally to the base <b><i>DataInfo</i></b>
 * outputs, indicates the input parameters and their values.
 * <p>
 * Implementations must also implement the <b><i>calculate</i></b> method. This method receives 3 parameters:
 * <ul>
 * <li>The index of the data to be calculated.</li>
 * <li>The list of indicator sources defined in the <b><i>IndicatorInfo</i></b> as inputs.</li>
 * <li>The list of already calculated values.</li>
 * </ul>
 *
 * @author Miquel Sas
 */
public abstract class Indicator {

	/**
	 * Calculates an indicator.
	 *
	 * @param indicator        The indicator to calculate.
	 * @param indicatorSources The list of indicator sources.
	 * @return The indicator data list.
	 */
	public static DataList calculate(Indicator indicator, List<IndicatorSource> indicatorSources) {
		IndicatorDataList indicatorData = new IndicatorDataList(indicator, indicatorSources);
		indicator.start(indicatorSources);
		int size = indicatorSources.get(0).getDataList().size();
		for (int index = 0; index < size; index++) {
			Data data = indicator.calculate(index, indicatorSources, indicatorData);
			indicatorData.add(data);
		}
		return indicatorData;
	}

	/** The indicator info to be configured. */
	private IndicatorInfo indicatorInfo;
	/** The number of indexes for all the indicator sources. */
	private int numIndexes;

	/**
	 * Constructor.
	 */
	public Indicator() {
		super();
		indicatorInfo = new IndicatorInfo();
	}

	/**
	 * Returns the indicator info.
	 *
	 * @return The indicator info.
	 */
	public IndicatorInfo getIndicatorInfo() {
		return indicatorInfo;
	}

	/**
	 * Calculates the total number of indexes as a helper to further calculations. Normally should be called in the
	 * <i>start</i> method.
	 *
	 * @param indicatorSources The list of indicator sources.
	 */
	protected void calculateNumIndexes(List<IndicatorSource> indicatorSources) {
		numIndexes = 0;
		for (IndicatorSource source : indicatorSources) {
			numIndexes += source.getIndexes().size();
		}
	}

	/**
	 * Returns the total number of indexes, that must be previously calculated with a call to
	 * <i>calculateNumIndexes</i>.
	 *
	 * @return the numIndexes
	 */
	protected int getNumIndexes() {
		return numIndexes;
	}

	/**
	 * Set the number of indexes, for indicators that do not set it based on the input sources.
	 *
	 * @param numIndexes The number of indexes.
	 */
	protected void setNumIndexes(int numIndexes) {
		this.numIndexes = numIndexes;
	}

	/**
	 * Called before starting calculations to give the indicator the opportunity to initialize any internal res.
	 *
	 * @param indicatorSources The list of indicator sources.
	 */
	public abstract void start(List<IndicatorSource> indicatorSources);

	/**
	 * Calculates the indicator data at the given index, for the list of indicator sources.
	 * <p>
	 * This indicator already calculated data is passed as a parameter because some indicators may need previous
	 * calculated values or use them to improve calculation performance.
	 *
	 * @param index            The data index.
	 * @param indicatorSources The list of indicator sources.
	 * @param indicatorData    This indicator already calculated data.
	 * @return The result data.
	 */
	public abstract Data calculate(int index, List<IndicatorSource> indicatorSources, DataList indicatorData);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Indicator) {
			Indicator indicator = (Indicator) obj;
			if (!getIndicatorInfo().equals(indicator.getIndicatorInfo())) {
				return false;
			}
			return numIndexes == indicator.getNumIndexes();
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 59 * hash + Objects.hashCode(this.indicatorInfo);
		hash = 59 * hash + this.numIndexes;
		return hash;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return getIndicatorInfo().toString();
	}

}
