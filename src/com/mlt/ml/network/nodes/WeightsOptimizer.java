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

/**
 * Weights optimizer.
 *
 * @author Miquel Sas
 */
public abstract class WeightsOptimizer {

	/** Weights node. */
	private WeightsNode node;

	/**
	 * Constructor.
	 */
	public WeightsOptimizer() {
		super();
	}

	/**
	 * @return The parent weights node.
	 */
	protected WeightsNode getNode() {
		return node;
	}

	/**
	 * Backward process for the weights for a range of input indexes, updateing the
	 * weight and calculating the input delta.
	 * 
	 * @param in Input index.
	 */
	public abstract void backward(int in);

	/**
	 * Perform any required finalization after the concurrent call to the
	 * backward per weight pass.
	 */
	public abstract void finalizeBackward();

	/**
	 * Perform any required initialization before the concurrent call to the
	 * backward per weight pass.
	 */
	public abstract void initializeBackward();
	
	/**
	 * Initialize the optimizer.
	 */
	public abstract void initializeOptimizer();

	/**
	 * @param node The parent weights node.
	 */
	public void setNode(WeightsNode node) {
		this.node = node;
	}
}
