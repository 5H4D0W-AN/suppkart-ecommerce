package com.suppkart.dto.request;

import java.math.BigDecimal;

import com.suppkart.model.enums.PaymentMethod;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CheckoutRequest {
    
    @NotNull(message = "Shipping address ID is required")
    private Long shippingAddressId;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    
    @Size(max = 500, message = "Delivery instructions must not exceed 500 characters")
    private String deliveryInstructions;
    
    @Size(max = 255, message = "Coupon code must not exceed 255 characters")
    private String couponCode;
    
    @DecimalMin(value = "0.0", message = "Tax amount must be non-negative")
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.0", message = "Shipping cost must be non-negative")
    private BigDecimal shippingCost = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.0", message = "Discount amount must be non-negative")
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    // Payment-specific fields
    private String upiId; // For UPI payments
    private String phoneNumber; // For PhonePe payments
    
    // Constructors
    public CheckoutRequest() {}
    
    public CheckoutRequest(Long shippingAddressId, PaymentMethod paymentMethod) {
        this.shippingAddressId = shippingAddressId;
        this.paymentMethod = paymentMethod;
    }
    
    // Getters and Setters
    public Long getShippingAddressId() {
        return shippingAddressId;
    }
    
    public void setShippingAddressId(Long shippingAddressId) {
        this.shippingAddressId = shippingAddressId;
    }
    
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getDeliveryInstructions() {
        return deliveryInstructions;
    }
    
    public void setDeliveryInstructions(String deliveryInstructions) {
        this.deliveryInstructions = deliveryInstructions;
    }
    
    public String getCouponCode() {
        return couponCode;
    }
    
    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }
    
    public BigDecimal getTaxAmount() {
        return taxAmount;
    }
    
    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }
    
    public BigDecimal getShippingCost() {
        return shippingCost;
    }
    
    public void setShippingCost(BigDecimal shippingCost) {
        this.shippingCost = shippingCost;
    }
    
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }
    
    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }
    
    public String getUpiId() {
        return upiId;
    }
    
    public void setUpiId(String upiId) {
        this.upiId = upiId;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
