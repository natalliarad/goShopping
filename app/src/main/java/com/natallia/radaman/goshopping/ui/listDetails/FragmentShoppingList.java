package com.natallia.radaman.goshopping.ui.listDetails;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.natallia.radaman.goshopping.R;
import com.natallia.radaman.goshopping.model.ShoppingList;
import com.natallia.radaman.goshopping.model.ShoppingListItem;
import com.natallia.radaman.goshopping.utils.AppConstants;
import com.natallia.radaman.goshopping.utils.AppUtils;

import java.util.Date;

/**
 * A simple Fragment subclass that shows a list of all shopping lists a user can see.
 * Use the {@link FragmentShoppingList#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentShoppingList extends Fragment {
    private ListFirebaseAdapter mActiveListFirebaseAdapter;
    private ListView mListView;

    public FragmentShoppingList() {
        /* Required empty public constructor */
    }

    /**
     * Create fragment and pass bundle with data as it's arguments
     * Right now there are not arguments...but eventually there will be.
     */
    public static FragmentShoppingList newInstance() {
        FragmentShoppingList fragment = new FragmentShoppingList();
        Bundle args = new Bundle();
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
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /**
         * Initalize UI elements
         */
        View rootView = inflater.inflate(R.layout.fragment_shopping_list, container, false);
        initializeScreen(rootView);

        /**
         * Create Firebase references
         */
        DatabaseReference activeListsRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(AppConstants.FIREBASE_URL_ACTIVE_LISTS);

        Query query = activeListsRef.orderByKey();

        FirebaseListOptions<ShoppingList> options = new FirebaseListOptions.Builder<ShoppingList>()
                .setLayout(R.layout.single_active_list)
                .setQuery(query, ShoppingList.class)
                .setLifecycleOwner(this)
                .build();
        /**
         * Create the adapter, giving it the activity, model class, layout for each row in
         * the list and finally, a reference to the Firebase location with the list data
         */
        mActiveListFirebaseAdapter = new ListFirebaseAdapter(options, getActivity());
        /**
         * Set the adapter to the mListView
         */
        mListView.setAdapter(mActiveListFirebaseAdapter);

        /**
         * Set interactive bits, such as click events and adapters
         */
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
    public void onDestroy() {
        super.onDestroy();
        mActiveListFirebaseAdapter.stopListening();
    }

    /**
     * Link layout elements from XML
     */
    private void initializeScreen(View rootView) {
        mListView = rootView.findViewById(R.id.list_view_active_lists);
    }
}
