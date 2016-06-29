package com.id11013962.clienttrackingapp.View;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.id11013962.clienttrackingapp.Model.AsyncResponseInterface;
import com.id11013962.clienttrackingapp.Model.DbParcelInfoDataModel;
import com.id11013962.clienttrackingapp.Model.DbParcelToDeliverDataModel;
import com.id11013962.clienttrackingapp.MongoDB.MongoGetParcelToDeliverAsyncTask;
import com.id11013962.clienttrackingapp.R;

/**
 * This activity displays the location of the parcel and estimate delivery time.
 */
public class ParcelLocationActivity extends Activity implements OnMapReadyCallback {
    private static final float DEFAULT_ZOOM = 15;
    private static final double TIME_ADDED_IN_HOURS = 0.75;
    private String mParcelNumberString;
    private String mParcelAddressString;
    private MapView mMapView;
    private GoogleMap mMap;
    private TextView mParcelAddress;
    private TextView mLastUpdatedTime;
    private TextView mEstimateDeliveryTime;

    /**
     * Setup intent for Async task for OnCreate.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parcel_location);

        // UI widget declaration
        mParcelAddress = (TextView) findViewById(R.id.parcel_location_address);
        mLastUpdatedTime = (TextView) findViewById(R.id.parcel_location_updated_time);
        mEstimateDeliveryTime = (TextView) findViewById(R.id.parcel_estimate_delivery);

        // Map Setup
        mMapView = (MapView) findViewById(R.id.display_google_map);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        // Get Intent
        Intent intent = getIntent();
        mParcelNumberString = intent.getStringExtra(Constants.GET_PARCEL_NUMBER);
        String address = intent.getStringExtra(Constants.PARCEL_ADDRESS);
        String suburb = intent.getStringExtra(Constants.GET_SUBURB);
        String state = intent.getStringExtra(Constants.GET_STATE);

        // Setup parcel address for Async Task
        mParcelAddressString = String.format(getString(R.string.address_full), address, suburb, state);
    }

    /**
     * Get the data from the database Asynchronously.
     * Get duration of two points using google distance API.
     * Calculate estimate time of delivery
     */
    @Override
    protected void onStart() {
        super.onStart();

        // Get Parcel Location and duration.
        MongoGetParcelToDeliverAsyncTask getParcel = new MongoGetParcelToDeliverAsyncTask(mParcelNumberString,
                this, mParcelAddressString, new AsyncResponseInterface() {

            @Override
            public void processFinish(DbParcelToDeliverDataModel parcelLocationData,
                                      String travelDuration, String suburb) {
                if (parcelLocationData != null) {

                    // Update UI and Map
                    LatLng currentLatLng = new LatLng(parcelLocationData.getLatitude(),
                            parcelLocationData.getLongitude());
                    mMap.addMarker(new MarkerOptions()
                            .position(currentLatLng)
                            .title(getString(R.string.parcel_position)));
                    CameraUpdate mapUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng, DEFAULT_ZOOM);
                    mMap.animateCamera(mapUpdate);

                    mParcelAddress.setText(suburb);
                    mLastUpdatedTime.setText(parcelLocationData.getDateTimeUpdated());

                    // Estimate delivery time
                    calculateDeliveryTime(travelDuration);
                }
            }

            @Override
            public void processFinish(DbParcelInfoDataModel output) {

            }
        });
        getParcel.execute();
    }

    /**
     * Get estimate delivery time.
     * travel duration of two points + time added in hours (45min)
     * Round of to nearest hour.
     */
    private void calculateDeliveryTime(String travelDuration) {
        String[] travelArray = travelDuration.trim().split(" ");
        double totalDurationInHour = TIME_ADDED_IN_HOURS;

        // Break down the string duration and combine to total hours.
        for (int i = 0; i < travelArray.length; i += 2) {
            switch (travelArray[i + 1].toLowerCase()) {
                case Constants.DAY:
                case Constants.DAYS:
                    totalDurationInHour += Integer.parseInt(travelArray[i]) * (24);
                    break;
                case Constants.HOUR:
                case Constants.HOURS:
                    totalDurationInHour += Integer.parseInt(travelArray[i]);
                    break;
                case Constants.MINUTE:
                case Constants.MINUTES:
                    totalDurationInHour += Integer.parseInt(travelArray[i]) * 0.0166667;
                    break;
                default:
            }
        }
        // Update UI
        mEstimateDeliveryTime.setText(String.valueOf(Math.ceil(totalDurationInHour) + getString(R.string.hours)));
    }

    /**
     * Initialise Google MAP
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }
}
