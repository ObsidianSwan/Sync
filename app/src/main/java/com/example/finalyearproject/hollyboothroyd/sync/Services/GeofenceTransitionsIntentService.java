package com.example.finalyearproject.hollyboothroyd.sync.Services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.finalyearproject.hollyboothroyd.sync.R;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by hollyboothroyd on 3/8/2018.
 */

public class GeofenceTransitionsIntentService extends IntentService {

    private DatabaseManager mDatabaseManager;

    public GeofenceTransitionsIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);

        mDatabaseManager = new DatabaseManager();
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            List<String> geofenceEnterTransitionDetails = getGeofenceEnterTransitionDetails(geofenceTransition, triggeringGeofences);
            // TODO get left users too

            // Send details to GMapFragment to display/remove local user
            geofenceTriggeredMessageToMaps(geofenceEnterTransitionDetails);

            //Log.i(TAG, geofenceTransitionDetails);
        } else {
            // Log the error.
            //Log.e(TAG, getString(R.string.geofence_transition_invalid_type,
                    //geofenceTransition));
        }
    }

    private void geofenceTriggeredMessageToMaps(List<String> geofenceEnterTransitionDetails){
        for(String id : geofenceEnterTransitionDetails){
            Intent intent = new Intent("geofenceEnterTriggered");
            intent.putExtra("userId", id);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }

    }


    private List<String> getGeofenceEnterTransitionDetails(int geofenceTransition, List<Geofence> triggeringGeofences){
        List<String> geofenceRequestIds = new ArrayList<>();
        if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            for (Geofence geofence : triggeringGeofences) {
                geofenceRequestIds.add(geofence.getRequestId());
            }
        }
        return geofenceRequestIds;
    }

    private String getTransitionString(int transitionType) {
        return "";
    }

    private void sendNotification(String notificationDetails) {
    }
}


class GeofenceErrorMessages {
    private GeofenceErrorMessages() {
    }

    public static String getErrorString(Context context, int errorCode) {
        Resources mResources = context.getResources();
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "Geofence service is not available now.";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Your app has registered too many geofences.";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "You have provided too many PendingIntents!";
            default:
                return "Unknown error.";
        }
    }
}
