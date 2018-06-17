package com.natallia.radaman.goshopping.ui.listAct;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.natallia.radaman.goshopping.R;
import com.natallia.radaman.goshopping.model.ShoppingList;
import com.natallia.radaman.goshopping.utils.AppConstants;
import com.natallia.radaman.goshopping.utils.AppUtils;

import java.util.HashMap;
import java.util.Map;

public class FragmentAddListDialog extends DialogFragment {
    String mEncodedEmail;
    EditText mEditTextListName;

    /**
     * Public static constructor that creates fragment and
     * passes a bundle with data into it when adapter is created
     */
    public static FragmentAddListDialog newInstance(String encodedEmail) {
        FragmentAddListDialog addListDialogFragment = new FragmentAddListDialog();
        Bundle bundle = new Bundle();
        bundle.putString(AppConstants.KEY_ENCODED_EMAIL, encodedEmail);
        addListDialogFragment.setArguments(bundle);
        return addListDialogFragment;
    }

    /**
     * Initialize instance variables with data from bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEncodedEmail = getArguments().getString(AppConstants.KEY_ENCODED_EMAIL);
    }

    /**
     * Open the keyboard automatically when the dialog fragment is opened
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomTheme_Dialog);
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.fragment_add_list_dialog, null);
        mEditTextListName = rootView.findViewById(R.id.edit_text_list_name);

        /**
         * Call addShoppingList() when user taps "Done" keyboard action
         */
        mEditTextListName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE || keyEvent.getAction() == KeyEvent
                        .ACTION_DOWN) {
                    addShoppingList();
                }
                return true;
            }
        });

        /* Inflate and set the layout for the dialog */
        /* Pass null as the parent view because its going in the dialog layout*/
        builder.setView(rootView)
                /* Add action buttons */
                .setPositiveButton(R.string.button_positive_create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        addShoppingList();
                    }
                });

        return builder.create();
    }

    /**
     * Add new active list
     */
    public void addShoppingList() {
        String userEnteredName = mEditTextListName.getText().toString();
        /**
         * If EditText input is not empty
         */
        if (!userEnteredName.equals("")) {
            /**
             * Create Firebase references
             */
            DatabaseReference userListsRef = FirebaseDatabase.getInstance()
                    .getReferenceFromUrl(AppConstants.FIREBASE_URL_USER_LISTS).child(mEncodedEmail);
            final DatabaseReference firebaseRef = FirebaseDatabase.getInstance()
                    .getReferenceFromUrl(AppConstants.FIREBASE_URL);
            DatabaseReference newListRef = userListsRef.push();


            /* Save listsRef.push() to maintain same random Id */
            final String listId = newListRef.getKey();

            /* HashMap for data to update */
            HashMap<String, Object> updateShoppingListData = new HashMap<>();

            /**
             * Set raw version of date to the ServerValue.TIMESTAMP value and save into
             * timestampCreatedMap
             */
            HashMap<String, Object> timestampCreated = new HashMap<>();
            timestampCreated.put(AppConstants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

            /* Build the shopping list */
            ShoppingList newShoppingList = new ShoppingList(userEnteredName, mEncodedEmail,
                    timestampCreated);

            HashMap<String, Object> shoppingListMap = (HashMap<String, Object>)
                    new ObjectMapper().convertValue(newShoppingList, Map.class);

            AppUtils.updateMapForAllWithValue(null, listId, mEncodedEmail,
                    updateShoppingListData, "", shoppingListMap);

            firebaseRef.updateChildren(updateShoppingListData);

            /* Close the dialog fragment */
            FragmentAddListDialog.this.getDialog().cancel();
        }
    }
}
