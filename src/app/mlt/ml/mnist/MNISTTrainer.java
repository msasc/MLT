/*
 * Copyright (C) 2018 Miquel Sas
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package app.mlt.ml.mnist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.mlt.desktop.TaskFrame;
import com.mlt.ml.data.PatternSource;
import com.mlt.ml.data.mnist.MNIST;
import com.mlt.ml.data.mnist.NumberImage;
import com.mlt.ml.function.Activation;
import com.mlt.ml.function.activation.ActivationSigmoid;
import com.mlt.ml.function.activation.ActivationSoftMax;
import com.mlt.ml.network.Builder;
import com.mlt.ml.network.Network;
import com.mlt.ml.network.Trainer;
import com.mlt.task.Task;
import com.mlt.util.Logs;
import com.mlt.util.Resources;

/**
 * Trainer of networks with different layers using the MNIS database.
 *
 * @author Miquel Sas
 */
public class MNISTTrainer {

	static {
		Resources.addBaseTextResource("res/strings/StringsLibrary.xml");
		Locale.setDefault(Locale.US);
	}

	private static PatternSource srcTrain;
	private static PatternSource srcTest;

	/**
	 * Start and launch the application.
	 * 
	 * @param args Startup arguments (unused).
	 */
	public static void main(String[] args) {

		/* List of images. */
		List<NumberImage> trainImages = null;
		List<NumberImage> testImages = null;
		try {
			trainImages = MNIST.getImagesTrain();
			testImages = MNIST.getImagesTest();
		} catch (IOException exc) {
			Logs.catching(exc);
			System.exit(1);
		}
		srcTrain = MNIST.getSource(new ArrayList<>(trainImages));
		srcTrain.setDescription("MNIST-Train");
		srcTest = MNIST.getSource(new ArrayList<>(testImages));
		srcTest.setDescription("MNIST-Test");

		List<Task> tasks = new ArrayList<>();
		tasks.add(getTrainerBP(144, 10));
		tasks.add(getTrainerBP(256, 144, 10));

		TaskFrame frame = new TaskFrame();
		frame.setTitle("MNIST Trainer");
		frame.addTasks(tasks);
		frame.show();

	}

	private static Trainer getTrainerBP(int... sizes) {

		Trainer trainer = new Trainer();
		trainer.setProgressModulus(100);

		Network network = new Network();

		StringBuilder name = new StringBuilder();
		for (int i = 0; i < sizes.length; i++) {
			if (i > 0) {
				name.append("-");
			}
			name.append("BP");
			name.append(sizes[i]);
			int inputSize = (i == 0 ? MNIST.INPUT_SIZE : sizes[i - 1]);
			int outputSize = sizes[i];
			Activation activation =
				(i < sizes.length - 1 ? new ActivationSigmoid() : new ActivationSoftMax());
			network.addBranch(Builder.branchPerceptron(inputSize, outputSize, activation));
		}
		network.setName("MNIST-IN784-" + name.toString());

		trainer.setNetwork(network);

		trainer.setPatternSourceTraining(srcTrain.clone());
		trainer.setPatternSourceTest(srcTest.clone());

		trainer.setFilePath("res/network/");
		trainer.setFileRoot(network.getName());
		
		trainer.setShuffle(false);
		trainer.setScore(false);
		trainer.setEpochs(10);
		trainer.setGenerateReport(true, "MNIST-Report");

		trainer.setTitle(network.getName());

		return trainer;
	}
}
