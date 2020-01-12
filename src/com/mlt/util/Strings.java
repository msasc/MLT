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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * String utilities.
 *
 * @author Miquel Sas
 */
public class Strings {

	/** Sample list of consonants to generate random words. */
	public static final String CONSONANTS_LOWER = "bcdfghjklmnpqrstvwxyz";
	/** Sample list of consonants to generate random words. */
	public static final String CONSONANTS_UPPER = "BCDFGHJKLMNPQRSTVWXYZ";
	/** Sample list of digits to generate random digits. */
	public static final String DIGITS = "0123456789";
	/** Sample list of letters to generate random letters. */
	public static final String LETTERS_LOWER = "abcdefghijklmnopqrstuvwxyz";
	/** Sample list of letters to generate random letters. */
	public static final String LETTERS_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	/** Sample list of vowels to generate random words. */
	public static final String VOWELS_LOWER = "aeiou";
	/** Sample list of vowelss to generate random words. */
	public static final String VOWELS_UPPER = "AEIOU";
	/** A String for a space character. */
	public static final String SPACE = " ";
	/** Represents a failed index search. */
	public static final int INDEX_NOT_FOUND = -1;
	/** Lowercase modifier. */
	public static final Modifier LOWERCASE = Modifier.LOWER;
	/** Uppercase modifier. */
	public static final Modifier UPPERCASE = Modifier.UPPER;
	
	/**
	 * Case modifier.
	 */
	public static enum Modifier {
		NONE, LOWER, UPPER;
	}
	
	/**
	 * Enumerates the types of string masks.
	 */
	public static enum Type {
		/** Alpha numeric with no case restriction. */
		ALPHA_NUM,
		/** Any character with no case restriction. */
		ANY,
		/** Digits. */
		DIGIT,
		/** Letter with no case restriction. */
		LETTER,
		/** Letter or digit with no case restriction. */
		LETTER_OR_DIGIT;
	}

