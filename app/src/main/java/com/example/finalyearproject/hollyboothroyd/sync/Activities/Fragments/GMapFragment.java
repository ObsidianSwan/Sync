package com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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

import com.example.finalyearproject.hollyboothroyd.sync.Model.Event;
import com.example.finalyearproject.hollyboothroyd.sync.Model.Notification;
import com.example.finalyearproject.hollyboothroyd.sync.Model.NotificationBase;
import com.example.finalyearproject.hollyboothroyd.sync.Model.Person;
import com.example.finalyearproject.hollyboothroyd.sync.Model.UserConnections;
import com.example.finalyearproject.hollyboothroyd.sync.Model.UserEvents;
import com.example.finalyearproject.hollyboothroyd.sync.Model.UserNotifications;
import com.example.finalyearproject.hollyboothroyd.sync.R;
import com.example.finalyearproject.hollyboothroyd.sync.Services.AccountManager;
import com.example.finalyearproject.hollyboothroyd.sync.Services.DatabaseManager;
import com.example.finalyearproject.hollyboothroyd.sync.Services.GeofenceTransitionsIntentService;
import com.example.finalyearproject.hollyboothroyd.sync.Services.LocationFilter;
import com.example.finalyearproject.hollyboothroyd.sync.UI.CustomInfoWindow;
import com.example.finalyearproject.hollyboothroyd.sync.Utils.Constants;
import com.example.finalyearproject.hollyboothroyd.sync.Utils.NotificationType;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by hollyboothroyd on 12/11/2017.
 */

public class GMapFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener, NavigationView.OnNavigationItemSelectedListener {

    private GoogleMap mMap;

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;

    private DatabaseManager mDatabaseManager;
    private AccountManager mAccountManager;

    private LatLng mUserLocation;

    private static GeofencingClient mGeofencingClient;
    private List<Geofence> mGeofenceList;
    private static PendingIntent mGeofencePendingIntent;
    private Circle mGeofenceCircle;

    private List<Person> mLocalPeopleList;
    private List<Marker> mPersonMarkerList;
    private HashMap<String, Person> mPersonMarkerMap;

    private List<Event> mLocalEventsList;
    private List<Marker> mEventMarkerList;
    private HashMap<String, Event> mEventMarkerMap;

    private CustomInfoWindow mCustomInfoWindow;
    private AlertDialog.Builder mDialogBuilder;
    private AlertDialog mDialog;

    private Button mPopupButton;
    private TextView mConnectionPendingMessage;

    private Button mFilterButton;
    private String mPersonPositionFilter = "";
    private String mPersonCompanyFilter = "";
    private String mPersonIndustryFilter = "";
    private String mEventTopicFilter = "";
    private String mEventIndustryFilter = "";

