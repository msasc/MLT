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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.mlt.util.Strings;
import com.mlt.util.StringConverter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Definition a an item usually of tabular data.
 *
 * @author Miquel Sas
 */
public class Field implements Comparable<Object> {

	/** Fixed display length for numbers. */
	private static final int NUMBER_DISPLAY_LENGTH = 20;
	/** Fixed display length for date fields "9999-99-99" */
	private static final int DATE_DISPLAY_LENGTH = 10;
	/** Fixed display length for date-time fields "9999-99-99 99:99:99" */
	private static final int DATE_TIME_DISPLAY_LENGTH = 19;
	/** Fixed display length for time fields "99:99:99" */
	private static final int TIME_DISPLAY_LENGTH = 8;

	/**
	 * Returns the list of all relations contained in the list of fields.
	 *
	 * @param fields The source list of fields.
	 * @return The list of relations.
	 */
	public static List<Relation> getRelations(List<Field> fields) {
		List<Relation> relations = new ArrayList<>();
		for (Field field : fields) {
			List<Relation> fieldRelations = field.getRelations();
			for (Relation relation : fieldRelations) {
				if (!relations.contains(relation)) {
					relations.add(relation);
				}
			}
		}
		return relations;
	}

	/**
	 * Field name.
	 */
	private String name;
	/**
	 * Optional field alias, if not set the name is used.
	 */
	private String alias;
	/**
	 * Length if applicable, otherwise -1.
	 */
	private int length = -1;
	/**
	 * Decimals if applicable, otherwise -1.
	 */
	private int decimals = -1;
	/**
	 * Type.
	 */
	private Types type;

	/**
	 * Description, normally the longer description.
	 */
	private String description;
	/**
	 * Label on forms.
	 */
	private String label;
	/**
	 * Header on grids.
	 */
	private String header;
	/**
	 * Title or short description.
	 */
	private String title;
	/**
	 * Adjusted display length.
	 */
	private int displayLength;
	/**
	 * Display decimals, normally used for DOUBLE, INTEGER and LONG types.
	 */
	private int displayDecimals = -1;
	/**
	 * A boolean that indicates whether the field is fixed width.
	 */
	private boolean fixedWidth = true;
	/**
	 * Field group.
	 */
	private FieldGroup fieldGroup;
	/**
	 * A boolean that indicates if this field is the main description of the
	 * possible parent record. It is useful in
	 * lookup actions to show the description beside the code. I no main description
	 * is found for the record, the first
	 * non-fixed with field is taken. If no non-fixed field exists, then no
	 * description is shown beside the lookup code.
	 */
	private boolean mainDescription = false;
	/**
	 * A boolean that indicates if this fields should be used in lookups.
	 */
	private boolean lookup = false;
	/**
	 * A boolean that indicates whether this field is a password field.
	 */
	private boolean password = false;

	/**
	 * Initial value.
	 */
	private Value initialValue;
	/**
	 * Maximum value.
	 */
	private Value maximumValue;
	/**
	 * Minimum value.
	 */
	private Value minimumValue;
	/**
	 * List of possible values.
	 */
	private List<Value> possibleValues = new ArrayList<>();
	/**
	 * A flag indicating whether a non empty value is required for this field.
	 */
	private boolean required;
	/**
	 * Case modifier for strings.
	 */
	private String caseModifier = "NONE";
	/**
	 * Optional calculator.
	 */
	private Calculator calculator;
	/**
	 * A boolean that indicates whether a boolean field is displayed/edited in a
	 * check or combo box.
	 */
	private boolean editBooleanInCheckBox = true;
	/**
	 * List of validators.
	 */
	private List<Validator<Value>> validators;
	/**
	 * Optional string converter.
	 */
	private StringConverter stringConverter;
	/**
	 * Horizontal alignment.
	 */
	private String horizontalAlignment = "LEFT";
	/**
	 * Edit mask.
	 */
	private String editMask;
	/**
	 * A pattern used to generate random values of this field (see
	 * <em>RandomData.getCode</em>)
	 */
	private RandomData.CodePattern randomPattern;
	/**
	 * An optional font name for the field in views.
	 */
	private String fontName;
	/**
	 * An optional font style for the field in views.
	 */
	private int fontStyle = -1;
	/**
	 * An optional font size for the field in views.
	 */
	private int fontSize = -1;

	/**
	 * A boolean that indicates if the field, when not present in an insert clause
	 * (DEFAULT) should be initialized with
	 * the database function for a date, time or time-stamp.
	 */
	private boolean currentDateTimeOrTimestamp = false;
	/**
	 * A flag that indicates whether this field is persistent.
	 */
	private boolean persistent = true;
	/**
	 * A flag that indicates whether this field can be null.
	 */
	private boolean nullable = true;
	/**
	 * A flag that indicates whether this field is a primary key field.
	 */
	private boolean primaryKey = false;
	/**
	 * A supported database function if the column is virtual or calculated.
	 */
	private String function;
	/**
	 * Optional parent table.
	 */
	private Table table;
	/**
	 * Optional parent view.
	 */
	private View view;

