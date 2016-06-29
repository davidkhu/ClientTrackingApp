package com.id11013962.clienttrackingapp.Model;

/**
 * Data Model for Parcel Information.
 * Attributes for each Parcel has these attributes.
 */
public class DbParcelInfoDataModel {
    public String parcelNumber;
    public String fullName;
    public String address;
    public String suburb;
    public String state;
    public String city;
    public String postcode;
    public String status;
    public String dateTimeDelivered;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDateTimeDelivered() {
        return dateTimeDelivered;
    }

    public void setDateTimeDelivered(String dateTimeDelivered) {
        this.dateTimeDelivered = dateTimeDelivered;
    }

    public String getParcelNumber() {
        return parcelNumber;
    }

    public void setParcelNumber(String parcelNumber) {
        this.parcelNumber = parcelNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSuburb() {
        return suburb;
    }

    public void setSuburb(String suburb) {
        this.suburb = suburb;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }
}
