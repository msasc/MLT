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

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.mlt.db.Calculator;
import com.mlt.db.Field;
import com.mlt.db.Record;
import com.mlt.db.Value;

import app.mlt.plaf.db.Domains;
import app.mlt.plaf.db.Fields;
import app.mlt.plaf.statistics.Average;
import app.mlt.plaf.statistics.StatisticsAverages;

/**
 * Statistics params field.
 *
 * @author Miquel Sas
 */
public class FieldStatisticsParamsDesc extends Field {

	/**
	 * Calculator.
	 */
	class ParamsDesc implements Calculator {

		@Override
		public Value getValue(Record record) {
			String id = record.getValue(Fields.STATISTICS_ID).toString();
			String params = record.getValue(Fields.STATISTICS_PARAMS).toString();
			if (id.equals("AVG") && !params.isEmpty()) {
				try {
					List<Average> avgs = StatisticsAverages.getAverages(params);
					return new Value(StatisticsAverages.getParametersDescription(avgs));
				} catch (ParserConfigurationException | SAXException | IOException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

	}

	/**
	 * Constructor.
	 * 
	 * @param name Field name.
	 */
	public FieldStatisticsParamsDesc(String name) {
		super(Domains.getString(
			name,
			1024,
			"Parameters description",
			"Parameters description"));
		setPersistent(false);
		setCalculator(new ParamsDesc());
	}
}
