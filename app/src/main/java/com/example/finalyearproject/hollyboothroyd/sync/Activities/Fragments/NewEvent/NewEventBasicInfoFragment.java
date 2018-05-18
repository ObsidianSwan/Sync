package com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments.NewEvent;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.finalyearproject.hollyboothroyd.sync.R;
import com.example.finalyearproject.hollyboothroyd.sync.Utils.Constants;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NewEventBasicInfoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class NewEventBasicInfoFragment extends Fragment {

    private static final String TAG = "NewEventInfoFragment";

    private EditText mEventTitleText;
    private EditText mEventIndustryText;
    private ImageButton mEventImage;
    private EditText mDescription;
    private Uri mImageUri;

    private OnFragmentInteractionListener mListener;

    public NewEventBasicInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_event_basic_info, container, false);

        // Set up UI
        getActivity().setTitle(getString(R.string.new_event_action_bar_title));

        mEventTitleText = (EditText) view.findViewById(R.id.new_event_title_text);
        mEventIndustryText = (EditText) view.findViewById(R.id.new_event_industry_text);
        mDescription = (EditText) view.findViewById(R.id.new_event_description_text);
        mEventImage = (ImageButton) view.findViewById(R.id.new_event_image_button);
        Button nextButton = (Button) view.findViewById(R.id.event_basic_info_next_button);

        // Open gallery to select event image
        mEventImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Constants.GALLERY_CODE);
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Retrieve user inputted data
                String eventTitle = mEventTitleText.getText().toString().trim();
                String eventIndustry = mEventIndustryText.getText().toString().trim();
                String eventDescription = mDescription.getText().toString().trim();

                // Perform basic input validation
                if (areEntriesValid(eventTitle, eventIndustry, eventDescription)) {
                    if (mListener != null) {
                        // Pass data to the CoreActivity to pass it to the next fragment
                        mListener.onNewEventInfoNextButtonPressed(eventTitle, eventIndustry, eventDescription, mImageUri.toString());
                    }
                }
            }
        });
        return view;
    }

    private boolean areEntriesValid(String eventTitle, String eventIndustry, String eventDescription) {
        // Reset errors.
        mEventTitleText.setError(null);
        mEventIndustryText.setError(null);
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
        // Check for a valid event title
        if (TextUtils.isEmpty(eventDescription)) {
            mDescription.setError(getString(R.string.error_field_required));
            focusView = mDescription;
        }
        // Check for a valid event image
        if (mImageUri == null) {
            Toast.makeText(getContext(), R.string.event_image_required_toast, Toast.LENGTH_SHORT).show();
            return false;
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

    public interface OnFragmentInteractionListener {
        void onNewEventInfoNextButtonPressed(String eventTitle, String eventIndustry, String eventDescription, String eventImageUri);
    }
}
