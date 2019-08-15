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

package com.mlt.desktop.graphic;

import java.awt.RenderingHints;
import java.util.Collections;
import java.util.List;

/**
 * A rendering hint pair.
 * 
 * @author Miquel Sas
 */
public class Hint {
	
	/** Empty list of hints. */
	public static final List<Hint> EMPTY_HINTS = Collections.emptyList();

	/** Key. */
	private RenderingHints.Key key;
	/** Value. **/
	private Object value;

	/**
	 * Constructor.
	 * 
	 * @param key   The key.
	 * @param value The value.
	 */
	public Hint(RenderingHints.Key key, Object value) {
		super();
		this.key = key;
		this.value = value;
	}

	/**
	 * Return the key.
	 * 
	 * @return The hint key.
	 */
	public RenderingHints.Key getKey() {
		return key;
	}

	/**
	 * Return the value.
	 * 
	 * @return The value.
	 */
	public Object getValue() {
		return value;
	}

}
