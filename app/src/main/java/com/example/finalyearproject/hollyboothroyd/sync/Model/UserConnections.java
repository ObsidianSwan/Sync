package com.example.finalyearproject.hollyboothroyd.sync.Model;

import android.support.annotation.NonNull;
import android.util.Log;

import com.example.finalyearproject.hollyboothroyd.sync.Services.DatabaseManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing content for ConnectionFragment
 * Initialized in CoreActivity so local connections map is updated when new connections in the GMapFragment are added
 */
public class UserConnections {
    private static final String TAG = "UserConnections";

    private DatabaseManager mDatabaseManager;

    public static final List<Person> CONNECTION_ITEMS = new ArrayList<Person>();
    public static final Map<String, Person> CONNECTION_ITEM_MAP = new HashMap<String, Person>();

    public static final List<Person> CONNECTION_REQUEST_ITEMS = new ArrayList<Person>();
    public static final Map<String, Person> CONNECTION_REQUEST_ITEM_MAP = new HashMap<String, Person>();

    private ValueEventListener mUserConnectionsListener;
    private ValueEventListener mUserConnectionRequestListener;

    public UserConnections() {

        mDatabaseManager = new DatabaseManager();

        // Listen for changes in the user connections database
        mUserConnectionsListener = mDatabaseManager.getUserConnectionsDatabaseReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Clear the connections items and map
                CONNECTION_ITEMS.clear();
                CONNECTION_ITEM_MAP.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final String connectionId = snapshot.getKey();
                    if (!CONNECTION_ITEM_MAP.containsKey(connectionId)) {
                        // Get the person associated with the connection id in the users database
                        mDatabaseManager.getPersonReference(connectionId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Person person = dataSnapshot.getValue(Person.class);
                                // Add person to connection item and map
                                CONNECTION_ITEMS.add(person);
                                CONNECTION_ITEM_MAP.put(person.getUserId(), person);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.e(TAG, databaseError.toString());
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.toString());
            }
        });

        // Listen for the users connection request database
        mUserConnectionRequestListener = mDatabaseManager.getUserConnectionRequestsDatabaseReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Clear the connection request item list and map
                CONNECTION_REQUEST_ITEMS.clear();
                CONNECTION_REQUEST_ITEM_MAP.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final String connectionId = snapshot.getKey();
                    if (!CONNECTION_REQUEST_ITEM_MAP.containsKey(connectionId))
                        // Get the person associated with the connection request id in the users database
                        mDatabaseManager.getPersonReference(connectionId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Person person = dataSnapshot.getValue(Person.class);
                                // Add person to connection request item and map
                                CONNECTION_REQUEST_ITEMS.add(person);
                                CONNECTION_REQUEST_ITEM_MAP.put(person.getUserId(), person);
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
        // Clear listeners
        mDatabaseManager.getUserConnectionsDatabaseReference().removeEventListener(mUserConnectionsListener);
        mDatabaseManager.getUserConnectionRequestsDatabaseReference().removeEventListener(mUserConnectionRequestListener);
    }

    public void clearConnections() {
        // Clear the list and map so when a user logs out and a new one logs in,
        // the connections list is accurate for that user

        CONNECTION_ITEMS.clear();
        CONNECTION_ITEM_MAP.clear();

        CONNECTION_REQUEST_ITEMS.clear();
        CONNECTION_REQUEST_ITEM_MAP.clear();
    }
}
