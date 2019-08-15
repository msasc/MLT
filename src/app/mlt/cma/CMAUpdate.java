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

package app.mlt.cma;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.mlt.desktop.Alert;
import com.mlt.desktop.TaskFrame;
import com.mlt.launch.Argument;
import com.mlt.launch.ArgumentManager;
import com.mlt.task.Task;
import com.mlt.task.file.FileCopy;
import com.mlt.util.Resources;
import com.mlt.util.Strings;

/**
 * CMA Updater.
 *
 * @author Miquel Sas
 */
public class CMAUpdate {

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
		ArgumentManager argMngr = checkArguments(args);
		List<Task> tasks = getTaskList(argMngr);
		TaskFrame frame = new TaskFrame();
		frame.addTasks(tasks);
		frame.show();
	}

	/**
	 * Returns the list of tasks to update.
	 * 
	 * @param argMngr The argument manager.
	 * @return The list of tasks.
	 */
	private static List<Task> getTaskList(ArgumentManager argMngr) {
		List<Task> tasks = new ArrayList<>();
		boolean purge = argMngr.isPassed("purge");
		String srcRoot = argMngr.getValue("source");

		/* Local: copy from workspace to execution image. */
		if (argMngr.getValue("target").equals("local")) {
			String dstRoot = argMngr.getValue("destination");
			addTasks(argMngr, tasks, srcRoot, dstRoot, purge);
		}

		/* Remote: copy from execution image to drives. */
		if (argMngr.getValue("target").equals("remote")) {
			String[] drives = getDrives(argMngr);
			for (String dstRoot : drives) {
				addTasks(argMngr, tasks, srcRoot, dstRoot, purge);
			}
		}

		for (int i = 0; i < tasks.size(); i++) {
			String title = (i + 1) + "- " + tasks.get(i).getTitle();
			tasks.get(i).setTitle(title);
		}

		return tasks;
	}

	/**
	 * Add the required list of tasks.
	 * 
	 * @param argMngr Argument manager.
	 * @param tasks   Tasks.
	 * @param srcRoot Source root.
	 * @param dstRoot Destination root
	 * @param purge   A boolean.
	 */
	private static void addTasks(ArgumentManager argMngr, List<Task> tasks, String srcRoot, String dstRoot,
		boolean purge) {
		/* Central. */
		if (argMngr.getValues("modules").contains("central")) {
			FileCopy fc = new FileCopy(Locale.US);
			fc.setTitle(getDescription(argMngr, "central"));
			fc.setPurgeDestination(purge);
			addLibrary(fc, srcRoot, dstRoot, "CMA_Central");
			addModuleBudgetDictionary(fc, srcRoot, dstRoot, "CMA_Central");
			addModuleBudgetLocal(fc, srcRoot, dstRoot, "CMA_Central", false);
			addModuleMarginsCentral(fc, srcRoot, dstRoot, "CMA_Central", true);
			addModuleMarginsDictionary(fc, srcRoot, dstRoot, "CMA_Central", false);
			addModuleMarginsLibrary(fc, srcRoot, dstRoot, "CMA_Central");
			addModuleMarginsLocal(fc, srcRoot, dstRoot, "CMA_Central");
			addModuleStrategicPlanCentral(fc, srcRoot, dstRoot, "CMA_Central");
			addModuleStrategicPlanLocal(fc, srcRoot, dstRoot, "CMA_Central");
			addModuleWorkingCapitalCentral(fc, srcRoot, dstRoot, "CMA_Central");
			addModuleWorkingCapitalLocal(fc, srcRoot, dstRoot, "CMA_Central");
			addModuleSecurity(fc, srcRoot, dstRoot, "CMA_Central");
			tasks.add(fc);
		}
		/* Dictionary. */
		if (argMngr.getValues("modules").contains("dictionary")) {
			FileCopy fc = new FileCopy(Locale.US);
			fc.setTitle(getDescription(argMngr, "dictionary"));
			fc.setPurgeDestination(purge);
			addLibrary(fc, srcRoot, dstRoot, "CMA_Dictionary");
			addModuleBudgetDictionary(fc, srcRoot, dstRoot, "CMA_Dictionary");
			addModuleMarginsCentral(fc, srcRoot, dstRoot, "CMA_Dictionary", false);
			addModuleMarginsDictionary(fc, srcRoot, dstRoot, "CMA_Dictionary", true);
			addModuleMarginsLibrary(fc, srcRoot, dstRoot, "CMA_Dictionary");
			addModuleMarginsLocal(fc, srcRoot, dstRoot, "CMA_Dictionary");
			addModuleSecurity(fc, srcRoot, dstRoot, "CMA_Dictionary");
			tasks.add(fc);
		}
		/* Local. */
		if (argMngr.getValues("modules").contains("local")) {
			FileCopy fc = new FileCopy(Locale.US);
			fc.setTitle(getDescription(argMngr, "local"));
			fc.setPurgeDestination(purge);
			addLibrary(fc, srcRoot, dstRoot, "CMA_Local");
			addModuleBudgetDictionary(fc, srcRoot, dstRoot, "CMA_Local");
			addModuleBudgetLocal(fc, srcRoot, dstRoot, "CMA_Local", true);
			addModuleMarginsCentral(fc, srcRoot, dstRoot, "CMA_Local", false);
			addModuleMarginsDictionary(fc, srcRoot, dstRoot, "CMA_Local", false);
			addModuleMarginsLibrary(fc, srcRoot, dstRoot, "CMA_Local");
			addModuleMarginsLocal(fc, srcRoot, dstRoot, "CMA_Local");
			addModuleStrategicPlanLocal(fc, srcRoot, dstRoot, "CMA_Local");
			addModuleWorkingCapitalCentral(fc, srcRoot, dstRoot, "CMA_Local");
			addModuleWorkingCapitalLocal(fc, srcRoot, dstRoot, "CMA_Local");
			addModuleSecurity(fc, srcRoot, dstRoot, "CMA_Local");
			tasks.add(fc);
		}
	}

	/**
	 * Returns a suitable description.
	 * 
	 * @param argMngr The argument manager
	 * @param module  Module (central/dictionary/local)
	 * @return The name.
	 */
	private static String getDescription(ArgumentManager argMngr, String module) {
		StringBuilder b = new StringBuilder();
		b.append("Copy task");
		b.append(" [environment: ");
		b.append(argMngr.getValue("environment"));
		b.append("] [target: ");
		b.append(argMngr.getValue("target"));
		b.append("] [module: " + module);
		b.append("]");
		return b.toString();
	}

	private static String getSrcParent(String workspaceDir, String srcRoot, String dstRoot, String dstParent) {
		if (dstRoot.length() > 2) {
			return srcRoot + workspaceDir;
		}
		return Strings.replace(dstParent, dstRoot, srcRoot);
	}

	/**
	 * Add the local library copy task
	 * 
	 * @param fc      File copy.
	 * @param srcRoot Source root.
	 * @param dstRoot Destination root.
	 * @param module  Module (CMA_Central/CMA_Dictionary/CMA_Local)
	 */
	private static void addLibrary(FileCopy fc, String srcRoot, String dstRoot, String module) {
		String dstParent = dstRoot + "\\" + module + "\\mads\\library";
		String srcParent = getSrcParent("\\XVR COM Lib", srcRoot, dstRoot, dstParent);
		addDirectory(fc, srcParent, dstParent, "bin");
		addDirectory(fc, srcParent, dstParent, "res");
		addDirectory(fc, srcParent, dstParent, "xsd");
		addDirectory(fc, srcParent, dstParent, "xml");
	}

	/**
	 * Add the local budget dictionary module copy task.
	 * 
	 * @param fc      File copy.
	 * @param srcRoot Source root.
	 * @param dstRoot Destination root.
	 * @param module  Module (CMA_Central/CMA_Dictionary/CMA_Local)
	 */
	private static void addModuleBudgetDictionary(FileCopy fc, String srcRoot, String dstRoot, String module) {
		String dstParent = dstRoot + "\\" + module + "\\mads\\module_budget_dictionary";
		String srcParent = getSrcParent("\\XVR COM Module Budget Dictionary", srcRoot, dstRoot, dstParent);
		// bin
		addDirectory(fc, srcParent, dstParent, "bin");
		// res
		addFile(fc, srcParent, dstParent, "res\\Budget_Dictionary_DBSchema.txt");
		addFile(fc, srcParent, dstParent, "res\\Budget_Dictionary_Descriptor.txt");
		addFile(fc, srcParent, dstParent, "res\\Budget_Dictionary_Domains.txt");
		addFile(fc, srcParent, dstParent, "res\\Budget_Dictionary_Strings.txt");
		// xml
		addFile(fc, srcParent, dstParent, "xml\\Budget_Dictionary_DBSchema.xml");
		addFile(fc, srcParent, dstParent, "xml\\Budget_Dictionary_Descriptor.xml");
		addFile(fc, srcParent, dstParent, "xml\\Budget_Dictionary_Domains.xml");
		addFile(fc, srcParent, dstParent, "xml\\Budget_Dictionary_Strings.xml");
	}

	/**
	 * Add the local budget local module copy task.
	 * 
	 * @param fc      File copy.
	 * @param srcRoot Source root.
	 * @param dstRoot Destination root.
	 * @param module  Module (CMA_Central/CMA_Dictionary/CMA_Local)
	 * @param menu    A boolean to indicate if the menu file should be copied.
	 */
	private static void addModuleBudgetLocal(FileCopy fc, String srcRoot, String dstRoot, String module, boolean menu) {
		String dstParent = dstRoot + "\\" + module + "\\mads\\module_budget_local";
		String srcParent = getSrcParent("\\XVR COM Module Budget Local", srcRoot, dstRoot, dstParent);
		// bin
		addDirectory(fc, srcParent, dstParent, "bin");
		// res
		addFile(fc, srcParent, dstParent, "res\\Budget_Local_DBSchema.txt");
		addFile(fc, srcParent, dstParent, "res\\Budget_Local_Descriptor.txt");
		addFile(fc, srcParent, dstParent, "res\\Budget_Local_Domains.txt");
		addFile(fc, srcParent, dstParent, "res\\Budget_Local_Strings.txt");
		// xml
		addFile(fc, srcParent, dstParent, "xml\\Budget_Local_DBSchema.xml");
		addFile(fc, srcParent, dstParent, "xml\\Budget_Local_Descriptor.xml");
		addFile(fc, srcParent, dstParent, "xml\\Budget_Local_Domains.xml");
		addFile(fc, srcParent, dstParent, "xml\\Budget_Local_Strings.xml");
		if (menu) {
			addFile(fc, srcParent, dstParent, "xml\\CMA_Local_Menu.xml");
		}
	}

	/**
	 * Add the local margins central module copy task.
	 * 
	 * @param fc      File copy.
	 * @param srcRoot Source root.
	 * @param dstRoot Destination root.
	 * @param module  Module (CMA_Central/CMA_Dictionary/CMA_Local)
	 * @param menu    A boolean to indicate if the menu file should be copied.
	 */
	private static void addModuleMarginsCentral(
		FileCopy fc, String srcRoot, String dstRoot, String module, boolean menu) {
		
		String dstParent = dstRoot + "\\" + module + "\\mads\\module_margins_central";
		String srcParent = getSrcParent("\\XVR COM Module Margins Central", srcRoot, dstRoot, dstParent);
		// bin
		addDirectory(fc, srcParent, dstParent, "bin");
		// res
		addFile(fc, srcParent, dstParent, "res\\Margins_Central_DBSchema.txt");
		addFile(fc, srcParent, dstParent, "res\\Margins_Central_Descriptor.txt");
		addFile(fc, srcParent, dstParent, "res\\Margins_Central_Domains.txt");
		addFile(fc, srcParent, dstParent, "res\\Margins_Central_Strings.txt");
		// xml
		addFile(fc, srcParent, dstParent, "xml\\Margins_Central_DBSchema.xml");
		addFile(fc, srcParent, dstParent, "xml\\Margins_Central_Descriptor.xml");
		addFile(fc, srcParent, dstParent, "xml\\Margins_Central_Domains.xml");
		addFile(fc, srcParent, dstParent, "xml\\Margins_Central_Strings.xml");
		if (menu) {
			addFile(fc, srcParent, dstParent, "xml\\CMA_Central_Menu.xml");
		}
	}

	/**
	 * Add the local margins dictionary module copy task.
	 * 
	 * @param fc      File copy.
	 * @param srcRoot Source root.
	 * @param dstRoot Destination root.
	 * @param module  Module (CMA_Central/CMA_Dictionary/CMA_Local)
	 * @param menu    A boolean to indicate if the menu file should be copied.
	 */
	private static void addModuleMarginsDictionary(
		FileCopy fc, String srcRoot, String dstRoot, String module, boolean menu) {
		String dstParent = dstRoot + "\\" + module + "\\mads\\module_margins_dictionary";
		String srcParent = getSrcParent("\\XVR COM Module Margins Dictionary", srcRoot, dstRoot, dstParent);
		// bin
		addDirectory(fc, srcParent, dstParent, "bin");
		// res
		addFile(fc, srcParent, dstParent, "res\\Margins_Dictionary_DBSchema.txt");
		addFile(fc, srcParent, dstParent, "res\\Margins_Dictionary_Descriptor.txt");
		addFile(fc, srcParent, dstParent, "res\\Margins_Dictionary_Domains.txt");
		addFile(fc, srcParent, dstParent, "res\\Margins_Dictionary_Strings.txt");
		// xml
		addFile(fc, srcParent, dstParent, "xml\\Margins_Dictionary_DBSchema.xml");
		addFile(fc, srcParent, dstParent, "xml\\Margins_Dictionary_Descriptor.xml");
		addFile(fc, srcParent, dstParent, "xml\\Margins_Dictionary_Domains.xml");
		addFile(fc, srcParent, dstParent, "xml\\Margins_Dictionary_Strings.xml");
		if (menu) {
			addFile(fc, srcParent, dstParent, "xml\\Margins_Dictionary_Menu.xml");
		}
	}

	/**
	 * Add the local margins library module copy task.
	 * 
	 * @param fc      File copy.
	 * @param srcRoot Source root.
	 * @param dstRoot Destination root.
	 * @param module  Module (CMA_Central/CMA_Dictionary/CMA_Local)
	 */
	private static void addModuleMarginsLibrary(FileCopy fc, String srcRoot, String dstRoot, String module) {
		String dstParent = dstRoot + "\\" + module + "\\mads\\module_margins_library";
		String srcParent = getSrcParent("\\XVR COM Module Margins Library", srcRoot, dstRoot, dstParent);
		// bin
		addDirectory(fc, srcParent, dstParent, "bin");
		// res
		addFile(fc, srcParent, dstParent, "res\\Margins_Library_DBSchema.txt");
		addFile(fc, srcParent, dstParent, "res\\Margins_Library_Descriptor.txt");
		addFile(fc, srcParent, dstParent, "res\\Margins_Library_Domains.txt");
		addFile(fc, srcParent, dstParent, "res\\Margins_Library_Strings.txt");
		// xml
		addFile(fc, srcParent, dstParent, "xml\\Margins_Library_DBSchema.xml");
		addFile(fc, srcParent, dstParent, "xml\\Margins_Library_Descriptor.xml");
		addFile(fc, srcParent, dstParent, "xml\\Margins_Library_Domains.xml");
		addFile(fc, srcParent, dstParent, "xml\\Margins_Library_Strings.xml");
	}

	/**
	 * Add the local margins local module copy task.
	 * 
	 * @param fc      File copy.
	 * @param srcRoot Source root.
	 * @param dstRoot Destination root.
	 * @param module  Module (CMA_Central/CMA_Dictionary/CMA_Local)
	 */
	private static void addModuleMarginsLocal(FileCopy fc, String srcRoot, String dstRoot, String module) {
		String dstParent = dstRoot + "\\" + module + "\\mads\\module_margins_local";
		String srcParent = getSrcParent("\\XVR COM Module Margins Local", srcRoot, dstRoot, dstParent);
		// bin
		addDirectory(fc, srcParent, dstParent, "bin");
		// res
		addFile(fc, srcParent, dstParent, "res\\Margins_Local_DBSchema.txt");
		addFile(fc, srcParent, dstParent, "res\\Margins_Local_Descriptor.txt");
		addFile(fc, srcParent, dstParent, "res\\Margins_Local_Domains.txt");
		addFile(fc, srcParent, dstParent, "res\\Margins_Local_Strings.txt");
		// xml
		addFile(fc, srcParent, dstParent, "xml\\Margins_Local_DBSchema.xml");
		addFile(fc, srcParent, dstParent, "xml\\Margins_Local_Descriptor.xml");
		addFile(fc, srcParent, dstParent, "xml\\Margins_Local_Domains.xml");
		addFile(fc, srcParent, dstParent, "xml\\Margins_Local_Strings.xml");
	}

	/**
	 * Add the local security module copy task.
	 * 
	 * @param fc      File copy.
	 * @param srcRoot Source root.
	 * @param dstRoot Destination root.
	 * @param module  Module (CMA_Central/CMA_Dictionary/CMA_Local)
	 */
	private static void addModuleSecurity(FileCopy fc, String srcRoot, String dstRoot, String module) {
		String dstParent = dstRoot + "\\" + module + "\\mads\\module_security";
		String srcParent = getSrcParent("\\XVR COM Module Seguridad", srcRoot, dstRoot, dstParent);
		// bin
		addDirectory(fc, srcParent, dstParent, "bin");
		// res
		addFile(fc, srcParent, dstParent, "res\\Module_Seguridad_DBSchema.txt");
		addFile(fc, srcParent, dstParent, "res\\Module_Seguridad_DBSchema_en.txt");
		addFile(fc, srcParent, dstParent, "res\\Module_Seguridad_Descriptor.txt");
		addFile(fc, srcParent, dstParent, "res\\Module_Seguridad_Descriptor_en.txt");
		addFile(fc, srcParent, dstParent, "res\\Module_Seguridad_Strings.txt");
		addFile(fc, srcParent, dstParent, "res\\Module_Seguridad_Strings_en.txt");
		addFile(fc, srcParent, dstParent, "res\\Seguridad_DBSchema.txt");
		addFile(fc, srcParent, dstParent, "res\\Seguridad_DBSchema_en.txt");
		addFile(fc, srcParent, dstParent, "res\\Seguridad_DBSchema_es.txt");
		addFile(fc, srcParent, dstParent, "res\\Seguridad_Descriptor.txt");
		addFile(fc, srcParent, dstParent, "res\\Seguridad_Descriptor_en.txt");
		addFile(fc, srcParent, dstParent, "res\\Seguridad_Descriptor_es.txt");
		addFile(fc, srcParent, dstParent, "res\\Seguridad_Domains.txt");
		addFile(fc, srcParent, dstParent, "res\\Seguridad_Strings.txt");
		// xml
		addFile(fc, srcParent, dstParent, "xml\\Module_Seguridad_Descriptor.xml");
		addFile(fc, srcParent, dstParent, "xml\\Seguridad_DBSchema.xml");
		addFile(fc, srcParent, dstParent, "xml\\Seguridad_Descriptor.xml");
		addFile(fc, srcParent, dstParent, "xml\\Seguridad_Domains.xml");
		addFile(fc, srcParent, dstParent, "xml\\Seguridad_Strings.xml");
	}

	/**
	 * Add the local strategic plan central module copy task.
	 * 
	 * @param fc      File copy.
	 * @param srcRoot Source root.
	 * @param dstRoot Destination root.
	 * @param module  Module (CMA_Central/CMA_Dictionary/CMA_Local)
	 */
	private static void addModuleStrategicPlanCentral(FileCopy fc, String srcRoot, String dstRoot, String module) {
		String dstParent = dstRoot + "\\" + module + "\\mads\\module_stplan_central";
		String srcParent = getSrcParent("\\XVR COM Module StrategicPlan Central", srcRoot, dstRoot, dstParent);
		// bin
		addDirectory(fc, srcParent, dstParent, "bin");
		// res
		addFile(fc, srcParent, dstParent, "res\\StrategicPlan_Central_DBSchema.txt");
		addFile(fc, srcParent, dstParent, "res\\StrategicPlan_Central_Descriptor.txt");
		addFile(fc, srcParent, dstParent, "res\\StrategicPlan_Central_Domains.txt");
		addFile(fc, srcParent, dstParent, "res\\StrategicPlan_Central_Strings.txt");
		// xml
		addFile(fc, srcParent, dstParent, "xml\\StrategicPlan_Central_DBSchema.xml");
		addFile(fc, srcParent, dstParent, "xml\\StrategicPlan_Central_Descriptor.xml");
		addFile(fc, srcParent, dstParent, "xml\\StrategicPlan_Central_Domains.xml");
		addFile(fc, srcParent, dstParent, "xml\\StrategicPlan_Central_Strings.xml");
	}

	/**
	 * Add the local strategic plan local module copy task.
	 * 
	 * @param fc      File copy.
	 * @param srcRoot Source root.
	 * @param dstRoot Destination root.
	 * @param module  Module (CMA_Central/CMA_Dictionary/CMA_Local)
	 */
	private static void addModuleStrategicPlanLocal(FileCopy fc, String srcRoot, String dstRoot, String module) {
		String dstParent = dstRoot + "\\" + module + "\\mads\\module_stplan_local";
		String srcParent = getSrcParent("\\XVR COM Module StrategicPlan Local", srcRoot, dstRoot, dstParent);
		// bin
		addDirectory(fc, srcParent, dstParent, "bin");
		// res
		addFile(fc, srcParent, dstParent, "res\\StrategicPlan_Local_DBSchema.txt");
		addFile(fc, srcParent, dstParent, "res\\StrategicPlan_Local_Descriptor.txt");
		addFile(fc, srcParent, dstParent, "res\\StrategicPlan_Local_Domains.txt");
		addFile(fc, srcParent, dstParent, "res\\StrategicPlan_Local_Strings.txt");
		// xml
		addFile(fc, srcParent, dstParent, "xml\\StrategicPlan_Local_DBSchema.xml");
		addFile(fc, srcParent, dstParent, "xml\\StrategicPlan_Local_Descriptor.xml");
		addFile(fc, srcParent, dstParent, "xml\\StrategicPlan_Local_Domains.xml");
		addFile(fc, srcParent, dstParent, "xml\\StrategicPlan_Local_Strings.xml");
	}

	/**
	 * Add the local working capital central module copy task.
	 * 
	 * @param fc      File copy.
	 * @param srcRoot Source root.
	 * @param dstRoot Destination root.
	 * @param module  Module (CMA_Central/CMA_Dictionary/CMA_Local)
	 */
	private static void addModuleWorkingCapitalCentral(FileCopy fc, String srcRoot, String dstRoot, String module) {
		String dstParent = dstRoot + "\\" + module + "\\mads\\module_wcapital_central";
		String srcParent = getSrcParent("\\XVR COM Module WorkingCapital Central", srcRoot, dstRoot, dstParent);
		// bin
		addDirectory(fc, srcParent, dstParent, "bin");
		// res
		addFile(fc, srcParent, dstParent, "res\\WorkingCapital_Central_DBSchema.txt");
		addFile(fc, srcParent, dstParent, "res\\WorkingCapital_Central_Descriptor.txt");
		addFile(fc, srcParent, dstParent, "res\\WorkingCapital_Central_Domains.txt");
		addFile(fc, srcParent, dstParent, "res\\WorkingCapital_Central_Strings.txt");
		// xml
		addFile(fc, srcParent, dstParent, "xml\\WorkingCapital_Central_DBSchema.xml");
		addFile(fc, srcParent, dstParent, "xml\\WorkingCapital_Central_Descriptor.xml");
		addFile(fc, srcParent, dstParent, "xml\\WorkingCapital_Central_Domains.xml");
		addFile(fc, srcParent, dstParent, "xml\\WorkingCapital_Central_Strings.xml");
	}

	/**
	 * Add the local working capital local module copy task.
	 * 
	 * @param fc      File copy.
	 * @param srcRoot Source root.
	 * @param dstRoot Destination root.
	 * @param module  Module (CMA_Central/CMA_Dictionary/CMA_Local)
	 */
	private static void addModuleWorkingCapitalLocal(FileCopy fc, String srcRoot, String dstRoot, String module) {
		String dstParent = dstRoot + "\\" + module + "\\mads\\module_wcapital_local";
		String srcParent = getSrcParent("\\XVR COM Module WorkingCapital Local", srcRoot, dstRoot, dstParent);
		// bin
		addDirectory(fc, srcParent, dstParent, "bin");
		// res
		addFile(fc, srcParent, dstParent, "res\\WorkingCapital_Local_DBSchema.txt");
		addFile(fc, srcParent, dstParent, "res\\WorkingCapital_Local_Descriptor.txt");
		addFile(fc, srcParent, dstParent, "res\\WorkingCapital_Local_Domains.txt");
		addFile(fc, srcParent, dstParent, "res\\WorkingCapital_Local_Strings.txt");
		// xml
		addFile(fc, srcParent, dstParent, "xml\\WorkingCapital_Local_DBSchema.xml");
		addFile(fc, srcParent, dstParent, "xml\\WorkingCapital_Local_Descriptor.xml");
		addFile(fc, srcParent, dstParent, "xml\\WorkingCapital_Local_Domains.xml");
		addFile(fc, srcParent, dstParent, "xml\\WorkingCapital_Local_Strings.xml");
	}

	/**
	 * Add a directory to the file copy task.
	 * 
	 * @param fc        The file copy task
	 * @param srcParent Source parent directory.
	 * @param dstParent Destination parent.
	 * @param fileName  File name.
	 */
	private static void addDirectory(
		FileCopy fc,
		String srcParent,
		String dstParent,
		String fileName) {
		addFileOrDirectory(fc, srcParent, dstParent, fileName, true);
	}

	/**
	 * Add a file to the file copy task.
	 * 
	 * @param fc        The file copy task
	 * @param srcParent Source parent directory.
	 * @param dstParent Destination parent.
	 * @param fileName  File name.
	 */
	private static void addFile(
		FileCopy fc,
		String srcParent,
		String dstParent,
		String fileName) {
		addFileOrDirectory(fc, srcParent, dstParent, fileName, false);
	}

	/**
	 * Add a file or directory to the file copy task.
	 * 
	 * @param fc        The file copy task
	 * @param srcParent Source parent directory.
	 * @param dstParent Destination parent.
	 * @param fileName  File name.
	 * @param directory A boolean that indicated whether adding a file or directory.
	 */
	private static void addFileOrDirectory(
		FileCopy fc,
		String srcParent,
		String dstParent,
		String fileName,
		boolean directory) {
		File fileSrc = new File(new File(srcParent), fileName);
		File fileDst = new File(new File(dstParent), fileName);
		if (directory) {
			fc.addDirectories(fileSrc, fileDst);
		} else {
			fc.addFiles(fileSrc, fileDst);
		}
	}

	/**
	 * Return the list of drives for a remote destination.
	 * 
	 * @param argMngr The argument manager.
	 * @return The list of drives.
	 */
	private static String[] getDrives(ArgumentManager argMngr) {
		String dest = argMngr.getValue("destination");
		String[] drives = new String[dest.length()];
		for (int i = 0; i < dest.length(); i++) {
			drives[i] = String.valueOf(dest.charAt(i)) + ":";
		}
		return drives;
	}

	/**
	 * Check the launch arguments.
	 * 
	 * @param args The arguments.
	 */
	private static ArgumentManager checkArguments(String[] args) {

		/* Validate arguments. */
		ArgumentManager argMngr = new ArgumentManager();
		/* Environment: Quality/Production. */
		Argument argEnv = new Argument("environment", "Environment: quality/qroduction", true, false, "quality", "production");
		/* Target: Local/Remote */
		Argument argTarget = new Argument("target", "Target: local/remote", true, false, "local", "remote");
		/*
		 * Destination, based on target.
		 * - If Local, indicate a directory like "C:\Development\Eclipse-Workspaces\Roca\cma-head"
		 * - If Remote, indicate a list of drives like "IJKLMN"
		 */
		Argument argDest = new Argument("destination", "Destination, depends on target", true, true, false);
		/*
		 * Source, based on environment and target.
		 * - If quality and local indicate the development root directory for quality
		 * - If quality and remote indicate the image root directory for quality
		 * Similar for production.
		 */
		Argument argSrc = new Argument("source", "Source, depends on anvironment and target", true, true, false);
		/* List of modules to update. */
		Argument argMods = new Argument("modules", "Modules: central/dictionary/local", true, true, "central", "dictionary", "local");
		/* Purge destination directories. */
		Argument argPurge = new Argument("purge", "Purge destination directories", false, false, false);

		argMngr.add(argEnv);
		argMngr.add(argTarget);
		argMngr.add(argDest);
		argMngr.add(argSrc);
		argMngr.add(argMods);
		argMngr.add(argPurge);

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
			return null;
		}

		/* Validate destination for target local. */
		if (argMngr.getValue("target").equals("Local")) {
			File dest = new File(argMngr.getValue("destination"));
			if (!dest.exists()) {
				Alert alert = new Alert();
				alert.setTitle("Local destination does not exist");
				alert.setType(Alert.Type.ERROR);
				alert.setText(dest.toString());
				alert.show();
				System.exit(0);
				return null;
			}
		}

		/* Validate destination for target remote. */
		if (argMngr.getValue("target").equals("Remote")) {
			String[] drives = getDrives(argMngr);
			for (String drive : drives) {
				File dest = new File(drive);
				if (!dest.exists()) {
					Alert alert = new Alert();
					alert.setTitle("Remote destination drive not exists");
					alert.setType(Alert.Type.ERROR);
					alert.setText(dest.toString());
					alert.show();
					System.exit(0);
					return null;
				}
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
			return null;
		}

		return argMngr;
	}
}
