package test;

import javax.swing.UIDefaults;
import javax.swing.UIManager;

public class TestUIDefaults {

	public static void main(String[] args) {
		UIDefaults ui = UIManager.getDefaults();
		for (Object key : ui.keySet()) {
			Object val = ui.get(key);
			System.out.println(key + ", " + val);
		}
	}

}
