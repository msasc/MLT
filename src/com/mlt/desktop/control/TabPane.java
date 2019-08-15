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
package com.mlt.desktop.control;

import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import com.mlt.desktop.event.MouseHandler;
import com.mlt.desktop.icon.AbstractIcon;
import com.mlt.desktop.icon.IconClose;
import com.mlt.desktop.icon.IconDock;
import com.mlt.desktop.icon.Icons;
import com.mlt.desktop.layout.Anchor;
import com.mlt.desktop.layout.Constraints;
import com.mlt.desktop.layout.Dimension;
import com.mlt.desktop.layout.Fill;
import com.mlt.desktop.layout.Insets;
import com.mlt.desktop.layout.Position;
import com.mlt.util.Resources;

/**
 * A tab pane control.
 *
 * @author Miquel Sas
 */
public class TabPane extends Control {

	/**
	 * Close tab action.
	 */
	class ActionClose implements ActionListener {

		Tab tab;

		ActionClose(Tab tab) {
			this.tab = tab;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			int index = getComponent().indexOfTabComponent(tab.getComponent());
			if (index >= 0) getComponent().removeTabAt(index);
		}

	}

	/**
	 * Dock tab action.
	 */
	class ActionDock implements ActionListener {

		Tab tab;

		ActionDock(Tab tab) {
			this.tab = tab;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			int index = getComponent().indexOfTabComponent(tab.getComponent());
			if (index < 0) return;

			FrameDock frame = new FrameDock(tab);
			GridBagPane content = (GridBagPane) frame.getContent();
			content.add(tab.control,
				new Constraints(Anchor.CENTER, Fill.BOTH, 0, 0, new Insets(0, 0, 0, 0)));
			if (tab.icon != null) {
				if (tab.icon instanceof AbstractIcon) {
					AbstractIcon icon = ((AbstractIcon) tab.icon).copy();
					icon.setSize(16, 16);
					Image img = Icons.getImage(frame.getComponent(), icon);
					frame.setImage(img);
				} else {
					Image img = Icons.getImage(frame.getComponent(), tab.icon);
					frame.setImage(img);
				}
			}
			frame.setSize(0.8, 0.8);
			frame.centerOnScreen();
			frame.setVisible(true);
		}

	}

	/**
	 * Dock frame.
	 */
	class FrameDock extends Frame {

		/**
		 * Close operation.
		 */
		class Listener extends Stage.Adapter {

			@Override
			public void closing(Stage stage) {
				insertTab(tab);
				FrameDock.this.setVisible(false);
			}
		}

		Tab tab;

		FrameDock(Tab tab) {
			super(new GridBagPane());
			this.tab = tab;
			setTitle(tab.title);
			addWindowListener(new Listener());
		}

		@Override
		public void setVisible(boolean b) {
			if (b) {
				docks.add(this);
			} else {
				docks.remove(this);
			}
			super.setVisible(b);
		}
	}

	/**
	 * Mouse listener.
	 */
	class MouseListener extends MouseHandler {

		Tab tab;

		MouseListener(Tab tab) {
			this.tab = tab;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			int index = getComponent().indexOfTabComponent(tab.getComponent());
			if (index >= 0) getComponent().setSelectedIndex(index);
		}
	}

	/**
	 * Tab. Has two buttons on the right of the text, one to float the tab and one
	 * to close it.
	 */
	class Tab extends GridBagPane {

		/** Initial tab index. */
		int index;
		/** Tab id. */
		String id;
		/** Tab icon. */
		Icon icon;
		/** Tab title. */
		String title;
		/** Tab tool tip. */
		String tooltip;
		/** Tab control. */
		Control control;

		/**
		 * Constructor.
		 *
		 * @param id      Id or name, used as a key to access the tab.
		 * @param icon    Optional icon.
		 * @param title   Title.
		 * @param tooltip Tooltip.
		 * @param control Control.
		 */
		Tab(int index, String id, Icon icon, String title, String tooltip, Control control) {

			/* Ensure the id. */
			if (id == null) throw new NullPointerException();
			setName(id);

			/* Save members. They will be reused when re-docked. */
			this.index = index;
			this.id = id;
			this.icon = icon;
			this.title = title;
			this.tooltip = tooltip;
			this.control = control;

			/* Set the tooltip to the entire tab title. */
			if (tooltip != null) setToolTipText(tooltip);

			/* Label with the optional icon. */
			Label label = new Label();
			if (icon != null) {
				label.setIcon(icon);
				label.setIconTextGap(5);
			}
			label.setText(title);
			label.setToolTipText(tooltip);
			add(label, new Constraints(Anchor.LEFT, Fill.NONE, 0, 0, new Insets(5, 0, 2, 0)));

			/* Dock button. */
			String tipDock = Resources.getText("tabDock");
			Button buttonDock = createButton("Dock", tipDock, new IconDock());
			buttonDock.setAction(new ActionDock(this));
			add(buttonDock,
				new Constraints(Anchor.RIGHT, Fill.NONE, 1, 0, new Insets(5, 10, 2, 0)));

			/* Close button. */
			String tipClose = Resources.getText("tabClose");
			Button buttonClose = createButton("Close", tipClose, new IconClose());
			buttonClose.setAction(new ActionClose(this));
			add(buttonClose,
				new Constraints(Anchor.RIGHT, Fill.NONE, 2, 0, new Insets(5, 2, 2, 0)));

			/* Nothing is opaque. */
			setOpaque(false);
			label.setOpaque(false);
			buttonClose.setOpaque(false);

			/* Mouse listener. */
			MouseListener mouseListener = new MouseListener(this);
			getComponent().addMouseListener(mouseListener);
			label.getComponent().addMouseListener(mouseListener);
			buttonDock.getComponent().addMouseListener(mouseListener);
			buttonClose.getComponent().addMouseListener(mouseListener);
		}

