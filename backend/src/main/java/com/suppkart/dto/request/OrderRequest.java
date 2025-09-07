package com.suppkart.dto.request;

import java.math.BigDecimal;

import com.suppkart.model.enums.PaymentMethod;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class OrderRequest {
    
    @NotNull(message = "Address ID is required")
    private Long addressId;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    
    @Size(max = 500, message = "Delivery instructions cannot exceed 500 characters")
    private String deliveryInstructions;
    
    @Size(max = 255, message = "Coupon code cannot exceed 255 characters")
    private String couponCode;
    
    private BigDecimal expectedTotal;
    
    // Constructors
    public OrderRequest() {}
    
    public OrderRequest(Long addressId, PaymentMethod paymentMethod) {
        this.addressId = addressId;
        this.paymentMethod = paymentMethod;
    }
    
    // Getters and Setters
    public Long getAddressId() {
        return addressId;
    }
    
    public void setAddressId(Long addressId) {
        this.addressId = addressId;
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
    
    public BigDecimal getExpectedTotal() {
        return expectedTotal;
    }
    
    public void setExpectedTotal(BigDecimal expectedTotal) {
        this.expectedTotal = expectedTotal;
    }
}
