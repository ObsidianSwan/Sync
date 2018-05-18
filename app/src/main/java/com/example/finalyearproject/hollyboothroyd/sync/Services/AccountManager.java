package com.example.finalyearproject.hollyboothroyd.sync.Services;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by hollyboothroyd
 * 11/11/2017.
 */

public class AccountManager {

    private FirebaseAuth mAuth;

    public AccountManager() {
        mAuth = FirebaseAuth.getInstance();
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public boolean isUserSignedIn() {
        return getCurrentUser() != null;
    }

    public void signUserOut() {
        mAuth.signOut();
    }

    public Task<AuthResult> signUpUser(String email, String password) {
        return mAuth.createUserWithEmailAndPassword(email, password);
    }

    public Task<AuthResult> signInUser(String email, String password) {
        return mAuth.signInWithEmailAndPassword(email, password);
    }
}