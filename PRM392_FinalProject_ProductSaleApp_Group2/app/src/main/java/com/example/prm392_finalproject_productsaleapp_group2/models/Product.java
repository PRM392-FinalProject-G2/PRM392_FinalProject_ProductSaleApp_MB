package com.example.prm392_finalproject_productsaleapp_group2.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Product {
    @SerializedName("productId")
    private int productId;
    
    @SerializedName("productName")
    private String productName;
    
    @SerializedName("briefDescription")
    private String briefDescription;
    
    @SerializedName("fullDescription")
    private String fullDescription;
    
    @SerializedName("technicalSpecifications")
    private String technicalSpecifications;
    
    @SerializedName("price")
    private int price;
    
    @SerializedName("categoryId")
    private int categoryId;
    
    @SerializedName("brandId")
    private int brandId;
    
    @SerializedName("popularity")
    private int popularity;
    
    @SerializedName("averageRating")
    private double averageRating;
    
    @SerializedName("reviewCount")
    private int reviewCount;
    
    @SerializedName("category")
    private Category category;
    
    @SerializedName("brand")
    private Brand brand;
    
    @SerializedName("productImages")
    private List<ProductImage> productImages;

    @SerializedName("productReviews") // tên này phải trùng với JSON trả về từ API
    private List<ProductReview> reviewList;
    
    // Legacy field for backward compatibility
    private String imageUrl;

    public Product() {}

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

    public String getFullDescription() {
        return fullDescription;
    }

    public void setFullDescription(String fullDescription) {
        this.fullDescription = fullDescription;
    }

    public String getTechnicalSpecifications() {
        return technicalSpecifications;
    }

    public void setTechnicalSpecifications(String technicalSpecifications) {
        this.technicalSpecifications = technicalSpecifications;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getBrandId() {
        return brandId;
    }

    public void setBrandId(int brandId) {
        this.brandId = brandId;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    public List<ProductImage> getProductImages() {
        return productImages;
    }

    public void setProductImages(List<ProductImage> productImages) {
        this.productImages = productImages;
    }

    public int getPopularity() {
        return popularity;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }
    public List<ProductReview> getReviewList() {
        return reviewList;
    }

    public void setReviewList(List<ProductReview> reviewList) {
        this.reviewList = reviewList;
    }

}

