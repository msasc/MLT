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

import java.awt.Font;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.LongStream;

import com.mlt.desktop.EditContext;
import com.mlt.desktop.control.TextArea;
import com.mlt.desktop.layout.Dimension;
import com.mlt.desktop.layout.Fill;
import com.mlt.util.Strings;

/**
 * A generator of random data, mainly records and recordsets, useful for tests.
 *
 * @author Miquel Sas
 */
public class RandomData {

	/**
	 * Test data generation using a pre-defined field list.
	 */
	public static class Test {

		public static final String CARTICLE = "CARTICLE";
		public static final String DARTICLE = "DARTICLE";
		public static final String CBUSINESS = "CBUSINESS";
		public static final String TCREATED = "TCREATED";
		public static final String QSALES = "QSALES";
		public static final String QPROD = "QPROD";
		public static final String QPURCH = "QPURCH";
		public static final String ICHECKED = "ICHECKED";
		public static final String IREQUIRED = "IREQUIRED";
		public static final String ISTATUS = "ISTATUS";

		/** Random data. */
		private RandomData rd;
		/** Field list. */
		private FieldList fields;

		/**
		 * Constructor.
		 */
		public Test() {
			super();

			rd = new RandomData();

			fields = new FieldList();

			Field fCARTICLE = new Field();
			fCARTICLE.setName("CARTICLE");
			fCARTICLE.setAlias("CARTICLE");
			fCARTICLE.setTitle("Artice code");
			fCARTICLE.setLabel("Artice code");
			fCARTICLE.setHeader("Artice");
			fCARTICLE.setType(Types.STRING);
			fCARTICLE.setLength(10);
			fCARTICLE.setFixedWidth(true);
			fCARTICLE.setPrimaryKey(true);
			fCARTICLE.setUppercase(true);
			fields.addField(fCARTICLE);

			Field fDTITLE = new Field();
			fDTITLE.setName("DTITLE");
			fDTITLE.setAlias("DTITLE");
			fDTITLE.setTitle("Title");
			fDTITLE.setLabel("Title");
			fDTITLE.setHeader("Title");
			fDTITLE.setType(Types.STRING);
			fDTITLE.setLength(100);
			fDTITLE.setFixedWidth(false);
			fields.addField(fDTITLE);

			Field fDARTICLE = new Field();
			fDARTICLE.setName("DARTICLE");
			fDARTICLE.setAlias("DARTICLE");
			fDARTICLE.setTitle("Description");
			fDARTICLE.setLabel("Description");
			fDARTICLE.setHeader("Description");
			fDARTICLE.setType(Types.STRING);
			fDARTICLE.setLength(1000);
			fDARTICLE.setFixedWidth(false);
			TextArea textArea = new TextArea();
			textArea.setPreferredSize(new Dimension(300, 200));
			textArea.setMinimumSize(new Dimension(300, 200));
			textArea.setFont(new Font("Courier", Font.PLAIN, 14));
			textArea.setLineWrap(true);
			fDARTICLE.getProperties().setObject(EditContext.EDIT_FIELD, textArea);
			fDARTICLE.getProperties().setObject(EditContext.FILL, Fill.BOTH);
//			TextPane textPane = new TextPane();
//			textPane.setPreferredSize(new Dimension(300, 400));
//			textPane.setMinimumSize(new Dimension(300, 400));
//			textPane.setFont(new Font("Courier", Font.PLAIN, 14));
//			fDARTICLE.getProperties().setObject(EditContext.EDIT_FIELD, textPane);
//			fDARTICLE.getProperties().setObject(EditContext.FILL, Fill.BOTH);
			fields.addField(fDARTICLE);

			Field fCBUSINESS = new Field();
			fCBUSINESS.setName("CBUSINESS");
			fCBUSINESS.setAlias("CBUSINESS");
			fCBUSINESS.setTitle("Business code");
			fCBUSINESS.setLabel("Business code");
			fCBUSINESS.setHeader("Business");
			fCBUSINESS.setType(Types.STRING);
			fCBUSINESS.setLength(4);
			fields.addField(fCBUSINESS);

			Field fTCREATED = new Field();
			fTCREATED.setName("TCREATED");
			fTCREATED.setAlias("TCREATED");
			fTCREATED.setTitle("Date created");
			fTCREATED.setLabel("Date created");
			fTCREATED.setHeader("Date created");
			fTCREATED.setType(Types.DATE);
			fields.addField(fTCREATED);

			Field fQSALES = new Field();
			fQSALES.setName("QSALES");
			fQSALES.setAlias("QSALES");
			fQSALES.setTitle("Sales");
			fQSALES.setLabel("Sales");
			fQSALES.setHeader("Sales");
			fQSALES.setType(Types.DECIMAL);
			fQSALES.setLength(14);
			fQSALES.setDecimals(4);
			fields.addField(fQSALES);

			Field fQPROD = new Field();
			fQPROD.setName("QPROD");
			fQPROD.setAlias("QPROD");
			fQPROD.setTitle("Production");
			fQPROD.setLabel("Production");
			fQPROD.setHeader("Production");
			fQPROD.setType(Types.DECIMAL);
			fQPROD.setLength(14);
			fQPROD.setDecimals(4);
			fields.addField(fQPROD);

			Field fQPURCH = new Field();
			fQPURCH.setName("QPURCH");
			fQPURCH.setAlias("QPURCH");
			fQPURCH.setTitle("Purchases");
			fQPURCH.setLabel("Purchases");
			fQPURCH.setHeader("Purchases");
			fQPURCH.setType(Types.DECIMAL);
			fQPURCH.setLength(14);
			fQPURCH.setDecimals(4);
			fields.addField(fQPURCH);

			Field fICHECKED = new Field();
			fICHECKED.setName("ICHECKED");
			fICHECKED.setAlias("ICHECKED");
			fICHECKED.setTitle("Checked");
			fICHECKED.setLabel("Checked");
			fICHECKED.setHeader("Checked");
			fICHECKED.setType(Types.BOOLEAN);
			fICHECKED.setEditBooleanInCheckBox(true);
			fields.addField(fICHECKED);

			Field fIREQUIRED = new Field();
			fIREQUIRED.setName("IREQUIRED");
			fIREQUIRED.setAlias("IREQUIRED");
			fIREQUIRED.setTitle("Required");
			fIREQUIRED.setLabel("Required");
			fIREQUIRED.setHeader("Required");
			fIREQUIRED.setType(Types.BOOLEAN);
			fIREQUIRED.setEditBooleanInCheckBox(false);
			fields.addField(fIREQUIRED);

			Field fISTATUS = new Field();
			fISTATUS.setName("ISTATUS");
			fISTATUS.setAlias("ISTATUS");
			fISTATUS.setTitle("Status");
			fISTATUS.setLabel("Status");
			fISTATUS.setHeader("Status");
			fISTATUS.setType(Types.STRING);
			fISTATUS.setLength(2);
			fISTATUS.addPossibleValue("01", "Created");
			fISTATUS.addPossibleValue("02", "Acceptance");
			fISTATUS.addPossibleValue("03", "Accepted");
			fISTATUS.addPossibleValue("04", "Engineered");
			fISTATUS.addPossibleValue("05", "Produced");
			fISTATUS.addPossibleValue("06", "Sales");
			fISTATUS.addPossibleValue("07", "Obsolete");
			fields.addField(fISTATUS);

		}

