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

package com.zunisoft.wishlist.model;

import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.zunisoft.common.db.ActiveRecord;
import com.zunisoft.common.db.RecordNotFoundException;
import com.zunisoft.wishlist.R;

import org.apache.http.impl.cookie.DateUtils;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

/**
 * Item model.
 *
 * This class represents basic wish list item.
 *
 * @author krdavis
 */
public class Item extends ActiveRecord {
    // Tag used by logging APIs
    private static final String TAG = "Item";

    // Database table columns
    public static final String COL_ROW_ID = "_id";
    public static final String COL_PHOTO_THUMBNAIL = "photo_thumbnail";
    public static final String COL_PHOTO_LARGE = "photo_large";
    public static final String COL_LOCATION = "location";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_DATE_CAPTURED = "date_captured";
    public static final String COL_DATE_PURCHASED = "date_purchased";
    public static final String COL_CATEGORY = "category";
    public static final String COL_DESIRABILITY = "desirability";
    public static final String COL_BARCODE = "barcode";
    public static final String COL_NOTES = "notes";

    /** Database table */
    public static final String SQL_TABLE_NAME = "item";

    /** Database table DDL create statement */
    public static final String SQL_CREATE_TABLE = "CREATE TABLE "
            + SQL_TABLE_NAME + " ("
            + COL_ROW_ID + " integer primary key autoincrement, "
            + COL_PHOTO_THUMBNAIL + " blob null, "
            + COL_PHOTO_LARGE + " blob null, "
            + COL_LOCATION + " text not null, "
            + COL_DESCRIPTION + " text not null, "
            + COL_DATE_CAPTURED + " text not null, "
            + COL_DATE_PURCHASED + " text null, "
            + COL_CATEGORY + " text not null, "
            + COL_DESIRABILITY + " integer not null default 0, "
            + COL_BARCODE + " text null, "
            + COL_NOTES + " text null"
            + ");";

    /** Default select list */
    public static final String[] DEFAULT_SELECT_LIST = { COL_ROW_ID,
            COL_PHOTO_THUMBNAIL, COL_PHOTO_LARGE, COL_LOCATION, COL_DESCRIPTION,
            COL_DATE_CAPTURED, COL_DATE_PURCHASED, COL_CATEGORY, COL_DESIRABILITY,
            COL_BARCODE, COL_NOTES };

    /** Minimum select list */
    public static final String[] MIN_SELECT_LIST = { COL_ROW_ID,
            COL_PHOTO_THUMBNAIL, COL_DESCRIPTION, COL_LOCATION, COL_DATE_CAPTURED,
            COL_DATE_PURCHASED, COL_CATEGORY, COL_DESIRABILITY};

    /** Default sort order */
    public static final String DEFAULT_SORT_ORDER = COL_DESCRIPTION + " ASC";

    // Data members
    private int id;
    private Bitmap  thumbnailPhoto;
    private Bitmap  largePhoto;
    private String  location;
    private String  description;
    private Date    dateCaptured;
    private Date    datePurchased;
    private String  category;
    private int     desirability;
    private String  barcode;
    private String  notes;

    // Member variables
    private String dateDisplayPattern;
    private DateFormat dateDisplayFormat;
    private String dateStoragePattern;
    private DateFormat dateStorageFormat;


    /**
     * Constructs a new <tt>Item</tt> object.
     *
     * @param res
     *            Application resources object.
     */
    public Item(Resources res) {
        super(res);
        Log.d(TAG, "Item()");

        // Set the date format objects
        dateDisplayPattern = res.getString(R.string.date_display_format);
        dateDisplayFormat = new SimpleDateFormat(dateDisplayPattern);

        dateStoragePattern = res.getString(R.string.date_storage_format);
        dateStorageFormat = new SimpleDateFormat(dateStoragePattern);
    }

