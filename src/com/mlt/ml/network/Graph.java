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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mlt.ml.network.Network.Backward;
import com.mlt.ml.network.Network.Concurrent;
import com.mlt.ml.network.Network.Forward;
import com.mlt.util.IO;

/**
 * Computational graph, made of nodes wired by edges.
 * <p>
 * The graph must have one and only one input edge, and one and only one output
 * edge.
 *
 * @author Miquel Sas
 */
class Graph {

	/**
	 * Fill the list of child paths of the parent path.
	 * 
	 * @param parentPath The parent path.
	 * @param childPaths The list of child paths to fill.
	 */
	private static void fillAllChildPaths(List<Edge> parentPath, List<List<Edge>> childPaths) {

		/* Add the child path if not already added. */
		if (!childPaths.contains(parentPath)) {
			childPaths.add(parentPath);
		}

		/* Get the input node of the last edge. */
		Node node = parentPath.get(parentPath.size() - 1).getInputNode();

		/* Node null or leaf, done. */
		if (node == null || node.isLeaf()) {
			return;
		}

		/* Scan input edges of the node. */
		for (int i = 0; i < node.inputEdges.size(); i++) {
			Edge edge = node.inputEdges.get(i);

			/* Avoid recurrent input edges. */
			if (edge.isRecurrent()) {
				continue;
			}

			/* Create the child path. */
			List<Edge> path = new ArrayList<>(parentPath);
			path.add(edge);

			/* Process the edge recurrently. */
			fillAllChildPaths(path, childPaths);
		}
	}

	/**
	 * Fill the list of child paths, partial and total, that are input paths of the
	 * node.
	 * 
	 * @param node       The scan node.
	 * @param childPaths The list of child paths.
	 */
	private static void fillAllChildPaths(Node node, List<List<Edge>> childPaths) {

		/* Scan node input edges. */
		for (int i = 0; i < node.inputEdges.size(); i++) {
			Edge edge = node.inputEdges.get(i);

			/* Avoid recurrent input edges. */
			if (edge.isRecurrent()) {
				continue;
			}

			/* Build a child path with the edge. */
			List<Edge> path = new ArrayList<>();
			path.add(edge);

			/* Process the edge recurrently. */
			fillAllChildPaths(path, childPaths);
		}
	}

	/**
	 * Recursively fill the map of nodes starting at the reference node.
	 * 
	 * @param node The reference node.
	 * @param map  The map of node to fill.
	 */
	private static void fillNodesMap(Node node, Map<String, Node> map) {
		if (node == null) {
			return;
		}
		String uuid = node.getUUID();
		if (map.containsKey(uuid)) {
			return;
		}
		map.put(uuid, node);
		for (Edge edge : node.inputEdges) {
			fillNodesMap(edge.getInputNode(), map);
		}
		for (Edge edge : node.outputEdges) {
			fillNodesMap(edge.getOutputNode(), map);
		}
	}

	/**
	 * Returns the backward branch node directly tied to the forward branch node or
	 * null.
	 * 
	 * @param forward The forward branch node to check.
	 * @param nodes   The source list of nodes.
	 * @return The backward branch node directly tied to the forward branch node.
	 */
	private static Node getBackwardBranch(Node forward, List<Node> nodes) {

		/* The start index to scan. */
		int index = nodes.indexOf(forward);
		if (index < 0) {
			throw new IllegalStateException();
		}

		/* Find the first backward branch node. */
		Node backward = null;
		for (int i = index + 1; i < nodes.size(); i++) {
			if (isForwardBranch(nodes.get(i))) {
				return null;
			}
			if (isBackwardBranch(nodes.get(i))) {
				backward = nodes.get(i);
				break;
			}
		}
		if (backward == null) {
			return null;
		}

		/* The number of output-input edges must match. */
		List<Edge> inputEdges = backward.getInputEdges();
		List<Edge> outputEdges = forward.getInputEdges();
		if (outputEdges.size() != inputEdges.size()) {
			return null;
		}

		/*
		 * Check that all output edges from the forward node go directly to the backward
		 * node.
		 */
		for (int i = 0; i < outputEdges.size(); i++) {
			Edge edge = outputEdges.get(i);
			if (!isDirectPath(edge, forward, backward)) {
				return null;
			}
		}

		return backward;
	}

