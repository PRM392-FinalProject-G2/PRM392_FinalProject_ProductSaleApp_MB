package com.example.prm392_finalproject_productsaleapp_group2.models;

public class VerifyOtpResponse {
    private boolean success;
    private String message;
    private String resetToken;
    private int expiresInSeconds;

    public VerifyOtpResponse() {
    }

    public VerifyOtpResponse(boolean success, String message, String resetToken, int expiresInSeconds) {
        this.success = success;
        this.message = message;
        this.resetToken = resetToken;
        this.expiresInSeconds = expiresInSeconds;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public int getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public void setExpiresInSeconds(int expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }
}