	/**
	 * Capitalize the string.
	 *
	 * @param str The string to capitalize.
	 * @return The capitalized string.
	 */
	public static String capitalize(String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}
		char first = str.charAt(0);
		char firstCap = Character.toUpperCase(first);
		if (first == firstCap) {
			return str;
		}
		StringBuilder b = new StringBuilder();
		b.append(firstCap);
		b.append(str.substring(1));
		return b.toString();
	}

	/**
	 * Concatenate the strings.
	 *
	 * @param strs The list of string to concatenate to the source.
	 * @return The concatenated string.
	 */
	public static String concat(String... strs) {
		StringBuilder b = new StringBuilder();
		if (strs != null) {
			for (int i = 0; i < strs.length; i++) {
				b.append(strs[i]);
			}
		}
		return b.toString();
	}

	/**
	 * Check whether the string contains the search char.
	 *
	 * @param source Source string.
	 * @param search Search char.
	 * @return A boolean.
	 */
	public static boolean contains(String source, char search) {
		return source.contains(String.valueOf(search));
	}

	/**
	 * Check whether the string contains the search string.
	 *
	 * @param source Source string.
	 * @param search Search string.
	 * @return A boolean.
	 */
	public static boolean contains(String source, String search) {
		return source.contains(search);
	}

	/**
	 * Check whether the string containing only the valid chars.
	 *
	 * @param source     The source string.
	 * @param validChars The list of valid chars.
	 * @return A boolean.
	 */
	public static boolean containsOnly(String source, String validChars) {
		for (int i = 0; i < source.length(); i++) {
			if (validChars.indexOf(source.charAt(i)) < 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Count the matches.
	 *
	 * @param source Source string.
	 * @param search Search char.
	 * @return The number of matches.
	 */
	public static int countMatches(String source, char search) {
		if (source.isEmpty()) {
			return 0;
		}
		int count = 0;
		for (int i = 0; i < source.length(); i++) {
			if (search == source.charAt(i)) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Count the matches.
	 *
	 * @param source Source string.
	 * @param search Search string.
	 * @return The number of matches.
	 */
	public static int countMatches(String source, String search) {
		if (isEmpty(source) || isEmpty(search)) {
			return 0;
		}
		int count = 0;
		int idx = 0;
		while ((idx = indexOf(source, search, idx)) != INDEX_NOT_FOUND) {
			count++;
			idx += search.length();
		}
		return count;
	}

	/**
	 * Check for equality.
	 *
	 * @param a String a.
	 * @param b String b.
	 * @return A boolean.
	 */
	public static boolean equals(String a, String b) {
		return a.equals(b);
	}

	/**
	 * Returns the first string not null, or an empty string.
	 *
	 * @param strings The list of strings.
	 * @return The first string not null, or an empty string.
	 */
	public static String getFirstNotNull(String... strings) {
		StringBuilder b = new StringBuilder();
		for (String s : strings) {
			if (s != null) {
				b.append(s);
				break;
			}
		}
		return b.toString();
	}

	/**
	 * Return all the stack trace of the throwable.
	 *
	 * @param e The throwable.
	 * @return The stack trace.
	 */
	public static String getStackTrace(Throwable e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}

	/**
	 * Check if the string is contained in the list of options.
	 *
	 * @param string  The source string.
	 * @param strings The list of strings.
	 * @return A boolean.
	 */
	public static boolean in(String string, String... strings) {
		if (strings == null) {
			return false;
		}
		for (int i = 0; i < strings.length; i++) {
			if (strings[i].equals(string)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the index of the search char.
	 *
	 * @param source Source string.
	 * @param search Search char.
	 * @return The search index.
	 */
	public static int indexOf(String source, char search) {
		return source.indexOf(search);
	}

	/**
	 * Returns the index of the search char.
	 *
	 * @param source    Source string.
	 * @param search    Search char.
	 * @param fromIndex From index.
	 * @return The search index.
	 */
	public static int indexOf(String source, char search, int fromIndex) {
		return source.indexOf(search, fromIndex);
	}

	/**
	 * Returns the index of the search char.
	 *
	 * @param source Source string.
	 * @param search Search string.
	 * @return The search index.
	 */
	public static int indexOf(String source, String search) {
		return source.indexOf(search);
	}

	/**
	 * Returns the index of the search string.
	 *
	 * @param source    Source string.
	 * @param search    Search string.
	 * @param fromIndex From index.
	 * @return The search index.
	 */
	public static int indexOf(String source, String search, int fromIndex) {
		return source.indexOf(search, fromIndex);
	}

	/**
	 * Check whether the string is empty. Null is empty.
	 *
	 * @param str The string.
	 * @return A boolean.
	 */
	public static boolean isEmpty(String str) {
		return (str == null ? true : str.isEmpty());
	}

	/**
	 * Check whether the string builder is empty. Null is empty.
	 *
	 * @param b The string builder.
	 * @return A boolean.
	 */
	public static boolean isEmpty(StringBuilder b) {
		return (b == null ? true : b.length() == 0);
	}

	/**
	 * Left pad a number. Parent Apache does not work as expected.
	 *
	 * @param num  The number to pad out, can not be null.
	 * @param size The size to pad to
	 * @return The left padded string.
	 */
	public static String leftPad(Number num, int size) {
		if (num == null) throw new NullPointerException();
		return leftPad(num.toString(), size, SPACE);
	}

	/**
	 * Left pad a string. Parent Apache does not work as expected.
	 *
	 * @param str  The string to pad out, may be null.
	 * @param size The size to pad to
	 * @return The left padded string.
	 */
	public static String leftPad(String str, int size) {
		return leftPad(str, size, SPACE);
	}

	/**
	 * Left pad a string. Parent Apache does not work as expected.
	 *
	 * @param str    The string to pad out, may be null.
	 * @param size   The size to pad to
	 * @param padChr The char to pad with.
	 * @return The left padded string.
	 */
	public static String leftPad(String str, int size, char padChr) {
		return leftPad(str, size, valueOf(padChr));
	}

	/**
	 * Left pad a string. Parent Apache does not work as expected.
	 *
	 * @param str    The string to pad out, may be null.
	 * @param size   The size to pad to
	 * @param padStr The string to pad with, null or empty treated as single space.
	 * @return The left padded string.
	 */
	public static String leftPad(String str, int size, String padStr) {
		if (str == null) {
			return null;
		}
		if (isEmpty(padStr)) {
			padStr = SPACE;
		}
		while (str.length() < size) {
			str = concat(padStr, str);
		}
		return substring(str, str.length() - size);
	}

	/**
	 * Return the length of the string. Null is zero length.
	 *
	 * @param str The string.
	 * @return The length of the string.
	 */
	public static int length(String str) {
		return (str == null ? 0 : str.length());
	}

	/**
	 * Parse a string
	 *
	 * @param string    The string to parse.
	 * @param separator The separator.
	 * @return the array of tokens
	 */
	public static String[] parse(String string, String separator) {
		StringTokenizer tokenizer = new StringTokenizer(string, separator);
		List<String> list = new ArrayList<>();
		while (tokenizer.hasMoreTokens()) {
			list.add(tokenizer.nextToken().trim());
		}
		return list.toArray(new String[list.size()]);
	}

	/**
	 * Parse and capitalize.
	 *
	 * @param srcStr Source string.
	 * @param srcSep Source separator.
	 * @param dstSep Destination separator.
	 * @return The capitalized string.
	 */
	public static String parseCapitalize(String srcStr, String srcSep, String dstSep) {
		StringBuilder b = new StringBuilder();
		String[] words = parse(srcStr, srcSep);
		for (int i = 0; i < words.length; i++) {
			if (i > 0) {
				b.append(dstSep);
			}
			b.append(capitalize(words[i]));
		}
		return b.toString();
	}

	/**
	 * Remove all incidences.
	 *
	 * @param source The source string.
	 * @param remove The char to remove.
	 * @return The result string.
	 */
	public static String remove(String source, char remove) {
		return source.replace(String.valueOf(remove), "");
	}

	/**
	 * Remove all incidences of the list of characters.
	 *
	 * @param source The source string.
	 * @param remove The characters to remove.
	 * @return The result string.
	 */
	public static String remove(String source, char... remove) {
		String result = source;
		for (char c : remove) {
			result = result.replace(String.valueOf(c), "");
		}
		return result;
	}

	/**
	 * Remove all incidences.
	 *
	 * @param source The source string.
	 * @param remove The string to remove.
	 * @return The result string.
	 */
	public static String remove(String source, String remove) {
		return source.replace(remove, "");
	}

	/**
	 * Remove all incidences of the strings.
	 *
	 * @param source The source string.
	 * @param remove The strings to remove.
	 * @return The result string.
	 */
	public static String remove(String source, String... remove) {
		String result = source;
		for (String s : remove) {
			result = result.replace(s, "");
		}
		return result;
	}

	/**
	 * Returns a string whose value is the concatenation of the argument string
	 * repeated count times.
	 *
	 * @param str   The string to repeat.
	 * @param count The number times.
	 * @return The repeated string.
	 */
	public static String repeat(String str, int count) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < count; i++) {
			b.append(str);
		}
		return b.toString();
	}

	/**
	 * Replaces all incidences.
	 *
	 * @param source  Source string.
	 * @param search  Search char.
	 * @param replace Replace char.
	 * @return The result string.
	 */
	public static String replace(String source, char search, char replace) {
		return source.replace(search, replace);
	}

	/**
	 * Replaces all incidences.
	 *
	 * @param source  Source string.
	 * @param search  Search string.
	 * @param replace Replace string.
	 * @return The result string.
	 */
	public static String replace(String source, String search, String replace) {
		return source.replace(search, replace);
	}

	/**
	 * Reverses a string.
	 *
	 * @param str the String to reverse, may be null
	 * @return the reversed String, {@code null} if null String input
	 */
	public static String reverse(final String str) {
		if (str == null) {
			return null;
		}
		return new StringBuilder(str).reverse().toString();
	}

	/**
	 * Right pad a number. Parent Apache does not work as expected.
	 *
	 * @param num  The number to pad out, can not be null.
	 * @param size The size to pad to
	 * @return The right padded string.
	 */
	public static String rightPad(Number num, int size) {
		if (num == null) throw new NullPointerException();
		return rightPad(num.toString(), size, SPACE);
	}

	/**
	 * Right pad a string. Parent Apache does not work as expected.
	 *
	 * @param str  The string to pad out, may be null.
	 * @param size The size to pad to
	 * @return The right padded string.
	 */
	public static String rightPad(String str, int size) {
		return rightPad(str, size, SPACE);
	}

	/**
	 * Right pad a string. Parent Apache does not work as expected.
	 *
	 * @param str    The string to pad out, may be null.
	 * @param size   The size to pad to
	 * @param padChr The char to pad with.
	 * @return The right padded string.
	 */
	public static String rightPad(String str, int size, char padChr) {
		return rightPad(str, size, valueOf(padChr));
	}

	/**
	 * Right pad a string. Parent Apache does not work as expected.
	 *
	 * @param str    The string to pad out, may be null.
	 * @param size   The size to pad to.
	 * @param padStr The string to pad with, null or empty treated as single space.
	 * @return The right padded string.
	 */
	public static String rightPad(String str, int size, String padStr) {
		if (str == null) {
			return null;
		}
		if (isEmpty(padStr)) {
			padStr = SPACE;
		}
		while (str.length() < size) {
			str = concat(str, padStr);
		}
		return substring(str, 0, size);
	}

	/**
	 * Tests whether the argument string starts with the specified prefix.
	 *
	 * @param str    The string.
	 * @param prefix The prefix.
	 * @return A boolean.
	 */
	public static boolean startsWith(String str, String prefix) {
		return str.startsWith(prefix);
	}

	/**
	 * Return the substring.
	 *
	 * @param str        The source string.
	 * @param beginIndex Begin index.
	 * @return The substring.
	 */
	public static String substring(String str, int beginIndex) {
		beginIndex = Math.max(0, beginIndex);
		beginIndex = Math.min(beginIndex, str.length());
		return str.substring(beginIndex);
	}

	/**
	 * Return the substring.
	 *
	 * @param str        The source string.
	 * @param beginIndex Begin index.
	 * @param endIndex   End index.
	 * @return The substring.
	 */
	public static String substring(String str, int beginIndex, int endIndex) {
		beginIndex = Math.max(0, beginIndex);
		beginIndex = Math.min(beginIndex, str.length());
		endIndex = Math.min(endIndex, str.length());
		return str.substring(beginIndex, endIndex);
	}

	/**
	 * Return the string representation of the vector.
	 * 
	 * @param a The vector.
	 * @return The string representation.
	 */
	public static String toString(int[] a) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < a.length; i++) {
			if (i > 0) {
				b.append(", ");
			}
			b.append(a[i]);
		}
		return b.toString();
	}

	/**
	 * Return the string representation of the vector.
	 * 
	 * @param a The vector.
	 * @return The string representation.
	 */
	public static String toString(double[] a) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < a.length; i++) {
			if (i > 0) {
				b.append(", ");
			}
			b.append(a[i]);
		}
		return b.toString();
	}

	/**
	 * Return the string.
	 *
	 * @param c The char.
	 * @return The string.
	 */
	public static String valueOf(char c) {
		return String.valueOf(c);
	}
}
