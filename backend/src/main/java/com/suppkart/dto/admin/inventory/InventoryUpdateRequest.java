package com.suppkart.dto.admin.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryUpdateRequest {
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;
    
    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason; // Optional reason for the update
    
    // Optional threshold update
    @Min(value = 0, message = "Low stock threshold cannot be negative")
    private Integer lowStockThreshold;
    
    // Additional metadata
    private Boolean sendNotification; // Whether to send low stock notification if applicable
    private String notes; // Additional notes for the update
    
    // Validation helper methods
    public String getReason() {
        return reason != null ? reason : "Manual inventory update";
    }
    
    public Boolean getSendNotification() {
        return sendNotification != null ? sendNotification : true;
    }
}