	/**
	 * Default constructor.
	 */
	public Field() {
		super();
	}

	/**
	 * Copy constructor.
	 *
	 * @param field The source field.
	 */
	public Field(Field field) {
		super();

		// Main properties.
		this.name = field.name;
		this.alias = field.alias;
		this.length = field.length;
		this.decimals = field.decimals;
		this.type = field.type;

		// Description and display properties.
		this.description = field.description;
		this.label = field.label;
		this.header = field.header;
		this.title = field.title;
		this.fixedWidth = field.fixedWidth;
		this.displayLength = field.displayLength;
		this.displayDecimals = field.displayDecimals;
		this.fixedWidth = field.fixedWidth;
		this.fieldGroup = field.fieldGroup;
		this.mainDescription = field.mainDescription;
		this.lookup = field.lookup;
		this.password = field.password;

		// Validation and formatting properties.
		this.initialValue = field.initialValue;
		this.maximumValue = field.maximumValue;
		this.minimumValue = field.minimumValue;
		this.possibleValues = new ArrayList<>(field.possibleValues);
		this.required = field.required;
		this.caseModifier = field.caseModifier;
		this.calculator = field.calculator;
		this.editBooleanInCheckBox = field.editBooleanInCheckBox;
		if (field.validators != null) {
			this.validators = new ArrayList<>();
			this.validators.addAll(field.validators);
		}
		this.stringConverter = field.stringConverter;
		this.horizontalAlignment = field.horizontalAlignment;
		this.randomPattern = field.randomPattern;
		this.fontName = field.fontName;
		this.fontStyle = field.fontStyle;
		this.fontSize = field.fontSize;

		// Database related properties.
		this.currentDateTimeOrTimestamp = field.currentDateTimeOrTimestamp;
		this.persistent = field.persistent;
		this.nullable = field.nullable;
		this.primaryKey = field.primaryKey;
		this.function = field.function;
		this.table = field.table;
		this.view = field.view;
	}

	/**
	 * Add a possible value.
	 *
	 * @param value The value.
	 * @param label The label.
	 */
	public void addPossibleValue(BigDecimal value, String label) {
		addPossibleValue(new Value(value), label);
	}

	/**
	 * Add a possible value.
	 *
	 * @param value The value.
	 * @param label The label.
	 */
	public void addPossibleValue(boolean value, String label) {
		addPossibleValue(new Value(value), label);
	}

	/**
	 * Add a possible value.
	 *
	 * @param value The value.
	 * @param label The label.
	 */
	public void addPossibleValue(Boolean value, String label) {
		addPossibleValue(new Value(value), label);
	}

	/**
	 * Add a possible value.
	 *
	 * @param value The value.
	 * @param label The label.
	 */
	public void addPossibleValue(byte[] value, String label) {
		addPossibleValue(new Value(value), label);
	}

	/**
	 * Add a possible value.
	 *
	 * @param value The value.
	 * @param label The label.
	 */
	public void addPossibleValue(double value, String label) {
		addPossibleValue(new Value(value), label);
	}

	/**
	 * Add a possible value.
	 *
	 * @param value The value.
	 * @param label The label.
	 */
	public void addPossibleValue(Double value, String label) {
		addPossibleValue(new Value(value), label);
	}

	/**
	 * Add a possible value.
	 *
	 * @param value The value.
	 * @param label The label.
	 */
	public void addPossibleValue(int value, String label) {
		addPossibleValue(new Value(value), label);
	}

	/**
	 * Add a possible value.
	 *
	 * @param value The value.
	 * @param label The label.
	 */
	public void addPossibleValue(Integer value, String label) {
		addPossibleValue(new Value(value), label);
	}

	/**
	 * Add a possible value.
	 *
	 * @param value The value.
	 * @param label The label.
	 */
	public void addPossibleValue(LocalDate value, String label) {
		addPossibleValue(new Value(value), label);
	}

	/**
	 * Add a possible value.
	 *
	 * @param value The value.
	 * @param label The label.
	 */
	public void addPossibleValue(LocalDateTime value, String label) {
		addPossibleValue(new Value(value), label);
	}

	/**
	 * Add a possible value.
	 *
	 * @param value The value.
	 * @param label The label.
	 */
	public void addPossibleValue(LocalTime value, String label) {
		addPossibleValue(new Value(value), label);
	}

	/**
	 * Add a possible value.
	 *
	 * @param value The value.
	 * @param label The label.
	 */
	public void addPossibleValue(long value, String label) {
		addPossibleValue(new Value(value), label);
	}

