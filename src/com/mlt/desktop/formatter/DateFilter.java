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

package com.mlt.desktop.formatter;

import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.mlt.desktop.layout.Direction;
import com.mlt.util.Chrono;
import com.mlt.util.Strings;

/**
 * Date and time filter. Accepts the following sequences of fields:
 * <ul>
 * <li>YEAR</li>
 * <li>YEAR-MONTH</li>
 * <li>YEAR-MONTH-DAY</li>
 * <li>HOUR</li>
 * <li>HOUR:MINUTE</li>
 * <li>HOUR:MINUTE:SECOND</li>
 * <li>HOUR:MINUTE:SECOND.TENTH</li>
 * <li>HOUR:MINUTE:SECOND.HUNDREDTH</li>
 * <li>HOUR:MINUTE:SECOND.MILLI</li>
 * <li>HOUR:MINUTE:SECOND.MICRO</li>
 * <li>HOUR:MINUTE:SECOND.NANO</li>
 * <li>YEAR-MONTH-DAY HOUR</li>
 * <li>YEAR-MONTH-DAY HOUR:MINUTE</li>
 * <li>YEAR-MONTH-DAY HOUR:MINUTE:SECOND</li>
 * <li>YEAR-MONTH-DAY HOUR:MINUTE:SECOND.TENTH</li>
 * <li>YEAR-MONTH-DAY HOUR:MINUTE:SECOND.HUNDREDTH</li>
 * <li>YEAR-MONTH-DAY HOUR:MINUTE:SECOND.MILLI</li>
 * <li>YEAR-MONTH-DAY HOUR:MINUTE:SECOND.MICRO</li>
 * <li>YEAR-MONTH-DAY HOUR:MINUTE:SECOND.NANO</li>
 * </ul>
 * The separators are set by default, but are optional and eligible.
 *
 * @author Miquel Sas
 */
public class DateFilter extends SegmentFilter {

	/**
	 * A date, time or date-time segment.
	 */
	private class ChronoSegment extends Segment {

		/** Chrono field. */
		private Chrono chrono;

