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
 * Use the {@link EditProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
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

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment EditProfileFragment.
     */
    public static EditProfileFragment newInstance() {
        EditProfileFragment fragment = new EditProfileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
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

        getActivity().setTitle("Edit Profile");

        mProfileImage = (ImageButton) view.findViewById(R.id.edit_profile_photo_button);
        mFirstName = (EditText) view.findViewById(R.id.edit_first_name_text);
        mLastName = (EditText) view.findViewById(R.id.edit_last_name_text);
        mPosition = (EditText) view.findViewById(R.id.edit_position_text);
        mCompany = (EditText) view.findViewById(R.id.edit_company_text);
        mIndustry = (EditText) view.findViewById(R.id.edit_industry_text);
        mCommitButton = (Button) view.findViewById(R.id.edit_profile_commit_button);

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
                if (mProfileImageUri != null && !mOriginalProfileImageUri.equals(mProfileImageUri.toString())) {
                    mProfileChanged = true;
                    mDatabaseManager.deletePersonImage(mOriginalProfileImageUri).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mDatabaseManager.uploadPersonImage(mProfileImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            // Get a URL to the uploaded content
                                            mProfileImageUri = task.getResult().getDownloadUrl();
                                            mDatabaseManager.getUserPeopleDatabaseReference().child("imageId").setValue(mProfileImageUri.toString());
                                            mOriginalProfileImageUri = mProfileImageUri.toString();
                                        } else {
                                            Log.e(TAG, "Upload person image failed");
                                        }
                                    }
                                });
                            } else {
                                Log.e(TAG, "Delete person image failed");
                            }
                        }
                    });

                }
                if (mFirstName != null && !mOriginalFirstName.equals(mFirstName.getText().toString())) {
                    mDatabaseManager.getUserPeopleDatabaseReference().child("firstName").setValue(mFirstName.getText().toString());
                    mOriginalFirstName = mFirstName.getText().toString();
                    mProfileChanged = true;
                }
                if (mLastName != null && !mOriginalLastName.equals(mLastName.getText().toString())) {
                    mDatabaseManager.getUserPeopleDatabaseReference().child("lastName").setValue(mLastName.getText().toString());
                    mOriginalLastName = mLastName.getText().toString();
                    mProfileChanged = true;
                }
                if (mPosition != null && !mOriginalPosition.equals(mPosition.getText().toString())) {
                    mDatabaseManager.getUserPeopleDatabaseReference().child("position").setValue(mPosition.getText().toString());
                    mOriginalPosition = mPosition.getText().toString();
                    mProfileChanged = true;
                }
                if (mCompany != null && !mOriginalCompany.equals(mCompany.getText().toString())) {
                    mDatabaseManager.getUserPeopleDatabaseReference().child("company").setValue(mCompany.getText().toString());
                    mOriginalCompany = mCompany.getText().toString();
                    mProfileChanged = true;
                }
                if (mIndustry != null && !mOriginalIndustry.equals(mIndustry.getText().toString())) {
                    mDatabaseManager.getUserPeopleDatabaseReference().child("industry").setValue(mIndustry.getText().toString());
                    mOriginalIndustry = mIndustry.getText().toString();
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

        if (requestCode == Constants.GALLERY_CODE && resultCode == RESULT_OK) {
            mProfileImageUri = data.getData();
            CropImage.activity(mProfileImageUri)
                    .setAspectRatio(1, 1)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(getActivity(), this);
        }

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

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onEditProfileInteraction(uri);
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
        void onEditProfileInteraction(Uri uri);
    }
}
