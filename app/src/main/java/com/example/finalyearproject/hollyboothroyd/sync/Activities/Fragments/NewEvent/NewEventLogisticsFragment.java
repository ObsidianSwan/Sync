package com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.NewEvent;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.finalyearproject.hollyboothroyd.sync.Model.Event;
import com.example.finalyearproject.hollyboothroyd.sync.R;
import com.example.finalyearproject.hollyboothroyd.sync.Services.AccountManager;
import com.example.finalyearproject.hollyboothroyd.sync.Services.DatabaseManager;
import com.example.finalyearproject.hollyboothroyd.sync.Utils.Constants;
import com.example.finalyearproject.hollyboothroyd.sync.Utils.Util;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NewEventLogisticsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class NewEventLogisticsFragment extends Fragment {

    private static final String TAG = "NewEventLogFragment";

    public static final String ARG_TITLE = "title";
    public static final String ARG_INDUSTRY = "industry";
    public static final String ARG_DESCRIPTION = "description";
    public static final String ARG_IMAGE = "image";

    private String mTitle;
    private String mIndustry;
    private String mDescription;
    private String mImageUri;

    private EditText mDate;
    private EditText mTime;
    private int mDay, mMonth, mYear, mMinute, mHour;

    private EditText mStreet;
    private EditText mCity;
    private EditText mState;
    private EditText mZipCode;
    private EditText mCountry;

    private Button mDoneButton;

    private View mProgressView;
    private View mEventLogisticsView;
    private View mFrameLayoutView;
    private View mDoneButtonView;

    private AccountManager mAccountManager;
    private DatabaseManager mDatabaseManager;

    private OnFragmentInteractionListener mListener;

    public NewEventLogisticsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Retrieve previously inputted event data
            mTitle = getArguments().getString(ARG_TITLE);
            mIndustry = getArguments().getString(ARG_INDUSTRY);
            mDescription = getArguments().getString(ARG_DESCRIPTION);
            mImageUri = getArguments().getString(ARG_IMAGE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_event_logistics, container, false);

        // Set up UI
        getActivity().setTitle(getString(R.string.new_event_action_bar_title));

        mAccountManager = new AccountManager();
        mDatabaseManager = new DatabaseManager();

        mProgressView = view.findViewById(R.id.event_logistics_progress);
        mEventLogisticsView = view.findViewById(R.id.event_logistics_form);
        mFrameLayoutView = view.findViewById(R.id.new_event_logistics_frame_layout);
        mDoneButtonView = view.findViewById(R.id.event_logistics_done_button);

        Button datePicker = (Button) view.findViewById(R.id.new_event_date_button);
        Button timePicker = (Button) view.findViewById(R.id.new_event_time_button);

        mDate = (EditText) view.findViewById(R.id.new_event_date_input_text);
        mTime = (EditText) view.findViewById(R.id.new_event_time_input_text);

        mStreet = (EditText) view.findViewById(R.id.new_event_location_street_text);
        mCity = (EditText) view.findViewById(R.id.new_event_location_city);
        mState = (EditText) view.findViewById(R.id.new_event_location_state);
        mZipCode = (EditText) view.findViewById(R.id.new_event_location_zipcode);
        mCountry = (EditText) view.findViewById(R.id.new_event_location_country);

        mDoneButton = (Button) view.findViewById(R.id.event_logistics_done_button);

        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                mYear = calendar.get(Calendar.YEAR);
                mMonth = calendar.get(Calendar.MONTH);
                mDay = calendar.get(Calendar.DAY_OF_MONTH);

                // Launch Date Picker Dialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // Display Selected date in textbox
                                String dayString;
                                String monthString;
                                // Format date in desired format
                                if (dayOfMonth < 10) {
                                    dayString = "0" + Integer.toString(dayOfMonth);
                                } else {
                                    dayString = Integer.toString(dayOfMonth);
                                }
                                if (monthOfYear < 10) {
                                    monthString = "0" + Integer.toString(monthOfYear + 1);
                                } else {
                                    monthString = Integer.toString(monthOfYear + 1);
                                }
                                mDate.setText(dayString + "-" + monthString + "-" + year);

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        mDate.addTextChangedListener(new TextWatcher() {
            int previousLength = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                previousLength = mDate.getText().toString().length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int length = s.length();
                // Format date in desired format
                if ((previousLength < length) && (length == 2 || length == 5)) {
                    s.append("-");
                }
            }
        });

        timePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                mHour = calendar.get(Calendar.HOUR_OF_DAY);
                mMinute = calendar.get(Calendar.MINUTE);

                // Launch Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
                                // Display Selected time in textbox
                                String hourString;
                                String minuteString;
                                // Format time in desired format
                                if (hourOfDay < 10) {
                                    hourString = "0" + Integer.toString(hourOfDay);
                                } else {
                                    hourString = Integer.toString(hourOfDay);
                                }
                                if (minute < 10) {
                                    minuteString = "0" + Integer.toString(minute);
                                } else {
                                    minuteString = Integer.toString(minute);
                                }
                                mTime.setText(hourString + ":" + minuteString);
                            }
                        }, mHour, mMinute, true);
                timePickerDialog.show();
            }
        });

        mTime.addTextChangedListener(new TextWatcher() {
            int previousLength = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                previousLength = mTime.getText().toString().length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                int length = s.length();
                // Format date in desired format
                if ((previousLength < length) && (length == 2)) {
                    s.append(":");
                }
            }
        });

        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String date = mDate.getText().toString().trim();
                String time = mTime.getText().toString().trim();
                String street = mStreet.getText().toString().trim();
                String city = mCity.getText().toString().trim();
                String state = mState.getText().toString().trim();
                String zipcode = mZipCode.getText().toString().trim();
                String country = mCountry.getText().toString().trim();

                // Find coordinates of location to be able to place location on map
                String completeAddress = street + ", " + city + ", " + state + ", " + zipcode + ", " + country;
                LatLng position = Util.getLocationFromAddress(getContext(), completeAddress);

                // Perform basic input validation
                if (areEntriesValid(date, time, street, city, state, zipcode, country, position)) {
                    registerEvent(date, time, street, city, state, zipcode, country, position);
                }
            }
        });
        return view;
    }

    private void registerEvent(final String date, final String time, final String street, final String city, final String state,
                               final String zipcode, final String country, final LatLng position) {
        showProgress(true);

        // Upload the event image to the database
        mDatabaseManager.uploadEventImage(Uri.parse(mImageUri)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                // Get a URL to the uploaded content
                if (taskSnapshot.getDownloadUrl() != null) {
                    String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                    // Register the event
                    registerEventInternal(date, time, street, city, state, zipcode, country, position, downloadUrl);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Adding the events's image was not successful
                Toast.makeText(getActivity(), R.string.generic_event_creation_failed, Toast.LENGTH_SHORT).show();
                showProgress(false);
                Log.e(TAG, getString(R.string.generic_event_creation_failed));
            }
        });
    }

    private void registerEventInternal(final String date, final String time, final String street, final String city, final String state,
                                       final String zipcode, final String country, final LatLng position, final String downloadUrl) {
        final String userId = mAccountManager.getCurrentUser().getUid();
        // Add the event to the Firebase Database with the image storage reference

        DatabaseReference newEventRef = mDatabaseManager.getNewEventReference();
        final String refKey = newEventRef.getKey();
        Event event = new Event(refKey, mTitle, mIndustry, date, time, street,
                city, state, zipcode, country, position.longitude, position.latitude, mDescription, downloadUrl, userId);

        // Add the event to the event database
        mDatabaseManager.addEvent(newEventRef, event).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                showProgress(false);
                if (task.isSuccessful()) {
                    // Add the event to the user's events created database
                    mDatabaseManager.addEventCreator(refKey, userId).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getActivity(), R.string.event_creation_successful, Toast.LENGTH_SHORT).show();
                                if (mListener != null) {
                                    // Inform the CoreActivity to handle the remainder of the event creation
                                    mListener.onNewEventLogisticsDoneButtonPressed();
                                }
                            } else {
                                Toast.makeText(getActivity(), R.string.generic_event_creation_failed, Toast.LENGTH_SHORT).show();
                                showProgress(false);
                                Log.e(TAG, getString(R.string.add_event_creator_failed));
                            }
                        }
                    });
                } else {
                    Toast.makeText(getActivity(), R.string.generic_event_creation_failed, Toast.LENGTH_SHORT).show();
                    showProgress(false);
                    Log.e(TAG, getString(R.string.add_event_failed));
                }
            }
        });
    }

    private boolean areEntriesValid(String date, String time, String street, String city, String state, String zipcode, String country, LatLng position) {
        // Reset errors.
        mDate.setError(null);
        mTime.setError(null);
        mStreet.setError(null);
        mCity.setError(null);
        mState.setError(null);
        mZipCode.setError(null);
        mCountry.setError(null);

        View focusView = null;

        // Check for a valid date
        if (TextUtils.isEmpty(date)) {
            mDate.setError(getString(R.string.error_field_required));
            focusView = mDate;
        } else if (!Util.isDateValid(date)) {
            mDate.setError(getString(R.string.error_invalid_date));
            focusView = mDate;
        }

        // Check for a valid time
        if (TextUtils.isEmpty(time)) {
            mTime.setError(getString(R.string.error_field_required));
            focusView = mTime;
        } else if (!Util.isTimeValid(time)) {
            mTime.setError(getString(R.string.error_invalid_time));
            focusView = mTime;
        }
        // Check for a valid event address
        if (TextUtils.isEmpty(street)) {
            mStreet.setError(getString(R.string.error_field_required));
            focusView = mStreet;
        } else if (TextUtils.isEmpty(city)) {
            mCity.setError(getString(R.string.error_field_required));
            focusView = mCity;
        } else if (TextUtils.isEmpty(state)) {
            mState.setError(getString(R.string.error_field_required));
            focusView = mState;
        } else if (TextUtils.isEmpty(zipcode)) {
            mZipCode.setError(getString(R.string.error_field_required));
            focusView = mZipCode;
        } else if (TextUtils.isEmpty(country)) {
            mCountry.setError(getString(R.string.error_field_required));
            focusView = mCountry;
        }

        if (position == null) {
            mCountry.setError(getString(R.string.enter_valid_address));
            mZipCode.setError(getString(R.string.enter_valid_address));
            mState.setError(getString(R.string.enter_valid_address));
            mCity.setError(getString(R.string.enter_valid_address));
            mStreet.setError(getString(R.string.enter_valid_address));
            focusView = mStreet;
        }

        if (focusView != null) {
            // There was an error; don't attempt go to the next fragment and focus the first
            // form field with an error.
            focusView.requestFocus();
            return false;
        }

        return true;

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
        void onNewEventLogisticsDoneButtonPressed();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            //Shows the progress UI and hides the event creation form.
            mFrameLayoutView.setVisibility(show ? View.GONE : View.VISIBLE);
            mDoneButtonView.setVisibility(show ? View.GONE : View.VISIBLE);
            mEventLogisticsView.setVisibility(show ? View.GONE : View.VISIBLE);
            mEventLogisticsView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mFrameLayoutView.setVisibility(show ? View.GONE : View.VISIBLE);
                    mDoneButtonView.setVisibility(show ? View.GONE : View.VISIBLE);
                    mEventLogisticsView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mDoneButtonView.setVisibility(show ? View.GONE : View.VISIBLE);
            mFrameLayoutView.setVisibility(show ? View.GONE : View.VISIBLE);
            mEventLogisticsView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
