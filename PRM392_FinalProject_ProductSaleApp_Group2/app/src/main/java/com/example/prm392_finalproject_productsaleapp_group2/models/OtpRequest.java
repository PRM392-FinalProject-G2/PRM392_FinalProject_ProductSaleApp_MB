package com.example.prm392_finalproject_productsaleapp_group2.models;

public class OtpRequest {
    private String email;
    private int userId;

    public OtpRequest(String email, int userId) {
        this.email = email;
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}

