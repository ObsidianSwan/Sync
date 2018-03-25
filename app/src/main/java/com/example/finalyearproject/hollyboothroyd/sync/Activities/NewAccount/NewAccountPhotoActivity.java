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
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.finalyearproject.hollyboothroyd.sync.Activities.CoreActivity;
import com.example.finalyearproject.hollyboothroyd.sync.Activities.LoginFormActivity;
import com.example.finalyearproject.hollyboothroyd.sync.Model.Person;
import com.example.finalyearproject.hollyboothroyd.sync.R;
import com.example.finalyearproject.hollyboothroyd.sync.Services.AccountManager;
import com.example.finalyearproject.hollyboothroyd.sync.Services.DatabaseManager;
import com.example.finalyearproject.hollyboothroyd.sync.Utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;


public class NewAccountPhotoActivity extends AppCompatActivity {

    private AccountManager accountManager;
    private DatabaseManager databaseManager;

    private ImageButton mProfileImage;
    private Button mDoneButton;
    private Uri mImageUri;
    private static final int GALLERY_CODE = 1;

    private View mProgressView;
    private View mSignUpFormView;

    Float personPinColor = Constants.personPinColorDefault;
    Float eventPinColor = Constants.eventPinColorDefault;

    private HashMap<String, Integer> mDefaultSettingsMap = new HashMap<String, Integer>() {{
        put(Constants.personPinColorName, personPinColor.intValue());
        put(Constants.eventPinColorName, eventPinColor.intValue());
        put(Constants.locationDistanceUpdateIntervalName, Constants.locationDistanceUpdateIntervalDefault);
        put(Constants.locationTimeUpdateIntervalName, Constants.locationTimeUpdateIntervalDefault);
        put(Constants.mapZoomLevelName, Constants.mapZoomLevelDefault);
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_account_photo);

        accountManager = new AccountManager();
        databaseManager = new DatabaseManager();

        mProfileImage = (ImageButton) findViewById(R.id.profile_photo_button);
        mDoneButton = (Button) findViewById(R.id.done_button);

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_CODE);
            }
        });


        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String firstName = getIntent().getStringExtra("firstName");
                String lastName = getIntent().getStringExtra("lastName");
                String email = getIntent().getStringExtra("email");
                String password = getIntent().getStringExtra("password");
                String position = getIntent().getStringExtra("position");
                String company = getIntent().getStringExtra("company");
                String industry = getIntent().getStringExtra("industry");

                registerUser(firstName, lastName, position, company, industry, mImageUri, email, password);
            }
        });

        mSignUpFormView = findViewById(R.id.sign_up_layout);
        mProgressView = findViewById(R.id.sign_up_progress);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            mImageUri = data.getData();
            CropImage.activity(mImageUri)
                    .setAspectRatio(1,1)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mImageUri = result.getUri();
                mProfileImage.setImageURI(mImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void registerUser(final String firstName, final String lastName, final String position, final String company, final String industry, final Uri imageUri, final String email, final String password) {
        // Create and authenticate the new user.
        showProgress(true);
            // Sign up a new user into the Firebase Authentication service
            accountManager.signUpUser(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // New user added
                        // Sign in the new user into Firebase
                        accountManager.signInUser(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {
                                    // Sign in was successful
                                    Toast.makeText(NewAccountPhotoActivity.this, "Signed in", Toast.LENGTH_LONG).show();

                                    // Add the user to the people database that is accessible by all users.
                                    // Add the user's image to the Firebase Storage
                                    databaseManager.uploadPersonImage(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            // Get a URL to the uploaded content
                                            String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                                            String userId = accountManager.getCurrentUser().getUid();
                                            // Add the user to the Firebase Database with the image storage reference
                                            Person person = new Person(firstName, lastName, position, company, industry, downloadUrl, userId, mDefaultSettingsMap);
                                            databaseManager.addPerson(person).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    showProgress(false);
                                                    if(task.isSuccessful())
                                                    {
                                                        if (ActivityCompat.checkSelfPermission(NewAccountPhotoActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(NewAccountPhotoActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                                            // Request Location permissions
                                                            ActivityCompat.requestPermissions(NewAccountPhotoActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
                                                        }
                                                        Toast.makeText(NewAccountPhotoActivity.this, R.string.sign_up_successful, Toast.LENGTH_SHORT).show();
                                                    }
                                                    else
                                                    {
                                                        //TODO: add logging tags. Debugging. Better retry
                                                        Toast.makeText(NewAccountPhotoActivity.this, R.string.generic_sign_up_failed, Toast.LENGTH_SHORT).show();
                                                        showProgress(false);
                                                    }
                                                }
                                            });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            // Adding the user's image was not successful
                                            //TODO: add logging tags. Debugging. Better retry
                                            Toast.makeText(NewAccountPhotoActivity.this, R.string.generic_sign_up_failed, Toast.LENGTH_SHORT).show();
                                            showProgress(false);
                                        }
                                    });
                                } else {
                                    // Sign in was not successful
                                    //TODO: add logging tags. Debugging. Better retry
                                    Toast.makeText(NewAccountPhotoActivity.this, R.string.generic_sign_up_failed, Toast.LENGTH_SHORT).show();
                                    showProgress(false);
                                }
                            }
                        });
                    } else {
                        //New user failed to be added
                        //TODO: add logging tags. Debugging. Better retry
                        Toast.makeText(NewAccountPhotoActivity.this, R.string.generic_sign_up_failed, Toast.LENGTH_SHORT).show();
                        showProgress(false);
                    }
                }
            });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(NewAccountPhotoActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                startActivity(new Intent(NewAccountPhotoActivity.this, CoreActivity.class));
            }
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mDoneButton.setVisibility(show ? View.GONE : View.VISIBLE);
            mSignUpFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mSignUpFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mDoneButton.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mDoneButton.setVisibility(show ? View.GONE : View.VISIBLE);
            mSignUpFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}



