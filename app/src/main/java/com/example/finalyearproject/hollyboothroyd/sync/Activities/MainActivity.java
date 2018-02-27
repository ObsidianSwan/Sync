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

    private static final String TAG = "";
    private FirebaseAuth mAuth;

    public DatabaseManager databaseManager;
    public AccountManager accountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        mAuth = FirebaseAuth.getInstance();

        // TODO: Place these into utils. Access one central DBMan and AccountMan.
        databaseManager = new DatabaseManager();
        accountManager = new AccountManager();

        if (accountManager.isUserSignedIn()) {
            // Show CoreActivity
            accountManager.signUserOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));

        } else {
            // Show LoginActivity
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
    }

   /* @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.

            mAuth.createUserWithEmailAndPassword("hb@surrey.ac.uk", "Hb333221")
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                //Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                            } else {
                                // If sign in fails, display a message to the user.
                                //Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }

                            // ...
                        }
                    });
        //}

    }
*/

}
