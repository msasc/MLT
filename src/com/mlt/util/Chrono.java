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

package com.mlt.util;

import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;

/**
 * Useful enumeration of chrono values.
 *
 * @author Miquel Sas
 */
public enum Chrono {

	/** Year. */
	YEAR(ChronoUnit.YEARS, ChronoField.YEAR, 1),
	/** Month. */
	MONTH(ChronoUnit.MONTHS, ChronoField.MONTH_OF_YEAR, 1),
	/** Day of month. */
	DAY(ChronoUnit.DAYS, ChronoField.DAY_OF_MONTH, 1),
	/** Hour. */
	HOUR(ChronoUnit.HOURS, ChronoField.HOUR_OF_DAY, 1),
	/** Minute. */
	MINUTE(ChronoUnit.MINUTES, ChronoField.MINUTE_OF_HOUR, 1),
	/** Second of minute. */
	SECOND(ChronoUnit.SECONDS, ChronoField.SECOND_OF_MINUTE, 1),
	/** Tenth of second. */
	TENTH(ChronoUnit.NANOS, ChronoField.NANO_OF_SECOND, 100000000),
	/** Hundredth of second. */
	HUNDREDTH(ChronoUnit.NANOS, ChronoField.NANO_OF_SECOND, 10000000),
	/** Millis of second. */
	MILLI(ChronoUnit.NANOS, ChronoField.NANO_OF_SECOND, 1000000),
	/** Micro of second. */
	MICRO(ChronoUnit.NANOS, ChronoField.NANO_OF_SECOND, 1000),
	/** Nano of second. */
	NANO(ChronoUnit.NANOS, ChronoField.NANO_OF_SECOND, 1);

	/**
	 * Return the string key to get a short name.
	 * 
	 * @param chrono The chrono field.
	 * @return The string ket.
	 */
	public static String getKeyShort(Chrono chrono) {
		switch (chrono) {
		case YEAR:
			return "unitYear";
		case MONTH:
			return "unitMonth";
		case DAY:
			return "unitDay";
		case HOUR:
			return "unitHour";
		case MINUTE:
			return "unitMinute";
		case SECOND:
			return "unitSecond";
		default:
			break;
		}
		throw new IllegalArgumentException();
	}

	/** Temporal unit used to increase/decrease a LocalDateTime. */
	private TemporalUnit temporalUnit;
	/** Temporal field used to access fields of a LocalDateTime. */
	private TemporalField temporalField;
	/** Multiplier of the unit. */
	private int multiplier;

	/**
	 * @param temporalUnit
	 * @param temporalField
	 */
	private Chrono(TemporalUnit temporalUnit, TemporalField temporalField, int multiplier) {
		this.temporalUnit = temporalUnit;
		this.temporalField = temporalField;
		this.multiplier = multiplier;
	}

	/**
	 * Return the temporal unit.
	 * 
	 * @return The temporal unit.
	 */
	public TemporalUnit getTemporalUnit() {
		return temporalUnit;
	}

	/**
	 * Return the temporal field.
	 * 
	 * @return The temporal field.
	 */
	public TemporalField getTemporalField() {
		return temporalField;
	}

	/**
	 * Return the multiplier.
	 * 
	 * @return The multiplier.
	 */
	public int getMultiplier() {
		return multiplier;
	}

}
