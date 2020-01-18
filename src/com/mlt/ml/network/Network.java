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

package com.mlt.ml.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;

import com.mlt.util.IO;
import com.mlt.util.Logs;

/**
 * A neural network internally represented by a computational graph, with only
 * one input edge and one output edge.
 *
 * @author Miquel Sas
 */
public class Network {

	/**
	 * Concurrent node, either forward or backward, that executes the list of
	 * callable forward or backward functions, concurrently.
	 */
	static class Concurrent {

		/** List of functions to execute concurrently. */
		List<Callable<Void>> functions = new ArrayList<>();

		/**
		 * Do execute.
		 */
		public void execute() {
			ForkJoinPool.commonPool().invokeAll(functions);
		}

		/**
		 * Return the list with all nodes in this concurrent.
		 * 
		 * @return The list with all nodes.
		 */
		List<Node> getNodes() {
			List<Node> nodes = new ArrayList<>();
			for (Callable<Void> function : functions) {
				if (function instanceof Backward) {
					Backward backward = (Backward) function;
					nodes.addAll(backward.nodes);
				}
				if (function instanceof Forward) {
					Forward forward = (Forward) function;
					nodes.addAll(forward.nodes);
				}
			}
			return nodes;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			return functions.toString();
		}
	}

	/**
	 * Callable function that executes sequentially the backward pass of a list of
	 * nodes. The list of nodes configures a branch that can be executed in parallel
	 * with other branches, but sequentially within the list.
	 */
	static class Backward implements Callable<Void> {

		/** List of nodes. */
		List<Node> nodes = new ArrayList<>();

		/**
		 * Process each node backward.
		 */
		@Override
		public Void call() throws Exception {
			for (int i = 0; i < nodes.size(); i++) {
				nodes.get(i).backward();
			}
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			return nodes.toString();
		}
	}

	/**
	 * Callable function that executes sequentially the forward pass of a list of
	 * nodes. The list of nodes configures a branch that can be executed in parallel
	 * with other branches, but sequentially within the list.
	 */
	static class Forward implements Callable<Void> {

		/** List of nodes. */
		List<Node> nodes = new ArrayList<>();

		/**
		 * Process each node forward.
		 */
		@Override
		public Void call() throws Exception {
			for (int i = 0; i < nodes.size(); i++) {
				nodes.get(i).forward();
			}
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			return nodes.toString();
		}
	}

	/** Internal computational graph. */
	private Graph graph = new Graph();
	/** History size. */
	private int historySize = 1;

	/** Input edge, cached from the graph. */
	private Edge inputEdge;
	/** Output edge, cached from the graph. */
	private Edge outputEdge;
	/** List of backward concurrent branches, cached from the graph. */
	private List<Concurrent> backwardConcurrents;
	/** List of forward concurrent branches, cached from the graph. */
	private List<Concurrent> forwardConcurrents;
	
	/** List of nodes, cached from the graph. */
	private List<Node> nodes;
	/** List of edges, cached from the graph. */
	private List<Edge> edges;

	/** A name that identifies the network. */
	private String name;
	/** Parallel flag. */
	private boolean parallel = true;

	/**
	 * Constructor.
	 */
	public Network() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param historySize The history size.
	 */
	public Network(int historySize) {
		super();
		this.historySize = historySize;
	}

	/**
	 * Add a branch of nodes to the map. The branch must be wired and have an input
	 * and an output edge. If the current graph is empty, then the input edge will
	 * become the input edge of the map. If the graph is not empty, then the input
	 * edge must be of the same size as the current output edge of the graph, and
	 * both edges will be wired.
	 * 
	 * @param branch The list of nodes.
	 */
	public void addBranch(List<Node> branch) {

		/*
		 * Add the branch to the graph. The graph validates that the branch is wired and
		 * has one and only one input and one and only one output edge. It also
		 * validates that the first node of the branch is the input node, and the last
		 * node the output node. Then, if there are more branches, wires this one to the
		 * graph output node, else, it simply add the branch.
		 */
		graph.addBranch(branch);

		/* Cached data. */
		inputEdge = graph.getInputEdge();
		outputEdge = graph.getOutputEdge();
		backwardConcurrents = graph.getBackwardConcurrents();
		forwardConcurrents = graph.getForwardConcurrents();
		nodes = graph.getNodes();
		edges = graph.getEdges();
	}

	/**
	 * Backward pass. Pushes output deltas or errors
	 * 
	 * @param outputDeltas
	 */
	public void backward(double[] outputDeltas) {
		outputEdge.pushBackward(outputDeltas);

		/* Filled up to the history size do unfold. */
		if (outputEdge.getBackwardQueueSize() < historySize) return;

		/* Backward nodes unfolding edges. */
		unfold();
	}

	/**
	 * Calculate forwarding the input values.
	 * 
	 * @param inputValues The input values.
	 * @return The network output values.
	 */
	public double[] calculate(double[] inputValues) {
		return forward(inputValues, false);
	}

