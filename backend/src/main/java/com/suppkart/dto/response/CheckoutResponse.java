package com.suppkart.dto.response;

import java.math.BigDecimal;

import com.suppkart.model.enums.PaymentMethod;

public class CheckoutResponse {
    
    private Long orderId;
    private String orderNumber;
    private PaymentMethod paymentMethod;
    private BigDecimal totalAmount;
    private String paymentUrl;
    private String paymentTransactionId;
    private String message;
    private boolean success;
    
    // For payment processing
    private String razorpayOrderId;
    private String razorpayKeyId;
    private String upiQrCode;
    private String upiDeepLink;
    
    // Constructors
    public CheckoutResponse() {}
    
    public CheckoutResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public CheckoutResponse(Long orderId, String orderNumber, PaymentMethod paymentMethod, 
                           BigDecimal totalAmount) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.paymentMethod = paymentMethod;
        this.totalAmount = totalAmount;
        this.success = true;
    }
    
    // Static factory methods
    public static CheckoutResponse success(Long orderId, String orderNumber, 
                                         PaymentMethod paymentMethod, BigDecimal totalAmount) {
        CheckoutResponse response = new CheckoutResponse(orderId, orderNumber, paymentMethod, totalAmount);
        response.setMessage("Order created successfully");
        return response;
    }
    
    public static CheckoutResponse failure(String message) {
        return new CheckoutResponse(false, message);
    }
    
    public static CheckoutResponse paymentRequired(Long orderId, String orderNumber, 
                                                 PaymentMethod paymentMethod, BigDecimal totalAmount, 
                                                 String paymentUrl) {
        CheckoutResponse response = new CheckoutResponse(orderId, orderNumber, paymentMethod, totalAmount);
        response.setPaymentUrl(paymentUrl);
        response.setMessage("Payment required to complete order");
        return response;
    }
    
    // Getters and Setters
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    
    public String getOrderNumber() {
        return orderNumber;
    }
    
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
    
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public String getPaymentUrl() {
        return paymentUrl;
    }
    
    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }
    
    public String getPaymentTransactionId() {
        return paymentTransactionId;
    }
    
    public void setPaymentTransactionId(String paymentTransactionId) {
        this.paymentTransactionId = paymentTransactionId;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }
    
    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId;
    }
    
    public String getRazorpayKeyId() {
        return razorpayKeyId;
    }
    
    public void setRazorpayKeyId(String razorpayKeyId) {
        this.razorpayKeyId = razorpayKeyId;
    }
    
    public String getUpiQrCode() {
        return upiQrCode;
    }
    
    public void setUpiQrCode(String upiQrCode) {
        this.upiQrCode = upiQrCode;
    }
    
    public String getUpiDeepLink() {
        return upiDeepLink;
    }
    
    public void setUpiDeepLink(String upiDeepLink) {
        this.upiDeepLink = upiDeepLink;
    }
}
