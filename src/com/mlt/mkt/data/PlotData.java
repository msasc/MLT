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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mlt.mkt.chart.plotter.DataPlotter;
import com.mlt.mkt.data.info.DataInfo;
import com.mlt.util.Numbers;
import com.mlt.util.Properties;

/**
 * A container for the data to plot in a <i>JChartContainer</i>.
 *
 * @author Miquel Sas
 */
public class PlotData implements Iterable<DataList> {

	/** Identifier (optional) */
	private String id;
	/** Description. */
	private String description;
	/** User properties. */
	private Properties properties = new Properties();

	/**
	 * The number of bars to show at start when start and end indexes are not
	 * defined.
	 */
	private int startNumberOfBars = 200;

	/** A list of data lists. */
	private List<DataList> dataLists = new ArrayList<>();

	/** The start index to plot, can be negative, less that the min index. */
	private int startIndex = Numbers.MIN_INTEGER;
	/** The end index to plot, can greater than the max index. */
	private int endIndex = Numbers.MIN_INTEGER;
	/**
	 * The maximum value to plot (retrieving dataBag from start index to end index).
	 */
	private double maximumValue = Numbers.MIN_DOUBLE;
	/**
	 * The minimum value to plot (retrieving dataBag from start index to end index).
	 */
	private double minimumValue = Numbers.MAX_DOUBLE;
	/** The maximum data index. */
	private int maximumIndex = Numbers.MIN_INTEGER;
	/** The minimum data index. */
	private int minimumIndex = Numbers.MAX_INTEGER;

	/** The scale to plot the data. */
	private PlotScale plotScale = PlotScale.LINEAR;
	/** Date-time formatter. */
	private DateTimeFormatter timeFormatter;

	/**
	 * Minimum number of visible bars to ensure that scrolls, zooms and other
	 * movements always leave them visible.
	 */
	private int minimumVisibleData = 5;

	/**
	 * A boolean that indicates if the minimum value, when it is greater tthan zero,
	 * should be forced to zero. This is necessary when plotting histograms on the
	 * volume.
	 */
	private boolean zeroAsMinimum = false;

	/**
	 * Constructor.
	 * 
	 * @param id The identifier.
	 */
	public PlotData(String id) {
		super();
		if (id == null) throw new NullPointerException();
		this.id = id;
		this.description = id;
	}

	/**
	 * Adds a data list to this plot data. Data lists must be added in the correct
	 * order.
	 * <ul>
	 * <li>
	 * If the container is the main price container, the the price data list must be
	 * the first added, and no volume data list can be added. A subsequent indicator
	 * data list is considered an on chart indicator.
	 * </li>
	 * <li>If the container is the volume container, the volume data list must be
	 * the first, no price data list can be added, and subsequent indicator lists
	 * are considered on chart indicators.
	 * </li>
	 * <li>
	 * If the container is an indicator container, the indicator must be first added
	 * and only indicators can be added.
	 * </li>
	 * </ul>
	 *
	 * @param dataList The data list.
	 * @return A boolean indicating if it was added.
	 */
	public boolean add(DataList dataList) {
		// Validate subsequent lists.
		if (!isEmpty()) {
			// The period must be the same.
			DataList firstList = get(0);
			if (!firstList.getDataInfo().getPeriod().equals(dataList.getDataInfo().getPeriod())) {
				throw new IllegalArgumentException(
					"Data lists in the same plot data must have the same period.");
			}
			// Merge data lists so all have the same size.
			mergeDataLists();
		}
		boolean added = dataLists.add(dataList);
		setStartAndEndIndexes();
		return added;
	}

	/**
	 * Add (subtract) units to a time.
	 *
	 * @param time    The source time
	 * @param unit    The unit.
	 * @param periods The number of periods.
	 * @return The result time.
	 */
	private LocalDateTime addToTime(LocalDateTime time, Unit unit, int periods) {
		LocalDateTime result;
		ChronoUnit chronoUnit;
		switch (unit) {
		case MILLISECOND:
			chronoUnit = ChronoUnit.MILLIS;
			break;
		case SECOND:
			chronoUnit = ChronoUnit.SECONDS;
			break;
		case MINUTE:
			chronoUnit = ChronoUnit.MINUTES;
			break;
		case HOUR:
			chronoUnit = ChronoUnit.HOURS;
			break;
		case DAY:
			chronoUnit = ChronoUnit.DAYS;
			break;
		case WEEK:
			chronoUnit = ChronoUnit.WEEKS;
			break;
		case MONTH:
			chronoUnit = ChronoUnit.MONTHS;
			break;
		case YEAR:
			chronoUnit = ChronoUnit.YEARS;
			break;
		default:
			chronoUnit = ChronoUnit.MILLIS;
			break;
		}
		if (periods < 0) {
			result = time.minus(Math.abs(periods), chronoUnit);
		} else {
			result = time.plus(periods, chronoUnit);
		}
		return result;
	}

