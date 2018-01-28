package com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.NewEvent;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.finalyearproject.hollyboothroyd.sync.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

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

    private ImageButton mEventImage;
    private Button mDoneButton;
    private Uri mImageUri;
    private static final int GALLERY_CODE = 1;

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
                                                          String street, String city, String state, String zipcode, String country) {
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
        View view = inflater.inflate(R.layout.fragment_new_event_description, container, false);

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

/*        mNextButton.setOnClickListener(new View.OnClickListener() {
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
                        //mListener.onFragmentInteraction(eventTitle, eventIndustry, eventTopic);
                    }
                }
            }
        });*/
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            mImageUri = data.getData();
            CropImage.activity(mImageUri)
                    .setAspectRatio(1,1)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(getActivity());
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
        // TODO: Update argument type and name
        void onNewEventDescriptionDoneButtonPressed(Uri uri);
    }
}
