package com.example.finalyearproject.hollyboothroyd.sync.Services;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by hollyboothroyd on 11/11/2017.
 */

public class AccountManager {

    private FirebaseAuth mAuth;

    /*Variables used to return status information to the calling activity*/
    private boolean isSignUpSuccessful = false;

    public AccountManager() {
        mAuth = FirebaseAuth.getInstance();
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public boolean isUserSignedIn() {
        if (getCurrentUser() != null) {
            //user is signed in
            return true;
        } else {
            //user is signed out
            return false;
        }
    }

    public void signUserOut()
    {
        mAuth.signOut();
    }

    public Task<AuthResult> signUpUser(String email, String password) {
        return mAuth.createUserWithEmailAndPassword(email, password);
    }

    public Task<AuthResult> signInUser(String email, String password) {
        return mAuth.signInWithEmailAndPassword(email, password);
    }
}