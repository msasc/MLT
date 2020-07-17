/*
 * Copyright (C) 2018 Miquel Sas
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package app.mlt.cma;

import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.KeyStroke;
import javax.swing.tree.TreePath;

import com.mlt.desktop.Alert;
import com.mlt.desktop.Option;
import com.mlt.desktop.OptionWindow;
import com.mlt.desktop.TaskFrame;
import com.mlt.desktop.control.CheckBox;
import com.mlt.desktop.control.Frame;
import com.mlt.desktop.control.GridBagPane;
import com.mlt.desktop.control.Tree;
import com.mlt.desktop.control.tree.TreeItemNode;
import com.mlt.desktop.event.MouseHandler;
import com.mlt.launch.Argument;
import com.mlt.launch.ArgumentManager;
import com.mlt.task.Task;
import com.mlt.task.file.FileCopy;
import com.mlt.util.Resources;

/**
 * CMA updater with detail directory/file selection for remote updates.
 * 
 * @author Miquel Sas
 */
public class CMAUpdate {

	/** IO information. */
	class FileIO {

		Type type;
		String srcParent;
		String dstParent;
		String[] names;
		File srcFile;
		File dstFile;
		List<FileIO> children = new ArrayList<>();
		boolean selected = false;

		File getSrcFile() {
			return srcFile;
		}

		File getDstFile(String drive) {
			return new File(drive + dstFile.getPath());
		}

		boolean isDirectory() {
			return type == Type.DIRECTORY;
		}

		boolean isFile() {
			return type == Type.FILE;
		}

		@Override
		public String toString() {
			StringBuilder b = new StringBuilder();
			b.append("[");
			b.append(type);
			b.append("]");
			b.append(",");
			b.append(" [");
			b.append(srcFile);
			b.append("]");
			b.append(",");
			b.append(" [");
			String[] drives = getDrives();
			for (int i = 0; i < drives.length; i++) {
				if (i > 0) {
					b.append(", ");
				}
				b.append(drives[i]);
			}
			b.append("]");
			b.append("[");
			b.append(dstFile);
			b.append("]");
			return b.toString();
		}

	}

