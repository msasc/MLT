/*
 * Copyright (C) 2018 Miquel Sas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.mlt.launch;

import java.nio.channels.IllegalSelectorException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mlt.util.Lists;
import com.mlt.util.Strings;

/**
 * Startup or command line argument manager.
 * 
 * @author Miquel Sas
 */
public class ArgumentManager {

	/** The list of defined arguments. */
	private List<Argument> arguments = new ArrayList<>();
	/** The map with parsed values. */
	private Map<String, List<String>> valuesMap = new HashMap<>();
	/** List of possible name-value separators. */
	private List<String> nameValueSeparators = new ArrayList<>(Lists.asList("=", ":"));
	/** List of errors occurred during parse. */
	private List<String> errors = new ArrayList<>();

	/**
	 * Default constructor.
	 */
	public ArgumentManager() {
		super();
	}

	/**
	 * Constructor assigning the list of arguments.
	 * 
	 * @param arguments The list of arguments.
	 */
	public ArgumentManager(Argument... arguments) {
		super();
		this.arguments.addAll(Lists.asList(arguments));
	}

	/**
	 * Constructor assigning the list of arguments.
	 * 
	 * @param arguments The list of arguments.
	 */
	public ArgumentManager(List<Argument> arguments) {
		super();
		this.arguments.addAll(arguments);
	}

	/**
	 * Add an argument to the list of valid arguments.
	 * 
	 * @param argument The argument.
	 */
	public void add(Argument argument) {
		arguments.add(argument);
	}

	/**
	 * Return the list of name-value separators to be setup. Default are "=" and ":".
	 * 
	 * @return The list of name-value separators to be setup.
	 */
	protected List<String> getNameValueSeparators() {
		return nameValueSeparators;
	}

	/**
	 * Parse the command line arguments and validate them, returning <tt>false</tt> if any error occurred.
	 * 
	 * @param args The command line arguments.
	 * @return <tt>false</tt> if any error occurred.
	 */
	public boolean parse(String[] args) {

		// Clear errors.
		errors.clear();

		// Check required arguments.
		List<Argument> requiredArguments = getRequiredArguments();
		for (Argument argument : requiredArguments) {
			if (!containsArgument(argument, args)) {
				errors.add("Argument " + argument.getName() + " is required");
			}
		}

		// Parse each command line argument, validating it and retrieving its values.
		for (String arg : args) {

			// Get the argument.
			Argument argument = getArgument(arg);
			if (argument == null) {
				errors.add("Invalid argument: " + getArg(arg));
				continue;
			}

			// List of passed values.
			List<String> values = parseValues(argument, arg);

			// Argument without values.
			if (!argument.isValuesRequired()) {
				if (!values.isEmpty()) {
					errors.add("Argument " + argument.getName() + " does not require values");
					continue;
				}
				valuesMap.put(argument.getName().toLowerCase(), null);
				continue;
			}

			// Argument with values.
			if (argument.isValuesRequired()) {
				if (values.isEmpty()) {
					errors.add("Argument " + argument.getName() + " requires at least a value");
					continue;
				}
				// If the argument does not accept multiple values, check the size of the list of values.
				if (!argument.isMultipleValues() && values.size() > 1) {
					errors.add("Argument " + argument.getName() + " can have only one single value");
					continue;
				}
				// Validate possible values.
				if (!argument.getPossibleValues().isEmpty()) {
					boolean error = false;
					for (String value : values) {
						boolean validated = false;
						for (String possibleValue : argument.getPossibleValues()) {
							if (value.toLowerCase().equals(possibleValue.toLowerCase())) {
								validated = true;
								break;
							}
						}
						if (!validated) {
							errors.add("Argument " + argument.getName() + " invalid value: " + value);
							error = true;
						}
					}
					if (error) {
						continue;
					}
				}

				// Store the values.
				valuesMap.put(argument.getName().toLowerCase(), values);
				continue;
			}

			// Should never come here.
			errors.add("Argument " + argument.getName() + " unknown error");
		}

		return errors.isEmpty();
	}

