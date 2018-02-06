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
    private static final int GALLERY_CODE = 1;

    private AccountManager accountManager;
    private DatabaseManager databaseManager;

    private OnFragmentInteractionListener mListener;

    public NewEventDescriptionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment NewEventDescriptionFragment.
     */
    // TODO: Rename and change types and number of parameters
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

        accountManager = new AccountManager();
        databaseManager = new DatabaseManager();

        mDescription = (EditText) view.findViewById(R.id.new_event_description_text);
        mEventImage = (ImageButton) view.findViewById(R.id.new_event_image_button);

        mEventImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_CODE);
            }
        });

        mDoneButton = (Button) view.findViewById(R.id.event_description_done_button);

        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String description = mDescription.getText().toString();
                registerEvent(description);


            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            mImageUri = data.getData();
            CropImage.activity(mImageUri)
                    .setAspectRatio(1, 1)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(getActivity(), this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mImageUri = result.getUri();
                mEventImage.setImageURI(mImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void registerEvent(final String description) {
        // TODO: Add progress spinner
        //showProgress(true);

        if (mImageUri == null) {
            //TODO: Create event without image
        }

        databaseManager.uploadEventImage(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                // Get a URL to the uploaded content
                String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                final String userId = accountManager.getCurrentUser().getUid();
                // Add the event to the Firebase Database with the image storage reference

                DatabaseReference newEventRef = databaseManager.getNewEventReference();
                final String refKey = newEventRef.getKey();
                Event event = new Event(refKey, mTitle, mIndustry, mTopic, mDate, mTime, mStreet,
                        mCity, mState, mZipCode, mCountry, mLongitude, mLatitude, description, downloadUrl, userId);
                databaseManager.addEvent(newEventRef, event).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //showProgress(false);
                        if (task.isSuccessful()) {

                            databaseManager.addEventCreator(refKey, userId).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getActivity(), R.string.event_creation_successful, Toast.LENGTH_LONG).show();
                                        if (mListener != null) {
                                            mListener.onNewEventDescriptionDoneButtonPressed(mLongitude, mLatitude);
                                        }
                                    } else {
                                        //TODO: add logging tags. Debugging. Better retry
                                        Toast.makeText(getActivity(), R.string.generic_event_creation_failed, Toast.LENGTH_LONG).show();
                                        //showProgress(false);
                                    }
                                }
                            });
                        } else {
                            //TODO: add logging tags. Debugging. Better retry
                            Toast.makeText(getActivity(), R.string.generic_event_creation_failed, Toast.LENGTH_LONG).show();
                            //showProgress(false);
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Adding the events's image was not successful
                //TODO: add logging tags. Debugging. Better retry
                Toast.makeText(getActivity(), R.string.generic_event_creation_failed, Toast.LENGTH_LONG).show();
                //showProgress(false);
            }
        });
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
        void onNewEventDescriptionDoneButtonPressed(Double longitude, Double Latitude);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    /*@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mSignUpFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mSignUpFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mSignUpFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mSignUpFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }*/
}
