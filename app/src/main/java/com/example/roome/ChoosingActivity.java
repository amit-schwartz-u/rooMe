package com.example.roome;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import java.util.concurrent.atomic.AtomicBoolean;

public class ChoosingActivity extends AppCompatActivity {

    protected static final int ANIMATION_DELAY_TIME = 500;
    // Firebase instance variables
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference firebaseDatabaseReference;
    final ArrayList<String>[] allApartmentSearcherIds = new ArrayList[1];
    final ArrayList<String>[] allRoommateSearcherIds = new ArrayList[1];
    final AtomicBoolean done = new AtomicBoolean(false);
    public static final String NOT_SEEN = "not_seen";
    public static final String YES_TO_HOUSE = "yes_to_house";
    public static final String MAYBE_TO_HOUSE = "maybe_to_house";
    public static final String NO_TO_HOUSE = "no_to_house";
    public static final String NOT_IN_LISTS = "doesnt appear on lists";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAnimation();
        setContentView(R.layout.activity_choosing);
        final ProgressBar progressBar = findViewById(R.id.progress_bar);
        initalizeFirebaseVariables();
        updateUserName();

        firebaseDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                FirebaseMediate.setDataSnapshot(dataSnapshot);
                allApartmentSearcherIds[0] = FirebaseMediate.getAllApartmentSearcherIds();
                allRoommateSearcherIds[0] = FirebaseMediate.getAllRoommateSearcherIds();
                done.set(true);
                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    /**
     * This method initalizes firebase variables.
     */
    private void initalizeFirebaseVariables() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabaseReference = firebaseDatabase.getReference();
    }

    public void setAnimation() {
        if (Build.VERSION.SDK_INT > MainActivity.MIN_SUPPORTED_API_LEVEL) {
            Slide slide = new Slide();
            slide.setSlideEdge(Gravity.LEFT);
            slide.setDuration(ANIMATION_DELAY_TIME);
            slide.setInterpolator(new DecelerateInterpolator());
            getWindow().setExitTransition(slide);
            getWindow().setEnterTransition(slide);
        }
    }

    /**
     * The method displays the user's name (from which it got from the login) in this
     * activity.
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
     * This method is the on click method  for roommateSearcher button. Adds user to data base and starts the
     * RoommateSearcherSetProfileActivity activity.
     *
     * @param view - the view of the app.
     */
    public void roommateSearcherOnclick(View view) {
        MyPreferences.setIsFirstTimeToFalse(getApplicationContext());
        User userObj = createNewUser();
        DatabaseReference newRef = firebaseDatabaseReference.child("users").child("RoommateSearcherUser").push();
        String key = newRef.getKey();
        newRef.setValue(userObj);
        MyPreferences.setUserUid(getApplicationContext(), key);
        while (!done.get()) ;
        Intent i = new Intent(ChoosingActivity.this, RoommateSearcherSetProfileActivity.class);
        startActivity(i);
        finish();

    }

    /**
     * This method is the on click method for apartmentSearcher button. Adds user to data base and starts the
     * MainActivityApartmentSearcher activity.
     *
     * @param view - the view of the app.
     */
    public void apartmentSearcherOnclick(View view) {
        MyPreferences.setIsRoommateSearcherToFalse(getApplicationContext());
        User userObj = createNewUser();
        DatabaseReference newRef = firebaseDatabaseReference.child("users").child("ApartmentSearcherUser").push();
        String key = newRef.getKey();
        newRef.setValue(userObj);
        MyPreferences.setUserUid(getApplicationContext(), key);
        while (!done.get()) ;
        // add all roommate searchers as relevant to see
        FirebaseMediate.setAptPrefList(ChoosingActivity.NOT_SEEN, key,
                allRoommateSearcherIds[0]);

        Intent i = new Intent(ChoosingActivity.this, MainActivityApartmentSearcher.class);
        startActivityWithAnimation(i);
        finish();
    }

    /**
     * This method returns a new user object initialized with the users name.
     *
     * @return a new user object initialized with the users name.
     */
    private User createNewUser() {
        String firstName;
        String lastName;
        //todo check if ok using - getLastSignedInAccount
        GoogleSignInAccount userAccount = GoogleSignIn.getLastSignedInAccount(ChoosingActivity.this);
        if (userAccount != null) {
            firstName = userAccount.getGivenName();
            lastName = userAccount.getFamilyName();
        } else {
            Profile user = Profile.getCurrentProfile();
            firstName = user.getFirstName();
            lastName = user.getLastName();
        }
        return new User(firstName, lastName);
    }

    public void startActivityWithAnimation(Intent intent) {
        if (Build.VERSION.SDK_INT > MainActivity.MIN_SUPPORTED_API_LEVEL) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this);
            startActivity(intent, options.toBundle());
        } else {
            startActivity(intent);
        }
    }
}