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

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Dialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.EditText;

import com.zunisoft.wishlist.R;

/**
 * Date picker dialog fragment.
 *
 * @author krdavis
 */
public class DatePickerDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    // Tag used by logging APIs
    private static final String TAG = "DatePickerDialogFragment";

    /**
     * Override to build your own custom Dialog container. This is typically used to show an
     * AlertDialog instead of a generic Dialog; when doing so, onCreateView(LayoutInflater,
     * ViewGroup, Bundle) does not need to be implemented since the AlertDialog takes care of
     * its own content.
     *
     * @param savedInstanceState
     *          The last saved instance state of the Fragment, or null if this is a freshly
     *          created Fragment.
     *
     * @return Return a new Dialog instance to be displayed by the Fragment.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog()");

        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    /**
     * The callback used to indicate the user is done filling in the date.
     *
     * @param view
     *          The view associated with this listener.
     * @param year
     *         The year that was set.
     * @param month
     *          The month that was set (0-11) for compatibility with Calendar.
     * @param day
     *          The day of the month that was set.
     */
    public void onDateSet(DatePicker view, int year, int month, int day) {
        Log.d(TAG, "onDateSet()");

        Calendar c = Calendar.getInstance();
        c.set(year, month, day);

        SimpleDateFormat sdf = new SimpleDateFormat(getString(
                R.string.date_display_format));
        String formattedDate = sdf.format(c.getTime());

        EditText txtDate = (EditText) getActivity().getWindow().getDecorView()
                .getRootView().findViewById(R.id.item_edit_date_purchased);
        txtDate.setText(formattedDate);
    }
}