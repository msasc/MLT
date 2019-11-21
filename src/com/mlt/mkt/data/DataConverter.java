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

package com.mlt.mkt.data;

import com.mlt.db.Record;

/**
 * Converter from record to data and vice versa, useful to convert timed data
 * stored in a database.
 *
 * @author Miquel Sas
 */
public interface DataConverter {

	/**
	 * Return the data given the record.
	 * 
	 * @param record The record.
	 * @return The data.
	 */
	Data getData(Record record);

	/**
	 * Return the record given the data.
	 * 
	 * @param data The data.
	 * @return The record.
	 */
	Record getRecord(Data data);

	/**
	 * Return the data index given a field alias or name.
	 * 
	 * @param alias The field alias.
	 * @return The data index.
	 */
	int getIndex(String alias);
}
