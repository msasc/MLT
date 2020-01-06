package test;

import java.util.ArrayList;
import java.util.List;

import com.mlt.util.Lists;
import com.mlt.util.Vector;

public class TestVectorAverages {
	public static void main(String[] args) {
		List<double[]> vectors = new ArrayList<>();
		vectors.add(new double[] { 1.0, 2.0, 3.0 });
		vectors.add(new double[] { 2.0, 2.0, 3.0 });
		vectors.add(new double[] { 4.0, 0.0, 3.0 });
		vectors.add(new double[] { 1.0, 0.0, 3.0 });
		vectors.add(new double[] { 1.0, 0.0, 3.0 });
		vectors.add(new double[] { 1.0, 0.0, 3.0 });
		vectors.add(new double[] { 1.0, 0.0, 3.0 });
		vectors.add(new double[] { 1.0, 0.0, 3.0 });
		System.out.println(Lists.asList(Vector.averageSMA(vectors)));
		System.out.println(Lists.asList(Vector.averageEMA(vectors)));
		System.out.println(Lists.asList(Vector.averageWMA(vectors)));
	}
}
