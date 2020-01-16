package test;

import com.mlt.util.ArrayQueue;

public class TestQueue {

	public static void main(String[] args) {
		ArrayQueue<Integer> q = new ArrayQueue<>();
		for (int i = 0; i < 201; i++) {
			q.addFirst(i);
			if (i > 0) q.addLast(i);
		}
		for (int i = 0; i < q.size(); i++) {
			System.out.println(q.get(i));
		}
		for (int i = q.size() - 1; i >= 201; i--) {
			q.remove(i);
		}
		System.out.println("----------------");
		for (int i = 0; i < q.size(); i++) {
			System.out.println(q.get(i));
		}
	}

}
