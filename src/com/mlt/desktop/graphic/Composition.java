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

import com.mlt.desktop.control.Canvas;
import com.mlt.util.Numbers;
import java.util.ArrayList;
import java.util.List;

/**
 * A composition concatenates a list of drawings with fill or draw operations.
 *
 * @author Miquel Sas
 */
public class Composition {

	/** Draw operation. */
	private static final int DRAW = 0;
	/** Fill operation. */
	private static final int FILL = 1;
	/** Paint operation, first fill and then draw. */
	private static final int PAINT = 2;

	/**
	 * Segment structure.
	 */
	class Segment {
		Drawing drawing;
		int operation;

		Segment(Drawing drawing, int operation) {
			this.drawing = drawing;
			this.operation = operation;
		}
	}

	/** List of segments. */
	private List<Segment> segments = new ArrayList<>();

	/** Identifier. */
	private String id;
	/** Saved area. */
	private Rectangle rect;
	/** Saved pixels. */
	private int[][] pixels;
	/** A boolean that indicates whether the composition is enabled. */
	private boolean enabled = true;

	/**
	 * Constructor.
	 *
	 * @param id The identification.
	 */
	public Composition(String id) {
		super();
		if (id == null) {
			throw new NullPointerException();
		}
		this.id = id;
	}

	/**
	 * Add a draw segment.
	 *
	 * @param drawing The drawing.
	 */
	public void addDraw(Drawing drawing) {
		if (drawing == null) {
			throw new NullPointerException();
		}
		segments.add(new Segment(drawing, DRAW));
	}

	/**
	 * Add a fill segment.
	 *
	 * @param drawing The drawing.
	 */
	public void addFill(Drawing drawing) {
		if (drawing == null) {
			throw new NullPointerException();
		}
		segments.add(new Segment(drawing, FILL));
	}

	/**
	 * Add a paint segment.
	 *
	 * @param drawing The drawing.
	 */
	public void addPaint(Drawing drawing) {
		if (drawing == null) {
			throw new NullPointerException();
		}
		segments.add(new Segment(drawing, PAINT));
	}

	/**
	 * Clear this composition. This removes previous drawings but not the previously saved area.
	 */
	public void clear() {
		if (!enabled) return;
		segments.clear();
	}

	public void clearSave() {
		pixels = null;
	}

	/**
	 * Returns an rectangle that completely encloses all the pixels of the composition.
	 *
	 * @param gc The graphics context.
	 * @return The bounds.
	 */
	private Rectangle getBounds(Canvas.Context gc) {

		double startX = Numbers.MAX_DOUBLE;
		double startY = Numbers.MAX_DOUBLE;
		double endX = Numbers.MIN_DOUBLE;
		double endY = Numbers.MIN_DOUBLE;

		for (Segment segment : segments) {
			Rectangle r = segment.drawing.getBounds();
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

		startX = Numbers.round(startX, 0);
		if (startX < 0) {
			startX = 0;
		}
		if (startX > 0) {
			startX -= 1;
		}

		startY = Numbers.round(startY, 0);
		if (startY < 0) {
			startY = 0;
		}
		if (startY > 0) {
			startY -= 1;
		}

		endX = Numbers.round(endX, 0);
		if (endX >= gc.getWidth()) {
			endX = gc.getWidth() - 1;
		}
		if (endX < gc.getWidth() - 1) {
			endX += 1;
		}
		if (endX < gc.getWidth() - 1) {
			endX += 1;
		}

		endY = Numbers.round(endY, 0);
		if (endY >= gc.getHeight()) {
			endY = gc.getHeight() - 1;
		}
		if (endY < gc.getHeight() - 1) {
			endY += 1;
		}
		if (endY < gc.getHeight() - 1) {
			endY += 1;
		}

		return new Rectangle(startX, startY, endX - startX + 1, endY - startY + 1);
	}

	/**
	 * Return the id.
	 *
	 * @return The id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Check enabled.
	 * 
	 * @return A boolean.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Render this composition.
	 *
	 * @param gc The graphics context.
	 */
	public void paint(Canvas.Context gc) {
		if (!enabled) return;
		for (Segment segment : segments) {
			Drawing drawing = segment.drawing;
			int op = segment.operation;
			if (op == FILL || op == PAINT) {
				gc.fill(drawing);
			}
			if (op == DRAW || op == PAINT) {
				gc.draw(drawing);
			}
		}
	}

	/**
	 * Restore the previously stored area.
	 *
	 * @param gc The graphics context.
	 */
	public void restore(Canvas.Context gc) {
		if (!enabled) return;
		if (pixels == null || rect == null) {
			return;
		}

		int startX = (int) rect.getX();
		int startY = (int) rect.getY();
		int width = (int) (rect.getWidth());
		int height = (int) rect.getHeight();
		int endX = startX + width - 1;
		int endY = startY + height - 1;

		int i, j;
		for (int x = startX; x <= endX; x++) {
			i = x - startX;
			for (int y = startY; y <= endY; y++) {
				j = y - startY;
				try {
					gc.setRGB(x, y, pixels[i][j]);
				} catch (Exception exc) {
					System.out.println(error("Rest", x, y, gc, exc));
				}
			}
		}
	}

	private String error(String prefix, int x, int y, Canvas.Context gc, Exception exc) {
		StringBuilder b = new StringBuilder();
		b.append(prefix);
		b.append(": ");
		b.append(x);
		b.append(", ");
		b.append(y);
		b.append(", ");
		b.append(gc.getWidth());
		b.append(", ");
		b.append(gc.getHeight());
		b.append(", ");
		b.append(exc.getMessage());
		return b.toString();
	}

	/**
	 * Save the area currently under this composition.
	 *
	 * @param gc The graphics context.
	 */
	public void save(Canvas.Context gc) {
		if (!enabled) return;

		rect = getBounds(gc);

		int startX = (int) rect.getX();
		int startY = (int) rect.getY();
		
		int width = (int) (rect.getWidth());
		int height = (int) rect.getHeight();
		if (width < 0 || height < 0) return;
		
		int endX = startX + width - 1;
		int endY = startY + height - 1;

		pixels = new int[width][height];

		int i, j;
		for (int x = startX; x <= endX; x++) {
			i = x - startX;
			for (int y = startY; y <= endY; y++) {
				j = y - startY;
				try {
					pixels[i][j] = gc.getRGB(x, y);
				} catch (Exception exc) {
					System.out.println(error("Save", x, y, gc, exc));
				}
			}
		}
	}

	/**
	 * Enable the composition.
	 * 
	 * @param enabled A boolean.
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
