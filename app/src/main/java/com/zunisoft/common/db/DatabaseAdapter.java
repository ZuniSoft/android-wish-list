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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.zunisoft.common.support.AndroidUtil;
import com.zunisoft.wishlist.model.Item;

import java.io.File;

/**
 * Database adapter.
 * 
 * This class manages the creation and versioning of the application's SQLite
 * database.
 * 
 * @author krdavis
 */
public class DatabaseAdapter extends SQLiteOpenHelper {
	// Tag used by logging APIs
	private static final String TAG = "DatabaseAdapter";
	
	// Database adapter instance
	private static DatabaseAdapter dbAdapter;

	// Database object
	private static SQLiteDatabase db;

	// Database paths and filename
	private static final String DATABASE_DIR = "databases";
	private static final String DATABASE_NAME = "wishlist.db";

	// Database version
	private static final int DATABASE_VERSION = 1;

	// Member variables
	private boolean isInitializing = false;
	private String dbFilePath;
	
	/**
	 * Constructs a new <tt>DatabaseAdapter</tt> object.
	 * 
	 * @param context
	 *            Application context.
	 * @param name
	 *            Database name.
	 * @param factory
	 *            Cursor factory.
	 * @param version
	 *            Database version.
	 */
	private DatabaseAdapter(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		
		Log.d(TAG, "DatabaseAdapter()");
		
		// Build the database directory and file paths
		File dbDir = new File(
				Environment.getExternalStorageDirectory()
				+ File.separator + AndroidUtil.SD_CARD_DATA_DIR
				+ File.separator + context.getPackageName()
				+ File.separator + DATABASE_DIR);
		File dbFile = new File(dbDir + File.separator + DATABASE_NAME);

		// Save the path to the database file
		dbFilePath = dbFile.getPath();

		// If the directory doesn't exist, create it
		if (!dbDir.exists()) {
			dbDir.mkdirs();
		}
	}

	/**
	 * Called when the database is created for the first time. 
	 * 
	 * @param db
	 *            The database.
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "onCreate()");

		db.execSQL(Item.SQL_CREATE_TABLE);
	}

	/**
	 * Create and/or open a database. This will be the same object returned by
	 * getWritableDatabase() unless some problem, such as a full disk, requires
	 * the database to be opened read-only. In that case, a read-only database
	 * object will be returned. If the problem is fixed, a future call to
	 * getWritableDatabase() may succeed, in which case the read-only database
	 * object will be closed and the read/write object will be returned in the
	 * future.
	 * 
	 * @return A database object valid until getWritableDatabase() or close() is
	 *         called.
	 * @throws SQLiteException
	 *             If the database cannot be opened.
	 * @throws IllegalStateException
	 *             If an attempt is made to open the database while it is
	 *             initializing.
	 */
	@Override
	public synchronized SQLiteDatabase getReadableDatabase() {
		Log.d(TAG, "getReadableDatabase()");
		
		// Check if the database is already open
		if (db != null && db.isOpen()) {
			return db;
		}

		// Check initialization status
		if (isInitializing) {
			throw new IllegalStateException(
					"getReadableDatabase() called recursively");
		}

		// Try to open database read/write
		try {
			return getWritableDatabase();
		} catch (SQLiteException e) {
			Log.e(TAG, "Couldn't open " + DATABASE_NAME
					+ " for writing (will try read-only):", e);
		}

		// Try to open database read-only
		SQLiteDatabase tdb = null;
		try {
			isInitializing = true;
			tdb = SQLiteDatabase.openDatabase(dbFilePath, null,
					SQLiteDatabase.OPEN_READONLY);
			
			if (tdb.getVersion() != DATABASE_VERSION) {
				throw new SQLiteException(
						"Can't upgrade read-only database from version "
								+ tdb.getVersion() + " to " + DATABASE_VERSION
								+ ": " + dbFilePath);
			}

			onOpen(tdb);
			Log.w(TAG, "Opened " + DATABASE_NAME + " in read-only mode");
			
			db = tdb;
			return db;
		} finally {
			isInitializing = false;
			if (tdb != null && tdb != db)
				tdb.close();
		}
	}

