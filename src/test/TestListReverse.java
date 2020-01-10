package test;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

public class TestListReverse {

	public static void main(String[] args) {
		List<Integer> list = new ArrayList<>();
		list.add(0);
		list.add(1);
		list.add(2);
		list.add(3);
		list.add(4);
		list.add(5);
		list.add(6);
		list.add(7);
		list.add(8);
		list.add(9);
		System.out.println(Lists.reverse(list));

	}

}
