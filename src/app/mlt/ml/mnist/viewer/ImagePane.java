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

package app.mlt.ml.mnist.viewer;

import java.awt.Color;
import java.awt.Font;

import com.mlt.desktop.border.LineBorderSides;
import com.mlt.desktop.control.GridBagPane;
import com.mlt.desktop.control.Label;
import com.mlt.desktop.graphic.Stroke;
import com.mlt.desktop.layout.Alignment;
import com.mlt.desktop.layout.Anchor;
import com.mlt.desktop.layout.Constraints;
import com.mlt.desktop.layout.Fill;
import com.mlt.desktop.layout.Insets;

/**
 * An image source. Has a status bar to show image information and an image canvas. Provides the way to produce the
 * image data and information.
 *
 * @author Miquel Sas
 */
public class ImagePane extends GridBagPane {

	/** Label. */
	private Label label;
	/** Image canvas. */
	private ImageCanvas canvas;
	/** Image source. */
	private ImageSource source;

	/**
	 * Constructor.
	 * 
	 * @param source The image source.
	 */
	public ImagePane(ImageSource source) {
		super();

		this.source = source;

		label = new Label();
		label.setBorder(new LineBorderSides(Color.LIGHT_GRAY, new Stroke(), true, false, true, false));
		label.setHorizontalAlignment(Alignment.CENTER);
		label.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
		add(label, new Constraints(Anchor.TOP, Fill.HORIZONTAL, 0, 0, 1, 1, 1, 0, new Insets(0, 0, 0, 0)));

		canvas = new ImageCanvas(this.source.getRows(), this.source.getColumns());
		canvas.setDrawGrid(true);
		canvas.setLineColor(new Color(192, 192, 160));
		canvas.setLineWidth(1f);
		canvas.setDrawFactor(0.9);
		add(canvas, new Constraints(Anchor.TOP, Fill.BOTH, 0, 1, 1, 1, 1, 1, new Insets(0, 0, 0, 0)));
	}

	/**
	 * Return the image canvas.
	 * 
	 * @return The image canvas.
	 */
	protected ImageCanvas getCanvas() {
		return canvas;
	}

	/**
	 * Return the image source.
	 * 
	 * @return The image source.
	 */
	public ImageSource getSource() {
		return source;
	}

	/**
	 * Request to paint the image and display its information.
	 */
	public void paintImage() {
		canvas.paint(source.getImage());
		label.setText(source.getInformation());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return source.getInformation();
	}
}
