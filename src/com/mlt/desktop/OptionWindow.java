/*
 * Copyright (C) 2017 Miquel Sas
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

package com.mlt.desktop;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.mlt.desktop.action.ActionList;
import com.mlt.desktop.control.BorderPane;
import com.mlt.desktop.control.Control;
import com.mlt.desktop.control.OptionPane;
import com.mlt.desktop.control.Stage;
import com.mlt.desktop.layout.Dimension;
import com.mlt.desktop.layout.Insets;
import com.mlt.desktop.layout.Orientation;
import com.mlt.desktop.layout.Position;

/**
 * A frame or dialog that has the following properties.
 * <ul>
 * <li>Can have an option pane.</li>
 * <li>The layout is a border layout. Components may be set top, left, bottom,
 * right and center.</li>
 * </ul>
 *
 *
 * @author Miquel Sas
 */
public class OptionWindow {

	/**
	 * The action that closes the window.
	 */
	private class ActionClose extends AbstractAction {

		private ActionClose() {}

		@Override
		public void actionPerformed(ActionEvent e) {
			close();
		}
	}

	/**
	 * The action that set the executed option.
	 */
	private class ActionOption extends AbstractAction {
		private Option option;

		private ActionOption(Option option) {
			this.option = option;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			optionExecuted = option;
		}
	}

	/**
	 * Internal option pane listener installed in the option pane.
	 */
	private class OptionListener implements OptionPane.Listener {

		@Override
		public void added(Option option) {

			/*
			 * Configure an action list starting with the action to set the executed option,
			 * then the action of the option, and finally, if required, the action to close
			 * the window.
			 */
			ActionList actions = new ActionList();
			actions.add(new ActionOption(option));
			if (option.getAction() != null) {
				actions.add(option.getAction());
			}
			if (option.isCloseWindow()) {
				actions.add(new ActionClose());
			}

			/*
			 * Create a button and a menu item with the action and set the to the option.
			 */
			Option.createStandardButton(option, actions);
			Option.createMenuItem(option, actions);

			/*
			 * If the option is default close option, register it.
			 */
			if (option.isDefaultClose()) {
				optionDefaultClose = option;
			}
		}

		@Override
		public void removed(Option option) {}
	}

	/**
	 * Window listener.
	 */
	private class WindowListener extends Stage.Adapter {
		@Override
		public void closing(Stage w) {
			if (optionDefaultClose != null) {
				if (optionDefaultClose.getButton() != null) {
					optionDefaultClose.getButton().doClick();
					return;
				}
				if (optionDefaultClose.getMenuItem() != null) {
					optionDefaultClose.getMenuItem().doClick();
				}
			}
		}
	}

	/** Internal framed window. */
	private Stage stage;
	/** Option pane. */
	private OptionPane optionPane;
	/** Option pane position. */
	private Position optionPanePos;
	/** Option pane Listener. */
	private OptionListener optionListener;
	/** Option executed, either it closes the window or not. */
	private Option optionExecuted;
	/** Default close option if any. */
	private Option optionDefaultClose;

	/**
	 * Constructor.
	 */
	public OptionWindow(Stage rootPane) {
		super();
		this.stage = rootPane;
		this.stage.setContent(new BorderPane());
		this.stage.addWindowListener(new WindowListener());
		this.optionListener = new OptionListener();
	}

	/**
	 * Center the window on screen.
	 */
	public void centerOnScreen() {
		stage.centerOnScreen();
	}

	/**
	 * Check that the position (top, left, bottom or right) is not accessed when the
	 * options are installed there.
	 * 
	 * @param pos The position.
	 */
	private void checkOptionsInstalled(Position pos) {
		if (optionPanePos != null && optionPanePos.equals(pos)) {
			throw new IllegalStateException("Options position is " + pos);
		}
	}

	/**
	 * Close and dispose.
	 */
	public void close() {
		stage.setVisible(false);
		stage.dispose();
	}

	/**
	 * Return the content border pane.
	 * 
	 * @return The content border pane.
	 */
	private BorderPane getBorderPane() {
		return (BorderPane) stage.getContent();
	}

	/**
	 * Return the bottom control.
	 * 
	 * @return The control.
	 */
	public Control getBottom() {
		checkOptionsInstalled(Position.BOTTOM);
		return getBorderPane().getBottom();
	}

	/**
	 * Return the center control.
	 * 
	 * @return The control.
	 */
	public Control getCenter() {
		return getBorderPane().getCenter();
	}

	/**
	 * Return the left control.
	 * 
	 * @return The control.
	 */
	public Control getLeft() {
		checkOptionsInstalled(Position.LEFT);
		return getBorderPane().getLeft();
	}

	/**
	 * Return the option executed.
	 * 
	 * @return The option executed.
	 */
	public Option getOptionExecuted() {
		return optionExecuted;
	}

	/**
	 * Give access to the options pane to manage, add or remove options. Note that
	 * the option pane must have been set
	 * with a call to <code>setOptions...</code>. Throws a NullPointerException if
	 * the option pane has not been set.
	 * 
	 * @return The option pane.
	 */
	public OptionPane getOptionPane() {
		if (optionPane == null) {
			throw new NullPointerException();
		}
		return optionPane;
	}

	/**
	 * Return the right control.
	 * 
	 * @return The control.
	 */
	public Control getRight() {
		checkOptionsInstalled(Position.RIGHT);
		return getBorderPane().getRight();
	}

	/**
	 * Return the top control.
	 * 
	 * @return The control.
	 */
	public Control getTop() {
		checkOptionsInstalled(Position.TOP);
		return getBorderPane().getTop();
	}

