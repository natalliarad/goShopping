package com.natallia.radaman.goshopping.model;

import com.google.firebase.database.ServerValue;
import com.natallia.radaman.goshopping.utils.AppConstants;

import java.util.HashMap;

public class ShoppingList {
    private String listName;
    private String author;
    private HashMap<String, Object> timestampLastChanged;
    private HashMap<String, Object> timestampCreated;

    /**
     * Required public constructor
     */
    public ShoppingList() {
    }

    /**
     * Use this constructor to create new ShoppingLists.
     * Takes shopping list listName and author. Set's the last
     * changed time to what is stored in ServerValue.TIMESTAMP
     *
     * @param listName
     * @param author
     */
    public ShoppingList(String listName, String author, HashMap<String, Object> timestampCreated) {
        this.listName = listName;
        this.author = author;
        this.timestampCreated = timestampCreated;
        HashMap<String, Object> timestampNowObject = new HashMap<String, Object>();
        timestampNowObject.put(AppConstants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);
        this.timestampLastChanged = timestampNowObject;
    }

    public String getListName() {
        return listName;
    }

    public String getAuthor() {
        return author;
    }

    public HashMap<String, Object> getTimestampLastChanged() {
        return timestampLastChanged;
    }

    public HashMap<String, Object> getTimestampCreated() {
        return timestampCreated;
    }

    public long getTimestampLastChangedLong() {

        return 222111;
        //(long) timestampLastChanged.get(AppConstants.FIREBASE_PROPERTY_TIMESTAMP)
    }

    public long getTimestampCreatedLong() {
        return 111222;
        //(long) timestampLastChanged.get(AppConstants.FIREBASE_PROPERTY_TIMESTAMP)
    }
}
