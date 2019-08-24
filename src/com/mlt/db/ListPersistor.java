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

package com.mlt.db;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.Year;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

import com.mlt.util.CacheMap;
import com.mlt.util.Logs;
import com.mlt.util.Numbers;

/**
 * A list persistor can access records by index ordered by an order.
 *
 * @author Miquel Sas
 */
public class ListPersistor implements Persistor {

	/** Enum move direction. */
	private enum Move {
		BACKWARD, FORWARD
	};

	/** Refresh task when the source is sensitive. */
	private class RefreshTask extends TimerTask {
		@Override
		public void run() {
			clearLimits();
		}
	}

	/** Enum the count side, before or after the approximate time. */
	private enum Side {
		AFTER, BEFORE
	}

	/**
	 * Return the boolean factored between lower and upper.
	 * 
	 * @param lower  The lower value.
	 * @param upper  The upper value.
	 * @param factor The factor.
	 * @return The result value.
	 */
	private static boolean getBoolean(boolean lower, boolean upper, double factor) {
		double lowerNum = (lower ? 1 : -1);
		double upperNum = (upper ? 1 : -1);
		double resultNum = getDouble(lowerNum, upperNum, factor);
		return (resultNum >= 0 ? true : false);
	}

	/**
	 * Return the byte array factored between lower and upper.
	 * 
	 * @param lower  The lower value.
	 * @param upper  The upper value.
	 * @param factor The factor.
	 * @return The result value.
	 */
	private static byte[] getByteArray(byte[] lower, byte[] upper, double factor) {
		int length = Math.max(lower.length, upper.length);
		byte[] result = new byte[length];
		for (int i = 0; i < length; i++) {
			double dLower = (i < lower.length ? lower[i] : 0);
			double dUpper = (i < upper.length ? upper[i] : 0);
			double dFactor = Math.abs((dUpper - dLower) * factor);
			double dValue = 0;
			if (dLower < dUpper) {
				dValue = dLower + dFactor;
			} else {
				dValue = dLower - dFactor;
			}
			dValue = Numbers.round(dValue, 0);
			result[i] = Double.valueOf(dValue).byteValue();
		}
		return result;
	}

	/**
	 * Return the number factored between lower and upper.
	 * 
	 * @param lower  The lower value.
	 * @param upper  The upper value.
	 * @param factor The factor.
	 * @return The result value.
	 */
	private static double getDouble(double lower, double upper, double factor) {
		if (factor < 0 || factor > 1) throw new IllegalArgumentException();
		double delta = Math.abs((upper - lower) * factor);
		double result = 0;
		if (lower < upper) {
			result = lower + delta;
		} else {
			result = lower - delta;
		}
		return result;
	}

	/**
	 * Return the number factored between lower and upper.
	 * 
	 * @param lower  The lower value.
	 * @param upper  The upper value.
	 * @param factor The factor.
	 * @return The result value.
	 */
	private static int getInteger(int lower, int upper, double factor) {
		double result = getDouble((double) lower, (double) upper, factor);
		return Double.valueOf(Numbers.round(result, 0)).intValue();
	}

	/**
	 * Return the local date factored from lower to upper.
	 * 
	 * @param lower  The lower value.
	 * @param upper  The upper value.
	 * @param factor The factor.
	 * @return The result value.
	 */
	private static LocalDate getLocalDate(LocalDate lower, LocalDate upper, double factor) {

		int yearLower = lower.getYear();
		int yearUpper = upper.getYear();
		int yearResult = getInteger(yearLower, yearUpper, factor);

		int monthLower = lower.getMonthValue();
		int monthUpper = upper.getMonthValue();
		int monthResult = getInteger(monthLower, monthUpper, factor);

		int dayLower = lower.getDayOfMonth();
		int dayUpper = upper.getDayOfMonth();
		int dayResult = getInteger(dayLower, dayUpper, factor);

		Year year = Year.of(yearResult);
		Month month = Month.of(monthResult);
		int length = month.length(year.isLeap());
		if (dayResult > length) {
			dayResult = length;
		}

		return LocalDate.of(yearResult, monthResult, dayResult);
	}

