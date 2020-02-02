package test;

import com.mlt.ml.function.Distance;
import com.mlt.ml.function.distance.DistanceEuclidean;

public class TestDistance {

	public static void main(String[] args) {
		Distance distance = new DistanceEuclidean();
		System.out.println(
			distance.distance(
				new double[] { 1.00, 0.00, 0.00 },
				new double[] { 0.10, 0.0999999, 0.0999999 }));
		System.out.println(
			distance.distance(
				new double[] { 1.00, 0.00, 0.00 },
				new double[] { 0.0999, 0.10, 0.00001 }));

	}
}
