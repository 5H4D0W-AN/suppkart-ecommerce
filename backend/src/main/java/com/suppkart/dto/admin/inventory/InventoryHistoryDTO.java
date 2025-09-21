package com.suppkart.dto.admin.inventory;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryHistoryDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private String productImage;
    private Long variantId;
    private String variantName;
    private String variantSku;
    private Integer previousQuantity;
    private Integer newQuantity;
    private Integer quantityChange; // newQuantity - previousQuantity
    private String changeType; // STOCK_ADJUSTMENT, PURCHASE, SALE, RETURN
    private String reason;
    private String updatedBy; // User who made the change
    private String updatedByName; // Full name of the user
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // Additional metadata
    private String referenceNumber; // Order number, PO number, etc.
    private String supplierName; // For purchases
    private String customerName; // For sales/returns
    private Double unitCost; // Cost per unit for purchases
    private Double unitPrice; // Sale price per unit for sales
    private Double totalValue; // Total value of the change
    
    // Status indicators
    private Boolean isIncrease;
    private Boolean isDecrease;
    private String changeTypeDisplay; // Human-readable change type
    private String changeDescription; // Detailed description
    
    // Helper methods for display
    public Boolean getIsIncrease() {
        return newQuantity > previousQuantity;
    }
    
    public Boolean getIsDecrease() {
        return newQuantity < previousQuantity;
    }
    
    public Integer getQuantityChange() {
        return newQuantity - previousQuantity;
    }
    
    public String getChangeTypeDisplay() {
        if (changeType == null) return "Unknown";
        
        switch (changeType.toUpperCase()) {
            case "PURCHASE":
                return "Purchase";
            case "SALE":
                return "Sale";
            case "RETURN":
                return "Return";
            case "STOCK_ADJUSTMENT":
                return "Stock Adjustment";
            default:
                return changeType;
        }
    }
    
    public String getChangeDescription() {
        int change = getQuantityChange();
        String direction = change > 0 ? "increased" : "decreased";
        String absChange = String.valueOf(Math.abs(change));
        
        return String.format("Stock %s by %s units (%s → %s)", 
                           direction, absChange, previousQuantity, newQuantity);
    }
}
