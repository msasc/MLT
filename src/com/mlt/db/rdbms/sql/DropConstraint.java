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
package com.mlt.db.rdbms.sql;

import com.mlt.db.Table;

/**
 * A generic DROP CONTRAINT builder.
 *
 * @author Miquel Sas
 */
public class DropConstraint extends Statement {

	/**
	 * The table to alter.
	 */
	private Table table = null;
	/**
	 * The constraint name.
	 */
	private String constraintName = null;

	/**
	 * Default constructor.
	 */
	public DropConstraint() {
		super();
	}

	/**
	 * Return the table.
	 *
	 * @return The table
	 */
	public Table getTable() {
		return table;
	}

	/**
	 * Set the table to which the constraint should be dropped.
	 *
	 * @param table The table.
	 */
	public void setTable(Table table) {
		this.table = table;
	}

	/**
	 * Return the constraint name.
	 *
	 * @return The constraintName
	 */
	public String getConstraintName() {
		return constraintName;
	}

	/**
	 * Set the name of the constraint to be dropped.
	 *
	 * @param constraintName The name of the constraint.
	 */
	public void setConstraintName(String constraintName) {
		this.constraintName = constraintName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toSQL() {

		if (getTable() == null) {
			throw new IllegalStateException("Malformed DROP CONSTRAINT query: table is null");
		}
		if (getConstraintName() == null) {
			throw new IllegalStateException("Malformed DROP CONSTRAINT query: constraint name is null");
		}

		StringBuilder b = new StringBuilder(256);
		b.append("ALTER TABLE ");
		b.append(getTable().getNameSchema());
		b.append(" DROP CONSTRAINT ");
		b.append(getConstraintName());

		return b.toString();
	}
}
