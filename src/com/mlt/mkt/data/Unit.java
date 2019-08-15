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
package com.mlt.mkt.data;

import com.mlt.util.Resources;

/**
 * Units used to define periods of aggregate incoming quotes.
 *
 * @author Miquel Sas
 */
public enum Unit {
	MILLISECOND("MS"), SECOND("SC"), MINUTE("MN"), HOUR("HR"), DAY("DY"), WEEK("WK"), MONTH("MT"), YEAR("YR");

	/**
	 * Returns the unit of the given id.
	 *
	 * @param id The unit id.
	 * @return The unit.
	 */
	public static Unit parseId(String id) {
		Unit[] units = values();
		for (Unit unit : units) {
			if (unit.getId().equals(id.toUpperCase())) {
				return unit;
			}
		}
		throw new IllegalArgumentException("Invalid unit id: " + id);
	}

	/** 2 char id. */
	private String id;

	/**
	 * Constructor.
	 *
	 * @param id Two char id.
	 */
	private Unit(String id) {
		this.id = id;
	}

	/**
	 * Returns the two char id.
	 *
	 * @return The id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the short description.
	 *
	 * @return The short description.
	 */
	public String getShortName() {
		switch (this) {
		case MILLISECOND:
			return Resources.getText("unitMillisecond");
		case SECOND:
			return Resources.getText("unitSecond");
		case MINUTE:
			return Resources.getText("unitMinute");
		case HOUR:
			return Resources.getText("unitHour");
		case DAY:
			return Resources.getText("unitDay");
		case WEEK:
			return Resources.getText("unitWeek");
		case MONTH:
			return Resources.getText("unitMonth");
		case YEAR:
			return Resources.getText("unitYear");
		default:
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Return a boolean indicating if this unit is an intraday unit.
	 *
	 * @return A boolean.
	 */
	public boolean isTime() {
		switch (this) {
		case MILLISECOND:
		case SECOND:
		case MINUTE:
		case HOUR:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Returns the date pattern.
	 *
	 * @return The date pattern.
	 */
	public String getDatePattern() {
		switch (this) {
		case MILLISECOND:
			return "yyyy-MM-dd";
		case SECOND:
			return "yyyy-MM-dd";
		case MINUTE:
			return "yyyy-MM-dd";
		case HOUR:
			return "yyyy-MM-dd";
		case DAY:
		case WEEK:
			return "yyyy-MM-dd";
		case MONTH:
		case YEAR:
			return "yyyy-MM";
		default:
			return "yyyy-MM";
		}
	}

	/**
	 * Returns the time pattern.
	 *
	 * @return The time pattern.
	 */
	public String getTimePattern() {
		switch (this) {
		case MILLISECOND:
			return "HH:mm:ss.SSS";
		case SECOND:
			return "HH:mm:ss";
		case MINUTE:
			return "HH:mm";
		case HOUR:
			return "HH:mm";
		default:
			return "";
		}
	}

	/**
	 * Returns the date-time pattern.
	 *
	 * @return The date-time pattern.
	 */
	public String getPattern() {
		StringBuilder pattern = new StringBuilder();
		pattern.append(getDatePattern());
		if (isTime()) {
			pattern.append(" ");
			pattern.append(getTimePattern());
		}
		return pattern.toString();
	}
}
