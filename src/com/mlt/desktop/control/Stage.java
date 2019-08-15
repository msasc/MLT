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
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.mlt.desktop.event.WindowHandler;
import com.mlt.desktop.layout.Dimension;

/**
 * Simple stage (window) implementation detached from all the window, dialog.
 *
 * @author Miquel Sas
 */
public abstract class Stage {

	/**
	 * Window listener adapter.
	 */
	public static class Adapter implements Listener {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void activated(Stage stage) {
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void closed(Stage stage) {
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void closing(Stage stage) {
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void deactivated(Stage stage) {
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void deiconified(Stage stage) {
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void focusGained(Stage stage) {
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void focusLost(Stage stage) {
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void iconified(Stage stage) {
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void opened(Stage stage) {
		}
	}

	/**
	 * Enumerates window events.
	 */
	private static enum Event {
		ACTIVATED, CLOSED, CLOSING, DEACTIVATED, DEICONIFIED, ICONIFIED, OPENED, FOCUS_GAINED, FOCUS_LOST;
	}

	/**
	 * Forwarder of window events.
	 */
	static class Forwarder extends WindowHandler {

		/** Window. */
		private Stage stage;

		/**
		 * Constructor.
		 * 
		 * @param stage The parent window.
		 */
		Forwarder(Stage stage) {
			super();
			this.stage = stage;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void windowActivated(WindowEvent e) {
			stage.fireWindowEvent(Event.ACTIVATED);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void windowClosed(WindowEvent e) {
			stage.fireWindowEvent(Event.CLOSED);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void windowClosing(WindowEvent e) {
			stage.fireWindowEvent(Event.CLOSING);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void windowDeactivated(WindowEvent e) {
			stage.fireWindowEvent(Event.DEACTIVATED);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void windowDeiconified(WindowEvent e) {
			stage.fireWindowEvent(Event.DEICONIFIED);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void windowGainedFocus(WindowEvent e) {
			stage.fireWindowEvent(Event.FOCUS_GAINED);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void windowIconified(WindowEvent e) {
			stage.fireWindowEvent(Event.ICONIFIED);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void windowLostFocus(WindowEvent e) {
			stage.fireWindowEvent(Event.FOCUS_LOST);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void windowOpened(WindowEvent e) {
			stage.fireWindowEvent(Event.OPENED);
		}
	}

	/**
	 * Listener to window events.
	 */
	public static interface Listener {

		/**
		 * Window activated.
		 * 
		 * @param stage The window.
		 */
		void activated(Stage stage);

		/**
		 * Window closed.
		 * 
		 * @param stage The window.
		 */
		void closed(Stage stage);

		/**
		 * Window closing.
		 * 
		 * @param stage The window.
		 */
		void closing(Stage stage);

		/**
		 * Window deactivated.
		 * 
		 * @param stage The window.
		 */
		void deactivated(Stage stage);

		/**
		 * Window deiconified.
		 * 
		 * @param stage The window.
		 */
		void deiconified(Stage stage);

		/**
		 * Window focus gained.
		 * 
		 * @param stage The window.
		 */
		void focusGained(Stage stage);

		/**
		 * Window focus lost.
		 * 
		 * @param stage The window.
		 */
		void focusLost(Stage stage);

		/**
		 * Window iconified.
		 * 
		 * @param stage The window.
		 */
		void iconified(Stage stage);

		/**
		 * Window opened.
		 * 
		 * @param stage The window.
		 */
		void opened(Stage stage);
	}

	/**
	 * A map to link java.awt.Windows with their parent Window. The link to be implemented through a user strategy due
	 * to the lack of a user object, at the level of component.
	 */
	private static HashMap<java.awt.Window, Stage> windowMap = new HashMap<>();
	/** Lock used to access the window map. */
	private static ReentrantLock windowLock = new ReentrantLock();

	/**
	 * Return the Stage or null.
	 * 
	 * @param awtWindow The java.awt.Window
	 * @return The Window
	 */
	public static Stage getStage(java.awt.Window awtWindow) {
		try {
			windowLock.lock();
			return windowMap.get(awtWindow);
		} finally {
			windowLock.unlock();
		}
	}

	static void putWindow(java.awt.Window awtWindow, Stage stage) {
		try {
			windowLock.lock();
			windowMap.put(awtWindow, stage);
		} finally {
			windowLock.unlock();
		}
	}

	static void removeWindow(java.awt.Window awtWindow) {
		try {
			windowLock.lock();
			windowMap.remove(awtWindow);
		} finally {
			windowLock.unlock();
		}
	}

	/** List of listeners. */
	private List<Stage.Listener> listeners = new ArrayList<>();

	/**
	 * Window instances are always created from here.
	 */
	Stage() {
		super();
	}

	/**
	 * Add a listener.
	 * 
	 * @param listener The window listener.
	 */
	public final void addWindowListener(Stage.Listener listener) {
		listeners.add(listener);
	}

	/**
	 * Center the window on screen.
	 */
	public abstract void centerOnScreen();

	/**
	 * Dispose the required window res.
	 */
	public abstract void dispose();

	/**
	 * Fire the event.
	 * 
	 * @param e The window event.
	 */
	protected void fireWindowEvent(Event e) {
		switch (e) {
		case ACTIVATED:
			listeners.forEach(listener -> listener.activated(this));
			break;
		case CLOSED:
			listeners.forEach(listener -> listener.closed(this));
			break;
		case CLOSING:
			listeners.forEach(listener -> listener.closing(this));
			break;
		case DEACTIVATED:
			listeners.forEach(listener -> listener.deactivated(this));
			break;
		case DEICONIFIED:
			listeners.forEach(listener -> listener.deiconified(this));
			break;
		case ICONIFIED:
			listeners.forEach(listener -> listener.iconified(this));
			break;
		case OPENED:
			listeners.forEach(listener -> listener.opened(this));
			break;
		case FOCUS_GAINED:
			listeners.forEach(listener -> listener.focusGained(this));
			break;
		case FOCUS_LOST:
			listeners.forEach(listener -> listener.focusLost(this));
			break;
		}
	}

	/**
	 * Returns the AWT component.
	 * 
	 * @return The AWT component.
	 */
	public abstract Component getComponent();

	/**
	 * Return the content pane.
	 * 
	 * @return The content pane.
	 */
	public abstract Pane getContent();

	/**
	 * Return the size.
	 * 
	 * @return The size.
	 */
	public abstract Dimension getSize();

	/**
	 * Return the title.
	 * 
	 * @return The title.
	 */
	public abstract String getTitle();

	/**
	 * Pack the window.
	 */
	public abstract void pack();

	/**
	 * Set the content pane.
	 * 
	 * @param content The content pane.
	 */
	public abstract void setContent(Pane content);

	/**
	 * Set the image associated with this window.
	 * 
	 * @param image The image.
	 */
	public abstract void setImage(Image image);

	/**
	 * Set the location on screen.
	 * 
	 * @param x X coordinate.
	 * @param y Y coordinate.
	 */
	public abstract void setLocation(int x, int y);

	/**
	 * Set the size.
	 * 
	 * @param size The size as a dimension.
	 */
	public abstract void setSize(Dimension size);

	/**
	 * Set the size as a factor of the screen size.
	 * 
	 * @param widthFactor  The width factor.
	 * @param heightFactor The height factor.
	 */
	public abstract void setSize(double widthFactor, double heightFactor);

	/**
	 * Set the frame/dialog title.
	 * 
	 * @param title The title.
	 */
	public abstract void setTitle(String title);

	/**
	 * Set the window visible.
	 * 
	 * @param b A boolean.
	 */
	public abstract void setVisible(boolean b);

	/**
	 * If visible send it to back.
	 */
	public abstract void toBack();

	/**
	 * Send to front and request focus.
	 */
	public abstract void toFront();
}
