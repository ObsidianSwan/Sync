package com.example.finalyearproject.hollyboothroyd.sync.Model;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.finalyearproject.hollyboothroyd.sync.R;
import com.example.finalyearproject.hollyboothroyd.sync.Services.DatabaseManager;
import com.example.finalyearproject.hollyboothroyd.sync.Services.LocationFilter;
import com.example.finalyearproject.hollyboothroyd.sync.Utils.Constants;
import com.example.finalyearproject.hollyboothroyd.sync.Utils.Util;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by hollyboothroyd
 * 1/28/2018.
 */

public class UserEvents {
    private static final String TAG = "UserEvents";

    public static List<UserEventsListener> mListeners = new ArrayList<>();

    private DatabaseManager mDatabaseManager;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;

    public static final List<Event> ALL_EVENTS = new ArrayList<>();
    public static final Map<String, Event> ALL_EVENTS_MAP = new HashMap<>();

    public static final List<Event> EVENTS_ATTENDING = new ArrayList<>();
    public static final Map<String, Event> EVENTS_ATTENDING_MAP = new HashMap<>();

    public static final List<Event> EVENTS_HOSTING = new ArrayList<>();
    public static final Map<String, Event> EVENTS_HOSTING_MAP = new HashMap<>();

    private ValueEventListener mAllEventsListener;
    private ValueEventListener mEventsAttendingListener;
    private ValueEventListener mEventsHostingListener;

    private int mLocationTimeUpdateInterval = Constants.locationTimeUpdateIntervalDefault;
    private int mLocationDistanceUpdateIntervalName = Constants.locationDistanceUpdateIntervalDefault;


    private boolean mAllEventsUpdated = false;
    private boolean mEventsAttendingUpdated = false;
    private boolean mEventsHostingUpdated = false;

    private int mSearchRadius = Constants.geofenceRadiusDefault;


