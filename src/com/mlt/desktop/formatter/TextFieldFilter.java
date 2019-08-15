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
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

import com.mlt.util.Strings;

/**
 * Document and navigation filter manager for single line text fields, without
 * attributes.
 *
 * @author Miquel Sas
 */
public class TextFieldFilter {

	/**
	 * The document that will be installed in the <em>JTextField</em>.
	 */
	private class Document extends PlainDocument {

		/**
		 * Constructor.
		 */
		private Document() {
			super();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void insertString(
			int offset,
			String text,
			AttributeSet a) throws BadLocationException {
			docInsert(offset, text);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void remove(int offset, int length) throws BadLocationException {
			docRemove(offset, length);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void replace(
			int offset,
			int length,
			String text,
			AttributeSet attrs) throws BadLocationException {
			docReplace(offset, length, text);
		}

		/**
		 * Raw insert.
		 * 
		 * @param offset Offset.
		 * @param text   Text.
		 * @throws BadLocationException
		 */
		private void rawInsert(int offset, String text) throws BadLocationException {
			super.insertString(offset, text, null);
		}

		/**
		 * Raw remove.
		 * 
		 * @param offset Offset.
		 * @param length Length.
		 * @throws BadLocationException
		 */
		private void rawRemove(int offset, int length) throws BadLocationException {
			super.remove(offset, length);
		}

		/**
		 * Raw replace.
		 * 
		 * @param offset Offset.
		 * @param length Length.
		 * @param text   Text.
		 * @throws BadLocationException
		 */
		private void rawReplace(int offset, int length, String text) throws BadLocationException {
			super.replace(offset, length, text, null);
		}

	}

	/**
	 * Key listener to forward key events in the text field.
	 */
	class KeyForwarder implements KeyListener {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void keyPressed(KeyEvent e) {
			keyListeners.forEach(l -> l.keyPressed(e));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void keyTyped(KeyEvent e) {
			keyListeners.forEach(l -> l.keyTyped(e));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void keyReleased(KeyEvent e) {
			keyListeners.forEach(l -> l.keyReleased(e));
		}
	}

	/** The text field component. */
	private JTextField textField;
	/** The document. */
	private Document document;
	/** Key listeners. */
	private List<KeyListener> keyListeners = new ArrayList<>();

	/**
	 * Constructor.
	 */
	public TextFieldFilter() {
		super();
	}

	/**
	 * Add a key listener to handle key events in the text field.
	 *
	 * @param keyListener The key listener.
	 */
	protected final void addKeyListener(KeyListener keyListener) {
		keyListeners.add(keyListener);
	}

	/**
	 * Document insert. This is the insert method to override.
	 * 
	 * @param offset The offset.
	 * @param text   The text.
	 * @throws BadLocationException
	 */
	protected void docInsert(int offset, String text) throws BadLocationException {
		rawInsert(offset, text);
	}

	/**
	 * Document remove. This is the remove method to override.
	 * 
	 * @param offset Offset.
	 * @param length Length.
	 * @throws BadLocationException
	 */
	protected void docRemove(int offset, int length) throws BadLocationException {
		rawRemove(offset, length);
	}

	/**
	 * Document replace. This is the replace method to override.
	 * 
	 * @param offset Offset within the document.
	 * @param length Length to replace.
	 * @param text   Text to insert.
	 * @throws BadLocationException
	 */
	protected void docReplace(int offset, int length, String text) throws BadLocationException {
		rawReplace(offset, length, text);
	}

	/**
	 * Return the text length.
	 * 
	 * @return The length.
	 */
	protected int getLength() {
		return document.getLength();
	}

	/**
	 * Return the next character from the document text.
	 *
	 * @param offset The starting offset.
	 * @return The chars.
	 * @throws BadLocationException
	 */
	protected String getNextChar(int offset) throws BadLocationException {
		return Strings.substring(getText(), offset, offset + 1);
	}

	/**
	 * Return the next characters from the document text.
	 *
	 * @param offset The starting offset.
	 * @param length The length or number of chars.
	 * @return The chars.
	 * @throws BadLocationException
	 */
	protected String getNextChars(int offset, int length) throws BadLocationException {
		return Strings.substring(getText(), offset, offset + length);
	}

	/**
	 * Return the next characters.
	 *
	 * @param str    The source string.
	 * @param offset The starting offset.
	 * @param length The length or number of chars.
	 * @return The chars.
	 */
	protected String getNextChars(String str, int offset, int length) {
		return Strings.substring(str, offset, offset + length);
	}

	/**
	 * Return the caret position.
	 * 
	 * @return The caret position.
	 */
	protected int getCaretPosition() {
		return textField.getCaretPosition();
	}

	/**
	 * Return the previous character from the document text.
	 *
	 * @param offset The starting offset.
	 * @return The chars.
	 * @throws BadLocationException
	 */
	protected String getPrevChar(int offset) throws BadLocationException {
		return Strings.substring(getText(), offset - 1, offset);
	}

	/**
	 * Return the current text.
	 * 
	 * @return The current text.
	 */
	protected String getText() {
		try {
			return document.getText(0, document.getLength());
		} catch (BadLocationException ignore) {
			return "";
		}
	}

	/**
	 * Raw insert.
	 * 
	 * @param offset The offset.
	 * @param text   The text.
	 * @throws BadLocationException
	 */
	protected void rawInsert(int offset, String text) throws BadLocationException {
		document.rawInsert(offset, text);
	}

	/**
	 * Raw overwrite the text. This is the remove method to be called to perform the
	 * underlying overwrite.
	 *
	 * @param offset Offset.
	 * @param text   String.
	 * @throws BadLocationException
	 */
	protected void rawOverwrite(int offset, String text) throws BadLocationException {
		document.rawReplace(offset, Strings.length(text), text);
	}

	/**
	 * Raw remove.
	 * 
	 * @param offset Offset.
	 * @param length Length.
	 * @throws BadLocationException
	 */
	protected void rawRemove(int offset, int length) throws BadLocationException {
		document.rawRemove(offset, length);
	}

	/**
	 * Raw replace.
	 * 
	 * @param offset Offset.
	 * @param length Length.
	 * @param text   Text.
	 * @throws BadLocationException
	 */
	protected void rawReplace(int offset, int length, String text) throws BadLocationException {
		document.rawReplace(offset, length, text);
	}

	/**
	 * Set the caret position.
	 * 
	 * @param position The position.
	 */
	protected void setCaretPosition(int position) {
		textField.setCaretPosition(position);
	}

	/**
	 * Set the swing text field to be filtered.
	 * 
	 * @param textField The text field.
	 */
	public void setTextField(JTextField textField) {
		this.textField = textField;
		this.document = new Document();
		document.setDocumentFilter(new DocumentFilter());
		textField.setDocument(document);
		textField.addKeyListener(new KeyForwarder());
	}
}
