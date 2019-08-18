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

import com.mlt.db.ForeignKey;
import com.mlt.db.Order;
import com.mlt.db.Table;
import com.mlt.db.rdbms.DBPersistor;

import app.mlt.plaf.DB;
import app.mlt.plaf.MLT;
import app.mlt.plaf.db.Fields;
import app.mlt.plaf.db.fields.FieldInstrumentId;
import app.mlt.plaf.db.fields.FieldPeriodId;
import app.mlt.plaf.db.fields.FieldServerId;
import app.mlt.plaf.db.fields.FieldTableName;

/**
 * Tickers table definition.
 *
 * @author Miquel Sas
 */
public class TableTickers extends Table {

	/**
	 * Constructor.
	 */
	public TableTickers() {
		super();

		setName(DB.TICKERS);
		setSchema(DB.SYSTEM_SCHEMA);

		addField(new FieldServerId(Fields.SERVER_ID));
		addField(new FieldInstrumentId(Fields.INSTRUMENT_ID));
		addField(new FieldPeriodId(Fields.PERIOD_ID));
		addField(new FieldTableName(Fields.TABLE_NAME));

		getField(Fields.SERVER_ID).setPrimaryKey(true);
		getField(Fields.INSTRUMENT_ID).setPrimaryKey(true);
		getField(Fields.PERIOD_ID).setPrimaryKey(true);

		Table tablePeriods = new TablePeriods();
		ForeignKey fkPeriods = new ForeignKey(false);
		fkPeriods.setLocalTable(this);
		fkPeriods.setForeignTable(tablePeriods);
		fkPeriods.add(getField(Fields.PERIOD_ID), tablePeriods.getField(Fields.PERIOD_ID));
		addForeignKey(fkPeriods);

		Order order = new Order();
		order.add(getField(Fields.SERVER_ID));
		order.add(getField(Fields.INSTRUMENT_ID));
		order.add(tablePeriods.getField(Fields.PERIOD_UNIT_INDEX));
		order.add(tablePeriods.getField(Fields.PERIOD_SIZE));

		setPersistor(new DBPersistor(MLT.getDBEngine(), getComplexView(order)));
	}

}