	/**
	 * Forward pass in a learning process.
	 * 
	 * @param inputValues The input values.
	 * @return The network output values.
	 */
	public double[] forward(double[] inputValues) {
		return forward(inputValues, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Network clone() {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			save(out);
			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
			Network network = new Network();
			network.restore(in);
			return network;
		} catch (IOException exc) {
			Logs.catching(exc);
			return null;
		}
	}

	/**
	 * Forward pass in a simple calculation, without history, or in a learning
	 * process, eventually keeping a history..
	 * 
	 * @param inputValues The input values.
	 * @param keepHistory A boolean that indicates whether history should be kept.
	 * @return The network output values.
	 */
	private double[] forward(double[] inputValues, boolean keepHistory) {
		inputEdge.pushForward(inputValues);

		if (parallel) {
			for (Concurrent concurrent : forwardConcurrents) {
				concurrent.execute();
			}
		} else {
			for (int i = 0; i < nodes.size(); i++) {
				nodes.get(i).forward();
			}
		}

		double[] outputValues = getOutputValues();
		if (!keepHistory) {
			while (!inputEdge.isEmpty()) {
				edges.forEach(edge -> edge.unfold());
			}
		}
		return outputValues;
	}

	/**
	 * Return a network description for reporting.
	 * 
	 * @return The description.
	 */
	public String getDescription() {
		StringWriter s = new StringWriter();
		PrintWriter p = new PrintWriter(s);
		
		p.print(getName());
		p.print(" (");
		List<Integer> sizes = graph.getSizes();
		for (int i = 0; i < sizes.size(); i++) {
			if (i > 0) {
				p.print(", ");
			}
			p.print(sizes.get(i));
		}
		p.print(")");
		p.println();
		
		List<Integer> branchIndexes = graph.getBranches();
		for (Integer index : branchIndexes) {
			List<Node> branch = graph.getBranch(index);
			for (Node node : branch) {
				p.println();
				p.print(node.getDescription());
			}
			p.println();
		}
		
		p.close();
		return s.toString();
	}

	/**
	 * Return the input edge.
	 * 
	 * @return The input edge.
	 */
	public Edge getInputEdge() {
		return inputEdge;
	}

	/**
	 * Return the network input size.
	 * 
	 * @return The input size.
	 */
	public int getInputSize() {
		if (inputEdge == null) throw new IllegalStateException();
		return inputEdge.getSize();
	}

	/**
	 * Return the network name.
	 * 
	 * @return The name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Return the current list of nodes.
	 * 
	 * @return The list of nodes.
	 */
	public List<Node> getNodes() {
		return graph.getNodes();
	}

	/**
	 * Return the output edge.
	 * 
	 * @return The output edge.
	 */
	public Edge getOutputEdge() {
		return outputEdge;
	}

	/**
	 * Return the network output size.
	 * 
	 * @return The output size.
	 */
	public int getOutputSize() {
		if (outputEdge == null) throw new IllegalStateException();
		return outputEdge.getSize();
	}

	/**
	 * Return the last values pushed to the output edge.
	 * 
	 * @return The output values.
	 */
	public double[] getOutputValues() {
		return outputEdge.getForwardData();
	}

	/**
	 * Initialize the network nodes.
	 */
	public void initialize() {
		edges.forEach(edge -> edge.initialize());
		nodes.forEach(node -> node.initialize());
	}

	/**
	 * Restore from an input stream the network data.
	 * 
	 * @param is The input stream.
	 * @throws IOException
	 */
	public void restore(InputStream is) throws IOException {
		/* Read the history size. */
		historySize = IO.readInt(is);
		/* Restore the graph. */
		graph.restore(is);
		/* Cached data. */
		inputEdge = graph.getInputEdge();
		outputEdge = graph.getOutputEdge();
		backwardConcurrents = graph.getBackwardConcurrents();
		forwardConcurrents = graph.getForwardConcurrents();
		nodes = graph.getNodes();
		edges = graph.getEdges();
	}

	/**
	 * Save to an output stream the network data.
	 * 
	 * @param os The output stream.
	 * @throws IOException
	 */
	public void save(OutputStream os) throws IOException {
		/* Save history sizes. */
		IO.writeInt(os, historySize);
		/* Save the graph. */
		graph.save(os);
	}

	/**
	 * Set the network root name.
	 * 
	 * @param name The name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Set the parallel flag.
	 * 
	 * @param parallel A boolean.
	 */
	public void setParallel(boolean parallel) {
		this.parallel = parallel;
	}

	/**
	 * Apply backward unfolding.
	 */
	public void unfold() {
		while (!inputEdge.isEmpty()) {
			if (parallel) {
				for (Concurrent concurrent : backwardConcurrents) {
					concurrent.execute();
				}
			} else {
				for (int i = nodes.size() - 1; i >= 0; i--) {
					nodes.get(i).backward();
				}
			}
			for (int i = 0; i < edges.size(); i++) {
				Edge edge = edges.get(i);
				edge.unfold();
			}
		}
	}
}
