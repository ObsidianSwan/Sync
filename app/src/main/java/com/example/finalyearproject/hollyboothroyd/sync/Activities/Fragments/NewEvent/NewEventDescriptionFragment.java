package com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.NewEvent;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.finalyearproject.hollyboothroyd.sync.Activities.CoreActivity;
import com.example.finalyearproject.hollyboothroyd.sync.Activities.NewAccount.NewAccountPhotoActivity;
import com.example.finalyearproject.hollyboothroyd.sync.Model.Event;
import com.example.finalyearproject.hollyboothroyd.sync.Model.Person;
import com.example.finalyearproject.hollyboothroyd.sync.R;
import com.example.finalyearproject.hollyboothroyd.sync.Services.AccountManager;
import com.example.finalyearproject.hollyboothroyd.sync.Services.DatabaseManager;
import com.example.finalyearproject.hollyboothroyd.sync.Utils.Constants;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NewEventDescriptionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NewEventDescriptionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewEventDescriptionFragment extends Fragment {

    private static final String TAG = "NewEventDesFragment";

    public static final String ARG_TITLE = "title";
    public static final String ARG_INDUSTRY = "industry";
    public static final String ARG_TOPIC = "topic";
    public static final String ARG_DATE = "date";
    public static final String ARG_TIME = "time";
    public static final String ARG_STREET = "street";
    public static final String ARG_CITY = "city";
    public static final String ARG_STATE = "state";
    public static final String ARG_ZIPCODE = "zipcode";
    public static final String ARG_COUNTRY = "country";
    public static final String ARG_LONGITUDE = "longitude";
    public static final String ARG_LATITUDE = "latitude";

    private String mTitle;
    private String mIndustry;
    private String mTopic;
    private String mDate;
    private String mTime;
    private String mStreet;
    private String mCity;
    private String mState;
    private String mZipCode;
    private String mCountry;
    private Double mLongitude;
    private Double mLatitude;

    private ImageButton mEventImage;
    private EditText mDescription;
    private Button mDoneButton;
    private Uri mImageUri;

    private View mProgressView;
    private View mEventDescriptionView;

    private AccountManager accountManager;
    private DatabaseManager databaseManager;

    private OnFragmentInteractionListener mListener;

    public NewEventDescriptionFragment() {
        // Required empty public constructor
    }

    // TODO remove

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment NewEventDescriptionFragment.
     */
    public static NewEventDescriptionFragment newInstance(String title, String industry, String topic, String date, String time,
                                                          String street, String city, String state, String zipcode, String country, LatLng position) {
        NewEventDescriptionFragment fragment = new NewEventDescriptionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_INDUSTRY, industry);
        args.putString(ARG_TOPIC, topic);
        args.putString(ARG_DATE, date);
        args.putString(ARG_TIME, time);
        args.putString(ARG_STREET, street);
        args.putString(ARG_CITY, city);
        args.putString(ARG_STATE, state);
        args.putString(ARG_ZIPCODE, zipcode);
        args.putString(ARG_COUNTRY, country);
        args.putDouble(ARG_LONGITUDE, position.longitude);
        args.putDouble(ARG_LATITUDE, position.latitude);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Retrieve previously inputted event data
            mTitle = getArguments().getString(ARG_TITLE);
            mIndustry = getArguments().getString(ARG_INDUSTRY);
            mTopic = getArguments().getString(ARG_TOPIC);
            mDate = getArguments().getString(ARG_DATE);
            mTime = getArguments().getString(ARG_TIME);
            mStreet = getArguments().getString(ARG_STREET);
            mCity = getArguments().getString(ARG_CITY);
            mState = getArguments().getString(ARG_STATE);
            mZipCode = getArguments().getString(ARG_ZIPCODE);
            mCountry = getArguments().getString(ARG_COUNTRY);
            mLongitude = getArguments().getDouble(ARG_LONGITUDE);
            mLatitude = getArguments().getDouble(ARG_LATITUDE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_event_description, container, false);

        // Set up UI
        getActivity().setTitle(getString(R.string.new_event_action_bar_title));

        accountManager = new AccountManager();
        databaseManager = new DatabaseManager();

        mEventDescriptionView = view.findViewById(R.id.event_description_layout);
        mProgressView = view.findViewById(R.id.event_description_progress);

        mDescription = (EditText) view.findViewById(R.id.new_event_description_text);
        mEventImage = (ImageButton) view.findViewById(R.id.new_event_image_button);

        // Open gallery to select event image
        mEventImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Constants.GALLERY_CODE);
            }
        });

        mDoneButton = (Button) view.findViewById(R.id.event_description_done_button);

        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get user input and register the event
                String description = mDescription.getText().toString().trim();
                registerEvent(description);


            }
        });
        return view;
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
                Log.i(TAG, "Image loaded successfully");
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.e(TAG, "Image loading failed: " + error.toString());
            }
        }
    }

    private void registerEvent(final String description) {
        showProgress(true);

        if (mImageUri == null) {
            // If there is no user provided image, use default event image url.
            registerEventInternal(description, Constants.eventDefaultImgStorageUrl);
        } else {
            // Upload the event image to the database
            databaseManager.uploadEventImage(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                    // Get a URL to the uploaded content
                    if(taskSnapshot.getDownloadUrl() != null) {
                        String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                        // Register the event
                        registerEventInternal(description, downloadUrl);
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
    }

    private void registerEventInternal(String description, String downloadUrl){
        final String userId = accountManager.getCurrentUser().getUid();
        // Add the event to the Firebase Database with the image storage reference

        // TODO why am i saving the ref
        DatabaseReference newEventRef = databaseManager.getNewEventReference();
        final String refKey = newEventRef.getKey();
        Event event = new Event(refKey, mTitle, mIndustry, mTopic, mDate, mTime, mStreet,
                mCity, mState, mZipCode, mCountry, mLongitude, mLatitude, description, downloadUrl, userId);

        // Add the event to the event database
        databaseManager.addEvent(newEventRef, event).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                showProgress(false);
                if (task.isSuccessful()) {
                    // Add the event to the user's events created database
                    databaseManager.addEventCreator(refKey, userId).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getActivity(), R.string.event_creation_successful, Toast.LENGTH_SHORT).show();
                                if (mListener != null) {
                                    // Inform the CoreActivity to handle the remainder of the event creation
                                    mListener.onNewEventDescriptionDoneButtonPressed();
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
        void onNewEventDescriptionDoneButtonPressed();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            //Shows the progress UI and hides the event creation form.
            mDoneButton.setVisibility(show ? View.GONE : View.VISIBLE);
            mEventDescriptionView.setVisibility(show ? View.GONE : View.VISIBLE);
            mEventDescriptionView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mDoneButton.setVisibility(show ? View.GONE : View.VISIBLE);
                    mEventDescriptionView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mDoneButton.setVisibility(show ? View.GONE : View.VISIBLE);
            mEventDescriptionView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
