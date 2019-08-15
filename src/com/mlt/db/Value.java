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
package com.mlt.db;

import com.mlt.util.Formats;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.mlt.util.Lists;
import com.mlt.util.Numbers;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Locale;

/**
 * An immutable value of supported types.
 *
 * @author Miquel Sas
 */
public class Value implements Comparable<Object> {

	/**
	 * Convert from a value.
	 *
	 * @param value  The value.
	 * @param locale The locale.
	 * @return The formatted string.
	 */
	public static String fromValue(Value value, Locale locale) {

		/* An empty string for a null value. */
		if (value.isNull()) {
			return "";
		}

		/* Format from type. */
		switch (value.getType()) {
		case BOOLEAN:
			return Formats.fromBoolean(value.getBoolean(), locale);
		case BYTEARRAY:
			return value.toString();
		case DATE:
			return Formats.fromDate(value.getDate());
		case DECIMAL:
			BigDecimal number = value.getBigDecimal();
			int scale = number.scale();
			return Formats.fromBigDecimal(number, scale, locale);
		case DOUBLE:
			return Formats.fromDouble(value.getDouble(), locale);
		case INTEGER:
			return Formats.fromInteger(value.getInteger(), locale);
		case LONG:
			return Formats.fromLong(value.getLong(), locale);
		case STRING:
			return value.getString();
		case TIME:
			return Formats.fromTime(value.getTime());
		case DATETIME:
			return Formats.fromDateTime(value.getDateTime());
		}

		/* Should never come here. */
		throw new IllegalStateException();
	}

	/**
	 * Return the value from a formatted string.
	 *
	 * @param type   The type of the value.
	 * @param scale  Decimal places when the value is a decimal.
	 * @param str    The formatted string.
	 * @param locale The locale.
	 * @return The parsed value.
	 * @throws ParseException
	 */
	public static Value toValue(Types type, int scale, String str, Locale locale) throws ParseException {

		switch (type) {
		case BOOLEAN:
			return new Value(Formats.toBoolean(str, locale));
		case BYTEARRAY:
			return new Value(str.getBytes());
		case DATE:
			return new Value(Formats.toDate(str));
		case DECIMAL:
			BigDecimal b = Numbers.getBigDecimal(Formats.toDouble(str, locale), scale);
			return new Value(b);
		case DOUBLE:
			return new Value(Formats.toDouble(str, locale));
		case INTEGER:
			return new Value(Formats.toInteger(str, locale));
		case LONG:
			return new Value(Formats.toLong(str, locale));
		case STRING:
			return new Value(str);
		case TIME:
			return new Value(Formats.toTime(str));
		case DATETIME:
			return new Value(Formats.toDateTime(str));
		}

		/* Should never come here. */
		throw new IllegalStateException();
	}

	/** The value type. */
	private Types type;
	/** The value itself. */
	private Object value;
	/** Optional label. */
	private String label;

	/**
	 * Private constructor for internal usage.
	 */
	private Value() {}

	/**
	 * Constructor assigning a number with precision. Note that for a null big
	 * decimal the precision is set to 0.
	 *
	 * @param b A big decimal
	 */
	public Value(BigDecimal b) {
		super();
		value = b;
		type = Types.DECIMAL;
	}

	/**
	 * Constructor assigning a boolean.
	 *
	 * @param b A boolean
	 */
	public Value(boolean b) {
		super();
		value = b;
		type = Types.BOOLEAN;
	}

	/**
	 * Constructor assigning a boolean.
	 *
	 * @param b A boolean
	 */
	public Value(Boolean b) {
		super();
		value = b;
		type = Types.BOOLEAN;
	}

	/**
	 * Constructor assigning a ByteArray
	 *
	 * @param byteArray The ByteArray
	 */
	public Value(byte[] byteArray) {
		super();
		value = byteArray;
		type = Types.BYTEARRAY;
	}

	/**
	 * Constructor assigning a double.
	 *
	 * @param d A double
	 */
	public Value(double d) {
		super();
		value = d;
		type = Types.DOUBLE;
	}

