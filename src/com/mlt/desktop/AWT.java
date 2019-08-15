/*
 * Copyright (C) 2018 Miquel Sas
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

import com.mlt.desktop.control.Control;
import com.mlt.desktop.control.Stage;
import com.mlt.desktop.control.TextArea;
import com.mlt.desktop.layout.Alignment;
import com.mlt.desktop.layout.Dimension;
import com.mlt.desktop.layout.Anchor;
import com.mlt.desktop.layout.Constraints;
import com.mlt.desktop.layout.Fill;
import com.mlt.desktop.layout.Insets;
import com.mlt.desktop.layout.Orientation;
import com.mlt.util.Numbers;

import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JWindow;
import javax.swing.KeyStroke;

import java.awt.GridBagConstraints;
import javax.swing.SwingConstants;

/**
 * AWT and Swing utilities.
 *
 * @author Miquel Sas
 */
public class AWT {

	/**
	 * A key listener to manage accelerator keys.
	 */
	public static class AcceleratorKeyListener extends KeyAdapter {

		/** The list of key strokes to manage. */
		private Map<KeyStroke, Option> keyStrokesMap;

		/**
		 * Default constructor.
		 */
		public AcceleratorKeyListener() {
			super();
		}

		/**
		 * Return a list with all options in buttons or menu items contained in all
		 * children components of the parent.
		 *
		 * @param parent The parent component.
		 * @return The list of options.
		 */
		private List<Option> getAllOptions(Component parent) {
			List<Component> components = getAllComponents(parent);
			List<Option> options = new ArrayList<>();
			for (Component component : components) {
				if (component instanceof JComponent) {
					Control control = Control.getControl((JComponent) component);
					if (control != null) {
						Option option = control.getOption();
						if (option != null) {
							options.add(option);
						}
					}
				}
			}
			return options;
		}

		/**
		 * Returns and if necessary rebuilds the list of key strokes.
		 *
		 * @param source The source of the key event.
		 * @return The map of key strokes and actions.
		 */
		private Map<KeyStroke, Option> getKeyStrokesMap(Object source) {
			if (keyStrokesMap == null) {
				keyStrokesMap = new HashMap<>();
				if (source instanceof Component) {
					List<Option> options = getAllOptions((Component) source);
					for (Option option : options) {
						/* Normal accelerator. */
						KeyStroke keyStroke = option.getAccelerator();
						if (keyStroke != null) {
							keyStrokesMap.put(keyStroke, option);
						}
						/* Optional secondary accelerator. */
						keyStroke = option.getSecondaryAccelerator();
						if (keyStroke != null) {
							keyStrokesMap.put(keyStroke, option);
						}
						/* Possible button mnemonic. */
					}
				}
			}
			return keyStrokesMap;
		}

		/**
		 * Invoked when a key has been pressed. See the class description for KeyEvent
		 * for a definition of a key pressed
		 * event.
		 *
		 * @param e The key event source.
		 */
		@Override
		public void keyPressed(KeyEvent e) {

			int keyCode = e.getKeyCode();
			int modifiers = e.getModifiersEx();
			KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, modifiers);

			// Manage special components that do not fire accelerator keys like, for
			// instance, VK_ENTER.
			if (e.getSource() instanceof TextArea) {
				TextArea textArea = (TextArea) e.getSource();
				if (textArea.isEditable()) {
					if (keyCode == KeyEvent.VK_ENTER) {
						return;
					}
				}
			}

