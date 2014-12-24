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

package com.zunisoft.common.support;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Android utilities.
 * 
 * @author krdavis
 */
public class AndroidUtil {
	// Tag used by logging APIs
	private static final String TAG = "AndroidUtil";

	/** SD card data directory */
	public static final String SD_CARD_DATA_DIR = "data";
	
	/** Temp directory */
	private static final String TEMP_DIR = "tmp";
	
	/**
	 * Gets the application's temp directory on the SD card. The directory will
	 * be created if it doesn't exist.
	 * 
	 * @param context
	 *            Application context.
	 * @return The temp directory path.
	 */
	public static String getTempDir(Context context) {
		Log.d(TAG, "getTempDir()");
		
		// Build the temp directory path
		File tempDir = new File(
				Environment.getExternalStorageDirectory()
				+ File.separator + SD_CARD_DATA_DIR
				+ File.separator + context.getPackageName()
				+ File.separator + TEMP_DIR);
		
		// If the directory doesn't exist, create it
		if (!tempDir.exists()) {
			tempDir.mkdirs();
		}
		
		return tempDir.getPath();
	}
	
	/**
	 * Purges all files from the application's temp directory.
	 * 
	 * @param context
	 *            Application context.
	 */
	public static void purgeTempDir(Context context) {
		Log.d(TAG, "purgeTempDir()");
		
		// Get the temp directory
		String tmpDir = getTempDir(context);
		File dir = new File(tmpDir);

		// Purge any files in the directory
		String[] children = dir.list();
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				String filename = children[i];
				File f = new File(tmpDir + File.separator + filename);
				if (f.exists()) {
					f.delete();
				}
			}
		}
	}
	
	/**
	 * Searches a character sequence array for a given string. This is a simple
	 * sequential search and returns the location of the first matching value.
	 * 
	 * @param strings
	 *            The character sequence array to search.
	 * @param string
	 *            The string to find in the character sequence array.
	 * @return The zero based index in the character sequence array where the
	 *         string was found or -1 if the string was not found.
	 */
	public static int stringSearch(CharSequence[] strings, String string) {
		Log.d(TAG, "stringSearch()");
		
		// Check if any NULL parameters were passed
		if (strings == null || string == null)
			return -1;
		
		// Find the string in the character sequence array
		for (int n = 0; n < strings.length; n++) {
			if (strings[n].equals(string))
				return n;
		}
		
		// The string was not found
		return -1;
	}
	
	/**
	 * Gets the current version of an activity's package.
	 * 
	 * @param activity
	 *            The activity for which the package version will be retrieved.
	 * @return The activity's package version.
	 */
	public static String getPackageVersion(Activity activity) {
		Log.d(TAG, "getPackageVersion()");
		
		String version = "?";

		try {
			PackageInfo pi = activity.getPackageManager().getPackageInfo(
					activity.getPackageName(), 0);
			version = pi.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			Log.e(TAG, "Package name not found", e);
		}

		return version;
	}
}
