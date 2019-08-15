package test;

import java.util.Locale;

import com.mlt.util.Chrono;
import com.mlt.util.Resources;

public class TestChronoField {
	
	static {
		System.setProperty("log4j.configurationFile", "res/log4j/Logger.xml");
		Resources.addBaseTextResource("res/strings/StringsLibrary.xml");
		Locale.setDefault(Locale.US);
	}

	public static void main(String[] args) {
		Chrono[] chronos = Chrono.values();
		for (Chrono c : chronos) {
			StringBuilder b = new StringBuilder();
			b.append(c.getTemporalField().getDisplayName(Locale.getDefault()));
			System.out.println(b.toString());
		}
	}

}
