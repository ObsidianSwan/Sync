package com.example.finalyearproject.hollyboothroyd.sync.Model;

/**
 * Created by hollyboothroyd on 2/25/2018.
 */

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Switch;

import com.example.finalyearproject.hollyboothroyd.sync.R;
import com.example.finalyearproject.hollyboothroyd.sync.Services.DatabaseManager;
import com.example.finalyearproject.hollyboothroyd.sync.Utils.NotificationType;
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
 * Helper class for providing content for NotificationFragment
 */
public class UserNotifications {
    private static final String TAG = "UserNotifications";

    private DatabaseManager mDatabaseManager;

    public static final List<NotificationBase> ITEMS = new ArrayList<NotificationBase>();
    public static final Map<String, NotificationBase> CONNECTION_REQUEST_ITEMS_MAP = new HashMap<String, NotificationBase>();

    private ValueEventListener mUserNotificationsListener;

    public UserNotifications(final Context context) {
        mDatabaseManager = new DatabaseManager();

        // Listen for changes in the users notification database
        mUserNotificationsListener = mDatabaseManager.getNotifications().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ITEMS.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final Notification notification = snapshot.getValue(Notification.class);
                    if (notification != null) {

                        // Sort the notifications by type
                        switch (notification.getType()) {
                            case CONNECTION_REQUEST:
                                mDatabaseManager.getPeopleDatabaseReference().child(notification.getItemId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Person person = dataSnapshot.getValue(Person.class);
                                        if (person != null) {
                                            // Create a notification with the necessary information
                                            NotificationBase connectionRequest = new NotificationBase(notification.getDbRefKey(), person.getUserId(), person.getFirstName() + " " + person.getLastName() + ": " + person.getPosition(),
                                                    person.getImageId(), NotificationType.CONNECTION_REQUEST, notification.getTimeStampDate());
                                            // Add the notification to the list to be displayed in the notification fragment
                                            ITEMS.add(connectionRequest);
                                            // Add the connection request item to the map for use in GMaps fragment
                                            CONNECTION_REQUEST_ITEMS_MAP.put(connectionRequest.getId(), connectionRequest);
                                        } else {
                                            // If the notification sender cannot be found, delete the notification
                                            mDatabaseManager.deleteUserNotification(notification.getDbRefKey()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.i(TAG, context.getString(R.string.delete_notification_successful));
                                                    } else {
                                                        Log.e(TAG, context.getString(R.string.delete_notification_error));
                                                    }
                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.e(TAG, context.getString(R.string.location_not_found_error));
                                    }
                                });
                                break;
                            case PROFILE_VIEW:
                                mDatabaseManager.getPeopleDatabaseReference().child(notification.getItemId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Person person = dataSnapshot.getValue(Person.class);
                                        // Create a notification with the necessary information
                                        if (person != null) {
                                            NotificationBase profileView = new NotificationBase(notification.getDbRefKey(), person.getUserId(), person.getFirstName() + " " + person.getLastName() + ": " + person.getPosition(),
                                                    person.getImageId(), NotificationType.PROFILE_VIEW, notification.getTimeStampDate());
                                            // Add the notification to the list to be displayed in the notification fragment
                                            ITEMS.add(profileView);
                                        } else {
                                            // If the notification sender cannot be found, delete the notification
                                            mDatabaseManager.deleteUserNotification(notification.getDbRefKey()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.i(TAG, context.getString(R.string.delete_notification_successful));
                                                    } else {
                                                        Log.e(TAG, context.getString(R.string.delete_notification_error));
                                                    }
                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.e(TAG, context.getString(R.string.location_not_found_error));
                                    }
                                });
                                break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, context.getString(R.string.location_not_found_error));
            }
        });
    }

    public void clearListeners() {
        // Clear the notification listeners
        mDatabaseManager.getNotifications().removeEventListener(mUserNotificationsListener);
    }

    public void clearNotifications() {
        // Clear the list and map so when a user logs out and a new one logs in,
        // the notifications list is accurate for that user
        ITEMS.clear();
        CONNECTION_REQUEST_ITEMS_MAP.clear();
    }
}
