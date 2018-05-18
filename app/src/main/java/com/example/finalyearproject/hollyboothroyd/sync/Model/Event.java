package com.example.finalyearproject.hollyboothroyd.sync.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hollyboothroyd
 * 1/28/2018.
 */

public class Event {

    private String mUid;
    private String mTitle;
    private String mIndustry;
    private String mDate;
    private String mTime;
    private String mStreet;
    private String mCity;
    private String mState;
    private String mZipCode;
    private String mImageId;
    private String mDescription;
    private String mCountry;
    private Double mLongitude;
    private Double mLatitude;
    private String mCreator;
    private List<String> mAttendees;


    // Used during event list population
    public Event() {
    }

    // Used when creating a new event without any attendees
    public Event(String uid, String title, String industry, String date, String time,
                 String street, String city, String state, String zipCode, String country,
                 Double longitude, Double latitude, String description, String imageId, String creator) {

        this.mUid = uid;
        this.mTitle = title;
        this.mIndustry = industry;
        this.mDate = date;
        this.mTime = time;
        this.mStreet = street;
        this.mCity = city;
        this.mState = state;
        this.mZipCode = zipCode;
        this.mCountry = country;
        this.mLongitude = longitude;
        this.mLatitude = latitude;
        this.mImageId = imageId;
        this.mDescription = description;
        this.mCreator = creator;
        this.mAttendees = new ArrayList<>();
    }

    // Used when creating a new event with attendees
    public Event(String uid, String title, String industry, String date, String time,
                 String street, String city, String state, String zipCode, String country,
                 Double longitude, Double latitude, String description, String imageId, String creator, List<String> attendees) {

        this.mUid = uid;
        this.mTitle = title;
        this.mIndustry = industry;
        this.mDate = date;
        this.mTime = time;
        this.mStreet = street;
        this.mCity = city;
        this.mState = state;
        this.mZipCode = zipCode;
        this.mCountry = country;
        this.mLongitude = longitude;
        this.mLatitude = latitude;
        this.mImageId = imageId;
        this.mDescription = description;
        this.mCreator = creator;
        this.mAttendees = attendees;
    }

    public String getUid() { return mUid; }

    public void setUid(String mUid) {
        this.mUid = mUid;
    }

    public String getTitle() { return mTitle; }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getIndustry() {
        return mIndustry;
    }

    public void setIndustry(String mIndustry) {
        this.mIndustry = mIndustry;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String mDate) {
        this.mDate = mDate;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String mTime) {
        this.mTime = mTime;
    }

    public String getStreet() {
        return mStreet;
    }

    public void setStreet(String mStreet) {
        this.mStreet = mStreet;
    }

    public String getCity() {
        return mCity;
    }

    public void setCity(String mCity) {
        this.mCity = mCity;
    }

    public String getState() {
        return mState;
    }

    public void setState(String mState) {
        this.mState = mState;
    }

    public String getZipCode() {
        return mZipCode;
    }

    public void setZipCode(String mZipCode) {
        this.mZipCode = mZipCode;
    }

    public String getImageId() {
        return mImageId;
    }

    public void setImageId(String mImageId) {
        this.mImageId = mImageId;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public String getCountry() {
        return mCountry;
    }

    public void setCountry(String mCountry) {
        this.mCountry = mCountry;
    }

    public Double getLongitude() { return mLongitude; }

    public void setLongitude(Double mLongitude) { this.mLongitude = mLongitude; }

    public Double getLatitude() { return mLatitude; }

    public void setLatitude(Double mLatitude) { this.mLatitude = mLatitude; }

    public String getCreator() {
        return mCreator;
    }

    public void setCreator(String mCreator) {
        this.mCreator = mCreator;
    }

    public List<String> getAttendees() {
        return mAttendees;
    }

    public void setAttendees(List<String> mAttendees) {
        this.mAttendees = mAttendees;
    }
}
