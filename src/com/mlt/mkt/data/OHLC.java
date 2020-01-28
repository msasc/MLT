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

/**
 * Centralizes function on bars or candlesticks related data, that is, data
 * structures that have its first values are open, high, low, close and
 * eventually volume.
 *
 * @author Miquel Sas
 */
public class OHLC {

	/** Open index. */
	public static final int OPEN = 0;
	/** High index. */
	public static final int HIGH = 1;
	/** Low index. */
	public static final int LOW = 2;
	/** Close index. */
	public static final int CLOSE = 3;
	/** Volume index. */
	public static final int VOLUME = 4;
	/** Bar/Candlestick (OHLCV) size. */
	public static final int SIZE = 5;

	/**
	 * @param data The data candlestick.
	 * @return The body value (abs value).
	 */
	public static double getBody(Data data) {
		double open = getOpen(data);
		double close = getClose(data);
		double body = Math.abs(open - close);
		return body;
	}

	/**
	 * @param data Tha data candlestick.
	 * @return The center of the body.
	 */
	public static double getBodyCenter(Data data) {
		double open = getOpen(data);
		double close = getClose(data);
		return (open + close) / 2;
	}

	/**
	 * @param data The data candlestick.
	 * @return The factor body vs range (raw value from 0:1).
	 */
	public static double getBodyFactor(Data data) {
		double body = getBody(data);
		double range = getRange(data);
		if (body > range) {
			body = range;
		}
		double factor = (range != 0 ? body / range : 0);
		return factor;
	}

	/**
	 * @param data The data candlestick.
	 * @return The relative position of the body.
	 */
	public static double getBodyPosition(Data data) {
		double center = getBodyCenter(data);
		double range = getRange(data);
		double low = getLow(data);
		double pos = (range != 0 ? (center - low) / range : 0);
		return pos;
	}

	/**
	 * @param data The data candlestick.
	 * @return The center of the candle.
	 */
	public static double getCenter(Data data) {
		double high = getHigh(data);
		double low = getLow(data);
		double center = (high + low) / 2;
		return center;
	}

	/**
	 * @param data The data candlestick.
	 * @return The close value.
	 */
	public static double getClose(Data data) {
		return data.getValue(CLOSE);
	}

	/**
	 * @param data The data candlestick.
	 * @return The high value.
	 */
	public static double getHigh(Data data) {
		return data.getValue(HIGH);
	}

	/**
	 * @param data The data candlestick.
	 * @return The low value.
	 */
	public static double getLow(Data data) {
		return data.getValue(LOW);
	}

	/**
	 * @param data The data candlestick.
	 * @return The open value.
	 */
	public static double getOpen(Data data) {
		return data.getValue(OPEN);
	}

	/**
	 * @param data The data candlestick.
	 * @return The high value (abs value).
	 */
	public static double getRange(Data data) {
		double high = getHigh(data);
		double low = getLow(data);
		return Math.abs(high - low);
	}

	/**
	 * @param dataCurr  The current data candlestick.
	 * @param dataPrev The previous data candlestick.
	 * @return The relative position of two consecutive candles.
	 */
	public static double getRelativePosition(Data dataCurr, Data dataPrev) {
		double current = getWeightedClosePrice(dataCurr);
		double previous = getWeightedClosePrice(dataPrev);
		double factor = (current / previous) - 1;
		return factor;
	}

	/**
	 * @param dataCurr  The current data candlestick.
	 * @param dataPrev The previous data candlestick.
	 * @return The relative ranges of two consecutive candles.
	 */
	public static double getRelativeRange(Data dataCurr, Data dataPrev) {
		double current = getRange(dataCurr);
		double previous = getRange(dataPrev);
		double factor = (previous == 0 ? 0 : (current / previous) - 1);
		return factor;
	}

	/**
	 * @param dataCurr  The current data candlestick.
	 * @param dataPrev The previous data candlestick.
	 * @return The relative bodies of two consecutive candles.
	 */
	public static double getRelativeBody(Data dataCurr, Data dataPrev) {
		double current = getBody(dataCurr);
		double previous = getBody(dataPrev);
		double factor = (previous == 0 ? 0 : (current / previous) - 1);
		return factor;
	}

	/**
	 * @param data The data candlestick.
	 * @return The sign as a weighted value between -1:1
	 */
	public static double getSign(Data data) {
		double sign = 0;
		double open = getOpen(data);
		double close = getClose(data);
		double factor = getBodyFactor(data);
		if (close > open) {
			sign = 1;
		}
		if (close < open) {
			sign = -1;
		}
		sign *= factor;
		return sign;
	}

	/**
	 * @param data The data element.
	 * @return The weighted close price (H + L + (2*C)) / 4
	 */
	public static double getWeightedClosePrice(Data data) {
		return (data.getValue(HIGH) + data.getValue(LOW) + (2 * data.getValue(CLOSE))) / 4;
	}

	/**
	 * @param data The data bar.
	 * @return A boolean indicating whether the OHLC is flat.
	 */
	public static boolean isFlat(Data data) {
		double open = data.getValue(OPEN);
		double high = data.getValue(HIGH);
		double low = data.getValue(LOW);
		double close = data.getValue(CLOSE);
		return (open == high && open == low && open == close);
	}

	/**
	 * @param data The data.
	 * @return A boolean indicating if this OHLC is bearish.
	 */
	public static boolean isBearish(Data data) {
		return !isBullish(data);
	}

	/**
	 * @param data The data.
	 * @return A boolean indicating if this bar is bullish.
	 */
	public static boolean isBullish(Data data) {
		return data.getValue(CLOSE) >= data.getValue(OPEN);
	}

}
