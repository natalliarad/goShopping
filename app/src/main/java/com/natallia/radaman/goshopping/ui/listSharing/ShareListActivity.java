package com.natallia.radaman.goshopping.ui.listSharing;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.natallia.radaman.goshopping.R;
import com.natallia.radaman.goshopping.model.ShoppingList;
import com.natallia.radaman.goshopping.model.User;
import com.natallia.radaman.goshopping.ui.BaseActivity;
import com.natallia.radaman.goshopping.utils.AppConstants;

import java.util.HashMap;

/**
 * Allows for you to check and un-check friends that you share the current list with
 */
public class ShareListActivity extends BaseActivity {
    private static final String LOG_TAG = ShareListActivity.class.getSimpleName();
    private FriendAdapter mFriendAdapter;
    private ListView mListView;
    private ShoppingList mShoppingList;
    private String mListId;
    private DatabaseReference mActiveListRef, mSharedWithRef;
    private ValueEventListener mActiveListRefListener, mSharedWithListener;
    private HashMap<String, User> mSharedWithUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_list);
        /* Get the push ID from the extra passed by ActiveListDetailsActivity */
        Intent intent = this.getIntent();
        mListId = intent.getStringExtra(AppConstants.KEY_LIST_ID);
        if (mListId == null) {
            /* No point in continuing without a valid ID. */
            finish();
            return;
        }
        /* Link layout elements from XML and setup the toolbar */
        initializeScreen();

        /* Create Firebase references */
        DatabaseReference currentUserFriendsRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(AppConstants.FIREBASE_URL_USER_FRIENDS).child(mEncodedEmail);
        mActiveListRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(AppConstants.FIREBASE_URL_USER_LISTS).child(mEncodedEmail).child(mListId);
        mSharedWithRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(AppConstants.FIREBASE_URL_LISTS_SHARED_WITH).child(mListId);


        /* Add ValueEventListeners to Firebase references
         * to control get data and control behavior and visibility of elements */

        mActiveListRefListener = mActiveListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ShoppingList shoppingList = dataSnapshot.getValue(ShoppingList.class);

                /* Saving the most recent version of current shopping list into mShoppingList
                 * and pass it to setShoppingList() if present
                 * finish() the activity otherwise */
                if (shoppingList != null) {
                    mShoppingList = shoppingList;
                    mFriendAdapter.setShoppingList(mShoppingList);
                } else {
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(LOG_TAG, getString(R.string.log_error_the_read_failed) +
                        databaseError.getMessage());
            }
        });

        mSharedWithListener = mSharedWithRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mSharedWithUsers = new HashMap<>();
                for (DataSnapshot currentUser : dataSnapshot.getChildren()) {
                    mSharedWithUsers.put(currentUser.getKey(), currentUser.getValue(User.class));
                }
                mFriendAdapter.setSharedWithUsers(mSharedWithUsers);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(LOG_TAG, getString(R.string.log_error_the_read_failed) +
                        databaseError.getMessage());
            }
        });

        /* Setup the adapter */
        Query query = currentUserFriendsRef.orderByKey();

        FirebaseListOptions<User> options = new FirebaseListOptions.Builder<User>()
                .setLayout(R.layout.single_user_item)
                .setQuery(query, User.class)
                .setLifecycleOwner(this)
                .build();
        mFriendAdapter = new FriendAdapter(options, this, mListId);
        /* Create ActiveListItemAdapter and set to listView */
        mListView.setAdapter(mFriendAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFriendAdapter.cleanup();
        mFriendAdapter.stopListening();
        mActiveListRef.removeEventListener(mActiveListRefListener);
        mSharedWithRef.removeEventListener(mSharedWithListener);
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
