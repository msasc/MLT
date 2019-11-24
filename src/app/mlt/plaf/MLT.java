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
package app.mlt.plaf;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import com.mlt.db.Persistor;
import com.mlt.db.PersistorDDL;
import com.mlt.db.Record;
import com.mlt.db.Value;
import com.mlt.db.rdbms.DBEngine;
import com.mlt.db.rdbms.DBEngineAdapter;
import com.mlt.db.rdbms.DataSourceInfo;
import com.mlt.db.rdbms.adapters.PostgreSQLAdapter;
import com.mlt.desktop.Alert;
import com.mlt.desktop.Option;
import com.mlt.desktop.control.Frame;
import com.mlt.desktop.control.GridBagPane;
import com.mlt.desktop.control.Menu;
import com.mlt.desktop.control.MenuBar;
import com.mlt.desktop.control.MenuItem;
import com.mlt.desktop.control.Stage;
import com.mlt.desktop.control.StatusBar;
import com.mlt.desktop.control.TabPane;
import com.mlt.desktop.layout.Anchor;
import com.mlt.desktop.layout.Constraints;
import com.mlt.desktop.layout.Fill;
import com.mlt.desktop.layout.Insets;
import com.mlt.launch.Argument;
import com.mlt.launch.ArgumentManager;
import com.mlt.mkt.data.Period;
import com.mlt.mkt.server.Server;
import com.mlt.mkt.servers.dukascopy.DkServer;
import com.mlt.util.Logs;
import com.mlt.util.Properties;
import com.mlt.util.Resources;
import com.mlt.util.Strings;

import app.mlt.plaf.action.ActionExitApplication;
import app.mlt.plaf.action.ActionInstruments;
import app.mlt.plaf.action.ActionStatistics;
import app.mlt.plaf.action.ActionTickers;

/**
 * MLT platform entry.
 *
 * @author Miquel Sas
 */
public class MLT {

	/**
	 * Logger configuration and text server initialization.
	 */
	static {
		System.setProperty("log4j.configurationFile", "res/log4j/Logger.xml");
		Resources.addBaseTextResource("res/strings/StringsLibrary.xml");
		Locale.setDefault(Locale.US);
	}

	/**
	 * Close operation.
	 */
	static class FrameListener extends Stage.Adapter {
		@Override
		public void closing(Stage stage) {
			ActionExitApplication exit = new ActionExitApplication();
			exit.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Exit"));
		}
	}

	/**
	 * Return the database engine.
	 * 
	 * @return The database engine.
	 */
	public static DBEngine getDBEngine() {
		return (DBEngine) properties.getObject("DB_ENGINE");
	}

	/**
	 * Return the main frame.
	 * 
	 * @return The main frame.
	 */
	public static Frame getFrame() {
		return (Frame) properties.getObject("FRAME");
	}

	/**
	 * Return the main menu bar.
	 * 
	 * @return The menu bar.
	 */
	public static MenuBar getMenuBar() {
		return (MenuBar) properties.getObject("MENU_BAR");
	}

	/**
	 * Return the server.
	 * 
	 * @return The server.
	 */
	public static Server getServer() {
		return (Server) properties.getObject("SERVER");
	}

	/**
	 * Return the main status bar.
	 * 
	 * @return The status bar.
	 */
	public static StatusBar getStatusBar() {
		return (StatusBar) properties.getObject("STATUS_BAR");
	}

	/**
	 * Return the main tabbed pane.
	 * 
	 * @return The tabbed pane.
	 */
	public static TabPane getTabbedPane() {
		return (TabPane) properties.getObject("TABBED_PANE");
	}

	/** Properties to set all static and public properties. */
	private static Properties properties = new Properties();

