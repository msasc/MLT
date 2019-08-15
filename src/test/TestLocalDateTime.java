package test;

import java.time.LocalDate;

public class TestLocalDateTime {

	public static void main(String[] args) {
		System.out.println(LocalDate.now());
		System.out.println(LocalDate.MIN);
		System.out.println(LocalDate.MAX);
		System.out.println(LocalDate.of(100,12,31));
		System.out.println(LocalDate.of(-100,12,31));
	}

}
