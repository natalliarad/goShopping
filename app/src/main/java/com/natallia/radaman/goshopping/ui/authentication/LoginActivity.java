package com.natallia.radaman.goshopping.ui.authentication;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.natallia.radaman.goshopping.R;
import com.natallia.radaman.goshopping.model.User;
import com.natallia.radaman.goshopping.ui.BaseActivity;
import com.natallia.radaman.goshopping.ui.MainActivity;
import com.natallia.radaman.goshopping.utils.AppConstants;
import com.natallia.radaman.goshopping.utils.AppFormValidatonUtils;
import com.natallia.radaman.goshopping.utils.AppUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents Sign in screen and functionality of the app
 */
public class LoginActivity extends BaseActivity {
    private static final String LOG_TAG = LoginActivity.class.getSimpleName();
    /* A dialog that is presented until the Firebase authentication finished. */
    private ProgressDialog mAuthProgressDialog;
    /* References to the Firebase */
    private DatabaseReference mFirebaseRef;
    /* Listener for Firebase session changes */
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseAuth mAuth;
    private EditText mEditTextEmailInput, mEditTextPasswordInput;

    private SharedPreferences mSharedPref;
    private SharedPreferences.Editor mSharedPrefEditor;

    /**
     * Variables related to Google Login
     */
    private GoogleSignInClient mGoogleSignInClient;
    /* A flag indicating that a PendingIntent is in progress and prevents us from starting further intents. */
    private boolean mGoogleIntentInProgress;
    /* Request code used to invoke sign in user interactions for Google+ */
    public static final int RC_GOOGLE_LOGIN = 1;
    /* A Google account object that is populated if the user signs in with Google */
    GoogleSignInAccount mGoogleAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        mSharedPrefEditor = mSharedPref.edit();
        /**
         * Create Firebase references
         */
        mFirebaseRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(AppConstants.FIREBASE_URL);
        /**
         //         *Configure Google Sign In
         //         */
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();

        /**
         * Link layout elements from XML and setup progress dialog
         */
        initializeScreen();

