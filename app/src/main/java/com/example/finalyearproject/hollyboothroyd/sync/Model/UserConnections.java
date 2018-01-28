package com.example.finalyearproject.hollyboothroyd.sync.Model;

import com.example.finalyearproject.hollyboothroyd.sync.Model.Person;
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
 * Helper class for providing content for ConnectionFragment
 * Initialized in CoreActivity so local connections map is updated when new connections in the GMapFragment are added
 */
public class UserConnections {

    public DatabaseManager mDatabaseManager;

    public static final List<Person> ITEMS = new ArrayList<Person>();
    public static final Map<String, Person> ITEM_MAP = new HashMap<String, Person>();

    // TODO: Convert into singleton
    public UserConnections(){

        mDatabaseManager = new DatabaseManager();

        mDatabaseManager.getUserConnectionsDatabaseReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Person connection = snapshot.getValue(Person.class);
                    ITEMS.add(connection);
                    ITEM_MAP.put(connection.getUserId(), connection);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
