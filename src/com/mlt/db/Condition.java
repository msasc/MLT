/*
 * Copyright (C) 2018 Miquel Sas
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.mlt.db;

import java.util.ArrayList;
import java.util.List;

import com.mlt.util.Lists;

/**
 * A condition used to build filtering criteria.
 *
 * @author Miquel Sas
 */
public class Condition {

	/**
	 * Enumeration constants of Operators.
	 */
	public static enum Operator {

		/**
		 * Starts with, in SQL {@code FIELD LIKE '....%'}
		 */
		LIKE_LEFT(1),
		/**
		 * Contains, in SQL {@code FIELD LIKE '%...%'}
		 */
		LIKE_MID(1),
		/**
		 * Ends with, in SQL {@code FIELD LIKE '%...'}
		 */
		LIKE_RIGHT(1),
		/**
		 * Equals, in SQL {@code FIELD = ?}
		 */
		FIELD_EQ(1),
		/**
		 * Greater than, in SQL {@code FIELD > ?}
		 */
		FIELD_GT(1),
		/**
		 * Greater than or equal to, in SQL {@code FIELD >= ?}
		 */
		FIELD_GE(1),
		/**
		 * Less than, in SQL {@code FIELD < ?}
		 */
		FIELD_LT(1),
		/**
		 * Less than or equal to, in SQL {@code FIELD <= ?}
		 */
		FIELD_LE(1),
		/**
		 * Not equal to, in SQL {@code FIELD != ?}
		 */
		FIELD_NE(1),
		/**
		 * In the list, in SQL {@code FIELD IN (?, ?, ...)}
		 */
		IN_LIST(-1),
		/**
		 * Is null, in SQL {@code FIELD IS NULL}
		 */
		IS_NULL(0),
		/**
		 * Between, in SQL {@code FIELD BETWEEN ? AND ?}
		 */
		BETWEEN(2),
		/**
		 * Does not start with, in SQL {@code FIELD NOT LIKE '....%'}
		 */
		NOT_LIKE_LEFT(1),
		/**
		 * Does not contains, in SQL {@code FIELD NOT LIKE '%....%'}
		 */
		NOT_LIKE_MID(1),
		/**
		 * Does not end with, in SQL {@code FIELD NOT LIKE '%....'}
		 */
		NOT_LIKE_RIGHT(1),
		/**
		 * Not in the list, in SQL {@code FIELD NOT IN (?, ?,...)}
		 */
		NOT_IN_LIST(-1),
		/**
		 * Not null, in SQL {@code FIELD IS NOT NULL}
		 */
		NOT_IS_NULL(0),
		/**
		 * Not between, in SQL {@code FIELD NOT BETWEEN ? AND ?}
		 */
		NOT_BETWEEN(2),
		/**
		 * Starts with, no case, in SQL {@code UPPER(FIELD) LIKE '....%'}
		 */
		LIKE_LEFT_NOCASE(1),
		/**
		 * Contains no case, in SQL {@code UPPER(FIELD) LIKE '%....%'}
		 */
		LIKE_MID_NOCASE(1),
		/**
		 * Ends with no case, in SQL {@code UPPER(FIELD) LIKE '%....'}
		 */
		LIKE_RIGHT_NOCASE(1),
		/**
		 * Equals no case, in SQL {@code UPPER(FIELD) = UPPER(?)}
		 */
		FIELD_EQ_NOCASE(1),
		/**
		 * Greater than no case, in SQL {@code UPPER(FIELD) > UPPER(?)}
		 */
		FIELD_GT_NOCASE(1),
		/**
		 * Greater than or equal to no case, in SQL {@code UPPER(FIELD) >= UPPER(?)}
		 */
		FIELD_GE_NOCASE(1),
		/**
		 * Less than no case, in SQL {@code UPPER(FIELD) < UPPER(?)}
		 */
		FIELD_LT_NOCASE(1),
		/**
		 * Less than or equal to no case, in SQL {@code UPPER(FIELD) <= UPPER(?)}
		 */
		FIELD_LE_NOCASE(1),
		/**
		 * Not equal no case {@code UPPER(FIELD) != UPPER(?)}
		 */
		FIELD_NE_NOCASE(1),
		/**
		 * In list no case {@code UPPER(FIELD) IN (UPPER(?), UPPER(?),...)}
		 */
		IN_LIST_NOCASE(-1),
		/**
		 * Between no case {@code UPPER(FIELD) BETWEEN UPPER(?) AND UPPER(?)}
		 */
		BETWEEN_NOCASE(2),
		/**
		 * Does not start with no case, in SQL {@code UPPER(FIELD) NOT LIKE '....%'}
		 */
		NOT_LIKE_LEFT_NOCASE(1),
		/**
		 * Does not contain no case, in SQL {@code UPPER(FIELD) NOT LIKE '%....%'}
		 */
		NOT_LIKE_MID_NOCASE(1),
		/**
		 * Does not end with no case, in SQL {@code UPPER(FIELD) NOT LIKE '%....'}
		 */
		NOT_LIKE_RIGHT_NOCASE(1),
		/**
		 * Not in list no case, in SQL
		 * {@code UPPER(FIELD) NOT IN (UPPER(?), UPPER(?),...)}
		 */
		NOT_IN_LIST_NOCASE(-1),
		/**
		 * Not between no case, in SQL
		 * {@code UPPER(FIELD) NOT BETWEEN UPPER(?) AND UPPER(?)}
		 */
		NOT_BETWEEN_NOCASE(2);

