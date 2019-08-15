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
import java.awt.Dialog.ModalityType;
import java.awt.Image;

import javax.swing.JDialog;

import com.mlt.desktop.AWT;
import com.mlt.desktop.layout.Dimension;

/**
 * Dialog extension of window.
 * 
 * @author Miquel Sas
 */
public class Dialog extends Stage {

	/** Swing dialog. */
	private JDialog dialog;
	/** Content pane. */
	private Pane content;

	/**
	 * Constructor.
	 */
	public Dialog() {
		this(null, new GridBagPane());
	}

	/**
	 * Constructor.
	 * 
	 * @param owner   This dialog owner.
	 * @param content The content pane.
	 */
	public Dialog(Stage owner, Pane content) {
		super();
		if (owner == null) {
			dialog = new JDialog();
		} else if (owner instanceof Frame) {
			dialog = new JDialog(((Frame) owner).frame);
		} else if (owner instanceof Dialog) {
			dialog = new JDialog(((Dialog) owner).dialog);
		} else {
			throw new IllegalArgumentException();
		}
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.addWindowListener(new Forwarder(this));
		dialog.setContentPane((this.content = content).getComponent());
		dialog.setModal(true);
		dialog.setModalityType(ModalityType.APPLICATION_MODAL);
		putWindow(dialog, this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void centerOnScreen() {
		AWT.centerOnScreen(dialog);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		removeWindow(dialog);
		dialog.dispose();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component getComponent() {
		return dialog;
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
	public Dimension getSize() {
		return AWT.fromAWT(dialog.getSize());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getTitle() {
		return dialog.getTitle();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pack() {
		dialog.pack();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setContent(Pane content) {
		this.content = content;
		dialog.setContentPane(content.getComponent());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setImage(Image image) {
		dialog.setIconImage(image);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setLocation(int x, int y) {
		dialog.setLocation(x, y);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSize(Dimension size) {
		dialog.setSize(AWT.toAWT(size));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSize(double widthFactor, double heightFactor) {
		AWT.setSize(dialog, widthFactor, heightFactor);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTitle(String title) {
		dialog.setTitle(title);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setVisible(boolean b) {
		dialog.setVisible(b);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void toBack() {
		dialog.toBack();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void toFront() {
		dialog.toFront();
	}
}