package com.id11013962.clienttrackingapp.MongoDB;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.id11013962.clienttrackingapp.Model.AsyncResponseInterface;
import com.id11013962.clienttrackingapp.Model.DbParcelToDeliverDataModel;
import com.id11013962.clienttrackingapp.R;
import com.id11013962.clienttrackingapp.View.ParcelLocationActivity;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

/**
 * Async Task to Mongo MLab Cloud database to get Parcel to Deliver Information.
 * This code is following the tutorial by Michael Kyazze but implemented for my own uses
 * Reference - https://michaelkyazze.wordpress.com/2014/05/18/android-mongodb-mongolab-hosted-sample-app-part-one/
 */
public class MongoGetParcelToDeliverAsyncTask extends AsyncTask<Void, Void, DbParcelToDeliverDataModel> {
    private static final String GMAP_TAG = "Exception Gmap Duration";
    private static final String GET_TAG = "GetLocation";
    static String server_output = null;
    static String temp_output = null;
    private String mParcelNumber;
    private Context mContext;
    private String mParcelAddressString;
    private String mSuburb;
    private String mTravelDuration;
    private ProgressDialog mProgressDialog;

    // Return result via AsyncResponseInterface.
    public AsyncResponseInterface delegate = null;

    /**
     * need parcel number for database query
     * activity context for Geo coder
     * Address string for Geo Coder
     * delegate for result return
     */
    public MongoGetParcelToDeliverAsyncTask(String mParcelNumberString,
                                            ParcelLocationActivity parcelLocationActivity,
                                            String parcelAddressString,
                                            AsyncResponseInterface delegate) {
        this.mParcelNumber = mParcelNumberString;
        this.delegate = delegate;
        this.mContext = parcelLocationActivity;
        this.mParcelAddressString = parcelAddressString;
    }

    /**
     * Show progress dialog as getting data and calculating distance duration
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setMessage("Calculating...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
    }

    /**
     * Request HTTP Connection and query the mongo MLab. using the Mlab API services.
     * Get walking duration between two Lat/Lng using Google Distance Matrix API URl request.
     */
    @Override
    protected DbParcelToDeliverDataModel doInBackground(Void... params) {
        DbParcelToDeliverDataModel dbData = new DbParcelToDeliverDataModel();
        try {
            Log.d(GET_TAG, mContext.getString(R.string.geting_location));

            // build query URL and request via the internet To GET data back.
            MongoParcelToDeliverQueryBuilder qb = new MongoParcelToDeliverQueryBuilder();
            URL url = new URL(qb.buildMongoDbSingleURL(mParcelNumber));
            HttpURLConnection conn = (HttpURLConnection) url
                    .openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            // Handle exception if error
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            // buffering characters so as to provide for the efficient reading of characters, arrays, and lines.
            // Reads text from a character-input stream
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            while ((temp_output = br.readLine()) != null) {
                server_output = temp_output;
            }

            // create a basic db list
            String mongoarray = "{ artificial_basicdb_list: " + server_output + "}";
            Object o = com.mongodb.util.JSON.parse(mongoarray);

            DBObject dbObj = (DBObject) o;
            BasicDBObject parcelToDeliver = (BasicDBObject) dbObj.get("artificial_basicdb_list");

            // set data fetched to data model
            dbData.setParcelNumber(parcelToDeliver.get("_id").toString());
            dbData.setLatitude(Double.parseDouble(parcelToDeliver.get("latitude").toString()));
            dbData.setLongitude(Double.parseDouble(parcelToDeliver.get("longitude").toString()));
            dbData.setDateTimeUpdated(parcelToDeliver.get("dateTimeUpdated").toString());

        } catch (ProtocolException | MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Using Geocoder to perform Geocoding and reverse Geocoding.
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        try {
            // Geo coder : Get the suburb of the parcel location
            List<Address> parcelAddressList = geocoder.getFromLocation(dbData.getLatitude(), dbData.getLongitude(), 1);
            mSuburb = (parcelAddressList.isEmpty() ? null : parcelAddressList.get(0).getLocality());

            // Get parcel receiver Lat/Lng from the address given.
            List<Address> clientAddressList = geocoder.getFromLocationName(mParcelAddressString, 1);
            LatLng clientAddress = new LatLng(clientAddressList.get(0).getLatitude(), clientAddressList.get(0).getLongitude());
            LatLng parcelLocation = new LatLng(dbData.getLatitude(), dbData.getLongitude());

            // Get the Google distance matrix API url 
            String url = getDirectionsUrl(parcelLocation, clientAddress);

            // get the duration from the internet
            mTravelDuration = getDuration(url);

        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(GET_TAG, mContext.getString(R.string.get_location_done));
        return dbData;
    }

    /**
     * Request HTTP Connection and query the google direction URL to give us a String JSON Object.
     * Get walking duration between two Lat/Lng using Google Distance Matrix API URl request.
     */
    private String getDuration(String url) {
        JSONObject object;
        String duration = null;

        // For storing data from web service
        String data = "";

        try {
            // Fetching the data from web service
            data = downloadUrl(url);
        } catch (Exception e) {
            Log.d(GMAP_TAG, e.toString());
        }

        // Convert the string JSON object into JSON and get the duration out.
        try {
            object = new JSONObject(data);
            JSONArray rows = object.getJSONArray("rows");
            JSONObject row = rows != null ? rows.getJSONObject(0) : null;
            JSONArray elements = row != null ? row.getJSONArray("elements") : null;
            JSONObject element = elements != null ? elements.getJSONObject(0) : null;
            JSONObject durationObj = element != null ? element.getJSONObject("duration") : null;
            duration = durationObj != null ? durationObj.getString("text") : null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return duration;
    }

    /**
     * Return result via interface.
     */
    @Override
    protected void onPostExecute(DbParcelToDeliverDataModel dbParcelToDeliverDataModel) {
        super.onPostExecute(dbParcelToDeliverDataModel);
        mProgressDialog.dismiss();
        delegate.processFinish(dbParcelToDeliverDataModel, mTravelDuration, mSuburb);
    }

    /**
     * Build Google Distance Matrix API URL request.
     */
    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origins=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destinations=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Mode Walking
        String mode = "mode=walking";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        return "https://maps.googleapis.com/maps/api/distancematrix/" + output + "?" + parameters;
    }

    /**
     * Request HTTP over the web and enter the google URL to get JSON duration object.
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuilder sb = new StringBuilder();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d(GMAP_TAG, e.toString());
        } finally {
            assert iStream != null;
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


}
