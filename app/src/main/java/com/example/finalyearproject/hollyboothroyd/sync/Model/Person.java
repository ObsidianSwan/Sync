package com.example.finalyearproject.hollyboothroyd.sync.Model;

import java.util.HashMap;

/**
 * Created by hollyboothroyd on 11/15/2017.
 */

public class Person {
    // TODO: mVariable
    private String firstName;
    private String lastName;
    private String position;
    private String company;
    private String industry;
    private String imageId;

    private String userId;
    private double longitude;
    private double latitude;

    private HashMap<String, Integer> userSettings;

    // Used during person list population
    public Person() {
    }

    // Used during account creation. Location permissions requested after login.
    public Person(String firstName, String lastName, String position, String company,
                  String industry, String imageId, String userId, HashMap<String, Integer> defaultUserSettings) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.position = position;
        this.company = company;
        this.industry = industry;
        this.imageId = imageId;
        this.userId = userId;
        this.userSettings = defaultUserSettings;
    }

/*    public Person(String firstName, String lastName, String position, String company,
                  String industry, String imageId, String userId, double longitude, double latitude) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.position = position;
        this.company = company;
        this.industry = industry;
        this.imageId = imageId;
        this.userId = userId;
        this.longitude = longitude;
        this.latitude = latitude;
    }*/

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getUserId() { return userId; }

    public void setUserId(String userId) { this.userId = userId; }

    public double getLongitude() { return longitude; }

    public void setLongitude(double longitude) { this.longitude = longitude; }

    public double getLatitude() { return latitude; }

    public void setLatitude(double latitude) { this.latitude = latitude; }

    public HashMap<String, Integer> getUserSettings() { return userSettings; }

    public void setUserSettings(HashMap<String, Integer> userSettings) { this.userSettings = userSettings; }
}