		/**
		 * This operator required number of values: -1 more than one, 1 one, 2 two, 0
		 * none.
		 */
		private int requiredValues = -1;

		/**
		 * Constructor assigning the number of required values.
		 *
		 * @param requiredValues Number of required values.
		 */
		Operator(int requiredValues) {
			this.requiredValues = requiredValues;
		}

		/**
		 * Returns the number of required values in the right operand.
		 *
		 * @return The number of required values.
		 */
		public int getRequiredValues() {
			return requiredValues;
		}

		/**
		 * Check if this operator is a NO CASE operator.
		 *
		 * @return A boolean
		 */
		public boolean isNoCase() {
			return toString().contains("NOCASE");
		}
	}

	/**
	 * Validates field, operator and values to construct a valid condition.
	 *
	 * @param field    The field or left operand.
	 * @param operator The operator.
	 * @param values   The possible list of values as right operand.
	 */
	public static final void validate(Field field, Operator operator, List<Value> values) {

		/*
		 * The field or the values can not be null.
		 */
		if (field == null) {
			throw new NullPointerException("Field can not be null");
		}
		if (values == null) {
			throw new NullPointerException("values can not be null");
		}
		/*
		 * Null or empty values can only apply to null or not null operators.
		 */
		if (values.isEmpty()) {
			if (!operator.equals(Operator.IS_NULL) && !operator.equals(Operator.NOT_IS_NULL)) {
				throw new IllegalArgumentException(
					"Right operand is expected for operator: " + operator);
			}
		}
		/*
		 * The type of the right operand must be compatible with the type of the field.
		 */
		final Types fieldType = field.getType();
		for (Value value : values) {
			boolean invalidValueType = false;
			if (fieldType.isBoolean() && !value.isBoolean()) {
				invalidValueType = true;
			}
			if (fieldType.isString() && !value.isString()) {
				invalidValueType = true;
			}
			if (fieldType.isNumber() && !value.isNumber()) {
				invalidValueType = true;
			}
			if (fieldType.isDate() && !value.isDate()) {
				invalidValueType = true;
			}
			if (fieldType.isTime() && !value.isTime()) {
				invalidValueType = true;
			}
			if (fieldType.isDateTime() && !value.isDateTime()) {
				invalidValueType = true;
			}
			if (fieldType.isByteArray() && !value.isByteArray()) {
				invalidValueType = true;
			}
			if (invalidValueType) {
				StringBuilder b = new StringBuilder();
				b.append("Invalid value type (");
				b.append(value.getType());
				b.append(field.getType());
				throw new IllegalArgumentException(b.toString());
			}
		}
		/*
		 * A no case operator requires a field and a value of type string.
		 */
		if (operator.isNoCase()) {
			if (!fieldType.isString()) {
				throw new IllegalArgumentException(
					"No case only applies to string fields and values");
			}
			for (Value value : values) {
				if (!value.isString()) {
					throw new IllegalArgumentException(
						"No case only applies to string fields and values");
				}
			}
		}
		/*
		 * The number of values in the right operand.
		 */
		if (operator.getRequiredValues() == -1 && values.isEmpty()) {
			throw new IllegalArgumentException("Invalid number of values for operator " + operator);
		}
		if (operator.getRequiredValues() >= 0) {
			if (operator.getRequiredValues() != values.size()) {
				throw new IllegalArgumentException("Invalid number of values for operator " + operator);
			}
		}
	}

	/**
	 * Creates a LIKE_LEFT condition.
	 *
	 * @param field The field to be checked.
	 * @param value The value to check.
	 * @return The condition.
	 */
	public static Condition likeLeft(Field field, Value value) {
		return new Condition(field, Operator.LIKE_LEFT, value);
	}

	/**
	 * Creates a LIKE_LEFT_NOCASE condition.
	 *
	 * @param field The field to be checked.
	 * @param value The value to check.
	 * @return The condition.
	 */
	public static Condition likeLeftNoCase(Field field, Value value) {
		return new Condition(field, Operator.LIKE_LEFT_NOCASE, value);
	}

	/**
	 * Creates a NOT_LIKE_LEFT_NOCASE condition.
	 *
	 * @param field The field to be checked.
	 * @param value The value to check.
	 * @return The condition.
	 */
	public static Condition notLikeLeftNoCase(Field field, Value value) {
		return new Condition(field, Operator.NOT_LIKE_LEFT_NOCASE, value);
	}

