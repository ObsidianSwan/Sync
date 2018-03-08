package com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.support.v4.app.Fragment;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalyearproject.hollyboothroyd.sync.Model.Event;
import com.example.finalyearproject.hollyboothroyd.sync.Model.Notification;
import com.example.finalyearproject.hollyboothroyd.sync.Model.NotificationBase;
import com.example.finalyearproject.hollyboothroyd.sync.Model.UserNotifications;
import com.example.finalyearproject.hollyboothroyd.sync.Services.LocationFilter;
import com.example.finalyearproject.hollyboothroyd.sync.Utils.NotificationType;
import com.example.finalyearproject.hollyboothroyd.sync.Model.Person;
import com.example.finalyearproject.hollyboothroyd.sync.Model.UserConnections;
import com.example.finalyearproject.hollyboothroyd.sync.Model.UserEvents;
import com.example.finalyearproject.hollyboothroyd.sync.R;
import com.example.finalyearproject.hollyboothroyd.sync.Services.AccountManager;
import com.example.finalyearproject.hollyboothroyd.sync.Services.DatabaseManager;
import com.example.finalyearproject.hollyboothroyd.sync.UI.CustomInfoWindow;
import com.example.finalyearproject.hollyboothroyd.sync.Utils.Constants;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
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

    private List<Person> mLocalPeopleList;
    private HashMap<String, Person> mPersonMarkerMap;

    private List<Event> mLocalEventsList;
    private HashMap<String, Event> mEventMarkerMap;

    private CustomInfoWindow mCustomInfoWindow;
    private AlertDialog.Builder mDialogBuilder;
    private AlertDialog mDialog;

    private Button mPopupButton;
    private TextView mConnectionPendingMessage;

    private float mPersonPinColor = Constants.personPinColorDefault;
    private float mEventPinColor = Constants.eventPinColorDefault;
    private int mLocationTimeUpdateInterval = Constants.locationTimeUpdateIntervalDefault;
    private int mLocationDistanceUpdateIntervalName = Constants.locationDistanceUpdateIntervalDefault;
    private int mMapZoomLevelName = Constants.mapZoomLevelDefault;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gmaps, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mDatabaseManager = new DatabaseManager();
        mAccountManager = new AccountManager();

        mLocalPeopleList = new ArrayList<>();
        mPersonMarkerMap = new HashMap<>();

        mLocalEventsList = new ArrayList<>();
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
                // TODO: Filter through LocationFilter. Add Title string
                // Add a marker to current position and move the camera
                //mMap.clear(); // Clear the map. Remove any previous markers
/*                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.addMarker(new MarkerOptions().position(userLocation).title("You"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));*/
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
                //mMap.clear();
                getPeople();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
                //mMap.clear();
                getEvents();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

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

    // Add longitude and latitude to person database
    // Update user location on the map
    @SuppressLint("MissingPermission")
    private void setUserLocation() {
        Location lastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownLocation == null) {
            Toast.makeText(getActivity(), R.string.could_not_find_location, Toast.LENGTH_LONG).show();
        } else {
            final LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            final LatLng obfuscatedLocation = LocationFilter.nRandObfuscation(userLocation, Constants.searchRadiusDefault);
            mDatabaseManager.updateCurrentUserLocation(obfuscatedLocation);
            mDatabaseManager.getUserSettings(Constants.mapZoomLevelName).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue(Integer.class) != null) {
                        mMapZoomLevelName = dataSnapshot.getValue(Integer.class);
                    }
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(obfuscatedLocation, mMapZoomLevelName));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    //TODO: Log?
                }
            });
        }
    }

    private void getPeople() {
        //TODO: Replace with local users
        for (Person person : mLocalPeopleList) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(mPersonPinColor));
            markerOptions.title(person.getFirstName() + " " + person.getLastName());
            markerOptions.position(new LatLng(person.getLatitude(), person.getLongitude()));
            markerOptions.snippet("Position: " + person.getPosition() +
                    "\nCompany: " + person.getCompany());

            Marker newMarker = mMap.addMarker(markerOptions);
            newMarker.setTag(Constants.personMarkerTag);

            // Store person data to a map to use in the mDialog and the CustomInfoWindow
            mPersonMarkerMap.put(newMarker.getId(), person);
            mCustomInfoWindow.addMarkerImage(newMarker.getId(), person.getImageId());
        }
    }

    private void getEvents() {
        //TODO: Replace with local events
        for (Event event : mLocalEventsList) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(mEventPinColor));
            markerOptions.title(event.getTitle());
            markerOptions.position(new LatLng(event.getLatitude(), event.getLongitude()));
            markerOptions.snippet("Topic: " + event.getTopic() +
                    "\nIndustry: " + event.getIndustry() +
                    "\n\nTime: " + event.getTime() +
                    "\nDate: " + event.getDate());

            Marker newMarker = mMap.addMarker(markerOptions);
            newMarker.setTag(Constants.eventMarkerTag);

            // Store person data to a map to use in the mDialog and the CustomInfoWindow
            mEventMarkerMap.put(newMarker.getId(), event);
            mCustomInfoWindow.addMarkerImage(newMarker.getId(), event.getImageId());
        }
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
        if(UserEvents.EVENTS_HOSTING_MAP.containsKey(event.getUid()) &&
                UserEvents.EVENTS_ATTENDING_MAP.containsKey(event.getUid())){
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
        else if (UserEvents.EVENTS_HOSTING_MAP.containsKey(event.getUid()) && !UserEvents.EVENTS_ATTENDING_MAP.containsKey(event.getUid())){
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

    private void attendEvent(final Event event){
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

    private void stopAttendingEvent(final Event event, final String userId){
        mDatabaseManager.deleteUserAttendingEvent(event.getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    mDatabaseManager.deleteEventAttending(event.getUid(), userId).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getActivity(), "You're no longer attending" + event.getTitle() + "!", Toast.LENGTH_SHORT).show();
                                mDialog.dismiss();
                            } else {
                                Toast.makeText(getActivity(), R.string.event_attendence_deletion_unsuccessful, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else{
                    Toast.makeText(getActivity(), R.string.event_attendence_deletion_unsuccessful, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteEvent(final Event event){
        // Delete attending references in user attending database
        mDatabaseManager.getUsersAttendingEvent(event.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String attendeeId = snapshot.getKey();
                    mDatabaseManager.deleteEventAttending(event.getUid(), attendeeId);
                    // TODO How to check if the deletions all happened successfully
                }
                // Stop hosting the event
                mDatabaseManager.deleteEventHosting(event.getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            // Delete event in event database
                            mDatabaseManager.deleteEvent(event.getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
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
          
    private void addConnection(final NotificationBase notification){
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

    private void sendConnectionRequest(final String personId){
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


    private void deleteConnection(final Person person){
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
                                    if(task.isSuccessful()){
                                        // Remove the connection reference from the connection users database
                                        mDatabaseManager.deleteUserConnection(connection.getUserId(), mAccountManager.getCurrentUser().getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
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

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }
}
