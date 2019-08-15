package test;

import com.mlt.util.Strings;

public class TestChars {

	public static void main(String[] args) {
		for (int i = 0; i < 256; i++) {
			char c = (char) i;
			String s = Character.valueOf(c).toString();
			if (i == 9 ||
				i == 10 ||
				i == 13 ||
				i == 28 ||
				i == 29 ||
				i == 30 ||
				i == 31) {
				s = " ";
			}
			StringBuilder b = new StringBuilder();
			b.append(Strings.leftPad(Integer.toString(i), 3));
			b.append(": ");
			b.append(s);
			b.append(", Lower: ");
			b.append(yn(Character.isLowerCase(c)));
			b.append(", Upper: ");
			b.append(yn(Character.isUpperCase(c)));
			b.append(", Alpha: ");
			b.append(yn(Character.isAlphabetic(c)));
			b.append(", Digit: ");
			b.append(yn(Character.isDigit(c)));
			b.append(", Letter: ");
			b.append(yn(Character.isLetterOrDigit(c)));
			b.append(", Title: ");
			b.append(yn(Character.isTitleCase(c)));
			b.append(", White: ");
			b.append(yn(Character.isWhitespace(c)));
			System.out.println(b.toString());
		}
		
		System.out.println();
		
		StringBuilder any = new StringBuilder();
		for (int i = 32; i < 256; i++) {
			any.append(Character.valueOf((char) i).toString());
		}
		System.out.println(any.toString());
		System.out.println(any.toString().toLowerCase());
		System.out.println(any.toString().toUpperCase());
		
	}

	private static String yn(boolean b) {
		return b ? "Y" : "N";
	}
}
