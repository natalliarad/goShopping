package com.natallia.radaman.goshopping.ui.listSharing;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.ListView;

import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.natallia.radaman.goshopping.R;
import com.natallia.radaman.goshopping.model.ShoppingListItem;
import com.natallia.radaman.goshopping.model.User;
import com.natallia.radaman.goshopping.ui.BaseActivity;
import com.natallia.radaman.goshopping.ui.listDetails.ListFireBaseItemAdapter;
import com.natallia.radaman.goshopping.utils.AppConstants;

public class InviteFriendActivity extends BaseActivity {
    private EditText mEditTextAddFriendEmail;
    private AutocompleteFriendAdapter mFriendsAutocompleteAdapter;
    private ListView mListViewAutocomplete;
    private DatabaseReference mUsersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friend);
        /**
         * Create Firebase references
         */
        mUsersRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(AppConstants.FIREBASE_URL_USERS);

        /**
         * Link layout elements from XML and setup the toolbar
         */
        initializeScreen();

        /**
         * Setup the adapter
         */
        Query query = mUsersRef.orderByChild(AppConstants.FIREBASE_PROPERTY_EMAIL);

        FirebaseListOptions<User> options = new FirebaseListOptions.Builder<User>()
                .setLayout(R.layout.single_autocomplete_item)
                .setQuery(query, User.class)
                .setLifecycleOwner(this)
                .build();
        mFriendsAutocompleteAdapter = new AutocompleteFriendAdapter(options, this, mEncodedEmail);
        /* Create ActiveListItemAdapter and set to listView */
        mListViewAutocomplete.setAdapter(mFriendsAutocompleteAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFriendsAutocompleteAdapter.stopListening();
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
