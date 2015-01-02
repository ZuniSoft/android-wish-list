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

package com.zunisoft.wishlist;

import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.zunisoft.common.concurrent.TaskListener;
import com.zunisoft.common.db.DatabaseAdapter;
import com.zunisoft.common.support.AndroidUtil;
import com.zunisoft.wishlist.fragment.AboutFragment;
import com.zunisoft.wishlist.fragment.ExternalStorageStatusFragment;
import com.zunisoft.wishlist.fragment.ItemListFragment;

/**
 * Wish List application startup activity.
 *
 * This class is instantiated when the application is launched.
 *
 * @author krdavis
 */

public class MainActivity extends ActionBarActivity implements TaskListener {

    // Tag used by logging APIs
    private static final String TAG = "MainActivity";

    // Activity dialogs
    private ProgressDialog pdialog;

    // Member variables
    private boolean missingStorage = false;
    private boolean isTaskRunning = false;

    // Setup task
    private AsyncTask setupTask;
    private boolean setupChecked = false;

    /**
     * Called when the activity is starting.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     *                           shut down then this Bundle contains the data it most recently
     *                           supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");

        super.onCreate(savedInstanceState);

        // Set the layout
        setContentView(R.layout.activity_main);

        // Get external storage state
        String storageState = Environment.getExternalStorageState();

        // Set the appropriate layout or activity
        if (Environment.MEDIA_MOUNTED.equals(storageState)) {
            missingStorage = false;

            // Set the layout
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, new ItemListFragment())
                        .commit();
            }

            // Init database if required
            if (!setupChecked) {
                SetupTask task = new SetupTask(this);
                task.execute();
            }

        } else {
            missingStorage = true;
            startExternalStorageStatus(storageState);
        }
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into
     * the background, but has not (yet) been killed.
     */
    @Override
    protected void onPause() {
        Log.d(TAG, "onPause()");

        super.onPause();

        if (pdialog != null && pdialog.isShowing()) {
            pdialog.dismiss();
        }
    }

    /**
     * Perform any final cleanup before an activity is destroyed. This can happen either because the
     * activity is finishing (someone called finish() on it, or because the system is temporarily
     * destroying this instance of the activity to save space. You can distinguish between these two
     * scenarios with the isFinishing() method.
     */
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");

        super.onDestroy();

        // Clean up temp directory
        AndroidUtil.purgeTempDir(this.getApplicationContext());
    }

    /**
     * This is the fragment-orientated version of onResume() that you can override to perform
     * operations in the Activity at the same point where its fragments are resumed. Be sure to
     * always call through to the super-class.
     */
    @Override
    protected void onResumeFragments() {
        Log.d(TAG, "onResumeFragments()");

        super.onResumeFragments();
    }

    /**
     * Initialize the contents of the Activity's standard options menu.
     *
     * @param menu The options menu in which to place menu items.
     * @return True for the menu to be displayed; if it returns false it will
     * not be shown.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu()");

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * This hook is called whenever an item in the options menu is selected.
     *
     * @param item The menu item that was selected.
     * @return Returns false to allow normal menu processing to proceed, true to
     * consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected() -> clicked on "
                + item.getItemId());

        // Process the selection event for the appropriate menu item
        switch (item.getItemId()) {
            case R.id.about:
                startAbout();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when an sub-activity launched exits, returning the requestCode,
     * resultCode and any additional data from it. The resultCode will be
     * RESULT_CANCELED if the activity explicitly returned that, didn't return
     * any result, or crashed during its operation.
     *
     * @param requestCode
     *            The integer request code originally supplied to
     *            startActivityForResult(), identifying who this result came
     *            from.
     * @param resultCode
     *            The integer result code returned by the child activity through
     *            its setResult().
     * @param intent
     *            An Intent, which can return result data to the caller (various
     *            data can be attached to Intent "extras").
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d(TAG, "onActivityResult()");

        // This lets the fragment get a chance at processing the results
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        fragment.onActivityResult(requestCode, resultCode, intent);

        super.onActivityResult(requestCode, resultCode, intent);
    }

    /**
     * Starts the ExternalStorageStatus activity.
     *
     * @param status The status of the external storage on the device. See the
     *               {@link android.os.Environment} class.
     */
    protected void startExternalStorageStatus(String status) {
        Log.d(TAG, "startExternalStorageStatus()");

        ExternalStorageStatusFragment fragment = new ExternalStorageStatusFragment();

        Bundle bundle = new Bundle();
        bundle.putString(ExternalStorageStatusFragment.FRAGMENT_EXTRA_STATUS_KEY, status);
        fragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, fragment).commit();
    }

    /**
     * Starts the About activity.
     */
    protected void startAbout() {
        Log.d(TAG, "startAbout()");

        AboutFragment fragment = new AboutFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Called when a task starts execution.
     */
    public void onTaskStarted() {
        Log.d(TAG, "onTaskStarted()");

        isTaskRunning = true;

        pdialog = ProgressDialog.show(this,
                getString(R.string.progress_dlg_generic_title),
                getString(R.string.progress_dlg_generic_msg),
                true, false);
    }

    /**
     * Called when a task finishes execution.
     *
     * @param result
     *            Result message for task that has finished executing.
     */
    public void onTaskFinished(String result) {
        Log.d(TAG, "onTaskFinished()");

        if (pdialog != null) {
            pdialog.dismiss();
        }

        if (Integer.valueOf(result) == SetupTask.SETUP_OK)
            setupChecked = true;

        isTaskRunning = false;
    }

    // Setup task
    private class SetupTask extends AsyncTask<Void, Void, Integer> {
        public static final int SETUP_OK = 0;
        public static final int SETUP_FAIL = 1;

        private final TaskListener listener;

        public SetupTask(TaskListener listener) {
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            listener.onTaskStarted();
        }

        @Override
        protected Integer doInBackground(final Void... args) {

            Integer setupResult;

            // Get the db adapter
            DatabaseAdapter dbAdapter = DatabaseAdapter
                    .getInstance(getApplicationContext());

            setupResult = dbAdapter.getDatabase() == null ? SETUP_FAIL : SETUP_OK;

            return setupResult;
        }

        @Override
        protected void onPostExecute(final Integer result) {
            listener.onTaskFinished(String.valueOf(result));
        }
    }

}
