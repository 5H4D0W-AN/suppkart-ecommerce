package com.suppkart.dto.admin.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustmentRequest {
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    private Long variantId; // Optional for product variants
    
    @NotNull(message = "Previous quantity is required")
    @Min(value = 0, message = "Previous quantity cannot be negative")
    private Integer previousQuantity;
    
    @NotNull(message = "New quantity is required")
    @Min(value = 0, message = "New quantity cannot be negative")
    private Integer newQuantity;
    
    @NotNull(message = "Change type is required")
    private String changeType; // STOCK_ADJUSTMENT, PURCHASE, SALE, RETURN
    
    @Size(max = 1000, message = "Reason cannot exceed 1000 characters")
    private String reason; // Detailed reason for the adjustment
    
    // Additional metadata
    private String referenceNumber; // Purchase order, sale order, return number, etc.
    private String supplierName; // For purchases
    private String customerName; // For sales/returns
    private Double unitCost; // Cost per unit for purchases
    private Double unitPrice; // Sale price per unit for sales
    
    // Validation and helper methods
    public Integer getQuantityChange() {
        return newQuantity - previousQuantity;
    }
    
    public boolean isIncrease() {
        return newQuantity > previousQuantity;
    }
    
    public boolean isDecrease() {
        return newQuantity < previousQuantity;
    }
    
    public String getReason() {
        if (reason != null && !reason.trim().isEmpty()) {
            return reason;
        }
        
        // Generate default reason based on change type
        switch (changeType.toUpperCase()) {
            case "PURCHASE":
                return "Stock increased due to purchase" + 
                       (referenceNumber != null ? " (PO: " + referenceNumber + ")" : "");
            case "SALE":
                return "Stock decreased due to sale" + 
                       (referenceNumber != null ? " (Order: " + referenceNumber + ")" : "");
            case "RETURN":
                return "Stock adjusted due to return" + 
                       (referenceNumber != null ? " (Return: " + referenceNumber + ")" : "");
            case "STOCK_ADJUSTMENT":
            default:
                return "Manual stock adjustment";
        }
    }
}
