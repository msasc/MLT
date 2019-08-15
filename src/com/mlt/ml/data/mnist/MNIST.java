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

package com.mlt.ml.data.mnist;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.mlt.ml.data.ListPatternSource;
import com.mlt.ml.data.Pattern;
import com.mlt.ml.data.PatternSource;
import com.mlt.util.Files;

/**
 * MNIST utilities.
 *
 * @author Miquel Sas
 */
public class MNIST {

	/** Rows. */
	public static final int IMAGE_ROWS = 28;
	/** Columns. */
	public static final int IMAGE_COLUMNS = 28;
	/** Networks input size. */
	public static final int INPUT_SIZE = IMAGE_ROWS * IMAGE_COLUMNS;

	/** Check labels file name. */
	public static final String TEST_LABELS = "res/mnist/t10k-labels.idx1-ubyte";
	/** Check images file name. */
	public static final String TEST_IMAGES = "res/mnist/t10k-images.idx3-ubyte";
	/** Learn labels file name. */
	public static final String TRAIN_LABELS = "res/mnist/train-labels.idx1-ubyte";
	/** Learn images file name. */
	public static final String TRAIN_IMAGES = "res/mnist/train-images.idx3-ubyte";

	/**
	 * Returns the list of number images given the labels and the images file names.
	 * 
	 * @param labelsFileName The labels file name.
	 * @param imagesFileName The images file name.
	 * @return The list of number images instances.
	 * @throws IOException If such an error occurs.
	 */
	public static List<NumberImage> getImages(String labelsFileName, String imagesFileName) throws IOException {
		File fileLabel = Files.getFileFromClassPathEntries(labelsFileName);
		File fileImage = Files.getFileFromClassPathEntries(imagesFileName);
		NumberImageReader reader = new NumberImageReader(fileLabel, fileImage);
		return reader.read();
	}

	/**
	 * Returns the list of number images for testing or calculating performance.
	 * 
	 * @return The list of number images instances.
	 * @throws IOException If such an error occurs.
	 */
	public static List<NumberImage> getImagesTest() throws IOException {
		return getImages(TEST_LABELS, TEST_IMAGES);
	}

	/**
	 * Returns the list of number images for training.
	 * 
	 * @return The list of number images instances.
	 * @throws IOException If such an error occurs.
	 */
	public static List<NumberImage> getImagesTrain() throws IOException {
		return getImages(TRAIN_LABELS, TRAIN_IMAGES);
	}

	/**
	 * Returns the list of patterns given the list of images.
	 * 
	 * @param images        The list of images.
	 * @param bipolarInput  A boolean that indicates if input is bipolar.
	 * @param bipolarOutput A boolean that indicates if output is bipolar.
	 * @return The list of patterns.
	 */
	public static List<Pattern> getPatterns(List<NumberImage> images, boolean bipolarInput, boolean bipolarOutput) {
		List<Pattern> patterns = new ArrayList<>();
		for (NumberImage image : images) {
			patterns.add(new NumberImagePattern(image, bipolarInput, bipolarOutput));
		}
		return patterns;
	}

	/**
	 * Returns the pattern source given the list of images.
	 * 
	 * @param images The list of number images.
	 * @return The patter source.
	 */
	public static PatternSource getSource(List<NumberImage> images) {
		return new ListPatternSource(getPatterns(images, false, false));
	}

	/**
	 * Returns the pattern source given the list of images.
	 * 
	 * @param images        The list of number images.
	 * @param bipolarInput  A boolean that indicates if input is bipolar.
	 * @param bipolarOutput A boolean that indicates if output is bipolar.
	 * @return The patter source.
	 */
	public static PatternSource getSource(List<NumberImage> images, boolean bipolarInput, boolean bipolarOutput) {
		return new ListPatternSource(getPatterns(images, bipolarInput, bipolarOutput));
	}

	/**
	 * Returns the patter source of test images.
	 * 
	 * @return The pattern source.
	 * @throws IOException If such an error occurs.
	 */
	public static PatternSource getSourceTest() throws IOException {
		return getSourceTest(false, false);
	}

	/**
	 * Returns the patter source of test images.
	 * 
	 * @param bipolarInput  A boolean that indicates if input is bipolar.
	 * @param bipolarOutput A boolean that indicates if output is bipolar.
	 * @return The pattern source.
	 * @throws IOException If such an error occurs.
	 */
	public static PatternSource getSourceTest(boolean bipolarInput, boolean bipolarOutput) throws IOException {
		return getSource(getImagesTest(), bipolarInput, bipolarOutput);
	}

	/**
	 * Returns the patter source of train images.
	 * 
	 * @return The pattern source.
	 * @throws IOException If such an error occurs.
	 */
	public static PatternSource getSourceTrain() throws IOException {
		return getSourceTrain(false, false);
	}

	/**
	 * Returns the patter source of train images.
	 * 
	 * @param bipolarInput  A boolean that indicates if input is bipolar.
	 * @param bipolarOutput A boolean that indicates if output is bipolar.
	 * @return The pattern source.
	 * @throws IOException If such an error occurs.
	 */
	public static PatternSource getSourceTrain(boolean bipolarInput, boolean bipolarOutput) throws IOException {
		return getSource(getImagesTrain(), bipolarInput, bipolarOutput);
	}

}
