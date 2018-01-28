package com.example.finalyearproject.hollyboothroyd.sync.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.example.finalyearproject.hollyboothroyd.sync.Activities.NewAccount.NewAccountBasicInfoActivity;
import com.example.finalyearproject.hollyboothroyd.sync.R;

public class LoginActivity extends AppCompatActivity {

    private Button mConnectWithLinkedInButton;
    private Button mNewAccountButton;
    private Button mLoginFormButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mConnectWithLinkedInButton = (Button) findViewById(R.id.connect_linkedin_button);
        mNewAccountButton = (Button) findViewById(R.id.new_account_button);
        mLoginFormButton = (Button) findViewById(R.id.login_form_button);

        mConnectWithLinkedInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show LinkedIn login
            }
        });

        mNewAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show New Account creation
                startActivity(new Intent(LoginActivity.this, NewAccountBasicInfoActivity.class));
            }
        });

        mLoginFormButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show existing user Login form
                startActivity(new Intent(LoginActivity.this, LoginFormActivity.class));
            }
        });

    }
}
