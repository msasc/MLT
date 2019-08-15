/*
 * Copyright (C) 2018 Miquel Sas
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
package com.mlt.desktop.border;

import com.mlt.desktop.graphic.Stroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;

import javax.swing.border.AbstractBorder;

/**
 * A line border deciding the sides.
 * 
 * @author Miquel Sas
 */
public class LineBorderSides extends AbstractBorder {

	/** Line paint. */
	private Paint paint = Color.BLACK;
	/** Stroke. */
	private Stroke stroke = new Stroke(1.0f);
	/** A boolean that indicates if the top side should be painted. */
	private boolean top = true;
	/** A boolean that indicates if the left side should be painted. */
	private boolean left = true;
	/** A boolean that indicates if the bottom side should be painted. */
	private boolean bottom = true;
	/** A boolean that indicates if the right side should be painted. */
	private boolean right = true;

	/**
	 * Constructor. Initializes paint and stroke, while top, left, bottom and right will be set as required.
	 * 
	 * @param paint  Line paint.
	 * @param stroke Line stroke.
	 */
	public LineBorderSides(Paint paint, Stroke stroke) {
		super();
		this.paint = paint;
		this.stroke = stroke;
	}

	/**
	 * Generic constructor.
	 * 
	 * @param paint  Line paint.
	 * @param stroke Line stroke.
	 * @param top    A boolean that indicates if the top side should be painted.
	 * @param left   A boolean that indicates if the left side should be painted.
	 * @param bottom A boolean that indicates if the bottom side should be painted.
	 * @param right  A boolean that indicates if the right side should be painted.
	 */
	public LineBorderSides(Paint paint, Stroke stroke, boolean top, boolean left, boolean bottom, boolean right) {
		super();
		this.paint = paint;
		this.stroke = stroke;
		this.top = top;
		this.left = left;
		this.bottom = bottom;
		this.right = right;
	}

	/**
	 * Returns the line paint.
	 * 
	 * @return The line paint.
	 */
	public Paint getPaint() {
		return paint;
	}

	/**
	 * Sets the line paint.
	 * 
	 * @param paint The line paint.
	 */
	public void setPaint(Paint paint) {
		this.paint = paint;
	}

	/**
	 * Return the stroke.
	 * 
	 * @return The stroke.
	 */
	public Stroke getStroke() {
		return stroke;
	}

	/**
	 * Set the stroke.
	 * 
	 * @param stroke The stroke.
	 */
	public void setStroke(Stroke stroke) {
		this.stroke = stroke;
	}

	/**
	 * Returns a boolean that indicates if the top side should be painted.
	 * 
	 * @return A boolean that indicates if the top side should be painted.
	 */
	public boolean isTop() {
		return top;
	}

	/**
	 * Sets a boolean that indicates if the top side should be painted.
	 * 
	 * @param top A boolean that indicates if the top side should be painted.
	 */
	public void setTop(boolean top) {
		this.top = top;
	}

	/**
	 * Returns a boolean that indicates if the left side should be painted.
	 * 
	 * @return A boolean that indicates if the left side should be painted.
	 */
	public boolean isLeft() {
		return left;
	}

	/**
	 * Sets a boolean that indicates if the left side should be painted.
	 * 
	 * @param left A boolean that indicates if the left side should be painted.
	 */
	public void setLeft(boolean left) {
		this.left = left;
	}

	/**
	 * Returns a boolean that indicates if the bottom side should be painted.
	 * 
	 * @return A boolean that indicates if the bottom side should be painted.
	 */
	public boolean isBottom() {
		return bottom;
	}

	/**
	 * Sets a boolean that indicates if the bottom side should be painted.
	 * 
	 * @param bottom A boolean that indicates if the bottom side should be painted.
	 */
	public void setBottom(boolean bottom) {
		this.bottom = bottom;
	}

	/**
	 * Returns a boolean that indicates if the right side should be painted.
	 * 
	 * @return A boolean that indicates if the right side should be painted.
	 */
	public boolean isRight() {
		return right;
	}

	/**
	 * Sets a boolean that indicates if the right side should be painted.
	 * 
	 * @param right A boolean that indicates if the right side should be painted.
	 */
	public void setRight(boolean right) {
		this.right = right;
	}

	/**
	 * Set this border empty.
	 */
	public void setEmpty() {
		top = left = bottom = right = false;
	}

	/**
	 * Set the borders.
	 * 
	 * @param top    A boolean that indicates if the top side should be painted.
	 * @param left   A boolean that indicates if the left side should be painted.
	 * @param bottom A boolean that indicates if the bottom side should be painted.
	 * @param right  A boolean that indicates if the right side should be painted.
	 * 
	 * @return This border.
	 */
	public LineBorderSides setSides(boolean top, boolean left, boolean bottom, boolean right) {
		this.top = top;
		this.left = left;
		this.bottom = bottom;
		this.right = right;
		return this;
	}

	/**
	 * Reinitialize the insets parameter with this Border's current Insets.
	 * 
	 * @param c      the component for which this border insets value applies
	 * @param insets the object to be reinitialized
	 * @return The insets.
	 */
	@Override
	public Insets getBorderInsets(Component c, Insets insets) {
		int width = (int) stroke.getLineWidth();
		insets.set(width, width, width, width);
		return insets;
	}

	/**
	 * Paints the border for the specified component with the specified position and size.
	 * 
	 * @param c      the component for which this border is being painted
	 * @param g      the paint graphics
	 * @param x      the x position of the painted border
	 * @param y      the y position of the painted border
	 * @param width  the width of the painted border
	 * @param height the height of the painted border
	 */
	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		float size = this.stroke.getLineWidth();
		if (size > 0.0f) {
			g = g.create();
			if (g instanceof Graphics2D) {
				Graphics2D g2d = (Graphics2D) g;
				java.awt.Stroke saveStroke = g2d.getStroke();
				Paint savePaint = g2d.getPaint();
				g2d.setStroke(stroke);
				g2d.setPaint(paint);
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				if (top) {
					g2d.draw(new Line2D.Float(0, size / 2, width, size / 2));
				}
				if (left) {
					g2d.draw(new Line2D.Float(size / 2, 0, size / 2, height));
				}
				if (bottom) {
					g2d.draw(new Line2D.Float(0, height - size / 2, width, height - size / 2));
				}
				if (right) {
					g2d.draw(new Line2D.Float(width - size / 2, 0, width - size / 2, height));
				}
				g2d.setStroke(saveStroke);
				g2d.setPaint(savePaint);
			}
			g.dispose();
		}

	}

}
