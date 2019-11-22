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

package app.mlt.plaf.statistics;

import com.mlt.mkt.data.Data;

/**
 * Candlestick utilities.
 *
 * @author Miquel Sas
 */
public class Candles {
	/**
	 * @param candle The data candlestick.
	 * @return The open value.
	 */
	public static double open(Data candle) {
		return candle.getValue(Data.OPEN);
	}

	/**
	 * @param candle The data candlestick.
	 * @return The high value.
	 */
	public static double high(Data candle) {
		return candle.getValue(Data.HIGH);
	}

	/**
	 * @param candle The data candlestick.
	 * @return The low value.
	 */
	public static double low(Data candle) {
		return candle.getValue(Data.LOW);
	}

	/**
	 * @param candle The data candlestick.
	 * @return The close value.
	 */
	public static double close(Data candle) {
		return candle.getValue(Data.CLOSE);
	}

	/**
	 * @param candle The data candlestick.
	 * @return The high value (abs value).
	 */
	public static double range(Data candle) {
		double high = high(candle);
		double low = low(candle);
		return Math.abs(high - low);
	}

	/**
	 * @param candle The data candlestick.
	 * @return The body value (abs value).
	 */
	public static double body(Data candle) {
		double open = open(candle);
		double close = close(candle);
		double body = Math.abs(open - close);
		return body;
	}

	/**
	 * @param candle Tha data candlestick.
	 * @return The center of the body.
	 */
	public static double bodyCenter(Data candle) {
		double open = open(candle);
		double close = close(candle);
		return (open + close) / 2;
	}

	/**
	 * @param candle The data candlestick.
	 * @return The factor body vs range (raw value from 0:1).
	 */
	public static double bodyFactor(Data candle) {
		double body = body(candle);
		double range = range(candle);
		if (body > range) {
			body = range;
		}
		double factor = (range != 0 ? body / range : 1);
		return factor;
	}
	
	/**
	 * @param candle The data candlestick.
	 * @return The relative position of the body.
	 */
	public static double bodyPos(Data candle) {
		double center = bodyCenter(candle);
		double range = range(candle);
		double low = low(candle);
		double pos = (range != 0 ? (center - low) / range : 1);
		return pos;
	}
	
	/**
	 * @param candle The data candlestick.
	 * @return The sign as a normalized value -1:1
	 */
	public static double sign(Data candle) {
		double sign = 0;
		double open = open(candle);
		double close = close(candle);
		double factor = bodyFactor(candle);
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
	 * @param candle The data candlestick.
	 * @return The center of the candle.
	 */
	public static double center(Data candle) {
		double high = high(candle);
		double low = low(candle);
		double center = (high + low) / 2;
		return center;
	}

	/**
	 * @param candleCurrent  The current data candlestick.
	 * @param candlePrevious The previous data candlestick.
	 * @return The center factor between the two candles.
	 */
	public static double centerFactor(Data candleCurrent, Data candlePrevious) {
		double current = center(candleCurrent);
		double previous = center(candlePrevious);
		double factor = (current / previous) - 1;
		return factor;
	}
}
