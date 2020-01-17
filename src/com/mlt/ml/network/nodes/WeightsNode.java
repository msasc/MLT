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
	 * Enumerates the gradient softeners.
	 */
	public static enum GradientSoftener {
		NONE, SMA, WMA
	}

	/**
	 * Gradients data structure.
	 */
	class Gradients {
		GradientSoftener softener = GradientSoftener.WMA;
		RangeFunction inputFunction;
		RangeFunction outputFunction;
		Queue<double[][]> inputQueue;
		Queue<double[][]> outputQueue;
		Queue<double[][]> deltasQueue;

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
	 * Momentum data structure.
	 */
	class Momentum {
		double[][] momentums;
		MomentumStrategy strategy = MomentumStrategy.FIXED;
		double increase = 1.2;
		double decrease = 0.8;
		double maximum = 0.9;
		double minimum = 0.1;
		double initialValue = 0.0;
		/**
		 * Calculate the momentum applying the deltas strategy.
		 * 
		 * @param momentum  Current momentum.
		 * @param prevDelta Previous delta.
		 * @param nextDelta Next delta.
		 * @return The adjusted momentum.
		 */
		double deltas(double momentum, double prevDelta, double nextDelta) {
		
			/*
			 * Sign of change of deltas.
			 */
			int sign = -1;
			if (Math.abs(prevDelta) <= minimumEqual && Math.abs(nextDelta) <= minimumEqual) {
				sign = 0;
			} else if (prevDelta > 0 && nextDelta > 0) {
				sign = 1;
			} else if (prevDelta < 0 && nextDelta < 0) {
				sign = 1;
			} else {
				sign = -1;
			}
		
			/*
			 * Sign positive, both deltas are eithe positive or negative, they go in the
			 * same direction, increase the momentum.
			 */
			if (sign == 1) {
				momentum = Math.min(momentum * m.increase, m.maximum);
			}
			/*
			 * Sign negative, deltas are one positive and one negative, oposite direction,
			 * decrease the momentum.
			 */
			if (sign == -1) {
				momentum = Math.max(momentum * m.decrease, m.minimum);
			}
		
			return momentum;
		}
	}

	/**
	 * Enumerates momentum strategies.
	 */
	public static enum MomentumStrategy {
		FIXED, DELTAS
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

		/* Add the new gradients deltas to be be saved. */
		g.deltasQueue.add(new double[inputSize][outputSize]);

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

		double[][] gradients = g.outputQueue.getLast();
		double[][] prevDeltas = g.deltasQueue.getLast(1);
		double[][] nextDeltas = g.deltasQueue.getLast(0);

		for (int in = start; in <= end; in++) {

			double inputDelta = 0;

			for (int out = 0; out < outputSize; out++) {

				double weight = weights[in][out];
				double outputDelta = t.outputDeltas[out];
				inputDelta += (weight * outputDelta);

				double learningRate = learningRates[in][out];
				double gradient = gradients[in][out];
				double nextDelta = learningRate * gradient;

				double prevDelta = prevDeltas[in][out];
				double momentum = m.momentums[in][out];
				double weightDelta = (momentum * prevDelta) + ((1 - momentum) * nextDelta);

				if (m.strategy == MomentumStrategy.DELTAS) {
					m.momentums[in][out] = m.deltas(momentum, prevDelta, nextDelta);
				}

				weights[in][out] += weightDelta;
				nextDeltas[in][out] = weightDelta;
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
		initializeMomentums();
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
	 * Initialize momentums.
	 */
	private void initializeMomentums() {
		m.momentums = new double[inputSize][outputSize];
		Matrix.fill(m.momentums, m.initialValue);
	}
	
	/**
	 * Initialize weights.
	 */
	private void initializeWeights() {
		weights = new double[inputSize][outputSize];
		Gaussian w = new Gaussian(true);
		for (int in = 0; in < inputSize; in++) {
			for (int out = 0; out < outputSize; out++) {
				weights[in][out] = w.nextGaussian();
			}
		}
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
		g.inputFunction = new RangeFunction(
			inputSize, 
			(start, end) -> g.gradients(start, end));

		/* Gradients output function only if softener is not NONE. */
		if (g.softener == GradientSoftener.SMA) {
			g.outputFunction = new RangeFunction(
				inputSize, 
				(start, end) -> g.gradientsSMA(start, end));
		}
		if (g.softener == GradientSoftener.WMA) {
			g.outputFunction = new RangeFunction(
				inputSize, 
				(start, end) -> g.gradientsWMA(start, end));
		}

		/* Clear queues and initialize deltas queue. */
		g.inputQueue = new FixedSizeQueue<>(queueSize);
		g.outputQueue = new FixedSizeQueue<>(queueSize);
		g.deltasQueue = new FixedSizeQueue<>(queueSize);
		g.deltasQueue.addLast(new double[inputSize][outputSize]);
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

		m.momentums = properties.getDouble2A("momentums");
		m.strategy = MomentumStrategy.valueOf(properties.getString("momentum-strategy"));
		m.increase = properties.getDouble("momentum-increase");
		m.decrease = properties.getDouble("momentum-decrease");
		m.maximum = properties.getDouble("momentum-maximum");
		m.minimum = properties.getDouble("momentum-minimum");

		minimumEqual = properties.getDouble("minimum-equal");

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

		properties.setDouble2A("momentums", m.momentums);
		properties.setString("momentum-strategy", m.strategy.name());
		properties.setDouble("momentum-increase", m.increase);
		properties.setDouble("momentum-decrease", m.decrease);
		properties.setDouble("momentum-maximum", m.maximum);
		properties.setDouble("momentum-minimum", m.minimum);

		properties.setDouble("minimum-equal", minimumEqual);

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
	 * Set the momentum strategy to DELTAS with default values.
	 * 
	 * @param initialValue The initial momentum value.
	 */
	public void setMomentumStrategyDeltas(double initialValue) {
		setMomentumStrategyDeltas(1.2, 0.8, 0.9, 0.1, initialValue);
	}

	/**
	 * Set the momentum strategy to DELTAS.
	 * 
	 * @param momentumIncrease Increase factor.
	 * @param momentumDecrease Decrease factor.
	 * @param momentumMaximum  Maximum momentum (less than 1.0).
	 * @param momentumMinimum  Minimum momentum (greater than 0.0)
	 * @param initialValue     Initial value.
	 */
	public void setMomentumStrategyDeltas(
		double momentumIncrease,
		double momentumDecrease,
		double momentumMaximum,
		double momentumMinimum,
		double initialValue) {

		if (momentumIncrease <= 1.0) {
			throw new IllegalArgumentException("Invalid momentum increase: " + momentumIncrease);
		}
		if (momentumDecrease <= 0.0 || momentumDecrease >= 1.0) {
			throw new IllegalArgumentException("Invalid momentum decrease: " + momentumDecrease);
		}
		if (momentumMaximum <= 0.0 || momentumMaximum >= 1.0) {
			throw new IllegalArgumentException("Invalid momentum maximum: " + momentumMaximum);
		}
		if (momentumMinimum <= 0.0 || momentumMinimum >= 1.0) {
			throw new IllegalArgumentException("Invalid momentum minimum: " + momentumMinimum);
		}
		if (momentumMaximum <= momentumMinimum) {
			throw new IllegalArgumentException(
				"Invalid momentum max-min: " + momentumMaximum + ", " + momentumMinimum);
		}
		if (initialValue > momentumMaximum || initialValue < momentumMinimum) {
			throw new IllegalArgumentException("Invalid initial value: " + initialValue);
		}
		m.strategy = MomentumStrategy.DELTAS;
		m.increase = momentumIncrease;
		m.decrease = momentumDecrease;
		m.maximum = momentumMaximum;
		m.minimum = momentumMinimum;
		m.initialValue = initialValue;
		initializeMomentums();
	}

	/**
	 * Set the momentum strategy to FIXED.
	 * 
	 * @param value The value.
	 */
	public void setMomentumStrategyFixed(double value) {
		if (value > m.maximum || value < 0.0) {
			throw new IllegalArgumentException("Invalid initial momentum value: " + value);
		}
		m.strategy = MomentumStrategy.FIXED;
		m.initialValue = value;
		initializeMomentums();
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
