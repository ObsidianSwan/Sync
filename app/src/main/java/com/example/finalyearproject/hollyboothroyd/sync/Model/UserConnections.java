package com.example.finalyearproject.hollyboothroyd.sync.Model;

import android.support.annotation.NonNull;

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

    private DatabaseManager mDatabaseManager;

    public static final List<Person> CONNECTION_ITEMS = new ArrayList<Person>();
    public static final Map<String, Person> CONNECTION_ITEM_MAP = new HashMap<String, Person>();

    public static final List<Person> CONNECTION_REQUEST_ITEMS = new ArrayList<Person>();
    public static final Map<String, Person> CONNECTION_REQUEST_ITEM_MAP = new HashMap<String, Person>();

    private ValueEventListener mUserConnectionsListener;
    private ValueEventListener mUserConnectionRequestListener;


    // TODO: Convert into singleton
    public UserConnections() {

        mDatabaseManager = new DatabaseManager();

        mUserConnectionsListener = mDatabaseManager.getUserConnectionsDatabaseReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                CONNECTION_ITEMS.clear();
                CONNECTION_ITEM_MAP.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    final String connectionId = snapshot.getKey();
                    if(!CONNECTION_ITEM_MAP.containsKey(connectionId)) {
                        mDatabaseManager.getPersonReference(connectionId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Person person = dataSnapshot.getValue(Person.class);
                                CONNECTION_ITEMS.add(person);
                                CONNECTION_ITEM_MAP.put(person.getUserId(), person);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mUserConnectionRequestListener = mDatabaseManager.getUserConnectionRequestsDatabaseReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                CONNECTION_REQUEST_ITEMS.clear();
                CONNECTION_REQUEST_ITEM_MAP.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    final String connectionId = snapshot.getKey();
                    if(!CONNECTION_REQUEST_ITEM_MAP.containsKey(connectionId)) {
                        mDatabaseManager.getPersonReference(connectionId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Person person = dataSnapshot.getValue(Person.class);
                                CONNECTION_REQUEST_ITEMS.add(person);
                                CONNECTION_REQUEST_ITEM_MAP.put(person.getUserId(), person);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void clearListeners(){
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
