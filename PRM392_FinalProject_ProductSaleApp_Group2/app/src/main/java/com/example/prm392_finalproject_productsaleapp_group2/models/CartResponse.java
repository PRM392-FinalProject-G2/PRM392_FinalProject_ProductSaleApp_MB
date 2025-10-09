package com.example.prm392_finalproject_productsaleapp_group2.models;

import java.util.List;

public class CartResponse {
    private int pageNumber;
    private int pageSize;
    private int totalItems;
    private int totalPages;
    private List<Cart> items;

    public CartResponse() {}

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public List<Cart> getItems() {
        return items;
    }

    public void setItems(List<Cart> items) {
        this.items = items;
    }
}

