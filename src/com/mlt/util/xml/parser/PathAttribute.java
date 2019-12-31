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

package com.mlt.util.xml.parser;

import org.xml.sax.SAXException;

import com.mlt.util.Strings;

/**
 * Parser attribute definition to validate possible attributes per path.
 *
 * @author Miquel Sas
 */
class PathAttribute {

	/** Valid types. */
	static final String[] TYPES =
		new String[] {
			"string", "integer", "integer-array", "double", "boolean", "name"
		};

	/** Name. */
	String name;
	/** Type. */
	String type;
	/** Required flag. */
	boolean required = true;
	/** Optional possible comma separated values. */
	String values;

	/**
	 * Constructor.
	 * 
	 * @param name Name.
	 * @param type Type.
	 */
	PathAttribute(String name, String type) {
		this(name, type, true);
	}

	/**
	 * Constructor.
	 * 
	 * @param name     Name.
	 * @param type     Type.
	 * @param required Required flag.
	 */
	PathAttribute(String name, String type, boolean required) {
		super();
		if (name == null) {
			throw new NullPointerException();
		}
		if (type == null) {
			throw new NullPointerException();
		}
		if (!Strings.in(type, TYPES)) {
			throw new IllegalArgumentException("Invalid type: " + type);
		}
		this.name = name;
		this.type = type;
		this.required = required;
	}

	/**
	 * Constructor.
	 * 
	 * @param name   Name.
	 * @param type   Type.
	 * @param values Possible comma separated values.
	 */
	PathAttribute(String name, String type, String values) {
		super();
		if (name == null) {
			throw new NullPointerException();
		}
		if (type == null) {
			throw new NullPointerException();
		}
		if (!Strings.in(type, TYPES)) {
			throw new IllegalArgumentException("Invalid type: " + type);
		}
		if (values == null) {
			throw new NullPointerException();
		}
		this.name = name;
		this.type = type;
		this.values = values;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PathAttribute) {
			PathAttribute attr = (PathAttribute) obj;
			if (name.equals(attr.name) && type.equals(attr.type) && required == attr.required) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Validate the value.
	 * 
	 * @param value The string value.
	 * @throws SAXException
	 */
	public void validate(String value) throws SAXException {
		/* Just the name present. */
		if (type.equals("name") && (value == null || value.isEmpty())) {
			return;
		}
		/* Check possible values. */
		if (values != null) {
			String[] tokens = Strings.parse(values, ",");
			boolean ok = false;
			for (int i = 0; i < tokens.length; i++) {
				if (tokens[i].trim().equals(value)) {
					ok = true;
					break;
				}
			}
			if (!ok) {
				throw new SAXException(
					"Attribute " + name + " must be in the list of values: " + values);
			}
		}
		/* String. */
		if (type.equals("string") && value != null) {
			return;
		}
		/* Integer. */
		if (type.equals("integer")) {
			try {
				Integer.parseInt(value);
			} catch (NumberFormatException exc) {
				throw new SAXException("Attribute " + name + " must be integer.");
			}
		}
		/* Double. */
		if (type.equals("double")) {
			try {
				Double.parseDouble(value);
			} catch (NumberFormatException exc) {
				throw new SAXException("Attribute " + name + " must be double.");
			}
		}
		/* Boolean. */
		if (type.equals("boolean")) {
			if (value.equals("true") || value.equals("false")) {
				return;
			}
			throw new SAXException("Attribute " + name + " must boolean (\"true\" or \"false\").");
		}
		/* Integer array. */
		if (type.equals("integer-array")) {
			int[] array = null;
			if (value != null) {
				String[] elements = Strings.parse(value, ",");
				array = new int[elements.length];
				for (int i = 0; i < elements.length; i++) {
					try {
						array[i] = Integer.parseInt(elements[i]);
					} catch (NumberFormatException exc) {
						throw new SAXException(
							"Attribute " + name + " is not a well formed integer array.");
					}
				}
			}
		}
	}
}
