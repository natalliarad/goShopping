package com.natallia.radaman.goshopping.ui.listAct;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.natallia.radaman.goshopping.R;
import com.natallia.radaman.goshopping.model.ShoppingList;
import com.natallia.radaman.goshopping.utils.AppConstants;
import com.natallia.radaman.goshopping.utils.AppUtils;

import java.util.HashMap;

public class FragmentRemoveListDialog extends DialogFragment {
    String mListId;
    String mListAuthor;
    final static String LOG_TAG = FragmentRemoveListDialog.class.getSimpleName();

    /**
     * Public static constructor that creates fragment and passes a bundle with data into it when adapter is created
     */
    public static FragmentRemoveListDialog newInstance(ShoppingList shoppingList, String listId) {
        FragmentRemoveListDialog removeListDialogFragment = new FragmentRemoveListDialog();
        Bundle bundle = new Bundle();
        bundle.putString(AppConstants.KEY_LIST_ID, listId);
        bundle.putString(AppConstants.KEY_LIST_OWNER, shoppingList.getAuthor());
        removeListDialogFragment.setArguments(bundle);
        return removeListDialogFragment;
    }

    /**
     * Initialize instance variables with data from bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListId = getArguments().getString(AppConstants.KEY_LIST_ID);
        mListAuthor = getArguments().getString(AppConstants.KEY_LIST_OWNER);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomTheme_Dialog)
                .setTitle(getActivity().getResources().getString(R.string.menu_remove_list))
                .setMessage(getString(R.string.dialog_message_are_you_sure_remove_list))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        removeList();
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

        return builder.create();
    }

    private void removeList() {
        DatabaseReference listToRemoveRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(AppConstants.FIREBASE_URL);
        /**
         * Create map and fill it in with deep path multi write operations list
         */
        HashMap<String, Object> removeListData = new HashMap<String, Object>();

        /* Remove the ShoppingLists from both user lists and active lists */
        AppUtils.updateMapForAllWithValue(mListId, mListAuthor, removeListData,
                "", null);

        /* Remove the associated list items */
        removeListData.put("/" + AppConstants.FIREBASE_LOCATION_SHOPPING_LIST_ITEMS + "/" + mListId,
                null);

        /* Do a deep-path update */
        listToRemoveRef.updateChildren(removeListData, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError,
                                   @NonNull DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.e(LOG_TAG, getString(R.string.log_error_updating_data) + databaseError
                            .getMessage());
                }
            }
        });
    }
}
