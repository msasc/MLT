package test;

import com.mlt.desktop.control.Console;
import com.mlt.desktop.control.StatusBar;

public class TestConsoleDimension {

	public static void main(String[] args) {
		Console console = new Console();
		System.out.println(console.getPreferredSize());
		StatusBar statusBar = new StatusBar();
		System.out.println(statusBar.getPreferredSize());
	}

}
