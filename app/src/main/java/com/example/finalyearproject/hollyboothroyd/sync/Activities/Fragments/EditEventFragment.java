package com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.ImageButton;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.finalyearproject.hollyboothroyd.sync.Model.Event;
import com.example.finalyearproject.hollyboothroyd.sync.R;
import com.example.finalyearproject.hollyboothroyd.sync.Services.DatabaseManager;
import com.example.finalyearproject.hollyboothroyd.sync.Utils.Constants;
import com.example.finalyearproject.hollyboothroyd.sync.Utils.Util;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Calendar;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EditEventFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class EditEventFragment extends Fragment {

    private static final String TAG = "EditEventFragment";

    private OnFragmentInteractionListener mListener;
    private DatabaseManager mDatabaseManager;

    private Event mEvent;

    private EditText mEventTitleText;
    private EditText mEventIndustryText;
    private ImageButton mEventImage;
    private EditText mDescription;
    private Button mDatePicker;
    private Button mTimePicker;
    private EditText mDate;
    private EditText mTime;
    private int mDay, mMonth, mYear, mMinute, mHour;
    private EditText mStreet;
    private EditText mCity;
    private EditText mState;
    private EditText mZipCode;
    private EditText mCountry;

    private Uri mImageUri;

    private boolean mProfileChanged = false;

    public EditEventFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabaseManager = new DatabaseManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_event, container, false);

        // Set up UI
        getActivity().setTitle(getString(R.string.edit_event_title));

        mEventTitleText = (EditText) view.findViewById(R.id.edit_event_title_text);
        mEventIndustryText = (EditText) view.findViewById(R.id.edit_event_industry_text);

        mDatePicker = (Button) view.findViewById(R.id.edit_event_date_button);
        mTimePicker = (Button) view.findViewById(R.id.edit_event_time_button);

        mDate = (EditText) view.findViewById(R.id.edit_event_date_input_text);
        mTime = (EditText) view.findViewById(R.id.edit_event_time_input_text);

        mStreet = (EditText) view.findViewById(R.id.edit_event_location_street_text);
        mCity = (EditText) view.findViewById(R.id.edit_event_location_city);
        mState = (EditText) view.findViewById(R.id.edit_event_location_state);
        mZipCode = (EditText) view.findViewById(R.id.edit_event_location_zipcode);
        mCountry = (EditText) view.findViewById(R.id.edit_event_location_country);

        mDescription = (EditText) view.findViewById(R.id.edit_event_description_text);
        mEventImage = (ImageButton) view.findViewById(R.id.edit_event_image_button);

        Button doneButton = (Button) view.findViewById(R.id.edit_event_done_button);

        setUpDatePicker();
        setUpTimePicker();

        setUpInputsFromEvent();

        // Open gallery to select event image
        mEventImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Constants.GALLERY_CODE);
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputtedEventTitle = mEventTitleText.getText().toString().trim();
                String inputtedEventIndustry = mEventIndustryText.getText().toString().trim();
                String inputtedDate = mDate.getText().toString().trim();
                String inputtedTime = mTime.getText().toString().trim();
                String inputtedStreet = mStreet.getText().toString().trim();
                String inputtedCity = mCity.getText().toString().trim();
                String inputtedState = mState.getText().toString().trim();
                String inputtedZipCode = mZipCode.getText().toString().trim();
                String inputtedCountry = mCountry.getText().toString().trim();
                String inputtedDescription = mDescription.getText().toString().trim();
                if (areEntriesValid(inputtedEventTitle, inputtedEventIndustry, inputtedDate, inputtedTime,
                        inputtedStreet, inputtedCity, inputtedState, inputtedZipCode, inputtedCountry, inputtedDescription))

                    // Check if the user's profile image has been changed
                    if (mImageUri != null && !mEvent.getImageId().equals(mImageUri.toString())) {
                        mProfileChanged = true;
                        // Delete the user's old image from the database
                        mDatabaseManager.deleteImage(mEvent.getImageId()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // Upload the user's new image to the database
                                    mDatabaseManager.uploadEventImage(mImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                // Get a URL to the uploaded content
                                                mImageUri = task.getResult().getDownloadUrl();
                                                mDatabaseManager.updateEventDetails(mEvent.getUid(), Constants.eventImgChildName, mImageUri.toString());
                                                mEvent.setImageId(mImageUri.toString());
                                            } else {
                                                Log.e(TAG, getString(R.string.upload_image_error));
                                            }
                                        }
                                    });
                                } else {
                                    Log.e(TAG, getString(R.string.delete_image_error));
                                }
                            }
                        });
                    }

                // Update the database if the users first name has changed
                if (mEvent.getTitle() != null && !mEvent.getTitle().equals(inputtedEventTitle)) {
                    mDatabaseManager.updateEventDetails(mEvent.getUid(), Constants.eventTitleChildName, inputtedEventTitle);
                    mEvent.setTitle(inputtedEventTitle);
                    mProfileChanged = true;
                }
                // Update the database if the users last name has changed
                if (mEvent.getIndustry() != null && !mEvent.getIndustry().equals(inputtedEventIndustry)) {
                    mDatabaseManager.updateEventDetails(mEvent.getUid(), Constants.eventIndustryChildName, inputtedEventIndustry);
                    mEvent.setIndustry(inputtedEventIndustry);
                    mProfileChanged = true;
                }
                // Update the database if the users position has changed
                if (mEvent.getDate() != null && !mEvent.getDate().equals(inputtedDate)) {
                    mDatabaseManager.updateEventDetails(mEvent.getUid(), Constants.eventDateChildName, inputtedDate);
                    mEvent.setDate(inputtedDate);
                    mProfileChanged = true;
                }
                // Update the database if the users company has changed
                if (mEvent.getTime() != null && !mEvent.getTime().equals(inputtedTime)) {
                    mDatabaseManager.updateEventDetails(mEvent.getUid(), Constants.eventTimeChildName, inputtedTime);
                    mEvent.setTime(inputtedTime);
                    mProfileChanged = true;
                }
                // Update the database if the users industry has changed
                if (mEvent.getStreet() != null && !mEvent.getStreet().equals(inputtedStreet)) {
                    mDatabaseManager.updateEventDetails(mEvent.getUid(), Constants.eventStreetChildName, inputtedStreet);
                    mEvent.setStreet(inputtedStreet);
                    updateLocation();
                    mProfileChanged = true;
                }
                // Update the database if the users first name has changed
                if (mEvent.getCity() != null && !mEvent.getCity().equals(inputtedCity)) {
                    mDatabaseManager.updateEventDetails(mEvent.getUid(), Constants.eventCityChildName, inputtedCity);
                    mEvent.setCity(inputtedCity);
                    updateLocation();
                    mProfileChanged = true;
                }
                // Update the database if the users last name has changed
                if (mEvent.getState() != null && !mEvent.getState().equals(inputtedState)) {
                    mDatabaseManager.updateEventDetails(mEvent.getUid(), Constants.eventStateChildName, inputtedState);
                    mEvent.setState(inputtedState);
                    updateLocation();
                    mProfileChanged = true;
                }
                // Update the database if the users position has changed
                if (mEvent.getZipCode() != null && !mEvent.getZipCode().equals(inputtedZipCode)) {
                    mDatabaseManager.updateEventDetails(mEvent.getUid(), Constants.eventZipCodeChildName, inputtedZipCode);
                    mEvent.setZipCode(inputtedZipCode);
                    updateLocation();
                    mProfileChanged = true;
                }
                // Update the database if the users company has changed
                if (mEvent.getCountry() != null && !mEvent.getCountry().equals(inputtedCountry)) {
                    mDatabaseManager.updateEventDetails(mEvent.getUid(), Constants.eventCountryChildName, inputtedCountry);
                    mEvent.setCountry(inputtedCountry);
                    updateLocation();
                    mProfileChanged = true;
                }
                // Update the database if the users company has changed
                if (mEvent.getDescription() != null && !mEvent.getDescription().equals(inputtedDescription)) {
                    mDatabaseManager.updateEventDetails(mEvent.getUid(), Constants.eventDescriptionChildName, inputtedDescription);
                    mEvent.setDescription(inputtedDescription);
                    mProfileChanged = true;
                }
                if (mProfileChanged) {
                    Toast.makeText(getActivity(), R.string.edit_event_successful_toast, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), R.string.edit_profile_no_changes_toast, Toast.LENGTH_SHORT).show();
                }
            }
        });


        return view;
    }

    private void updateLocation() {
        // Find coordinates of location to be able to place location on map
        String completeAddress = mEvent.getStreet() + ", " + mEvent.getCity() + ", " + mEvent.getState() + ", " + mEvent.getZipCode() + ", " + mEvent.getCountry();
        LatLng position = Util.getLocationFromAddress(getContext(), completeAddress);
        if (position != null) {
            mDatabaseManager.updateEventDetails(mEvent.getUid(), Constants.eventLongitudeChildName, String.valueOf(position.longitude));
            mDatabaseManager.updateEventDetails(mEvent.getUid(), Constants.eventLatitudeChildName, String.valueOf(position.latitude));
        }
    }

    private void setUpInputsFromEvent() {
        if (mEvent != null) {
            mEventTitleText.setText(mEvent.getTitle());
            mEventIndustryText.setText(mEvent.getIndustry());
            mDate.setText(mEvent.getDate());
            mTime.setText(mEvent.getTime());
            mStreet.setText(mEvent.getStreet());
            mCity.setText(mEvent.getCity());
            mState.setText(mEvent.getState());
            mZipCode.setText(mEvent.getZipCode());
            mCountry.setText(mEvent.getCountry());
            mDescription.setText(mEvent.getDescription());
            mImageUri = Uri.parse(mEvent.getImageId());
            Picasso.with(getContext()).load(mEvent.getImageId()).into(mEventImage);
        }
    }

    private void setUpDatePicker() {
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
    }

    private void setUpTimePicker() {
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
    }

    public void setEvent(Event event) {
        mEvent = event;
    }

    private boolean areEntriesValid(String eventTitle, String eventIndustry, String date, String time, String street,
                                    String city, String state, String zipcode, String country, String eventDescription) {
        // Reset errors.
        mEventTitleText.setError(null);
        mEventIndustryText.setError(null);
        mDate.setError(null);
        mTime.setError(null);
        mStreet.setError(null);
        mCity.setError(null);
        mState.setError(null);
        mZipCode.setError(null);
        mCountry.setError(null);
        mDescription.setError(null);

        View focusView = null;

        // Check for a valid event title
        if (TextUtils.isEmpty(eventTitle)) {
            mEventTitleText.setError(getString(R.string.error_field_required));
            focusView = mEventTitleText;
        }
        // Check for a valid event industry
        if (TextUtils.isEmpty(eventIndustry)) {
            mEventIndustryText.setError(getString(R.string.error_field_required));
            focusView = mEventIndustryText;
        }

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
        // Check for a valid event title
        if (TextUtils.isEmpty(eventDescription)) {
            mDescription.setError(getString(R.string.error_field_required));
            focusView = mDescription;
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Open crop image page and set aspect ratio to 1:1
        if (requestCode == Constants.GALLERY_CODE && resultCode == RESULT_OK) {
            mImageUri = data.getData();
            CropImage.activity(mImageUri)
                    .setAspectRatio(1, 1)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(getActivity(), this);
        }

        // Once the user is happy with their cropped image, save the image URI and set the profile image to the cropped image
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mImageUri = result.getUri();
                mEventImage.setImageURI(mImageUri);
                Log.i(TAG, getString(R.string.cropper_image_load_successful));
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.e(TAG, "Image loading failed: " + error.toString());
            }
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
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
