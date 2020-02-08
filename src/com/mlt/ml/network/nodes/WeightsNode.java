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
import com.mlt.util.BlockQueue;
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
	 * Gradients data structure.
	 */
	class Gradients {
		GradientSoftener softener = GradientSoftener.WMA;
		RangeFunction inputFunction;
		RangeFunction outputFunction;
		Queue<double[][]> inputQueue;
		Queue<double[][]> outputQueue;

		/**
		 * @param start Start input index.
		 * @param end   End input index.
		 */
		void gradients(int start, int end) {
			for (int in = start; in <= end; in++) {
				for (int out = 0; out < outputSize; out++) {
					t.matrix[in][out] = t.inputValues[in] * t.outputDeltas[out];
				}
			}
		}

		/**
		 * Consurrently (by input index) calculates the SMA of the raw gradients queue.
		 * 
		 * @param start Start input index.
		 * @param end   End input index.
		 */
		void gradientsSMA(int start, int end) {
			if (g.inputQueue.isEmpty()) {
				return;
			}
			Iterator<double[][]> iter = g.inputQueue.iterator();
			while (iter.hasNext()) {
				double[][] gradients = iter.next();
				for (int in = start; in <= end; in++) {
					for (int out = 0; out < outputSize; out++) {
						t.matrix[in][out] += gradients[in][out];
					}
				}
			}
			double size = ((double) g.inputQueue.size());
			for (int in = start; in <= end; in++) {
				for (int out = 0; out < outputSize; out++) {
					t.matrix[in][out] /= size;
				}
			}
		}

		/**
		 * Consurrently (by input index) calculates the WMA of the raw gradients queue.
		 * 
		 * @param start Start input index.
		 * @param end   End input index.
		 */
		void gradientsWMA(int start, int end) {
			if (g.inputQueue.isEmpty()) {
				return;
			}
			Iterator<double[][]> iter = g.inputQueue.iterator();
			double weight = 1;
			double total = 0;
			while (iter.hasNext()) {
				double[][] gradients = iter.next();
				for (int in = start; in <= end; in++) {
					for (int out = 0; out < outputSize; out++) {
						t.matrix[in][out] += (gradients[in][out] * weight);
					}
				}
				total += weight;
				weight += 1;
			}
			for (int in = start; in <= end; in++) {
				for (int out = 0; out < outputSize; out++) {
					t.matrix[in][out] /= total;
				}
			}
		}
	}

	/**
	 * Learning rate (eta - wiki nomenclature) data structure.
	 */

	/**
	 * Enumerates the gradient softeners.
	 */
	public static enum GradientSoftener {
		NONE, SMA, WMA
	}

	/**
	 * Momentum (alpha - wiki nomenclature) data structure.
	 */
	class Momentum {

		double[][] momentums;
		MomentumStrategy strategy = MomentumStrategy.FIXED;
		double increase = 1.2;
		double decrease = 0.8;
		double maximum = 0.99;
		double minimum = 0.01;
		double initialValue = 0.0;

		/**
		 * Calculate the momentum applying the gradients strategy.
		 * 
		 * @param momentum Current momentum.
		 * @param prevGrad Previous gradient.
		 * @param nextGrad Next gradien.
		 * @return The adjusted momentum.
		 */
		double gradients(double momentum, double prevGrad, double nextGrad) {

			/*
			 * Sign of change of deltas.
			 */
			int sign = -1;
			if (Math.abs(prevGrad) <= minimumEqual && Math.abs(nextGrad) <= minimumEqual) {
				sign = 0;
			} else if (prevGrad > 0 && nextGrad > 0) {
				sign = 1;
			} else if (prevGrad < 0 && nextGrad < 0) {
				sign = 1;
			} else {
				sign = -1;
			}

			/*
			 * Sign positive, both deltas are eithe positive or negative, they go in the
			 * same direction, increase the momentum.
			 */
			if (sign == 1) {
				momentum = Math.min(momentum * increase, maximum);
			}
			/*
			 * Sign negative, deltas are one positive and one negative, oposite direction,
			 * decrease the momentum.
			 */
			if (sign == -1) {
				momentum = Math.max(momentum * decrease, minimum);
			}

			return momentum;
		}

		/**
		 * Initialize momentums.
		 */
		void initialize() {
			momentums = new double[inputSize][outputSize];
			Matrix.fill(momentums, initialValue);
		}

		/**
		 * Restore parameters from properties.
		 */
		void restoreProperties() {
			momentums = properties.getDouble2A("momentums");
			increase = properties.getDouble("momentum-increase");
			decrease = properties.getDouble("momentum-decrease");
			maximum = properties.getDouble("momentum-maximum");
			minimum = properties.getDouble("momentum-minimum");
		}

		/**
		 * Save parameters to properties.
		 */
		void saveProperties() {
			properties.setDouble2A("momentums", momentums);
			properties.setDouble("momentum-increase", increase);
			properties.setDouble("momentum-decrease", decrease);
			properties.setDouble("momentum-maximum", maximum);
			properties.setDouble("momentum-minimum", minimum);
		}

		void setStrategy(MomentumStrategy strategy, double... params) {
			this.strategy = strategy;

			if (strategy == MomentumStrategy.FIXED) {
				double value = params[0];
				if (value > this.maximum || value < 0.0) {
					throw new IllegalArgumentException("Invalid initial momentum value: " + value);
				}
				this.initialValue = value;
			}

			if (strategy == MomentumStrategy.GRADIENTS) {
				double increase = params[0];
				double decrease = params[1];
				double maximum = params[2];
				double minimum = params[3];
				double initialValue = params[4];
				if (increase <= 1.0) {
					throw new IllegalArgumentException("Invalid momentum increase: " + increase);
				}
				if (decrease <= 0.0 || decrease >= 1.0) {
					throw new IllegalArgumentException("Invalid momentum decrease: " + decrease);
				}
				if (maximum <= 0.0 || maximum >= 1.0) {
					throw new IllegalArgumentException("Invalid momentum maximum: " + maximum);
				}
				if (minimum <= 0.0 || minimum >= 1.0) {
					throw new IllegalArgumentException("Invalid momentum minimum: " + minimum);
				}
				if (maximum <= minimum) {
					throw new IllegalArgumentException(
						"Invalid momentum max-min: " + maximum + ", " + minimum);
				}
				if (initialValue > maximum || initialValue < minimum) {
					throw new IllegalArgumentException("Invalid initial value: " + initialValue);
				}
				this.increase = increase;
				this.decrease = decrease;
				this.maximum = maximum;
				this.minimum = minimum;
				this.initialValue = initialValue;
			}

			initialize();
		}
	}

	/**
	 * Enumerates momentum strategies.
	 */
	public static enum MomentumStrategy {
		FIXED, GRADIENTS
	}

	/**
	 * Transient cache data structure.
	 */
	class Transient {
		double[] inputDeltas;
		double[] inputValues;
		double[] outputDeltas;
		double[] outputValues;
		double[][] matrix;
	}

	/** Input size. */
	private int inputSize;
	/** Output size. */
	private int outputSize;
	/** Matrix of weights (in-out). */
	private double[][] weights;

	/** Learning rates. */
	private double[][] learningRates;

	/** Backward function. */
	private RangeFunction backwardFunction;
	/** Forward function. */
	private RangeFunction forwardFunction;

	/** Gradients data structure. */
	private Gradients g;
	/** Momentum data structure. */
	private Momentum m;
	/** Transient cached vectors and matrices. */
	private Transient t;

	/**
	 * Minimum absolute value to consider two values to be equal. Absolute values
	 * less equal this minimum are considered to be equal.
	 */
	private double minimumEqual = 1.0e-12;
	/** Default size for all queues. */
	private int queueSize = 5;

	/**
	 * Constructor used to restore.
	 */
	public WeightsNode() {
		super();
		g = new Gradients();
		m = new Momentum();
		t = new Transient();
	}

	/**
	 * Constructor.
	 * 
	 * @param inputSize  Input size.
	 * @param outputSize Output size.
	 */
	public WeightsNode(int inputSize, int outputSize) {
		this();
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
	public void adjustStep() {
		if (m.strategy == MomentumStrategy.GRADIENTS) {
			double[][] prevGradients = g.outputQueue.getLast(1);
			double[][] nextGradients = g.outputQueue.getLast(0);
			for (int in = 0; in < inputSize; in++) {
				for (int out = 0; out < outputSize; out++) {
					double prevGrad = prevGradients[in][out];
					double nextGrad = nextGradients[in][out];
					double momentum = m.momentums[in][out];
					m.momentums[in][out] = m.gradients(momentum, prevGrad, nextGrad);
				}
			}
		}
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
		t.inputValues = inputEdges.get(0).getForwardData();
		t.outputDeltas = outputEdges.get(0).getBackwardData();

		/* Calculate and add gradients to the input queue. */
		t.matrix = new double[inputSize][outputSize];
		g.inputFunction.process();
		g.inputQueue.addLast(t.matrix);

		/* Process (collector) the input queue and add to the output queue. */
		if (g.softener == GradientSoftener.NONE) {
			t.matrix = g.inputQueue.getLast();
		} else {
			t.matrix = new double[inputSize][outputSize];
			g.outputFunction.process();
		}
		g.outputQueue.addLast(t.matrix);

		/* Process the main bacward function. */
		backwardFunction.process();

		/* Push backward resultin input deltas. */
		pushBackward(t.inputDeltas);
	}

	/**
	 * Backward process from input indexes start to end.
	 * 
	 * @param start Start input index.
	 * @param end   End input index.
	 */
	private void backward(int start, int end) {

		double[][] prevGradients = g.outputQueue.getLast(1);
		double[][] nextGradients = g.outputQueue.getLast(0);

		for (int in = start; in <= end; in++) {

			double inputDelta = 0;

			for (int out = 0; out < outputSize; out++) {

				double weight = weights[in][out];
				double outputDelta = t.outputDeltas[out];
				inputDelta += (weight * outputDelta);

				double prevGrad = prevGradients[in][out];
				double nextGrad = nextGradients[in][out];
				double momentum = m.momentums[in][out];
				double gradient = momentum * prevGrad + (1 - momentum) * nextGrad;

				double learningRate = learningRates[in][out];
				double weightDelta = learningRate * gradient;
				weights[in][out] += weightDelta;
			}

			t.inputDeltas[in] = inputDelta;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void forward() {
		t.inputValues = inputEdges.get(0).getForwardData();
		forwardFunction.process();
		pushForward(t.outputValues);
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
				double input = t.inputValues[in];
				double weight = weights[in][out];
				signal += (input * weight);
			}
			t.outputValues[out] = signal;
		}
	}

	/**
	 * Return the description of the momentum and learning rate optimization method.
	 */
	@Override
	public String getExtendedDescription() {
		StringBuilder b = new StringBuilder();
		b.append("GS: ");
		b.append(g.softener);
		b.append("; MM: ");
		b.append(m.strategy);
		b.append(", ");
		b.append(m.initialValue);
		return b.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getId() {
		return "WG";
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
	 * {@inheritDoc}
	 */
	@Override
	public void initialize() {
		validateState();
		initializeLearningRates();
		m.initialize();
		initializeWeights();
		initializeVectorsAndFunctions();
	}

	/**
	 * Initialize learning rates.
	 */
	private void initializeLearningRates() {
		learningRates = new double[inputSize][outputSize];
		Matrix.fill(learningRates, 0.01);
	}

	/**
	 * Initialize weights.
	 */
	private void initializeWeights() {
		weights = new double[inputSize][outputSize];
		Gaussian g = new Gaussian(true);
		for (int in = 0; in < inputSize; in++) {
			for (int out = 0; out < outputSize; out++) {
				weights[in][out] = g.nextGaussian();
			}
		}
		g.end();
	}

	/**
	 * Initialize vectors and functions.
	 */
	private void initializeVectorsAndFunctions() {

		/* Cached vectors not retrieved from edges. */
		t.inputDeltas = new double[inputSize];
		t.outputValues = new double[outputSize];

		/* Forward function. */
		forwardFunction = new RangeFunction(outputSize, (start, end) -> forward(start, end));

		/* Backward function. */
		backwardFunction = new RangeFunction(inputSize, (start, end) -> backward(start, end));

		/* Gradients input function. */
		g.inputFunction =
			new RangeFunction(
				inputSize,
				(start, end) -> g.gradients(start, end));

		/* Gradients output function only if softener is not NONE. */
		if (g.softener == GradientSoftener.SMA) {
			g.outputFunction =
				new RangeFunction(
					inputSize,
					(start, end) -> g.gradientsSMA(start, end));
		}
		if (g.softener == GradientSoftener.WMA) {
			g.outputFunction =
				new RangeFunction(
					inputSize,
					(start, end) -> g.gradientsWMA(start, end));
		}

		/* Clear queues and initialize output queue with the last gradients. */
		g.inputQueue = new BlockQueue<>(queueSize);
		g.outputQueue = new BlockQueue<>(queueSize);
		g.outputQueue.addLast(new double[inputSize][outputSize]);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void restore(InputStream is) throws IOException {
		restoreProperties(is);
		inputSize = properties.getInteger("input-size");
		outputSize = properties.getInteger("output-size");
		weights = properties.getDouble2A("weights");
		g.softener = GradientSoftener.valueOf(properties.getString("gradients-softener"));
		learningRates = properties.getDouble2A("learning-rates");
		minimumEqual = properties.getDouble("minimum-equal");
		m.restoreProperties();

		/* Initialize cached vectors and functions. */
		initializeVectorsAndFunctions();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void save(OutputStream os) throws IOException {
		properties.setInteger("input-size", inputSize);
		properties.setInteger("output-size", outputSize);
		properties.setDouble2A("weights", weights);
		properties.setString("gradients-softener", g.softener.name());
		properties.setDouble2A("learning-rates", learningRates);
		properties.setDouble("minimum-equal", minimumEqual);
		m.saveProperties();
		saveProperties(os);
	}

	/**
	 * @param gradientSoftener The gradients softener.
	 */
	public void setGradientSoftener(GradientSoftener gradientSoftener) {
		if (gradientSoftener == null) {
			gradientSoftener = GradientSoftener.NONE;
		}
		g.softener = gradientSoftener;
		initializeVectorsAndFunctions();
	}

	/**
	 * @param minimumEqual Absolute minimum value to consider two values equal.
	 */
	public void setMinimumEquals(double minimumEqual) {
		if (minimumEqual <= 0.0 || minimumEqual > 1.0e-8) {
			throw new IllegalArgumentException("Invalid minimum equal: " + minimumEqual);
		}
		this.minimumEqual = minimumEqual;
	}

	/**
	 * Set the momentum strategy to GRADIENTS with default values.
	 * 
	 * @param initialValue The initial momentum value.
	 */
	public void setMomentumStrategyGradients(double initialValue) {
		setMomentumStrategyGradients(1.2, 0.8, 0.9, 0.1, initialValue);
	}

	/**
	 * Set the momentum strategy to GRADIENTS.
	 * 
	 * @param increase     Increase factor.
	 * @param decrease     Decrease factor.
	 * @param maximum      Maximum momentum (less than 1.0).
	 * @param minimum      Minimum momentum (greater than 0.0)
	 * @param initialValue Initial value.
	 */
	public void setMomentumStrategyGradients(
		double increase,
		double decrease,
		double maximum,
		double minimum,
		double initialValue) {
		double[] params = new double[] { increase, decrease, maximum, minimum, initialValue };
		m.setStrategy(MomentumStrategy.GRADIENTS, params);
	}

	/**
	 * Set the momentum strategy to FIXED.
	 * 
	 * @param value The value.
	 */
	public void setMomentumStrategyFixed(double value) {
		m.setStrategy(MomentumStrategy.FIXED, value);
	}

	/**
	 * @param queueSize The common size of queues.
	 */
	public void setQueueSize(int queueSize) {
		this.queueSize = queueSize;
		initializeVectorsAndFunctions();
	}

	/**
	 * Validate initil state.
	 */
	private void validateState() {
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
	}
}