	/**
	 * Check if the maximum and minimum values have been calculated.
	 *
	 * @return A boolean that indicates if the maximum and minimum values have been
	 *         calculated.
	 */
	public boolean areMaximumAndMinimumValuesCalculated() {
		return maximumValue != Numbers.MIN_DOUBLE && minimumValue != Numbers.MAX_DOUBLE;
	}

	/**
	 * Calculates plot frame based on start and end index: minimum and maximum
	 * values, start end end time.
	 */
	public void calculateFrame() {

		// Check that there is data to calculate the frame.
		if (isEmpty()) {
			throw new IllegalStateException();
		}

		// Ensure that indicators are calculated up to the start index minus one.
		ensureIndicatorsCalculated();

		int dataSize = getDataSize();
		double maxValue = Numbers.MIN_DOUBLE;
		double minValue = Numbers.MAX_DOUBLE;
		int maxIndex = Numbers.MIN_INTEGER;
		int minIndex = Numbers.MAX_INTEGER;
		for (int i = startIndex; i < endIndex; i++) {
			if (i < 0 || i >= dataSize) {
				continue;
			}
			for (DataList dataList : dataLists) {
				Data data = dataList.get(i);
				if (data == null || !data.isValid()) {
					continue;
				}
				if (i > maxIndex) {
					maxIndex = i;
				}
				if (i < minIndex) {
					minIndex = i;
				}
				List<DataPlotter> dataPlotters = dataList.getPlotters();
				for (DataPlotter dataPlotter : dataPlotters) {
					double[] values = dataPlotter.getValues(data);
					for (double value : values) {
						if (value > maxValue) {
							maxValue = value;
						}
						if (value < minValue) {
							minValue = value;
						}
					}
				}
			}
		}

		// Assign calculated minimum and maximum values and indexes.
		minimumValue = minValue;
		maximumValue = maxValue;
		minimumIndex = minIndex;
		maximumIndex = maxIndex;

		// Reset minimum if applicable.
		if (zeroAsMinimum && minimumValue > 0) {
			minimumValue = 0;
		}
	}

	/**
	 * Center start and end indexes.
	 *
	 * @param start Start index.
	 * @param end   End index.
	 */
	public void center(int start, int end) {

		// Min/max indexes.
		int minIndex = 0;
		int maxIndex = getDataSize() - 1;

		// Current start and end indexes.
		int startIndex = this.startIndex;
		int endIndex = this.endIndex;

		// Remainder.
		int remainder = (endIndex - startIndex + 1) - (end - start + 1);

		// If remainder is greater than zero, just center.
		if (remainder > 0) {
			startIndex = start - remainder / 2;
			if (startIndex < minIndex) {
				startIndex = minIndex;
			}
			endIndex = end + remainder / 2;
			if (endIndex > maxIndex) {
				endIndex = maxIndex;
			}
		} else {
			startIndex = start;
			if (startIndex < minIndex) {
				startIndex = minIndex;
			}
			endIndex = end;
			if (endIndex > maxIndex) {
				endIndex = maxIndex;
			}
		}

		// Assign.
		this.startIndex = startIndex;
		this.endIndex = endIndex;
	}

	/**
	 * Check that indexes do not overlap.
	 */
	private void checkIndexes() {
		int dataSize = getDataSize();
		if (endIndex < startIndex) {
			endIndex = startIndex;
		}
		if (startIndex < 0 && endIndex < 0) {
			startIndex = endIndex = 0;
		}
		if (startIndex >= dataSize && endIndex >= dataSize) {
			startIndex = endIndex = dataSize - 1;
		}
		if (startIndex >= 0 && endIndex < dataSize) {
			if (endIndex == startIndex) {
				if (endIndex < dataSize - 1) {
					endIndex += 1;
				} else {
					startIndex -= 1;
				}
			}
		}
	}

