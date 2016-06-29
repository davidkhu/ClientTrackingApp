package com.id11013962.clienttrackingapp.MongoDB;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import com.id11013962.clienttrackingapp.Model.AsyncResponseInterface;
import com.id11013962.clienttrackingapp.Model.DbParcelInfoDataModel;
import com.id11013962.clienttrackingapp.R;
import com.id11013962.clienttrackingapp.View.MainActivity;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Async Task to Mongo MLab Cloud database to get Parcel Information.
 * * This code is following the tutorial by Michael Kyazze but implemented for my own uses
 * Reference - https://michaelkyazze.wordpress.com/2014/05/18/android-mongodb-mongolab-hosted-sample-app-part-one/
 */
public class MongoGetParcelInfoAsyncTask extends AsyncTask<Void, Void, DbParcelInfoDataModel> {
    static String server_output = null;
    static String temp_output = null;
    private String mParcelNumber;
    private Context mContext;
    private ProgressDialog mProgressDialog;
    private static final String TAG = "GetParcelInfo";

    // this is for returning our result via the AsyncResponseInterface.
    public AsyncResponseInterface delegate = null;

    /**
     * need parcel number for database query
     * activity context to show alert Dialog
     * delegate for result return
     */
    public MongoGetParcelInfoAsyncTask(MainActivity activity, String parcelNumber, AsyncResponseInterface delegate) {
        this.mParcelNumber = parcelNumber;
        this.mContext = activity;
        this.delegate = delegate;
    }

    /**
     * show loading screen while getting data.
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setMessage(mContext.getString(R.string.getting_parcel));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
    }

    /**
     * Request HTTP Connection and query the mongo MLab. using the Mlab API services.
     */
    @Override
    protected DbParcelInfoDataModel doInBackground(Void... params) {
        // Data model to fill.
        DbParcelInfoDataModel parcelInfoDataModel = new DbParcelInfoDataModel();
        try {
            Log.d(TAG, mContext.getString(R.string.getting_data));

            // build query URL and request via the internet.
            // GET for requesting the data in database.
            MongoParcelInfoQueryBuilder qb = new MongoParcelInfoQueryBuilder();
            URL url = new URL(qb.buildMongoDbSingleItemURL(mParcelNumber));
            HttpURLConnection conn = (HttpURLConnection) url
                    .openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            // If cannot find the parcelNumber return null.
            if (conn.getResponseCode() != 200) {
                return parcelInfoDataModel = null;
            }

            // Reads text from a character-input stream
            // buffering characters so as to provide for the efficient reading of characters, arrays, and lines.
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            while ((temp_output = br.readLine()) != null) {
                server_output = temp_output;
            }

            // Get the string and parse to object from the output reader.
            String mongoArray = "{ artificial_basicdb_list: " + server_output + "}";
            Object object = com.mongodb.util.JSON.parse(mongoArray);

            DBObject dbObj = (DBObject) object;
            // Get the actual object
            BasicDBObject parcelToDeliver = (BasicDBObject) dbObj.get("artificial_basicdb_list");

            // Add it to our data model.
            parcelInfoDataModel.setParcelNumber(parcelToDeliver.get("_id").toString());
            parcelInfoDataModel.setFullName(parcelToDeliver.get("fullname").toString());
            parcelInfoDataModel.setAddress(parcelToDeliver.get("address").toString());
            parcelInfoDataModel.setSuburb(parcelToDeliver.get("suburb").toString());
            parcelInfoDataModel.setState(parcelToDeliver.get("state").toString());
            parcelInfoDataModel.setCity(parcelToDeliver.get("city").toString());
            parcelInfoDataModel.setPostcode(parcelToDeliver.get("postcode").toString());
            parcelInfoDataModel.setStatus(parcelToDeliver.get("status").toString());
            parcelInfoDataModel.setDateTimeDelivered(parcelToDeliver.get("dateTimeDelivered").toString());
        } catch (ProtocolException | MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, mContext.getString(R.string.get_data_done));
        return parcelInfoDataModel;
    }

    /**
     * check data
     * if data is invalid show error message.
     */
    @Override
    protected void onPostExecute(DbParcelInfoDataModel dbParcelInfoDataModel) {
        super.onPostExecute(dbParcelInfoDataModel);
        mProgressDialog.dismiss();

        // If null, failed to get data.
        // Means data is not on db. Invalid parcel number
        // Show alert dialog
        if(dbParcelInfoDataModel == null){
            AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(mContext);

            dlgAlert.setMessage(R.string.invalid_parcel_number);
            dlgAlert.setTitle(R.string.error_title);
            dlgAlert.setPositiveButton(R.string.ok_button, null);
            dlgAlert.setCancelable(true);
            dlgAlert.create().show();

            dlgAlert.setPositiveButton(R.string.ok_button,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
        }
        // return result
        delegate.processFinish(dbParcelInfoDataModel);
    }
}
