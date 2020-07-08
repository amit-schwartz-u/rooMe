package com.example.roome;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flatdialoglibrary.dialog.FlatDialog;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * This class represents the sign in activity. This activity signs in a new user to the app. Users
 * can also sign in with their google accounts.
 */
public class SignInActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;
    private SignInButton googleSignInButton;
    private GoogleApiClient googleApiClient;
    private Button infoButton;

    // Firebase instance variables
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        // Initialize FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();

        // Assign fields
        googleSignInButton = findViewById(R.id.btn_sign_in);
        // Set the dimensions of the sign-in button.
        googleSignInButton.setSize(SignInButton.SIZE_STANDARD);

        // Set click listeners
        googleSignInButton.setOnClickListener(this);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        infoButton = findViewById(R.id.buttonAbout);
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appInfo();
            }
        });
    }

    private void appInfo(){
        final FlatDialog flatDialog = new FlatDialog(SignInActivity.this);
        flatDialog.setTitle("rooMe")
                .setSecondButtonText("Dismiss")
                .setSubtitle("about rooMe app")
                //todo - you can choose to change the info as you'd like, see strings.xml
                .withSecondButtonListner(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        flatDialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * defines an onClick function for the sign in button
     * @param v the button view
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_sign_in) {
            signIn();
        }
    }

    /**
     * handles a connection fail in google sign in
     * @param connectionResult connection result code
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    /**
     * performs google sign in
     */
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }

    /**
     * updates the fragment view after updating data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign-In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign-In failed
                Log.e(TAG, "Google Sign-In failed.");
            }
        }
    }

    /**
     * authenticates the user with google accounts
     * @param acct current account
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebase Auth With Gooogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        final ProgressDialog progressDialog = new ProgressDialog(SignInActivity.this);
        progressDialog.setTitle("Signing in...");
        progressDialog.show();
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signIn With Credential: onComplete:" + task.isSuccessful());
                        progressDialog.dismiss();
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signIn With Credential", task.getException());
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            startActivity(new Intent(SignInActivity.this, ChoosingActivity.class));
                            finish();
                        }
                    }
                });
    }

    // ------------ without google version----------------

    /**
     * performs sign in without google when clicking the sign in button
     */
    public void signWithoutGoogleFunc(View view) {

        EditText firstNameEditText = findViewById(R.id.et_first_name_without_google);
        EditText lastNameEditText = findViewById(R.id.et_last_name_without_google);
        String first = firstNameEditText.getText().toString();
        String last = lastNameEditText.getText().toString();

        if (credentialsValid(first) && credentialsValid(last))
        {
            MyPreferences.setManualFirstName(getApplicationContext(),first);
            MyPreferences.setManualLastName(getApplicationContext(),last);
            startActivity(new Intent(SignInActivity.this, ChoosingActivity.class));
            finish();
        }
        else
        {
            openErrorMsgDialog();
        }
    }

    private void openErrorMsgDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error:")
                .setMessage("Invalid input for first or last name.\n" +
                        "Please enter English characters only.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private boolean credentialsValid(String name) {
        return ((!name.equals(""))
                && (name.matches("^[a-zA-Z]*$")));
    }

}
