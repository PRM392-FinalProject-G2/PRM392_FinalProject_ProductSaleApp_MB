package com.example.prm392_finalproject_productsaleapp_group2.models;

public class Payment {
    private int paymentId;
    private int orderId;
    private double amount;
    private String paymentStatus;
    private String paymentDate;

    // Constructors
    public Payment() {}

    public Payment(int paymentId, int orderId, double amount, String paymentStatus, String paymentDate) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.paymentDate = paymentDate;
    }

    // Getters and Setters
    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    // Helper methods
    public String getFormattedAmount() {
        return String.format("%.0f ₫", amount);
    }
}
