package com.natallia.radaman.goshopping.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.firebase.database.ServerValue;
import com.natallia.radaman.goshopping.utils.AppConstants;

import java.util.HashMap;

public class ShoppingList {
    private String listName;
    private String author;
    private HashMap<String, Object> timestampLastChanged;
    private HashMap<String, Object> timestampCreated;
    private HashMap<String, User> usersShopping;

    /**
     * Required public constructor
     */
    public ShoppingList() {
    }

    /**
     * Use this constructor to create new ShoppingLists.
     * Takes shopping list listName and owner. Set's the last
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
        this.usersShopping = new HashMap<>();
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

    @JsonIgnore
    public long getTimestampLastChangedLong() {

        return (long) timestampLastChanged.get(AppConstants.FIREBASE_PROPERTY_TIMESTAMP);
    }

    @JsonIgnore
    public long getTimestampCreatedLong() {
        return (long) timestampLastChanged.get(AppConstants.FIREBASE_PROPERTY_TIMESTAMP);
    }

    public HashMap getUsersShopping() {
        return usersShopping;
    }

    public void setTimestampLastChangedToNow() {
        HashMap<String, Object> timestampNowObject = new HashMap<String, Object>();
        timestampNowObject.put(AppConstants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);
        this.timestampLastChanged = timestampNowObject;
    }
}
