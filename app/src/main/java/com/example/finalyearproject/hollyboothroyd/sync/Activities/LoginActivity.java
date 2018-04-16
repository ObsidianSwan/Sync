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
            }
        });

        newAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show New Account creation
                startActivity(new Intent(LoginActivity.this, NewAccountBasicInfoActivity.class));
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
}
