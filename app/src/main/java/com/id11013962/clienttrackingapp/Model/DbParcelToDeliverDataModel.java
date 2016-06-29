package com.id11013962.clienttrackingapp.Model;

/**
 * Data Model for Parcel to be delivered.
 * This data is for when parcel is registered with the tracker app.
 */
public class DbParcelToDeliverDataModel {
    public double latitude;
    public double longitude;
    public String parcelNumber;
    public String dateTimeUpdated;

    public String getDateTimeUpdated() {
        return dateTimeUpdated;
    }

    public void setDateTimeUpdated(String dateTimeUpdated) {
        this.dateTimeUpdated = dateTimeUpdated;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getParcelNumber() {
        return parcelNumber;
    }

    public void setParcelNumber(String parcelNumber) {
        this.parcelNumber = parcelNumber;
    }
}
