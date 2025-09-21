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
public class InventoryDTO {
    private Long productId;
    private String productName;
    private String productSku;
    private String productImage;
    private Long variantId;
    private String variantName;
    private String variantSku;
    private String sku; // Combined SKU for display
    private Integer quantity;
    private Integer lowStockThreshold;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUpdated;
    
    // Status indicators
    private Boolean isLowStock;
    private Boolean isOutOfStock;
    private Boolean isInStock;
    
    // Category information
    private String categoryName;
    private String brandName;
    
    // Pricing information
    private Double price;
    private Double salePrice;
    
    // Additional metadata
    private String status; // ACTIVE, INACTIVE, DISCONTINUED
    private Integer totalSold; // Total quantity sold
    private Integer reservedQuantity; // Quantity reserved in pending orders
    private Integer availableQuantity; // quantity - reservedQuantity
}
