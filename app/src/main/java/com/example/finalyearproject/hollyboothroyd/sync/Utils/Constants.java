package com.example.finalyearproject.hollyboothroyd.sync.Utils;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

/**
 * Created by hollyboothroyd on 12/10/2017.
 */

public class Constants {

    // SharePreferences Settings
    public static final String preferences = "Settings";

    // Map update and search names and defaults
    public static final String locationTimeUpdateIntervalName = "locationTimeUpdateInterval";
    public static final String locationDistanceUpdateIntervalName = "locationDistanceUpdateInterval";
    public static final String searchRadiusName = "searchRadius";

    public static final int locationTimeUpdateIntervalDefault = 1000;
    public static final int locationDistanceUpdateIntervalDefault = 5;
    public static final int searchRadiusDefault = 5; // TODO: Select proper value

    public static final String mapZoomLevelName = "mapZoomLevel";
    public static final int mapZoomLevelDefault = 15;

    public static final String personMarkerTag = "Person";
    public static final String eventMarkerTag = "Event";

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

    public static final int disRefreshRatePickerMinValue = 5;
    public static final int disRefreshRatePickerMaxValue = 500;
}
