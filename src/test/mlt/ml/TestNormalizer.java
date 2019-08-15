package test.mlt.ml;

import com.mlt.ml.function.Normalizer;

public class TestNormalizer {

	public static void main(String[] args) {
		Normalizer n = new Normalizer(5, 0, 0, 1);
		System.out.println(n.normalize(5));
		System.out.println(n.normalize(0));
	}

}
