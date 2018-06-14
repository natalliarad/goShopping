package com.natallia.radaman.goshopping.model;

public class ShoppingListItem {
    private String itemName;
    private String author;

    /**
     * Required public constructor
     */
    public ShoppingListItem() {
    }

    /**
     * Use this constructor to create new ShoppingListItem.
     * Takes shopping list item name and list item owner email as params
     *
     * @param itemName
     * @param author
     */
    public ShoppingListItem(String itemName, String author) {
        this.itemName = itemName;
        this.author = author;
    }

    public String getItemName() {
        return itemName;
    }

    public String getAuthor() {
        return author;
    }
}
