package com.natallia.radaman.goshopping.ui.listDetails;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
import com.natallia.radaman.goshopping.model.ShoppingListItem;
import com.natallia.radaman.goshopping.ui.BaseActivity;
import com.natallia.radaman.goshopping.ui.listAct.FragmentAddListItemDialog;
import com.natallia.radaman.goshopping.ui.listAct.FragmentEditListItemNameDialog;
import com.natallia.radaman.goshopping.ui.listAct.FragmentEditListNameDialog;
import com.natallia.radaman.goshopping.ui.listAct.FragmentRemoveListDialog;
import com.natallia.radaman.goshopping.utils.AppConstants;
import com.natallia.radaman.goshopping.utils.AppUtils;

public class ListDetailsActivity extends BaseActivity {
    private static final String LOG_TAG = ListDetailsActivity.class.getSimpleName();
    private DatabaseReference mActiveListRef;
    private ListFireBaseItemAdapter mListFireBaseItemAdapter;
    private ListView mListView;
    private String mListId;
    /* Stores whether the current user is the owner */
    private boolean mCurrentUserIsAuthor = false;
    private ShoppingList mShoppingList;
    private ValueEventListener mActiveListRefListener;

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
        mActiveListRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(AppConstants.FIREBASE_URL_ACTIVE_LISTS).child(mListId);
        DatabaseReference listItemsRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(AppConstants.FIREBASE_URL_SHOPPING_LIST_ITEMS).child(mListId);

        /**
         * Link layout elements from XML and setup the toolbar
         */
        initializeScreen();
        /**
         * Setup the adapter
         */
        Query query = listItemsRef.orderByKey();

        FirebaseListOptions<ShoppingListItem> options = new FirebaseListOptions.Builder<ShoppingListItem>()
                .setLayout(R.layout.single_active_list_item)
                .setQuery(query, ShoppingListItem.class)
                .setLifecycleOwner(this)
                .build();
        mListFireBaseItemAdapter = new ListFireBaseItemAdapter(options, this, mListId);
        /* Create ActiveListItemAdapter and set to listView */
        mListView.setAdapter(mListFireBaseItemAdapter);

        /**
         * Save the most recent version of current shopping list into mShoppingList instance
         * variable an update the UI to match the current list.
         */

        mActiveListRefListener = mActiveListRef.addValueEventListener(new ValueEventListener() {
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
                        String itemName = shoppingListItem.getItemName();
                        String itemId = mListFireBaseItemAdapter.getRef(position).getKey();

                        showEditListItemNameDialog(itemName, itemId);
                        return true;
                    }
                }
                return false;
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
        share.setVisible(false);
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
        mActiveListRef.removeEventListener(mActiveListRefListener);
    }

    /**
     * Link layout elements from XML and setup the toolbar
     */
    private void initializeScreen() {
        mListView = findViewById(R.id.list_view_shopping_list_items);
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

    }
}