	/**
	 * Constructor assigning a double.
	 *
	 * @param d A double
	 */
	public Value(Double d) {
		super();
		value = d;
		type = Types.DOUBLE;
	}

	/**
	 * Constructor assigning an integer.
	 *
	 * @param i An integer
	 */
	public Value(int i) {
		super();
		value = i;
		type = Types.INTEGER;
	}

	/**
	 * Constructor assigning an integer.
	 *
	 * @param i An integer
	 */
	public Value(Integer i) {
		super();
		value = i;
		type = Types.INTEGER;
	}

	/**
	 * Constructor assigning a date.
	 *
	 * @param d The date
	 */
	public Value(LocalDate d) {
		super();
		value = d;
		type = Types.DATE;
	}

	/**
	 * Constructor assigning a timestamp.
	 *
	 * @param t The timestamp
	 */
	public Value(LocalDateTime t) {
		super();
		value = t;
		type = Types.DATETIME;
	}

	/**
	 * Constructor assigning a time.
	 *
	 * @param t The time
	 */
	public Value(LocalTime t) {
		super();
		value = t;
		type = Types.TIME;
	}

	/**
	 * Constructor assigning a long.
	 *
	 * @param l A long
	 */
	public Value(long l) {
		super();
		value = l;
		type = Types.LONG;
	}

	/**
	 * Constructor assigning a long.
	 *
	 * @param l A long
	 */
	public Value(Long l) {
		super();
		value = l;
		type = Types.LONG;
	}

	/**
	 * Constructor assigning a string.
	 *
	 * @param s A string
	 */
	public Value(String s) {
		super();
		value = s;
		type = Types.STRING;
	}

