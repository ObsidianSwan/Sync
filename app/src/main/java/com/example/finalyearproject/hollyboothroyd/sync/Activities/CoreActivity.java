package com.example.finalyearproject.hollyboothroyd.sync.Activities;

import com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.ConnectionFragment;
import com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.EditProfileFragment;
import com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.LogoutFragment;
import com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.NewEvent.NewEventBasicInfoFragment;
import com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.NewEvent.NewEventDescriptionFragment;
import com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.NewEvent.NewEventLogisticsFragment;
import com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.NotificationFragment;
import com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.SettingsFragment;
import com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.ViewEventsFragment;
import com.example.finalyearproject.hollyboothroyd.sync.Model.Event;
import com.example.finalyearproject.hollyboothroyd.sync.Model.NotificationBase;
import com.example.finalyearproject.hollyboothroyd.sync.Model.UserConnections;
import com.example.finalyearproject.hollyboothroyd.sync.Model.Person;
import com.example.finalyearproject.hollyboothroyd.sync.Model.UserEvents;
import com.example.finalyearproject.hollyboothroyd.sync.Model.UserNotifications;
import com.example.finalyearproject.hollyboothroyd.sync.Services.AccountManager;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.GMapFragment;
import com.example.finalyearproject.hollyboothroyd.sync.R;
import com.example.finalyearproject.hollyboothroyd.sync.Services.DatabaseManager;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class CoreActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ConnectionFragment.OnListFragmentInteractionListener,
        LogoutFragment.OnFragmentInteractionListener, SettingsFragment.OnFragmentInteractionListener, NewEventBasicInfoFragment.OnFragmentInteractionListener,
        NewEventLogisticsFragment.OnFragmentInteractionListener, NewEventDescriptionFragment.OnFragmentInteractionListener, ViewEventsFragment.OnListFragmentInteractionListener,
        EditProfileFragment.OnFragmentInteractionListener, NotificationFragment.OnListFragmentInteractionListener {

    private static final String TAG = "CoreActivity";

    private DatabaseManager mDatabaseManager;
    private AccountManager mAccountManager;
    private FragmentManager mSupportFragmentManager;

    private UserEvents mUserEvents;
    private UserNotifications mUserNotifications;
    private UserConnections mUserConnections;

    private int mCurrentFragment = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabaseManager = new DatabaseManager();
        mAccountManager = new AccountManager();

        // Instantiate these so the database listener is set up and the user connections, events, and notifications lists stay up to date
        mUserConnections = new UserConnections();
        mUserEvents = new UserEvents(this);
        mUserNotifications = new UserNotifications(this);

        mSupportFragmentManager = getSupportFragmentManager();

        // Go to the GMaps fragment on start
        mSupportFragmentManager.beginTransaction().replace(R.id.content_frame, new GMapFragment(), getString(R.string.gmaps_tag)).commit();
        mCurrentFragment = R.string.gmaps_tag;

        // Set up the navigation drawer and the tool bar
        setContentView(R.layout.activity_navigation_drawer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Set up header view and components
        View header = navigationView.getHeaderView(0);

        ImageView profileImage = (ImageView) header.findViewById(R.id.nav_profile_image);
        TextView userNameText = (TextView) header.findViewById(R.id.nav_user_name);
        TextView userPositionText = (TextView) header.findViewById(R.id.nav_position);
        TextView userConnectionsText = (TextView) header.findViewById(R.id.nav_connections_number);

        setDrawerHeaderText(profileImage, userNameText, userPositionText, userConnectionsText);
    }

    private void setDrawerHeaderText(final ImageView profileImage, final TextView userNameText, final TextView userPositionText, final TextView userConnectionsText) {
        mDatabaseManager.getUserPeopleDatabaseReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Person currentUser = dataSnapshot.getValue(Person.class);
                if (currentUser != null) {
                    // Set the header text to be the users information
                    Picasso.with(CoreActivity.this).load(currentUser.getImageId()).into(profileImage);
                    userNameText.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
                    userPositionText.setText(currentUser.getPosition());

                    // Retrieve and set the number of connections the user has
                    mDatabaseManager.getUserConnectionsDatabaseReference().addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Long connectionCount = dataSnapshot.getChildrenCount();
                            String connectionText = "";
                            String completeText = "";
                            if (connectionCount < 1 || connectionCount > 1) {
                                connectionText = getString(R.string.connections_text);
                            } else if (connectionCount == 1) {
                                connectionText = getString(R.string.connection_text);
                            }
                            completeText = String.valueOf(connectionCount) + " " + connectionText;
                            userConnectionsText.setText(completeText);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e(TAG, databaseError.toString());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.toString());
            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if(mCurrentFragment == R.string.gmaps_tag){
            // Do nothing if the GMaps fragment is showing
        }
        // Pressing the back button does not explicitly tell which fragment originated the back button or the destination fragment
        // To keep the mCurrentFragment variable accurate, when back is pressed, the nested fragments need to be checked and mCurrentFragment reassigned
        else if (mCurrentFragment == R.string.create_event_logistics_tag){
            mSupportFragmentManager.beginTransaction().replace(R.id.content_frame, new NewEventBasicInfoFragment(), getString(R.string.create_event_basic_info_tag)).commit();
            mCurrentFragment = R.string.create_event_basic_info_tag;
        } else if (mCurrentFragment == R.string.create_event_description_tag) {
            mSupportFragmentManager.beginTransaction().replace(R.id.content_frame, new NewEventLogisticsFragment(), getString(R.string.create_event_logistics_tag)).commit();
            mCurrentFragment = R.string.create_event_logistics_tag;
        } else if (mCurrentFragment != 0){
            // Return to the GMaps fragment, if one of the non-nested menu fragments is showing
            mSupportFragmentManager.beginTransaction().replace(R.id.content_frame, new GMapFragment(), getString(R.string.gmaps_tag)).commit();
            mCurrentFragment = R.string.gmaps_tag;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        // Go to fragment or SyncUp activity and save which fragment the user is on
        if (id == R.id.nav_menu_map && mCurrentFragment != R.string.gmaps_tag) {
            mSupportFragmentManager.beginTransaction().replace(R.id.content_frame, new GMapFragment(), getString(R.string.gmaps_tag)).commit();
            mCurrentFragment = R.string.gmaps_tag;
        } else if (id == R.id.nav_menu_notification) {
            mSupportFragmentManager.beginTransaction().replace(R.id.content_frame, new NotificationFragment(), getString(R.string.notifications_tag)).commit();
            mCurrentFragment = R.string.notifications_tag;
        } else if (id == R.id.nav_view_connections) {
            mSupportFragmentManager.beginTransaction().replace(R.id.content_frame, new ConnectionFragment(), getString(R.string.view_connections_tag)).commit();
            mCurrentFragment = R.string.view_connections_tag;
        } else if (id == R.id.nav_sync_up) {
            startActivity(new Intent(CoreActivity.this, NFCActivity.class));
            mCurrentFragment = 0;
        } else if (id == R.id.nav_view_events) {
            mSupportFragmentManager.beginTransaction().replace(R.id.content_frame, new ViewEventsFragment(), getString(R.string.view_events_tag)).commit();
            mCurrentFragment = R.string.view_events_tag;
        } else if (id == R.id.nav_create_event) {
            mSupportFragmentManager.beginTransaction().replace(R.id.content_frame, new NewEventBasicInfoFragment(), getString(R.string.create_event_basic_info_tag)).commit();
            mCurrentFragment = R.string.create_event_basic_info_tag;
        } else if (id == R.id.nav_edit_profile) {
            mSupportFragmentManager.beginTransaction().replace(R.id.content_frame, new EditProfileFragment(), getString(R.string.edit_profile_tag)).commit();
            mCurrentFragment = R.string.edit_profile_tag;
        } else if (id == R.id.nav_settings) {
            mSupportFragmentManager.beginTransaction().replace(R.id.content_frame, new SettingsFragment(), getString(R.string.settings_tag)).commit();
            mCurrentFragment = R.string.settings_tag;
        } else if (id == R.id.nav_logout) {
            mSupportFragmentManager.beginTransaction().replace(R.id.content_frame, new LogoutFragment(), getString(R.string.logout_tag)).commit();
            mCurrentFragment = R.string.logout_tag;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onListFragmentInteraction(Person connection) {
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }

    @Override
    public void onSettingsInteraction() {
        // The user has decided to delete their account. Clear out all listeners and saved data
        clearListeners();

        // TODO delete account bug
        mAccountManager.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), R.string.delete_account_success, Toast.LENGTH_SHORT).show();
                }
            }
        });
        startActivity(new Intent(this, LoginActivity.class));
        mCurrentFragment = 0;

    }

    @Override
    public void onNewEventInfoNextButtonPressed(String eventTitle, String eventIndustry, String eventTopic) {
        // Pass new event basic info data to next fragment
        NewEventLogisticsFragment eventLogisticsFragment = new NewEventLogisticsFragment();
        Bundle args = new Bundle();
        args.putString(NewEventLogisticsFragment.ARG_TITLE, eventTitle);
        args.putString(NewEventLogisticsFragment.ARG_INDUSTRY, eventIndustry);
        args.putString(NewEventLogisticsFragment.ARG_TOPIC, eventTopic);
        eventLogisticsFragment.setArguments(args);

        FragmentTransaction transaction = mSupportFragmentManager.beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.content_frame, eventLogisticsFragment, getString(R.string.create_event_logistics_tag));
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();

        mCurrentFragment = R.string.create_event_logistics_tag;
    }

    @Override
    public void onNewEventLogisticsNextButtonPressed(String eventTitle, String eventIndustry, String eventTopic, String date, String time,
                                                     String street, String city, String state, String zipcode, String country, LatLng position) {
        // Pass new event logistics and basic info data to next fragment
        NewEventDescriptionFragment eventDescriptionFragment = new NewEventDescriptionFragment();
        Bundle args = new Bundle();
        args.putString(NewEventDescriptionFragment.ARG_TITLE, eventTitle);
        args.putString(NewEventDescriptionFragment.ARG_INDUSTRY, eventIndustry);
        args.putString(NewEventDescriptionFragment.ARG_TOPIC, eventTopic);
        args.putString(NewEventDescriptionFragment.ARG_DATE, date);
        args.putString(NewEventDescriptionFragment.ARG_TIME, time);
        args.putString(NewEventDescriptionFragment.ARG_STREET, street);
        args.putString(NewEventDescriptionFragment.ARG_CITY, city);
        args.putString(NewEventDescriptionFragment.ARG_STATE, state);
        args.putString(NewEventDescriptionFragment.ARG_ZIPCODE, zipcode);
        args.putString(NewEventDescriptionFragment.ARG_COUNTRY, country);
        args.putDouble(NewEventDescriptionFragment.ARG_LONGITUDE, position.longitude);
        args.putDouble(NewEventDescriptionFragment.ARG_LATITUDE, position.latitude);
        eventDescriptionFragment.setArguments(args);

        FragmentTransaction transaction = mSupportFragmentManager.beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.content_frame, eventDescriptionFragment, getString(R.string.create_event_description_tag));
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();

        mCurrentFragment = R.string.create_event_description_tag;
    }

    @Override
    public void onNewEventDescriptionDoneButtonPressed() {
        // After the event has been created, load up the GMaps fragment
        mSupportFragmentManager.beginTransaction().replace(R.id.content_frame, new GMapFragment(), getString(R.string.gmaps_tag)).commit();
        mCurrentFragment = R.string.gmaps_tag;
    }

    @Override
    public void onListFragmentInteraction(Event item) {
    }

    @Override
    public void onLogoutInteraction() {
        // The user has decided to logout of their account. Clear out all listeners and saved data
        clearListeners();

        mAccountManager.signUserOut();
        startActivity(new Intent(this, LoginActivity.class));
        mCurrentFragment = 0;
    }

    // TODO call this on OnDestroy
    private void clearListeners(){
        // Clear out all listeners and saved data
        mUserEvents.clearEvents();
        mUserEvents.clearListeners();

        mUserNotifications.clearNotifications();
        mUserNotifications.clearListeners();

        mUserConnections.clearConnections();
        mUserConnections.clearListeners();
    }

    @Override
    public void onEditProfileInteraction() {

    }

    @Override
    public void onListFragmentInteraction(NotificationBase item) {

    }

}
