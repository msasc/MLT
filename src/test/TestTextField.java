package test;

import java.awt.event.KeyEvent;
import java.util.Locale;

import javax.swing.KeyStroke;

import com.mlt.desktop.Option;
import com.mlt.desktop.OptionWindow;
import com.mlt.desktop.control.Frame;
import com.mlt.desktop.control.GridBagPane;
import com.mlt.desktop.control.Label;
import com.mlt.desktop.control.TextField;
import com.mlt.desktop.formatter.MaskFilter;
import com.mlt.desktop.layout.Anchor;
import com.mlt.desktop.layout.Constraints;
import com.mlt.desktop.layout.Fill;
import com.mlt.desktop.layout.Insets;
import com.mlt.desktop.layout.Pad;
import com.mlt.util.Resources;
import com.mlt.util.Strings;

public class TestTextField {

	static {
		System.setProperty("log4j.configurationFile", "res/log4j/Logger.xml");
		Resources.addBaseTextResource("res/strings/StringsLibrary.xml");
		Locale.setDefault(Locale.US);
	}
	
	public static void main(String[] args) {

		OptionWindow wnd = new OptionWindow(new Frame(new GridBagPane()));
		wnd.setTitle("Testing text field");
		wnd.setOptionsBottom();

		Label label = new Label("Simple text field");
//		StringField field = new StringField();
//		StringFilter filter = new StringFilter(StringFilter.UPPERCASE, 40, null, null);
//		field.setFilter(filter);
//		field.setValue(new Value("aaaa"));
		
		TextField field = new TextField();
		
		MaskFilter filter = new MaskFilter();
		filter.addMasked(
			Strings.Type.LETTER_OR_DIGIT,
			Strings.Modifier.UPPER,
			4, 
			Pad.RIGHT, 
			"0", 
			null, 
			null);
		filter.addFixed("--");
		filter.addMasked(
			Strings.Type.DIGIT,
			Strings.Modifier.NONE,
			4, 
			Pad.RIGHT, 
			"0", 
			null, 
			null);
		filter.addFixed("--");
		filter.addMasked(
			Strings.Type.LETTER,
			Strings.Modifier.LOWER,
			20, 
			Pad.RIGHT, 
			"z", 
			null, 
			null);
		
		field.setFilter(filter);

		Insets insets = new Insets(5, 5, 5, 5);

		GridBagPane center = new GridBagPane();
		center.add(label, new Constraints(Anchor.LEFT, Fill.NONE, 0, 0, insets));
		center.add(field, new Constraints(Anchor.LEFT, Fill.HORIZONTAL, 1, 0, insets));
		wnd.setCenter(center);

		Option optionClose = new Option();
		optionClose.setKey("CLOSE");
		optionClose.setText("Close");
		optionClose.setToolTip("Close the window");
		optionClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
		optionClose.setDefaultClose(true);
		optionClose.setAction(listener -> {
			wnd.close();
		});

		wnd.getOptionPane().add(optionClose);
		wnd.getOptionPane().setMnemonics();

		wnd.setSize(0.6, 0.5);
		wnd.centerOnScreen();
		wnd.show();
	}
}
