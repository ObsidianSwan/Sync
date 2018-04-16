package com.example.finalyearproject.hollyboothroyd.sync.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.example.finalyearproject.hollyboothroyd.sync.R;
import com.example.finalyearproject.hollyboothroyd.sync.Services.AccountManager;
import com.example.finalyearproject.hollyboothroyd.sync.Services.DatabaseManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

        } else {
            // Show LoginActivity if the user needs to log in
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
    }
}
