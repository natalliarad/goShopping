package com.natallia.radaman.goshopping.ui.listDetails;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.natallia.radaman.goshopping.R;
import com.natallia.radaman.goshopping.model.ShoppingList;
import com.natallia.radaman.goshopping.model.ShoppingListItem;
import com.natallia.radaman.goshopping.model.User;
import com.natallia.radaman.goshopping.ui.BaseActivity;
import com.natallia.radaman.goshopping.ui.listAct.FragmentAddListItemDialog;
import com.natallia.radaman.goshopping.ui.listAct.FragmentEditListItemNameDialog;
import com.natallia.radaman.goshopping.ui.listAct.FragmentEditListNameDialog;
import com.natallia.radaman.goshopping.ui.listAct.FragmentRemoveListDialog;
import com.natallia.radaman.goshopping.ui.listSharing.ShareListActivity;
import com.natallia.radaman.goshopping.utils.AppConstants;
import com.natallia.radaman.goshopping.utils.AppUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ListDetailsActivity extends BaseActivity {
    private static final String LOG_TAG = ListDetailsActivity.class.getSimpleName();
    private DatabaseReference mCurrentListRef, mCurrentUserRef;
    private ListFireBaseItemAdapter mListFireBaseItemAdapter;
    private Button mButtonShopping;
    private TextView mTextViewPeopleShopping;
    private ListView mListView;
    private String mListId;
    private User mCurrentUser;
    /* Stores whether the current user is shopping */
    private boolean mShopping = false;
    /* Stores whether the current user is the owner */
    private boolean mCurrentUserIsAuthor = false;
    private ShoppingList mShoppingList;
    private ValueEventListener mActiveListRefListener, mCurrentUserRefListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_details);
        /* Get the push ID from the extra passed by ShoppingListFragment */
        Intent intent = this.getIntent();
        mListId = intent.getStringExtra(AppConstants.KEY_LIST_ID);
        if (mListId == null) {
            /* No point in continuing without a valid ID. */
            finish();
            return;
        }
        /**
         * Create Firebase references
         */
        mCurrentListRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(AppConstants.FIREBASE_URL_USER_LISTS).child(mEncodedEmail).child(mListId);
        mCurrentUserRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(AppConstants.FIREBASE_URL_USERS).child(mEncodedEmail);
        DatabaseReference listItemsRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(AppConstants.FIREBASE_URL_SHOPPING_LIST_ITEMS).child(mListId);

        /**
         * Link layout elements from XML and setup the toolbar
         */
        initializeScreen();
        /**
         * Setup the adapter
         */
        Query query = listItemsRef.orderByChild(AppConstants.FIREBASE_PROPERTY_BOUGHT_BY);

        FirebaseListOptions<ShoppingListItem> options = new FirebaseListOptions.Builder<ShoppingListItem>()
                .setLayout(R.layout.single_active_list_item)
                .setQuery(query, ShoppingListItem.class)
                .setLifecycleOwner(this)
                .build();
        mListFireBaseItemAdapter = new ListFireBaseItemAdapter(options, this, mListId, mEncodedEmail);
        /* Create ActiveListItemAdapter and set to listView */
        mListView.setAdapter(mListFireBaseItemAdapter);

        /**
         * Save the most recent version of current shopping list into mShoppingList instance
         * variable an update the UI to match the current list.
         */

        /* Save the most up-to-date version of current user in mCurrentUser */
        mCurrentUserRefListener = mCurrentUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User currentUser = dataSnapshot.getValue(User.class);
                if (currentUser != null) mCurrentUser = currentUser;
                else finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(LOG_TAG, getString(R.string.log_error_the_read_failed) +
                        databaseError.getMessage());
            }
        });

        final Activity thisActivity = this;

        mActiveListRefListener = mCurrentListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                /**
                 * Saving the most recent version of current shopping list into mShoppingList if present
                 * finish() the activity if the list is null (list was removed or unshared by
                 * it's author while current user is in the list details activity)
                 */
                ShoppingList shoppingList = dataSnapshot.getValue(ShoppingList.class);
                if (shoppingList == null) {
                    finish();
                    /**
                     * Make sure to call return, otherwise the rest of the method will execute,
                     * even after calling finish.
                     */
                    return;
                }
                mShoppingList = shoppingList;
                /**
                 * Pass the shopping list to the adapter if it is not null.
                 * We do this here because mShoppingList is null when first created.
                 */
                mListFireBaseItemAdapter.setShoppingList(mShoppingList);

                /* Check if the current user is owner */
                mCurrentUserIsAuthor = AppUtils.checkIfAuthor(shoppingList, mEncodedEmail);
                /* Calling invalidateOptionsMenu causes onCreateOptionsMenu to be called */
                invalidateOptionsMenu();

                /* Set title appropriately. */
                setTitle(shoppingList.getListName());

                HashMap<String, User> usersShopping = mShoppingList.getUsersShopping();
                if (usersShopping != null && usersShopping.size() != 0 &&
                        usersShopping.containsKey(mEncodedEmail)) {
                    mShopping = true;
                    mButtonShopping.setText(getString(R.string.button_stop_shopping));
                    mButtonShopping.setBackgroundColor(ContextCompat.getColor(ListDetailsActivity
                            .this, R.color.dark_grey));
                } else {
                    mButtonShopping.setText(getString(R.string.button_start_shopping));
                    mButtonShopping.setBackgroundColor(ContextCompat.getColor(ListDetailsActivity.this,
                            R.color.primary_dark));
                    mShopping = false;
                }
                setWhosShoppingText(mShoppingList.getUsersShopping());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(LOG_TAG, getString(R.string.log_error_the_read_failed) + databaseError.getMessage());
            }
        });

        /**
         * Show edit list item name dialog on listView item long click event
         */
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                /* Check that the view is not the empty footer item */
                if (view.getId() != R.id.list_view_footer_empty) {
                    ShoppingListItem shoppingListItem = mListFireBaseItemAdapter.getItem(position);

                    if (shoppingListItem != null) {
                        /*
                        If the person is the owner and not shopping and the item is not bought, then
                        they can edit it.
                         */
                        if (shoppingListItem.getAuthor().equals(mEncodedEmail) && !mShopping &&
                                !shoppingListItem.isBought()) {
                            String itemName = shoppingListItem.getItemName();
                            String itemId = mListFireBaseItemAdapter.getRef(position).getKey();
                            showEditListItemNameDialog(itemName, itemId);
                            return true;
                        }
                    }
                }
                return false;
            }
        });

        /* Perform buy/return action on listView item click event if current user is shopping. */
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /* Check that the view is not the empty footer item */
                if (view.getId() != R.id.list_view_footer_empty) {
                    final ShoppingListItem selectedListItem = mListFireBaseItemAdapter.getItem(position);
                    String itemId = mListFireBaseItemAdapter.getRef(position).getKey();

                    if (selectedListItem != null) {
                        if (mShopping) {
                            /* Create map and fill it in with deep path multi write operations list */
                            HashMap<String, Object> updatedItemBoughtData = new HashMap<String, Object>();
                            /* Buy selected item if it is NOT already bought */
                            if (!selectedListItem.isBought()) {
                                updatedItemBoughtData.put(AppConstants.FIREBASE_PROPERTY_BOUGHT, true);
                                updatedItemBoughtData.put(AppConstants.FIREBASE_PROPERTY_BOUGHT_BY,
                                        mEncodedEmail);
                            } else {
                                /* Return selected item only if it was bought by current user */
                                if (selectedListItem.getBoughtBy().equals(mEncodedEmail)) {
                                    updatedItemBoughtData.put(AppConstants.FIREBASE_PROPERTY_BOUGHT, false);
                                    updatedItemBoughtData.put(AppConstants.FIREBASE_PROPERTY_BOUGHT_BY,
                                            null);
                                }
                            }
                            /* Do update */
                            DatabaseReference firebaseItemLocation = FirebaseDatabase.getInstance()
                                    .getReferenceFromUrl(AppConstants.FIREBASE_URL_SHOPPING_LIST_ITEMS)
                                    .child(mListId).child(itemId);
                            firebaseItemLocation.updateChildren(updatedItemBoughtData,
                                    new DatabaseReference.CompletionListener() {

                                        @Override
                                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                            if (databaseError != null) {
                                                Log.d(LOG_TAG, getString(R.string.log_error_updating_data) +
                                                        databaseError.getMessage());
                                            }
                                        }
                                    });
                        }
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu; this adds items to the action bar if it is present. */
        getMenuInflater().inflate(R.menu.menu_list_detail, menu);

        /**
         * Get menu items
         */
        MenuItem remove = menu.findItem(R.id.action_remove_list);
        MenuItem edit = menu.findItem(R.id.action_edit_list_name);
        MenuItem share = menu.findItem(R.id.action_share_list);
        MenuItem archive = menu.findItem(R.id.action_archive);

        /* Only the edit and remove options are implemented */
        remove.setVisible(mCurrentUserIsAuthor);
        edit.setVisible(mCurrentUserIsAuthor);
        share.setVisible(mCurrentUserIsAuthor);
        archive.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_edit_list_name:
                /**
                 * Show edit list dialog when the edit action is selected
                 */
                showEditListNameDialog();
                return true;
            case R.id.action_remove_list:
                /**
                 * removeList() when the remove action is selected
                 */
                removeList();
                return true;
            case R.id.action_share_list:
                Intent intent = new Intent(ListDetailsActivity.this, ShareListActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_archive:
                archiveList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mListFireBaseItemAdapter.stopListening();
        mCurrentListRef.removeEventListener(mActiveListRefListener);
        mCurrentUserRef.removeEventListener(mCurrentUserRefListener);
    }

    /**
     * Link layout elements from XML and setup the toolbar
     */
    private void initializeScreen() {
        mListView = findViewById(R.id.list_view_shopping_list_items);
        mTextViewPeopleShopping = findViewById(R.id.text_view_people_shopping);
        mButtonShopping = findViewById(R.id.button_shopping);
        Toolbar toolbar = findViewById(R.id.app_bar);
        /* Common toolbar setup */
        setSupportActionBar(toolbar);
        /* Add back button to the action bar */
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        /* Inflate the footer, set root layout to null*/
        View footer = getLayoutInflater().inflate(R.layout.footer, null);
        mListView.addFooterView(footer);
    }

    /**
     * Set appropriate text for Start/Stop shopping button and Who's shopping textView
     * depending on the current user shopping status
     */
    private void setWhosShoppingText(HashMap<String, User> usersShopping) {

        if (usersShopping != null) {
            ArrayList<String> usersWhoAreNotYou = new ArrayList<>();
            /**
             * If at least one user is shopping
             * Add userName to the list of users shopping if this user is not current user
             */
            for (User user : usersShopping.values()) {
                if (user != null && !(user.getEmail().equals(mEncodedEmail))) {
                    usersWhoAreNotYou.add(user.getName());
                }
            }

            int numberOfUsersShopping = usersShopping.size();
            String usersShoppingText;

            /**
             * If current user is shopping...
             * If current user is the only person shopping, set text to "You are shopping"
             * If current user and one user are shopping, set text "You and userName are shopping"
             * Else set text "You and N others shopping"
             */
            if (mShopping) {
                switch (numberOfUsersShopping) {
                    case 1:
                        usersShoppingText = getString(R.string.text_you_are_shopping);
                        break;
                    case 2:
                        usersShoppingText = String.format(
                                getString(R.string.text_you_and_other_are_shopping),
                                usersWhoAreNotYou.get(0));
                        break;
                    default:
                        usersShoppingText = String.format(
                                getString(R.string.text_you_and_number_are_shopping),
                                usersWhoAreNotYou.size());
                }
                /**
                 * If current user is not shopping..
                 * If there is only one person shopping, set text to "userName is shopping"
                 * If there are two users shopping, set text "userName1 and userName2 are shopping"
                 * Else set text "userName and N others shopping"
                 */
            } else {
                switch (numberOfUsersShopping) {
                    case 1:
                        usersShoppingText = String.format(
                                getString(R.string.text_other_is_shopping),
                                usersWhoAreNotYou.get(0));
                        break;
                    case 2:
                        usersShoppingText = String.format(
                                getString(R.string.text_other_and_other_are_shopping),
                                usersWhoAreNotYou.get(0),
                                usersWhoAreNotYou.get(1));
                        break;
                    default:
                        usersShoppingText = String.format(
                                getString(R.string.text_other_and_number_are_shopping),
                                usersWhoAreNotYou.get(0),
                                usersWhoAreNotYou.size() - 1);
                }
            }
            mTextViewPeopleShopping.setText(usersShoppingText);
        } else {
            mTextViewPeopleShopping.setText("");
        }
    }

    /**
     * Archive current list when user selects "Archive" menu item
     */
    public void archiveList() {
    }

    /**
     * Start AddItemsFromProductActivity to add meal ingredients into the shopping list
     * when the user taps on "add product" fab
     */
    public void addProduct(View view) {
    }

    /**
     * Remove current shopping list and its items from all nodes
     */
    public void removeList() {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = FragmentRemoveListDialog.newInstance(mShoppingList, mListId);
        dialog.show(getSupportFragmentManager(), "FragmentRemoveListDialog");
    }

    /**
     * Show the add list item dialog when user taps "Add list item" fab
     */
    public void showAddListItemDialog(View view) {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = FragmentAddListItemDialog.newInstance(mShoppingList, mListId, mEncodedEmail);
        dialog.show(getSupportFragmentManager(), "FragmentAddListItemDialog");
    }

    /**
     * Show edit list name dialog when user selects "Edit list name" menu item
     */
    public void showEditListNameDialog() {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = FragmentEditListNameDialog.newInstance(mShoppingList, mListId, mEncodedEmail);
        dialog.show(this.getSupportFragmentManager(), "FragmentEditListNameDialog");
    }

    /**
     * Show the edit list item name dialog after longClick on the particular item
     */
    public void showEditListItemNameDialog(String itemName, String itemId) {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = FragmentEditListItemNameDialog.newInstance(mShoppingList, itemName,
                itemId, mListId, mEncodedEmail);
        dialog.show(this.getSupportFragmentManager(), "FragmentEditListItemNameDialog");
    }

    /**
     * This method is called when user taps "Start/Stop shopping" button
     */
    public void toggleShopping(View view) {
        /**
         * Create map and fill it in with deep path multi write operations list
         */
        HashMap<String, Object> updatedUserData = new HashMap<String, Object>();
        String propertyToUpdate = AppConstants.FIREBASE_PROPERTY_USERS_SHOPPING + "/" + mEncodedEmail;
        if (mShopping) {
            /* Add the value to update at the specified property for all lists */
            AppUtils.updateMapForAllWithValue(mListId, mShoppingList.getAuthor(), updatedUserData,
                    propertyToUpdate, null);
            /* Appends the timestamp changes for all lists */
            AppUtils.updateMapWithTimestampLastChanged(mListId, mShoppingList.getAuthor(),
                    updatedUserData);
            /* Do a deep-path update */
            mFirebaseRef.updateChildren(updatedUserData);
        } else {
            /**
             * If current user is not shopping, create map to represent User model add to usersShopping map
             */
            HashMap<String, Object> currentUser = (HashMap<String, Object>)
                    new ObjectMapper().convertValue(mCurrentUser, Map.class);

            /* Add the value to update at the specified property for all lists */
            AppUtils.updateMapForAllWithValue(mListId, mShoppingList.getAuthor(), updatedUserData,
                    propertyToUpdate, currentUser);

            /* Appends the timestamp changes for all lists */
            AppUtils.updateMapWithTimestampLastChanged(mListId, mShoppingList.getAuthor(),
                    updatedUserData);

            /* Do a deep-path update */
            mFirebaseRef.updateChildren(updatedUserData);
        }
    }
}