		/**
		 * Constructor.
		 * 
		 * @param chrono The chrono field.
		 */
		private ChronoSegment(Chrono chrono) {
			super();
			this.chrono = chrono;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected boolean acceptRemove(int offset, Direction direction) {
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected boolean acceptReplace(int offset, char c) {
			if (chrono == Chrono.YEAR) {
				boolean negativeYears = Math.min(minimumYear, maximumYear) < 0;
				if (negativeYears) {
					if (!Character.isDigit(c) && c != '-') {
						return false;
					}
				} else {
					if (!Character.isDigit(c)) {
						return false;
					}
				}
			} else {
				if (!Character.isDigit(c)) {
					return false;
				}
			}
			return offset < getPad();
		}

		/**
		 * Change the current field by increasing or decreasing one unit.
		 * 
		 * @param increase A boolean.
		 */
		private void change(boolean increase) {
			boolean change = true;
			if (chrono == Chrono.YEAR && increase && time.getYear() >= getMax()) {
				change = false;
			}
			if (chrono == Chrono.YEAR && !increase && time.getYear() <= getMin()) {
				change = false;
			}
			if (change) {
				if (increase) {
					time = time.plus(chrono.getMultiplier(), chrono.getTemporalUnit());
				} else {
					time = time.minus(chrono.getMultiplier(), chrono.getTemporalUnit());
				}
			}
		}

		/**
		 * Return the maximum possible value of this field.
		 * 
		 * @return The maximum value.
		 */
		private int getMax() {
			switch (chrono) {
			case YEAR:
				return maximumYear;
			case MONTH:
				return 12;
			case DAY:
				return time.toLocalDate().lengthOfMonth();
			case HOUR:
				return 23;
			case MINUTE:
				return 59;
			case SECOND:
				return 59;
			case TENTH:
				return 9;
			case HUNDREDTH:
				return 99;
			case MILLI:
				return 999;
			case MICRO:
				return 999999;
			case NANO:
				return 999999999;
			}
			throw new IllegalStateException();
		}

		/**
		 * Return the minimum possible value of this field.
		 * 
		 * @return The minimum value.
		 */
		private int getMin() {
			switch (chrono) {
			case YEAR:
				return minimumYear;
			case MONTH:
				return 1;
			case DAY:
				return 1;
			case HOUR:
				return 0;
			case MINUTE:
				return 0;
			case SECOND:
				return 0;
			case TENTH:
				return 0;
			case HUNDREDTH:
				return 0;
			case MILLI:
				return 0;
			case MICRO:
				return 0;
			case NANO:
				return 0;
			}
			throw new IllegalStateException();
		}

		/**
		 * Return the padding of this field.
		 * 
		 * @return The padding.
		 */
		private int getPad() {
			switch (chrono) {
			case YEAR:
				int year = Math.abs(time.getYear());
				return (year <= 9999 ? 4 : Integer.toString(year).length());
			case MONTH:
				return 2;
			case DAY:
				return 2;
			case HOUR:
				return 2;
			case MINUTE:
				return 2;
			case SECOND:
				return 2;
			case TENTH:
				return 1;
			case HUNDREDTH:
				return 2;
			case MILLI:
				return 3;
			case MICRO:
				return 6;
			case NANO:
				return 9;
			}
			throw new IllegalStateException();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String getText() {
			StringBuilder b = new StringBuilder();
			if (chrono == Chrono.YEAR) {
				boolean negative = (time.getYear() < 0);
				int year = Math.abs(time.getYear());
				b.append(new StringBuilder(Strings.leftPad(Integer.toString(year), getPad(), "0")));
				if (negative) {
					b.insert(0, '-');
				}
			} else {
				b.append(new StringBuilder(Strings.leftPad(Integer.toString(getValue()), getPad(), "0")));
			}
			return b.toString();
		}

		/**
		 * Return the current value of this field.
		 * 
		 * @return The value.
		 */
		private int getValue() {
			return time.get(chrono.getTemporalField()) / chrono.getMultiplier();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void keyPressed(KeyEvent e, int caretPos) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_DOWN:
				change(true);
				break;
			case KeyEvent.VK_UP:
				change(false);
				break;
			default:
				return;
			}
			setText();
			setCaretPosition(caretPos);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected int remove(int offset, Direction direction) {
			
			/* Remove. */
			StringBuilder b = new StringBuilder(getText());
			b.replace(offset, offset + 1, "0");
			setValue(b.toString());

			/* Removed, move caret if delete right. */
			if (direction == Direction.RIGHT) {
				return 1;
			}
			return 0;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected int replace(int offset, char c) {
			
			/* Year signum. */
			boolean negativeYears = Math.min(minimumYear, maximumYear) < 0;
			if (chrono == Chrono.YEAR && negativeYears && c == '-') {
				LocalDate localDate = time.toLocalDate();
				LocalTime localTime = time.toLocalTime();
				int year = localDate.getYear();
				int month = localDate.getMonthValue();
				int day = localDate.getDayOfMonth();
				year *= -1;
				localDate = LocalDate.of(year, month, day);
				time = LocalDateTime.of(localDate, localTime);
				return 0;
			}
			
			/* Skip signum. */
			if (c == '-') {
				return 0;
			}

			/* Overwrite. */
			StringBuilder b = new StringBuilder(getText());
			b.replace(offset, offset + 1, Strings.valueOf(c));
			setValue(b.toString());

			/* Overwritten, move caret one position. */
			return 1;
		}

		/**
		 * Set the field value parsing the string.
		 * 
		 * @param str The string value.
		 */
		private void setValue(String str) {

			if (str.isEmpty()) {
				str = "0";
			}

			int val = Integer.valueOf(str);
			val = (val < getMin() ? getMin() : (val > getMax() ? getMax() : val));
			val *= chrono.getMultiplier();

			int year = time.getYear();
			int month = time.getMonthValue();
			int day = time.getDayOfMonth();
			int hour = time.getHour();
			int minute = time.getMinute();
			int second = time.getSecond();
			int nano = time.getNano();

			switch (chrono) {
			case YEAR:
				year = val;
				break;
			case MONTH:
				month = val;
				break;
			case DAY:
				day = val;
				break;
			case HOUR:
				hour = val;
				break;
			case MINUTE:
				minute = val;
				break;
			case SECOND:
				second = val;
				break;
			case TENTH:
			case HUNDREDTH:
			case MILLI:
			case MICRO:
			case NANO:
				nano = val;
				break;
			}

			/* Correct the day if the field was the month. */
			if (chrono == Chrono.MONTH) {
				LocalDate date = LocalDate.of(year, month, 1);
				int max = date.lengthOfMonth();
				day = (day < 1 ? 1 : (day > max ? max : day));
			}

			time = LocalDateTime.of(year, month, day, hour, minute, second, nano);
		}
	}

	/** The local-date-time. */
	private LocalDateTime time = LocalDateTime.of(0, 1, 1, 0, 0, 0, 0);
	/** Maximum year. */
	private int maximumYear = LocalDate.MAX.getYear();
	/** Minimum year. */
	private int minimumYear = LocalDate.MIN.getYear();

	/**
	 * Constructor.
	 */
	public DateFilter() {
		super();
	}

	/**
	 * Return the internal local date-time.
	 * 
	 * @return The time.
	 */
	public LocalDateTime getLocalDateTime() {
		return time;
	}

	/**
	 * Set this filter to edit an ISO date.
	 */
	public void setDate() {
		clearSegments();
		addSegment(new ChronoSegment(Chrono.YEAR));
		addSegment(new Separator("-"));
		addSegment(new ChronoSegment(Chrono.MONTH));
		addSegment(new Separator("-"));
		addSegment(new ChronoSegment(Chrono.DAY));
	}

	/**
	 * Set this filter to edit an ISO date-time.
	 */
	public void setDateTime() {
		clearSegments();
		addSegment(new ChronoSegment(Chrono.YEAR));
		addSegment(new Separator("-"));
		addSegment(new ChronoSegment(Chrono.MONTH));
		addSegment(new Separator("-"));
		addSegment(new ChronoSegment(Chrono.DAY));
		addSegment(new Separator(" "));
		addSegment(new ChronoSegment(Chrono.HOUR));
		addSegment(new Separator(":"));
		addSegment(new ChronoSegment(Chrono.MINUTE));
		addSegment(new Separator(":"));
		addSegment(new ChronoSegment(Chrono.SECOND));
	}

	/**
	 * Set the internal local date-time.
	 * 
	 * @param time The time.
	 */
	public void setLocalDateTime(LocalDateTime time) {
		this.time = time;
		setText();
	}

	/**
	 * Set this filter to edit an ISO time.
	 */
	public void setTime() {
		clearSegments();
		addSegment(new ChronoSegment(Chrono.HOUR));
		addSegment(new Separator(":"));
		addSegment(new ChronoSegment(Chrono.MINUTE));
		addSegment(new Separator(":"));
		addSegment(new ChronoSegment(Chrono.SECOND));
	}
	
	public void setYearLimits(int minimumYear, int maximumYear) {
		if (maximumYear < minimumYear) {
			throw new IllegalArgumentException();
		}
		this.minimumYear = minimumYear;
		this.maximumYear = maximumYear;
	}
}
