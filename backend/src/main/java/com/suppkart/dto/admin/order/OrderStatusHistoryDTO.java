package com.suppkart.dto.admin.order;

import java.time.LocalDateTime;

/**
 * DTO for order status history in admin order details
 */
public class OrderStatusHistoryDTO {
    
    private String status;
    private LocalDateTime timestamp;
    private String comment;
    private String updatedBy;

    // Constructors
    public OrderStatusHistoryDTO() {}

    public OrderStatusHistoryDTO(String status, LocalDateTime timestamp, String comment, String updatedBy) {
        this.status = status;
        this.timestamp = timestamp;
        this.comment = comment;
        this.updatedBy = updatedBy;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
