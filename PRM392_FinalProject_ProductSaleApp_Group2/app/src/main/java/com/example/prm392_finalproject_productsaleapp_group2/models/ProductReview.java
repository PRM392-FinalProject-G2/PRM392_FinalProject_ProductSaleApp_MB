package com.example.prm392_finalproject_productsaleapp_group2.models;

import com.google.gson.annotations.SerializedName;

public class ProductReview {

    @SerializedName("reviewId")
    private int reviewId;

    @SerializedName("productId")
    private int productId;

    @SerializedName("userId")
    private int userId;

    @SerializedName("username")
    private String username;

    @SerializedName("userAvatarUrl")
    private String userAvatarUrl;

    @SerializedName("rating")
    private int rating;

    @SerializedName("comment")
    private String comment;

    @SerializedName("createdAt")
    private String createdAt;

    // =================== Getter & Setter ===================
    public int getReviewId() {
        return reviewId;
    }

    public void setReviewId(int reviewId) {
        this.reviewId = reviewId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserAvatarUrl() {
        return userAvatarUrl;
    }

    public void setUserAvatarUrl(String userAvatarUrl) {
        this.userAvatarUrl = userAvatarUrl;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
