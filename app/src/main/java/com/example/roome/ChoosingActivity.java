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

import com.example.roome.user_classes.Apartment;
import com.example.roome.user_classes.RoommateSearcherUser;
import com.example.roome.user_classes.User;
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

/**
 * This class represents the Choosing Activity. On the first login to the app the user chooses
 * between ApartmentSearcher (if he is looking to join to an apt) and RoommateSearcher (if he is looking
 * for a roommate).
 */
public class ChoosingActivity extends AppCompatActivity {

    protected static final int ANIMATION_DELAY_TIME = 500;
    /* Firebase instance variables */
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference firebaseDatabaseReference;
    private final ArrayList<String>[] allApartmentSearcherIds = new ArrayList[1];
    private final ArrayList<String>[] allRoommateSearcherIds = new ArrayList[1];
    private final AtomicBoolean done = new AtomicBoolean(false);
    /* Firebase data base lists names */
    public static final String NOT_SEEN = "not_seen";
    public static final String YES_TO_HOUSE = "yes_to_house";
    public static final String DELETE_USERS = "delete_user";
    public static final String MAYBE_TO_HOUSE = "maybe_to_house";
    public static final String NO_TO_HOUSE = "no_to_house";
    public static final String NOT_IN_LISTS = "doesn't appear on lists";
    public static final String NO_TO_ROOMMATE = "no_to_roommate";


    public static final String MATCH = "match";
    public static final String NOT_MATCH = "not_match";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EspressoIdlingResource.increment();
        super.onCreate(savedInstanceState);
        setAnimation();
        setContentView(R.layout.activity_choosing);
        final ProgressBar progressBar = findViewById(R.id.pb_progress_bar);
        initializeFirebaseVariables();
        updateUserName();

        addListenerToFirebaseDbReference(progressBar);
        EspressoIdlingResource.decrement();
    }

    /**
     * This method adds adds a Single Event Listener to the data base reference.
     *
     * @param progressBar The progress bar for finishing sign in activity.
     */
    private void addListenerToFirebaseDbReference(final ProgressBar progressBar) {
        firebaseDatabaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                EspressoIdlingResource.increment();
                allRoommateSearcherIds[0] =
                        FirebaseMediate.getAllRoommateSearcherIds(dataSnapshot.child("RoommateSearcherUser"));
                allApartmentSearcherIds[0] = FirebaseMediate.getAllApartmentSearcherIds(dataSnapshot.child("ApartmentSearcherUser"));
                done.set(true);
                progressBar.setVisibility(View.GONE);
                EspressoIdlingResource.decrement();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    /**
     * This method initializes firebase variables.
     */
    private void initializeFirebaseVariables() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabaseReference = firebaseDatabase.getReference();
    }

    /**
     * This method defines the animation of the activity.
     */
    private void setAnimation() {
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
     * This method displays the user's name (from which it got from the login) in this
     * activity.
     */
    private void updateUserName() {
        String userName;
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(ChoosingActivity.this);
        if (firebaseUser != null) {
            userName = firebaseUser.getDisplayName();
        } else if (acct != null) {
            userName = acct.getDisplayName();
        } else {
            userName = getUserNameFromMyPreferences();
        }
        TextView textView = findViewById(R.id.tv_hello_name);
        textView.setText(String.format("Hi %s!", userName));
    }

    /**
     * RoommateSearcher hasn't been implemented - this is why it is commented.
     * This method is the on click method for roommateSearcher button. Adds user to data base and starts the
     * RoommateSearcherSetProfileActivity activity.
     *
     * @param view - the button view.
     */
    public void roommateSearcherOnclick(View view) {
        User userObj = createNewUser();
        RoommateSearcherUser roommateSearcherUser = new RoommateSearcherUser();
        roommateSearcherUser.setFirstName(userObj.getFirstName());
        roommateSearcherUser.setLastName(userObj.getLastName());
        Apartment userApartment = new Apartment(false, null, null, 2, 0);
        roommateSearcherUser.setApartment(userApartment);
        DatabaseReference newRef = firebaseDatabaseReference.child("users").child("RoommateSearcherUser").push();
        String key = newRef.getKey();
        newRef.setValue(roommateSearcherUser);
        MyPreferences.setUserUid(getApplicationContext(), key);
        while (!done.get()) ;
        for (String aptId : allApartmentSearcherIds[0]) {
            DatabaseReference newRef2 = firebaseDatabaseReference.child("preferences").child("ApartmentSearcherUser").child(aptId).child(ChoosingActivity.NOT_SEEN).push();
            newRef2.setValue(key); //todo: check if its the id of the roommate user
        }
        Intent i = new Intent(ChoosingActivity.this, MainActivityRoommateSearcher.class);
        startActivity(i);
        finish();
    }

    /**
     * This method is the on click method for apartmentSearcher button. Adds user to data base and starts the
     * MainActivityApartmentSearcher activity.
     *
     * @param view - the button view.
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
        GoogleSignInAccount userAccount = GoogleSignIn.getLastSignedInAccount(ChoosingActivity.this);
        String firstName, lastName;
        if (userAccount == null) {
            firstName = getUserFirstNameFromMyPreferences();
            lastName = getUserLastNameFromMyPreferences();
        } else {
            firstName = userAccount.getGivenName();
            lastName = userAccount.getFamilyName();
        }
        return new User(firstName, lastName);
    }

    /**
     * This method returns the user last name saved in MyPreferences.
     *
     * @return the user last name saved in MyPreferences.
     */
    private String getUserLastNameFromMyPreferences() {
        return MyPreferences.getManualLastName(getApplicationContext());
    }

    /**
     * This method returns the user name saved in MyPreferences.
     *
     * @return the user name saved in MyPreferences.
     */
    private String getUserNameFromMyPreferences() {
        return getUserFirstNameFromMyPreferences() + " " + getUserLastNameFromMyPreferences();
    }

    /**
     * This method returns the user first name saved in MyPreferences.
     *
     * @return the user first name saved in MyPreferences.
     */
    private String getUserFirstNameFromMyPreferences() {
        return MyPreferences.getManualFirstName(getApplicationContext());
    }

    /**
     * This method starts the MainActivityApartmentSearcher activity With adjusted animation
     *
     * @param intent - The passed intent
     */
    private void startActivityWithAnimation(Intent intent) {
        if (Build.VERSION.SDK_INT > MainActivity.MIN_SUPPORTED_API_LEVEL) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this);
            startActivity(intent, options.toBundle());
        } else {
            startActivity(intent);
        }
    }
}