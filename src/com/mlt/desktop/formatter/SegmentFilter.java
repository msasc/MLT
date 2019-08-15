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

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.text.BadLocationException;

import com.mlt.desktop.event.KeyHandler;
import com.mlt.desktop.layout.Direction;

/**
 * A filter that delegates removing or replacing text to a list of segments.
 *
 * @author Miquel Sas
 */
public class SegmentFilter extends TextFieldFilter {

	/**
	 * A key listener to notify the proper segment of the key pressed.
	 */
	private class KeyListener extends KeyHandler {
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void keyPressed(KeyEvent e) {

			int caretPos = getCaretPosition();

			/* Ctrl-Left. */
			if (e.getKeyCode() == KeyEvent.VK_LEFT && e.isControlDown()) {
				Segment current = getSegment(caretPos);
				if (current != null) {
					int index = segments.indexOf(current);
					/*
					 * Caret at the begining of the segment (end of previous segment), move to
					 * the begining of previous segment.
					 */
					if (caretPos == getSegmentOffset(current) && index > 0) {
						Segment previous = segments.get(index - 1);
						int offset = getSegmentOffset(previous);
						if (offset >= 0) {
							setCaretPosition(offset);
							e.consume();
							return;
						}
					}
					/*
					 * Move to the begining of the currrent segment.
					 */
					setCaretPosition(getSegmentOffset(current));
					e.consume();
					return;
				}
			}

			/* Ctrl-Right. */
			if (e.getKeyCode() == KeyEvent.VK_RIGHT && e.isControlDown()) {
				Segment segment = getSegment(caretPos);
				if (segment != null) {
					int offset = getSegmentOffset(segment);
					int length = segment.getText().length();
					setCaretPosition(offset + length);
					e.consume();
					return;
				}
			}

			/* Forward the key to the appropiate segment. */
			int offset = 0;
			for (int i = 0; i < segments.size(); i++) {

				Segment segment = segments.get(i);
				int length = segment.getText().length();

				/* If the caret position is within the segment, do send. */
				if (caretPos >= offset && caretPos < offset + length) {
					segment.keyPressed(e, caretPos);
					return;
				}

				/* If the caret is at the end of the segment. */
				if (caretPos == offset + length) {
					/* Next segment is not enabled. */
					if (i < segments.size() - 1 && !segments.get(i + 1).isEnabled()) {
						segment.keyPressed(e, caretPos);
						return;
					}
					/* This is the last segment. */
					if (i == segments.size() - 1) {
						segment.keyPressed(e, caretPos);
						return;
					}
				}

				/* Move offset. */
				offset += length;
			}
		}
	}

	/**
	 * A segment of the filter.
	 */
	protected static abstract class Segment {
		/**
		 * The parent filter.
		 */
		private SegmentFilter filter;
		/**
		 * A boolean that indicates whether the segment is enabled. When a segment is
		 * disabled, it is just rendered, no replace or remove operation is required
		 * from the segment.
		 */
		private boolean enabled = true;

		/**
		 * Indicates whether this segment accepts removing one character at the given
		 * offset.
		 * 
		 * @param offset    The offset from which to remove the character.
		 * @param direction The remove direction.
		 * @return A boolean that indicates whether this segment will remove one char
		 *         from the offset.
		 */
		protected abstract boolean acceptRemove(int offset, Direction direction);

		/**
		 * Indicates whether this segment accepts replacing next character at the given
		 * relative offset.
		 * 
		 * @param offset The offset within the segment.
		 * @param c      The character to replace.
		 * @return A boolean.
		 */
		protected abstract boolean acceptReplace(int offset, char c);

		/**
		 * Return the parent filter.
		 * 
		 * @return The parent segment filter.
		 */
		protected SegmentFilter getFilter() {
			return filter;
		}

		/**
		 * Return the current string of the segment.
		 * 
		 * @return The segment string.
		 */
		protected abstract String getText();

		/**
		 * Check whether this segment is enabled.
		 * 
		 * @return A boolean.
		 */
		protected boolean isEnabled() {
			return enabled;
		}

		/**
		 * Notified when a key is pressed. By default does nothing. Can be overwritten
		 * to handle additional actions on keys, for example, increasing a value when up
		 * or down keys are pressed.
		 * 
		 * @param e        The key event.
		 * @param caretPos The current caret position.
		 * @return The relative new caret position.
		 */
		protected void keyPressed(KeyEvent e, int caretPos) {}

		/**
		 * Remove next character at the relative offset.
		 * 
		 * @param offset    The relative offset.
		 * @param direction The remove direction.
		 * @return The positions to move the caret.
		 */
		protected abstract int remove(int offset, Direction direction);

		/**
		 * Replace next character at the given offset.
		 * 
		 * @param offset The offset within the segment.
		 * @param c      The character to replace.
		 * @return The positions to move the caret.
		 */
		protected abstract int replace(int offset, char c);