	/**
	 * Parse values for an argument that matches with values.
	 * 
	 * @param argument The argument.
	 * @param arg      The command line argument.
	 * @return The list of values.
	 */
	private List<String> parseValues(Argument argument, String arg) {
		List<String> values = new ArrayList<>();
		if (checkArgValue(argument, arg)) {
			String valueString = getValueString(argument, arg);
			String[] valueArray = Strings.parse(valueString, "+");
			values.addAll(Lists.asList(valueArray));
		}
		return values;
	}

	/**
	 * Return the value string.
	 * 
	 * @param argument The argument.
	 * @param arg      The command line argument.
	 * @return The value string.
	 */
	private String getValueString(Argument argument, String arg) {
		for (String sep : nameValueSeparators) {
			if (getArg(arg).toLowerCase().startsWith(argument.getName().toLowerCase() + sep)) {
				return getArg(arg).substring((argument.getName() + sep).length());
			}
		}
		throw new IllegalSelectorException();
	}

	/**
	 * Check if the argument has value.
	 * 
	 * @param argument The argument.
	 * @param arg      The command line argument.
	 * @return A boolean.
	 */
	private boolean checkArgValue(Argument argument, String arg) {
		for (String sep : nameValueSeparators) {
			if (getArg(arg).toLowerCase().startsWith(argument.getName().toLowerCase() + sep)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the argument that corresponds to the command line argument, if any.
	 * 
	 * @param arg The command line argument.
	 * @return The parameterized argument.
	 */
	private Argument getArgument(String arg) {
		for (Argument argument : arguments) {
			if (isArgument(argument, arg)) {
				return argument;
			}
		}
		return null;
	}

	/**
	 * Returns a boolean indicating if the argument is contained in the list of command line arguments.
	 * 
	 * @param argument The argument to check.
	 * @param args     The list of command line arguments.
	 * @return A boolean.
	 */
	private boolean containsArgument(Argument argument, String[] args) {
		for (String arg : args) {
			if (isArgument(argument, arg)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if the argument meets the command line argument.
	 * 
	 * @param argument The argument.
	 * @param arg      The command line argument.
	 * @return A boolean that indicates whether they meet.
	 */
	private boolean isArgument(Argument argument, String arg) {
		if (checkArgValue(argument, arg)) {
			return true;
		}
		return getArg(arg).toLowerCase().equals(argument.getName().toLowerCase());
	}

	/**
	 * Returns the argument without the starting slash if present.
	 * 
	 * @param arg The command line aergument.
	 * @return The argument without the starting slash if present.
	 */
	private String getArg(String arg) {
		// Do not consider starting slash.
		if (arg.startsWith("--")) {
			return arg.substring(2);
		}
		// Do not consider starting slash.
		if (arg.startsWith("/")) {
			return arg.substring(1);
		}
		return arg;
	}

	/**
	 * Returns the list of required arguments.
	 * 
	 * @return The list of required arguments.
	 */
	private List<Argument> getRequiredArguments() {
		List<Argument> requiredArguments = new ArrayList<>();
		for (Argument argument : arguments) {
			if (argument.isRequired()) {
				requiredArguments.add(argument);
			}
		}
		return requiredArguments;
	}

	/**
	 * Returns the list of errors occurred during parse.
	 * 
	 * @return The list of errors occurred during parse.
	 */
	public List<String> getErrors() {
		return errors;
	}

	/**
	 * Check, after parsing, if the argument name was passed.
	 * 
	 * @param argumentName The argument name.
	 * @return A boolean.
	 */
	public boolean isPassed(String argumentName) {
		return valuesMap.containsKey(argumentName.toLowerCase());
	}

	/**
	 * Returns the list of values of an argument.
	 * 
	 * @param argumentName The argument name.
	 * @return The list of values.
	 */
	public List<String> getValues(String argumentName) {
		return valuesMap.get(argumentName.toLowerCase());
	}

	/**
	 * Returns the (first) value or the argument or null if not present or the argument does not accept values.
	 * 
	 * @param argumentName The argument name.
	 * @return The (first) value or null.
	 */
	public String getValue(String argumentName) {
		List<String> values = getValues(argumentName);
		if (values != null && !values.isEmpty()) {
			return values.get(0);
		}
		return null;
	}
}
