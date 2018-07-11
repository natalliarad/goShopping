package com.natallia.radaman.goshopping.ui.listDetails;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.natallia.radaman.goshopping.model.ShoppingList;

import com.natallia.radaman.goshopping.R;
import com.natallia.radaman.goshopping.model.User;
import com.natallia.radaman.goshopping.utils.AppConstants;

public class ListFirebaseAdapter extends FirebaseListAdapter<ShoppingList> {
    Activity mActivity;
    private String mEncodedEmail;

    /**
     * Public constructor that initializes private instance variables when adapter is created
     */
    public ListFirebaseAdapter(FirebaseListOptions<ShoppingList> options, Activity
            activity, String encodedEmail) {
        super(options);
        this.mEncodedEmail = encodedEmail;
        this.mActivity = activity;
    }

    @Override
    protected void populateView(View view, ShoppingList list, int position) {
        /* Grab the needed Textivews and strings */
        TextView textViewListName = view.findViewById(R.id.text_view_list_name);
        final TextView textViewCreatedByUser = view.findViewById(R.id.text_view_created_by_user);
        final TextView textViewUsersShopping = view.findViewById(R.id.text_view_people_shopping_count);

        String authorEmail = list.getAuthor();

        /* Set the list name and author */
        textViewListName.setText(list.getListName());

        /* Show "1 person is shopping" if one person is shopping
         * Show "N people shopping" if two or more users are shopping
         * Show nothing if nobody is shopping */
        if (list.getUsersShopping() != null) {
            int usersShopping = list.getUsersShopping().size();
            if (usersShopping == 1) {
                textViewUsersShopping.setText(String.format(
                        mActivity.getResources().getString(R.string.person_shopping),
                        usersShopping));
            } else {
                textViewUsersShopping.setText(String.format(
                        mActivity.getResources().getString(R.string.people_shopping),
                        usersShopping));
            }
        } else {
            /* otherwise show nothing */
            textViewUsersShopping.setText("");
        }
        /* Set "Created by" text to "You" if current user is author of the list
         * Set "Created by" text to userName if current user is NOT author of the list */
        if (authorEmail != null) {
            if (authorEmail.equals(mEncodedEmail)) {
                textViewCreatedByUser.setText(mActivity.getResources().getString(R.string.text_you));
            } else {
                DatabaseReference userRef = FirebaseDatabase.getInstance()
                        .getReferenceFromUrl(AppConstants.FIREBASE_URL_USERS).child(authorEmail);
                /* Get the user's name */
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            textViewCreatedByUser.setText(user.getName());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(mActivity.getClass().getSimpleName(),
                                mActivity.getString(R.string.log_error_the_read_failed) +
                                        databaseError.getMessage());
                    }
                });
            }
        }
    }
}
