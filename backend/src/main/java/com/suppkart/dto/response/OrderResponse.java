package com.suppkart.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.suppkart.model.enums.OrderStatus;
import com.suppkart.model.enums.PaymentMethod;
import com.suppkart.model.enums.PaymentStatus;

public class OrderResponse {
    
    private Long orderId;
    private String orderNumber;
    private OrderStatus orderStatus;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String paymentTransactionId;
    
    // Shipping Address
    private String shippingFirstName;
    private String shippingLastName;
    private String shippingAddressLine1;
    private String shippingAddressLine2;
    private String shippingCity;
    private String shippingState;
    private String shippingPostalCode;
    private String shippingCountry;
    private String shippingPhone;
    
    // Financial Details
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal shippingCost;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    
    // Additional Information
    private String deliveryInstructions;
    private String couponCode;
    
    // Tracking Information
    private String trackingNumber;
    private String carrierName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime estimatedDeliveryDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deliveredAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelledAt;
    
    private String cancellationReason;
    
    // Order Items
    private List<OrderItemResponse> orderItems;
    
    // Audit Fields
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // Constructors
    public OrderResponse() {}
    
    public OrderResponse(Long orderId, String orderNumber, OrderStatus orderStatus, 
                        PaymentMethod paymentMethod, BigDecimal totalAmount) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.orderStatus = orderStatus;
        this.paymentMethod = paymentMethod;
        this.totalAmount = totalAmount;
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
    
    public OrderStatus getOrderStatus() {
        return orderStatus;
    }
    
    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
    
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }
    
    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public String getPaymentTransactionId() {
        return paymentTransactionId;
    }
    
    public void setPaymentTransactionId(String paymentTransactionId) {
        this.paymentTransactionId = paymentTransactionId;
    }
    
    public String getShippingFirstName() {
        return shippingFirstName;
    }
    
    public void setShippingFirstName(String shippingFirstName) {
        this.shippingFirstName = shippingFirstName;
    }
    
    public String getShippingLastName() {
        return shippingLastName;
    }
    
    public void setShippingLastName(String shippingLastName) {
        this.shippingLastName = shippingLastName;
    }
    
    public String getShippingAddressLine1() {
        return shippingAddressLine1;
    }
    
    public void setShippingAddressLine1(String shippingAddressLine1) {
        this.shippingAddressLine1 = shippingAddressLine1;
    }
    
    public String getShippingAddressLine2() {
        return shippingAddressLine2;
    }
    
    public void setShippingAddressLine2(String shippingAddressLine2) {
        this.shippingAddressLine2 = shippingAddressLine2;
    }
    
    public String getShippingCity() {
        return shippingCity;
    }
    
    public void setShippingCity(String shippingCity) {
        this.shippingCity = shippingCity;
    }
    
    public String getShippingState() {
        return shippingState;
    }
    
    public void setShippingState(String shippingState) {
        this.shippingState = shippingState;
    }
    
    public String getShippingPostalCode() {
        return shippingPostalCode;
    }
    
    public void setShippingPostalCode(String shippingPostalCode) {
        this.shippingPostalCode = shippingPostalCode;
    }
    
    public String getShippingCountry() {
        return shippingCountry;
    }
    
    public void setShippingCountry(String shippingCountry) {
        this.shippingCountry = shippingCountry;
    }
    
    public String getShippingPhone() {
        return shippingPhone;
    }
    
    public void setShippingPhone(String shippingPhone) {
        this.shippingPhone = shippingPhone;
    }
    
    public BigDecimal getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
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
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
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
    
    public String getTrackingNumber() {
        return trackingNumber;
    }
    
    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }
    
    public String getCarrierName() {
        return carrierName;
    }
    
    public void setCarrierName(String carrierName) {
        this.carrierName = carrierName;
    }
    
    public LocalDateTime getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }
    
    public void setEstimatedDeliveryDate(LocalDateTime estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }
    
    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }
    
    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }
    
    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }
    
    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }
    
    public String getCancellationReason() {
        return cancellationReason;
    }
    
    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
    
    public List<OrderItemResponse> getOrderItems() {
        return orderItems;
    }
    
    public void setOrderItems(List<OrderItemResponse> orderItems) {
        this.orderItems = orderItems;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Utility methods
    public String getShippingFullName() {
        return shippingFirstName + " " + shippingLastName;
    }
    
    public String getShippingFullAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append(shippingAddressLine1);
        if (shippingAddressLine2 != null && !shippingAddressLine2.trim().isEmpty()) {
            sb.append(", ").append(shippingAddressLine2);
        }
        sb.append(", ").append(shippingCity);
        sb.append(", ").append(shippingState);
        sb.append(" ").append(shippingPostalCode);
        sb.append(", ").append(shippingCountry);
        return sb.toString();
    }
    
    public boolean canBeCancelled() {
        return orderStatus == OrderStatus.PENDING || 
               orderStatus == OrderStatus.CONFIRMED || 
               orderStatus == OrderStatus.PROCESSING;
    }
    
    public boolean isDelivered() {
        return orderStatus == OrderStatus.DELIVERED;
    }
    
    public boolean isCancelled() {
        return orderStatus == OrderStatus.CANCELLED;
    }
    
    public int getTotalItemCount() {
        return orderItems != null ? 
            orderItems.stream().mapToInt(OrderItemResponse::getQuantity).sum() : 0;
    }
    
    // Nested class for OrderItem response
    public static class OrderItemResponse {
        private Long orderItemId;
        private String productName;
        private String variantName;
        private String imageUrl;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        
        // Constructors
        public OrderItemResponse() {}
        
        public OrderItemResponse(Long orderItemId, String productName, Integer quantity, 
                               BigDecimal unitPrice, BigDecimal totalPrice) {
            this.orderItemId = orderItemId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.totalPrice = totalPrice;
        }
        
        // Getters and Setters
        public Long getOrderItemId() {
            return orderItemId;
        }
        
        public void setOrderItemId(Long orderItemId) {
            this.orderItemId = orderItemId;
        }
        
        public String getProductName() {
            return productName;
        }
        
        public void setProductName(String productName) {
            this.productName = productName;
        }
        
        public String getVariantName() {
            return variantName;
        }
        
        public void setVariantName(String variantName) {
            this.variantName = variantName;
        }
        
        public String getImageUrl() {
            return imageUrl;
        }
        
        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
        
        public Integer getQuantity() {
            return quantity;
        }
        
        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
        
        public BigDecimal getUnitPrice() {
            return unitPrice;
        }
        
        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }
        
        public BigDecimal getTotalPrice() {
            return totalPrice;
        }
        
        public void setTotalPrice(BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
        }
    }
}