	/**
	 * Creates a LIKE_MID condition.
	 *
	 * @param field The field to be checked.
	 * @param value The value to check.
	 * @return The condition.
	 */
	public static Condition likeMid(Field field, Value value) {
		return new Condition(field, Operator.LIKE_MID, value);
	}

	/**
	 * Creates a LIKE_MID_NOCASE condition.
	 *
	 * @param field The field to be checked.
	 * @param value The value to check.
	 * @return The condition.
	 */
	public static Condition likeMidNoCase(Field field, Value value) {
		return new Condition(field, Operator.LIKE_MID_NOCASE, value);
	}

	/**
	 * Creates a NOT_LIKE_MID condition.
	 *
	 * @param field The field to be checked.
	 * @param value The value to check.
	 * @return The condition.
	 */
	public static Condition notLikeMid(Field field, Value value) {
		return new Condition(field, Operator.NOT_LIKE_MID, value);
	}

	/**
	 * Creates a NOT_LIKE_MID_NOCASE condition.
	 *
	 * @param field The field to be checked.
	 * @param value The value to check.
	 * @return The condition.
	 */
	public static Condition notLikeMidNoCased(Field field, Value value) {
		return new Condition(field, Operator.NOT_LIKE_MID_NOCASE, value);
	}

	/**
	 * Creates a LIKE_RIGHT condition.
	 *
	 * @param field The field to be checked.
	 * @param value The value to check.
	 * @return The condition.
	 */
	public static Condition likeRight(Field field, Value value) {
		return new Condition(field, Operator.LIKE_RIGHT, value);
	}

	/**
	 * Creates a LIKE_RIGHT_NOCASE condition.
	 *
	 * @param field The field to be checked.
	 * @param value The value to check.
	 * @return The condition.
	 */
	public static Condition likeRightNoCase(Field field, Value value) {
		return new Condition(field, Operator.LIKE_RIGHT_NOCASE, value);
	}

	/**
	 * Creates a NOT_LIKE_RIGHT condition.
	 *
	 * @param field The field to be checked.
	 * @param value The value to check.
	 * @return The condition.
	 */
	public static Condition notLikeRight(Field field, Value value) {
		return new Condition(field, Operator.NOT_LIKE_RIGHT, value);
	}

	/**
	 * Creates a NOT_LIKE_RIGHT_NOCASE condition.
	 *
	 * @param field The field to be checked.
	 * @param value The value to check.
	 * @return The condition.
	 */
	public static Condition notLikeRightNoCase(Field field, Value value) {
		return new Condition(field, Operator.NOT_LIKE_RIGHT_NOCASE, value);
	}

	/**
	 * Creates a FIELD_EQ condition.
	 *
	 * @param field The field to be checked.
	 * @param value The value to check.
	 * @return The condition.
	 */
	public static Condition fieldEQ(Field field, Value value) {
		return new Condition(field, Operator.FIELD_EQ, value);
	}

	/**
	 * Creates a FIELD_GT condition.
	 *
	 * @param field The field to be checked.
	 * @param value The value to check.
	 * @return The condition.
	 */
	public static Condition fieldGT(Field field, Value value) {
		return new Condition(field, Operator.FIELD_GT, value);
	}

	/**
	 * Creates a FIELD_GE condition.
	 *
	 * @param field The field to be checked.
	 * @param value The value to check.
	 * @return The condition.
	 */
	public static Condition fieldGE(Field field, Value value) {
		return new Condition(field, Operator.FIELD_GE, value);
	}

	/**
	 * Creates a FIELD_LT condition.
	 *
	 * @param field The field to be checked.
	 * @param value The value to check.
	 * @return The condition.
	 */
	public static Condition fieldLT(Field field, Value value) {
		return new Condition(field, Operator.FIELD_LT, value);
	}

	/**
	 * Creates a FIELD_LEcondition.
	 *
	 * @param field The field to be checked.
	 * @param value The value to check.
	 * @return The condition.
	 */
	public static Condition fieldLE(Field field, Value value) {
		return new Condition(field, Operator.FIELD_LE, value);
	}

	/**
	 * Creates a FIELD_NE condition.
	 *
	 * @param field The field to be checked.
	 * @param value The value to check.
	 * @return The condition.
	 */
	public static Condition fieldNE(Field field, Value value) {
		return new Condition(field, Operator.FIELD_NE, value);
	}

	/**
	 * Creates a FIELD_EQ_NOCASE condition.
	 *
	 * @param field The field to be checked.
	 * @param value The value to check.
	 * @return The condition.
	 */
	public static Condition fieldEQNoCase(Field field, Value value) {
		return new Condition(field, Operator.FIELD_EQ_NOCASE, value);
	}

	/**
	 * Creates a FIELD_GT_NOCASE condition.
	 *
	 * @param field The field to be checked.
	 * @param value The value to check.
	 * @return The condition.
	 */
	public static Condition fieldGTNoCase(Field field, Value value) {
		return new Condition(field, Operator.FIELD_GT_NOCASE, value);
	}

