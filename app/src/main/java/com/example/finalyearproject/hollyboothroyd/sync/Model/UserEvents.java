package com.example.finalyearproject.hollyboothroyd.sync.Model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by hollyboothroyd on 1/28/2018.
 */

public class UserEvents {
    private static final String TAG = "UserEvents";

    private DatabaseManager mDatabaseManager;
    private LocationManager mLocationManager;

    public static final List<Event> ALL_EVENTS = new ArrayList<Event>();
    public static final Map<String, Event> ALL_EVENTS_MAP = new HashMap<String, Event>();

    public static final List<Event> EVENTS_ATTENDING = new ArrayList<Event>();
    public static final Map<String, Event> EVENTS_ATTENDING_MAP = new HashMap<String, Event>();

    public static final List<Event> EVENTS_HOSTING = new ArrayList<Event>();
    public static final Map<String, Event> EVENTS_HOSTING_MAP = new HashMap<String, Event>();

    private ValueEventListener mAllEventsListener;
    private ValueEventListener mEventsAttendingListener;
    private ValueEventListener mEventsHostingListener;

    private int mSearchRadius = Constants.geofenceRadiusDefault;

    // It is okay to suppress the missing permissions because UserEvents is only
    // instantiated after permissions have been granted
    @SuppressLint("MissingPermission")
    // TODO fix this
    public UserEvents(final Context context) {

        mDatabaseManager = new DatabaseManager();
        mLocationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

        // Retrieve the search radius to determine if events were local
        mDatabaseManager.getUserSettings(Constants.searchRadiusName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mSearchRadius = Constants.geofenceRadiusDefault;
                if (dataSnapshot.getValue(Integer.class) != null) {
                    mSearchRadius = dataSnapshot.getValue(Integer.class);
                }

                // Listen for changes in the events database
                mAllEventsListener = mDatabaseManager.getAllEventsDatabaseReference().addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ALL_EVENTS.clear();
                        ALL_EVENTS_MAP.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Event event = snapshot.getValue(Event.class);
                            if (event != null) {
                                LatLng eventPosition = new LatLng(event.getLatitude(), event.getLongitude());
                                // Get the users last known location
                                Location userLastKnowLocation = Util.getLastKnownLocation(mLocationManager);
                                // If the users last location can be found, populate the map with local events
                                if(userLastKnowLocation != null) {
                                    LatLng userPosition = new LatLng(userLastKnowLocation.getLatitude(), userLastKnowLocation.getLongitude());
                                    // Check that the event is within the search radius of the current user
                                    if (LocationFilter.eventWithinRange(userPosition, eventPosition, mSearchRadius)) {
                                        ALL_EVENTS.add(event);
                                        ALL_EVENTS_MAP.put(event.getUid(), event);
                                    }
                                } else {
                                    Log.e(TAG, context.getString(R.string.location_not_found_error));
                                    Toast.makeText(context, R.string.could_not_find_location, Toast.LENGTH_SHORT).show();
                                }
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
                        // Clear the event attending map and list
                        EVENTS_ATTENDING.clear();
                        EVENTS_ATTENDING_MAP.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            mDatabaseManager.getEvent(snapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Event event = dataSnapshot.getValue(Event.class);
                                    // Add the event to the event attending map and list
                                    EVENTS_ATTENDING.add(event);
                                    EVENTS_ATTENDING_MAP.put(event.getUid(), event);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.e(TAG, databaseError.toString());
                                }
                            });
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
                // Clear the event hosting map and list
                EVENTS_HOSTING.clear();
                EVENTS_HOSTING_MAP.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    mDatabaseManager.getEvent(snapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Event event = dataSnapshot.getValue(Event.class);
                            // Add the event to the event hosting map and list
                            EVENTS_HOSTING.add(event);
                            EVENTS_HOSTING_MAP.put(event.getUid(), event);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e(TAG, databaseError.toString());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.toString());
            }
        });
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
