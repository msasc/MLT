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

package com.mlt.ml.network.nodes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import com.mlt.ml.function.RangeFunction;
import com.mlt.ml.network.Edge;
import com.mlt.ml.network.Gaussian;
import com.mlt.ml.network.Node;
import com.mlt.util.FixedSizeQueue;
import com.mlt.util.Matrix;
import com.mlt.util.Queue;

/**
 * Weights node with adaptative optimizers using stochastic gradient descent
 * back propagation.
 *
 * @author Miquel Sas
 */
public class WeightsNode extends Node {

	/**
	 * Enumerates the gradients softeners.
	 */
	public static enum GradientSoftener {
		NONE, SMA, WMA
	}

	/** Input size. */
	private int inputSize;
	/** Output size. */
	private int outputSize;
	/** Matrix of weights (in-out). */
	private double[][] weights;
	/** Learning rates. */
	private double[][] learningRates;
	/** Momentums. */
	private double[][] momentums;

	/** Backward function. */
	private RangeFunction backwardFunction;
	/** Forward function. */
	private RangeFunction forwardFunction;
	/** Gradients calculator function. */
	private RangeFunction gradientsInputFunction;
	/** Gradients collector function. */
	private RangeFunction gradientsOutputFunction;

	/** Queue of input gradients. */
	private Queue<double[][]> gradientsInputQueue;
	/** Queue of output gradients. */
	private Queue<double[][]> gradientsOutputQueue;
	/** Queue of gradients deltas. */
	private Queue<double[][]> gradientsDeltasQueue;

	/** Default size for all queues. */
	private int queueSize = 5;
	/** Gradients softnener. */
	private GradientSoftener gradientsSoftener = GradientSoftener.WMA;

	/** Cached input deltas, to be accessed concurrently. */
	private double[] inputDeltas;
	/** Cached input values, to be accessed concurrently. */
	private double[] inputValues;
	/** Cached output deltas, to be accessed concurrently. */
	private double[] outputDeltas;
	/** Cached output values, to be accessed concurrently. */
	private double[] outputValues;
	/** Cached matrix accessed concurrently. */
	private double[][] matrix;

