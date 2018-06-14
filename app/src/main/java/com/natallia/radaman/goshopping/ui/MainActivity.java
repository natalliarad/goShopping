package com.natallia.radaman.goshopping.ui;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.natallia.radaman.goshopping.R;
import com.natallia.radaman.goshopping.model.User;
import com.natallia.radaman.goshopping.ui.listAct.FragmentAddListDialog;
import com.natallia.radaman.goshopping.ui.listDetails.FragmentShoppingList;
import com.natallia.radaman.goshopping.ui.products.FragmentAddProductDialog;
import com.natallia.radaman.goshopping.ui.products.FragmentProduct;
import com.natallia.radaman.goshopping.utils.AppConstants;

/**
 * Represents the home screen of the app which
 * has a ViewPager with ShoppingListsFragment and MealsFragment
 */
public class MainActivity extends BaseActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private DatabaseReference mUserRef;
    private ValueEventListener mUserRefListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /**
         * Create Firebase references
         */
        mUserRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(AppConstants.FIREBASE_URL_USERS).child(mEncodedEmail);
        /**
         * Link layout elements from XML and setup the toolbar
         */
        initializeScreen();
        /**
         * Add ValueEventListeners to Firebase references
         * to control get data and control behavior and visibility of elements
         */
        mUserRefListener = mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                /**
                 * Set the activity title to current user name if user is not null
                 */
                if (user != null) {
                    /* Assumes that the first word in the user's name is the user's first name. */
                    String firstName = user.getName().split("\\s+")[0];
                    String title = firstName + "'s Lists";
                    setTitle(title);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(LOG_TAG, getString(R.string.log_error_the_read_failed) +
                        databaseError.getMessage());
            }
        });
    }

    /**
     * Override onOptionsItemSelected to use main_menu instead of BaseActivity menu
     *
     * @param menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu; this adds items to the action bar if it is present. */
        getMenuInflater().inflate(R.menu.menu_base, menu);
        return true;
    }

    /**
     * Override onOptionsItemSelected to add action_settings only to the MainActivity
     *
     * @param item
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mUserRef.removeEventListener(mUserRefListener);
    }

    /**
     * Link layout elements from XML and setup the toolbar
     */
    public void initializeScreen() {
        ViewPager viewPager = findViewById(R.id.pager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        Toolbar toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        /**
         * Create SectionPagerAdapter, set it as adapter to viewPager with setOffscreenPageLimit(2)
         **/
        SectionPagerAdapter adapter = new SectionPagerAdapter(getSupportFragmentManager());
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(adapter);
        /**
         * Setup the mTabLayout with view pager
         */
        tabLayout.setupWithViewPager(viewPager);
    }

    /**
     * Create an instance of the AddList dialog fragment and show it
     */
    public void showAddListDialog(View view) {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = FragmentAddListDialog.newInstance(mEncodedEmail);
        dialog.show(MainActivity.this.getFragmentManager(), "FragmentAddListDialog");
    }

    /**
     * Create an instance of the AddProduct dialog fragment and show it
     */
    public void showAddProductDialog(View view) {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = FragmentAddProductDialog.newInstance();
        dialog.show(MainActivity.this.getFragmentManager(), "FragmentAddProductDialog");
    }

    /**
     * SectionPagerAdapter class that extends FragmentStatePagerAdapter to save fragments state
     */
    public class SectionPagerAdapter extends FragmentStatePagerAdapter {

        public SectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Use positions (0 and 1) to find and instantiate fragments with newInstance()
         *
         * @param position
         */
        @Override
        public Fragment getItem(int position) {

            Fragment fragment = null;
            /**
             * Set fragment to different fragments depending on position in ViewPager
             */
            switch (position) {
                case 0:
                    fragment = FragmentShoppingList.newInstance();
                    break;
                case 1:
                    fragment = FragmentProduct.newInstance();
                    break;
                default:
                    fragment = FragmentShoppingList.newInstance();
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        /**
         * Set string resources as titles for each fragment by it's position
         *
         * @param position
         */
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.title_shopping_lists_pager);
                case 1:
                default:
                    return getString(R.string.title_product_pager);
            }
        }
    }
}
