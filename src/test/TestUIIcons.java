package test;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.Icon;
import javax.swing.KeyStroke;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

import com.mlt.db.Field;
import com.mlt.db.FieldList;
import com.mlt.db.Record;
import com.mlt.db.RecordList;
import com.mlt.db.Types;
import com.mlt.desktop.Option;
import com.mlt.desktop.OptionWindow;
import com.mlt.desktop.control.Frame;
import com.mlt.desktop.control.GridBagPane;
import com.mlt.desktop.control.TablePane;
import com.mlt.desktop.control.TableRecord;
import com.mlt.desktop.control.TableRecordModel;

public class TestUIIcons {

	static class IconUI {
		String key;
		Icon icon;
	}

	public static void main(String[] args) {
		UIDefaults ui = UIManager.getDefaults();
		Enumeration<Object> keys = ui.keys();
		List<IconUI> icons = new ArrayList<>();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Icon icon = ui.getIcon(key);
			if (icon != null && icon instanceof Icon) {
				try {
					Canvas canvas = new Canvas();
					Graphics g = canvas.getGraphics();
					icon.paintIcon(canvas, g, 0, 0);
					IconUI ic = new IconUI();
					ic.key = (String) key;
					ic.icon = icon;
					icons.add(ic);
				} catch (Exception exc) {
					System.out.println(key);
				}
			}
		}

		Field fKEY = new Field();
		fKEY.setType(Types.STRING);
		fKEY.setName("KEY");
		fKEY.setHeader("Key");

		Field fICON = new Field();
		fICON.setType(Types.ICON);
		fICON.setName("ICON");
		fICON.setHeader("Icon");

		FieldList fields = new FieldList();
		fields.addField(fKEY);
		fields.addField(fICON);

		RecordList rs = new RecordList();
		for (IconUI icon : icons) {
			Record rc = new Record(fields);
			rc.setValue("KEY", icon.key);
			rc.setValue("ICON", icon.icon);
			rs.add(rc);
		}
		Record master = new Record(fields);

		TableRecordModel model = new TableRecordModel(master);
		model.addColumn("KEY");
		model.addColumn("ICON");
		model.setRecordSet(rs);
		
		TableRecord table = new TableRecord();
		table.setModel(model);
		
		OptionWindow wnd = new OptionWindow(new Frame(new GridBagPane()));
		wnd.setTitle("UI icons");
		wnd.setOptionsBottom();
		
		wnd.setCenter(new TablePane(table));

		Option option = new Option();
		option.setKey("CLOSE");
		option.setText("Close");
		option.setToolTip("Close the window");
		option.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
		option.setDefaultClose(true);
		option.setAction(listener -> {
			wnd.close();
		});
		wnd.getOptionPane().add(option);
		
		wnd.pack();
		wnd.centerOnScreen();
		wnd.show();
	}

}
