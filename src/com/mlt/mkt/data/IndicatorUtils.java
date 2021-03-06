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
package com.mlt.mkt.data;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.mlt.db.Value;
import com.mlt.mkt.chart.plotter.data.LinePlotter;
import com.mlt.mkt.data.indicators.ExponentialMovingAverage;
import com.mlt.mkt.data.indicators.PeriodIndicator;
import com.mlt.mkt.data.indicators.SimpleMovingAverage;
import com.mlt.mkt.data.indicators.WeightedMovingAverage;
import com.mlt.util.Lists;

/**
 * Indicator utilities.
 *
 * @author Miquel Sas
 */
public class IndicatorUtils {

	/**
	 * Returns an EMA indicator data list.
	 *
	 * @param dataList The source data list.
	 * @param period   The period of the SMA.
	 * @param index    The index in the data of the source to calculate the average.
	 * @param color    Plot color.
	 * @return The indicator data list.
	 */
	public static IndicatorDataList getExponentialMovingAverage(
		DataList dataList,
		int index,
		Color color,
		int period) {

		ExponentialMovingAverage ema = new ExponentialMovingAverage();
		ema.getIndicatorInfo().getParameter(PeriodIndicator.PERIOD_INDEX)
			.setValue(new Value(period));
		IndicatorSource source = new IndicatorSource(dataList, index);
		IndicatorDataList avgList = new IndicatorDataList(ema, Lists.asList(source));
		LinePlotter plotter = new LinePlotter();
		plotter.setColorBullishEven(color);
		plotter.setColorBearishEven(color);
		plotter.setColorBullishOdd(color);
		plotter.setColorBearishOdd(color);
		avgList.addPlotter(plotter);
		return avgList;
	}

	/**
	 * Returns a SMA indicator data list.
	 *
	 * @param dataList The source data list.
	 * @param period   The period of the SMA.
	 * @param index    The index in the data of the source to calculate the average.
	 * @param color    Plot color.
	 * @return The indicator data list.
	 */
	public static IndicatorDataList getSimpleMovingAverage(
		DataList dataList,
		int index,
		Color color,
		int period) {

		SimpleMovingAverage sma = new SimpleMovingAverage();
		sma.getIndicatorInfo().getParameter(PeriodIndicator.PERIOD_INDEX)
			.setValue(new Value(period));
		IndicatorSource source = new IndicatorSource(dataList, index);
		IndicatorDataList avgList = new IndicatorDataList(sma, Lists.asList(source));
		LinePlotter plotter = new LinePlotter();
		plotter.setColorBullishEven(color);
		plotter.setColorBearishEven(color);
		plotter.setColorBullishOdd(color);
		plotter.setColorBearishOdd(color);
		avgList.addPlotter(plotter);
		return avgList;
	}

	/**
	 * Returns a WMA indicator data list.
	 *
	 * @param dataList The source data list.
	 * @param period   The period of the SMA.
	 * @param index    The index in the data of the source to calculate the average.
	 * @param color    Plot color.
	 * @return The indicator data list.
	 */
	public static IndicatorDataList getWeightedMovingAverage(
		DataList dataList,
		int index,
		Color color,
		int period) {

		WeightedMovingAverage sma = new WeightedMovingAverage();
		sma.getIndicatorInfo().getParameter(PeriodIndicator.PERIOD_INDEX)
			.setValue(new Value(period));
		IndicatorSource source = new IndicatorSource(dataList, index);
		IndicatorDataList avgList = new IndicatorDataList(sma, Lists.asList(source));
		LinePlotter plotter = new LinePlotter();
		plotter.setColorBullishEven(color);
		plotter.setColorBearishEven(color);
		plotter.setColorBullishOdd(color);
		plotter.setColorBearishOdd(color);
		avgList.addPlotter(plotter);
		return avgList;
	}

	/**
	 * Returns a smoothed SMA indicator data list.
	 *
	 * @param dataList         The source data list.
	 * @param index            The index in the data of the source to calculate the
	 *                         average.
	 * @param period           The period of the SMA.
	 * @param smoothingPeriods The list of smoothing periods.
	 * @return The indicator data list.
	 */
	public static IndicatorDataList getSmoothedSimpleMovingAverage(
		DataList dataList,
		int index,
		int period,
		int... smoothingPeriods) {
		return getSmoothedSimpleMovingAverage(
			dataList, index, Color.BLACK, period, smoothingPeriods);
	}

