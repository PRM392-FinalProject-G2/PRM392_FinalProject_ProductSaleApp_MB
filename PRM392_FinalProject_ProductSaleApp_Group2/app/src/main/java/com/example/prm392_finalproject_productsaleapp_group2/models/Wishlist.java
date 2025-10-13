package com.example.prm392_finalproject_productsaleapp_group2.models;

import com.google.gson.annotations.SerializedName;

public class Wishlist {
    @SerializedName("wishlistId")
    private int wishlistId;
    
    @SerializedName("userId")
    private int userId;
    
    @SerializedName("productId")
    private int productId;
    
    @SerializedName("product")
    private Product product;
    
    @SerializedName("createdAt")
    private String createdAt;

    public Wishlist() {}

    public Wishlist(int userId, int productId) {
        this.userId = userId;
        this.productId = productId;
    }

    public int getWishlistId() {
        return wishlistId;
    }

    public void setWishlistId(int wishlistId) {
        this.wishlistId = wishlistId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}