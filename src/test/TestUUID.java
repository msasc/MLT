package test;

import java.util.UUID;

public class TestUUID {

	public static void main(String[] args) {
		String uuid = UUID.randomUUID().toString();
		System.out.println(uuid + " (" + uuid.length() + ")");

	}

}
