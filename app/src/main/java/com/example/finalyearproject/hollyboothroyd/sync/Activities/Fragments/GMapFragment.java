package com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.support.v4.app.Fragment;
import android.content.SharedPreferences;
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
import static android.content.Context.MODE_PRIVATE;

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

    private SharedPreferences mPreferences;

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

        mPreferences = getActivity().getApplicationContext().getSharedPreferences(Constants.preferences, MODE_PRIVATE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        mPreferences.getInt(Constants.locationTimeUpdateIntervalName, Constants.locationTimeUpdateIntervalDefault),
                        mPreferences.getInt(Constants.locationDistanceUpdateIntervalName, Constants.locationDistanceUpdateIntervalDefault),
                        mLocationListener);
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
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    mPreferences.getInt(Constants.locationTimeUpdateIntervalName, Constants.locationTimeUpdateIntervalDefault),
                    mPreferences.getInt(Constants.locationDistanceUpdateIntervalName, Constants.locationDistanceUpdateIntervalDefault),
                    mLocationListener);

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

    // Add longitude and latitude to person database
    // Update user location on the map
    @SuppressLint("MissingPermission")
    private void setUserLocation() {
        // TODO: Go through location filter
        Location lastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownLocation == null) {
            Toast.makeText(getActivity(), R.string.could_not_find_location, Toast.LENGTH_LONG).show();
        } else {
            LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            mDatabaseManager.updateCurrentUserLocation(userLocation);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, mPreferences.getInt(Constants.mapZoomLevelName, Constants.mapZoomLevelDefault)));
        }
    }

    private void getPeople() {
        //TODO: Replace with local users
        for (Person person : mLocalPeopleList) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(mPreferences.getFloat(Constants.personPinColorName, Constants.personPinColorDefault)));
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
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(mPreferences.getFloat(Constants.eventPinColorName, Constants.eventPinColorDefault)));
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
        Button attendButton = (Button) view.findViewById(R.id.popup_attend);
        TextView attendingMessage = (TextView) view.findViewById(R.id.popup_attending);

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

        if (UserEvents.EVENTS_ATTENDING_MAP.containsKey(event.getUid())) {
            // Don't show attend button if the user is already attending the event
            attendButton.setVisibility(View.GONE);
            attendingMessage.setVisibility(View.VISIBLE);
        } else {
            attendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //final Event desiredEvent = event;
                    mDatabaseManager.attendNewEvent(event).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getActivity(), "You're attending " + event.getTitle() + "!", Toast.LENGTH_LONG).show();
                                mDialog.dismiss();
                            } else {
                                Toast.makeText(getActivity(), R.string.event_attendence_unsuccessful, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
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

    private void personPopupCreation(Marker marker) {

        View view = getActivity().getLayoutInflater().inflate(R.layout.person_popup, null);

        Button dismissPopupButton = (Button) view.findViewById(R.id.dismiss_popup_button);
        ImageView personImage = (ImageView) view.findViewById(R.id.popup_image);
        TextView personName = (TextView) view.findViewById(R.id.popup_name);
        TextView personPosition = (TextView) view.findViewById(R.id.popup_position);
        TextView personCompany = (TextView) view.findViewById(R.id.popup_company);
        TextView personIndustry = (TextView) view.findViewById(R.id.popup_industry);
        Button connectButton = (Button) view.findViewById(R.id.popup_connect);
        TextView connectedMessage = (TextView) view.findViewById(R.id.popup_connected);

        final Person person = mPersonMarkerMap.get(marker.getId());
        if (!mPersonMarkerMap.isEmpty()) {
            Picasso.with(getActivity()).load(person.getImageId()).into(personImage);
        }

        personName.setText(person.getFirstName() + " " + person.getLastName());
        personPosition.setText("Position: " + person.getPosition());
        personCompany.setText("Company: " + person.getCompany());
        personIndustry.setText("Industry: " + person.getIndustry());


        if (person.getUserId().equals(String.valueOf(mAccountManager.getCurrentUser().getUid()))) {
            // Don't show connection button for the users marker popup
            connectButton.setVisibility(View.GONE);
        } else if (UserConnections.ITEM_MAP.containsKey(person.getUserId())) {
            // Don't show connection button if the person is already a connection
            connectButton.setVisibility(View.GONE);
            connectedMessage.setVisibility(View.VISIBLE);
        } else {
            connectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Get current user. This will be modified when the connection approval process is put in place
                    mDatabaseManager.getUserPeopleDatabaseReference().addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final Person user = dataSnapshot.getValue(Person.class);
                            // TODO: Add connection approval process
                            // First add other user to current user base
                            mDatabaseManager.addNewConnection(user.getUserId(), person).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        mDatabaseManager.addNewConnection(person.getUserId(), user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(getActivity(), R.string.connection_successful, Toast.LENGTH_LONG).show();
                                                    mDialog.dismiss();
                                                } else {
                                                    Toast.makeText(getActivity(), R.string.connection_unsuccessful, Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    } else {
                                        Toast.makeText(getActivity(), R.string.connection_unsuccessful, Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
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

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }
}
