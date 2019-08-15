/*
 * Copyright (C) 2018 Miquel Sas
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General PublicLicense as published by the Free Software
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

package com.mlt.ml.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface that must implement all objects involved in a computational network
 * graph, to restore/save their internal data.
 * <p>
 * Objects that implement the <em>Persistent</em> interface must provide a
 * default constructor.
 *
 * @author Miquel Sas
 */
public interface Persistent {
	/**
	 * Restore from an input stream.
	 * 
	 * @param is The input stream.
	 * @throws IOException
	 */
	void restore(InputStream is) throws IOException;

	/**
	 * Save to an output stream.
	 * 
	 * @param os The output stream.
	 * @throws IOException
	 */
	void save(OutputStream os) throws IOException;
}