	/**
	 * Return the list of branches, lists of intermediate nodes, between the
	 * backward and the forward nodes. The backward and forward branch nodes must be
	 * connected with direct branches.
	 * 
	 * @param backward The backward branch node.
	 * @param forward  The forward branch node.
	 * @return The list of branches.
	 */
	private static List<List<Node>> getBranchesBackward(Node backward, Node forward) {
		List<List<Node>> branches = new ArrayList<>();
		List<Edge> inputEdges = backward.getInputEdges();
		for (int i = 0; i < inputEdges.size(); i++) {
			Edge edge = inputEdges.get(i);
			List<Node> branch = new ArrayList<>();
			while (!edge.getInputNode().equals(forward)) {
				branch.add(edge.getInputNode());
				edge = edge.getInputNode().getInputEdges().get(0);
			}
			if (!branch.isEmpty()) {
				branches.add(branch);
			}
		}
		return branches;
	}

	/**
	 * Return the list of branches, lists of intermediate nodes, between the forward
	 * and the backward nodes. The forward and backward branch nodes must be
	 * connected with direct branches.
	 * 
	 * @param forward  The forward branch node.
	 * @param backward The backward branch node.
	 * @return The list of branches.
	 */
	private static List<List<Node>> getBranchesForward(Node forward, Node backward) {
		List<List<Node>> branches = new ArrayList<>();
		List<Edge> outputEdges = forward.getInputEdges();
		for (int i = 0; i < outputEdges.size(); i++) {
			Edge edge = outputEdges.get(i);
			List<Node> branch = new ArrayList<>();
			while (!edge.getOutputNode().equals(backward)) {
				branch.add(edge.getOutputNode());
				edge = edge.getOutputNode().getOutputEdges().get(0);
			}
			if (!branch.isEmpty()) {
				branches.add(branch);
			}
		}
		return branches;
	}

	/**
	 * Returns the forward branch node directly tied to the backward branch node or
	 * null.
	 * 
	 * @param backward The backward branch node to check.
	 * @param nodes    The source list of nodes.
	 * @return The forward branch node directly tied to the backward branch node.
	 */
	private static Node getForwardBranch(Node backward, List<Node> nodes) {

		/* The start index to scan. */
		int index = nodes.indexOf(backward);
		if (index < 0) {
			throw new IllegalStateException();
		}

		/* Find the first backward branch node. */
		Node forward = null;
		for (int i = index + 1; i < nodes.size(); i++) {
			if (isBackwardBranch(nodes.get(i))) {
				return null;
			}
			if (isForwardBranch(nodes.get(i))) {
				forward = nodes.get(i);
				break;
			}
		}
		if (forward == null) {
			return null;
		}

		/* The number of output-input edges must match. */
		if (backward.getOutputEdges().size() != forward.getInputEdges().size()) {
			return null;
		}

		/*
		 * Check that all output edges from the forward node go directly to the backward
		 * node.
		 */
		List<Edge> outputEdges = forward.getOutputEdges();
		for (int i = 0; i < outputEdges.size(); i++) {
			Edge edge = outputEdges.get(i);
			if (!isDirectPath(edge, forward, backward)) {
				return null;
			}
		}

		return forward;
	}

	/**
	 * Return the input edge or null.
	 * 
	 * @param map The map of nodes.
	 * @return The input edge or null.
	 */
	private static Edge getInputEdge(Map<String, Node> map) {
		List<Edge> inputEdges = getInputEdges(map);
		if (!inputEdges.isEmpty()) {
			return inputEdges.get(0);
		}
		return null;
	}

	/**
	 * Return a list with all input edges in the map of nodes.
	 * 
	 * @param map The map of nodes.
	 * @return The list of input edges.
	 */
	private static List<Edge> getInputEdges(Map<String, Node> map) {
		List<Edge> inputEdges = new ArrayList<>();
		Iterator<Node> nodes = map.values().iterator();
		while (nodes.hasNext()) {
			Node node = nodes.next();
			for (Edge edge : node.inputEdges) {
				if (edge.isInput() && !inputEdges.contains(edge)) {
					inputEdges.add(edge);
				}
			}
		}
		return inputEdges;
	}

