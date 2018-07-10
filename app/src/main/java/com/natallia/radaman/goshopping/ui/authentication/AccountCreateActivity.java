package com.natallia.radaman.goshopping.ui.authentication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
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
import com.natallia.radaman.goshopping.utils.AppUtils;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class AccountCreateActivity extends BaseActivity {
    private static final String LOG_TAG = AccountCreateActivity.class.getSimpleName();
    private ProgressDialog mAuthProgressDialog;
    private DatabaseReference mFirebaseRef;
    private FirebaseAuth mAuth;
    private EditText mEditTextUsernameCreate, mEditTextEmailCreate, mEditTextPasswordCreate;
    private String mUserName, mUserEmail, mPassword;
    private SecureRandom mRandom = new SecureRandom();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_create);
        /**
         * Create Firebase references
         */
        mFirebaseRef = FirebaseDatabase.getInstance().getReferenceFromUrl(AppConstants.FIREBASE_URL);
        mAuth = FirebaseAuth.getInstance();

        /**
         * Link layout elements from XML and setup the progress dialog
         */
        initializeScreen();
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
     * Link layout elements from XML and setup the progress dialog
     */
    public void initializeScreen() {
        mEditTextUsernameCreate = findViewById(R.id.edit_text_username_create);
        mEditTextEmailCreate = findViewById(R.id.edit_text_email_create);
        mEditTextPasswordCreate = findViewById(R.id.edit_text_password_create);
        LinearLayout linearLayoutCreateAccountActivity = findViewById(R.id.linear_layout_create_account_activity);
        //initializeBackground(linearLayoutCreateAccountActivity);

        /* Setup the progress dialog that is displayed later when authenticating with Firebase */
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle(getResources().getString(R.string.progress_dialog_loading));
        mAuthProgressDialog.setMessage(getResources().getString(R.string.progress_dialog_creating_user_with_firebase));
        mAuthProgressDialog.setCancelable(false);
    }

    /**
     * Open LoginActivity when user taps on "Sign in" textView
     */
    public void onSignInPressed(View view) {
        Intent intent = new Intent(AccountCreateActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Create new account using Firebase email/password provider
     */
    public void onCreateAccountPressed(View view) {
        mUserName = mEditTextUsernameCreate.getText().toString();
        mUserEmail = mEditTextEmailCreate.getText().toString().toLowerCase();
        //mPassword = new BigInteger(130, mRandom).toString(32);
        mPassword = mEditTextPasswordCreate.getText().toString();
        /**
         * Check that email and user name are okay
         */
        boolean validEmail = isEmailValid(mUserEmail);
        boolean validUserName = isUserNameValid(mUserName);
        if (!validEmail || !validUserName) return;
        /**
         * If everything was valid show the progress dialog to indicate that
         * account creation has started
         */
        mAuthProgressDialog.show();
        /**
         * Create new user with specified email and password
         */
        mAuth.createUserWithEmailAndPassword(mUserEmail, mPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            /**
                             * If user was successfully created, run resetPassword() to send temporary 24h
                             * password to the user's email and make sure that user owns specified email
                             */
                            final FirebaseUser user = mAuth.getCurrentUser();
                            sendEmailVerification(user);
                        } else {
                            /* Error occurred, log the error and dismiss the progress dialog */
                            Log.d(LOG_TAG, getString(R.string.log_error_occurred) +
                                    task.getException().getMessage());
                            mAuthProgressDialog.dismiss();
                            /* Display the appropriate error message */
                            showErrorToast(task.getException().getMessage());
                        }
                    }
                });
    }

    /**
     * Creates a new user in Firebase from the Java POJO
     */
    private void createUserInFirebaseHelper(final String authUserId) {
        final String encodedEmail = AppUtils.encodeEmail(mUserEmail);
        /**
         * Create the user and uid mapping
         */
        HashMap<String, Object> userAndUidMapping = new HashMap<String, Object>();

        /* Set raw version of date to the ServerValue.TIMESTAMP value and save into dateCreatedMap */
        HashMap<String, Object> timestampJoined = new HashMap<>();
        timestampJoined.put(AppConstants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

        /* Create a HashMap version of the user to add */
        User newUser = new User(mUserName, encodedEmail, timestampJoined);
        HashMap<String, Object> newUserMap = (HashMap<String, Object>)
                new ObjectMapper().convertValue(newUser, Map.class);
        /* Add the user and UID to the update map */
        userAndUidMapping.put("/" + AppConstants.FIREBASE_LOCATION_USERS + "/" + encodedEmail,
                newUserMap);
        userAndUidMapping.put("/" + AppConstants.FIREBASE_LOCATION_UID_MAPPINGS + "/"
                + authUserId, encodedEmail);
        /* Try to update the database; if there is already a user, this will fail */
        mFirebaseRef.updateChildren(userAndUidMapping, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError != null) {
                    /* Try just making a uid mapping */
                    mFirebaseRef.child(AppConstants.FIREBASE_LOCATION_UID_MAPPINGS)
                            .child(authUserId).setValue(encodedEmail);
                }
                /**
                 *  The value has been set or it failed; either way, log out the user since
                 *  they were only logged in with a temp password
                 **/
                mAuth.signOut();
            }
        });
    }

    private boolean isEmailValid(String email) {
        boolean isGoodEmail =
                (email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches());
        if (!isGoodEmail) {
            mEditTextEmailCreate.setError(String.format(getString(R.string.error_invalid_email_not_valid),
                    email));
            return false;
        }
        return isGoodEmail;
    }

    private boolean isUserNameValid(String userName) {
        if (userName.equals("")) {
            mEditTextUsernameCreate.setError(getResources().getString(R.string.error_cannot_be_empty));
            return false;
        }
        return true;
    }

    /**
     * Show error toast to users
     */
    private void showErrorToast(String message) {
        Toast.makeText(AccountCreateActivity.this, message, Toast.LENGTH_LONG).show();
    }

    private void sendEmailVerification(final FirebaseUser firebaseUser) {
        firebaseUser.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mAuth.signInWithEmailAndPassword(mUserEmail,
                                    mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        mAuthProgressDialog.dismiss();
                                        Log.i(LOG_TAG, getString(R.string.log_message_auth_successful));

                                        SharedPreferences sp = PreferenceManager
                                                .getDefaultSharedPreferences(AccountCreateActivity.this);
                                        SharedPreferences.Editor spe = sp.edit();
                                        /**
                                         * Save name and email to sharedPreferences to create User database record
                                         * when the registered user will sign in for the first time
                                         */
                                        spe.putString(AppConstants.KEY_SIGNUP_EMAIL, mUserEmail).apply();
                                        /**
                                         * Encode user email replacing "." with ","
                                         * to be able to use it as a Firebase db key
                                         */
                                        createUserInFirebaseHelper(firebaseUser.getUid());
                                        /**
                                         *  Password reset email sent, open app chooser to pick app
                                         *  for handling inbox email intent
                                         */
                                        Intent intent = new Intent(Intent.ACTION_MAIN);
                                        intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                                        try {
                                            startActivity(intent);
                                            finish();
                                        } catch (android.content.ActivityNotFoundException ex) {
                                            /* User does not have any app to handle email */
                                        }
                                    } else {
                                        Log.e(LOG_TAG, task.getException().getMessage());
                                    }
                                }
                            });
                        } else {
                            /* Error occurred, log the error and dismiss the progress dialog */
                            Log.d(LOG_TAG, getString(R.string.log_error_occurred) +
                                    task.getException().getMessage());
                            mAuthProgressDialog.dismiss();
                        }
                    }
                });
    }
}
