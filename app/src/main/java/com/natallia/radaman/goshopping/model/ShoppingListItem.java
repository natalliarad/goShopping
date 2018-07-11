package com.natallia.radaman.goshopping.model;

public class ShoppingListItem {
    private String itemName;
    private String author;
    private String boughtBy;
    private boolean bought;

    /**
     * Required public constructor
     */
    public ShoppingListItem() {
    }

    /**
     * Use this constructor to create new ShoppingListItem.
     * Takes shopping list item name and list item author email as params
     */
    public ShoppingListItem(String itemName, String author) {
        this.itemName = itemName;
        this.author = author;
        this.boughtBy = null;
        this.bought = false;

    }

    public String getItemName() {
        return itemName;
    }

    public String getAuthor() {
        return author;
    }

    public String getBoughtBy() {
        return boughtBy;
    }

    public boolean isBought() {
        return bought;
    }

}