	/**
	 * Return the list of paths that go from the node to the input edge.
	 * 
	 * @param node The scan node.
	 * @return The list of paths that go from the node to the input edge.
	 */
	private static List<List<Edge>> getInputPaths(Node node) {
		List<List<Edge>> childPaths = new ArrayList<>();
		fillAllChildPaths(node, childPaths);
		List<List<Edge>> inputPaths = new ArrayList<>();
		for (List<Edge> path : childPaths) {
			if (isInputPath(path)) {
				inputPaths.add(path);
			}
		}
		return inputPaths;
	}

	/**
	 * Return the nodes in the map in a list ordered from input to output by the
	 * distance to the input edge.
	 * 
	 * @param map The map, collection of nodes.
	 * @return The ordered list of nodes.
	 */
	private static List<Node> getNodesList(Map<String, Node> map) {

		/* Build a list with the nodes in the map. */
		List<Node> nodes = new ArrayList<>(map.values());

		/* Save and remove recurrent nodes from the list. */
		List<Node> recurrentNodes = new ArrayList<>();
		for (Node node : nodes) {
			if (node.isRecurrent()) {
				recurrentNodes.add(node);
			}
		}
		nodes.removeAll(recurrentNodes);

		/* Save and remove leaf nodes from the list. */
		List<Node> leafNodes = new ArrayList<>();
		for (Node node : nodes) {
			if (node.isLeaf()) {
				leafNodes.add(node);
			}
		}
		nodes.removeAll(leafNodes);

		/* Tag the nodes with the distance. */
		Map<Node, Integer> mapDistance = new HashMap<>();
		for (Node node : nodes) {
			List<List<Edge>> paths = getInputPaths(node);
			int distance = 0;
			for (List<Edge> path : paths) {
				distance = Math.max(distance, path.size());
			}
			mapDistance.put(node, distance);
		}

		/* Sort the nodes. */
		nodes.sort((n1, n2) -> Integer.compare(mapDistance.get(n1), mapDistance.get(n2)));

		/* Insert the leaf nodes just before the output node nearer the input edge. */
		leafNodes.forEach(node -> insertNode(node, nodes));
		/*
		 * Insert the recurrent nodes just before the output node nearer the input edge.
		 */
		recurrentNodes.forEach(node -> insertNode(node, nodes));

		return nodes;
	}

	/**
	 * Return a map with all nodes in the graph where the argument node belongs.
	 * 
	 * @param node The reference node.
	 * @return A map with all nodes in the graph where the argument node belongs.
	 */
	private static Map<String, Node> getNodesMap(Node node) {
		Map<String, Node> map = new HashMap<>();
		fillNodesMap(node, map);
		return map;
	}

	/**
	 * Return the output edge or null.
	 * 
	 * @param map The map of nodes.
	 * @return The output edge or null.
	 */
	private static Edge getOutputEdge(Map<String, Node> map) {
		List<Edge> outputEdges = getOutputEdges(map);
		if (!outputEdges.isEmpty()) {
			return outputEdges.get(0);
		}
		return null;
	}

	/**
	 * Return a list with all output edges in the map of nodes.
	 * 
	 * @param map The map of nodes.
	 * @return The list of output edges.
	 */
	private static List<Edge> getOutputEdges(Map<String, Node> map) {
		List<Edge> outputEdges = new ArrayList<>();
		Iterator<Node> nodes = map.values().iterator();
		while (nodes.hasNext()) {
			Node node = nodes.next();
			for (Edge edge : node.outputEdges) {
				if (edge.isOutput() && !outputEdges.contains(edge)) {
					outputEdges.add(edge);
				}
			}
		}
		return outputEdges;
	}

	/**
	 * Insert the the node to the list, just before the output node nearer the input
	 * edge.
	 * 
	 * @param node  The node to insert.
	 * @param nodes The final list of nodes.
	 */
	private static void insertNode(Node node, List<Node> nodes) {
		int insertIndex = Integer.MAX_VALUE;
		for (int i = 0; i < node.outputEdges.size(); i++) {
			Edge edge = node.outputEdges.get(i);
			Node outputNode = edge.getOutputNode();
			if (outputNode == null) {
				throw new IllegalStateException();
			}
			int index = nodes.indexOf(outputNode);
			if (index == -1) {
				throw new IllegalStateException();
			}
			if (index < insertIndex) {
				insertIndex = index;
			}
		}
		if (insertIndex == Integer.MAX_VALUE) {
			throw new IllegalStateException();
		}
		nodes.add(insertIndex - 1, node);
	}

