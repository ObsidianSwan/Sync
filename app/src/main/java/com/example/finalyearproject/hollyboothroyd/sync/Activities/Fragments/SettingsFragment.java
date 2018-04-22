package com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.finalyearproject.hollyboothroyd.sync.Model.Event;
import com.example.finalyearproject.hollyboothroyd.sync.Model.Person;
import com.example.finalyearproject.hollyboothroyd.sync.Model.UserConnections;
import com.example.finalyearproject.hollyboothroyd.sync.Model.UserEvents;
import com.example.finalyearproject.hollyboothroyd.sync.R;
import com.example.finalyearproject.hollyboothroyd.sync.Services.AccountManager;
import com.example.finalyearproject.hollyboothroyd.sync.Services.DatabaseManager;
import com.example.finalyearproject.hollyboothroyd.sync.Utils.Constants;
import com.example.finalyearproject.hollyboothroyd.sync.Utils.Util;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

// implements AdapterView.OnItemSelectedListener

public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";

    private OnFragmentInteractionListener mListener;

    private final HashMap<String, Float> pinColorMap = new HashMap<String, Float>();
    private final HashMap<String, Integer> privacyIntensityMap = new HashMap<String, Integer>();

    private DatabaseManager mDatabaseManager;
    private AccountManager mAccountManager;

    private NumberPicker mZoomPicker;
    private NumberPicker mTimeRefreshRatePicker;
    private NumberPicker mDisRefreshRatePicker;
    private NumberPicker mSearchRadiusPicker;
    private Spinner mPersonPinColorSpinner;
    private Spinner mEventPinColorSpinner;
    private Spinner mPrivacyIntensitySpinner;
    private Button mDeleteAccountButton;


    private int mZoomValue = Constants.mapZoomLevelDefault;
    private int mTimeRefreshRateValue = Constants.locationTimeUpdateIntervalDefault;
    private int mDisRefreshRateValue = Constants.locationDistanceUpdateIntervalDefault;
    private int mSearchRadiusValue = Constants.obfuscationRadiusDefault;
    private String mPrivacyIntensityValue = String.valueOf(Constants.privacyIntensityDefault);
    private String mPersonPinColorValue = String.valueOf(Constants.personPinColorDefault);
    private String mEventPinColorValue = String.valueOf(Constants.eventPinColorDefault);

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SettingsFragment.
     */
    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDatabaseManager = new DatabaseManager();
        mAccountManager = new AccountManager();

        // Populate pinColorMap
        pinColorMap.put("Azure", BitmapDescriptorFactory.HUE_AZURE);
        pinColorMap.put("Blue", BitmapDescriptorFactory.HUE_BLUE);
        pinColorMap.put("Cyan", BitmapDescriptorFactory.HUE_CYAN);
        pinColorMap.put("Green", BitmapDescriptorFactory.HUE_GREEN);
        pinColorMap.put("Magenta", BitmapDescriptorFactory.HUE_MAGENTA);
        pinColorMap.put("Orange", BitmapDescriptorFactory.HUE_ORANGE);
        pinColorMap.put("Red", BitmapDescriptorFactory.HUE_RED);
        pinColorMap.put("Rose", BitmapDescriptorFactory.HUE_ROSE);
        pinColorMap.put("Violet", BitmapDescriptorFactory.HUE_VIOLET);
        pinColorMap.put("Yellow", BitmapDescriptorFactory.HUE_YELLOW);

        // Populate privacyIntensityMap
        privacyIntensityMap.put("None", 0);
        privacyIntensityMap.put("Basic", 1);
        privacyIntensityMap.put("Intermediate", 2);
        privacyIntensityMap.put("Advanced", 3);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Set up UI
        getActivity().setTitle(R.string.settings_action_bar_title);

        mZoomPicker = (NumberPicker) view.findViewById(R.id.settings_map_zoom_number_picker);
        mTimeRefreshRatePicker = (NumberPicker) view.findViewById(R.id.settings_time_refresh_number_picker);
        mDisRefreshRatePicker = (NumberPicker) view.findViewById(R.id.settings_dis_refresh_number_picker);
        mSearchRadiusPicker = (NumberPicker) view.findViewById(R.id.settings_search_distance_picker);
        mPersonPinColorSpinner = (Spinner) view.findViewById(R.id.settings_user_color_spinner);
        mEventPinColorSpinner = (Spinner) view.findViewById(R.id.settings_event_color_spinner);
        mPrivacyIntensitySpinner = (Spinner) view.findViewById(R.id.settings_privacy_intensity_spinner);
        mDeleteAccountButton = (Button) view.findViewById(R.id.delete_account_button);
        Button commitChangesButton = (Button) view.findViewById(R.id.commit_settings_button);

        setUpZoomPicker();
        setUpTimeRefreshRatePicker();
        setUpDisRefreshRatePicker();
        setUpSearchRadiusPicker();
        setUpPinColorSpinner();
        setUpPrivacyIntensitySpinner();

        // For each settings value, check if the current value is different from the original local value
        // Then set the new value in the user's setting database
        // Then set the local value with the new value
        commitChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mZoomValue != mZoomPicker.getValue()) {
                    mDatabaseManager.setUserSettings(Constants.mapZoomLevelName, mZoomPicker.getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mZoomValue = mZoomPicker.getValue();
                                Toast.makeText(getActivity(), R.string.zoom_level_setting_change_successful, Toast.LENGTH_SHORT).show();
                                Log.i(TAG, getString(R.string.zoom_level_setting_change_successful));
                            } else {
                                Toast.makeText(getActivity(), R.string.setting_change_unsuccessful_text, Toast.LENGTH_SHORT).show();
                                Log.e(TAG, getString(R.string.setting_change_unsuccessful_text));
                            }
                        }
                    });
                }
                if (mTimeRefreshRateValue != mTimeRefreshRatePicker.getValue()) {
                    mDatabaseManager.setUserSettings(Constants.locationTimeUpdateIntervalName, mTimeRefreshRatePicker.getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mTimeRefreshRateValue = mTimeRefreshRatePicker.getValue();
                                Toast.makeText(getActivity(), R.string.time_refresh_setting_change_succesful, Toast.LENGTH_SHORT).show();
                                Log.i(TAG, getString(R.string.time_refresh_setting_change_succesful));
                            } else {
                                Toast.makeText(getActivity(), R.string.setting_change_unsuccessful_text, Toast.LENGTH_SHORT).show();
                                Log.e(TAG, getString(R.string.setting_change_unsuccessful_text));
                            }
                        }
                    });
                }
                if (mDisRefreshRateValue != mDisRefreshRatePicker.getValue()) {
                    mDatabaseManager.setUserSettings(Constants.locationDistanceUpdateIntervalName, mDisRefreshRatePicker.getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getActivity(), R.string.distance_refresh_setting_change, Toast.LENGTH_SHORT).show();
                                Log.i(TAG, getString(R.string.distance_refresh_setting_change));
                            } else {
                                Toast.makeText(getActivity(), R.string.setting_change_unsuccessful_text, Toast.LENGTH_SHORT).show();
                                Log.e(TAG, getString(R.string.setting_change_unsuccessful_text));
                            }
                        }
                    });
                }
                if (mSearchRadiusValue != mSearchRadiusPicker.getValue()) {
                    mDatabaseManager.setUserSettings(Constants.searchRadiusName, mSearchRadiusPicker.getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mSearchRadiusValue = mSearchRadiusPicker.getValue();
                                Toast.makeText(getActivity(), R.string.search_radius_setting_change, Toast.LENGTH_SHORT).show();
                                Log.i(TAG, getString(R.string.search_radius_setting_change));
                            } else {
                                Toast.makeText(getActivity(), R.string.setting_change_unsuccessful_text, Toast.LENGTH_SHORT).show();
                                Log.e(TAG, getString(R.string.setting_change_unsuccessful_text));
                            }
                        }
                    });
                }
                if (!mPersonPinColorValue.equals(mPersonPinColorSpinner.getSelectedItem().toString())) {
                    // Get pin color from the pin color map
                    float color = pinColorMap.get(mPersonPinColorSpinner.getSelectedItem().toString());
                    mDatabaseManager.setUserSettings(Constants.personPinColorName, color).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mPersonPinColorValue = mPersonPinColorSpinner.getSelectedItem().toString();
                                Toast.makeText(getActivity(), R.string.person_pin_setting_change_successful, Toast.LENGTH_SHORT).show();
                                Log.i(TAG, getString(R.string.person_pin_setting_change_successful));
                            } else {
                                Toast.makeText(getActivity(), R.string.setting_change_unsuccessful_text, Toast.LENGTH_SHORT).show();
                                Log.e(TAG, getString(R.string.setting_change_unsuccessful_text));
                            }
                        }
                    });
                }
                if (!mEventPinColorValue.equals(mEventPinColorSpinner.getSelectedItem().toString())) {
                    // Get pin color from the pin color map
                    float color = pinColorMap.get(mEventPinColorSpinner.getSelectedItem().toString());
                    mDatabaseManager.setUserSettings(Constants.eventPinColorName, color).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mEventPinColorValue = mEventPinColorSpinner.getSelectedItem().toString();
                                Toast.makeText(getActivity(), R.string.event_pin_setting_change_successful_text, Toast.LENGTH_SHORT).show();
                                Log.i(TAG, getString(R.string.event_pin_setting_change_successful_text));
                            } else {
                                Toast.makeText(getActivity(), R.string.setting_change_unsuccessful_text, Toast.LENGTH_SHORT).show();
                                Log.e(TAG, getString(R.string.setting_change_unsuccessful_text));
                            }
                        }
                    });
                }
                if (!mPrivacyIntensityValue.equals(mPrivacyIntensitySpinner.getSelectedItem().toString())) {
                    int intensity = privacyIntensityMap.get(mPrivacyIntensitySpinner.getSelectedItem().toString());
                    mDatabaseManager.setUserSettings(Constants.privacyIntensityName, intensity).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mPrivacyIntensityValue = mPrivacyIntensitySpinner.getSelectedItem().toString();
                                Toast.makeText(getActivity(), R.string.privacy_intensity_setting_change_successful, Toast.LENGTH_SHORT).show();
                                Log.i(TAG, getString(R.string.privacy_intensity_setting_change_successful));
                            } else {
                                Toast.makeText(getActivity(), R.string.setting_change_unsuccessful_text, Toast.LENGTH_SHORT).show();
                                Log.e(TAG, getString(R.string.setting_change_unsuccessful_text));
                            }
                        }
                    });
                }
            }
        });


        mDeleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Make the user press the delete account button multiple times
                // to reduce the likelihood of an accidental deletion
                if (mDeleteAccountButton.getText().toString().equals(getString(R.string.delete_account_button))) {
                    mDeleteAccountButton.setText(R.string.verify_account_deletion_text_button);
                } else if (mDeleteAccountButton.getText().toString().equals(getString(R.string.verify_account_deletion_text_button))) {
                    mDeleteAccountButton.setText(R.string.verify_account_deletion_text_two_button);
                } else if (mDeleteAccountButton.getText().toString().equals(getString(R.string.verify_account_deletion_text_two_button))) {
                    // Stop attending events
                    for (Event event : UserEvents.EVENTS_ATTENDING) {
                        stopAttendingEvent(event.getUid());
                    }
                    // Delete events hosting
                    for (Event event : UserEvents.EVENTS_HOSTING) {
                        deleteEvent(event.getUid());
                    }
                    // Delete connections
                    for (Person connection : UserConnections.CONNECTION_ITEMS) {
                        deleteConnection(connection.getUserId());
                    }
                    // Delete location
                    mDatabaseManager.getLocationDatabaseReference().child(mAccountManager.getCurrentUser().getUid()).setValue(null);
                    // Delete account database
                    mDatabaseManager.deletePerson().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Delete account in Core Activity so the appropriate clean up can occur
                                if (mListener != null) {
                                    mListener.onSettingsInteraction();
                                }
                            }
                        }
                    });
                }
            }
        });

        return view;
    }

    private void stopAttendingEvent(final String eventId) {
        final String userId = mAccountManager.getCurrentUser().getUid();
        // Remove user from event attendee DB
        mDatabaseManager.deleteEventAttending(eventId, userId);
        Log.i(TAG, getString(R.string.stop_attending_event_successful));
    }

    private void deleteEvent(final String eventId) {
        // Get all users attending an event
        mDatabaseManager.getUsersAttendingEvent(eventId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String attendeeId = snapshot.getKey();
                    // Remove users from event attendee DB
                    // TODO: check the event is no longer in other users DB
                    mDatabaseManager.deleteEventAttending(eventId, attendeeId);
                }
                // Delete event in event database
                mDatabaseManager.deleteEvent(eventId).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.i(TAG, getString(R.string.delete_event_successful));
                        } else {
                            Log.e(TAG, getString(R.string.delete_event_error));
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

    private void deleteConnection(final String connectionId) {
        final String currentUserId = mAccountManager.getCurrentUser().getUid();
        // Remove the connection reference from the connection users database
        mDatabaseManager.deleteConnection(connectionId, currentUserId).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.i(TAG, getString(R.string.delete_connection_successful));
                } else {
                    Log.e(TAG, getString(R.string.delete_other_user_connection_error));
                }
            }
        });
    }


    private void setUpZoomPicker() {
        mZoomPicker.setMinValue(Constants.zoomPickerMinValue);
        mZoomPicker.setMaxValue(Constants.zoomPickerMaxValue);
        // During initialization, set the spinner to select the users saved settings
        mDatabaseManager.getUserSettings(Constants.mapZoomLevelName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mZoomValue = 0;
                if (dataSnapshot.getValue(Integer.class) != null) {
                    mZoomValue = dataSnapshot.getValue(Integer.class);
                }
                mZoomPicker.setValue(mZoomValue);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.toString());
            }
        });
    }

    private void setUpTimeRefreshRatePicker() {
        mTimeRefreshRatePicker.setMinValue(Constants.timeRefreshRatePickerMinValue);
        mTimeRefreshRatePicker.setMaxValue(Constants.timeRefreshRatePickerMaxValue);
        // During initialization, set the spinner to select the users saved settings
        mDatabaseManager.getUserSettings(Constants.locationTimeUpdateIntervalName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mTimeRefreshRateValue = 0;
                if (dataSnapshot.getValue(Integer.class) != null) {
                    mTimeRefreshRateValue = dataSnapshot.getValue(Integer.class);
                }
                mTimeRefreshRatePicker.setValue(mTimeRefreshRateValue);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.toString());
            }
        });
    }

    private void setUpDisRefreshRatePicker() {
        mDisRefreshRatePicker.setMinValue(Constants.disRefreshRatePickerMinValue);
        mDisRefreshRatePicker.setMaxValue(Constants.disRefreshRatePickerMaxValue);
        // During initialization, set the spinner to select the users saved settings
        mDatabaseManager.getUserSettings(Constants.locationDistanceUpdateIntervalName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mDisRefreshRateValue = 0;
                if (dataSnapshot.getValue(Integer.class) != null) {
                    mDisRefreshRateValue = dataSnapshot.getValue(Integer.class);
                }
                mDisRefreshRatePicker.setValue(mDisRefreshRateValue);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.toString());
            }
        });
    }

    private void setUpSearchRadiusPicker() {
        mSearchRadiusPicker.setMinValue(Constants.searchRadiusPickerMinValue);
        mSearchRadiusPicker.setMaxValue(Constants.searchRadiusPickerMaxValue);
        // During initialization, set the spinner to select the users saved settings
        mDatabaseManager.getUserSettings(Constants.searchRadiusName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mSearchRadiusValue = Constants.obfuscationRadiusDefault;
                if (dataSnapshot.getValue(Integer.class) != null) {
                    mSearchRadiusValue = dataSnapshot.getValue(Integer.class);
                }
                mSearchRadiusPicker.setValue(mSearchRadiusValue);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.toString());
            }
        });
    }

    private void setupPersonPinColorSpinner(final ArrayAdapter arrayAdapter) {
        mPersonPinColorSpinner.setAdapter(arrayAdapter);
        // During initialization, set the spinner to select the users saved settings
        mDatabaseManager.getUserSettings(Constants.personPinColorName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(Integer.class) != null) {
                    int pinColor = dataSnapshot.getValue(Integer.class);
                    mPersonPinColorValue = Util.getMapKeyFloat(pinColorMap, pinColor);
                    if (mPersonPinColorValue != null) {
                        int position = arrayAdapter.getPosition(mPersonPinColorValue);
                        mPersonPinColorSpinner.setSelection(position, false);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.toString());
            }
        });
    }

    private void setupEventPinColorSpinner(final ArrayAdapter arrayAdapter) {
        mEventPinColorSpinner.setAdapter(arrayAdapter);
        // During initialization, set the spinner to select the users saved settings
        mDatabaseManager.getUserSettings(Constants.eventPinColorName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(Integer.class) != null) {
                    int pinColor = dataSnapshot.getValue(Integer.class);
                    mEventPinColorValue = Util.getMapKeyFloat(pinColorMap, pinColor);
                    if (mEventPinColorValue != null) {
                        int position = arrayAdapter.getPosition(mEventPinColorValue);
                        mEventPinColorSpinner.setSelection(position, false);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.toString());
            }
        });
    }

    private void setUpPinColorSpinner() {
        final ArrayAdapter<CharSequence> pinArrayAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.pin_color_array, android.R.layout.simple_spinner_item);
        pinArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        setupPersonPinColorSpinner(pinArrayAdapter);
        setupEventPinColorSpinner(pinArrayAdapter);
    }

    private void setUpPrivacyIntensitySpinner() {
        final ArrayAdapter<CharSequence> privacyArrayAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.privacy_intensity_array, android.R.layout.simple_spinner_item);
        privacyArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPrivacyIntensitySpinner.setAdapter(privacyArrayAdapter);
        // During initialization, set the spinner to select the users saved settings
        mDatabaseManager.getUserSettings(Constants.privacyIntensityName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(Integer.class) != null) {
                    int privacyIntensity = dataSnapshot.getValue(Integer.class);
                    mPrivacyIntensityValue = Util.getMapKeyInt(privacyIntensityMap, privacyIntensity);
                    if (mPrivacyIntensityValue != null) {
                        int position = privacyArrayAdapter.getPosition(mPrivacyIntensityValue);
                        mPrivacyIntensitySpinner.setSelection(position, false);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.toString());
            }
        });
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {
        void onSettingsInteraction();
    }
}
