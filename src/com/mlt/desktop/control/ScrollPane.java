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

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import com.mlt.desktop.layout.Orientation;

/**
 * Scroll pane extension.
 *
 * @author Miquel Sas
 */
public class ScrollPane extends Control {

	/**
	 * Scroll bar policies.
	 */
	public static enum ScrollBarPolicy {
		AS_NEEDED, NEVER, ALWAYS;
	}

	/**
	 * Sub classed to use this library scroll bar controls.
	 */
	class ScrollPaneCmp extends JScrollPane {

		/**
		 * Constructor.
		 */
		private ScrollPaneCmp() {
			super();
		}

		/**
		 * Constructor.
		 * 
		 * @param view The view.
		 */
		private ScrollPaneCmp(Component view) {
			super(view);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public JScrollBar createHorizontalScrollBar() {
			return ScrollPane.this.horizontalScrollBar.getComponent();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public JScrollBar createVerticalScrollBar() {
			return ScrollPane.this.verticalScrollBar.getComponent();
		}

	}

	/** Control view. */
	private Control view;
	/** Horizontal scroll bar. */
	private ScrollBar horizontalScrollBar = new ScrollBar(Orientation.HORIZONTAL);
	/** Vertical scroll bar. */
	private ScrollBar verticalScrollBar = new ScrollBar(Orientation.VERTICAL);

	/**
	 * Constructor.
	 */
	public ScrollPane() {
		super();
		setComponent(new ScrollPaneCmp());
	}

	/**
	 * Constructor assigning the control view.
	 * 
	 * @param view The control view.
	 */
	public ScrollPane(Control view) {
		super();
		this.view = view;
		setComponent(new ScrollPaneCmp(view.getComponent()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JScrollPane getComponent() {
		return (JScrollPane) super.getComponent();
	}

	/**
	 * Return the horizontal scroll bar.
	 * 
	 * @return The horizontal scroll bar.
	 */
	public ScrollBar getHorizontalScrollBar() {
		return horizontalScrollBar;
	}

	/**
	 * Return the vertical scroll bar.
	 * 
	 * @return The vertical scroll bar.
	 */
	public ScrollBar getVerticalScrollBar() {
		return verticalScrollBar;
	}

	/**
	 * Return the control view.
	 * 
	 * @return The control which component is the view.
	 */
	public Control getView() {
		return view;
	}

	/**
	 * Set the horizontal scroll bar policy.
	 * 
	 * @param policy The policy.
	 */
	public void setHorizontalScrollBarPolicy(ScrollBarPolicy policy) {
		switch (policy) {
		case AS_NEEDED:
			getComponent().setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			break;
		case NEVER:
			getComponent().setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			break;
		case ALWAYS:
			getComponent().setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
			break;
		}
	}

	/**
	 * Set the vertical scroll bar policy.
	 * 
	 * @param policy The policy.
	 */
	public void setVerticalScrollBarPolicy(ScrollBarPolicy policy) {
		switch (policy) {
		case AS_NEEDED:
			getComponent().setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			break;
		case NEVER:
			getComponent().setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
			break;
		case ALWAYS:
			getComponent().setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			break;
		}
	}

	/**
	 * Set the view.
	 * 
	 * @param view The view control.
	 */
	public void setView(Control view) {
		getComponent().getViewport().setView(view.getComponent());
		this.view = view;
	}
}
