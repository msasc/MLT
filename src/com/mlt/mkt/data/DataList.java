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
package com.mlt.mkt.data;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.mlt.mkt.chart.DataContext;
import com.mlt.mkt.chart.plotter.DataPlotter;
import com.mlt.mkt.chart.plotter.data.BarPlotter;
import com.mlt.mkt.chart.plotter.data.CandlestickPlotter;
import com.mlt.mkt.chart.plotter.data.HistogramPlotter;
import com.mlt.mkt.chart.plotter.data.LinePlotter;
import com.mlt.mkt.data.info.DataInfo;
import com.mlt.util.Numbers;

/**
 * A list of data objects.
 *
 * @author Miquel Sas
 */
public abstract class DataList {

	/**
	 * Returns the list of first level indicator data lists, given a list of data
	 * lists.
	 *
	 * @param dataLists The source list of data lists.
	 * @return The list of first level indicator data lists.
	 */
	public static List<IndicatorDataList> getIndicatorDataLists(List<DataList> dataLists) {
		List<IndicatorDataList> indicatorDataLists = new ArrayList<>();
		for (int i = 0; i < dataLists.size(); i++) {
			DataList dataList = dataLists.get(i);
			if (dataList instanceof IndicatorDataList) {
				IndicatorDataList indicatorDataList = (IndicatorDataList) dataList;
				indicatorDataLists.add(indicatorDataList);
			}
		}
		return indicatorDataLists;
	}

	/**
	 * Returns the list of indicator data lists, in the order of precendence they
	 * are to be calculated. If an indicator
	 * uses the data of another indicator, the last one must be calculated first.
	 *
	 * @param dataLists The source list of data lists.
	 * @return The list of indicator data lists,
	 */
	public static List<IndicatorDataList> getIndicatorDataListsToCalculate(
		List<DataList> dataLists) {
		List<IndicatorDataList> firstLevelIndicatorDataLists = getIndicatorDataLists(dataLists);
		List<IndicatorDataList> indicatorDataLists = new ArrayList<>();
		fillIndicatorDataLists(indicatorDataLists, firstLevelIndicatorDataLists);
		return indicatorDataLists;
	}

	/**
	 * Fill the result indicator data list in the appropriate calculation order.
	 *
	 * @param results The result lists.
	 * @param parents The parent lists.
	 */
	private static void fillIndicatorDataLists(
		List<IndicatorDataList> results,
		List<IndicatorDataList> parents) {
		// Process required first.
		for (IndicatorDataList parent : parents) {
			List<IndicatorDataList> required = getIndicatorDataListsRequired(parent);
			fillIndicatorDataLists(results, required);
		}
		// Process current level.
		for (IndicatorDataList parent : parents) {
			if (!results.contains(parent)) {
				results.add(parent);
			}
		}
	}

	/**
	 * Returns the list of indicator data lists that the argument indicator data
	 * list requires as indicator sources.
	 *
	 * @param parent The parent indicator data list.
	 * @return The list of children required indicator data lists.
	 */
	private static List<IndicatorDataList> getIndicatorDataListsRequired(IndicatorDataList parent) {
		List<IndicatorDataList> children = new ArrayList<>();
		List<IndicatorSource> sources = parent.getIndicatorSources();
		for (IndicatorSource source : sources) {
			if (source.getDataList() instanceof IndicatorDataList) {
				IndicatorDataList child = (IndicatorDataList) source.getDataList();
				children.add(child);
			}
		}
		return children;
	}

	/**
	 * Returns all the unique data lists involved.
	 *
	 * @param parent The top list.
	 * @return All the unique data lists involved.
	 */
	public static List<DataList> getDataLists(DataList parent) {
		List<DataList> allDataLists = getAllDataLists(parent);
		List<DataList> dataLists = new ArrayList<>();
		for (DataList dataList : allDataLists) {
			if (!dataLists.contains(dataList)) {
				dataLists.add(dataList);
			}
		}
		return dataLists;
	}

	/**
	 * Returns all the data lists, even repeating.
	 *
	 * @param parent The top list.
	 * @return A list with all lists involved.
	 */
	private static List<DataList> getAllDataLists(DataList parent) {
		List<DataList> children = new ArrayList<>();
		if (parent instanceof IndicatorDataList) {
			IndicatorDataList indicator = (IndicatorDataList) parent;
			List<IndicatorSource> sources = indicator.getIndicatorSources();
			for (IndicatorSource source : sources) {
				children.addAll(getAllDataLists(source.getDataList()));
			}
		}
		children.add(parent);
		return children;
	}

