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
package com.mlt.desktop.graphic;

import java.util.ArrayList;
import java.util.List;

import com.mlt.desktop.control.Canvas;
import com.mlt.util.Lists;
import com.mlt.util.Numbers;

/**
 * A render manages a list of drawings that have to be saved, restored or
 * rendered in a given order.
 *
 * @author Miquel Sas
 */
public class Render {

	/** The list of compositions. */
	private List<Composition> compositions = new ArrayList<>();

	/**
	 * Default constructor.
	 */
	public Render() {
		super();
	}

	/**
	 * Add a composition to the list.
	 *
	 * @param composition The composition.
	 */
	public void add(Composition composition) {
		compositions.add(composition);
	}

	/**
	 * Return the composition at the given index.
	 *
	 * @param index The index.
	 * @return The composition.
	 */
	public Composition get(int index) {
		return compositions.get(index);
	}

	/**
	 * Return the composition with the given id.
	 *
	 * @param id The id.
	 * @return The composition.
	 */
	public Composition get(String id) {
		for (Composition composition : compositions) {
			if (composition.getId().equals(id)) {
				return composition;
			}
		}
		return null;
	}

	/**
	 * Remove the composition at the given index.
	 *
	 * @param index The index.
	 */
	public void remove(int index) {
		compositions.remove(index);
	}

	/**
	 * Return the composition with the given id.
	 *
	 * @param id The id.
	 */
	public void remove(String id) {
		for (int i = 0; i < compositions.size(); i++) {
			if (compositions.get(i).getId().equals(id)) {
				compositions.remove(i);
				break;
			}
		}
	}

	/**
	 * Return the number of compositions.
	 *
	 * @return The size.
	 */
	public int size() {
		return compositions.size();
	}

	/**
	 * Constructor.
	 *
	 * @param compositions Initial list of compositions.
	 */
	public Render(Composition... compositions) {
		super();
		this.compositions.addAll(Lists.asList(compositions));
	}

	/**
	 * Paint the list of compositions (restore, save and render).
	 *
	 * @param gc The graphics context.
	 */
	public void render(Canvas.Context gc) {
		restore(gc);
		save(gc);
		paint(gc);
	}

	/**
	 * Restore the list of compositions.
	 *
	 * @param gc The graphics context.
	 */
	public void restore(Canvas.Context gc) {
		compositions.forEach(composition -> composition.restore(gc));
	}

	/**
	 * Save the list of compositions.
	 *
	 * @param gc The graphics context.
	 */
	public void save(Canvas.Context gc) {
		compositions.forEach(composition -> composition.save(gc));
	}

	/**
	 * Render the list of compositions.
	 *
	 * @param gc The graphics context.
	 */
	public void paint(Canvas.Context gc) {
		compositions.forEach(composition -> composition.paint(gc));
	}

	/**
	 * Clear saved canvas by compositions.
	 */
	public void clearSave() {
		compositions.forEach(composition -> composition.clearSave());
	}

	/**
	 * Add a draw segment.
	 *
	 * @param id      The composition id.
	 * @param drawing The drawing.
	 */
	public void addDraw(String id, Drawing drawing) {
		if (drawing == null) {
			throw new NullPointerException();
		}
		get(id).addDraw(drawing);
	}

	/**
	 * Add a draw segment.
	 *
	 * @param index   The composition index.
	 * @param drawing The drawing.
	 */
	public void addDraw(int index, Drawing drawing) {
		if (drawing == null) {
			throw new NullPointerException();
		}
		get(index).addDraw(drawing);
	}

	/**
	 * Add a fill segment.
	 *
	 * @param index   The composition index.
	 * @param drawing The drawing.
	 */
	public void addFill(int index, Drawing drawing) {
		if (drawing == null) {
			throw new NullPointerException();
		}
		get(index).addFill(drawing);
	}

	/**
	 * Add a fill segment.
	 *
	 * @param id      The composition id.
	 * @param drawing The drawing.
	 */
	public void addFill(String id, Drawing drawing) {
		if (drawing == null) {
			throw new NullPointerException();
		}
		get(id).addFill(drawing);
	}

	/**
	 * Add a paint segment.
	 *
	 * @param index   The composition index.
	 * @param drawing The drawing.
	 */
	public void addPaint(int index, Drawing drawing) {
		if (drawing == null) {
			throw new NullPointerException();
		}
		get(index).addPaint(drawing);
	}

	/**
	 * Add a paint segment.
	 *
	 * @param id      The composition id.
	 * @param drawing The drawing.
	 */
	public void addPaint(String id, Drawing drawing) {
		if (drawing == null) {
			throw new NullPointerException();
		}
		get(id).addPaint(drawing);
	}

	/**
	 * Clear this composition. This removes previous drawings but not the previously
	 * saved area.
	 *
	 * @param index The composition index.
	 */
	public void clear(int index) {
		get(index).clear();
	}

	/**
	 * Clear this composition. This removes previous drawings but not the previously
	 * saved area.
	 *
	 * @param id The composition id.
	 */
	public void clear(String id) {
		get(id).clear();
	}

	/**
	 * Return the strict bounds that encloses all the compositions.
	 * 
	 * @return The bounds of the render.
	 */
	public Rectangle getBounds() {

		double startX = Numbers.MAX_DOUBLE;
		double startY = Numbers.MAX_DOUBLE;
		double endX = Numbers.MIN_DOUBLE;
		double endY = Numbers.MIN_DOUBLE;

		for (Composition composition : compositions) {
			Rectangle r = composition.getBounds();
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
	 * Enable the composition.
	 * 
	 * @param index   The composition index.
	 * @param enabled A boolean.
	 */
	public void setEnabled(int index, boolean enabled) {
		get(index).setEnabled(enabled);
	}

	/**
	 * Enable the composition.
	 * 
	 * @param id      The composition id.
	 * @param enabled A boolean.
	 */
	public void setEnabled(String id, boolean enabled) {
		get(id).setEnabled(enabled);
	}
}
