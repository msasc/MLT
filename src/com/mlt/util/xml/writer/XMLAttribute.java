/*
 * Copyright (C) 2015 Miquel Sas
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
package com.mlt.util.xml.writer;

/**
 * An XML attribute packs a key and a value.
 * 
 * @author Miquel Sas
 */
public class XMLAttribute {

	/**
	 * The key.
	 */
	private String key;
	/**
	 * The value.
	 */
	private String value;

	/**
	 * Default constructor.
	 */
	public XMLAttribute() {
		super();
	}

	/**
	 * Contructor to build an attribute without value.
	 * 
	 * @param key The key.
	 */
	public XMLAttribute(String key) {
		super();
		this.key = key;
	}

	/**
	 * Constructor assigning the key and the value.
	 * 
	 * @param key   The key.
	 * @param value The value.
	 */
	public XMLAttribute(String key, String value) {
		super();
		this.key = key;
		this.value = value;
	}

	/**
	 * Constructor assigning the key and the value.
	 * 
	 * @param key   The key.
	 * @param value The value.
	 */
	public XMLAttribute(String key, int value) {
		super();
		this.key = key;
		this.value = Integer.toString(value);
	}

	/**
	 * Constructor assigning the key and the value.
	 * 
	 * @param key   The key.
	 * @param value The value.
	 */
	public XMLAttribute(String key, double value) {
		super();
		this.key = key;
		this.value = Double.toString(value);
	}

	/**
	 * Constructor assigning the key and the value.
	 * 
	 * @param key   The key.
	 * @param value The value.
	 */
	public XMLAttribute(String key, Object value) {
		super();
		this.key = key;
		this.value = value.toString();
	}

	/**
	 * Get the key.
	 * 
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Set the key.
	 * 
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * Get the value.
	 * 
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Set the value.
	 * 
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return key.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof XMLAttribute) {
			XMLAttribute attr = (XMLAttribute) obj;
			return key.equals(attr.key);
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("[");
		b.append(key);
		b.append(",");
		b.append(value);
		b.append("]");
		return b.toString();
	}

}
