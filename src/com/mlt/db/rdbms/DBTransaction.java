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

package com.mlt.db.rdbms;

import java.sql.Connection;
import java.sql.SQLException;

import com.mlt.db.Persistor;
import com.mlt.db.PersistorException;
import com.mlt.db.Record;
import com.mlt.db.Transaction;

/**
 *
 * @author Miquel Sas
 */
public class DBTransaction implements Transaction {

	/** Database engine. */
	private DBEngine dbEngine;
	/** Connection. */
	private Connection cn;

	/**
	 * Constructor.
	 * 
	 * @param dbEngine The database engine.
	 */
	public DBTransaction(DBEngine dbEngine) {
		super();
		this.dbEngine = dbEngine;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start() throws PersistorException {
		try {
			cn = dbEngine.getConnection();
			cn.setAutoCommit(false);
		} catch (SQLException exc) {
			throw new PersistorException(exc);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void commit() throws PersistorException {
		if (cn == null) {
			throw new PersistorException("Transaction not started");
		}
		try {
			cn.commit();
			cn.setAutoCommit(true);
		} catch (SQLException exc) {
			throw new PersistorException(exc);
		} finally {
			if (cn != null) {
				try {
					cn.close();
				} catch (SQLException ignore) {}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void rollback() throws PersistorException {
		if (cn == null) {
			throw new PersistorException("Transaction not started");
		}
		try {
			cn.rollback();
			cn.setAutoCommit(true);
		} catch (SQLException exc) {
			throw new PersistorException(exc);
		} finally {
			if (cn != null) {
				try {
					cn.close();
				} catch (SQLException ignore) {}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int delete(Persistor persistor, Record record) throws PersistorException {
		return persistor.delete(record);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int insert(Persistor persistor, Record record) throws PersistorException {
		return persistor.insert(record);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int update(Persistor persistor, Record record) throws PersistorException {
		return persistor.update(record);
	}

}
