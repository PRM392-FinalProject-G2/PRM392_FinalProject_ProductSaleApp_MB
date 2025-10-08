package com.example.prm392_finalproject_productsaleapp_group2.models;

public class FilterResponse<T> {
    private int totalItems;
    private java.util.List<T> items;

    // Constructors
    public FilterResponse() {}

    public FilterResponse(int totalItems, java.util.List<T> items) {
        this.totalItems = totalItems;
        this.items = items;
    }

    // Getters and Setters
    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public java.util.List<T> getItems() {
        return items;
    }

    public void setItems(java.util.List<T> items) {
        this.items = items;
    }
}
