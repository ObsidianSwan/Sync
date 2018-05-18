package com.example.finalyearproject.hollyboothroyd.sync.Services;

import android.net.Uri;
import android.util.Log;

import com.example.finalyearproject.hollyboothroyd.sync.Model.Event;
import com.example.finalyearproject.hollyboothroyd.sync.Model.Notification;
import com.example.finalyearproject.hollyboothroyd.sync.Model.Person;
import com.example.finalyearproject.hollyboothroyd.sync.Utils.Constants;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

/**
 * Created by hollyboothroyd
 * 11/11/2017.
 */

public class DatabaseManager {

    private static final String TAG = "DatabaseManager";

    private StorageReference mStorage;
    private DatabaseReference mPeopleDatabaseReference;
    private DatabaseReference mLocationDatabaseReference;
    private DatabaseReference mEventDatabaseReference;

    private String mCurrentUserId;

    public DatabaseManager() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();
        mPeopleDatabaseReference = database.getReference().child(Constants.peopleDatabaseRefName);
        mLocationDatabaseReference = database.getReference().child(Constants.locationDatabaseRefName);
        mEventDatabaseReference = database.getReference().child(Constants.eventDatabaseRefName);

        AccountManager accountManager = new AccountManager();
        if(accountManager.getCurrentUser() != null) {
            mCurrentUserId = accountManager.getCurrentUser().getUid();
        }
    }

    // Users

    public DatabaseReference getPeopleDatabaseReference() {
        return mPeopleDatabaseReference;
    }

    public Task<UploadTask.TaskSnapshot> uploadPersonImage(Uri imageUri) {
        final StorageReference filePath = mStorage.child(Constants.personImgStorageName).child(imageUri.getLastPathSegment());
        return filePath.putFile(imageUri);
    }

    public Task<Void> deleteImage(String oldImageUri) {
        StorageReference oldImageRef = mStorage.getStorage().getReferenceFromUrl(oldImageUri);
        return oldImageRef.delete();
    }

    public Task<Void> addPerson(Person person) {
        return mPeopleDatabaseReference.child(person.getUserId()).setValue(person);
    }

    public Task<Void> deletePerson(){
        return mPeopleDatabaseReference.child(mCurrentUserId).setValue(null);
    }

    // Location

    public DatabaseReference getLocationDatabaseReference() {
        return mLocationDatabaseReference;
    }

    public DatabaseReference getUserLocationDatabaseReference(String userId) {
        return mLocationDatabaseReference.child(userId);
    }

    public void updateCurrentUserLocation(LatLng userLocation) {
        HashMap<String, Object> personLocationHash = new HashMap<>();
        personLocationHash.put("longitude", userLocation.longitude);
        personLocationHash.put("latitude", userLocation.latitude);

        DatabaseReference currentPersonLocation = mLocationDatabaseReference.child(mCurrentUserId);
        currentPersonLocation.updateChildren(personLocationHash, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(databaseError != null) {
                    Log.e(TAG, databaseError.toString());
                }
            }
        });
    }

    public DatabaseReference getPersonReference(String personId){
        return mPeopleDatabaseReference.child(personId);
    }

    public DatabaseReference getUserPeopleDatabaseReference() {
        return mPeopleDatabaseReference.child(mCurrentUserId);
    }

    public Task<Void> updateUserProfileInformation(String field, String value){
        return mPeopleDatabaseReference.child(mCurrentUserId).child(field).setValue(value);
    }

    // Connections

    public DatabaseReference getUserConnectionsDatabaseReference() {
        return mPeopleDatabaseReference.child(mCurrentUserId).child(Constants.connectionDatabaseRefName);
    }

    public Task<Void> addConnection(String userAId, String userBId){
        return mPeopleDatabaseReference.child(userAId).child(Constants.connectionDatabaseRefName).child(userBId).setValue(true);
    }

    // Connection Requests

    public Task<Void> addUserConnectionRequest(String personId){
        return mPeopleDatabaseReference.child(mCurrentUserId).child(Constants.connectionRequestsDatabaseRefName).child(personId).setValue(true);
    }

    public DatabaseReference getUserConnectionRequestsDatabaseReference() {
        return mPeopleDatabaseReference.child(mCurrentUserId).child(Constants.connectionRequestsDatabaseRefName);
    }

    public Task<Void> deleteUserNotification(String notificationId) {
        return mPeopleDatabaseReference.child(mCurrentUserId).child(Constants.userNotificationDatabaseRefName).child(notificationId).setValue(null);
    }

    public Task<Void> deleteUserConnectionRequest(String personId) {
        return mPeopleDatabaseReference.child(personId).child(Constants.connectionRequestsDatabaseRefName).child(mCurrentUserId).setValue(null);
    }

    // Delete Connection

    public Task<Void> deleteConnection(String userId, String connectionId){
        return mPeopleDatabaseReference.child(userId).child(Constants.connectionDatabaseRefName).child(connectionId).setValue(null);
    }

    // Notifications

    public DatabaseReference getNewNotifcationReference(String personId) {
        return mPeopleDatabaseReference.child(personId).child(Constants.userNotificationDatabaseRefName).push();
    }

    public Task<Void> sendNotification(DatabaseReference notificationReference, Notification notification) {
        return notificationReference.setValue(notification);
    }

    public DatabaseReference getNotifications() {
        return mPeopleDatabaseReference.child(mCurrentUserId).child(Constants.userNotificationDatabaseRefName);
    }

    // Events

    public DatabaseReference getAllEventsDatabaseReference() {
        return mEventDatabaseReference;
    }

    public Task<Void> updateEventDetails(String eventId, String field, String value){
        return mEventDatabaseReference.child(eventId).child(field).setValue(value);
    }

    public DatabaseReference getEventsAttendingDatabaseReference() {
        return getUserPeopleDatabaseReference().child(Constants.peopleEventsAttendingDatabaseRefName);
    }

    public DatabaseReference getEventsHostingDatabaseReference() {
        return getUserPeopleDatabaseReference().child(Constants.peopleEventsCreatedDatabaseRefName);
    }

    public DatabaseReference getUsersAttendingEvent(String eventId){
        return mEventDatabaseReference.child(eventId).child(Constants.eventAttendeesDatabaseRefName);
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
        return mPeopleDatabaseReference.child(creatorUid).child(Constants.peopleEventsCreatedDatabaseRefName).child(eventKey).setValue(true);
    }

    public Task<UploadTask.TaskSnapshot> uploadEventImage(Uri imageUri) {
        final StorageReference filePath = mStorage.child(Constants.eventImgStorageName).child(imageUri.getLastPathSegment());
        return filePath.putFile(imageUri);
    }

    public Task<Void> addUserAttendingEvent(String eventId) {
        return mEventDatabaseReference.child(eventId).child(Constants.eventAttendeesDatabaseRefName).child(mCurrentUserId).setValue(true);
    }

    public Task<Void> addEventAttending(String eventId){
        return mPeopleDatabaseReference.child(mCurrentUserId).child(Constants.peopleEventsAttendingDatabaseRefName).child(eventId).setValue(true);
    }

    // Delete event
    public Task<Void> deleteEventHosting(String eventId){
        return mPeopleDatabaseReference.child(mCurrentUserId).child(Constants.peopleEventsCreatedDatabaseRefName).child(eventId).setValue(null);
    }

    public Task<Void> deleteUserAttendingEvent(String eventId) {
        return mEventDatabaseReference.child(eventId).child(Constants.eventAttendeesDatabaseRefName).child(mCurrentUserId).setValue(null);
    }

    public Task<Void> deleteEventAttending(String eventId, String personId){
        return mPeopleDatabaseReference.child(personId).child(Constants.peopleEventsAttendingDatabaseRefName).child(eventId).setValue(null);
    }

    public Task<Void> deleteEvent(String eventId){
        return mEventDatabaseReference.child(eventId).setValue(null);
    }

    // Settings

    public DatabaseReference getUserSettings(String settingName) {
        return getUserPeopleDatabaseReference().child(Constants.userSettingsDatabaseRefName).child(settingName);
    }

    public Task<Void> setUserSettings(String settingName, Object value) {
        return getUserPeopleDatabaseReference().child(Constants.userSettingsDatabaseRefName).child(settingName).setValue(value);
    }
}
