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

import com.mlt.util.Properties;

/**
 * A state at a given point in time, of the reinforcement learning process with policy gradient strategy.
 * <p>
 * A <b><i>State</i></b> contains the following information components:
 * <ul>
 * <li>The input vector that will be delivered to the agent to be processed to produce the next action.</li>
 * <li>The agent state properties at the current time.</li>
 * <li>Any environment state properties, visible or not to the agent, at the current time.</li>
 * </ul>
 *
 * @author Miquel Sas
 */
public class State {

	/** Input values of the network. */
	private double[] inputValues;
	/** Agent state properties. */
	private Properties agentState;
	/** Environment state properties. */
	private Properties environmentState;

	/**
	 * Constructor.
	 * 
	 * @param inputValues Input values of the network.
	 * @param agentState Agent state properties.
	 * @param environmentState Environment state properties.
	 */
	public State(double[] inputValues, Properties agentState, Properties environmentState) {
		super();
		this.inputValues = inputValues;
		this.agentState = agentState;
		this.environmentState = environmentState;
	}


	/**
	 * Return the input values to be forwarded the agent or network.
	 * 
	 * @return The input values.
	 */
	public double[] getInputValues() {
		return inputValues;
	}

	/**
	 * Returns the agent properties.
	 * 
	 * @return The agent properties.
	 */
	public Properties getAgentState() {
		return agentState;
	}

	/**
	 * Return the environment properties.
	 * 
	 * @return The environment properties.
	 */
	public Properties getEnvironmentState() {
		return environmentState;
	}
}
