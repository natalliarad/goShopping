package com.natallia.radaman.goshopping.ui.authentication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.AuthResult;
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
import com.natallia.radaman.goshopping.ui.MainActivity;
import com.natallia.radaman.goshopping.utils.AppConstants;
import com.natallia.radaman.goshopping.utils.AppUtils;

import java.security.SecureRandom;
import java.util.HashMap;

public class AccountCreateActivity extends AppCompatActivity {
    private static final String LOG_TAG = AccountCreateActivity.class.getSimpleName();
    private ProgressDialog mAuthProgressDialog;
    private DatabaseReference mFirebaseRef;
    private FirebaseAuth mAuth;
    private EditText mEditTextUsernameCreate, mEditTextEmailCreate, mEditTextPasswordCreate;
    private String mUserName, mUserEmail, mPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_create);
        /**
         * Create Firebase references
         */
        mFirebaseRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(AppConstants.FIREBASE_URL);
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
        mPassword = mEditTextPasswordCreate.getText().toString();

        /**
         * Check that email and user name are okay
         */
        boolean validEmail = isEmailValid(mUserEmail);
        boolean validUserName = isUserNameValid(mUserName);
        boolean validPassword = isPasswordValid(mPassword);
        if (!validEmail || !validUserName || !validPassword) return;

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
                            // Sign in success, update UI with the signed-in user's information
                            mAuthProgressDialog.dismiss();
                            Log.i(LOG_TAG, getString(R.string.log_message_auth_successful));
                            FirebaseUser user = mAuth.getCurrentUser();
                            createUserInFirebaseHelper(user);
                            successGoToActivity();
                        } else {
                            Log.d(LOG_TAG, getString(R.string.log_error_occurred) +
                                    task.getException());
                            mAuthProgressDialog.dismiss();
                            String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                            switch (errorCode) {

                                case "ERROR_INVALID_EMAIL":
                                    mEditTextEmailCreate.setError(getString(R.string.error_email_taken));
                                    mEditTextEmailCreate.requestFocus();
                                    break;

                                case "ERROR_WRONG_PASSWORD":
                                    mEditTextPasswordCreate.setError("password is incorrect");
                                    mEditTextPasswordCreate.requestFocus();
                                    mEditTextPasswordCreate.setText("");
                                    break;

                                case "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL":
                                    showErrorToast(task.getException().getMessage());
                                    break;

                                case "ERROR_EMAIL_ALREADY_IN_USE":
                                    mEditTextEmailCreate.setError(getString(R.string.error_email_taken));
                                    mEditTextEmailCreate.requestFocus();
                                    break;

                                case "ERROR_CREDENTIAL_ALREADY_IN_USE":
                                    showErrorToast(task.getException().getMessage());
                                    break;

                                case "ERROR_USER_DISABLED":
                                    showErrorToast(task.getException().getMessage());
                                    break;

                                case "ERROR_WEAK_PASSWORD":
                                    mEditTextPasswordCreate.setError("password is incorrect");
                                    mEditTextPasswordCreate.requestFocus();
                                    mEditTextPasswordCreate.setText("");
                                    break;
                            }
                        }
                    }
                });
    }

    /**
     * Creates a new user in Firebase from the Java POJO
     */
    private void createUserInFirebaseHelper(FirebaseUser user) {
        final String encodedEmail = AppUtils.encodeEmail(user.getEmail());
        final DatabaseReference userLocation = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(AppConstants.FIREBASE_URL_USERS).child(encodedEmail);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor spe = sp.edit();
        spe.putString(AppConstants.KEY_ENCODED_EMAIL, encodedEmail).apply();
        /**
         * See if there is already a user (for example, if they already logged in with an associated
         * Google account.
         */
        userLocation.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                /* Set raw version of date to the ServerValue.TIMESTAMP value and save into dateCreatedMap */
                HashMap<String, Object> timestampJoined = new HashMap<>();
                timestampJoined.put(AppConstants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);
                User newUser = new User(mUserName, encodedEmail, timestampJoined);
                userLocation.setValue(newUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(LOG_TAG, getString(R.string.log_error_occurred) + databaseError.getMessage());
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

    private boolean isPasswordValid(String password) {
        if (password.length() < 6) {
            mEditTextPasswordCreate.setError(getResources().getString(R.string.error_invalid_password_not_valid));
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

    private void successGoToActivity() {
        /* Go to main activity */
        Intent intent = new Intent(AccountCreateActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
