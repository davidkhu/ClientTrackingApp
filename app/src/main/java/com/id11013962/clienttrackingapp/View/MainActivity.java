package com.id11013962.clienttrackingapp.View;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.id11013962.clienttrackingapp.Model.AsyncResponseInterface;
import com.id11013962.clienttrackingapp.Model.DbParcelInfoDataModel;
import com.id11013962.clienttrackingapp.Model.DbParcelToDeliverDataModel;
import com.id11013962.clienttrackingapp.MongoDB.MongoGetParcelInfoAsyncTask;
import com.id11013962.clienttrackingapp.R;

import java.util.ArrayList;

/**
 * Display Main screen of parcel tracking app. Will show welcome screen using App Intro activity.
 * This screen allow you to search for a parcel and display them on a RecyclerView
 */
public class MainActivity extends AppCompatActivity {
    private static final String LIST_STATE_KEY = "Save";
    protected Button mTrackButton;
    protected EditText mParcelNumber;
    protected RecyclerView mRecyclerView;
    protected ParcelAdapter mParcelAdapter;
    private ArrayList<DbParcelInfoDataModel> mParcelInfoList = new ArrayList<>();
    protected RecyclerView.LayoutManager mLayoutManager;
    protected Parcelable mRecyclerListState;

    /**
     * Display welcome slides, and setup recyclerview
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI Widgets
        mTrackButton = (Button) findViewById(R.id.main_activity_track_button);
        mParcelNumber = (EditText) findViewById(R.id.main_activity_enter_tracking);
        mParcelNumber.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        // Show welcome slides.
        Intent intent = new Intent(this, AppIntro.class);
        startActivity(intent);

        // Setup Recycler View
        mRecyclerView = (RecyclerView) findViewById(R.id.main_activity_parcel_recycler_view);
        mParcelAdapter = new ParcelAdapter(this, mParcelInfoList);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new RecyclerViewDividerDecoration(this, LinearLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(mParcelAdapter);

        // Recycler view onTouchListener.
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, mRecyclerView, new ClickListener() {
            // Handles click on an item in the RecyclerView. Start next activity.
            @Override
            public void onClick(View view, int position) {
                DbParcelInfoDataModel mSelectedItem = mParcelInfoList.get(position);

                Intent intent = new Intent(MainActivity.this, ParcelInfoActivity.class);
                intent.putExtra(Constants.GET_PARCEL_NUMBER, mSelectedItem.getParcelNumber());
                intent.putExtra(Constants.PARCEL_NAME, mSelectedItem.getFullName());
                intent.putExtra(Constants.PARCEL_ADDRESS, mSelectedItem.getAddress());
                intent.putExtra(Constants.GET_SUBURB, mSelectedItem.getSuburb());
                intent.putExtra(Constants.GET_STATE, mSelectedItem.getState());
                intent.putExtra(Constants.GET_STATUS, mSelectedItem.getStatus());
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    /**
     * Handle the track button pressed
     * Checks: network connection and empty edit text
     * Errors: network connection , empty parcel number
     * Calls aSync Task to get data from the database.
     */
    public void trackButtonHandler(View view) {
        if (isNetworkAvailable()) {
            if (mParcelNumber.getText().toString().matches(getString(R.string.empty_string))) {
                Toast.makeText(this, R.string.please_enter_parcel_number, Toast.LENGTH_SHORT).show();
            } else {
                String parcelNumber = mParcelNumber.getText().toString();

                // Get data from database and return data to display on RecyclerView.
                // AsyncResponseInterface is used to get data value back.
                MongoGetParcelInfoAsyncTask parcelInfoAsyncTask = new MongoGetParcelInfoAsyncTask(this, parcelNumber,
                        new AsyncResponseInterface() {
                            @Override
                            public void processFinish(DbParcelInfoDataModel output) {
                                DbParcelInfoDataModel mParcelInfo = output;
                                if (mParcelInfo != null) {
                                    mParcelInfoList.add(mParcelInfo);
                                    mParcelAdapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void processFinish(DbParcelToDeliverDataModel output,
                                                      String travelDuration, String suburb) {
                            }

                        });
                parcelInfoAsyncTask.execute();
            }
        } else {
            displayErrorDialog();
        }
    }

    /**
     * Error Dialog for No Network Connection
     */
    private void displayErrorDialog() {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);

        dlgAlert.setMessage(R.string.no_network);
        dlgAlert.setTitle(R.string.error);
        dlgAlert.setPositiveButton(R.string.ok, null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();

        dlgAlert.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
    }

    /**
     * Save recyclerView data
     */
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

        // Save list state
        mRecyclerListState = mLayoutManager.onSaveInstanceState();
        state.putParcelable(LIST_STATE_KEY, mRecyclerListState);
    }

    /**
     * Restore recyclerView data
     */
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);

        // Retrieve list state and list/item positions
        if (state != null)
            mRecyclerListState = state.getParcelable(LIST_STATE_KEY);
    }

    /**
     * Restore recyclerView data onResume
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (mRecyclerListState != null) {
            mLayoutManager.onRestoreInstanceState(mRecyclerListState);
        }
    }

    /**
     * Restore recyclerView data onRestart
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        if (mRecyclerListState != null) {
            mLayoutManager.onRestoreInstanceState(mRecyclerListState);
        }
    }

    /**
     * Check if WIFI or Mobile Network is available
     */
    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        // if no network is available networkInfo will be null
        // otherwise check if connected
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    /**
     * Interface for RecyclerView onClickListener custom functions
     */
    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    /**
     * Recycler View Touch Listener class.
     * Code this followed from tutorial android hive
     * Link; - http://www.androidhive.info/2016/01/android-working-with-recycler-view/
     */
    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private MainActivity.ClickListener clickListener;

        /**
         * OnTouchListener for RecyclerView class
         */
        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final MainActivity.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
}
