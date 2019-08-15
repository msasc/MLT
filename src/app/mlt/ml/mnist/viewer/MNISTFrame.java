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

package app.mlt.ml.mnist.viewer;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.mlt.desktop.AWT;
import com.mlt.desktop.Alert;
import com.mlt.desktop.FileChooser;
import com.mlt.desktop.Option;
import com.mlt.desktop.border.LineBorderSides;
import com.mlt.desktop.control.BorderPane;
import com.mlt.desktop.control.Control;
import com.mlt.desktop.control.Frame;
import com.mlt.desktop.control.GridBagPane;
import com.mlt.desktop.control.GridPane;
import com.mlt.desktop.control.OptionPane;
import com.mlt.desktop.control.ScrollPane;
import com.mlt.desktop.control.SplitPane;
import com.mlt.desktop.control.Stage;
import com.mlt.desktop.control.StatusBar;
import com.mlt.desktop.event.ComponentHandler;
import com.mlt.desktop.event.KeyHandler;
import com.mlt.desktop.event.MouseHandler;
import com.mlt.desktop.graphic.Stroke;
import com.mlt.desktop.layout.Anchor;
import com.mlt.desktop.layout.Constraints;
import com.mlt.desktop.layout.Dimension;
import com.mlt.desktop.layout.Fill;
import com.mlt.desktop.layout.Insets;
import com.mlt.desktop.layout.Orientation;
import com.mlt.ml.data.ListPatternSource;
import com.mlt.ml.data.Pattern;
import com.mlt.ml.data.PatternSource;
import com.mlt.ml.data.mnist.MNIST;
import com.mlt.ml.data.mnist.NumberImagePattern;
import com.mlt.ml.network.Network;
import com.mlt.ml.network.Node;
import com.mlt.ml.network.nodes.Filter2DNode;
import com.mlt.util.Logs;

import app.mlt.ml.mnist.viewer.source.SourceFilter2D;
import app.mlt.ml.mnist.viewer.source.SourceMNIST;
import app.mlt.ml.mnist.viewer.source.SourceTransferNode;

/**
 * The MNIST database and networks viewer frame.
 *
 * @author Miquel Sas
 */
