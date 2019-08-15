/*
 * Copyright (C) 2015 Miquel Sas
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
package com.mlt.desktop.icon;

import com.mlt.util.Files;
import com.mlt.util.Logs;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Lists this library icon path names and accesses them.
 *
 * @author Miquel Sas
 */
public class Icons {

	public static final String ALERT_ERROR = "res/images/alert/error.png";
	public static final String ALERT_INFORMATION = "res/images/alert/information.png";
	public static final String ALERT_CONFIRMATION = "res/images/alert/confirm.png";
	public static final String ALERT_WARNING = "res/images/alert/warning.png";

	/*
	 * Common application icons 16x16
	 */
	public static final String APP_16x16_ACCEPT = "res/images/app/16x16/accept.png";
	public static final String APP_16x16_BROWSE = "res/images/app/16x16/browse.png";
	public static final String APP_16x16_CANCEL = "res/images/app/16x16/cancel.png";
	public static final String APP_16x16_CHART = "res/images/app/16x16/chart.png";
	public static final String APP_16x16_CHECKED = "res/images/app/16x16/checked.png";
	public static final String APP_16x16_CLEAR = "res/images/app/16x16/clear.png";
	public static final String APP_16x16_CLOSE = "res/images/app/16x16/close.png";
	public static final String APP_16x16_COLUMNS = "res/images/app/16x16/columns.png";
	public static final String APP_16x16_CREATE = "res/images/app/16x16/new.png";
	public static final String APP_16x16_DELETE = "res/images/app/16x16/delete.png";
	public static final String APP_16x16_DOWNLOAD = "res/images/app/16x16/download.png";
	public static final String APP_16x16_EXECUTE = "res/images/app/16x16/execute.png";
	public static final String APP_16x16_LIST = "res/images/app/16x16/list.png";
	public static final String APP_16x16_NEW = "res/images/app/16x16/new.png";
	public static final String APP_16x16_PURGE = "res/images/app/16x16/purge.png";
	public static final String APP_16x16_SELECT_ALL = "res/images/app/16x16/select-all.png";
	public static final String APP_16x16_SORT = "res/images/app/16x16/sort.png";
	public static final String APP_16x16_STOP = "res/images/app/16x16/stop.png";
	public static final String APP_16x16_UNCHECKED = "res/images/app/16x16/unchecked.png";

	/*
	 * Very flat and simple icons 24x24.
	 */

	public static final String FLAT_24x24_CANCEL = "res/images/app/24x24/cancel.png";
	public static final String FLAT_24x24_CLOSE = "res/images/app/24x24/close.png";
	public static final String FLAT_24x24_END = "res/images/app/24x24/end.png";
	public static final String FLAT_24x24_INFO = "res/images/app/24x24/info.png";
	public static final String FLAT_24x24_PAUSE = "res/images/app/24x24/pause.png";
	public static final String FLAT_24x24_EXECUTE = "res/images/app/24x24/resume.png";
	public static final String FLAT_24x24_UP = "res/images/app/24x24/up.png";
	public static final String FLAT_24x24_DOWN = "res/images/app/24x24/down.png";
	public static final String FLAT_24x24_LEFT = "res/images/app/24x24/left.png";
	public static final String FLAT_24x24_RIGHT = "res/images/app/24x24/right.png";

	public static final String FLAT_24x24_SCROLL_BACK = "res/images/app/24x24/scroll-back.png";
	public static final String FLAT_24x24_SCROLL_FRONT = "res/images/app/24x24/scroll-front.png";
	public static final String FLAT_24x24_SCROLL_START = "res/images/app/24x24/scroll-start.png";
	public static final String FLAT_24x24_SCROLL_END = "res/images/app/24x24/scroll-end.png";
	public static final String FLAT_24x24_ZOOM_IN = "res/images/app/24x24/zoom-in.png";
	public static final String FLAT_24x24_ZOOM_OUT = "res/images/app/24x24/zoom-out.png";

	/** The list of jar files that contains images. */
	private static ArrayList<String> iconsFiles = new ArrayList<>();
	/**
	 * The map that will contain images loaded from jar files or the file system.
	 */
	private static HashMap<String, ImageIcon> iconsMap = new HashMap<>();

	/**
	 * Add a file name to the list of file names that contain icon images (jar files).
	 *
	 * @param fileName The file name to add.
	 */
	synchronized public static void addIconImageFile(String fileName) {
		iconsFiles.add(fileName);
	}

	/**
	 * Returns the image icon scanning a list of jar files that contain images and if not found finally scanning the
	 * file system.
	 *
	 * @param imageName The image path name.
	 * @return The ImageIcon or null if the image was not found or an IO exception was thrown.
	 */
	synchronized public static ImageIcon getIcon(String imageName) {
		try {
			ImageIcon imageIcon = iconsMap.get(imageName);
			if (imageIcon != null) {
				return imageIcon;
			}
			for (String fileName : iconsFiles) {
				File file = Files.getFile(fileName);
				byte[] bytes = Files.getJarEntry(file, imageName);
				if (bytes != null) {
					imageIcon = new ImageIcon(bytes);
					iconsMap.put(imageName, imageIcon);
					return imageIcon;
				}
			}
			File file = Files.getFile(imageName);
			byte[] bytes = Files.getFileBytes(file);
			if (bytes != null) {
				imageIcon = new ImageIcon(bytes);
				iconsMap.put(imageName, imageIcon);
				return imageIcon;
			}
			throw new IOException(MessageFormat.format("Image {0} not found", imageName));
		} catch (Exception exc) {
			Logs.catching(exc);
		}
		return null;
	}

	/**
	 * Returns an image of the icon. The icon should not use the component passed to the paint method.
	 *
	 * @param icon The icon.
	 * @return The image.
	 */
	public static Image getImage(Icon icon) {
		return getImage(null, icon);
	}

	/**
	 * Returns an image of the icon. The icon should not use the component passed to the paint method.
	 *
	 * @param component The component that hosts the icon.
	 * @param icon      The icon.
	 * @return The image.
	 */
	public static Image getImage(Component component, Icon icon) {
		BufferedImage img = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = (Graphics2D) img.getGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		icon.paintIcon(component, g2d, 0, 0);
		return img;
	}
}