	/**
	 * Check whether the argument node is a backward branch, has more than one input
	 * edge and only one output edge.
	 * 
	 * @param node The node to check.
	 * @return A boolean.
	 */
	private static boolean isBackwardBranch(Node node) {
		return (node.inputEdges.size() > 1 && node.outputEdges.size() == 1);
	}

	/**
	 * Check whether the output edge from the start node goes directly to the end
	 * node.
	 * 
	 * @param edge  The edge to check.
	 * @param start The start node.
	 * @param end   The end node.
	 * @return A boolean.
	 */
	private static boolean isDirectPath(Edge edge, Node start, Node end) {
		if (edge.isOutput() || edge.isRecurrent()) {
			return false;
		}
		if (edge.getInputNode().equals(start) && edge.getOutputNode().equals(end)) {
			return true;
		}
		if (!edge.getOutputNode().isTransfer()) {
			return false;
		}
		return isDirectPath(edge.getOutputNode().getOutputEdges().get(0), edge.getOutputNode(),
			end);
	}

	/**
	 * Check whether the node is a forward branch, has only one input edge and more
	 * than one output edge.
	 * 
	 * @param node The node to check.
	 * @return A boolean.
	 */
	private static boolean isForwardBranch(Node node) {
		return (node.inputEdges.size() == 1 && node.outputEdges.size() > 1);
	}

	/**
	 * Check whether a path is an input path, ends with the input edge of the graph.
	 * 
	 * @param path The path to check.
	 * @return A boolean.
	 */
	private static boolean isInputPath(List<Edge> path) {
		if (path == null) {
			return false;
		}
		if (path.isEmpty()) {
			return false;
		}
		Edge edge = path.get(path.size() - 1);
		return edge.isInput();
	}

	/**
	 * Check whether the branch is wired.
	 * 
	 * @param branch The list of nodes or the branch to check.
	 * @return A boolean.
	 */
	private static boolean isWired(List<Node> branch) {
		if (branch.isEmpty()) {
			return false;
		}
		Map<String, Node> map = getNodesMap(branch.get(0));
		for (Node node : branch) {
			if (!map.containsKey(node.getUUID())) {
				return false;
			}
		}
		return true;
	}

	/** Map of nodes by UUID key, that defines the graph. */
	private Map<String, Node> nodesMap = new HashMap<>();

