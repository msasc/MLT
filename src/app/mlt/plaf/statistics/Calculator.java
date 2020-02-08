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

import com.mlt.db.Record;
import com.mlt.util.BlockQueue;
import com.mlt.util.Numbers;

import app.mlt.plaf_old.DB;

/**
 * Calculator of statistics averages, average deltas, slopes, spreads,
 * variances, candles.
 *
 * @author Miquel Sas
 */
public abstract class Calculator {

	/**
	 * Average calculator.
	 */
	public static class Avg extends Calculator {

		private String name;
		private Average avg;

		public Avg(String name, Average avg) {
			this.name = name;
			this.avg = avg;
		}

		@Override
		public void calculate(BlockQueue<Record> src, List<BlockQueue<Record>> raws) {
			double val = avg.getAverage(src);
			src.getLast().setValue(name, val);
		}
	}

	/**
	 * Average delta calculator.
	 */
	public static class AvgDelta extends Calculator {

		private String name;
		private Average avg;

		public AvgDelta(String name, Average avg) {
			this.name = name;
			this.avg = avg;
		}

		@Override
		public void calculate(BlockQueue<Record> src, List<BlockQueue<Record>> raws) {
			double average = getVariance(src, name, avg.getPeriod());
			raws.get(0).getLast().setValue(name, average);
		}
	}
	
	/**
	 * Average slope calculator.
	 */
	public static class AvgSlope extends Calculator {

		private String nameAvg;
		private String nameSlope;

		public AvgSlope(String nameAvg, String nameSlope) {
			this.nameAvg = nameAvg;
			this.nameSlope = nameSlope;
		}

		@Override
		public void calculate(BlockQueue<Record> src, List<BlockQueue<Record>> raws) {
			Record rcCurr = src.getLast(0);
			Record rcPrev = rcCurr;
			if (src.size() > 1) {
				rcPrev = src.getLast(1);
			}
			double prev = rcPrev.getValue(nameAvg).getDouble();
			double curr = rcCurr.getValue(nameAvg).getDouble();
			double slope = Numbers.delta(curr, prev);
			raws.get(0).getLast().setValue(nameSlope, slope);
		}
	}
	
	/**
	 * Constructor.
	 */
	public Calculator() {
		super();
	}

	/**
	 * Calculates the value of one or several fields and stores the results in last
	 * record of the appropriate buffer.
	 * 
	 * @param src  The source buffer that has max-period records with open,
	 *             high, low, close and average values.
	 * @param raws The list of raw buffers. The first one stores raw values,
	 *             subsequent store delta values.
	 */
	public abstract void calculate(
		BlockQueue<Record> src,
		List<BlockQueue<Record>> raws);
	
	/**
	 * @param buffer  Raw buffer.
	 * @param nameAvg Average name.
	 * @param period  Slow period.
	 * @return Variance between the close price and the average.
	 */
	protected double getVariance(BlockQueue<Record> buffer, String nameAvg, int period) {
		return getVariance(buffer, DB.FIELD_BAR_CLOSE, nameAvg, period);
	}

	/**
	 * @param buffer   Raw buffer.
	 * @param nameFast Fast average name.
	 * @param nameSlow Medium average name.
	 * @param period   Slow period.
	 * @return Variance between the fast and slow averages.
	 */
	protected double getVariance(
		BlockQueue<Record> buffer,
		String nameFast,
		String nameSlow,
		int period) {

		period = Math.min(buffer.size(), period);
		double variance = 0;
		for (int i = 0; i < period; i++) {
			Record rc = buffer.getLast(i);
			double fast = rc.getValue(nameFast).getDouble();
			double slow = rc.getValue(nameSlow).getDouble();
			double var = Numbers.delta(fast - slow, slow);
			variance += var;
		}
		variance /= ((double) period);
		return variance;
	}
}
