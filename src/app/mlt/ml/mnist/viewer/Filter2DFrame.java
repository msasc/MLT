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

import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;

import com.mlt.desktop.AWT;
import com.mlt.desktop.control.BorderPane;
import com.mlt.desktop.control.Frame;
import com.mlt.desktop.control.Stage;
import com.mlt.desktop.event.KeyHandler;
import com.mlt.desktop.event.MouseHandler;
import com.mlt.ml.network.nodes.Filter2DNode;

import app.mlt.ml.mnist.viewer.source.SourceFilter2D;

/**
 * A frame to view 2D filters.
 *
 * @author Miquel Sas
 */
public class Filter2DFrame extends Frame {

	/**
	 * Close operation.
	 */
	class FrameListener extends Stage.Adapter {
		@Override
		public void closing(Stage stage) {
			System.exit(0);
		}

		@Override
		public void opened(Stage stage) {
			if (!filterPanes.isEmpty()) {
				filterIndex = 0;
			}
			paintImage();
		}
	}
	
	/**
	 * Key listener.
	 */
	class KeyListener extends KeyHandler {
		int pageSize = 100;

		@Override
		public void keyPressed(KeyEvent e) {
			int keyCode = e.getKeyCode();
			if (keyCode == KeyEvent.VK_ESCAPE) {
				System.exit(0);
			}
			if (keyCode == KeyEvent.VK_DOWN) {
				if (filterIndex < filterPanes.size() - 1) {
					filterIndex++;
				}
			}
			if (keyCode == KeyEvent.VK_UP) {
				if (filterIndex > 0) {
					filterIndex--;
				}
			}
			if (keyCode == KeyEvent.VK_LEFT) {
				if (filterIndex > 0) {
					filterIndex--;
				}
			}
			if (keyCode == KeyEvent.VK_RIGHT) {
				if (filterIndex < filterPanes.size() - 1) {
					filterIndex++;
				}
			}
			if (keyCode == KeyEvent.VK_HOME) {
				filterIndex = 0;
			}
			if (keyCode == KeyEvent.VK_END) {
				filterIndex = filterPanes.size() - 1;
			}
			paintImage();
		}
	}

	/**
	 * Mouse listener.
	 */
	class MouseListener extends MouseHandler {
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			int move = e.getWheelRotation();
			filterIndex += move;
			if (filterIndex >= filterPanes.size()) {
				filterIndex = filterPanes.size() - 1;
			}
			if (filterIndex < 0) {
				filterIndex = 0;
			}
			paintImage();
		}
	}


	/** List of filters (on image panes). */
	private List<ImagePane> filterPanes = new ArrayList<>();
	/** Selected filter pane. */
	private int filterIndex = -1;

	/**
	 * Constructor.
	 */
	public Filter2DFrame() {
		super(new BorderPane());

		setTitle("Filter image viewer");
		
		FrameListener frameListener = new FrameListener();
		KeyListener keyListener = new KeyListener();
		MouseListener mouseListener = new MouseListener();
		
		addWindowListener(frameListener);
		AWT.addKeyListener(getBorderPane(), keyListener);
		AWT.addMouseWheelListener(getBorderPane(), mouseListener);
	}

	/**
	 * Add a filter node.
	 * 
	 * @param filterNode The filter node.
	 */
	public void addFilter(Filter2DNode filterNode) {
		SourceFilter2D source = new SourceFilter2D(filterNode);
		ImagePane filterPane = new ImagePane(source);
		filterPanes.add(filterPane);
	}

	/**
	 * Return the content border pane.
	 * 
	 * @return The content border pane.
	 */
	private BorderPane getBorderPane() {
		return (BorderPane) getContent();
	}
	
	/**
	 * Request the canvas to repaint the selected filter image.
	 */
	private void paintImage() {
		if (filterIndex < 0 || filterPanes.isEmpty()) {
			return;
		}
		getBorderPane().setCenter(filterPanes.get(filterIndex));
		filterPanes.get(filterIndex).paintImage();
		getBorderPane().revalidate();
	}

}
