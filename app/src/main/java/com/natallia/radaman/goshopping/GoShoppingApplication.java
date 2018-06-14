package com.natallia.radaman.goshopping;

import com.google.firebase.database.FirebaseDatabase;

public class GoShoppingApplication extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        /* Initialize Firebase */
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
