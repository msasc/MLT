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

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.tree.TreeCellRenderer;

import com.mlt.desktop.control.Control;
import com.mlt.desktop.control.GridBagPane;
import com.mlt.desktop.control.Label;
import com.mlt.desktop.layout.Anchor;
import com.mlt.desktop.layout.Constraints;
import com.mlt.desktop.layout.Fill;
import com.mlt.desktop.layout.Insets;

/**
 * Tree menu item cell renderer.
 *
 * @author Miquel Sas
 */
public class TreeItemCellRenderer implements TreeCellRenderer {
	
	/** Default icon used to show non-leaf nodes that are collapsed. */
	private Icon iconCollapsed = UIManager.getIcon("Tree.closedIcon");
	/** Icon used to show non-leaf nodes that are expanded. */
	private Icon iconExpanded = UIManager.getIcon("Tree.openIcon");
	/** Icon used to show leaf nodes. */
	private Icon iconLeaf = UIManager.getIcon("Tree.leafIcon");

	/** Default color to use for the background when a node is selected. */
	private Color colorBackgroundSelected = UIManager.getColor("Tree.selectionBackground");
	/** Default color to use for the background when the node isn't selected. */
	private Color colorBackgroundUnselected = UIManager.getColor("Tree.textBackground");
	/** Default color to use for the selection border. */
	private Color colorBorder = UIManager.getColor("Tree.dropLineColor");
	/** Default color foreground selected nodes. */
	private Color colorForegroundSelected = UIManager.getColor("Tree.selectionForeground");
	/** Default color to use for the foreground for non-selected nodes. */
	private Color colorForegroundUnselected = UIManager.getColor("Tree.textForeground");

	/** List of conrtrols. */
	private List<Control> controls = new ArrayList<>();
	/** Label for the icon. */
	private Label labelIcon;
	/** Pane to render the item. */
	private GridBagPane paneRender;

	/**
	 * Constructor.
	 */
	public TreeItemCellRenderer() {
		labelIcon = new Label();
		paneRender = new GridBagPane();
		paneRender.setOpaque(false);
	}
	
	private Constraints getConstraints(int gridx, double leftMargin) {
		Insets insets = new Insets(0, leftMargin, 0, 0);
		return new Constraints(Anchor.LEFT, Fill.NONE, gridx, 0, insets);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component getTreeCellRendererComponent(
		JTree tree,
		Object value,
		boolean selected,
		boolean expanded,
		boolean leaf,
		int row,
		boolean hasFocus) {

		/* Node, must be a tree item node. */
		if (!(value instanceof TreeItemNode)) {
			return paneRender.getComponent();
		}
		TreeItemNode node = (TreeItemNode) value;

		/* Special tree initialization state. */
		if (node.isRoot()) {
			paneRender.removeAll();
			return paneRender.getComponent();
		}
		
		/* Icon. */
		labelIcon.setIconTextGap(0);
		if (leaf) {
			if (node.getIconLeaf() != null) {
				labelIcon.setIcon(node.getIconLeaf());
			} else {
				labelIcon.setIcon(iconLeaf);
			}
		} else {
			if (expanded) {
				if (node.getIconExpanded() != null) {
					labelIcon.setIcon(node.getIconExpanded());
				} else {
					labelIcon.setIcon(iconExpanded);
				}
			} else {
				if (node.getIconCollapsed() != null) {
					labelIcon.setIcon(node.getIconCollapsed());
				} else {
					labelIcon.setIcon(iconCollapsed);
				}
			}
		}

		/* Fill the labels of this node. */
		controls.clear();

		/* Fill the labels for this node without any size check. */
		List<Control> nodeControls = node.getControls();
		for (Control control : nodeControls) {
			if (selected) {
				control.setBackground(colorBackgroundSelected);
				control.setForeground(colorForegroundSelected);
				control.setBorder(new LineBorder(colorBorder));
			} else {
				control.setBackground(colorBackgroundUnselected);
				control.setForeground(colorForegroundUnselected);
				control.setBorder(new EmptyBorder(1, 1, 1, 1));
			}
			controls.add(control);
		}
		
		/* Fill the render pane. */
		int gridx = 0;
		paneRender.removeAll();
		paneRender.add(labelIcon, getConstraints(gridx++, 0));
		for (Control control : controls) {
			paneRender.add(control, getConstraints(gridx++, 5));
		}

		return paneRender.getComponent();
	}

	/**
	 * @param colorBackgroundSelected Background selected color.
	 */
	public void setColorBackgroundSelected(Color colorBackgroundSelected) {
		this.colorBackgroundSelected = colorBackgroundSelected;
	}

	/**
	 * @param colorBackgroundUnselected Background unselected color.
	 */
	public void setColorBackgroundUnselected(Color colorBackgroundUnselected) {
		this.colorBackgroundUnselected = colorBackgroundUnselected;
	}

	/**
	 * @param colorBorder Border color.
	 */
	public void setColorBorder(Color colorBorder) {
		this.colorBorder = colorBorder;
	}

	/**
	 * @param colorForegroundSelected Foreground selected color.
	 */
	public void setColorForegroundSelected(Color colorForegroundSelected) {
		this.colorForegroundSelected = colorForegroundSelected;
	}

	/**
	 * @param colorForegroundUnselected Foreground unselected color.
	 */
	public void setColorForegroungUnselected(Color colorForegroundUnselected) {
		this.colorForegroundUnselected = colorForegroundUnselected;
	}

	/**
	 * @param iconCollapsed The default icon for collapsed non-lead nodes.
	 */
	public void setIconCollapsed(Icon iconCollapsed) {
		this.iconCollapsed = iconCollapsed;
	}

	/**
	 * @param iconExpanded The default icon for expanded non-leaf nodes.
	 */
	public void setIconExpanded(Icon iconExpanded) {
		this.iconExpanded = iconExpanded;
	}

	/**
	 * @param iconLeaf The default icon for leaf nodes.
	 */
	public void setIconLeaf(Icon iconLeaf) {
		this.iconLeaf = iconLeaf;
	}

}
