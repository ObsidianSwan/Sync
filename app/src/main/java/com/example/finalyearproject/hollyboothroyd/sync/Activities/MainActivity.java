package com.example.finalyearproject.hollyboothroyd.sync.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.finalyearproject.hollyboothroyd.sync.R;
import com.example.finalyearproject.hollyboothroyd.sync.Services.AccountManager;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public AccountManager accountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accountManager = new AccountManager();

        if (accountManager.isUserSignedIn()) {
            // Show CoreActivity if the user is still logged in
            startActivity(new Intent(MainActivity.this, CoreActivity.class));
            Log.i(TAG, getString(R.string.logged_in_intitation));

        } else {
            // Show LoginActivity if the user needs to log in
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            Log.i(TAG, getString(R.string.not_logged_in_initiation));
        }
    }
}
