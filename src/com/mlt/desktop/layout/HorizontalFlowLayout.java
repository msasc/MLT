/*
 * Copyright (C) 2017 Miquel Sas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.mlt.desktop.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import com.mlt.desktop.AWT;

/**
 * An horizontal flow layout will layout components in rows, wrapping them at the container width.
 *
 * @author Miquel Sas
 */
public class HorizontalFlowLayout extends LayoutAdapter {

	/** Horizontal alignment, default is right. */
	private Alignment horizontalAlignment = Alignment.RIGHT;
	/** Vertical alignment within the row, default is center. */
	private Alignment verticalAlignment = Alignment.CENTER;
	/** Border insets. */
	private Insets borderInsets;
	/** Horizontal gap. */
	private int horizontalGap = 5;
	/** Vertical gap. */
	private int verticalGap = 5;

	/**
	 * Constructor.
	 */
	public HorizontalFlowLayout() {
		this(new Insets(5, 5, 5, 5));
	}

	/**
	 * Constructor assigning the border insets.
	 *
	 * @param top    Top.
	 * @param left   Left.
	 * @param bottom Bottom.
	 * @param right  Right.
	 */
	public HorizontalFlowLayout(Insets borderInsets) {
		super();
		this.borderInsets = borderInsets;
	}

	/**
	 * Return the border insets.
	 * 
	 * @return The border insets.
	 */
	public Insets getBorderInsets() {
		return borderInsets;
	}

	/**
	 * Return the row height.
	 *
	 * @param row The row as a list of components.
	 * @return The row height.
	 */
	private int getRowHeight(List<Component> row) {
		int height = 0;
		for (Component component : row) {
			int cmpHeight = component.getPreferredSize().height;
			height = Math.max(height, cmpHeight);
		}
		return height;
	}

	/**
	 * Returns the list of rows from the visible components of the container.
	 *
	 * @param container The parent container.
	 * @return The list of rows.
	 */
	private List<List<Component>> getRows(Container container) {

		int insetsLeft = (int) borderInsets.getRight();
		int insetsRight = (int) borderInsets.getRight();

		int containerWidth = container.getWidth();
		if (containerWidth == 0) {
			Component top = AWT.getTopComponent(container);
			Window window = null;
			if (top != null && top instanceof Window) {
				window = (Window) top;
			}
			containerWidth = (int) (AWT.getScreenSize(window).getWidth() * 0.8);
		}
		List<Component> visibleComponents = getVisibleComponents(container);
		List<List<Component>> rows = new ArrayList<>();
		List<Component> row = new ArrayList<>();
		for (Component component : visibleComponents) {
			if (!row.isEmpty()) {
				if (insetsLeft + getRowWidth(row, component) + insetsRight > containerWidth) {
					rows.add(row);
					row = new ArrayList<>();
				}
			}
			row.add(component);
		}
		rows.add(row);
		return rows;
	}

	/**
	 * Return the total width of the row.
	 *
	 * @param row The row or list of components.
	 * @return The total width.
	 */
	private int getRowWidth(List<Component> row) {
		return getRowWidth(row, null);
	}

