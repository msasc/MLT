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
package com.mlt.mkt.chart.plotter;

import com.mlt.desktop.control.Canvas;
import java.awt.Color;

import com.mlt.mkt.chart.DataContext;
import com.mlt.mkt.data.Data;
import com.mlt.mkt.data.DataList;
import com.mlt.mkt.data.info.DataInfo;
import com.mlt.mkt.data.info.OutputInfo;

/**
 * Base class for data plotters of timed data.
 * 
 * @author Miquel Sas
 */
public abstract class DataPlotter extends Plotter {

	/**
	 * Indexes within the data element used by the plotter.
	 */
	private int[] indexes;
	/**
	 * The color used for a bearish line bar candle is an even period. For periods lower than day, the color changes
	 * when the day changes, for the day period when the week changes, for the week the month and for the month of the
	 * year.
	 */
	private Color colorBearishEven = new Color(160, 32, 32);
	/**
	 * The color used for a bearish line bar candle is an odd period. For periods lower than day, the color changes when
	 * the day changes, for the day period when the week changes, for the week the month and for the month of the year.
	 */
	private Color colorBearishOdd = new Color(160, 32, 32);
	// private Color colorBearishOdd = new Color(25, 25, 25);
	/**
	 * The color used for a bullish line/bar/candle in an even period. For periods lower than day, the color changes
	 * when the day changes, for the day period when the week changes, for the week the month and for the month of the
	 * year.
	 */
	private Color colorBullishEven = new Color(32, 160, 32);
	/**
	 * The color used for a bullish line/bar/candle in an odd period. For periods lower than day, the color changes when
	 * the day changes, for the day period when the week changes, for the week the month and for the monthof the year.
	 */
	private Color colorBullishOdd = new Color(32, 160, 32);
	// private Color colorBullishOdd = new Color(215, 215, 215);

	/**
	 * A boolean to control if the plotter should plot, thus allowing to hide/show plot actions.
	 */
	private boolean plot = true;
	/**
	 * Data width factor: width of data vs width of period.
	 */
	private double dataWidthFactor = 0.7;

	/**
	 * Constructor.
	 */
	public DataPlotter() {
		super();
	}

	/**
	 * Return the data width.
	 * 
	 * @param dc The data context.
	 * @return The data width.
	 */
	public double getDataWidth(DataContext dc) {
		double periodWidth = dc.getPeriodWidth();
		if (periodWidth <= 4) {
			return 1;
		}
		return periodWidth * dataWidthFactor;
	}

	/**
	 * Return the data margin.
	 * 
	 * @param dc The data context.
	 * @return The margin.
	 */
	public double getDataMargin(DataContext dc) {
		return (dc.getPeriodWidth() - getDataWidth(dc)) / 2;
	}

	/**
	 * Check if the plotter should plot.
	 * 
	 * @return A boolean.
	 */
	public boolean isPlot() {
		return plot;
	}

	/**
	 * Set the data width factor.
	 * 
	 * @param dataWidthFactor The data width factor.
	 */
	public void setDataWidthFactor(double dataWidthFactor) {
		this.dataWidthFactor = dataWidthFactor;
	}

	/**
	 * Set if the plotter should plot.
	 * 
	 * @param plot A boolean.
	 */
	public void setPlot(boolean plot) {
		this.plot = plot;
	}

	/**
	 * Return the index (the first).
	 * 
	 * @return The first index.
	 */
	public int getIndex() {
		return indexes[0];
	}

	/**
	 * Set an unique index.
	 * 
	 * @param index The index.
	 */
	public void setIndex(int index) {
		setIndexes(new int[] { index });
	}

	/**
	 * Returns the indexes within the data element used by the plotter.
	 * 
	 * @return The indexes within the data element used by the plotter.
	 */
	public int[] getIndexes() {
		return indexes;
	}

	/**
	 * Returns the indexes to apply to the data item. By default, all data values.
	 * 
	 * @param data The data item.
	 * @return The indexes.
	 */
	public int[] getIndexes(Data data) {
		if (indexes == null) {
			indexes = new int[data.size()];
			for (int i = 0; i < data.size(); i++) {
				indexes[i] = i;
			}
		}
		return indexes;
	}

	/**
	 * Returns the list of values given the data element.
	 * 
	 * @param data The data item.
	 * @return The list of values.
	 */
	public double[] getValues(Data data) {
		int[] indexes = getIndexes(data);
		double[] values = new double[indexes.length];
		for (int i = 0; i < indexes.length; i++) {
			values[i] = data.getValue(indexes[i]);
		}
		return values;
	}