	/**
	 * Constructor.
	 */
	Graph() {
		super();
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
	void addBranch(List<Node> branch) {

		/* Check wired. */
		if (!isWired(branch)) {
			throw new IllegalArgumentException("Branch with not all nodes wired");
		}

		/* A temporary map to handle the nodes of the branch. */
		Map<String, Node> branchMap = new HashMap<>();
		branch.forEach(node -> branchMap.put(node.getUUID(), node));

		/* Check whether the branch has an input and an output edge. */
		Edge branchInputEdge = getInputEdge(branchMap);
		if (branchInputEdge == null) {
			throw new IllegalArgumentException("Branch without an input edge");
		}
		Edge branchOutputEdge = getOutputEdge(branchMap);
		if (branchOutputEdge == null) {
			throw new IllegalArgumentException("Branch without an output edge");
		}

		/* If the graph is empty, just add the branch. */
		if (nodesMap.isEmpty()) {
			nodesMap.putAll(branchMap);
			return;
		}

		/* Check graph output edge size vs branch input edge size. */
		Edge graphOutputEdge = getOutputEdge();
		if (graphOutputEdge.getSize() != branchInputEdge.getSize()) {
			throw new IllegalArgumentException("Invalid branch input size");
		}
		int size = graphOutputEdge.getSize();

		/* Remove edges from graph output and branch input, and connect them. */
		Node graphOutputNode = graphOutputEdge.getInputNode();
		Node branchInputNode = branchInputEdge.getOutputNode();
		graphOutputNode.outputEdges.remove(graphOutputEdge);
		branchInputNode.inputEdges.remove(branchInputEdge);
		Edge edge = new Edge(size);
		graphOutputNode.addOutputEdge(edge);
		branchInputNode.addInputEdge(edge);

		/* Accept the resulting map. */
		nodesMap.clear();
		nodesMap.putAll(getNodesMap(graphOutputNode));
	}

	/**
	 * Returns the list of concurrent functions to execute the backward pass.
	 * 
	 * @return The list of concurrent functions.
	 */
	List<Concurrent> getBackwardConcurrents() {

		/* Process a modifiable copy of the list of nodes, backward order. */
		List<Node> nodes = getNodes();
		List<Node> scanNodes = new ArrayList<>();
		for (int i = nodes.size() - 1; i >= 0; i--) {
			scanNodes.add(nodes.get(i));
		}

		/* The result list of concurrent functions. */
		List<Concurrent> concurrents = new ArrayList<>();

		/* Process every first node until the list is empty */
		while (!scanNodes.isEmpty()) {
			Node node = scanNodes.get(0);

			/*
			 * If the node is a forward branch, check if there is a backward branch after,
			 * and all branches connect directly, in which case we should add a concurrent
			 * with the forward branch, a concurrent with all intermediate branches, and a
			 * concurrent with the final backward branch.
			 */
			if (isBackwardBranch(node)) {
				Node forwardNode = Graph.getForwardBranch(node, scanNodes);
				if (forwardNode != null) {
					List<List<Node>> branches = Graph.getBranchesBackward(node, forwardNode);
					if (!branches.isEmpty()) {

						/* Current backward node as a single node concurrent. */
						Backward nodeBackward = new Backward();
						nodeBackward.nodes.add(node);
						Concurrent concurrentBackward = new Concurrent();
						concurrentBackward.functions.add(nodeBackward);
						concurrents.add(concurrentBackward);
						scanNodes.removeAll(concurrentBackward.getNodes());

						/* Branches as a concurrent with several functions. */
						Concurrent concurrentBranches = new Concurrent();
						for (List<Node> branch : branches) {
							Backward backward = new Backward();
							backward.nodes = branch;
							concurrentBranches.functions.add(backward);
						}
						concurrents.add(concurrentBranches);
						scanNodes.removeAll(concurrentBranches.getNodes());

						/* Forward node as a single node concurrent. */
						Backward nodeForward = new Backward();
						nodeForward.nodes.add(forwardNode);
						Concurrent concurrentForward = new Concurrent();
						concurrentForward.functions.add(nodeForward);
						concurrents.add(concurrentForward);
						scanNodes.removeAll(concurrentForward.getNodes());
					}
					continue;
				}
			}

			/*
			 * If the node has sibling, process all together. Recall that the siblings
			 * contain at least the current node.
			 */
			if (node.getSiblings().size() > 1) {
				List<Node> siblings = node.getSiblings();
				Concurrent concurrent = new Concurrent();
				for (Node sibling : siblings) {
					Backward backward = new Backward();
					backward.nodes.add(sibling);
					concurrent.functions.add(backward);
				}
				concurrents.add(concurrent);
				scanNodes.removeAll(concurrent.getNodes());
				continue;
			}

			/*
			 * Rest of nodes.
			 */
			Backward backward = new Backward();
			backward.nodes.add(node);
			Concurrent concurrent = new Concurrent();
			concurrent.functions.add(backward);
			concurrents.add(concurrent);
			scanNodes.removeAll(concurrent.getNodes());
			continue;
		}

		return concurrents;
	}

	/**
	 * Return a list with all edges without any special order.
	 * 
	 * @return The list of edges.
	 */
	List<Edge> getEdges() {
		List<Node> nodes = getNodes();
		Map<String, Edge> map = new HashMap<>();
		for (Node node : nodes) {
			for (Edge edge : node.inputEdges) {
				map.put(edge.getUUID(), edge);
			}
			for (Edge edge : node.outputEdges) {
				map.put(edge.getUUID(), edge);
			}
		}
		return new ArrayList<>(map.values());
	}

	/**
	 * Returns the list of concurrent functions to execute the forward pass.
	 * 
	 * @return The list of concurrent functions.
	 */
	List<Concurrent> getForwardConcurrents() {

		/* Process a modifiable copy of the list of nodes. */
		List<Node> nodes = getNodes();
		List<Node> scanNodes = new ArrayList<>();
		for (int i = 0; i < nodes.size(); i++) {
			scanNodes.add(nodes.get(i));
		}

		/* The result list of concurrent functions. */
		List<Concurrent> concurrents = new ArrayList<>();

		/* Process every first node until the list is empty */
		while (!scanNodes.isEmpty()) {
			Node node = scanNodes.get(0);

			/*
			 * If the node is a forward branch, check if there is a backward branch after,
			 * and all branches connect
			 * directly, in which case we should add a concurrent with the forward branch, a
			 * concurrent with all
			 * intermediate branches, and a concurrent with the final backward branch.
			 */
			if (isForwardBranch(node)) {
				Node backwardNode = getBackwardBranch(node, scanNodes);
				if (backwardNode != null) {
					List<List<Node>> branches = getBranchesForward(node, backwardNode);
					if (!branches.isEmpty()) {

						/* Current forward node as a single node concurrent. */
						Forward nodeForward = new Forward();
						nodeForward.nodes.add(node);
						Concurrent concurrentForward = new Concurrent();
						concurrentForward.functions.add(nodeForward);
						concurrents.add(concurrentForward);
						scanNodes.removeAll(concurrentForward.getNodes());

						/* Branches as a concurrent with several functions. */
						Concurrent concurrentBranches = new Concurrent();
						for (List<Node> branch : branches) {
							Forward forward = new Forward();
							forward.nodes = branch;
							concurrentBranches.functions.add(forward);
						}
						concurrents.add(concurrentBranches);
						scanNodes.removeAll(concurrentBranches.getNodes());

						/* Backward node as a single node concurrent. */
						Forward nodeBackward = new Forward();
						nodeBackward.nodes.add(backwardNode);
						Concurrent concurrentBackward = new Concurrent();
						concurrentBackward.functions.add(nodeBackward);
						concurrents.add(concurrentBackward);
						scanNodes.removeAll(concurrentBackward.getNodes());
					}
					continue;
				}
			}

			/*
			 * If the node has sibling, process all together. Recall that the siblings
			 * contain at least the current
			 * node.
			 */
			if (node.getSiblings().size() > 1) {
				List<Node> siblings = node.getSiblings();
				Concurrent concurrent = new Concurrent();
				for (Node sibling : siblings) {
					Forward forward = new Forward();
					forward.nodes.add(sibling);
					concurrent.functions.add(forward);
				}
				concurrents.add(concurrent);
				scanNodes.removeAll(concurrent.getNodes());
				continue;
			}

			/*
			 * Rest of nodes.
			 */
			Forward forward = new Forward();
			forward.nodes.add(node);
			Concurrent concurrent = new Concurrent();
			concurrent.functions.add(forward);
			concurrents.add(concurrent);
			scanNodes.removeAll(concurrent.getNodes());
			continue;
		}

		return concurrents;
	}

	/**
	 * Return the input edge or null.
	 * 
	 * @return The input edge or null.
	 */
	Edge getInputEdge() {
		List<Edge> inputEdges = getInputEdges(nodesMap);
		if (inputEdges.size() == 1) {
			return inputEdges.get(0);
		}
		return null;
	}

	/**
	 * Return the nodes in the map in a list ordered from input to output by the
	 * distance to the input edge.
	 * 
	 * @param map The map, collection of nodes.
	 * @return The ordered list of nodes.
	 */
	List<Node> getNodes() {
		return getNodesList(nodesMap);
	}

	/**
	 * Return the output edge or null.
	 * 
	 * @return The output edge or null.
	 */
	Edge getOutputEdge() {
		List<Edge> outputEdges = getOutputEdges(nodesMap);
		if (outputEdges.size() == 1) {
			return outputEdges.get(0);
		}
		return null;
	}
	
	/**
	 * Restore the graph from an input stream.
	 * 
	 * @param is The input stream.
	 * @throws IOException
	 */
	void restore(InputStream is) throws IOException {
		
		/* Restore the list of nodes. */
		List<Node> nodes = new ArrayList<>();
		int countNodes = IO.readInt(is);
		for (int i = 0; i < countNodes; i++) {
			String nodeClassName = IO.readString(is);
			try {
				Node node = (Node) Class.forName(nodeClassName).newInstance();
				node.restore(is);
				nodes.add(node);
			} catch (Exception exc) {
				throw new IOException(exc);
			}
		}
		
		/* Ensure node with empty input and output edges. */
		for (int i = 0; i < nodes.size(); i++) {
			nodes.get(i).inputEdges.clear();
			nodes.get(i).outputEdges.clear();
		}
		
		/* Build a map of nodes by UUID. */
		Map<String, Node> map = new HashMap<>();
		for (int i = 0; i < nodes.size(); i++) {
			map.put(nodes.get(i).getUUID(), nodes.get(i));
		}
		
		/* Restore edges wiring them. */
		int lengthUUID = nodes.get(0).getUUID().length();
		int countEdges = IO.readInt(is);
		for (int i = 0; i < countEdges; i++) {
			
			/* Edge size, recurrent, and UUID (in-out) */
			int size = IO.readInt(is);
			boolean recurrent = IO.readBoolean(is);
			String uuid = IO.readString(is);
			int inputNodeIndex = IO.readInt(is);
			int outputNodeIndex = IO.readInt(is);
			
			/* Input and output node UUIDs. */
			String inputNodeUUID = null;
			String outputNodeUUID = null;
			if (uuid.length() == (2 * lengthUUID) + 4) {
				inputNodeUUID = uuid.substring(1, 1 + lengthUUID);
				outputNodeUUID = uuid.substring(lengthUUID + 3, 3 + (2 * lengthUUID));
			}
			if (uuid.length() == lengthUUID + 4) {
				if (uuid.startsWith("[]")) {
					outputNodeUUID = uuid.substring(3, 3 + lengthUUID);
				} else {
					inputNodeUUID = uuid.substring(1, 1 + lengthUUID);
				}
			}
			
			/* Set the edge and wire. */
			Edge edge = new Edge(size, recurrent);
			if (inputNodeUUID != null) {
				Node inputNode = map.get(inputNodeUUID);
				if (inputNode == null || inputNodeIndex == -1) {
					throw new IOException("Input node not found");
				}
				while (inputNode.outputEdges.size() <= inputNodeIndex) {
					inputNode.outputEdges.add(null);
				}
				inputNode.outputEdges.set(inputNodeIndex, edge);
				edge.setInputNode(inputNode);
			}
			if (outputNodeUUID != null) {
				Node outputNode = map.get(outputNodeUUID);
				if (outputNode == null || outputNodeIndex == -1) {
					throw new IOException("Output node not found");
				}
				while (outputNode.inputEdges.size() <= outputNodeIndex) {
					outputNode.inputEdges.add(null);
				}
				outputNode.inputEdges.set(outputNodeIndex, edge);
				edge.setOutputNode(outputNode);
			}
		}
		
		/* Set the map. */
		nodesMap.clear();
		nodesMap.putAll(map);
	}
	
	/**
	 * Save the graph to an output stream.
	 * 
	 * @param os The output stream.
	 * @throws IOException
	 */
	void save(OutputStream os) throws IOException {
		
		/* Save the list of nodes. */
		List<Node> nodes = getNodes();
		IO.writeInt(os, nodes.size());
		for (int i = 0; i < nodes.size(); i++) {
			Node node = nodes.get(i);
			IO.writeString(os, node.getClass().getName());
			node.save(os);
		}
		
		/* Save the list of edges. */
		List<Edge> edges = getEdges();
		IO.writeInt(os, edges.size());
		for (int i = 0; i < edges.size(); i++) {
			Edge edge = edges.get(i);
			IO.writeInt(os, edge.getSize());
			IO.writeBoolean(os, edge.isRecurrent());
			IO.writeString(os, edge.getUUID());
			int inputNodeIndex = -1;
			if (edge.getInputNode() != null) {
				inputNodeIndex = edge.getInputNode().outputEdges.indexOf(edge);
			}
			int outputNodeIndex = -1;
			if (edge.getOutputNode() != null) {
				outputNodeIndex = edge.getOutputNode().inputEdges.indexOf(edge);
			}
			IO.writeInt(os, inputNodeIndex);
			IO.writeInt(os, outputNodeIndex);
		}
	}
}
