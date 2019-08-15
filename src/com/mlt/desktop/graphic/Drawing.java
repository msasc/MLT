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
package com.mlt.desktop.graphic;

import com.mlt.util.Colors;
import com.mlt.util.Lists;
import com.mlt.util.Numbers;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * A generic drawing to use with the graphics context. A drawing has to implement a unique method to get its shape.
 *
 * @author Miquel Sas
 */
public abstract class Drawing {

	/**
	 * Return the enclosing bounds
	 *
	 * @param drawings The list of drawings.
	 * @return The bounds
	 */
	public static Rectangle getBounds(Drawing... drawings) {
		return getBounds(Lists.asList(drawings));
	}

	/**
	 * Return the enclosing bounds
	 *
	 * @param drawings The list of drawings.
	 * @return The bounds
	 */
	public static Rectangle getBounds(List<Drawing> drawings) {
		double startX = Numbers.MAX_DOUBLE;
		double startY = Numbers.MAX_DOUBLE;
		double endX = Numbers.MIN_DOUBLE;
		double endY = Numbers.MIN_DOUBLE;

		for (Drawing drawing : drawings) {
			Rectangle r = drawing.getBounds();
			double x = r.getX();
			double y = r.getY();
			double w = r.getWidth();
			double h = r.getHeight();
			if (x < startX) {
				startX = x;
			}
			if (y < startY) {
				startY = y;
			}
			if (x + w > endX) {
				endX = x + w;
			}
			if (y + h > endY) {
				endY = y + h;
			}
		}
		return new Rectangle(startX, startY, endX - startX, endY - startY);
	}

	/**
	 * Scale the drawing.
	 *
	 * @param d The drawing.
	 * @param x The x scale.
	 * @param y The y scale.
	 * @return The scaled shape.
	 */
	public static Drawing scale(Drawing d, double x, double y) {
		return transform(d, AffineTransform.getScaleInstance(x, y));
	}

	/**
	 * Translate the drawing.
	 *
	 * @param d The drawing.
	 * @param x The x translation.
	 * @param y The y translation.
	 * @return The scaled shape.
	 */
	public static Drawing translate(Drawing d, double x, double y) {
		return transform(d, AffineTransform.getTranslateInstance(x, y));
	}

	/**
	 * Apply the affine transform to the drawing.
	 *
	 * @param d The drawing.
	 * @param t The affine transform.
	 * @return The new drawing.
	 */
	public static Drawing transform(Drawing d, AffineTransform t) {
		GeneralPath p = new GeneralPath();
		p.append(d.getShape().getPathIterator(t), true);
		return new Outline(p);
	}

	/** Drawing stroke. */
	private Stroke stroke = new Stroke(1.0);
	/** Draw paint. */
	private Paint drawPaint = Colors.BLACK;
	/** Fill paint. */
	private Paint fillPaint = Colors.WHITE;
	/** List of rendering hints. */
	private List<Hint> hints;

	/**
	 * Default constructor.
	 */
	public Drawing() {
		super();
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
		if (stroke == null) {
			throw new NullPointerException();
		}
		this.stroke = stroke;
	}

	/**
	 * Return the paint to draw.
	 *
	 * @return The paint.
	 */
	public Paint getDrawPaint() {
		return drawPaint;
	}

	/**
	 * Set the paint to draw.
	 *
	 * @param drawPaint The paint.
	 */
	public void setDrawPaint(Paint drawPaint) {
		if (drawPaint == null) {
			throw new NullPointerException();
		}
		this.drawPaint = drawPaint;
	}

	/**
	 * Return the paint to fill.
	 *
	 * @return The paint.
	 */
	public Paint getFillPaint() {
		return fillPaint;
	}

	/**
	 * Set the paint to fill.
	 *
	 * @param fillPaint The paint.
	 */
	public void setFillPaint(Paint fillPaint) {
		if (fillPaint == null) {
			throw new NullPointerException();
		}
		this.fillPaint = fillPaint;
	}

	/**
	 * Tests if the specified coordinates are inside the boundary of the drawing.
	 *
	 * @param x x coord.
	 * @param y y coord.
	 * @return A boolean.
	 */
	public boolean contains(double x, double y) {
		return getShape().contains(x, y);
	}

	/**
	 * Tests if the specified point is inside the boundary of the drawing.
	 *
	 * @param p The poit to test.
	 * @return A boolean.
	 */
	public boolean contains(Point p) {
		return contains(p.getX(), p.getY());
	}

	/**
	 * Tests if the interior of the drawing entirely contains the specified rectangular area.
	 *
	 * @param x      x coord.
	 * @param y      y coord.
	 * @param width  Width.
	 * @param height Height.
	 * @return A boolean.
	 */
	public boolean contains(double x, double y, double width, double height) {
		return getShape().contains(x, y, width, height);
	}

	/**
	 * Tests if the interior of the drawing entirely contains the specified rectangular area.
	 *
	 * @param r The rectangle.
	 * @return A boolean.
	 */
	public boolean contains(Rectangle r) {
		return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
	}

	/**
	 * Return the shape bound corrected by the stroke width.
	 *
	 * @return The effective bounds.
	 */
	public Rectangle getBounds() {
		Rectangle b = getShapeBounds();
		double lineWidth = stroke.getLineWidth();
		double x = b.getX() - lineWidth / 2;
		double y = b.getY() - lineWidth / 2;
		double width = b.getWidth() + lineWidth;
		double height = b.getHeight() + lineWidth;
		return new Rectangle(x, y, width, height);
	}

	/**
	 * Add a hint for the drawing.
	 * 
	 * @param key   The key.
	 * @param value The value.
	 */
	public void addHint(RenderingHints.Key key, Object value) {
		if (hints == null) {
			hints = new ArrayList<>();
		}
		hints.add(new Hint(key, value));
	}

	/**
	 * Return the list of hints to apply to the drawing.
	 * 
	 * @return The list of hints.
	 */
	public List<Hint> getHints() {
		if (hints == null) {
			return Hint.EMPTY_HINTS;
		}
		return hints;
	}

	/**
	 * Returns an rectangle that completely encloses the drawing.
	 *
	 * @return The bounds.
	 */
	public Rectangle getShapeBounds() {
		Rectangle2D r = getShape().getBounds2D();
		return new Rectangle(r.getX(), r.getY(), r.getWidth(), r.getHeight());
	}

	/**
	 * Return this drawing shape.
	 *
	 * @return The shape.
	 */
	public abstract Shape getShape();

	/**
	 * Tests if the interior of the drawing intersects the interior of a specified rectangular area.
	 *
	 * @param x      x coord.
	 * @param y      y coord.
	 * @param width  Width.
	 * @param height Height.
	 * @return A boolean.
	 */
	public boolean intersects(double x, double y, double width, double height) {
		return getShape().intersects(x, y, width, height);
	}

	/**
	 * Tests if the interior of the drawing intersects the interior of a specified rectangular area.
	 *
	 * @param r The rectangle.
	 * @return A boolean.
	 */
	public boolean intersects(Rectangle r) {
		return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
	}
}
