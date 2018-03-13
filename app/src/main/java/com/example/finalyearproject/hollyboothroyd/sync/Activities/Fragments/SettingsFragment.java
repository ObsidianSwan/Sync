package com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

    private OnFragmentInteractionListener mListener;

    private final HashMap<String, Float> pinColorMap = new HashMap<String, Float>();

    private DatabaseManager mDatabaseManager;
    private AccountManager mAccountManager;

    private NumberPicker mZoomPicker;
    private NumberPicker mTimeRefreshRatePicker;
    private NumberPicker mDisRefreshRatePicker;
    private Spinner mPersonPinColorSpinner;
    private Spinner mEventPinColorSpinner;
    private Button mDeleteAccountButton;
    private Button mCommitChangesButton;


    private int mZoomValue;
    private int mTimeRefreshRateValue;
    private int mDisRefreshRateValue;
    private String mPersonPinColorValue;
    private String mEventPinColorValue;

    // Needed to prevent the spinner from executing OnItemSelected method body
    private boolean mInitialPersonSpinnerEvent = true;
    private boolean mInitialEventSpinnerEvent = true;


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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        getActivity().setTitle(R.string.settings_action_bar_title);

        mZoomPicker = (NumberPicker) view.findViewById(R.id.settings_map_zoom_number_picker);
        mTimeRefreshRatePicker = (NumberPicker) view.findViewById(R.id.settings_time_refresh_number_picker);
        mDisRefreshRatePicker = (NumberPicker) view.findViewById(R.id.settings_dis_refresh_number_picker);
        mPersonPinColorSpinner = (Spinner) view.findViewById(R.id.settings_user_color_spinner);
        mEventPinColorSpinner = (Spinner) view.findViewById(R.id.settings_event_color_spinner);
        mDeleteAccountButton = (Button) view.findViewById(R.id.delete_account_button);
        mCommitChangesButton = (Button) view.findViewById(R.id.commit_settings_button);


        setUpZoomPicker();
        setUpTimeRefreshRatePicker();
        setUpDisRefreshRatePicker();

        final ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.pin_color_array, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        setupPersonPinColorSpinner(arrayAdapter);
        setupEventPinColorSpinner(arrayAdapter);

        mCommitChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mZoomValue != mZoomPicker.getValue()) {
                    mDatabaseManager.setUserSettings(Constants.mapZoomLevelName, mZoomPicker.getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getActivity(), R.string.zoom_level_setting_change_successful, Toast.LENGTH_SHORT).show();
                            } else{
                                Toast.makeText(getActivity(), R.string.setting_change_unsuccessful_text, Toast.LENGTH_SHORT).show();
                                // TODO log
                            }
                        }
                    });
                }
                if (mTimeRefreshRateValue != mTimeRefreshRatePicker.getValue()) {
                    mDatabaseManager.setUserSettings(Constants.locationTimeUpdateIntervalName, mTimeRefreshRatePicker.getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getActivity(), R.string.time_refresh_setting_change_succesful, Toast.LENGTH_SHORT).show();
                            } else{
                                Toast.makeText(getActivity(), R.string.setting_change_unsuccessful_text, Toast.LENGTH_SHORT).show();
                                // TODO log
                            }
                        }
                    });
                }
                if (mDisRefreshRateValue != mDisRefreshRatePicker.getValue()) {
                    mDatabaseManager.setUserSettings(Constants.locationDistanceUpdateIntervalName, mDisRefreshRatePicker.getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getActivity(), R.string.distance_refresh_setting_change, Toast.LENGTH_SHORT).show();
                            } else{
                                Toast.makeText(getActivity(), R.string.setting_change_unsuccessful_text, Toast.LENGTH_SHORT).show();
                                // TODO log
                            }
                        }
                    });
                }
                if (!mPersonPinColorValue.equals(mPersonPinColorSpinner.getSelectedItem().toString())) {
                    float color = pinColorMap.get(mPersonPinColorSpinner.getSelectedItem().toString());
                    mDatabaseManager.setUserSettings(Constants.personPinColorName, color).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getActivity(), R.string.person_pin_setting_change_successful, Toast.LENGTH_SHORT).show();
                            } else{
                                Toast.makeText(getActivity(), R.string.setting_change_unsuccessful_text, Toast.LENGTH_SHORT).show();
                                // TODO log
                            }
                        }
                    });
                }
                if (!mEventPinColorValue.equals(mEventPinColorSpinner.getSelectedItem().toString())) {
                    float color = pinColorMap.get(mEventPinColorSpinner.getSelectedItem().toString());
                    mDatabaseManager.setUserSettings(Constants.eventPinColorName, color).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                           if(task.isSuccessful()){
                               Toast.makeText(getActivity(), R.string.event_pin_setting_change_successful_text, Toast.LENGTH_SHORT).show();
                           } else{
                               Toast.makeText(getActivity(), R.string.setting_change_unsuccessful_text, Toast.LENGTH_SHORT).show();
                               // TODO log
                           }
                        }
                    });
                }
            }
        });


        mDeleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        mDatabaseManager.deleteUserAttendingEvent(eventId).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mDatabaseManager.deleteEventAttending(eventId, userId);
                    // TODO check deletion happened
                } else {
                    // TODO log
                }
            }
        });
    }

    private void deleteEvent(final String eventId) {
        // Delete attending references in user attending database
        mDatabaseManager.getUsersAttendingEvent(eventId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String attendeeId = snapshot.getKey();
                    mDatabaseManager.deleteEventAttending(eventId, attendeeId);
                    // TODO How to check if the deletions all happened successfully
                }
                // Stop hosting the event
                mDatabaseManager.deleteEventHosting(eventId).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Delete event in event database
                            mDatabaseManager.deleteEvent(eventId);
                            // TODO how to check if deletion happened successfully
                        } else {
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

    private void deleteConnection(final String connectionId) {
        // Get the connection database reference from the connectionId
        mDatabaseManager.getUserConnectionReference(connectionId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String dbRef = dataSnapshot.getValue(String.class);
                // Remove the connection from the connection database
                mDatabaseManager.deleteConnection(dbRef).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Remove the connection reference from the current users database
                            mDatabaseManager.deleteUserConnection(mAccountManager.getCurrentUser().getUid(), connectionId).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Remove the connection reference from the connection users database
                                        mDatabaseManager.deleteUserConnection(connectionId, mAccountManager.getCurrentUser().getUid());
                                        // TODO check success
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
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
                // TODO:Log
            }
        });

