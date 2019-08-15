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
package app.mlt.plaf.db.fields;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.mlt.db.Calculator;
import com.mlt.db.Field;
import com.mlt.db.Record;
import com.mlt.db.Value;
import com.mlt.mkt.data.Period;
import com.mlt.util.StringConverter;

import app.mlt.plaf.db.Domains;
import app.mlt.plaf.db.Fields;

/**
 * Data filter field.
 *
 * @author Miquel Sas
 */
public class FieldTimeFmt extends Field {

	/**
	 * Calculator
	 */
	public static class Time implements Calculator {
		@Override
		public Value getValue(Record record) {
			return record.getValue(Fields.TIME);
		}

	}

	/**
	 * String converter.
	 */
	public static class Converter implements StringConverter {
		/** Date format. */
		private SimpleDateFormat format;

		/**
		 * Constructor.
		 * 
		 * @param format The date format.
		 */
		public Converter(SimpleDateFormat format) {
			super();
			this.format = format;
		}

		@Override
		public Object stringToValue(String text) throws ParseException {
			return null;
		}

		@Override
		public String valueToString(Object obj) throws ParseException {
			if (!(obj instanceof Value)) {
				return obj.toString();
			}
			Value value = (Value) obj;
			return format.format(new Timestamp(value.getLong()));
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param name   The name of the field.
	 * @param period The period.
	 */
	public FieldTimeFmt(String name, Period period) {
		super(Domains.getLong(name, "Time fmt", "Time fmt"));
		setPersistent(false);
		String pattern = null;
		switch (period.getUnit()) {
		case MILLISECOND:
			pattern = "yyyy-MM-dd HH:mm:ss.SSS";
			break;
		case SECOND:
			pattern = "yyyy-MM-dd HH:mm:ss";
			break;
		case MINUTE:
			pattern = "yyyy-MM-dd HH:mm";
			break;
		case HOUR:
			pattern = "yyyy-MM-dd HH";
			break;
		case DAY:
			pattern = "yyyy-MM-dd";
			break;
		case WEEK:
			pattern = "yyyy-MM-dd";
			break;
		case MONTH:
			pattern = "yyyy-MM";
			break;
		case YEAR:
			pattern = "yyyy";
			break;
		default:
			throw new IllegalArgumentException();
		}
		setCalculator(new Time());
		setStringConverter(new Converter(new SimpleDateFormat(pattern)));
	}
}