	/**
	 * Add a possible value.
	 *
	 * @param value The value.
	 * @param label The label.
	 */
	public void addPossibleValue(Long value, String label) {
		addPossibleValue(new Value(value), label);
	}

	/**
	 * Add a possible value.
	 *
	 * @param value The value.
	 * @param label The label.
	 */
	public void addPossibleValue(String value, String label) {
		addPossibleValue(new Value(value), label);
	}

	/**
	 * Add a possible value.
	 *
	 * @param value The value.
	 */
	public void addPossibleValue(Value value) {
		addPossibleValue(value, null);
	}

	/**
	 * Add a possible value.
	 *
	 * @param value The value.
	 * @param label The label.
	 */
	public void addPossibleValue(Value value, String label) {
		if (value.getType() != getType()) {
			throw new IllegalArgumentException("Invalid value type");
		}
		value.setLabel(label);
		possibleValues.add(value);
	}

	/**
	 * Add a validator to the list of default validators.
	 *
	 * @param validator The validator.
	 */
	public void addValidator(Validator<Value> validator) {
		if (validators == null) {
			validators = new ArrayList<>();
		}
		validators.add(validator);
	}

	/**
	 * Clear the list of possible values.
	 */
	public void clearPossibleValues() {
		possibleValues.clear();
	}

