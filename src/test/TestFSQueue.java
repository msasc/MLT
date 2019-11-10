package test;

import com.mlt.util.FixedSizeList;

public class TestFSQueue {

	public static void main(String[] args) {
		FixedSizeList<Integer> q = new FixedSizeList<>(10);
		for (int i = 0; i < 200; i++) {
			q.add(i);
			System.out.println(q.toString());
		}
	}

}
