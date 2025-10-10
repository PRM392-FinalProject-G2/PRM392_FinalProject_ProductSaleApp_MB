package com.example.prm392_finalproject_productsaleapp_group2.models;

import com.google.gson.annotations.SerializedName;

public class FCMTokenRequest {
    @SerializedName("userId")
    private int userId;

    @SerializedName("fcmToken")
    private String fcmToken;

    public FCMTokenRequest(int userId, String fcmToken) {
        this.userId = userId;
        this.fcmToken = fcmToken;
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
}