	/*
	 * Comparable and object overrides.
	 */
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(Object o) {
		Field field = null;
		try {
			field = (Field) o;
		} catch (ClassCastException exc) {
			throw new UnsupportedOperationException(
				"Not comparable type: " + o.getClass().getName());
		}
		if (getAlias().equals(field.getAlias())) {
			if (getType().equals(field.getType())) {
				if (getLength() == field.getLength()) {
					if (getDecimals() == field.getDecimals()) {
						return 0;
					}
				}
			}
		}
		return getAlias().compareTo(field.getAlias());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Field) {
			Field field = (Field) obj;
			return (compareTo(field) == 0);
		}
		return false;
	}

	/**
	 * Get the field alias.
	 *
	 * @return The field alias.
	 */
	public String getAlias() {
		if (alias == null) {
			return getName();
		}
		return alias;
	}

	/**
	 * Returns the default value padded with characters if it is string.
	 *
	 * @return The default blank value.
	 */
	public Value getBlankValue() {
		if (isString()) {
			return new Value(Strings.repeat(" ", getLength()));
		}
		return getDefaultValue();
	}

	/**
	 * Return the optional calculator.
	 *
	 * @return The optional calculator.
	 */
	public Calculator getCalculator() {
		return calculator;
	}

	/**
	 * Get the number of decimal places if applicable.
	 *
	 * @return The number of decimal places.
	 */
	public int getDecimals() {
		if (!isNumber()) {
			return 0;
		}
		if (isInteger() || isLong()) {
			return 0;
		}
		return decimals;
	}

	/**
	 * Returns the default value for this field.
	 *
	 * @return The default value.
	 */
	public Value getDefaultValue() {
		if (isBoolean()) {
			return new Value(false);
		}
		if (isByteArray()) {
			return new Value(new byte[0]);
		}
		if (isDate()) {
			return new Value((LocalDate) null);
		}
		if (isDecimal()) {
			return new Value(new BigDecimal(0).setScale(getDecimals(), RoundingMode.HALF_UP));
		}
		if (isDouble()) {
			return new Value((double) 0);
		}
		if (isInteger()) {
			return new Value(0);
		}
		if (isLong()) {
			return new Value((long) 0);
		}
		if (isString()) {
			return new Value("");
		}
		if (isTime()) {
			return new Value((LocalTime) null);
		}
		if (isDateTime()) {
			return new Value((LocalDateTime) null);
		}
		return null;
	}

	/**
	 * Get the long description.
	 *
	 * @return The long description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Return the display decimals.
	 *
	 * @return The decimals used to display the value.
	 */
	public int getDisplayDecimals() {
		if (displayDecimals >= 0) {
			return displayDecimals;
		}
		return getDecimals();
	}

	/**
	 * Returns a display description of this field using the description, title,
	 * label, header or something not null.
	 *
	 * @return A not null description.
	 */
	public String getDisplayDescription() {
		return Strings.getFirstNotNull(getDescription(), getTitle(), getLabel(), getHeader());
	}

	/**
	 * Returns a display header of this field using the header, label, title,
	 * description or something not null.
	 *
	 * @return A not null header.
	 */
	public String getDisplayHeader() {
		return Strings.getFirstNotNull(getHeader(), getLabel(), getTitle(), getDescription());
	}

	/**
	 * Returns a display label of this field using the label, header, title,
	 * description or something not null.
	 *
	 * @return A not null header.
	 */
	public String getDisplayLabel() {
		return Strings.getFirstNotNull(getLabel(), getHeader(), getTitle(), getDescription());
	}

	/**
	 * Get the display length.
	 *
	 * @return The display length.
	 */
	public int getDisplayLength() {
		/* Fixed display length for number fields. */
		if (isNumber()) {
			return NUMBER_DISPLAY_LENGTH;
		}
		if (isDate()) {
			return DATE_DISPLAY_LENGTH;
		}
		if (isTime()) {
			return TIME_DISPLAY_LENGTH;
		}
		if (isDateTime()) {
			return DATE_TIME_DISPLAY_LENGTH;
		}
		if (displayLength > 0) {
			return displayLength;
		}
		return getLength();
	}

	/**
	 * Returns a display title of this field using the title, label, header,
	 * description or something not null.
	 *
	 * @return A not null title.
	 */
	public String getDisplayTitle() {
		return Strings.getFirstNotNull(getTitle(), getLabel(), getHeader(), getDescription());
	}

	/**
	 * Return the optional edit mask.
	 * 
	 * @return The edit mask.
	 */
	public String getEditMask() {
		return editMask;
	}

	/**
	 * Returns the field group if any.
	 *
	 * @return The field group if any.
	 */
	public FieldGroup getFieldGroup() {
		return (fieldGroup != null ? fieldGroup : FieldGroup.EMPTY_FIELD_GROUP);
	}

	/**
	 * Return the font name.
	 *
	 * @return The font name.
	 */
	public String getFontName() {
		return fontName;
	}

	/**
	 * Return the font size.
	 *
	 * @return The font size.
	 */
	public int getFontSize() {
		return fontSize;
	}

	/**
	 * Return the font style.
	 *
	 * @return The font style.
	 */
	public int getFontStyle() {
		return fontStyle;
	}

	/**
	 * Gets the function or formula.
	 *
	 * @return The function.
	 */
	public String getFunction() {
		return function;
	}

	/**
	 * Get the header used in tables.
	 *
	 * @return The header used in tables.
	 */
	public String getHeader() {
		return header;
	}

	/**
	 * Return the horizontal alignment.
	 *
	 * @return The horizontal alignment.
	 */
	public String getHorizontalAlignment() {
		return horizontalAlignment;
	}

	/**
	 * Get the initial value.
	 *
	 * @return The initial value.
	 */
	public Value getInitialValue() {
		return initialValue;
	}

	/**
	 * Get the label used in forms.
	 *
	 * @return The label used in forms.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Get the length if applicable, otherwise -1.
	 *
	 * @return The field length if applicable, otherwise -1.
	 */
	public int getLength() {
		return length;
	}

	/**
	 * Get the maximum value.
	 *
	 * @return The maximum value.
	 */
	public Value getMaximumValue() {
		return maximumValue;
	}

	/**
	 * Get the minimum value.
	 *
	 * @return The minimum value.
	 */
	public Value getMinimumValue() {
		return minimumValue;
	}

	/**
	 * Get the name.
	 *
	 * @return The name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the name to use in a <code>CREATE TABLE</code> or
	 * <code>ALTER TABLE</code> statement.
	 *
	 * @return The name.
	 */
	public String getNameCreate() {
		return getName();
	}

	/**
	 * Returns the name to use in an <code>DELETE</code> statement.
	 *
	 * @return The name.
	 */
	public String getNameDelete() {
		return getNameUpdate();
	}

	/**
	 * Returns the name to use in a <code>GROUP BY</code> clause of a
	 * <code>SELECT</code> query.
	 *
	 * @return The name.
	 */
	public String getNameGroupBy() {
		StringBuilder name = new StringBuilder();
		if (isVirtual()) {
			name.append("(");
			name.append(getFunction());
			name.append(")");
		} else {
			name.append(getNameParent());
		}
		return name.toString();
	}

	/**
	 * Returns the name to use in an <code>ORDER BY</code> clause of a select query.
	 *
	 * @return The name.
	 */
	public String getNameOrderBy() {
		StringBuilder name = new StringBuilder();
		if (isVirtual()) {
			name.append("(");
			name.append(getFunction());
			name.append(")");
		} else {
			name.append(getNameParent());
		}
		return name.toString();
	}

	/**
	 * Returns the name of the field in the database, qualified with the parent
	 * table or view alias if it exists.
	 *
	 * @return The name.
	 */
	public String getNameParent() {
		String name = getName();
		String tableAlias = (getTable() != null ? getTable().getAlias() : null);
		if (tableAlias != null) {
			return tableAlias + "." + name;
		}
		return name;
	}

	/**
	 * Returns the name to use in a relation of a select statement.
	 *
	 * @return The name.
	 */
	public String getNameRelate() {
		return getNameParent();
	}

	/**
	 * Returns the name to use in the column list of a <code>SELECT</code> query.
	 *
	 * @return The name.
	 */
	public String getNameSelect() {
		StringBuilder name = new StringBuilder();
		if (isVirtual()) {
			name.append("(");
			name.append(getFunction());
			name.append(")");
		} else {
			name.append(getNameParent());
		}
		if (getAlias() != null) {
			name.append(" AS ");
			name.append(getAlias());
		}
		return name.toString();
	}

	/**
	 * Returns the name to use in an <code>UPDATE</code> statement.
	 *
	 * @return The name.
	 */
	public String getNameUpdate() {
		String name = getName();
		String parentName = (getTable() != null ? getTable().getName() : null);
		if (parentName != null) {
			return parentName + "." + name;
		}
		return name;
	}

	/**
	 * Returns the name to use in a <code>WHERE</code> clause.
	 *
	 * @return The name.
	 */
	public String getNameWhere() {
		if (isVirtual()) {
			return "(" + getFunction() + ")";
		}
		return getNameParent();
	}

	/**
	 * Returns the null value for this field.
	 *
	 * @return The null value.
	 */
	public Value getNullValue() {
		return getType().getNullValue();
	}

	/**
	 * Return the possible value given the label.
	 *
	 * @param label The label
	 * @return The possible value.
	 */
	public Value getPossibleValue(String label) {
		for (Value value : possibleValues) {
			String valueLabel;
			if (value.getLabel() != null) {
				valueLabel = value.getLabel();
			} else {
				valueLabel = value.toString();
			}
			if (valueLabel.equals(label)) {
				return value;
			}
		}
		throw new IllegalArgumentException("Invalid possible value label");
	}

	/**
	 * Returns the possible value label or null if not applicable or not found.
	 *
	 * @param value The target value.
	 * @return The possible value label.
	 */
	public String getPossibleValueLabel(Value value) {
		for (Value possibleValue : possibleValues) {
			if (possibleValue.equals(value)) {
				String label = possibleValue.getLabel();
				if (label == null) {
					label = possibleValue.toString();
				}
				return label;
			}
		}
		return "";
	}

	/**
	 * Return the list of possible values.
	 *
	 * @return The list of possible values.
	 */
	public List<Value> getPossibleValues() {
		return new ArrayList<>(possibleValues);
	}

	/**
	 * Return the random pattern for random string values generation.
	 *
	 * @return The random pattern.
	 */
	public RandomData.CodePattern getRandomPattern() {
		return randomPattern;
	}

	/**
	 * Returns the list of relations associated with the parent view or table of
	 * this field.
	 *
	 * @return The list of relations.
	 */
	public List<Relation> getRelations() {
		List<Relation> relations = new ArrayList<>();
		if (getView() != null) {
			relations.addAll(getView().getRelations());
		} else {
			if (getTable() != null) {
				List<ForeignKey> foreignKeys = getTable().getForeignKeys();
				for (ForeignKey foreignKey : foreignKeys) {
					relations.add(foreignKey.getRelation());
				}
			}
		}
		return relations;
	}

	/**
	 * Return the string converter.
	 *
	 * @return The string converter.
	 */
	public StringConverter getStringConverter() {
		return stringConverter;
	}

	/**
	 * Return this field parent table.
	 *
	 * @return The parent table.
	 */
	public Table getTable() {
		return table;
	}

	/**
	 * Get the title or short description.
	 *
	 * @return The title or short description.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Get the type.
	 *
	 * @return The type.
	 */
	public Types getType() {
		return type;
	}

	/**
	 * Returns the validation message or null if validation is ok.
	 *
	 * @param value The value to check for the validation message.
	 * @return The validation message or null if validation is ok.
	 */
	public String getValidationMessage(Value value) {
		if (validators != null) {
			for (Validator<Value> validator : validators) {
				String message = validator.getMessage(value);
				if (message != null && !message.isEmpty()) {
					return message;
				}
			}
		}
		return null;
	}

	/**
	 * Return the list of validators.
	 *
	 * @return The list of validators.
	 */
	public List<Validator<Value>> getValidators() {
		if (validators == null) {
			return new ArrayList<>();
		}
		return validators;
	}

	/**
	 * Return this field parent view.
	 *
	 * @return The parent view.
	 */
	public View getView() {
		return view;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int hash = 3;
		return hash;
	}

	/*
	 * Type checking.
	 */
	/**
	 * Check if this field is boolean.
	 *
	 * @return A boolean.
	 */
	public boolean isBoolean() {
		return getType().isBoolean();
	}

	/**
	 * Check if this field is binary (byte[]).
	 *
	 * @return A boolean.
	 */
	public boolean isByteArray() {
		return getType().isByteArray();
	}

	/**
	 * Check if his field should initialize to the current date.
	 *
	 * @return A boolean indicating that it should be initialized to the current
	 *         date.
	 */
	public boolean isCurrentDate() {
		return isDate() && isCurrentDateTimeOrTimestamp();
	}

	/**
	 * Check the current date, time or time-stamp flag.
	 *
	 * @return A boolean.
	 */
	public boolean isCurrentDateTimeOrTimestamp() {
		return currentDateTimeOrTimestamp;
	}

	/**
	 * Check if his field should initialize to the current time.
	 *
	 * @return A boolean indicating that it should be initialized to the current
	 *         time.
	 */
	public boolean isCurrentTime() {
		return isTime() && isCurrentDateTimeOrTimestamp();
	}

	/**
	 * Check if his field should initialize to the current time-stamp.
	 *
	 * @return A boolean indicating that it should be initialized to the current
	 *         time-stamp.
	 */
	public boolean isCurrentTimestamp() {
		return isDateTime() && isCurrentDateTimeOrTimestamp();
	}

	/**
	 * Check if this field is a date.
	 *
	 * @return A boolean.
	 */
	public boolean isDate() {
		return getType().isDate();
	}

	/**
	 * Check if this field is a time.
	 *
	 * @return A boolean.
	 */
	public boolean isDateTime() {
		return getType().isDateTime();
	}

	/**
	 * Check if this field is a number (decimal) with fixed precision.
	 *
	 * @return A boolean.
	 */
	public boolean isDecimal() {
		return getType().isDecimal();
	}

	/**
	 * Check if this field is a double.
	 *
	 * @return A boolean.
	 */
	public boolean isDouble() {
		return getType().isDouble();
	}

	/**
	 * Check whether boolean will be edited with a check box.
	 *
	 * @return A boolean.
	 */
	public boolean isEditBooleanInCheckBox() {
		return editBooleanInCheckBox;
	}

	/**
	 * Check if the field is fixed with. Non fixed width fields expand to the width
	 * of the form.
	 *
	 * @return A boolean indicating if the field is fixed-width.
	 */
	public boolean isFixedWidth() {
		return fixedWidth;
	}

	/**
	 * Check if this field is a floating point number.
	 *
	 * @return A boolean.
	 */
	public boolean isFloatingPoint() {
		return getType().isFloatingPoint();
	}

	/**
	 * Check if this field is a foreign field, that is, belongs to a foreign table
	 * in the list of relations of the
	 * parent view.
	 *
	 * @return A boolean that indicates if this field is a foreign field
	 */
	public boolean isForeign() {
		/*
		 * Parent table null and parent view not null can not be. Or both null, or
		 * parent view or none.
		 */
		if (getTable() == null) {
			return false;
		}
		if (getView() == null) {
			return false;
		}
		if (getView().getMasterTable().equals(getTable())) {
			return false;
		}
		List<Relation> relations = getView().getRelations();
		for (Relation relation : relations) {
			if (relation.getForeignTable().equals(getTable())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check whether the horizontal alignment is center.
	 *
	 * @return A boolean.
	 */
	public boolean isHorizontalAlignmentCenter() {
		return horizontalAlignment.equals("CENTER");
	}

	/**
	 * Check whether the horizontal alignment is left.
	 *
	 * @return A boolean.
	 */
	public boolean isHorizontalAlignmentLeft() {
		return horizontalAlignment.equals("LEFT");
	}

	/**
	 * Check whether the horizontal alignment is right.
	 *
	 * @return A boolean.
	 */
	public boolean isHorizontalAlignmentRight() {
		return horizontalAlignment.equals("RIGHT");
	}

	/**
	 * Check if this field is an integer.
	 *
	 * @return A boolean.
	 */
	public boolean isInteger() {
		return getType().isInteger();
	}

	/**
	 * Check if this field is a local field. Has no parent table or belongs to the
	 * parent table.
	 *
	 * @return A boolean that indicates if this field is a local field.
	 */
	public boolean isLocal() {
		return !isForeign();
	}

	/**
	 * Check if this field is a long.
	 *
	 * @return A boolean.
	 */
	public boolean isLong() {
		return getType().isLong();
	}

	/**
	 * Check if this is a lookup field.
	 *
	 * @return A boolean.
	 */
	public boolean isLookup() {
		return lookup;
	}

	/**
	 * Check whether edition is lower case.
	 *
	 * @return A boolean
	 */
	public boolean isLowercase() {
		return caseModifier.equals("LOWER");
	}

	/**
	 * Return the main-description indicator.
	 *
	 * @return A boolean.
	 */
	public boolean isMainDescription() {
		return mainDescription;
	}

	/**
	 * Check whether this field can be null.
	 *
	 * @return A boolean
	 */
	public boolean isNullable() {
		return nullable;
	}

	/**
	 * Check if this field is a number (decimal, double or integer).
	 *
	 * @return A boolean.
	 */
	public boolean isNumber() {
		return getType().isNumber();
	}

	/**
	 * Return indicating if the field is a password field.
	 *
	 * @return A boolean.
	 */
	public boolean isPassword() {
		return password;
	}

	/**
	 * Check whether this field is persistent.
	 *
	 * @return A boolean
	 */
	public boolean isPersistent() {
		return persistent;
	}

	/**
	 * Check if the field has possible values.
	 *
	 * @return A boolean.
	 */
	public boolean isPossibleValues() {
		return !possibleValues.isEmpty();
	}

	/**
	 * Check whether this field is a primary key field.
	 *
	 * @return A boolean
	 */
	public boolean isPrimaryKey() {
		return primaryKey;
	}

	/**
	 * Check whether a non empty value is required for this field.
	 *
	 * @return A boolean.
	 */
	public boolean isRequired() {
		return required;
	}

	/**
	 * Check if this field is a string.
	 *
	 * @return A boolean.
	 */
	public boolean isString() {
		return getType().isString();
	}

	/**
	 * Check if this field is a time.
	 *
	 * @return A boolean.
	 */
	public boolean isTime() {
		return getType().isTime();
	}

	/**
	 * Check whether edition is upper case.
	 *
	 * @return A boolean
	 */
	public boolean isUppercase() {
		return caseModifier.equals("UPPER");
	}

	/**
	 * Check if this column is virtual. A column is virtual is it has a function but
	 * not a name.
	 *
	 * @return A <code>boolean</code>.
	 */
	public boolean isVirtual() {
		return (getFunction() != null);
	}

	/**
	 * Set the field alias.
	 *
	 * @param alias The field alias.
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}

	/**
	 * Set the optional calculator.
	 *
	 * @param calculator The optional calculator.
	 */
	public void setCalculator(Calculator calculator) {
		this.calculator = calculator;
	}

	/**
	 * Set the current date, time or time-stamp flag.
	 *
	 * @param currentDateTimeOrTimestamp A boolean.
	 */
	public void setCurrentDateTimeOrTimestamp(boolean currentDateTimeOrTimestamp) {
		this.currentDateTimeOrTimestamp = currentDateTimeOrTimestamp;
	}

	/**
	 * Set the number of decimal places.
	 *
	 * @param decimals The number of decimal places.
	 */
	public void setDecimals(int decimals) {
		this.decimals = decimals;
		setDisplayDecimals(decimals);
	}

	/**
	 * Set the long description.
	 *
	 * @param description The long description.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Set the decimals used to display the value.
	 *
	 * @param displayDecimals The display decimals.
	 */
	public void setDisplayDecimals(int displayDecimals) {
		this.displayDecimals = displayDecimals;
	}

	/**
	 * Set the display length.
	 *
	 * @param displayLength The display length.
	 */
	public void setDisplayLength(int displayLength) {
		this.displayLength = displayLength;
	}

	/**
	 * Set whether boolean will be edited with a check box.
	 *
	 * @param editBooleanInCheckBox A boolean.
	 */
	public void setEditBooleanInCheckBox(boolean editBooleanInCheckBox) {
		this.editBooleanInCheckBox = editBooleanInCheckBox;
	}

	/**
	 * Set the edit mask. The field must be a string field.
	 * 
	 * @param editMask The edit mask.
	 */
	public void setEditMask(String editMask) {
		if (!isString()) {
			throw new IllegalArgumentException();
		}
		this.editMask = editMask;
	}

	/**
	 * Sets the field group.
	 *
	 * @param fieldGroup The field group.
	 */
	public void setFieldGroup(FieldGroup fieldGroup) {
		this.fieldGroup = fieldGroup;
	}

	/**
	 * Set if the field is fixed width.
	 *
	 * @param fixedWidth A boolean
	 */
	public void setFixedWidth(boolean fixedWidth) {
		this.fixedWidth = fixedWidth;
	}

	/**
	 * Set the font name.
	 *
	 * @param fontName The font name.
	 */
	public void setFontName(String fontName) {
		this.fontName = fontName;
	}

	/**
	 * Set the font size.
	 *
	 * @param fontSize The font size.
	 */
	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	/**
	 * Set the font style.
	 *
	 * @param fontStyle The font style.
	 */
	public void setFontStyle(int fontStyle) {
		this.fontStyle = fontStyle;
	}

	/**
	 * Sets the function or formula.
	 *
	 * @param function The function.
	 */
	public void setFunction(String function) {
		this.function = function;
	}

	/**
	 * Set the header used in tables.
	 *
	 * @param header The header used in tables.
	 */
	public void setHeader(String header) {
		this.header = header;
	}

	/**
	 * Set the horizontal alignment center.
	 */
	public void setHorizontalAlignmentCenter() {
		this.horizontalAlignment = "CENTER";
	}

	/**
	 * Set the horizontal alignment left.
	 */
	public void setHorizontalAlignmentLeft() {
		this.horizontalAlignment = "LEFT";
	}

	/**
	 * Set the horizontal alignment right.
	 */
	public void setHorizontalAlignmentRight() {
		this.horizontalAlignment = "RIGHT";
	}

	/**
	 * Set the initial value.
	 *
	 * @param initialValue The initial value.
	 */
	public void setInitialValue(Value initialValue) {
		Types.validateValueType(initialValue, getType());
		this.initialValue = initialValue;
	}

	/**
	 * Set the label used in forms.
	 *
	 * @param label The label used in forms.
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Set the field length.
	 *
	 * @param length The field length.
	 */
	public void setLength(int length) {
		this.length = length;
	}

	/**
	 * Set the field as lookup.
	 *
	 * @param lookup A boolean.
	 */
	public void setLookup(boolean lookup) {
		this.lookup = lookup;
	}

	/**
	 * Set whether edition is lower case.
	 *
	 * @param lowercase A boolean indicating that the value is lowercase.
	 */
	public void setLowercase(boolean lowercase) {
		this.caseModifier = (lowercase ? "LOWER" : "NONE");
	}

	/**
	 * Set the main-description indicator.
	 *
	 * @param mainDescription A boolean.
	 */
	public void setMainDescription(boolean mainDescription) {
		this.mainDescription = mainDescription;
	}

	/**
	 * Set the maximum value.
	 *
	 * @param maximumValue The maximum value.
	 */
	public void setMaximumValue(Value maximumValue) {
		Types.validateValueType(maximumValue, getType());
		this.maximumValue = maximumValue;
	}

	/**
	 * Set the minimum value.
	 *
	 * @param minimumValue The minimum value.
	 */
	public void setMinimumValue(Value minimumValue) {
		Types.validateValueType(minimumValue, getType());
		this.minimumValue = minimumValue;
	}

	/**
	 * Set the name.
	 *
	 * @param name The name of the field.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Set whether this field can be null.
	 *
	 * @param nullable A boolean
	 */
	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}

	/**
	 * Set if the field is a password field.
	 *
	 * @param password A boolean.
	 */
	public void setPassword(boolean password) {
		this.password = password;
	}

	/**
	 * Sets whether this field is persistent.
	 *
	 * @param persistent A boolean.
	 */
	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}

	/**
	 * Set whether this field is a primary key field.
	 *
	 * @param primaryKey A boolean.
	 */
	public void setPrimaryKey(boolean primaryKey) {
		this.primaryKey = primaryKey;
		if (primaryKey) {
			setNullable(false);
		}
	}

	/**
	 * Set the random pattern for random string values generation.
	 *
	 * @param randomPattern The random pattern.
	 */
	public void setRandomPattern(RandomData.CodePattern randomPattern) {
		this.randomPattern = randomPattern;
	}

	/**
	 * Set whether a non empty value is required for this field.
	 *
	 * @param required A boolean.
	 */
	public void setRequired(boolean required) {
		this.required = required;
	}

	/**
	 * Set the string converter.
	 *
	 * @param stringConverter The string converter.
	 */
	public void setStringConverter(StringConverter stringConverter) {
		this.stringConverter = stringConverter;
	}

	/**
	 * Set this field parent table.
	 *
	 * @param table The parent table.
	 */
	public void setTable(Table table) {
		this.table = table;
	}

	/**
	 * Set the title or short description.
	 *
	 * @param title The title or short description.
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Set the type.
	 *
	 * @param type The type.
	 */
	public void setType(Types type) {
		this.type = type;
		if (isNumber()) {
			setHorizontalAlignmentRight();
		} else {
			if (isBoolean()) {
				setHorizontalAlignmentCenter();
			} else {
				setHorizontalAlignmentLeft();
			}
		}
	}

	/**
	 * Set whether edition is upper case.
	 *
	 * @param uppercase A boolean indicating that the value is uppercase.
	 */
	public void setUppercase(boolean uppercase) {
		this.caseModifier = (uppercase ? "UPPER" : "NONE");
	}

	/**
	 * Set this field parent view.
	 *
	 * @param view The parent view.
	 */
	public void setView(View view) {
		this.view = view;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("Field: ");
		b.append(getAlias());
		b.append(", ");
		b.append(getType());
		b.append(", ");
		b.append(getLength());
		b.append(", ");
		b.append(getDecimals());
		return b.toString();
	}

	/**
	 * Validates the convenience of the argument value.
	 *
	 * @param value The value to validate.
	 * @return A boolean indicating if the value is valid.
	 */
	public boolean validate(Value value) {
		if (validators != null) {
			for (Validator<Value> validator : validators) {
				if (!validator.validate(value)) {
					return false;
				}
			}
		}
		return true;
	}
}
