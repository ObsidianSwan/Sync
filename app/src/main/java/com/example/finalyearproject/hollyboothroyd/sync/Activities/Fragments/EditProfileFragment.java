package com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.finalyearproject.hollyboothroyd.sync.Activities.CoreActivity;
import com.example.finalyearproject.hollyboothroyd.sync.Model.Person;
import com.example.finalyearproject.hollyboothroyd.sync.R;
import com.example.finalyearproject.hollyboothroyd.sync.Services.DatabaseManager;
import com.example.finalyearproject.hollyboothroyd.sync.Utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.net.URI;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EditProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class EditProfileFragment extends Fragment {

    private static final String TAG = "EditProfileFragment";

    private DatabaseManager mDatabaseManager;

    private ImageButton mProfileImage;
    private Uri mProfileImageUri;
    private EditText mFirstName;
    private EditText mLastName;
    private EditText mPosition;
    private EditText mCompany;
    private EditText mIndustry;

    private String mOriginalProfileImageUri;
    private String mOriginalFirstName;
    private String mOriginalLastName;
    private String mOriginalPosition;
    private String mOriginalCompany;
    private String mOriginalIndustry;

    private Button mCommitButton;

    private boolean mProfileChanged = false;

    private OnFragmentInteractionListener mListener;

    public EditProfileFragment() {
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
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        // Set up the UI
        getActivity().setTitle(getString(R.string.edit_profile_title));

        mProfileImage = (ImageButton) view.findViewById(R.id.edit_profile_photo_button);
        mFirstName = (EditText) view.findViewById(R.id.edit_first_name_text);
        mLastName = (EditText) view.findViewById(R.id.edit_last_name_text);
        mPosition = (EditText) view.findViewById(R.id.edit_position_text);
        mCompany = (EditText) view.findViewById(R.id.edit_company_text);
        mIndustry = (EditText) view.findViewById(R.id.edit_industry_text);
        mCommitButton = (Button) view.findViewById(R.id.edit_profile_commit_button);

        // Open the gallery to select the profile image
        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Constants.GALLERY_CODE);
            }
        });

        // This is set to only listen for a single event, so when the image changes in the database, it does not flash to reload
        mDatabaseManager.getUserPeopleDatabaseReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    // Populate page with users information
                    Person currentUser = dataSnapshot.getValue(Person.class);
                    if (currentUser.getImageId() != null) {
                        mOriginalProfileImageUri = currentUser.getImageId();
                        Picasso.with(getActivity()).load(currentUser.getImageId()).into(mProfileImage);
                    }
                    mOriginalFirstName = currentUser.getFirstName();
                    mOriginalLastName = currentUser.getLastName();
                    mOriginalPosition = currentUser.getPosition();
                    mOriginalCompany = currentUser.getCompany();
                    mOriginalIndustry = currentUser.getIndustry();

                    mProfileImageUri = Uri.parse(mOriginalProfileImageUri);
                    mFirstName.setText(mOriginalFirstName);
                    mLastName.setText(mOriginalLastName);
                    mPosition.setText(mOriginalPosition);
                    mCompany.setText(mOriginalCompany);
                    mIndustry.setText(mOriginalIndustry);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mCommitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProfileChanged = false;

                String mInputtedFirstName = mFirstName.getText().toString().trim();
                String mInputtedLastName = mLastName.getText().toString().trim();
                String mInputtedPosition = mPosition.getText().toString().trim();
                String mInputtedCompany = mCompany.getText().toString().trim();
                String mInputtedIndustry = mIndustry.getText().toString().trim();

                // Check if the user's profile image has been changed
                if (mProfileImageUri != null && !mOriginalProfileImageUri.equals(mProfileImageUri.toString())) {
                    mProfileChanged = true;
                    // Delete the user's old image from the database
                    mDatabaseManager.deletePersonImage(mOriginalProfileImageUri).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Upload the user's new image to the database
                                mDatabaseManager.uploadPersonImage(mProfileImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            // Get a URL to the uploaded content
                                            mProfileImageUri = task.getResult().getDownloadUrl();
                                            mDatabaseManager.getUserPeopleDatabaseReference().child(Constants.userImgChildName).setValue(mProfileImageUri.toString());
                                            mOriginalProfileImageUri = mProfileImageUri.toString();
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
                if (mFirstName != null && !mOriginalFirstName.equals(mInputtedFirstName)) {
                    mDatabaseManager.getUserPeopleDatabaseReference().child(Constants.userFirstNameChildName).setValue(mInputtedFirstName);
                    mOriginalFirstName = mInputtedFirstName;
                    mProfileChanged = true;
                }
                // Update the database if the users last name has changed
                if (mLastName != null && !mOriginalLastName.equals(mInputtedLastName)) {
                    mDatabaseManager.getUserPeopleDatabaseReference().child(Constants.userLastNameChildName).setValue(mInputtedLastName);
                    mOriginalLastName = mInputtedLastName;
                    mProfileChanged = true;
                }
                // Update the database if the users position has changed
                if (mPosition != null && !mOriginalPosition.equals(mInputtedPosition)) {
                    mDatabaseManager.getUserPeopleDatabaseReference().child(Constants.userPositionChildName).setValue(mInputtedPosition);
                    mOriginalPosition = mInputtedPosition;
                    mProfileChanged = true;
                }
                // Update the database if the users company has changed
                if (mCompany != null && !mOriginalCompany.equals(mInputtedCompany)) {
                    mDatabaseManager.getUserPeopleDatabaseReference().child(Constants.userCompanyChildName).setValue(mInputtedCompany);
                    mOriginalCompany = mInputtedCompany;
                    mProfileChanged = true;
                }
                // Update the database if the users industry has changed
                if (mIndustry != null && !mOriginalIndustry.equals(mInputtedIndustry)) {
                    mDatabaseManager.getUserPeopleDatabaseReference().child(Constants.userIndustryChildName).setValue(mInputtedIndustry);
                    mOriginalIndustry = mInputtedIndustry;
                    mProfileChanged = true;
                }
                if (mProfileChanged) {
                    Toast.makeText(getActivity(), R.string.edit_profile_successfully_changed, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), R.string.edit_profile_no_changes_toast, Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Open crop image page and set aspect ratio to 1:1
        if (requestCode == Constants.GALLERY_CODE && resultCode == RESULT_OK) {
            mProfileImageUri = data.getData();
            CropImage.activity(mProfileImageUri)
                    .setAspectRatio(1, 1)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(getActivity(), this);
        }

        // Once the user is happy with their cropped image, save the image URI and set the profile image to the cropped image
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mProfileImageUri = result.getUri();
                mProfileImage.setImageURI(mProfileImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.e(TAG, error.toString());
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
        void onEditProfileInteraction();
    }
}
