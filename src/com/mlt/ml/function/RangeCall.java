package com.mlt.ml.function;

import java.util.concurrent.Callable;
import java.util.function.BiConsumer;

/**
 * Range call.
 * 
 * @author Miquel Sas
 */
public class RangeCall implements Callable<Void> {
	
	private Range range;
	private BiConsumer<Integer, Integer> function;
	
	public RangeCall(Range range, BiConsumer<Integer, Integer> function) {
		super();
		this.range = range;
		this.function = function;
	}

	@Override
	public Void call() throws Exception {
		function.accept(range.getStart(), range.getEnd());
		return null;
	}
}