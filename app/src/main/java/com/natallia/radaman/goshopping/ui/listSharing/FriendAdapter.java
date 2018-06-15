package com.natallia.radaman.goshopping.ui.listSharing;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.natallia.radaman.goshopping.model.ShoppingList;
import com.natallia.radaman.goshopping.model.User;

import com.natallia.radaman.goshopping.R;

import java.util.HashMap;

public class FriendAdapter extends FirebaseListAdapter<User> {
    Activity mActivity;
    private static final String LOG_TAG = FriendAdapter.class.getSimpleName();

    /**
     * Public constructor that initializes private instance variables when adapter is created
     */
    public FriendAdapter(FirebaseListOptions<User> options, Activity activity) {
        super(options);
        this.mActivity = activity;
    }

    /**
     * Protected method that populates the view attached to the adapter (list_view_friends_autocomplete)
     * with items inflated from single_user_item.xml
     * populateView also handles data changes and updates the listView accordingly
     */
    @Override
    protected void populateView(View v, User model, int position) {
        ((TextView) v.findViewById(R.id.user_name)).setText(model.getName());
    }

    /**
     * Public method that is used to pass ShoppingList object when it is loaded in ValueEventListener
     */
    public void setShoppingList(ShoppingList shoppingList) {

    }

    /**
     * Public method that is used to pass SharedUsers when they are loaded in ValueEventListener
     */
    public void setSharedWithUsers(HashMap<String, User> sharedUsersList) {

    }

    /**
     * This method does the tricky job of adding or removing a friend from the sharedWith list.
     *
     * @param addFriend           This is true if the friend is being added, false is the friend is being removed.
     * @param friendToAddOrRemove This is the friend to either add or remove
     * @return
     */
    private HashMap<String, Object> updateFriendInSharedWith(Boolean addFriend, User friendToAddOrRemove) {
        return null;
    }
}