			Option option = getKeyStrokesMap(e.getSource()).get(keyStroke);
			if (option != null) {
				if (option.getButton() != null) {
					e.consume();
					option.getButton().doClick();
					return;
				}
				if (option.getMenuItem() != null) {
					e.consume();
					option.getMenuItem().doClick();
				}
			}
		}
	}

	/**
	 * Add the key listener to the component and its children.
	 *
	 * @param c The component.
	 * @param l The listener.
	 */
	public static void addKeyListener(Component c, KeyListener l) {
		List<Component> components = getAllChildComponents(c);
		for (Component component : components) {
			component.addKeyListener(l);
		}
	}

	/**
	 * Add the key listener to the control and its children.
	 *
	 * @param c The control.
	 * @param l The listener.
	 */
	public static void addKeyListener(Control c, KeyListener l) {
		addKeyListener(c.getComponent(), l);
	}

	/**
	 * Add the listener to the component and its children.
	 *
	 * @param c The component.
	 * @param l The listener.
	 */
	public static void addMouseWheelListener(Component c, MouseWheelListener l) {
		List<Component> components = getAllChildComponents(c);
		for (Component component : components) {
			component.addMouseWheelListener(l);
		}
	}

	/**
	 * Add the listener to the control and its children.
	 *
	 * @param c The control.
	 * @param l The listener.
	 */
	public static void addMouseWheelListener(Control c, MouseWheelListener l) {
		addMouseWheelListener(c.getComponent(), l);
	}

	/**
	 * Center the window on the screen.
	 *
	 * @param window The window to center.
	 */
	public static void centerOnScreen(Window window) {
		Dimension sz = fromAWT(window.getSize());
		Dimension szScreen = getScreenSize(window);
		double x =
			(szScreen.getWidth() > sz.getWidth() ? (szScreen.getWidth() - sz.getWidth()) / 2 : 0);
		double y = (szScreen.getHeight() > sz.getHeight()
			? (szScreen.getHeight() - sz.getHeight()) / 2 : 0);
		window.setLocation(Double.valueOf(x).intValue(), Double.valueOf(y).intValue());
	}

	/**
	 * Fills the array list with the all the components contained in the parent
	 * component and its sub-components.
	 *
	 * @param list The list to fill.
	 * @param cmp  The parent component.
	 */
	public static void fillComponentList(Component cmp, List<Component> list) {
		list.add(cmp);
		if (cmp instanceof Container) {
			Container cnt = (Container) cmp;
			for (int i = 0; i < cnt.getComponentCount(); i++) {
				Component child = cnt.getComponent(i);
				fillComponentList(child, list);
			}
		}
	}

	/**
	 * Convert.
	 *
	 * @param size AWT dimension.
	 * @return MLT dimension.
	 */
	public static Dimension fromAWT(java.awt.Dimension size) {
		return new Dimension(size.getWidth(), size.getHeight());
	}

	/**
	 * Convert.
	 *
	 * @param insets AWT insets.
	 * @return MLT insets.
	 */
	public static Insets fromAWT(java.awt.Insets insets) {
		return new Insets(insets.top, insets.left, insets.bottom, insets.right);
	}

	/**
	 * Return the orientation given a Swing orientation.
	 * 
	 * @param orientation The Swing orientation.
	 * @return The Orientation.
	 */
	public static Orientation fromAWT(int orientation) {
		if (orientation == SwingConstants.HORIZONTAL) {
			return Orientation.HORIZONTAL;
		}
		if (orientation == SwingConstants.VERTICAL) {
			return Orientation.VERTICAL;
		}
		return Orientation.HORIZONTAL;
	}

	/**
	 * Returns the list of all components contained in a component and its
	 * subcomponents.
	 *
	 * @return The list of components.
	 * @param parent The parent component.
	 */
	public static List<Component> getAllChildComponents(Component parent) {
		List<Component> list = new ArrayList<>();
		fillComponentList(parent, list);
		return list;
	}

	/**
	 * Returns the list of all components contained in the tres that contains the
	 * component.
	 *
	 * @return The list of components.
	 * @param parent The starting component.
	 */
	public static List<Component> getAllComponents(Component parent) {
		return getAllChildComponents(getTopComponent(parent));
	}

	/**
	 * Returns the graphics device that should apply to a window.
	 *
	 * @param window The window.
	 * @return The graphics device.
	 */
	private static GraphicsDevice getGraphicsDevice(Window window) {
		if (window != null) {
			return window.getGraphicsConfiguration().getDevice();
		}
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
	}

	/**
	 * Returns the size of the screen containing the argument window or the primary
	 * screen if current window is not
	 * selected.
	 *
	 * @param window The window.
	 * @return The screen size.
	 */
	public static Dimension getScreenSize(Window window) {
		return fromAWT(getGraphicsDevice(window).getConfigurations()[0].getBounds().getSize());
	}

	/**
	 * Return the top component, normally a Frame or Dialog.
	 *
	 * @return The top component, normally a frame or dialog.
	 * @param cmp The start search component.
	 */
	public static Component getTopComponent(Component cmp) {
		while (cmp != null) {
			if (cmp instanceof JFrame || cmp instanceof JDialog || cmp instanceof JWindow) {
				return cmp;
			}
			cmp = cmp.getParent();
		}
		return null;
	}

	/**
	 * Installs an instance of the accelerator key listener in the tree of controls
	 * where the argument control is
	 * included, starting in the first parent Frame or Dialog.
	 *
	 * @param control The starting control in the tree.
	 */
	public static void installAcceleratorKeyListener(Control control) {
		installKeyListener(control, new AcceleratorKeyListener());
	}

	/**
	 * Installs an instance the key listener in the tree of controls where the
	 * argument control is included, starting in
	 * the first parent Frame or Dialog.
	 *
	 * @param control     The control.
	 * @param keyListener The listener to install.
	 */
	public static void installKeyListener(Control control, KeyListener keyListener) {
		addKeyListener(getTopComponent(control.getComponent()), keyListener);
	}

	/**
	 * Installs an instance the key listener in the tree of controls where the
	 * argument control is included, starting in
	 * the first parent Frame or Dialog.
	 *
	 * @param stage       The stage.
	 * @param keyListener The listener to install.
	 */
	public static void installKeyListener(Stage stage, KeyListener keyListener) {
		installKeyListener(stage.getContent(), keyListener);
	}

	/**
	 * Installs the listener in the tree of controls where the argument control is
	 * included, starting in the first
	 * parent Frame or Dialog parent.
	 *
	 * @param control            The control.
	 * @param mouseWheelListener The listener to install.
	 */
	public static void installMouseWheelListener(
		Control control,
		MouseWheelListener mouseWheelListener) {
		addMouseWheelListener(getTopComponent(control.getComponent()), mouseWheelListener);
	}

	/**
	 * Installs the listener in the tree of controls where the argument control is
	 * included, starting in the first
	 * parent Frame or Dialog parent.
	 *
	 * @param stage              The stage.
	 * @param mouseWheelListener The listener to install.
	 */
	public static void installMouseWheelListener(
		Stage stage,
		MouseWheelListener mouseWheelListener) {
		installMouseWheelListener(stage.getContent(), mouseWheelListener);
	}

	/**
	 * Remove the listener to the component and its children.
	 *
	 * @param c The component.
	 * @param l The listener.
	 */
	public static void removeMouseWheelListener(Component c, MouseWheelListener l) {
		List<Component> components = getAllChildComponents(c);
		for (Component component : components) {
			component.removeMouseWheelListener(l);
		}
	}

	/**
	 * Remove the listener to the control and its children.
	 *
	 * @param c The control.
	 * @param l The listener.
	 */
	public static void removeMouseWheelListener(Control c, MouseWheelListener l) {
		removeMouseWheelListener(c.getComponent(), l);
	}

	/**
	 * Set the window applying a width and/or height factor relative to the screen
	 * dimension.
	 *
	 * @param window       The window.
	 * @param widthFactor  The width factor relative to the screen (0 &lt; factor
	 *                     &lt;= 1).
	 * @param heightFactor The height factor relative to the screen (0 &lt; factor
	 *                     &lt;= 1).
	 * @return The screen dimension.
	 */
	public static void setSize(Window window, double widthFactor, double heightFactor) {
		Dimension d = getScreenSize(window);
		double width = d.getWidth() * widthFactor;
		double height = d.getHeight() * heightFactor;
		window.setSize(Double.valueOf(width).intValue(), Double.valueOf(height).intValue());
	}

	/**
	 * Convert.
	 *
	 * @param alignment MLT alignment.
	 * @return AWT alignment.
	 */
	public static int toAWT(Alignment alignment) {
		switch (alignment) {
		case TOP:
			return SwingConstants.TOP;
		case LEFT:
			return SwingConstants.LEFT;
		case CENTER:
			return SwingConstants.CENTER;
		case BOTTOM:
			return SwingConstants.BOTTOM;
		case RIGHT:
			return SwingConstants.RIGHT;
		default:
			return SwingConstants.CENTER;
		}
	}

	/**
	 * Convert.
	 *
	 * @param anchor MLT anchor.
	 * @return Grid bag constraints anchor.
	 */
	public static int toAWT(Anchor anchor) {
		switch (anchor) {
		case TOP:
			return GridBagConstraints.NORTH;
		case TOP_LEFT:
			return GridBagConstraints.NORTHWEST;
		case TOP_RIGHT:
			return GridBagConstraints.NORTHEAST;
		case LEFT:
			return GridBagConstraints.WEST;
		case BOTTOM:
			return GridBagConstraints.SOUTH;
		case BOTTOM_LEFT:
			return GridBagConstraints.SOUTHWEST;
		case BOTTOM_RIGHT:
			return GridBagConstraints.SOUTHEAST;
		case RIGHT:
			return GridBagConstraints.EAST;
		case CENTER:
			return GridBagConstraints.CENTER;
		}
		return GridBagConstraints.CENTER;
	}

	/**
	 * Convert.
	 *
	 * @param constraints MLT constraints.
	 * @return AWT grid bag constraints.
	 */
	public static GridBagConstraints toAWT(Constraints constraints) {
		GridBagConstraints awt_constraints = new GridBagConstraints();
		awt_constraints.anchor = toAWT(constraints.getAnchor());
		awt_constraints.fill = toAWT(constraints.getFill());
		awt_constraints.gridx = constraints.getX();
		awt_constraints.gridy = constraints.getY();
		awt_constraints.gridwidth = constraints.getWidth();
		awt_constraints.gridheight = constraints.getHeight();
		awt_constraints.weightx = constraints.getWeightx();
		awt_constraints.weighty = constraints.getWeighty();
		awt_constraints.insets = toAWT(constraints.getInsets());
		awt_constraints.ipadx = 0;
		awt_constraints.ipady = 0;
		return awt_constraints;
	}

	/**
	 * Convert.
	 *
	 * @param size MLT dimension.
	 * @return AWT dimension.
	 */
	public static java.awt.Dimension toAWT(Dimension size) {
		int w = (int) Numbers.round(size.getWidth(), 0);
		int h = (int) Numbers.round(size.getHeight(), 0);
		return new java.awt.Dimension(w, h);
	}

	/**
	 * Convert.
	 *
	 * @param fill MLT fill.
	 * @return AWT fill.
	 */
	public static int toAWT(Fill fill) {
		switch (fill) {
		case NONE:
			return GridBagConstraints.NONE;
		case HORIZONTAL:
			return GridBagConstraints.HORIZONTAL;
		case VERTICAL:
			return GridBagConstraints.VERTICAL;
		case BOTH:
			return GridBagConstraints.BOTH;
		}
		return GridBagConstraints.NONE;
	}

	/**
	 * Convert.
	 *
	 * @param insets MLT insets.
	 * @return AWT insets.
	 */
	public static java.awt.Insets toAWT(Insets insets) {
		int t = (int) Numbers.round(insets.getTop(), 0);
		int l = (int) Numbers.round(insets.getLeft(), 0);
		int b = (int) Numbers.round(insets.getBottom(), 0);
		int r = (int) Numbers.round(insets.getRight(), 0);
		return new java.awt.Insets(t, l, b, r);
	}

	/**
	 * Convert.
	 *
	 * @param orientation MLT orientation.
	 * @return AWT orientation.
	 */
	public static int toAWT(Orientation orientation) {
		switch (orientation) {
		case HORIZONTAL:
			return SwingConstants.HORIZONTAL;
		case VERTICAL:
			return SwingConstants.VERTICAL;
		}
		return SwingConstants.HORIZONTAL;
	}

	/**
	 * Returns a string representation of a key stroke.
	 *
	 * @param keyStroke The key stroke.
	 * @return The string representation.
	 */
	public static String toString(KeyStroke keyStroke) {
		StringBuilder b = new StringBuilder();
		if (keyStroke != null) {
			int modifiers = keyStroke.getModifiers();
			if (modifiers > 0) {
				b.append(KeyEvent.getModifiersExText(modifiers));
				b.append("+");
			}
			b.append(KeyEvent.getKeyText(keyStroke.getKeyCode()));
		}
		return b.toString();
	}
}
