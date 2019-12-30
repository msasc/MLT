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

import com.mlt.db.ListPersistor;
import com.mlt.db.Persistor;
import com.mlt.db.Record;
import com.mlt.ml.data.Pattern;
import com.mlt.ml.data.PatternSource;

/**
 * Pattenr source for statistics averages.
 *
 * @author Miquel Sas
 */
public class PatternSourceAverages implements PatternSource {

	/** Statistics. */
	private StatisticsAverages stats;
	/** List persistor. */
	private ListPersistor persistor;
	/** Calculated flag. */
	private boolean calculated = true;

	/**
	 * Constructor.
	 * 
	 * @param stats The statistics.
	 */
	public PatternSourceAverages(StatisticsAverages stats, boolean calculated) {
		this.stats = stats;
		Persistor viewPersistor = stats.getView(true, false, true, true, true).getPersistor();
		this.persistor = new ListPersistor(viewPersistor);
		this.calculated = calculated;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		throw new UnsupportedOperationException("Clear not supported");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Pattern get(int index) {
		Record rc = persistor.getRecord(index);
		Pattern pattern = stats.getPattern(rc, calculated);
		return pattern;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size() {
		return persistor.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shuffle() {
		throw new UnsupportedOperationException("Shuffle not supported");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEmpty() {
		return size() == 0;
	}
}
