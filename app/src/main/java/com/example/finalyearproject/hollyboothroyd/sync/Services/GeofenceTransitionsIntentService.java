package com.example.finalyearproject.hollyboothroyd.sync.Services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.finalyearproject.hollyboothroyd.sync.Model.Person;
import com.example.finalyearproject.hollyboothroyd.sync.R;
import com.example.finalyearproject.hollyboothroyd.sync.Utils.Constants;
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
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            List<String> geofenceEnterTransitionDetails = getGeofenceEnterTransitionDetails(geofenceTransition, triggeringGeofences);
            // TODO get left users too

            // Send details to GMapFragment to display/remove local user
            geofenceTriggeredMessageToMaps(geofenceEnterTransitionDetails);

            Log.i(TAG, String.valueOf(geofenceTransition));
        } else {
            // Log the error.
            Log.e(TAG, getString(R.string.geofence_transition_invalid_type) + ": " + geofenceTransition);
        }
    }

    private void geofenceTriggeredMessageToMaps(List<String> geofenceEnterTransitionDetails){
        // Send the geofence trigger message to the GMaps fragment
        for(String id : geofenceEnterTransitionDetails){
            Intent intent = new Intent(getString(R.string.geofence_enter_trigger));
            intent.putExtra(Constants.geofenceUserId, id);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }

    }


    private List<String> getGeofenceEnterTransitionDetails(int geofenceTransition, List<Geofence> triggeringGeofences){
        List<String> geofenceRequestIds = new ArrayList<>();
        // Return the request ids of the geofences that triggered with enter
        if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL || geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            for (Geofence geofence : triggeringGeofences) {
                geofenceRequestIds.add(geofence.getRequestId());
            }
        }
        return geofenceRequestIds;
    }
}


class GeofenceErrorMessages {
    private GeofenceErrorMessages() {
    }

    public static String getErrorString(Context context, int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return context.getString(R.string.geofence_service_unavailable_error);
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return context.getString(R.string.geofence_max_error);
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return context.getString(R.string.max_pending_intents_error);
            default:
                return context.getString(R.string.generic_error_text);
        }
    }
}
