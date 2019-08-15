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
package com.mlt.util;

import java.util.Locale;

/**
 * Provides access to res, string, icons.
 *
 * @author Miquel Sas
 */
public class Resources {

	/**
	 * Statically access a string when there is only a locale available.
	 *
	 * @param key The string key.
	 * @return The string.
	 */
	public static String getText(String key) {
		return TextServer.getText(key, Locale.getDefault());
	}

	/**
	 * Statically access a string when there is only a locale available.
	 *
	 * @param key    The string key.
	 * @param locale The locale.
	 * @return The string.
	 */
	public static String getText(String key, Locale locale) {
		return TextServer.getText(key, locale);
	}

	/**
	 * Adds a base resource to the list of base res.
	 *
	 * @param fileName The base resource to add.
	 */
	public static void addBaseTextResource(String fileName) {
		TextServer.addBaseTextResource(fileName);
	}

}
