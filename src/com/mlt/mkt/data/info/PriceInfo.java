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
package com.mlt.mkt.data.info;

import com.mlt.mkt.data.OHLC;
import com.mlt.mkt.data.Instrument;
import com.mlt.mkt.data.Period;

/**
 * Data information for prices.
 * 
 * @author Miquel Sas
 */
public class PriceInfo extends DataInfo {

	/**
	 * Constructor assigning instrument and period.
	 * 
	 * @param instrument The instrument.
	 * @param period     The period.
	 */
	public PriceInfo(Instrument instrument, Period period) {
		super();
		setInstrument(instrument);
		setName(instrument.getId());
		setDescription(instrument.getDescription());
		setPeriod(period);
		addOutput("Open", "O", OHLC.OPEN, "Open data value");
		addOutput("High", "H", OHLC.HIGH, "High data value");
		addOutput("Low", "L", OHLC.LOW, "Low data value");
		addOutput("Close", "C", OHLC.CLOSE, "Close data value");
	}
}