		/**
		 * Return a copy of the internal test field list.
		 * 
		 * @return The copy of the field list.
		 */
		public FieldList getFieldList() {
			return new FieldList(fields);
		}

		/**
		 * Return a default record using the test field list.
		 */
		public Record getRecordDefault() {
			return new Record(fields);
		}

		/**
		 * Return a random record using the test field list.
		 */
		public Record getRecordRandom() {
			return rd.getRecord(fields);
		}

		/**
		 * Return a random recordset of the given size using the test field list.
		 * 
		 * @param size The size of the recordset.
		 * @return The random recordset.
		 */
		public RecordSet getRecordSet(int size) {
			RecordList rs = new RecordList(fields);
			for (int i = 0; i < size; i++) {
				rs.add(getRecordRandom());
			}
			return rs;
		}
	}

	/**
	 * Code pattern. Any character in the pattern not included in the valid pattern
	 * characters is treated literally.
	 * Valid pattern characters are:
	 * <ul>
	 * <li><b>#</b> a digit</li>
	 * <li><b>A</b> an upper case alpha numerical digit or letter</li>
	 * <li><b>a</b> a lower case alpha numerical digit or letter</li>
	 * <li><b>?</b> an alpha numerical digit or letter with random case</li>
	 * <li><b>L</b> an upper case letter</li>
	 * <li><b>l</b> a lower case letter</li>
	 * <li><b>!</b> a letter with random case</li>
	 * </ul>
	 */
	public static class CodePattern {

		/**
		 * Pattern.
		 */
		private String pattern;
		/**
		 * Source letters.
		 */
		private String letters;
		/**
		 * Source digits.
		 */
		private String digits;

