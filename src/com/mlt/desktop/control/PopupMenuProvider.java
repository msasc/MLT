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

import java.util.List;

import com.mlt.desktop.Option;
import com.mlt.desktop.Option.Provider;

/**
 * Provider of a popup menu for a control.
 *
 * @author Miquel Sas
 */
public interface PopupMenuProvider {

	/**
	 * Default provider.
	 */
	public static class Default implements PopupMenuProvider {

		/** Provider of options (on the fly). */
		private Option.Provider optionProvider;

		/**
		 * @param optionProvider The provider of options.
		 */
		public Default(Provider optionProvider) {
			this.optionProvider = optionProvider;
		}

		@Override
		public PopupMenu getPopupMenu(Control control) {
			List<Option> options = optionProvider.getOptions();
			Option.sort(options);
			PopupMenu popup = new PopupMenu();
			for (int i = 0; i < options.size(); i++) {
				if (i > 0) {
					Option.Group gcurr = options.get(i).getOptionGroup();
					Option.Group gprev = options.get(i - 1).getOptionGroup();
					if (!gcurr.equals(gprev)) {
						popup.addSeparator();
					}
				}
				popup.add(options.get(i).getMenuItem());
			}
			return popup;
		}

	}

	/**
	 * Returns the popup menu to be launched from the control.
	 * 
	 * @param control The control that requires the menu.
	 * @return The popup menu.
	 */
	PopupMenu getPopupMenu(Control control);
}