	/**
	 * Returns a smoothed SMA indicator data list.
	 *
	 * @param dataList         The source data list.
	 * @param index            The index in the data of the source to calculate the
	 *                         average.
	 * @param color            Plot color.
	 * @param period           The period of the SMA.
	 * @param smoothingPeriods The list of smoothing periods.
	 * @return The indicator data list.
	 */
	public static IndicatorDataList getSmoothedSimpleMovingAverage(
		DataList dataList,
		int index,
		Color color,
		int period,
		int... smoothingPeriods) {

		int indexPeriod = PeriodIndicator.PERIOD_INDEX;

		SimpleMovingAverage sma = new SimpleMovingAverage();
		sma.getIndicatorInfo().getParameter(indexPeriod).setValue(new Value(period));
		IndicatorSource source = new IndicatorSource(dataList, index);
		IndicatorDataList lst = new IndicatorDataList(sma, Lists.asList(source));
		LinePlotter plotter = new LinePlotter();
		plotter.setColorBullishEven(color);
		plotter.setColorBearishEven(color);
		plotter.setColorBullishOdd(color);
		plotter.setColorBearishOdd(color);
		plotter.setIndex(0);
		lst.addPlotter(plotter);

		int indexSma = 0;
		for (int smooth : smoothingPeriods) {
			SimpleMovingAverage smoothedSma = new SimpleMovingAverage();
			smoothedSma.getIndicatorInfo().getParameter(indexPeriod).setValue(new Value(smooth));
			source = new IndicatorSource(lst, indexSma);
			lst = new IndicatorDataList(smoothedSma, Lists.asList(source));
			plotter = new LinePlotter();
			plotter.setColorBullishEven(color);
			plotter.setColorBearishEven(color);
			plotter.setColorBullishOdd(color);
			plotter.setColorBearishOdd(color);
			plotter.setIndex(indexSma);
			lst.addPlotter(plotter);
		}

		return lst;
	}

	/**
	 * Returns a smoothed WMA indicator data list.
	 *
	 * @param dataList         The source data list.
	 * @param index            The index in the data of the source to calculate the
	 *                         average.
	 * @param period           The period of the WMA.
	 * @param smoothingPeriods The list of smoothing periods.
	 * @return The indicator data list.
	 */
	public static IndicatorDataList getSmoothedWeightedMovingAverage(
		DataList dataList,
		int index,
		int period,
		int... smoothingPeriods) {
		return getSmoothedWeightedMovingAverage(
			dataList, index, Color.BLACK, period, smoothingPeriods);
	}

	/**
	 * Returns a smoothed WMA indicator data list.
	 *
	 * @param dataList         The source data list.
	 * @param index            The index in the data of the source to calculate the
	 *                         average.
	 * @param color            Plot color.
	 * @param period           The period of the WMA.
	 * @param smoothingPeriods The list of smoothing periods.
	 * @return The indicator data list.
	 */
	public static IndicatorDataList getSmoothedWeightedMovingAverage(
		DataList dataList,
		int index,
		Color color,
		int period,
		int... smoothingPeriods) {

		int indexPeriod = PeriodIndicator.PERIOD_INDEX;

		WeightedMovingAverage wma = new WeightedMovingAverage();
		wma.getIndicatorInfo().getParameter(indexPeriod).setValue(new Value(period));
		IndicatorSource source = new IndicatorSource(dataList, index);
		IndicatorDataList lst = new IndicatorDataList(wma, Lists.asList(source));
		LinePlotter plotter = new LinePlotter();
		plotter.setColorBullishEven(color);
		plotter.setColorBearishEven(color);
		plotter.setColorBullishOdd(color);
		plotter.setColorBearishOdd(color);
		plotter.setIndex(0);
		lst.addPlotter(plotter);

		int indexWma = 0;
		for (int smooth : smoothingPeriods) {
			WeightedMovingAverage smoothedWma = new WeightedMovingAverage();
			smoothedWma.getIndicatorInfo().getParameter(indexPeriod).setValue(new Value(smooth));
			source = new IndicatorSource(lst, indexWma);
			lst = new IndicatorDataList(smoothedWma, Lists.asList(source));
			plotter = new LinePlotter();
			plotter.setColorBullishEven(color);
			plotter.setColorBearishEven(color);
			plotter.setColorBullishOdd(color);
			plotter.setColorBearishOdd(color);
			plotter.setIndex(0);
			lst.addPlotter(plotter);
		}

		return lst;
	}
	/**
	 * Returns a the list of smoothed WMA indicator data list.
	 *
	 * @param dataList         The source data list.
	 * @param index            The index in the data of the source to calculate the
	 *                         average.
	 * @param color            Plot color.
	 * @param period           The period of the WMA.
	 * @param smoothingPeriods The list of smoothing periods.
	 * @return The indicator data list.
	 */
	public static List<IndicatorDataList> getSmoothedWeightedMovingAverages(
		DataList dataList,
		int index,
		Color color,
		int period,
		int... smoothingPeriods) {

		int indexPeriod = PeriodIndicator.PERIOD_INDEX;
		
		List<IndicatorDataList> lists = new ArrayList<>();

		WeightedMovingAverage wma = new WeightedMovingAverage();
		wma.getIndicatorInfo().getParameter(indexPeriod).setValue(new Value(period));
		IndicatorSource source = new IndicatorSource(dataList, index);
		IndicatorDataList list = new IndicatorDataList(wma, Lists.asList(source));
		LinePlotter plotter = new LinePlotter();
		plotter.setColorBullishEven(color);
		plotter.setColorBearishEven(color);
		plotter.setColorBullishOdd(color);
		plotter.setColorBearishOdd(color);
		plotter.setIndex(0);
		list.addPlotter(plotter);
		
		lists.add(list);

		int indexWma = 0;
		for (int smooth : smoothingPeriods) {
			WeightedMovingAverage smoothedWma = new WeightedMovingAverage();
			smoothedWma.getIndicatorInfo().getParameter(indexPeriod).setValue(new Value(smooth));
			source = new IndicatorSource(list, indexWma);
			list = new IndicatorDataList(smoothedWma, Lists.asList(source));
			list.addPlotter(plotter);
			lists.add(list);
		}

		return lists;
	}
}