	/**
	 * Creates a FIELD_GE_NOCASE condition.
	 *
	 * @param field The field to be checked.
	 * @param value The value to check.
	 * @return The condition.
	 */
	public static Condition fieldGENoCase(Field field, Value value) {
		return new Condition(field, Operator.FIELD_GE_NOCASE, value);
	}

	/**
	 * Creates a FIELD_LT_NOCASE condition.
	 *
	 * @param field The field to be checked.
	 * @param value The value to check.
	 * @return The condition.
	 */
	public static Condition fieldLTNoCase(Field field, Value value) {
		return new Condition(field, Operator.FIELD_LT_NOCASE, value);
	}

	/**
	 * Creates a FIELD_LE_NOCASE condition.
	 *
	 * @param field The field to be checked.
	 * @param value The value to check.
	 * @return The condition.
	 */
	public static Condition fieldLENoCase(Field field, Value value) {
		return new Condition(field, Operator.FIELD_LE_NOCASE, value);
	}

	/**
	 * Creates a FIELD_NE_NOCASE condition.
	 *
	 * @param field The field to be checked.
	 * @param value The value to check.
	 * @return The condition.
	 */
	public static Condition fieldNENoCase(Field field, Value value) {
		return new Condition(field, Operator.FIELD_NE_NOCASE, value);
	}

	/**
	 * Creates a IN_LIST condition.
	 *
	 * @param field  The field to be checked.
	 * @param values The values to check.
	 * @return The condition.
	 */
	public static Condition inList(Field field, Value... values) {
		return new Condition(field, Operator.IN_LIST, Lists.asList(values));
	}

	/**
	 * Creates a IN_LIST condition.
	 *
	 * @param field  The field to be checked.
	 * @param values The values to check.
	 * @return The condition.
	 */
	public static Condition inList(Field field, List<Value> values) {
		return new Condition(field, Operator.IN_LIST, values);
	}

	/**
	 * Creates a NOT_IN_LIST condition.
	 *
	 * @param field  The field to be checked.
	 * @param values The values to check.
	 * @return The condition.
	 */
	public static Condition notInList(Field field, Value... values) {
		return new Condition(field, Operator.NOT_IN_LIST, Lists.asList(values));
	}

	/**
	 * Creates a NOT_IN_LIST condition.
	 *
	 * @param field  The field to be checked.
	 * @param values The values to check.
	 * @return The condition.
	 */
	public static Condition notInList(Field field, List<Value> values) {
		return new Condition(field, Operator.NOT_IN_LIST, values);
	}

	/**
	 * Creates a IN_LIST_NOCASE condition.
	 *
	 * @param field  The field to be checked.
	 * @param values The values to check.
	 * @return The condition.
	 */
	public static Condition inListNoCase(Field field, Value... values) {
		return new Condition(field, Operator.IN_LIST_NOCASE, Lists.asList(values));
	}

	/**
	 * Creates a IN_LIST_NOCASE condition.
	 *
	 * @param field  The field to be checked.
	 * @param values The values to check.
	 * @return The condition.
	 */
	public static Condition inListNoCase(Field field, List<Value> values) {
		return new Condition(field, Operator.IN_LIST_NOCASE, values);
	}

	/**
	 * Creates a NOT_IN_LIST_NOCASE condition.
	 *
	 * @param field  The field to be checked.
	 * @param values The values to check.
	 * @return The condition.
	 */
	public static Condition notInListNoCase(Field field, Value... values) {
		return new Condition(field, Operator.NOT_IN_LIST_NOCASE, Lists.asList(values));
	}

	/**
	 * Creates a NOT_IN_LIST_NOCASE condition.
	 *
	 * @param field  The field to be checked.
	 * @param values The values to check.
	 * @return The condition.
	 */
	public static Condition notInListNoCase(Field field, List<Value> values) {
		return new Condition(field, Operator.NOT_IN_LIST_NOCASE, values);
	}

	/**
	 * Creates a IS_NULL condition.
	 *
	 * @param field The field to be checked.
	 * @return The condition.
	 */
	public static Condition isNull(Field field) {
		return new Condition(field, Operator.IS_NULL, new ArrayList<>());
	}

	/**
	 * Creates a NOT_IS_NULL condition.
	 *
	 * @param field The field to be checked.
	 * @return The condition.
	 */
	public static Condition isNotNull(Field field) {
		return new Condition(field, Operator.NOT_IS_NULL, new ArrayList<>());
	}

	/**
	 * Creates a BETWEEN condition.
	 *
	 * @param field  The field to be checked.
	 * @param value1 The first value to check.
	 * @param value2 The last value to check.
	 * @return The condition.
	 */
	public static Condition between(Field field, Value value1, Value value2) {
		return new Condition(field, Operator.BETWEEN, value1, value2);
	}

