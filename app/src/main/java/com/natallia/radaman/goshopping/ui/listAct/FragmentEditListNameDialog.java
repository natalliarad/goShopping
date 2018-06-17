package com.natallia.radaman.goshopping.ui.listAct;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.natallia.radaman.goshopping.model.ShoppingList;
import com.natallia.radaman.goshopping.R;
import com.natallia.radaman.goshopping.model.User;
import com.natallia.radaman.goshopping.utils.AppConstants;
import com.natallia.radaman.goshopping.utils.AppUtils;

import java.util.HashMap;

public class FragmentEditListNameDialog extends FragmentEditListDialog {
    private static final String LOG_TAG = FragmentEditListNameDialog.class.getSimpleName();
    String mListName;
    //String mListId;
    //HashMap mSharedWith;

    /**
     * Public static constructor that creates fragment and passes a bundle with data into it when adapter is created
     */
    public static FragmentEditListNameDialog newInstance(ShoppingList shoppingList, String listId,
                                                         String encodedEmail,
                                                         HashMap<String, User> sharedWithUsers) {
        FragmentEditListNameDialog editListNameDialogFragment = new FragmentEditListNameDialog();
        Bundle bundle = FragmentEditListDialog.newInstanceHelper(shoppingList,
                R.layout.dialog_edit_list, listId, encodedEmail, sharedWithUsers);
        bundle.putString(AppConstants.KEY_LIST_NAME, shoppingList.getListName());
        editListNameDialogFragment.setArguments(bundle);
        return editListNameDialogFragment;
    }

    /**
     * Initialize instance variables with data from bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListName = getArguments().getString(AppConstants.KEY_LIST_NAME);
        //mListId = getArguments().getString(AppConstants.KEY_LIST_ID);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        /** {@link EditListDialogFragment#createDialogHelper(int)} is a
         * superclass method that creates the dialog
         **/
        Dialog dialog = super.createDialogHelper(R.string.positive_button_edit_item);
        /**
         * {@link EditListDialogFragment#helpSetDefaultValueEditText(String)} is a superclass
         * method that sets the default text of the TextView
         */
        helpSetDefaultValueEditText(mListName);

        return dialog;
    }

    /**
     * Changes the list name in all copies of the current list
     */
    protected void doListEdit() {
        final String inputListName = mEditTextForList.getText().toString();

        /**
         * Check that the user inputted list name is not empty, has changed the original name
         * and that the dialog was properly initialized with the current name and id of the list.
         */
        if (!inputListName.equals("") && mListName != null &&
                mListId != null && !inputListName.equals(mListName)) {
            /* Get the location to remove from */
            DatabaseReference firebaseRef = FirebaseDatabase.getInstance()
                    .getReferenceFromUrl(AppConstants.FIREBASE_URL);

            /**
             * Create map and fill it in with deep path multi write operations list
             */
            HashMap<String, Object> updatedListData = new HashMap<>();

            /* Add the value to update at the specified property for all lists */
            AppUtils.updateMapForAllWithValue(mSharedWith, mListId, mAuthor, updatedListData,
                    AppConstants.FIREBASE_PROPERTY_LIST_NAME, inputListName);

            /* Update affected lists timestamps */
            AppUtils.updateMapWithTimestampLastChanged(mSharedWith, mListId, mAuthor, updatedListData);


            /* Do a deep-path update */
            firebaseRef.updateChildren(updatedListData, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    /* Now that we have the timestamp, update the reversed timestamp */
                    AppUtils.updateTimestampReversed(databaseError, LOG_TAG, mListId,
                            mSharedWith, mAuthor);
                }
            });
        }
    }
}
