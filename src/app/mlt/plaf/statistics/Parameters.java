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

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.mlt.desktop.Alert;
import com.mlt.util.xml.parser.ParserHandler;

/**
 * Parse and store statistics parameters.
 *
 * @author Miquel Sas
 */
public class Parameters extends ParserHandler {

	/** List of averages. */
	private List<Average> averages = new ArrayList<>();
	/** List of deltas. */
	private List<Integer> deltas = new ArrayList<>();
	/** Bars ahead. */
	private int barsAhead;
	/** Percentage for calculated labels. */
	private double percentCalc;

	/**
	 * Constructor.
	 */
	public Parameters() {
		super();

		/* Setup valid paths. */
		set("statistics");
		set("statistics/averages");
		set("statistics/averages/average", "period", "integer");
		set("statistics/averages/average", "delta", "double");
		set("statistics/averages/average", "smooths", "integer-array", false);
		set("statistics/deltas");
		set("statistics/deltas/delta", "size", "integer");
		set("statistics/zig-zag", "bars-ahead", "integer");
		set("statistics/label-calc", "percent", "double");
	}

	/**
	 * Called to notify an element start.
	 */
	@Override
	public void elementStart(
		String namespace,
		String elementName,
		String path,
		Attributes attributes) throws SAXException {

		try {
			/* Validate the path. */
			validate(path, attributes);

			/* Validate and retrieve attributes of averages/average path. */
			if (path.equals("statistics/averages/average")) {

				/* Period. */
				int period = getInteger(attributes, "period");
				if (period <= 0) {
					throw new Exception("Invalid period " + period);
				}

				/* Delta. */
				double delta = getDouble(attributes, "delta");
				if (delta <= 0) {
					throw new Exception("Invalid delta " + delta);
				}

				/* Smooths. */
				int[] smooths = getIntegerArray(attributes, "smooths");

				/* Add the average. */
				averages.add(new Average(period, delta, smooths));
			}

			/* Validate and retrieve deltas history parameter. */
			if (path.equals("statistics/deltas/delta")) {
				int size = getInteger(attributes, "size");
				if (size <= 0) {
					throw new Exception("Invalid delta size " + size);
				}
				deltas.add(size);
			}
			/* Validate and retrieve bars ahead parameter. */
			if (path.equals("statistics/zig-zag")) {
				barsAhead = getInteger(attributes, "bars-ahead");
				if (barsAhead <= 0) {
					throw new Exception("Invalid bars-ahead " + barsAhead);
				}
			}
			/* Validate and retrieve percent calc. */
			if (path.equals("statistics/label-calc")) {
				percentCalc = getDouble(attributes, "percent");
				if (percentCalc <= 0 || percentCalc >= 50) {
					throw new Exception("Invalid percentage for label calc " + percentCalc);
				}
			}

		} catch (Exception exc) {
			Alert.error(exc.getMessage());
			throw new SAXException(exc.getMessage(), exc);
		}
	}

	/**
	 * @return The averages.
	 */
	public List<Average> getAverages() {
		return averages;
	}

	/**
	 * @return The number of bars ahead used to calcuate pivots.
	 */
	public int getBarsAhead() {
		return barsAhead;
	}

	/**
	 * @return The list of deltas.
	 */
	public List<Integer> getDeltas() {
		return deltas;
	}

	/**
	 * @return A description of this parameters.
	 */
	public String getDescription() {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < averages.size(); i++) {
			if (i > 0) {
				b.append(", ");
			}
			b.append(averages.get(i).toString());
		}
		return b.toString();
	}

	/**
	 * @return The percentage to generate calculated labels over pivots.
	 */
	public double getPercentCalc() {
		return percentCalc;
	}
}