	/**
	 * Creates a NOT_BETWEEN condition.
	 *
	 * @param field  The field to be checked.
	 * @param value1 The first value to check.
	 * @param value2 The last value to check.
	 * @return The condition.
	 */
	public static Condition notBetween(Field field, Value value1, Value value2) {
		return new Condition(field, Operator.NOT_BETWEEN, value1, value2);
	}

	/**
	 * Creates a BETWEEN_NOCASE condition.
	 *
	 * @param field  The field to be checked.
	 * @param value1 The first value to check.
	 * @param value2 The last value to check.
	 * @return The condition.
	 */
	public static Condition betweenNoCase(Field field, Value value1, Value value2) {
		return new Condition(field, Operator.BETWEEN_NOCASE, value1, value2);
	}

	/**
	 * Creates a NOT_BETWEEN_NOCASE condition.
	 *
	 * @param field  The field to be checked.
	 * @param value1 The first value to check.
	 * @param value2 The last value to check.
	 * @return The condition.
	 */
	public static Condition notBetweenNoCase(Field field, Value value1, Value value2) {
		return new Condition(field, Operator.NOT_BETWEEN_NOCASE, value1, value2);
	}

	/**
	 * Creates a literal condition that is responsibility of the developer to be
	 * correct. This condition can not be
	 * checked on the fly, only the RDBMS can check it when sent.
	 *
	 * @param condition The expression condition.
	 * @return The condition.
	 */
	public static Condition literal(String condition) {
		return new Condition(condition);
	}

	/** The field to compare is the left operand. */
	private Field field;
	/** The operator to apply. */
	private Operator operator;
	/** The right operand is a list of one or more values. */
	private List<Value> values;
	/**
	 * A string condition that is the developer responsibility to be well formed.
	 */
	private String condition;

	/**
	 * Generic constructor.
	 *
	 * @param field    The field or left operand
	 * @param operator The operator
	 * @param value    The value that is the right operand.
	 */
	public Condition(Field field, Operator operator, Value value) {
		this.field = field;
		this.operator = operator;
		this.values = new ArrayList<>();
		this.values.add(value);
		validate(field, operator, this.values);
	}

	/**
	 * Constructor for the between condition.
	 *
	 * @param field    The field or left operand
	 * @param operator The operator
	 * @param value1   The first value.
	 * @param value2   The second value.
	 */
	public Condition(Field field, Operator operator, Value value1, Value value2) {
		this.field = field;
		this.operator = operator;
		this.values = new ArrayList<>();
		this.values.add(value1);
		this.values.add(value2);
		validate(field, operator, this.values);
	}

	/**
	 * Generic constructor.
	 *
	 * @param field    The field or left operand
	 * @param operator The operator
	 * @param values   The value or values that are the right operand.
	 */
	public Condition(Field field, Operator operator, List<Value> values) {
		this.field = field;
		this.operator = operator;
		this.values = new ArrayList<>();
		this.values.addAll(values);
		validate(field, operator, this.values);
	}

	/**
	 * Constructor with a string condition that is the responsibility of the
	 * developer to be correct.
	 *
	 * @param condition The condition.
	 */
	public Condition(String condition) {
		if (condition == null) {
			throw new NullPointerException();
		}
		this.condition = condition;
	}

	/**
	 * Returns the field or left operand.
	 *
	 * @return The field.
	 */
	public Field getField() {
		return field;
	}

	/**
	 * Returns the operator.
	 *
	 * @return The operator
	 */
	public Operator getOperator() {
		return operator;
	}

	/**
	 * Returns the list of values or right operand.
	 *
	 * @return The list of values.
	 */
	public List<Value> getValues() {
		return values;
	}

	/**
	 * Return the expression condition.
	 *
	 * @return The condition.
	 */
	public String getCondition() {
		return condition;
	}

	public boolean isLiteral() {
		return condition != null;
	}

	/**
	 * Check if this condition is not case sensitive.
	 *
	 * @return A boolean indicating if this condition is not case sensitive.
	 */
	@SuppressWarnings("incomplete-switch")
	public boolean isNoCase() {
		switch (getOperator()) {
		case LIKE_LEFT_NOCASE:
		case LIKE_MID_NOCASE:
		case LIKE_RIGHT_NOCASE:
		case FIELD_EQ_NOCASE:
		case FIELD_GT_NOCASE:
		case FIELD_GE_NOCASE:
		case FIELD_LT_NOCASE:
		case FIELD_LE_NOCASE:
		case FIELD_NE_NOCASE:
		case IN_LIST_NOCASE:
		case BETWEEN_NOCASE:
		case NOT_LIKE_LEFT_NOCASE:
		case NOT_LIKE_MID_NOCASE:
		case NOT_LIKE_RIGHT_NOCASE:
		case NOT_IN_LIST_NOCASE:
		case NOT_BETWEEN_NOCASE:
			return true;
		}
		return false;
	}

