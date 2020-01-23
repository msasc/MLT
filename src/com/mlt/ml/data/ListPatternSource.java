/*
 * Copyright (C) 2015 Miquel Sas
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.mlt.ml.data;

import java.util.ArrayList;
import java.util.List;

import com.mlt.ml.function.Matcher;
import com.mlt.ml.function.match.CategoryMatcher;
import com.mlt.util.Lists;

/**
 * A list pattern source.
 *
 * @author Miquel Sas
 */
public class ListPatternSource implements PatternSource {

	/** The underlying pattern list. */
	private List<Pattern> patterns;
	/** The description. */
	private String description;
	/** Match function, default is match category. */
	private Matcher matcher = new CategoryMatcher();

	/**
	 * Contructor to clone the source.
	 */
	private ListPatternSource() {}

	/**
	 * Constructor.
	 * 
	 * @param patterns The list of patterns.
	 */
	public ListPatternSource(List<Pattern> patterns) {
		super();
		this.patterns = patterns;
	}

	/**
	 * Constructor.
	 * 
	 * @param patternSorce The list of patterns.
	 */
	public ListPatternSource(PatternSource source) {
		super();
		this.patterns = new ArrayList<>();
		for (int i = 0; i < source.size(); i++) {
			patterns.add(source.get(i));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		patterns.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PatternSource clone() {
		ListPatternSource source = new ListPatternSource();
		source.patterns = new ArrayList<>(this.patterns);
		source.description = this.description;
		return source;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Pattern get(int index) {
		return patterns.get(index);
	}

	/**
	 * @return The description.
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * @return The description.
	 */
	@Override
	public Matcher getMatch() {
		return matcher;
	}

	/**
	 * @param description The description.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size() {
		return patterns.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shuffle() {
		Lists.shuffle(patterns);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEmpty() {
		return patterns.isEmpty();
	}
}
