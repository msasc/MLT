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
package com.mlt.mkt.chart;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;

import com.mlt.util.Numbers;

/**
 * Time periods used in the horizontal axis.
 *
 * @author Miquel Sas
 */
enum TimePeriod {
	FIVE_MINUTES(1000L * 60L * 5L, "HH:mm"), FIFTEEN_MINUTES(1000L * 60L * 15L, "HH:mm"),
	THIRTY_MINUTES(1000L * 60L * 30L, "HH:mm"), ONE_HOUR(1000L * 60L * 60L, "HH:mm"),
	THREE_HOURS(1000L * 60L * 60L * 3L, "HH:mm"), SIX_HOURS(1000L * 60L * 60L * 6L, "HH:mm"),
	TWELVE_HOURS(1000L * 60L * 60L * 12L, "HH:mm"), DAY(1000L * 60L * 60L * 24L, "yyyy-MM-dd"),
	WEEK(1000L * 60L * 60L * 24L * 7L, "yyyy-MM-dd"), MONTH(1000L * 60L * 60L * 24L * 30L, "yyyy-MM"),
	QUARTER(1000L * 60L * 60L * 24L * 90L, "yyyy-MM"), YEAR(1000L * 60L * 60L * 24L * 365L, "yyyy"),
	QUINQUENIUM(1000L * 60L * 60L * 24L * 365L * 5L, "yyyy"), DECADE(1000L * 60L * 60L * 24L * 365L * 10L, "yyyy");

	/** Approximate epoch time in millis. */
	private long millis;
	/** Pattern. */
	private String pattern;
	/** Sample to calculate sizes. */
	private String sample;
	/** Time formatter. */
	private DateTimeFormatter formatter;

	/**
	 * Constructor.
	 *
	 * @param millis  Approximate epoch time in millis.
	 * @param pattern Pattern.
	 */
	TimePeriod(long millis, String pattern) {
		this.millis = millis;
		this.pattern = pattern;
	}

	/**
	 * Return the time.
	 *
	 * @return The time.
	 */
	long getMillis() {
		return millis;
	}

	/**
	 * Return the pattern.
	 *
	 * @return The pattern.
	 */
	String getPattern() {
		return pattern;
	}

	/**
	 * Build and return the sample.
	 *
	 * @return The ssample string.
	 */
	String getSample() {
		if (sample == null) {
			StringBuilder b = new StringBuilder();
			for (int i = 0; i < pattern.length(); i++) {
				char c = pattern.charAt(i);
				if (c == '-' || c == ':') {
					b.append(c);
				} else {
					b.append('0');
				}
			}
			sample = b.toString();
		}
		return sample;
	}

	/**
	 * Build and return the formatter.
	 *
	 * @return The formatter.
	 */
	DateTimeFormatter getFormatter() {
		if (formatter == null) {
			formatter = DateTimeFormatter.ofPattern(getPattern());
		}
		return formatter;
	}

	/**
	 * Returns the time string to plot.
	 *
	 * @param time The time in millis.
	 * @return The string to plot.
	 */
	String getStringToPlot(long time) {
		ZoneId zone = ZoneId.systemDefault();
		LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), zone);
		return dateTime.format(getFormatter());
	}

	/**
	 * Check if the given time is the start time of this time period.
	 *
	 * @param timeCurrent  The current time to check.
	 * @param timePrevious The previous time to check.
	 */
	boolean isStartTimePeriod(long timeCurrent, long timePrevious) {

		ZoneId zone = ZoneId.systemDefault();
		LocalDateTime current = LocalDateTime.ofInstant(Instant.ofEpochMilli(timeCurrent), zone);
		LocalDateTime previous = LocalDateTime.ofInstant(Instant.ofEpochMilli(timePrevious), zone);

		switch (this) {
		case FIVE_MINUTES:
			if (current.getMinute() == previous.getMinute()) {
				return false;
			}
			if (Numbers.remainder(current.getMinute(), 5) == 0) {
				return true;
			}
			return false;
		case FIFTEEN_MINUTES:
			if (current.getMinute() == previous.getMinute()) {
				return false;
			}
			if (Numbers.remainder(current.getMinute(), 15) == 0) {
				return true;
			}
			return false;
		case THIRTY_MINUTES:
			if (current.getMinute() == previous.getMinute()) {
				return false;
			}
			if (Numbers.remainder(current.getMinute(), 30) == 0) {
				return true;
			}
			return false;
		case ONE_HOUR:
			if (current.getHour() == previous.getHour()) {
				return false;
			}
			return true;
		case THREE_HOURS:
			if (current.getHour() == previous.getHour()) {
				return false;
			}
			if (Numbers.remainder(current.getHour(), 3) == 0) {
				return true;
			}
			return false;
		case SIX_HOURS:
			if (current.getHour() == previous.getHour()) {
				return false;
			}
			if (Numbers.remainder(current.getHour(), 6) == 0) {
				return true;
			}
			return false;
		case TWELVE_HOURS:
			if (current.getHour() == previous.getHour()) {
				return false;
			}
			if (Numbers.remainder(current.getHour(), 12) == 0) {
				return true;
			}
			return false;
		case DAY:
			if (current.getDayOfMonth() == previous.getDayOfMonth()) {
				return false;
			}
			return true;
		case WEEK:
			int currentWeek = current.get(ChronoField.ALIGNED_WEEK_OF_YEAR);
			int previousWeek = previous.get(ChronoField.ALIGNED_WEEK_OF_YEAR);
			if (currentWeek == previousWeek) {
				return false;
			}
			return true;
		case MONTH:
			if (current.getMonth() == previous.getMonth()) {
				return false;
			}
			return true;
		case QUARTER:
			if (current.getMonth() == previous.getMonth()) {
				return false;
			}
			if (current.getMonthValue() == 1) {
				return true;
			}
			if (current.getMonthValue() == 4) {
				return true;
			}
			if (current.getMonthValue() == 7) {
				return true;
			}
			if (current.getMonthValue() == 10) {
				return true;
			}
			return false;
		case YEAR:
			if (current.getYear() == previous.getYear()) {
				return false;
			}
			return true;
		case QUINQUENIUM:
			if (current.getYear() == previous.getYear()) {
				return false;
			}
			if (Numbers.remainder(current.getYear(), 5) == 0) {
				return true;
			}
			return false;
		case DECADE:
			if (current.getYear() == previous.getYear()) {
				return false;
			}
			if (Numbers.remainder(current.getYear(), 10) == 0) {
				return true;
			}
			return false;
		default:
			return false;
		}
	}
}
