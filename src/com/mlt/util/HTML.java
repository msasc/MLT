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

import java.awt.Color;

/**
 * Helper to build HTML texts to include in controls.
 *
 * @author Miquel Sas
 */
public class HTML {

	/**
	 * Internal buffer.
	 */
	private StringBuilder b;

	/**
	 * Constructor.
	 */
	public HTML() {
		super();
		b = new StringBuilder();
	}

	/**
	 * Append a string to the current paragraph.
	 *
	 * @param str The string.
	 */
	public void append(String str) {
		b.append(str);
	}

	/**
	 * Append a number to the current paragraph.
	 *
	 * @param str The string.
	 */
	public void append(Number number) {
		b.append(number);
	}

	/**
	 * Append a string with a CSS style, to the current paragraph.
	 *
	 * @param str   The string.
	 * @param style The CSS style, not null.
	 */
	public void append(String str, String style) {
		HTML.this.startStyle(style);
		b.append(str);
		endStyle();
	}

	/**
	 * Append a string with a CSS style, to the current paragraph.
	 *
	 * @param str   The string.
	 * @param style The CSS style, not null.
	 */
	public void append(String str, String... styles) {
		startStyle(styles);
		b.append(str);
		endStyle();
	}

	/**
	 * Append the string with a color.
	 *
	 * @param str   The string.
	 * @param color The color.
	 */
	public void append(String str, Color color) {
		append(str, Colors.toCSS(color));
	}

	/**
	 * Clear the buffer.
	 */
	public void clear() {
		b.delete(0, b.length());
	}

	/**
	 * Check whether this buffer is empty.
	 *
	 * @return A boolean.
	 */
	public boolean isEmpty() {
		return Strings.isEmpty(b);
	}

	/**
	 * Append a new line.
	 */
	public void newLine() {
		b.append("<br>");
	}

	/**
	 * Append a paragraph.
	 */
	public void paragraph() {
		b.append("<p>");
	}

	/**
	 * Append a separator.
	 */
	public void separator() {
		b.append("<hr>");
	}

	/**
	 * Start the style that will apply to the subsequent text.
	 *
	 * @param style The style.
	 */
	public void startStyle(String style) {
		b.append("<span style=\"");
		b.append(style);
		b.append("\">");
	}

	/**
	 * Start the styles that will apply to the subsequent text.
	 *
	 * @param styles The list of style.
	 */
	public void startStyle(String... styles) {
		b.append("<span style=\"");
		for (int i = 0; i < styles.length; i++) {
			if (i > 0) {
				b.append(";");
			}
			b.append(styles[i]);
		}
		b.append("\">");
	}

	/**
	 * End the style previously started.
	 */
	public void endStyle() {
		b.append("</span>");
	}

	/**
	 * Start a tag.
	 *
	 * @param tag The HTML tag.
	 */
	public void startTag(String tag) {
		b.append("<");
		b.append(tag);
		b.append(">");
	}

	/**
	 * Start a tag.
	 *
	 * @param tag   The HTML tag.
	 * @param style The style.
	 */
	public void startTag(String tag, String style) {
		b.append("<");
		b.append(tag);
		b.append(" style:\"");
		b.append(style);
		b.append("\">");
	}

	/**
	 * End a tag.
	 *
	 * @param tag The HTML tag.
	 */
	public void endTag(String tag) {
		b.append("</");
		b.append(tag);
		b.append(">");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return b.toString();
	}

	/**
	 * Return the HTML string.
	 *
	 * @param htmlTag A boolean that indicates whether the HTML prefix and suffix should be added.
	 * @return The HTML string.
	 */
	public String toString(boolean htmlTag) {
		StringBuilder html = new StringBuilder();
		if (htmlTag) {
			html.append("<html>");
		}
		html.append(b.toString());
		if (htmlTag) {
			html.append("</html>");
		}
		return html.toString();
	}
}
