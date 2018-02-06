package com.example.finalyearproject.hollyboothroyd.sync.Model;

import com.example.finalyearproject.hollyboothroyd.sync.Services.DatabaseManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hollyboothroyd on 1/28/2018.
 */

public class UserEvents {

    private DatabaseManager mDatabaseManager;

    public static final List<Event> ALL_EVENTS = new ArrayList<Event>();
    public static final Map<String, Event> ALL_EVENTS_MAP = new HashMap<String, Event>();

    public static final List<Event> EVENTS_ATTENDING = new ArrayList<Event>();
    public static final Map<String, Event> EVENTS_ATTENDING_MAP = new HashMap<String, Event>();

    public static final List<Event> EVENTS_HOSTING = new ArrayList<Event>();
    public static final Map<String, Event> EVENTS_HOSTING_MAP = new HashMap<String, Event>();

    // TODO: Convert into singleton
    public UserEvents() {

        mDatabaseManager = new DatabaseManager();

        mDatabaseManager.getAllEventsDatabaseReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ALL_EVENTS.clear();
                ALL_EVENTS_MAP.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Event event = snapshot.getValue(Event.class);
                    ALL_EVENTS.add(event);
                    ALL_EVENTS_MAP.put(event.getUid(), event);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseManager.getEventsAttendingDatabaseReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                EVENTS_ATTENDING.clear();
                EVENTS_ATTENDING_MAP.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    mDatabaseManager.getEvent(snapshot.getValue(String.class)).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Event event = dataSnapshot.getValue(Event.class);
                            EVENTS_ATTENDING.add(event);
                            EVENTS_ATTENDING_MAP.put(event.getUid(), event);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseManager.getEventsHostingDatabaseReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                EVENTS_HOSTING.clear();
                EVENTS_HOSTING_MAP.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    mDatabaseManager.getEvent(snapshot.getValue(String.class)).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Event event = dataSnapshot.getValue(Event.class);
                            EVENTS_HOSTING.add(event);
                            EVENTS_HOSTING_MAP.put(event.getUid(), event);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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