		/**
		 * Create the button.
		 *
		 * @param name    Button name.
		 * @param tooltip Tool tip.
		 * @param icon    Icon.
		 * @return The button.
		 */
		private Button createButton(String name, String tooltip, AbstractIcon icon) {
			Button button = new Button();
			button.setName(name);
			button.setText(null);
			button.setToolTipText(tooltip);
			button.setIconTextGap(0);
			button.setMargin(new Insets(0, 0, 0, 0));
			icon.setSize(20, 20);
			button.setIcon(icon);
			Dimension size = new Dimension(icon.getIconWidth(), icon.getIconHeight());
			button.setMinimumSize(size);
			button.setMaximumSize(size);
			button.setPreferredSize(size);
			return button;
		}
	}

	/** List of docked tabs. */
	private List<FrameDock> docks = new ArrayList<>();

	/**
	 * Constructor.
	 */
	public TabPane() {
		super();
		setComponent(new JTabbedPane());
		getComponent().setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
	}

	/**
	 * Add a tab.
	 *
	 * @param id      Id.
	 * @param title   Title.
	 * @param tooltip Tool tip.
	 * @param control Control.
	 */
	public void addTab(String id, String title, String tooltip, Control control) {
		addTab(id, null, title, tooltip, control);
	}

	/**
	 * Add a tab.
	 *
	 * @param id      Id.
	 * @param icon    Icon.
	 * @param title   Title.
	 * @param tooltip Tool tip.
	 * @param control Control.
	 */
	public void addTab(String id, Icon icon, String title, String tooltip, Control control) {
		int index = getComponent().getTabCount();
		Tab tab = new Tab(index, id, icon, title, tooltip, control);
		insertTab(tab);
	}

	/**
	 * Insert the tab trying to preserve its index.
	 *
	 * @param tab The tab to insert.
	 */
	private void insertTab(Tab tab) {
		int index = tab.index;
		if (index > getComponent().getTabCount()) {
			tab.index = index = getComponent().getTabCount();
		}
		getComponent().insertTab(null, null, tab.control.getComponent(), tab.tooltip, index);
		getComponent().setTabComponentAt(index, tab.getComponent());
		getComponent().setSelectedIndex(index);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final JTabbedPane getComponent() {
		return (JTabbedPane) super.getComponent();
	}

	/**
	 * Tab control access.
	 *
	 * @param index The index.
	 * @return The tab.
	 */
	private Tab getTab(int index) {
		Component cmp = getComponent().getTabComponentAt(index);
		Tab tab = (Tab) Control.getControl((JComponent) cmp);
		return tab;
	}

	/**
	 * Return the number of tabs.
	 *
	 * @return The number of tabs.
	 */
	public int getTabCount() {
		return getComponent().getTabCount();
	}

	/**
	 * Return the first tab index with the given id.
	 *
	 * @param id The tab id.
	 * @return The first tab index with the given id or -1.
	 */
	public int getTabIndex(String id) {
		for (int i = 0; i < getTabCount(); i++) {
			Tab tab = getTab(i);
			if (tab.id.equals(id)) {
				return tab.index;
			}
		}
		for (int i = 0; i < docks.size(); i++) {
			Tab tab = docks.get(i).tab;
			if (tab.id.equals(id)) {
				return tab.index;
			}
		}
		return -1;
	}

	/**
	 * Return the tab placement.
	 *
	 * @return The tab placement.
	 */
	public Position getTabPlacement() {
		switch (getComponent().getTabPlacement()) {
		case JTabbedPane.TOP:
			return Position.TOP;
		case JTabbedPane.LEFT:
			return Position.LEFT;
		case JTabbedPane.BOTTOM:
			return Position.BOTTOM;
		case JTabbedPane.RIGHT:
			return Position.RIGHT;
		}
		throw new IllegalStateException();
	}

	/**
	 * Select the tab with the argument index.
	 * 
	 * @param index The index.
	 */
	public void setSelectedIndex(int index) {
		getComponent().setSelectedIndex(index);
	}

	/**
	 * Set the tab placement.
	 *
	 * @param placement The tab placement.
	 */
	public void setTabPlacement(Position placement) {
		if (placement == null) throw new NullPointerException();
		if (placement == Position.CENTER) throw new IllegalArgumentException();
		int tabPlacement = 0;
		switch (placement) {
		case TOP:
			tabPlacement = JTabbedPane.TOP;
			break;
		case LEFT:
			tabPlacement = JTabbedPane.LEFT;
			break;
		case BOTTOM:
			tabPlacement = JTabbedPane.BOTTOM;
			break;
		case RIGHT:
			tabPlacement = JTabbedPane.RIGHT;
			break;
		default:
			tabPlacement = JTabbedPane.TOP;
			break;
		}
		getComponent().setTabPlacement(tabPlacement);
	}
}