	/** The data info. */
	private DataInfo dataInfo;
	/** The plot type to apply to this data. */
	private PlotType plotType = PlotType.LINE;
	/** A boolean that indicates if the data list should be plotted. */
	private boolean plot = true;
	/** List of data plotters. */
	private List<DataPlotter> dataPlotters = new ArrayList<>();

	/**
	 * Constructor assigning the data type..
	 *
	 * @param dataInfo The data info.
	 */
	public DataList(DataInfo dataInfo) {
		super();
		this.dataInfo = dataInfo;
	}

	/**
	 * Add the data element to this list.
	 *
	 * @param data The data element.
	 */
	public abstract void add(Data data);

	/**
	 * Add the data plotter.
	 *
	 * @param plotter The plotter.
	 */
	public void addPlotter(DataPlotter plotter) {
		dataPlotters.add(plotter);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DataList) {
			DataList dataList = (DataList) obj;
			if (!getDataInfo().equals(dataList.getDataInfo())) {
				return false;
			}
			return getPlotType().equals(dataList.getPlotType());
		}
		return false;
	}

	/**
	 * Returns the data element at the given index.
	 *
	 * @param index The index.
	 * @return The data element at the given index.
	 */
	public abstract Data get(int index);

	/**
	 * Returns this data list data info.
	 *
	 * @return The data info.
	 */
	public DataInfo getDataInfo() {
		return dataInfo;
	}

	/**
	 * Returns the list of data plotters.
	 *
	 * @return The list of data plotters.
	 */
	public List<DataPlotter> getPlotters() {
		if (dataPlotters.isEmpty()) {
			switch (getPlotType()) {
			case BAR:
				dataPlotters.add(new BarPlotter());
				break;
			case CANDLESTICK:
				dataPlotters.add(new CandlestickPlotter());
				break;
			case HISTOGRAM:
				dataPlotters.add(new HistogramPlotter(OHLC.VOLUME));
				break;
			case LINE:
				dataPlotters.add(new LinePlotter(OHLC.CLOSE));
				break;
			default:
				break;
			}
		}
		return dataPlotters;
	}

	/**
	 * Returns the odd code, 1 odd, 2 even, 0 none.
	 *
	 * @param data The data item.
	 * @return The odd code.
	 */
	public int getOddCode(Data data) {
		if (data == null) {
			return 0;
		}
		LocalDateTime t = LocalDateTime.now(ZoneId.of("UTC"));
		switch (getDataInfo().getPeriod().getUnit()) {
		case MILLISECOND:
		case SECOND:
		case MINUTE:
		case HOUR:
			if (Numbers.isOdd(t.getDayOfMonth())) {
				return 1;
			}
			return 2;
		case DAY:
			if (Numbers.isOdd(t.get(ChronoField.ALIGNED_WEEK_OF_YEAR))) {
				return 1;
			}
			return 2;
		case WEEK:
			if (Numbers.isOdd(t.getMonthValue())) {
				return 1;
			}
			return 2;
		case MONTH:
			if (Numbers.isOdd(t.getYear())) {
				return 1;
			}
			return 2;
		default:
			return 0;
		}
	}

	/**
	 * Returns the type of plot.
	 *
	 * @return The type of plot.
	 */
	public PlotType getPlotType() {
		return plotType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int hash = 3;
		hash = 97 * hash + Objects.hashCode(this.dataInfo);
		hash = 97 * hash + Objects.hashCode(this.plotType);
		hash = 97 * hash + (this.plot ? 1 : 0);
		hash = 97 * hash + Objects.hashCode(this.dataPlotters);
		return hash;
	}

	/**
	 * Returns <tt>true</tt> if this list contains no elements.
	 *
	 * @return <tt>true</tt> if this list contains no elements.
	 */
	public abstract boolean isEmpty();

	/**
	 * Check if a given period is Even.
	 *
	 * @param index The index of the period.
	 * @return A boolean that indicates if the period is Even.
	 */
	public boolean isEven(int index) {
		if (isEmpty()) {
			return false;
		}
		if (isEmpty()) {
			return false;
		}
		if (size() <= index) {
			return false;
		}
		return (getOddCode(get(index)) == 2);
	}

	/**
	 * Returns a boolean indicating whether the value at value index, of the data at
	 * data index, is a maximum for the
	 * argument period.
	 *
	 * @param dataIndex  The index of the data element.
	 * @param valueIndex The index of the value within the data.
	 * @param period     The period, number of data elements to check before and
	 *                   after.
	 * @return A boolean indicating whether the value is a maximum.
	 */
	public boolean isMaximum(int dataIndex, int valueIndex, int period) {
		return isMinimumMaximum(dataIndex, valueIndex, period, false);
	}

	/**
	 * Returns a boolean indicating whether the value at value index, of the data at
	 * data index, is a minimum for the
	 * argument period.
	 *
	 * @param dataIndex  The index of the data element.
	 * @param valueIndex The index of the value within the data.
	 * @param period     The period, number of data elements to check before and
	 *                   after.
	 * @return A boolean indicating whether the value is a minimum.
	 */
	public boolean isMinimum(int dataIndex, int valueIndex, int period) {
		return isMinimumMaximum(dataIndex, valueIndex, period, true);
	}

	/**
	 * Returns a boolean indicating whether the value at value index, of the data at
	 * data index, is a minimum/maximum
	 * for the argument period.
	 *
	 * @param dataIndex  The index of the data element.
	 * @param valueIndex The index of the value within the data.
	 * @param period     The period, number of data elements to check before and
	 *                   after.
	 * @param minimum    A boolean that indicates whether to check minimum or
	 *                   maximum.
	 * @return A boolean indicating whether the value is a minimum/maximum.
	 */
	private boolean isMinimumMaximum(int dataIndex, int valueIndex, int period, boolean minimum) {
		if (dataIndex < period) {
			return false;
		}
		if (dataIndex > size() - 1 - period) {
			return false;
		}
		double value = get(dataIndex).getValue(valueIndex);
		int startBackward = Math.max(0, dataIndex - period);
		for (int i = dataIndex - 1; i >= startBackward; i--) {
			if (minimum) {
				if (get(i).getValue(valueIndex) < value) {
					return false;
				}
			} else {
				if (get(i).getValue(valueIndex) > value) {
					return false;
				}
			}
		}
		int endForward = Math.min(dataIndex + period, size() - 1);
		for (int i = dataIndex + 1; i <= endForward; i++) {
			if (minimum) {
				if (get(i).getValue(valueIndex) < value) {
					return false;
				}
			} else {
				if (get(i).getValue(valueIndex) > value) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Check if a given period is odd.
	 *
	 * @param index The index of the period.
	 * @return A boolean that indicates if the period is odd.
	 */
	public boolean isOdd(int index) {
		if (isEmpty()) {
			return false;
		}
		if (isEmpty()) {
			return false;
		}
		if (size() <= index) {
			return false;
		}
		return (getOddCode(get(index)) == 1);
	}

	/**
	 * Check if the data list must be plotted.
	 *
	 * @return A boolean.
	 */
	public boolean isPlot() {
		return plot;
	}

	/**
	 * Check if a data list has to be plotted from scratch, mainly because it plots
	 * lines with dashes.
	 *
	 * @return A boolean that indicates if the data list has to be plotte from
	 *         scratch.
	 */
	public boolean isPlotFromScratch() {
		return false;
	}

	/**
	 * Check whether plotter is installed.
	 * 
	 * @param plotter The plotter.
	 * @return A boolean.
	 */
	public boolean isPlotter(DataPlotter plotter) {
		return dataPlotters.contains(plotter);
	}

	/**
	 * Remove and return the data at the given index.
	 *
	 * @param index The index.
	 * @return The removed data or null.
	 */
	public abstract Data remove(int index);

	/**
	 * Remove the given plotter.
	 * 
	 * @param plotter The plotter to remove.
	 */
	public void removePlotter(DataPlotter plotter) {
		dataPlotters.remove(plotter);
	}

	/**
	 * Set the plotter context to data plotters.
	 *
	 * @param context The plotter context.
	 */
	public void setContext(DataContext context) {
		getPlotters().forEach(plotter -> plotter.setContext(context));
	}

	/**
	 * Set if the data list must be plotted.
	 *
	 * @param plot A boolean.
	 */
	public void setPlot(boolean plot) {
		this.plot = plot;
	}

	/**
	 * Sets the type of plot.
	 *
	 * @param plotType The type of plot.
	 */
	public void setPlotType(PlotType plotType) {
		this.plotType = plotType;
	}

	/**
	 * Returns the number of elements in this list.
	 *
	 * @return The number of elements in this list.
	 */
	public abstract int size();

	/**
	 * Returns a string representation.
	 *
	 * @return A readable string representation.
	 */
	@Override
	public String toString() {
		DataInfo info = getDataInfo();
		StringBuilder b = new StringBuilder();
		b.append("[");
		b.append(info);
		b.append("]");
		return b.toString();
	}
}
