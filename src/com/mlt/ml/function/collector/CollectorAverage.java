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

package com.mlt.ml.function.collector;

import java.util.Collection;

import com.mlt.ml.function.Collector;
import com.mlt.util.Vector;

/**
 * Average (SMA, EMA, WMA) collector.
 *
 * @author Miquel Sas
 */
public class CollectorAverage implements Collector<double[]> {

	/** Average type. */
	public static enum Type {
		SMA, EMA, WMA
	}
	
	/** Average type. */
	private Type type;

	/**
	 * Constructor.
	 */
	public CollectorAverage() {
		super();
		this.type = Type.EMA;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double[] collect(Collection<double[]> vectors) {
		switch (type) {
		case EMA:
			return Vector.averageEMA_Bad(vectors);
		case SMA:
			return Vector.averageSMA(vectors);
		case WMA:
			return Vector.averageWMA(vectors);
		}
		return null;
	}

}