    /**
     * Retrieves all records from the database associated with the model.
     *
     * @return Result set of all records associated with the model.
     */
    @Override
    public Cursor findAll() {
        Log.d(TAG, "findAll()");

        return db.query(SQL_TABLE_NAME, DEFAULT_SELECT_LIST, null, null, null,
                null, DEFAULT_SORT_ORDER);
    }

    /**
     * Finds all records matching data in any item list view fields.
     *
     * @param searchText
     *            The item text to search on.
     * @param minimumFields
     *            If true, returns a list with a reduced number of fields to
     *            help conserve memory.
     *
     * @return Result set of the records that were found to match the specified
     *         criteria.
     */
    public Cursor findAllByListFields(String searchText, boolean minimumFields) {
        Log.d(TAG, "findAllByListFields()");

        String selection;
        String fields[];

        if (minimumFields)
            fields = MIN_SELECT_LIST;
        else
            fields = DEFAULT_SELECT_LIST;

        if (searchText == null || searchText.trim().length() == 0) {
            // No filter
            selection = null;
        } else {
            // Escape the search string
            searchText = searchText.trim().replace(SINGLE_QUOTE, ESC_SINGLE_QUOTE);

            // Build selection expression
            String likeExpr = " LIKE '" + searchText + "%'";
            String dateExpr = "";

            // Check the search string for dates
            String[] tokens = searchText.split(" ");
            ArrayList<Date> dates = new ArrayList<Date>();

            // Get any valid dates
            for(int i = 0; i < tokens.length; i++) {
                try {
                    Date date = dateDisplayFormat.parse(tokens[i]);
                    dates.add(date);
                } catch (ParseException e) {
                    Log.d(TAG, "Error parsing date!");
                }
            }

            // Format any dates for storage search
            Iterator<Date> it = dates.iterator();
            while(it.hasNext())
            {
                dateExpr = dateExpr + COL_DATE_CAPTURED + " = '" + dateStorageFormat.format(it.next()) + "'";

                if (it.hasNext())
                    dateExpr = dateExpr + " OR ";
            }

            // Build the WHERE clause
            selection = COL_DESCRIPTION + likeExpr
                    + " OR " + COL_CATEGORY + likeExpr
                    + " OR " + COL_LOCATION + likeExpr
                    + " OR " + COL_DESIRABILITY + likeExpr;

            if (!dateExpr.isEmpty())
                selection = selection + " OR " + dateExpr;

            Log.d(TAG, "Search filter = " + selection);
        }

        // Perform the search
        return db.query(SQL_TABLE_NAME, fields, selection, null,
                null, null, DEFAULT_SORT_ORDER);
    }

