package com.example.prm392_finalproject_productsaleapp_group2.models;

public class Category {
    private int categoryId;
    private String categoryName;
    private String imageUrl;


    public Category() {}

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getImageUrl() {return imageUrl;}
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

