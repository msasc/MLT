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
package com.mlt.desktop.formatter;

import java.text.DecimalFormatSymbols;

import javax.swing.text.BadLocationException;

import com.mlt.desktop.layout.Direction;
import com.mlt.util.Strings;

/**
 * Document and navigation number filter.
 *
 * @author Miquel Sas
 */
public class NumberFilter extends TextFieldFilter {

	/**
	 * Enumerates the types of number formats.
	 */
	public static enum Type {
		/**
		 * Locale. Uses localized decimal and grouping separators.
		 */
		LOCALE,
		/**
		 * Plain mathematical notation, no grouping, the dot as decimal separator and
		 * not exponential.
		 */
		PLAIN,
		/**
		 * Exponential mathematical notation, with the dot as decimal separator.
		 */
		EXPONENTIAL;
	}

	/** Number of maximum fractional digits, less than zero for no limit. */
	private int decimalDigits = -1;
	/** Number of maximum integer digits, less than zero for no limit. */
	private int integerDigits = -1;
	/** Format type. */
	private Type type = Type.LOCALE;

	/** Remove direction. */
	private transient Direction removeDirection;

	/**
	 * Constructor.
	 * 
	 * @param type          The type.
	 * @param integerDigits Integer digits. Possible values are -1 or any negative,
	 *                      to indicate no limit in the number of integer digits,
	 *                      and any number greater than zero to indicate the limit
	 *                      in the number of integer digits. Zero is not allowed.
	 * @param decimalDigits Fractional digits. Possible values are -1 or negative to
	 *                      indicate no limit in the number of decimal digits, zero
	 *                      to indicate no decimal digits, and any positive number
	 *                      to set the limit of decimal digits.
	 */
	public NumberFilter(Type type, int integerDigits, int decimalDigits) {
		super();

		/* Validate. */
		if (type == null) {
			throw new NullPointerException();
		}
		if (type == Type.EXPONENTIAL) {
			throw new IllegalArgumentException("Type EXPONENTIAL not already suppoorted");
		}
		if (integerDigits == 0) {
			throw new IllegalArgumentException(
				"The number of integer digits must be negative (-1) or positive");
		}

		this.type = type;
		this.integerDigits = integerDigits;
		this.decimalDigits = decimalDigits;
	}

	/**
	 * Format and set the document text.
	 * 
	 * @throws BadLocationException
	 */
	private void formatDoc() throws BadLocationException {

		/* Backup current document text and caret position. */
		String doc = getText();
		int pos = getCaretPosition();

		/* Retrieve the sign. */
		String txtSig = (Strings.startsWith(doc, "-") ? "-" : "");

		/* Current document text without grouping separators and sign. */
		String txt = Strings.remove(Strings.remove(doc, getGroupingSeparator()), "-");

		/* If decimal digits are zero, ensure there is no decimal portion. */
		if (decimalDigits == 0) {
			int index = Strings.indexOf(txt, getDecimalSeparator());
			if (index >= 0) {
				txt = Strings.substring(txt, 0, index);
			}
		}

		/* The integer part. If the result is empty, set it zero. */
		String txtInt = getIntegerPart(txt);

		/* If the integer text is empty, then set it to "0". */
		if (txtInt.isEmpty()) {
			txtInt = "0";
		}

		/* Decimal part. */
		String txtDec = getDecimalPart(txt);

		/* Right pad the decimal part if required. */
		if (decimalDigits > 0) {
			txtDec = Strings.rightPad(txtDec, decimalDigits, "0");
		}

		/* Shrink the integer part if it is greater than the length. */
		if (integerDigits > 0 && Strings.length(txtInt) > integerDigits) {
			txtInt = Strings.leftPad(txtInt, integerDigits, "0");
		}
		/* Remove leading zeros of the integer part. */
		while (Strings.length(txtInt) > 1 && Strings.substring(txtInt, 0, 1).equals("0")) {
			txtInt = Strings.substring(txtInt, 1);
		}
		/* If grouping is used, set the group separators to the integer part. */
		if (type == Type.LOCALE) {
			txtInt = insertGroupingSeparator(txtInt);
		}

		/* Rebuild the text. */
		txt = rebuildDoc(txtSig, txtInt, txtDec);

		/* Raw replace the document text. */
		rawReplace(0, Strings.length(doc), txt);

		/* Calculate and set the new caret position. */
		int caretPos = getCaretPos(pos, doc, txt);
		setCaretPosition(caretPos);
	}

