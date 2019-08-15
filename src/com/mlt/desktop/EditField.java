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

package com.mlt.desktop;

import com.mlt.db.Value;
import com.mlt.desktop.control.Control;

/**
 * Interface that should implement controls to edit fields.
 *
 * @author Miquel Sas
 */
public interface EditField {
	/**
	 * Return the control that implements this edit field.
	 * 
	 * @return The control.
	 */
	Control getControl();

	/**
	 * Return the edit context associated to this edit field.
	 * 
	 * @return
	 */
	EditContext getEditContext();

	/**
	 * Return the value.
	 * 
	 * @return The value.
	 */
	Value getValue();

	/**
	 * Check whether the edit field is enabled.
	 * 
	 * @return A boolean.
	 */
	boolean isEnabled();

	/**
	 * Enable/disable the edit field.
	 * 
	 * @param enabled A boolean.
	 */
	void setEnabled(boolean enabled);

	/**
	 * Set the value.
	 * 
	 * @param value The value.
	 */
	void setValue(Value value);
}