	/**
	 * Start and launch the application.
	 * 
	 * @param args Startup arguments.
	 */
	public static void main(String[] args) {

		/*
		 * Launch arguments management.
		 */
		ArgumentManager argMngr = new ArgumentManager();
		Argument argConnection =
			new Argument("dataSourceFile", "Database connection file", true, true, false);
		Argument argServer = new Argument("server", "Trading server", true, false, "Dukascopy");
		Argument argAccount =
			new Argument("account", "Trading account", true, false, "demo", "live");
		argMngr.add(argConnection);
		argMngr.add(argServer);
		argMngr.add(argAccount);

		/*
		 * Validate arguments.
		 */
		if (!argMngr.parse(args)) {
			Alert alert = new Alert();
			alert.setTitle("Argument errors");
			alert.setType(Alert.Type.ERROR);
			StringBuilder text = new StringBuilder();
			for (String error : argMngr.getErrors()) {
				text.append(error + "\n");
			}
			alert.setText(text.toString());
			alert.show();
			return;
		}

		/*
		 * Initialize the trading server.
		 */
		if (argMngr.getValue("server").equals("Dukascopy")) {
			properties.setObject("SERVER", new DkServer());
		}

		/*
		 * Show it in the main frame.
		 */
		properties.setObject("FRAME", new Frame(new GridBagPane()));
		getFrame().addWindowListener(new FrameListener());
		getFrame().setTitle("MLT Platform - " + getServer().getTitle());

		/* Menu bar. */
		properties.setObject("MENU_BAR", new MenuBar());
		getFrame().setMenuBar(getMenuBar());
		setupMenuBar();

		/* Content pane with its tabbed pane and status bar. */
		GridBagPane content = new GridBagPane();
		properties.setObject("TABBED_PANE", new TabPane());
		properties.setObject("STATUS_BAR", new StatusBar());
		content.add(getTabbedPane(),
			new Constraints(Anchor.TOP, Fill.BOTH, 0, 0, new Insets(0, 0, 0, 0)));
		content.add(getStatusBar(),
			new Constraints(Anchor.BOTTOM, Fill.HORIZONTAL, 0, 1, new Insets(0, 0, 0, 0)));
		getFrame().setContent(content);

		getFrame().setSize(0.8, 0.8);
		getFrame().centerOnScreen();
		getFrame().setVisible(true);

		getStatusBar().setLabel("DATE", LocalDate.now().toString());

		/*
		 * Launch the rest of startup actions in a thread.
		 */
		new Thread(() -> {
			configureDatabase(argMngr.getValue("dataSourceFile"));
		}).start();
	}

	/**
	 * Configure and return the menu bar.
	 * 
	 * @return The menu bar.
	 */
	private static void setupMenuBar() {

		/* File menu. */
		Menu file = Option.createMenu("File");

		/* File exit. */
		MenuItem fileExit = Option.createMenuItem("Exit");
		fileExit.setAction(new ActionExitApplication());
		file.add(fileExit);

		/* Instruments menu. */
		Menu instruments = Option.createMenu("Instruments");
		MenuItem instrumentsAvail = Option.createMenuItem("Available instruments");
		instrumentsAvail.setAction(new ActionInstruments.Available());
		MenuItem instrumentsSync = Option.createMenuItem("Synchronize instruments");
		instruments.add(instrumentsAvail);
		instruments.add(instrumentsSync);

		/* Tickers menu. */
		Menu tickers = Option.createMenu("Tickers");
		MenuItem tickersDefine = Option.createMenuItem("Define");
		tickersDefine.setAction(new ActionTickers());
		MenuItem tickersStats = Option.createMenuItem("Statistics");
		tickersStats.setAction(new ActionStatistics());
		tickers.add(tickersDefine);
		tickers.add(tickersStats);

		Option.setMnemonics(file, instruments, tickers);
		Option.setMnemonics(fileExit);
		Option.setMnemonics(instrumentsAvail, instrumentsSync);
		Option.setMnemonics(tickersDefine, tickersStats);

		/* Setup the menu bar. */
		getMenuBar().add(file);
		getMenuBar().add(instruments);
		getMenuBar().add(tickers);
	}

