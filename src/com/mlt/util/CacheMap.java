/*
 * Copyright (C) 2018 Miquel Sas
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
package com.mlt.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A useful map to cache objects with a cache size.
 *
 * @author Miquel Sas
 * @param <K> Type of the key.
 * @param <V> Type of the value.
 */
public class CacheMap<K, V> implements Map<K, V> {

	/** Internal data map. */
	private Map<K, V> mapData = new TreeMap<>();
	/** Internal time map. */
	private Map<Long, K> mapTime = new TreeMap<>();
	/** Last time key. */
	private long time = Long.MIN_VALUE;
	/** Cache size. Less equal than zero, no cache. */
	private int cacheSize = 50000;
	/** Cache factor: 0.5, removes half of the cache. */
	private double cacheFactor = 0.2;

	/**
	 * Constructor.
	 */
	public CacheMap() {
		super();
	}

	/**
	 * Constructor assigning the cache size.
	 *
	 * @param cacheSize The cache size.
	 */
	public CacheMap(int cacheSize) {
		super();
		this.cacheSize = cacheSize;
	}

	/**
	 * Returns the cache size.
	 *
	 * @return The cache size.
	 */
	public int getCacheSize() {
		return cacheSize;
	}

	/**
	 * Set the cache size.
	 *
	 * @param cacheSize The cache size.
	 */
	public void setCacheSize(int cacheSize) {
		if (cacheSize <= 100) {
			throw new IllegalArgumentException("Invalid cache size: " + cacheSize);
		}
		this.cacheSize = cacheSize;
	}

	/**
	 * Returns the cache factor.
	 *
	 * @return The cache factor.
	 */
	public double getCacheFactor() {
		return cacheFactor;
	}

	/**
	 * Set the cache factor.
	 *
	 * @param cacheFactor The cache factor.
	 */
	public void setCacheFactor(double cacheFactor) {
		if (cacheFactor <= 0 || cacheFactor > 1) {
			throw new IllegalArgumentException("Invalid chache factor: " + cacheFactor);
		}
		this.cacheFactor = cacheFactor;
	}

	/**
	 * Returns the size of this map.
	 *
	 * @return The size of this map.
	 */
	@Override
	public int size() {
		return mapData.size();
	}

	/**
	 * Check empty.
	 *
	 * @return A boolean.
	 */
	@Override
	public boolean isEmpty() {
		return mapData.isEmpty();
	}

	/**
	 * Check if the map contains the key.
	 *
	 * @return A boolean.
	 */
	@Override
	public boolean containsKey(Object key) {
		return mapData.containsKey(key);
	}

	/**
	 * Check if the map contains the value.
	 *
	 * @return A boolean.
	 */
	@Override
	public boolean containsValue(Object value) {
		return mapData.containsValue(value);
	}

	/**
	 * Returns the value with the given key or null.
	 *
	 * @param key The key.
	 */
	@Override
	public V get(Object key) {
		return mapData.get(key);
	}

	/**
	 * Put the key pair value.
	 *
	 * @param key   The key.
	 * @param value The value.
	 */
	@Override
	public V put(K key, V value) {
		
		/* If the cache size is reached, remove older values. */
		if (mapData.size() >= cacheSize) {
			int count = Double.valueOf(Double.valueOf(cacheSize) * cacheFactor).intValue();
			Iterator<Long> iterator = mapTime.keySet().iterator();
			List<Long> times = new ArrayList<>();
			List<K> keys = new ArrayList<>();
			while (iterator.hasNext()) {
				long t = iterator.next();
				K k = mapTime.get(t);
				times.add(t);
				keys.add(k);
				if (--count <= 0) {
					break;
				}
			}
			for (Long t : times) {
				mapTime.remove(t);
			}
			for (K k : keys) {
				mapData.remove(k);
			}
		}
		mapTime.put(time++, key);
		return mapData.put(key, value);
	}

	/**
	 * Remove the given key.
	 *
	 * @param key The key to remove.
	 */
	@Override
	public V remove(Object key) {
		return mapData.remove(key);
	}

	/**
	 * Not supported.
	 */
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Clear the map.
	 */
	@Override
	public void clear() {
		mapData.clear();
	}

	/**
	 * Return the key set.
	 *
	 * @return The key set.
	 */
	@Override
	public Set<K> keySet() {
		return mapData.keySet();
	}

	/**
	 * Returns the values collection.
	 *
	 * @return The values collection.
	 */
	@Override
	public Collection<V> values() {
		return mapData.values();
	}

	/**
	 * Returns the entry set.
	 *
	 * @return The entry set.
	 */
	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return mapData.entrySet();
	}

}
