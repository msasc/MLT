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

import java.awt.Font;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import com.mlt.desktop.layout.Dimension;

/**
 * A text drawing.
 *
 * @author Miquel Sas
 */
public class Text extends Drawing {

	/**
	 * Return the text size.
	 *
	 * @param text The text.
	 * @param font The font.
	 * @return The size.
	 */
	public static Dimension getSize(String text, Font font) {
		return new Text(text, font, 0, 0).getShapeBounds().getSize();
	}

	/**
	 * A default font render context to build text objects.
	 */
	private static final FontRenderContext FONT_RENDER_CONTEXT = 
		new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB).createGraphics().getFontRenderContext();

	/** The text shape. */
	private Shape shape;

	/**
	 * Constructor.
	 *
	 * @param text The text string.
	 * @param font The desired font.
	 * @param x    x coordinate.
	 * @param y    y coordinate.
	 */
	public Text(String text, Font font, double x, double y) {
		super();
		TextLayout layout = new TextLayout(text, font, FONT_RENDER_CONTEXT);
		shape = layout.getOutline(AffineTransform.getTranslateInstance(x, y));
		addHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Shape getShape() {
		return shape;
	}

}
