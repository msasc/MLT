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

package com.mlt.ml.network;

import java.util.ArrayList;
import java.util.List;

import com.mlt.ml.function.Activation;
import com.mlt.ml.network.nodes.ActivationNode;
import com.mlt.ml.network.nodes.AdditionNode;
import com.mlt.ml.network.nodes.BiasNode;
import com.mlt.ml.network.nodes.WeightsNode;

/**
 * Builder of network components.
 *
 * @author Miquel Sas
 */
public class Builder {
	/**
	 * Return a list of nodes, ordered from input to output, wired as a perceptron
	 * branch or layer.
	 * 
	 * @param id         A prefix id to build node names.
	 * @param inputSize  Input size.
	 * @param outputSize Output size.
	 * @param activation Activation.
	 * @return
	 */
	public static List<Node> branchPerceptron(String id, int inputSize, int outputSize, Activation activation) {
		
		List<Node> nodes = new ArrayList<>();
		
		Edge inputEdge = new Edge(inputSize);
		WeightsNode weightsNode = new WeightsNode(id, inputSize, outputSize);
		weightsNode.addInputEdge(inputEdge);
		nodes.add(weightsNode);
		
		BiasNode biasNode = new BiasNode(id, outputSize);
		nodes.add(biasNode);
		
		AdditionNode additionNode = new AdditionNode(id, outputSize);
		connect(outputSize, biasNode, additionNode);
		connect(outputSize, weightsNode, additionNode);
		nodes.add(additionNode);
		
		ActivationNode activationNode = new ActivationNode(id, outputSize, activation);
		connect(outputSize, additionNode, activationNode);
		Edge outputEdge = new Edge(outputSize);
		activationNode.addOutputEdge(outputEdge);
		nodes.add(activationNode);
		
		nodes.forEach(node -> node.initialize());
		return nodes;
	}

	/**
	 * Connect two nodes of a network with an edge of the given size.
	 * 
	 * @param size       The size.
	 * @param inputNode  Input node.
	 * @param outputNode Output node.
	 */
	private static void connect(int size, Node inputNode, Node outputNode) {
		connect(size, inputNode, outputNode, false);
	}
	/**
	 * Connect two nodes of a network with an edge of the given size.
	 * 
	 * @param size         The size.
	 * @param inputNode    Input node.
	 * @param outputNode   Output node.
	 * @param backwardFlow Backward flow (recurrent) flag.
	 */
	private static void connect(int size, Node inputNode, Node outputNode, boolean backwardFlow) {
		Edge edge = new Edge(size, backwardFlow);
		inputNode.addOutputEdge(edge);
		outputNode.addInputEdge(edge);
	}
}
