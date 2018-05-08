package com.example.finalyearproject.hollyboothroyd.sync.Activities;

import com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.ConnectionFragment;
import com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.EditEventFragment;
import com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.EditProfileFragment;
import com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.LogoutFragment;
import com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.NewEvent.NewEventBasicInfoFragment;
import com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.NewEvent.NewEventLogisticsFragment;
import com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.NotificationFragment;
import com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.SettingsFragment;
import com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.ViewEventsFragment;
import com.example.finalyearproject.hollyboothroyd.sync.Activities.NewAccount.NewAccountPhotoActivity;
import com.example.finalyearproject.hollyboothroyd.sync.Model.Event;
import com.example.finalyearproject.hollyboothroyd.sync.Model.NotificationBase;
import com.example.finalyearproject.hollyboothroyd.sync.Model.UserConnections;
import com.example.finalyearproject.hollyboothroyd.sync.Model.Person;
import com.example.finalyearproject.hollyboothroyd.sync.Model.UserEvents;
import com.example.finalyearproject.hollyboothroyd.sync.Model.UserNotifications;
import com.example.finalyearproject.hollyboothroyd.sync.Services.AccountManager;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class CoreActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ConnectionFragment.OnListFragmentInteractionListener,
        LogoutFragment.OnFragmentInteractionListener, SettingsFragment.OnFragmentInteractionListener, NewEventBasicInfoFragment.OnFragmentInteractionListener,
        NewEventLogisticsFragment.OnFragmentInteractionListener, ViewEventsFragment.OnListFragmentInteractionListener,
        EditProfileFragment.OnFragmentInteractionListener, NotificationFragment.OnListFragmentInteractionListener, EditEventFragment.OnFragmentInteractionListener, GMapFragment.OnFragmentInteractionListener {

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
        if (ActivityCompat.checkSelfPermission(CoreActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(CoreActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request Location permissions
            ActivityCompat.requestPermissions(CoreActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        } else {
            mUserEvents = new UserEvents(this);
        }
        mUserNotifications = new UserNotifications(this);

        mSupportFragmentManager = getSupportFragmentManager();

        // This is needed to set the correct fragment when the orientation changes
        // If the savedInstanceState has not been set, it is the first creation of the CoreActivity
        if (savedInstanceState == null) {
            mSupportFragmentManager.beginTransaction().replace(R.id.content_frame, new GMapFragment(), getString(R.string.gmaps_tag)).commit();
            mCurrentFragment = R.string.gmaps_tag;
        } else {
            // Otherwise set the last known fragment
            mCurrentFragment = savedInstanceState.getInt(getString(R.string.current_fragment_saved_instance));
            switch (mCurrentFragment) {
                case R.string.gmaps_tag:
                    mSupportFragmentManager.beginTransaction().replace(R.id.content_frame, new GMapFragment(), getString(R.string.gmaps_tag)).commit();
                    mCurrentFragment = R.string.gmaps_tag;
                    break;
                case R.string.notifications_tag:
                    mSupportFragmentManager.beginTransaction().replace(R.id.content_frame, new NotificationFragment(), getString(R.string.notifications_tag)).commit();
                    mCurrentFragment = R.string.notifications_tag;
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
                case R.string.edit_profile_tag:
                    mSupportFragmentManager.beginTransaction().replace(R.id.content_frame, new EditProfileFragment(), getString(R.string.edit_profile_tag)).commit();
                    mCurrentFragment = R.string.edit_profile_tag;
                    break;
                case R.string.settings_tag:
                    mSupportFragmentManager.beginTransaction().replace(R.id.content_frame, new SettingsFragment(), getString(R.string.settings_tag)).commit();
                    mCurrentFragment = R.string.settings_tag;
                    break;
                case R.string.logout_tag:
                    mSupportFragmentManager.beginTransaction().replace(R.id.content_frame, new LogoutFragment(), getString(R.string.logout_tag)).commit();
                    mCurrentFragment = R.string.logout_tag;
                    break;
                default:
                    mSupportFragmentManager.beginTransaction().replace(R.id.content_frame, new GMapFragment(), getString(R.string.gmaps_tag)).commit();
                    mCurrentFragment = R.string.gmaps_tag;
            }
        }

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
        } else if (mCurrentFragment == R.string.gmaps_tag) {
            // Do nothing if the GMaps fragment is showing
        }
        // Pressing the back button does not explicitly tell which fragment originated the back button or the destination fragment
        // To keep the mCurrentFragment variable accurate, when back is pressed, the nested fragments need to be checked and mCurrentFragment reassigned
        else if (mCurrentFragment == R.string.create_event_logistics_tag) {
            mSupportFragmentManager.beginTransaction().replace(R.id.content_frame, new NewEventBasicInfoFragment(), getString(R.string.create_event_basic_info_tag)).commit();
            mCurrentFragment = R.string.create_event_basic_info_tag;
        } else if (mCurrentFragment != 0) {
            // Return to the GMaps fragment, if one of the non-nested menu fragments is showing
            mSupportFragmentManager.beginTransaction().replace(R.id.content_frame, new GMapFragment(), getString(R.string.gmaps_tag)).commit();
            mCurrentFragment = R.string.gmaps_tag;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Check if the location permissions have been granted
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mUserEvents = new UserEvents(this);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt("currentFragment", mCurrentFragment);
        super.onSaveInstanceState(savedInstanceState);
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
    public void onNewEventInfoNextButtonPressed(String eventTitle, String eventIndustry, String eventDescription, String eventImageUri) {
        // Pass new event basic info data to next fragment
        NewEventLogisticsFragment eventLogisticsFragment = new NewEventLogisticsFragment();
        Bundle args = new Bundle();
        args.putString(NewEventLogisticsFragment.ARG_TITLE, eventTitle);
        args.putString(NewEventLogisticsFragment.ARG_INDUSTRY, eventIndustry);
        args.putString(NewEventLogisticsFragment.ARG_DESCRIPTION, eventDescription);
        args.putString(NewEventLogisticsFragment.ARG_IMAGE, eventImageUri);
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
    public void onNewEventLogisticsDoneButtonPressed() {
        // After the event has been created, load up the GMaps fragment
        mSupportFragmentManager.beginTransaction().replace(R.id.content_frame, new GMapFragment(), getString(R.string.gmaps_tag)).commit();
        mCurrentFragment = R.string.gmaps_tag;
    }

    @Override
    public void onListFragmentInteraction(Event event) {
        EditEventFragment editEventFragment = new EditEventFragment();
        editEventFragment.setEvent(event);

        FragmentTransaction transaction = mSupportFragmentManager.beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.content_frame, editEventFragment, getString(R.string.edit_event_fragment));
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();

        mCurrentFragment = R.string.edit_event_fragment;
    }

    @Override
    public void onLogoutInteraction() {
        // The user has decided to logout of their account. Clear out all listeners and saved data
        clearListeners();

        mAccountManager.signUserOut();
        Toast.makeText(this, R.string.logged_out_toast, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class));
        mCurrentFragment = 0;
    }

    private void clearListeners() {
        // Clear out all listeners and saved data
        mUserEvents.clearEvents();
        mUserEvents.clearListeners();

        mUserNotifications.clearNotifications();
        mUserNotifications.clearListeners();

        mUserConnections.clearConnections();
        mUserConnections.clearListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearListeners();
    }

    @Override
    public void onEditProfileInteraction() {

    }

    @Override
    public void onListFragmentInteraction(NotificationBase item) {

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onFragmentInteraction(Event event) {
        EditEventFragment editEventFragment = new EditEventFragment();
        editEventFragment.setEvent(event);

        FragmentTransaction transaction = mSupportFragmentManager.beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.content_frame, editEventFragment, getString(R.string.edit_event_fragment));
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();

        mCurrentFragment = R.string.edit_event_fragment;
    }
}
