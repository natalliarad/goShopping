package com.natallia.radaman.goshopping.ui.listAct;

import android.app.Dialog;
import android.os.Bundle;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.natallia.radaman.goshopping.model.ShoppingList;
import com.natallia.radaman.goshopping.R;
import com.natallia.radaman.goshopping.utils.AppConstants;

import java.util.HashMap;

public class FragmentEditListNameDialog extends FragmentEditListDialog {
    private static final String LOG_TAG = FragmentEditListNameDialog.class.getSimpleName();
    String mListName;
    String mListId;

    /**
     * Public static constructor that creates fragment and passes a bundle with data into it when adapter is created
     */
    public static FragmentEditListNameDialog newInstance(ShoppingList shoppingList, String listId,
                                                         String encodedEmail) {
        FragmentEditListNameDialog editListNameDialogFragment = new FragmentEditListNameDialog();
        Bundle bundle = FragmentEditListDialog.newInstanceHelper(shoppingList,
                R.layout.dialog_edit_list, listId, encodedEmail);
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
        mListId = getArguments().getString(AppConstants.KEY_LIST_ID);
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
         * Set input text to be the current list name if it is not empty
         */
        if (!inputListName.equals("")) {

            if (mListName != null && mListId != null) {

                /**
                 * If editText input is not equal to the previous name
                 */
                if (!inputListName.equals(mListName)) {
                    /* Get the location to remove from */
                    DatabaseReference shoppingListRef = FirebaseDatabase.getInstance()
                            .getReferenceFromUrl(AppConstants.FIREBASE_URL_ACTIVE_LISTS).child(mListId);

                    /* Make a Hashmap for the specific properties you are changing */
                    HashMap<String, Object> updatedProperties = new HashMap<String, Object>();
                    updatedProperties.put(AppConstants.FIREBASE_PROPERTY_LIST_NAME, inputListName);

                    /* Add the timestamp for last changed to the updatedProperties Hashmap */
                    HashMap<String, Object> changedTimestampMap = new HashMap<>();
                    changedTimestampMap.put(AppConstants.FIREBASE_PROPERTY_TIMESTAMP,
                            ServerValue.TIMESTAMP);

                    /* Add the updated timestamp */
                    updatedProperties.put(AppConstants.FIREBASE_PROPERTY_TIMESTAMP_LAST_CHANGED,
                            changedTimestampMap);

                    /* Do the update */
                    shoppingListRef.updateChildren(updatedProperties);
                }
            }
        }
    }
}