	/**
	 * Return the window.
	 * 
	 * @return The window.
	 */
	public Stage getWindow() {
		return stage;
	}

	/**
	 * Pack the window.
	 */
	public void pack() {
		stage.pack();
		/*
		 * Avoid packing to a size greater than the screen size.
		 */
		Dimension paneSize = stage.getSize();
		Dimension screenSize = AWT.getScreenSize(null);
		double width = paneSize.getWidth();
		double height = paneSize.getHeight();
		if (width > screenSize.getWidth() || height > screenSize.getHeight()) {
			if (width > screenSize.getWidth()) {
				width = screenSize.getWidth() * 0.9;
			}
			if (height > screenSize.getHeight()) {
				height = screenSize.getHeight() * 0.9;
			}
			stage.setSize(paneSize);
		}
	}

	/**
	 * Set the bottom control.
	 * 
	 * @param control The control.
	 */
	public void setBottom(Control control) {
		setBottom(control, null);
	}

	/**
	 * Set the bottom control.
	 * 
	 * @param control The control.
	 * @param insets  Insets.
	 */
	public void setBottom(Control control, Insets insets) {
		checkOptionsInstalled(Position.BOTTOM);
		getBorderPane().setBottom(control, insets);
	}

	/**
	 * Set the center control.
	 * 
	 * @param control The control.
	 */
	public void setCenter(Control control) {
		setCenter(control, null);
	}

	/**
	 * Set the center control.
	 * 
	 * @param control The control.
	 * @param insets  Insets.
	 */
	public void setCenter(Control control, Insets insets) {
		getBorderPane().setCenter(control, insets);
	}

	/**
	 * Set the left control.
	 * 
	 * @param control The control.
	 */
	public void setLeft(Control control) {
		setLeft(control, null);
	}

	/**
	 * Set the left control.
	 * 
	 * @param control The control.
	 * @param insets  Insets.
	 */
	public void setLeft(Control control, Insets insets) {
		checkOptionsInstalled(Position.LEFT);
		getBorderPane().setLeft(control, insets);
	}

	/**
	 * Set the options at the position.
	 * 
	 * @param position The position.
	 */
	private void setOptions(Position position) {
		/* Clear options if present. */
		if (optionPanePos != null) {
			Position pos = optionPanePos;
			optionPanePos = null;
			optionPane = null;
			switch (pos) {
			case TOP:
				setTop(null);
				break;
			case LEFT:
				setLeft(null);
				break;
			case BOTTOM:
				setBottom(null);
				break;
			case RIGHT:
				setRight(null);
				break;
			default:
				break;
			}
		}

		/* Set the options. */
		Orientation orientation;
		switch (position) {
		case TOP:
			orientation = Orientation.HORIZONTAL;
			break;
		case LEFT:
			orientation = Orientation.VERTICAL;
			break;
		case BOTTOM:
			orientation = Orientation.HORIZONTAL;
			break;
		case RIGHT:
			orientation = Orientation.VERTICAL;
			break;
		default:
			orientation = Orientation.HORIZONTAL;
			break;
		}
		optionPane = new OptionPane(orientation);
		optionPane.addListener(optionListener);
		optionPanePos = position;
		switch (position) {
		case TOP:
			getBorderPane().setTop(optionPane);
			break;
		case LEFT:
			getBorderPane().setLeft(optionPane);
			break;
		case BOTTOM:
			getBorderPane().setBottom(optionPane);
			break;
		case RIGHT:
			getBorderPane().setRight(optionPane);
			break;
		default:
			getBorderPane().setBottom(optionPane);
			break;
		}
	}

	/**
	 * Set the option pane bottom.
	 */
	public void setOptionsBottom() {
		setOptions(Position.BOTTOM);
	}

	/**
	 * Set the option pane left.
	 */
	public void setOptionsLeft() {
		setOptions(Position.LEFT);
	}

	/**
	 * Set the option pane right.
	 */
	public void setOptionsRight() {
		setOptions(Position.RIGHT);
	}

	/**
	 * Set the option pane top.
	 */
	public void setOptionsTop() {
		setOptions(Position.TOP);
	}

	/**
	 * Set the right control.
	 * 
	 * @param control The control.
	 */
	public void setRight(Control control) {
		setRight(control, null);
	}

	/**
	 * Set the right control.
	 * 
	 * @param control The control.
	 * @param insets  Insets.
	 */
	public void setRight(Control control, Insets insets) {
		checkOptionsInstalled(Position.RIGHT);
		getBorderPane().setRight(control, insets);
	}

	/**
	 * Set the size as a factor of the screen size.
	 * 
	 * @param widthFactor  The width factor.
	 * @param heightFactor The height factor.
	 */
	public void setSize(double widthFactor, double heightFactor) {
		stage.setSize(widthFactor, heightFactor);
	}

	/**
	 * Set the title.
	 * 
	 * @param title The title.
	 * @see com.mlt.lib.swing.core.RootPane#setTitle(java.lang.String)
	 */
	public void setTitle(String title) {
		stage.setTitle(title);
	}

	/**
	 * Set the top control.
	 * 
	 * @param control The control.
	 */
	public void setTop(Control control) {
		setTop(control, null);
	}

	/**
	 * Set the top control.
	 * 
	 * @param control The control.
	 * @param insets  Insets.
	 */
	public void setTop(Control control, Insets insets) {
		checkOptionsInstalled(Position.TOP);
		getBorderPane().setTop(control, insets);
	}

	/**
	 * Show the window.
	 */
	public void show() {
		if (optionPane != null) {
			AWT.installAcceleratorKeyListener(stage.getContent());
			optionPane.setMnemonics();
		}
		stage.setVisible(true);
	}
}
