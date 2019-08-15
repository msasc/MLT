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

import com.mlt.desktop.layout.Direction;
import com.mlt.desktop.layout.Pad;
import com.mlt.util.Strings;

/**
 * Mask filter that extends the segment filter. Mask segments are always fixed
 * length and thus padded, by default right padded with spaces.
 *
 * @author Miquel Sas
 */
public class MaskFilter extends SegmentFilter {

	/**
	 * Masker interface.
	 */
	public static interface Masker {

		/**
		 * Check whether the incoming character should be accepted.
		 * 
		 * @param c The character to check.
		 * @return A boolean.
		 */
		boolean accept(char c);

		/**
		 * Return the character properly masked .
		 * 
		 * @param c The character to mask.
		 * @return The masked character.
		 */
		char mask(char c);
	}

	/**
	 * Masker using standard types.
	 */
	private class MaskerType implements Masker {

		/** Type. */
		private Strings.Type type;
		/** Case modifier. */
		private Strings.Modifier modifier;
		/** Characters to force exclude. */
		private String exclude;
		/** Characters included a part from those include by the type. */
		private String include;

		/**
		 * Constructor.
		 */
		private MaskerType() {
			super();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean accept(char c) {
			if (c < 32) {
				return false;
			}
			if (exclude != null) {
				if (Strings.contains(exclude, c)) {
					return false;
				}
			}
			if (include != null) {
				if (Strings.contains(include, c)) {
					return true;
				}
			}
			switch (type) {
			case DIGIT:
				if (Character.isDigit(c)) return true;
				break;
			case ALPHA_NUM:
				if (Character.isAlphabetic(c)) return true;
				if (Character.isDigit(c)) return true;
				break;
			case ANY:
				if (c >= 32) return true;
				break;
			case LETTER:
				if (Character.isLetter(c)) return true;
				break;
			case LETTER_OR_DIGIT:
				if (Character.isLetterOrDigit(c)) return true;
				break;
			}
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public char mask(char c) {
			if (include != null) {
				if (Strings.contains(include, c)) {
					return c;
				}
			}
			switch (type) {
			case DIGIT:
				return c;
			case ALPHA_NUM:
			case ANY:
			case LETTER:
			case LETTER_OR_DIGIT:
				if (modifier != null) {
					if (modifier == Strings.LOWERCASE) {
						c = Character.toLowerCase(c);
					}
					if (modifier == Strings.UPPERCASE) {
						c = Character.toUpperCase(c);
					}
				}
				return c;
			}
			throw new IllegalStateException();
		}
	}

	/**
	 * Mask segment.
	 */
	private class MaskSegment extends Segment {

		/** Masker. */
		private Masker masker;
		/** Length. */
		private int length;
		/** Pad side. */
		private Pad padSide;
		/** Pad string. */
		private String padString;

		/** Internal buffer. */
		private StringBuilder buffer = new StringBuilder();

		/**
		 * Constructor.
		 * 
		 * @param masker    Masker.
		 * @param length    Length.
		 * @param padSide   Pad side.
		 * @param padString Pad string.
		 */
		private MaskSegment(Masker masker, int length, Pad padSide, String padString) {
			super();
			if (masker == null) {
				throw new NullPointerException();
			}
			if (length <= 0) {
				throw new IllegalArgumentException("Invalid length");
			}
			if (padSide == null) {
				padSide = Pad.RIGHT;
			}
			if (padString == null) {
				padString = " ";
			}
			this.masker = masker;
			this.length = length;
			this.padSide = padSide;
			this.padString = padString;

			formatDoc();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected boolean acceptRemove(int offset, Direction direction) {
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected boolean acceptReplace(int offset, char c) {
			return (masker.accept(c) && offset < length);
		}

		/**
		 * Format the segment document by padding it.
		 */
		private void formatDoc() {
			String text = buffer.toString();
			if (padSide == Pad.LEFT) {
				text = Strings.leftPad(text, length, padString);
			} else {
				text = Strings.rightPad(text, length, padString);
			}
			buffer.replace(0, buffer.length(), text);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String getText() {
			return buffer.toString();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected int remove(int offset, Direction direction) {
			buffer.delete(offset, offset + 1);
			formatDoc();
			return 0;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected int replace(int offset, char c) {
			buffer.replace(offset, offset + 1, Strings.valueOf(masker.mask(c)));
			formatDoc();
			return 1;
		}

	}

	/**
	 * Constructor.
	 */
	public MaskFilter() {
		super();
	}

	/**
	 * Add a mask segment.
	 * 
	 * @param masker    Generic masker.
	 * @param length    Length.
	 * @param padSide   Pad side.
	 * @param padString Pad string.
	 */
	public void addMasked(Masker masker, int length, Pad padSide, String padString) {
		addSegment(new MaskSegment(masker, length, padSide, padString));
	}

	/**
	 * Add a masked segment using standard string types and modifiers.
	 * 
	 * @param type      String type.
	 * @param modifier  String modifier.
	 * @param length    Length.
	 * @param padSide   Pad side.
	 * @param padString Pad string.
	 * @param exclude   Exclude characters.
	 * @param include   Include characters.
	 */
	public void addMasked(
		Strings.Type type,
		Strings.Modifier modifier,
		int length,
		Pad padSide,
		String padString,
		String exclude,
		String include) {

		MaskerType masker = new MaskerType();
		masker.type = type;
		masker.modifier = modifier;
		masker.exclude = exclude;
		masker.include = include;

		addSegment(new MaskSegment(masker, length, padSide, padString));
	}

	/**
	 * Add a fixed segment.
	 * 
	 * @param text The text.
	 */
	public void addFixed(String text) {
		addSegment(new Separator(text));
	}
}
