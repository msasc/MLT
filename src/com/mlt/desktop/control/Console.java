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

package com.mlt.desktop.control;

import java.awt.Font;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

/**
 * A console.
 *
 * @author Miquel Sas
 */
public class Console extends TextArea {

	/**
	 * Writer.
	 */
	class ConsoleWriter extends Writer {

		@Override
		public void write(char[] cbuf, int off, int len) throws IOException {
			if (cbuf == null) {
				throw new NullPointerException();
			}
			if ((off < 0) ||
				(off > cbuf.length) ||
				(len < 0) ||
				((off + len) > cbuf.length) ||
				((off + len) < 0)) {
				throw new IndexOutOfBoundsException();
			}
			if (len == 0) {
				return;
			}
			char[] dest = new char[len];
			System.arraycopy(cbuf, off, dest, 0, len);
			Console.this.append(new String(dest));
		}

		@Override
		public void flush() throws IOException {}

		@Override
		public void close() throws IOException {}
	}

	/** Print writer. */
	private PrintWriter printer; 
	/** Maximum number of line, zero no limit, default is 10000. */
	private int maxLines;

	/**
	 * Default constructor.
	 */
	public Console() {
		this(10000);
	}

	/**
	 * Default constructor.
	 */
	public Console(int maxLines) {
		super();
		setComponent(new JTextArea());
		getComponent().setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
		this.maxLines = maxLines;
		this.printer = new PrintWriter(new ConsoleWriter());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public JTextArea getComponent() {
		return (JTextArea) super.getComponent();
	}

	/**
	 * Append the string to the console.
	 * 
	 * @param str The string to print.
	 */
	private void append(String str) {
		JTextArea ta = getComponent();
		ta.append(str);
		if (maxLines > 0) {
			try {
				int lineCount = ta.getLineCount();
				if (lineCount > (maxLines + maxLines / 2)) {
					int startLine = 0;
					int endLine = lineCount - maxLines;
					int startOffset = ta.getLineStartOffset(startLine);
					int endOffset = ta.getLineEndOffset(endLine);
					replaceRange("", startOffset, endOffset);
				}
				int startOffset = ta.getLineStartOffset(ta.getLineCount() - 1);
				setCaretPosition(startOffset);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Clear the console.
	 */
	public void clear() {
		getComponent().setText("");
	}

	/**
	 * @param str The string to print.
	 * @see java.io.PrintWriter#print(java.lang.String)
	 */
	public void print(String str) {
		printer.print(str);
	}

	/**
	 * @see java.io.PrintWriter#println()
	 */
	public void println() {
		printer.println();
	}

	/**
	 * @param str The string to print.
	 * @see java.io.PrintWriter#println(java.lang.String)
	 */
	public void println(String str) {
		printer.println(str);
	}
}
