package com.natallia.radaman.goshopping.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.natallia.radaman.goshopping.R;
import com.natallia.radaman.goshopping.ui.authentication.AccountCreateActivity;
import com.natallia.radaman.goshopping.ui.authentication.LoginActivity;
import com.natallia.radaman.goshopping.utils.AppConstants;

/**
 * BaseActivity is used as a base class for all activities in the app. It implements GoogleApiClient
 * callbacks to enable "Logout" in all activities and defines variables that are being shared
 * across all activities
 */
public abstract class BaseActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener {
    protected String mEncodedEmail, mProvider;
    /* Client used to interact with Google APIs. */
    protected GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mFirebaseAuth;
    protected FirebaseAuth.AuthStateListener mAuthListener;
    protected DatabaseReference mFirebaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Setup the Google API object to allow Google logins */
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        /* Build a GoogleApiClient with access to the Google Sign-In API and the
         * options specified by gso. */
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        /* Getting mProvider and mEncodedEmail from SharedPreferences */
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(BaseActivity.this);
        /* Get mEncodedEmail and mProvider from SharedPreferences, use null as default value */
        mEncodedEmail = sp.getString(AppConstants.KEY_ENCODED_EMAIL, null);
        mProvider = sp.getString(AppConstants.KEY_PROVIDER, null);

        if (!((this instanceof LoginActivity) || (this instanceof AccountCreateActivity))) {
            mFirebaseRef = FirebaseDatabase.getInstance()
                    .getReferenceFromUrl(AppConstants.FIREBASE_URL);
            mFirebaseAuth = FirebaseAuth.getInstance();
            mAuthListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        //user is signed in

                    } else {
                        //user is signed out
                        /* Clear out shared preferences */
                        SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
                        SharedPreferences.Editor spe = sharedPref.edit();
                        spe.putString(AppConstants.KEY_ENCODED_EMAIL, null);
                        spe.putString(AppConstants.KEY_PROVIDER, null);
                        spe.commit();

                        takeUserToLoginScreenOnUnAuth();
                    }
                }
            };

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!((this instanceof LoginActivity) || (this instanceof AccountCreateActivity))) {
            mFirebaseAuth.addAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!((this instanceof LoginActivity) || (this instanceof AccountCreateActivity))) {
            mFirebaseAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu; this adds items to the action bar if it is present. */
        getMenuInflater().inflate(R.menu.menu_base, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
            case R.id.action_logout:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    /**
     * Logs out the user from their current session and starts LoginActivity.
     * Also disconnects the mGoogleApiClient if connected and provider is Google
     */
    protected void logout() {
        /* Logout if mProvider is not null */
        if (mProvider != null) {
            mFirebaseAuth.signOut();
            if (mProvider.equals(AppConstants.GOOGLE_PROVIDER)) {
                /* Logout from Google+ */
                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                            }
                        });
            }
        }
    }

    private void takeUserToLoginScreenOnUnAuth() {
        /* Move user to LoginActivity, and remove the back stack */
        Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }
}
