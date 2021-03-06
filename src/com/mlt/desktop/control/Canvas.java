/*
 * Copyright (C) 2018 Miquel Sas
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.mlt.desktop.control;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import com.mlt.desktop.graphic.Drawing;
import com.mlt.desktop.graphic.Hint;
import com.mlt.desktop.graphic.Rectangle;
import com.mlt.desktop.layout.Dimension;
import com.mlt.util.Numbers;

/**
 * A canvas control.
 *
 * @author Miquel Sas
 */
public abstract class Canvas extends Control {

	/**
	 * Internal canvas pane.
	 */
	private class CanvasPane extends JPanel {
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void paint(Graphics g) {
			gc.parent = (Graphics2D) g;
			gc.refresh();
			paintCanvas(gc);
			gc.flush();
			paintBorder(g);
		}
	}

	/**
	 * The graphics contexts to paint on a <em>Canvas</em>.
	 *
	 * @author Miquel Sas
	 */
	public class Context {

		/** The graphics object. */
		private Graphics2D g2d;
		/** List of saved rendering hints. */
		private List<Hint> hints = new ArrayList<>();
		/** The buffered image used to paint on. */
		private BufferedImage img;
		/** Indicates whether the context is used in an immediate repaint. */
		private boolean immediateRepaint = false;
		/** The parent graphics. */
		private Graphics2D parent;

		/**
		 * Constructor.
		 */
		private Context() {
			super();
		}

		/**
		 * Clear the canvas with the argument color.
		 *
		 * @param paint The color.
		 */
		public void clear(Paint paint) {
			Rectangle rect = new Rectangle(0, 0, getWidth(), getHeight());
			rect.setFillPaint(paint);
			fill(rect);
		}

		/**
		 * Draw the drawing.
		 *
		 * @param drawing The drawing.
		 */
		public void draw(Drawing drawing) {
			saveAntSetHints(drawing.getHints());
			g2d.setStroke(drawing.getStroke());
			g2d.setPaint(drawing.getDrawPaint());
			g2d.draw(drawing.getShape());
			restoreHints();
		}

		/**
		 * Fill the drawing.
		 *
		 * @param drawing The drawing.
		 */
		public void fill(Drawing drawing) {
			saveAntSetHints(drawing.getHints());
			g2d.setPaint(drawing.getFillPaint());
			g2d.fill(drawing.getShape());
			restoreHints();
		}

		/**
		 * Flush the content.
		 */
		private void flush() {
			parent.drawImage(gc.img, 0, 0, null);
		}

		/**
		 * Return the current font.
		 *
		 * @return The current font.
		 */
		public Font getFont() {
			return g2d.getFont();
		}

		/**
		 * Return the font rendering context.
		 *
		 * @return The font rendering context.
		 */
		public FontRenderContext getFontRenderContext() {
			return g2d.getFontRenderContext();
		}

		/**
		 * Return the height of the drawing area.
		 *
		 * @return The height.
		 */
		public double getHeight() {
			return getSize().getHeight();
		}

		/**
		 * Return the pixel at.
		 *
		 * @param x x coord.
		 * @param y y coord.
		 * @return
		 */
		public int getRGB(int x, int y) {
			return img.getRGB(x, y);
		}

		/**
		 * Return the width of the drawing area.
		 *
		 * @return The width.
		 */
		public double getWidth() {
			return getSize().getWidth();
		}

		/**
		 * Check if this context is used in an immediate repaint.
		 *
		 * @return A boolean.
		 */
		public boolean isImmediateRepaint() {
			return immediateRepaint;
		}

		/**
		 * Refresh this context to match the underlying canvas size.
		 *
		 * @param g The parent graphics.
		 */
		private void refresh() {
			Dimension sz = getSize();
			if (img == null ||
				g2d == null ||
				img.getWidth() < sz.getWidth() ||
				img.getHeight() < sz.getHeight()) {

				int width = (int) Numbers.round(sz.getWidth(), 0);
				int height = (int) Numbers.round(sz.getHeight(), 0);
				img = parent.getDeviceConfiguration().createCompatibleImage(width, height);
				g2d = img.createGraphics();
			}
		}

		/**
		 * Restore the save hints.
		 */
		private void restoreHints() {
			for (Hint hint : hints) {
				g2d.setRenderingHint(hint.getKey(), hint.getValue());
			}
			hints.clear();
		}

		/**
		 * Save the current rendering hints and set the argument ones.
		 * 
		 * @param drawingHints The drawing hints.
		 */
		private void saveAntSetHints(List<Hint> drawingHints) {
			if (!drawingHints.isEmpty()) {
				hints.clear();
				for (Hint hint : drawingHints) {
					RenderingHints.Key key = hint.getKey();
					Object value = g2d.getRenderingHint(key);
					hints.add(new Hint(key, value));
					g2d.setRenderingHint(key, hint.getValue());
				}
			}
		}

		/**
		 * Set the pixel color.
		 *
		 * @param x   x coord.
		 * @param y   y coord.
		 * @param rgb RGB (sRGB) color.
		 */
		public void setRGB(int x, int y, int rgb) {
			img.setRGB(x, y, rgb);
		}

	}

	/** Shortcut to BasicStroke.JOIN_MITER. */
	public static final int JOIN_MITER = BasicStroke.JOIN_MITER;
	/** Shortcut to BasicStroke.JOIN_ROUND. */
	public static final int JOIN_ROUND = BasicStroke.JOIN_ROUND;
	/** The canvas context used to paint. */
	private Context gc;

	/**
	 * Constructor.
	 */
	public Canvas() {
		setComponent(new CanvasPane());
		gc = new Context();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CanvasPane getComponent() {
		return (CanvasPane) super.getComponent();
	}

	/**
	 * Return the font render context.
	 *
	 * @return The font render context.
	 */
	public FontRenderContext getFontRenderContext() {
		return gc.getFontRenderContext();
	}

	/**
	 * Return the height of this canvas.
	 *
	 * @return The height.
	 */
	public double getHeight() {
		return getSize().getHeight();
	}

	/**
	 * Return the width of this canvas.
	 *
	 * @return The width.
	 */
	public double getWidth() {
		return getSize().getWidth();
	}

	/**
	 * Paint. This is the method to override.
	 *
	 * @param gc The context.
	 */
	protected abstract void paintCanvas(Context gc);

	/**
	 * Force an immediate repaint.
	 */
	public void repaintImmediately() {
		gc.immediateRepaint = true;
		getComponent().paintImmediately(getComponent().getBounds());
		gc.immediateRepaint = false;
	}

}