	/**
	 * Configure the database.
	 */
	private static void configureDatabase(String connectionFile) {
		String prefix = "Database check: ";
		getStatusBar().setLabel("DBCHK", prefix + "...");
		try {

			/* Connection file. */
			getStatusBar().setLabel("DBCHK", prefix + "connection file...");
			File cnFile = new File(connectionFile);
			if (!cnFile.exists()) {
				throw new FileNotFoundException(
					"Connection file " + connectionFile + " does not exist.");
			}

			/* Data source info and db engine. */
			getStatusBar().setLabel("DBCHK", prefix + "setup...");
			DataSourceInfo info = DataSourceInfo.getDataSourceInfo(cnFile);
			DBEngineAdapter adapter = new PostgreSQLAdapter();
			DBEngine dbEngine = new DBEngine(adapter, info);
			properties.setObject("DB_ENGINE", dbEngine);

			/* Persistor DDL. */
			PersistorDDL ddl = DB.ddl();

			/* Check for the system schema. */
			getStatusBar().setLabel("DBCHK", prefix + "check system schema...");
			if (!ddl.existsSchema(DB.schema_system())) {
				ddl.createSchema(DB.schema_system());
			}

			/* Check for server schema. */
			getStatusBar().setLabel("DBCHK", prefix + "check server schema...");
			String schema = DB.schema_server();
			if (!ddl.existsSchema(schema)) {
				ddl.createSchema(schema);
			}

			/* Check for the necessary table Servers in the system schema. */
			getStatusBar().setLabel("DBCHK", prefix + "check servers table...");
			if (!ddl.existsTable(DB.schema_system(), DB.SERVERS)) {
				ddl.buildTable(DB.table_servers());
			}
			synchronizeSupportedServer(DB.persistor_servers());

			/* Check for the necessary table Periods in the system schema. */
			getStatusBar().setLabel("DBCHK", prefix + "check periods table...");
			if (!ddl.existsTable(DB.schema_system(), DB.PERIODS)) {
				ddl.buildTable(DB.table_periods());
			}
			synchronizeStandardPeriods(DB.persistor_periods());

			/* Check for the necessary table Instruments in the system schema. */
			getStatusBar().setLabel("DBCHK", prefix + "check instruments table...");
			if (!ddl.existsTable(DB.schema_system(), DB.INSTRUMENTS)) {
				ddl.buildTable(DB.table_instruments());
			}

			/* Check for the necessary table Tickers in the system schema. */
			getStatusBar().setLabel("DBCHK", prefix + "check tickers table...");
			if (!ddl.existsTable(DB.schema_system(), DB.TICKERS)) {
				ddl.buildTable(DB.table_tickers());
			}

			/* Check for the necessary table Statistics in the system schema. */
			getStatusBar().setLabel("DBCHK", prefix + "check statistics table...");
			if (!ddl.existsTable(DB.schema_system(), DB.STATISTICS)) {
				ddl.buildTable(DB.table_statistics());
			}

		} catch (Exception exc) {
			Logs.catching(exc);
			Alert.error("Initialization error", Strings.getStackTrace(exc));
		}
		getStatusBar().removeLabel("DBCHK");
	}

	/**
	 * Synchronize supported servers.
	 * 
	 * @param persistor The persistor.
	 * @throws Exception If any error occurs.
	 */
	private static void synchronizeSupportedServer(Persistor persistor) throws Exception {
		Record record = persistor.getDefaultRecord();
		record.setValue(Fields.SERVER_ID, new Value(getServer().getId()));
		record.setValue(Fields.SERVER_NAME, new Value(getServer().getName()));
		record.setValue(Fields.SERVER_TITLE, new Value(getServer().getTitle()));
		persistor.save(record);
	}

	/**
	 * Synchronize standard periods.
	 * 
	 * @param persistor The persistor.
	 * @throws Exception If any error occurs.
	 */
	private static void synchronizeStandardPeriods(Persistor persistor) throws Exception {
		List<Period> periods = Period.getStandardPeriods();
		for (Period period : periods) {
			Record record = persistor.getDefaultRecord();
			record.setValue(Fields.PERIOD_ID, new Value(period.getId()));
			record.setValue(Fields.PERIOD_NAME, new Value(period.toString()));
			record.setValue(Fields.PERIOD_SIZE, new Value(period.getSize()));
			record.setValue(Fields.PERIOD_UNIT_INDEX, new Value(period.getUnit().ordinal()));
			if (!persistor.exists(record)) {
				persistor.insert(record);
			}
		}
	}
}
