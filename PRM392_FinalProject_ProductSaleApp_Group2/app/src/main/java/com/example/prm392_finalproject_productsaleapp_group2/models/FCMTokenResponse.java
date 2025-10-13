package com.example.prm392_finalproject_productsaleapp_group2.models;

import com.google.gson.annotations.SerializedName;

public class FCMTokenResponse {
    @SerializedName("tokenId")
    private int tokenId;

    @SerializedName("userId")
    private int userId;

    @SerializedName("fcmToken")
    private String fcmToken;

    @SerializedName("isActive")
    private boolean isActive;

    @SerializedName("lastUpdatedDate")
    private String lastUpdatedDate;

    // Getters and Setters
    public int getTokenId() {
        return tokenId;
    }

    public void setTokenId(int tokenId) {
        this.tokenId = tokenId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(String lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }
}

