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

import java.text.ParseException;
import java.util.Locale;

import com.mlt.db.Value;
import com.mlt.util.Formats;
import com.mlt.util.StringConverter;

/**
 * Convert a number with a fixed scale.
 *
 * @author Miquel Sas
 */
public class NumberScaleConverter implements StringConverter {
	
	/** Scale. */
	private int scale;

	/**
	 * Constructor.
	 * 
	 * @param scale The scale.
	 */
	public NumberScaleConverter(int scale) {
		super();
		this.scale = scale;
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
		if (!value.isNumber()) {
			return obj.toString();
		}
		double d = value.getNumber().doubleValue();
		return Formats.fromDouble(d, scale, Locale.getDefault());
	}

}