		/**
		 * Constructor.
		 *
		 * @param pattern Pattern.
		 */
		public CodePattern(String pattern) {
			this(pattern, Strings.LETTERS_UPPER, Strings.DIGITS);
		}

		/**
		 * Constructor.
		 *
		 * @param pattern Pattern.
		 * @param letters Source letters.
		 * @param digits  Source digits.
		 */
		public CodePattern(String pattern, String letters, String digits) {
			super();
			this.pattern = pattern;
			this.letters = letters;
			this.digits = digits;
		}

		/**
		 * Return the pattern.
		 *
		 * @return The pattern.
		 */
		public String getPattern() {
			return pattern;
		}

		/**
		 * Return source the letters.
		 *
		 * @return The letters.
		 */
		public String getLetters() {
			return letters;
		}

		/**
		 * Return the source digits.
		 *
		 * @return The digits.
		 */
		public String getDigits() {
			return digits;
		}

	}

	/** Random. */
	private Random random;
	/** Time start for random dates, times and timestamps. */
	private Long timeStart;
	/** Time end for random dates, times and timestamps. */
	private Long timeEnd;
	/** LongStream to generate dates, times and timestamps. */
	private LongStream longStream;
	/** The long iterator. */
	private Iterator<Long> longIterator;

	/**
	 * Constructor.
	 */
	public RandomData() {
		super();
		this.random = new Random();
	}

	/**
	 * Constructor.
	 *
	 * @param timeStart Start time for dates, times or timestamps.
	 * @param timeEnd   Start time for dates, times or timestamps.
	 */
	public RandomData(Long timeStart, Long timeEnd) {
		super();
		this.random = new Random();
		if (timeStart >= timeEnd) {
			throw new IllegalArgumentException();
		}
		this.timeStart = timeStart;
		this.timeEnd = timeEnd;
	}

	/**
	 * Returns a random boolean.
	 *
	 * @return A randomly generated boolean.
	 */
	public boolean getBoolean() {
		return (random.nextInt(2) == 1);
	}

	/**
	 * Returns a random char within the source string.
	 *
	 * @param source The source string.
	 * @return The random char.
	 */
	public char getChar(String source) {
		int index = random.nextInt(source.length());
		return source.charAt(index);
	}

	/**
	 * Returns a random string.
	 *
	 * @param length The desired length of the string.
	 * @param source The source string from where to extract characters.
	 * @return The randomly generated string.
	 */
	public String getString(int length, String source) {
		return getString(length, source, false, true);
	}

