package com.example.finalyearproject.hollyboothroyd.sync.Activities.NewAccount;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.finalyearproject.hollyboothroyd.sync.R;
import com.example.finalyearproject.hollyboothroyd.sync.Services.AccountManager;
import com.linkedin.platform.APIHelper;
import com.linkedin.platform.errors.LIApiError;
import com.linkedin.platform.listeners.ApiListener;
import com.linkedin.platform.listeners.ApiResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

public class NewAccountBasicInfoActivity extends AppCompatActivity {

    private static final String TAG = "NewAccountBasicInfo";

    private EditText mFirstNameText;
    private EditText mLastNameText;
    private EditText mEmailText;
    private EditText mPasswordText;
    private EditText mVerifyPasswordText;

    private final String basicInfoUrl = "https://api.linkedin.com/v1/people/~:(first-name,last-name,email-address)?format=json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_account_basic_info);

        // Set up UI
        mFirstNameText = (EditText) findViewById(R.id.first_name_text);
        mLastNameText = (EditText) findViewById(R.id.last_name_text);
        mEmailText = (EditText) findViewById(R.id.email_text);
        mPasswordText = (EditText) findViewById(R.id.password_text);
        mVerifyPasswordText = (EditText) findViewById(R.id.verify_password_text);
        Button nextButton = (Button) findViewById(R.id.basic_info_next_button);

        if(getIntent().getBooleanExtra("isLinkedInConnected", false)){
            APIHelper apiHelper = APIHelper.getInstance(getApplicationContext());
            apiHelper.getRequest(NewAccountBasicInfoActivity.this, basicInfoUrl, new ApiListener() {
                @Override
                public void onApiSuccess(ApiResponse s) {
                    JSONObject result = s.getResponseDataAsJson();
                    Log.i(TAG, getString(R.string.retrieve_linkedin_basic_info_successful));
                    try {
                        mFirstNameText.setText(result.get("firstName").toString());
                        mLastNameText.setText(result.get("lastName").toString());
                        mEmailText.setText(result.get("emailAddress").toString());
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

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Retrieve text from inputs
                String firstName = mFirstNameText.getText().toString().trim();
                String lastName = mLastNameText.getText().toString().trim();
                String email = mEmailText.getText().toString().trim();
                String password = mPasswordText.getText().toString().trim();
                String verifyPassword = mVerifyPasswordText.getText().toString().trim();

                // Check if the user inputs pass basic validations
                if(areCredentialsValid(firstName, lastName, email, password, verifyPassword)) {
                    // Save the inputted data to be sent to the next account creation activity
                    Intent intent = new Intent(NewAccountBasicInfoActivity.this, NewAccountJobInfoActivity.class);
                    intent.putExtra("firstName", firstName);
                    intent.putExtra("lastName", lastName);
                    intent.putExtra("email", email);
                    intent.putExtra("password", password);
                    intent.putExtra("isLinkedInConnected", getIntent().getBooleanExtra("isLinkedInConnected", false));

                    startActivity(intent);
                }
            }
        });
    }

    private boolean areCredentialsValid(String firstName, String lastName, String email, String password, String verifyPassword) {
        // Reset errors.
        mFirstNameText.setError(null);
        mLastNameText.setError(null);
        mEmailText.setError(null);
        mPasswordText.setError(null);
        mVerifyPasswordText.setError(null);

        View focusView = null;

        // Check for a valid first name
        if (TextUtils.isEmpty(firstName)) {
            mFirstNameText.setError(getString(R.string.error_field_required));
            focusView = mFirstNameText;
        }
        // Check for a valid last name
        if (TextUtils.isEmpty(lastName)) {
            mLastNameText.setError(getString(R.string.error_field_required));
            focusView = mLastNameText;
        }
        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailText.setError(getString(R.string.error_field_required));
            focusView = mEmailText;
        } else if (!isEmailValid(email)) {
            mEmailText.setError(getString(R.string.error_invalid_email));
            focusView = mEmailText;
        }
        // Check for a valid password.
        if (TextUtils.isEmpty(password)) {
            mPasswordText.setError(getString(R.string.error_field_required));
            focusView = mPasswordText;
        } else if (!isPasswordValid(password)) {
            mPasswordText.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordText;
        }
        // Check for a valid verifying password.
        if (TextUtils.isEmpty(verifyPassword)) {
            mVerifyPasswordText.setError(getString(R.string.error_field_required));
            focusView = mVerifyPasswordText;
        } else if (!isPasswordValid(verifyPassword)) {
            mVerifyPasswordText.setError(getString(R.string.error_invalid_password));
            focusView = mVerifyPasswordText;
        } else if (!verifyPassword.equals(password)){
            mVerifyPasswordText.setError(getString(R.string.error_invalid_verify_password));
            focusView = mVerifyPasswordText;
        }

        if (focusView != null) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            return false;
        }

        return true;
    }

    private boolean isEmailValid(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }
}
