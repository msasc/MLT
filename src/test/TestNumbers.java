package test;

import com.mlt.util.Numbers;

public class TestNumbers {

	public static void main(String[] args) {
		System.out.println(Numbers.getBigDecimal(1.0e-6, 6));
		System.out.println(Numbers.getBigDecimal(-1.0e-6, 6));
		System.out.println(-3.923411739933649E-14 > -1.0E-308);
	}

}
