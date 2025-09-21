package com.suppkart.dto.admin.inventory;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Stock Alert information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAlertDTO {
    
    private Long id;
    
    private Long productId;
    
    private String productName;
    
    private String productSku;
    
    private String productImage;
    
    private Long variantId;
    
    private String variantName;
    
    private String variantSku;
    
    private String alertType;
    
    private Integer threshold;
    
    private Integer currentStock;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime resolvedAt;
    
    private Boolean isResolved;
    
    private Boolean notificationSent;
    
    // Additional fields for better display
    private String categoryName;
    
    private String brandName;
    
    private String status;
    
    private String priority;
    
    private Integer daysActive;
    
    private String resolvedBy;
    
    private String resolvedByName;
    
    private String resolutionNotes;
    
    // Helper methods
    public String getAlertTypeDisplay() {
        if (alertType == null) return "";
        return switch (alertType) {
            case "LOW_STOCK" -> "Low Stock";
            case "OUT_OF_STOCK" -> "Out of Stock";
            default -> alertType;
        };
    }
    
    public String getPriorityLevel() {
        if ("OUT_OF_STOCK".equals(alertType)) {
            return "CRITICAL";
        } else if ("LOW_STOCK".equals(alertType)) {
            if (currentStock != null && threshold != null) {
                double ratio = (double) currentStock / threshold;
                if (ratio <= 0.5) {
                    return "HIGH";
                } else {
                    return "MEDIUM";
                }
            }
            return "MEDIUM";
        }
        return "LOW";
    }
    
    public String getStockStatus() {
        if (currentStock == null) return "UNKNOWN";
        if (currentStock == 0) return "OUT_OF_STOCK";
        if (threshold != null && currentStock <= threshold) return "LOW_STOCK";
        return "IN_STOCK";
    }
    
    public Boolean getIsActive() {
        return !Boolean.TRUE.equals(isResolved);
    }
    
    public Boolean getIsCritical() {
        return "OUT_OF_STOCK".equals(alertType) || 
               (currentStock != null && currentStock == 0);
    }
    
    public String getDisplayName() {
        StringBuilder name = new StringBuilder();
        name.append(productName != null ? productName : "Unknown Product");
        if (variantName != null && !variantName.trim().isEmpty()) {
            name.append(" - ").append(variantName);
        }
        return name.toString();
    }
    
    public String getSkuDisplay() {
        if (variantSku != null && !variantSku.trim().isEmpty()) {
            return variantSku;
        }
        return productSku != null ? productSku : "";
    }
    
    public Integer getStockDeficit() {
        if (threshold == null || currentStock == null) return null;
        return Math.max(0, threshold - currentStock);
    }
    
    public String getAlertDescription() {
        String displayName = getDisplayName();
        String skuDisplay = getSkuDisplay();
        
        if ("OUT_OF_STOCK".equals(alertType)) {
            return String.format("%s (%s) is out of stock", displayName, skuDisplay);
        } else if ("LOW_STOCK".equals(alertType)) {
            return String.format("%s (%s) is running low - %d remaining (threshold: %d)", 
                    displayName, skuDisplay, currentStock, threshold);
        }
        return String.format("Stock alert for %s (%s)", displayName, skuDisplay);
    }
}
