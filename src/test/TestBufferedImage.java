package test;

import java.awt.image.BufferedImage;

public class TestBufferedImage {

	public static void main(String[] args) {
		int width = 200;
		int height = 200;
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				img.setRGB(x, y, -1);
			}
		}
	}
}
