package test;

import java.awt.event.KeyEvent;
import java.util.Locale;

import javax.swing.KeyStroke;

import com.mlt.desktop.Option;
import com.mlt.desktop.OptionWindow;
import com.mlt.desktop.control.DatePane;
import com.mlt.desktop.control.Frame;
import com.mlt.desktop.control.GridBagPane;
import com.mlt.util.Resources;

public class TestDatePane {

	static {
		System.setProperty("log4j.configurationFile", "res/log4j/Logger.xml");
		Resources.addBaseTextResource("res/strings/StringsLibrary.xml");
		Locale.setDefault(Locale.US);
	}
	
	public static void main(String[] args) {

		OptionWindow wnd = new OptionWindow(new Frame(new GridBagPane()));
		wnd.setTitle("Testing text field");
		wnd.setOptionsBottom();

		DatePane datePane = new DatePane();
		
		wnd.setCenter(datePane);

		Option optionClose = new Option();
		optionClose.setKey("CLOSE");
		optionClose.setText("Close");
		optionClose.setToolTip("Close the window");
		optionClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
		optionClose.setDefaultClose(true);
		optionClose.setAction(l -> { wnd.close(); });

		wnd.getOptionPane().add(optionClose);
		wnd.getOptionPane().setMnemonics();

		wnd.setSize(0.30, 0.45);
		wnd.centerOnScreen();
		wnd.show();
	}
}
