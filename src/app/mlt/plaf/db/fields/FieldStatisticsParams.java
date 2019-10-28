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
package app.mlt.plaf.db.fields;

import java.awt.Font;

import com.mlt.db.Field;
import com.mlt.db.Types;
import com.mlt.desktop.EditContext;
import com.mlt.desktop.control.TextArea;
import com.mlt.desktop.layout.Dimension;
import com.mlt.desktop.layout.Fill;

import app.mlt.plaf.db.Domains;

/**
 * Statistics params field.
 *
 * @author Miquel Sas
 */
public class FieldStatisticsParams extends Field {

	/**
	 * Constructor.
	 * 
	 * @param name Field name.
	 */
	public FieldStatisticsParams(String name) {
		super(Domains.getString(
			name,
			Types.FIXED_LENGTH * 10,
			"Statistics params",
			"Statistics params"));
		
		TextArea textArea = new TextArea();
		textArea.setPreferredSize(new Dimension(600, 300));
		textArea.setFont(new Font("Courier", Font.PLAIN, 14));
		getProperties().setObject(EditContext.EDIT_FIELD, textArea);
		getProperties().setObject(EditContext.FILL, Fill.BOTH);
	}
}
