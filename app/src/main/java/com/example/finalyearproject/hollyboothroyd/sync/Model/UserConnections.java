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

    public static final List<Connection> CONNECTION_REQUEST_ITEMS = new ArrayList<Connection>();
    public static final Map<String, Connection> CONNECTION_REQUEST_ITEM_MAP = new HashMap<String, Connection>();

    // TODO: Convert into singleton
    public UserConnections(){

        mDatabaseManager = new DatabaseManager();

        mDatabaseManager.getUserConnectionsDatabaseReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                CONNECTION_ITEMS.clear();
                CONNECTION_ITEM_MAP.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Connection connection = snapshot.getValue(Connection.class);
                    mDatabaseManager.getPersonReference(connection.getUserId()).addValueEventListener(new ValueEventListener() {
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

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseManager.getUserConnectionRequestsDatabaseReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                CONNECTION_REQUEST_ITEMS.clear();
                CONNECTION_REQUEST_ITEM_MAP.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Connection connection = snapshot.getValue(Connection.class);
                    // Check if the connection has been approved.
                    // If it has (ie. it is in the CONNECTION_ITEM_MAP) then remove it from the connection request database
                    // and do not add it to the CONNECTION_REQUEST_ITEM_MAP
                    if(CONNECTION_ITEM_MAP.containsKey(connection.getUserId())){
                        mDatabaseManager.deleteUserConnectionRequest(connection.getConnectionDbRef(), connection.getUserId());
                    } else {
                        CONNECTION_REQUEST_ITEMS.add(connection);
                        CONNECTION_REQUEST_ITEM_MAP.put(connection.getUserId(), connection);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
}
