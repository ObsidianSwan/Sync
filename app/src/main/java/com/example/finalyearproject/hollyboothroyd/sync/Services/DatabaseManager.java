package com.example.finalyearproject.hollyboothroyd.sync.Services;

import android.app.Activity;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executor;

/**
 * Created by hollyboothroyd on 11/11/2017.
 */

public class DatabaseManager {

    private FirebaseDatabase mDatabase;
    private StorageReference mStorage;
    private DatabaseReference mPeopleDatabaseReference;
    private DatabaseReference mConnectionsDatabaseReference;
    private AccountManager mAccountManager;

    private boolean isAddPersonSuccessful = false;

    public DatabaseManager() {
        mDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();
        mPeopleDatabaseReference = mDatabase.getReference().child("people");
        mConnectionsDatabaseReference = mDatabase.getReference().child("connections");

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

    public DatabaseReference getPeopleDatabaseReference() {
        return mPeopleDatabaseReference;
    }


    public Task<UploadTask.TaskSnapshot> uploadPersonImage(Uri imageUri) {
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
                currentPerson.setFirstName(dataSnapshot.getValue(Person.class).getFirstName());
                currentPerson.setLastName(dataSnapshot.getValue(Person.class).getLastName());
                currentPerson.setPosition(dataSnapshot.getValue(Person.class).getPosition());
                currentPerson.setCompany(dataSnapshot.getValue(Person.class).getCompany());
                currentPerson.setIndustry(dataSnapshot.getValue(Person.class).getIndustry());
                currentPerson.setLatitude(dataSnapshot.getValue(Person.class).getLatitude());
                currentPerson.setLongitude(dataSnapshot.getValue(Person.class).getLongitude());
                currentPerson.setImageId(dataSnapshot.getValue(Person.class).getImageId());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return currentPerson;
    }

    public DatabaseReference getUserPeopleDatabaseReference() { return  mPeopleDatabaseReference.child(mAccountManager.getCurrentUser().getUid()); }

    public DatabaseReference getUserConnectionsDatabaseReference() { return mConnectionsDatabaseReference.child(mAccountManager.getCurrentUser().getUid()); }

    public Task<Void> addNewConnection(final String userId, final Person connection){
        DatabaseReference newConnection = mConnectionsDatabaseReference.child(userId).push();
        return newConnection.setValue(connection);
    }
}