package com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.NewEvent;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.finalyearproject.hollyboothroyd.sync.R;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
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
 * Use the {@link NewEventLogisticsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewEventLogisticsFragment extends Fragment {
    public static final String ARG_TITLE = "title";
    public static final String ARG_INDUSTRY = "industry";
    public static final String ARG_TOPIC = "topic";

    private static final Pattern sDatePattern =
            Pattern.compile("^(0[1-9]|[12][0-9]|3[01])[- /.](0[1-9]|1[012])[- /.](19|20)\\d\\d$");
    private static final Pattern sTimePattern =
            Pattern.compile("([01]?[0-9]|2[0-3]):[0-5][0-9]");

    private String mTitle;
    private String mIndustry;
    private String mTopic;

    private EditText mDate;
    private EditText mTime;
    private Button mDatePicker;
    private Button mTimePicker;
    private int mDay, mMonth, mYear, mMinute, mHour;

    private EditText mStreet;
    private EditText mCity;
    private EditText mState;
    private EditText mZipCode;
    private EditText mCountry;

    private Button mNextButton;

    private OnFragmentInteractionListener mListener;

    public NewEventLogisticsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title
     * @param industry
     * @param topic
     * @return A new instance of fragment NewEventLogisticsFragment.
     */
    public static NewEventLogisticsFragment newInstance(String title, String industry, String topic) {
        NewEventLogisticsFragment fragment = new NewEventLogisticsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_INDUSTRY, industry);
        args.putString(ARG_TOPIC, topic);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTitle = getArguments().getString(ARG_TITLE);
            mIndustry = getArguments().getString(ARG_INDUSTRY);
            mTopic = getArguments().getString(ARG_TOPIC);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_event_logistics, container, false);

        getActivity().setTitle(getString(R.string.new_event_action_bar_title));

        mDatePicker = (Button) view.findViewById(R.id.new_event_date_button);
        mTimePicker = (Button) view.findViewById(R.id.new_event_time_button);

        mDate = (EditText) view.findViewById(R.id.new_event_date_input_text);
        mTime = (EditText) view.findViewById(R.id.new_event_time_input_text);

        mStreet = (EditText) view.findViewById(R.id.new_event_location_street_text);
        mCity = (EditText) view.findViewById(R.id.new_event_location_city);
        mState = (EditText) view.findViewById(R.id.new_event_location_state);
        mZipCode = (EditText) view.findViewById(R.id.new_event_location_zipcode);
        mCountry = (EditText) view.findViewById(R.id.new_event_location_country);

        mNextButton = (Button) view.findViewById(R.id.event_logistics_next_button);

        mDatePicker.setOnClickListener(new View.OnClickListener() {
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
                                if(dayOfMonth < 10){
                                    dayString = "0" + Integer.toString(dayOfMonth);
                                } else {
                                    dayString = Integer.toString(dayOfMonth);
                                }
                                if(monthOfYear < 10){
                                    monthString = "0" + Integer.toString(monthOfYear + 1);
                                } else {
                                    monthString = Integer.toString(monthOfYear + 1);
                                }
                                mDate.setText(dayString + "-"
                                        + monthString + "-" + year);

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
                if ((previousLength < length) && (length == 2 || length == 5)) {
                    s.append("-");
                }
            }
        });

        mTimePicker.setOnClickListener(new View.OnClickListener() {
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
                                if(hourOfDay < 10){
                                    hourString = "0" + Integer.toString(hourOfDay);
                                } else {
                                    hourString = Integer.toString(hourOfDay);
                                }
                                if(minute < 10){
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
                if ((previousLength < length) && (length == 2)) {
                    s.append(":");
                }
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String date = mDate.getText().toString();
                String time = mTime.getText().toString();
                String street = mStreet.getText().toString();
                String city = mCity.getText().toString();
                String state = mState.getText().toString();
                String zipcode = mZipCode.getText().toString();
                String country = mCountry.getText().toString();

                String completeAddress = street + ", " + city + ", " + state + ", " + zipcode + ", " + country;
                LatLng position = getLocationFromAddress(completeAddress);

                if (areEntriesValid(date, time, street, city, state, zipcode, country, position)) {
                    if (mListener != null) {
                        mListener.onNewEventLogisticsNextButtonPressed(mTitle, mIndustry, mTopic, date, time, street, city, state, zipcode, country, position);
                    }
                }
            }
        });
        return view;
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

        // TODO: Turn this into a loop
        // Check for a valid date
        if (TextUtils.isEmpty(date)) {
            mDate.setError(getString(R.string.error_field_required));
            focusView = mDate;
        } else if (!isDateValid(date)) {
            mDate.setError(getString(R.string.error_invalid_date));
            focusView = mDate;
        }

        // Check for a valid time
        if (TextUtils.isEmpty(time)) {
            mTime.setError(getString(R.string.error_field_required));
            focusView = mTime;
        } else if (!isTimeValid(time)) {
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

        if(position == null) {
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

    private boolean isDateValid(String date) {
        if(!sDatePattern.matcher(date).matches()){
            return false;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(date);
            return true;
        } catch (ParseException ex) {
            return false;
        }
    }

    private boolean isTimeValid(String time) {
        return sTimePattern.matcher(time).matches();
    }

    public LatLng getLocationFromAddress(String inputtedAddress) {

        Geocoder coder = new Geocoder(getContext());
        List<Address> address;
        LatLng position = null;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(inputtedAddress, 5);
            if (address.isEmpty()) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            position = new LatLng(location.getLatitude(), location.getLongitude() );

        } catch (IOException ex) {

            ex.printStackTrace();
        }

        return position;
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
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onNewEventLogisticsNextButtonPressed(String eventTitle, String eventIndustry, String eventTopic, String date, String time, String street,
                                                  String city, String state, String zipcode, String country, LatLng position);
    }
}
