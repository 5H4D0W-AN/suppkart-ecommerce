package com.suppkart.dto.admin.customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating customer status in admin interface
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerStatusRequest {
    
    /**
     * New status for the customer
     * Valid values: ACTIVE, INACTIVE, SUSPENDED, BANNED
     */
    @NotBlank(message = "Status is required")
    @Size(max = 20, message = "Status must not exceed 20 characters")
    private String status;
    
    /**
     * Reason for status change
     */
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
    
    /**
     * Whether to send notification to customer about status change
     */
    @Builder.Default
    private Boolean sendNotification = false;
    
    /**
     * Whether to suspend all active orders
     * Only applicable when status is SUSPENDED or BANNED
     */
    @Builder.Default
    private Boolean suspendOrders = false;
    
    /**
     * Whether to block future orders
     * Only applicable when status is SUSPENDED or BANNED
     */
    @Builder.Default
    private Boolean blockFutureOrders = false;
    
    /**
     * Admin notes for internal reference
     */
    @Size(max = 1000, message = "Admin notes must not exceed 1000 characters")
    private String adminNotes;
    
    /**
     * Duration of suspension in days (if applicable)
     */
    private Integer suspensionDays;
    
    /**
     * Whether to refund pending orders
     */
    @Builder.Default
    private Boolean refundPendingOrders = false;
    
    // Helper methods
    public boolean isSuspensionStatus() {
        return "SUSPENDED".equalsIgnoreCase(status) || "BANNED".equalsIgnoreCase(status);
    }
    
    public boolean isActiveStatus() {
        return "ACTIVE".equalsIgnoreCase(status);
    }
    
    public boolean shouldNotifyCustomer() {
        return sendNotification != null && sendNotification;
    }
    
    public boolean shouldSuspendOrders() {
        return suspendOrders != null && suspendOrders && isSuspensionStatus();
    }
    
    public boolean shouldBlockFutureOrders() {
        return blockFutureOrders != null && blockFutureOrders && isSuspensionStatus();
    }
    
    public boolean shouldRefundPendingOrders() {
        return refundPendingOrders != null && refundPendingOrders && isSuspensionStatus();
    }
    
    public String getEffectiveReason() {
        if (reason != null && !reason.trim().isEmpty()) {
            return reason.trim();
        }
        return "Status updated by admin";
    }
}
