package com.example.prm392_finalproject_productsaleapp_group2.models;

import com.google.gson.annotations.SerializedName;

public class UserResponse {
    @SerializedName("userId")
    private int userId;
    
    @SerializedName("username")
    private String username;
    
    @SerializedName("avatarurl")
    private String avatarUrl;
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("phoneNumber")
    private String phone;
    
    @SerializedName("address")
    private String address;
    
    @SerializedName("role")
    private String role;

    // Constructors
    public UserResponse() {}

    public UserResponse(int userId, String username, String avatarUrl, String email, String phone, String address) {
        this.userId = userId;
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    // Getters and Setters
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

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
