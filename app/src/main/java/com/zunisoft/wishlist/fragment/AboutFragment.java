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
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zunisoft.common.support.AndroidUtil;
import com.zunisoft.wishlist.R;

/**
 * Application about box fragment.
 * 
 * This class displays information about the application to the user.
 * 
 * @author krdavis
 */
public class AboutFragment extends Fragment implements View.OnClickListener {
	// Tag used by logging APIs
	private static final String TAG = "AboutFragment";

    /**
     * Empty constructor
     */
    public AboutFragment() {
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
        View rootView = inflater.inflate(R.layout.about, container, false);

		// Set the application version
		TextView text = (TextView) rootView.findViewById(R.id.app_version);
		text.setText(AndroidUtil.getPackageVersion(getActivity()));
		
		// Set the "OK" button
		View okButton = rootView.findViewById(R.id.about_ok_button);
		okButton.setOnClickListener(this);

        return rootView;
	}
	
	/**
	 * Called when a view has been clicked.
	 * 
	 * @param v
	 *            The view that was clicked.
	 */
	public void onClick(View v) {
		Log.d(TAG, "onClick() -> clicked on " + v.getId());

		// Process the click event for the appropriate button
		switch (v.getId()) {
		case R.id.about_ok_button:
			this.getFragmentManager().popBackStack();
			break;
		}
	}
}
