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
package com.mlt.util.xml.parser;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.mlt.util.Strings;

/**
 * @author Miquel Sas
 */
public class ParserHandler {

	private static int getAttributeIndex(Attributes attributes, String name) {
		for (int i = 0; i < attributes.getLength(); i++) {
			if (attributes.getQName(i).equals(name)) {
				return i;
			}
		}
		return -1;
	}

	private static PathAttribute getPathAttribute(List<PathAttribute> attrs, String name) {
		for (PathAttribute attr : attrs) {
			if (attr.name.equals(name)) {
				return attr;
			}
		}
		return null;
	}

	/**
	 * The deque with the different objects.
	 */
	private Deque<Object> deque = new ArrayDeque<>();
	/**
	 * Configuration of valid paths and attributes.
	 */
	private HashMap<String, List<PathAttribute>> paths = new HashMap<>();

	/**
	 * Default constructor.
	 */
	public ParserHandler() {
		super();
	}

	/**
	 * Check whether the nme attribute exists.
	 * 
	 * @param attributes SAX attributes.
	 * @param name       Attribute name.
	 * @return A boolean.
	 */
	public boolean exists(Attributes attributes, String name) {
		return attributes.getIndex(name) >= 0;
	}

	/**
	 * Return the value.
	 * 
	 * @param attributes SAX attributes.
	 * @param name       Attribute name.
	 * @return The value.
	 */
	public boolean getBoolean(Attributes attributes, String name) {
		if (attributes.getValue(name).equals("true")) {
			return true;
		}
		if (attributes.getValue(name).equals("false")) {
			return false;
		}
		throw new IllegalStateException();
	}

	/**
	 * Return the value.
	 * 
	 * @param attributes SAX attributes.
	 * @param name       Attribute name.
	 * @return The value.
	 */
	public double getDouble(Attributes attributes, String name) {
		return Double.parseDouble(attributes.getValue(name));
	}

	/**
	 * Return the value.
	 * 
	 * @param attributes SAX attributes.
	 * @param name       Attribute name.
	 * @return The value.
	 */
	public int getInteger(Attributes attributes, String name) {
		return Integer.parseInt(attributes.getValue(name));
	}

	/**
	 * Return the value.
	 * 
	 * @param attributes SAX attributes.
	 * @param name       Attribute name.
	 * @return The value.
	 */
	public int[] getIntegerArray(Attributes attributes, String name) {
		int[] array = null;
		String value = attributes.getValue(name);
		if (value != null) {
			String[] elements = Strings.parse(value, ",");
			array = new int[elements.length];
			for (int i = 0; i < elements.length; i++) {
				array[i] = Integer.parseInt(elements[i]);
			}
		}
		return array;
	}

	/**
	 * Return the value.
	 * 
	 * @param attributes SAX attributes.
	 * @param name       Attribute name.
	 * @return The value.
	 */
	public String getString(Attributes attributes, String name) {
		return attributes.getValue(name);
	}

	/**
	 * Set a path with no attributes to be later validated.
	 * 
	 * @param path The valid path.
	 */
	public void set(String path) {
		paths.put(path, new ArrayList<>());
	}

	/**
	 * Set a path if not already set, and the attribute.
	 * 
	 * @param path The path.
	 * @param name The attribute name.
	 * @param type The attribute type.
	 */
	public void set(String path, String name, String type) {
		set(path, name, type, true);
	}

	/**
	 * Set a path if not already set, and the attribute.
	 * 
	 * @param path     The path.
	 * @param name     The attribute name.
	 * @param type     The attribute type.
	 * @param required A boolean.
	 */
	public void set(String path, String name, String type, boolean required) {
		if (!paths.keySet().contains(path)) {
			paths.put(path, new ArrayList<>());
		}
		PathAttribute attribute = new PathAttribute(name, type, required);
		List<PathAttribute> attributes = paths.get(path);
		if (!attributes.contains(attribute)) {
			attributes.add(attribute);
		}
	}

	/**
	 * Set a path if not already set, and the attribute.
	 * 
	 * @param path   The path.
	 * @param name   The attribute name.
	 * @param type   The attribute type.
	 * @param values Comma separated list of possible values.
	 */
	public void set(String path, String name, String type, String values) {
		if (!paths.keySet().contains(path)) {
			paths.put(path, new ArrayList<>());
		}
		PathAttribute attribute = new PathAttribute(name, type, values);
		List<PathAttribute> attributes = paths.get(path);
		if (!attributes.contains(attribute)) {
			attributes.add(attribute);
		}
	}

