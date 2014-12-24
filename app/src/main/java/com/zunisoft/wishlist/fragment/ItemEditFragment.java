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

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.json.JSONException;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import com.zunisoft.common.concurrent.TaskListener;
import com.zunisoft.common.db.DatabaseAdapter;
import com.zunisoft.common.db.RecordNotFoundException;
import com.zunisoft.common.support.DatePickerDialogFragment;
import com.zunisoft.common.support.JSONFunctions;
import com.zunisoft.wishlist.model.Item;
import com.zunisoft.wishlist.R;

/**
 * Application item edit fragment.
 * 
 * This class enables editing of a pet's basic information.
 * 
 * @author krdavis
 */
public class ItemEditFragment extends Fragment implements View.OnClickListener, TaskListener {
	// Tag used by logging APIs
	private static final String TAG = "ItemEditFragment";

    // API Key
    private static final String API_KEY = "068db773023e5250f66afcddd6240564";

    // Intents
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 65000;

    // Bundle keys
    public static final String ITEM_ID_KEY = "ITEM_ID_KEY";
    private static final String SAVED_INSTANCE_STATE_LARGE_PHOTO_KEY = "LARGE_PHOTO";

    // Dialogs
    private ProgressDialog pdialog;

    // View Controls
    private ImageButton cameraButton;
    private ImageButton purchasedButton;
    private EditText editDescription;
    private EditText editLocation;
    private EditText editDateAdded;
    private EditText editDatePurchased;
    private EditText editCategory;
    private RatingBar ratingDesirability;
    private EditText editBarcode;
    private EditText editNotes;

    // Database adapter
    private DatabaseAdapter dbAdapter;

    // Member variables
    private Bitmap imageLarge;
    private boolean isTaskRunning = false;
    private boolean isEditMode;
    private DateFormat dateFormat;
    private Item item;
    private int itemId;

    // Field data map - database column name|view ID
    private HashMap<String, Integer> fieldDataMap = new HashMap<String, Integer>();

