package com.example.finalyearproject.hollyboothroyd.sync.Utils;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

/**
 * Created by hollyboothroyd
 * 12/10/2017.
 */

public class Constants {

    // Map update and search names and defaults
    public static final String locationTimeUpdateIntervalName = "locationTimeUpdateInterval";
    public static final String locationDistanceUpdateIntervalName = "locationDistanceUpdateInterval";
    public static final String searchRadiusName = "searchRadius";
    public static final String privacyIntensityName = "privacyIntensity";

    public static final int locationTimeUpdateIntervalDefault = 1000;
    public static final int locationDistanceUpdateIntervalDefault = 5;
    public static final int obfuscationRadiusDefault = 60; // Meters
    public static final int geofenceRadiusDefault = 200; //Meters
    public static final int privacyIntensityDefault = 2; //Intermediate: N-Rand
    public static final float metersPerDegree = 111300f;
    public static final int obfuscationTrials = 4;
    public static final double earthRadiusMeters = 6371000.00;
    public static final int geofenceCircleColor = 0x90CCCCCC; // Light grey with 90% opacity

    public static final String mapZoomLevelName = "mapZoomLevel";
    public static final int mapZoomLevelDefault = 15;

    public static final String personMarkerTag = "Person";
    public static final String eventMarkerTag = "Event";

    public static final String geofenceLatitude = "latitude";
    public static final String geofenceLongitude = "longitude";
    public static final String geofenceUserId = "userId";

    // Location Privacy Names


    // Personalization
    public static final String personPinColorName = "personPinColor";
    public static final String eventPinColorName = "eventPinColor";

    public static final float personPinColorDefault = BitmapDescriptorFactory.HUE_GREEN;
    public static final float eventPinColorDefault = BitmapDescriptorFactory.HUE_AZURE;

    // Settings
    public static final int zoomPickerMinValue = 0;
    public static final int zoomPickerMaxValue = 18;

    public static final int timeRefreshRatePickerMinValue = 1;
    public static final int timeRefreshRatePickerMaxValue = 60;
    public static final int timeRefreshRatePickerStepValue = 5;
    public static final int timeRefreshRatePickerSize = 13;

    public static final int disRefreshRatePickerMinValue = 1;
    public static final int disRefreshRatePickerMaxValue = 500;
    public static final int disRefreshRatePickerStepValue = 20;
    public static final int disRefreshRatePickerSize = 26;

    public static final int searchRadiusPickerMinValue = 1;
    public static final int searchRadiusPickerMaxValue = 800;
    public static final int searchRadiusPickerStepValue = 20;
    public static final int searchRadiusPickerSize = 36;


    // Database/storage references
    public static final String peopleDatabaseRefName = "people";
    public static final String userNotificationDatabaseRefName = "notifications";
    public static final String connectionDatabaseRefName = "connections";
    public static final String locationDatabaseRefName = "people_locations";
    public static final String connectionRequestsDatabaseRefName = "connection_requests";

    public static final String eventDatabaseRefName = "event";
    public static final String peopleEventsCreatedDatabaseRefName = "events_created";
    public static final String peopleEventsAttendingDatabaseRefName = "events_attending";
    public static final String eventAttendeesDatabaseRefName = "event_attendees";

    public static final String userSettingsDatabaseRefName = "userSettings";

    public static final int GALLERY_CODE = 1;
    public static final String userImgChildName = "imageId";
    public static final String userFirstNameChildName = "firstName";
    public static final String userLastNameChildName = "lastName";
    public static final String userPositionChildName = "position";
    public static final String userIndustryChildName = "industry";
    public static final String userCompanyChildName = "company";
    public static final String userLinkedInChildName = "linkedin";

    public static final String eventTitleChildName = "title";
    public static final String eventIndustryChildName = "industry";
    public static final String eventDateChildName = "date";
    public static final String eventTimeChildName = "time";
    public static final String eventStreetChildName = "street";
    public static final String eventCityChildName = "city";
    public static final String eventStateChildName = "state";
    public static final String eventZipCodeChildName = "zipCode";
    public static final String eventCountryChildName = "country";
    public static final String eventDescriptionChildName = "description";
    public static final String eventImgChildName = "imageId";
    public static final String eventLongitudeChildName = "longitude";
    public static final String eventLatitudeChildName = "latitude";

    public static final String personImgStorageName = "person_images";
    public static final String eventImgStorageName = "event_images";

    // Event tabs
    public static final String allEventsTab = "allEvents";
    public static final String allEventsTabName = "All Events";
    public static final String eventsAttendingTab = "eventsAttending";
    public static final String eventsAttendingTabName = "Events Attending";
    public static final String eventsHostingTab = "eventsHosting";
    public static final String eventsHostingTabName = "Events Hosting";
}