        /**
         * Call signInPassword() when user taps "Done" keyboard action
         */
        mEditTextPasswordInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE || keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    signInPassword();
                }
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        /**
         * This is the authentication listener that maintains the current user session
         * and signs in automatically on application launch
         */
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mAuthProgressDialog.dismiss();
                /**
                 * If there is a valid session to be restored, start MainActivity.
                 * No need to pass data via SharedPreferences because app
                 * already holds userName/provider data from the latest session
                 */
                if (firebaseAuth.getCurrentUser() != null) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        };

        /* Add auth listener to Firebase ref */
        mAuth.addAuthStateListener(mAuthStateListener);
        /**
         * Get the newly registered user email if present, use null as default value
         */
        String signupEmail = mSharedPref.getString(AppConstants.KEY_SIGNUP_EMAIL, null);
        /**
         * Fill in the email editText and remove value from SharedPreferences if email is present
         */
        if (signupEmail != null) {
            mEditTextEmailInput.setText(signupEmail);

            /**
             * Clear signupEmail sharedPreferences to make sure that they are used just once
             */
            mSharedPrefEditor.putString(AppConstants.KEY_SIGNUP_EMAIL, null).apply();
        }
    }

    /**
     * Cleans up listeners tied to the user's authentication state
     */
    @Override
    protected void onPause() {
        super.onPause();
        mAuth.removeAuthStateListener(mAuthStateListener);
    }

    /**
     * Override onCreateOptionsMenu to inflate nothing
     *
     * @param menu The menu with which nothing will happen
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    /**
     * Sign in with Password provider when user clicks sign in button
     */
    public void onSignInPressed(View view) {
        signInPassword();
    }

    /**
     * Open CreateAccountActivity when user taps on "Sign up" TextView
     */
    public void onSignUpPressed(View view) {
        Intent intent = new Intent(LoginActivity.this, AccountCreateActivity.class);
        startActivity(intent);
    }

    /**
     * Link layout elements from XML and setup the progress dialog
     */
    public void initializeScreen() {
        mEditTextEmailInput = findViewById(R.id.edit_text_email);
        mEditTextPasswordInput = findViewById(R.id.edit_text_password);
        LinearLayout linearLayoutLoginActivity = findViewById(R.id.linear_layout_login_activity);
        //initializeBackground(linearLayoutLoginActivity);
        /* Setup the progress dialog that is displayed later when authenticating with Firebase */
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle(getString(R.string.progress_dialog_loading));
        mAuthProgressDialog.setMessage(getString(R.string.progress_dialog_authenticating_with_firebase));
        mAuthProgressDialog.setCancelable(false);
        /* Setup Google Sign In */
        setupGoogleSignIn();
    }

    /**
     * Sign in with Password provider (used when user taps "Done" action on keyboard)
     */
    public void signInPassword() {
        String email = mEditTextEmailInput.getText().toString();
        String password = mEditTextPasswordInput.getText().toString();
        /**
         * If email and password are not empty show progress dialog and try to authenticate
         */
        if (email.equals("")) {
            mEditTextEmailInput.setError(getString(R.string.error_cannot_be_empty));
            return;
        }
        if (password.equals("")) {
            mEditTextPasswordInput.setError(getString(R.string.error_cannot_be_empty));
            return;
        }
        mAuthProgressDialog.show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        final String provider;
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (task.isSuccessful()) {
                            provider = user.getProviders().get(0);
                            mAuthProgressDialog.dismiss();
                            Log.i(LOG_TAG, provider + " " + getString(R.string.log_message_auth_successful));
                            if (user != null) {
                                /**
                                 * If user has logged in with Google provider
                                 */
                                if (provider.contains("google")) {
                                    setAuthenticatedUserGoogle(user);
                                } else {
                                    setAuthenticatedUserPasswordProvider(user);
                                }
                                /* Save provider name and encodedEmail for later use and start MainActivity */
                                mSharedPrefEditor.putString(AppConstants.KEY_PROVIDER, user
                                        .getProviders().get(0)).apply();
                                mSharedPrefEditor.putString(AppConstants.KEY_ENCODED_EMAIL, mEncodedEmail)
                                        .apply();

                                /* Go to main activity */
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            mAuthProgressDialog.dismiss();
                            /**
                             * Use utility method to check the network connection state
                             * Show "No network connection" if there is no connection
                             * Show Firebase specific error message otherwise
                             */
                            String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                            switch (errorCode) {
                                case "ERROR_USER_DOES_NOT_EXIST":
                                case "ERROR_INVALID_EMAIL":
                                    mEditTextEmailInput.setError(getString(R.string.error_message_email_issue));
                                    mEditTextEmailInput.requestFocus();
                                    break;

                                case "ERROR_WRONG_PASSWORD":
                                    mEditTextPasswordInput.setError("password is incorrect");
                                    mEditTextPasswordInput.requestFocus();
                                    mEditTextPasswordInput.setText("");
                                    break;

                                case "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL":
                                    showErrorToast(task.getException().getMessage());
                                    break;

                                case "ERROR_EMAIL_ALREADY_IN_USE":
                                    mEditTextEmailInput.setError(getString(R.string.error_email_taken));
                                    mEditTextEmailInput.requestFocus();
                                    break;

                                case "ERROR_CREDENTIAL_ALREADY_IN_USE":
                                    showErrorToast(task.getException().getMessage());
                                    break;

                                case "ERROR_USER_DISABLED":
                                    showErrorToast(task.getException().getMessage());
                                    break;

                                case "ERROR_WEAK_PASSWORD":
                                    mEditTextPasswordInput.setError("password is incorrect");
                                    mEditTextPasswordInput.requestFocus();
                                    mEditTextPasswordInput.setText("");
                                    break;
                                case "ERROR_NETWORK_REQUEST_FAILED":
                                    showErrorToast(getString(R.string.error_message_failed_sign_in_no_network));
                                    break;
                                default:
                                    showErrorToast(task.getException().getMessage());
                            }
                        }
                    }
                });
    }

    /**
     * Helper method that makes sure a user is created if the user
     * logs in with Firebase's email/password provider.
     *
     * @param firebaseUser object returned from onAuthenticated
     */
    private void setAuthenticatedUserPasswordProvider(final FirebaseUser firebaseUser) {
        final String unprocessedEmail = firebaseUser.getEmail();
        /**
         * Encode user email replacing "." with ","
         * to be able to use it as a Firebase db key
         */
        mEncodedEmail = AppUtils.encodeEmail(unprocessedEmail);
        final DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(AppConstants.FIREBASE_URL_USERS).child(mEncodedEmail);
        /**
         * Check if current user has logged in at least once
         */
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    /**
                     * If recently registered user has hasLoggedInWithPassword = "false"
                     * (never logged in using password provider)
                     */
                    if (firebaseUser.isEmailVerified()) {
                        if (user.isHasLoggedInWithPassword() != true)
                            user.setHasLoggedInWithPassword(true);
                        userRef.child(AppConstants.FIREBASE_PROPERTY_USER_HAS_LOGGED_IN_WITH_PASSWORD)
                                .setValue(true);
                        /* The password was changed */
                        Log.d(LOG_TAG, getString(
                                R.string.log_message_password_changed_successfully)
                                + mEditTextPasswordInput.getText().toString());
                    } else {
                        showErrorToast(getString(R.string.error_verify_email_address));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(LOG_TAG, getString(R.string.log_error_the_read_failed) +
                        databaseError.getMessage());
            }
        });
    }

    /**
     * Helper method that makes sure a user is created if the user
     * logs in with Firebase's Google login provider.
     *
     * @param firebaseUser AuthData object returned from onAuthenticated
     */
    private void setAuthenticatedUserGoogle(final FirebaseUser firebaseUser) {
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mSharedPrefEditor = mSharedPref.edit();
        String unprocessedEmail = firebaseUser.getEmail();
        mEncodedEmail = AppUtils.encodeEmail(unprocessedEmail);
        mSharedPrefEditor.putString(AppConstants.KEY_GOOGLE_EMAIL, unprocessedEmail).apply();
        mSharedPrefEditor.putString(AppConstants.KEY_ENCODED_EMAIL, mEncodedEmail).apply();
        mSharedPrefEditor.putString(AppConstants.KEY_PROVIDER, firebaseUser.getProviders().get(0));

        /* Get username from authData */
        final String userName = firebaseUser.getDisplayName();

        /* Make a user */
        final DatabaseReference userLocation = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(AppConstants.FIREBASE_URL_USERS).child(mEncodedEmail);

        userLocation.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                /* If nothing is there ...*/
                if (dataSnapshot.getValue() == null) {
                    HashMap<String, Object> userAndUidMapping = new HashMap<>();
                    HashMap<String, Object> timestampJoined = new HashMap<>();
                    timestampJoined.put(AppConstants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

                    User newUser = new User(userName, mEncodedEmail, timestampJoined);
                    newUser.setHasLoggedInWithPassword(true);
                    userLocation.setValue(newUser);
                    HashMap<String, Object> newUserMap = (HashMap<String, Object>)
                            new ObjectMapper().convertValue(newUser, Map.class);
                    /* Add the user and UID to the update map */
                    userAndUidMapping.put("/" + AppConstants.FIREBASE_LOCATION_USERS + "/" + mEncodedEmail,
                            newUserMap);
                    userAndUidMapping.put("/" + AppConstants.FIREBASE_LOCATION_UID_MAPPINGS + "/"
                            + firebaseUser.getUid(), mEncodedEmail);
                    /* Update the database; it will fail if a user already exists */
                    mFirebaseRef.updateChildren(userAndUidMapping, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                /* Try just making a uid mapping */
                                mFirebaseRef.child(AppConstants.FIREBASE_LOCATION_UID_MAPPINGS)
                                        .child(firebaseUser.getUid()).setValue(mEncodedEmail);
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(LOG_TAG, getString(R.string.log_error_occurred) + databaseError.getMessage());
            }
        });
    }

    /**
     * Show error toast to users
     */
    private void showErrorToast(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Signs you into ShoppingList++ using the Google Login Provider
     *
     * @param account A Google OAuth access token returned from Google
     */
    private void loginWithGoogle(GoogleSignInAccount account) {
       // AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        //mAuth.signInWithCredential(credential);
    }

    /**
     * GOOGLE SIGN IN CODE
     */

    /* Sets up the Google Sign In Button : https://developers.google.com/android/reference/com/google/android/gms/common/SignInButton */
    private void setupGoogleSignIn() {
        SignInButton signInButton = findViewById(R.id.login_with_google);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSignInGooglePressed(v);
            }
        });
    }

    /**
     * Sign in with Google plus when user clicks "Sign in with Google" textView (button)
     */
    public void onSignInGooglePressed(View view) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE_LOGIN);
        mAuthProgressDialog.show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        super.onConnectionFailed(connectionResult);
        /**
         * An unresolvable error has occurred and Google APIs (including Sign-In) will not
         * be available.
         */
        mAuthProgressDialog.dismiss();
        showErrorToast(connectionResult.toString());
    }

    /**
     * This callback is triggered when any startActivityForResult finishes. The requestCode maps to
     * the value passed into startActivityForResult.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /* Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...); */
        if (requestCode == RC_GOOGLE_LOGIN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                handleSignInResult(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(LOG_TAG, "Google sign in failed", e);
            }
        }
    }

    private void handleSignInResult(GoogleSignInAccount acct) {
        Log.d(LOG_TAG, "firebaseAuthWithGoogle:" + acct.getId());
        mAuthProgressDialog.show();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            mAuthProgressDialog.dismiss();
                            Log.d(LOG_TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            setAuthenticatedUserGoogle(user);
                            /* Get username from authData */
                            final String userName = user.getDisplayName();
                            /* If no user exists, make a user */
                            final DatabaseReference userLocation = FirebaseDatabase.getInstance()
                                    .getReferenceFromUrl(AppConstants.FIREBASE_URL_USERS).child(mEncodedEmail);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(LOG_TAG, "signInWithCredential:failure", task.getException());
                        }
                        mAuthProgressDialog.dismiss();
                    }
                });
    }
}