	/**
	 * Ensure that indicators are calculated from look backward up to the start
	 * index minus one.
	 */
	private void ensureIndicatorsCalculated() {
		// Index 0, no need to do nothing.
		if (startIndex == 0) {
			return;
		}

		// If no indicator data lists...
		List<IndicatorDataList> indicatorDataLists =
			DataList.getIndicatorDataListsToCalculate(dataLists);
		if (indicatorDataLists.isEmpty()) {
			return;
		}

		// Get the maximum look backward.
		int lookBackward = 0;
		for (IndicatorDataList indicatorDataList : indicatorDataLists) {
			Indicator indicator = indicatorDataList.getIndicator();
			lookBackward = Math.max(lookBackward, indicator.getIndicatorInfo().getLookBackward());
		}

		// Remove calculated from start to end, and calculate again.
		int start = Math.max(0, startIndex - lookBackward + 1);
		int end = startIndex - 1;
		for (IndicatorDataList indicatorDataList : indicatorDataLists) {
			for (int index = start; index <= end; index++) {
				indicatorDataList.remove(index);
			}
		}
		for (IndicatorDataList indicatorDataList : indicatorDataLists) {
			for (int index = start; index <= end; index++) {
				indicatorDataList.calculate(index);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PlotData) {
			PlotData plotData = (PlotData) obj;
			return getId().equals(plotData.getId());
		}
		return false;
	}

	/**
	 * Returns the data list in the argument index.
	 *
	 * @param index The index.
	 * @return The data list.
	 */
	public DataList get(int index) {
		return dataLists.get(index);
	}

	/**
	 * Returns a suitable number of bars to scroll or zoom depending on the number
	 * of bars visible.
	 *
	 * @param factor Factor of visible bars to scroll or zoom.
	 * @return The number of bars to scroll or zoom.
	 */
	public int getBarsToScrollOrZoom(double factor) {
		if (factor <= 0 || factor >= 0.2) {
			throw new IllegalArgumentException("Invalid factor " + factor);
		}
		int startIndex = getStartIndex();
		int endIndex = getEndIndex();
		int barsVisible = endIndex - startIndex + 1;
		int barsToScrollOrZoom = (int) (barsVisible * factor);
		if (barsToScrollOrZoom < 1) {
			barsToScrollOrZoom = 1;
		}
		return barsToScrollOrZoom;
	}

	/**
	 * Returns the data of the data list.
	 *
	 * @param dataListIndex The data list index.
	 * @param dataIndex     The index of the data in the data list.
	 * @return The data object.
	 */
	public Data getData(int dataListIndex, int dataIndex) {
		return get(dataListIndex).get(dataIndex);
	}

	/**
	 * Return the data info of the list.
	 *
	 * @param dataList The data list.
	 * @return The data info.
	 */
	public DataInfo getDataInfo(int dataList) {
		return get(dataList).getDataInfo();
	}

	/**
	 * Returns all the data lists.
	 *
	 * @return All the data lists.
	 */
	public List<DataList> getDataLists() {
		return dataLists;
	}

	/**
	 * Return the data size or size of the first data list.
	 *
	 * @return The data size.
	 */
	public int getDataSize() {
		return dataLists.get(0).size();
	}

	/**
	 * @return The description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the end index to plot.
	 *
	 * @return The end index.
	 */
	public int getEndIndex() {
		return endIndex;
	}

	/**
	 * @return The plot data identifier.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Return the instrument info.
	 *
	 * @return The instrument info.
	 */
	public String getInfoInstrument() {
		StringBuilder b = new StringBuilder();
		b.append(getDataInfo(0).getInstrument().getDescription());
		return b.toString();
	}

	/**
	 * Returns the period info.
	 *
	 * @return The period info.
	 */
	public String getInfoPeriod() {
		Period period = getPeriod();
		int size = period.getSize();
		StringBuilder b = new StringBuilder();
		b.append(size);
		b.append(" ");
		b.append(period.getUnit().getShortName());
		if (size > 1) {
			b.append("s");
		}
		return b.toString();
	}

	/**
	 * Returns the time information given the long timestamp.
	 *
	 * @param time The timestamp.
	 * @return The time information.
	 */
	private String getInfoTime(long time) {
		LocalDateTime dateTime =
			LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
		return dateTime.format(getTimeFormatter());
	}

	/**
	 * Returns the time information given an index.
	 *
	 * @param index The index.
	 * @return The time information.
	 */
	public String getInfoTimeFromIndex(int index) {
		long time = getTime(index);
		return getInfoTime(time);
	}

	/**
	 * Returns the time information when the x coordinate is out of range.
	 *
	 * @param index The index out of range.
	 * @return The time information.
	 */
	public String getInfoTimeFromIndexOutOfRange(int index) {
		LocalDateTime dateTime = getTimeFromIndexOutOfRange(index);
		return dateTime.format(getTimeFormatter());
	}

	/**
	 * Returns the maximum index to plot.
	 *
	 * @return The maximum index.
	 */
	public int getMaximumIndex() {
		return maximumIndex;
	}

	/**
	 * Returns the maximum value to plot.
	 *
	 * @return The maximum value.
	 */
	public double getMaximumValue() {
		return maximumValue;
	}

	/**
	 * Returns the minimum index plot.
	 *
	 * @return The minimum index.
	 */
	public int getMinimumIndex() {
		return minimumIndex;
	}

	/**
	 * Returns the minimum value to plot.
	 *
	 * @return The minimum value.
	 */
	public double getMinimumValue() {
		return minimumValue;
	}

	/**
	 * Returns the data period.
	 *
	 * @return The data period.
	 */
	public Period getPeriod() {
		if (isEmpty()) {
			return null;
		}
		return getDataInfo(0).getPeriod();
	}

	/**
	 * Returns the pip scale to use.
	 *
	 * @return The pip scale.
	 * @throws UnsupportedOperationException If the pip scale can not be resolved.
	 */
	public int getPipScale() {
		if (!isEmpty()) {
			int pipScale = -1;
			for (int i = 0; i < size(); i++) {
				pipScale = Math.max(pipScale, get(i).getDataInfo().getPipScale());
			}
			return pipScale;
		}
		throw new UnsupportedOperationException("Pip scale can not be resolved");
	}
	
	public List<DataPlotter> getPlotters() {
		List<DataPlotter> plotters = new ArrayList<>();
		for (DataList dataList : dataLists) {
			plotters.addAll(dataList.getPlotters());
		}
		return plotters;
	}

	/**
	 * Returns the scale to plot this data.
	 *
	 * @return The scale.
	 */
	public PlotScale getPlotScale() {
		return plotScale;
	}

	/**
	 * @return The user properties.
	 */
	public Properties getProperties() {
		return properties;
	}

	/**
	 * Returns the start index to plot.
	 *
	 * @return The start index.
	 */
	public int getStartIndex() {
		return startIndex;
	}

	/**
	 * Returns the tick scale to use.
	 *
	 * @return The tick scale.
	 * @throws UnsupportedOperationException If the tick scale can not be resolved.
	 */
	public int getTickScale() {
		if (!isEmpty()) {
			int tickScale = -1;
			for (int i = 0; i < size(); i++) {
				tickScale = Math.max(tickScale, get(i).getDataInfo().getTickScale());
			}
			return tickScale;
		}
		throw new UnsupportedOperationException("Tick scale can not be resolved");
	}

	/**
	 * Return the time of the given data index.
	 *
	 * @param index The index.
	 * @return The time.
	 */
	public long getTime(int index) {
		return getData(0, index).getTime();
	}

	/**
	 * Return the proper time formatter.
	 *
	 * @return The time formatter.
	 */
	public DateTimeFormatter getTimeFormatter() {
		if (timeFormatter == null) {
			timeFormatter = DateTimeFormatter.ofPattern(getPeriod().getUnit().getPattern());
		}
		return timeFormatter;
	}

	/**
	 * Return the date time from the index.
	 * 
	 * @param index The index.
	 * @return The date time.
	 */
	public LocalDateTime getTimeFromIndex(int index) {
		long time = getTime(index);
		LocalDateTime dateTime =
			LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
		return dateTime;
	}

	/**
	 * Return the time for an index out of range.
	 * 
	 * @param index The index.
	 * @return The time.
	 */
	public LocalDateTime getTimeFromIndexOutOfRange(int index) {
		Period period = getPeriod();
		Unit unit = period.getUnit();
		int periods = period.getSize();
		int firstIndex = 0;
		int lastIndex = getDataSize() - 1;
		long time = 0;
		LocalDateTime dateTime = null;
		if (index < firstIndex) {
			time = getTime(firstIndex);
			dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
			while (index++ < firstIndex) {
				dateTime = addToTime(dateTime, unit, -periods);
			}
		} else if (index > lastIndex) {
			time = getTime(lastIndex);
			dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
			while (index-- > lastIndex) {
				dateTime = addToTime(dateTime, unit, periods);
			}
		}
		return dateTime;
	}

	/**
	 * Returns the volume scale.
	 *
	 * @return The volume scale.
	 * @throws UnsupportedOperationException If the tick scale can not be resolved.
	 */
	public int getVolumeScale() {
		if (!isEmpty()) {
			int volumeScale = -1;
			for (int i = 0; i < size(); i++) {
				volumeScale = Math.max(volumeScale, get(i).getDataInfo().getVolumeScale());
			}
			return volumeScale;
		}
		throw new UnsupportedOperationException("Volume scale can not be resolved");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return getId().hashCode();
	}

	/**
	 * Returns a boolean indicating if this plot data is empty.
	 *
	 * @return A boolean indicating if this plot data is empty.
	 */
	public boolean isEmpty() {
		return dataLists.isEmpty();
	}

	/**
	 * Check whether an index is out of range.
	 *
	 * @param index The index.
	 * @return A boolean.
	 */
	public boolean isOutOfRange(int index) {
		return (index < 0 || index >= getDataSize());
	}

	/**
	 * Return the iterator.
	 *
	 * @return The iterator.
	 * @see java.util.List#iterator()
	 */
	@Override
	public Iterator<DataList> iterator() {
		return dataLists.iterator();
	}

	/**
	 * Merge data lists so all have the same size.
	 */
	private void mergeDataLists() {
		int maxSize = 0;
		for (DataList dataList : dataLists) {
			maxSize = Math.max(maxSize, dataList.size());
		}
		for (DataList dataList : dataLists) {
			int length = dataList.get(0).size();
			for (int i = dataList.size(); i < maxSize; i++) {
				Data data = new Data();
				data.setData(new double[length]);
				data.setValid(false);
				dataList.add(data);
			}
		}
	}

	/**
	 * Mode to the index or period, centering it on screen, with the current number
	 * of shown bars.
	 *
	 * @param index The index to move to.
	 */
	public void move(int index) {
		int minIndex = 0;
		int maxIndex = getDataSize() - 1;
		if (index < minIndex || index > maxIndex) {
			return;
		}
		int indexes = endIndex - startIndex + 1;
		startIndex = index - indexes / 2;
		if (startIndex < minIndex) {
			startIndex = minIndex;
		}
		endIndex = index + indexes / 2;
		if (endIndex > maxIndex) {
			endIndex = maxIndex;
		}
	}

	/**
	 * Removes the data list at the given index.
	 *
	 * @param index The index.
	 * @return The removed data list.
	 */
	public DataList remove(int index) {
		DataList dataList = dataLists.remove(index);
		setStartAndEndIndexes();
		return dataList;
	}

	/**
	 * Scroll a certain number of periods or bars.
	 * <ul>
	 * <li>
	 * If the number of periods to scroll is negative, the <i>startIndex</i> and
	 * <i>endIndex</i> decrease, moving the plot to the right.
	 * </li>
	 * <li>
	 * If the number is positive, reverse the policy.
	 * </li>
	 * <li>
	 * The limit to scroll is to leave at least the minimum number of visible bars.
	 * </li>
	 * </ul>
	 *
	 * @param periods The number of periods to scroll.
	 * @return A boolean indicating whether scroll has been performed.
	 */
	public boolean scroll(int periods) {
		if (isEmpty()) {
			return false;
		}
		if (periods == 0) {
			return false;
		}
		int dataSize = getDataSize();
		if (dataSize == 0) {
			return false;
		}

		// Ensure at least the minimum number of visible bars on the left or on the
		// right. Check bars on the left and scroll left.
		if (maximumIndex == getDataSize() - 1 && periods > 0) {
			int currentVisible = maximumIndex - minimumIndex + 1;
			if (currentVisible - periods < minimumVisibleData) {
				periods = currentVisible - minimumVisibleData;
			}
		}
		// Check bars on the right and scroll right.
		if (minimumIndex == 0 && periods < 0) {
			int currentVisible = maximumIndex - minimumIndex + 1;
			if (currentVisible + periods < minimumVisibleData) {
				periods = -(currentVisible - minimumVisibleData);
			}
		}

		startIndex += periods;
		endIndex += periods;
		return true;
	}

	/**
	 * Scroll to the end of data leaving a minimum visible data margin.
	 *
	 * @return A boolean.
	 */
	public boolean scrollEnd() {
		if (isEmpty()) {
			return false;
		}
		int dataSize = getDataSize();
		if (dataSize == 0) {
			return false;
		}
		int currentVisible = endIndex - startIndex + 1;
		endIndex = dataSize - 1 + minimumVisibleData;
		startIndex = endIndex - currentVisible + 1;
		return true;
	}

	/**
	 * Scroll to the start of data leaving a minimum visible data margin.
	 *
	 * @return A boolean.
	 */
	public boolean scrollStart() {
		if (isEmpty()) {
			return false;
		}
		int dataSize = getDataSize();
		if (dataSize == 0) {
			return false;
		}
		int currentVisible = endIndex - startIndex + 1;
		startIndex = 0 - minimumVisibleData + 1;
		endIndex = startIndex + currentVisible - 1;
		return true;
	}

	/**
	 * @param description The description.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Sets the end index to plot.
	 *
	 * @param endIndex The end index.
	 */
	private void setEndIndex(int endIndex) {
		this.endIndex = endIndex;
	}

	/**
	 * Set the initial start and end indexes to show the argument number of periods.
	 *
	 * @param periods The number of periods to show.
	 */
	public void setIndexes(int periods) {
		if (!isEmpty()) {
			int size = getDataSize();
			int endIndex = size - 1;
			int startIndex = endIndex - periods + 1;
			if (startIndex < 0) {
				startIndex = 0;
			}
			setStartIndex(startIndex);
			setEndIndex(endIndex);
		}
	}

	/**
	 * Set the start and end indexes from the argument plot data.
	 *
	 * @param plotData The source plot data.
	 */
	public void setIndexes(PlotData plotData) {
		setStartIndex(plotData.getStartIndex());
		setEndIndex(plotData.getEndIndex());
	}

	/**
	 * Sets the scale to plot this data.
	 *
	 * @param plotScale The scale.
	 */
	public void setPlotScale(PlotScale plotScale) {
		this.plotScale = plotScale;
	}

	/**
	 * Set the start and end indexes if they are not set.
	 */
	private void setStartAndEndIndexes() {
		if (dataLists.isEmpty()) {
			startIndex = endIndex = Numbers.MIN_INTEGER;
			return;
		}
		if (startIndex != Numbers.MIN_INTEGER && endIndex != Numbers.MIN_INTEGER) {
			return;
		}
		int size = dataLists.get(0).size();
		if (size > startNumberOfBars) {
			endIndex = size - 1;
			startIndex = endIndex - startNumberOfBars + 1;
		} else {
			endIndex = size - 1;
			startIndex = 0;
		}
	}

	/**
	 * Set the start and end indexes from the argument plot data.
	 * 
	 * @param plotData The source plot data.
	 */
	public void setStartAndEndIndexes(PlotData plotData) {
		setStartIndex(plotData.getStartIndex());
		setEndIndex(plotData.getEndIndex());
	}

	/**
	 * Sets the start index to plot.Drag
	 *
	 * @param startIndex The start index t plot.Drag
	 */
	private void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	/**
	 * Set the zero as the minimum value if the minimum is greater than zero.
	 * 
	 * @param zeroAsMinimum A boolean.
	 */
	public void setZeroAsMinimum(boolean zeroAsMinimum) {
		this.zeroAsMinimum = zeroAsMinimum;
	}

	/**
	 * Returns the size of this plot data, the number of data lists.
	 *
	 * @return The number of data lists.
	 */
	public int size() {
		return dataLists.size();
	}

	/**
	 * Returns a string representation of this plota data.
	 *
	 * @return A string representation.
	 */
	@Override
	public String toString() {
		Instrument instrument = null;
		Period period = null;
		List<String> names = new ArrayList<>();
		for (DataList dataList : dataLists) {
			if (instrument == null) {
				instrument = dataList.getDataInfo().getInstrument();
			}
			if (period == null) {
				period = dataList.getDataInfo().getPeriod();
			}
			names.add(dataList.getDataInfo().getName());
		}
		StringBuilder b = new StringBuilder();
		b.append(instrument);
		b.append(", ");
		b.append(period);
		for (String name : names) {
			b.append(", ");
			b.append(name);
		}
		return b.toString();
	}

	/**
	 * Zoom a certain number of periods.
	 * <ul>
	 * <li>
	 * If the number of periods is negative, then zoom out increasing the number of
	 * bars shown by the number of periods.
	 * </li>
	 * <li>
	 * If the number of periods is positive, then zoom in decreasing the number of
	 * bars shown by the number of periods, leaving at least one bar visible, that
	 * is, <i>startIndex</i> and <i>endIndex</i> are the same and in the range of
	 * data.
	 * </li>
	 * <li>
	 * If zoom out and both <i>startIndex</i> and <i>endIndex</i> are in the range
	 * of data, then decrease <i>startIndex</i> and increase <i>endIndex</i> by the
	 * same number of periods.
	 * </li>
	 * <li>
	 * If zoom out and only one of the indexes is out of range, then move the othe
	 * one accordingly.
	 * </li>
	 * <li>
	 * If zoom out and both indexes are out of range, do not zoom.
	 * </li>
	 * </ul>
	 *
	 * @param periods The number of periods or bars to zoom.
	 * @return A boolean indicating whether the zoom was performed.
	 */
	public boolean zoom(int periods) {
		if (isEmpty()) {
			return false;
		}
		if (periods == 0) {
			return false;
		}
		if (get(0).isEmpty()) {
			return false;
		}
		int dataSize = getDataSize();
		boolean zoomOut = (periods < 0);
		boolean zoomIn = !zoomOut;
		periods = Math.abs(periods);

		// Zoom out
		if (zoomOut) {

			// If both indexes are out of range, do not zoom.
			if (startIndex < 0 && endIndex >= dataSize) {
				return false;
			}

			// If both indexes are in the range of data, decrease the start index and
			// increase the end index.
			if (startIndex >= 0 && endIndex < dataSize) {
				startIndex -= periods;
				endIndex += periods;
				return true;
			}

			// If only startIndex is in the range...
			if (startIndex >= 0 && endIndex >= dataSize) {
				startIndex -= periods;
				return true;
			}

			// If only endIndex is in the range...
			if (startIndex < 0 && endIndex < dataSize) {
				endIndex += periods;
				return true;
			}
		}

		// Zoom in: always zoom, with the limit that indexes do not overlap and leaving
		// at least one visible bar.
		if (zoomIn) {

			if (endIndex - startIndex + 1 <= minimumVisibleData) {
				return false;
			}

			// If start and end indexes are the same, do nothing.
			if (startIndex == endIndex - 1) {
				return false;
			}

			// If both indexes are out of range will zoom in the same way as if only the end
			// index is out of range, that is, zoom in the left of the chart maintaining as
			// possible the right blank proportion.
			if (endIndex >= dataSize) {
				double startPeriods = endIndex - startIndex + 1;
				double blankPeriods = endIndex - dataSize;
				double blankFactor = blankPeriods / startPeriods;
				double endPeriods = startPeriods - periods;
				int endBlankPeriods = (int) (endPeriods * blankFactor);
				// Since there were blank periods, leave at list one.
				if (endBlankPeriods == 0) {
					endBlankPeriods = 1;
				}
				endIndex = dataSize + endBlankPeriods;
				startIndex = endIndex - (int) endPeriods + 1;
				checkIndexes();
				return true;
			}

			// If both indexes are in the range zoom both sides
			if (startIndex >= 0 && endIndex < dataSize) {
				startIndex += periods;
				endIndex -= periods;
				checkIndexes();
				return true;
			}

			// If only endIndex is in the range, zoom the right of the chart maintaining as
			// possible the left blank proportion.
			if (startIndex < 0 && endIndex < dataSize) {
				double startPeriods = endIndex - startIndex + 1;
				double blankPeriods = Math.abs(startIndex);
				double blankFactor = blankPeriods / startPeriods;
				double endPeriods = startPeriods - periods;
				int endBlankPeriods = (int) (endPeriods * blankFactor);
				// Since there were blank periods, leave at list one.
				if (endBlankPeriods == 0) {
					endBlankPeriods = 1;
				}
				startIndex = -endBlankPeriods;
				endIndex = startIndex + (int) endPeriods - 1;
				checkIndexes();
				return true;
			}
		}

		return false;
	}
}
