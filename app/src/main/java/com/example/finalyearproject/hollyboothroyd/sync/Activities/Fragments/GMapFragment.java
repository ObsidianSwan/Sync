package com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalyearproject.hollyboothroyd.sync.Activities.CoreActivity;
import com.example.finalyearproject.hollyboothroyd.sync.Model.Event;
import com.example.finalyearproject.hollyboothroyd.sync.Model.Notification;
import com.example.finalyearproject.hollyboothroyd.sync.Model.NotificationBase;
import com.example.finalyearproject.hollyboothroyd.sync.Model.Person;
import com.example.finalyearproject.hollyboothroyd.sync.Model.UserConnections;
import com.example.finalyearproject.hollyboothroyd.sync.Model.UserEvents;
import com.example.finalyearproject.hollyboothroyd.sync.Model.UserEventsListener;
import com.example.finalyearproject.hollyboothroyd.sync.Model.UserNotifications;
import com.example.finalyearproject.hollyboothroyd.sync.R;
import com.example.finalyearproject.hollyboothroyd.sync.Services.AccountManager;
import com.example.finalyearproject.hollyboothroyd.sync.Services.DatabaseManager;
import com.example.finalyearproject.hollyboothroyd.sync.Services.GeofenceTransitionsIntentService;
import com.example.finalyearproject.hollyboothroyd.sync.Services.LocationFilter;
import com.example.finalyearproject.hollyboothroyd.sync.UI.CustomInfoWindow;
import com.example.finalyearproject.hollyboothroyd.sync.Utils.Constants;
import com.example.finalyearproject.hollyboothroyd.sync.Utils.NotificationType;
import com.example.finalyearproject.hollyboothroyd.sync.Utils.Util;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.linkedin.platform.APIHelper;
import com.linkedin.platform.errors.LIApiError;
import com.linkedin.platform.listeners.ApiListener;
import com.linkedin.platform.listeners.ApiResponse;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by hollyboothroyd on 12/11/2017.
 */
 public class GMapFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener, NavigationView.OnNavigationItemSelectedListener, UserEventsListener {

    private static final String TAG = "GMapFragment";

    private GoogleMap mMap;

    private OnFragmentInteractionListener mListener;

    private Person mCurrentUser;
    private String mCurrentUserId;

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;

    private DatabaseReference mPeopleDBRef;
    private ValueEventListener mPeopleDBListener;

    private DatabaseReference mLocationDBRef;
    private ValueEventListener mLocationDBListener;

    private DatabaseManager mDatabaseManager;

    private GeofencingClient mGeofencingClient;
    private List<Geofence> mGeofenceList;
    private static PendingIntent mGeofencePendingIntent;
    private Circle mGeofenceCircle;

    private ConcurrentHashMap<String, Person> mPeopleMap;
    private ConcurrentHashMap<String, Person> mLocalPeopleMap;

    private List<Marker> mPersonMarkerList;
    private ConcurrentHashMap<String, Person> mPersonMarkerMap;

    private List<Marker> mEventMarkerList;
    private ConcurrentHashMap<String, Event> mEventMarkerMap;

    private CustomInfoWindow mCustomInfoWindow;
    private AlertDialog.Builder mDialogBuilder;
    private AlertDialog mDialog;

    private AlertDialog.Builder mLinkedinDialogBuilder;
    private AlertDialog mLinkedinDialog;
    private List<View> mLinkedinDiaglogViews;

    private Button mPopupButton;
    private TextView mConnectionPendingMessage;

    private String mPersonPositionFilter = "";
    private String mPersonCompanyFilter = "";
    private String mPersonIndustryFilter = "";
    private String mEventIndustryFilter = "";

    // Set settings to the defaults while the database settings are being retrieved
    private float mPersonPinColor = Constants.personPinColorDefault;
    private float mEventPinColor = Constants.eventPinColorDefault;
    private int mLocationTimeUpdateInterval = Constants.locationTimeUpdateIntervalDefault;
    private int mLocationDistanceUpdateIntervalName = Constants.locationDistanceUpdateIntervalDefault;
    private int mMapZoomLevelName = Constants.mapZoomLevelDefault;
    private int mSearchRadius = Constants.geofenceRadiusDefault;
    private int mPrivacyIntensity = Constants.privacyIntensityDefault;

    private final String linkedInProfileDetails = "https://api.linkedin.com/v1/people/~:(first-name,last-name,email-address,picture-urls::(original),positions,industry)?format=json";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gmaps, container, false);

        mDatabaseManager = new DatabaseManager();
        AccountManager accountManager = new AccountManager();

        mCurrentUserId = accountManager.getCurrentUser().getUid();

        mGeofencingClient = LocationServices.getGeofencingClient(getActivity());
        mGeofenceList = new ArrayList<>();

        // All users
        mPeopleMap = new ConcurrentHashMap<>();

        // Users within the local area
        mLocalPeopleMap = new ConcurrentHashMap<>();

        // Markers of the local users
        mPersonMarkerMap = new ConcurrentHashMap<>();
        mPersonMarkerList = new ArrayList<>();

        // Markers of the local events
        mEventMarkerMap = new ConcurrentHashMap<>();
        mEventMarkerList = new ArrayList<>();
        UserEvents.mListeners.add(this);

        // Linkedin Popup views
        mLinkedinDiaglogViews = new ArrayList<>();

        // Save reference to DB to remove listener later
        mPeopleDBRef = mDatabaseManager.getPeopleDatabaseReference();
        // Listen for any changes to the people database
        mPeopleDBListener = mPeopleDBRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Clear the people map
                mPeopleMap.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Person person = snapshot.getValue(Person.class);
                    // If the person is not already in the map, populate the map
                    if (person != null && !mPeopleMap.containsKey(person.getUserId())) {
                        mPeopleMap.put(person.getUserId(), person);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.toString());
            }
        });

        // Pull down user settings that customize the map look and functionality
        setUpCustomization();

        Button filterButton = (Button) view.findViewById(R.id.filter_button);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open filter popup
                filterPopupCreation();
            }
        });


        // Check profile details match their LinkedIn if they are connected
        // TODO write in report a future feature could be addding linkedin connectivity post account creation
        mDatabaseManager.getUserPeopleDatabaseReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    final Person currentUser = dataSnapshot.getValue(Person.class);
                    if (currentUser != null && currentUser.getIsLinkedInConnected()) {
                        APIHelper apiHelper = APIHelper.getInstance(getContext());
                        apiHelper.getRequest(getActivity(), linkedInProfileDetails, new ApiListener() {
                            @Override
                            public void onApiSuccess(ApiResponse s) {
                                JSONObject result = s.getResponseDataAsJson();
                                Log.i(TAG, getString(R.string.linkedin_profile_retrieval_successful));
                                try {
                                    mLinkedinDialogBuilder = new AlertDialog.Builder(getActivity());
                                    if (!currentUser.getFirstName().toLowerCase().trim().equals(result.get("firstName").toString().toLowerCase().trim())) {
                                        linkedInUpdatePopup("first name", result.get("firstName").toString(), Constants.userFirstNameChildName);
                                    }
                                    if (!currentUser.getLastName().toLowerCase().trim().equals(result.get("lastName").toString().toLowerCase().trim())) {
                                        linkedInUpdatePopup("last name", result.get("lastName").toString(), Constants.userLastNameChildName);
                                    }
                                    JSONObject currentJob = result.getJSONObject("positions").getJSONArray("values").getJSONObject(0);
                                    if (!currentUser.getPosition().toLowerCase().trim().equals(currentJob.get("title").toString().toLowerCase().trim())) {
                                        linkedInUpdatePopup("position", currentJob.get("title").toString(), Constants.userPositionChildName);
                                    }
                                    if (!currentUser.getCompany().toLowerCase().trim().toLowerCase().trim().equals(currentJob.getJSONObject("company").get("name").toString().toLowerCase().trim())) {
                                        linkedInUpdatePopup("company", currentJob.getJSONObject("company").get("name").toString(), Constants.userCompanyChildName);
                                    }
                                    if (!currentUser.getIndustry().toLowerCase().trim().equals(result.get("industry").toString().toLowerCase().trim())) {
                                        linkedInUpdatePopup("industry", result.get("industry").toString(), Constants.userIndustryChildName);
                                    }
                                    JSONArray array = result.getJSONObject("pictureUrls").getJSONArray("values");
                                    if (!currentUser.getImageId().toLowerCase().trim().equals(array.getString(0).toLowerCase().trim())) {
                                        linkedInUpdatePopup("profile picture", array.getString(0), Constants.userImgChildName);
                                    }

                                    if(mLinkedinDiaglogViews.size() > 0) {
                                        mLinkedinDialogBuilder.setView(mLinkedinDiaglogViews.get(0));
                                        mLinkedinDiaglogViews.remove(0);
                                        mLinkedinDialog = mLinkedinDialogBuilder.create();
                                        mLinkedinDialog.show();
                                    }
                                } catch (JSONException e) {
                                    Log.e(TAG, e.toString());
                                }
                            }

                            @Override
                            public void onApiError(LIApiError error) {
                                Log.e(TAG, error.toString());
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.toString());
            }
        });

        return view;
    }

    private void linkedInUpdatePopup(String textInfoField, final String updateValue, final String databaseChild) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.linkedin_update_popup, null);

        // Set up the UI
        TextView updateInfoText = (TextView) view.findViewById(R.id.linkedin_update_info_text);
        Button acceptButton = (Button) view.findViewById(R.id.linkedin_update_accept_button);
        Button denyButton = (Button) view.findViewById(R.id.linkedin_update_deny_button);

        updateInfoText.setText("Your " + textInfoField + " is different from your LinkedIn. Would you like to update it to match your LinkedIn profile?");

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), R.string.edit_profile_successfully_changed, Toast.LENGTH_SHORT).show();
                mLinkedinDialog.dismiss();
                if(mLinkedinDiaglogViews.size() > 0){
                    mLinkedinDialogBuilder.setView(mLinkedinDiaglogViews.get(0));
                    mLinkedinDiaglogViews.remove(0);
                    mLinkedinDialog = mLinkedinDialogBuilder.create();
                    mLinkedinDialog.show();
                }
                mDatabaseManager.getUserPeopleDatabaseReference().child(databaseChild).setValue(updateValue);
            }
        });

        denyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLinkedinDialog.dismiss();
                if(mLinkedinDiaglogViews.size() > 0){
                    mLinkedinDialogBuilder.setView(mLinkedinDiaglogViews.get(0));
                    mLinkedinDiaglogViews.remove(0);
                    mLinkedinDialog = mLinkedinDialogBuilder.create();
                    mLinkedinDialog.show();
                }
            }
        });

        mLinkedinDiaglogViews.add(view);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void setUpCustomization() {
        // Set the person pin color to the users choice found in the settings database
        mDatabaseManager.getUserSettings(Constants.personPinColorName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Set the color to the default in case the value cannot be set to the database value
                float value = Constants.personPinColorDefault;
                if (dataSnapshot.getValue(Float.class) != null) {
                    // Set the color to the saved settings
                    value = dataSnapshot.getValue(Float.class);
                }
                mPersonPinColor = value;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.toString());
            }
        });

        // Set the event pin color to the users choice found in the settings database
        mDatabaseManager.getUserSettings(Constants.eventPinColorName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Set the color to the default in case the value cannot be set to the database value
                float value = Constants.eventPinColorDefault;
                if (dataSnapshot.getValue(Integer.class) != null) {
                    // Set the color to the saved settings
                    value = dataSnapshot.getValue(Float.class);
                }
                mEventPinColor = value;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.toString());
            }
        });

        // Set the privacy intensity value to the users choice found in the settings database
        mDatabaseManager.getUserSettings(Constants.privacyIntensityName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Set the privacy intensity level to the default in case the value cannot be set to the database value
                int value = Constants.privacyIntensityDefault;
                if (dataSnapshot.getValue(Integer.class) != null) {
                    // Set the privacy intensity level to the saved settings
                    value = dataSnapshot.getValue(Integer.class);
                }
                mPrivacyIntensity = value;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.toString());
            }
        });

        // Set the search radius value to the users choice found in the settings database
        mDatabaseManager.getUserSettings(Constants.searchRadiusName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Set the search radius level to the default in case the value cannot be set to the database value
                int value = Constants.geofenceRadiusDefault;
                if (dataSnapshot.getValue(Integer.class) != null) {
                    // Set the search radius level to the saved settings
                    value = dataSnapshot.getValue(Integer.class);
                }
                mSearchRadius = value;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.toString());
            }
        });

        // Set the map zoom value to the users choice found in the settings database
        mDatabaseManager.getUserSettings(Constants.mapZoomLevelName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Set the map zoom level to the default in case the value cannot be set to the database value
                int value = Constants.mapZoomLevelDefault;
                if (dataSnapshot.getValue(Integer.class) != null) {
                    // Set the map zoom level to the saved settings
                    value = dataSnapshot.getValue(Integer.class);
                }

                mMapZoomLevelName = value;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.toString());
            }
        });

        // Set the location time update interval value to the users choice found in the settings database
        mDatabaseManager.getUserSettings(Constants.locationTimeUpdateIntervalName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Set the location time update interval to the default in case the value cannot be set to the database value
                int value = Constants.locationTimeUpdateIntervalDefault;
                if (dataSnapshot.getValue(Integer.class) != null) {
                    // Set the location time update interval to the saved settings
                    value = dataSnapshot.getValue(Integer.class);
                }

                mLocationTimeUpdateInterval = value;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.toString());
            }
        });

        // Set the location distance update interval value to the users choice found in the settings database
        mDatabaseManager.getUserSettings(Constants.locationDistanceUpdateIntervalName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Set the location distance update interval to the default in case the value cannot be set to the database value
                int value = Constants.locationDistanceUpdateIntervalDefault;
                if (dataSnapshot.getValue(Integer.class) != null) {
                    // Set the location distance update interval to the saved settings
                    value = dataSnapshot.getValue(Integer.class);
                }

                mLocationDistanceUpdateIntervalName = value;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.toString());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Remove the listeners
        if (mPeopleDBRef != null) {
            mPeopleDBRef.removeEventListener(mPeopleDBListener);
        }
        if (mLocationDBRef != null) {
            mLocationDBRef.removeEventListener(mLocationDBListener);
        }
        mLocationListener = null;
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Set up map item click listeners
        mCustomInfoWindow = new CustomInfoWindow(getActivity().getApplicationContext());
        mMap.setInfoWindowAdapter(mCustomInfoWindow);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerClickListener(this);

        // Save reference to DB to remove listener later
        mLocationDBRef = mDatabaseManager.getLocationDatabaseReference();
        // Listen for any changes to the location database
        mLocationDBListener = mLocationDBRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Remove all existing geofences
                removeGeofences();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // The current user does not need a geofence placed around it
                    if (!snapshot.getKey().equals(mCurrentUserId) && mPeopleMap.containsKey(snapshot.getKey())) {
                        // TODO check the location of the person changed
                        HashMap<String, Double> locationHash = (HashMap<String, Double>) snapshot.getValue();
                        // Create a geofence around the person, so if the user enters the persons
                        // geofence, the system is notified to put the person on the map
                        if (locationHash != null) {
                            setUpGeofence(new LatLng(locationHash.get(Constants.geofenceLatitude), locationHash.get(Constants.geofenceLongitude)), snapshot.getKey());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.toString());
            }
        });

        // To ensure that the current user's marker is quickly populated when the map loads,
        // a dedicated single event listener for the user's person content is requested.
        // This info is used to create the user's marker
        mDatabaseManager.getPeopleDatabaseReference().child(mCurrentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mCurrentUser = dataSnapshot.getValue(Person.class);
                if (mCurrentUser != null) {
                    setUserLocation();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.toString());
            }
        });

        // Listen for any changes to the current users location
        mLocationManager = (LocationManager) this.getActivity().getSystemService(LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.i("Location: ", location.toString());
                if (getActivity() != null) {
                    // Update the users location in the database
                    updateUserLocation(new LatLng(location.getLatitude(), location.getLongitude()), false);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        // Location permission previously granted
        requestLocationUpdates();

        // Register listener for NFC broadcasts
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver, new IntentFilter(getString(R.string.geofence_enter_trigger)));
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String userId = intent.getStringExtra(Constants.geofenceUserId);
            Person localPerson = mPeopleMap.get(userId);

            // If the person is not currently within the geofence radius (ie. not a local person)
            if (mPeopleMap.containsKey(userId) && !mLocalPeopleMap.containsKey(userId)) {
                mLocalPeopleMap.put(userId, localPerson);
                updatePersonMarker(localPerson, false);

            } // If the person is currently within the geofence radius (ie. a local person)
            else if (mPeopleMap.containsKey(userId) && mLocalPeopleMap.containsKey(userId)) {
                mLocalPeopleMap.put(userId, localPerson);
                updatePersonMarker(localPerson, true);
            }
        }

    };

    private void updatePersonMarker(Person localPerson, boolean isLocal) {
        // Remove the local person's pin from the marker map and list
        if (isLocal) {
            removeMarker(localPerson);
        }

        // Populate the map with the new person that filtering requirements
        if (meetsPeopleFilteringRequirements(localPerson)) {
            createPersonMarker(localPerson);
        }
    }

    private void removeMarker(Person person) {
        // Iterate through the person markers
        for (HashMap.Entry<String, Person> entry : mPersonMarkerMap.entrySet()) {

            // Find the marker whose value/person matches the local person to be updated
            if (entry.getValue().getUserId().equals(person.getUserId())) {

                // Iterate through the person marker list to find the marker object with the person
                Iterator<Marker> listIterator = mPersonMarkerList.iterator();
                while (listIterator.hasNext()) {
                    Marker marker = listIterator.next();

                    // If the marker matches the marker key of the found person,
                    // remove the marker from the Google map, the list, and the personMarkerMap
                    if (marker.getId().equals(entry.getKey())) {
                        marker.remove();
                        listIterator.remove();
                        mPersonMarkerMap.remove(entry.getKey());
                    }
                }
            }
        }
    }

    private void requestLocationUpdates() {
        // Create location update request with the users settings values or default values.
        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    mLocationTimeUpdateInterval,
                    mLocationDistanceUpdateIntervalName,
                    mLocationListener);
        } catch (SecurityException ex) {
            Log.e(TAG, ex.toString());
        }
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        // Set trigger type to INITIAL_TRIGGER_DWELL to reduce 'alert spam' if users briefly enter or
        // exit the geofence
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(getActivity(), GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(getActivity(), 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    // Add longitude and latitude to person database
    // Update user location on the map
    @SuppressLint("MissingPermission")
    private void setUserLocation() {
        Location lastKnownLocation = Util.getLastKnownLocation(mLocationManager);
        if (lastKnownLocation == null) {
            Toast.makeText(getActivity(), R.string.could_not_find_location, Toast.LENGTH_SHORT).show();
        } else {
            updateUserLocation(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()), true);
        }
    }

    private void updateUserLocation(final LatLng userLocation, final boolean animateMap) {
        final LatLng obfuscatedLocation;

        // Obfuscate the users location based on the desired privacy intensity level
        switch (mPrivacyIntensity) {
            case (0):  // None
                obfuscatedLocation = userLocation;
                break;
            case (1):  // Basic
                obfuscatedLocation = LocationFilter.randObfuscation(userLocation, mSearchRadius);
                break;
            case (2):  // Intermediate
                obfuscatedLocation = LocationFilter.nRandObfuscation(userLocation, mSearchRadius);
                break;
            case (3):  // Advanced
                obfuscatedLocation = LocationFilter.thetaRandObfuscation(userLocation, mSearchRadius);
                break;
            default:  // Default to intermediate
                obfuscatedLocation = LocationFilter.nRandObfuscation(userLocation, mSearchRadius);
                break;
        }

        // Send obfuscated location to the database for other users to see
        mDatabaseManager.updateCurrentUserLocation(obfuscatedLocation);

        // Create the user's marker on the map
        if (mCurrentUser != null) {
            mLocalPeopleMap.put(mCurrentUserId, mCurrentUser);
            removeMarker(mCurrentUser);
            createPersonMarker(mCurrentUser);
        }

        // Remove any existing geofence circle indicators
        if (mGeofenceCircle != null) {
            mGeofenceCircle.remove();
        }

        // Insert geofence circle
        CircleOptions circleOptions = new CircleOptions()
                .center(userLocation)
                .radius(mSearchRadius)
                .fillColor(Constants.geofenceCircleColor)
                .strokeColor(Color.GRAY);
        mGeofenceCircle = mMap.addCircle(circleOptions);

        getEvents();

        if (animateMap) {
            // Move the map center to the users location
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, mMapZoomLevelName));
        }
    }

    @SuppressLint("MissingPermission")
    private void setUpGeofence(LatLng userLocation, String userId) {
        // Create new geofence for the current users location
        mGeofenceList.add(new Geofence.Builder()
                .setRequestId(userId)
                .setCircularRegion(
                        userLocation.latitude,
                        userLocation.longitude,
                        mSearchRadius
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());

        // Add the geofence to the client
        if (getActivity() != null) {
            mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                    .addOnSuccessListener(getActivity(), new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Geofences added
                            Log.i(TAG, getString(R.string.add_geofence_successful));
                        }
                    })
                    .addOnFailureListener(getActivity(), new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Failed to add geofences
                            Log.e(TAG, e.toString());
                        }
                    });
        }
    }

    //TODO !!!! Clean up when the user logs out
    private void removeGeofences() {
        if (mGeofenceList != null) {
            mGeofenceList.clear();
            if (mGeofencePendingIntent != null && mGeofencingClient != null && getActivity() != null) {
                mGeofencingClient.removeGeofences(mGeofencePendingIntent)
                        .addOnSuccessListener(getActivity(), new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Geofences removed
                                Log.i(TAG, getString(R.string.remove_geofence_successful));
                            }
                        })
                        .addOnFailureListener(getActivity(), new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Failed to remove geofences
                                Log.e(TAG, e.toString());
                            }
                        });
            }
        }
    }

    private void getPeople() {
        // Clear the map
        clearPeopleMarkers();

        // Populate the map with local people who meet the filtering requirements
        for (HashMap.Entry<String, Person> entry : mLocalPeopleMap.entrySet()) {
            if (meetsPeopleFilteringRequirements(entry.getValue())) {
                createPersonMarker(entry.getValue());
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void createPersonMarker(final Person person) {
        // Check if the person marker is the current users
        if (person.getUserId().equals(mCurrentUserId)) {
            // Set up the UI
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(mPersonPinColor));
            markerOptions.title(person.getFirstName() + " " + person.getLastName());

            // Set current users pin to actual location and not the obfuscated location
            Location lastKnownLocation = Util.getLastKnownLocation(mLocationManager);
            markerOptions.position(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()));

            markerOptions.snippet("Position: " + person.getPosition() +
                    "\nCompany: " + person.getCompany());

            Marker newMarker = mMap.addMarker(markerOptions);
            newMarker.setTag(Constants.personMarkerTag);

            // Store person data to a map to use in the mDialog and the CustomInfoWindow
            // Save person markers in a map to be able to clear person markers individually or as a group separate from the events markers
            mPersonMarkerMap.put(newMarker.getId(), person);
            mPersonMarkerList.add(newMarker);
            mCustomInfoWindow.addMarkerImage(newMarker.getId(), person.getImageId());

        } else {
            mDatabaseManager.getUserLocationDatabaseReference(person.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(mPersonPinColor));
                        markerOptions.title(person.getFirstName() + " " + person.getLastName());

                        HashMap<String, Double> locationHash = (HashMap<String, Double>) dataSnapshot.getValue();
                        markerOptions.position(new LatLng(locationHash.get("latitude"), locationHash.get("longitude")));

                        markerOptions.snippet("Position: " + person.getPosition() +
                                "\nCompany: " + person.getCompany());

                        Marker newMarker = mMap.addMarker(markerOptions);
                        newMarker.setTag(Constants.personMarkerTag);

                        // Store person data to a map to use in the mDialog and the CustomInfoWindow
                        // Save person markers in a map to be able to clear person markers individually or as a group separate from the events markers
                        mPersonMarkerMap.put(newMarker.getId(), person);
                        mPersonMarkerList.add(newMarker);
                        mCustomInfoWindow.addMarkerImage(newMarker.getId(), person.getImageId());
                    } catch (ClassCastException ex) {
                        Log.e(TAG, ex.toString());
                        throw ex;
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, databaseError.toString());
                }
            });
        }
    }

    private void clearPeopleMarkers() {
        // Clear out the people map and list
        if (mPersonMarkerMap != null) {
            mPersonMarkerMap.clear();
        }
        if (mPersonMarkerList != null) {
            for (Marker marker : mPersonMarkerList) {
                marker.remove();
            }
            mPersonMarkerList.clear();
        }
    }

    private boolean meetsPeopleFilteringRequirements(Person person) {
        boolean meetsFilteringRequirements = true;
        // If there are no filtering requirements, then the person automatically meets the requirements
        if (mPersonPositionFilter.equals("") && mPersonCompanyFilter.equals("") && mPersonIndustryFilter.equals("")) {
            meetsFilteringRequirements = true;
        } // If the person position filter has been specified and the person matches that position, then the person meets the requirements
        else if (!mPersonPositionFilter.equals("") && person.getPosition().toLowerCase().trim().equals(mPersonPositionFilter)) {
            meetsFilteringRequirements = true;
        } // If the person company filter has been specified and the person matches that company, then the person meets the requirements
        else if (!mPersonCompanyFilter.equals("") && person.getCompany().toLowerCase().trim().equals(mPersonCompanyFilter)) {
            meetsFilteringRequirements = true;
        }// If the person industry filter has been specified and the person matches that industry, then the person meets the requirements
        else if (!mPersonIndustryFilter.equals("") && person.getIndustry().toLowerCase().trim().equals(mPersonIndustryFilter)) {
            meetsFilteringRequirements = true;
        } // If the person position, company, or industry filter has been specified, but the person does not match, then the person does not meet the requirements
        else {
            meetsFilteringRequirements = false;
        }
        return meetsFilteringRequirements;
    }

    private void getEvents() {
        clearEventMarkers();

        // Create an event marker for all the events that the user is tracking
        for (Event event : UserEvents.ALL_EVENTS) {
            // Check the event abides by the filtering rules established by the user
            if (meetsEventFilteringRequirements(event)) {
                createEventMarker(event);
            }
        }

        for (Event event : UserEvents.EVENTS_ATTENDING) {
            // Check the event abides by the filtering rules established by the user
            if (meetsEventFilteringRequirements(event)) {
                // Check if the event has already been added to the GMap
                // to prevent multiple markers per event
                for (HashMap.Entry<String, Event> entry : mEventMarkerMap.entrySet()) {
                    if (!entry.getValue().getUid().equals(event.getUid())) {
                        createEventMarker(event);
                    }
                }
            }
        }

        for (Event event : UserEvents.EVENTS_HOSTING) {
            // Check the event abides by the filtering rules established by the user
            if (meetsEventFilteringRequirements(event)) {
                // Check if the event has already been added to the GMap
                // to prevent multiple markers per event
                for (HashMap.Entry<String, Event> entry : mEventMarkerMap.entrySet()) {
                    if (!entry.getValue().getUid().equals(event.getUid())) {
                        createEventMarker(event);
                    }
                }
            }
        }
    }

    private void createEventMarker(Event event) {
        LatLng eventLocation = new LatLng(event.getLatitude(), event.getLongitude());

        // Set up the marker options
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(mEventPinColor));
        markerOptions.title(event.getTitle());
        markerOptions.position(eventLocation);
        markerOptions.snippet("Industry: " + event.getIndustry() +
                "\n\nTime: " + event.getTime() +
                "\nDate: " + event.getDate());

        // Add the marker to the map and set it's tag to indicate it is an event marker
        Marker newMarker = mMap.addMarker(markerOptions);
        newMarker.setTag(Constants.eventMarkerTag);

        // Store event data to a map to use in the mDialog and the CustomInfoWindow
        // Save event markers in a map to be able to clear event markers individually
        // or as a group separate from the people markers
        mEventMarkerMap.put(newMarker.getId(), event);
        mEventMarkerList.add(newMarker);
        mCustomInfoWindow.addMarkerImage(newMarker.getId(), event.getImageId());
    }

    private void clearEventMarkers() {
        // Clear the event marker map and list
        if (mEventMarkerList.size() > 0 && mEventMarkerMap.size() > 0) {
            mEventMarkerMap.clear();
            for (Marker marker : mEventMarkerList) {
                marker.remove();
            }
            mEventMarkerList.clear();
        }
    }

    private boolean meetsEventFilteringRequirements(Event event) {
         // If the event industry filter has been specified, but the event does not match, then the event does not meet the requirements
        if (!mEventIndustryFilter.equals("") && !event.getIndustry().toLowerCase().equals(mEventIndustryFilter)){
            return false;
        }
        return true;
    }

    @Override
    public void onInfoWindowClick(final Marker marker) {
        mDialogBuilder = new AlertDialog.Builder(getActivity());

        // Check which type of marker it is (person or event) and open the correct marker type
        if (marker.getTag() == Constants.personMarkerTag) {
            personPopupCreation(marker);
        } else if (marker.getTag() == Constants.eventMarkerTag) {
            eventPopupCreation(marker);
        }
    }

    private void eventPopupCreation(Marker marker) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.event_popup, null);

        // Set up the UI
        Button dismissPopupButton = (Button) view.findViewById(R.id.dismiss_popup_button);
        ImageView eventImage = (ImageView) view.findViewById(R.id.popup_image);
        TextView eventTitle = (TextView) view.findViewById(R.id.popup_title);
        TextView eventIndustry = (TextView) view.findViewById(R.id.popup_industry);
        TextView eventDate = (TextView) view.findViewById(R.id.popup_date);
        TextView eventTime = (TextView) view.findViewById(R.id.popup_time);
        TextView eventAddress = (TextView) view.findViewById(R.id.popup_address);
        TextView eventDescription = (TextView) view.findViewById(R.id.popup_description);
        Button eventButton = (Button) view.findViewById(R.id.popup_event_button);
        Button eventButton2 = (Button) view.findViewById(R.id.popup_event_button2);
        Button eventButton3 = (Button) view.findViewById(R.id.popup_event_button3);

        final Event event = mEventMarkerMap.get(marker.getId());
        if (!mEventMarkerMap.isEmpty()) {
            Picasso.with(getActivity()).load(event.getImageId()).into(eventImage);
        }

        // Populate the UI with the event information
        eventTitle.setText(event.getTitle());
        eventIndustry.setText("Industry: " + event.getIndustry());
        eventDate.setText(event.getDate());
        eventTime.setText(event.getTime());

        String address = event.getStreet() + ", \n" + event.getCity() + ", " + event.getState() + ", \n" +
                event.getZipCode() + ", " + event.getCountry();
        eventAddress.setText(address);
        eventDescription.setText(event.getDescription());

        // The user is both hosting and attending the event
        if (UserEvents.EVENTS_HOSTING_MAP.containsKey(event.getUid()) &&
                UserEvents.EVENTS_ATTENDING_MAP.containsKey(event.getUid())) {
            eventButton.setText(R.string.stop_attending_button_text);
            eventButton2.setText(R.string.delete_event_button_text);
            eventButton3.setText(R.string.edit_event_button_text);
            eventButton2.setVisibility(View.VISIBLE);
            eventButton3.setVisibility(View.VISIBLE);

            // Stop attending the event if the user is already attending
            eventButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopAttendingEvent(event, mCurrentUserId);
                }
            });

            // Delete event if the user is hosting the event
            eventButton2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteEvent(event);
                }
            });

            eventButton3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                    if (null != mListener) {
                        // Notify the active callbacks interface (the activity, if the
                        // fragment is attached to one) that an item wants to be edited.
                        mListener.onFragmentInteraction(event);
                    }
                }
            });

        } // The user is hosting, but not attending the event
        else if (UserEvents.EVENTS_HOSTING_MAP.containsKey(event.getUid()) && !UserEvents.EVENTS_ATTENDING_MAP.containsKey(event.getUid())) {
            eventButton2.setText(R.string.delete_event_button_text);
            eventButton3.setText(R.string.edit_event_button_text);
            eventButton2.setVisibility(View.VISIBLE);
            eventButton3.setVisibility(View.VISIBLE);

            // Attend event
            eventButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    attendEvent(event);
                }
            });

            // Delete event if the user is hosting the event
            eventButton2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteEvent(event);
                }
            });

            eventButton3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                    if (null != mListener) {
                        // Notify the active callbacks interface (the activity, if the
                        // fragment is attached to one) that an item wants to be edited.
                        mListener.onFragmentInteraction(event);
                    }
                }
            });

        } // The user is attending, but not hosting the event
        else if (UserEvents.EVENTS_ATTENDING_MAP.containsKey(event.getUid()) &&
                !UserEvents.EVENTS_HOSTING_MAP.containsKey(event.getUid())) {

            // Stop attending the event if the user is already attending
            eventButton.setText(R.string.stop_attending_button_text);
            eventButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopAttendingEvent(event, mCurrentUserId);
                }
            });
        } // The user is not hosting or attending the event
        else {
            // Attend event if the user is not attending
            eventButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    attendEvent(event);
                }
            });
        }
        dismissPopupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });

        mDialogBuilder.setView(view);
        mDialog = mDialogBuilder.create();
        mDialog.show();
    }

    private void attendEvent(final Event event) {
        // Add user to event attendee database
        mDatabaseManager.addUserAttendingEvent(event.getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Add event to user's attending database
                    mDatabaseManager.addEventAttending(event.getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getActivity(), "You're attending " + event.getTitle() + "!", Toast.LENGTH_SHORT).show();
                                mDialog.dismiss();
                                Log.i(TAG, getString(R.string.attend_event_successful));
                            } else {
                                Toast.makeText(getActivity(), R.string.event_attendence_unsuccessful, Toast.LENGTH_SHORT).show();
                                Log.e(TAG, getString(R.string.event_attendence_unsuccessful));
                                // TODO: Remove the added previous db entry
                            }
                        }
                    });

                } else {
                    Toast.makeText(getActivity(), R.string.event_attendence_unsuccessful, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, getString(R.string.event_attendence_unsuccessful));
                }
            }
        });
    }

    private void stopAttendingEvent(final Event event, final String userId) {
        // Remove user from event attendee database
        mDatabaseManager.deleteUserAttendingEvent(event.getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Remove event from event attending database
                    mDatabaseManager.deleteEventAttending(event.getUid(), userId).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getActivity(), "You're no longer attending" + event.getTitle() + "!", Toast.LENGTH_SHORT).show();
                                mDialog.dismiss();
                                Log.i(TAG, getString(R.string.stop_attending_event_successful));
                            } else {
                                Toast.makeText(getActivity(), R.string.event_attendence_deletion_unsuccessful, Toast.LENGTH_SHORT).show();
                                Log.e(TAG, getString(R.string.event_attendence_deletion_unsuccessful));
                            }
                        }
                    });
                } else {
                    Toast.makeText(getActivity(), R.string.event_attendence_deletion_unsuccessful, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, getString(R.string.event_attendence_deletion_unsuccessful));
                }
            }
        });
    }

    private void deleteEvent(final Event event) {
        // Delete attending references in user attending database
        mDatabaseManager.getUsersAttendingEvent(event.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String attendeeId = snapshot.getKey();
                    mDatabaseManager.deleteEventAttending(event.getUid(), attendeeId);
                }
                // Stop hosting the event
                mDatabaseManager.deleteEventHosting(event.getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Delete event in event database
                            mDatabaseManager.deleteEvent(event.getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getActivity(), R.string.delete_event_successful, Toast.LENGTH_SHORT).show();
                                        mDialog.dismiss();
                                        Log.i(TAG, getString(R.string.delete_event_successful));
                                    } else {
                                        Toast.makeText(getActivity(), R.string.delete_event_unsuccessful, Toast.LENGTH_SHORT).show();
                                        Log.e(TAG, getString(R.string.delete_event_unsuccessful));
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(getActivity(), R.string.delete_event_unsuccessful, Toast.LENGTH_SHORT).show();
                            Log.e(TAG, getString(R.string.delete_event_unsuccessful));
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.toString());
            }
        });
    }

    private void personPopupCreation(Marker marker) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.person_popup, null);

        // Set up UI
        Button dismissPopupButton = (Button) view.findViewById(R.id.dismiss_popup_button);
        ImageView personImage = (ImageView) view.findViewById(R.id.popup_image);
        TextView personName = (TextView) view.findViewById(R.id.popup_name);
        TextView personPosition = (TextView) view.findViewById(R.id.popup_position);
        TextView personCompany = (TextView) view.findViewById(R.id.popup_company);
        TextView personIndustry = (TextView) view.findViewById(R.id.popup_industry);
        View popupInteractor = (View) view.findViewById(R.id.popup_interaction);
        mPopupButton = (Button) view.findViewById(R.id.popup_button);
        mConnectionPendingMessage = (TextView) view.findViewById(R.id.popup_connection_pending);


        final Person person = mPersonMarkerMap.get(marker.getId());
        if (!mPersonMarkerMap.isEmpty()) {
            Picasso.with(getActivity()).load(person.getImageId()).into(personImage);
        }

        // Populate UI with person information
        personName.setText(person.getFirstName() + " " + person.getLastName());
        personPosition.setText("Position: " + person.getPosition());
        personCompany.setText("Company: " + person.getCompany());
        personIndustry.setText("Industry: " + person.getIndustry());

        // This is the current users popup
        if (person.getUserId().equals(mCurrentUserId)) {
            // Don't show connection button for the users marker popup
            popupInteractor.setVisibility(View.GONE);
        } // The users are connected
        else if (UserConnections.CONNECTION_ITEM_MAP.containsKey(person.getUserId())) {
            mPopupButton.setText(R.string.disconnect_button);
            mPopupButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteConnection(person);
                }
            });
        } // The current user has already sent a connection request to the other user
        else if (UserConnections.CONNECTION_REQUEST_ITEM_MAP.containsKey(person.getUserId())) {
            // Don't show connection button if a connection is already pending
            mPopupButton.setVisibility(View.GONE);
            mConnectionPendingMessage.setVisibility(View.VISIBLE);
        } // The current user has a connection request from the other user
        else if (UserNotifications.CONNECTION_REQUEST_ITEMS_MAP.containsKey(person.getUserId())) {
            final NotificationBase notification = UserNotifications.CONNECTION_REQUEST_ITEMS_MAP.get(person.getUserId());
            mPopupButton.setText(R.string.accept_connection_request_button_text);
            mPopupButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addConnection(notification);
                }
            });
        } // Users are not connected and no connection requests have been sent
        else {
            mPopupButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendConnectionRequest(person.getUserId());
                }
            });
        }

        dismissPopupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });

        // Send a profile view notification unless it is the current users profile
        if (!person.getUserId().equals(mCurrentUserId)) {
            DatabaseReference newNotificationRef = mDatabaseManager.getNewNotifcationReference(person.getUserId());
            String refKey = newNotificationRef.getKey();
            Notification notification = new Notification(refKey, mCurrentUserId, NotificationType.PROFILE_VIEW);
            mDatabaseManager.sendNotification(newNotificationRef, notification).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.i(TAG, getString(R.string.send_notification_successful));
                    } else {
                        Log.e(TAG, getString(R.string.send_notification_error));
                    }
                }
            });
        }

        mDialogBuilder.setView(view);
        mDialog = mDialogBuilder.create();
        mDialog.show();
    }

    private void addConnection(final NotificationBase notification) {
        mDatabaseManager.getUserPeopleDatabaseReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Add connection to current users database
                mDatabaseManager.addConnection(notification.getId(), mCurrentUserId).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Add connection to other users database
                            mDatabaseManager.addConnection(mCurrentUserId, notification.getId()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Delete connection request in the other users database
                                        mDatabaseManager.deleteUserConnectionRequest(notification.getId()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    // Delete connection notification in the current users database
                                                    mDatabaseManager.deleteUserNotification(notification.getDbRefKey()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(getContext(), R.string.connection_accepted_toast_text, Toast.LENGTH_SHORT).show();
                                                                mDialog.dismiss();
                                                                Log.i(TAG, getString(R.string.connection_accepted_toast_text));
                                                            } else {
                                                                Log.e(TAG, getString(R.string.delete_notification_error));
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    Log.e(TAG, getString(R.string.delete_connection_request_error));
                                                }
                                            }
                                        });
                                    } else {
                                        Log.e(TAG, getString(R.string.add_connection_other_user_error));
                                    }
                                }
                            });
                        } else {
                            Log.e(TAG, getString(R.string.add_connection_current_user_error));
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.toString());
            }
        });
    }

    private void sendConnectionRequest(final String personId) {
        DatabaseReference newNotificationRef = mDatabaseManager.getNewNotifcationReference(personId);
        String refKey = newNotificationRef.getKey();
        Notification notification = new Notification(refKey, mCurrentUserId, NotificationType.CONNECTION_REQUEST);
        // Send connection request notification to other user
        mDatabaseManager.sendNotification(newNotificationRef, notification).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Send a connection request to the other user
                    mDatabaseManager.addUserConnectionRequest(personId).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Don't show connection button now that the connection request is pending
                                mPopupButton.setVisibility(View.GONE);
                                mConnectionPendingMessage.setVisibility(View.VISIBLE);
                                Toast.makeText(getActivity(), R.string.connection_request_sent_success, Toast.LENGTH_SHORT).show();
                                Log.i(TAG, getString(R.string.connection_request_sent_success));
                            } else {
                                Toast.makeText(getActivity(), R.string.connection_request_failed, Toast.LENGTH_SHORT).show();
                                Log.e(TAG, getString(R.string.connection_request_failed));
                            }
                        }
                    });
                } else {
                    Toast.makeText(getActivity(), R.string.connection_request_failed, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, getString(R.string.send_notification_error));
                }
            }
        });
    }

    private void deleteConnection(final Person person) {
        // Remove the connection reference from the current users database
        mDatabaseManager.deleteConnection(mCurrentUserId, person.getUserId()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Remove the connection reference from the connection users database
                    mDatabaseManager.deleteConnection(person.getUserId(), mCurrentUserId).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "You're no longer connected with " + person.getFirstName(), Toast.LENGTH_SHORT).show();
                                mDialog.dismiss();
                                Log.i(TAG, getString(R.string.delete_connection_successful));
                            } else {
                                Log.e(TAG, getString(R.string.delete_other_user_connection_error));
                            }
                        }
                    });
                } else {
                    Log.e(TAG, getString(R.string.delete_current_user_connection_error));
                }
            }
        });
    }

    private void filterPopupCreation() {
        View view = getActivity().getLayoutInflater().inflate(R.layout.filter_popup, null);
        mDialogBuilder = new AlertDialog.Builder(getActivity());

        // Set up popup UI
        Button dismissPopupButton = (Button) view.findViewById(R.id.dismiss_popup_button);
        final EditText personPosition = (EditText) view.findViewById(R.id.filter_person_position_entry);
        final EditText personCompany = (EditText) view.findViewById(R.id.filter_person_company_entry);
        final EditText personIndustry = (EditText) view.findViewById(R.id.filter_person_industry_entry);
        final EditText eventIndustry = (EditText) view.findViewById(R.id.filter_event_industry_entry);
        Button filterButton = (Button) view.findViewById(R.id.filter_button);

        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Retrieve user inputted filter
                mPersonPositionFilter = personPosition.getText().toString().toLowerCase().trim();
                mPersonCompanyFilter = personCompany.getText().toString().toLowerCase().trim();
                mPersonIndustryFilter = personIndustry.getText().toString().toLowerCase().trim();
                mEventIndustryFilter = eventIndustry.getText().toString().toLowerCase().trim();

                if (!mPersonPositionFilter.equals("") || !mPersonCompanyFilter.equals("") || !mPersonIndustryFilter.equals("")) {
                    getPeople();
                }
                if (!mEventIndustryFilter.equals("")) {
                    getEvents();
                }

                mDialog.dismiss();
            }
        });

        dismissPopupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });

        mDialogBuilder.setView(view);
        mDialog = mDialogBuilder.create();
        mDialog.show();


    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @Override
    public void userEventsUpdated() {
        getEvents();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Event event);
    }

}
