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

package com.mlt.ml.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.mlt.util.IO;
import com.mlt.util.Logs;

/**
 * Helper used to generate Gaussian values to initialize weights, either by
 * using a Random generator or a stored file of gaussians in order to initialize
 * wieghts always with the same value and thus be able to compare performances
 * from the same basis.
 *
 * @author Miquel Sas
 */
public class Weights {

	private String filePath = "res/network/";
	private String fileName = "weights.dat";
	private boolean fixed;
	private Random random;

	private List<Double> gaussians;
	private int index = -1;
	private int size = 1000000;

	/**
	 * @param fixed A boolean that indicates whether gaussian values will be fixed,
	 *              retrieves from a file, or new randomly generated.
	 */
	public Weights(boolean fixed) {
		this.fixed = fixed;
	}

	/**
	 * @return Next gaussian value.
	 */
	public double nextGaussian() {
		if (fixed) {
			return nextGaussianFixed();
		}
		return nextGaussianRandom();
	}

	private List<Double> loadGaussians() throws Exception {
		File file = new File(filePath, fileName);
		if (!file.exists()) {
			Random random = new Random();
			BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(file));
			for (int i = 0; i < size; i++) {
				IO.writeDouble(bo, random.nextGaussian());
			}
			bo.close();
		}
		List<Double> gaussians = new ArrayList<>();
		BufferedInputStream bi = new BufferedInputStream(new FileInputStream(file));
		for (int i = 0; i < size; i++) {
			gaussians.add(IO.readDouble(bi));
		}
		bi.close();
		return gaussians;
	}

	/**
	 * @return The next gaussian read from the file.
	 */
	private double nextGaussianFixed() {

		/* Load the list of gaussians if necessary. */
		if (gaussians == null) {
			try {
				gaussians = loadGaussians();
			} catch (Exception exc) {
				Logs.catching(exc);
				return 0.0;
			}
		}
		
		index++;
		if (index == size) {
			index = 0;
		}

		return gaussians.get(index);
	}

	/**
	 * @return The next gaussian generated with using Random.
	 */
	private double nextGaussianRandom() {
		if (random == null) {
			random = new Random();
		}
		return random.nextGaussian();
	}
}
