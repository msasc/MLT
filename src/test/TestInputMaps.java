package test;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import com.google.common.base.Strings;

public class TestInputMaps {
	
	static class KeyComparator implements Comparator<KeyStroke> {
		@Override
		public int compare(KeyStroke k1, KeyStroke k2) {
			int c1 = k1.getKeyCode();
			int c2 = k2.getKeyCode();
			if (Integer.compare(c1, c2) != 0) {
				return Integer.compare(c1, c2);
			}
			int m1 = k1.getModifiers();
			int m2 = k2.getModifiers();
			return Integer.compare(m1, m2);
		}
		
	}

	public static void main(String[] args) {
		Locale.setDefault(Locale.ENGLISH);
		printInputMap(new JTextArea());
	}

	private static void printInputMap(JComponent cmp) {
		InputMap map = cmp.getInputMap();
		String title = cmp.getClass().getName();
		System.out.println(title);
		System.out.println(Strings.repeat("- ", (title.length() / 2) + 1));
		KeyStroke[] keys = map.allKeys();
		Arrays.sort(keys, new KeyComparator());
		for (KeyStroke key : keys) {
			System.out.println(toString(key) + ": " + map.get(key));
		}
	}
	
	private static String toString(KeyStroke keyStroke) {
		StringBuilder b = new StringBuilder();
		if (keyStroke != null) {
			b.append(KeyEvent.getKeyText(keyStroke.getKeyCode()));
			int modifiers = keyStroke.getModifiers();
			if (modifiers > 0) {
				b.append(" (");
				b.append(KeyEvent.getModifiersExText(modifiers));
				b.append(")");
			}
		}
		return b.toString();
	}
}
