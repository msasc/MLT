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
package com.mlt.db.rdbms;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

import com.mlt.db.Field;
import com.mlt.db.FieldList;
import com.mlt.db.Record;
import com.mlt.db.Types;
import com.mlt.db.Value;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Database utilities to write to a prepared statement or to read from a
 * resultset.
 *
 * @author Miquel Sas
 */
public class DBUtils {

	/**
	 * Read a record from a ResultSet.
	 *
	 * @param fieldList The field list
	 * @param rs        The source result set
	 * @return The record.
	 * @throws SQLException If such an error occurs.
	 */
	public static Record readRecord(FieldList fieldList, ResultSet rs) throws SQLException {
		Record record = new Record(fieldList);
		List<Field> fields = fieldList.getFields();
		int index = 1;
		for (int i = 0; i < fields.size(); i++) {
			Field field = fields.get(i);
			Types type = field.getType();
			int decimals = field.getDecimals();
			Value value;
			if (field.isPersistent() || field.isVirtual()) {
				value = DBUtils.fromResultSet(type, decimals, index++, rs);
			} else {
				value = field.getDefaultValue();
			}
			record.setValue(i, value, false);
		}
		return record;
	}

	/**
	 * Reads a value from a result set.
	 *
	 * @param type      The type
	 * @param decimals  The scale
	 * @param index     The index in the result set
	 * @param resultSet The result set
	 * @return The appropriate value.
	 * @throws SQLException If such an error occurs.
	 */
	public static Value fromResultSet(Types type, int decimals, int index, ResultSet resultSet) throws SQLException {
		if (type == null) {
			throw new NullPointerException();
		}
		Value value = null;
		switch (type) {
		case BOOLEAN:
			String s = resultSet.getString(index);
			boolean b = (s != null && s.equals("Y"));
			value = new Value(b);
			break;
		case DECIMAL:
			BigDecimal bd = resultSet.getBigDecimal(index);
			if (bd != null) {
				bd = bd.setScale(decimals, RoundingMode.HALF_UP);
				value = new Value(bd);
			}
			break;
		case INTEGER:
			value = new Value(resultSet.getInt(index));
			break;
		case LONG:
			value = new Value(resultSet.getLong(index));
			break;
		case DOUBLE:
			value = new Value(resultSet.getDouble(index));
			break;
		case DATE:
			Date date = resultSet.getDate(index);
			if (date == null) {
				value = new Value((LocalDate) null);
			} else {
				value = new Value(date.toLocalDate());
			}
			break;
		case TIME:
			Time time = resultSet.getTime(index);
			if (time == null) {
				value = new Value((LocalTime) null);
			} else {
				value = new Value(time.toLocalTime());
			}
			break;
		case DATETIME:
			Timestamp timestamp = resultSet.getTimestamp(index);
			if (timestamp == null) {
				value = new Value((LocalDateTime) null);
			} else {
				value = new Value(timestamp.toLocalDateTime());
			}
			break;
		case STRING:
			value = new Value(resultSet.getString(index));
			break;
		case BYTEARRAY:
			value = new Value(resultSet.getBytes(index));
			break;
		default:
			break;
		}
		if (!type.isNumber() && resultSet.wasNull()) {
			value = type.getNullValue();
		}
		return value;
	}

	/**
	 * Set the value to a <code>PreparedStatement</code> parameter at the specified
	 * index.
	 *
	 * @param value The value to set to the prepared statement.
	 * @param index The parameter index.
	 * @param ps    The <code>PreparedStatement</code>.
	 * @throws SQLException If such an error occurs.
	 */
	public static void toPreparedStatement(Value value, int index, PreparedStatement ps) throws SQLException {
		Types type = value.getType();
		if (value.isNull()) {
			ps.setNull(index, type.getJDBCType(0));
		} else {
			switch (type) {
			case BOOLEAN:
				ps.setString(index, (value.getBoolean() ? "Y" : "N"));
				break;
			case DECIMAL:
				ps.setBigDecimal(index, value.getBigDecimal());
				break;
			case DOUBLE:
				ps.setDouble(index, value.getDouble());
				break;
			case INTEGER:
				ps.setInt(index, value.getInteger());
				break;
			case LONG:
				ps.setLong(index, value.getLong());
				break;
			case STRING:
				int length = value.getString().length();
				if (length <= Types.FIXED_LENGTH) {
					ps.setString(index, value.getString());
				} else {
					String string = value.getString();
					ps.setCharacterStream(index, new StringReader(string), string.length());
				}
				break;
			case BYTEARRAY:
				byte[] bytes = value.getByteArray();
				if (bytes.length <= Types.FIXED_LENGTH) {
					ps.setBytes(index, bytes);
				} else {
					ps.setBinaryStream(index, new ByteArrayInputStream(bytes), bytes.length);
				}
				break;
			case DATE:
				ps.setDate(index, Date.valueOf(value.getDate()));
				break;
			case TIME:
				ps.setTime(index, Time.valueOf(value.getTime()));
				break;
			case DATETIME:
				ps.setTimestamp(index, Timestamp.valueOf(value.getDateTime()));
				break;
			}
		}
	}
}