	/**
	 * Return the local date-time factored from lower to upper.
	 * 
	 * @param lower  The lower value.
	 * @param upper  The upper value.
	 * @param factor The factor.
	 * @return The result value.
	 */
	private static LocalDateTime getLocalDateTime(
		LocalDateTime lower,
		LocalDateTime upper,
		double factor) {

		LocalDate dateLower = lower.toLocalDate();
		LocalDate dateUpper = upper.toLocalDate();
		LocalDate dateResult = getLocalDate(dateLower, dateUpper, factor);

		LocalTime timeLower = lower.toLocalTime();
		LocalTime timeUpper = upper.toLocalTime();
		LocalTime timeResult = getLocalTime(timeLower, timeUpper, factor);

		return LocalDateTime.of(dateResult, timeResult);
	}

	/**
	 * Return the local time factored from lower to upper.
	 * 
	 * @param lower  The lower value.
	 * @param upper  The upper value.
	 * @param factor The factor.
	 * @return The result value.
	 */
	private static LocalTime getLocalTime(LocalTime lower, LocalTime upper, double factor) {

		int hourLower = lower.getHour();
		int hourUpper = upper.getHour();
		int hourResult = getInteger(hourLower, hourUpper, factor);

		int minuteLower = lower.getMinute();
		int minuteUpper = upper.getMinute();
		int minuteResult = getInteger(minuteLower, minuteUpper, factor);

		int secondLower = lower.getSecond();
		int secondUpper = upper.getSecond();
		int secondResult = getInteger(secondLower, secondUpper, factor);

		return LocalTime.of(hourResult, minuteResult, secondResult);
	}

	/**
	 * Return the number factored between lower and upper.
	 * 
	 * @param lower  The lower value.
	 * @param upper  The upper value.
	 * @param factor The factor.
	 * @return The result value.
	 */
	private static long getLong(long lower, long upper, double factor) {
		double result = getDouble((double) lower, (double) upper, factor);
		return Double.valueOf(Numbers.round(result, 0)).longValue();
	}

	/**
	 * Return the primary key factored between lower and upper.
	 * 
	 * @param lower  The lower value.
	 * @param upper  The upper value.
	 * @param factor The factor.
	 * @return The result value.
	 */
	private static OrderKey getOrderKey(OrderKey lower, OrderKey upper, double factor) {
		if (lower.size() != upper.size()) {
			throw new IllegalArgumentException();
		}
		OrderKey result = new OrderKey();
		for (int i = 0; i < lower.size(); i++) {
			Value lowerValue = lower.get(i).getValue();
			Value upperValue = upper.get(i).getValue();
			result.add(getValue(lowerValue, upperValue, factor));
		}
		return result;
	}

	/**
	 * Return a string that is between the lower and the upper, applying a factor.
	 * If the factor is 0.0, then return the lower string, and if the factor is 1.0,
	 * then return the upper string.
	 * 
	 * @param lower  The lower string.
	 * @param upper  The upper string.
	 * @param factor The factor.
	 * @return The factor string.
	 */
	private static String getString(String lower, String upper, double factor) {
		if (lower == null) throw new NullPointerException();
		if (upper == null) throw new NullPointerException();
		if (factor < 0 || factor > 1) throw new IllegalArgumentException();

		StringBuilder result = new StringBuilder();
		try {
			byte[] bytesLower = lower.getBytes("UTF-16");
			byte[] bytesUpper = upper.getBytes("UTF-16");
			int length = Math.max(bytesLower.length, bytesUpper.length);
			byte[] bytesResult = new byte[length];
			for (int i = 0; i < length; i++) {
				double dLower = (i < bytesLower.length ? bytesLower[i] : 0);
				double dUpper = (i < bytesUpper.length ? bytesUpper[i] : 0);
				double dFactor = Math.abs((dUpper - dLower) * factor);
				double dValue = 0;
				if (dLower < dUpper) {
					dValue = dLower + dFactor;
				} else {
					dValue = dLower - dFactor;
				}
				dValue = Numbers.round(dValue, 0);
				bytesResult[i] = Double.valueOf(dValue).byteValue();
			}
			result.append(new String(bytesResult, "UTF-16"));
		} catch (UnsupportedEncodingException exc) {
			exc.printStackTrace();
		}
		return result.toString();
	}

