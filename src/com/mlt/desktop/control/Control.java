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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.event.ComponentListener;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.border.Border;

import com.mlt.desktop.AWT;
import com.mlt.desktop.Option;
import com.mlt.desktop.event.MouseHandler;
import com.mlt.desktop.layout.Dimension;
import com.mlt.util.Numbers;
import com.mlt.util.Properties;

/**
 * Base control, essentially a component wrapper, intended to expose only the
 * methods of component used around this library.
 *
 * @author Miquel Sas
 */
public class Control {

	/**
	 * Return a list with all controls in the tree containing the argument control.
	 *
	 * @param sourceControl The control to start scanning first up (parents) and
	 *                      then down (children).
	 * @return The list with all controls in the tree containing the control.
	 */
	public static List<Control> getAllControls(Control sourceControl) {
		List<Control> controls = new ArrayList<>();
		List<Component> components = AWT.getAllComponents(sourceControl.getComponent());
		for (int i = 0; i < components.size(); i++) {
			if (components.get(i) instanceof JComponent) {
				JComponent component = (JComponent) components.get(i);
				Control control = getControl(component);
				if (control != null) {
					controls.add(control);
				}
			}
		}
		return controls;
	}

	/**
	 * Return a list with all controls in the tree starting at the argument control.
	 *
	 * @param sourceControl The control to start scanning.
	 * @return The list with all controls in the tree containing the control.
	 */
	public static List<Control> getAllChildControls(Control sourceControl) {
		List<Control> controls = new ArrayList<>();
		if (sourceControl == null) return controls;
		List<Component> components = AWT.getAllChildComponents(sourceControl.getComponent());
		for (int i = 0; i < components.size(); i++) {
			if (components.get(i) instanceof JComponent) {
				JComponent component = (JComponent) components.get(i);
				Control control = getControl(component);
				if (control != null) {
					controls.add(control);
				}
			}
		}
		return controls;
	}

	/**
	 * Return the control stored in the component.
	 *
	 * @param component The JComponent.
	 * @return The control.
	 */
	public static Control getControl(JComponent component) {
		if (component == null) {
			return null;
		}
		return (Control) getProperties(component).getObject("CONTROL");
	}

	/**
	 * Returns the properties installed in the component. Since the JComponent class
	 * does not offer a direct method to install user objects, we use the tricky
	 * workaround of using the input map with a MIN_INTEGER key stroke.
	 *
	 * @param component The JComponent.
	 * @return The properties.
	 */
	private static Properties getProperties(JComponent component) {
		InputMap map = component.getInputMap();
		KeyStroke key = KeyStroke.getKeyStroke(Numbers.MIN_INTEGER, 0);
		Properties properties = (Properties) map.get(key);
		if (properties == null) {
			properties = new Properties();
			map.put(key, properties);
		}
		return properties;
	}