	/**
	 * Returns a random string.
	 *
	 * @param length          The desired length of the string.
	 * @param source          The source string from where to extract characters.
	 * @param startWithSpaces Control starting with spaces.
	 * @param trim            Control trimming the result.
	 * @return The randomly generated string.
	 */
	public String getString(int length, String source, boolean startWithSpaces, boolean trim) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < length; i++) {
			char c = getChar(source);
			if (i == 0 && !startWithSpaces && c == ' ') {
				while (c == ' ') {
					c = getChar(source);
				}
			}
			b.append(c);
		}
		return (trim ? b.toString().trim() : b.toString());
	}

	/**
	 * Returns a random string.
	 */
	public String getString(int minLength, int maxLength, String source) {
		int length = minLength + random.nextInt(maxLength - minLength);
		return getString(length, source);
	}

	/**
	 * Return a text made of token.
	 * 
	 * @param length Text length.
	 * @return The tokens.
	 */
	public String getTokens(int length) {
		StringBuilder b = new StringBuilder();
		while (b.length() < length) {
			int bound = (length - b.length()) / 4;
			if (bound < 5) {
				bound = 5;
			}
			if (bound > 30) {
				bound = 30;
			}
			int tokenLength = random.nextInt(bound);
			while (tokenLength < 2) {
				tokenLength = random.nextInt(bound);
			}
			boolean vowel = getBoolean();
			for (int i = 0; i < tokenLength; i++) {
				b.append(getChar(vowel ? Strings.VOWELS_UPPER : Strings.CONSONANTS_UPPER));
				vowel = !vowel;
			}
			b.append(' ');
		}
		return b.toString().substring(0, length).trim();
	}

	/**
	 * Returns a random big decimal.
	 *
	 * @param length The total length.
	 * @return A randomly generated number with optional decimal places.
	 */
	public BigDecimal getDecimal(int length) {
		return getDecimal(length, 0);
	}

	/**
	 * Returns a random big decimal.
	 *
	 * @param length   The total length.
	 * @param decimals The number of decimal places.
	 * @return A randomly generated number with optional decimal places.
	 */
	public BigDecimal getDecimal(int length, int decimals) {
		int integer = random.nextInt(length - (decimals > 0 ? decimals + 1 : 0)) + 1;
		String integerPart = getString(integer, Strings.DIGITS);
		String decimalPart = getString(decimals, Strings.DIGITS);
		String number = integerPart + "." + decimalPart;
		return new BigDecimal(number).setScale(decimals, RoundingMode.HALF_UP);
	}

	public BigDecimal getDecimal(Field field) {
		if (!field.isNumber()) {
			throw new IllegalArgumentException("Field is not a number");
		}
		if (field.getDecimals() < 0) {
			throw new IllegalArgumentException("Field must have fixed zero or more decimals");
		}
		return getDecimal(field.getLength(), field.getDecimals());
	}

	/**
	 * Return a random code.
	 *
	 * @param pattern The code pattern.
	 * @return The random code.
	 */
	public String getCode(CodePattern pattern) {
		return getCode(pattern.getPattern(), pattern.getLetters(), pattern.getDigits());
	}

	/**
	 * Returns a random code based on the given pattern. Any character in the mask
	 * not included in the valid mask
	 * characters is treated literally. Valid mask characters are:
	 * <ul>
	 * <li><b>#</b> a digit</li>
	 * <li><b>A</b> an upper case alpha numerical digit or letter</li>
	 * <li><b>a</b> a lower case alpha numerical digit or letter</li>
	 * <li><b>?</b> an alpha numerical digit or letter with random case</li>
	 * <li><b>L</b> an upper case letter</li>
	 * <li><b>l</b> a lower case letter</li>
	 * <li><b>!</b> a letter with random case</li>
	 * </ul>
	 *
	 * @param pattern The pattern.
	 * @return The random code.
	 */
	public String getCode(String pattern) {
		return getCode(pattern, Strings.LETTERS_UPPER, Strings.DIGITS);
	}

	/**
	 * Returns a random text.
	 */
	public String getCode(Field field, int prefixLength) {
		int suffixLength = field.getLength() - prefixLength;
		return getString(prefixLength, Strings.LETTERS_UPPER)
			+ getString(suffixLength, Strings.DIGITS);
	}

	/**
	 * Returns a random code based on the given pattern. Any character in the mask
	 * not included in the valid mask
	 * characters is treated literally. Valid mask characters are:
	 * <ul>
	 * <li><b>#</b> a digit</li>
	 * <li><b>A</b> an upper case alpha numerical digit or letter</li>
	 * <li><b>a</b> a lower case alpha numerical digit or letter</li>
	 * <li><b>?</b> an alpha numerical digit or letter with random case</li>
	 * <li><b>L</b> an upper case letter</li>
	 * <li><b>l</b> a lower case letter</li>
	 * <li><b>!</b> a letter with random case</li>
	 * </ul>
	 *
	 * @param pattern The pattern.
	 * @param letters Optional letters.
	 * @param digits  Optional list of digits.
	 * @return The random code.
	 */
	public String getCode(String pattern, String letters, String digits) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < pattern.length(); i++) {
			char c = pattern.charAt(i);
			Character r;
			switch (c) {
			case '#':
				r = getChar(digits);
				break;
			case 'A':
				r = Character.toUpperCase(getChar(digits + letters));
				break;
			case 'a':
				r = Character.toLowerCase(getChar(digits + letters));
				break;
			case '?':
				if (getBoolean()) {
					r = Character.toUpperCase(getChar(digits + letters));
				} else {
					r = Character.toLowerCase(getChar(digits + letters));
				}
				break;
			case 'L':
				r = Character.toUpperCase(getChar(letters));
				break;
			case 'l':
				r = Character.toLowerCase(getChar(letters));
				break;
			case '!':
				if (getBoolean()) {
					r = Character.toUpperCase(getChar(letters));
				} else {
					r = Character.toLowerCase(getChar(letters));
				}
				break;
			default:
				r = c;
				break;
			}
			b.append(r);
		}
		return b.toString();
	}

	/**
	 * Return a random date.
	 *
	 * @return A random date.
	 */
	public LocalDate getDate() {
		int year = random.nextInt(10000);
		int month = random.nextInt(12) + 1;
		int days = LocalDate.of(year, month, 1).lengthOfMonth();
		int day = random.nextInt(days) + 1;
		return LocalDate.of(year, month, day);
	}

	/**
	 * Return a random time.
	 *
	 * @return A random time.
	 */
	public LocalTime getTime() {
		int hour = random.nextInt(24);
		int minute = random.nextInt(60);
		int second = random.nextInt(60);
		return LocalTime.of(hour, minute, second);
	}

	/**
	 * Return a random timestamp.
	 *
	 * @return A random timestamp.
	 */
	public LocalDateTime getDateTime() {
		return LocalDateTime.of(getDate(), getTime());
	}

	/**
	 * Return a random time in millis between time start and time end.
	 *
	 * @return The random time in millis.
	 */
	public long getTimeInMillis() {

		/* Check start and end initialized. */
		if (timeStart == null || timeEnd == null) {
			throw new IllegalStateException();
		}

		/* Ensure that the long stream is initialized. */
		if (longStream == null) {
			longStream = random.longs(timeStart, timeEnd + 1);
			longIterator = longStream.iterator();
		}

		return longIterator.next();
	}

	/**
	 * Randomly get an element of the list.
	 *
	 * @param <T>  The type.
	 * @param list The list.
	 * @return The selected element.
	 */
	public <T> T getElement(List<T> list) {
		return list.get(random.nextInt(list.size()));
	}

	/**
	 * Returns a random value for a possible values field.
	 */
	public Value getPossibleValue(Field field) {
		int count = field.getPossibleValues().size();
		int index = random.nextInt(count);
		return field.getPossibleValues().get(index);
	}

	/**
	 * Randomly generate a record given the field list.
	 *
	 * @param fields The field list.
	 * @return The generated record.
	 */
	public Record getRecord(FieldList fields) {
		Record rc = new Record(fields);
		for (int i = 0; i < fields.size(); i++) {
			Field field = fields.getField(i);

			/* Possible values, set one of them. */
			if (field.isPossibleValues()) {
				rc.setValue(i, getElement(field.getPossibleValues()));
				continue;
			}

			/* Boolean field. */
			if (field.isBoolean()) {
				rc.setValue(i, new Value(getBoolean()));
				continue;
			}

			/* String field, do as appropriate. */
			if (field.isString()) {
				/* Has a random pattern. */
				if (field.getRandomPattern() != null) {
					rc.setValue(i, new Value(getCode(field.getRandomPattern())));
					continue;
				}
				/* Get a valid string... */
				String value = null;
				if (field.isFixedWidth()) {
					value = getString(field.getDisplayLength(), Strings.LETTERS_UPPER);
				} else {
					int length = random.nextInt(field.getDisplayLength());
					while (length == 0 || length < field.getDisplayLength() / 4) {
						length = random.nextInt(field.getDisplayLength());
					}
					value = getTokens(length);
				}
				/* Do assign it. */
				rc.setValue(i, new Value(value));
				continue;
			}

			/* Number field. */
			if (field.isNumber()) {
				/* Decimal field, most common and easy to generate. */
				if (field.isDecimal()) {
					int length = field.getLength();
					int decimals = field.getDecimals();
					rc.setValue(i, new Value(getDecimal(length, decimals)));
					continue;
				}
				/* Double, 20 integer positions and 10 decimal places. */
				if (field.isDouble()) {
					int length = 31;
					int decimals = 10;
					rc.setValue(i, new Value(getDecimal(length, decimals).doubleValue()));
					continue;
				}
				/* Long, 30 integer positions. */
				if (field.isLong()) {
					int length = 30;
					int decimals = 0;
					rc.setValue(i, new Value(getDecimal(length, decimals).longValue()));
					continue;
				}
				/* Integer, 15 integer positions. */
				if (field.isLong()) {
					int length = 15;
					int decimals = 0;
					rc.setValue(i, new Value(getDecimal(length, decimals).intValue()));
					continue;
				}
			}

			/* Date, time or timestamp field. */
			if (field.isDate()) {
				rc.setValue(i, new Value(getDate()));
				continue;
			}
			if (field.isTime()) {
				rc.setValue(i, new Value(getTime()));
				continue;
			}
			if (field.isDateTime()) {
				rc.setValue(i, new Value(getDateTime()));
				continue;
			}

			/* Other, byte array or value, set to null. */
			rc.setNull(i);
		}
		return rc;
	}

	/**
	 * Generate a random recordset.
	 *
	 * @param size   The desired size.
	 * @param fields The list of fields.
	 * @return The recordset.
	 */
	public RecordSet getRecordSet(int size, FieldList fields) {
		RecordSet rs = new RecordList(fields);
		for (int i = 0; i < size; i++) {
			rs.add(getRecord(fields));
		}
		return rs;
	}
}
