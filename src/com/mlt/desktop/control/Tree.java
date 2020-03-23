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

package com.mlt.desktop.control;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import com.mlt.desktop.control.tree.TreeItemCellRenderer;
import com.mlt.desktop.control.tree.TreeItemModel;
import com.mlt.desktop.control.tree.TreeItemModelCustomizer;
import com.mlt.desktop.control.tree.TreeItemNode;

/**
 * Tree control.
 *
 * @author Miquel Sas
 */
public class Tree extends Control {

	/**
	 * Constructor.
	 */
	public Tree() {
		setComponent(new JScrollPane(new JTree()));
		getTreeComponent().setCellRenderer(new TreeItemCellRenderer());
		getTreeComponent().setShowsRootHandles(true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final JScrollPane getComponent() {
		return (JScrollPane) super.getComponent();
	}
	
	/**
	 * @return The model.
	 */
	public TreeItemModel getModel() {
		return (TreeItemModel) getTreeComponent().getModel();
	}
	
	/**
	 * @param x X.
	 * @param y Y.
	 * @return The path.
	 */
	public TreePath getPathForLocation(int x, int y) {
		return getTreeComponent().getPathForLocation(x, y);
	}

	/**
	 * @return The JTree component.
	 */
	public final JTree getTreeComponent() {
		return (JTree) getComponent().getViewport().getView();
	}

	/**
	 * @param customizer The model customizer.
	 */
	public void setModelCustomizer(TreeItemModelCustomizer customizer) {
		TreeItemModel model = (TreeItemModel) getTreeComponent().getModel();
		model.setCustomizer(customizer);
	}

	/**
	 * @param root The root node.
	 */
	public void setRoot(TreeItemNode root) {
		getTreeComponent().setModel(new TreeItemModel(root));
	}

	/**
	 * @param visible A boolean that indicates whether the root should be visible.
	 */
	public void setRootVisible(boolean visible) {
		getTreeComponent().setRootVisible(visible);
	}
}
