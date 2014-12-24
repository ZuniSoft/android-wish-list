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

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.zunisoft.common.concurrent.TaskListener;
import com.zunisoft.common.db.DatabaseAdapter;
import com.zunisoft.wishlist.R;
import com.zunisoft.wishlist.adapter.ItemListAdapter;
import com.zunisoft.wishlist.model.Item;

/**
 * Application item list fragment.
 *
 * This class displays item list to the user.
 *
 * @author krdavis
 */

public class ItemListFragment extends Fragment implements TaskListener {
    // Tag used by logging APIs
    private static final String TAG = "ItemListFragment";

    ListView listView;
    private boolean isTaskRunning = false;
    private ProgressDialog pdialog;
    private Cursor cursor;
    private ItemListAdapter adapter;

    /**
     * Empty constructor
     */
    public ItemListFragment() {
    }

    /**
     * Called when the fragment is starting.
     *
     * @param savedInstanceState
     *            If the fragment is being re-initialized after previously being
     *            shut down then this Bundle contains the data it most recently
     *            supplied in onSaveInstanceState(Bundle).
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");

        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    /**
     * Called when the fragment's activity has been created and this fragment's view hierarchy
     * instantiated. It can be used to do final initialization once these pieces are in place, such
     * as retrieving views or restoring state. It is also useful for fragments that use
     * setRetainInstance(boolean) to retain their instance, as this callback tells the fragment when
     * it is fully associated with the new activity instance. This is called after
     * onCreateView(LayoutInflater, ViewGroup, Bundle) and before onViewStateRestored(Bundle).
     *
     * @param savedInstanceState
     *          If the fragment is being re-created from a previous saved state, this is the state.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated()");

        super.onActivityCreated(savedInstanceState);
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
        View rootView = inflater.inflate(R.layout.item_list, container, false);

        ItemListTask task = new ItemListTask(this);
        task.execute(cursor);

        return rootView;
    }

    /**
     * Called when a fragment is first attached to its activity. onCreate(Bundle) will be called after this.
     *
     * @param activity
     *          The activity the fragment is being attached.
     */
    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "onAttach()");

        super.onAttach(activity);
    }

    /**
     * Called when the fragment is no longer attached to its activity.
     */
    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach()");

        super.onDetach();

        if (pdialog != null && pdialog.isShowing()) {
            pdialog.dismiss();
        }
    }

    /**
     * Initialize the contents of the fragment's standard options menu.
     *
     * @param menu The options menu in which to place menu items.
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(TAG, "onCreateOptionsMenu");

        inflater.inflate(R.menu.menu_item_list, menu);
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
            case R.id.action_add_new:
                ItemEditFragment fragment = new ItemEditFragment();

                Bundle bundle = new Bundle();
                bundle.putLong(ItemEditFragment.ITEM_ID_KEY, 0);
                fragment.setArguments(bundle);

                getFragmentManager().beginTransaction()
                        .replace(R.id.container, fragment)
                        .addToBackStack(null)
                        .commit();
                return true;
        }


        return true;
    }

    /**
     * Controls the display of the empty list message.
     *
     * @param state
     *            Empty list message state. True to display the message, false
     *            to hide it.
     */
    protected void showEmptyListMsg(boolean state) {
        Log.d(TAG, "showEmptyListMsg()");

        // Add visible code for search box
        getActivity().findViewById(R.id.item_list_view).setVisibility(
                state == true ? View.GONE : View.VISIBLE);
        getActivity().findViewById(R.id.item_list_empty_view).setVisibility(
                state == true ? View.VISIBLE : View.GONE);
    }

    /**
     * Called when a task starts execution.
     */
    public void onTaskStarted() {
        Log.d(TAG, "onTaskStarted()");

        isTaskRunning = true;
        pdialog = ProgressDialog.show(getActivity(),
                getActivity().getString(R.string.progress_dlg_generic_title),
                getActivity().getString(R.string.progress_dlg_generic_msg),
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
        isTaskRunning = false;
    }

    // Item list task
    private class ItemListTask extends AsyncTask<Cursor, Void, Cursor> {
        private final TaskListener listener;

        public ItemListTask(TaskListener listener) {
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            listener.onTaskStarted();
        }

        @Override
        protected Cursor doInBackground(final Cursor... args) {

            DatabaseAdapter db;
            Item item;

            db = DatabaseAdapter.getInstance(getActivity().getApplicationContext());

            // Create a new item object
            item = new Item(getActivity().getApplicationContext().getResources());
            item.setSQLiteDatabase(db.getDatabase());

            cursor = item.findAll();

            return cursor;
        }

        @Override
        protected void onPostExecute(final Cursor cursor) {
            if (cursor.getCount() == 0) {
                showEmptyListMsg(true);
            } else {
                showEmptyListMsg(false);

                String[] from = new String[] { Item.COL_DESCRIPTION, Item.COL_LOCATION,
                    Item.COL_CATEGORY, Item.COL_DATE_CAPTURED, Item.COL_DESIRABILITY };
                int[] to = new int[] { R.id.item_list_row_description, R.id.item_list_row_location,
                    R.id.item_edit_category, R.id.item_edit_date_captured, R.id.item_edit_desirability};

                adapter = new ItemListAdapter(getActivity().getApplicationContext(),
                        R.layout.item_list_row, cursor, from, to);

                //Find the listview reference
                listView = (ListView) getActivity().findViewById(R.id.item_list);

                listView.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                    Log.d(TAG, "list position: " + position);
                    Log.d(TAG, "id: " + id);

                    ItemEditFragment fragment = new ItemEditFragment();

                    Bundle bundle = new Bundle();
                    bundle.putLong(ItemEditFragment.ITEM_ID_KEY, id);
                    fragment.setArguments(bundle);

                    getFragmentManager().beginTransaction()
                            .replace(R.id.container, fragment)
                            .addToBackStack(null)
                            .commit();

                }});

                //Hook up our adapter to our ListView
                listView.setAdapter(adapter);
            }

            listener.onTaskFinished(null);
        }
    }
}
