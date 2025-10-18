package com.example.prm392_finalproject_productsaleapp_group2.models;

public class PaymentCreateResponse {
    private int orderId;
    private int paymentId;
    private long originalAmount;
    private long voucherDiscount;
    private long finalAmount;
    private String paymentUrl;

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }
    public long getOriginalAmount() { return originalAmount; }
    public void setOriginalAmount(long originalAmount) { this.originalAmount = originalAmount; }
    public long getVoucherDiscount() { return voucherDiscount; }
    public void setVoucherDiscount(long voucherDiscount) { this.voucherDiscount = voucherDiscount; }
    public long getFinalAmount() { return finalAmount; }
    public void setFinalAmount(long finalAmount) { this.finalAmount = finalAmount; }
    public String getPaymentUrl() { return paymentUrl; }
    public void setPaymentUrl(String paymentUrl) { this.paymentUrl = paymentUrl; }
}