	/** Mouse listener. */
	class MouseListener extends MouseHandler {

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 1) {
				TreePath path = tree.getPathForLocation(e.getX(), e.getY());
				if (path != null) {
					TreeItemNode parent = (TreeItemNode) path.getLastPathComponent();

					List<TreeItemNode> nodes = new ArrayList<>();
					nodes.add(parent);
					fillChildren(nodes, parent);

					boolean selected = !isSelected(parent);
					for (TreeItemNode node : nodes) {
						setSelected(node, selected);
						tree.getModel().nodeChanged(node);
					}
				}
			}
			super.mouseClicked(e);
		}

	}

	/** Type (file or directory) */
	enum Type {
		DIRECTORY, FILE
	}

	/** Logger configuration and text server initialization. */
	static {
		Resources.addBaseTextResource("res/strings/StringsLibrary.xml");
		Locale.setDefault(Locale.US);
	}

	/**
	 * @param args Startup arguments.
	 */
	public static void main(String[] args) {
		CMAUpdate cma = new CMAUpdate();
		cma.setup(args);
	}

	private ArgumentManager argMngr;
	private Tree tree;
	private TreeItemNode root;
	private Font fontPlain = new Font(Font.DIALOG, Font.PLAIN, 16);
	private Font fontBold = new Font(Font.DIALOG, Font.BOLD, 16);

	private CMAUpdate() {
		argMngr = new ArgumentManager();
		root = new TreeItemNode();
	}

	private void checkArguments(String[] args) {

		/* Environment: Quality/Production. */
		Argument argEnv = new Argument("environment", "Environment: quality/production", true, false, "development", "quality", "production");

		/* Target: Local/Remote */
		Argument argTarget = new Argument("target", "Target: local/remote", true, false, "local", "remote");

		/* Destination (without drive). */
		Argument argDest = new Argument("destination", "Destination root", true, true, false);

		/* Drives. */
		Argument argDrives = new Argument("drives", "Drives", true, true, false);

		/* Source. */
		Argument argSrc = new Argument("source", "Source root", true, true, false);

		argMngr.add(argEnv);
		argMngr.add(argTarget);
		argMngr.add(argDest);
		argMngr.add(argDrives);
		argMngr.add(argSrc);

		/* Validate arguments. */
		if (!argMngr.parse(args)) {
			Alert alert = new Alert();
			alert.setTitle("Argument errors");
			alert.setType(Alert.Type.ERROR);
			StringBuilder b = new StringBuilder();
			for (String error : argMngr.getErrors()) {
				b.append(error + "\n");
			}
			alert.setText(b.toString());
			alert.show();
			System.exit(0);
			return;
		}

		/* Validate destination. */
		String[] drives = getDrives();
		for (String drive : drives) {
			StringBuilder dst = new StringBuilder();
			dst.append(drive);
			dst.append(argMngr.getValue("destination"));
			File dest = new File(dst.toString());
			if (!dest.exists()) {
				Alert alert = new Alert();
				alert.setTitle("Local destination does not exist");
				alert.setType(Alert.Type.ERROR);
				alert.setText(dest.toString());
				alert.show();
				System.exit(0);
				return;
			}
		}

		/* Validate source. */
		File src = new File(argMngr.getValue("source"));
		if (!src.exists()) {
			Alert alert = new Alert();
			alert.setTitle("Source directory does not exist");
			alert.setType(Alert.Type.ERROR);
			alert.setText(src.toString());
			alert.show();
			System.exit(0);
			return;
		}
	}

	private void fillChildren(List<TreeItemNode> children, TreeItemNode parent) {
		for (int i = 0; i < parent.getChildCount(); i++) {
			TreeItemNode child = parent.getChildAt(i);
			children.add(child);
			fillChildren(children, child);
		}
	}

	private void fillFiles(List<FileIO> ios, FileIO parent) {
		ios.add(parent);
		if (parent.isDirectory()) {
			File[] files = parent.srcFile.listFiles();
			String srcParent = parent.srcFile.getPath();
			String dstParent = parent.dstFile.getPath();
			for (File file : files) {
				if (file.isDirectory()) {
					String name = file.getName();
					FileIO child = new FileIO();
					child.type = Type.DIRECTORY;
					child.srcParent = srcParent;
					child.dstParent = dstParent;
					child.names = new String[] { name };
					child.srcFile = new File(srcParent, name);
					child.dstFile = new File(dstParent, name);
					fillFiles(parent.children, child);
				}
			}
			for (File file : files) {
				if (file.isFile()) {
					String name = file.getName();
					FileIO child = new FileIO();
					child.type = Type.FILE;
					child.srcParent = srcParent;
					child.dstParent = dstParent;
					child.names = new String[] { name };
					child.srcFile = new File(srcParent, name);
					child.dstFile = new File(dstParent, name);
					fillFiles(parent.children, child);
				}
			}
		}
	}

	/**
	 * @param ios The list of IO files.
	 */
	private void fillTree(List<FileIO> ios, TreeItemNode parent) {
		for (FileIO io : ios) {
			TreeItemNode child = new TreeItemNode();
			child.setUserObject(io);
			child.add("CHK", new CheckBox());
			if (io.isDirectory()) {
				child.add("SRC", io.srcFile.getPath(), fontBold);
			} else {
				child.add("SRC", io.srcFile.getPath(), fontPlain);
			}
//			child.add("DST", io.dstFile.getPath(), font);
			setSelected(child, true);
			parent.add(child);
			if (io.isDirectory()) {
				fillTree(io.children, child);
			}
		}
	}

	/**
	 * @param node The node.
	 * @return The checkbox in the node.
	 */
	private CheckBox getCheckBox(TreeItemNode node) {
		return (CheckBox) node.getControl("CHK");
	}

	private String[] getDrives() {
		String dest = argMngr.getValue("drives");
		String[] drives = new String[dest.length()];
		for (int i = 0; i < dest.length(); i++) {
			drives[i] = String.valueOf(dest.charAt(i)) + ":";
		}
		return drives;
	}

	private FileIO getIO(
		Type type,
		String srcParent,
		String dstParent,
		String... names) {

		StringBuilder fileName = new StringBuilder();
		for (int i = 0; i < names.length; i++) {
			if (i > 0) {
				fileName.append(File.separator);
			}
			fileName.append(names[i]);
		}

		FileIO io = new FileIO();
		io.type = type;
		io.srcParent = srcParent;
		io.dstParent = dstParent;
		io.names = names;
		io.srcFile = new File(new File(srcParent), fileName.toString());
		io.dstFile = new File(new File(dstParent), fileName.toString());
		return io;
	}

	/**
	 * @param node The node.
	 * @return The IO user object.
	 */
	private FileIO getIOFile(TreeItemNode node) {
		return (FileIO) node.getUserObject();
	}

	private List<FileIO> getIOFiles() {

		String srcParent, dstParent, srcModule, dstModule, filePrefix;

		List<FileIO> iosTmp = new ArrayList<>();

		/* Library. */
		srcParent = getParentSrc(argMngr.getValue("source"), "XVR COM Lib", "library");
		dstParent = getParentDst(argMngr.getValue("destination"), "library");
		iosTmp.add(getIO(Type.DIRECTORY, srcParent, dstParent, "bin"));
		iosTmp.add(getIO(Type.DIRECTORY, srcParent, dstParent, "res"));
		iosTmp.add(getIO(Type.DIRECTORY, srcParent, dstParent, "xsd"));
		iosTmp.add(getIO(Type.DIRECTORY, srcParent, dstParent, "xml"));

		/* Budget dictionary. */
		srcModule = "XVR COM Module Budget Dictionary";
		dstModule = "module_budget_dictionary";
		filePrefix = "Budget_Dictionary";
		iosTmp.addAll(getIOFiles(srcModule, dstModule, filePrefix));

		/* Budget local. */
		srcModule = "XVR COM Module Budget Local";
		dstModule = "module_budget_local";
		filePrefix = "Budget_Local";
		iosTmp.addAll(getIOFiles(srcModule, dstModule, filePrefix, "CMA_Local_Menu.xml"));

		/* Margins central. */
		srcModule = "XVR COM Module Margins Central";
		dstModule = "module_margins_central";
		filePrefix = "Margins_Central";
		iosTmp.addAll(getIOFiles(srcModule, dstModule, filePrefix, "CMA_Central_Menu.xml"));

		/* Margins dictionary. */
		srcModule = "XVR COM Module Margins Dictionary";
		dstModule = "module_margins_dictionary";
		filePrefix = "Margins_Dictionary";
		iosTmp.addAll(getIOFiles(srcModule, dstModule, filePrefix, "Margins_Dictionary_Menu.xml"));

		/* Margins library. */
		srcModule = "XVR COM Module Margins Library";
		dstModule = "module_margins_library";
		filePrefix = "Margins_Library";
		iosTmp.addAll(getIOFiles(srcModule, dstModule, filePrefix));

		/* Margins local. */
		srcModule = "XVR COM Module Margins Local";
		dstModule = "module_margins_local";
		filePrefix = "Margins_Local";
		iosTmp.addAll(getIOFiles(srcModule, dstModule, filePrefix));

		/* Seguridad. */
		srcModule = "XVR COM Module Seguridad";
		dstModule = "module_security";
		filePrefix = "Seguridad";
		iosTmp.addAll(getIOFiles(srcModule, dstModule, filePrefix));

		/* Strategic plan central. */
		srcModule = "XVR COM Module StrategicPlan Central";
		dstModule = "module_stplan_central";
		filePrefix = "StrategicPlan_Central";
		iosTmp.addAll(getIOFiles(srcModule, dstModule, filePrefix));

		/* Strategic plan local. */
		srcModule = "XVR COM Module StrategicPlan Local";
		dstModule = "module_stplan_local";
		filePrefix = "StrategicPlan_Local";
		iosTmp.addAll(getIOFiles(srcModule, dstModule, filePrefix));

		/* Working capital central. */
		srcModule = "XVR COM Module WorkingCapital Central";
		dstModule = "module_wcapital_central";
		filePrefix = "WorkingCapital_Central";
		iosTmp.addAll(getIOFiles(srcModule, dstModule, filePrefix));

		/* Working capital local. */
		srcModule = "XVR COM Module WorkingCapital Local";
		dstModule = "module_wcapital_local";
		filePrefix = "WorkingCapital_Local";
		iosTmp.addAll(getIOFiles(srcModule, dstModule, filePrefix));

		/* Final list. */
		List<FileIO> ios = new ArrayList<>();
		for (FileIO io : iosTmp) {
			fillFiles(ios, io);
		}

		return ios;
	}

	private List<FileIO> getIOFiles(String srcModule, String dstModule, String filePrefix) {
		return getIOFiles(srcModule, dstModule, filePrefix, null);
	}

	private List<FileIO> getIOFiles(String srcModule, String dstModule, String filePrefix, String menuFile) {

		String srcRoot = argMngr.getValue("source");
		String dstRoot = argMngr.getValue("destination");

		String srcParent = getParentSrc(srcRoot, srcModule, dstModule);
		String dstParent = getParentDst(dstRoot, dstModule);

		List<FileIO> ios = new ArrayList<>();

		ios.add(getIO(Type.DIRECTORY, srcParent, dstParent, "bin"));
		ios.add(getIO(Type.FILE, srcParent, dstParent, "res", filePrefix + "_DBSchema.txt"));
		ios.add(getIO(Type.FILE, srcParent, dstParent, "res", filePrefix + "_Descriptor.txt"));
		ios.add(getIO(Type.FILE, srcParent, dstParent, "res", filePrefix + "_Domains.txt"));
		ios.add(getIO(Type.FILE, srcParent, dstParent, "res", filePrefix + "_Strings.txt"));
		ios.add(getIO(Type.FILE, srcParent, dstParent, "xml", filePrefix + "_DBSchema.xml"));
		ios.add(getIO(Type.FILE, srcParent, dstParent, "xml", filePrefix + "_Descriptor.xml"));
		ios.add(getIO(Type.FILE, srcParent, dstParent, "xml", filePrefix + "_Domains.xml"));
		ios.add(getIO(Type.FILE, srcParent, dstParent, "xml", filePrefix + "_Strings.xml"));

		if (menuFile != null) {
			ios.add(getIO(Type.FILE, srcParent, dstParent, "xml", menuFile));
		}

		return ios;
	}

	private String getParentDst(String dstRoot, String module) {
		StringBuilder b = new StringBuilder();
		b.append(dstRoot);
		b.append(File.separator);
		b.append("mads");
		b.append(File.separator);
		b.append(module);
		return b.toString();
	}

	private String getParentSrc(String srcRoot, String local, String remote) {
		StringBuilder b = new StringBuilder();
		b.append(srcRoot);
		b.append(File.separator);
		if (argMngr.getValue("target").equals("local")) {
			b.append(local);
		} else {
			b.append("mads");
			b.append(File.separator);
			b.append(remote);
		}
		return b.toString();
	}

	/**
	 * @param node The node.
	 * @return a boolean.
	 */
	private boolean isSelected(TreeItemNode node) {
		return getIOFile(node).selected;
	}

	/**
	 * @param node     The node.
	 * @param selected A boolean.
	 */
	private void setSelected(TreeItemNode node, boolean selected) {
		getIOFile(node).selected = selected;
		getCheckBox(node).setSelected(selected);
	}

	/**
	 * @param args Startup arguments.
	 */
	private void setup(String[] args) {

		/* Parse arguments. */
		checkArguments(args);

		/* Get IO filesand setup the root node. */
		List<FileIO> ios = getIOFiles();
		root.removeAllChildren();
		fillTree(ios, root);

		OptionWindow wnd = new OptionWindow(new Frame(new GridBagPane()));
		wnd.setTitle("Select to copy");
		wnd.setOptionsBottom();

		tree = new Tree();
		tree.addMouseListener(new MouseListener());
		tree.setFont(new Font(Font.DIALOG, Font.PLAIN, 16));
		tree.setRoot(root);
		tree.setRootVisible(false);
		wnd.setCenter(tree);

		Option selectAll = new Option();
		selectAll.setKey("SELECT_ALL");
		selectAll.setText("Select all");
		selectAll.setToolTip("Select all nodes");
		selectAll.setAction(listener -> {
			List<TreeItemNode> nodes = new ArrayList<>();
			fillChildren(nodes, root);
			for (TreeItemNode node : nodes) {
				setSelected(node, true);
				tree.getModel().nodeChanged(node);
			}
		});

		Option clearAll = new Option();
		clearAll.setKey("CLEAR_ALL");
		clearAll.setText("Clear all");
		clearAll.setToolTip("Clear all nodes");
		clearAll.setAction(listener -> {
			List<TreeItemNode> nodes = new ArrayList<>();
			fillChildren(nodes, root);
			for (TreeItemNode node : nodes) {
				setSelected(node, false);
				tree.getModel().nodeChanged(node);
			}
		});

		Option update = new Option();
		update.setKey("UPDATE");
		update.setText("Update");
		update.setToolTip("Update");
		update.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
		update.setAction(listener -> {
			wnd.close();
			update();
		});

		Option cancel = new Option();
		cancel.setKey("CANCEL");
		cancel.setText("Cancel");
		cancel.setToolTip("Cancel update");
		cancel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
		cancel.setDefaultClose(true);
		cancel.setAction(listener -> {
			wnd.close();
			System.exit(0);
		});

		wnd.getOptionPane().add(selectAll, clearAll, update, cancel);

		wnd.pack();
		wnd.setSize(0.9, 0.9);
		wnd.centerOnScreen();
		wnd.show();
	}

	private void update() {

		List<TreeItemNode> nodes = new ArrayList<>();
		fillChildren(nodes, root);
		for (int i = nodes.size() - 1; i >= 0; i--) {
			if (!nodes.get(i).isLeaf() || !isSelected(nodes.get(i))) {
				nodes.remove(i);
			}
		}

		int groupSize = 200;
		List<List<TreeItemNode>> groups = new ArrayList<>();
		List<TreeItemNode> groupTmp = new ArrayList<>();
		for (TreeItemNode node : nodes) {
			groupTmp.add(node);
			if (groupTmp.size() >= groupSize) {
				groups.add(new ArrayList<>(groupTmp));
				groupTmp.clear();
			}
		}
		if (!groupTmp.isEmpty()) {
			groups.add(new ArrayList<>(groupTmp));
			groupTmp.clear();
		}

		String src = argMngr.getValue("source");
		String dst = argMngr.getValue("destination");
		String[] drives = getDrives();
		
		List<Task> fcs = new ArrayList<>();
		for (int i = 0; i < drives.length; i++) {
			
			String drive = drives[i];
			for (int j = 0; j < groups.size(); j++) {
				
				StringBuilder title = new StringBuilder();
				title.append("Copy [");
				title.append(src);
				title.append("] to [");
				title.append(drive);
				title.append(dst);
				title.append("] - ");
				title.append(j);
				
				FileCopy fc = new FileCopy(Locale.US);
				fc.setTitle(title.toString());
				fc.setPurgeDestination(false);
			
				for (TreeItemNode node : groups.get(j)) {
					FileIO io = getIOFile(node);
					File srcFile = io.getSrcFile();
					File dstFile = io.getDstFile(drive);
					fc.addFiles(srcFile, dstFile);
				}
				fcs.add(fc);
			}
		}

		TaskFrame frame = new TaskFrame();
		frame.addTasks(fcs);
		frame.show();
	}

	private void updateOld() {

		List<TreeItemNode> nodes = new ArrayList<>();
		fillChildren(nodes, root);
		for (int i = nodes.size() - 1; i >= 0; i--) {
			if (!nodes.get(i).isLeaf() || !isSelected(nodes.get(i))) {
				nodes.remove(i);
			}
		}

		String src = argMngr.getValue("source");
		String dst = argMngr.getValue("destination");
		String[] drives = getDrives();

		FileCopy[] fcs = new FileCopy[drives.length];
		for (int i = 0; i < drives.length; i++) {

			String drive = drives[i];

			StringBuilder title = new StringBuilder();
			title.append("Copy [");
			title.append(src);
			title.append("] to [");
			title.append(drive);
			title.append(dst);
			title.append("]");

			FileCopy fc = new FileCopy(Locale.US);
			fc.setTitle(title.toString());
			fc.setPurgeDestination(false);

			for (TreeItemNode node : nodes) {
				FileIO io = getIOFile(node);
				File srcFile = io.getSrcFile();
				File dstFile = io.getDstFile(drive);
				fc.addFiles(srcFile, dstFile);
			}
			fcs[i] = fc;
		}

		TaskFrame frame = new TaskFrame();
		frame.addTasks(fcs);
		frame.show();
	}
}
