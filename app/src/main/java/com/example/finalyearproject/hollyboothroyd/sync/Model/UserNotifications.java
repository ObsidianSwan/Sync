package com.example.finalyearproject.hollyboothroyd.sync.Model;

/**
 * Created by hollyboothroyd on 2/25/2018.
 */

import android.widget.Switch;

import com.example.finalyearproject.hollyboothroyd.sync.Services.DatabaseManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing content for NotificationFragment
 */
public class UserNotifications {

    private DatabaseManager mDatabaseManager;

    public static final List<NotificationBase> ITEMS = new ArrayList<NotificationBase>();
    public static final Map<String, NotificationBase> ITEM_MAP = new HashMap<String, NotificationBase>();

    // TODO: Convert into singleton
    public UserNotifications(){
        mDatabaseManager = new DatabaseManager();

        mDatabaseManager.getNotifications().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ITEMS.clear();
                ITEM_MAP.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    final Notification notification = snapshot.getValue(Notification.class);
                    switch (notification.getType()){
                        case CONNECTION_REQUEST:
                            mDatabaseManager.getPeopleDatabaseReference().child(notification.getItemId()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Person person = dataSnapshot.getValue(Person.class);
                                    ConnectionRequestNotification connectionRequest = new ConnectionRequestNotification(notification.getDbRefKey(), person, notification.getTimeStampDate());
                                    ITEMS.add(connectionRequest);
                                    ITEM_MAP.put(connectionRequest.getId(), connectionRequest);
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

    public void clearNotifications() {
        // Clear the list and map so when a user logs out and a new one logs in,
        // the notifications list is accurate for that user
        ITEMS.clear();
        ITEM_MAP.clear();
    }
}
