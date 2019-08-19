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

package app.mlt.plaf.db.converters;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.mlt.db.Value;
import com.mlt.util.StringConverter;

/**
 * Formatted time converter.
 *
 * @author Miquel Sas
 */
public class TimeFmtConverter implements StringConverter {
	
	
	/** Date format. */
	private SimpleDateFormat format;

	/**
	 * Constructor.
	 * 
	 * @param format The date format.
	 */
	public TimeFmtConverter(SimpleDateFormat format) {
		super();
		this.format = format;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object stringToValue(String text) throws ParseException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String valueToString(Object obj) throws ParseException {
		if (!(obj instanceof Value)) {
			return obj.toString();
		}
		Value value = (Value) obj;
		return format.format(new Timestamp(value.getLong()));
	}
}