	/**
	 * Constructor used to restore.
	 */
	public WeightsNode() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param inputSize  Input size.
	 * @param outputSize Output size.
	 */
	public WeightsNode(int inputSize, int outputSize) {
		super();
		this.inputSize = inputSize;
		this.outputSize = outputSize;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addInputEdge(Edge edge) throws IllegalStateException {
		if (inputEdges.size() > 0) {
			throw new IllegalStateException("More than one input edge");
		}
		if (edge.getSize() != inputSize) {
			throw new IllegalStateException("Invalid input edge size");
		}
		edge.setOutputNode(this);
		inputEdges.add(edge);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addOutputEdge(Edge edge) throws IllegalStateException {
		if (outputEdges.size() > 0) {
			throw new IllegalStateException("More than one output edge");
		}
		if (edge.getSize() != outputSize) {
			throw new IllegalStateException("Invalid output edge size");
		}
		edge.setInputNode(this);
		outputEdges.add(edge);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void backward() {
		if (inputEdges.get(0).isRecurrent()) {
			return;
		}

		/* Retrieve input values and output deltas. */
		inputValues = inputEdges.get(0).getForwardData();
		outputDeltas = outputEdges.get(0).getBackwardData();

		/* Calculate and add gradients to the input queue. */
		matrix = new double[inputSize][outputSize];
		gradientsInputFunction.process();
		gradientsInputQueue.addLast(matrix);

		/* Process (collector) the input queue and add to the output queue. */
		if (gradientsSoftener == GradientSoftener.NONE) {
			matrix = gradientsInputQueue.getLast();
		} else {
			matrix = new double[inputSize][outputSize];
			gradientsOutputFunction.process();
		}
		gradientsOutputQueue.addLast(matrix);

		/* Add the new gradients deltas to be be saved. */
		gradientsDeltasQueue.add(new double[inputSize][outputSize]);

		/* Process the main bacward function. */
		backwardFunction.process();

		/* Push backward resultin input deltas. */
		pushBackward(inputDeltas);
	}

	/**
	 * Backward process from input indexes start to end.
	 * 
	 * @param start Start input index.
	 * @param end   End input index.
	 */
	private void backward(int start, int end) {

		double[][] gradients = gradientsOutputQueue.getLast();
		double[][] prevDeltas = gradientsDeltasQueue.getLast(1);
		double[][] nextDeltas = gradientsDeltasQueue.getLast(0);

		for (int in = start; in <= end; in++) {

			double inputDelta = 0;

			for (int out = 0; out < outputSize; out++) {

				double weight = weights[in][out];
				double outputDelta = outputDeltas[out];
				inputDelta += (weight * outputDelta);

				double learningRate = learningRates[in][out];
				double momentum = momentums[in][out];
				double gradient = gradients[in][out];
				double prevDelta = prevDeltas[in][out];
				double nextDelta = learningRate * gradient;

				double weightDelta = (momentum * prevDelta) + ((1 - momentum) * nextDelta);
				weights[in][out] += weightDelta;
				nextDeltas[in][out] = weightDelta;
			}

			inputDeltas[in] = inputDelta;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void forward() {
		inputValues = inputEdges.get(0).getForwardData();
		forwardFunction.process();
		pushForward(outputValues);
	}

	/**
	 * Forward process from output indexes start to end.
	 * 
	 * @param start Start output index.
	 * @param end   End output index.
	 */
	private void forward(int start, int end) {
		for (int out = start; out <= end; out++) {
			double signal = 0;
			for (int in = 0; in < inputSize; in++) {
				double input = inputValues[in];
				double weight = weights[in][out];
				signal += (input * weight);
			}
			outputValues[out] = signal;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getInputSize() {
		return inputSize;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getOutputSize() {
		return outputSize;
	}

	/**
	 * @param start Start input index.
	 * @param end   End input index.
	 */
	private void gradients(int start, int end) {
		for (int in = start; in <= end; in++) {
			for (int out = 0; out < outputSize; out++) {
				matrix[in][out] = inputValues[in] * outputDeltas[out];
			}
		}
	}

	/**
	 * Consurrently (by input index) calculates the SMA of the raw gradients queue.
	 * 
	 * @param start Start input index.
	 * @param end   End input index.
	 */
	private void gradientsSMA(int start, int end) {
		if (gradientsInputQueue.isEmpty()) {
			return;
		}
		Iterator<double[][]> iter = gradientsInputQueue.iterator();
		while (iter.hasNext()) {
			double[][] gradients = iter.next();
			for (int in = start; in <= end; in++) {
				for (int out = 0; out < outputSize; out++) {
					matrix[in][out] += gradients[in][out];
				}
			}
		}
		double size = ((double) gradientsInputQueue.size());
		for (int in = start; in <= end; in++) {
			for (int out = 0; out < outputSize; out++) {
				matrix[in][out] /= size;
			}
		}
	}

	/**
	 * Consurrently (by input index) calculates the WMA of the raw gradients queue.
	 * 
	 * @param start Start input index.
	 * @param end   End input index.
	 */
	private void gradientsWMA(int start, int end) {
		if (gradientsInputQueue.isEmpty()) {
			return;
		}
		Iterator<double[][]> iter = gradientsInputQueue.iterator();
		double weight = 1;
		double total = 0;
		while (iter.hasNext()) {
			double[][] gradients = iter.next();
			for (int in = start; in <= end; in++) {
				for (int out = 0; out < outputSize; out++) {
					matrix[in][out] += (gradients[in][out] * weight);
				}
			}
			total += weight;
			weight += 1;
		}
		for (int in = start; in <= end; in++) {
			for (int out = 0; out < outputSize; out++) {
				matrix[in][out] /= total;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize() {

		/* Validate. */
		if (inputEdges.size() == 0) {
			throw new IllegalStateException("Input edges empty");
		}
		if (inputEdges.size() > 1) {
			throw new IllegalStateException("More than one input edge");
		}
		if (inputEdges.get(0).getSize() != inputSize) {
			throw new IllegalStateException("Invalid input edge size");
		}
		if (outputEdges.size() == 0) {
			throw new IllegalStateException("Output edges empty");
		}
		if (outputEdges.size() > 1) {
			throw new IllegalStateException("More than one output edge");
		}
		if (outputEdges.get(0).getSize() != outputSize) {
			throw new IllegalStateException("Invalid output edge size");
		}

		/* Initialize weights. */
		weights = new double[inputSize][outputSize];
		Gaussian w = new Gaussian(true);
		for (int in = 0; in < inputSize; in++) {
			for (int out = 0; out < outputSize; out++) {
				weights[in][out] = w.nextGaussian();
			}
		}

		/* Initialize learning rates and momentums. */
		learningRates = new double[inputSize][outputSize];
		Matrix.fill(learningRates, 0.01);
		momentums = new double[inputSize][outputSize];
		Matrix.fill(momentums, 0.0);

		/* Initialize cached vectors and functions. */
		initializeVectorsAndFunctions();
	}

	/**
	 * Initialize vectors and functions.
	 */
	private void initializeVectorsAndFunctions() {

		/* Cached vectors not retrieved from edges. */
		inputDeltas = new double[inputSize];
		outputValues = new double[outputSize];

		/* Forward function. */
		forwardFunction = new RangeFunction(outputSize, (start, end) -> forward(start, end));

		/* Backward function. */
		backwardFunction = new RangeFunction(inputSize, (start, end) -> backward(start, end));

		/* Gradients input function. */
		gradientsInputFunction =
			new RangeFunction(inputSize, (start, end) -> gradients(start, end));

		/* Gradients output function only if softener is not NONE. */
		if (gradientsSoftener == GradientSoftener.SMA) {
			gradientsOutputFunction =
				new RangeFunction(inputSize, (start, end) -> gradientsSMA(start, end));
		}
		if (gradientsSoftener == GradientSoftener.WMA) {
			gradientsOutputFunction =
				new RangeFunction(inputSize, (start, end) -> gradientsWMA(start, end));
		}

		/* Clear queues and initializa deltas queue. */
		gradientsInputQueue = new FixedSizeQueue<>(queueSize);
		gradientsOutputQueue = new FixedSizeQueue<>(queueSize);
		gradientsDeltasQueue = new FixedSizeQueue<>(queueSize);
		gradientsDeltasQueue.addLast(new double[inputSize][outputSize]);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void restore(InputStream is) throws IOException {
		restoreProperties(is);
		gradientsSoftener = GradientSoftener.valueOf(properties.getString("gradients-softener"));
		inputSize = properties.getInteger("input-size");
		outputSize = properties.getInteger("output-size");
		weights = properties.getDouble2A("weights");
		learningRates = properties.getDouble2A("learning-rates");
		momentums = properties.getDouble2A("momentums");

		/* Initialize cached vectors and functions. */
		initializeVectorsAndFunctions();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void save(OutputStream os) throws IOException {
		properties.setString("gradients-softener", gradientsSoftener.name());
		properties.setInteger("input-size", inputSize);
		properties.setInteger("output-size", outputSize);
		properties.setDouble2A("weights", weights);
		properties.setDouble2A("learning-rates", learningRates);
		properties.setDouble2A("momentums", momentums);
		saveProperties(os);
	}

	/**
	 * @param gradientsSoftener The gradients softener.
	 */
	public void setGradientSoftener(GradientSoftener gradientsSoftener) {
		if (gradientsSoftener == null) {
			gradientsSoftener = GradientSoftener.NONE;
		}
		this.gradientsSoftener = gradientsSoftener;
		initializeVectorsAndFunctions();
	}

	/**
	 * @param queueSize The common size of queues.
	 */
	public void setQueueSize(int queueSize) {
		this.queueSize = queueSize;
		initializeVectorsAndFunctions();
	}

}
