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
import java.io.IOException;
import java.util.Random;

import com.mlt.util.IO;
import com.mlt.util.Logs;

/**
 * Helper used to generate Gaussian values to initialize weights, either by
 * using a Random generator or a stored file of gaussians in order to initialize
 * wieghts always with the same value and thus be able to compare performances
 * on the same basis.
 *
 * @author Miquel Sas
 */
public class Gaussian {

	private String filePath = "res/network/";
	private String fileName = "guassians.dat";
	private boolean generated;
	private Random random;

	private int index = -1;
	private int size = 10000000;
	private BufferedInputStream in;

	/**
	 * @param generated A boolean that indicates whether gaussian values will be
	 *                  generated once, retrieved from a file, or new randomly
	 *                  generated.
	 */
	public Gaussian(boolean generated) {
		this.generated = generated;

		try {
			start();
		} catch (IOException exc) {
			Logs.catching(exc);
		}
	}
	
	public void end() {
		if (in != null) {
			try {
				in.close();
				in = null;
			} catch (IOException exc) {
				Logs.catching(exc);
			}
		}
		index = -1;
	}

	@Override
	protected void finalize() throws Throwable {
		end();
	}

	/**
	 * Generates the gaussian file.
	 */
	private void generate() throws IOException {
		if (!getFile().exists()) {
			Random random = new Random();
			BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(getFile()));
			for (int i = 0; i < size; i++) {
				double w = 0;
				while (true) {
					w = random.nextGaussian();
					if (w != 0) {
						break;
					}
				}
				IO.writeDouble(bo, w);
			}
			bo.close();
		}
	}

	/**
	 * @return The file.
	 */
	private File getFile() {
		return new File(filePath, fileName);
	}

	/**
	 * @return Next gaussian value.
	 */
	public double nextGaussian() {
		if (generated) {
			try {
				return nextGaussianFixed();
			} catch (Exception exc) {
				Logs.catching(exc);
			}
		}
		return nextGaussianRandom();
	}

	/**
	 * @return The next gaussian read from the file.
	 */
	private double nextGaussianFixed() throws Exception {

		if (in == null) {
			in = new BufferedInputStream(new FileInputStream(getFile()));
		}

		index++;
		if (index == size) {
			index = 0;
			in.close();
			in = new BufferedInputStream(new FileInputStream(getFile()));
		}

		return IO.readDouble(in);
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

	private void start() throws IOException {
		if (!generated) {
			return;
		}
		generate();
	}
}
