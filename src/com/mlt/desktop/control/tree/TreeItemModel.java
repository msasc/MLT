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

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

/**
 * Tree model for tree items.
 *
 * @author Miquel Sas
 */
public class TreeItemModel extends DefaultTreeModel {
	
	/** Customizer. */
	private TreeItemModelCustomizer customizer;

	/**
	 * @param root
	 */
	public TreeItemModel(TreeNode root) {
		super(root);
	}

	/**
	 * @param root
	 * @param asksAllowsChildren
	 */
	public TreeItemModel(TreeNode root, boolean asksAllowsChildren) {
		super(root, asksAllowsChildren);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isLeaf(Object node) {
		if (!(node instanceof TreeItemNode)) {
			throw new IllegalArgumentException();
		}
		TreeItemNode treeNode = (TreeItemNode) node;
		if (customizer != null) {
			return customizer.isLeaf(treeNode);
		}
		return super.isLeaf(node);
	}

	/**
	 * @param customizer The customizer.
	 */
	public void setCustomizer(TreeItemModelCustomizer customizer) {
		this.customizer = customizer;
	}
}
