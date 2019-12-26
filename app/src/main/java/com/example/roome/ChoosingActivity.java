package com.example.roome;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.roome.user_classes.User;
import com.facebook.Profile;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChoosingActivity extends AppCompatActivity {

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mFirebaseDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choosing);
        // Initialize Firebase
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseDatabaseReference = mFirebaseDatabase.getReference();

        updateUserName();
    }

    /**
     * The function displays the user's name (from which it got from the login) in this
     * activity
     */
    private void updateUserName() { //todo change
        String userName = mFirebaseUser.getDisplayName();
        TextView textView = findViewById(R.id.tv_hello_name);
        textView.setText(String.format("Hi %s!", userName));
    }

    //add a toast to show when successfully signed in

    /**
     * customizable toast
     *
     * @param message
     */
    private void toastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void roommateSearcherOnclick(View view) {
        User userObj = createNewUser();
        mFirebaseDatabaseReference.child("users").child("RoommateSearcherUser").child(mFirebaseUser.getUid()).setValue(userObj);
        toastMessage("Adding " + mFirebaseUser.getDisplayName() + " to database..."); //todo remove
        Intent i = new Intent(ChoosingActivity.this, MainActivityRoommateSearcher.class);
        startActivity(i);
        finish();
    }

    public void apartmentSearcherOnclick(View view) {
        User userObj = createNewUser();
        mFirebaseDatabaseReference.child("users").child("ApartmentSearcherUser").child(mFirebaseUser.getUid()).setValue(userObj);
        toastMessage("Adding " + mFirebaseUser.getDisplayName() + " to database..."); //todo remove
        Intent i = new Intent(ChoosingActivity.this, MainActivityApartmentSearcher.class);
        startActivity(i);
        finish();
    }

    private User createNewUser() {
        String firstName;
        String lastName;
        GoogleSignInAccount userAccount = GoogleSignIn.getLastSignedInAccount(ChoosingActivity.this);
        if(userAccount!=null) {
            firstName = userAccount.getGivenName();
            lastName = userAccount.getFamilyName();
        }
        else {
            Profile user = Profile.getCurrentProfile();
            firstName = user.getFirstName();
            lastName = user.getLastName();
        }
        return new User(firstName, lastName);
    }
}
