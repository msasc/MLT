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

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import com.mlt.desktop.control.Button;
import com.mlt.desktop.control.Control;
import com.mlt.desktop.control.Menu;
import com.mlt.desktop.control.MenuItem;
import com.mlt.desktop.control.PopupMenu;
import com.mlt.desktop.layout.Insets;
import com.mlt.util.Lists;
import com.mlt.util.Numbers;
import com.mlt.util.Properties;
import com.mlt.util.Resources;

/**
 * Packs the attributes of an button or menu item option, like the optional
 * action, the text, the tooltip, etc.
 *
 * @author Miquel Sas
 */
public class Option {

	/**
	 * Option groups are used to group options.
	 */
	public static class Group implements Comparable<Group> {

		/** Predefined default option group. */
		public static final Group DEFAULT = new Group("DEFAULT", Numbers.MIN_INTEGER);
		/** Predefined edit option group (New, Modify, Delete...). */
		public static final Group EDIT = new Group("EDIT", 100);
		/** Predefine configure option group. */
		public static final Group CONFIGURE = new Group("CONFIGURE", 200);
		/** Predefined option group for input options. */
		public static final Group INPUT = new Group("INPUT", 300);
		/** Predefined option group for output options. */
		public static final Group OUTPUT = new Group("OUTPUT", 400);
		/** Predefined option group for detail options. */
		public static final Group DETAIL = new Group("DETAIL", 500);
		/** Predefined option group for lookups. */
		public static final Group LOOKUP = new Group("LOOKUP", 600);
		/** Predefined option group for undetermined operations. */
		public static final Group OPERATION = new Group("OPERATION", 700);
		/** Predefined exit option group. */
		public static final Group EXIT = new Group("EXIT", Numbers.MAX_INTEGER);

		/**
		 * The name or identifier of the group.
		 */
		private String name;
		/**
		 * The index to sort the group within a list of groups.
		 */
		private int sortIndex = -1;

		/**
		 * Constructor assigning the name.
		 * 
		 * @param name      This group name.
		 * @param sortIndex The sort index.
		 */
		public Group(String name, int sortIndex) {
			super();
			this.name = name;
			this.sortIndex = sortIndex;
		}

		/**
		 * Get this group name.
		 * 
		 * @return The name.
		 */
		public String getName() {
			return name;
		}

