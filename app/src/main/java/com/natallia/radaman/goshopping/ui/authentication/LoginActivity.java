package com.natallia.radaman.goshopping.ui.authentication;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
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

/**
 * Represents Sign in screen and functionality of the app
 */
public class LoginActivity extends BaseActivity {
    private static final String LOG_TAG = LoginActivity.class.getSimpleName();
    /* A dialog that is presented until the Firebase authentication finished. */
    private ProgressDialog mAuthProgressDialog;
    private DatabaseReference mFirebaseRef;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private EditText mEditTextEmailInput, mEditTextPasswordInput;

    /**
     * Variables related to Google Login
     */
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

        /**
         * Create Firebase references
         */
        mFirebaseRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(AppConstants.FIREBASE_URL);

        /**
         *Configure Google Sign In
         */
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
    }

    @Override
    public void onPause() {
        super.onPause();
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
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.i(LOG_TAG, getString(R.string.log_message_auth_successful));
                            mAuthProgressDialog.dismiss();
                            FirebaseUser user = mAuth.getCurrentUser();
                            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            SharedPreferences.Editor spe = sp.edit();
                            //String unprocessedEmail = user.getEmail();
                            setAuthenticatedUserPasswordProvider(user);
                            spe.putString(AppConstants.KEY_PROVIDER, user.getProviderId()).apply();
                            spe.putString(AppConstants.KEY_ENCODED_EMAIL, mEncodedEmail).apply();
                            successGoToActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d(LOG_TAG, getString(R.string.log_error_occurred) + task.getException());
                            mAuthProgressDialog.dismiss();
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
     * @param firebaseUser AuthData object returned from onAuthenticated
     */
    private void setAuthenticatedUserPasswordProvider(FirebaseUser firebaseUser) {
        final String unprocessedEmail = firebaseUser.getEmail();
        /**
         * Encode user email replacing "." with ","
         * to be able to use it as a Firebase db key
         */
        mEncodedEmail = AppUtils.encodeEmail(unprocessedEmail);
    }

    /**
     * Helper method that makes sure a user is created if the user
     * logs in with Firebase's Google login provider.
     *
     * @param firebaseUser AuthData object returned from onAuthenticated
     */
    private void setAuthenticatedUserGoogle(FirebaseUser firebaseUser) {
        /**
         * If google api client is connected, get the lowerCase user email
         * and save in sharedPreferences
         */
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor spe = sp.edit();
        String unprocessedEmail = firebaseUser.getEmail();
        mEncodedEmail = AppUtils.encodeEmail(unprocessedEmail);
        spe.putString(AppConstants.KEY_ENCODED_EMAIL, mEncodedEmail).apply();
        spe.putString(AppConstants.KEY_PROVIDER, firebaseUser.getProviderId());

        /**
         * Encode user email replacing "." with "," to be able to use it
         * as a Firebase db key
         */

        /* Get username from authData */
        final String userName = firebaseUser.getDisplayName();

        /* If no user exists, make a user */
        final DatabaseReference userLocation = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(AppConstants.FIREBASE_URL_USERS).child(mEncodedEmail);
        userLocation.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                /* If nothing is there ...*/
                if (dataSnapshot.getValue() == null) {
                    HashMap<String, Object> timestampJoined = new HashMap<>();
                    timestampJoined.put(AppConstants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

                    User newUser = new User(userName, mEncodedEmail, timestampJoined);
                    userLocation.setValue(newUser);
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

    }

    /**
     * GOOGLE SIGN IN CODE
     * <p>
     * This code is mostly boiler plate from
     * https://developers.google.com/identity/sign-in/android/start-integrating
     * and
     * https://github.com/googlesamples/google-services/blob/master/android/signin/app/src/main/java/com/google/samples/quickstart/signin/SignInActivity.java
     * <p>
     * The big picture steps are:
     * 1. User clicks the sign in with Google button
     * 2. An intent is started for sign in.
     * - If the connection fails it is caught in the onConnectionFailed callback
     * - If it finishes, onActivityResult is called with the correct request code.
     * 3. If the sign in was successful, set the mGoogleAccount to the current account and
     * then call get GoogleOAuthTokenAndLogin
     * 4. getGoogleOAuthTokenAndLogin launches an AsyncTask to get an OAuth2 token from Google.
     * 5. Once this token is retrieved it is available to you in the onPostExecute method of
     * the AsyncTask. **This is the token required by Firebase**
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
    public void onConnectionFailed(ConnectionResult result) {
        /**
         * An unresolvable error has occurred and Google APIs (including Sign-In) will not
         * be available.
         */
        mAuthProgressDialog.dismiss();
        showErrorToast(result.toString());
    }

    /**
     * This callback is triggered when any startActivityForResult finishes. The requestCode maps to
     * the value passed into startActivityForResult.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /* Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...); */
        if (requestCode == RC_GOOGLE_LOGIN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(LOG_TAG, "Google sign in failed", e);
            }
        }
    }

    /**
     * Start auth with Google
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
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
                            successGoToActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(LOG_TAG, "signInWithCredential:failure", task.getException());
                        }
                        mAuthProgressDialog.dismiss();
                    }
                });
    }

    private void successGoToActivity() {
        /* Go to main activity */
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
