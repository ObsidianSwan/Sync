package com.example.finalyearproject.hollyboothroyd.sync.Services;

import android.app.Activity;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.EditText;

import com.example.finalyearproject.hollyboothroyd.sync.Model.Connection;
import com.example.finalyearproject.hollyboothroyd.sync.Model.Event;
import com.example.finalyearproject.hollyboothroyd.sync.Model.Notification;
import com.example.finalyearproject.hollyboothroyd.sync.Model.Person;
import com.example.finalyearproject.hollyboothroyd.sync.Utils.Constants;
import com.example.finalyearproject.hollyboothroyd.sync.Utils.NotificationType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Created by hollyboothroyd on 11/11/2017.
 */

public class DatabaseManager {

    private FirebaseDatabase mDatabase;
    private StorageReference mStorage;
    private DatabaseReference mPeopleDatabaseReference;
    private DatabaseReference mUserNotificationDatabaseReference;
    private DatabaseReference mConnectionsDatabaseReference;

    private DatabaseReference mEventDatabaseReference;
    private AccountManager mAccountManager;

    public DatabaseManager() {
        mDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();
        mPeopleDatabaseReference = mDatabase.getReference().child(Constants.peopleDatabaseRefName);
        mConnectionsDatabaseReference = mDatabase.getReference().child(Constants.connectionDatabaseRefName);

        mEventDatabaseReference = mDatabase.getReference().child(Constants.eventDatabaseRefName);

        mAccountManager = new AccountManager();
        //TODO: when DMMan and AccountMan is put into utils return this to be here.
        //mUserNotificationDatabaseReference = mPeopleDatabaseReference.child(mAccountManager.getCurrentUser().getUid()).child(Constants.userNotificationDatabaseRefName);

        mPeopleDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // Users

    public DatabaseReference getPeopleDatabaseReference() {
        return mPeopleDatabaseReference;
    }

    public Task<UploadTask.TaskSnapshot> uploadPersonImage(Uri imageUri) {
        final StorageReference filePath = mStorage.child(Constants.personImgStorageName).child(imageUri.getLastPathSegment());
        return filePath.putFile(imageUri);
    }

    public Task<Void> deletePersonImage(String oldImageUri) {
        StorageReference oldImageRef = mStorage.getStorage().getReferenceFromUrl(oldImageUri);
        return oldImageRef.delete();
    }

    public Task<Void> addPerson(Person person) {
        return mPeopleDatabaseReference.child(person.getUserId()).setValue(person);
    }

    public void updateCurrentUserLocation(LatLng userLocation) {
        // TODO: Put hashmap conversion in utils
        HashMap<String, Object> personLocationHash = new HashMap<>();
        personLocationHash.put("longitude", userLocation.longitude);
        personLocationHash.put("latitude", userLocation.latitude);

        String userId = mAccountManager.getCurrentUser().getUid();
        DatabaseReference currentPerson = mPeopleDatabaseReference.child(userId);
        currentPerson.updateChildren(personLocationHash, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

            }
        });
    }

    public DatabaseReference getPersonReference(String personId){
        return mPeopleDatabaseReference.child(personId);
    }