/*        mZoomPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mDatabaseManager.setUserSettings(Constants.mapZoomLevelName, newVal).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), R.string.setting_change_successful_text, Toast.LENGTH_SHORT).show();
                        } else {
                            // TODO: add logging
                            Toast.makeText(getActivity(), R.string.setting_change_unsuccessful_text, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });*/
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
                // TODO:Log
            }
        });

/*        mTimeRefreshRatePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mDatabaseManager.setUserSettings(Constants.locationTimeUpdateIntervalName, newVal).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), R.string.setting_change_successful_text, Toast.LENGTH_SHORT).show();
                        } else {
                            // TODO: add logging
                            Toast.makeText(getActivity(), R.string.setting_change_unsuccessful_text, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });*/
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
                // TODO:Log
            }
        });

/*        mDisRefreshRatePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mDatabaseManager.setUserSettings(Constants.locationDistanceUpdateIntervalName, newVal).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), R.string.setting_change_successful_text, Toast.LENGTH_SHORT).show();
                        } else {
                            // TODO: add logging
                            Toast.makeText(getActivity(), R.string.setting_change_unsuccessful_text, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });*/
    }

    private void setupPersonPinColorSpinner(final ArrayAdapter arrayAdapter) {
        mPersonPinColorSpinner.setAdapter(arrayAdapter);
        // During initialization, set the spinner to select the users saved settings
        mDatabaseManager.getUserSettings(Constants.personPinColorName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue(Integer.class) != null) {
                    int pinColor = dataSnapshot.getValue(Integer.class);
                    mPersonPinColorValue = Util.getMapKey(pinColorMap, pinColor);
                    if (mPersonPinColorValue != null) {
                        int position = arrayAdapter.getPosition(mPersonPinColorValue);
                        mPersonPinColorSpinner.setSelection(position, false);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // TODO:Log
            }
        });
        //mPersonPinColorSpinner.setOnItemSelectedListener(this);
    }

    private void setupEventPinColorSpinner(final ArrayAdapter arrayAdapter) {
        mEventPinColorSpinner.setAdapter(arrayAdapter);
        // During initialization, set the spinner to select the users saved settings
        mDatabaseManager.getUserSettings(Constants.eventPinColorName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue(Integer.class) != null) {
                    int pinColor = dataSnapshot.getValue(Integer.class);
                    mEventPinColorValue = Util.getMapKey(pinColorMap, pinColor);
                    if (mEventPinColorValue != null) {
                        int position = arrayAdapter.getPosition(mEventPinColorValue);
                        mEventPinColorSpinner.setSelection(position, false);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // TODO:Log
            }
        });
        //mEventPinColorSpinner.setOnItemSelectedListener(this);
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

   /* @Override
    public void onItemSelected(AdapterView<?> parent, View view, final int position, final long id) {
        Spinner spinner = (Spinner) parent;
        float color = pinColorMap.get(spinner.getItemAtPosition(position));
        if (spinner.getId() == R.id.settings_user_color_spinner) {
            // Flag stops the user settings to be set during initialization and creating a toast message.
            // This is necessary due to a bug in OnItemSelected. This event is called during initialization without any user interaction
            if (!mInitialPersonSpinnerEvent) {
                mDatabaseManager.setUserSettings(Constants.personPinColorName, color).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), R.string.setting_change_successful_text, Toast.LENGTH_SHORT).show();
                        } else {
                            // TODO: add logging
                            Toast.makeText(getActivity(), R.string.setting_change_unsuccessful_text, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            mInitialPersonSpinnerEvent = false;
        } else if (spinner.getId() == R.id.settings_event_color_spinner) {
            // Flag stops the user settings to be set during initialization and creating a toast message.
            // This is necessary due to a bug in OnItemSelected. This event is called during initialization without any user interaction
            if (!mInitialEventSpinnerEvent) {
                mDatabaseManager.setUserSettings(Constants.eventPinColorName, color).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), R.string.setting_change_successful_text, Toast.LENGTH_SHORT).show();
                        } else {
                            // TODO: add logging
                            Toast.makeText(getActivity(), R.string.setting_change_unsuccessful_text, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            mInitialEventSpinnerEvent = false;
        }
    }*/

    /*@Override
    public void onNothingSelected(AdapterView<?> parent) {

    }*/

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onSettingsInteraction();
    }
}
