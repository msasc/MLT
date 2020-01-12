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

import com.mlt.ml.function.collector.CollectorAddition;
import com.mlt.ml.function.collector.CollectorTransfer;

/**
 * A branch addition node.
 *
 * @author Miquel Sas
 */
public class AdditionNode extends BranchNode {

	/**
	 * Constructor for restore.
	 */
	public AdditionNode() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param size Size.
	 */
	public AdditionNode(int size) {
		super(size, new CollectorAddition(), new CollectorTransfer());
	}
}
