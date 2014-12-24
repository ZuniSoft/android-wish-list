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

/**
 * Signals that a specific record was not found in the database.
 * 
 * @author krdavis
 */
public class RecordNotFoundException extends Exception {
	// Serial version UID
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new <tt>RecordNotFoundException</tt> with the current stack
	 * trace.
	 */
	public RecordNotFoundException() {
		super();
	}

	/**
	 * Constructs a new <tt>RecordNotFoundException</tt> with the current stack
	 * trace and the specified detail message.
	 * 
	 * @param detailMessage
	 *            The detail message for this exception.
	 */
	public RecordNotFoundException(String detailMessage) {
		super(detailMessage);
	}
}
