package com.suppkart.dto.admin.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryFilterRequest {
    private String search; // Search by product name, SKU, or variant name
    private Long categoryId; // Filter by category
    private String brandName; // Filter by brand
    private Boolean lowStock; // Show only low stock items
    private Boolean outOfStock; // Show only out of stock items
    private Boolean inStock; // Show only in stock items
    private String status; // Filter by product status (ACTIVE, INACTIVE, DISCONTINUED)
    private String sortBy; // Sort field (quantity, lastUpdated, productName, etc.)
    private String sortDirection; // Sort direction (ASC, DESC)
    
    // Additional filters
    private Integer minQuantity; // Minimum quantity filter
    private Integer maxQuantity; // Maximum quantity filter
    private Double minPrice; // Minimum price filter
    private Double maxPrice; // Maximum price filter
    
    // Pagination parameters
    private Integer page; // Page number (0-based)
    private Integer size; // Page size
    
    // Default values
    public String getSortBy() {
        return sortBy != null ? sortBy : "lastUpdated";
    }
    
    public String getSortDirection() {
        return sortDirection != null ? sortDirection : "DESC";
    }
    
    public Integer getPage() {
        return page != null ? page : 0;
    }
    
    public Integer getSize() {
        return size != null ? size : 20;
    }
}
