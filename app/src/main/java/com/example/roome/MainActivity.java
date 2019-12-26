package com.example.roome;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * this activity is presenting the app logo for a few seconds and then disappears.
 * this activity will be shown on every app visit.
 */
public class MainActivity extends AppCompatActivity {
    private static final String FROM = "called from";
    private static final String MAIN_SRC = "MAIN";

    private String mUsername;
    private String mPhotoUrl;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final SharedPreferences reader = getApplicationContext().getSharedPreferences(MyPreferences.MY_PREFERENCES, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = reader.edit();
        editor.putBoolean(MyPreferences.IS_FIRST_TIME, true);
        editor.apply();
//        FacebookSdk.sdkInitialize(getApplicationContext());
//        AppEventsLogger.activateApp(MainActivity.this); //todo check if wanted? https://developers.facebook.com/apps/490409844921663/fb-login/quickstart/
        //Time passed till next activity is launched
        int TIME_OUT = 3000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Initialize Firebase Auth
                mFirebaseAuth = FirebaseAuth.getInstance();
                mFirebaseUser = mFirebaseAuth.getCurrentUser();
                if (mFirebaseUser == null) {
                    // Not signed in, launch the Sign In activity
                    startActivity(new Intent(MainActivity.this, SignInActivity.class));
                    finish();
                    return;
                } else {
                    mUsername = mFirebaseUser.getDisplayName();
                    if (mFirebaseUser.getPhotoUrl() != null) {
                        mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
                    }
                }
                Intent i = determineNextActivity(MainActivity.this, "main");
                startActivity(i);
                finish();
            }
        }, TIME_OUT);
    }

    static Intent determineNextActivity(Context context, String calledFrom) {
        Intent i;
        boolean isFirstTime = MyPreferences.isFirstTime(context);
        if (isFirstTime) {
            //show start activity
            i = new Intent(context, ChoosingActivity.class);
        } else {
            boolean isRoommateSearcher = MyPreferences.isRoommateSearcher(context);
            if (isRoommateSearcher) {
                i = new Intent(context, MainActivityRoommateSearcher.class);
            } else {
                i = new Intent(context, MainActivityApartmentSearcher.class);
            }
            i.putExtra(MainActivity.FROM, calledFrom);
        }
        return i;
    }
}
