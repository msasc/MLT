package test;

import java.awt.event.KeyEvent;
import java.util.Locale;

import javax.swing.KeyStroke;

import com.mlt.db.FieldGroup;
import com.mlt.db.RandomData;
import com.mlt.db.Record;
import com.mlt.desktop.Option;
import com.mlt.desktop.OptionWindow;
import com.mlt.desktop.control.FormRecordPane;
import com.mlt.desktop.control.Frame;
import com.mlt.desktop.control.GridBagPane;
import com.mlt.util.Resources;

public class TestFormRecordPane {

	static {
		System.setProperty("log4j.configurationFile", "res/log4j/Logger.xml");
		Resources.addBaseTextResource("res/strings/StringsLibrary.xml");
		Locale.setDefault(Locale.US);
	}
	
	private static boolean pack = true;
	private static boolean group = false;
	private static boolean columns = false;
	private static boolean rows = true;

	public static void main(String[] args) {
		if (group) {
			showWindowGroup();
		}
		if (columns) {
			showWindowGridColumns();
		}
		if (rows) {
			showWindowGridRows();
		}
//		showWindow("Only pane");
	}

	private static Option getOptionClose(OptionWindow wnd) {
		Option option = new Option();
		option.setKey("CLOSE");
		option.setText("Close");
		option.setToolTip("Close the window");
		option.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
		option.setDefaultClose(true);
		option.setAction(listener -> {
			wnd.close();
		});
		return option;
	}
	
	private static void showWindow(FormRecordPane form, String title) {
		
		form.layout();
		form.updateEditors();

		OptionWindow wnd = new OptionWindow(new Frame(new GridBagPane()));
		wnd.setTitle(title);
		wnd.setOptionsBottom();

		wnd.setCenter(form.getPane());
		wnd.getOptionPane().add(getOptionClose(wnd));
		wnd.getOptionPane().setMnemonics();

		if (pack) {
			wnd.pack();
		} else {
			wnd.setSize(0.6, 0.5);
		}
		wnd.centerOnScreen();
		wnd.show();
	}

	private static void showWindowGridColumns() {

		Record rc = new RandomData.Test().getRecordRandom();

		FormRecordPane rp = new FormRecordPane(rc);
		rp.setLayoutByColumns(FieldGroup.EMPTY_FIELD_GROUP);

		rp.addField(RandomData.Test.QSALES, 0, 0);
		rp.addField(RandomData.Test.QPURCH, 0, 0);
		rp.addField(RandomData.Test.QPROD, 0, 0);

		rp.addField(RandomData.Test.ICHECKED, 1, 0);
		rp.addField(RandomData.Test.IREQUIRED, 1, 0);
		rp.addField(RandomData.Test.ISTATUS, 1, 0);

		rp.addField(RandomData.Test.CARTICLE, 0, 1);
		rp.addField(RandomData.Test.DARTICLE, 0, 1);
		rp.addField(RandomData.Test.CBUSINESS, 0, 1);
		rp.addField(RandomData.Test.TCREATED, 0, 1);
		
		showWindow(rp, "Form pane grids organized by columns");
	}

	private static void showWindowGridRows() {

		Record rc = new RandomData.Test().getRecordRandom();

		FormRecordPane rp = new FormRecordPane(rc);
		rp.setLayoutByRows(FieldGroup.EMPTY_FIELD_GROUP);

		rp.addField(RandomData.Test.CARTICLE, 0, 0);
		rp.addField(RandomData.Test.DTITLE, 0, 0);
		rp.addField(RandomData.Test.DARTICLE, 0, 0);
		rp.addField(RandomData.Test.CBUSINESS, 0, 0);
		rp.addField(RandomData.Test.TCREATED, 0, 0);

		rp.addField(RandomData.Test.QSALES, 1, 0);
		rp.addField(RandomData.Test.QPURCH, 1, 0);
		rp.addField(RandomData.Test.QPROD, 1, 0);

		rp.addField(RandomData.Test.ICHECKED, 1, 1);
		rp.addField(RandomData.Test.IREQUIRED, 1, 1);
		rp.addField(RandomData.Test.ISTATUS, 1, 1);

		showWindow(rp, "Form pane grids organized by rows");
	}

	private static void showWindowGroup() {

		Record rc = new RandomData.Test().getRecordRandom();

		FieldGroup fgKey = new FieldGroup(
			0, "Key", "Key data", "Key data, like primary key, description or creation date");

		rc.getField(RandomData.Test.CARTICLE).setFieldGroup(fgKey);
		rc.getField(RandomData.Test.DARTICLE).setFieldGroup(fgKey);
		rc.getField(RandomData.Test.CBUSINESS).setFieldGroup(fgKey);
		rc.getField(RandomData.Test.TCREATED).setFieldGroup(fgKey);

		FieldGroup fgNum = new FieldGroup(
			0, "Num", "Numeric data", "Numeric data like sales, production or purchases");

		rc.getField(RandomData.Test.QSALES).setFieldGroup(fgNum);
		rc.getField(RandomData.Test.QPURCH).setFieldGroup(fgNum);
		rc.getField(RandomData.Test.QPROD).setFieldGroup(fgNum);

		FormRecordPane rp = new FormRecordPane(rc);

		rp.addField(RandomData.Test.CARTICLE);
		rp.addField(RandomData.Test.DARTICLE);
		rp.addField(RandomData.Test.CBUSINESS);
		rp.addField(RandomData.Test.TCREATED);

		rp.addField(RandomData.Test.QSALES);
		rp.addField(RandomData.Test.QPURCH);
		rp.addField(RandomData.Test.QPROD);

		showWindow(rp, "Form pane grids organized field groups");
	}
}
