package com.example.finalyearproject.hollyboothroyd.sync.Activities.NewAccount;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.finalyearproject.hollyboothroyd.sync.Activities.CoreActivity;
import com.example.finalyearproject.hollyboothroyd.sync.Activities.PrivacyPolicyActivity;
import com.example.finalyearproject.hollyboothroyd.sync.Model.Person;
import com.example.finalyearproject.hollyboothroyd.sync.R;
import com.example.finalyearproject.hollyboothroyd.sync.Services.AccountManager;
import com.example.finalyearproject.hollyboothroyd.sync.Services.DatabaseManager;
import com.example.finalyearproject.hollyboothroyd.sync.Utils.Constants;
import com.example.finalyearproject.hollyboothroyd.sync.Utils.DownloadImageTask;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.storage.UploadTask;
import com.linkedin.platform.APIHelper;
import com.linkedin.platform.errors.LIApiError;
import com.linkedin.platform.listeners.ApiListener;
import com.linkedin.platform.listeners.ApiResponse;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class NewAccountPhotoActivity extends AppCompatActivity {

    private static final String TAG = "NewAccountPhotoActivity";

    private AccountManager accountManager;
    private DatabaseManager databaseManager;

    private ImageButton mProfileImage;
    private Button mDoneButton;
    private Button mRetryButton;
    private CheckBox mPrivacyPolicyCheckBox;
    private Uri mImageUri;

    private static final String PROFILE_IMAGE_URL = "https://api.linkedin.com/v1/people/~:(picture-urls::(original))?format=json";

    private View mProgressView;
    private View mSignUpFormView;
    private View mRetryView;
    private View mPrivacyPolicyView;

    Float personPinColor = Constants.personPinColorDefault;
    Float eventPinColor = Constants.eventPinColorDefault;

    // Set up default settings map to be saved into new account database
    private HashMap<String, Integer> mDefaultSettingsMap = new HashMap<String, Integer>() {{
        put(Constants.personPinColorName, personPinColor.intValue());
        put(Constants.eventPinColorName, eventPinColor.intValue());
        put(Constants.locationDistanceUpdateIntervalName, Constants.locationDistanceUpdateIntervalDefault);
        put(Constants.locationTimeUpdateIntervalName, Constants.locationTimeUpdateIntervalDefault);
        put(Constants.mapZoomLevelName, Constants.mapZoomLevelDefault);
        put(Constants.searchRadiusName, Constants.geofenceRadiusDefault);
        put(Constants.privacyIntensityName, Constants.privacyIntensityDefault);
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_account_photo);

        accountManager = new AccountManager();
        databaseManager = new DatabaseManager();

        // Set up UI
        mProfileImage = (ImageButton) findViewById(R.id.profile_photo_button);
        mDoneButton = (Button) findViewById(R.id.done_button);
        mRetryButton = (Button) findViewById(R.id.retry_button);
        Button privacyPolicyButton = (Button) findViewById(R.id.privacy_policy);
        mPrivacyPolicyCheckBox = (CheckBox) findViewById(R.id.privacy_policy_checkbox);

        // Retrieve the users LinkedIn profile picture if their LinkedIn account is integrated
        if (getIntent().getBooleanExtra(Constants.userLinkedInChildName, false)) {
            APIHelper apiHelper = APIHelper.getInstance(getApplicationContext());
            apiHelper.getRequest(NewAccountPhotoActivity.this, PROFILE_IMAGE_URL, new ApiListener() {
                @Override
                public void onApiSuccess(ApiResponse s) {
                    JSONObject result = s.getResponseDataAsJson();
                    Log.i(TAG, getString(R.string.retrieve_linkedin_profile_image_successful));
                    try {
                        JSONArray array = result.getJSONObject("pictureUrls").getJSONArray("values");
                        new DownloadImageTask(mProfileImage).execute(array.getString(0));
                        mImageUri = Uri.parse(array.getString(0));
                    } catch (JSONException e) {
                        Log.e(TAG, e.toString());
                    }
                }

                @Override
                public void onApiError(LIApiError error) {
                    Log.e(TAG, error.toString());
                }
            });
        }

        // Open gallery to select profile image
        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Constants.GALLERY_CODE);
            }
        });

        // Open the privacy policy activity
        privacyPolicyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NewAccountPhotoActivity.this, PrivacyPolicyActivity.class));
            }
        });


        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Retrieve previously inputted account details
                String firstName = getIntent().getStringExtra("firstName");
                String lastName = getIntent().getStringExtra("lastName");
                String email = getIntent().getStringExtra("email");
                String password = getIntent().getStringExtra("password");
                String position = getIntent().getStringExtra("position");
                String company = getIntent().getStringExtra("company");
                String industry = getIntent().getStringExtra("industry");
                boolean isLinkedInConnected = getIntent().getBooleanExtra("isLinkedInConnected", false);

                // Register user
                if (mPrivacyPolicyCheckBox.isChecked()) {
                    registerUser(firstName, lastName, position, company, industry, mImageUri, email, password, isLinkedInConnected);
                } else {
                    mPrivacyPolicyCheckBox.setError(getString(R.string.privacy_policy_error_text));
                }
            }
        });

        mSignUpFormView = findViewById(R.id.sign_up_layout);
        mProgressView = findViewById(R.id.sign_up_progress);
        mRetryView = findViewById(R.id.retry_layout);
        mPrivacyPolicyView = findViewById(R.id.privacy_policy_view);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Open crop image page and set aspect ratio to 1:1
        if (requestCode == Constants.GALLERY_CODE && resultCode == RESULT_OK) {
            mImageUri = data.getData();
            CropImage.activity(mImageUri)
                    .setAspectRatio(1, 1)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
        }

        // Once the user is happy with their cropped image, save the image URI and set the profile image to the cropped image
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mImageUri = result.getUri();
                mProfileImage.setImageURI(mImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Log.e(TAG, result.getError().toString());
            }
        }
    }

    private void registerUser(final String firstName, final String lastName, final String position, final String company,
                              final String industry, final Uri imageUri, final String email, final String password, final boolean isLinkedInConnected) {
        // Create and authenticate the new user.
        showProgress(true);
        // Sign up a new user into the Firebase Authentication service
        accountManager.signUpUser(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // New user added
                    // Sign in the new user into Firebase
                    signUpUser(email, password, imageUri, firstName, lastName, position, company, industry, isLinkedInConnected);
                } else {
                    //New user failed to be added
                    Toast.makeText(NewAccountPhotoActivity.this, R.string.generic_sign_up_failed, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, getString(R.string.generic_sign_up_failed));
                    // Allow the user to retry this sign up stage
                    showRetryPage(true);
                    mRetryButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Try to register account again
                            // Close the retry page
                            registerUser(firstName, lastName, position, company, industry, imageUri, email, password, isLinkedInConnected);
                            showRetryPage(false);
                        }
                    });
                }
            }
        });
    }

    private void signUpUser(final String email, final String password, final Uri imageUri, final String firstName, final String lastName,
                            final String position, final String company, final String industry, final boolean isLinkedInConnected) {
        accountManager.signInUser(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    // Sign in was successful
                    Log.i(TAG, getString(R.string.new_user_signed_in_successfully));
                    // Add the user to the people database that is accessible by all users.
                    // Add the user's image to the Firebase Storage
                    if (!isLinkedInConnected) {
                        addUserPhoto(imageUri, firstName, lastName, position, company, industry, isLinkedInConnected);
                    } else {
                        String userId = accountManager.getCurrentUser().getUid();
                        // Add the user to the Firebase Database with the image storage reference
                        Person person = new Person(firstName, lastName, position, company, industry, imageUri.toString(), userId, mDefaultSettingsMap, isLinkedInConnected);
                        addUser(person);
                    }
                } else {
                    // Sign in was not successful
                    Toast.makeText(NewAccountPhotoActivity.this, R.string.generic_sign_up_failed, Toast.LENGTH_SHORT).show();
                    showRetryPage(true);
                    // Allow the user to retry this sign up stage
                    Log.e(TAG, getString(R.string.generic_sign_up_failed));
                    mRetryButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Try to sign into account again
                            // Close the retry page
                            signUpUser(email, password, imageUri, firstName, lastName, position, company, industry, isLinkedInConnected);
                            showRetryPage(false);
                        }
                    });
                }
            }
        });
    }

    private void addUserPhoto(final Uri imageUri, final String firstName, final String lastName, final String position, final String company,
                              final String industry, final boolean isLinkedInConnected) {
        databaseManager.uploadPersonImage(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Get a URL to the uploaded content
                String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                String userId = accountManager.getCurrentUser().getUid();
                // Add the user to the Firebase Database with the image storage reference
                Person person = new Person(firstName, lastName, position, company, industry, downloadUrl, userId, mDefaultSettingsMap, isLinkedInConnected);
                addUser(person);

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Adding the user's image was not successful
                        Toast.makeText(NewAccountPhotoActivity.this, R.string.generic_sign_up_failed, Toast.LENGTH_SHORT).show();
                        // Allow the user to retry this sign up stage
                        showRetryPage(true);
                        Log.e(TAG, getString(R.string.generic_sign_up_failed));
                        mRetryButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Try to add profile image again
                                // Close the retry page
                                addUserPhoto(imageUri, firstName, lastName, position, company, industry, isLinkedInConnected);
                                showRetryPage(false);
                            }
                        });
                    }
                });
    }

    private void addUser(final Person person) {
        databaseManager.addPerson(person).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                showProgress(false);
                if (task.isSuccessful()) {

                    if (ActivityCompat.checkSelfPermission(NewAccountPhotoActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(NewAccountPhotoActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // Request Location permissions
                        ActivityCompat.requestPermissions(NewAccountPhotoActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
                    } else {
                        // If location permissions are already granted, go to GMaps fragment
                        startActivity(new Intent(NewAccountPhotoActivity.this, CoreActivity.class));
                    }
                    Toast.makeText(NewAccountPhotoActivity.this, R.string.sign_up_successful, Toast.LENGTH_SHORT).show();
                    Log.i(TAG, getString(R.string.sign_up_successful));
                } else {
                    Toast.makeText(NewAccountPhotoActivity.this, R.string.generic_sign_up_failed, Toast.LENGTH_SHORT).show();
                    // Allow the user to retry this sign up stage
                    showRetryPage(true);
                    Log.e(TAG, getString(R.string.generic_sign_up_failed));
                    mRetryButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Try to add person to database again
                            // Close the retry page
                            addUser(person);
                            showRetryPage(false);
                        }
                    });
                }
            }
        });
    }

    private void showRetryPage(final boolean show) {
        // Show/Hide retry view and conversely show/hide the other layouts
        mRetryView.setVisibility(show ? View.VISIBLE : View.GONE);
        mDoneButton.setVisibility(show ? View.GONE : View.VISIBLE);
        mSignUpFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mProgressView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Check if the location permissions have been granted
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(NewAccountPhotoActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                // Open the GMaps fragment if the location permissions have been granted
                startActivity(new Intent(NewAccountPhotoActivity.this, CoreActivity.class));
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            // Shows the progress UI and hides the login form.
            mPrivacyPolicyView.setVisibility(show ? View.GONE : View.VISIBLE);
            mDoneButton.setVisibility(show ? View.GONE : View.VISIBLE);
            mSignUpFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mSignUpFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mDoneButton.setVisibility(show ? View.GONE : View.VISIBLE);
                    mSignUpFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                    mPrivacyPolicyView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mSignUpFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mPrivacyPolicyView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}




