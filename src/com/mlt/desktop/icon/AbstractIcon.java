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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.swing.Icon;

import com.mlt.desktop.graphic.Rectangle;
import com.mlt.desktop.graphic.Stroke;
import com.mlt.util.Logs;
import com.mlt.util.Numbers;
import com.mlt.util.Properties;

/**
 * Root of drawn icons. Offers the most common properties and prepares the graphics context to perform the paint.
 *
 * @author Miquel Sas
 */
public abstract class AbstractIcon implements Icon {

	/** Properties. */
	private Properties properties = new Properties();

	/**
	 * Constructor.
	 */
	public AbstractIcon() {
		super();
	}

	public AbstractIcon copy() {
		try {
			Constructor<? extends AbstractIcon> constructor = getClass().getDeclaredConstructor();
			if (constructor != null) {
				AbstractIcon copy = constructor.newInstance();
				copy.properties.putAll(properties);
				copy.properties.remove("CONTEXT");
				return copy;
			}
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
		| IllegalArgumentException | InvocationTargetException e) {
			Logs.catching(e);
		}
		return null;
	}

	/**
	 * Return the bounds.
	 *
	 * @return The bounds rectangle.
	 */
	public Rectangle getBounds() {
		return new Rectangle(0, 0, getWidth(), getHeight());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getIconWidth() {
		return (int) Numbers.round(getWidth(), 0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getIconHeight() {
		return (int) Numbers.round(getHeight(), 0);
	}

	/**
	 * Offer access to the properties map, for extenders to include particular properties.
	 *
	 * @return The properties map.
	 */
	protected Properties getProperties() {
		return properties;
	}

	/**
	 * Return the icon height. Default is 15.
	 *
	 * @return The icon height.
	 */
	public double getHeight() {
		return properties.getDouble("HEIGHT", 15.0);
	}

	/**
	 * Set the icon height.
	 *
	 * @param height The height.
	 */
	public void setHeight(double height) {
		properties.setDouble("HEIGHT", height);
	}

	/**
	 * Return the icon width. Default is 15.
	 *
	 * @return The icon width.
	 */
	public double getWidth() {
		return properties.getDouble("WIDTH", 15.0);
	}

	/**
	 * Set the icon width.
	 *
	 * @param width The width.
	 */
	public void setWidth(double width) {
		properties.setDouble("WIDTH", width);
	}

	/**
	 * Return the top margin. Default is 1/3.75 the height.
	 *
	 * @return The margin.
	 */
	public double getMarginTop() {
		return getHeight() * properties.getDouble("MARGIN_TOP_FACTOR", 0.25);
	}

	/**
	 * Return the left margin. Default is 1/3 the width.
	 *
	 * @return The margin.
	 */
	public double getMarginLeft() {
		return getWidth() * properties.getDouble("MARGIN_LEFT_FACTOR", 0.25);
	}

	/**
	 * Return the bottom margin. Default is 1/3.75 the height.
	 *
	 * @return The margin.
	 */
	public double getMarginBottom() {
		return getHeight() * properties.getDouble("MARGIN_BOTTOM_FACTOR", 0.25);
	}

	/**
	 * Return the right margin. Default is 1/3 the width.
	 *
	 * @return The margin.
	 */
	public double getMarginRight() {
		return getWidth() * properties.getDouble("MARGIN_RIGHT_FACTOR", 0.25);
	}

	/**
	 * Set the margins.
	 *
	 * @param top    Top margin.
	 * @param left   Left margin.
	 * @param bottom Bottom margin.
	 * @param right  Right margin.
	 */
	public void setMargins(double top, double left, double bottom, double right) {
		properties.setDouble("MARGIN_TOP_FACTOR", top / getHeight());
		properties.setDouble("MARGIN_LEFT_FACTOR", left / getWidth());
		properties.setDouble("MARGIN_BOTTOM_FACTOR", bottom / getHeight());
		properties.setDouble("MARGIN_RIGHT_FACTOR", right / getWidth());
	}

	/**
	 * Set the margin factors.
	 *
	 * @param top    Top margin factor.
	 * @param left   Left margin factor.
	 * @param bottom Bottom margin factor.
	 * @param right  Right margin factor.
	 */
	public void setMarginFactors(double top, double left, double bottom, double right) {
		properties.setDouble("MARGIN_TOP_FACTOR", top);
		properties.setDouble("MARGIN_LEFT_FACTOR", left);
		properties.setDouble("MARGIN_BOTTOM_FACTOR", bottom);
		properties.setDouble("MARGIN_RIGHT_FACTOR", right);
	}

	/**
	 * Set the size.
	 *
	 * @param width  The width.
	 * @param height The height.
	 */
	public void setSize(double width, double height) {
		setWidth(width);
		setHeight(height);
	}

	/**
	 * Check whether this icon should be opaque, that is, should paint the background. Default is false.
	 *
	 * @return A boolean.
	 */
	public boolean isOpaque() {
		return properties.getBoolean("OPAQUE", false);
	}

	/**
	 * Set whether this icon should be opaque, that is, should paint the background.
	 *
	 * @param opaque A boolean.
	 */
	public void setOpaque(boolean opaque) {
		properties.setBoolean("OPAQUE", opaque);
	}

	/**
	 * Return the paint (color) foreground enabled.
	 *
	 * @return The paint.
	 */
	public Paint getPaintForegroundEnabled() {
		return (Paint) properties.getObject("FOREGROUND_ENABLED", Color.DARK_GRAY);
	}

	/**
	 * Set the paint (color) foreground enabled.
	 *
	 * @param paint The paint.
	 */
	public void setPaintForegroundEnabled(Paint paint) {
		properties.setObject("FOREGROUND_ENABLED", paint);
	}

	/**
	 * Return the paint (color) foreground disabled.
	 *
	 * @return The paint.
	 */
	public Paint getPaintForegroundDisabled() {
		return (Paint) properties.getObject("FOREGROUND_DISABLED", Color.GRAY);
	}

	/**
	 * Set the paint (color) foreground disabled.
	 *
	 * @param paint The paint.
	 */
	public void setPaintForegroundDisabled(Paint paint) {
		properties.setObject("FOREGROUND_DISABLED", paint);
	}

	/**
	 * Return the paint (color) background enabled.
	 *
	 * @return The paint.
	 */
	public Paint getPaintBackgroundEnabled() {
		return (Paint) properties.getObject("BACKGROUND_ENABLED", new Color(240, 240, 240));
	}

	/**
	 * Set the paint (color) background enabled.
	 *
	 * @param paint The paint.
	 */
	public void setPaintBackgroundEnabled(Paint paint) {
		properties.setObject("BACKGROUND_ENABLED", paint);
	}

	/**
	 * Return the paint (color) background disabled.
	 *
	 * @return The paint.
	 */
	public Paint getPaintBackgroundDisabled() {
		return (Paint) properties.getObject("BACKGROUND_DISABLED", new Color(240, 240, 240));
	}

	/**
	 * Set the paint (color) background disabled.
	 *
	 * @param paint The paint.
	 */
	public void setPaintBackgroundDisabled(Paint paint) {
		properties.setObject("BACKGROUND_DISABLED", paint);
	}

	/**
	 * Check whether automatic management of background is enabled. Default is true.
	 * <p>
	 * When background is enable, if the icon is opaque, the background is automatically filled using the appropriate
	 * paint if the component is enabled, disabled or focused.
	 *
	 * @return A boolean.
	 */
	public boolean isBackgroundEnabled() {
		return properties.getBoolean("BACKGROUND_MODE", true);
	}

	/**
	 * Set whether automatic management of background is enabled. Default is true.
	 * <p>
	 * When background is enable, if the icon is opaque, the background is automatically filled using the appropriate
	 * paint if the component is enabled, disabled or focused.
	 *
	 * @param enabled A boolean.
	 */
	public void setBackgroundEnabled(boolean enabled) {
		properties.setBoolean("BACKGROUND_MODE", enabled);
	}

	/**
	 * Return the stroke enabled. Default is <em>Stroke(1.0f)</em>.
	 *
	 * @return The stroke.
	 */
	public Stroke getStrokeEnabled() {
		return (Stroke) properties.getObject("STROKE_ENABLED", new Stroke(1.0));
	}

	/**
	 * Set the stroke enabled.
	 *
	 * @param stroke The stroke.
	 */
	public void setStrokeEnabled(Stroke stroke) {
		properties.setObject("STROKE_ENABLED", stroke);
	}

	/**
	 * Return the stroke disabled. Default is <em>Stroke(1.0f)</em>.
	 *
	 * @return The stroke.
	 */
	public Stroke getStrokeDisabled() {
		return (Stroke) properties.getObject("STROKE_DISABLED", new Stroke(1.0f));
	}

	/**
	 * Set the stroke disabled.
	 *
	 * @param stroke The stroke.
	 */
	public void setStrokeDisabled(Stroke stroke) {
		properties.setObject("STROKE_DISABLED", stroke);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {

		Graphics2D g2d = (Graphics2D) g;
		Paint savePaint = g2d.getPaint();
		java.awt.Stroke saveStroke = g2d.getStroke();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.translate(x, y);

		/* Fill the background if required. */
		if (isBackgroundEnabled() && isOpaque()) {
			if (c.isEnabled()) {
				g2d.setPaint(getPaintBackgroundEnabled());
			} else {
				g2d.setPaint(getPaintBackgroundDisabled());
			}
			g2d.fill(getBounds().getShape());
		}

		/* Set the appropriate paint and stroke. */
		if (c == null || c.isEnabled()) {
			g2d.setPaint(getPaintForegroundEnabled());
			g2d.setStroke(getStrokeEnabled());
		} else {
			g2d.setPaint(getPaintForegroundDisabled());
			g2d.setStroke(getStrokeDisabled());
		}

		/* Do paint. */
		paintIcon(g2d);

		g2d.translate(-x, -y);
		g2d.setPaint(savePaint);
		g2d.setStroke(saveStroke);
	}

	/**
	 * Paint the icon on the graphics context.Strictly paint, no need to save and restore graphics properties. If the
	 * implementation uses the default foreground paints and strokes for enabled, disabled and focused, it does not have
	 * to set them.
	 *
	 * @param g2d The 2D graphics.
	 */
	protected abstract void paintIcon(Graphics2D g2d);
}