//    private static final String LOG_TAG = LoginActivity.class.getSimpleName();
//    /* A dialog that is presented until the Firebase authentication finished. */
//    private ProgressDialog mAuthProgressDialog;
//    private DatabaseReference mFirebaseRef;
//    private GoogleSignInClient mGoogleSignInClient;
//    private FirebaseAuth mAuth;
//    private EditText mEditTextEmailInput, mEditTextPasswordInput;
//
//    /**
//     * Variables related to Google Login
//     */
//    /* A flag indicating that a PendingIntent is in progress and prevents us from starting further intents. */
//    private boolean mGoogleIntentInProgress;
//    /* Request code used to invoke sign in user interactions for Google+ */
//    public static final int RC_GOOGLE_LOGIN = 1;
//    /* A Google account object that is populated if the user signs in with Google */
//    GoogleSignInAccount mGoogleAccount;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_login);
//
//        /**
//         * Create Firebase references
//         */
//        mFirebaseRef = FirebaseDatabase.getInstance()
//                .getReferenceFromUrl(AppConstants.FIREBASE_URL);
//
//        /**
//         *Configure Google Sign In
//         */
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build();
//        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
//        mAuth = FirebaseAuth.getInstance();
//        /**
//         * Link layout elements from XML and setup progress dialog
//         */
//        initializeScreen();
//
//        /**
//         * Call signInPassword() when user taps "Done" keyboard action
//         */
//        mEditTextPasswordInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
//                if (actionId == EditorInfo.IME_ACTION_DONE || keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
//                    signInPassword();
//                }
//                return true;
//            }
//        });
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//    }
//
//    /**
//     * Override onCreateOptionsMenu to inflate nothing
//     *
//     * @param menu The menu with which nothing will happen
//     */
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        return true;
//    }
//
//    /**
//     * Sign in with Password provider when user clicks sign in button
//     */
//    public void onSignInPressed(View view) {
//        signInPassword();
//    }
//
//    /**
//     * Open CreateAccountActivity when user taps on "Sign up" TextView
//     */
//    public void onSignUpPressed(View view) {
//        Intent intent = new Intent(LoginActivity.this, AccountCreateActivity.class);
//        startActivity(intent);
//    }
//
//    /**
//     * Link layout elements from XML and setup the progress dialog
//     */
//    public void initializeScreen() {
//        mEditTextEmailInput = findViewById(R.id.edit_text_email);
//        mEditTextPasswordInput = findViewById(R.id.edit_text_password);
//        LinearLayout linearLayoutLoginActivity = findViewById(R.id.linear_layout_login_activity);
//        //initializeBackground(linearLayoutLoginActivity);
//        /* Setup the progress dialog that is displayed later when authenticating with Firebase */
//        mAuthProgressDialog = new ProgressDialog(this);
//        mAuthProgressDialog.setTitle(getString(R.string.progress_dialog_loading));
//        mAuthProgressDialog.setMessage(getString(R.string.progress_dialog_authenticating_with_firebase));
//        mAuthProgressDialog.setCancelable(false);
//        /* Setup Google Sign In */
//        setupGoogleSignIn();
//    }
//
//    /**
//     * Sign in with Password provider (used when user taps "Done" action on keyboard)
//     */
//    public void signInPassword() {
//        String email = mEditTextEmailInput.getText().toString();
//        String password = mEditTextPasswordInput.getText().toString();
//        /**
//         * If email and password are not empty show progress dialog and try to authenticate
//         */
//        if (email.equals("")) {
//            mEditTextEmailInput.setError(getString(R.string.error_cannot_be_empty));
//            return;
//        }
//        if (password.equals("")) {
//            mEditTextPasswordInput.setError(getString(R.string.error_cannot_be_empty));
//            return;
//        }
//        mAuthProgressDialog.show();
//        mAuth.signInWithEmailAndPassword(email, password)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            // Sign in success, update UI with the signed-in user's information
//                            Log.i(LOG_TAG, getString(R.string.log_message_auth_successful));
//                            mAuthProgressDialog.dismiss();
//                            FirebaseUser user = mAuth.getCurrentUser();
//                            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//                            SharedPreferences.Editor spe = sp.edit();
//                            //String unprocessedEmail = user.getEmail();
//                            setAuthenticatedUserPasswordProvider(user);
//                            spe.putString(AppConstants.KEY_PROVIDER, user.getProviderId()).apply();
//                            spe.putString(AppConstants.KEY_ENCODED_EMAIL, mEncodedEmail).apply();
//                            successGoToActivity();
//                        } else {
//                            // If sign in fails, display a message to the user.
//                            Log.d(LOG_TAG, getString(R.string.log_error_occurred) + task.getException());
//                            mAuthProgressDialog.dismiss();
//                            String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
//                            switch (errorCode) {
//
//                                case "ERROR_USER_DOES_NOT_EXIST":
//                                case "ERROR_INVALID_EMAIL":
//                                    mEditTextEmailInput.setError(getString(R.string.error_message_email_issue));
//                                    mEditTextEmailInput.requestFocus();
//                                    break;
//
//                                case "ERROR_WRONG_PASSWORD":
//                                    mEditTextPasswordInput.setError("password is incorrect");
//                                    mEditTextPasswordInput.requestFocus();
//                                    mEditTextPasswordInput.setText("");
//                                    break;
//
//                                case "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL":
//                                    showErrorToast(task.getException().getMessage());
//                                    break;
//
//                                case "ERROR_EMAIL_ALREADY_IN_USE":
//                                    mEditTextEmailInput.setError(getString(R.string.error_email_taken));
//                                    mEditTextEmailInput.requestFocus();
//                                    break;
//
//                                case "ERROR_CREDENTIAL_ALREADY_IN_USE":
//                                    showErrorToast(task.getException().getMessage());
//                                    break;
//
//                                case "ERROR_USER_DISABLED":
//                                    showErrorToast(task.getException().getMessage());
//                                    break;
//
//                                case "ERROR_WEAK_PASSWORD":
//                                    mEditTextPasswordInput.setError("password is incorrect");
//                                    mEditTextPasswordInput.requestFocus();
//                                    mEditTextPasswordInput.setText("");
//                                    break;
//                                case "ERROR_NETWORK_REQUEST_FAILED":
//                                    showErrorToast(getString(R.string.error_message_failed_sign_in_no_network));
//                                    break;
//                                default:
//                                    showErrorToast(task.getException().getMessage());
//                            }
//                        }
//                    }
//                });
//    }
//
//    /**
//     * Helper method that makes sure a user is created if the user
//     * logs in with Firebase's email/password provider.
//     *
//     * @param firebaseUser AuthData object returned from onAuthenticated
//     */
//    private void setAuthenticatedUserPasswordProvider(FirebaseUser firebaseUser) {
//        final String unprocessedEmail = firebaseUser.getEmail();
//        /**
//         * Encode user email replacing "." with ","
//         * to be able to use it as a Firebase db key
//         */
//        mEncodedEmail = AppUtils.encodeEmail(unprocessedEmail);
//    }
//
//    /**
//     * Helper method that makes sure a user is created if the user
//     * logs in with Firebase's Google login provider.
//     *
//     * @param firebaseUser AuthData object returned from onAuthenticated
//     */
//    private void setAuthenticatedUserGoogle(FirebaseUser firebaseUser) {
//        /**
//         * If google api client is connected, get the lowerCase user email
//         * and save in sharedPreferences
//         */
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//        SharedPreferences.Editor spe = sp.edit();
//        String unprocessedEmail = firebaseUser.getEmail();
//        mEncodedEmail = AppUtils.encodeEmail(unprocessedEmail);
//        spe.putString(AppConstants.KEY_ENCODED_EMAIL, mEncodedEmail).apply();
//        spe.putString(AppConstants.KEY_PROVIDER, firebaseUser.getProviderId());
//
//        /**
//         * Encode user email replacing "." with "," to be able to use it
//         * as a Firebase db key
//         */
//
//        /* Get username from authData */
//        final String userName = firebaseUser.getDisplayName();
//
//        /* If no user exists, make a user */
//        final DatabaseReference userLocation = FirebaseDatabase.getInstance()
//                .getReferenceFromUrl(AppConstants.FIREBASE_URL_USERS).child(mEncodedEmail);
//        userLocation.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                /* If nothing is there ...*/
//                if (dataSnapshot.getValue() == null) {
//                    HashMap<String, Object> timestampJoined = new HashMap<>();
//                    timestampJoined.put(AppConstants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);
//
//                    User newUser = new User(userName, mEncodedEmail, timestampJoined);
//                    userLocation.setValue(newUser);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Log.d(LOG_TAG, getString(R.string.log_error_occurred) + databaseError.getMessage());
//            }
//        });
//    }
//
//    /**
//     * Show error toast to users
//     */
//    private void showErrorToast(String message) {
//        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
//    }
//
//    /**
//     * Signs you into ShoppingList++ using the Google Login Provider
//     *
//     * @param account A Google OAuth access token returned from Google
//     */
//    private void loginWithGoogle(GoogleSignInAccount account) {
//
//    }
//
//
//    /* Sets up the Google Sign In Button : https://developers.google.com/android/reference/com/google/android/gms/common/SignInButton */
//    private void setupGoogleSignIn() {
//        SignInButton signInButton = findViewById(R.id.login_with_google);
//        signInButton.setSize(SignInButton.SIZE_WIDE);
//        signInButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onSignInGooglePressed(v);
//            }
//        });
//    }
//
//    /**
//     * Sign in with Google plus when user clicks "Sign in with Google" textView (button)
//     */
//    public void onSignInGooglePressed(View view) {
//        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//        startActivityForResult(signInIntent, RC_GOOGLE_LOGIN);
//        mAuthProgressDialog.show();
//    }
//
//    @Override
//    public void onConnectionFailed(ConnectionResult result) {
//        /**
//         * An unresolvable error has occurred and Google APIs (including Sign-In) will not
//         * be available.
//         */
//        mAuthProgressDialog.dismiss();
//        showErrorToast(result.toString());
//    }
//
//    /**
//     * This callback is triggered when any startActivityForResult finishes. The requestCode maps to
//     * the value passed into startActivityForResult.
//     */
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        /* Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...); */
//        if (requestCode == RC_GOOGLE_LOGIN) {
//            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//            try {
//                // Google Sign In was successful, authenticate with Firebase
//                GoogleSignInAccount account = task.getResult(ApiException.class);
//                firebaseAuthWithGoogle(account);
//            } catch (ApiException e) {
//                // Google Sign In failed, update UI appropriately
//                Log.w(LOG_TAG, "Google sign in failed", e);
//            }
//        }
//    }
//
//    /**
//     * Start auth with Google
//     */
//    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
//        Log.d(LOG_TAG, "firebaseAuthWithGoogle:" + acct.getId());
//        mAuthProgressDialog.show();
//
//        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
//        mAuth.signInWithCredential(credential)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            // Sign in success, update UI with the signed-in user's information
//                            mAuthProgressDialog.dismiss();
//                            Log.d(LOG_TAG, "signInWithCredential:success");
//                            FirebaseUser user = mAuth.getCurrentUser();
//                            setAuthenticatedUserGoogle(user);
//                            /* Get username from authData */
//                            final String userName = user.getDisplayName();
//                            /* If no user exists, make a user */
//                            final DatabaseReference userLocation = FirebaseDatabase.getInstance()
//                                    .getReferenceFromUrl(AppConstants.FIREBASE_URL_USERS).child(mEncodedEmail);
//                            successGoToActivity();
//                        } else {
//                            // If sign in fails, display a message to the user.
//                            Log.w(LOG_TAG, "signInWithCredential:failure", task.getException());
//                        }
//                        mAuthProgressDialog.dismiss();
//                    }
//                });
//    }
//
//    private void successGoToActivity() {
//        /* Go to main activity */
//        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intent);
//        finish();
//    }


//    // Get auth credentials from the user for re-authentication.
//    AuthCredential credential = EmailAuthProvider
//            .getCredential(unprocessedEmail, mEditTextPasswordInput.getText().toString());
//                        firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
//@Override
//public void onComplete(@NonNull Task<Void> task) {
//        if (task.isSuccessful()) {
//        firebaseUser.updatePassword(mEditTextPasswordInput.getText().toString())
//        .addOnCompleteListener(new OnCompleteListener<Void>() {
//@Override
//public void onComplete(@NonNull Task<Void> task) {
//        if (!task.isSuccessful()) {
//
//        } else {
//        Log.d(LOG_TAG, getString(R.string.log_error_failed_to_change_password)
//        + task.getException().getMessage());
//        }
//        }
//        });
//        } else {
//        Log.e(LOG_TAG, getString(R.string.log_error_the_read_failed) +
//        task.getException().getMessage());
//        }
//        }
//        });
