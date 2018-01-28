package com.example.finalyearproject.hollyboothroyd.sync.Activities.NewAccount;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.finalyearproject.hollyboothroyd.sync.R;
import com.example.finalyearproject.hollyboothroyd.sync.Services.AccountManager;

public class NewAccountBasicInfoActivity extends AppCompatActivity {

    private EditText mFirstNameText;
    private EditText mLastNameText;
    private EditText mEmailText;
    private EditText mPasswordText;
    private EditText mVerifyPasswordText;
    private Button mNextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_account_basic_info);

        mFirstNameText = (EditText) findViewById(R.id.first_name_text);
        mLastNameText = (EditText) findViewById(R.id.last_name_text);
        mEmailText = (EditText) findViewById(R.id.email_text);
        mPasswordText = (EditText) findViewById(R.id.password_text);
        mVerifyPasswordText = (EditText) findViewById(R.id.verify_password_text);
        mNextButton = (Button) findViewById(R.id.basic_info_next_button);

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String firstName = mFirstNameText.getText().toString();
                String lastName = mLastNameText.getText().toString();
                String email = mEmailText.getText().toString();
                String password = mPasswordText.getText().toString();
                String verifyPassword = mVerifyPasswordText.getText().toString();

                if(areCredentialsValid(firstName, lastName, email, password, verifyPassword)) {
                    Intent intent = new Intent(NewAccountBasicInfoActivity.this, NewAccountJobInfoActivity.class);
                    intent.putExtra("firstName", firstName);
                    intent.putExtra("lastName", lastName);
                    intent.putExtra("email", email);
                    intent.putExtra("password", password);

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
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }
}