    /**
     * Empty constructor
     */
    public ItemEditFragment() {
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

        // Setup the database adapter
        dbAdapter = DatabaseAdapter.getInstance(getActivity().getApplicationContext());

        // Setup the date formats
        dateFormat = new SimpleDateFormat(getResources().getString(
                R.string.date_display_format));
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
        View rootView = inflater.inflate(R.layout.item_edit, container, false);

        // View Controls
        editDescription = (EditText)rootView.findViewById(R.id.item_edit_description);
        editLocation = (EditText)rootView.findViewById(R.id.item_edit_location);
        editCategory = (EditText)rootView.findViewById(R.id.item_edit_category);
        editDateAdded = (EditText)rootView.findViewById(R.id.item_edit_date_captured);
        editDatePurchased = (EditText)rootView.findViewById(R.id.item_edit_date_purchased);
        ratingDesirability = (RatingBar)rootView.findViewById(R.id.item_edit_desirability);
        editBarcode = (EditText)rootView.findViewById(R.id.item_edit_barcode);
        editNotes = (EditText)rootView.findViewById(R.id.item_edit_notes);

        // Setup date purchased button
        purchasedButton = (ImageButton)rootView.findViewById(R.id.item_edit_date_purchased_button);
        purchasedButton.setOnClickListener(this);

        // Set the "Camera" button
        cameraButton = (ImageButton)rootView.findViewById(R.id.item_edit_camera_button);
        cameraButton.setOnClickListener(this);

        // Set the "Scan" button
        View scanButton = rootView.findViewById(R.id.item_edit_barcode_button);
        scanButton.setOnClickListener(this);

        // Set the "Cancel" button
        View cancelButton = rootView.findViewById(R.id.item_edit_cancel_button);
        cancelButton.setOnClickListener(this);

        // Set the "OK" button
        View saveButton = rootView.findViewById(R.id.item_edit_save_button);
        saveButton.setOnClickListener(this);

        // Set field maps
        fieldDataMap.put(Item.COL_DESCRIPTION,
                R.id.item_edit_description);
        fieldDataMap.put(Item.COL_LOCATION,
                R.id.item_edit_location);
        fieldDataMap.put(Item.COL_DATE_CAPTURED,
                R.id.item_edit_date_captured);
        fieldDataMap.put(Item.COL_DATE_PURCHASED,
                R.id.item_edit_date_purchased);
        fieldDataMap.put(Item.COL_CATEGORY,
                R.id.item_edit_category);
        fieldDataMap.put(Item.COL_DESIRABILITY,
                R.id.item_edit_desirability);
        fieldDataMap.put(Item.COL_BARCODE,
                R.id.item_edit_barcode);
        fieldDataMap.put(Item.COL_NOTES,
                R.id.item_edit_notes);

        // Get the item ID
        Bundle bundle = this.getArguments();
        int itemId = (int)bundle.getLong(ITEM_ID_KEY, 0);
        Log.d(TAG, "Item ID: "  + itemId);

        try {
            // Create a new pet object
            item = new Item(getResources());
            item.setSQLiteDatabase(dbAdapter.getDatabase());

            // Load the specified pet
            item.load(itemId);

            // Set the view components
            editDescription.setText(item.getDescription());
            editLocation.setText(item.getLocation());

            String date = dateFormat.format(item.getCaptureDate());
            editDateAdded.setText(date);

            if (item.getPurchasedDate() != null) {
                date = dateFormat.format(item.getPurchasedDate());
                editDatePurchased.setText(date);
            }

            editCategory.setText(item.getCategory());
            ratingDesirability.setRating(item.getDesirability());
            editBarcode.setText(item.getBarcode());
            editNotes.setText(item.getNotes());

            // Set the photos
            if (savedInstanceState != null) {
                imageLarge = (Bitmap) savedInstanceState
                        .getParcelable(SAVED_INSTANCE_STATE_LARGE_PHOTO_KEY);
            } else if (item.getLargePhoto() != null) {
                imageLarge = item.getLargePhoto();
            } else {
                // Set default photo
                imageLarge = BitmapFactory.decodeResource(getActivity().getApplicationContext().getResources(),
                        R.drawable.ic_camera);
            }
            cameraButton.setBackground(new BitmapDrawable(getResources(), imageLarge));

            isEditMode = true;
        } catch (RecordNotFoundException e) {
            // Set the capture date (today)
            DateFormat dateDisplayFormat = new SimpleDateFormat(getString(
                    R.string.date_display_format));
            editDateAdded.setText(dateDisplayFormat.format(new Date()));


            // Set photos
            if (savedInstanceState != null) {
                imageLarge = (Bitmap) savedInstanceState
                        .getParcelable(SAVED_INSTANCE_STATE_LARGE_PHOTO_KEY);
            } else {
                // Set default photo
                imageLarge = BitmapFactory.decodeResource(getActivity().getApplicationContext().getResources(),
                        R.drawable.ic_camera);
            }
            cameraButton.setBackground(new BitmapDrawable(getResources(), imageLarge));

            isEditMode = false;
        }

        return rootView;
	}

