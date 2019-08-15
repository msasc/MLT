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

import javax.swing.text.BadLocationException;

import com.mlt.desktop.layout.Pad;
import com.mlt.util.Strings;

/**
 * A basic filter for string text fields. Sets a masker of the incoming text, an
 * optional maximum length, and optional padding conditions if there is a
 * maximum length.
 *
 * @author Miquel Sas
 */
public class StringFilter extends TextFieldFilter {

	/** Lowercase masker. */
	public static final MaskerCase LOWERCASE = new MaskerCase(Strings.Modifier.LOWER);
	/** Uppercase masker. */
	public static final MaskerCase UPPERCASE = new MaskerCase(Strings.Modifier.UPPER);

	/**
	 * Masker interface.
	 */
	public static interface Masker {
		/**
		 * Mask the incoming text and transform it into the desired text.
		 * 
		 * @param text The incoming text.
		 * @return The desired text.
		 */
		String mask(String text);
	}

	/**
	 * Uppercase-lowercase masker.
	 */
	public static class MaskerCase implements Masker {

		/** Case modifier. */
		private Strings.Modifier modifier = Strings.Modifier.NONE;

		/**
		 * Constructor.
		 * 
		 * @param modifier The case modifier.
		 */
		public MaskerCase(Strings.Modifier modifier) {
			super();
			this.modifier = (modifier == null ? Strings.Modifier.NONE : modifier);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String mask(String text) {
			switch (modifier) {
			case NONE:
				return text;
			case LOWER:
				return text.toLowerCase();
			case UPPER:
				return text.toUpperCase();
			default:
				throw new IllegalStateException();
			}
		}

	}

	/**
	 * A masker that does none.
	 */
	public static class MaskerNone implements Masker {

		/**
		 * Constructor.
		 */
		private MaskerNone() {
			super();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String mask(String text) {
			return text;
		}
	}

	/** Masker. */
	private Masker masker;
	/** Maximum length. */
	private int maximumLength = -1;
	/** Pad side. */
	private Pad padSide;
	/** Pad string. */
	private String padString;

	/**
	 * Constructor.
	 * 
	 * @param masker Text masker.
	 */
	public StringFilter(Masker masker) {
		super();
		this.masker = masker;
	}

	/**
	 * Constructor setting only the maximum length with a none masker.
	 * 
	 * @param maximumLength The maximum length.
	 */
	public StringFilter(int maximumLength) {
		super();
		this.masker = new MaskerNone();
		this.maximumLength = maximumLength;
	}

	/**
	 * Constructor.
	 * 
	 * @param masker        Text masker.
	 * @param maximumLength Maximum length, -1 for no limit.
	 */
	public StringFilter(Masker masker, int maximumLength) {
		super();
		this.masker = masker;
		this.maximumLength = maximumLength;
	}

	/**
	 * Constructor.
	 * 
	 * @param masker        Text masker.
	 * @param maximumLength Maximum length, -1 for no limit.
	 * @param padSide       Pad side, null for non pad.
	 * @param padString     Pad string, default is space.
	 */
	public StringFilter(Masker masker, int maximumLength, Pad padSide, String padString) {
		super();
		this.masker = masker;
		this.maximumLength = maximumLength;
		this.padSide = padSide;
		this.padString = padString;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void docRemove(int offset, int length) throws BadLocationException {
		super.docRemove(offset, length);
		formatDoc();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void docReplace(int offset, int length, String text) throws BadLocationException {
		super.docReplace(offset, length, masker.mask(text));
		formatDoc();
	}

	/**
	 * Format the document after removing or replacing the masked text, applying
	 * maximum length or padding if applicable.
	 * 
	 * @throws BadLocationException
	 */
	private void formatDoc() throws BadLocationException {
		if (maximumLength > 0) {

			String text = getText();
			int caretPos = getCaretPosition();

			/* No padding, just ensure the length. */
			if (padSide == null) {
				if (text.length() > maximumLength) {
					rawRemove(maximumLength, text.length() - maximumLength);
				}
				return;
			}

			/* Padding, apply it. */
			String padString = (this.padString == null ? " " : this.padString);
			if (padSide == Pad.LEFT) {
				text = Strings.leftPad(text, maximumLength, padString);
			} else {
				text = Strings.rightPad(text, maximumLength, padString);
			}
			rawReplace(0, getLength(), text);
			caretPos = Math.min(maximumLength, caretPos);
			setCaretPosition(caretPos);
		}
	}
}
