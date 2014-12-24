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

package com.zunisoft.wishlist.fragment;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zunisoft.wishlist.R;

/**
 * External storage status fragment.
 *
 * This fragment displays messages regarding the status of the device's external
 * storage. When the "OK" button is pressed, the application is terminated.
 *
 * @author krdavis
 */
public class ExternalStorageStatusFragment extends Fragment implements View.OnClickListener {
    // Tag used by logging APIs
    private static final String TAG = "ExternalStorageStatusFragment";

    /**
     * Intent extra data key - Status key (string).
     */
    public static final String FRAGMENT_EXTRA_STATUS_KEY = "STATUS_KEY";

    /**
     * Empty constructor
     */
    public ExternalStorageStatusFragment() {
    }

    /**
     * Called to have the fragment instantiate its user interface view. This is optional, and
     * non-graphical fragments can return null (which is the default implementation). This will be
     * called between onCreate(Bundle) and onActivityCreated(Bundle).
     *
     * If you return a View from here, you will later be called in onDestroyView() when the view is
     * being released.
     *
     * @param inflater
     *          The LayoutInflater object that can be used to inflate any views in the fragment,
     * @param container
     *          If non-null, this is the parent view that the fragment's UI should be attached to.
     *          The fragment should not add the view itself, but this can be used to generate the
     *          LayoutParams of the view.
     * @param savedInstanceState
     *          If non-null, this fragment is being re-constructed from a previous saved state as
     *          given here.
     *
     * @return The View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        
        // Get the root view
        View rootView = inflater.inflate(R.layout.external_storage_status, container, false);

        initComponents(rootView);

        return rootView;
    }

    /**
     * Initializes the view's components.
     *
     * @param rootView
     *            The root view of this fragment.
     */
    protected void initComponents(View rootView) {
        Log.d(TAG, "initComponents()");

        // Get the storage status
        Bundle bundle = this.getArguments();
        String status = bundle.getString(FRAGMENT_EXTRA_STATUS_KEY, "FRAGMENT_EXTRA_STATUS_KEY");

        if (status == null) getActivity().getSupportFragmentManager().popBackStack();

        // Set message text
        TextView tvMsg = (TextView) rootView.findViewById(R.id.external_storage_status_msg);
        TextView tvDetail = (TextView) rootView.findViewById(R.id.external_storage_status_detail_msg);

        if (Environment.MEDIA_UNMOUNTED.equals(status)) {
            tvMsg.setText(getResources().getString(
                    R.string.external_storage_status_unmounted_msg));
            tvDetail.setText(getResources().getString(
                    R.string.external_storage_status_unmounted_detail_msg));
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(status)) {
            tvMsg.setText(getResources().getString(
                    R.string.external_storage_status_read_only_msg));
            tvDetail.setText(getResources().getString(
                    R.string.external_storage_status_read_only_detail_msg));
        } else if (Environment.MEDIA_SHARED.equals(status)) {
            tvMsg.setText(getResources().getString(
                    R.string.external_storage_status_shared_msg));
            tvDetail.setText(getResources().getString(
                    R.string.external_storage_status_shared_detail_msg));
        } else {
            tvMsg.setText(getResources().getString(
                    R.string.external_storage_status_default_msg));
            tvDetail.setText(getResources().getString(
                    R.string.external_storage_status_default_detail_msg));
        }

        // Set the "OK" button
        View okButton = rootView.findViewById(R.id.external_storage_status_ok_button);
        okButton.setOnClickListener(this);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v
     *        The view that was clicked.
     */
    public void onClick(View v) {
        Log.d(TAG, "onClick() -> clicked on " + v.getId());

        // Process the click event for the appropriate button
        switch (v.getId()) {
            case R.id.external_storage_status_ok_button:
                android.os.Process.killProcess(android.os.Process.myPid());
                break;
        }
    }
}
