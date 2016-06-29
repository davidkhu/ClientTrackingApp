package com.id11013962.clienttrackingapp.View;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.id11013962.clienttrackingapp.R;

import java.util.Objects;

/**
 * Information about the parcel displayed on the UI
 * Minimal information here, only showing the suburbs not full address because security purposes.
 */
public class ParcelInfoActivity extends Activity {
    private String mParcelNumberString;
    private String mParcelAddressString;
    private String mParcelSuburbString;
    private String mParcelStateString;

    /**
     * Sets up data and display UI.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parcel_info);

        // UI Widget
        TextView mParcelNumber = (TextView) findViewById(R.id.parcel_info_parcel_number);
        TextView mParcelToName = (TextView) findViewById(R.id.parcel_info_delivery_name);
        TextView mParcelAddress = (TextView) findViewById(R.id.parcel_info_delivery_address);
        TextView mParcelStatus = (TextView) findViewById(R.id.parcel_info_status);
        Button mParcelLocationButton = (Button) findViewById(R.id.parcel_item_track_parcel);

        // Get the data from the intent
        Intent intent = getIntent();
        mParcelNumberString = intent.getStringExtra(Constants.GET_PARCEL_NUMBER);
        mParcelAddressString = intent.getStringExtra(Constants.PARCEL_ADDRESS);
        String name = intent.getStringExtra(Constants.PARCEL_NAME);
        mParcelSuburbString = intent.getStringExtra(Constants.GET_SUBURB);
        mParcelStateString = intent.getStringExtra(Constants.GET_STATE);
        String status = intent.getStringExtra(Constants.GET_STATUS);

        // Set UI
        mParcelNumber.setText(mParcelNumberString);
        mParcelToName.setText(name);
        mParcelAddress.setText(String.format(getString(R.string.address_result), mParcelSuburbString, mParcelStateString));
        mParcelStatus.setText(status);

        // Check if status is in Transit, if so make it visible.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (Objects.equals(status, "In Transit")){
                mParcelLocationButton.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Handles the track parcel button.
     * Pass data to next activity
     */
    public void ParcelLocationButtonHandler(View v){
        if (isNetworkAvailable()){
            Intent intent = new Intent(this, ParcelLocationActivity.class);
            intent.putExtra(Constants.GET_PARCEL_NUMBER, mParcelNumberString);
            intent.putExtra(Constants.PARCEL_ADDRESS, mParcelAddressString);
            intent.putExtra(Constants.GET_SUBURB, mParcelSuburbString);
            intent.putExtra(Constants.GET_STATE, mParcelStateString);
            startActivity(intent);
        }else{
            displayErrorDialog();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
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
}
