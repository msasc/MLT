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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * A <code>TextServer</code> services text res. Text res can be located in property files under a common
 * directory root, in property files directly passed to the server, or in compressed files available through the class
 * path, that contain a set of property files.
 *
 * @author Miquel Sas
 */
class TextServer {

	/** List of base res loaded. */
	private static ArrayList<String> baseResources = new ArrayList<>();
	/** The text server that has the base res loaded. */
	private static TextServer baseTextServer = new TextServer();
	/** A map with localized text servers. */
	private static HashMap<Locale, TextServer> localizedTextServers = new HashMap<>();

	/**
	 * Returns a string for a given locale.
	 *
	 * @param key    The key to search the string.
	 * @param locale The locale to use. base server when the key is not found.
	 * @return the String.
	 */
	static String getText(String key, Locale locale) {
		TextServer textServer;
		String string;
		// Try with the appropriate server.
		textServer = getLocalizedTextServer(locale);
		string = getText(key, textServer, locale);
		if (string != null) {
			return string;
		}
		// Try with the base text server.
		textServer = getBaseTextServer();
		string = getText(key, textServer, new Locale(""));
		if (string != null) {
			return string;
		}
		// Then return the key.
		return "[" + key + "]";
	}

	/**
	 * Get a string from a given text server.
	 *
	 * @param key        The string key
	 * @param textServer The text server to use.
	 * @param locale     The local use.
	 * @return The required string or null.
	 */
	static String getText(String key, TextServer textServer, Locale locale) {
		String string = null;
		// First attempt to check if the string is already loaded in the server.
		string = textServer.getServerString(key);
		if (string != null) {
			return string;
		}
		// A second attempt to load not loaded res.
		for (int i = 0; i < baseResources.size(); i++) {
			String fileName = baseResources.get(i);
			if (textServer.hasLoaded(fileName)) {
				continue;
			}
			try {
				textServer.loadResource(fileName, locale);
			} catch (IOException e) {
				Logs.catching(e);
			}
			string = textServer.getServerString(key);
			if (string != null) {
				return string;
			}
		}
		// Finally the string was not found.
		return null;
	}

	/**
	 * Adds a base resource to the list of base res.
	 *
	 * @param fileName The base resource to add.
	 */
	static void addBaseTextResource(String fileName) {
		if (!baseResources.contains(fileName)) {
			baseResources.add(fileName);
		}
	}

	/**
	 * Returns the base text server.
	 *
	 * @return The base text server.
	 */
	static TextServer getBaseTextServer() {
		return baseTextServer;
	}

	/**
	 * Returns the localized text server.
	 *
	 * @param locale The locale to use.
	 * @return The localized text server.
	 */
	static TextServer getLocalizedTextServer(Locale locale) {
		TextServer textServer = localizedTextServers.get(locale);
		if (textServer == null) {
			textServer = new TextServer();
			localizedTextServers.put(locale, textServer);
		}
		return textServer;
	}

	/**
	 * The loaded properties.
	 */
	private final Properties textProperties = new Properties();
	/**
	 * List of res loaded in this server.
	 */
	private final List<String> res = new ArrayList<>();

	/**
	 * Default constructor.
	 */
	private TextServer() {
		super();
	}

	/**
	 * Check if the server has loaded the given resource.
	 *
	 * @param fileName The file name of the text resource.
	 * @return A boolean.
	 */
	private boolean hasLoaded(String fileName) {
		return res.contains(fileName);
	}

	/**
	 * Gets a string searching by key.
	 *
	 * @param key The key to search.
	 * @return The string.
	 */
	private String getServerString(String key) {
		return textProperties.getProperty(key);
	}

	/**
	 * Load a resource file, either a normal properties file, or a zipped file with many properties files.
	 *
	 * @param fileName The absolute file name.
	 * @param locale   The locale or null to load base res.
	 * @throws IOException If an IO error occurs.
	 */
	private void loadResource(String fileName, Locale locale) throws IOException {
		// Check the resource to load
		if (hasLoaded(fileName)) {
			return;
		}
		// Separate name and extension.
		String ext = Files.getFileExtension(fileName);
		// Check compressed.
		boolean zipped = (ext.equalsIgnoreCase("zip") || ext.equalsIgnoreCase("jar"));
		if (!zipped) {
			loadResourceStd(fileName, locale);
		} else {
			loadResourceZip(fileName, locale);
		}
		res.add(fileName);
	}

	/**
	 * Load a normal resource file.
	 *
	 * @param fileName The absolute file name.
	 * @param locale   The locale or null to load base res.
	 * @throws IOException If an IO error occurs.
	 */
	private void loadResourceStd(String fileName, Locale locale) throws IOException {
		String name = Files.getFileName(fileName);
		String ext = Files.getFileExtension(fileName);
		File file = Files.getLocalizedFile(locale, name, ext);
		if (file != null) {
			Properties properties = Paths.getProperties(file);
			mergeResources(properties);
		}
	}

	/**
	 * Load the res from the zip file, taking only those that apply the locale.
	 *
	 * @param fileName The absolute file name.
	 * @param locale   The locale or null to load base res.
	 * @throws IOException If an IO error occurs.
	 */
	private void loadResourceZip(String fileName, Locale locale) throws IOException {
		File file = Files.getFileFromClassPathEntries(fileName);
		FileInputStream fis = new FileInputStream(file);
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
		ZipEntry entry;
		while ((entry = zis.getNextEntry()) != null) {
			String name = entry.getName();
			boolean merge = false;
			if (locale != null && isLocalizedResource(name, locale)) {
				merge = true;
			}
			if (locale == null && isBaseResource(name)) {
				merge = true;
			}
			if (merge) {
				mergeResources(Paths.getProperties(zis));
			}
		}
		zis.close();
		fis.close();
	}

	/**
	 * Check if a resource name is a base name.
	 *
	 * @param resourceName The resource name.
	 * @return A boolean that indicates if the resoource is a base resource.
	 */
	private boolean isBaseResource(String resourceName) {
		String name = Files.getFileName(resourceName);
		if (name.charAt(name.length() - 3) == '_') {
			return false;
		}
		return true;
	}

	/**
	 * Check if a resource is localized.
	 *
	 * @param resourceName The resource name.
	 * @param locale       The locale.
	 * @return A boolean that indicates if the resoource is a base resource.
	 */
	private boolean isLocalizedResource(String resourceName, Locale locale) {
		String name = Files.getFileName(resourceName);
		String language_country = locale.getLanguage() + "_" + locale.getCountry();
		String language = locale.getLanguage();
		return name.endsWith(language_country) || name.endsWith(language);
	}

	/**
	 * Merge the incoming properties with this text server properties.
	 *
	 * @param properties The properties to merge with this text server.
	 */
	private void mergeResources(Properties properties) {
		Enumeration<Object> e = properties.keys();
		while (e.hasMoreElements()) {
			Object key = e.nextElement();
			if (!textProperties.containsKey(key)) {
				textProperties.put(key, properties.get(key));
			}
		}
	}

}
