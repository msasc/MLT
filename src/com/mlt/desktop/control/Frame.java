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

import javax.swing.JFrame;

import com.mlt.desktop.AWT;
import com.mlt.desktop.layout.Dimension;

/**
 * Frame extension of window.
 * 
 * @author Miquel Sas
 */
public class Frame extends Stage {

	/** Swing frame. */
	JFrame frame;
	/** Menu bar. */
	private MenuBar menuBar;
	/** Content pane. */
	private Pane content;

	/**
	 * Constructor.
	 * 
	 * @param content The content pane.
	 */
	public Frame(Pane content) {
		super();
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new Forwarder(this));
		frame.setContentPane((this.content = content).getComponent());
		putWindow(frame, this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void centerOnScreen() {
		AWT.centerOnScreen(frame);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		removeWindow(frame);
		frame.dispose();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component getComponent() {
		return frame;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Pane getContent() {
		return content;
	}

	/**
	 * Return the menu bar.
	 * 
	 * @return The menu bar.
	 */
	public MenuBar getMenuBar() {
		return menuBar;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Dimension getSize() {
		return AWT.fromAWT(frame.getSize());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getTitle() {
		return frame.getTitle();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pack() {
		frame.pack();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setContent(Pane content) {
		this.content = content;
		frame.setContentPane(content.getComponent());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setImage(Image image) {
		frame.setIconImage(image);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setLocation(int x, int y) {
		frame.setLocation(x, y);
	}

	/**
	 * Set the menu bar.
	 * 
	 * @param menuBar The menu bar.
	 */
	public void setMenuBar(MenuBar menuBar) {
		this.menuBar = menuBar;
		frame.setJMenuBar(menuBar.getComponent());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSize(Dimension size) {
		frame.setSize(AWT.toAWT(size));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSize(double widthFactor, double heightFactor) {
		AWT.setSize(frame, widthFactor, heightFactor);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setTitle(String title) {
		frame.setTitle(title);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setVisible(boolean b) {
		frame.setVisible(b);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void toBack() {
		frame.toBack();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void toFront() {
		frame.toFront();
	}
}