package test;

import com.mlt.util.Vector;

public class TestShuffle {

	public static void main(String[] args) {
		int[] v = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		Vector.shuffle(v);
		System.out.println(Vector.toString(v));
		Vector.shuffle(v);
		System.out.println(Vector.toString(v));
		Vector.shuffle(v);
		System.out.println(Vector.toString(v));
		Vector.shuffle(v);
		System.out.println(Vector.toString(v));
		Vector.shuffle(v);
		System.out.println(Vector.toString(v));
		Vector.shuffle(v);
		System.out.println(Vector.toString(v));
	}
}
