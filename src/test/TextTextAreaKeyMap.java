package test;

import javax.swing.InputMap;
import javax.swing.JTextArea;
import javax.swing.text.Keymap;

public class TextTextAreaKeyMap {

	public static void main(String[] args) {
		JTextArea textArea = new JTextArea("Hello world");
		Keymap keyMap = textArea.getKeymap();
		InputMap inputMap = textArea.getInputMap();
		System.out.println(keyMap.getBoundKeyStrokes());
	}

}
