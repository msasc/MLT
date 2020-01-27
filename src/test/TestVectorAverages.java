package test;

import java.util.ArrayList;
import java.util.List;

import com.mlt.util.Lists;
import com.mlt.util.Vector;

public class TestVectorAverages {
	public static void main(String[] args) {
		List<double[]> vectors = new ArrayList<>();
		vectors.add(new double[] { 0.377027, 0.000000 });
		vectors.add(new double[] { 0.150974, -0.599566 });
//		vectors.add(new double[] { 0.135336, -0.103585 });
		System.out.println(Lists.asList(Vector.averageWMA(vectors)));
	}
}
