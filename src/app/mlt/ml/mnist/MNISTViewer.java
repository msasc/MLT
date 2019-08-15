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

package app.mlt.ml.mnist;

import com.mlt.util.Resources;

import app.mlt.ml.mnist.viewer.MNISTFrame;

/**
 * View MNIST number images, with optional nodes like weights, filters or pools.
 *
 * @author Miquel Sas
 */
public class MNISTViewer {

	/** Load static res. */
	static {
		Resources.addBaseTextResource("res/strings/StringsLibrary.xml");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MNISTFrame frame = new MNISTFrame();
		frame.setSize(0.5, 0.5);
		frame.centerOnScreen();
		frame.setVisible(true);
	}

}
