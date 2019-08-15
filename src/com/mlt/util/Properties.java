/*
 * Copyright (C) 2018 Miquel Sas
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the
 * GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the
 * License, or (at
 * your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without
 * even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If
 * not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.mlt.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A useful and quite generic properties table with typed accessors for commonly
 * used objects. Using a map to store the properties of an object has several
 * advantages, like for instance a natural copy mechanism.
 * 
 * @author Miquel Sas
 */
public class Properties {

	/**
	 * The properties map.
	 */
	private final Map<String, Object> properties = new HashMap<>();

	/**
	 * Constructor.
	 */
	public Properties() {
		super();
	}

	/**
	 * Clear this properties.
	 */
	public void clear() {
		properties.clear();
	}

	/**
	 * Clone this properties providing a full copy of the objects when possible,
	 * not simply a copy of the reference.
	 * 
	 * @return A copy of this properties.
	 */
	@Override
	public Properties clone() {
		Properties p = new Properties();
		Iterator<String> keys = properties.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			Object value = properties.get(key);

			/*
			 * Types Boolean, Double, Integer, Long and String are immutable, so
			 * they can be
			 * directly assigned.
			 */
			if (value instanceof Boolean || value instanceof Integer
				|| value instanceof Long
				|| value instanceof String) {
				p.properties.put(key, value);
				continue;
			}

			/*
			 * Vector of doubles, make a copy.
			 */
			if (value instanceof double[]) {
				double[] src = (double[]) value;
				double[] dst = Vector.copy(src);
				p.properties.put(key, dst);
				continue;
			}

			/*
			 * 2d matrix of doubles, make a copy.
			 */
			if (value instanceof double[][]) {
				double[][] src = (double[][]) value;
				double[][] dst = Matrix.copy(src);
				p.properties.put(key, dst);
				continue;
			}

			/*
			 * Other objects, just put the reference.
			 */
			p.properties.put(key, value);
		}
		return p;
	}

	/**
	 * Returns a stored boolean value, returning <code>false</code> if not set.
	 * 
	 * @param key The key.
	 * @return The stored boolean value.
	 */
	public boolean getBoolean(String key) {
		return getBoolean(key, false);
	}

	/**
	 * Returns a stored boolean value, returning the default one if not set.
	 * 
	 * @param key          The key.
	 * @param defaultValue The default value.
	 * @return The stored boolean value.
	 */
	public boolean getBoolean(String key, boolean defaultValue) {
		Boolean value = (Boolean) properties.get(key);
		return (value == null ? defaultValue : value);
	}

	/**
	 * Returns a stored double value, returning <code>0</code> if not set.
	 * 
	 * @param key The key.
	 * @return The stored double value.
	 */
	public double getDouble(String key) {
		return getDouble(key, 0);
	}

	/**
	 * Returns a stored double value, returning the default one if not set.
	 * 
	 * @param key          The key.
	 * @param defaultValue The default value.
	 * @return The stored double value.
	 */
	public double getDouble(String key, double defaultValue) {
		Double value = (Double) properties.get(key);
		return (value == null ? defaultValue : value);
	}

	/**
	 * Return a stored double 2d matrix.
	 * 
	 * @param key The key.
	 * @return The double 2d matrix.
	 */
	public double[][] getDouble2A(String key) {
		return (double[][]) properties.get(key);
	}

	/**
	 * Return a stored double vector.
	 * 
	 * @param key The key.
	 * @return The double vector.
	 */
	public double[] getDouble1A(String key) {
		return (double[]) properties.get(key);
	}

	/**
	 * Returns a stored integer value, returning <code>0</code> if not set.
	 * 
	 * @param key The key.
	 * @return The stored integer value.
	 */
	public int getInteger(String key) {
		return getInteger(key, 0);
	}

	/**
	 * Returns a stored integer value, returning the default one if not set.
	 * 
	 * @param key          The key.
	 * @param defaultValue The default value.
	 * @return The stored integer value.
	 */
	public int getInteger(String key, int defaultValue) {
		Integer value = (Integer) properties.get(key);
		return (value == null ? defaultValue : value);
	}

	/**
	 * Returns a stored long value, returning <code>0</code> if not set.
	 * 
	 * @param key The key.
	 * @return The stored long value.
	 */
	public long getLong(String key) {
		return getLong(key, 0);
	}

	/**
	 * Returns a stored long value, returning the default one if not set.
	 * 
	 * @param key          The key.
	 * @param defaultValue The default value.
	 * @return The stored long value.
	 */
	public long getLong(String key, long defaultValue) {
		Long value = (Long) properties.get(key);
		return (value == null ? defaultValue : value);
	}

	/**
	 * Returns a stored object, returning <code>null</code> if not set.
	 * 
	 * @param key The key.
	 * @return The stored object.
	 */
	public Object getObject(String key) {
		return getObject(key, null);
	}

	/**
	 * Returns a stored object, returning the default one if not set.
	 * 
	 * @param key          The key.
	 * @param defaultValue The default value.
	 * @return The stored object.
	 */
	public Object getObject(String key, Object defaultValue) {
		Object value = properties.get(key);
		return (value == null ? defaultValue : value);
	}

	/**
	 * Returns a stored string value, returning <code>null</code> if not set.
	 * 
	 * @param key The key.
	 * @return The stored string value.
	 */
	public String getString(String key) {
		return getString(key, null);
	}

	/**
	 * Returns a stored string value, returning the default one if not set.
	 * 
	 * @param key          The key.
	 * @param defaultValue The default value.
	 * @return The stored string value.
	 */
	public String getString(String key, String defaultValue) {
		String value = (String) properties.get(key);
		return (value == null ? defaultValue : value);
	}

	/**
	 * Return the set of keys.
	 * 
	 * @return The set of keys.
	 */
	public Set<String> keySet() {
		return properties.keySet();
	}

	/**
	 * Fill this properties with the argument properties.
	 * 
	 * @param properties The properties used to fill this properties.
	 */
	public void putAll(Properties properties) {
		Iterator<String> keys = properties.properties.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			Object value = properties.properties.get(key);
			this.properties.put(key, value);
		}
	}

	/**
	 * Remove the property at key.
	 * 
	 * @param key The key.
	 * @return The removed property or null.
	 */
	public Object remove(String key) {
		return properties.remove(key);
	}

	/**
	 * Store a boolean value.
	 * 
	 * @param key   The key.
	 * @param value The value.
	 */
	public void setBoolean(String key, boolean value) {
		properties.put(key, value);
	}

	/**
	 * Store a double value.
	 * 
	 * @param key   The key.
	 * @param value The value.
	 */
	public void setDouble(String key, double value) {
		properties.put(key, value);
	}

	/**
	 * Set the vector.
	 * 
	 * @param key The key.
	 * @param a   The 2d matrix.
	 */
	public void setDouble2A(String key, double[][] a) {
		properties.put(key, a);
	}

	/**
	 * Set the vector.
	 * 
	 * @param key The key.
	 * @param v   The vector.
	 */
	public void setDouble1A(String key, double[] v) {
		properties.put(key, v);
	}

	/**
	 * Store an integer value.
	 * 
	 * @param key   The key.
	 * @param value The value.
	 */
	public void setInteger(String key, int value) {
		properties.put(key, value);
	}

	/**
	 * Store an long value.
	 * 
	 * @param key   The key.
	 * @param value The value.
	 */
	public void setLong(String key, long value) {
		properties.put(key, value);
	}

	/**
	 * Store a object.
	 * 
	 * @param key   The key.
	 * @param value The object.
	 */
	public void setObject(String key, Object value) {
		properties.put(key, value);
	}

	/**
	 * Store a string value.
	 * 
	 * @param key   The key.
	 * @param value The value.
	 */
	public void setString(String key, String value) {
		properties.put(key, value);
	}
}