	/**
	 * Calculate the new caret position.
	 * 
	 * @param pos Caret position backup.
	 * @param doc Document text backup.
	 * @param txt Current formatted text.
	 * @return The caret position that should be set.
	 * @throws BadLocationException
	 */
	private int getCaretPos(int pos, String doc, String txt) throws BadLocationException {
		/* If it is the start of edition, only one char and position 1, leave it. */
		if (pos == 1 && Strings.length(doc) == 1) {
			return pos;
		}
		/* If it is a remove with defined direction, leave it. */
		if (removeDirection != null) {
			return pos;
		}
		/* Same relative position from the right. */
		int rightChars = doc.length() - pos;
		return txt.length() - rightChars;
	}

	/**
	 * Return the number of decimal digits.
	 * 
	 * @return The number of decimal digits.
	 */
	public int getDecimalDigits() {
		return decimalDigits;
	}

	/**
	 * Return the decimal part.
	 * 
	 * @param txt The source text, normally without grouping separators.
	 * @return The decimal part.
	 */
	private String getDecimalPart(String txt) {
		int index = Strings.indexOf(txt, getDecimalSeparator());
		if (index >= 0) return Strings.substring(txt, index + 1);
		return "";
	}

	/**
	 * Return the decimal separator.
	 * 
	 * @return The decimal separator.
	 */
	private String getDecimalSeparator() {
		if (type != Type.LOCALE) return ".";
		return Strings.valueOf(DecimalFormatSymbols.getInstance().getDecimalSeparator());
	}

	/**
	 * Return the grouping separator.
	 * 
	 * @return The grouping separator.
	 */
	private String getGroupingSeparator() {
		return Strings.valueOf(DecimalFormatSymbols.getInstance().getGroupingSeparator());
	}

	/**
	 * Return the number of integer digits.
	 * 
	 * @return The number of integer digits.
	 */
	public int getIntegerDigits() {
		return integerDigits;
	}

	/**
	 * Return the integer part, after removing any grouping separator.
	 * 
	 * @param txt The source text, normally without grouping separators.
	 * @return The integer part.
	 */
	private String getIntegerPart(String txt) {
		txt = Strings.remove(txt, getGroupingSeparator());
		int index = Strings.indexOf(txt, getDecimalSeparator());
		if (index >= 0) return Strings.substring(txt, 0, index);
		return txt;
	}

	/**
	 * Return the type.
	 * 
	 * @return The type.
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Return the list of valid characters in the string to be inserted.
	 * 
	 * @return The list of valid characters
	 */
	private String getValidChars() {
		StringBuilder b = new StringBuilder();
		b.append("-0123456789");
		if (decimalDigits != 0) {
			b.append(getDecimalSeparator());
		}
		if (type == Type.LOCALE) {
			b.append(getGroupingSeparator());
		}
		return b.toString();
	}

	/**
	 * Insert grouping separators to the string that is the integer part.
	 * 
	 * @param str The integer part string.
	 * @return The integer part with grouping separators.
	 */
	private String insertGroupingSeparator(String str) {
		StringBuilder b = new StringBuilder();
		int count = 0;
		for (int i = str.length() - 1; i >= 0; i--) {
			if (count == 3) {
				b.append(getGroupingSeparator());
				count = 0;
			}
			b.append(str.charAt(i));
			count++;
		}
		return Strings.reverse(b.toString());
	}

