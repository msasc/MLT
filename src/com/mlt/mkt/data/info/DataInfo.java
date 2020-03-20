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
package com.mlt.mkt.data.info;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import com.mlt.mkt.chart.plotter.DataPlotter;
import com.mlt.mkt.data.Data;
import com.mlt.mkt.data.DataList;
import com.mlt.mkt.data.Instrument;
import com.mlt.mkt.data.Period;
import com.mlt.mkt.data.PlotData;
import com.mlt.util.Formats;

/**
 * Base information that describes data in a data list.
 *
 * @author Miquel Sas
 */
public class DataInfo {

	/**
	 * Information formatter.
	 */
	public interface Formatter {
		/**
		 * Return the data info as a string.
		 * 
		 * @param info      The data info, that has scales and other necessary
		 *                  parameters.
		 * @param plotData  The plot data.
		 * @param listIndex The data list index.
		 * @param index     The data index.
		 * @return The information to display.
		 */
		String getInfoData(DataInfo info, PlotData plotData, int listIndex, int index);
	}

	/**
	 * Default formatter.
	 */
	private static class DefaultFormatter implements Formatter {
		@Override
		public String getInfoData(DataInfo info, PlotData plotData, int listIndex, int index) {
			DataList dataList = plotData.get(listIndex);
			Data data = dataList.get(index);
			if (!data.isValid()) {
				return "";
			}
			StringBuilder b = new StringBuilder();
			int count = info.getOutputCount();
			for (int i = 0; i < count; i++) {
				OutputInfo output = info.getOutput(i);
				if (output.getPlotter() != null && !output.getPlotter().isPlot()) {
					continue;
				}
				int outputIndex = output.getIndex();
				if (b.length() > 0) {
					b.append(", ");
				}
				String shortName = output.getShortName();
				if (shortName != null) {
					b.append(shortName);
					b.append(": ");
				}
				b.append(
					Formats.fromDouble(
						data.getValue(outputIndex),
						info.getTickScale(),
						Locale.getDefault()));
			}
			return b.toString();
		}
	}

	/** Name. */
	private String name;
	/** A title to use in list or tool tips. */
	private String title;
	/** An optional long description that completely describes the data. */
	private String description;

	/** Instrument of data if applicable. */
	private Instrument instrument;
	/** Period. */
	private Period period;

	/**
	 * The pip scale used for the data in this data list. If -1, take it from the
	 * instrument.
	 */
	private int pipScale = -1;
	/**
	 * The tick scale used for the data in this data list. If -1, take it from the
	 * instrument.
	 */
	private int tickScale = -1;

	/** The list of informations about outputs. */
	private List<OutputInfo> outputs = new ArrayList<>();
	/** Map of output indexes. */
	private Map<String, Integer> mapIndexes = new HashMap<>();
	/** Information formatter. */
	private Formatter formatter;

	/**
	 * Constructor.
	 */
	public DataInfo() {
		super();
		formatter = new DefaultFormatter();
	}

	/**
	 * Constructor.
	 * 
	 * @param formatter The information formatter.
	 */
	public DataInfo(Formatter formatter) {
		super();
		this.formatter = formatter;
	}

	/**
	 * Add the output and map the index to the name.
	 *
	 * @param output The output.
	 */
	private void addOutput(OutputInfo output) {
		outputs.add(output);
		mapIndexes.put(output.getName(), output.getIndex());
	}

	/**
	 * Adds an output to the list of outputs.
	 *
	 * @param name        The output name.
	 * @param shortName   The output short name.
	 * @param index       The index in the data.
	 * @param description The output description.
	 */
	public void addOutput(String name, String shortName, int index, String description) {
		addOutput(name, shortName, index, description, null);
	}

