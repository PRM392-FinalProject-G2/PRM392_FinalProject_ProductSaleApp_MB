package com.example.prm392_finalproject_productsaleapp_group2.models;

import com.google.gson.annotations.SerializedName;

public class ProductImage {
    @SerializedName("imageId")
    private int imageId;
    
    @SerializedName("productId")
    private int productId;
    
    @SerializedName("imageUrl")
    private String imageUrl;
    
    @SerializedName("isPrimary")
    private boolean isPrimary;

    public ProductImage() {}

    public ProductImage(int productId, String imageUrl, boolean isPrimary) {
        this.productId = productId;
        this.imageUrl = imageUrl;
        this.isPrimary = isPrimary;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }
}





