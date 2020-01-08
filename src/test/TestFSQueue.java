package test;

import java.util.Iterator;

import com.mlt.util.FixedSizeList;

public class TestFSQueue {

	public static void main(String[] args) {
		FixedSizeList<Integer> q = new FixedSizeList<>(10);
		for (int i = 0; i < 30; i++) {
			q.add(i);
			System.out.println(q.toString());
		}
		System.out.println("--------------------------------------------------");
		Iterator<Integer> iter = q.iterator();
		while (iter.hasNext()) {
			System.out.println(iter.next());
			iter.remove();
		}
	}

}
