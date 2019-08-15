/*
 * Copyright (C) 2017 Miquel Sas
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
package com.mlt.desktop;

import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.Icon;
import javax.swing.KeyStroke;

import com.mlt.desktop.control.Dialog;
import com.mlt.desktop.control.GridBagPane;
import com.mlt.desktop.control.Label;
import com.mlt.desktop.control.ScrollPane;
import com.mlt.desktop.control.Stage;
import com.mlt.desktop.control.TextPane;
import com.mlt.desktop.icon.Icons;
import com.mlt.desktop.layout.Anchor;
import com.mlt.desktop.layout.Constraints;
import com.mlt.desktop.layout.Fill;
import com.mlt.desktop.layout.Insets;
import com.mlt.util.Lists;
import com.mlt.util.Resources;

/**
 * An alert dialog that supports the standard PLAIN, INFORMATION, WARNING, CONFIRMATION and ERROR configurations, with
 * some predefined buttons. The image is laid out in the left and the content is a text pane. If the text is an HTML
 * text, the pane is set a content type "text/html", if not, the default content type is "text/plain".
 *
 * @author Miquel Sas
 */
public class Alert {

	/**
	 * Enumerate alert types.
	 */
	public static enum Type {
		/** No icon with default OK option. */
		PLAIN,
		/**
		 * Information icon with default OK option.
		 */
		INFORMATION,
		/** Warning icon with default OK option. */
		WARNING,
		/** Error icon with default OK option. */
		ERROR,
		/**
		 * Confirmation icon with default OK and CANCEL options.
		 */
		CONFIRMATION
	}

	/**
	 * Generic alert.
	 *
	 * @param owner   The window owner.
	 * @param title   The title.
	 * @param text    The text.
	 * @param icon    The icon.
	 * @param options List of options.
	 * @return The selected option.
	 */
	public static Option alert(Stage owner, String title, String text, Icon icon, Option... options) {
		Alert alert = new Alert(owner);
		alert.setTitle(title);
		alert.setText(text);
		alert.setIcon(icon);
		alert.setOptions(options);
		return alert.show();
	}

	/**
	 * Generic alert.
	 *
	 * @param owner   The window owner.
	 * @param title   The title.
	 * @param text    The text.
	 * @param type    The type.
	 * @param options List of options.
	 * @return The selected option.
	 */
	public static Option alert(Stage owner, String title, String text, Type type, Option... options) {
		Alert alert = new Alert(owner);
		alert.setTitle(title);
		alert.setText(text);
		alert.setType(type);
		alert.setOptions(options);
		return alert.show();
	}

	/**
	 * Confirmation alert.
	 *
	 * @param message The message.
	 * @return The selected option.
	 */
	public static Option confirm(String message) {
		return confirm(Resources.getText("alertTitleConfirm"), message);
	}

	/**
	 * Confirmation alert.
	 *
	 * @param title   The title.
	 * @param message The message.
	 * @return The selected option.
	 */
	public static Option confirm(String title, String message) {
		return confirm(null, title, message);
	}

	/**
	 * Confirmation alert.
	 *
	 * @param owner   The window owner.
	 * @param title   The title.
	 * @param message The message.
	 * @return The selected option.
	 */
	public static Option confirm(Stage owner, String title, String message) {
		return alert(owner, title, message, Type.CONFIRMATION);
	}

	/**
	 * Error alert.
	 *
	 * @param message The message.
	 * @return The selected option.
	 */
	public static Option error(String message) {
		return error(Resources.getText("alertTitleError"), message);
	}

	/**
	 * Error alert.
	 *
	 * @param title   The title.
	 * @param message The message.
	 * @return The selected option.
	 */
	public static Option error(String title, String message) {
		return error(null, title, message);
	}

	/**
	 * Error alert.
	 *
	 * @param owner   The window owner.
	 * @param title   The title.
	 * @param message The message.
	 * @return The selected option.
	 */
	public static Option error(Stage owner, String title, String message) {
		return alert(owner, title, message, Type.ERROR);
	}

	/**
	 * Info alert.
	 *
	 * @param message The message.
	 * @return The selected option.
	 */
	public static Option info(String message) {
		return info(Resources.getText("alertTitleInfo"), message);
	}

	/**
	 * Info alert.
	 *
	 * @param title   The title.
	 * @param message The message.
	 * @return The selected option.
	 */
	public static Option info(String title, String message) {
		return info(null, title, message);
	}

	/**
	 * Info alert.
	 *
	 * @param owner   The window owner.
	 * @param title   The title.
	 * @param message The message.
	 * @return The selected option.
	 */
	public static Option info(Stage owner, String title, String message) {
		return alert(owner, title, message, Type.INFORMATION);
	}

	/**
	 * Plain alert.
	 *
	 * @param message The message.
	 * @return The selected option.
	 */
	public static Option plain(String message) {
		return plain(Resources.getText("alertTitlePlain"), message);
	}

	/**
	 * Plain alert.
	 *
	 * @param title   The title.
	 * @param message The message.
	 * @return The selected option.
	 */
	public static Option plain(String title, String message) {
		return plain(null, title, message);
	}

	/**
	 * Plain alert.
	 *
	 * @param owner   The window owner.
	 * @param title   The title.
	 * @param message The message.
	 * @return The selected option.
	 */
	public static Option plain(Stage owner, String title, String message) {
		return alert(owner, title, message, Type.PLAIN);
	}

	/**
	 * Warning alert.
	 *
	 * @param message The message.
	 * @return The selected option.
	 */
	public static Option warning(String message) {
		return warning(Resources.getText("alertTitleWarning"), message);
	}

