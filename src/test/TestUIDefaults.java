package test;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.TreeMap;

import javax.swing.UIDefaults;
import javax.swing.UIManager;

public class TestUIDefaults {

	public static void main(String[] args) {
		UIDefaults ui = UIManager.getDefaults();
		TreeMap<Object, Object> map = new TreeMap<>();
		Enumeration<Object> keys = ui.keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object val = ui.get(key);
			map.put(key.toString(), val);
		}
		Iterator<Object> iter = map.keySet().iterator();
		while (iter.hasNext()) {
			Object key = iter.next();
			Object val = map.get(key);
			System.out.println(key + ", " + val);
		}
	}

}
