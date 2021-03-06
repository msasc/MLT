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

package com.mlt.ml.rlearn;

/**
 * An action of the reinforcement learning process with policy gradient strategy.
 *
 * @author Miquel Sas
 */
public interface Action {
	/**
	 * Indicates whether this action starts an episode.
	 * 
	 * @return A boolean.
	 */
	boolean isStartEpisode();

	/**
	 * Indicates whether this action ends an episode.
	 * 
	 * @return A boolean.
	 */
	boolean isEndEpisode();

	/**
	 * Return the gradients that, applied to the back propagation, encourages the action.
	 * 
	 * @return The encouraging gradients.
	 */
	double[] getGradientsEncourage();

	/**
	 * Return a gradients that, applied to the back propagation, discourages the action.
	 * 
	 * @return The discouraging gradients.
	 */
	double[] getGradientsDiscourage();
}