    /**
     * Loads a record with the specified ID from the database into the model.
     *
     * @param id
     *            The ID of the record to load.
     * @throws RecordNotFoundException
     *             If the record cannot be found.
     */
    @Override
    public void load(int id) throws RecordNotFoundException {
        Log.d(TAG, "load()");

        Cursor cursor = db.query(true, SQL_TABLE_NAME, DEFAULT_SELECT_LIST,
                COL_ROW_ID + "=" + id, null, null, null, null, null);

        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                // Set the fields
                setId(cursor.getInt(cursor.getColumnIndex(COL_ROW_ID)));
                setDescription(cursor.getString(cursor.getColumnIndex(COL_DESCRIPTION)));
                setLocation(cursor.getString(cursor.getColumnIndex(COL_LOCATION)));

                // Set the date of capture
                try {
                    setCaptureDate(dateStorageFormat.parse(cursor.getString(cursor
                            .getColumnIndex(COL_DATE_CAPTURED))));
                } catch (ParseException e) {
                    Log.e(TAG, "Parsing date of capture failed", e);
                }

                // Set the date of purchase
                try {
                    if (cursor.getString(cursor
                            .getColumnIndex(COL_DATE_PURCHASED)) != null)
                        setPurchasedDate(dateStorageFormat.parse(cursor.getString(cursor
                            .getColumnIndex(COL_DATE_PURCHASED))));
                } catch (ParseException e) {
                    Log.e(TAG, "Parsing date of purchase failed", e);
                }

                setCategory(cursor.getString(cursor.getColumnIndex(COL_CATEGORY)));
                setDesirability(cursor.getInt(cursor.getColumnIndex(COL_DESIRABILITY)));
                setBarcode(cursor.getString(cursor.getColumnIndex(COL_BARCODE)));
                setNotes(cursor.getString(cursor.getColumnIndex(COL_NOTES)));

                // Set the thumbnail photo
                byte[] blob = cursor.getBlob(cursor
                        .getColumnIndex(COL_PHOTO_THUMBNAIL));
                if (blob != null)
                    setThumbnailPhoto(BitmapFactory.decodeByteArray(blob, 0,
                            blob.length));

                // Set the large photo
                blob = cursor.getBlob(cursor.getColumnIndex(COL_PHOTO_LARGE));
                if (blob != null)
                    setLargePhoto(BitmapFactory
                            .decodeByteArray(blob, 0, blob.length));
            } else {
                throw new RecordNotFoundException();
            }
        } finally {
            cursor.close();
        }
    }

    /**
     * Reloads the model from the database.
     *
     * @throws RecordNotFoundException
     *             If the record cannot be found.
     */
    @Override
    public void reload() throws RecordNotFoundException {
        Log.d(TAG, "reload()");

        if (id != 0) {
            try {
                load(id);
            } catch (RecordNotFoundException e) {
                id = 0;
                throw e;
            }
        }
    }

    /**
     * Saves the model to the database.
     *
     * @return The row ID of newly inserted row, the number of rows affected by
     *         an update or -1 if an error occurred.
     */
    @Override
    public long save() {
        Log.d(TAG, "save()");

        long retval;

        if (validate()) {
            ContentValues values = new ContentValues();

            // Set the fields
            values.put(COL_DESCRIPTION, description);
            values.put(COL_LOCATION, location);
            values.put(COL_DATE_CAPTURED, DateUtils.formatDate(dateCaptured, dateStoragePattern));

            if (datePurchased != null)
                values.put(COL_DATE_PURCHASED, DateUtils.formatDate(datePurchased, dateStoragePattern));

            values.put(COL_CATEGORY, category);
            values.put(COL_DESIRABILITY, desirability);
            values.put(COL_BARCODE, barcode);
            values.put(COL_NOTES, notes);

            // Set the thumbnail photo
            if (thumbnailPhoto != null) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                thumbnailPhoto.compress(Bitmap.CompressFormat.PNG, 100, out);
                values.put(COL_PHOTO_THUMBNAIL, out.toByteArray());
            } else {
                byte[] out = null;
                values.put(COL_PHOTO_THUMBNAIL, out);
            }

            // Set the large photo
            if (largePhoto != null) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                largePhoto.compress(Bitmap.CompressFormat.PNG, 100, out);
                values.put(COL_PHOTO_LARGE, out.toByteArray());
            } else {
                byte[] out = null;
                values.put(COL_PHOTO_LARGE, out);
            }

            if (id <= 0) {
                retval = db.insert(SQL_TABLE_NAME, null, values);

                if (retval != -1) id = (int) retval;
            } else {
                retval = db.update(SQL_TABLE_NAME, values, COL_ROW_ID + "="
                        + id, null);
            }
        } else {
            retval = -1;
        }
        return retval;
    }

    /**
     * Deletes the model from the database.
     *
     * @return True on success, false otherwise.
     */
    @Override
    public boolean delete() {
        Log.d(TAG, "delete()");

        return db.delete(SQL_TABLE_NAME, COL_ROW_ID + "=" + id,
                null) > 0;
    }

    /**
     * Performs validation checks on the model.
     *
     * Any validation errors can be retrieved by calling getErrors().
     *
     * @return True on success, false otherwise.
     */
    @Override
    public boolean validate() {
        Log.d(TAG, "validate()");

        super.validate();

        boolean validated = true;

        // Description
        if (!validatePresenceOf(description, COL_DESCRIPTION, res
                .getString(R.string.validation_required_field)))
            validated = false;

        // Location
        if (!validatePresenceOf(location, COL_LOCATION, res
                .getString(R.string.validation_required_field)))
            validated = false;

        // Capture Date
        if (!validatePresenceOf(dateCaptured, COL_DATE_CAPTURED, res
                .getString(R.string.validation_required_field)))
            validated = false;

        // Category
        if (!validatePresenceOf(category, COL_CATEGORY, res
                .getString(R.string.validation_required_field)))
            validated = false;

        // Desirability
        if (!validateRangeOf(desirability, 0, 5, COL_DESIRABILITY, res
                .getString(R.string.validation_value_out_of_range)))
            validated = false;

        return validated;
    }

    /**
     * Gets the item's ID.
     *
     * @return The item's ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the item's ID.
     *
     * @param id
     *            The item's ID.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the item's description.
     *
     * @return The item's description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the item's description.
     *
     * @param description
     *            The item's description.
     */
    public void setDescription(String description) {
        this.description = description.trim();
    }

    /**
     * Gets the item's location.
     *
     * @return The item's location.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the item's location.
     *
     * @param location
     *            The item's location.
     */
    public void setLocation(String location) {
        this.location = location.trim();
    }

    /**
     * Gets the item's capture date.
     *
     * @return The item's capture date.
     */
    public Date getCaptureDate() {
        return dateCaptured;
    }

    /**
     * Sets the item's capture date.
     *
     * @param dateCaptured
     *            The item's capture date.
     */
    public void setCaptureDate(Date dateCaptured) {
        this.dateCaptured = dateCaptured;
    }

    /**
     * Gets the item's purchase date.
     *
     * @return The item's purchase date.
     */
    public Date getPurchasedDate() {
        return datePurchased;
    }

    /**
     * Sets the item's purchase date.
     *
     * @param datePurchased
     *            The item's purchase date.
     */
    public void setPurchasedDate(Date datePurchased) {
        this.datePurchased = datePurchased;
    }

    /**
     * Gets the item's category.
     *
     * @return The item's category.
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the item's category.
     *
     * @param category
     *            The item's category.
     */
    public void setCategory(String category) {
        this.category = category.trim();
    }

    /**
     * Gets the item's desirability.
     *
     * @return The item's desirability.
     */
    public int getDesirability() {
        return desirability;
    }

    /**
     * Sets the item's desirability.
     *
     * @param desirability
     *            The item's desirability.
     */
    public void setDesirability(int desirability) {
        this.desirability = desirability;
    }

    /**
     * Gets the item's barcode.
     *
     * @return The item's barcode.
     */
    public String getBarcode() {
        return barcode;
    }

    /**
     * Sets the item's barcode.
     *
     * @param barcode
     *            The item's barcode.
     */
    public void setBarcode(String barcode) {
        this.barcode = barcode.trim();
    }

    /**
     * Gets the item's notes.
     *
     * @return The item's notes.
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Sets the item's notes.
     *
     * @param notes
     *            The item's notes.
     */
    public void setNotes(String notes) {
        this.notes = notes.trim();
    }

    /**
     * Gets the item's thumbnail photo.
     *
     * @return The item's thumbnail photo.
     */
    public Bitmap getThumbnailPhoto() {
        return thumbnailPhoto;
    }

    /**
     * Sets the item's thumbnail photo.
     *
     * @param thumbnailPhoto
     *            The item's thumbnail photo.
     */
    public void setThumbnailPhoto(Bitmap thumbnailPhoto) {
        this.thumbnailPhoto = thumbnailPhoto;
    }

    /**
     * Gets the item's large photo.
     *
     * @return The item's photo.
     */
    public Bitmap getLargePhoto() {
        return largePhoto;
    }

    /**
     * Sets the item's large photo.
     *
     * @param photo
     *            The item's photo.
     */
    public void setLargePhoto(Bitmap photo) {
        this.largePhoto = photo;
    }

}

