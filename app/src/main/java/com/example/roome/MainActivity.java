package com.example.roome;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
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
    private static final int MIN_SUPPORTED_API_LEVEL = 20;
    //Time passed till next activity is launched
    private static final int TIME_OUT = 3000;

    private String userName;
    private String mPhotoUrl;

    // Firebase instance variables
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        //todo delete 4 rows
//        final SharedPreferences reader = getApplicationContext().getSharedPreferences(MyPreferences.MY_PREFERENCES, Context.MODE_PRIVATE);
//        final SharedPreferences.Editor editor = reader.edit();
//        editor.putBoolean(MyPreferences.IS_FIRST_TIME, true);
//        editor.apply();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                AccessToken accessToken = AccessToken.getCurrentAccessToken();
//                boolean isLoggedIn = accessToken != null && !accessToken.isExpired(); //todo use facebook
                if (firebaseUser == null) {
                    // Not signed in, launch the Sign In activity
                    startActivity(new Intent(MainActivity.this, SignInActivity.class));
                    finish();
                    return;
                } else {
                    userName = firebaseUser.getDisplayName();
                    if (firebaseUser.getPhotoUrl() != null) {
                        mPhotoUrl = firebaseUser.getPhotoUrl().toString();
                    }
                }
                startActivityWithAnimation();
                finish();
            }
        }, TIME_OUT);
    }

    /**
     * this method starts a new activity and adding the transition animation for relevant versions
     * of android
     */
    public void startActivityWithAnimation(){
        Intent i;
        boolean isFirstTime = MyPreferences.isFirstTime(MainActivity.this);
        if (isFirstTime) {
            //show home activity
            i = new Intent(MainActivity.this, ChoosingActivity.class);
        } else {
            boolean isRoommateSearcher = MyPreferences.isRoommateSearcher(MainActivity.this);
            if (isRoommateSearcher) {
                i = new Intent(MainActivity.this, MainActivityRoommateSearcher.class);
            } else {
                i = new Intent(MainActivity.this, MainActivityApartmentSearcher.class);

            }
            i.putExtra(MainActivity.FROM, MAIN_SRC);
        }
        if(Build.VERSION.SDK_INT > MIN_SUPPORTED_API_LEVEL){
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this);
            startActivity(i,options.toBundle());
        }else {
            startActivity(i);
        }
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