	/**
	 * Create and/or open a database that will be used for reading and writing.
	 * Once opened successfully, the database is cached, so you can call this
	 * method every time you need to write to the database. Make sure to call
	 * close() when you no longer need it. Errors such as bad permissions or a
	 * full disk may cause this operation to fail, but future attempts may
	 * succeed if the problem is fixed.
	 * 
	 * @return A database object valid until close() is called.
	 * @throws SQLiteException
	 *             If the database cannot be opened for writing.
	 * @throws IllegalStateException
	 *             If an attempt is made to open the database while it is
	 *             initializing.
	 */
	@Override
	public synchronized SQLiteDatabase getWritableDatabase() {
		Log.d(TAG, "getWritableDatabase()");
		
		// Check if the database is already open
		if (db != null && db.isOpen() && !db.isReadOnly()) {
			return db;
		}

		// Check initialization status
		if (isInitializing) {
			throw new IllegalStateException(
					"getWritableDatabase() called recursively");
		}

		// Open the database
		boolean success = false;
		SQLiteDatabase tdb = null;
		try {
			isInitializing = true;
			tdb = SQLiteDatabase.openOrCreateDatabase(dbFilePath, null);
			
			int version = tdb.getVersion();
			Log.d(TAG, "Database version = " + version);
			
			if (version != DATABASE_VERSION) {
				tdb.beginTransaction();
				try {
					if (version == 0) {
						onCreate(tdb);
					} else {
						onUpgrade(tdb, version, DATABASE_VERSION);
					}
					tdb.setVersion(DATABASE_VERSION);
					tdb.setTransactionSuccessful();
				} finally {
					tdb.endTransaction();
				}
			}

			onOpen(tdb);
			success = true;
			return tdb;
		} finally {
			isInitializing = false;
			if (success) {
				if (db != null) {
					try {
						db.close();
					} catch (Exception e) {
						Log.w(TAG, "Error closing the database.");
					}
				}
				db = tdb;
			} else {
				if (tdb != null)
					tdb.close();
			}
		}
	}

	/**
	 * Called when the database needs to be upgraded.
	 * 
	 * @param db
	 *            The database.
	 * @param oldVersion
	 *            The old database version.
	 * @param newVersion
	 *            The new database version.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i(TAG, "Upgrading database from version " + oldVersion + " to "
				+ newVersion);
		
		// Apply upgrades
		if (oldVersion == 1 && newVersion == 2) {
			// Version 1 to 2 introduced some new feature
			//String messageAlterSql = "alter table test add column new_column NULL";
			//db.execSQL(messageAlterSql);
		}
	}

	/**
	 * Initializes the database adapter.
	 * 
	 * @param context
	 *            Application context.
	 */
	private static void initialize(Context context) {
		Log.d(TAG, "initialize()");
		
		if (dbAdapter == null) {
			dbAdapter = new DatabaseAdapter(context, DATABASE_NAME, null,
					DATABASE_VERSION);
			db = dbAdapter.getWritableDatabase();
		}
	}

	/**
	 * Gets an instance of the database adapter.
	 * 
	 * @param context
	 *            Application context.
	 * @return Database adapter instance.
	 */
	public static final DatabaseAdapter getInstance(Context context) {
		Log.d(TAG, "getInstance()");
		
		initialize(context);
		return dbAdapter;
	}

	/**
	 * Gets the database object.
	 * 
	 * @return Database object.
	 */
	public SQLiteDatabase getDatabase() {
		Log.d(TAG, "getDatabase()");
		
		return db;
	}

	/**
	 * Closes the database.
	 */
	@Override
	public synchronized void close(){
		Log.d(TAG, "close()");
		
		if (dbAdapter != null) {
			db.close();
			dbAdapter = null;
		}
	}
}