	/**
	 * Return the value factored between lower and upper.
	 * 
	 * @param lower  The lower value.
	 * @param upper  The upper value.
	 * @param factor The factor.
	 * @return The result value.
	 */
	private static Value getValue(Value lower, Value upper, double factor) {
		if (lower.getType() != upper.getType()) {
			throw new IllegalArgumentException();
		}

		switch (lower.getType()) {
		case BOOLEAN:
			return new Value(getBoolean(lower.getBoolean(), upper.getBoolean(), factor));
		case BYTEARRAY:
			return new Value(getByteArray(lower.getByteArray(), upper.getByteArray(), factor));
		case DATE:
			return new Value(getLocalDate(lower.getDate(), upper.getDate(), factor));
		case DATETIME:
			return new Value(getLocalDateTime(lower.getDateTime(), upper.getDateTime(), factor));
		case DECIMAL:
			return new Value(getLocalDateTime(lower.getDateTime(), upper.getDateTime(), factor));
		case DOUBLE:
			return new Value(getDouble(lower.getDouble(), upper.getDouble(), factor));
		case INTEGER:
			return new Value(getInteger(lower.getInteger(), upper.getInteger(), factor));
		case LONG:
			return new Value(getLong(lower.getLong(), upper.getLong(), factor));
		case STRING:
			return new Value(getString(lower.getString(), upper.getString(), factor));
		case TIME:
			return new Value(getLocalTime(lower.getTime(), upper.getTime(), factor));
		}
		throw new IllegalStateException();
	}

	/** Associated persistor. */
	private Persistor persistor;
	/** Applying order. */
	private Order order;
	/** A timer that refreshes the status data. */
	private Timer refreshTimer;
	/** Lock used when the source is sensitive. */
	private ReentrantLock lock = new ReentrantLock();
	/** The size or number of records of the data source. */
	private int size = -1;
	/** First record in the source. */
	private Record firstRecord;
	/** Last record in the source. */
	private Record lastRecord;
	/** A map to cache records. */
	private CacheMap<Integer, Record> mapRecords = new CacheMap<>(10000);
	/** The page size, used to read and cache a page of records. */
	private int pageSize = 100;
	/** An global criteria. */
	private Criteria globalCriteria;

	/**
	 * Constructor.
	 * 
	 * @param persistor The underlying persistor.
	 */
	public ListPersistor(Persistor persistor) {
		super();
		/* Must have a primary key. */
		FieldList fields = persistor.getFieldList();
		if (fields.getPrimaryKeyFields().isEmpty()) {
			throw new IllegalArgumentException("Persistor must have primary key fields");
		}
		this.persistor = persistor;
		this.order = persistor.getFieldList().getPrimaryOrder();
	}

	/**
	 * Constructor.
	 * 
	 * @param persistor The underlying persistor.
	 */
	public ListPersistor(Persistor persistor, Order order) {
		super();
		this.persistor = persistor;
		this.order = order;
	}

