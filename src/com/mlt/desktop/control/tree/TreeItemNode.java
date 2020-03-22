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

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;

import com.mlt.desktop.control.Control;
import com.mlt.desktop.control.Label;

/**
 * Tree node item. Can configure the font, the open, close and leaf icons, and a
 * list of lable and icon pairs to set the line.
 *
 * @author Miquel Sas
 */
public class TreeItemNode extends DefaultMutableTreeNode {

	/** Icon used to show non-leaf nodes that are collapsed. */
	private Icon iconCollapsed;
	/** Icon used to show leaf nodes. */
	private Icon iconLeaf;
	/** Icon used to show non-leaf nodes that are expanded. */
	private Icon iconExpanded;
	/** List of elements. */
	private List<Control> controls;

	/**
	 * A boolean that indicates whether the node elements should be aligned with its
	 * siblings.
	 */
	private boolean alignWithSiblings = true;

	/**
	 * Constructor.
	 */
	public TreeItemNode() {
		controls = new ArrayList<>();
	}

	/**
	 * @param control a control.
	 */
	public void add(Control control) {
		controls.add(control);
	}

	/**
	 * @param icon Icon.
	 */
	public void add(Icon icon) {
		controls.add(new Label(icon));
	}

	/**
	 * @param text Text.
	 */
	public void add(String text) {
		add(text, null);
	}

	/**
	 * @param text Text.
	 * @param font Font.
	 */
	public void add(String text, Font font) {
		Label label = new Label(text);
		if (font != null) {
			label.setFont(font);
		}
		controls.add(label);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TreeItemNode getChildAt(int index) {
		return (TreeItemNode) super.getChildAt(index);
	}

	/**
	 * @return The list of controls.
	 */
	public List<Control> getControls() {
		return controls;
	}

	/**
	 * @return Close icon.
	 */
	public Icon getIconCollapsed() {
		return iconCollapsed;
	}

	/**
	 * @return Open icon.
	 */
	public Icon getIconExpanded() {
		return iconExpanded;
	}

	/**
	 * @return Leaf icon.
	 */
	public Icon getIconLeaf() {
		return iconLeaf;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TreeItemNode getParent() {
		return (TreeItemNode) super.getParent();
	}

	/**
	 * @return The list of siblings of this node.
	 */
	public List<TreeItemNode> getSiblings() {
		List<TreeItemNode> siblings = new ArrayList<>();
		TreeItemNode parent = getParent();
		if (parent != null) {
			for (int i = 0; i < parent.getChildCount(); i++) {
				siblings.add(parent.getChildAt(i));
			}
		}
		return siblings;
	}

	/**
	 * @return A boolean indicating whether the node elements should be aligned with
	 *         its siblings.
	 */
	public boolean isAlignWithSiblings() {
		return alignWithSiblings;
	}

	/**
	 * @param alignWithSiblings A boolean that indicates whether the node elements
	 *                          should be aligned with its siblings.
	 */
	public void setAlignWithSiblings(boolean alignWithSiblings) {
		this.alignWithSiblings = alignWithSiblings;
	}

	/**
	 * @param iconCollapsed Collapsed icon.
	 */
	public void setIconCollapsed(Icon iconCollapsed) {
		this.iconCollapsed = iconCollapsed;
	}

	/**
	 * @param iconExpanded Expanded icon.
	 */
	public void setIconExpanded(Icon iconExpanded) {
		this.iconExpanded = iconExpanded;
	}

	/**
	 * @param iconLeaf Leaf icon.
	 */
	public void setIconLeaf(Icon iconLeaf) {
		this.iconLeaf = iconLeaf;
	}
}
