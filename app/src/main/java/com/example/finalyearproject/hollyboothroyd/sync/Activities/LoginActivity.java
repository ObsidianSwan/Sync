package com.example.finalyearproject.hollyboothroyd.sync.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.finalyearproject.hollyboothroyd.sync.Activities.NewAccount.NewAccountBasicInfoActivity;
import com.example.finalyearproject.hollyboothroyd.sync.R;
import com.linkedin.platform.LISessionManager;
import com.linkedin.platform.errors.LIAuthError;
import com.linkedin.platform.listeners.AuthListener;
import com.linkedin.platform.utils.Scope;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button connectWithLinkedInButton = (Button) findViewById(R.id.connect_linkedin_button);
        Button newAccountButton = (Button) findViewById(R.id.new_account_button);
        Button loginFormButton = (Button) findViewById(R.id.login_form_button);

        connectWithLinkedInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show LinkedIn login
                LISessionManager.getInstance(getApplicationContext()).init(LoginActivity.this, buildScope(), new AuthListener() {
                    @Override
                    public void onAuthSuccess() {
                        // Authentication was successful
                        Log.i(TAG, getString(R.string.linkedin_authentication_successful));
                        // Save the inputted data to be sent to the next account creation activity
                        Intent intent = new Intent(LoginActivity.this, NewAccountBasicInfoActivity.class);
                        intent.putExtra("isLinkedInConnected", true);
                        startActivity(intent);
                    }

                    @Override
                    public void onAuthError(LIAuthError error) {
                        Log.e(TAG, error.toString());
                        Toast.makeText(getApplicationContext(), R.string.linkedin_auth_error_toast, Toast.LENGTH_SHORT).show();
                    }
                }, true);
            }
        });

        newAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show New Account creation
                Intent intent = new Intent(LoginActivity.this, NewAccountBasicInfoActivity.class);
                intent.putExtra("isLinkedInConnected", false);
                startActivity(intent);
            }
        });

        loginFormButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show existing user Login form
                startActivity(new Intent(LoginActivity.this, LoginFormActivity.class));
            }
        });

    }

    // Build the list of member permissions our LinkedIn session requires
    private static Scope buildScope() {
        return Scope.build(Scope.R_BASICPROFILE, Scope.R_EMAILADDRESS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LISessionManager.getInstance(getApplicationContext()).onActivityResult(this, requestCode, resultCode, data);
    }
}