		/**
		 * Set this group name.
		 * 
		 * @param name The name to set.
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * Check whether the argument object is equal to this option group.
		 *
		 * @param obj The object to compare
		 * @return A boolean.
		 */
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Group)) {
				return false;
			}
			Group group = (Group) obj;
			return getName().equals(group.getName());
		}

		/**
		 * Returns the hash code for this field.
		 *
		 * @return The hash code
		 */
		@Override
		public int hashCode() {
			return getName().hashCode();
		}

		/**
		 * Gets a string representation of the field.
		 * 
		 * @return A string representation of this field.
		 */
		@Override
		public String toString() {
			return getName();
		}

		/**
		 * Returns the sort index.
		 * 
		 * @return The sort index.
		 */
		public int getSortIndex() {
			return sortIndex;
		}

		/**
		 * Sets the sort index.
		 * 
		 * @param sortIndex The sort index to set.
		 */
		public void setSortIndex(int sortIndex) {
			this.sortIndex = sortIndex;
		}

		/**
		 * Returns a negative integer, zero, or a positive integer as this value is less
		 * than, equal to, or greater than
		 * the specified value.
		 * <p>
		 * A field is considered to be equal to another field if the alias, type, length
		 * and decimals are the same.
		 *
		 * @param group The object to compare.
		 * @return The comparison integer.
		 */
		@Override
		public int compareTo(Group group) {
			return Integer.valueOf(getSortIndex()).compareTo(group.getSortIndex());
		}

	}

	/**
	 * A comparator to sort options by group index and sort index within the group.
	 */
	private static class OptionCmp implements Comparator<Option> {

		/**
		 * Constructor.
		 */
		private OptionCmp() {}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int compare(Option o1, Option o2) {
			if (o1 == null && o2 == null) {
				return 0;
			}
			if (o1 != null && o2 == null) {
				return -1;
			}
			if (o1 == null && o2 != null) {
				return 1;
			}

			Group g1 = o1.getOptionGroup();
			Group g2 = o2.getOptionGroup();

			int gs1 = g1.getSortIndex();
			int gs2 = g2.getSortIndex();

			int compare = Integer.compare(gs1, gs2);
			if (compare != 0) {
				return compare;
			}

			int s1 = o1.getSortIndex();
			int s2 = o2.getSortIndex();

			return Integer.compare(s1, s2);
		}
	}

	/*
	 * Pre-defined options and helpers.
	 */

	/** Pre-defined key ACCEPT. */
	public static final String KEY_ACCEPT = "ACCEPT";
	/** Pre-defined key APPLY. */
	public static final String KEY_APPLY = "APPLY";
	/** Pre-defined key CANCEL. */
	public static final String KEY_CANCEL = "CANCEL";
	/** Pre-defined key CLOSE. */
	public static final String KEY_CLOSE = "CLOSE";
	/** Pre-defined key FINISH. */
	public static final String KEY_FINISH = "FINISH";
	/** Pre-defined key IGNORE. */
	public static final String KEY_IGNORE = "IGNORE";
	/** Pre-defined key NEXT. */
	public static final String KEY_NEXT = "NEXT";
	/** Pre-defined key NO. */
	public static final String KEY_NO = "NO";
	/** Pre-defined key OK. */
	public static final String KEY_OK = "OK";
	/** Pre-defined key OPEN. */
	public static final String KEY_OPEN = "OPEN";
	/** Pre-defined key PREVIOUS. */
	public static final String KEY_PREVIOUS = "PREVIOUS";
	/** Pre-defined key RETRY. */
	public static final String KEY_RETRY = "RETRY";
	/** Pre-defined key SELECT. */
	public static final String KEY_SELECT = "SELECT";
	/** Pre-defined key YES. */
	public static final String KEY_YES = "YES";

	/**
	 * Add menu items to the popup menu.
	 * 
	 * @param popupMenu The popup menu.
	 * @param options   The options.
	 */
	public static void addMenuItems(PopupMenu popupMenu, List<Option> actions) {
		List<Option> optionsVisible = getOptionsVisibleInPopupMenu(actions);
		for (int i = 0; i < optionsVisible.size(); i++) {
			MenuItem menuItem = optionsVisible.get(i).getMenuItem();
			if (menuItem == null) {
				menuItem = createMenuItem(optionsVisible.get(i));
			}
			if (i == 0) {
				popupMenu.add(menuItem);
				continue;
			}
			Option.Group groupCurr = optionsVisible.get(i).getOptionGroup();
			Option.Group groupPrev = optionsVisible.get(i - 1).getOptionGroup();
			if (!groupCurr.equals(groupPrev)) {
				popupMenu.addSeparator();
			}
			popupMenu.add(menuItem);
		}
	}

	/**
	 * Returns the number of option groups to decide grouping.
	 * 
	 * @return The number of option groups.
	 */
	public static int countOptionGroups(List<Option> options) {
		List<String> groups = new ArrayList<>();
		for (Option option : options) {
			String groupName = option.getOptionGroup().getName();
			if (!groups.contains(groupName)) {
				groups.add(groupName);
			}
		}
		return groups.size();
	}

	/**
	 * Create the menu.
	 * 
	 * @param option The option.
	 * @return The menu.
	 */
	public static Menu createMenu(Option option) {
		return createMenu(option, null);
	}

	/**
	 * Create the menu.
	 * 
	 * @param text The option text.
	 * @return The menu.
	 */
	public static Menu createMenu(String text) {
		Option option = new Option();
		option.setText(text);
		return createMenu(option, null);
	}

	/**
	 * Create the menu.
	 * 
	 * @param option         The option.
	 * @param actionListener Optional action to assign to the option. If null, the
	 *                       action of the option will be
	 *                       assigned.
	 * @return The menu.
	 */
	public static Menu createMenu(Option option, ActionListener actionListener) {
		Menu menu = new Menu();
		setupMenuItem(menu, option, actionListener);
		return menu;
	}

	/**
	 * Create the menu item.
	 * 
	 * @param option The option.
	 * @return The menu item.
	 */
	public static MenuItem createMenuItem(Option option) {
		return createMenuItem(option, null);
	}

	/**
	 * Create the menu item.
	 * 
	 * @param text The option text.
	 * @return The menu item.
	 */
	public static MenuItem createMenuItem(String text) {
		Option option = new Option();
		option.setText(text);
		return createMenuItem(option, null);
	}

	/**
	 * Create the menu item.
	 * 
	 * @param option         The option.
	 * @param actionListener Optional action to assign to the option. If null, the
	 *                       action of the option will be
	 *                       assigned.
	 * @return The menu item.
	 */
	public static MenuItem createMenuItem(Option option, ActionListener actionListener) {
		MenuItem menuItem = new MenuItem();
		setupMenuItem(menuItem, option, actionListener);
		return menuItem;
	}

	/**
	 * Create a standard button from the option.
	 *
	 * @param option The option.
	 * @return The button.
	 */
	public static Button createStandardButton(Option option) {
		return createStandardButton(option, null);
	}

	/**
	 * Create a standard button from the option.
	 *
	 * @param option         The option.
	 * @param actionListener Optional action to assign to the option. If null, the
	 *                       action of the option will be
	 *                       assigned.
	 * @return The button.
	 */
	public static Button createStandardButton(Option option, ActionListener actionListener) {
		Button button = new Button();
		if (actionListener == null) {
			actionListener = option.getAction();
		}
		button.setAction(actionListener);
		String text = getButtonText(option);
		if (text != null) {
			button.setText(text);
		}
		String toolTipText = option.getToolTip();
		if (toolTipText != null) {
			button.setToolTipText(toolTipText);
		}
		Icon icon = option.getIcon();
		if (icon != null) {
			button.setIcon(icon);
		}
		button.setMargin(new Insets(2, 4, 2, 4));
		button.setOption(option);
		option.setButton(button);
		return button;
	}

	/**
	 * Returns an unique list with all options in the tree that contains the
	 * argument control.
	 * 
	 * @param control THe source control.
	 * @return The list of options.
	 */
	public static List<Option> getAllOptions(Control control) {
		List<Control> controls = Control.getAllControls(control);
		List<Option> options = new ArrayList<>();
		for (int i = 0; i < controls.size(); i++) {
			Option option = controls.get(i).getOption();
			if (option != null && !options.contains(option)) {
				options.add(option);
			}
		}
		return options;
	}

	/**
	 * Returns the text for a button. If the option has a key stroke, an HTML text
	 * that informs of the key is builded.
	 * 
	 * @param option The option.
	 * @return The button text.
	 */
	public static String getButtonText(Option option) {
		KeyStroke keyStroke = option.getAccelerator();
		String text = option.getText();
		if (text == null) {
			throw new IllegalArgumentException();
		}
		boolean displayAccelerator = option.isDisplayAcceleratorInButton();
		StringBuilder b = new StringBuilder();
		if (keyStroke == null || !displayAccelerator) {
			b.append(text);
		} else {
			b.append("<html>");
			b.append(text);
			b.append(" ");
			b.append("<small>");
			Color color = UIManager.getColor("MenuItem.acceleratorForeground");
			if (color == null) {
				color = Color.BLACK;
			}
			b.append("<span style=\"color:rgb(");
			b.append(color.getRed());
			b.append(",");
			b.append(color.getGreen());
			b.append(",");
			b.append(color.getBlue());
			b.append(")\">");
			b.append("(");
			b.append(AWT.toString(keyStroke));
			b.append(")");
			b.append("</span>");
			b.append("</small>");
			b.append("</html>");
		}
		return b.toString();
	}

	/**
	 * Returns the mnemonic integer.
	 * 
	 * @param c The character.
	 * @return The mnemonic.
	 */
	private static int getMnemonic(char c) {
		int mnemonic = 0;
		switch (Character.toUpperCase(c)) {
		case '0':
			mnemonic = KeyEvent.VK_0;
			break;
		case '1':
			mnemonic = KeyEvent.VK_1;
			break;
		case '2':
			mnemonic = KeyEvent.VK_2;
			break;
		case '3':
			mnemonic = KeyEvent.VK_3;
			break;
		case '4':
			mnemonic = KeyEvent.VK_4;
			break;
		case '5':
			mnemonic = KeyEvent.VK_5;
			break;
		case '6':
			mnemonic = KeyEvent.VK_6;
			break;
		case '7':
			mnemonic = KeyEvent.VK_7;
			break;
		case '8':
			mnemonic = KeyEvent.VK_8;
			break;
		case '9':
			mnemonic = KeyEvent.VK_9;
			break;
		case 'A':
			mnemonic = KeyEvent.VK_A;
			break;
		case 'B':
			mnemonic = KeyEvent.VK_B;
			break;
		case 'C':
			mnemonic = KeyEvent.VK_C;
			break;
		case 'D':
			mnemonic = KeyEvent.VK_D;
			break;
		case 'E':
			mnemonic = KeyEvent.VK_E;
			break;
		case 'F':
			mnemonic = KeyEvent.VK_F;
			break;
		case 'G':
			mnemonic = KeyEvent.VK_G;
			break;
		case 'H':
			mnemonic = KeyEvent.VK_H;
			break;
		case 'I':
			mnemonic = KeyEvent.VK_I;
			break;
		case 'J':
			mnemonic = KeyEvent.VK_J;
			break;
		case 'K':
			mnemonic = KeyEvent.VK_K;
			break;
		case 'L':
			mnemonic = KeyEvent.VK_L;
			break;
		case 'M':
			mnemonic = KeyEvent.VK_M;
			break;
		case 'N':
			mnemonic = KeyEvent.VK_N;
			break;
		case 'O':
			mnemonic = KeyEvent.VK_O;
			break;
		case 'P':
			mnemonic = KeyEvent.VK_P;
			break;
		case 'Q':
			mnemonic = KeyEvent.VK_Q;
			break;
		case 'R':
			mnemonic = KeyEvent.VK_R;
			break;
		case 'S':
			mnemonic = KeyEvent.VK_S;
			break;
		case 'T':
			mnemonic = KeyEvent.VK_T;
			break;
		case 'U':
			mnemonic = KeyEvent.VK_U;
			break;
		case 'V':
			mnemonic = KeyEvent.VK_V;
			break;
		case 'W':
			mnemonic = KeyEvent.VK_W;
			break;
		case 'X':
			mnemonic = KeyEvent.VK_X;
			break;
		case 'Y':
			mnemonic = KeyEvent.VK_Y;
			break;
		case 'Z':
			mnemonic = KeyEvent.VK_Z;
			break;
		default:
			break;
		}
		return mnemonic;
	}

	/**
	 * Returns the list of options visible in the button panel.
	 * 
	 * @param options The source list of options.
	 * @return The list of options visible in the button panel.
	 */
	public static List<Option> getOptionsVisibleInButtonsPane(List<Option> options) {
		List<Option> optionsVisible = new ArrayList<>();
		for (Option option : options) {
			if (option.isVisibleInButtonsPane()) {
				optionsVisible.add(option);
			}
		}
		sort(optionsVisible);
		return optionsVisible;
	}

	/**
	 * Returns the list of options visible in the popup menu.
	 * 
	 * @param options The source list of options.
	 * @return The list of options visible in the popup menu.
	 */
	public static List<Option> getOptionsVisibleInPopupMenu(List<Option> options) {
		List<Option> optionsVisible = new ArrayList<>();
		for (Option option : options) {
			if (option.isVisibleInPopupMenu()) {
				optionsVisible.add(option);
			}
		}
		sort(optionsVisible);
		return optionsVisible;
	}

	/**
	 * Check whether the option key is ACCEPT.
	 * 
	 * @param option The option.
	 * @return A boolean.
	 */
	public static boolean isAccept(Option option) {
		return option.equals(KEY_ACCEPT);
	}

	/**
	 * Check whether the option key is APPLY.
	 * 
	 * @param option The option.
	 * @return A boolean.
	 */
	public static boolean isApply(Option option) {
		return option.equals(KEY_APPLY);
	}

	/**
	 * Check whether the option key is CANCEL.
	 * 
	 * @param option The option.
	 * @return A boolean.
	 */
	public static boolean isCancel(Option option) {
		return option.equals(KEY_CANCEL);
	}

	/**
	 * Check whether the option key is CLOSE.
	 * 
	 * @param option The option.
	 * @return A boolean.
	 */
	public static boolean isClose(Option option) {
		return option.equals(KEY_CLOSE);
	}

	/**
	 * Check whether the option key is FINISH.
	 * 
	 * @param option The option.
	 * @return A boolean.
	 */
	public static boolean isFinish(Option option) {
		return option.equals(KEY_FINISH);
	}

	/**
	 * Check whether the option key is IGNORE.
	 * 
	 * @param option The option.
	 * @return A boolean.
	 */
	public static boolean isIgnore(Option option) {
		return option.equals(KEY_IGNORE);
	}

	/**
	 * Check whether the option key is NEXT.
	 * 
	 * @param option The option.
	 * @return A boolean.
	 */
	public static boolean isNext(Option option) {
		return option.equals(KEY_NEXT);
	}

	/**
	 * Check whether the option key is NO.
	 * 
	 * @param option The option.
	 * @return A boolean.
	 */
	public static boolean isNo(Option option) {
		return option.equals(KEY_NO);
	}

	/**
	 * Check whether the option key is OK.
	 * 
	 * @param option The option.
	 * @return A boolean.
	 */
	public static boolean isOk(Option option) {
		return option.equals(KEY_OK);
	}

	/**
	 * Check whether the option key is OPEN.
	 * 
	 * @param option The option.
	 * @return A boolean.
	 */
	public static boolean isOpen(Option option) {
		return option.equals(KEY_OPEN);
	}

	/**
	 * Check whether the option key is PREVIOUS.
	 * 
	 * @param option The option.
	 * @return A boolean.
	 */
	public static boolean isPrevious(Option option) {
		return option.equals(KEY_PREVIOUS);
	}

	/**
	 * Check whether the option key is RETRY.
	 * 
	 * @param option The option.
	 * @return A boolean.
	 */
	public static boolean isRetry(Option option) {
		return option.equals(KEY_RETRY);
	}

	/**
	 * Check whether the option key is SELECT.
	 * 
	 * @param option The option.
	 * @return A boolean.
	 */
	public static boolean isSelect(Option option) {
		return option.equals(KEY_SELECT);
	}

	/**
	 * Check whether the option key is YES.
	 * 
	 * @param option The option.
	 * @return A boolean.
	 */
	public static boolean isYes(Option option) {
		return option.equals(KEY_YES);
	}

	/**
	 * Create an option.
	 * 
	 * @param key           Key.
	 * @param text          Text.
	 * @param toolTip       Tool tip.
	 * @param accelerator   Accelerator key.
	 * @param defaultOption Default option.
	 * @param defaultClose  Default close.
	 * @param closeWindow   Close window.
	 * @return The option.
	 */
	public static Option option(
		String key,
		String text,
		String toolTip,
		KeyStroke accelerator,
		boolean defaultClose,
		boolean closeWindow) {

		Option option = new Option();
		option.setKey(key);
		option.setText(text);
		option.setToolTip(toolTip);
		option.setAccelerator(accelerator);
		option.setDefaultClose(defaultClose);
		option.setCloseWindow(closeWindow);

		return option;
	}

	public static Option option_ACCEPT() {
		return option_ACCEPT(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
	}

	public static Option option_ACCEPT(KeyStroke accelerator) {
		return option(
			KEY_ACCEPT,
			Resources.getText("buttonAccept"),
			Resources.getText("buttonAccept"),
			accelerator,
			false, false);
	}

	public static Option option_APPLY() {
		return option_APPLY(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
	}

	public static Option option_APPLY(KeyStroke accelerator) {
		return option(
			KEY_APPLY,
			Resources.getText("buttonApply"),
			Resources.getText("buttonApply"),
			accelerator, false, false);
	}

	public static Option option_CANCEL() {
		return option_CANCEL(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
	}

	public static Option option_CANCEL(KeyStroke accelerator) {
		return option(
			KEY_CANCEL,
			Resources.getText("buttonCancel"),
			Resources.getText("buttonCancel"),
			accelerator, true, true);
	}

	public static Option option_CLOSE() {
		return option_CLOSE(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
	}

	public static Option option_CLOSE(KeyStroke accelerator) {
		return option(
			KEY_CLOSE,
			Resources.getText("buttonClose"),
			Resources.getText("buttonClose"),
			accelerator, true, true);
	}

	public static Option option_FINISH() {
		return option_FINISH(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
	}

	public static Option option_FINISH(KeyStroke accelerator) {
		return option(
			KEY_FINISH,
			Resources.getText("buttonFinish"),
			Resources.getText("buttonFinish"),
			accelerator, false, true);
	}

	public static Option option_IGNORE() {
		return option_IGNORE(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK));
	}

	public static Option option_IGNORE(KeyStroke accelerator) {
		return option(
			KEY_IGNORE,
			Resources.getText("buttonFinish"),
			Resources.getText("buttonFinish"),
			accelerator, false, true);
	}

	public static Option option_NEXT() {
		return option_NEXT(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK));
	}

	public static Option option_NEXT(KeyStroke accelerator) {
		return option(
			KEY_NEXT,
			Resources.getText("buttonNext"),
			Resources.getText("buttonNext"),
			accelerator, false, true);
	}

	public static Option option_NO() {
		return option_NO();
	}

	public static Option option_NO(KeyStroke accelerator) {
		return option(
			KEY_NO,
			Resources.getText("buttonNo"),
			Resources.getText("buttonNo"),
			accelerator, true, true);
	}

	public static Option option_OK() {
		return option_OK(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
	}

	public static Option option_OK(KeyStroke accelerator) {
		return option(
			KEY_OK,
			Resources.getText("buttonOk"),
			Resources.getText("buttonOk"),
			accelerator, false, true);
	}

	public static Option option_OPEN() {
		return option_OPEN(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
	}

	public static Option option_OPEN(KeyStroke accelerator) {
		return option(
			KEY_OPEN,
			Resources.getText("buttonOpen"),
			Resources.getText("buttonOpen"),
			accelerator, false, true);
	}

	public static Option option_PREVIOUS() {
		return option_PREVIOUS(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.ALT_DOWN_MASK));
	}

	public static Option option_PREVIOUS(KeyStroke accelerator) {
		return option(
			KEY_PREVIOUS,
			Resources.getText("buttonPrevious"),
			Resources.getText("buttonPrevious"),
			accelerator, false, true);
	}

	public static Option option_RETRY() {
		return option_RETRY(
			KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
			KeyEvent.ALT_DOWN_MASK + KeyEvent.CTRL_DOWN_MASK));
	}

	public static Option option_RETRY(KeyStroke accelerator) {
		return option(
			KEY_RETRY,
			Resources.getText("buttonRetry"),
			Resources.getText("buttonRetry"),
			accelerator, false, true);
	}

	public static Option option_SELECT() {
		return option_SELECT(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
	}

	public static Option option_SELECT(KeyStroke accelerator) {
		return option(
			KEY_SELECT,
			Resources.getText("buttonSelect"),
			Resources.getText("buttonSelect"),
			accelerator, false, true);
	}

	public static Option option_YES() {
		return option_YES(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
	}

	public static Option option_YES(KeyStroke accelerator) {
		return option(
			KEY_YES,
			Resources.getText("buttonYes"),
			Resources.getText("buttonYes"),
			accelerator, false, true);
	}

	/*
	 * Helpers to manage lists of options and create buttons and menu items.
	 */

	/**
	 * Sets the mnemonics for a list of option buttons and menu items if present.
	 * Starts with the first character of the
	 * text, and if the mnemonic is used, scans the text for the first character not
	 * used.
	 * 
	 * @param options The list of options to set the mnemonics.
	 */
	public static void setMnemonics(List<? extends Option> options) {

		// Map of characters already set as mnemonics.
		List<Character> mnemonicChars = new ArrayList<>();

		// Scan buttons.
		for (Option option : options) {

			// Button text (original).
			String text = option.getText();
			if (text == null || text.isEmpty()) {
				continue;
			}
			Button button = option.getButton();
			MenuItem menuItem = option.getMenuItem();

			// Search a valid character.
			int index = -1;
			while (++index < text.length()) {
				char c = text.charAt(index);

				/* Skip possible tag. */
				if (c == '<') {
					while (++index < text.length() && text.charAt(index) != '>') {}
					continue;
				}

				/* Already set mnemonic. */
				if (mnemonicChars.contains(c)) {
					continue;
				}

				/* Do set it. */
				int mnemonic = getMnemonic(c);
				if (mnemonic > 0) {
					mnemonicChars.add(c);
					if (button != null) {
						button.setMnemonic(mnemonic);
						setMnemonicToHTML(button.getComponent(), mnemonic);
					}
					if (menuItem != null) {
						menuItem.setMnemonic(mnemonic);
						setMnemonicToHTML(menuItem.getComponent(), mnemonic);
					}
					break;
				}
			}
		}
	}

	/**
	 * Sets the mnemonics for a list of option buttons and menu items if present.
	 * Starts with the first character of the
	 * text, and if the mnemonic is used, scans the text for the first character not
	 * used.
	 * 
	 * @param options The list of options to set the mnemonics.
	 */
	public static void setMnemonics(Option... options) {
		setMnemonics(Lists.asList(options));
	}

	/**
	 * Sets the mnemonics for a list of option buttons and menu items if present.
	 * Starts with the first character of the
	 * text, and if the mnemonic is used, scans the text for the first character not
	 * used.
	 * 
	 * @param options The list of options to set the mnemonics.
	 */
	public static void setMnemonics(Button... buttons) {
		List<Option> options = new ArrayList<>();
		for (Button button : buttons) {
			options.add(button.getOption());
		}
		setMnemonics(options);
	}

	/**
	 * Sets the mnemonics for a list of option buttons and menu items if present.
	 * Starts with the first character of the
	 * text, and if the mnemonic is used, scans the text for the first character not
	 * used.
	 * 
	 * @param options The list of options to set the mnemonics.
	 */
	public static void setMnemonics(MenuItem... menuItems) {
		List<Option> options = new ArrayList<>();
		for (MenuItem menuItem : menuItems) {
			options.add(menuItem.getOption());
		}
		setMnemonics(options);
	}

	/**
	 * Underline the mnemonic when the text is an HTML text.
	 * 
	 * @param button   The abstract button.
	 * @param mnemonic The mnemonic.
	 */
	private static void setMnemonicToHTML(AbstractButton button, int mnemonic) {

		String text = button.getText();
		if (!text.toLowerCase().startsWith("<html>")) {
			return;
		}

		/*
		 * Scan the text skipping <> chars to find the mnemonic.
		 */
		int index = -1;
		while (++index < text.length()) {
			char c = text.charAt(index);

			/* Skip possible tag. */
			if (c == '<') {
				while (++index < text.length() && text.charAt(index) != '>') {}
				continue;
			}

			/* Check mnemonic. */
			if (getMnemonic(c) == mnemonic) {
				String prefix = text.substring(0, index);
				String strMnemonic = text.substring(index, index + 1);
				String suffix = text.substring(index + 1);
				StringBuilder b = new StringBuilder();
				b.append(prefix);
				b.append("<u>");
				b.append(strMnemonic);
				b.append("</u>");
				b.append(suffix);
				button.setText(b.toString());
				break;
			}
		}
	}

	/**
	 * Setup the menu item
	 * 
	 * @param menuItem       The menu item.
	 * @param option         The option.
	 * @param actionListener The optional action.
	 */
	private static void setupMenuItem(
		MenuItem menuItem,
		Option option,
		ActionListener actionListener) {
		if (actionListener == null) {
			actionListener = option.getAction();
		}
		menuItem.setAction(actionListener);
		String text = option.getText();
		if (text != null) {
			menuItem.setText(text);
		}
		String toolTipText = option.getToolTip();
		if (toolTipText != null) {
			menuItem.setToolTipText(toolTipText);
		}
		Icon icon = option.getIcon();
		if (icon != null) {
			menuItem.setIcon(icon);
		}
		KeyStroke accelerator = option.getAccelerator();
		if (accelerator != null) {
			menuItem.setAccelerator(accelerator);
		}
		menuItem.setOption(option);
		option.setMenuItem(menuItem);
	}

	/**
	 * Sort the list of options.
	 * 
	 * @param options The list of options to sort.
	 */
	public static void sort(List<Option> options) {
		Option[] arr = options.toArray(new Option[options.size()]);
		Arrays.sort(arr, new OptionCmp());
		options.clear();
		options.addAll(Arrays.asList(arr));
	}

	/** Internal properties to store attributes. */
	private Properties properties = new Properties();

	/**
	 * Constructor.
	 */
	public Option() {}

	/**
	 * Execute (click) the option.
	 */
	public void doClick() {
		if (getButton() != null) {
			getButton().doClick();
			return;
		}
		if (getMenuItem() != null) {
			getMenuItem().doClick();
			return;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(String str) {
		return equals((Object) str);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Option) {
			Option option = (Option) obj;
			return Objects.equals(getKey(), option.getKey());
		}
		if (obj instanceof String) {
			String option = (String) obj;
			return Objects.equals(getKey(), option);
		}
		return false;
	}

	/**
	 * Return the accelerator key to configure the button or menu item.
	 * 
	 * @return The tooltip text.
	 */
	public KeyStroke getAccelerator() {
		return (KeyStroke) properties.getObject("ACCELERATOR");
	}

	/**
	 * Return the action.
	 * 
	 * @return The action.
	 */
	public ActionListener getAction() {
		return (ActionListener) properties.getObject("ACTION_LISTENER");
	}

	/**
	 * Return the button or null.
	 * 
	 * @return The button.
	 */
	public Button getButton() {
		return (Button) properties.getObject("BUTTON");
	}

	/**
	 * Return the control that ultimately launched the option.
	 * 
	 * @return The control.
	 */
	public Control getControl() {
		return (Control) properties.getObject("CONTROL");
	}

	/**
	 * Return the icon. If not set returns null.
	 * 
	 * @return The icon.
	 */
	public Icon getIcon() {
		return (Icon) properties.getObject("ICON");
	}

	/**
	 * Return the key.
	 * 
	 * @return
	 */
	public String getKey() {
		return properties.getString("KEY");
	}

	/**
	 * Return the menu item or null.
	 * 
	 * @return The menu item.
	 */
	public MenuItem getMenuItem() {
		return (MenuItem) properties.getObject("MENU_ITEM");
	}

	/**
	 * Return the option group. If not set returns the default option group.
	 * 
	 * @return The option group.
	 */
	public Group getOptionGroup() {
		return (Group) properties.getObject("OPTION_GROUP", Group.DEFAULT);
	}

	/**
	 * Return an optional secondary accelerator.
	 * 
	 * @return The key stroke.
	 */
	public KeyStroke getSecondaryAccelerator() {
		return (KeyStroke) properties.getObject("SECONDARY_ACCELERATOR");
	}

	/**
	 * Return the sort index. If not set returns 0.
	 * 
	 * @return The sort index.
	 */
	public int getSortIndex() {
		return properties.getInteger("SORT_INDEX", 0);
	}

	/**
	 * Return the text. If not set returns null.
	 * 
	 * @return The text.
	 */
	public String getText() {
		return properties.getString("TEXT");
	}

	/**
	 * Return the tooltip. If not set returns null.
	 * 
	 * @return The tooltip.
	 */
	public String getToolTip() {
		return properties.getString("TOOLTIP");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(getKey());
	}

	/**
	 * Check if the option should close the window when activated.
	 * 
	 * @return A boolean.
	 */
	public boolean isCloseWindow() {
		return properties.getBoolean("CLOSE_WINDOW");
	}

	/**
	 * Check if the option is the default close option.
	 * 
	 * @return A boolean.
	 */
	public boolean isDefaultClose() {
		return properties.getBoolean("DEFAULT_CLOSE");
	}

	/**
	 * Check if the option should display the accelerator in buttons when present.
	 * Default is true.
	 * 
	 * @return A boolean.
	 */
	public boolean isDisplayAcceleratorInButton() {
		return properties.getBoolean("DISPLAY_ACCELERATOR", true);
	}

	/**
	 * Check if the option should be visible in a button panel.
	 * 
	 * @return A boolean.
	 */
	public boolean isVisibleInButtonsPane() {
		return properties.getBoolean("VISIBLE_IN_BUTTONS_PANE", true);
	}

	/**
	 * Check if the option should be visible in a popup menu.
	 * 
	 * @return A boolean.
	 */
	public boolean isVisibleInPopupMenu() {
		return properties.getBoolean("VISIBLE_IN_POPUP_MENU", true);
	}

	/**
	 * Set the accelerator key.
	 * 
	 * @param keyStroke The accelerator key.
	 */
	public void setAccelerator(KeyStroke keyStroke) {
		properties.setObject("ACCELERATOR", keyStroke);
	}

	/**
	 * Set the action.
	 * 
	 * @param actionListener The action.
	 */
	public void setAction(ActionListener actionListener) {
		properties.setObject("ACTION_LISTENER", actionListener);
	}

	/**
	 * Set the button.
	 * 
	 * @param button The button.
	 */
	public void setButton(Button button) {
		button.setOption(this);
		properties.setObject("BUTTON", button);
	}

	/**
	 * Set that the parent window should be closed when the option is activated.
	 * 
	 * @param b A boolean.
	 */
	public void setCloseWindow(boolean b) {
		properties.setBoolean("CLOSE_WINDOW", b);
	}

	/**
	 * Set the control that ultimately launches the option.
	 * 
	 * @param control The control.
	 */
	public void setControl(Control control) {
		properties.setObject("CONTROL", control);
	}

	/**
	 * Set that the option is the default close option.
	 * 
	 * @param b A boolean.
	 */
	public void setDefaultClose(boolean b) {
		properties.setBoolean("DEFAULT_CLOSE", b);
	}

	/**
	 * Set that the option should display the accelerator in buttons when present.
	 * 
	 * @param b A boolean.
	 */
	public void setDisplayAcceleratorInButton(boolean b) {
		properties.setBoolean("DISPLAY_ACCELERATOR", b);
	}

	/**
	 * Set the icon.
	 * 
	 * @param icon The icon.
	 */
	public void setIcon(Icon icon) {
		properties.setObject("ICON", icon);
	}

	/**
	 * Set the key.
	 * 
	 * @param key The key.
	 */
	public void setKey(String key) {
		properties.setString("KEY", key);
	}

	/**
	 * Set the menu item.
	 * 
	 * @param menuItem The menu item.
	 */
	public void setMenuItem(MenuItem menuItem) {
		menuItem.setOption(this);
		properties.setObject("MENU_ITEM", menuItem);
	}

	/**
	 * Set the option group.
	 * 
	 * @param group The option group.
	 */
	public void setOptionGroup(Group group) {
		properties.setObject("OPTION_GROUP", group);
	}

	/**
	 * Set the optional secondary accelerator.
	 * 
	 * @param accelerator The key stroke.
	 */
	public void setSecondaryAccelerator(KeyStroke accelerator) {
		properties.setObject("SECONDARY_ACCELERATOR", accelerator);
	}

	/**
	 * Set the sort index.
	 * 
	 * @param index The index.
	 */
	public void setSortIndex(int index) {
		properties.setInteger("SORT_INDEX", index);
	}

	/**
	 * Set the text.
	 * 
	 * @param text The text.
	 */
	public void setText(String text) {
		properties.setString("TEXT", text);
	}

	/**
	 * Set the tooltip.
	 * 
	 * @param toolTip The tooltip.
	 */
	public void setToolTip(String toolTip) {
		properties.setString("TOOLTIP", toolTip);
	}

	/**
	 * Set if the option should be visible in a buttons panel.
	 * 
	 * @param b A boolean.
	 */
	public void setVisibleInButtonsPane(boolean b) {
		properties.setBoolean("VISIBLE_IN_BUTTONS_PANE", b);
	}

	/**
	 * Set if the option should be visible in a popup menu.
	 * 
	 * @param b A boolean.
	 */
	public void setVisibleInPopupMenu(boolean b) {
		properties.setBoolean("VISIBLE_IN_POPUP_MENU", b);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return (getText() != null ? getText() : getKey());
	}

}
