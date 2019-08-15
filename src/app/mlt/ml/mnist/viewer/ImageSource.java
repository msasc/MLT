/*
 * Copyright (C) 2018 Miquel Sas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package app.mlt.ml.mnist.viewer;

/**
 * An image source provider.
 *
 * @author Miquel Sas
 */
public abstract class ImageSource {

	/**
	 * Constructor.
	 */
	public ImageSource() {
		super();
	}

	/**
	 * Return the number of rows of the image.
	 * 
	 * @return The number of rows.
	 */
	public abstract int getRows();

	/**
	 * Return the number of columns of the image.
	 * 
	 * @return The number of columns.
	 */
	public abstract int getColumns();

	/**
	 * Return the image vector conveniently normalized.
	 * 
	 * @return The image vector.
	 */
	public abstract double[] getImage();

	/**
	 * Return the current image information.
	 * 
	 * @return The image information.
	 */
	public abstract String getInformation();
}
