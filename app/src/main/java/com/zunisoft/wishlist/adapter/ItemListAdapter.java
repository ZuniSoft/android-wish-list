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

package com.zunisoft.wishlist.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.zunisoft.common.db.DatabaseAdapter;
import com.zunisoft.wishlist.R;
import com.zunisoft.wishlist.model.Item;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A ListAdapter that manages a ListView backed by a cursor of items.
 *
 * @see com.zunisoft.wishlist.model.Item
 *
 * @author krdavis
 */
public class ItemListAdapter extends SimpleCursorAdapter {
    // Tag used by logging APIs
    private static final String TAG = "ItemListAdapter";

    // Member variables
    private Cursor cursor;
    private Context context;
    private DatabaseAdapter dba;
    private Item item;
    private DateFormat dateStorageFormat;
    private DateFormat dateDisplayFormat;

    /**
     * Constructs a new <tt>ItemListAdapter</tt>.
     *
     * @param context
     *            The context where the ListView associated with this
     *            SimpleListItemFactory is running.
     * @param layout
     *            Resource identifier of a layout file that defines the views
     *            for this list item. The layout file should include at least
     *            those named views defined in "to".
     * @param c
     *            The database cursor. Can be null if the cursor is not
     *            available yet.
     * @param from
     *            A list of column names representing the data to bind to the
     *            UI. Can be null if the cursor is not available yet.
     * @param to
     *            The views that should display column in the "from" parameter.
     *            These should all be TextViews. The first N views in this list
     *            are given the values of the first N columns in the from
     *            parameter. Can be null if the cursor is not available yet.
     */
    public ItemListAdapter(Context context, int layout, Cursor c, String[] from,
                          int[] to) {
        super(context, layout, c, from, to);

        Log.d(TAG, "ItemListAdapter()");

        this.cursor = c;
        this.context = context;

        this.dba = DatabaseAdapter.getInstance(context);
        this.item = new Item(this.context.getResources());
        this.item.setSQLiteDatabase(dba.getDatabase());

        // Set the date format objects
        String datePattern = context.getResources().getString(
                R.string.date_storage_format);
        dateStorageFormat = new SimpleDateFormat(datePattern);

        datePattern = context.getResources().getString(
                R.string.date_display_format);
        dateDisplayFormat = new SimpleDateFormat(datePattern);
    }

    /**
     * Binds all of the field names passed into the "to" parameter of the
     * constructor with their corresponding cursor columns as specified in the
     * "from" parameter. If no appropriate binding can be found, an
     * IllegalStateException is thrown.
     *
     * @param view
     *            Existing view, returned earlier by newView.
     * @param context
     *            Interface to application's global information.
     * @param cursor
     *            The cursor from which to get the data. The cursor is already
     *            moved to the correct position.
     * @throws IllegalStateException
     *             If binding cannot occur.
     */
    public void bindView(View view, Context context, Cursor cursor) {
        Log.d(TAG, "bindView()");

        // Set the thumbnail photo
        ImageView imageView = (ImageView) view
                .findViewById(R.id.item_list_row_thumbnail_photo);

        byte[] blob = cursor.getBlob(this.cursor
                .getColumnIndex(Item.COL_PHOTO_THUMBNAIL));

        if (blob != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(blob, 0, blob.length);
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(R.drawable.ic_camera);
        }

        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        // Decsription
        TextView textView = (TextView) view
                .findViewById(R.id.item_list_row_description);

        String val = cursor.getString(this.cursor
                .getColumnIndex(Item.COL_DESCRIPTION));

        textView.setText(val);

        // Location
        textView = (TextView) view
                .findViewById(R.id.item_list_row_location);

        val = cursor.getString(this.cursor
                .getColumnIndex(Item.COL_LOCATION));

        textView.setText(val);

        // Category
        textView = (TextView) view
                .findViewById(R.id.item_list_row_category);

        val = cursor.getString(this.cursor
                .getColumnIndex(Item.COL_CATEGORY));

        textView.setText(val);

        // Date of capture
        textView = (TextView) view
                .findViewById(R.id.item_list_row_date);

        try {
            Date dob = dateStorageFormat.parse(cursor.getString(this.cursor
                    .getColumnIndex(Item.COL_DATE_CAPTURED)));
            textView.setText(dateDisplayFormat.format(dob));
        } catch (ParseException e) {
            Log.e(TAG, "Parsing date of capture failed", e);
        }

        // Desirability
        RatingBar rb = (RatingBar) view
                .findViewById(R.id.item_list_row_desirability);

        int rate = cursor.getInt(this.cursor
                .getColumnIndex(Item.COL_DESIRABILITY));

        rb.setRating((float)rate);

        // Purchased image
        if ((cursor.getString(this.cursor.getColumnIndex(Item.COL_DATE_PURCHASED)) != null)) {
            ImageView purchasedView = (ImageView) view
                    .findViewById(R.id.item_list_row_purchased);

            purchasedView.setBackgroundResource(R.drawable.ic_purchased);
            purchasedView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }

        super.bindView(view, context, cursor);
    }

}

