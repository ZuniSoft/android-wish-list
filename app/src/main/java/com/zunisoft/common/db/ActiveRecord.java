/*
 * ZUNISOFT CONFIDENTIAL
 * _____________________
 *
 *  Copyright [2014] - [2015] ZuniSoft, LLC
 *  All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains
 * the property of ZuniSoft, LLC and its suppliers, if any.
 * The intellectual and technical concepts contained herein
 * are proprietary to ZuniSoft, LLC and its suppliers and may
 * be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly
 * forbidden unless prior written permission is obtained from
 * ZuniSoft, LLC.
 */

package com.zunisoft.common.db;

import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Base class for data objects.
 * 
 * This is a pseudo active record abstract class that will be extended by
 * concrete data objects.
 * 
 * @author krdavis
 */
public abstract class ActiveRecord {
	/** Database object */
	protected SQLiteDatabase db;
	
	/** Errors collection - database column name|error message */
	protected HashMap<String, String> errors = new HashMap<String, String>();
	
	/** Application resources object */
	protected Resources res;
	
	/** Single quote */
	protected static final String SINGLE_QUOTE = "'";
	
	/** Escaped single quote */
	protected static final String ESC_SINGLE_QUOTE = "''";
	
	/**
	 * Constructs a new <tt>ActiveRecord</tt> object.
	 * 
	 * @param res
	 *            Application resources object.
	 */
	public ActiveRecord(Resources res) {
		this.res = res;
	}
	
	/**
	 * Saves the model to the database.
	 * 
	 * @return The row ID of newly inserted row, the number of rows affected by
	 *         an update or -1 if an error occurred.
	 */
	abstract public long save();

	/**
	 * Deletes the model from the database.
	 * 
	 * @return True on success, false otherwise.
	 */
	abstract public boolean delete();

	/**
	 * Loads a record with the specified ID from the database into the model.
	 * 
	 * @param id
	 *            The ID of the record to load.
	 * @throws RecordNotFoundException
	 *             If the record cannot be found.
	 */
	abstract public void load(int id) throws RecordNotFoundException;

	/**
	 * Reloads the model from the database.
	 * 
	 * @throws RecordNotFoundException
	 *             If the record cannot be found.
	 */
	abstract public void reload() throws RecordNotFoundException;

	/**
	 * Retrieves all records from the database associated with the model.
	 * 
	 * @return Result set of all records associated with the model.
	 */
	abstract public Cursor findAll();
	
	/**
	 * Sets the database object for the model.
	 * 
	 * @param db
	 *            Database object.
	 */
	public void setSQLiteDatabase(SQLiteDatabase db) {
		this.db = db;
	}
	
	/**
	 * Gets the errors collection that holds all of the model's error messages.
	 * 
	 * The map key holds the database column and the value holds the error
	 * message.
	 * 
	 * @return The errors collection (read-only).
	 */
	public Map<String, String> getErrors() {
		return Collections.unmodifiableMap(errors);
	}
	
	/**
	 * Performs validation checks on the model.
	 * 
	 * This method should be overridden. Any validation errors can be retrieved
	 * by calling getErrors().
	 * 
	 * @return True on success, false otherwise.
	 */
	public boolean validate() {
		errors.clear();
		return true;
	}
	
	/**
	 * Validates whether the specified value is of the correct form by matching
	 * it against the regular expression provided.
	 * 
	 * @param value
	 *            String to validate.
	 * @param regex
	 *            Regular expression used to validate the specified value.
	 * @param colName
	 *            Database column name associated with the value being
	 *            validated.
	 * @param msg
	 *            Message to add to the errors collection if the value is
	 *            invalid.
	 * @return True on success, false otherwise.
	 */
	protected boolean validateFormatOf(String value, String regex,
			String colName, String msg) {
		boolean validated = true;
		
		if (!Pattern.matches(regex, value)) {
			errors.put(colName, msg);
			validated = false;
		}
		
		return validated;
	}
	
	/**
	 * Validates that the specified value matches the length restrictions
	 * supplied.
	 * 
	 * @param value
	 *            String to validate.
	 * @param min
	 *            Minimum length the specified value
	 * @param max
	 *            Maximum length the specified value
	 * @param colName
	 *            Database column name associated with the value being
	 *            validated.
	 * @param msg
	 *            Message to add to the errors collection if the value is
	 *            invalid.
	 * @return True on success, false otherwise.
	 * @throws InvalidParameterException
	 *             If min < 0, max < 0 or min > max.
	 */
	protected boolean validateLengthOf(String value, int min, int max,
			String colName, String msg) throws InvalidParameterException {
		boolean minValidated = true;
		boolean maxValidated = true;

		// Check for invalid length parameters
		if (min < 0 || max < 0 || min > max)
			throw new InvalidParameterException();
		
		// Check minimum length
		if (value.length() < min) {
			minValidated = false;
		}
		
		// Check maximum length
		if (value.length() > max) {
			maxValidated = false;
		}
			
		// Check the results and return
		if (minValidated && maxValidated) {
			return true;
		} else {
			errors.put(colName, msg);
			return false;
		}
	}
	
