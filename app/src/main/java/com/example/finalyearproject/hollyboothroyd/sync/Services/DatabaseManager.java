package com.example.finalyearproject.hollyboothroyd.sync.Services;

import android.app.Activity;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.EditText;

import com.example.finalyearproject.hollyboothroyd.sync.Model.Event;
import com.example.finalyearproject.hollyboothroyd.sync.Model.Person;
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
    private DatabaseReference mConnectionsDatabaseReference;
    private DatabaseReference mEventDatabaseReference;
    private AccountManager mAccountManager;

    private boolean isAddPersonSuccessful = false;

    public DatabaseManager() {
        mDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();
        // TODO: Make these constants
        mPeopleDatabaseReference = mDatabase.getReference().child("people");
        mConnectionsDatabaseReference = mDatabase.getReference().child("connections");
        mEventDatabaseReference = mDatabase.getReference().child("event");

        mAccountManager = new AccountManager();

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
        // TODO: Constant
        final StorageReference filePath = mStorage.child("Person_Images").child(imageUri.getLastPathSegment());
        return filePath.putFile(imageUri);
    }

    public Task<Void> addPerson(String firstName, String lastName, String position, String company, String industry, String downloadUrl, String userId) {
        return mPeopleDatabaseReference.child(userId).setValue(new Person(firstName, lastName, position, company, industry, downloadUrl, userId));
    }

    public void updateCurrentUserLocation(LatLng userLocation){
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

    public Person getCurrentUserPerson(){
        final Person currentPerson = new Person();
        String userId = mAccountManager.getCurrentUser().getUid();
        mPeopleDatabaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue(Person.class) != null){
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
    }

    // Connections

    public DatabaseReference getUserPeopleDatabaseReference() { return  mPeopleDatabaseReference.child(mAccountManager.getCurrentUser().getUid()); }

    public DatabaseReference getUserConnectionsDatabaseReference() { return mConnectionsDatabaseReference.child(mAccountManager.getCurrentUser().getUid()); }

    public Task<Void> addNewConnection(final String userId, final Person connection){
        DatabaseReference newConnection = mConnectionsDatabaseReference.child(userId).push();
        return newConnection.setValue(connection);
    }

    // Events

    // TODO: Add distance check
    public DatabaseReference getAllEventsDatabaseReference() { return mEventDatabaseReference; }

    public DatabaseReference getEventsAttendingDatabaseReference() { return mPeopleDatabaseReference.child(mAccountManager.getCurrentUser().getUid()).child("events_attending"); }

    public DatabaseReference getEventsHostingDatabaseReference() { return mPeopleDatabaseReference.child(mAccountManager.getCurrentUser().getUid()).child("events_created"); }

    public DatabaseReference getEvent(String uId) { return mEventDatabaseReference.child(uId); }

    public DatabaseReference getNewEventReference(){
        return mEventDatabaseReference.push();
    }

    public Task<Void> addEvent(DatabaseReference newEventReference, Event event) {
        return newEventReference.setValue(event);
    }

    public Task<Void> addEventCreator(String eventKey, String creatorUid){
        return mPeopleDatabaseReference.child(creatorUid).child("events_created").push().setValue(eventKey);
    }

    public Task<UploadTask.TaskSnapshot> uploadEventImage(Uri imageUri) {
        // TODO: Constant
        final StorageReference filePath = mStorage.child("Event_Images").child(imageUri.getLastPathSegment());
        return filePath.putFile(imageUri);
    }

    // Reason why I saved both the user and the event was to save time for look up
    public Task<Void> attendNewEvent(final Event event){
        String userId = mAccountManager.getCurrentUser().getUid();

        DatabaseReference eventAttending = mEventDatabaseReference.child(event.getUid()).child("event_attendees").push();
        eventAttending.setValue(userId);

        return mPeopleDatabaseReference.child(userId).child("events_attending").push().setValue(event.getUid());
    }
}