	/**
	 * Return the total width of the row with an additional component including the left and right border insets.
	 *
	 * @param row       The row or list of components.
	 * @param component The optional additional component.
	 * @return The total width.
	 */
	private int getRowWidth(List<Component> row, Component component) {
		int width = 0;
		for (int i = 0; i < row.size(); i++) {
			if (i > 0) {
				width += horizontalGap;
			}
			width += row.get(i).getPreferredSize().width;
		}
		if (component != null) {
			width += horizontalGap;
			width += component.getPreferredSize().width;
		}
		return width;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void layoutContainer(Container container) {
		synchronized (container.getTreeLock()) {
			moveComponents(container);
		}
	}

	/**
	 * Return the layout size.
	 *
	 * @param container The parent container.
	 * @return The layout size.
	 */
	private Dimension layoutSize(Container container) {

		int insetsTop = (int) borderInsets.getTop();
		int insetsBottom = (int) borderInsets.getBottom();
		int insetsLeft = (int) borderInsets.getRight();
		int insetsRight = (int) borderInsets.getRight();

		int width = container.getWidth();
		int height = 0;
		height += insetsTop;
		List<List<Component>> rows = getRows(container);
		for (int i = 0; i < rows.size(); i++) {
			if (i > 0) {
				height += verticalGap;
			}
			height += getRowHeight(rows.get(i));
			width = Math.max(width, getRowWidth(rows.get(i)));
		}
		height += insetsBottom;
		if (container.getWidth() == 0) {
			width += insetsLeft + insetsRight;
		}
		return new Dimension(width, height);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Dimension minimumLayoutSize(Container container) {
		synchronized (container.getTreeLock()) {
			return layoutSize(container);
		}
	}

	private void moveComponents(Container container) {

		int insetsTop = (int) borderInsets.getTop();
		int insetsLeft = (int) borderInsets.getLeft();
		int insetsRight = (int) borderInsets.getRight();

		int containerWidth = container.getWidth();
		int rowTop = insetsTop;
		List<List<Component>> rows = getRows(container);
		for (int i = 0; i < rows.size(); i++) {

			/* Move the top of the row conveniently. */
			if (i > 0) {
				rowTop += getRowHeight(rows.get(i - 1)) + verticalGap;
			}

			/* Current row, its width and height, and the remaining padding. */
			List<Component> row = rows.get(i);
			int rowWidth = getRowWidth(row);
			int rowHeight = getRowHeight(row);
			int padding = containerWidth - insetsLeft - rowWidth - insetsRight;

			/* Calculate the start left depending on the horizontal alignment. */
			int rowLeft = 0;
			switch (horizontalAlignment) {
			case LEFT:
				rowLeft = insetsLeft;
				break;
			case RIGHT:
				rowLeft = insetsLeft + padding;
				break;
			case CENTER:
				rowLeft = insetsLeft + (padding / 2);
				break;
			default:
				rowLeft = insetsLeft + padding;
				break;
			}

			/*
			 * Move row components. The vertical position of the will depend on the vertical alignment.
			 */
			for (int j = 0; j < row.size(); j++) {

				/* Move the row start left point when required. */
				if (j > 0) {
					rowLeft += row.get(j - 1).getPreferredSize().width + horizontalGap;
				}

				/* Calculate component top depending on the vertical alignment. */
				Component cmp = row.get(j);
				int cmpHeight = cmp.getPreferredSize().height;
				int cmpTop = 0;
				switch (verticalAlignment) {
				case TOP:
					cmpTop = rowTop;
					break;
				case BOTTOM:
					cmpTop = rowTop + (rowHeight - cmpHeight);
					break;
				case CENTER:
					cmpTop = rowTop + ((rowHeight - cmpHeight) / 2);
					break;
				default:
					cmpTop = rowTop + (rowHeight - cmpHeight);
					break;
				}
				cmp.setSize(cmp.getPreferredSize());
				cmp.setLocation(rowLeft, cmpTop);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Dimension preferredLayoutSize(Container container) {
		synchronized (container.getTreeLock()) {
			try {
				return layoutSize(container);
			} finally {
				container.repaint();
			}
		}
	}

	/**
	 * Set the border insets.
	 *
	 * @param top    Top.
	 * @param left   Left.
	 * @param bottom Bottom.
	 * @param right  Right.
	 */
	public void setBorderInsets(int top, int left, int bottom, int right) {
		borderInsets = new Insets(top, left, bottom, right);
	}

	/**
	 * Set the horizontal alignment.
	 *
	 * @param horizontalAlignment The alignment.
	 */
	public void setHorizontalAlignment(Alignment horizontalAlignment) {
		this.horizontalAlignment = horizontalAlignment;
	}

	/**
	 * Set the horizontal gap.
	 *
	 * @param horizontalGap The gap.
	 */
	public void setHorizontalGap(int horizontalGap) {
		this.horizontalGap = horizontalGap;
	}

	/**
	 * Set the vertical alignment.
	 *
	 * @param verticalAlignment The alignment.
	 */
	public void setVerticalAlignment(Alignment verticalAlignment) {
		this.verticalAlignment = verticalAlignment;
	}

	/**
	 * Set the vertical gap.
	 *
	 * @param verticalGap The gap.
	 */
	public void setVerticalGap(int verticalGap) {
		this.verticalGap = verticalGap;
	}
}
