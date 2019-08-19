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

import com.mlt.db.Order;
import com.mlt.db.Table;
import com.mlt.db.rdbms.DBPersistor;

import app.mlt.plaf.DB;
import app.mlt.plaf.MLT;
import app.mlt.plaf.db.Fields;
import app.mlt.plaf.db.fields.FieldPeriodId;
import app.mlt.plaf.db.fields.FieldPeriodName;
import app.mlt.plaf.db.fields.FieldPeriodSize;
import app.mlt.plaf.db.fields.FieldPeriodUnitIndex;

/**
 * Servers table definition.
 *
 * @author Miquel Sas
 */
public class TablePeriods extends Table {

	/**
	 * Constructor.
	 */
	public TablePeriods() {
		super();

		setName(DB.PERIODS);
		setSchema(DB.schema_system());

		addField(new FieldPeriodId(Fields.PERIOD_ID));
		addField(new FieldPeriodName(Fields.PERIOD_NAME));
		addField(new FieldPeriodUnitIndex(Fields.PERIOD_UNIT_INDEX));
		addField(new FieldPeriodSize(Fields.PERIOD_SIZE));

		getField(Fields.PERIOD_ID).setPrimaryKey(true);

		Order order = new Order();
		order.add(getField(Fields.PERIOD_UNIT_INDEX));
		order.add(getField(Fields.PERIOD_SIZE));

		setPersistor(new DBPersistor(MLT.getDBEngine(), getComplexView(order)));
	}
}