    public UserEvents(final Context context) {

        mDatabaseManager = new DatabaseManager();
        mLocationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

        // Set the location time update interval value to the users choice found in the settings database
        mDatabaseManager.getUserSettings(Constants.locationTimeUpdateIntervalName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Set the location time update interval to the default in case the value cannot be set to the database value
                int value = Constants.locationTimeUpdateIntervalDefault;
                if (dataSnapshot.getValue(Integer.class) != null) {
                    // Set the location time update interval to the saved settings
                    value = dataSnapshot.getValue(Integer.class);
                }

                mLocationTimeUpdateInterval = value;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.toString());
            }
        });

        // Set the location distance update interval value to the users choice found in the settings database
        mDatabaseManager.getUserSettings(Constants.locationDistanceUpdateIntervalName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Set the location distance update interval to the default in case the value cannot be set to the database value
                int value = Constants.locationDistanceUpdateIntervalDefault;
                if (dataSnapshot.getValue(Integer.class) != null) {
                    // Set the location distance update interval to the saved settings
                    value = dataSnapshot.getValue(Integer.class);
                }

                mLocationDistanceUpdateIntervalName = value;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.toString());
            }
        });

        // Check if location permissions have been granted
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Retrieve the search radius to determine if events were local
            mDatabaseManager.getUserSettings(Constants.searchRadiusName).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mSearchRadius = Constants.geofenceRadiusDefault;
                    if (dataSnapshot.getValue(Integer.class) != null) {
                        mSearchRadius = dataSnapshot.getValue(Integer.class);
                    }

                    // Listen for changes in the events database
                    // Listen for any changes to the current users location
                    mLocationListener = new LocationListener() {
                        @Override
                        public void onLocationChanged(final Location location) {
                            // Check to remove existing events first
                            // This is quicker than waiting to pull down all events
                            mAllEventsUpdated = false;
                            Iterator<Event> listIterator = ALL_EVENTS.iterator();
                            while (listIterator.hasNext()) {
                                Event event = listIterator.next();
                                LatLng eventPosition = new LatLng(event.getLatitude(), event.getLongitude());
                                if (!LocationFilter.eventWithinRange(new LatLng(location.getLatitude(), location.getLongitude()), eventPosition, mSearchRadius)) {
                                    listIterator.remove();
                                    ALL_EVENTS_MAP.remove(event.getUid());
                                    mAllEventsUpdated = true;
                                }
                            }
                            if (mAllEventsUpdated) {
                                for (UserEventsListener listener : mListeners) {
                                    listener.userEventsUpdated();
                                }
                            }

                            // Add new events that should appear as the user moves
                            mAllEventsListener = mDatabaseManager.getAllEventsDatabaseReference().addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    mAllEventsUpdated = false;
                                    ALL_EVENTS.clear();
                                    ALL_EVENTS_MAP.clear();
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        Event event = snapshot.getValue(Event.class);
                                        if (event != null) {
                                            LatLng eventPosition = new LatLng(event.getLatitude(), event.getLongitude());
                                            // If the users last location can be found, populate the map with local events
                                            if (location != null) {
                                                LatLng userPosition = new LatLng(location.getLatitude(), location.getLongitude());
                                                // Check that the event is within the search radius of the current user
                                                if (LocationFilter.eventWithinRange(userPosition, eventPosition, mSearchRadius)) {
                                                    ALL_EVENTS.add(event);
                                                    ALL_EVENTS_MAP.put(event.getUid(), event);
                                                    mAllEventsUpdated = true;
                                                }
                                            } else {
                                                Log.e(TAG, context.getString(R.string.location_not_found_error));
                                                Toast.makeText(context, R.string.could_not_find_location, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                    if (mAllEventsUpdated) {
                                        for (UserEventsListener listener : mListeners) {
                                            listener.userEventsUpdated();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.e(TAG, databaseError.toString());
                                }
                            });
                        }

                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {

                        }

                        @Override
                        public void onProviderEnabled(String provider) {

                        }

                        @Override
                        public void onProviderDisabled(String provider) {

                        }
                    };

                    requestLocationUpdates();

                    mAllEventsListener = mDatabaseManager.getAllEventsDatabaseReference().addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            mAllEventsUpdated = false;
                            ALL_EVENTS.clear();
                            ALL_EVENTS_MAP.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Event event = snapshot.getValue(Event.class);
                                if (event != null) {
                                    LatLng eventPosition = new LatLng(event.getLatitude(), event.getLongitude());
                                    // Get the users last known location
                                    Location userLastKnowLocation = Util.getLastKnownLocation(mLocationManager);
                                    // If the users last location can be found, populate the map with local events
                                    if (userLastKnowLocation != null) {
                                        LatLng userPosition = new LatLng(userLastKnowLocation.getLatitude(), userLastKnowLocation.getLongitude());
                                        // Check that the event is within the search radius of the current user
                                        if (LocationFilter.eventWithinRange(userPosition, eventPosition, mSearchRadius)) {
                                            ALL_EVENTS.add(event);
                                            ALL_EVENTS_MAP.put(event.getUid(), event);
                                            mAllEventsUpdated = true;
                                        }
                                    } else {
                                        Log.e(TAG, context.getString(R.string.location_not_found_error));
                                        Toast.makeText(context, R.string.could_not_find_location, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                            if (mAllEventsUpdated) {
                                for (UserEventsListener listener : mListeners) {
                                    listener.userEventsUpdated();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e(TAG, databaseError.toString());
                        }
                    });

                    // Listen for changes in the users events attending database
                    mEventsAttendingListener = mDatabaseManager.getEventsAttendingDatabaseReference().addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            mEventsAttendingUpdated = false;
                            // Clear the event attending map and list
                            EVENTS_ATTENDING.clear();
                            EVENTS_ATTENDING_MAP.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                mDatabaseManager.getEvent(snapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Event event = dataSnapshot.getValue(Event.class);
                                        // Add the event to the event attending map and list
                                        if (event != null) {
                                            EVENTS_ATTENDING.add(event);
                                            EVENTS_ATTENDING_MAP.put(event.getUid(), event);
                                            mEventsAttendingUpdated = true;
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.e(TAG, databaseError.toString());
                                    }
                                });
                            }
                            if (mEventsAttendingUpdated) {
                                for (UserEventsListener listener : mListeners) {
                                    listener.userEventsUpdated();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e(TAG, databaseError.toString());
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, databaseError.toString());
                }
            });

            // Listen for changes in the users events hosting database
            mEventsHostingListener = mDatabaseManager.getEventsHostingDatabaseReference().addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mEventsHostingUpdated = false;
                    // Clear the event hosting map and list
                    EVENTS_HOSTING.clear();
                    EVENTS_HOSTING_MAP.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        mDatabaseManager.getEvent(snapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Event event = dataSnapshot.getValue(Event.class);
                                // Add the event to the event hosting map and list
                                if (event != null) {
                                    EVENTS_HOSTING.add(event);
                                    EVENTS_HOSTING_MAP.put(event.getUid(), event);
                                    mEventsHostingUpdated = true;
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.e(TAG, databaseError.toString());
                            }
                        });
                    }
                    if (mEventsHostingUpdated) {
                        for (UserEventsListener listener : mListeners) {
                            listener.userEventsUpdated();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, databaseError.toString());
                }
            });
        }
    }

    private void requestLocationUpdates() {
        // Create location update request with the users settings values or default values.
        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    mLocationTimeUpdateInterval,
                    mLocationDistanceUpdateIntervalName,
                    mLocationListener);
        } catch (SecurityException ex) {
            Log.e(TAG, ex.toString());
        }
    }

    public void clearListeners() {
        mDatabaseManager.getAllEventsDatabaseReference().removeEventListener(mAllEventsListener);
        mDatabaseManager.getEventsAttendingDatabaseReference().removeEventListener(mEventsAttendingListener);
        mDatabaseManager.getEventsHostingDatabaseReference().removeEventListener(mEventsHostingListener);
    }

    public void clearEvents() {
        // Clear the list and map so when a user logs out and a new one logs in,
        // the event list is accurate for that user
        ALL_EVENTS.clear();
        ALL_EVENTS_MAP.clear();

        EVENTS_ATTENDING.clear();
        EVENTS_ATTENDING_MAP.clear();

        EVENTS_HOSTING.clear();
        EVENTS_HOSTING_MAP.clear();
    }
}