	/**
	 * Set the indexes within the data element used by the plotter.
	 * 
	 * @param indexes The indexes within the data element used by the plotter.
	 */
	public final void setIndexes(int[] indexes) {
		this.indexes = indexes;
	}

	/**
	 * Do plot.
	 * 
	 * @param ctx        The canvas graphics context.
	 * @param dataList   The data list to plot.
	 * @param startIndex The start index.
	 * @param endIndex   The end index.
	 */
	public abstract void plot(Canvas.Context ctx, DataList dataList, int startIndex, int endIndex);

	/**
	 * Sets the color used for a bearish line bar candle is an even period. For periods lower than day, the color
	 * changes when the day changes, for the day period when the week changes, for the week of the month and for the
	 * month of the year.
	 * 
	 * @param colorBearishEven The color used for a bearish line bar candle is an even period.
	 */
	public void setColorBearishEven(Color colorBearishEven) {
		this.colorBearishEven = colorBearishEven;
	}

	/**
	 * Sets the color used for a bearish line bar candle is an odd period. For periods lower than day, the color changes
	 * when the day changes, for the day period when the week changes, for the week the month and for the month of the
	 * year.
	 * 
	 * @param colorBearishOdd The color used for a bearish line bar candle is an odd period.
	 */
	public void setColorBearishOdd(Color colorBearishOdd) {
		this.colorBearishOdd = colorBearishOdd;
	}

	/**
	 * Sets the color used for a bullish line/bar/candle in an even period. For periods lower than day, the color
	 * changes when the day changes, for the day period when the week changes, for the week the month and for the month
	 * of the year.
	 * 
	 * @param colorBullishEven The color used for a bullish line/bar/candle in an even period.
	 */
	public void setColorBullishEven(Color colorBullishEven) {
		this.colorBullishEven = colorBullishEven;
	}

	/**
	 * Sets the color used for a bullish line/bar/candle in an odd period. For periods lower than day, the color changes
	 * when the day changes, for the day period when the week changes, for the week the month and for the month of the
	 * year.
	 * 
	 * @param colorBullishOdd The color used for a bullish line/bar/candle in an odd period.
	 */
	public void setColorBullishOdd(Color colorBullishOdd) {
		this.colorBullishOdd = colorBullishOdd;
	}

	/**
	 * Returns the color used for a bearish line bar candle is an even period. For periods lower than day, the color
	 * changes when the day changes, for the day period when the week changes, for the week the month and for the month
	 * of the year.
	 * 
	 * @return the colorBearishEven The color used for a bearish line bar candle is an even period.
	 */
	public Color getColorBearishEven() {
		return colorBearishEven;
	}

	/**
	 * Returns the color used for a bearish line bar candle is an odd period. For periods lower than day, the color
	 * changes when the day changes, for the day period when the week changes, for the week the month and for the month
	 * of the year.
	 * 
	 * @return the colorBearishOdd The color used for a bearish line bar candle is an odd period.
	 */
	public Color getColorBearishOdd() {
		return colorBearishOdd;
	}

	/**
	 * Returns the color used for a bullish line/bar/candle in an even period. For periods lower than day, the color
	 * changes when the day changes, for the day period when the week changes, for the week the month and for the month
	 * of the year.
	 * 
	 * @return the colorBullishEven The color used for a bullish line/bar/candle in an even period.
	 */
	public Color getColorBullishEven() {
		return colorBullishEven;
	}

	/**
	 * Returns the color used for a bullish line/bar/candle in an odd period. For periods lower than day, the color
	 * changes when the day changes, for the day period when the week changes, for the week the month and for the month,
	 * the year.
	 * 
	 * @return the colorBullishOdd The color used for a bullish line/bar/candle in an odd period.
	 */
	public Color getColorBullishOdd() {
		return colorBullishOdd;
	}

	/**
	 * Returns a string representation of this plotter and the data it plots.
	 * 
	 * @param info The data info of the data list the plotter plots.
	 * @return A string representation.
	 */
	public String toString(DataInfo info) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < indexes.length; i++) {
			String sep = (i == 0 ? " " : ", ");
			int index = indexes[i];
			OutputInfo output = info.getOutputByDataIndex(index);
			if (output != null) {
				if (b.length() > 0) {
					b.append(sep);
				}
				b.append(output);
			}
		}
		return b.toString();
	}
}
