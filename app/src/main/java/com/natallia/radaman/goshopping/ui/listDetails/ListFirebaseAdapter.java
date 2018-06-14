package com.natallia.radaman.goshopping.ui.listDetails;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.natallia.radaman.goshopping.model.ShoppingList;

import com.natallia.radaman.goshopping.R;
import com.natallia.radaman.goshopping.model.ShoppingListItem;

public class ListFirebaseAdapter extends FirebaseListAdapter<ShoppingList> {
    Activity mActivity;

    /**
     * Public constructor that initializes private instance variables when adapter is created
     */
    public ListFirebaseAdapter(FirebaseListOptions<ShoppingList> options, Activity
            activity) {
        super(options);
        this.mActivity = activity;
    }

    @Override
    protected void populateView(View view, ShoppingList list, int position) {
        TextView textViewListName = view.findViewById(R.id.text_view_list_name);
        TextView textViewCreatedByUser = view.findViewById(R.id.text_view_created_by_user);


        /* Set the list name and owner */
        textViewListName.setText(list.getListName());
        textViewCreatedByUser.setText(list.getAuthor());
    }
}
