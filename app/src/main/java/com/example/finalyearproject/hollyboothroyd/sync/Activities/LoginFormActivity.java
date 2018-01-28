package com.example.finalyearproject.hollyboothroyd.sync.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.finalyearproject.hollyboothroyd.sync.R;
import com.example.finalyearproject.hollyboothroyd.sync.Services.AccountManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import static com.google.android.gms.tasks.Tasks.await;

/**
 * A login screen that offers login via email/password.
 */
public class LoginFormActivity extends AppCompatActivity {

    private AccountManager accountManager;

    // UI references.
    private EditText mEmailText;
    private EditText mPasswordText;
    private View mProgressView;
    private View mLoginFormView;
    private Button mLoginButton;

    private FirebaseAuth mAuth;
    private boolean mIsSignInSuccessful;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_form);

        accountManager = new AccountManager();

        // Set up the login form.
        mEmailText = findViewById(R.id.email);
        mPasswordText = findViewById(R.id.password);

        mAuth = FirebaseAuth.getInstance();

        mLoginButton = findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Store values at the time of the login attempt.
                String email = mEmailText.getText().toString();
                String password = mPasswordText.getText().toString();

                if (areCredentialsValid(email, password)) {
                    // Show a progress spinner, and kick off a background task to
                    // perform the user login attempt.
                    showProgress(true);
                    accountManager.signInUser(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            showProgress(false);
                            if (task.isSuccessful()) {
                                // Sign in was successful
                                Toast.makeText(LoginFormActivity.this, "Signed in", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(LoginFormActivity.this, CoreActivity.class));
                            } else {
                                // Sign in was not successful
                                Toast.makeText(LoginFormActivity.this, "Signed in failed", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private boolean areCredentialsValid(String email, String password) {
        // Reset errors.
        mEmailText.setError(null);
        mPasswordText.setError(null);

        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(password)) {
            mPasswordText.setError(getString(R.string.error_field_required));
            focusView = mPasswordText;
        } else if (!isPasswordValid(password)) {
            mPasswordText.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordText;
        }
        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailText.setError(getString(R.string.error_field_required));
            focusView = mEmailText;
        } else if (!isEmailValid(email)) {
            mEmailText.setError(getString(R.string.error_invalid_email));
            focusView = mEmailText;
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

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginButton.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginButton.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}


