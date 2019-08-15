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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.mlt.ml.data.mnist.MNIST;
import com.mlt.ml.data.mnist.NumberImage;
import com.mlt.util.Resources;
import com.mlt.util.Strings;

/**
 * Convert the MNIST database files to CSV format.
 *
 * @author Miquel Sas
 */
public class MNISTCSV {
	
	static {
		Resources.addBaseTextResource("res/strings/StringsLibrary.xml");
		Locale.setDefault(Locale.US);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			
			File path = new File("res/mnist/");
			
			List<NumberImage> trainImages = MNIST.getImagesTrain();
			File train = new File(path, "mnist_train.csv");
			FileWriter fw_train = new FileWriter(train);
			BufferedWriter bw_train = new BufferedWriter(fw_train);
			printHeader(bw_train);
			for (NumberImage img : trainImages) {
				printImage(bw_train, img);
			}
			bw_train.close();
			fw_train.close();
			
			List<NumberImage> testImages = MNIST.getImagesTest();
			File test = new File(path, "mnist_test.csv");
			FileWriter fw_test = new FileWriter(test);
			BufferedWriter bw_test = new BufferedWriter(fw_test);
			printHeader(bw_test);
			for (NumberImage img : testImages) {
				printImage(bw_test, img);
			}
			bw_test.close();
			fw_test.close();
			
		} catch (Exception exc) {
			exc.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}
	
	private static void printHeader(BufferedWriter w) throws IOException {
		w.write("\"label\"");
		for (int i = 0; i < 784; i++) {
			w.write(", \"b" + Strings.leftPad(Integer.toString(i), 3, "0") + "\"");
		}
	}
	
	private static void printImage(BufferedWriter w, NumberImage img) throws IOException {
		int number = img.getNumber();
		int[][] image = img.getImage();
		int rows = MNIST.IMAGE_ROWS;
		int cols = MNIST.IMAGE_COLUMNS;
		w.newLine();
		w.write(Integer.toString(number));
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				w.write(", ");
				w.write(Integer.toString(image[r][c]));
			}
		}
	}
}
