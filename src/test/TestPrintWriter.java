package test;

import java.io.PrintWriter;
import java.io.StringWriter;

public class TestPrintWriter {

	public static void main(String[] args) {
		StringWriter s = new StringWriter();
		PrintWriter p = new PrintWriter(s);
		
		p.println("Node class: " + TestPrintWriter.class.getName());
		p.println();
		p.print("In another line");
		
		p.close();
		System.out.println(s.toString());
	}

}
