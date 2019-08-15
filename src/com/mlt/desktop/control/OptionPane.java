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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mlt.desktop.Option;
import com.mlt.desktop.layout.Alignment;
import com.mlt.desktop.layout.Anchor;
import com.mlt.desktop.layout.Constraints;
import com.mlt.desktop.layout.Fill;
import com.mlt.desktop.layout.HorizontalFlowLayout;
import com.mlt.desktop.layout.Insets;
import com.mlt.desktop.layout.Orientation;
import com.mlt.util.Lists;

/**
 * A pane to layout options (buttons) either horizontally or vertically.
 *
 * @author Miquel Sas
 */
public class OptionPane extends Pane implements PopupMenuProvider {

	/**
	 * Listener.
	 */
	public interface Listener {

		/**
		 * Called when an option has been added.
		 *
		 * @param option
		 */
		void added(Option option);

		/**
		 * Called when the option has been removed.
		 *
		 * @param option
		 */
		void removed(Option option);
	}

	/** Orientation. */
	private Orientation orientation;
	/** Alignment when orientation is horizontal. */
	private Alignment horizontalAlignment = Alignment.CENTER;
	/** Alignment when orientation is vertical. */
	private Alignment verticalAlignment = Alignment.CENTER;
	/** List of options. */
	private List<Option> options = new ArrayList<>();

	/** List of listeners. */
	private List<Listener> listeners = new ArrayList<>();

	/**
	 * Constructor.
	 *
	 * @param orientation Orientation.
	 */
	public OptionPane(Orientation orientation) {
		super();
		this.orientation = orientation;
	}

	/**
	 * Add the list of options.
	 *
	 * @param options The list of options.
	 */
	public void add(List<Option> options) {
		this.options.addAll(options);
		options.forEach(option -> listeners.forEach(listener -> listener.added(option)));
		layoutOptions();
	}

	/**
	 * Add the list of options.
	 *
	 * @param options The list of options.
	 */
	public void add(Option... options) {
		add(Lists.asList(options));
	}

	/**
	 * Add a listener to the list of listeners.
	 *
	 * @param listener The listener.
	 */
	public void addListener(Listener listener) {
		listeners.add(listener);
	}

	/**
	 * Clear the list of options.
	 */
	public void clear() {
		options.clear();
		layoutOptions();
	}

	/**
	 * Check if the list of listeners contains the aargument listener.
	 *
	 * @param listener The listener to check.
	 * @return A boolean.
	 */
	public boolean containsListener(Listener listener) {
		return listeners.contains(listener);
	}

	/**
	 * Return the horizontal option group.
	 *
	 * @param group The group, list of option.
	 * @return The layout group.
	 */
	private Pane getHorizontalOptionGroup(List<Option> group) {
		GridBagPane paneGroup = new GridBagPane();
		for (int i = 0; i < group.size(); i++) {
			Option option = group.get(i);
			Button button = option.getButton();
			if (button == null) {
				button = Option.createStandardButton(option);
			}
			Insets insets = null;
			if (i == 0) {
				insets = new Insets(0, 0, 0, 0);
			} else {
				insets = new Insets(0, 2, 0, 0);
			}
			paneGroup.add(button, new Constraints(Anchor.CENTER, Fill.NONE, i, 0, insets));
		}
		return paneGroup;
	}

	/**
	 * Returns the list of options grouped by group.
	 *
	 * @param options The source list of options, properly sorted.
	 * @return The list of options grouped by group.
	 */
	private List<List<Option>> getOptionGroups(List<Option> options) {
		List<List<Option>> groups = new ArrayList<>();
		List<Option> group = new ArrayList<>();
		for (int i = 0; i < options.size(); i++) {
			Option optionCurr = options.get(i);
			if (i == 0) {
				group.add(optionCurr);
				continue;
			}
			Option optionPrev = options.get(i - 1);
			Option.Group groupCurr = optionCurr.getOptionGroup();
			Option.Group groupPrev = optionPrev.getOptionGroup();
			if (!groupCurr.equals(groupPrev)) {
				groups.add(group);
				group = new ArrayList<>();
			}
			group.add(optionCurr);
		}
		if (!group.isEmpty()) {
			groups.add(group);
		}
		return groups;
	}

	/**
	 * Return an unmodifiable copy of the options.
	 * 
	 * @return The options.
	 */
	public List<Option> getOptions() {
		return Collections.unmodifiableList(options);
	}

	/**
	 * Return the popup menu with the actions.
	 *
	 * @return The popup menu.
	 */
	@Override
	public PopupMenu getPopupMenu(Control control) {
		List<Option> optionsVisible = Option.getOptionsVisibleInPopupMenu(options);
		optionsVisible.forEach(option -> option.setControl(control));
		PopupMenu popupMenu = new PopupMenu();
		Option.addMenuItems(popupMenu, optionsVisible);
		return popupMenu;
	}

	/**
	 * Horizontal layout.
	 */
	private void layoutHorizontal() {

		/* Reset the layout. */
		removeAll();
		HorizontalFlowLayout layout = new HorizontalFlowLayout();
		layout.setHorizontalAlignment(horizontalAlignment);
		layout.setVerticalAlignment(verticalAlignment);
		setLayout(layout);

		/* List of options to show in the buttons pane. */
		List<Option> options = Option.getOptionsVisibleInButtonsPane(this.options);

		/* Count the number of groups to decide whether to group them. */
		int count = Option.countOptionGroups(options);
		if (count == 1) {
			/*
			 * If there is only one group, add the buttons without grouping them.
			 */
			options.forEach(option -> add(option.getButton()));
		} else {
			/*
			 * If there is more than one group, build the list of options per group and add
			 * the groups contained in a
			 * pane.
			 */
			List<List<Option>> groups = getOptionGroups(options);
			groups.forEach(group -> add(getHorizontalOptionGroup(group)));
		}

	}

	/**
	 * Do layout the options.
	 */
	private void layoutOptions() {

		/*
		 * Ensure that buttons and menu items exist and are properly linked to the
		 * option.
		 */
		for (Option option : options) {
			Button button = option.getButton();
			if (button == null) {
				button = Option.createStandardButton(option);
			}
			MenuItem menuItem = option.getMenuItem();
			if (menuItem == null) {
				menuItem = Option.createMenuItem(option);
			}
		}

		/*
		 * Layout orientation.
		 */
		if (orientation.equals(Orientation.HORIZONTAL)) {
			layoutHorizontal();
		} else {

		}
	}

	/**
	 * Remove the argument option.
	 *
	 * @param option The option to remove.
	 */
	public void remove(Option option) {
		options.remove(option);
		listeners.forEach(listener -> listener.removed(option));
		layoutOptions();
	}

	/**
	 * Set mnemonics to buttons and menu items of the options.
	 */
	public void setMnemonics() {
		Option.setMnemonics(options);
	}
}
