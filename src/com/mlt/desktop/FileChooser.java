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

package com.mlt.desktop;

import java.awt.Component;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;

import com.mlt.util.Resources;

/**
 * File chooser extension.
 *
 * @author Miquel Sas
 */
public class FileChooser extends JFileChooser {

	/**
	 * Constructor.
	 */
	public FileChooser() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param currentDirectoryPath The current path.
	 */
	public FileChooser(String currentDirectoryPath) {
		super(currentDirectoryPath);
	}

	/**
	 * Constructor.
	 * 
	 * @param currentDirectory The current path.
	 */
	public FileChooser(File currentDirectory) {
		super(currentDirectory);
	}

	/**
	 * Check if selection should be approved before.
	 */
	@Override
	public void approveSelection() {
		List<File> files = getCheckFiles();
		for (File file : files) {

			// Check that the file name conforms the parameterized filters.
			if (!accept(file)) {
				String error = Resources.getText("fileChooserInvalidFileName");
				Alert.error(MessageFormat.format(error, file.getName()));
				return;
			}

			// File option: open. Check that file exists.
			if (getDialogType() == OPEN_DIALOG) {
				if (!file.exists()) {
					String error = Resources.getText("fileChooserOpenNotExists");
					Alert.error(MessageFormat.format(error, file.getName()));
					return;
				}
			}

			// File option save, confirm replace.
			if (getDialogType() == SAVE_DIALOG) {
				if (file.exists()) {
					String title = Resources.getText("fileChooserConfirmSaveTitle");
					String text =
						MessageFormat.format(Resources.getText("fileChooserConfirmSaveText"), file.getName());
					if (!Option.isOk(Alert.confirm(text, title))) {
						return;
					}
				}
			}
		}
		super.approveSelection();
	}

	/**
	 * Returns the list of files to check for approval.
	 * 
	 * @return The list of files.
	 */
	private List<File> getCheckFiles() {
		List<File> files = new ArrayList<>();
		if (getSelectedFile() != null) {
			files.add(getSelectedFile());
		}
		if (getSelectedFiles() != null) {
			for (File file : getSelectedFiles()) {
				if (!files.contains(file)) {
					files.add(file);
				}
			}
		}
		return files;
	}
	/**
	 * RunTickers the dialog using the parameterized dialog type.
	 * 
	 * @return The return state.
	 */
	public int showDialog() {
		return showDialog(null);
	}

	/**
	 * RunTickers the dialog using the parameterized dialog type.
	 * 
	 * @param parent The parent component or null.
	 * @return The return state.
	 */
	public int showDialog(Component parent) {
		return super.showDialog(parent, null);
	}

}
