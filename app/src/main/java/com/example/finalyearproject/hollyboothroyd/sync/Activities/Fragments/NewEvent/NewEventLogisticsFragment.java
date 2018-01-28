package com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.NewEvent;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.example.finalyearproject.hollyboothroyd.sync.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
    // TODO: Rename and change types and number of parameters
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
                                mDate.setText(dayOfMonth + "-"
                                        + (monthOfYear + 1) + "-" + year);

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
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
                                mTime.setText(hourOfDay + ":" + minute);
                            }
                        }, mHour, mMinute, true);
                timePickerDialog.show();
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

                if (areEntriesValid(date, time, street, city, state, zipcode, country)) {
                    if (mListener != null) {
                        mListener.onNewEventLogisticsNextButtonPressed(mTitle, mIndustry, mTopic, date, time, street, city, state, zipcode, country);
                    }
                }
            }
        });
        return view;
    }

    private boolean areEntriesValid(String date, String time, String street, String city, String state, String zipcode, String country) {
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
        if (TextUtils.isEmpty(time)) {
            mTime.setError(getString(R.string.error_field_required));
            focusView = mTime;
        } else if (!isTimeValid(time)) {
            mTime.setError(getString(R.string.error_invalid_time));
            focusView = mTime;
        }
        // Check for a valid event industry
        if (TextUtils.isEmpty(street)) {
            mStreet.setError(getString(R.string.error_field_required));
            focusView = mStreet;
        }
        // Check for a valid event topic
        if (TextUtils.isEmpty(city)) {
            mCity.setError(getString(R.string.error_field_required));
            focusView = mCity;
        }
        // Check for a valid event industry
        if (TextUtils.isEmpty(state)) {
            mState.setError(getString(R.string.error_field_required));
            focusView = mState;
        }
        // Check for a valid event topic
        if (TextUtils.isEmpty(zipcode)) {
            mZipCode.setError(getString(R.string.error_field_required));
            focusView = mZipCode;
        }
        // Check for a valid event industry
        if (TextUtils.isEmpty(country)) {
            mCountry.setError(getString(R.string.error_field_required));
            focusView = mCountry;
        }

        if (focusView != null) {
            // There was an error; don't attempt go to the next fragment and focus the first
            // form field with an error.
            focusView.requestFocus();
            return false;
        }

        return true;

    }

    private boolean isDateValid(String inputtedDate) {
        //TODO: validate date
/*        SimpleDateFormat dateFormat = new SimpleDateFormat("DD-MM-YYYY");
        try {
            Date date = dateFormat.parse(inputtedDate);
            return date.after((Date) Calendar.getInstance().getTime());
        } catch (ParseException e) {
            return false;
        }*/
        return true;
    }

    private boolean isTimeValid(String time) {
        return sTimePattern.matcher(time).matches();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            //mListener.onFragmentInteraction(uri);
        }
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
                                                  String city, String state, String zipcode, String country);
    }
}
