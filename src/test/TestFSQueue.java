package test;

import java.util.Iterator;

import com.mlt.util.FixedSizeQueue;

public class TestFSQueue {

	public static void main(String[] args) {
		FixedSizeQueue<Integer> q = new FixedSizeQueue<>(10);
		for (int i = 0; i < 30; i++) {
			q.addLast(i);
			System.out.println(q.toString());
		}
		System.out.println("--------------------------------------------------");
		q.clear();
		for (int i = 0; i < 30; i++) {
			q.addFirst(i);
			System.out.println(q.toString());
		}
		System.out.println("--------------------------------------------------");
		Iterator<Integer> asc = q.ascendingIterator();
		while (asc.hasNext()) {
			System.out.println(asc.next());
		}
		System.out.println("--------------------------------------------------");
		Iterator<Integer> desc = q.descendingIterator();
		while (desc.hasNext()) {
			System.out.println(desc.next());
		}
	}

}
