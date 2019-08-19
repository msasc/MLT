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
package app.mlt.plaf.db.tables;

import com.mlt.db.Table;
import com.mlt.db.rdbms.DBPersistor;

import app.mlt.plaf.DB;
import app.mlt.plaf.MLT;
import app.mlt.plaf.db.Fields;
import app.mlt.plaf.db.fields.FieldServerId;
import app.mlt.plaf.db.fields.FieldServerName;
import app.mlt.plaf.db.fields.FieldServerTitle;

/**
 * Servers table definition.
 *
 * @author Miquel Sas
 */
public class TableServers extends Table {

	/**
	 * Constructor.
	 */
	public TableServers() {
		super();

		setName(DB.SERVERS);
		setSchema(DB.schema_system());

		addField(new FieldServerId(Fields.SERVER_ID));
		addField(new FieldServerName(Fields.SERVER_NAME));
		addField(new FieldServerTitle(Fields.SERVER_TITLE));

		getField(Fields.SERVER_ID).setPrimaryKey(true);

		setPersistor(new DBPersistor(MLT.getDBEngine(), getComplexView(getPrimaryKey())));
	}

}
