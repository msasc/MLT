/*
 * Copyright (C) 2018 Miquel Sas
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General PublicLicense as published by the Free Software
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

import com.mlt.ml.function.Collector;
import com.mlt.ml.function.collector.CollectorAddition;
import com.mlt.ml.function.collector.CollectorTransfer;
import com.mlt.ml.network.Edge;

/**
 * A node that has a single input edge and several output edges, all with the
 * same size. In the forward pass, the input is forwarded to all output edges.
 * In the backward pass the deltas of the output edges are processed by a
 * collector function, normally adding them.
 *
 * @author Miquel Sas
 */
public class SwitchNode extends BranchNode {

	/**
	 * Constructor used for restore.
	 */
	public SwitchNode() {
		super();
	}

	/**
	 * Constructor with a default backward function of addition.
	 * 
	 * @param id   Node id.
	 * @param size The node input-output size.
	 */
	public SwitchNode(String id, int size) {
		this(id, size, new CollectorAddition());
	}

	/**
	 * Constructor.
	 * 
	 * @param id               Node id.
	 * @param size             Node input-output size.
	 * @param backwardFunction Backward collector function.
	 */
	public SwitchNode(String id, int size, Collector backwardFunction) {
		super(id, size, new CollectorTransfer(), backwardFunction);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addInputEdge(Edge edge) throws IllegalStateException {
		if (inputEdges.size() > 0) {
			throw new IllegalStateException("More than one input edge");
		}
		super.addInputEdge(edge);
	}
}