    private float mPersonPinColor = Constants.personPinColorDefault;
    private float mEventPinColor = Constants.eventPinColorDefault;
    private int mLocationTimeUpdateInterval = Constants.locationTimeUpdateIntervalDefault;
    private int mLocationDistanceUpdateIntervalName = Constants.locationDistanceUpdateIntervalDefault;
    private int mMapZoomLevelName = Constants.mapZoomLevelDefault;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_gmaps, container, false);

        mFilterButton = (Button) view.findViewById(R.id.filter_button);
        mFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterPopupCreation();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mDatabaseManager = new DatabaseManager();
        mAccountManager = new AccountManager();

        mGeofencingClient = LocationServices.getGeofencingClient(getActivity());
        mGeofenceList = new ArrayList<>();

        mLocalPeopleList = new ArrayList<>();
        mPersonMarkerList = new ArrayList<>();
        mPersonMarkerMap = new HashMap<>();

        mLocalEventsList = new ArrayList<>();
        mEventMarkerList = new ArrayList<>();
        mEventMarkerMap = new HashMap<>();

        mDatabaseManager.getUserSettings(Constants.personPinColorName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                float value = Constants.personPinColorDefault;
                if (dataSnapshot.getValue(Integer.class) != null) {
                    value = dataSnapshot.getValue(Float.class);
                }
                mPersonPinColor = value;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseManager.getUserSettings(Constants.eventPinColorName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                float value = Constants.eventPinColorDefault;
                if (dataSnapshot.getValue(Integer.class) != null) {
                    value = dataSnapshot.getValue(Float.class);
                }
                mEventPinColor = value;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdates();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mCustomInfoWindow = new CustomInfoWindow(getActivity().getApplicationContext());
        mMap.setInfoWindowAdapter(mCustomInfoWindow);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerClickListener(this);

        mLocationManager = (LocationManager) this.getActivity().getSystemService(LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("Location: ", location.toString());
                if (getActivity() != null) {
                    updateUserLocation(location.getLatitude(), location.getLongitude(), false);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) { }

            @Override
            public void onProviderEnabled(String provider) { }

            @Override
            public void onProviderDisabled(String provider) { }
        };

        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request Location permissions
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 2);

            setUserLocation();
        } else {
            // Location permission previously granted
            requestLocationUpdates();
            setUserLocation();
        }

        DatabaseReference peopleDatabaseReference = mDatabaseManager.getPeopleDatabaseReference();
        peopleDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Person person = snapshot.getValue(Person.class);
                    //TODO: Check this updates if someone changes their photo. And removes people when they delete account
                    mLocalPeopleList.add(person);
                }
                getPeople();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // TODO Log
            }
        });

        DatabaseReference eventDatabaseReference = mDatabaseManager.getAllEventsDatabaseReference();
        //TODO: Clear listeners
        eventDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Event event = snapshot.getValue(Event.class);
                    //TODO: Check this updates if someone changes their photo. And removes people when they delete account
                    mLocalEventsList.add(event);
                }
                getEvents();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // TODO LOG
            }
        });

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver, new IntentFilter("geofenceEnterTriggered"));
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String userId = intent.getStringExtra("userId");

        }

    };

    private void requestLocationUpdates() {
        // Create location update request with the last known good value or default value.
        // When the database calls are returned, then the users current saved settings will be used instead.
        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    mLocationTimeUpdateInterval,
                    mLocationDistanceUpdateIntervalName,
                    mLocationListener);
        } catch (SecurityException ex) {
            //TODO:log
        }
        mDatabaseManager.getUserSettings(Constants.locationTimeUpdateIntervalName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(Integer.class) != null) {
                    mLocationTimeUpdateInterval = dataSnapshot.getValue(Integer.class);
                }
                mDatabaseManager.getUserSettings(Constants.locationDistanceUpdateIntervalName).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue(Integer.class) != null) {
                            mLocationDistanceUpdateIntervalName = dataSnapshot.getValue(Integer.class);
                        }
                        try {
                            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                    mLocationTimeUpdateInterval,
                                    mLocationDistanceUpdateIntervalName,
                                    mLocationListener);
                        } catch (SecurityException ex) {
                            //TODO:log
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //TODO: Log?
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //TODO: Log?
            }
        });
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        // Set trigger type to INITIAL_TRIGGER_DWELL to reduce 'alert spam' if users briefly enter or
        // exit the geofence
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL);
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
        Location lastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownLocation == null) {
            Toast.makeText(getActivity(), R.string.could_not_find_location, Toast.LENGTH_LONG).show();
        } else {
            updateUserLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), true);
        }
    }

    private void updateUserLocation(double latitude, double longitude, final boolean animateMap) {
        mUserLocation = new LatLng(latitude, longitude);
        final LatLng obfuscatedLocation = LocationFilter.nRandObfuscation(mUserLocation, Constants.obfuscationRadiusDefault);

        // Send obfuscated location to the database for other users to see
        mDatabaseManager.updateCurrentUserLocation(obfuscatedLocation);

        // Use actual location for geofence and zoom location
        setUpGeofence(mUserLocation);
        mDatabaseManager.getUserSettings(Constants.mapZoomLevelName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(Integer.class) != null) {
                    mMapZoomLevelName = dataSnapshot.getValue(Integer.class);
                }
                if (animateMap) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mUserLocation, mMapZoomLevelName));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //TODO: Log?
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void setUpGeofence(LatLng userLocation) {
        // Remove any existing geofences
        removeGeofences();

        // Create new geofence for the current users location
        mGeofenceList.add(new Geofence.Builder()
                .setRequestId(mAccountManager.getCurrentUser().getUid())
                .setCircularRegion(
                        userLocation.latitude,
                        userLocation.longitude,
                        Constants.geofenceRadiusDefault
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());

        // Add the geofence to the client
        mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences added
                        // TODO:Log
                    }
                })
                .addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to add geofences
                        // TODO:log
                    }
                });

        CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(userLocation.latitude, userLocation.longitude))
                .radius(Constants.geofenceRadiusDefault)
                .fillColor(Constants.geofenceCircleColor)
                .strokeColor(Color.GRAY);
        mGeofenceCircle = mMap.addCircle(circleOptions);
    }

    //TODO !!!! Clean up when the user logs out
    private void removeGeofences() {
        if (mGeofenceList != null) {
            mGeofenceList.clear();
            if (mGeofencePendingIntent != null) {
                mGeofencingClient.removeGeofences(mGeofencePendingIntent)
                        .addOnSuccessListener(getActivity(), new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Geofences removed
                                // ...
                            }
                        })
                        .addOnFailureListener(getActivity(), new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Failed to remove geofences
                                // ...
                            }
                        });
            }
        }
        if (mGeofenceCircle != null) {
            mGeofenceCircle.remove();
        }
    }

    @SuppressLint("MissingPermission")
    private void getPeople() {
        //TODO: Replace with local users
        clearPeopleMarkers();

        String currentUserId = mAccountManager.getCurrentUser().getUid();
        for (Person person : mLocalPeopleList) {
            if(meetsPeopleFilteringRequirements(person) || person.getUserId().equals(currentUserId)) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(mPersonPinColor));
                markerOptions.title(person.getFirstName() + " " + person.getLastName());
                if (person.getUserId().equals(currentUserId)) {
                    // Set current users pin to actual location and not the obfuscated location
                    Location lastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    markerOptions.position(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()));
                } else {
                    markerOptions.position(new LatLng(person.getLatitude(), person.getLongitude()));
                }
                markerOptions.snippet("Position: " + person.getPosition() +
                        "\nCompany: " + person.getCompany());

                Marker newMarker = mMap.addMarker(markerOptions);
                newMarker.setTag(Constants.personMarkerTag);

                // Store person data to a map to use in the mDialog and the CustomInfoWindow
                // Save person markers in a map to be able to clear person markers individually or as a group separate from the events markers
                mPersonMarkerMap.put(newMarker.getId(), person);
                mPersonMarkerList.add(newMarker);
                mCustomInfoWindow.addMarkerImage(newMarker.getId(), person.getImageId());
            }
        }
    }

    private void clearPeopleMarkers() {
        if (mPersonMarkerList != null && mPersonMarkerMap != null) {
            mPersonMarkerMap.clear();
            for (Marker marker : mPersonMarkerList) {
                marker.remove();
            }
            mPersonMarkerList.clear();
        }
    }

    private boolean meetsPeopleFilteringRequirements(Person person) {
        boolean meetsFilteringRequirements = true;
        // If there are no filtering requirements, then the person automatically meets the requirements
        if(mPersonPositionFilter.equals("") && mPersonCompanyFilter.equals("") && mPersonIndustryFilter.equals("")){
            meetsFilteringRequirements = true;
        } // If the person position filter has been specified and the person matches that position, then the person meets the requirements
        else if(!mPersonPositionFilter.equals("") && person.getPosition().toLowerCase().equals(mPersonPositionFilter)){

        } // If the person company filter has been specified and the person matches that company, then the person meets the requirements
        else if (!mPersonCompanyFilter.equals("") && person.getCompany().toLowerCase().equals(mPersonCompanyFilter)){
            meetsFilteringRequirements = true;
        }// If the person industry filter has been specified and the person matches that industry, then the person meets the requirements
        else if (!mPersonIndustryFilter.equals("") && person.getIndustry().toLowerCase().equals(mPersonIndustryFilter)){
            meetsFilteringRequirements = true;
        } // If the person position, company, or industry filter has been specified, but the person does not match, then the person does not meet the requirements
        else {
            meetsFilteringRequirements = false;
        }
        return meetsFilteringRequirements;
    }

    private void getEvents() {
        clearEventMarkers();

        for (Event event : mLocalEventsList) {
            LatLng eventLocation = new LatLng(event.getLatitude(), event.getLongitude());
            if(LocationFilter.eventWithinRange(mUserLocation, eventLocation)
                    || UserEvents.EVENTS_HOSTING_MAP.containsKey(event.getUid())
                    || UserEvents.EVENTS_ATTENDING_MAP.containsKey(event.getUid())){
                if(meetsEventFilteringRequirements(event)) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(mEventPinColor));
                    markerOptions.title(event.getTitle());
                    markerOptions.position(eventLocation);
                    markerOptions.snippet("Topic: " + event.getTopic() +
                            "\nIndustry: " + event.getIndustry() +
                            "\n\nTime: " + event.getTime() +
                            "\nDate: " + event.getDate());

                    Marker newMarker = mMap.addMarker(markerOptions);
                    newMarker.setTag(Constants.eventMarkerTag);

                    // Store event data to a map to use in the mDialog and the CustomInfoWindow
                    // Save event markers in a map to be able to clear event markers individually or as a group separate from the people markers
                    mEventMarkerMap.put(newMarker.getId(), event);
                    mEventMarkerList.add(newMarker);
                    mCustomInfoWindow.addMarkerImage(newMarker.getId(), event.getImageId());
                }
            }
        }
    }

    private void clearEventMarkers() {
        if (mEventMarkerList != null && mEventMarkerMap != null) {
            mEventMarkerMap.clear();
            for (Marker marker : mEventMarkerList) {
                marker.remove();
            }
            mEventMarkerList.clear();
        }
    }

    private boolean meetsEventFilteringRequirements(Event event) {
        boolean meetsFilteringRequirements = true;
        // If there are no filtering requirements, then the event automatically meets the requirements
        if(mEventTopicFilter.equals("") && mEventIndustryFilter.equals("")){
            meetsFilteringRequirements = true;
        } // If the event topic filter has been specified and the event matches that topic, then the event meets the requirements
        else if(!mEventTopicFilter.equals("") && event.getTopic().toLowerCase().equals(mEventTopicFilter)){

        } // If the event industry filter has been specified and the event matches that industry, then the event meets the requirements
        else if (!mEventIndustryFilter.equals("") && event.getIndustry().toLowerCase().equals(mEventIndustryFilter)){
            meetsFilteringRequirements = true;
        } // If the event topic or industry filter has been specified, but the event does not match, then the event does not meet the requirements
        else {
            meetsFilteringRequirements = false;
        }
        return meetsFilteringRequirements;
    }

    @Override
    public void onInfoWindowClick(final Marker marker) {
        mDialogBuilder = new AlertDialog.Builder(getActivity());

        if (marker.getTag() == Constants.personMarkerTag) {
            personPopupCreation(marker);
        } else if (marker.getTag() == Constants.eventMarkerTag) {
            eventPopupCreation(marker);
        }
    }

    private void eventPopupCreation(Marker marker) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.event_popup, null);

        Button dismissPopupButton = (Button) view.findViewById(R.id.dismiss_popup_button);
        ImageView eventImage = (ImageView) view.findViewById(R.id.popup_image);
        TextView eventTitle = (TextView) view.findViewById(R.id.popup_title);
        TextView eventTopic = (TextView) view.findViewById(R.id.popup_topic);
        TextView eventIndustry = (TextView) view.findViewById(R.id.popup_industry);
        TextView eventDate = (TextView) view.findViewById(R.id.popup_date);
        TextView eventTime = (TextView) view.findViewById(R.id.popup_time);
        TextView eventDescription = (TextView) view.findViewById(R.id.popup_description);
        Button eventButton = (Button) view.findViewById(R.id.popup_event_button);
        Button eventButton2 = (Button) view.findViewById(R.id.popup_event_button2);

        final Event event = mEventMarkerMap.get(marker.getId());
        if (!mEventMarkerMap.isEmpty()) {
            Picasso.with(getActivity()).load(event.getImageId()).into(eventImage);
        }

        eventTitle.setText(event.getTitle());
        eventTopic.setText("Topic: " + event.getTopic());
        eventIndustry.setText("Industry: " + event.getIndustry());
        eventDate.setText("Date: " + event.getDate());
        eventTime.setText("Time: " + event.getTime());
        eventDescription.setText(event.getDescription());

        // The user is both hosting and attending the event
        if (UserEvents.EVENTS_HOSTING_MAP.containsKey(event.getUid()) &&
                UserEvents.EVENTS_ATTENDING_MAP.containsKey(event.getUid())) {
            eventButton.setText(R.string.stop_attending_button_text);
            eventButton2.setText(R.string.delete_event_button_text);
            eventButton2.setVisibility(View.VISIBLE);

            // Stop attending the event if the user is already attending
            eventButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String userId = mAccountManager.getCurrentUser().getUid();
                    stopAttendingEvent(event, userId);
                }
            });

            // Delete event if the user is hosting the event
            eventButton2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteEvent(event);
                }
            });

        } // The user is hosting, but not attending the event
        else if (UserEvents.EVENTS_HOSTING_MAP.containsKey(event.getUid()) && !UserEvents.EVENTS_ATTENDING_MAP.containsKey(event.getUid())) {
            eventButton2.setText(R.string.delete_event_button_text);
            eventButton2.setVisibility(View.VISIBLE);

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

        } // The user is attending, but not hosting the event
        else if (UserEvents.EVENTS_ATTENDING_MAP.containsKey(event.getUid()) &&
                !UserEvents.EVENTS_HOSTING_MAP.containsKey(event.getUid())) {
            final String userId = mAccountManager.getCurrentUser().getUid();

            // Stop attending the event if the user is already attending
            eventButton.setText(R.string.stop_attending_button_text);
            eventButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopAttendingEvent(event, userId);
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
        mDatabaseManager.addUserAttendingEvent(event.getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mDatabaseManager.addEventAttending(event.getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getActivity(), "You're attending " + event.getTitle() + "!", Toast.LENGTH_LONG).show();
                                mDialog.dismiss();
                            } else {
                                Toast.makeText(getActivity(), R.string.event_attendence_unsuccessful, Toast.LENGTH_LONG).show();
                                // TODO: Remove the added previous db entry
                            }
                        }
                    });

                } else {
                    Toast.makeText(getActivity(), R.string.event_attendence_unsuccessful, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void stopAttendingEvent(final Event event, final String userId) {
        mDatabaseManager.deleteUserAttendingEvent(event.getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mDatabaseManager.deleteEventAttending(event.getUid(), userId).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getActivity(), "You're no longer attending" + event.getTitle() + "!", Toast.LENGTH_SHORT).show();
                                mDialog.dismiss();
                            } else {
                                Toast.makeText(getActivity(), R.string.event_attendence_deletion_unsuccessful, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(getActivity(), R.string.event_attendence_deletion_unsuccessful, Toast.LENGTH_SHORT).show();
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
                    // TODO How to check if the deletions all happened successfully
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
                                    } else {
                                        Toast.makeText(getActivity(), R.string.delete_event_unsuccessful, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(getActivity(), R.string.delete_event_unsuccessful, Toast.LENGTH_SHORT).show();
                            // TODO LOg
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void personPopupCreation(Marker marker) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.person_popup, null);

        Button dismissPopupButton = (Button) view.findViewById(R.id.dismiss_popup_button);
        ImageView personImage = (ImageView) view.findViewById(R.id.popup_image);
        TextView personName = (TextView) view.findViewById(R.id.popup_name);
        TextView personPosition = (TextView) view.findViewById(R.id.popup_position);
        TextView personCompany = (TextView) view.findViewById(R.id.popup_company);
        TextView personIndustry = (TextView) view.findViewById(R.id.popup_industry);
        mPopupButton = (Button) view.findViewById(R.id.popup_button);
        mConnectionPendingMessage = (TextView) view.findViewById(R.id.popup_connection_pending);


        final Person person = mPersonMarkerMap.get(marker.getId());
        if (!mPersonMarkerMap.isEmpty()) {
            Picasso.with(getActivity()).load(person.getImageId()).into(personImage);
        }

        personName.setText(person.getFirstName() + " " + person.getLastName());
        personPosition.setText("Position: " + person.getPosition());
        personCompany.setText("Company: " + person.getCompany());
        personIndustry.setText("Industry: " + person.getIndustry());

        // This is the current users popup
        if (person.getUserId().equals(String.valueOf(mAccountManager.getCurrentUser().getUid()))) {
            // Don't show connection button for the users marker popup
            mPopupButton.setVisibility(View.GONE);
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
        if (!person.getUserId().equals(String.valueOf(mAccountManager.getCurrentUser().getUid()))) {
            DatabaseReference newNotificationRef = mDatabaseManager.getNewNotifcationReference(person.getUserId());
            String refKey = newNotificationRef.getKey();
            Notification notification = new Notification(refKey, mAccountManager.getCurrentUser().getUid(), NotificationType.PROFILE_VIEW);
            mDatabaseManager.sendNotification(newNotificationRef, notification).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        //TODO log
                    } else {
                        //TODO log
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
                final String currentUserId = mAccountManager.getCurrentUser().getUid();
                // Create a new connection item in the connection database
                DatabaseReference connectionRef = mDatabaseManager.getNewConnectionReference();
                final String dbRef = connectionRef.getKey();
                //Connection connection = new Connection(dbRef, notification.getId(), currentUserId);
                mDatabaseManager.addNewConnection(connectionRef, notification.getId(), currentUserId).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Add connection reference key to current users database
                            mDatabaseManager.addConnectionReference(dbRef, notification.getId(), currentUserId).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Add connection reference key to other users database
                                        mDatabaseManager.addConnectionReference(dbRef, currentUserId, notification.getId()).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                                                                        } else {
                                                                            // TODO LOG
                                                                        }
                                                                    }
                                                                });
                                                            } else {
                                                                // TODO: LOG
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    //TODO:Log
                                                }
                                            }
                                        });
                                    } else {
                                        //TODO:Log
                                    }
                                }
                            });
                        } else {
                            //TODO: Log
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //TODO:Log
            }
        });
    }

    private void sendConnectionRequest(final String personId) {
        DatabaseReference newNotificationRef = mDatabaseManager.getNewNotifcationReference(personId);
        String refKey = newNotificationRef.getKey();
        Notification notification = new Notification(refKey, mAccountManager.getCurrentUser().getUid(), NotificationType.CONNECTION_REQUEST);
        mDatabaseManager.sendNotification(newNotificationRef, notification).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Save reference to the request so the other users the current user has requested is known
                    mDatabaseManager.addUserConnectionRequest(personId).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Don't show connection button now that the connection request is pending
                                mPopupButton.setVisibility(View.GONE);
                                mConnectionPendingMessage.setVisibility(View.VISIBLE);
                                Toast.makeText(getActivity(), R.string.connection_request_sent_success, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), R.string.connection_request_failed, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(getActivity(), R.string.connection_request_failed, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteConnection(final Person person) {
        final Person connection = UserConnections.CONNECTION_ITEM_MAP.get(person.getUserId());

        // Get the connection database reference from the connectionId
        mDatabaseManager.getUserConnectionReference(connection.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String dbRef = dataSnapshot.getValue(String.class);
                // Remove the connection from the connection database
                mDatabaseManager.deleteConnection(dbRef).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Remove the connection reference from the current users database
                            mDatabaseManager.deleteUserConnection(mAccountManager.getCurrentUser().getUid(), connection.getUserId()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Remove the connection reference from the connection users database
                                        mDatabaseManager.deleteUserConnection(connection.getUserId(), mAccountManager.getCurrentUser().getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(getContext(), "You're no longer connected with " + person.getFirstName(), Toast.LENGTH_SHORT).show();
                                                    mDialog.dismiss();
                                                } else {
                                                    // TODO: Log
                                                }
                                            }
                                        });
                                    } else {
                                        // TODO: Log
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(getContext(), R.string.cannot_disconnect_toast_text, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void filterPopupCreation() {
        View view = getActivity().getLayoutInflater().inflate(R.layout.filter_popup, null);
        mDialogBuilder = new AlertDialog.Builder(getActivity());

        Button dismissPopupButton = (Button) view.findViewById(R.id.dismiss_popup_button);
        final EditText personPosition = (EditText) view.findViewById(R.id.filter_person_position_entry);
        final EditText personCompany = (EditText) view.findViewById(R.id.filter_person_company_entry);
        final EditText personIndustry = (EditText) view.findViewById(R.id.filter_person_industry_entry);
        final EditText eventTopic = (EditText) view.findViewById(R.id.filter_event_topic_entry);
        final EditText eventIndustry = (EditText) view.findViewById(R.id.filter_event_industry_entry);
        Button filterButton = (Button) view.findViewById(R.id.filter_button);

        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPersonPositionFilter = personPosition.getText().toString().toLowerCase().trim();
                mPersonCompanyFilter = personCompany.getText().toString().toLowerCase().trim();
                mPersonIndustryFilter = personIndustry.getText().toString().toLowerCase().trim();
                mEventTopicFilter = eventTopic.getText().toString().toLowerCase().trim();
                mEventIndustryFilter = eventIndustry.getText().toString().toLowerCase().trim();

                if(mPersonPositionFilter.equals("") ||  mPersonCompanyFilter.equals("") || mPersonIndustryFilter.equals("")){
                    getPeople();
                }
                if(mEventTopicFilter.equals("") ||  mEventIndustryFilter.equals("")){
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

}
