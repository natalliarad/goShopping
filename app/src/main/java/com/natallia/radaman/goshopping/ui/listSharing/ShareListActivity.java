package com.natallia.radaman.goshopping.ui.listSharing;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.natallia.radaman.goshopping.R;
import com.natallia.radaman.goshopping.model.User;
import com.natallia.radaman.goshopping.ui.BaseActivity;
import com.natallia.radaman.goshopping.utils.AppConstants;

/**
 * Allows for you to check and un-check friends that you share the current list with
 */
public class ShareListActivity extends BaseActivity {
    private static final String LOG_TAG = ShareListActivity.class.getSimpleName();
    private FriendAdapter mFriendAdapter;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_list);
        /**
         * Link layout elements from XML and setup the toolbar
         */
        initializeScreen();

        /**
         * Create Firebase references
         */
        DatabaseReference currentUserFriendsRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(AppConstants.FIREBASE_URL_USER_FRIENDS).child(mEncodedEmail);

        /**
         * Setup the adapter
         */
        Query query = currentUserFriendsRef.orderByKey();

        FirebaseListOptions<User> options = new FirebaseListOptions.Builder<User>()
                .setLayout(R.layout.single_user_item)
                .setQuery(query, User.class)
                .setLifecycleOwner(this)
                .build();
        mFriendAdapter = new FriendAdapter(options, this);
        /* Create ActiveListItemAdapter and set to listView */
        mListView.setAdapter(mFriendAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFriendAdapter.stopListening();
    }

    /**
     * Link layout elements from XML and setup the toolbar
     */
    public void initializeScreen() {
        mListView = findViewById(R.id.list_view_friend_share);
        Toolbar toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        /* Add back button to the action bar */
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Launch AddFriendActivity to find and add user to current user's friends list
     * when the button AddFriend is pressed
     */
    public void onAddFriendPressed(View view) {
        Intent intent = new Intent(ShareListActivity.this, InviteFriendActivity.class);
        startActivity(intent);
    }
}
