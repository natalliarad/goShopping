package com.natallia.radaman.goshopping.ui.listAct;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.natallia.radaman.goshopping.model.ShoppingList;

import com.natallia.radaman.goshopping.R;
import com.natallia.radaman.goshopping.model.ShoppingListItem;
import com.natallia.radaman.goshopping.model.User;
import com.natallia.radaman.goshopping.utils.AppConstants;
import com.natallia.radaman.goshopping.utils.AppUtils;

import java.util.HashMap;
import java.util.Map;

public class FragmentAddListItemDialog extends FragmentEditListDialog {
    /**
     * Public static constructor that creates fragment and passes a bundle with data into it when adapter is created
     */
    public static FragmentAddListItemDialog newInstance(ShoppingList shoppingList, String listId,
                                                        String encodedEmail,
                                                        HashMap<String, User> sharedWithUsers) {
        FragmentAddListItemDialog addListItemDialogFragment = new FragmentAddListItemDialog();

        Bundle bundle = FragmentEditListDialog.newInstanceHelper(shoppingList,
                R.layout.dialog_add_item, listId, encodedEmail, sharedWithUsers);
        addListItemDialogFragment.setArguments(bundle);

        return addListItemDialogFragment;
    }

    /**
     * Initialize instance variables with data from bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        /** {@link FragmentEditListDialog#createDialogHelper(int)} is a
         * superclass method that creates the dialog
         **/
        return super.createDialogHelper(R.string.positive_button_add_list_item);
    }

    /**
     * Adds new item to the current shopping list
     */
    @Override
    protected void doListEdit() {
        String mItemName = mEditTextForList.getText().toString();
        /**
         * Adds list item if the input name is not empty
         */
        if (!mItemName.equals("")) {
            DatabaseReference firebaseRef = FirebaseDatabase.getInstance()
                    .getReferenceFromUrl(AppConstants.FIREBASE_URL);
            DatabaseReference itemsRef = FirebaseDatabase.getInstance()
                    .getReferenceFromUrl(AppConstants.FIREBASE_URL_SHOPPING_LIST_ITEMS).child(mListId);

            /* Make a map for the item you are adding */
            HashMap<String, Object> updatedItemToAddMap = new HashMap<>();

            /* Save push() to maintain same random Id */
            DatabaseReference newRef = itemsRef.push();
            String itemId = newRef.getKey();

            /* Make a POJO for the item and immediately turn it into a HashMap */
            ShoppingListItem itemToAddObject = new ShoppingListItem(mItemName, mEncodedEmail);
            HashMap<String, Object> itemToAdd =
                    (HashMap<String, Object>) new ObjectMapper().convertValue(itemToAddObject, Map.class);


            /* Add the item to the update map*/
            updatedItemToAddMap.put("/" + AppConstants.FIREBASE_LOCATION_SHOPPING_LIST_ITEMS + "/"
                    + mListId + "/" + itemId, itemToAdd);

            /* Update affected lists timestamps */
            AppUtils.updateMapWithTimestampLastChanged(mSharedWith, mListId, mAuthor, updatedItemToAddMap);

            /* Do the update */
            firebaseRef.updateChildren(updatedItemToAddMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    /* Now that we have the timestamp, update the reversed timestamp */
                    AppUtils.updateTimestampReversed(databaseError, "AddListItem", mListId,
                            mSharedWith, mAuthor);
                }
            });

            /**
             * Close the dialog fragment when done
             */
            FragmentAddListItemDialog.this.getDialog().cancel();
        }
    }

}
