package com.natallia.radaman.goshopping.ui.listSharing;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ListView;

import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.natallia.radaman.goshopping.R;
import com.natallia.radaman.goshopping.model.User;
import com.natallia.radaman.goshopping.ui.BaseActivity;
import com.natallia.radaman.goshopping.utils.AppConstants;

public class InviteFriendActivity extends BaseActivity {
    private EditText mEditTextAddFriendEmail;
    private AutocompleteFriendAdapter mFriendsAutocompleteAdapter;
    private String mInput;
    private ListView mListViewAutocomplete;
    private DatabaseReference mUsersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friend);
        /* Create Firebase references */
        mUsersRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(AppConstants.FIREBASE_URL_USERS);

        /* Link layout elements from XML and setup the toolbar */
        initializeScreen();

        /* Set interactive bits, such as click events/adapters */
        mEditTextAddFriendEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                /* Get the input after every textChanged event and transform it to lowercase */
                mInput = mEditTextAddFriendEmail.getText().toString().toLowerCase();

                /* Clean up the old adapter */
                if (mFriendsAutocompleteAdapter != null)
                    mFriendsAutocompleteAdapter.stopListening();
                /* Nullify the adapter data if the input length is less than 2 characters */
                if (mInput.equals("") || mInput.length() < 2) {
                    mListViewAutocomplete.setAdapter(null);

                    /* Define and set the adapter otherwise. */
                } else {
                    /* Setup the adapter */
                    Query query = mUsersRef.orderByChild(AppConstants.FIREBASE_PROPERTY_EMAIL)
                            .startAt(mInput).endAt(mInput + "~").limitToFirst(5);

                    FirebaseListOptions<User> options = new FirebaseListOptions.Builder<User>()
                            .setLayout(R.layout.single_autocomplete_item)
                            .setQuery(query, User.class)
                            .setLifecycleOwner(InviteFriendActivity.this)
                            .build();
                    mFriendsAutocompleteAdapter = new AutocompleteFriendAdapter(options,
                            InviteFriendActivity.this, mEncodedEmail);
                    /* Create ActiveListItemAdapter and set to listView */
                    mListViewAutocomplete.setAdapter(mFriendsAutocompleteAdapter);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFriendsAutocompleteAdapter != null) {
            mFriendsAutocompleteAdapter.stopListening();
        }
    }

    /**
     * Link layout elements from XML and setup the toolbar
     */
    public void initializeScreen() {
        mListViewAutocomplete = findViewById(R.id.list_view_friends_autocomplete);
        mEditTextAddFriendEmail = findViewById(R.id.edit_text_add_friend_email);
        Toolbar toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        /* Add back button to the action bar */
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}
