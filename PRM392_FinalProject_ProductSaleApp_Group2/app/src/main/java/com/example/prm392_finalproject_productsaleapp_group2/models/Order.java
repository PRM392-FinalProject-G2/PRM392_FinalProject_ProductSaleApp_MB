package com.example.prm392_finalproject_productsaleapp_group2.models;

import java.util.List;

public class Order {
    private int orderId;
    private int cartId;
    private int userId;
    private String paymentMethod;
    private String billingAddress;
    private String orderStatus;
    private String orderDate;
    private UserResponse user;
    private Cart cart;
    private List<Payment> payments;

    // Constructors
    public Order() {}

    public Order(int orderId, int userId, int cartId, String orderStatus, String orderDate) {
        this.orderId = orderId;
        this.userId = userId;
        this.cartId = cartId;
        this.orderStatus = orderStatus;
        this.orderDate = orderDate;
    }

    // Getters and Setters
    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCartId() {
        return cartId;
    }

    public void setCartId(int cartId) {
        this.cartId = cartId;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(String billingAddress) {
        this.billingAddress = billingAddress;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }

    // Helper methods
    public String getFormattedTotalAmount() {
        if (cart != null) {
            return String.format("%.0f ₫", cart.getTotalPrice());
        }
        return "0 ₫";
    }

    public String getStatusDisplayText() {
        switch (orderStatus.toLowerCase()) {
            case "pending":
                return "Chờ giao hàng";
            case "delivering":
                return "Đang giao hàng";
            case "success":
                return "Đã giao";
            default:
                return orderStatus;
        }
    }

    public String getStatusBackgroundResource() {
        switch (orderStatus.toLowerCase()) {
            case "pending":
                return "status_pending_background";
            case "delivering":
                return "status_delivering_background";
            case "success":
                return "status_success_background";
            default:
                return "status_pending_background";
        }
    }

    // Get formatted order date
    public String getFormattedOrderDate() {
        if (orderDate != null && !orderDate.isEmpty()) {
            // Simple date formatting - you can improve this later
            return orderDate.substring(0, 10); // Get only date part
        }
        return "";
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", cartId=" + cartId +
                ", userId=" + userId +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", billingAddress='" + billingAddress + '\'' +
                ", orderStatus='" + orderStatus + '\'' +
                ", orderDate='" + orderDate + '\'' +
                '}';
    }
}