	/**
	 * Adds an output to the list of outputs.
	 *
	 * @param name        The output name.
	 * @param shortName   The output short name.
	 * @param index       The index in the data.
	 * @param description The output description.
	 * @param plotter     The data plotter.
	 */
	public void addOutput(
		String name,
		String shortName,
		int index,
		String description,
		DataPlotter plotter) {
		addOutput(new OutputInfo(name, shortName, index, description, plotter));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DataInfo) {
			DataInfo info = (DataInfo) obj;
			if (!getName().equals(info.getName())) {
				return false;
			}
			if (!getInstrument().equals(info.getInstrument())) {
				return false;
			}
			if (!getPeriod().equals(info.getPeriod())) {
				return false;
			}
			if (getPipScale() != info.getPipScale()) {
				return false;
			}
			if (getTickScale() != info.getTickScale()) {
				return false;
			}
			int count = getOutputCount();
			if (count != info.getOutputCount()) {
				return false;
			}
			for (int i = 0; i < count; i++) {
				if (!getOutput(i).equals(info.getOutput(i))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Returns the long description.
	 *
	 * @return The long description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns a display information of the given data.
	 *
	 * @param dataList The data list.
	 * @param index    The index.
	 * @return The display info.
	 */
	public String getInfoData(PlotData plotData, int listIndex, int index) {
		return formatter.getInfoData(this, plotData, listIndex, index);
	}

	/**
	 * Returns the data instrument.
	 *
	 * @return The data instrument.
	 */
	public Instrument getInstrument() {
		return instrument;
	}

	/**
	 * Returns the identifier or name.
	 *
	 * @return The unique identifier or name, like for instance <b>SMA</b>.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the output at the given index.
	 *
	 * @param index The output index.
	 * @return The output.
	 */
	public OutputInfo getOutput(int index) {
		if (index < outputs.size()) {
			return outputs.get(index);
		}
		return null;
	}

	/**
	 * Returns the output info by data index or null if not found.
	 *
	 * @param index The data index.
	 * @return Teh output info.
	 */
	public OutputInfo getOutputByDataIndex(int index) {
		for (OutputInfo output : outputs) {
			if (output.getIndex() == index) {
				return output;
			}
		}
		return null;
	}

	/**
	 * Returns the number of outputs.
	 *
	 * @return The number of outputs.
	 */
	public int getOutputCount() {
		return outputs.size();
	}

	/**
	 * Returns the output index given the name of the output.
	 *
	 * @param name The name of the output.
	 * @return The output index or -1 if the name is not valid.
	 */
	public int getOutputIndex(String name) {
		Integer index = mapIndexes.get(name);
		if (index == null) {
			return -1;
		}
		return index;
	}

	/**
	 * Returns the data period.
	 *
	 * @return The data period.
	 */
	public Period getPeriod() {
		return period;
	}

	/**
	 * Returns the pip scale to use.
	 *
	 * @return The pip scale.
	 * @throws UnsupportedOperationException If the pip scale can not be resolved.
	 */
	public int getPipScale() {
		if (pipScale >= 0) {
			return pipScale;
		}
		if (instrument != null) {
			return instrument.getPipScale();
		}
		throw new UnsupportedOperationException("Pip scale can not be resolved");
	}

	/**
	 * Returns the tick scale to use.
	 *
	 * @return The tick scale.
	 * @throws UnsupportedOperationException If the tick scale can not be resolved.
	 */
	public int getTickScale() {
		if (tickScale >= 0) {
			return tickScale;
		}
		if (instrument != null) {
			return instrument.getTickScale();
		}
		throw new UnsupportedOperationException("Tick scale can not be resolved");
	}

	/**
	 * Returns the title to use in list or tool tips.
	 *
	 * @return The title to use in list or tool tips.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Returns the volume scale or -1 if it can not be resolved.
	 *
	 * @return The volume scale.
	 */
	public int getVolumeScale() {
		if (instrument != null) {
			return instrument.getVolumeScale();
		}
		return -1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 67 * hash + Objects.hashCode(this.name);
		hash = 67 * hash + Objects.hashCode(this.instrument);
		hash = 67 * hash + Objects.hashCode(this.period);
		hash = 67 * hash + this.pipScale;
		hash = 67 * hash + this.tickScale;
		return hash;
	}

	/**
	 * Sets the long description.
	 *
	 * @param description The long description.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Sets the data instrument.
	 *
	 * @param instrument The data instrument.
	 */
	public void setInstrument(Instrument instrument) {
		this.instrument = instrument;
	}

	/**
	 * Sets the unique identifier or name, like for instance <b>SMA</b>.
	 *
	 * @param id The unique identifier or name, like for instance <b>SMA</b>.
	 */
	public void setName(String id) {
		this.name = id;
	}

	/**
	 * Sets the data period.
	 *
	 * @param period The data period.
	 */
	public void setPeriod(Period period) {
		this.period = period;
	}

	/**
	 * Sets the pip scale for the data in this data list. If -1, retrieve it from
	 * the instrument.
	 *
	 * @param pipScale The pip scale.
	 */
	public void setPipScale(int pipScale) {
		this.pipScale = pipScale;
	}

	/**
	 * Sets the tick or minimum value scale.
	 *
	 * @param tickScale The tick or minimum value scale.
	 */
	public void setTickScale(int tickScale) {
		this.tickScale = tickScale;
	}

	/**
	 * Sets the title to use in list or tool tips.
	 *
	 * @param title The title to use in list or tool tips.
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Returns a string representation of this data info.
	 *
	 * @return A string representation.
	 */
	@Override
	public String toString() {
		StringBuilder info = new StringBuilder();
		info.append(getInstrument());
		info.append(", ");
		info.append(getPeriod());
		info.append(", ");
		info.append(getName());
		return info.toString();
	}
}
