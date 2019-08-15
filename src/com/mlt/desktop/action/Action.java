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

package com.mlt.desktop.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.mlt.util.Properties;

/**
 * Root of this development system actions. Swing actions are avoided to detach
 * them from automatically configuring buttons and menu items.
 *
 * @author Miquel Sas
 */
public abstract class Action implements ActionListener {

	/**
	 * Create a new action event triggered by the source.
	 * 
	 * @param source The source object.
	 * @return The action event.
	 */
	public static ActionEvent event(Object source) {
		return new ActionEvent(source, 0, "", System.currentTimeMillis(), 0);
	}

	/** Properties. */
	private Properties properties;

	/**
	 * Default constructor.
	 */
	public Action() {
		super();
	}

	/**
	 * Return the property.
	 * 
	 * @param key The string key.
	 * @return The property.
	 */
	public Object getProperty(String key) {
		if (properties == null) {
			return null;
		}
		return properties.getObject(key);
	}

	/**
	 * Set the property.
	 * 
	 * @param key      The key.
	 * @param property The property.
	 */
	public void setProperty(String key, Object property) {
		if (properties == null) {
			properties = new Properties();
		}
		properties.setObject(key, property);
	}
}
