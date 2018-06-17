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

    /**
     * Email is being decoded just once to display real email in AutocompleteFriendAdapter
     */
    public static String decodeEmail(String userEmail) {
        return userEmail.replace(",", ".");
    }

    /**
     * Return true if currentUserEmail equals to shoppingList.owner()
     * Return false otherwise
     */
    public static boolean checkIfAuthor(ShoppingList shoppingList, String currentUserEmail) {
        return (shoppingList.getAuthor() != null &&
                shoppingList.getAuthor().equals(currentUserEmail));
    }

    /**
     * Adds values to a pre-existing HashMap for updating a property for all of the ShoppingList copies.
     * The HashMap can then be used to update the property
     * for all ShoppingList copies.
     *
     * @param listId           The id of the shopping list.
     * @param author           The owner of the shopping list.
     * @param mapToUpdate      The map containing the key, value pairs which will be used
     *                         to update the Firebase database. This MUST be a Hashmap of key
     *                         value pairs who's urls are absolute (i.e. from the root node)
     * @param propertyToUpdate The property to update
     * @param valueToUpdate    The value to update
     * @return The updated HashMap with the new value inserted in all lists
     */
    public static HashMap<String, Object> updateMapForAllWithValue
    (final HashMap<String, User> sharedWith, final String listId,
     final String author, HashMap<String, Object> mapToUpdate,
     String propertyToUpdate, Object valueToUpdate) {

        mapToUpdate.put("/" + AppConstants.FIREBASE_LOCATION_USER_LISTS + "/" + author + "/"
                + listId + "/" + propertyToUpdate, valueToUpdate);
        if (sharedWith != null) {
            for (User user : sharedWith.values()) {
                mapToUpdate.put("/" + AppConstants.FIREBASE_LOCATION_USER_LISTS + "/" + user.getEmail() + "/"
                        + listId + "/" + propertyToUpdate, valueToUpdate);
            }
        }

        return mapToUpdate;
    }

    /**
     * Adds values to a pre-existing HashMap for updating all Last Changed Timestamps for all of
     * the ShoppingList copies. This method uses {@link #updateMapForAllWithValue} to update the
     * last changed timestamp for all ShoppingList copies.
     *
     * @param listId               The id of the shopping list.
     * @param author               The owner of the shopping list.
     * @param mapToAddDateToUpdate The map containing the key, value pairs which will be used
     *                             to update the Firebase database. This MUST be a Hashmap of key
     *                             value pairs who's urls are absolute (i.e. from the root node)
     * @return
     */
    public static HashMap<String, Object> updateMapWithTimestampLastChanged
    (final HashMap<String, User> sharedWith, final String listId,
     final String author, HashMap<String, Object> mapToAddDateToUpdate) {
        /**
         * Set raw version of date to the ServerValue.TIMESTAMP value and save into dateCreatedMap
         */
        HashMap<String, Object> timestampNowHash = new HashMap<>();
        timestampNowHash.put(AppConstants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

        updateMapForAllWithValue(sharedWith, listId, author, mapToAddDateToUpdate,
                AppConstants.FIREBASE_PROPERTY_TIMESTAMP_LAST_CHANGED, timestampNowHash);

        return mapToAddDateToUpdate;
    }
}