public class MNISTFrame extends Frame {
	/**
	 * Action select MNIST database.
	 */
	class ActionDatabase implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				/* Query database. */
				Option train = new Option();
				train.setKey("TRAIN");
				train.setText("Train");
				train.setCloseWindow(true);
				Option test = new Option();
				test.setKey("TEST");
				test.setText("Test");
				test.setCloseWindow(true);
				Option cancel = new Option();
				cancel.setKey("CANCEL");
				cancel.setText("Cancel");
				cancel.setDefaultClose(true);
				cancel.setCloseWindow(true);
				Alert alert = new Alert();
				alert.setTitle("MNIST database");
				alert.setText("Please select the MNIST database");
				alert.setOptions(train, test, cancel);
				Option result = alert.show();
				if (result.equals(cancel)) {
					return;
				}
				if (result.equals(train)) {
					allImages = MNIST.getSourceTrain();
				}
				if (result.equals(test)) {
					allImages = MNIST.getSourceTest();
				}
				images = allImages;
				imageIndex = 0;
			} catch (Exception exc) {
				Logs.catching(exc);
				System.exit(-1);
			}
			optionPane.clear();
			optionPane.add(getOptionDatabase());
			optionPane.add(getOptionNetwork());
			addFilterNumberOptions();
			installListeners();
			paintImage();
		}
	}

	/**
	 * Action filter off.
	 */
	class ActionFilterOff implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			filterSet = false;
			applyFilters();
		}

	}

	/**
	 * Action filter.
	 */
	class ActionFilterOn implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			filterSet = true;
			applyFilters();
		}
	}

	/**
	 * Action filter.
	 */
	class ActionFilterNumber implements ActionListener {
		int number;

		ActionFilterNumber(int number) {
			this.number = number;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			filterNumber = number;
			applyFilters();
		}
	}

	/**
	 * Action network.
	 */
	class ActionNetwork implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {

			/* Check network files. */
			File file = null;
			File path = new File("res/network/");
			if (path.exists()) {
				File[] files = path.listFiles();
				if (files != null && files.length > 0) {
					/* Select a network file. */
					FileChooser chooser = new FileChooser(path);
					chooser.setDialogTitle("Please, select a network file");
					chooser.setDialogType(FileChooser.OPEN_DIALOG);
					chooser.setFileSelectionMode(FileChooser.FILES_ONLY);
					chooser.setAcceptAllFileFilterUsed(true);
					chooser.setFileFilter(new FileNameExtensionFilter("DAT files", "dat"));
					if (chooser.showDialog(null) == FileChooser.APPROVE_OPTION) {
						file = chooser.getSelectedFile();
					}
				}
			}
			if (file != null) {
				networkFile = file;
				new ActionFilterOff().actionPerformed(e);
				new Thread(new NetworkLoader()).start();
				optionPane.clear();
				optionPane.add(getOptionDatabase(), getOptionNetwork());
				optionPane.getOptions().forEach(option -> option.getButton().setEnabled(false));
			}
		}

	}

	/**
	 * Close operation.
	 */
	class FrameListener extends Stage.Adapter {
		@Override
		public void closing(Stage stage) {
			System.exit(0);
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
				if (imageIndex < images.size() - 1) {
					imageIndex++;
				}
			}
			if (keyCode == KeyEvent.VK_UP) {
				if (imageIndex > 0) {
					imageIndex--;
				}
			}
			if (keyCode == KeyEvent.VK_LEFT) {
				if (imageIndex > 0) {
					imageIndex--;
				}
			}
			if (keyCode == KeyEvent.VK_RIGHT) {
				if (imageIndex < images.size() - 1) {
					imageIndex++;
				}
			}
			if (keyCode == KeyEvent.VK_PAGE_UP) {
				if (imageIndex > 0) {
					imageIndex -= (imageIndex == 0 ? pageSize - 1 : pageSize);
					if (imageIndex < 0) {
						imageIndex = 0;
					}
				}
			}
			if (keyCode == KeyEvent.VK_PAGE_DOWN) {
				if (imageIndex < images.size() - 1) {
					imageIndex += (imageIndex == 0 ? pageSize - 1 : pageSize);
					if (imageIndex >= images.size()) {
						imageIndex = images.size() - 1;
					}
				}
			}
			if (keyCode == KeyEvent.VK_HOME) {
				imageIndex = 0;
			}
			if (keyCode == KeyEvent.VK_END) {
				imageIndex = images.size() - 1;
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
			imageIndex += move;
			if (imageIndex >= images.size()) {
				imageIndex = images.size() - 1;
			}
			if (imageIndex < 0) {
				imageIndex = 0;
			}
			paintImage();
		}
	}

	/**
	 * Network thread.
	 */
	class NetworkLoader implements Runnable {
		@Override
		public void run() {
			try {

				/* Cancel filters. */
				filterSet = false;
				filterNumber = -1;

				/*
				 * Load the network, normally so fast that it's nice to sleep to show a message.
				 */
				statusBar.clearStatusBar();
				statusBar.setLabel("NET", "Restoring the netwotk " + networkFile.getName());
				network = new Network();
				FileInputStream fi = new FileInputStream(networkFile);
				BufferedInputStream bi = new BufferedInputStream(fi);
				network.restore(bi);
				bi.close();
				fi.close();
				Thread.sleep(500);
				statusBar.removeLabel("NET");

				/* Process the list of images with the network. */
				for (int i = 0; i < images.size(); i++) {
					statusBar.setProgress("NET", "Processing images", i, images.size());
					NumberImagePattern pattern = (NumberImagePattern) images.get(i);
					double[] networkOutput = network.calculate(pattern.getInputValues());
					int number = NumberImagePattern.getNumber(networkOutput);
					pattern.getProperties().setInteger("NET-OUT", number);
				}

				/* Fill the list of right panes. */
				fillRightPanes();

				/* Layout images again. */
				layoutImages();

				statusBar.clearStatusBar();
				displayImageInfo();

				optionPane.clear();
				optionPane.add(getOptionDatabase());
				optionPane.add(getOptionNetwork());
				optionPane.add(getOptionFilterOn());
				addFilterNumberOptions();
				installListeners();
				getContent().revalidate();

			} catch (Exception exc) {
				exc.printStackTrace();
				System.exit(1);
			}
		}
	}

	/**
	 * Scroll pane resize listener.
	 */
	class ScrollPaneHandler extends ComponentHandler {
		@Override
		public void componentResized(ComponentEvent e) {

			Object source = e.getSource();
			if (!(source instanceof JScrollPane)) {
				return;
			}

			ScrollPane scrollPane = (ScrollPane) Control.getControl((JScrollPane) source);
			Dimension size = scrollPane.getSize();
			double width = size.getWidth();
			double height = width / 2;
			GridPane gridRight = (GridPane) scrollPane.getView();
			for (int row = 0; row < gridRight.getRows(); row++) {
				GridPane gridRow = (GridPane) gridRight.getControl(row, 0);
				gridRow.setSize(new Dimension(width, height));
				double imgPaneWidth = (width / gridRow.getColumns()) * 0.98;
				for (int col = 0; col < gridRow.getColumns(); col++) {
					ImagePane imgPane = (ImagePane) gridRow.getControl(0, col);
					imgPane.setPreferredSize(new Dimension(imgPaneWidth, height));
				}
			}
		}
	}

	/** Status bar (border pane bottom). */
	private StatusBar statusBar;
	/** Option pane. (border pane bottom). */
	private OptionPane optionPane;

	/** Pattern source with all images, no filtering. */
	private PatternSource allImages;
	/** Patter source currently displayed. */
	private PatternSource images;
	/** Image index. */
	private int imageIndex;

	/** Network file is selected. */
	private File networkFile;
	/** Network if selected. */
	private Network network;

	/** Key listener. */
	private KeyListener keyListener;
	/** Mouse listener. */
	private MouseListener mouseListener;

	/** Image pane for main MNIST images. */
	private ImagePane leftPane;

	/** Image panes for transfer nodes on the right. */
	private List<List<ImagePane>> rightPanes = new ArrayList<>();

	/** A boolean that indicates if the network error filter has been set. */
	private boolean filterSet = false;
	/** The number to filter data, -1 any. */
	private int filterNumber = -1;

	/**
	 * Constructor.
	 */
	public MNISTFrame() {
		super(new BorderPane());

		setTitle("Number image viewer");
		addWindowListener(new FrameListener());

		keyListener = new KeyListener();
		mouseListener = new MouseListener();

		statusBar = new StatusBar();
		statusBar.setBorder(
			new LineBorderSides(Color.LIGHT_GRAY, new Stroke(), true, false, true, false));
		statusBar.setProgressBorder(new LineBorder(Color.LIGHT_GRAY));
		statusBar.setPreferredSize(statusBar.getPreferredSize());
		optionPane = new OptionPane(Orientation.HORIZONTAL);
		optionPane.add(getOptionDatabase());

		GridBagPane pane = new GridBagPane();
		pane.add(statusBar,
			new Constraints(Anchor.BOTTOM, Fill.HORIZONTAL, 0, 0, new Insets(0, 0, 0, 0)));
		pane.add(optionPane,
			new Constraints(Anchor.BOTTOM, Fill.HORIZONTAL, 0, 1, new Insets(0, 0, 0, 0)));
		getBorderPane().setBottom(pane);

		layoutImages();
	}

	/**
	 * add filter number options.
	 */
	private void addFilterNumberOptions() {
		for (int i = 0; i < 10; i++) {
			optionPane.add(getOptionFilterNumber(i));
		}
		optionPane.add(getOptionFilterNumber(-1));
	}

	/**
	 * Apply filters.
	 */
	private void applyFilters() {
		List<Pattern> patterns = new ArrayList<>();
		for (int i = 0; i < allImages.size(); i++) {
			NumberImagePattern pattern = (NumberImagePattern) allImages.get(i);
			int patternNumber = pattern.getImage().getNumber();
			int networkNumber = pattern.getProperties().getInteger("NET-OUT");
			boolean add = true;
			if (filterSet && patternNumber == networkNumber) {
				add = false;
			}
			if (filterNumber >= 0 && filterNumber != patternNumber) {
				add = false;
			}
			if (add) {
				patterns.add(pattern);
			}
		}
		images = new ListPatternSource(patterns);
		imageIndex = 0;

		optionPane.clear();
		optionPane.add(getOptionDatabase());
		optionPane.add(getOptionNetwork());
		if (network != null) {
			if (filterSet) {
				optionPane.add(getOptionFilterOff());
			} else {
				optionPane.add(getOptionFilterOn());
			}
		}
		addFilterNumberOptions();
		installListeners();
		paintImage();
		getContent().revalidate();
	}

	/**
	 * Display the selected image information in the status bar.
	 */
	private void displayImageInfo() {
		StringBuilder text = new StringBuilder();
		text.append((imageIndex + 1) + " of " + images.size());
		statusBar.setLabel("IMG", text.toString());
	}

	/**
	 * Fill the list of image panes with transfer nodes on the right.
	 */
	private void fillRightPanes() {

		/* Build a list with transfer nodes. */
		List<Node> allNodes = network.getNodes();
		List<Node> transferNodes = new ArrayList<>();
		for (Node node : allNodes) {
			if (node.isTransfer()) {
				transferNodes.add(node);
			}
		}

		/* Build a list with filter nodes. */
		List<Node> filterNodes = new ArrayList<>();
		for (Node node : transferNodes) {
			if (node instanceof Filter2DNode) {
				filterNodes.add(node);
			}
		}

		/* Remove the filter nodes from the list of transfer nodes. */
		transferNodes.removeAll(filterNodes);

		/* The list of right nodes to build the list of right panes. */
		List<List<Node>> rightNodes = new ArrayList<>();

		/* Add the filter nodes and the output node if it is a pool. */
		for (Node filter : filterNodes) {
			List<Node> nodes = new ArrayList<>();
			nodes.add(filter);
			rightNodes.add(nodes);
		}

		/* Add the rest of transfer nodes. */
		for (Node transfer : transferNodes) {
			List<Node> nodes = new ArrayList<>();
			nodes.add(transfer);
			rightNodes.add(nodes);
		}

		/* Build the final list of right panes. */
		rightPanes.clear();
		for (List<Node> nodes : rightNodes) {
			List<ImagePane> panes = new ArrayList<>();
			for (Node node : nodes) {
				if (node instanceof Filter2DNode) {
					SourceFilter2D source = new SourceFilter2D(node);
					panes.add(new ImagePane(source));
				}
				SourceTransferNode source = new SourceTransferNode(node, node.isOutput());
				panes.add(new ImagePane(source));
			}
			rightPanes.add(panes);
		}
	}

	/**
	 * Install key and mouse listeners.
	 */
	private void installListeners() {
		AWT.installKeyListener(leftPane, keyListener);
		AWT.removeMouseWheelListener(leftPane, mouseListener);
		AWT.addMouseWheelListener(leftPane, mouseListener);
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
	 * Return an option to select the database.
	 * 
	 * @return The option.
	 */
	private Option getOptionDatabase() {
		Option option = new Option();
		option.setKey("DATABASE");
		option.setText("Select the MNIST database");
		option.setToolTip("Select the MNIST images database");
		option.setAction(new ActionDatabase());
		return option;
	}

	/**
	 * Return the option to clear the filter of images.
	 * 
	 * @return The option.
	 */
	private Option getOptionFilterOff() {
		Option option = new Option();
		option.setKey("FILTER-OFF");
		option.setText("Clear filter");
		option.setToolTip("Clear the filter that shows only unmatched images");
		option.setAction(new ActionFilterOff());
		return option;
	}

	/**
	 * Return the option to filter images.
	 * 
	 * @return The option.
	 */
	private Option getOptionFilterOn() {
		Option option = new Option();
		option.setKey("FILTER-ON");
		option.setText("Set filter");
		option.setToolTip("Filter images with output error");
		option.setAction(new ActionFilterOn());
		return option;
	}

	/**
	 * Return the option to filter number.
	 * 
	 * @return The option.
	 */
	private Option getOptionFilterNumber(int number) {
		Option option = new Option();
		option.setKey("FILTER-" + number);
		String text = (number >= 0 ? Integer.toString(number) : "Any");
		option.setText(text);
		String tip =
			(number >= 0 ? "Filter images with the number " + number : "Clear number filters");
		option.setToolTip(tip);
		option.setAction(new ActionFilterNumber(number));
		return option;
	}

	/**
	 * Return an option to select the network.
	 * 
	 * @return The option.
	 */
	private Option getOptionNetwork() {
		Option option = new Option();
		option.setKey("NETWORK");
		option.setText("Select network");
		option.setToolTip("Select the network to apply to images");
		option.setAction(new ActionNetwork());
		return option;
	}

	/**
	 * Layout the images.
	 */
	private void layoutImages() {

		/* No transfer sources, the single MNIST image. */
		if (rightPanes.isEmpty()) {
			leftPane = new ImagePane(new SourceMNIST());
			getBorderPane().setCenter(leftPane);
			paintImage();
			return;
		}
		getBorderPane().removeCenter();

		/*
		 * Transfer nodes, the main MNIST image uses half the available area, and the
		 * other half is used by a scroll
		 * pane to show the list of outputs of the transfer nodes.
		 */
		Dimension size = getContent().getSize();
		double width = size.getWidth() * 0.45;
		double height = size.getHeight() * 0.95;

		SplitPane splitPane = new SplitPane(Orientation.HORIZONTAL);

		leftPane = new ImagePane(new SourceMNIST());
		leftPane.setPreferredSize(new Dimension(width, height));
		splitPane.setLeftControl(leftPane);

		ScrollPane scrollPane = new ScrollPane();
		scrollPane.setBorder(null);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		scrollPane.addComponentListener(new ScrollPaneHandler());

		GridPane gridPaneRight = new GridPane(rightPanes.size(), 1);
		for (int row = 0; row < rightPanes.size(); row++) {
			List<ImagePane> rowPanes = rightPanes.get(row);
			double paneSize = width / rowPanes.size();
			GridPane gridPaneRow = new GridPane(1, rowPanes.size());
			for (int col = 0; col < rowPanes.size(); col++) {
				ImagePane pane = rowPanes.get(col);
				pane.setPreferredSize(new Dimension(paneSize, paneSize));
				gridPaneRow.setControl(0, col, pane);
			}
			gridPaneRight.setControl(row, 0, gridPaneRow);
		}

		scrollPane.setView(gridPaneRight);
		splitPane.setRightControl(scrollPane);
		splitPane.setContinuousLayout(true);
		splitPane.resetToPreferredSizes();

		getBorderPane().setCenter(splitPane);
		getBorderPane().revalidate();
		getBorderPane().repaint();
		paintImage();
	}

	/**
	 * Request the canvas to repaint the selected image.
	 */
	private void paintImage() {
		if (images == null) {
			return;
		}
		SourceMNIST source = (SourceMNIST) leftPane.getSource();
		if (images.isEmpty()) {
			source.setPattern(null);
		} else {
			source.setPattern((NumberImagePattern) images.get(imageIndex));
		}
		leftPane.paintImage();
		displayImageInfo();

		if (!rightPanes.isEmpty() && !images.isEmpty()) {
			double[] inputValues = images.get(imageIndex).getInputValues();
			network.forward(inputValues);
			for (List<ImagePane> panes : rightPanes) {
				for (ImagePane pane : panes) {
					pane.paintImage();
				}
			}
		}
	}
}
