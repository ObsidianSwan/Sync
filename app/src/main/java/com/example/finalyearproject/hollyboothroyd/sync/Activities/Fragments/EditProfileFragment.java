package com.example.finalyearproject.hollyboothroyd.sync.Activities.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.finalyearproject.hollyboothroyd.sync.Model.Person;
import com.example.finalyearproject.hollyboothroyd.sync.R;
import com.example.finalyearproject.hollyboothroyd.sync.Services.DatabaseManager;
import com.example.finalyearproject.hollyboothroyd.sync.Utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.regex.Pattern;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EditProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class EditProfileFragment extends Fragment {

    private static final String TAG = "EditProfileFragment";

    private static final int EMAIL_AND_PASSWORD_UPDATED = 0;
    private static final int EMAIL_UPDATED = 1;
    private static final int PASSWORD_UPDATED = 2;

    private DatabaseManager mDatabaseManager;

    private ImageButton mProfileImage;
    private Uri mProfileImageUri;
    private EditText mFirstName;
    private EditText mLastName;
    private EditText mPosition;
    private EditText mCompany;
    private EditText mIndustry;
    private EditText mNewEmail;
    private EditText mNewPassword;
    private EditText mReenteredNewPassword;

    private String mOriginalProfileImageUri;
    private String mOriginalFirstName;
    private String mOriginalLastName;
    private String mOriginalPosition;
    private String mOriginalCompany;
    private String mOriginalIndustry;
    private boolean mIsLinkedInConnected;

    private AlertDialog mDialog;

    private boolean mProfileChanged = false;
    private boolean mAccountDetailsChanged = false;

    private FirebaseUser mCurrentUser;

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
        mNewEmail = (EditText) view.findViewById(R.id.edit_email_text);
        mNewPassword = (EditText) view.findViewById(R.id.edit_password_text);
        mReenteredNewPassword = (EditText) view.findViewById(R.id.edit_reenter_password_text);

        Button commitButton = (Button) view.findViewById(R.id.edit_profile_commit_button);

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
                    if (currentUser != null) {
                        if (currentUser.getImageId() != null) {
                            mOriginalProfileImageUri = currentUser.getImageId();
                            Picasso.with(getActivity()).load(currentUser.getImageId()).into(mProfileImage);
                        }

                        // Save the original user data to be used to compare to the inputted data
                        // Used to check if the database needs to be updated
                        mOriginalFirstName = currentUser.getFirstName();
                        mOriginalLastName = currentUser.getLastName();
                        mOriginalPosition = currentUser.getPosition();
                        mOriginalCompany = currentUser.getCompany();
                        mOriginalIndustry = currentUser.getIndustry();
                        mIsLinkedInConnected = currentUser.getIsLinkedInConnected();

                        // Set up the UI to contain the user's existing profile details
                        mProfileImageUri = Uri.parse(mOriginalProfileImageUri);
                        mFirstName.setText(mOriginalFirstName);
                        mLastName.setText(mOriginalLastName);
                        mPosition.setText(mOriginalPosition);
                        mCompany.setText(mOriginalCompany);
                        mIndustry.setText(mOriginalIndustry);

                        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        commitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProfileChanged = false;

                // Retrieve the inputted data
                String inputtedFirstName = mFirstName.getText().toString().trim();
                String inputtedLastName = mLastName.getText().toString().trim();
                String inputtedPosition = mPosition.getText().toString().trim();
                String inputtedCompany = mCompany.getText().toString().trim();
                String inputtedIndustry = mIndustry.getText().toString().trim();
                String inputtedEmail = mNewEmail.getText().toString().trim();
                String inputtedPassword = mNewPassword.getText().toString().trim();
                String inputtedReenteredPassword = mReenteredNewPassword.getText().toString().trim();

                // Check if the user's profile image has been changed
                if (mProfileImageUri != null && !mOriginalProfileImageUri.equals(mProfileImageUri.toString())) {
                    mProfileChanged = true;
                    // Delete the user's old image from the database first if the user has not created an account via linkedin.
                    // LinkedIn photos are not saved in Firebase Storage
                    if (!mIsLinkedInConnected) {
                        mDatabaseManager.deleteImage(mOriginalProfileImageUri).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                                                if (mProfileImageUri != null) {
                                                    mDatabaseManager.updateUserProfileInformation(Constants.userImgChildName, mProfileImageUri.toString());
                                                    mOriginalProfileImageUri = mProfileImageUri.toString();
                                                }
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

                    } else{
                        mDatabaseManager.updateUserProfileInformation(Constants.userImgChildName, mProfileImageUri.toString());
                        mOriginalProfileImageUri = mProfileImageUri.toString();
                    }
                }
                // Update the database if the users first name has changed
                if (mFirstName != null && !mOriginalFirstName.equals(inputtedFirstName)) {
                    mDatabaseManager.updateUserProfileInformation(Constants.userFirstNameChildName, inputtedFirstName);
                    mOriginalFirstName = inputtedFirstName;
                    mProfileChanged = true;
                }
                // Update the database if the users last name has changed
                if (mLastName != null && !mOriginalLastName.equals(inputtedLastName)) {
                    mDatabaseManager.updateUserProfileInformation(Constants.userLastNameChildName, inputtedLastName);
                    mOriginalLastName = inputtedLastName;
                    mProfileChanged = true;
                }
                // Update the database if the users position has changed
                if (mPosition != null && !mOriginalPosition.equals(inputtedPosition)) {
                    mDatabaseManager.updateUserProfileInformation(Constants.userPositionChildName, inputtedPosition);
                    mOriginalPosition = inputtedPosition;
                    mProfileChanged = true;
                }
                // Update the database if the users company has changed
                if (mCompany != null && !mOriginalCompany.equals(inputtedCompany)) {
                    mDatabaseManager.updateUserProfileInformation(Constants.userCompanyChildName, inputtedCompany);
                    mOriginalCompany = inputtedCompany;
                    mProfileChanged = true;
                }
                // Update the database if the users industry has changed
                if (mIndustry != null && !mOriginalIndustry.equals(inputtedIndustry)) {
                    mDatabaseManager.updateUserProfileInformation(Constants.userIndustryChildName, inputtedIndustry);
                    mOriginalIndustry = inputtedIndustry;
                    mProfileChanged = true;
                }

                // Update the account authentication if the users email and password has changed
                if (!inputtedPassword.equals("") && !inputtedReenteredPassword.equals("")) {
                    // Check that the password and verify password match
                    if (inputtedPassword.equals(inputtedReenteredPassword)) {
                        if (!inputtedEmail.equals("") && isEmailValid(inputtedEmail)) {
                            editAccountDetailsPopupCreation(EMAIL_AND_PASSWORD_UPDATED, inputtedEmail, inputtedPassword);
                            mAccountDetailsChanged = true;
                        }
                    } else {
                        mReenteredNewPassword.setError(getString(R.string.error_invalid_verify_password));
                    }
                } // Update the account authentication if the users email has changed
                else if (!inputtedEmail.equals("") && isEmailValid(inputtedEmail)) {
                    editAccountDetailsPopupCreation(EMAIL_UPDATED, inputtedEmail, null);
                    mAccountDetailsChanged = true;
                } // Update the account authentication if the users password has changed
                else if (!inputtedPassword.equals("") && !inputtedReenteredPassword.equals("") && isPasswordValid(inputtedPassword)) {
                    // Check that the password and verify password match
                    if (inputtedPassword.equals(inputtedReenteredPassword)) {
                        editAccountDetailsPopupCreation(PASSWORD_UPDATED, inputtedPassword, null);
                        mAccountDetailsChanged = true;
                    } else {
                        mReenteredNewPassword.setError(getString(R.string.error_invalid_verify_password));
                    }
                }

                if (mProfileChanged) {
                    Toast.makeText(getActivity(), R.string.edit_profile_successfully_changed, Toast.LENGTH_SHORT).show();
                } else if (mAccountDetailsChanged) {
                    // Do nothing. Toast will be shown after account has been verified
                } else {
                    Toast.makeText(getActivity(), R.string.edit_profile_no_changes_toast, Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    private void editAccountDetailsPopupCreation(final int editedField, final String editedContent, final String editedContent2) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        final View view = LayoutInflater.from(getContext()).inflate(R.layout.account_details_popup, null);

        // Set up the UI
        Button dismissPopupButton = (Button) view.findViewById(R.id.dismiss_popup_button);
        final EditText email = (EditText) view.findViewById(R.id.edit_original_email_text);
        final EditText password = (EditText) view.findViewById(R.id.edit_original_password_text);
        Button verifyButton = (Button) view.findViewById(R.id.verify_button);

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (email.getText() == null || email.getText().toString().equals("")) {
                    email.setError(getString(R.string.error_field_required));
                } else if (password.getText() == null || password.getText().toString().equals("")) {
                    password.setError(getString(R.string.error_field_required));
                } else {
                    // Get auth credentials from the user for re-authentication.
                    AuthCredential credential = EmailAuthProvider
                            .getCredential(email.getText().toString().trim(), password.getText().toString().trim());

                    // Prompt the user to re-provide their sign-in credentials
                    // This is necessary if the user has not logged in in awhile
                    mCurrentUser.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Update the user's email and password
                                        if (editedField == EMAIL_AND_PASSWORD_UPDATED) {
                                            mCurrentUser.updateEmail(editedContent).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d(TAG, getString(R.string.email_updated_log));
                                                        mCurrentUser.updatePassword(editedContent2).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.d(TAG, getString(R.string.password_updated_log));
                                                                    mDialog.dismiss();
                                                                    Toast.makeText(getActivity(), R.string.edit_profile_successfully_changed, Toast.LENGTH_SHORT).show();
                                                                } else {
                                                                    Log.d(TAG, getString(R.string.password_update_failed_log));
                                                                }
                                                            }
                                                        });
                                                    } else {
                                                        Log.d(TAG, getString(R.string.email_update_log));
                                                    }
                                                }
                                            });
                                        }
                                        // Update the user's email
                                        if (editedField == EMAIL_UPDATED) {
                                            mCurrentUser.updateEmail(editedContent).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d(TAG, getString(R.string.email_updated_log));
                                                        mDialog.dismiss();
                                                        Toast.makeText(getActivity(), R.string.edit_profile_successfully_changed, Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Log.d(TAG, getString(R.string.email_update_log));
                                                        Toast.makeText(getActivity(), R.string.email_update_error, Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                        // Update the user's password
                                        if (editedField == PASSWORD_UPDATED) {
                                            mCurrentUser.updatePassword(editedContent).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d(TAG, getString(R.string.password_updated_log));
                                                        mDialog.dismiss();
                                                        Toast.makeText(getActivity(), R.string.edit_profile_successfully_changed, Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Log.d(TAG, getString(R.string.password_update_failed_log));
                                                    }
                                                }
                                            });
                                        }
                                    } else {
                                        Toast.makeText(getActivity(), R.string.incorrect_login_details, Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, getString(R.string.authentication_failed_log));
                                    }
                                }
                            });
                }
            }
        });

        dismissPopupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });


        dialogBuilder.setView(view);
        mDialog = dialogBuilder.create();
        mDialog.show();

    }

    private boolean isEmailValid(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
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
