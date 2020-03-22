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

package com.mlt.desktop.control.tree;

/**
 * Simple tree model customizer for tree items.
 *
 * @author Miquel Sas
 */
public interface TreeItemModelCustomizer {
	
	/**
	 * @param node The node.
	 * @return The number of childs.
	 */
	int getChildCount(TreeItemNode node);
	
	/**
	 * Check whether the node is a leaf node.
	 * 
	 * @param node The node.
	 * @return A boolean.
	 */
	boolean isLeaf(TreeItemNode node);
}
