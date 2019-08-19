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
import app.mlt.plaf.db.fields.FieldInstrumentDesc;
import app.mlt.plaf.db.fields.FieldInstrumentId;
import app.mlt.plaf.db.fields.FieldInstrumentPipScale;
import app.mlt.plaf.db.fields.FieldInstrumentPipValue;
import app.mlt.plaf.db.fields.FieldInstrumentPrimaryCurrency;
import app.mlt.plaf.db.fields.FieldInstrumentSecondaryCurrency;
import app.mlt.plaf.db.fields.FieldInstrumentTickScale;
import app.mlt.plaf.db.fields.FieldInstrumentTickValue;
import app.mlt.plaf.db.fields.FieldInstrumentVolumeScale;
import app.mlt.plaf.db.fields.FieldServerId;

/**
 * Instruments table definition.
 *
 * @author Miquel Sas
 */
public class TableInstruments extends Table {

	/**
	 * Constructor.
	 */
	public TableInstruments() {
		super();

		setName(DB.INSTRUMENTS);
		setSchema(DB.schema_system());

		addField(new FieldServerId(Fields.SERVER_ID));
		addField(new FieldInstrumentId(Fields.INSTRUMENT_ID));
		addField(new FieldInstrumentDesc(Fields.INSTRUMENT_DESC));
		addField(new FieldInstrumentPipValue(Fields.INSTRUMENT_PIP_VALUE));
		addField(new FieldInstrumentPipScale(Fields.INSTRUMENT_PIP_SCALE));
		addField(new FieldInstrumentTickValue(Fields.INSTRUMENT_TICK_VALUE));
		addField(new FieldInstrumentTickScale(Fields.INSTRUMENT_TICK_SCALE));
		addField(new FieldInstrumentVolumeScale(Fields.INSTRUMENT_VOLUME_SCALE));
		addField(new FieldInstrumentPrimaryCurrency(Fields.INSTRUMENT_PRIMARY_CURRENCY));
		addField(new FieldInstrumentSecondaryCurrency(Fields.INSTRUMENT_SECONDARY_CURRENCY));

		getField(Fields.SERVER_ID).setPrimaryKey(true);
		getField(Fields.INSTRUMENT_ID).setPrimaryKey(true);
		
		setPersistor(new DBPersistor(MLT.getDBEngine(), getComplexView(getPrimaryKey())));
	}

}
