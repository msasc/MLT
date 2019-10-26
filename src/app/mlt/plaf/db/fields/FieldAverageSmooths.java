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
package app.mlt.plaf.db.fields;

import com.mlt.db.Field;
import com.mlt.db.Validator;
import com.mlt.db.Value;
import com.mlt.util.Strings;

import app.mlt.plaf.db.Domains;

/**
 * Server id field.
 *
 * @author Miquel Sas
 */
public class FieldAverageSmooths extends Field {

	/**
	 * Return the list of smooths given the value as a tring comma separated.
	 * 
	 * @param value The value, validated as a string of integers comma separated.
	 * @return The array of integer smooths.
	 */
	public static int[] getSmooths(Value value) {
		if (value.isEmpty()) {
			return null;
		}
		String[] strArr = Strings.parse(value.toString(), ",");
		if (strArr.length == 0) {
			return null;
		}
		int[] smooths = new int[strArr.length];
		for (int i = 0; i < strArr.length; i++) {
			try {
				smooths[i] = Integer.parseInt(strArr[i].trim());
			} catch (NumberFormatException exc) {
				return null;
			}
		}
		return smooths;
	}

	/**
	 * Validator parser.
	 */
	class SmoothsValidator extends Validator<Value> {

		@Override
		public boolean validate(Value value, Object operation) {
			if (getSmooths(value) == null) {
				return false;
			}
			return true;
		}

		@Override
		public String getMessage(Value value, Object operation) {
			if (getSmooths(value) == null) {
				return "Invalid list of smooths";
			}
			return null;
		}

	}

	/**
	 * Constructor.
	 * 
	 * @param name Field name.
	 */
	public FieldAverageSmooths(String name) {
		super(Domains.getString(name, 20, "Average smooths", "Average smooths"));
		addValidator(new SmoothsValidator());
	}
}
