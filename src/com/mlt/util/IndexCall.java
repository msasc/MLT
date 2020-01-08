package com.mlt.util;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Index call.
 * 
 * @author Miquel Sas
 */
public class IndexCall implements Callable<Void> {
	
	private int index;
	private Consumer<Integer> function;
	
	public IndexCall(int index, Consumer<Integer> function) {
		super();
		this.index = index;
		this.function = function;
	}

	@Override
	public Void call() throws Exception {
		function.accept(index);
		return null;
	}
}