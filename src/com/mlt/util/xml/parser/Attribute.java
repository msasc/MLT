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

import com.mlt.util.Strings;

/**
 * Attribute definition to validate possible attributes per path.
 *
 * @author Miquel Sas
 */
public class Attribute {

	/** Valid types. */
	public static final String[] TYPES =
		new String[] {
			"string", "integer", "double", "boolean", "name"
		};

	/** Name. */
	private String name;
	/** Type. */
	private String type;
	/** Required flag. */
	private boolean required = true;

	/**
	 * Constructor.
	 * 
	 * @param name Name.
	 * @param type Type.
	 */
	public Attribute(String name, String type) {
		this(name, type, true);
	}

	/**
	 * Constructor.
	 * 
	 * @param name     Name.
	 * @param type     Type.
	 * @param required Required flag.
	 */
	public Attribute(String name, String type, boolean required) {
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
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Attribute) {
			Attribute attr = (Attribute) obj;
			if (name.equals(attr.name) && type.equals(attr.type) && required == attr.required) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return The name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return The type.
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return A boolean indicating if the attribute is required.
	 */
	public boolean isRequired() {
		return required;
	}

}