	/**
	 * Check if this condition is a not condition.
	 *
	 * @return A boolean indicating if this condition is a not condition.
	 */
	@SuppressWarnings("incomplete-switch")
	public boolean isNot() {
		switch (getOperator()) {
		case NOT_LIKE_LEFT:
		case NOT_LIKE_MID:
		case NOT_LIKE_RIGHT:
		case NOT_IN_LIST:
		case NOT_IS_NULL:
		case NOT_BETWEEN:
		case NOT_LIKE_LEFT_NOCASE:
		case NOT_LIKE_MID_NOCASE:
		case NOT_LIKE_RIGHT_NOCASE:
		case NOT_IN_LIST_NOCASE:
		case NOT_BETWEEN_NOCASE:
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (getClass() != o.getClass()) {
			return false;
		}
		final Condition cmp = (Condition) o;
		if (field != null && !field.equals(cmp.field)) {
			return false;
		}
		if (operator != null && !operator.equals(cmp.operator)) {
			return false;
		}
		if (values != null && !values.equals(cmp.values)) {
			return false;
		}
		return !(condition != null && !condition.equals(cmp.condition));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int hash = 0;
		if (field != null) {
			hash ^= field.hashCode();
		}
		if (operator != null) {
			hash ^= operator.hashCode();
		}
		if (values != null) {
			hash ^= values.hashCode();
		}
		if (condition != null) {
			hash ^= condition.hashCode();
		}
		return hash;
	}

	/**
	 * Check the like left condition.
	 *
	 * @param vChk The value to check.
	 * @return A boolean.
	 */
	private boolean checkLikeLeft(Value vChk) {
		Value vCnd = values.get(0);
		return vChk.getString().startsWith(vCnd.getString());
	}

	/**
	 * Check the like left condition no case.
	 *
	 * @param vChk The value to check.
	 * @return A boolean.
	 */
	private boolean checkLikeLeftNoCase(Value vChk) {
		Value vCnd = values.get(0);
		return vChk.getString().toUpperCase().startsWith(vCnd.getString().toUpperCase());
	}

	/**
	 * Check the like mid condition.
	 *
	 * @param vChk The value to check.
	 * @return A boolean.
	 */
	private boolean checkLikeMid(Value vChk) {
		Value vCnd = values.get(0);
		return vChk.getString().contains(vCnd.getString());
	}

	/**
	 * Check the like mid condition no case.
	 *
	 * @param vChk The value to check.
	 * @return A boolean.
	 */
	private boolean checkLikeMidNoCase(Value vChk) {
		Value vCnd = values.get(0);
		return vChk.getString().toUpperCase().contains(vCnd.getString().toUpperCase());
	}

	/**
	 * Check the like right condition.
	 *
	 * @param vChk The value to check.
	 * @return A boolean.
	 */
	private boolean checkLikeRight(Value vChk) {
		Value vCnd = values.get(0);
		return vChk.getString().endsWith(vCnd.getString());
	}

	/**
	 * Check the like right condition no case.
	 *
	 * @param vChk The value to check.
	 * @return A boolean.
	 */
	private boolean checkLikeRightNoCase(Value vChk) {
		Value vCnd = values.get(0);
		return vChk.getString().toUpperCase().endsWith(vCnd.getString().toUpperCase());
	}

	/**
	 * Check the field EQ condition.
	 *
	 * @param vChk The value to check.
	 * @return A boolean.
	 */
	private boolean checkFieldEQ(Value vChk) {
		Value vCnd = values.get(0);
		return vChk.compareTo(vCnd) == 0;
	}

	/**
	 * Check the field EQ condition no case.
	 *
	 * @param vChk The value to check.
	 * @return A boolean.
	 */
	private boolean checkFieldEQNoCase(Value vChk) {
		Value vCnd = values.get(0);
		return vChk.getString().toUpperCase().compareTo(vCnd.getString().toUpperCase()) == 0;
	}

	/**
	 * Check the field GT condition.
	 *
	 * @param vChk The value to check.
	 * @return A boolean.
	 */
	private boolean checkFieldGT(Value vChk) {
		Value vCnd = values.get(0);
		return vChk.compareTo(vCnd) > 0;
	}

	/**
	 * Check the field GT condition no case.
	 *
	 * @param vChk The value to check.
	 * @return A boolean.
	 */
	private boolean checkFieldGTNoCase(Value vChk) {
		Value vCnd = values.get(0);
		return vChk.getString().toUpperCase().compareTo(vCnd.getString().toUpperCase()) > 0;
	}

	/**
	 * Check the field GE condition.
	 *
	 * @param vChk The value to check.
	 * @return A boolean.
	 */
	private boolean checkFieldGE(Value vChk) {
		Value vCnd = values.get(0);
		return vChk.compareTo(vCnd) >= 0;
	}

	/**
	 * Check the field GE condition no case.
	 *
	 * @param vChk The value to check.
	 * @return A boolean.
	 */
	private boolean checkFieldGENoCase(Value vChk) {
		Value vCnd = values.get(0);
		return vChk.getString().toUpperCase().compareTo(vCnd.getString().toUpperCase()) >= 0;
	}

	/**
	 * Check the field LT condition.
	 *
	 * @param vChk The value to check.
	 * @return A boolean.
	 */
	private boolean checkFieldLT(Value vChk) {
		Value vCnd = values.get(0);
		return vChk.compareTo(vCnd) < 0;
	}

	/**
	 * Check the field LT condition no case.
	 *
	 * @param vChk The value to check.
	 * @return A boolean.
	 */
	private boolean checkFieldLTNoCase(Value vChk) {
		Value vCnd = values.get(0);
		return vChk.getString().compareTo(vCnd.getString().toUpperCase()) < 0;
	}

	/**
	 * Check the field LE condition.
	 *
	 * @param vChk The value to check.
	 * @return A boolean.
	 */
	private boolean checkFieldLE(Value vChk) {
		Value vCnd = values.get(0);
		return vChk.compareTo(vCnd) <= 0;
	}

	/**
	 * Check the field LE condition no case.
	 *
	 * @param vChk The value to check.
	 * @return A boolean.
	 */
	private boolean checkFieldLENoCase(Value vChk) {
		Value vCnd = values.get(0);
		return vChk.getString().toUpperCase().compareTo(vCnd.getString().toUpperCase()) <= 0;
	}

	/**
	 * Check the in list condition.
	 *
	 * @param vChk The value to check.
	 * @return A boolean.
	 */
	private boolean checkInList(Value vChk) {
		return vChk.in(values);
	}

	/**
	 * Check the in list condition no case.
	 *
	 * @param vChk The value to check.
	 * @return A boolean.
	 */
	private boolean checkInListNoCase(Value vChk) {
		String sChk = vChk.getString().toUpperCase();
		for (int i = 0; i < values.size(); i++) {
			if (sChk.compareTo(values.get(i).getString().toUpperCase()) == 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check the is null condition.
	 *
	 * @param vChk The value to check.
	 * @return A boolean.
	 */
	private boolean checkIsNull(Value vChk) {
		return vChk.isNull();
	}

	/**
	 * Check the between condition.
	 *
	 * @param vChk The value to check.
	 * @return A boolean.
	 */
	private boolean checkBetween(Value vChk) {
		Value vMin = values.get(0);
		Value vMax = values.get(1);
		return vChk.compareTo(vMin) >= 0 && vChk.compareTo(vMax) <= 0;
	}

	/**
	 * Check the between condition no case.
	 *
	 * @param vChk The value to check.
	 * @return A boolean.
	 */
	private boolean checkBetweenNoCase(Value vChk) {
		String sChk = vChk.getString().toUpperCase();
		Value vMin = values.get(0);
		Value vMax = values.get(1);
		return sChk.compareTo(vMin.getString().toUpperCase()) >= 0
			&& sChk.compareTo(vMax.getString().toUpperCase()) <= 0;
	}

	/**
	 * Check that a record meets the condition.
	 *
	 * @param record The record to check.
	 * @return A boolean indicating if the record meets the condition.
	 */
	public boolean check(Record record) {
		if (isLiteral()) {
			throw new IllegalStateException();
		}
		int index = record.getFieldList().getFieldIndex(field.getAlias());
		if (index < 0) {
			return false;
		}
		Value value = record.getValue(index);
		return check(value);
	}

	/**
	 * Check that a value meets the condition.
	 *
	 * @param value The value to check.
	 * @return A boolean indicating if the value meets the condition.
	 */
	public boolean check(Value value) {
		if (isLiteral()) {
			throw new IllegalStateException();
		}
		switch (getOperator()) {
		case LIKE_LEFT:
			return checkLikeLeft(value);
		case LIKE_MID:
			return checkLikeMid(value);
		case LIKE_RIGHT:
			return checkLikeRight(value);
		case FIELD_EQ:
			return checkFieldEQ(value);
		case FIELD_GT:
			return checkFieldGT(value);
		case FIELD_GE:
			return checkFieldGE(value);
		case FIELD_LT:
			return checkFieldLT(value);
		case FIELD_LE:
			return checkFieldLE(value);
		case FIELD_NE:
			return !checkFieldEQ(value);
		case IN_LIST:
			return checkInList(value);
		case IS_NULL:
			return checkIsNull(value);
		case BETWEEN:
			return checkBetween(value);
		case NOT_LIKE_LEFT:
			return !checkLikeLeft(value);
		case NOT_LIKE_MID:
			return !checkLikeMid(value);
		case NOT_LIKE_RIGHT:
			return !checkLikeRight(value);
		case NOT_IN_LIST:
			return !checkInList(value);
		case NOT_IS_NULL:
			return !checkIsNull(value);
		case NOT_BETWEEN:
			return !checkBetween(value);
		case LIKE_LEFT_NOCASE:
			return checkLikeLeftNoCase(value);
		case LIKE_MID_NOCASE:
			return checkLikeMidNoCase(value);
		case LIKE_RIGHT_NOCASE:
			return checkLikeRightNoCase(value);
		case FIELD_EQ_NOCASE:
			return checkFieldEQNoCase(value);
		case FIELD_GT_NOCASE:
			return checkFieldGTNoCase(value);
		case FIELD_GE_NOCASE:
			return checkFieldGENoCase(value);
		case FIELD_LT_NOCASE:
			return checkFieldLTNoCase(value);
		case FIELD_LE_NOCASE:
			return checkFieldLENoCase(value);
		case FIELD_NE_NOCASE:
			return !checkFieldEQNoCase(value);
		case IN_LIST_NOCASE:
			return checkInListNoCase(value);
		case BETWEEN_NOCASE:
			return checkBetweenNoCase(value);
		case NOT_LIKE_LEFT_NOCASE:
			return !checkLikeLeftNoCase(value);
		case NOT_LIKE_MID_NOCASE:
			return !checkLikeMidNoCase(value);
		case NOT_LIKE_RIGHT_NOCASE:
			return !checkLikeRightNoCase(value);
		case NOT_IN_LIST_NOCASE:
			return !checkInListNoCase(value);
		case NOT_BETWEEN_NOCASE:
			return !checkBetweenNoCase(value);
		default:
			throw new IllegalStateException();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		if (isLiteral()) {
			return condition;
		}
		StringBuilder b = new StringBuilder();
		if (isNoCase()) {
			b.append("UPPER(");
		}
		b.append(getField().getName());
		if (isNoCase()) {
			b.append(")");
		}

		switch (getOperator()) {
		case LIKE_LEFT:
		case LIKE_LEFT_NOCASE:
		case NOT_LIKE_LEFT:
		case NOT_LIKE_LEFT_NOCASE:
			if (isNot()) {
				b.append(" NOT");
			}
			b.append(" LIKE '");
			b.append(toString(getValues().get(0), isNoCase()));
			b.append("%'");
			break;
		case LIKE_MID:
		case LIKE_MID_NOCASE:
		case NOT_LIKE_MID:
		case NOT_LIKE_MID_NOCASE:
			if (isNot()) {
				b.append(" NOT");
			}
			b.append(" LIKE '%");
			b.append(toString(getValues().get(0), isNoCase()));
			b.append("%'");
			break;
		case LIKE_RIGHT:
		case LIKE_RIGHT_NOCASE:
		case NOT_LIKE_RIGHT:
		case NOT_LIKE_RIGHT_NOCASE:
			if (isNot()) {
				b.append(" NOT");
			}
			b.append(" LIKE '%");
			b.append(toString(getValues().get(0), isNoCase()));
			b.append("'");
			break;
		case FIELD_EQ:
		case FIELD_EQ_NOCASE:
			b.append(" = ");
			b.append(toString(getValues().get(0), isNoCase()));
			break;
		case FIELD_GT:
		case FIELD_GT_NOCASE:
			b.append(" > ");
			b.append(toString(getValues().get(0), isNoCase()));
			break;
		case FIELD_GE:
		case FIELD_GE_NOCASE:
			b.append(" >= ");
			b.append(toString(getValues().get(0), isNoCase()));
			break;
		case FIELD_LT:
		case FIELD_LT_NOCASE:
			b.append(" < ");
			b.append(toString(getValues().get(0), isNoCase()));
			break;
		case FIELD_LE:
		case FIELD_LE_NOCASE:
			b.append(" <= ");
			b.append(toString(getValues().get(0), isNoCase()));
			break;
		case FIELD_NE:
		case FIELD_NE_NOCASE:
			b.append(" != ");
			b.append(toString(getValues().get(0), isNoCase()));
			break;
		case IN_LIST:
		case IN_LIST_NOCASE:
		case NOT_IN_LIST:
		case NOT_IN_LIST_NOCASE:
			if (isNot()) {
				b.append(" NOT");
			}
			b.append(" IN (");
			for (int i = 0; i < getValues().size(); i++) {
				if (i > 0) {
					b.append(", ");
				}
				b.append(toString(getValues().get(i), isNoCase()));
			}
			b.append(")");
			break;
		case IS_NULL:
		case NOT_IS_NULL:
			b.append(" IS");
			if (isNot()) {
				b.append(" NOT");
			}
			b.append(" NULL");
			break;
		case BETWEEN:
		case BETWEEN_NOCASE:
		case NOT_BETWEEN:
		case NOT_BETWEEN_NOCASE:
			if (isNot()) {
				b.append(" NOT");
			}
			b.append(" BETWEEN ");
			b.append(toString(getValues().get(0), isNoCase()));
			b.append(" AND ");
			b.append(toString(getValues().get(1), isNoCase()));
			break;
		default:
			throw new IllegalStateException();
		}
		return b.toString();
	}

	/**
	 * Returns the value converted to string, uppercase if applies.
	 *
	 * @param value  The value.
	 * @param noCase The no case flag.
	 * @return The value converted to string.
	 */
	private String toString(Value value, boolean noCase) {
		if (noCase) {
			return value.toString().toUpperCase();
		}
		return value.toString();
	}
}
