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
package com.mlt.util;

import java.awt.Color;

/**
 * Helper to build HTML texts to include in controls.
 *
 * @author Miquel Sas
 */
public class HTML {

	/** Head styles. */
	private StringBuilder headStyles = new StringBuilder();
	/** Body. */
	private StringBuilder body = new StringBuilder();

	/**
	 * Constructor.
	 */
	public HTML() {
		super();
	}

	/**
	 * Add styles to the list of head styles.
	 * 
	 * @param tag    The tag.
	 * @param styles The list of styles.
	 */
	public void addStyles(String tag, String styles) {
		addStyles(tag, null, styles);
	}

	/**
	 * Add styles to the list of head styles.
	 * 
	 * @param tag    The tag.
	 * @param clazz  The class.
	 * @param styles The list of styles.
	 */
	public void addStyles(String tag, String clazz, String styles) {
		if (headStyles.length() > 0) {
			println(headStyles);
		}
		print(headStyles, tag);
		if (clazz != null) {
			print(headStyles, ".");
			print(headStyles, clazz);
		}
		print(headStyles, " {");
		String[] styleList = parse(styles);
		for (String style : styleList) {
			println(headStyles);
			print(headStyles, style);
			print(headStyles, ";");
		}
		println(headStyles, "}");
	}

	/**
	 * End the style previously started.
	 */
	public void endStyle() {
		body.append("</span>");
	}

	/**
	 * End a tag.
	 *
	 * @param tag The HTML tag.
	 */
	public void endTag(String tag) {
		body.append("</");
		body.append(tag);
		body.append(">");
	}

	/**
	 * Check whether this buffer is empty.
	 *
	 * @return A boolean.
	 */
	public boolean isEmpty() {
		return Strings.isEmpty(body);
	}

	/**
	 * Parse the list of styles.
	 * 
	 * @param style The list of styles separated by ";".
	 * @return The array of styles.
	 */
	private String[] parse(String style) {
		String[] styles = Strings.parse(style, ";");
		for (int i = 0; i < styles.length; i++) {
			styles[i] = styles[i].trim();
		}
		return styles;
	}

	/**
	 * Append a number to the current paragraph.
	 *
	 * @param str The string.
	 */
	public void print(Number number) {
		print(body, number);
	}

	/**
	 * Append a string to the current paragraph.
	 *
	 * @param str The string.
	 */
	public void print(String str) {
		print(body, str);
	}

	/**
	 * Append the string with a color.
	 *
	 * @param str   The string.
	 * @param color The color.
	 */
	public void print(String str, Color color) {
		print(str, Colors.toCSS(color));
	}

	/**
	 * Append a string with a CSS style, to the current paragraph.
	 *
	 * @param str   The string.
	 * @param style The CSS style, not null.
	 */
	public void print(String str, String style) {
		startStyle(style);
		print(body, str);
		endStyle();
	}

	/**
	 * Append a string with a CSS style, to the current paragraph.
	 *
	 * @param str   The string.
	 * @param style The CSS style, not null.
	 */
	public void print(String str, String... styles) {
		startStyle(styles);
		print(body, str);
		endStyle();
	}

	/**
	 * Print the object to the buffer.
	 * 
	 * @param b The buffer.
	 * @param o The object.
	 */
	private void print(StringBuilder b, Object o) {
		b.append(o);
	}

	/**
	 * Print a new line to the buffer.
	 * 
	 * @param b The buffer.
	 */
	private void println(StringBuilder b) {
		b.append("\n");
	}

	/**
	 * Print a new line.
	 * 
	 * @param b The string builder.
	 */
	private void println(StringBuilder b, String s) {
		println(b);
		print(b, s);
	}

	/**
	 * Start the style that will apply to the subsequent text.
	 *
	 * @param style The style.
	 */
	public void startStyle(String style) {
		print(body, "<span style=\"");
		print(body, style);
		print(body, "\">");
	}

	/**
	 * Start the styles that will apply to the subsequent text.
	 *
	 * @param styles The list of style.
	 */
	public void startStyle(String... styles) {
		print(body, "<span style=\"");
		for (int i = 0; i < styles.length; i++) {
			if (i > 0) {
				print(body, ";");
			}
			print(body, styles[i]);
		}
		print(body, "\">");
	}

	/**
	 * Start a tag.
	 *
	 * @param tag The HTML tag.
	 */
	public void startTag(String tag) {
		print(body, "<");
		print(body, tag);
		print(body, ">");
	}

	/**
	 * Start a tag.
	 *
	 * @param tag   The HTML tag.
	 * @param style The style.
	 */
	public void startTag(String tag, String style) {
		print(body, "<");
		print(body, tag);
		print(body, " style:\"");
		print(body, style);
		print(body, "\">");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder html = new StringBuilder();
		if (!Strings.isEmpty(headStyles)) {
			print(html, "<!DOCTYPE html>");
			print(html, "<html>");
			print(html, "<head>");
			print(html, "<style>");
			print(html, headStyles.toString());
			print(html, "</style>");
			print(html, "</head>");
			print(html, "<body>");
			print(html, body.toString());
			print(html, "</body>");
			print(html, "</html>");
		} else {
			print(html, "<html>");
			print(html, body.toString());
			print(html, "</html>");
		}
		return html.toString();
	}
}
