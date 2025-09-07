package com.suppkart.dto.request;

import java.math.BigDecimal;

import com.suppkart.model.enums.PaymentMethod;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PaymentRequest {
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotNull(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency = "INR";
    
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
    
    @Size(max = 255, message = "Customer email cannot exceed 255 characters")
    private String customerEmail;
    
    @Size(max = 20, message = "Customer phone cannot exceed 20 characters")
    private String customerPhone;
    
    @Size(max = 100, message = "Customer name cannot exceed 100 characters")
    private String customerName;
    
    // Additional fields for specific payment methods
    private String upiId; // For UPI payments
    private String vpa; // For UPI payments
    private String cardToken; // For saved card payments
    
    // Callback URLs
    private String successUrl;
    private String failureUrl;
    private String callbackUrl;
    
    // Order reference
    @NotNull(message = "Order ID is required")
    private String orderId;
    
    // Constructors
    public PaymentRequest() {}
    
    public PaymentRequest(PaymentMethod paymentMethod, BigDecimal amount, String orderId) {
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.orderId = orderId;
    }
    
    // Getters and Setters
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCustomerEmail() {
        return customerEmail;
    }
    
    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }
    
    public String getCustomerPhone() {
        return customerPhone;
    }
    
    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public String getUpiId() {
        return upiId;
    }
    
    public void setUpiId(String upiId) {
        this.upiId = upiId;
    }
    
    public String getVpa() {
        return vpa;
    }
    
    public void setVpa(String vpa) {
        this.vpa = vpa;
    }
    
    public String getCardToken() {
        return cardToken;
    }
    
    public void setCardToken(String cardToken) {
        this.cardToken = cardToken;
    }
    
    public String getSuccessUrl() {
        return successUrl;
    }
    
    public void setSuccessUrl(String successUrl) {
        this.successUrl = successUrl;
    }
    
    public String getFailureUrl() {
        return failureUrl;
    }
    
    public void setFailureUrl(String failureUrl) {
        this.failureUrl = failureUrl;
    }
    
    public String getCallbackUrl() {
        return callbackUrl;
    }
    
    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    @Override
    public String toString() {
        return "PaymentRequest{" +
                "paymentMethod=" + paymentMethod +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", orderId='" + orderId + '\'' +
                ", customerEmail='" + customerEmail + '\'' +
                '}';
    }
}
