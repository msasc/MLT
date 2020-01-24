package test;

import com.mlt.util.Strings;

public class TestStrings {
	public static void main(String[] args) {
		System.out.println(Strings.centerPad("abcdefghij", 8, " "));
		System.out.println(Strings.centerPad("abcdefghij", 15, "-"));
	}
}
