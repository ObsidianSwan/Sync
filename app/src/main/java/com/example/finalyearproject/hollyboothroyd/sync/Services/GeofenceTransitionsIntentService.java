package com.example.finalyearproject.hollyboothroyd.sync.Services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.finalyearproject.hollyboothroyd.sync.R;
import com.example.finalyearproject.hollyboothroyd.sync.Utils.Constants;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by hollyboothroyd
 * 3/8/2018.
 */

public class GeofenceTransitionsIntentService extends IntentService {

    public GeofenceTransitionsIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    @Override
    public void onCreate() {
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

        // Get the geofences that were triggered. A single event can trigger
        // multiple geofences.
        List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

        // Get the transition details as a String.
        List<String> geofenceEnterTransitionDetails = getGeofenceTransitionDetails(triggeringGeofences);

        // Check if the other user is now local (enter or dwell triggers) or no longer local (exit trigger)
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            // Send details to GMapFragment to display local user
            geofenceAddTriggeredMessageToMaps(geofenceEnterTransitionDetails);
        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            // Send details to GMapFragment to remove user
            geofenceRemoveTriggeredMessageToMaps(geofenceEnterTransitionDetails);
        }

        Log.i(TAG, String.valueOf(geofenceTransition));

    }

    // Create an intent that informs the map to add the user's pin to the map
    private void geofenceAddTriggeredMessageToMaps(List<String> geofenceEnterTransitionDetails) {
        // Send the geofence trigger message to the GMaps fragment
        for (String id : geofenceEnterTransitionDetails) {
            Intent intent = new Intent(getString(R.string.geofence_enter_trigger));
            intent.putExtra(Constants.geofenceUserId, id);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    // Create an intent that informs the map to remove the user's pin from the map
    private void geofenceRemoveTriggeredMessageToMaps(List<String> geofenceEnterTransitionDetails) {
        // Send the geofence trigger message to the GMaps fragment
        for (String id : geofenceEnterTransitionDetails) {
            Intent intent = new Intent(getString(R.string.geofence_exit_trigger));
            intent.putExtra(Constants.geofenceUserId, id);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    // Retrieve the triggering users' ID
    private List<String> getGeofenceTransitionDetails(List<Geofence> triggeringGeofences) {
        List<String> geofenceRequestIds = new ArrayList<>();
        // Return the request ids of the triggered geofences
        for (Geofence geofence : triggeringGeofences) {
            geofenceRequestIds.add(geofence.getRequestId());
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