	/**
	 * Validates whether the specified value is a number.
	 * 
	 * @param value
	 *            String to validate.
	 * @param colName
	 *            Database column name associated with the value being
	 *            validated.
	 * @param msg
	 *            Message to add to the errors collection if the value is
	 *            invalid.
	 * @return True on success, false otherwise.
	 */
	protected boolean validateNumericalityOf(String value, String colName,
			String msg) {
		boolean validated = true;
		
		try {
			Double.parseDouble(value);
		} catch (Exception e) {
			errors.put(colName, msg);
			validated = false;
		}

		return validated;
	}
	
	/**
	 * Validates that the specified value is not null or blank.
	 * 
	 * @param value
	 *            String to validate.
	 * @param colName
	 *            Database column name associated with the value being
	 *            validated.
	 * @param msg
	 *            Message to add to the errors collection if the value is
	 *            invalid.
	 * @return True on success, false otherwise.
	 */
	protected boolean validatePresenceOf(String value, String colName,
			String msg) {
		boolean validated = true;
		
		if (value == null || value.trim().length() == 0) {
			errors.put(colName, msg);
			validated = false;
		}
		
		return validated;
	}
	
	/**
	 * Validates that the specified value is not null.
	 * 
	 * @param value
	 *            Date to validate.
	 * @param colName
	 *            Database column name associated with the value being
	 *            validated.
	 * @param msg
	 *            Message to add to the errors collection if the value is
	 *            invalid.
	 * @return True on success, false otherwise.
	 */
	protected boolean validatePresenceOf(Date value, String colName,
			String msg) {
		boolean validated = true;
		
		if (value == null) {
			errors.put(colName, msg);
			validated = false;
		}
		
		return validated;
	}
	
	/**
	 * Validates that the specified value is within the numeric range
	 * restrictions supplied.
	 * 
	 * @param value
	 *            Integer to validate.
	 * @param min
	 *            Minimum range value
	 * @param max
	 *            Maximum range value
	 * @param colName
	 *            Database column name associated with the value being
	 *            validated.
	 * @param msg
	 *            Message to add to the errors collection if the value is
	 *            invalid.
	 * @return True on success, false otherwise.
	 * @throws InvalidParameterException
	 *             If min < 0, max < 0 or min > max.
	 */
	protected boolean validateRangeOf(int value, int min, int max,
			String colName, String msg) throws InvalidParameterException {
		boolean minValidated = true;
		boolean maxValidated = true;

		// Check for invalid length parameters
		if (min < Integer.MIN_VALUE || max > Integer.MAX_VALUE || min > max)
			throw new InvalidParameterException();
		
		// Check minimum length
		if (value < min) {
			minValidated = false;
		}
		
		// Check maximum length
		if (value > max) {
			maxValidated = false;
		}
			
		// Check the results and return
		if (minValidated && maxValidated) {
			return true;
		} else {
			errors.put(colName, msg);
			return false;
		}
	}
	
	/**
	 * Validates that the specified value is within the numeric range
	 * restrictions supplied.
	 * 
	 * @param value
	 *            Float to validate.
	 * @param min
	 *            Minimum range value
	 * @param max
	 *            Maximum range value
	 * @param colName
	 *            Database column name associated with the value being
	 *            validated.
	 * @param msg
	 *            Message to add to the errors collection if the value is
	 *            invalid.
	 * @return True on success, false otherwise.
	 * @throws InvalidParameterException
	 *             If min < 0, max < 0 or min > max.
	 */
	protected boolean validateRangeOf(float value, float min, float max,
			String colName, String msg) throws InvalidParameterException {
		boolean minValidated = true;
		boolean maxValidated = true;

		// Check for invalid length parameters
		if (min < Float.MIN_VALUE || max > Float.MAX_VALUE || min > max)
			throw new InvalidParameterException();
		
		// Check minimum length
		if (value < min) {
			minValidated = false;
		}
		
		// Check maximum length
		if (value > max) {
			maxValidated = false;
		}
			
		// Check the results and return
		if (minValidated && maxValidated) {
			return true;
		} else {
			errors.put(colName, msg);
			return false;
		}
	}
}
