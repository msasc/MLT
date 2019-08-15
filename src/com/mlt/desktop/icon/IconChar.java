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
package com.mlt.desktop.icon;

import java.awt.BasicStroke;
import java.awt.Font;

import com.mlt.desktop.graphic.Drawing;
import com.mlt.desktop.graphic.Rectangle;
import com.mlt.desktop.graphic.Text;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;

/**
 * A icon that renders a string or list of characters.
 *
 * @author Miquel Sas
 */
public class IconChar extends AbstractIcon {

	/**
	 * Constructor.
	 */
	public IconChar() {
		super();
	}

	/**
	 * Return the text to render.
	 * 
	 * @return The text to render.
	 */
	public String getText() {
		return getProperties().getString("TEXT", "i");
	}

	/**
	 * Set the text to render.
	 * 
	 * @param text The text to render.
	 */
	public void setText(String text) {
		getProperties().setString("TEXT", text);
	}

	/**
	 * Return the font used to render the string. The shape of the text is scaled to fit the available bound, so a
	 * sufficiently big size is the best option.
	 * 
	 * @return The font.
	 */
	public Font getFont() {
		return (Font) getProperties().getObject("FONT", new Font(Font.DIALOG, Font.PLAIN, 12));
	}

	/**
	 * Set the font used to render the string. The shape of the text is scaled to fit the available bound, so a
	 * sufficiently big size is the best option.
	 * 
	 * @param font The font.
	 */
	public void setFont(Font font) {
		getProperties().setObject("FONT", font);
	}

	/**
	 * Check whether the arrow is empty (not filled).
	 * 
	 * @return A boolean.
	 */
	public boolean isEmpty() {
		return getProperties().getBoolean("EMPTY", true);
	}

	/**
	 * Set whether the arrow is empty (not filled).
	 * 
	 * @param empty A boolean.
	 */
	public void setEmpty(boolean empty) {
		getProperties().setBoolean("EMPTY", empty);
	}

	/**
	 * Check whether the arrow is filled.
	 * 
	 * @return A boolean.
	 */
	public boolean isFilled() {
		return !isEmpty();
	}

	/**
	 * Set whether the arrow is filled.
	 * 
	 * @param filled A boolean.
	 */
	public void setFilled(boolean filled) {
		setEmpty(!filled);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void paintIcon(Graphics2D g2d) {

		Object saveHint = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		/* Origin and available bounds. */
		double x = getMarginLeft();
		double y = getMarginTop();
		double width = getWidth() - getMarginLeft() - getMarginRight();
		double height = getHeight() - getMarginTop() - getMarginBottom();

		Drawing text = new Text(getText(), getFont(), 0, 0);
		Rectangle bounds = text.getShapeBounds();

		/* Translate to (0, 0) origin. */
		text = Drawing.translate(text, -bounds.getX(), -bounds.getY());
		/* Scale up the width until does not fit. */
		while (true) {
			bounds = text.getShapeBounds();
			if (bounds.getWidth() > width)
				break;
			text = Drawing.scale(text, 1.001, 1.000);
		}
		/* Scale down the width until it fits again. */
		while (true) {
			bounds = text.getShapeBounds();
			if (bounds.getWidth() <= width)
				break;
			text = Drawing.scale(text, 0.999, 1.000);
		}
		/* Scale up the height until does not fit. */
		while (true) {
			bounds = text.getShapeBounds();
			if (bounds.getHeight() > height)
				break;
			text = Drawing.scale(text, 1.000, 1.001);
		}
		/* Scale down the height until it fits again. */
		while (true) {
			bounds = text.getShapeBounds();
			if (bounds.getHeight() <= height)
				break;
			text = Drawing.scale(text, 1.000, 0.999);
		}
		/* Translate to drawing origin. */
		text = Drawing.translate(text, x, y);
		
		Stroke saveStroke = g2d.getStroke();
		g2d.setStroke(new BasicStroke(0.5f));

		/* Draw or fill. */
		if (isFilled()) {
			g2d.fill(text.getShape());
		} else {
			g2d.draw(text.getShape());
		}

		g2d.setStroke(saveStroke);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, saveHint);
	}
}
