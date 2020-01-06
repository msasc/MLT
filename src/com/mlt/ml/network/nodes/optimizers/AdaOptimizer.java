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

package com.mlt.ml.network.nodes.optimizers;

import java.util.ArrayDeque;
import java.util.Deque;

import com.mlt.ml.function.Collector;
import com.mlt.ml.function.collector.CollectorAverage;
import com.mlt.ml.function.collector.CollectorTransfer;
import com.mlt.ml.network.nodes.WeightsOptimizer;
import com.mlt.util.Matrix;
import com.mlt.util.Vector;

/**
 * Adaptative optimizer, with an learning rate and a momentum for each weight,
 * and a queue of output deltas to optionally calculate moving averages and
 * other functions on those output deltas.
 *
 * @author Miquel Sas
 */
public class AdaOptimizer extends WeightsOptimizer {

	/** Learning rates. */
	private double[][] learningRates;
	/** Momentums. */
	private double[][] momentums;
	/** Last weight deltas to apply to momentums. */
	private double[][] weightDeltas;
	/** Averages of output deltas. */
	private double[] averages;
	/** Standard deviations on output deltas. */
	private double[] stddevs;
	
	/** Output deltas processed by the collector. */
	private double[] outputDeltas;
	/** Output deltas queue. */
	private Deque<double[]> deltasQueue;
	/** Maximum queue size. */
	private int maxQueueSize = 50;
	/** Collector function to apply to the queue of deltas. */
	private Collector collector;
	
	/** Counter of steps. */
	private int step;

	/**
	 * Constructor.
	 */
	public AdaOptimizer() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void backward(int start, int end) {
		for (int in = start; in <= end; in++) {
			double input = inputValues[in];
			double inputDelta = 0;
			for (int out = 0; out < outputSize; out++) {
				double weight = weights[in][out];
				double outputDelta = outputDeltas[out];
				inputDelta += (weight * outputDelta);
				/* Weight delta. */
				double learningRate = learningRates[in][out];
				double momentum = momentums[in][out];
				double lastDelta = weightDeltas[in][out];
				double prevDelta = momentum * lastDelta;
				double nextDelta = (1 - momentum) * (learningRate * outputDelta * input);
				double weightDelta = prevDelta + nextDelta;
				weights[in][out] += weightDelta;
				weightDeltas[in][out] = weightDelta;
			}
			inputDeltas[in] = inputDelta;
		}
	}

	/**
	 * Check if should initialize internal arrays.
	 */
	private void checkInitialize() {
		if (learningRates != null) {
			return;
		}
		
		/* Learning rates. */
		learningRates = new double[inputSize][outputSize];
		Matrix.fill(learningRates, 0.01);
		
		/* Momentums. */
		momentums = new double[inputSize][outputSize];
		Matrix.fill(momentums, 0.5);
		
		/* Last weight deltas. */
		weightDeltas = new double[inputSize][outputSize];
		Matrix.fill(weightDeltas, 0.0);
		
		/* Averages of output deltas. */
		averages = new double[outputSize];
		Vector.fill(averages, 0.0);
		
		/* Standard deviations of output deltas. */
		stddevs = new double[outputSize];
		Vector.fill(stddevs, 0.0);
		
		/* Queue of output deltasand default collector. */
		deltasQueue = new ArrayDeque<>();
		collector = new CollectorAverage();
		
		/* Counter of steps. */
		step = 0;
	}
	
	/**
	 * @param outputDeltas The output deltas to push in the queue.
	 */
	private void pushOutputDeltas(double[] outputDeltas) {
		deltasQueue.addLast(outputDeltas);
		if (deltasQueue.size() > maxQueueSize) {
			deltasQueue.removeFirst();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void set(
		int inputSize,
		int outputSize,
		double[][] weights,
		double[] inputDeltas,
		double[] inputValues,
		double[] outputDeltas,
		double[] outputValues) {
		
		super.set(
			inputSize,
			outputSize,
			weights,
			inputDeltas,
			inputValues,
			outputDeltas,
			outputValues);
		
		/* Check initialize. */
		checkInitialize();
		
		/* Push output deltas. */
		pushOutputDeltas(outputDeltas);
		
		/* Retrieve output deltas. */
		this.outputDeltas = collector.collect(deltasQueue);
	}

}