    /**
     * Called to retrieve per-instance state from an activity before being
     * killed so that the state can be restored in onCreate(Bundle) or
     * onRestoreInstanceState(Bundle).
     *
     * @param outState
     *            Bundle in which to place your saved state.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState()");

        super.onSaveInstanceState(outState);

        // Photos
        outState.putParcelable(SAVED_INSTANCE_STATE_LARGE_PHOTO_KEY,
                imageLarge);
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

        if (isEditMode)
            inflater.inflate(R.menu.menu_item_edit, menu);
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
            case R.id.action_delete:
                this.item.delete();
                this.getFragmentManager().popBackStack();
                return true;
        }

        return true;
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
            case R.id.item_edit_camera_button:
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                getActivity().startActivityForResult(intent,
                        CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                break;
            case R.id.item_edit_barcode_button:
                IntentIntegrator scanIntegrator = new IntentIntegrator(getActivity());
                scanIntegrator.initiateScan();
                break;
            case R.id.item_edit_date_purchased_button:
                DatePickerDialogFragment picker = new DatePickerDialogFragment();
                picker.show(getFragmentManager(), "datePicker");
                break;
            case R.id.item_edit_cancel_button:
                this.getFragmentManager().popBackStack();
                break;
		    case R.id.item_edit_save_button:
			    saveRecord();
			    break;
		}
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
        Log.d(TAG, "onActivityResult() -> request code " + requestCode);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {

                Bitmap bmp = (Bitmap) intent.getExtras().get("data");
                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                // Convert ByteArray to Bitmap::
                imageLarge = BitmapFactory.decodeByteArray(byteArray, 0,
                        byteArray.length);
                cameraButton.setBackground(new BitmapDrawable(getResources(), imageLarge));
            }
        } else {

            IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

            if (scanningResult != null) {
                String scanContent = scanningResult.getContents();
                editBarcode.setText(scanContent);

                BarcodeTask task = new BarcodeTask(this);
                task.execute(scanContent);
            } else {
                Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                        "No scan data received!", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    /**
     * Saves component data to the database.
     */
    protected void saveRecord() {
        Log.d(TAG, "saveRecord()");

        // Set the model with the view component's data
        item.setDescription(editDescription.getText().toString());
        item.setLocation(editLocation.getText().toString());

        try {
            Date capture = dateFormat.parse(editDateAdded.getText().toString());
            item.setCaptureDate(capture);
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date of capture");
        }

        try {
            Date purchase = dateFormat.parse(editDatePurchased.getText().toString());
            item.setPurchasedDate(purchase);
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date of purchase");
        }

        item.setCategory(editCategory.getText().toString());
        item.setDesirability((int)ratingDesirability.getRating());
        item.setBarcode(editBarcode.getText().toString());
        item.setNotes(editNotes.getText().toString());

        // Photos
        Bitmap imageThumbnail = Bitmap.createBitmap(256, 256, imageLarge.getConfig());
        Canvas canvas = new Canvas(imageThumbnail);
        canvas.drawBitmap(imageLarge, null, new Rect(0, 0, 256, 256), null);

        item.setThumbnailPhoto(imageThumbnail);
        item.setLargePhoto(imageLarge);

        // Save the record
        if (item.save() == -1) {
            Log.d(TAG, "Save failed. Reason");

            if (!item.getErrors().isEmpty())
                setValidationHints(item.getErrors());
        } else {
            this.getFragmentManager().popBackStack();
        }


    }

    /**
     * Sets validation hints for the UI.
     *
     *  @param errors
     *            Collection of user input validation errors.
     */
    public void setValidationHints(Map<String, String> errors) {
        Log.d(TAG, "setValidationHints()");

        String field;
        String hint;
        int viewId;
        TextView tv;

        // Iterate through the errors collection and set validation hints
        // on the appropriate Views
        Log.d(TAG, "Errors:");
        for(HashMap.Entry<String, String> entry : errors.entrySet()) {
            Log.d(TAG, "Key: " + entry.getKey() + " Value: " + entry.getValue());

            // Get the values
            field = entry.getKey();
            hint = entry.getValue();
            viewId = (Integer) fieldDataMap.get(field);

            // Set the hint
            tv = (TextView) getActivity().findViewById(viewId);
            tv.setError(hint);
        }
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
    private class BarcodeTask extends AsyncTask<String, Void, HashMap> {
        private final TaskListener listener;

        public BarcodeTask(TaskListener listener) {
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            listener.onTaskStarted();
        }

        @Override
        protected HashMap doInBackground(final String... args) {
            HashMap<String, String> map = new HashMap<String, String>();
            JSONObject jsonobject;

            String code = args[0];

            // Retrieve JSON Objects from the given URL address
            jsonobject = JSONFunctions
                    .getJSONfromURL("http://www.outpan.com/api/get-product.php?apikey=" + API_KEY
                                    + "&barcode=" + code, JSONFunctions.METHOD_GET);

            // Retrieve the data points from JSON
            try {
                // Description
                map.put("description", jsonobject.getString("name"));

                // Attributes
                JSONObject jsonChildObject = (JSONObject)jsonobject.get("attributes");
                Iterator iterator  = jsonChildObject.keys();
                String key;
                String attrs = "";

                while(iterator.hasNext()){
                    key = (String)iterator.next();

                    attrs = attrs + "\n" + key + ": " + (jsonChildObject.get(key)).toString();

                    System.out.println(key + " value: " + (jsonChildObject.get(key)).toString());
                }

                map.put("notes", attrs);

            } catch (JSONException e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return map;
        }

        @Override
        protected void onPostExecute(final HashMap map) {
            // Set JSON data point in the UI
            editDescription.setText((String)map.get("description"));
            editNotes.setText((String)map.get("notes"));

            listener.onTaskFinished(null);
        }
    }
}
