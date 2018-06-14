package com.natallia.radaman.goshopping.ui.listDetails;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.natallia.radaman.goshopping.R;
import com.natallia.radaman.goshopping.model.ShoppingList;
import com.natallia.radaman.goshopping.model.ShoppingListItem;
import com.natallia.radaman.goshopping.utils.AppConstants;

import java.util.HashMap;

public class ListFireBaseItemAdapter extends FirebaseListAdapter<ShoppingListItem> {
    Activity mActivity;
    private ShoppingList mShoppingList;
    private String mListId;

    /**
     * Public constructor that initializes private instance variables when adapter is created
     */
    public ListFireBaseItemAdapter(FirebaseListOptions<ShoppingListItem> options, Activity
            activity, String listId) {
        super(options);
        this.mActivity = activity;
        this.mListId = listId;
    }

    /**
     * Public method that is used to pass shoppingList object when it is loaded in ValueEventListener
     */
    public void setShoppingList(ShoppingList shoppingList) {
        this.mShoppingList = shoppingList;
        this.notifyDataSetChanged();
    }

    /**
     * Protected method that populates the view attached to the adapter (list_view_friends_autocomplete)
     * with items inflated from single_active_list_item.xml
     * populateView also handles data changes and updates the listView accordingly
     */
    @Override
    protected void populateView(View view, ShoppingListItem item, int position) {
        ImageButton buttonRemoveItem = view.findViewById(R.id.button_remove_item);
        TextView textViewMealItemName = view.findViewById(R.id.text_view_active_list_item_name);

        textViewMealItemName.setText(item.getItemName());

        /* Gets the id of the item to remove */
        final String itemToRemoveId = this.getRef(position).getKey();

        /**
         * Set the on click listener for "Remove list item" button
         */
        buttonRemoveItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity, R.style
                        .CustomTheme_Dialog)
                        .setTitle(mActivity.getString(R.string.remove_item_option))
                        .setMessage(mActivity.getString(R.string.dialog_message_are_you_sure_remove_item))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                removeItem(itemToRemoveId);
                                /* Dismiss the dialog */
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                /* Dismiss the dialog */
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert);

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });
    }

    private void removeItem(String itemId) {
        DatabaseReference listItemsRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(AppConstants.FIREBASE_URL);

        /* Make a map for the removal */
        HashMap<String, Object> updatedRemoveItemMap = new HashMap<String, Object>();

        /* Remove the item by passing null */
        updatedRemoveItemMap.put("/" + AppConstants.FIREBASE_LOCATION_SHOPPING_LIST_ITEMS + "/"
                + mListId + "/" + itemId, null);

        /* Make the timestamp for last changed */
        HashMap<String, Object> changedTimestampMap = new HashMap<>();
        changedTimestampMap.put(AppConstants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

        /* Add the updated timestamp */
        updatedRemoveItemMap.put("/" + AppConstants.FIREBASE_LOCATION_ACTIVE_LISTS +
                        "/" + mListId + "/" + AppConstants.FIREBASE_PROPERTY_TIMESTAMP_LAST_CHANGED,
                changedTimestampMap);

        /* Do the update */
        listItemsRef.updateChildren(updatedRemoveItemMap);
    }
}