	/**
	 * Warning alert.
	 *
	 * @param title   The title.
	 * @param message The message.
	 * @return The selected option.
	 */
	public static Option warning(String title, String message) {
		return warning(null, title, message);
	}

	/**
	 * Warning alert.
	 *
	 * @param owner   The window owner.
	 * @param title   The title.
	 * @param message The message.
	 * @return The selected option.
	 */
	public static Option warning(Stage owner, String title, String message) {
		return alert(owner, title, message, Type.WARNING);
	}

	/** Option window. */
	private OptionWindow wnd;
	/** Text pane. */
	private TextPane textPane;

	/**
	 * Constructor.
	 */
	public Alert() {
		this(null);
	}

	/**
	 * Constructor.
	 *
	 * @param owner The window owner.
	 */
	public Alert(Stage owner) {
		super();

		/*
		 * Set the option window with a modal dialog. Set the options bottom.
		 */
		wnd = new OptionWindow(new Dialog(owner, new GridBagPane()));
		wnd.setOptionsBottom();

		/*
		 * The text will be handled by a scrollable text pane.
		 */
		textPane = new TextPane();
		textPane.setEditable(false);
		textPane.setBackground(new Label().getBackground());
		ScrollPane scrollPane = new ScrollPane(textPane);
		scrollPane.setBorder(null);
		GridBagPane paneText = new GridBagPane();
		Insets insets = new Insets(5, 5, 0, 5);
		paneText.add(scrollPane, new Constraints(Anchor.CENTER, Fill.BOTH, 0, 0, insets));
		wnd.setCenter(paneText);
	}

	/**
	 * Set the icon.
	 *
	 * @param icon The icon.
	 */
	public void setIcon(Icon icon) {
		wnd.setLeft(new Label(icon));
	}

	/**
	 * Set the options.
	 *
	 * @param options The options.
	 */
	public void setOptions(Option... options) {
		setOptions(Lists.asList(options));
	}

	/**
	 * Set the options.
	 *
	 * @param options The options.
	 */
	public void setOptions(List<Option> options) {
		if (options != null && !options.isEmpty()) {
			wnd.getOptionPane().clear();
			if (options.size() == 1) {
				Option option = options.get(0);
				option.setDefaultClose(true);
				/*
				 * Set an optional VK_ESCAPE accelerator if not set the VK_ESCAPE key.
				 */
				KeyStroke accelerator = option.getAccelerator();
				KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
				if (accelerator == null || !accelerator.equals(escape)) {
					option.setSecondaryAccelerator(escape);
				}

			}
			wnd.getOptionPane().add(options);
		}
	}

	/**
	 * Set the title.
	 *
	 * @param title The title.
	 */
	public void setTitle(String title) {
		wnd.setTitle(title);
	}

	/**
	 * Set the alert text.
	 *
	 * @param text The text.
	 */
	public void setText(String text) {
		if (text.toLowerCase().startsWith("<html>")) {
			textPane.setContentType("text/html");
			text = text.substring(6);
			if (text.toLowerCase().endsWith("</html>")) {
				text = text.substring(0, text.length() - 7);
			}
		} else {
			textPane.setContentType("text/plain");
		}
		textPane.setText(text);
	}

	/**
	 * Set an HTML text, without start and end HTML tags.
	 *
	 * @param text The text
	 */
	public void setHTML(String text) {
		textPane.setContentType("text/html");
		if (text.toLowerCase().startsWith("<html>")) {
			text = text.substring(6);
			if (text.toLowerCase().endsWith("</html>")) {
				text = text.substring(0, text.length() - 7);
			}
		}
		textPane.setText(text);
	}

	/**
	 * Set the alert type.
	 *
	 * @param type The type.
	 */
	public void setType(Type type) {
		if (type == null) {
			return;
		}
		switch (type) {
		case PLAIN:
			setOptions(getOptionOk());
			break;
		case INFORMATION:
			setOptions(getOptionOk());
			setIcon(Icons.getIcon(Icons.ALERT_INFORMATION));
			break;
		case ERROR:
			setOptions(getOptionOk());
			setIcon(Icons.getIcon(Icons.ALERT_ERROR));
			break;
		case WARNING:
			setOptions(getOptionOk());
			setIcon(Icons.getIcon(Icons.ALERT_WARNING));
			break;
		case CONFIRMATION:
			setOptions(getOptionOk(), getOptionCancel());
			setIcon(Icons.getIcon(Icons.ALERT_CONFIRMATION));
			break;
		}
	}

	/**
	 * Return the appropriate OK option for an alert.
	 *
	 * @return The option.
	 */
	private Option getOptionOk() {
		Option ok = Option.option_OK();
		ok.setDefaultClose(false);
		ok.setDisplayAcceleratorInButton(false);
		return ok;
	}

	/**
	 * Return the appropriate CANCEL option for an alert.
	 *
	 * @return The option.
	 */
	private Option getOptionCancel() {
		Option cancel = Option.option_CANCEL();
		cancel.setDefaultClose(true);
		cancel.setDisplayAcceleratorInButton(false);
		return cancel;
	}

	/**
	 * Show the window.
	 *
	 * @return The executed option.
	 */
	public Option show() {
		wnd.getOptionPane().setMnemonics();
		wnd.pack();
		wnd.centerOnScreen();
		wnd.show();
		return wnd.getOptionExecuted();
	}

	/**
	 * Show the window.
	 *
	 * @param width  Height factor.
	 * @param height Height factor.
	 * @return The executed option.
	 */
	public Option show(double width, double height) {
		wnd.getOptionPane().setMnemonics();
		wnd.setSize(width, height);
		wnd.centerOnScreen();
		wnd.show();
		return wnd.getOptionExecuted();
	}
}
