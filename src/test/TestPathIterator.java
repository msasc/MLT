package test;

import java.awt.geom.GeneralPath;

public class TestPathIterator {

	public static void main(String[] args) {
		GeneralPath path = new GeneralPath();
		path.moveTo(0, 0);
		path.reset();
		System.out.println(path.getPathIterator(null).isDone());
	}

}