	/**
	 * Copy constructor.
	 *
	 * @param v A value
	 */
	public Value(Value v) {
		super();
		if (v == null) {
			throw new NullPointerException();
		}
		type = v.type;
		value = v.value;
		label = v.label;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(Object o) {
		// Argument can not be null
		if (o == null) {
			throw new NullPointerException();
		}
		// Must be the same type
		if (!(o instanceof Value)) {
			throw new UnsupportedOperationException(
				"Not comparable type: " + o.getClass().getName());
		}
		Value v = (Value) o;

		// Null types
		if (isNull() && v.isNull()) {
			return 0;
		}
		if (isNull() && !v.isNull()) {
			return -1;
		}
		if (!isNull() && v.isNull()) {
			return 1;
		}
		// Compare only if comparable
		if (isBoolean()) {
			if (!v.isBoolean()) {
				throw new UnsupportedOperationException(
					"Not comparable type: " + o.getClass().getName());
			}
			boolean b1 = getBoolean();
			boolean b2 = v.getBoolean();
			return (!b1 && b2 ? -1 : (b1 && !b2 ? 1 : 0));
		}
		if (isNumber()) {
			if (!v.isNumber()) {
				throw new UnsupportedOperationException(
					"Not comparable type: " + o.getClass().getName());
			}
			double d1 = getDouble();
			double d2 = v.getDouble();
			return Double.compare(d1, d2);
		}
		if (isString()) {
			if (!v.isString()) {
				throw new UnsupportedOperationException(
					"Not comparable type: " + o.getClass().getName());
			}
			return getString().compareTo(v.getString());
		}
		if (isDate()) {
			if (!v.isDate()) {
				throw new UnsupportedOperationException(
					"Not comparable type: " + o.getClass().getName());
			}
			return getDate().compareTo(v.getDate());
		}
		if (isTime()) {
			if (!v.isTime()) {
				throw new UnsupportedOperationException(
					"Not comparable type: " + o.getClass().getName());
			}
			return getTime().compareTo(v.getTime());
		}
		if (isDateTime()) {
			if (!v.isDateTime()) {
				throw new UnsupportedOperationException(
					"Not comparable type: " + o.getClass().getName());
			}
			return getDateTime().compareTo(v.getDateTime());
		}
		if (isByteArray()) {
			if (!v.isByteArray()) {
				throw new UnsupportedOperationException(
					"Not comparable type: " + o.getClass().getName());
			}
			return Lists.compare(getByteArray(), v.getByteArray());
		}
		throw new IllegalArgumentException("Value " + toString() + " is not comparable");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		// Null
		if (o == null) {
			return isNull();
		}
		if (isNull()) {
			return false;
		}
		// Boolean
		if (o instanceof Boolean) {
			if (!isBoolean()) {
				return false;
			}
			Boolean b = (Boolean) o;
			return getBoolean().equals(b);
		}
		// String
		if (o instanceof String) {
			if (!isString()) {
				return false;
			}
			String s = (String) o;
			return getString().equals(s);
		}
		// Decimal, Double, Integer, Long
		if (o instanceof Number) {
			if (!isNumber()) {
				return false;
			}
			Number n = (Number) o;
			return getNumber().equals(n);
		}
		// Date
		if (o instanceof LocalDate) {
			if (!isDate()) {
				return false;
			}
			LocalDate d = (LocalDate) o;
			return getDate().equals(d);
		}
		// Time
		if (o instanceof LocalTime) {
			if (!isTime()) {
				return false;
			}
			LocalTime t = (LocalTime) o;
			return getTime().equals(t);
		}
		// Timestamp
		if (o instanceof LocalDateTime) {
			if (!isDateTime()) {
				return false;
			}
			LocalDateTime t = (LocalDateTime) o;
			return getDateTime().equals(t);
		}
		// ByteArray
		if (o instanceof byte[]) {
			if (!isByteArray()) {
				return false;
			}
			byte[] byteArray = (byte[]) o;
			return Lists.equals(getByteArray(), byteArray);
		}
		// Value
		if (o instanceof Value) {
			Value v = (Value) o;
			// Types must be the same except for numbers where the number must be the same
			if ((isBoolean() && !v.isBoolean()) || (!isBoolean() && v.isBoolean())) {
				return false;
			}
			if ((isString() && !v.isString()) || (!isString() && v.isString())) {
				return false;
			}
			if ((isDate() && !v.isDate()) || (!isDate() && v.isDate())) {
				return false;
			}
			if ((isTime() && !v.isTime()) || (!isTime() && v.isTime())) {
				return false;
			}
			if ((isDateTime() && !v.isDateTime()) || (!isDateTime() && v.isDateTime())) {
				return false;
			}
			if ((isNumber() && !v.isNumber()) || (!isNumber() && v.isNumber())) {
				return false;
			}
			if ((isByteArray() && !v.isByteArray()) || (!isByteArray() && v.isByteArray())) {
				return false;
			}
			if (isBoolean()) {
				return getBoolean().equals(v.getBoolean());
			}
			if (isString()) {
				return getString().equals(v.getString());
			}
			if (isDate()) {
				return getDate().equals(v.getDate());
			}
			if (isTime()) {
				return getTime().equals(v.getTime());
			}
			if (isDateTime()) {
				return getDateTime().equals(v.getDateTime());
			}
			if (isNumber()) {
				return getNumber().equals(v.getNumber());
			}
			if (isByteArray()) {
				return Lists.equals(getByteArray(), v.getByteArray());
			}
		}
		return false;
	}

	/**
	 * Get the value as a BigDecimal it it's a number, otherwise throw an exception.
	 *
	 * @return A BigDecimal
	 */
	public BigDecimal getBigDecimal() {
		if (isDecimal()) {
			return (BigDecimal) value;
		}
		if (isNumber()) {
			if (isDouble()) {
				return new BigDecimal(getDouble());
			}
			return new BigDecimal(getLong());
		}
		throw new UnsupportedOperationException("Value " + value + " is not a number");
	}

	/**
	 * Get the value as a <code>boolean</code>.
	 *
	 * @return A boolean
	 */
	public Boolean getBoolean() {
		if (isBoolean()) {
			if (isNull()) {
				return false;
			}
			return ((Boolean) value);
		}
		throw new UnsupportedOperationException("Value " + value + " is not a boolean");
	}

	/**
	 * Get the value as a ByteArray.
	 *
	 * @return A ByteArray
	 */
	public byte[] getByteArray() {
		if (isByteArray()) {
			return (byte[]) value;
		}
		throw new UnsupportedOperationException("Value " + value + " is not a byte array");
	}

	/**
	 * Returns a copy of this value.
	 *
	 * @return The copy.
	 */
	public Value getCopy() {
		Value v = new Value();
		v.label = label;
		v.type = type;
		if (isNull()) {
			return v;
		}
		switch (type) {
		case BYTEARRAY:
			v.value = Lists.copy(getByteArray());
			break;
		case BOOLEAN:
		case DATE:
		case DECIMAL:
		case DOUBLE:
		case INTEGER:
		case LONG:
		case STRING:
		case TIME:
		case DATETIME:
		default:
			v.value = value;
			break;
		}
		return v;
	}

	/**
	 * Get the value as a <code>Date</code>.
	 *
	 * @return A Date
	 */
	public LocalDate getDate() {
		if (isDate()) {
			return (LocalDate) value;
		}
		throw new UnsupportedOperationException("Value " + value + " is not a date");
	}

	/**
	 * Get the value as a <code>Timestamp</code>.
	 *
	 * @return A Timestamp
	 */
	public LocalDateTime getDateTime() {
		if (isDateTime()) {
			return (LocalDateTime) value;
		}
		throw new UnsupportedOperationException("Value " + value + " is not a date-time");
	}

	/**
	 * Get the value as a double it it's a number, otherwise throw an exception.
	 *
	 * @return A double
	 */
	public Double getDouble() {
		if (isNumber()) {
			if (isNull()) {
				return Double.valueOf(0);
			}
			return ((Number) value).doubleValue();
		}
		throw new UnsupportedOperationException("Value " + value + " is not a number");
	}

	/**
	 * Get the value as an <code>int</code>.
	 *
	 * @return An integer
	 */
	public Integer getInteger() {
		if (isNumber()) {
			if (isNull()) {
				return Integer.valueOf(0);
			}
			return ((Number) value).intValue();
		}
		throw new UnsupportedOperationException("Value " + value + " is not a number");
	}

	/**
	 * Return the optional label.
	 * 
	 * @return The label.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Get the value as an <code>long</code>.
	 *
	 * @return A long
	 */
	public Long getLong() {
		if (isNumber()) {
			if (isNull()) {
				return Long.valueOf(0);
			}
			return ((Number) value).longValue();
		}
		throw new UnsupportedOperationException("Value " + value + " is not a number");
	}

	/**
	 * Get the value as a number if it is so.
	 *
	 * @return The number.
	 */
	public Number getNumber() {
		if (isNumber()) {
			return (Number) value;
		}
		throw new UnsupportedOperationException("Value " + value + " is not a number");
	}

	/**
	 * Get the value as a <code>String</code>.
	 *
	 * @return A String
	 */
	public String getString() {
		if (!isString()) {
			throw new UnsupportedOperationException("Value " + value + " is not a string");
		}
		return (String) value;
	}

	/**
	 * Get the value as a <code>Time</code>.
	 *
	 * @return A Time
	 */
	public LocalTime getTime() {
		if (isTime()) {
			return (LocalTime) value;
		}
		throw new UnsupportedOperationException("Value " + value + " is not a time");
	}

	/**
	 * Returns this value type.
	 *
	 * @return The type.
	 */
	public Types getType() {
		return type;
	}

	/**
	 * Privately access the value
	 *
	 * @return The value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		if (!isNull()) {
			if (isBoolean()) {
				Boolean b = getBoolean();
				return b.hashCode();
			}
			if (isByteArray()) {
				return Arrays.hashCode(getByteArray());
			}
			if (isNumber()) {
				Double d = getDouble();
				return d.hashCode();
			}
			if (isString()) {
				return getString().hashCode();
			}
			if (isDate()) {
				return getDate().hashCode();
			}
			if (isTime()) {
				return getTime().hashCode();
			}
			if (isDateTime()) {
				return getDateTime().hashCode();
			}
		}
		return Numbers.MIN_INTEGER;
	}

	/**
	 * Returns true if this value is in the list, false otherwise.
	 *
	 * @param values The list of values to check.
	 * @return True if this value is in the list.
	 */
	public boolean in(List<Value> values) {
		return Lists.in(this, values);
	}

	/**
	 * Returns true if this value is in the list, false otherwise.
	 *
	 * @param values The list of values to check.
	 * @return True if this value is in the list.
	 */
	public boolean in(Value... values) {
		return Lists.in(this, values);
	}

	/**
	 * Check if this value is empty or a blank string (only spaces).
	 *
	 * @return A boolean
	 */
	public boolean isBlank() {
		return isEmpty() || (isString() && getString().trim().length() == 0);
	}

	/**
	 * Check if this value is boolean.
	 *
	 * @return A boolean.
	 */
	public boolean isBoolean() {
		return getType().isBoolean();
	}

	/**
	 * Check if this value is binary (byte[]).
	 *
	 * @return A boolean.
	 */
	public boolean isByteArray() {
		return getType().isByteArray();
	}

	/**
	 * Check if this value is a date.
	 *
	 * @return A boolean.
	 */
	public boolean isDate() {
		return getType().isDate();
	}

	/**
	 * Check if this value is a time.
	 *
	 * @return A boolean.
	 */
	public boolean isDateTime() {
		return getType().isDateTime();
	}

	/**
	 * Check if this value is a number (decimal) with fixed precision.
	 *
	 * @return A boolean.
	 */
	public boolean isDecimal() {
		return getType().isDecimal();
	}

	/**
	 * Check if this value is a double.
	 *
	 * @return A boolean.
	 */
	public boolean isDouble() {
		return getType().isDouble();
	}

	/**
	 * Check if the value is empty, that is, null, empty string or zero if is
	 * number.
	 *
	 * @return A boolean
	 */
	public boolean isEmpty() {
		if (isNull()) {
			return true;
		}
		if (isString() && getString().length() == 0) {
			return true;
		}
		return isNumber() && getDouble() == 0;
	}

	/**
	 * Check if this value is a floating point number.
	 *
	 * @return A boolean.
	 */
	public boolean isFloatingPoint() {
		return getType().isFloatingPoint();
	}

	/**
	 * Check if this value is an integer.
	 *
	 * @return A boolean.
	 */
	public boolean isInteger() {
		return getType().isInteger();
	}

	/**
	 * Check if this value is a long.
	 *
	 * @return A boolean.
	 */
	public boolean isLong() {
		return getType().isLong();
	}

	/**
	 * Check if this value is null. Null is not a type, but a value can be null if
	 * the holder object its so.
	 *
	 * @return A boolean indicating if the value is null.
	 */
	public boolean isNull() {
		return (value == null);
	}

	/**
	 * Check if this value is a number (decimal, double or integer).
	 *
	 * @return A boolean.
	 */
	public boolean isNumber() {
		return getType().isNumber();
	}

	/**
	 * Check if this value is a string.
	 *
	 * @return A boolean.
	 */
	public boolean isString() {
		return getType().isString();
	}

	/**
	 * Check if this value is a time.
	 *
	 * @return A boolean.
	 */
	public boolean isTime() {
		return getType().isTime();
	}

	/**
	 * Returns true if this value is not in the list, false otherwise.
	 *
	 * @param values The list of values to check.
	 * @return True if this value is not in the list.
	 */
	public boolean notIn(List<Value> values) {
		return !Lists.in(this, values);
	}

	/**
	 * Returns true if this value is not in the list, false otherwise.
	 *
	 * @param values The list of values to check.
	 * @return True if this value is not in the list.
	 */
	public boolean notIn(Value... values) {
		return !Lists.in(this, values);
	}

	/**
	 * Set the optional label.
	 * 
	 * @param label The label.
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		if (isNull()) {
			return "null";
		}
		if (isByteArray()) {
			return new String((byte[]) value);
		}
		return value.toString();
	}
}