	/**
	 * Clear the cache.
	 */
	private void clearCache() {
		try {
			lock.lock();
			mapRecords.clear();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Clear size, first and last record.
	 */
	private void clearLimits() {
		try {
			lock.lock();
			size = -1;
			firstRecord = null;
			lastRecord = null;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long count(Criteria criteria) throws PersistorException {
		return persistor.count(criteria);
	}

	/**
	 * Count records before or after.
	 * 
	 * @param key  The key.
	 * @param side The side (AFTER=GT/BEFORE=LT)
	 * @return The number of records.
	 */
	private int countRecords(OrderKey key, Side side) {
		Criteria criteria = getCriteriaSide(key, side);
		int count = 0;
		try {
			count = (int) persistor.count(criteria);
		} catch (PersistorException exc) {
			Logs.catching(exc);
		}
		return count;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int delete(Criteria criteria) throws PersistorException {
		clearCache();
		clearLimits();
		return persistor.delete(criteria);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int delete(Record record) throws PersistorException {
		clearCache();
		clearLimits();
		return persistor.delete(record);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean exists(OrderKey primaryKey) throws PersistorException {
		return persistor.exists(primaryKey);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean exists(Record record) throws PersistorException {
		return persistor.exists(record);
	}

	/**
	 * Return the criteria to apply with a key and a move/side direction.
	 * 
	 * @param key  The key.
	 * @param move The direction.
	 * @return The criteria to apply (GT or LT)
	 */
	private Criteria getCriteriaMove(OrderKey key, Move move) {
		Criteria criteriaSegments = new Criteria(Criteria.OR);
		int segments = order.size();
		while (segments > 0) {
			Criteria criteriaSegment = new Criteria();
			if (segments == 1) {
				Field field = order.getField(0);
				Value value = key.getValue(0);
				if (move == Move.FORWARD) {
					criteriaSegment.add(Condition.fieldGT(field, value));
				} else {
					criteriaSegment.add(Condition.fieldLT(field, value));
				}
			} else {
				for (int i = 0; i < segments; i++) {
					Field field = order.getField(i);
					Value value = key.getValue(i);
					if (i < segments - 1) {
						criteriaSegment.add(Condition.fieldEQ(field, value));
					} else {
						if (move == Move.FORWARD) {
							criteriaSegment.add(Condition.fieldGT(field, value));
						} else {
							criteriaSegment.add(Condition.fieldLT(field, value));
						}
					}
				}
			}
			criteriaSegments.add(criteriaSegment);
			segments--;
		}
		Criteria criteria = new Criteria();
		if (globalCriteria != null) criteria.add(globalCriteria);
		criteria.add(criteriaSegments);
		return criteria;
	}

	/**
	 * Return the criteria to apply with a key and a move/side direction.
	 * 
	 * @param key  The key.
	 * @param side The side.
	 * @return The criteria to apply (GT or LT)
	 */
	private Criteria getCriteriaSide(OrderKey key, Side side) {
		if (side == Side.AFTER) {
			return getCriteriaMove(key, Move.FORWARD);
		}
		return getCriteriaMove(key, Move.BACKWARD);
	}

	/*
	 * Persistor implementation.
	 */

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PersistorDDL getDDL() {
		return persistor.getDDL();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Record getDefaultRecord() {
		return persistor.getDefaultRecord();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field getField(int index) {
		return persistor.getField(index);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field getField(String alias) {
		return getField(alias);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getFieldCount() {
		return persistor.getFieldCount();
	}

	@Override
	public int getFieldIndex(String alias) {
		return persistor.getFieldIndex(alias);
	}

	@Override
	public FieldList getFieldList() {
		return persistor.getFieldList();
	}

	/**
	 * Return the first record.
	 * 
	 * @return The record.
	 */
	private Record getFirstRecord() {
		if (firstRecord == null) {
			try {
				lock.lock();
				order.setAscending();
				Criteria criteria = new Criteria();
				if (globalCriteria != null) criteria.add(globalCriteria);
				RecordIterator iter = persistor.iterator(criteria, order);
				if (iter.hasNext()) {
					firstRecord = iter.next();
				}
				iter.close();
			} catch (PersistorException exc) {
				Logs.catching(exc);
			} finally {
				lock.unlock();
			}
		}
		return firstRecord;
	}

	/**
	 * Return the last record.
	 * 
	 * @return The record.
	 */
	private Record getLastRecord() {
		if (lastRecord == null) {
			try {
				lock.lock();
				order.setDescending();
				Criteria criteria = new Criteria();
				if (globalCriteria != null) criteria.add(globalCriteria);
				RecordIterator iter = persistor.iterator(criteria, order);
				if (iter.hasNext()) {
					lastRecord = iter.next();
				}
				iter.close();
			} catch (PersistorException exc) {
				Logs.catching(exc);
			} finally {
				lock.unlock();
			}
		}
		return lastRecord;
	}

	/**
	 * Return an order key that approximately corresponds to the index.
	 * 
	 * @param index The required index.
	 * @return The order key that approximately corresponds to the index.
	 */
	private OrderKey getOrderKey(int index) {
		double factor = ((double) index) / ((double) (size() - 1));
		OrderKey lower = getFirstRecord().getPrimaryKey();
		OrderKey upper = getLastRecord().getPrimaryKey();
		return getOrderKey(lower, upper, factor);
	}

	/**
	 * Return a record by index.
	 * 
	 * @param index The index of the record.
	 * @return The record.
	 */
	public Record getRecord(int index) {
		try {
			lock.lock();

			/*
			 * First record is directly cached.
			 */
			if (index == 0) {
				return getFirstRecord();
			}

			/*
			 * Last record is directly cached.
			 */
			if (index >= size() - 1) {
				return getLastRecord();
			}

			/*
			 * Check whether the record with the given index is already mapped, and if so,
			 * just return it.
			 */
			Record record = mapRecords.get(index);
			if (record != null) {
				return record;
			}

			/*
			 * The record is not available. Get an approximate (proportional) order key.
			 */
			OrderKey key = getOrderKey(index);

			/*
			 * Check whether a record with this approximate key exist. If so, map it and
			 * load a page forward and backward.
			 */
			record = getRecordByKey(key);
			if (record != null) {
				mapRecords.put(index, record);
				loadPage(index, key);
			}

			/*
			 * The calculated approximate time does not correspond to a physical record.
			 * Count records before and after and decide the direction and the number of
			 * records to skip to find the record with the required index.
			 * 
			 * To increase performance count the smaller side.
			 */
			Side side = ((index < size() / 2) ? Side.BEFORE : Side.AFTER);
			int count = countRecords(key, side);
			int skip = 0;
			Move move = null;
			int size = size();

			/* Case 1: count before and count > index */
			if (side == Side.BEFORE && count > index) {
				skip = count - index - 1;
				move = Move.BACKWARD;
			}
			/* Case 2: count before and count == index */
			if (side == Side.BEFORE && count == index) {
				skip = 0;
				move = Move.FORWARD;
			}
			/* Case 3: count before and count < index */
			if (side == Side.BEFORE && count < index) {
				skip = index - count;
				move = Move.FORWARD;
			}
			/* Case 4: count after and count > (size - index) */
			if (side == Side.AFTER && count > (size - index)) {
				skip = count - (size - index);
				move = Move.FORWARD;
			}
			/* Case 5: count after and count == (size - index) */
			if (side == Side.AFTER && count == (size - index)) {
				skip = 0;
				move = Move.FORWARD;
			}
			/* Case 6: count after and count < (size - index) */
			if (side == Side.AFTER && count < (size - index)) {
				skip = (size - index) - count - 1;
				move = Move.BACKWARD;
			}
			record = getRecord(key, skip, move);
			if (record != null) {
				key = record.getOrderKey(order);
				mapRecords.put(index, record);
				loadPage(index, key);
			}
			if (record == null) {
				Logs.warning("Null record not expected");
			}
			return record;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Record getRecord(List<Value> primaryKeyValues) throws PersistorException {
		return persistor.getRecord(primaryKeyValues);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Record getRecord(OrderKey primaryKey) throws PersistorException {
		return persistor.getRecord(primaryKey);
	}

	/**
	 * Return a record from the approximate key.
	 * 
	 * @param key  The key.
	 * @param skip Number of records to skip.
	 * @param move Skip direction.
	 * @return The record or null.
	 */
	private Record getRecord(OrderKey key, int skip, Move move) {
		Record record = null;
		try {
			Criteria criteria = getCriteriaMove(key, move);
			if (move == Move.BACKWARD) {
				order.setDescending();
			}
			if (move == Move.FORWARD) {
				order.setAscending();
			}
			RecordIterator iter = persistor.iterator(criteria, order);
			int skept = 0;
			while (skept <= skip && iter.hasNext()) {
				record = iter.next();
				skept++;
			}
			iter.close();
		} catch (PersistorException exc) {
			Logs.catching(exc);
		}
		return record;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Record getRecord(Value... primaryKeyValues) throws PersistorException {
		return persistor.getRecord(primaryKeyValues);
	}

	/**
	 * Return the first record with the key, or null.
	 * 
	 * @param key The key.
	 * @return The record or null.
	 */
	private Record getRecordByKey(OrderKey key) {
		Record record = null;
		try {
			Criteria criteria = new Criteria();
			for (int i = 0; i < order.size(); i++) {
				Field field = order.getField(i);
				Value value = key.getValue(i);
				criteria.add(Condition.fieldEQ(field, value));
			}
			order.setAscending();
			RecordIterator iter = persistor.iterator(criteria, order);
			if (iter.hasNext()) {
				record = iter.next();
			}
			iter.close();
		} catch (PersistorException exc) {
			Logs.catching(exc);
		}
		return record;
	}

	/*
	 * Persistor implementation.
	 */

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Transaction getTransaction() {
		return persistor.getTransaction();
	}

	/*
	 * Persistor implementation.
	 */

	/**
	 * {@inheritDoc}
	 */
	@Override
	public View getView() {
		return persistor.getView();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int insert(Record record) throws PersistorException {
		clearCache();
		clearLimits();
		return persistor.insert(record);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RecordIterator iterator(Criteria criteria) throws PersistorException {
		return persistor.iterator(criteria);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RecordIterator iterator(Criteria criteria, Order order) throws PersistorException {
		return persistor.iterator(criteria, order);
	}

	/**
	 * Load and map a page of records from the reference record.
	 * 
	 * @param index Reference record index.
	 * @param key   Reference record key.
	 */
	private void loadPage(int index, OrderKey key) {
		try {
			order.setAscending();

			/* Check if the page is already loaded and mapped. */
			int countMapped = 0;
			for (int i = 0; i < pageSize; i++) {
				Record record = mapRecords.get(index + 1);
				if (record != null) {
					key = record.getOrderKey(order);
					index++;
					countMapped++;
				} else {
					break;
				}
			}

			/* If the page was all loaded and mapped, we are done. */
			if (countMapped == pageSize) {
				return;
			}

			/* Load and map the rest of records up to the page size. */
			int toLoad = pageSize - countMapped;
			Criteria criteria = getCriteriaSide(key, Side.AFTER);
			RecordIterator iter = persistor.iterator(criteria, order);
			while (iter.hasNext()) {
				Record record = iter.next();
				mapRecords.put(++index, record);
				if (--toLoad <= 0) {
					break;
				}
			}
			iter.close();

		} catch (PersistorException exc) {
			Logs.catching(exc);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ValueMap max(Criteria criteria, int... indexes) throws PersistorException {
		return persistor.max(criteria, indexes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ValueMap max(Criteria criteria, String... aliases) throws PersistorException {
		return persistor.max(criteria, aliases);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ValueMap min(Criteria criteria, int... indexes) throws PersistorException {
		return persistor.min(criteria, indexes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ValueMap min(Criteria criteria, String... aliases) throws PersistorException {
		return persistor.min(criteria, aliases);
	}

	/*
	 * Persistor implementation.
	 */

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean refresh(Record record) throws PersistorException {
		clearCache();
		clearLimits();
		return persistor.refresh(record);
	}

	/*
	 * Persistor implementation.
	 */

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int save(Record record) throws PersistorException {
		clearCache();
		clearLimits();
		return persistor.save(record);
	}

	/*
	 * Persistor implementation.
	 */

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RecordSet select(Criteria criteria) throws PersistorException {
		return persistor.select(criteria);
	}

	/*
	 * Persistor implementation.
	 */

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RecordSet select(Criteria criteria, Order order) throws PersistorException {
		return persistor.select(criteria, order);
	}

	/**
	 * Set the cache factor.
	 * 
	 * @param size The size.
	 */
	public void setCacheFactor(double factor) {
		mapRecords.setCacheFactor(factor);
	}

	/**
	 * Set the cache size.
	 * 
	 * @param size The size.
	 */
	public void setCacheSize(int size) {
		mapRecords.setCacheSize(size);
	}

	/**
	 * Set the global criteria.
	 * 
	 * @param globalCriteria The global criteria that applies to all data acceses.
	 */
	public void setGlobalCriteria(Criteria globalCriteria) {
		this.globalCriteria = globalCriteria;
	}

	/**
	 * Set the page size.
	 * 
	 * @param size The size.
	 */
	public void setPageSize(int size) {
		pageSize = size;
	}

	/**
	 * Set whether this source is sensitive to size increases. If the refresh delay
	 * is less than or equal to zero, the source is not sensitive.
	 * 
	 * @param refreshDelay The refresh delay.
	 */
	public void setSensitive(long refreshDelay) {
		if (refreshDelay > 0) {
			refreshTimer = new Timer("Refresh-Size", true);
			refreshTimer.schedule(new RefreshTask(), refreshDelay, refreshDelay);
		} else {
			if (refreshTimer != null) refreshTimer.cancel();
			refreshTimer = null;
		}
	}

	/**
	 * Return the size or number of records of the data source.
	 * 
	 * @return The size.
	 */
	public int size() {
		if (size < 0) {
			try {
				lock.lock();
				Criteria criteria = new Criteria();
				if (globalCriteria != null) criteria.add(globalCriteria);
				size = (int) persistor.count(criteria);
			} catch (PersistorException exc) {
				Logs.catching(exc);
			} finally {
				lock.unlock();
			}
		}
		return size;
	}

	/*
	 * Persistor implementation.
	 */

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ValueMap sum(Criteria criteria, int... indexes) throws PersistorException {
		return persistor.sum(criteria, indexes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ValueMap sum(Criteria criteria, String... aliases) throws PersistorException {
		return persistor.sum(criteria, aliases);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int update(Criteria criteria, ValueMap map) throws PersistorException {
		clearCache();
		clearLimits();
		return persistor.update(criteria, map);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int update(Record record) throws PersistorException {
		clearCache();
		clearLimits();
		return persistor.update(record);
	}
}
