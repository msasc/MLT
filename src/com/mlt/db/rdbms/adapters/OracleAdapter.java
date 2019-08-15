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
package com.mlt.db.rdbms.adapters;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import javax.sql.DataSource;

import com.mlt.db.Field;
import com.mlt.db.Types;
import com.mlt.db.rdbms.DBEngineAdapter;
import com.mlt.db.rdbms.DataSourceInfo;
import com.mlt.db.rdbms.adapters.sql.OracleCreateSchema;
import com.mlt.db.rdbms.adapters.sql.OracleDropSchema;
import com.mlt.db.rdbms.sql.CreateSchema;
import com.mlt.db.rdbms.sql.DropSchema;

/**
 * The Oracle database adapter.
 *
 * @author Miquel Sas
 */
public class OracleAdapter extends DBEngineAdapter {

	/**
	 * Default constructor.
	 */
	public OracleAdapter() {
		super();
		setDriverClassName("oracle.jdbc.driver.OracleDriver");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataSource getDataSource(DataSourceInfo info) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCurrentDate() {
		return "SYSDATE";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCurrentTime() {
		return "SYSDATE";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCurrentTimestamp() {
		return "SYSDATE";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFieldDefinition(Field field) {
		StringBuilder b = new StringBuilder();

		b.append(field.getNameCreate());
		b.append(" ");

		Types type = field.getType();
		switch (type) {
		case BOOLEAN:
			b.append("CHAR(1)");
			break;
		case BYTEARRAY:
			if (field.getLength() <= Types.FIXED_LENGTH) {
				b.append("RAW");
				b.append("(");
				b.append(field.getLength());
				b.append(")");
			} else {
				b.append("LONG RAW");
			}
			break;
		case STRING:
			if (field.getLength() <= Types.FIXED_LENGTH) {
				b.append("VARCHAR2");
				b.append("(");
				b.append(Math.min(field.getLength(), Types.FIXED_LENGTH));
				b.append(")");
			} else {
				b.append("LONG");
			}
			break;
		case DECIMAL:
			b.append("NUMBER");
			b.append("(");
			b.append(field.getLength());
			b.append(",");
			b.append(field.getDecimals());
			b.append(")");
			break;
		case DOUBLE:
			b.append("NUMBER");
			break;
		case LONG:
			b.append("NUMBER");
			break;
		case INTEGER:
			b.append("NUMBER");
			break;
		case DATE:
			b.append("DATE");
			break;
		case TIME:
			b.append("DATE");
			break;
		case DATETIME:
			b.append("DATE");
			break;
		}

		return b.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isExplicitRelation() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toStringSQL(Date date) {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat ef = new SimpleDateFormat("G");
		String sdate = df.format(date);
		String sera = ef.format(date);
		String sdatefmt = (sera.equals("BC") ? "-" + sdate : sdate);
		return "TO_DATE('" + sdatefmt + "','SYYYYMMDD')";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toStringSQL(Time time) {
		SimpleDateFormat df = new SimpleDateFormat("HHmmss");
		String stime = df.format(time);
		return "TO_DATE('" + stime + "','HH24MISS')";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toStringSQL(Timestamp timestamp) {
		SimpleDateFormat tf = new SimpleDateFormat("yyyyMMddHHmmss");
		String stime = tf.format(timestamp);
		return "TO_DATE('" + stime + "','YYYYMMDDHH24MISS')";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CreateSchema getStatementCreateSchema(String schema) {
		OracleCreateSchema createSchema = new OracleCreateSchema();
		createSchema.setDBEngineAdapter(this);
		createSchema.setSchema(schema);
		return createSchema;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DropSchema getStatementDropSchema(String schema) {
		OracleDropSchema dropSchema = new OracleDropSchema();
		dropSchema.setDBEngineAdapter(this);
		dropSchema.setSchema(schema);
		return dropSchema;
	}

}
