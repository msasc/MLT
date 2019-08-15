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
package com.mlt.desktop.control;

import java.awt.Component;
import java.awt.Image;

import javax.swing.JWindow;

import com.mlt.desktop.AWT;
import com.mlt.desktop.layout.Dimension;

/**
 * Window, with no frames or menu bar.
 * 
 * @author Miquel Sas
 */
class Window extends Stage {

	/** Swing window. */
	private JWindow window;
	/** Content pane. */
	private Pane content;

	/**
	 * Constructor.
	 * 
	 * @param content The content pane.
	 */
	public Window(Pane content) {
		super();
		window = new JWindow();
		window.addWindowListener(new Forwarder(this));
		window.setContentPane((this.content = content).getComponent());
		putWindow(window, this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void centerOnScreen() {
		AWT.centerOnScreen(window);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component getComponent() {
		return window;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Pane getContent() {
		return content;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setContent(Pane content) {
		this.content = content;
		window.setContentPane(content.getComponent());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSize(double widthFactor, double heightFactor) {
		AWT.setSize(window, widthFactor, heightFactor);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getTitle() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTitle(String title) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Dimension getSize() {
		return AWT.fromAWT(window.getSize());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setImage(Image image) {
		window.setIconImage(image);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setLocation(int x, int y) {
		window.setLocation(x, y);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSize(Dimension size) {
		window.setSize(AWT.toAWT(size));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pack() {
		window.pack();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setVisible(boolean b) {
		window.setVisible(b);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		removeWindow(window);
		window.dispose();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void toBack() {
		window.toBack();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void toFront() {
		window.toFront();
	}
}