	/**
	 * Mouse listener to launch the possible popup menu.
	 */
	class PopupHandler extends MouseHandler {

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3) {
				if (getPopupMenuProvider() != null) {
					/*
					 * Recalculate mouse relative location vs component, because with scroll panes
					 * it is not correct.
					 */
					Point mp = e.getLocationOnScreen();
					Point cp = getComponent().getLocationOnScreen();
					Point p = new Point(mp.x - cp.x, mp.y - cp.y);
					PopupMenu popupMenu = getPopupMenuProvider().getPopupMenu(Control.this);
					popupMenu.getComponent().show(getComponent(), p.x, p.y);
				}
			}
		}
	}

	/** Internal component. */
	private JComponent component;

	/**
	 * Constructor.
	 */
	protected Control() {
		super();
	}

	/**
	 * Add a component listener.
	 * 
	 * @param l The listener.
	 */
	public void addComponentListener(ComponentListener l) {
		List<Component> components = AWT.getAllChildComponents(getComponent());
		components.forEach(component -> component.addComponentListener(l));
	}

	/**
	 * Add a focus listener.
	 * 
	 * @param l The listener.
	 */
	public void addFocusListener(FocusListener l) {
		List<Component> components = AWT.getAllChildComponents(getComponent());
		components.forEach(component -> component.addFocusListener(l));
	}

	/**
	 * Add a key listener to all components contained in this control.
	 *
	 * @param l The listener.
	 */
	public void addKeyListener(KeyListener l) {
		List<Component> components = AWT.getAllChildComponents(getComponent());
		components.forEach(component -> component.addKeyListener(l));
	}

	/**
	 * Add a mouse listener to all components contained in this control.
	 *
	 * @param l The listener.
	 */
	public void addMouseListener(MouseListener l) {
		List<Component> components = AWT.getAllChildComponents(getComponent());
		components.forEach(component -> component.addMouseListener(l));
	}

	/**
	 * Add a mouse motion listener to all components contained in this control.
	 *
	 * @param l The listener.
	 */
	public void addMouseMotionListener(MouseMotionListener l) {
		List<Component> components = AWT.getAllChildComponents(getComponent());
		components.forEach(component -> component.addMouseMotionListener(l));
	}

	/**
	 * Add a mouse wheel listener to all components contained in this control.
	 *
	 * @param l The listener.
	 */
	public void addMouseWheelListener(MouseWheelListener l) {
		List<Component> components = AWT.getAllChildComponents(getComponent());
		components.forEach(component -> component.addMouseWheelListener(l));
	}

	/**
	 * Return the background color.
	 *
	 * @return The background color.
	 */
	public Color getBackground() {
		return component.getBackground();
	}

	/**
	 * Return the border.
	 *
	 * @return The border.
	 */
	public Border getBorder() {
		return component.getBorder();
	}

	/**
	 * Return the UI control component. Override to return the proper control
	 * component.
	 *
	 * @return The control component.
	 */
	public JComponent getComponent() {
		return component;
	}

	/**
	 * Return the default font.
	 *
	 * @return The font.
	 */
	public Font getFont() {
		return component.getFont();
	}

	/**
	 * Return the font metrics of the current font.
	 * 
	 * @return The font metrics.
	 */
	public FontMetrics getFontMetrics() {
		return getComponent().getFontMetrics(getFont());
	}

	/**
	 * Return the foreground color.
	 *
	 * @return The foreground color.
	 */
	public Color getForeground() {
		return component.getForeground();
	}

	/**
	 * Return the maximum size.
	 *
	 * @return The maximum size.
	 */
	public Dimension getMaximumSize() {
		return AWT.fromAWT(component.getMaximumSize());
	}

	/**
	 * Return the minimum size.
	 *
	 * @return The minimum size.
	 */
	public Dimension getMinimumSize() {
		return AWT.fromAWT(component.getMinimumSize());
	}

	/**
	 * Return the name.
	 *
	 * @return The name.
	 */
	public String getName() {
		return component.getName();
	}

	/**
	 * Return the option property (optional).
	 *
	 * @return The option.
	 */
	public Option getOption() {
		return (Option) getProperty("OPTION");
	}

	/**
	 * Return the parent control.
	 * 
	 * @return The parent control.
	 */
	public Control getParent() {
		Component parent = component.getParent();
		if (parent instanceof JComponent) {
			return getControl((JComponent) parent);
		}
		return null;
	}

	/**
	 * Return the preferred size.
	 *
	 * @return The preferred size.
	 */
	public Dimension getPreferredSize() {
		return AWT.fromAWT(component.getPreferredSize());
	}

	/**
	 * Return the popup menu provider.
	 *
	 * @return The popup menu provider.
	 */
	public PopupMenuProvider getPopupMenuProvider() {
		return (PopupMenuProvider) getProperty("POPUP_MENU_PROVIDER");
	}

	/**
	 * Return the property set in the component.
	 *
	 * @param key The key.
	 * @return The property.
	 */
	public Object getProperty(String key) {
		return getProperties(getComponent()).getObject(key);
	}

	/**
	 * Return the size.
	 *
	 * @return The size.
	 */
	public Dimension getSize() {
		return AWT.fromAWT(component.getSize());
	}

	/**
	 * Return the stage containing the control or null if not contained in any
	 * stage.
	 *
	 * @return The stage or null.
	 */
	public Stage getStage() {
		Component cmp = getComponent();
		while (true) {
			if (cmp == null) {
				break;
			}
			if (cmp instanceof JFrame || cmp instanceof JDialog || cmp instanceof JWindow) {
				break;
			}
			cmp = cmp.getParent();
		}
		if (cmp != null) {
			return Stage.getStage((java.awt.Window) cmp);
		}
		return null;
	}

	/**
	 * Return the tooltip text.
	 *
	 * @return The tooltip text.
	 * @see javax.swing.JComponent#getToolTipText()
	 */
	public String getToolTipText() {
		return getComponent().getToolTipText();
	}

	/**
	 * Invalidate the control.
	 */
	public void invalidate() {
		component.invalidate();
	}

	/**
	 * Check enabled.
	 *
	 * @return A boolean.
	 */
	public boolean isEnabled() {
		return component.isEnabled();
	}

	/**
	 * Check opaque.
	 *
	 * @return A boolean.
	 */
	public boolean isOpaque() {
		return component.isOpaque();
	}

	/**
	 * Check whether any of this control components is the source of the event.
	 * 
	 * @param e The the event.
	 * @return A boolean indicating whether any of this control components is the
	 *         source.
	 */
	public boolean isSource(EventObject e) {
		List<Component> components = AWT.getAllChildComponents(getComponent());
		for (Component component : components) {
			if (component == e.getSource()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check visibility.
	 *
	 * @return A boolean.
	 */
	public boolean isVisible() {
		return component.isVisible();
	}

	/**
	 * Remove the property.
	 *
	 * @param key The key.
	 * @return The property or null.
	 */
	protected Object removeProperty(String key) {
		return getProperties(getComponent()).remove(key);
	}

	/**
	 * Repaint.
	 */
	public void repaint() {
		component.repaint();
	}

	/**
	 * Request focus.
	 */
	public void requestFocus() {
		component.requestFocus();
	}

	/**
	 * Re-validate.
	 */
	public void revalidate() {
		component.revalidate();
	}

	/**
	 * Set the background color.
	 *
	 * @param background The background color.
	 */
	public final void setBackground(Color background) {
		component.setBackground(background);
	}

	/**
	 * Set the border.
	 *
	 * @param border The border.
	 */
	public final void setBorder(Border border) {
		component.setBorder(border);
	}

	/**
	 * Set the component.
	 *
	 * @param component The swing component.
	 */
	protected final void setComponent(JComponent component) {
		this.component = component;
		/* Store the control. */
		setProperty("CONTROL", this);
	}

	/**
	 * Set enabled/disabled.
	 *
	 * @param enabled A boolean.
	 */
	public void setEnabled(boolean enabled) {
		component.setEnabled(enabled);
	}

	public void setFocusable(boolean focusable) {
		getComponent().setFocusable(focusable);
		AWT.getAllChildComponents(getComponent()).forEach(cmp -> cmp.setFocusable(focusable));
	}

	/**
	 * Set the font.
	 *
	 * @param font The font.
	 */
	public void setFont(Font font) {
		component.setFont(font);
	}

	/**
	 * Set the foreground color.
	 *
	 * @param foreground The foreground color.
	 */
	public void setForeground(Color foreground) {
		component.setForeground(foreground);
	}

	/**
	 * Set the maximum size.
	 *
	 * @param size The maximum size.
	 */
	public void setMaximumSize(Dimension size) {
		component.setMaximumSize(AWT.toAWT(size));
	}

	/**
	 * Set the minimum size.
	 *
	 * @param size The minimum size.
	 */
	public void setMinimumSize(Dimension size) {
		component.setMinimumSize(AWT.toAWT(size));
	}

	/**
	 * Set the name.
	 *
	 * @param name The name.
	 */
	public final void setName(String name) {
		component.setName(name);
	}

	/**
	 * Set opaque.
	 *
	 * @param opaque A boolean.
	 */
	public final void setOpaque(boolean opaque) {
		component.setOpaque(opaque);
	}

	/**
	 * Set the option.
	 *
	 * @param option The option.
	 */
	public void setOption(Option option) {
		setProperty("OPTION", option);
	}

	/**
	 * Set the popup menu provider.
	 *
	 * @param popupMenuProvider The popup menu provider.
	 */
	public void setPopupMenuProvider(PopupMenuProvider popupMenuProvider) {
		setProperty("POPUP_MENU_PROVIDER", popupMenuProvider);
		PopupHandler handler = new PopupHandler();
		addMouseListener(handler);
		setProperty("POPUP_HANDLER", handler);
	}

	/**
	 * Set the preferred size.
	 *
	 * @param size The preferred size.
	 */
	public void setPreferredSize(Dimension size) {
		component.setPreferredSize(AWT.toAWT(size));
	}

	/**
	 * Set the property in the component.
	 *
	 * @param key      The key.
	 * @param property The property.
	 */
	public void setProperty(String key, Object property) {
		getProperties(getComponent()).setObject(key, property);
	}

	/**
	 * Set the size.
	 *
	 * @param size The size.
	 */
	public void setSize(Dimension size) {
		component.setSize(AWT.toAWT(size));
	}

	/**
	 * Set the tooltip text.
	 *
	 * @param text The text.
	 */
	public final void setToolTipText(String text) {
		getComponent().setToolTipText(text);
	}

	/**
	 * Set visible.
	 *
	 * @param visible A boolean.
	 */
	public void setVisible(boolean visible) {
		component.setVisible(visible);
	}
}