		/**
		 * Set the segment as enabled.
		 * 
		 * @param enabled A boolean.
		 */
		protected final void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			return getText();
		}
	}

	/**
	 * Fixed string separator segment.
	 */
	public static class Separator extends Segment {

		/** The separator string. */
		private String separator;

		/**
		 * Constructor.
		 */
		public Separator(String separator) {
			super();
			if (separator == null) throw new NullPointerException();
			this.separator = separator;
			setEnabled(false);
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
			if (offset < getText().length()) {
				if (getText().substring(offset, offset + 1).charAt(0) == c) {
					return true;
				}
			}
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String getText() {
			return separator;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected int remove(int offset, Direction direction) {
			if (direction == Direction.LEFT) {
				return -offset;
			}
			return getText().length() - offset;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected int replace(int offset, char c) {
			if (getText().substring(offset, offset + 1).charAt(0) == c) {
				return 1;
			}
			return 0;
		}

	}

	/** List of segments. */
	private List<Segment> segments = new ArrayList<>();

	/**
	 * Constructor.
	 */
	public SegmentFilter() {
		super();
		addKeyListener(new KeyListener());
	}

	/**
	 * Add a segment.
	 * 
	 * @param segment The segment.
	 */
	protected void addSegment(Segment segment) {
		segment.filter = this;
		segments.add(segment);
	}

	/**
	 * Clear the list of segments.
	 */
	protected void clearSegments() {
		segments.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void docRemove(int offset, int length) throws BadLocationException {
		for (int i = 0; i < length; i++) {
			docRemove(offset);
			offset = getCaretPosition();
		}
	}
	
	/**
	 * Remove one character at the given offset.
	 * @param offset
	 */
	private void docRemove(int offset) {
		/* Remove direction. */
		Direction direction = (offset < getCaretPosition() ? Direction.LEFT : Direction.RIGHT);

		/* Iterate segments. */
		int segmentOffset = 0;
		for (int i = 0; i < segments.size(); i++) {
			Segment segment = segments.get(i);
			int segmentLength = segment.getText().length();

			/*
			 * If the current offset is within the segment try whether the segment accepts
			 * the character.
			 */
			if (offset >= segmentOffset && offset < segmentOffset + segmentLength) {
				if (segment.acceptRemove(offset - segmentOffset, direction)) {
					int move = segment.remove(offset - segmentOffset, direction);
					setText();
					setCaretPosition(offset + move);
					return;
				}

				/*
				 * The segment does not accept the char, move the offset the start of next
				 * segment.
				 */
				offset = segmentOffset + segmentLength;
			}

			/* Move segment offset. */
			segmentOffset += segmentLength;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void docReplace(int offset, int length, String text) throws BadLocationException {
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			docReplace(offset, c);
			offset = getCaretPosition();
		}
	}

	/**
	 * Do replace char by char.
	 * 
	 * @param offset The offset.
	 * @param c      The character to replace.
	 * @throws BadLocationException
	 */
	private void docReplace(int offset, char c) throws BadLocationException {
		int segmentOffset = 0;
		for (int i = 0; i < segments.size(); i++) {
			Segment segment = segments.get(i);
			int segmentLength = segment.getText().length();
			/* Offset within the segment. */
			if (offset >= segmentOffset && offset < segmentOffset + segmentLength) {
				/*
				 * Check whether the segment accepts to replace the text and, if so, request the
				 * segment to effectively replace the text and move to the suggested position.
				 */
				if (segment.acceptReplace(offset - segmentOffset, c)) {
					int move = segment.replace(offset - segmentOffset, c);
					setText();
					setCaretPosition(offset + move);
					return;
				}
				/*
				 * The segment does not accept to replace the text. Move the text offset to the
				 * begining of next segment, so next segment will be requested to accept it.
				 */
				offset = segmentOffset + segmentLength;
			}
			/* Move segment offset. */
			segmentOffset += segmentLength;
		}
	}

	/**
	 * Return the segment where the caret is within.
	 * 
	 * @param caretPos The caret position.
	 * @return The segment or null.
	 */
	protected Segment getSegment(int caretPos) {
		int offset = 0;
		for (int i = 0; i < segments.size(); i++) {
			Segment segment = segments.get(i);
			int length = segment.getText().length();
			boolean within = false;
			if (i == segments.size() - 1) {
				within = (caretPos >= offset && caretPos <= offset + length);
			} else {
				within = (caretPos >= offset && caretPos < offset + length);
			}
			if (within) {
				return segment;
			}
			offset += length;
		}
		return null;
	}

	/**
	 * Return the initial offset of the segment or -1 if not found.
	 * 
	 * @param segment The segment to check.
	 * @return The offset.
	 */
	protected int getSegmentOffset(Segment segment) {
		int offset = 0;
		for (int i = 0; i < segments.size(); i++) {
			Segment seg = segments.get(i);
			if (seg.equals(segment)) {
				return offset;
			}
			offset += seg.getText().length();
		}
		return -1;
	}

	/**
	 * Return an unmodifiable copy of the segments to be accessed by extenders.
	 * 
	 * @return The list of segments.
	 */
	protected List<Segment> getSegments() {
		return Collections.unmodifiableList(segments);
	}

	/**
	 * Set the text performing a full rebuild from the segments.
	 * 
	 * @throws BadLocationException
	 */
	protected void setText() {
		try {
			rawRemove(0, getText().length());
			StringBuilder b = new StringBuilder();
			for (int i = 0; i < segments.size(); i++) {
				b.append(segments.get(i).getText());
			}
			rawReplace(0, 0, b.toString());
		} catch (BadLocationException ignore) {}
	}
}
