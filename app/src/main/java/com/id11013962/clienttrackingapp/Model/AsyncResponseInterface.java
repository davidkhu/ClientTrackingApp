package com.id11013962.clienttrackingapp.Model;

/**
 * Asynchronous Response results interface.
 */
public interface AsyncResponseInterface {
    // Get parcel information.
    void processFinish(DbParcelInfoDataModel output);

    // Get parcel to be delivered location information
    void processFinish(DbParcelToDeliverDataModel output, String travelDuration, String suburb);
}
