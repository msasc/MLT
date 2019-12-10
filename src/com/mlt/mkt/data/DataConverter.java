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

package com.mlt.mkt.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.mlt.db.Field;
import com.mlt.db.Record;
import com.mlt.db.Value;

/**
 * Converter from record to timed data and vice versa. The general contract is
 * that field 0 is the time.
 *
 * @author Miquel Sas
 */
public class DataConverter {

	/**
	 * Property structure.
	 */
	private static class Property {
		/** Record index. */
		int index;
		/** Record and property alias. */
		String alias;
		/** Data type. */
		Type type;
	}

	/**
	 * Property types.
	 */
	private static enum Type {
		BOOLEAN, DOUBLE, INTEGER, LONG, STRING
	}

	/** Master or default record. */
	private Record masterRecord;
	/** The indexes of the value fields. */
	private int[] indexes;
	/** Indexes map. */
	private HashMap<String, Integer> mapIndexes;
	/** Optional record values stroed in data properties. */
	private List<Property> properties = new ArrayList<>();

	/**
	 * Constructor assinging only the master record.
	 * 
	 * @param masterRecord The master record.
	 */
	public DataConverter(Record masterRecord) {
		this.masterRecord = masterRecord;
		int length = 0;
		for (int i = 1; i < masterRecord.size(); i++) {
			Field field = masterRecord.getField(i);
			if (field.isDouble()) {
				length++;
			}
		}
		indexes = new int[length];
		mapIndexes = new HashMap<>();
		int index = 0;
		for (int i = 1; i < masterRecord.size(); i++) {
			Field field = masterRecord.getField(i);
			if (field.isDouble() && field.isPersistent()) {
				indexes[index] = i;
				mapIndexes.put(field.getAlias(), index);
				index++;
			}
		}
	}

	/**
	 * Generic constructor, indicating the indexes.
	 * 
	 * @param masterRecord Master record.
	 * @param indexes      Indexes to map.
	 */
	public DataConverter(Record masterRecord, int... indexes) {
		this.masterRecord = masterRecord;
		this.indexes = indexes;
		this.mapIndexes = new HashMap<>();
		for (int i = 0; i < indexes.length; i++) {
			int index = indexes[i];
			if (index <= 0 || index >= masterRecord.size()) {
				throw new IllegalArgumentException("Invalid index " + index);
			}
			String alias = masterRecord.getField(index).getAlias();
			mapIndexes.put(alias, i);
		}
	}

	/**
	 * Add a field index to store the data as properties.
	 * 
	 * @param index The field index.
	 */
	public void addProperty(int index) {
		if (index < 0 || index >= masterRecord.size()) {
			throw new IllegalArgumentException("Invalid field index: " + index);
		}
		String alias = masterRecord.getField(index).getAlias();
		Type type = null;
		switch (masterRecord.getField(index).getType()) {
		case BOOLEAN:
			type = Type.BOOLEAN;
			break;
		case DOUBLE:
			type = Type.DOUBLE;
			break;
		case INTEGER:
			type = Type.INTEGER;
			break;
		case LONG:
			type = Type.LONG;
			break;
		case STRING:
			type = Type.STRING;
			break;
		default:
			throw new IllegalArgumentException(
				"Invalid field data type: " + masterRecord.getField(index).getType());
		}
		Property property = new Property();
		property.index = index;
		property.alias = alias;
		property.type = type;
		properties.add(property);
	}

	/**
	 * Add a field alias to store the data as properties.
	 * 
	 * @param alias The field alias.
	 */
	public void addProperty(String alias) {
		int index = masterRecord.getFieldList().getFieldIndex(alias);
		if (index < 0) {
			throw new IllegalArgumentException("Invalid field alias: " + alias);
		}
		addProperty(index);
	}

	/**
	 * Retur the alias of the data index.
	 * 
	 * @param index The data index.
	 * @return Tha corresponding field alias.
	 */
	public String getAlias(int index) {
		if (index < 0 || index > indexes.length) {
			throw new ArrayIndexOutOfBoundsException("Invalid index " + index);
		}
		return masterRecord.getField(indexes[index]).getAlias();
	}

	/**
	 * Return the data given the record.
	 * 
	 * @param record The record.
	 * @return The data.
	 */
	public Data getData(Record record) {
		long time = record.getValue(0).getLong();
		double[] values = new double[indexes.length];
		for (int i = 0; i < indexes.length; i++) {
			values[i] = record.getValue(indexes[i]).getDouble();
		}
		Data data = new Data(time, values);
		if (!properties.isEmpty()) {
			for (Property property : properties) {
				switch (property.type) {
				case BOOLEAN:
					data.getProperties().setBoolean(
						property.alias,
						record.getValue(property.index).getBoolean());
					break;
				case DOUBLE:
					data.getProperties().setDouble(
						property.alias,
						record.getValue(property.index).getDouble());
					break;
				case INTEGER:
					data.getProperties().setInteger(
						property.alias,
						record.getValue(property.index).getInteger());
					break;
				case LONG:
					data.getProperties().setLong(
						property.alias,
						record.getValue(property.index).getLong());
					break;
				case STRING:
					data.getProperties().setString(
						property.alias,
						record.getValue(property.index).getString());
					break;
				}
			}
		}
		return data;
	}

	/**
	 * Return the data index given a field alias or name.
	 * 
	 * @param alias The field alias.
	 * @return The data index.
	 */
	public int getIndex(String alias) {
		Integer index = mapIndexes.get(alias);
		return (index == null ? -1 : index);
	}

	/**
	 * Return the record given the data.
	 * 
	 * @param data The data.
	 * @return The record.
	 */
	public Record getRecord(Data data) {
		Record record = Record.copy(masterRecord);
		record.setValue(0, new Value(data.getTime()));
		for (int i = 0; i < indexes.length; i++) {
			double value = data.getValue(i);
			int index = indexes[i];
			record.setValue(index, new Value(value));
		}
		if (!properties.isEmpty()) {
			for (Property property : properties) {
				switch (property.type) {
				case BOOLEAN:
					record.setValue(
						property.index,
						data.getProperties().getBoolean(property.alias));
					break;
				case DOUBLE:
					record.setValue(
						property.index,
						data.getProperties().getDouble(property.alias));
					break;
				case INTEGER:
					record.setValue(
						property.index,
						data.getProperties().getInteger(property.alias));
					break;
				case LONG:
					record.setValue(
						property.index,
						data.getProperties().getLong(property.alias));
					break;
				case STRING:
					record.setValue(
						property.index,
						data.getProperties().getString(property.alias));
					break;
				}
			}
		}
		return record;
	}
}
