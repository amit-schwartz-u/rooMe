package com.example.roome;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.roome.user_classes.ApartmentSearcherUser;
import com.example.roome.user_classes.RoommateSearcherUser;
import com.example.roome.user_classes.User;
import com.facebook.Profile;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChoosingActivity extends AppCompatActivity {

    // Firebase instance variables
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference firebaseDatabaseReference;
    final ArrayList<String>[] allApartmentSearcherIds = new ArrayList[1];
    final ArrayList<String>[] allRoommateSearcherIds = new ArrayList[1];
    final AtomicBoolean done = new AtomicBoolean(false);
    private String num; //todo for random


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAnimation();
        setContentView(R.layout.activity_choosing);

        // Initialize Firebase
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabaseReference = firebaseDatabase.getReference();

//        updateUserName();  todo uncomment this

        firebaseDatabaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                FirebaseMediate.setDataSnapshot(dataSnapshot);
                allApartmentSearcherIds[0] = FirebaseMediate.getAllApartmentSearcherIds();
                allRoommateSearcherIds[0] = FirebaseMediate.getAllRoommateSearcherIds();
                done.set(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //todo this for random -------------
        Random rnd = new Random();
        num = Integer.toString(rnd.nextInt(1000));
        TextView textView = findViewById(R.id.tv_hello_name);
        textView.setText(String.format("Hi %s!", num));
        //todo -------------------------------------
    }

    public void setAnimation() {
        if (Build.VERSION.SDK_INT > 20) {
            Slide slide = new Slide();
            slide.setSlideEdge(Gravity.LEFT);
            slide.setDuration(400);
            slide.setInterpolator(new DecelerateInterpolator());
            getWindow().setExitTransition(slide);
            getWindow().setEnterTransition(slide);
        }
    }

    /**
     * The function displays the user's name (from which it got from the login) in this
     * activity
     */
    private void updateUserName() {//todo
        String userName = "";
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(ChoosingActivity.this);
        if (firebaseUser != null) {
            userName = firebaseUser.getDisplayName();
        } else if (acct != null) {
            userName = acct.getDisplayName();
        }
        TextView textView = findViewById(R.id.tv_hello_name);
        textView.setText(String.format("Hi %s!", userName));
    }

    /**
     * on click for roommateSearcher button
     *
     * @param view
     */
    public void roommateSearcherOnclick(View view) {
        MyPreferences.setIsFirstTimeToFalse(getApplicationContext());
        RoommateSearcherUser userObj = createRandomRoomateUser(); //todo create real roommate
        DatabaseReference newRef = firebaseDatabaseReference.child("users").child("RoommateSearcherUser").push();
        String key = newRef.getKey();
        newRef.setValue(userObj);
        MyPreferences.setUserUid(getApplicationContext(), key);
//        firebaseDatabaseReference.child("users").child("RoommateSearcherUser").child(key).setValue(userObj);
        while (!done.get()) ;
        firebaseDatabaseReference.child("preferences").child("RoommateSearcherUser").child(key).child("0").setValue(allApartmentSearcherIds[0]);
        Intent i = new Intent(ChoosingActivity.this, MainActivityRoommateSearcher.class);
        startActivity(i);
        finish();

    }

    /**
     * on click for apartmentSearcher button
     *
     * @param view
     */
    public void apartmentSearcherOnclick(View view) {
        MyPreferences.setIsFirstTimeToFalse(getApplicationContext());
        MyPreferences.setIsRoommateSearcherToFalse(getApplicationContext());
        ApartmentSearcherUser userObj = createRandomAptUser(); //todo create real apt searcher
        DatabaseReference newRef = firebaseDatabaseReference.child("users").child("ApartmentSearcherUser").push();
        String key = newRef.getKey();
        newRef.setValue(userObj);
        MyPreferences.setUserUid(getApplicationContext(), key);
//        firebaseDatabaseReference.child("users").child("ApartmentSearcherUser").child(firebaseUser.getUid()).setValue(userObj);
        while (!done.get()) ;
        firebaseDatabaseReference.child("preferences").child("ApartmentSearcherUser").child(key).child("0").setValue(allRoommateSearcherIds[0]);
        Intent i = new Intent(ChoosingActivity.this, MainActivityApartmentSearcher.class);
        startActivity(i);
        finish();
    }

    /**
     * @return
     */
    private User createNewUser() {
        String firstName;
        String lastName;
        //todo check if ok using - getLastSignedInAccount
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

    private ApartmentSearcherUser createRandomAptUser() { //todo random
        return new ApartmentSearcherUser(num, num, 20);
    }

    private RoommateSearcherUser createRandomRoomateUser() { //todo random

        return new RoommateSearcherUser(num, num, 25);
    }
}
