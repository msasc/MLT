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

import com.mlt.db.Record;
import com.mlt.db.Table;
import com.mlt.db.rdbms.DBPersistor;
import com.mlt.mkt.data.Data;
import com.mlt.mkt.data.Instrument;
import com.mlt.mkt.data.Period;

import app.mlt.plaf.DB;
import app.mlt.plaf.MLT;
import app.mlt.plaf.db.Fields;
import app.mlt.plaf.db.fields.FieldDataInst;
import app.mlt.plaf.db.fields.FieldTime;
import app.mlt.plaf.db.fields.FieldTimeFmt;
import app.mlt.plaf.db.fields.FieldVolume;

/**
 * Table of market data, either prices.
 *
 * @author Miquel Sas
 */
public class TableTicker extends Table {

	/**
	 * Return the data element given the ticker record.
	 * 
	 * @param rc The ticker record.
	 * @return The data element.
	 */
	public static Data getData(Record rc) {
		long time = rc.getValue(Fields.BAR_TIME).getLong();
		double open = rc.getValue(Fields.BAR_OPEN).getDouble();
		double high = rc.getValue(Fields.BAR_HIGH).getDouble();
		double low = rc.getValue(Fields.BAR_LOW).getDouble();
		double close = rc.getValue(Fields.BAR_CLOSE).getDouble();
		double volume = rc.getValue(Fields.BAR_CLOSE).getDouble();
		return new Data(time, open, high, low, close, volume);
	}

	/**
	 * Constructor.
	 *
	 * @param instrument Instrument.
	 * @param period     Period.
	 */
	public TableTicker(Instrument instrument, Period period) {
		super();

		setName(DB.name_ticker(instrument, period));
		setSchema(DB.schema_server());

		addField(new FieldTime(Fields.BAR_TIME));
		addField(new FieldDataInst(instrument, Fields.BAR_OPEN, "Open", "Open"));
		addField(new FieldDataInst(instrument, Fields.BAR_HIGH, "High", "High"));
		addField(new FieldDataInst(instrument, Fields.BAR_LOW, "Low", "Low"));
		addField(new FieldDataInst(instrument, Fields.BAR_CLOSE, "Close", "Close"));
		addField(new FieldVolume(instrument, Fields.BAR_VOLUME));
		addField(new FieldTimeFmt(Fields.BAR_TIME_FMT, period));

		getField(Fields.BAR_TIME).setPrimaryKey(true);
		setPersistor(new DBPersistor(MLT.getDBEngine(), getComplexView(getPrimaryKey())));
	}
}