	/**
	 * Validate that the path is a valid path and the attributes correspond to the
	 * accepted attributes.
	 * 
	 * @param path       The path.
	 * @param attributes The SAX attributes.
	 * @throws SAXException If not correct.
	 */
	public void validate(String path, Attributes attributes) throws SAXException {

		/* The path must exist. */
		if (!paths.keySet().contains(path)) {
			throw new SAXException("Invalid path: " + path);
		}

		/* Retrieve the list of path attributes and check them. */
		List<PathAttribute> pathAttrs = paths.get(path);

		/* Emptyness. */
		if (pathAttrs.isEmpty() && attributes.getLength() == 0) {
			return;
		}

		/* Required attributes must be present. */
		for (PathAttribute attr : pathAttrs) {
			if (attr.required) {
				int index = getAttributeIndex(attributes, attr.name);
				if (index == -1) {
					throw new SAXException(
						"Required attribute \"" + attr.name + "\" not preesent.");
				}
			}
		}

		/* Validate attributes. */
		for (int i = 0; i < attributes.getLength(); i++) {
			String name = attributes.getQName(i);
			PathAttribute attr = getPathAttribute(pathAttrs, name);
			if (attr == null) {
				throw new SAXException("Attribute \"" + name + "\" is not a valid attribute.");
			}
			String value = attributes.getValue(i);
			attr.validate(value);
		}
	}

	/**
	 * Validate that the path exists in the list of valid paths.
	 * 
	 * @param path The path to validate.
	 * @throws SAXException If the path is not valid.
	 */
	public void validatePath(String path) throws SAXException {
		if (!paths.keySet().contains(path)) {
			throw new SAXException("Invalid path: " + path);
		}
	}

	/**
	 * Receive notification of the beginning of the document.
	 *
	 * @throws SAXException Any SAX exception, possibly wrapping another exception.
	 */
	public void documentStart() throws SAXException {}

	/**
	 * Receive notification of the end of the document.
	 *
	 * @throws SAXException Any SAX exception, possibly wrapping another exception.
	 */
	public void documentEnd() throws SAXException {}

	/**
	 * Receive notification of the start of an element.
	 *
	 * @param namespace   The name space if present.
	 * @param elementName The name of the element, without the prefix.
	 * @param path        The path from the root to the current element.
	 * @param attributes  The attributes attached to the element. If there are no
	 *                    attributes, they are empty.
	 * @throws SAXException Any SAX exception, possibly wrapping another exception.
	 */
	public void elementStart(
		String namespace,
		String elementName,
		String path,
		Attributes attributes) throws SAXException {}

	/**
	 * Receive notification about the body text of an element.
	 * 
	 * @param namespace   The name space if present.
	 * @param elementName The name of the element, without the prefix.
	 * @param path        The path from the root to the current element.
	 * @param text        The text in the body.
	 * @throws SAXException Any SAX exception, possibly wrapping another exception.
	 */
	public void elementBody(
		String namespace,
		String elementName,
		String path,
		String text) throws SAXException {}

	/**
	 * Receive notification about the end of an element.
	 * 
	 * @param namespace   The name space if present.
	 * @param elementName The name of the element, without the prefix.
	 * @param path        The path from the root to the current element.
	 * @throws SAXException Any SAX exception, possibly wrapping another exception.
	 */
	public void elementEnd(String namespace, String elementName, String path) throws SAXException {}

	/**
	 * Returns the deque used to push, peek and pop objects.
	 * 
	 * @return The deque.
	 */
	public Deque<Object> getDeque() {
		return deque;
	}

	/**
	 * Receive notification of a recoverable parser error.
	 * <p>
	 * The default implementation does nothing. Application writers may override
	 * this method in a subclass to take specific actions for each error, such as
	 * inserting the message in a log file or printing it to the console.
	 * </p>
	 *
	 * @param e The error information encoded as an exception.
	 * @throws SAXException Any SAX exception, possibly wrapping another exception.
	 */
	public void error(SAXParseException e) throws SAXException {}

	/**
	 * Report a fatal XML parsing error.
	 * <p>
	 * The default implementation throws a SAXParseException. Application writers
	 * may override this method in a subclass if they need to take specific actions
	 * for each fatal error (such as collecting all of the errors into a single
	 * report): in any case, the application must stop all regular processing when
	 * this method is invoked, since the document is no longer reliable, and the
	 * parser may no longer report parsing events.
	 * </p>
	 *
	 * @param e The error information encoded as an exception.
	 * @throws SAXException Any SAX exception, possibly wrapping another exception.
	 */
	public void fatalError(SAXParseException e) throws SAXException {
		throw e;
	}

	/**
	 * Receive notification of a parser warning.
	 * <p>
	 * The default implementation does nothing. Application writers may override
	 * this method in a subclass to take specific actions for each warning, such as
	 * inserting the message in a log file or printing it to the console.
	 * </p>
	 *
	 * @param e The warning information encoded as an exception.
	 * @throws SAXException Any SAX exception, possibly wrapping another exception.
	 */
	public void warning(SAXParseException e) throws SAXException {}
}
