package com.natallia.radaman.goshopping.ui.listDetails;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.natallia.radaman.goshopping.R;
import com.natallia.radaman.goshopping.model.ShoppingList;
import com.natallia.radaman.goshopping.utils.AppConstants;

/**
 * A simple Fragment subclass that shows a list of all shopping lists a user can see.
 * Use the {@link FragmentShoppingList#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentShoppingList extends Fragment {
    private String mEncodedEmail;
    private ListFirebaseAdapter mActiveListFirebaseAdapter;
    private ListView mListView;

    public FragmentShoppingList() {

    }

    /**
     * Create fragment and pass bundle with data as it's arguments.
     */
    public static FragmentShoppingList newInstance(String encodedEmail) {
        FragmentShoppingList fragment = new FragmentShoppingList();
        Bundle args = new Bundle();
        args.putString(AppConstants.KEY_ENCODED_EMAIL, encodedEmail);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * Initialize instance variables with data from bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mEncodedEmail = getArguments().getString(AppConstants.KEY_ENCODED_EMAIL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /* Initalize UI elements */
        View rootView = inflater.inflate(R.layout.fragment_shopping_list, container, false);
        initializeScreen(rootView);

        /* Set interactive bits, such as click events and adapters */
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ShoppingList selectedList = mActiveListFirebaseAdapter.getItem(position);
                if (selectedList != null) {
                    Intent intent = new Intent(getActivity(), ListDetailsActivity.class);
                    /* Get the list ID using the adapter's get ref method to get the Firebase
                     * ref and then grab the key.
                     */
                    String listId = mActiveListFirebaseAdapter.getRef(position).getKey();
                    intent.putExtra(AppConstants.KEY_LIST_ID, listId);
                    /* Starts an active showing the details for the selected list */
                    startActivity(intent);
                }
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortOrder = sharedPref.getString(AppConstants.KEY_PREF_SORT_ORDER_LISTS,
                AppConstants.ORDER_BY_KEY);
        /* Create Firebase references */
        Query orderedActiveUserListsRef;
        DatabaseReference activeListsRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(AppConstants.FIREBASE_URL_USER_LISTS).child(mEncodedEmail);

        /* Sort active lists by "date created" if it's been selected in the SettingsActivity */
        if (sortOrder.equals(AppConstants.ORDER_BY_KEY)) {
            orderedActiveUserListsRef = activeListsRef.orderByKey();
        } else {
            /* Sort active by lists by name or datelastChanged. Otherwise
             * depending on what's been selected in SettingsActivity */
            orderedActiveUserListsRef = activeListsRef.orderByChild(sortOrder);
        }

        FirebaseListOptions<ShoppingList> options = new FirebaseListOptions.Builder<ShoppingList>()
                .setLayout(R.layout.single_active_list)
                .setQuery(orderedActiveUserListsRef, ShoppingList.class)
                .setLifecycleOwner(this)
                .build();
        /* Create the adapter, giving it the FirebaseListOptions options, activity and user email
         * for each row in the list */
        mActiveListFirebaseAdapter = new ListFirebaseAdapter(options, getActivity(), mEncodedEmail);
        /* Set the adapter to the mListView */
        mListView.setAdapter(mActiveListFirebaseAdapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        mActiveListFirebaseAdapter.stopListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Link layout elements from XML
     */
    private void initializeScreen(View rootView) {
        mListView = rootView.findViewById(R.id.list_view_active_lists);
    }
}