/*    public Person getCurrentUserPerson() {
        final Person currentPerson = new Person();
        String userId = mAccountManager.getCurrentUser().getUid();
        mPeopleDatabaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(Person.class) != null) {
                    currentPerson.setFirstName(dataSnapshot.getValue(Person.class).getFirstName());
                    currentPerson.setLastName(dataSnapshot.getValue(Person.class).getLastName());
                    currentPerson.setPosition(dataSnapshot.getValue(Person.class).getPosition());
                    currentPerson.setCompany(dataSnapshot.getValue(Person.class).getCompany());
                    currentPerson.setIndustry(dataSnapshot.getValue(Person.class).getIndustry());
                    currentPerson.setLatitude(dataSnapshot.getValue(Person.class).getLatitude());
                    currentPerson.setLongitude(dataSnapshot.getValue(Person.class).getLongitude());
                    currentPerson.setImageId(dataSnapshot.getValue(Person.class).getImageId());
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return currentPerson;
    }*/

    public DatabaseReference getUserPeopleDatabaseReference() {
        return mPeopleDatabaseReference.child(mAccountManager.getCurrentUser().getUid());
    }

    // Connections

    public DatabaseReference getUserConnectionsDatabaseReference() {
        return mPeopleDatabaseReference.child(mAccountManager.getCurrentUser().getUid()).child(Constants.connectionDatabaseRefName);
    }

    public Task<Void> deleteUserConnectionRequestNotification(String notificationId) {
        return mPeopleDatabaseReference.child(mAccountManager.getCurrentUser().getUid()).child(Constants.userNotificationDatabaseRefName).child(notificationId).setValue(null);
    }

    public DatabaseReference getNewConnectionReference(String personId) {
        return mPeopleDatabaseReference.child(personId).child(Constants.connectionDatabaseRefName).push();
    }

    public Task<Void> addUserConnection(DatabaseReference connectionReference, Connection connection){
        return connectionReference.setValue(connection);
    }

    public DatabaseReference getUserConnectionRequestsDatabaseReference() {
        return mPeopleDatabaseReference.child(mAccountManager.getCurrentUser().getUid()).child(Constants.connectionRequestsDatabaseRefName);
    }

    public Task<Void> deleteUserConnectionRequest(String connectionId, String personId) {
        return mPeopleDatabaseReference.child(personId).child(Constants.connectionRequestsDatabaseRefName).child(connectionId).setValue(null);
    }

    public DatabaseReference getNewConnectionRequestReference() {
        return mPeopleDatabaseReference.child(mAccountManager.getCurrentUser().getUid()).child(Constants.connectionRequestsDatabaseRefName).push();
    }

    public Task<Void> addUserConnectionRequest(DatabaseReference connectionRequestReference, Connection connection){
        return connectionRequestReference.setValue(connection);
    }

    // Notifications

    public DatabaseReference getNewNotifcationReference(String personId) {
        return mPeopleDatabaseReference.child(personId).child(Constants.userNotificationDatabaseRefName).push();
    }

    public Task<Void> sendNotification(DatabaseReference notificationReference, Notification notification) {
        return notificationReference.setValue(notification);
    }

    public DatabaseReference getNotifications() {
        return mPeopleDatabaseReference.child(mAccountManager.getCurrentUser().getUid()).child(Constants.userNotificationDatabaseRefName);
    }

    // Events

    // TODO: Add distance check
    public DatabaseReference getAllEventsDatabaseReference() {
        return mEventDatabaseReference;
    }

    public DatabaseReference getEventsAttendingDatabaseReference() {
        return getUserPeopleDatabaseReference().child(Constants.peopleEventsAttendingDatabaseRefName);
    }

    public DatabaseReference getEventsHostingDatabaseReference() {
        return getUserPeopleDatabaseReference().child(Constants.peopleEventsCreatedDatabaseRefName);
    }

    public DatabaseReference getEvent(String uId) {
        return mEventDatabaseReference.child(uId);
    }

    public DatabaseReference getNewEventReference() {
        return mEventDatabaseReference.push();
    }

    public Task<Void> addEvent(DatabaseReference newEventReference, Event event) {
        return newEventReference.setValue(event);
    }

    public Task<Void> addEventCreator(String eventKey, String creatorUid) {
        return mPeopleDatabaseReference.child(creatorUid).child(Constants.peopleEventsCreatedDatabaseRefName).push().setValue(eventKey);
    }

    public Task<UploadTask.TaskSnapshot> uploadEventImage(Uri imageUri) {
        final StorageReference filePath = mStorage.child(Constants.eventImgStorageName).child(imageUri.getLastPathSegment());
        return filePath.putFile(imageUri);
    }

    public Task<Void> attendNewEvent(final Event event) {
        String userId = mAccountManager.getCurrentUser().getUid();

        DatabaseReference eventAttending = mEventDatabaseReference.child(event.getUid()).child(Constants.eventAttendeesDatabaseRefName).push();
        eventAttending.setValue(userId);

        return mPeopleDatabaseReference.child(userId).child(Constants.peopleEventsAttendingDatabaseRefName).push().setValue(event.getUid());
    }

    // Settings

    public DatabaseReference getUserSettings(String settingName) {
        return getUserPeopleDatabaseReference().child(Constants.userSettingsDatabaseRefName).child(settingName);
    }

    public Task<Void> setUserSettings(String settingName, Object value) {
        return getUserPeopleDatabaseReference().child(Constants.userSettingsDatabaseRefName).child(settingName).setValue(value);
    }
}
