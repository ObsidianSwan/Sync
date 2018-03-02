package com.example.finalyearproject.hollyboothroyd.sync.Model;

/**
 * Created by hollyboothroyd on 2/25/2018.
 */

import android.widget.Switch;

import com.example.finalyearproject.hollyboothroyd.sync.Services.DatabaseManager;
import com.example.finalyearproject.hollyboothroyd.sync.Utils.NotificationType;
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
    //public static final Map<String, NotificationBase> ITEM_MAP = new HashMap<String, NotificationBase>();

    // Different lists are needed to make sure the correct item is being retrieved when there are multiple
    // types of notifications that may have the same key
    public static final Map<String, NotificationBase> CONNECTION_REQUEST_ITEMS_MAP = new HashMap<String, NotificationBase>();

    private ValueEventListener mUserNotificationsListener;

    // TODO: Convert into singleton
    public UserNotifications(){
        mDatabaseManager = new DatabaseManager();

        mUserNotificationsListener = mDatabaseManager.getNotifications().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ITEMS.clear();
                //ITEM_MAP.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    final Notification notification = snapshot.getValue(Notification.class);
                    if(notification != null) {
                        switch (notification.getType()) {
                            case CONNECTION_REQUEST:
                                mDatabaseManager.getPeopleDatabaseReference().child(notification.getItemId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Person person = dataSnapshot.getValue(Person.class);
                                        NotificationBase connectionRequest = new NotificationBase(notification.getDbRefKey(), person.getUserId(), person.getFirstName() + " " + person.getLastName() + ": " + person.getPosition(),
                                                person.getImageId(), NotificationType.CONNECTION_REQUEST, notification.getTimeStampDate());
                                        ITEMS.add(connectionRequest);
                                        CONNECTION_REQUEST_ITEMS_MAP.put(connectionRequest.getId(), connectionRequest);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                                break;
                            case PROFILE_VIEW:
                                mDatabaseManager.getPeopleDatabaseReference().child(notification.getItemId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Person person = dataSnapshot.getValue(Person.class);
                                        NotificationBase profileView = new NotificationBase(notification.getDbRefKey(), person.getUserId(), person.getFirstName() + " " + person.getLastName() + ": " + person.getPosition(),
                                                person.getImageId(), NotificationType.PROFILE_VIEW, notification.getTimeStampDate());
                                        ITEMS.add(profileView);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                                break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void clearListeners(){
        mDatabaseManager.getNotifications().removeEventListener(mUserNotificationsListener);
    }

    public void clearNotifications() {
        // Clear the list and map so when a user logs out and a new one logs in,
        // the notifications list is accurate for that user
        ITEMS.clear();
        CONNECTION_REQUEST_ITEMS_MAP.clear();
    }
}
