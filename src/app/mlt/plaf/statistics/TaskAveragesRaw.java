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

package app.mlt.plaf.statistics;

import java.util.List;

import com.mlt.db.Criteria;
import com.mlt.db.Persistor;
import com.mlt.db.Record;
import com.mlt.db.RecordIterator;
import com.mlt.db.Table;
import com.mlt.db.Value;
import com.mlt.mkt.data.Instrument;
import com.mlt.mkt.data.Period;
import com.mlt.task.Task;
import com.mlt.util.FixedSizeList;

import app.mlt.plaf.DB;
import app.mlt.plaf.db.Fields;

/**
 * Calculate all the raw values for the states table of the statistics averages.
 *
 * @author Miquel Sas
 */
public class TaskAveragesRaw extends Task {

	/** Underlying statistics averages. */
	private StatisticsAverages stats;

	/**
	 * Constructor.
	 * 
	 * @param stats The statistics averages.
	 */
	public TaskAveragesRaw(StatisticsAverages stats) {
		super();
		this.stats = stats;
		setTitle(stats.getLabel() + " - Calculate raw values");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected long calculateTotalWork() throws Throwable {
		Instrument instrument = stats.getInstrument();
		Period period = stats.getPeriod();
		Persistor persistor = DB.persistor_ticker(instrument, period);
		setTotalWork(persistor.count(null));
		return getTotalWork();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void compute() throws Throwable {

		/* Count. */
		calculateTotalWork();

		/* States and ticker tables. */
		Table states = stats.getTableStates();
		Table ticker = DB.table_ticker(stats.getInstrument(), stats.getPeriod());

		/* Delete data (if process from scratch) */
		states.getPersistor().delete((Criteria) null);

		/* Averages and maximum period. */
		List<Average> averages = stats.getAverages();
		int maxPeriod = averages.get(averages.size() - 1).getPeriod();

		/* Iterate ticker. */
		long totalWork = getTotalWork();
		long workDone = 0;
		FixedSizeList<Double> buffer = new FixedSizeList<>(maxPeriod);
		RecordIterator iter = ticker.getPersistor().iterator(null, ticker.getPrimaryKey());
		Record rcPrev = null;
		while (iter.hasNext()) {

			/* Check cancel requested. */
			if (isCancelRequested()) {
				setCancelled();
				break;
			}

			/* Retrieve record. */
			Record rcTick = iter.next();
			double close = rcTick.getValue(Fields.BAR_CLOSE).getDouble();
			buffer.add(close);

			/* Notify work. */
			workDone++;
			if (workDone % 100 == 0 || workDone == totalWork) {
				StringBuilder b = new StringBuilder();
				b.append(rcTick.toString(Fields.BAR_TIME_FMT));
				b.append(", ");
				b.append(rcTick.toString(Fields.BAR_OPEN));
				b.append(", ");
				b.append(rcTick.toString(Fields.BAR_HIGH));
				b.append(", ");
				b.append(rcTick.toString(Fields.BAR_LOW));
				b.append(", ");
				b.append(rcTick.toString(Fields.BAR_CLOSE));
				update(b.toString(), workDone, totalWork);
			}

			/* Statistics record. */
			Record rcStat = states.getDefaultRecord();
			rcStat.setValue(Fields.BAR_TIME, rcTick.getValue(Fields.BAR_TIME));
			rcStat.setValue(Fields.BAR_OPEN, rcTick.getValue(Fields.BAR_OPEN));
			rcStat.setValue(Fields.BAR_HIGH, rcTick.getValue(Fields.BAR_HIGH));
			rcStat.setValue(Fields.BAR_LOW, rcTick.getValue(Fields.BAR_LOW));
			rcStat.setValue(Fields.BAR_CLOSE, rcTick.getValue(Fields.BAR_CLOSE));

			/* Calculate averages. */
			for (int i = 0; i < averages.size(); i++) {
				Average average = averages.get(i);
				double value = average.getAverage(buffer);
				String name = stats.getNameAverage(i);
				rcStat.setValue(name, new Value(value));
			}

			/* Calculate raw slopes. */
			if (rcPrev != null) {
				for (int i = 0; i < averages.size(); i++) {
					String nameAverage = stats.getNameAverage(i);
					double prev = rcPrev.getValue(nameAverage).getDouble();
					double curr = rcStat.getValue(nameAverage).getDouble();
					double slope = 0;
					if (prev != 0) {
						slope = (curr / prev) - 1;
					}
					String nameSlope = stats.getNameSlope(i, "raw");
					rcStat.setValue(nameSlope, new Value(slope));
				}
			}

			/* Insert the record. */
			states.getPersistor().insert(rcStat);

			/* Register previous record. */
			rcPrev = rcStat;
		}
		iter.close();
	}
}
