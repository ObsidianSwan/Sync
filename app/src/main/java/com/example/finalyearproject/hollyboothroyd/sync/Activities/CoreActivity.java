package com.example.finalyearproject.hollyboothroyd.sync.Activities;

import com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.ConnectionFragment;
import com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.LogoutFragment;
import com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.NewEvent.NewEventBasicInfoFragment;
import com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.NewEvent.NewEventDescriptionFragment;
import com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.NewEvent.NewEventLogisticsFragment;
import com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.SettingsFragment;
import com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.ViewEventsFragment;
import com.example.finalyearproject.hollyboothroyd.sync.Model.Event;
import com.example.finalyearproject.hollyboothroyd.sync.Model.UserConnections;
import com.example.finalyearproject.hollyboothroyd.sync.Model.Person;
import com.example.finalyearproject.hollyboothroyd.sync.Model.UserEvents;
import com.example.finalyearproject.hollyboothroyd.sync.Services.AccountManager;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
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

import com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.GMapFragment;
import com.example.finalyearproject.hollyboothroyd.sync.R;
import com.example.finalyearproject.hollyboothroyd.sync.Services.DatabaseManager;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class CoreActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ConnectionFragment.OnListFragmentInteractionListener,
        LogoutFragment.OnFragmentInteractionListener, SettingsFragment.OnFragmentInteractionListener, NewEventBasicInfoFragment.OnFragmentInteractionListener,
        NewEventLogisticsFragment.OnFragmentInteractionListener, NewEventDescriptionFragment.OnFragmentInteractionListener, ViewEventsFragment.OnListFragmentInteractionListener {

    private DatabaseManager mDatabaseManager;
    private AccountManager mAccountManager;
    private FragmentManager mSupportFragmentManager;

    private UserEvents mUserEvents;

    private int mCurrentFragment = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabaseManager = new DatabaseManager();
        mAccountManager = new AccountManager();

        UserConnections connections = new UserConnections();
        mUserEvents = new UserEvents();

        mSupportFragmentManager = getSupportFragmentManager();

        if(savedInstanceState == null){
            mSupportFragmentManager.beginTransaction().replace(R.id.content_frame, new GMapFragment(), getString(R.string.gmaps_tag)).commit();
            mCurrentFragment = R.string.gmaps_tag;
        }else{
            mCurrentFragment = savedInstanceState.getInt("currentFragment");
            switch(mCurrentFragment){
                case R.string.gmaps_tag:
                    mSupportFragmentManager.beginTransaction().replace(R.id.content_frame, new GMapFragment(), getString(R.string.gmaps_tag)).commit();
                    mCurrentFragment = R.string.gmaps_tag;
                    break;
                case R.string.view_connections_tag:
                    mSupportFragmentManager.beginTransaction().replace(R.id.content_frame, new ConnectionFragment(), getString(R.string.view_connections_tag)).commit();
                    mCurrentFragment = R.string.view_connections_tag;
                    break;
                case R.string.view_events_tag:
                    mSupportFragmentManager.beginTransaction().replace(R.id.content_frame, new ViewEventsFragment(), getString(R.string.view_events_tag)).commit();
                    mCurrentFragment = R.string.view_events_tag;
                    break;
                case R.string.create_event_basic_info_tag:
                    mSupportFragmentManager.beginTransaction().replace(R.id.content_frame, new NewEventBasicInfoFragment(), getString(R.string.create_event_basic_info_tag)).commit();
                    mCurrentFragment = R.string.create_event_basic_info_tag;
                    break;
                case R.string.settings_tag:
                    mSupportFragmentManager.beginTransaction().replace(R.id.content_frame, new SettingsFragment(), getString(R.string.settings_tag)).commit();
                    mCurrentFragment = R.string.settings_tag;
                    break;
                case R.string.logout_tag:
                    mSupportFragmentManager.beginTransaction().replace(R.id.content_frame, new LogoutFragment(), getString(R.string.logout_tag)).commit();
                    mCurrentFragment = R.string.logout_tag;
                    break;
            }
                    //TODO: Add SyncUp and edit profile
/*                case FRAGMENT_A:
                    addAreaFragment();
                    break;
                case FRAGMENT_B:
                    addFragmentB();*/

        }

        setContentView(R.layout.activity_navigation_drawer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

/*        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

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

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putInt("currentFragment", mCurrentFragment);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void setDrawerHeaderText(final ImageView profileImage, final TextView userNameText, final TextView userPositionText, final TextView userConnectionsText) {
        mDatabaseManager.getUserPeopleDatabaseReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Person currentUser = dataSnapshot.getValue(Person.class);
                if (currentUser.getImageId() != null) {
                    Picasso.with(CoreActivity.this).load(currentUser.getImageId()).into(profileImage);
                }
                userNameText.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
                userPositionText.setText(currentUser.getPosition());

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

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (isMenuFragmentShowing()) {
            mSupportFragmentManager.beginTransaction().replace(R.id.content_frame, new GMapFragment(), getString(R.string.gmaps_tag)).commit();
            mCurrentFragment = R.string.gmaps_tag;
        } else if (mSupportFragmentManager.findFragmentByTag(getString(R.string.gmaps_tag)) != null && mSupportFragmentManager.findFragmentByTag(getString(R.string.gmaps_tag)).isVisible()){
            // Do nothing
        } else {
            // Bug fix: Pressing the back button does not explicitly tell which fragment originated the back button or the destination fragment
            // To keep the mCurrentFragment variable accurate, when back is pressed, the nested fragments need to be explicitly checked and mCurrentFragment reassigned
            if(mCurrentFragment == R.string.create_event_logistics_tag){
                mCurrentFragment = R.string.create_event_basic_info_tag;
            } else if (mCurrentFragment == R.string.create_event_description_tag) {
                mCurrentFragment = R.string.create_event_logistics_tag;
            }
            super.onBackPressed();
        }
    }

    // This is needed for the custom back button behaviour.
    // It checks if one of the menu fragments (view connections, view events, create event, settings, or logout) is showing
    // If it is then return true
    private boolean isMenuFragmentShowing() {
        // Check if the fragment is not null first or the app will crash. If the fragment is not null, check if it is visible
        // TODO: add edit profile and syncup
        if(mCurrentFragment == R.string.view_connections_tag || mCurrentFragment == R.string.view_events_tag ||
                mCurrentFragment == R.string.create_event_basic_info_tag || mCurrentFragment == R.string.settings_tag ||
                mCurrentFragment == R.string.logout_tag) {
            return true;
        }
        return false;
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
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_menu_map && mCurrentFragment != R.string.gmaps_tag) {
            mSupportFragmentManager.beginTransaction().replace(R.id.content_frame, new GMapFragment(), getString(R.string.gmaps_tag)).commit();
            mCurrentFragment = R.string.gmaps_tag;
        } else if (id == R.id.nav_view_connections) {
            mSupportFragmentManager.beginTransaction().replace(R.id.content_frame, new ConnectionFragment(), getString(R.string.view_connections_tag)).commit();
            mCurrentFragment = R.string.view_connections_tag;
        } else if (id == R.id.nav_sync_up) {

        } else if (id == R.id.nav_view_events) {
            mSupportFragmentManager.beginTransaction().replace(R.id.content_frame, new ViewEventsFragment(), getString(R.string.view_events_tag)).commit();
            mCurrentFragment = R.string.view_events_tag;
        } else if (id == R.id.nav_create_event) {
            mSupportFragmentManager.beginTransaction().replace(R.id.content_frame, new NewEventBasicInfoFragment(), getString(R.string.create_event_basic_info_tag)).commit();
            mCurrentFragment = R.string.create_event_basic_info_tag;
        } else if (id == R.id.nav_edit_profile) {

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
        // When user has selected a connection in the ConnectionsFragment
        // Do something to display that article
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onNewEventInfoNextButtonPressed(String eventTitle, String eventIndustry, String eventTopic) {
        // New event basic info data pass to next fragment
        // TODO: Check if fragment is open before

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
        // New event logistics data pass to next fragment
        // TODO: Check if fragment is open before

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
    public void onNewEventDescriptionDoneButtonPressed(Double longitude, Double latitude) {
        mSupportFragmentManager.beginTransaction().replace(R.id.content_frame, new GMapFragment(), getString(R.string.gmaps_tag)).commit();
        mCurrentFragment = R.string.gmaps_tag;
    }

    @Override
    public void onListFragmentInteraction(Event item) { }

    @Override
    public void onLogoutInteraction() {
        mUserEvents.clearEvents();
        mAccountManager.signUserOut();
        startActivity(new Intent(this, LoginActivity.class));
        mCurrentFragment = 0;
    }
}
