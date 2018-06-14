package com.natallia.radaman.goshopping.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.FirebaseError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.natallia.radaman.goshopping.model.ShoppingList;
import com.natallia.radaman.goshopping.model.User;

import java.text.SimpleDateFormat;
import java.util.HashMap;

public class AppUtils {
    /**
     * Format the timestamp with SimpleDateFormat
     */
    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private Context mContext = null;

    /**
     * Public constructor that takes mContext for later use
     */
    public AppUtils(Context con) {
        mContext = con;
    }

    /**
     * Encode user email to use it as a Firebase key (Firebase does not allow "." in the key name)
     * Encoded email is also used as "userEmail", list and item "owner" value
     */
    public static String encodeEmail(String userEmail) {
        return userEmail.replace(".", ",");
    }
}
