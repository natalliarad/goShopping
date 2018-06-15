package com.natallia.radaman.goshopping.ui.listSharing;

import android.app.Activity;
import android.view.View;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.database.DataSnapshot;
import com.natallia.radaman.goshopping.model.User;

public class AutocompleteFriendAdapter extends FirebaseListAdapter<User> {
    Activity mActivity;

    /**
     * Public constructor that initializes private instance variables when adapter is created
     */
    public AutocompleteFriendAdapter(FirebaseListOptions<User> options, Activity activity) {
        super(options);
        this.mActivity = activity;
    }

    /**
     * Protected method that populates the view attached to the adapter (list_view_friends_autocomplete)
     * with items inflated from single_autocomplete_item.xml
     * populateView also handles data changes and updates the listView accordingly
     */
    @Override
    protected void populateView(View v, User model, int position) {

    }

    /**
     * Checks if the friend you try to add is the current user
     **/
    private boolean isNotCurrentUser(User user) {
        return true;
    }

    /**
     * Checks if the friend you try to add is already added, given a dataSnapshot of a user
     **/
    private boolean isNotAlreadyAdded(DataSnapshot dataSnapshot, User user) {
        return true;
    }
}
