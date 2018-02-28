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

    public static final List<Connection> CONNECTION_DELETION_ITEMS = new ArrayList<Connection>();
    public static final Map<String, Connection> CONNECTION_DELETION_ITEM_MAP = new HashMap<String, Connection>();

    public static final List<Person> CONNECTION_ITEMS = new ArrayList<Person>();
    public static final Map<String, Connection> CONNECTION_ITEM_MAP = new HashMap<String, Connection>();

    public static final List<Connection> CONNECTION_REQUEST_ITEMS = new ArrayList<Connection>();
    public static final Map<String, Connection> CONNECTION_REQUEST_ITEM_MAP = new HashMap<String, Connection>();

    private ValueEventListener mDeletedUserConnectionsListener;
    private ValueEventListener mUserConnectionsListener;
    private ValueEventListener mUserConnectionRequestListener;


    // TODO: Convert into singleton
    public UserConnections(){

        mDatabaseManager = new DatabaseManager();


        mDeletedUserConnectionsListener = mDatabaseManager.getDeletedUserConnections().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                CONNECTION_DELETION_ITEMS.clear();
                CONNECTION_DELETION_ITEM_MAP.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Connection deletedConnection = snapshot.getValue(Connection.class);
                    CONNECTION_DELETION_ITEMS.add(deletedConnection);
                    CONNECTION_DELETION_ITEM_MAP.put(deletedConnection.getUserId(), deletedConnection);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mUserConnectionsListener = mDatabaseManager.getUserConnectionsDatabaseReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                CONNECTION_ITEMS.clear();
                CONNECTION_ITEM_MAP.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    final Connection connection = snapshot.getValue(Connection.class);
                    // Check if the connection has been deleted by another user.
                    // If it has (ie. it is in the CONNECTION_DELETION_ITEMS) then remove it from the connection and deleted connection databases
                    // and do not add it to the CONNECTION_ITEM_MAP
                    if(CONNECTION_DELETION_ITEM_MAP.containsKey(connection.getUserId())){
                        // Remove the connection from the users connection database
                        String deletionRef = CONNECTION_DELETION_ITEM_MAP.get(connection.getUserId()).getConnectionDbRef();
                        mDatabaseManager.deleteCurrentUserDeletedConnection(deletionRef).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    mDatabaseManager.deleteCurrentUserConnection(connection.getConnectionDbRef()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            int i = 0;
                                            i++;
                                        }
                                    });
                                } else {

                                }
                            }
                        });
                    } else {
                        if(!CONNECTION_ITEM_MAP.containsKey(connection.getUserId())) {
                            CONNECTION_ITEM_MAP.put(connection.getUserId(), connection);
                            mDatabaseManager.getPersonReference(connection.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Person person = dataSnapshot.getValue(Person.class);
                                    CONNECTION_ITEMS.add(person);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
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
                    Connection connection = snapshot.getValue(Connection.class);
                    // Check if the connection has been approved.
                    // If it has (ie. it is in the CONNECTION_ITEM_MAP) then remove it from the connection request database
                    // and do not add it to the CONNECTION_REQUEST_ITEM_MAP
                    if(CONNECTION_ITEM_MAP.containsKey(connection.getUserId())){
                        mDatabaseManager.deleteCurrentUserConnectionRequest(connection.getConnectionDbRef());
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

    public void clearListeners(){
        mDatabaseManager.getUserConnectionsDatabaseReference().removeEventListener(mDeletedUserConnectionsListener);
        mDatabaseManager.getUserConnectionsDatabaseReference().removeEventListener(mUserConnectionsListener);
        mDatabaseManager.getUserConnectionRequestsDatabaseReference().removeEventListener(mUserConnectionRequestListener);
    }

    public void clearConnections() {
        // Clear the list and map so when a user logs out and a new one logs in,
        // the connections list is accurate for that user
        CONNECTION_DELETION_ITEMS.clear();
        CONNECTION_DELETION_ITEM_MAP.clear();

        CONNECTION_ITEMS.clear();
        CONNECTION_ITEM_MAP.clear();

        CONNECTION_REQUEST_ITEMS.clear();
        CONNECTION_REQUEST_ITEM_MAP.clear();
    }
}
