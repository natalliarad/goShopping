package com.natallia.radaman.goshopping.ui.listAct;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.natallia.radaman.goshopping.R;
import com.natallia.radaman.goshopping.model.ShoppingList;
import com.natallia.radaman.goshopping.model.User;
import com.natallia.radaman.goshopping.utils.AppConstants;
import com.natallia.radaman.goshopping.utils.AppUtils;

import java.util.HashMap;

public class FragmentEditListItemNameDialog extends FragmentEditListDialog {
    String mItemName, mItemId;

    /**
     * Public static constructor that creates fragment and passes a bundle with data into it when adapter is created
     */
    public static FragmentEditListItemNameDialog newInstance(ShoppingList shoppingList, String itemName,
                                                             String itemId, String listId, String encodedEmail,
                                                             HashMap<String, User> sharedWithUsers) {
        FragmentEditListItemNameDialog editListItemNameDialogFragment = new FragmentEditListItemNameDialog();

        Bundle bundle = FragmentEditListDialog.newInstanceHelper(shoppingList, R.layout.dialog_edit_item,
                listId, encodedEmail, sharedWithUsers);
        bundle.putString(AppConstants.KEY_LIST_ITEM_NAME, itemName);
        bundle.putString(AppConstants.KEY_LIST_ITEM_ID, itemId);
        editListItemNameDialogFragment.setArguments(bundle);
        return editListItemNameDialogFragment;
    }

    /**
     * Initialize instance variables with data from bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mItemName = getArguments().getString(AppConstants.KEY_LIST_ITEM_NAME);
        mItemId = getArguments().getString(AppConstants.KEY_LIST_ITEM_ID);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        /** {@link FragmentEditListDialog#createDialogHelper(int)} is a
         * superclass method that creates the dialog
         */
        Dialog dialog = super.createDialogHelper(R.string.positive_button_edit_item);
        /**
         * {@link EditListDialogFragment#helpSetDefaultValueEditText(String)} is a superclass
         * method that sets the default text of the TextView
         */
        super.helpSetDefaultValueEditText(mItemName);

        return dialog;
    }

    /**
     * Change selected list item name to the editText input if it is not empty
     */
    protected void doListEdit() {
        String nameInput = mEditTextForList.getText().toString();
        /**
         * Set input text to be the current list item name if it is not empty and is not the
         * previous name.
         */
        if (!nameInput.equals("") && !nameInput.equals(mItemName)) {
            DatabaseReference firebaseRef = FirebaseDatabase.getInstance()
                    .getReferenceFromUrl(AppConstants.FIREBASE_URL);

            /* Make a map for the item you are changing the name of */
            HashMap<String, Object> updatedDataItemToEditMap = new HashMap<>();

            /* Add the new name to the update map*/
            updatedDataItemToEditMap.put("/" + AppConstants.FIREBASE_LOCATION_SHOPPING_LIST_ITEMS + "/"
                            + mListId + "/" + mItemId + "/" + AppConstants.FIREBASE_PROPERTY_ITEM_NAME,
                    nameInput);

            /* Update affected lists timestamps */
            AppUtils.updateMapWithTimestampLastChanged(mSharedWith, mListId, mAuthor,
                    updatedDataItemToEditMap);

            /* Do the update */
            firebaseRef.updateChildren(updatedDataItemToEditMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    /* Now that we have the timestamp, update the reversed timestamp */
                    AppUtils.updateTimestampReversed(databaseError, "EditListItem", mListId,
                            mSharedWith, mAuthor);
                }
            });
        }
    }
}