	/**
	 * Rebuild the final text.
	 * 
	 * @param txtSig The sign.
	 * @param txtInt The integer part.
	 * @param txtDec The decimal part.
	 * @return The rebuilt document.
	 * @throws BadLocationException
	 */
	private String rebuildDoc(
		String txtSig,
		String txtInt,
		String txtDec) throws BadLocationException {
		StringBuilder b = new StringBuilder();
		b.append(txtSig);
		b.append(txtInt);
		if (!txtDec.isEmpty() || Strings.contains(getText(), getDecimalSeparator())) {
			b.append(getDecimalSeparator());
			b.append(txtDec);
		}
		return b.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void docRemove(int offset, int length) throws BadLocationException {

		/*
		 * Register the remove direction, required to set the proper caret position
		 * after formatting.
		 */
		if (length == 1) {
			if (offset < getCaretPosition()) {
				removeDirection = Direction.LEFT;
			} else {
				removeDirection = Direction.RIGHT;
			}
		} else {
			removeDirection = null;
		}

		/*
		 * If the remove direction is left, and the previous char is the decimal
		 * separator, move to the previous caret position and remove from the previous
		 * offset.
		 */
		if (removeDirection == Direction.LEFT) {
			if (Strings.equals(getPrevChar(getCaretPosition()), getDecimalSeparator())) {
				setCaretPosition(getCaretPosition() - 1);
				docRemove(offset - 1, length);
				return;
			}
		}

		/*
		 * Next two chars '0.', do move and remove.
		 */
		if (removeDirection == Direction.RIGHT) {
			if (Strings.equals(getNextChars(offset, 2), "0" + getDecimalSeparator())) {
				setCaretPosition(getCaretPosition() + 2);
				docRemove(offset + 2, length);
				return;
			}
		}

		/*
		 * Next char is the decimal separator, do move and remove.
		 */
		if (removeDirection == Direction.RIGHT) {
			if (Strings.equals(getNextChar(offset), getDecimalSeparator())) {
				setCaretPosition(getCaretPosition() + 1);
				docRemove(offset + 1, length);
				return;
			}
		}

		/*
		 * Raw remove.
		 */
		rawRemove(offset, length);

		/*
		 * Format the document.
		 */
		formatDoc();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void docReplace(int offset, int length, String text) throws BadLocationException {

		/*
		 * Register the remove direction, required to set the proper caret position
		 * after formatting.
		 */
		removeDirection = null;

		/*
		 * The string must contain only valid chars. Valid chars are the digits, the
		 * minus sign and the decimal separator.
		 */
		if (!Strings.containsOnly(text, getValidChars())) {
			return;
		}

		/*
		 * If there is a minus sign in the replace string, there must be only one, it
		 * must be the first and the operation must be a replace, that is, the offset
		 * must be zero and the length the document length.
		 */
		if (Strings.contains(text, "-")) {
			if (!Strings.startsWith(text, "-")) {
				return;
			}
			if (Strings.countMatches(text, "-") > 1) {
				return;
			}
			if (offset != 0 || length != Strings.length(getText())) {
				return;
			}
		}

		/*
		 * Check whether a change of sign should be performed, when the string is
		 * exactly the minus sign and it is an insert operation (length == 0).
		 */
		if (Strings.equals(text, "-") && length == 0) {
			if (Strings.startsWith(getText(), "-")) {
				rawRemove(0, 1);
			} else {
				rawReplace(0, 0, "-");
			}
			return;
		}

		/*
		 * Check whether the caret should be moved after the decimal separator, when the
		 * string to insert is exactly the decimal separator, the current document text
		 * contains it, and it is an insert operation (length == 0).
		 */
		if (Strings.equals(text, getDecimalSeparator()) && length == 0) {
			if (Strings.contains(getText(), getDecimalSeparator())) {
				setCaretPosition(Strings.indexOf(getText(), getDecimalSeparator()) + 1);
				return;
			}
		}

		/*
		 * Check whether the caret should be moved one position forward (to the right),
		 * and the replace called again from there, because the next char is the
		 * grouping separator and it is an insert operation (length == 0).
		 */
		String nextChar = Strings.substring(getText(), offset, offset + 1);
		if (Strings.equals(nextChar, getGroupingSeparator()) && length == 0) {
			setCaretPosition(offset + 1);
			docReplace(offset + 1, length, text);
			return;
		}

		/*
		 * Check whether the caret should be moved one position forward (to the right),
		 * and the replace called again from there, because the next char is the decimal
		 * separator and it is an insert operation (length == 0), and the integer part
		 * of the document text has reached the maximum length.
		 */
		if (Strings.equals(nextChar, getDecimalSeparator()) && length == 0) {
			if (integerDigits > 0 && getIntegerPart(getText()).length() >= integerDigits) {
				setCaretPosition(offset + 1);
				docReplace(offset + 1, length, text);
				return;
			}
		}

		/*
		 * If the offset is at the end of the document text, do nothing when there
		 * should be a decimal part and it is full, or there is not and not should be a
		 * decimal part, and the integer part is full.
		 */
		if (offset >= getText().length() && length == 0) {
			if (decimalDigits > 0 && getDecimalPart(getText()).length() >= decimalDigits) {
				return;
			}
			if (integerDigits > 0 &&
				decimalDigits == 0 &&
				getIntegerPart(getText()).length() >= integerDigits) {
				return;
			}
		}

		/*
		 * Check whether should overwrite and if so, perform a raw overwrite, otherwise
		 * perform a raw replace.
		 */
		if (shouldOverwrite(offset, length, text)) {
			rawReplace(offset, Strings.length(text), text);
		} else {
			rawReplace(offset, length, text);
		}

		/*
		 * Format the document.
		 */
		formatDoc();
	}

	/**
	 * Check whether an overwrite must be performed in stead of an insert.
	 * 
	 * @param offset The offset.
	 * @param length The length to replace.
	 * @param str    The string to replace.
	 * @return A boolean indicating whether overwrite or replace.
	 * @throws BadLocationException
	 */
	private boolean shouldOverwrite(
		int offset,
		int length,
		String str) throws BadLocationException {
		/*
		 * Never overwrite when it is a replace operation (length != 0).
		 */
		if (length != 0) {
			return false;
		}

		/*
		 * Retrieve the position of the decimal separator in the document text. It will
		 * be used to check whether the offset is in the decimal or integer part. All
		 * this if str is one char length.
		 */
		int indexSep = Strings.indexOf(getText(), getDecimalSeparator());
		boolean oneChar = (str.length() == 1);

		/*
		 * If the offset is in the decimal part of a text with decimal separator, and
		 * not at the end, do overwrite.
		 */
		if (oneChar && indexSep >= 0 &&
			offset > indexSep &&
			offset < getText().length()) {
			return true;
		}

		/*
		 * If the offset is in the integer part of a text with decimal separator, and
		 * not just before it, do overwrite.
		 */
		if (oneChar && indexSep > 0 && offset < indexSep) {
			return true;
		}

		/*
		 * Always insert when next character is the decimal separator.
		 */
		if (Strings.startsWith(Strings.substring(getText(), offset), getDecimalSeparator())) {
			return false;
		}

		/*
		 * Current document text without grouping separators.
		 */
		String txt = Strings.remove(getText(), getGroupingSeparator());
		/*
		 * If the document is empty must insert.
		 */
		if (Strings.isEmpty(txt)) {
			return false;
		}
		/*
		 * Only when the string is one character length.
		 */
		if (Strings.length(str) > 1) {
			return false;
		}
		/*
		 * Only when the required operation is an insert, the length is zero.
		 */
		if (length != 0) {
			return false;
		}
		/*
		 * If only integer part and the integer part has no limit or is not full, and we
		 * are at the end of the text, should insert.
		 */
		if (decimalDigits <= 0) {
			/* Must be at the end. */
			if (offset == txt.length()) {
				/* No limit on the integer part. */
				if (integerDigits <= 0) {
					return false;
				}
				/* Integer part not full. */
				if (txt.length() < integerDigits) {
					return false;
				}
			}
		}

		/* Overwrite. */
		return true;
	}

}
