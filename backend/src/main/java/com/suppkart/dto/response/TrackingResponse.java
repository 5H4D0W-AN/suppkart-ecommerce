package com.suppkart.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.suppkart.model.enums.OrderStatus;

public class TrackingResponse {
    
    private Long orderId;
    private String orderNumber;
    private OrderStatus orderStatus;
    private String trackingNumber;
    private String carrierName;
    private String carrierUrl;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime estimatedDeliveryDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deliveredAt;
    
    private String currentLocation;
    private String lastUpdateMessage;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUpdated;
    
    private List<TrackingEvent> trackingEvents;
    
    // Constructors
    public TrackingResponse() {}
    
    public TrackingResponse(Long orderId, String orderNumber, OrderStatus orderStatus) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.orderStatus = orderStatus;
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
    
    public String getCarrierUrl() {
        return carrierUrl;
    }
    
    public void setCarrierUrl(String carrierUrl) {
        this.carrierUrl = carrierUrl;
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
    
    public String getCurrentLocation() {
        return currentLocation;
    }
    
    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }
    
    public String getLastUpdateMessage() {
        return lastUpdateMessage;
    }
    
    public void setLastUpdateMessage(String lastUpdateMessage) {
        this.lastUpdateMessage = lastUpdateMessage;
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    public List<TrackingEvent> getTrackingEvents() {
        return trackingEvents;
    }
    
    public void setTrackingEvents(List<TrackingEvent> trackingEvents) {
        this.trackingEvents = trackingEvents;
    }
    
    // Utility methods
    public boolean isDelivered() {
        return orderStatus == OrderStatus.DELIVERED && deliveredAt != null;
    }
    
    public boolean hasTrackingInfo() {
        return trackingNumber != null && !trackingNumber.trim().isEmpty();
    }
    
    // Nested class for tracking events
    public static class TrackingEvent {
        private String status;
        private String location;
        private String description;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime timestamp;
        
        // Constructors
        public TrackingEvent() {}
        
        public TrackingEvent(String status, String location, String description, LocalDateTime timestamp) {
            this.status = status;
            this.location = location;
            this.description = description;
            this.timestamp = timestamp;
        }
        
        // Getters and Setters
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
        
        public String getLocation() {
            return location;
        }
        
        public void setLocation(String location) {
            this.location = location;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }
}
