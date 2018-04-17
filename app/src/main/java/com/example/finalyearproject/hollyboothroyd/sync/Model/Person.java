package com.example.finalyearproject.hollyboothroyd.sync.Model;

import java.util.HashMap;

/**
 * Created by hollyboothroyd on 11/15/2017.
 */

public class Person {
    private String mFirstName;
    private String mLastName;
    private String mPosition;
    private String mCompany;
    private String mIndustry;
    private String mImageId;
    private String mUserId;
    private boolean mIsLinkedInConnected;

    private HashMap<String, Integer> mUserSettings;

    // Used during person list population
    public Person() {
    }

    // Used during account creation. Location permissions requested after login.
    public Person(String firstName, String lastName, String position, String company,
                  String industry, String imageId, String userId, HashMap<String, Integer> defaultUserSettings, boolean isLinkedInConnected) {
        this.mFirstName = firstName;
        this.mLastName = lastName;
        this.mPosition = position;
        this.mCompany = company;
        this.mIndustry = industry;
        this.mImageId = imageId;
        this.mUserId = userId;
        this.mUserSettings = defaultUserSettings;
        this.mIsLinkedInConnected = isLinkedInConnected;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String firstName) {
        this.mFirstName = firstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public void setLastName(String lastName) {
        this.mLastName = lastName;
    }

    public String getPosition() {
        return mPosition;
    }

    public void setPosition(String position) {
        this.mPosition = position;
    }

    public String getCompany() {
        return mCompany;
    }

    public void setCompany(String company) {
        this.mCompany = company;
    }

    public String getIndustry() {
        return mIndustry;
    }

    public void setIndustry(String industry) {
        this.mIndustry = industry;
    }

    public String getImageId() {
        return mImageId;
    }

    public void setImageId(String imageId) {
        this.mImageId = imageId;
    }

    public String getUserId() { return mUserId; }

    public void setUserId(String userId) { this.mUserId = userId; }

    public HashMap<String, Integer> getUserSettings() { return mUserSettings; }

    public void setUserSettings(HashMap<String, Integer> userSettings) { this.mUserSettings = userSettings; }

    public boolean getIsLinkedInConnected() { return mIsLinkedInConnected; }

    public void setIsLinkedInConnected(boolean isLinkedInConnected) { this.mIsLinkedInConnected = isLinkedInConnected; }
}
