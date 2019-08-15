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

package app.mlt.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.mlt.desktop.Alert;
import com.mlt.desktop.TaskFrame;
import com.mlt.launch.Argument;
import com.mlt.launch.ArgumentManager;
import com.mlt.task.file.FileCopy;
import com.mlt.util.Files;
import com.mlt.util.Resources;
import com.mlt.util.Strings;

/**
 * Copy files with the same extension from several subdirectories to a simgle directory, enumerating the names.
 *
 * @author Miquel Sas
 */
public class CopyEnumerate {

	/**
	 * Logger configuration and text server initialization.
	 */
	static {
		Resources.addBaseTextResource("res/strings/StringsLibrary.xml");
		Locale.setDefault(Locale.US);
	}

	/**
	 * Start and launch the application.
	 * 
	 * @param args Startup arguments.
	 */
	public static void main(String[] args) {

		/* Arguments. */
		ArgumentManager argMngr = checkArguments(args);
		if (argMngr == null) {
			System.exit(1);
			return;
		}
		File srcDir = new File(argMngr.getValue("src"));
		List<String> exts = argMngr.getValues("exts");

		List<File> srcFilesRaw = null;
		try {
			srcFilesRaw = Files.list(srcDir);
		} catch (IOException ioExc) {
			ioExc.printStackTrace();
			System.exit(1);
			return;
		}
		
		TaskFrame frame = new TaskFrame();

		for (String ext : exts) {
			
			File dstDir = new File(argMngr.getValue("dst") + ext);
			
			List<File> srcFiles = new ArrayList<>();
			for (File file : srcFilesRaw) {
				String fileName = file.getName();
				String fileExt = Files.getFileExtension(fileName);
				if (fileExt.equals(ext)) {
					srcFiles.add(file);
				}
			}
			
			List<File> dstFiles = new ArrayList<>();
			for (int i = 0; i < srcFiles.size(); i++) {
				String dstName = ext.toUpperCase() + "-" + Strings.leftPad(Integer.toString(i), 4, "0") + "." + ext;
				File dstFile = new File(dstDir, dstName);
				if (dstFile.exists()) {
					dstFile.delete();
				}
				dstFiles.add(dstFile);
			}

			FileCopy fc = new FileCopy(Locale.US);
			fc.setTitle("Copy " + srcDir + " to " + dstDir);
			fc.setPurgeDestination(false);
			for (int i = 0; i < srcFiles.size(); i++) {
				fc.addFiles(srcFiles.get(i), dstFiles.get(i));
			}
			frame.addTasks(fc);
		}

		/* Open task frame. */
		frame.show();
	}

	/**
	 * Alert an error.
	 * 
	 * @param title Title.
	 * @param text  Text.
	 */
	private static void alertError(String title, String text) {
		Alert alert = new Alert();
		alert.setTitle(title);
		alert.setType(Alert.Type.ERROR);
		alert.setText(text);
		alert.show();
	}

	/**
	 * Check the launch arguments.
	 * 
	 * @param args The arguments.
	 */
	private static ArgumentManager checkArguments(String[] args) {

		/* Validate arguments. */
		ArgumentManager argMngr = new ArgumentManager();
		/* Source directory. */
		Argument argSrc = new Argument("src", "Source directory", true, false);
		/* Destination directory. */
		Argument argDst = new Argument("dst", "Destination directory", true, false);
		/* File extension. */
		Argument argExt = new Argument("exts", "File extension", true, true);

		argMngr.add(argSrc);
		argMngr.add(argDst);
		argMngr.add(argExt);

		/* Validate arguments. */
		if (!argMngr.parse(args)) {
			StringBuilder b = new StringBuilder();
			for (String error : argMngr.getErrors()) {
				b.append(error + "\n");
			}
			alertError("Argument errors", b.toString());
			return null;
		}

		/* Validate source directory. */
		String srcName = argMngr.getValue("src");
		File srcFile = new File(srcName);
		if (!srcFile.exists()) {
			alertError("Source directory does not exist", srcName);
			return null;
		}
		if (!srcFile.isDirectory()) {
			alertError("Source is not a directory", srcName);
			return null;
		}

		/* Validate destination directories. */
		List<String> exts = argMngr.getValues("exts");
		String dstName = argMngr.getValue("dst");
		for (String ext : exts) {
			String dstDirName = dstName + ext;
			File dstFile = new File(dstDirName);
			if (!dstFile.exists()) {
				alertError("Destination directory does not exist", dstDirName);
				return null;
			}
			if (!dstFile.isDirectory()) {
				alertError("Dstination is not a directory", dstDirName);
				return null;
			}
		}

		return argMngr;
	}

}
