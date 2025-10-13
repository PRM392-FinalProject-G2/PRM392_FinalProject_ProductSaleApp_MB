package com.example.prm392_finalproject_productsaleapp_group2.models;

import com.google.gson.annotations.SerializedName;

public class WishlistMobile {
    @SerializedName("wishlistId")
    private int wishlistId;
    
    @SerializedName("productId")
    private int productId;
    
    @SerializedName("productName")
    private String productName;
    
    @SerializedName("briefDescription")
    private String briefDescription;
    
    @SerializedName("price")
    private int price;
    
    @SerializedName("primaryImageUrl")
    private String primaryImageUrl;
    
    @SerializedName("createdAt")
    private String createdAt;

    public WishlistMobile() {}

    // Getters and Setters
    public int getWishlistId() {
        return wishlistId;
    }

    public void setWishlistId(int wishlistId) {
        this.wishlistId = wishlistId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getBriefDescription() {
        return briefDescription;
    }

    public void setBriefDescription(String briefDescription) {
        this.briefDescription = briefDescription;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getPrimaryImageUrl() {
        return primaryImageUrl;
    }

    public void setPrimaryImageUrl(String primaryImageUrl) {
        this.primaryImageUrl = primaryImageUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
